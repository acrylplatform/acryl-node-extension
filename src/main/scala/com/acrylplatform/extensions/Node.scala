package com.acrylplatform.extensions

import java.text.DecimalFormat

import com.acrylplatform.account.{AddressOrAlias, PublicKey}
import com.acrylplatform.extensions.node.Settings
import com.acrylplatform.extensions.{Context => ExtensionContext}
import com.acrylplatform.transaction.Asset.Acryl
import com.acrylplatform.transaction.lease.{LeaseCancelTransaction, LeaseTransaction}
import com.acrylplatform.transaction.transfer.{MassTransferTransaction, TransferTransaction}
import com.acrylplatform.utils.ScorexLogging
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import net.ceedubs.ficus.Ficus._
import scalaj.http.Http

import scala.concurrent.Future

class Node(context: ExtensionContext) extends Extension with ScorexLogging {

  val minerPublicKey: PublicKey = context.wallet.privateKeyAccounts.head.publicKey
  private[this] val settings    = context.settings.config.as[Settings]("node-extension")
  var lastKnownHeight           = 0

  def acryl(acrylets: Long): String =
    new DecimalFormat("###,###.########")
      .format((BigDecimal(acrylets) / 100000000).doubleValue())

  def blockUrl(height: Int): String = settings.blockUrl.replaceAll("%s", height.toString)

  def sendNotification(text: String): Unit = {
    Http(settings.webhook.url)
      .headers(settings.webhook.headers.flatMap(s =>
        s.split(":") match {
          case Array(a, b) =>
            Seq((a.trim, b.trim))
          case _ =>
            log.error(s"""Can't parse "$s" header! Please check "webhook.headers" config. Its values must be in the "name: value" format""")
            Seq()
      }))
      .postData(settings.webhook.body.replaceAll("%s", text))
      .method(settings.webhook.method)
      .asString
  }

  def info(message: String): Unit = {
    log.info(message)
    sendNotification(message)
  }

  def warn(message: String): Unit = {
    log.warn(message)
    sendNotification(message)
  }

  def err(message: String): Unit = {
    log.error(message)
    sendNotification(message)
  }

  implicit class IsMiner(a: AddressOrAlias) {
    def isMiner: Boolean =
      context.blockchain.resolveAlias(a).right.get.stringRepr == minerPublicKey.toAddress.stringRepr
  }

  def checkNextBlock(): Unit = {
    val height = context.blockchain.height
    if (height == lastKnownHeight + 1) { // otherwise, most likely, the node is not yet synchronized
      val block = context.blockchain.blockAt(lastKnownHeight).get

      if (settings.notifications.leasing) {
        val leased = block.transactionData.collect {
          case l: LeaseTransaction =>
            if (l.recipient isMiner)
              l.amount
            else 0
        }.sum
        val leaseCanceled = block.transactionData.collect {
          case l: LeaseCancelTransaction =>
            val lease = context.blockchain.leaseDetails(l.leaseId).get
            if (lease.recipient isMiner)
              lease.amount
            else 0
        }.sum

        if (leased != leaseCanceled)
          info(
            s"Leasing amount was ${if (leased > leaseCanceled) "increased" else "decreased"}" +
              s" by ${acryl(Math.abs(leased - leaseCanceled))} Acryl at ${blockUrl(lastKnownHeight)}")
      }

      if (settings.notifications.acrylReceived) {
        val acrylReceived = block.transactionData.collect {
          case mt: MassTransferTransaction if mt.assetId == Acryl =>
            mt.transfers.collect {
              case t if t.address.isMiner => t.amount
            }.sum
          case t: TransferTransaction if t.assetId == Acryl && t.recipient.isMiner => t.amount
        }.sum
        if (acrylReceived > 0)
          info(s"Received ${acryl(acrylReceived)} Acryl at ${blockUrl(lastKnownHeight)}")
      }

      if (minerPublicKey == block.getHeader.signerData.generator) {
        val blockFee     = context.blockchain.totalFee(lastKnownHeight).get
        val prevBlockFee = context.blockchain.totalFee(lastKnownHeight - 1).get
        val reward       = (prevBlockFee * 0.6 + blockFee * 0.4).toLong
        info(s"Mined ${acryl(reward)} Acryl ${blockUrl(lastKnownHeight)}")
      }
    }
    lastKnownHeight = height
  }

  override def start(): Unit = {
    import scala.concurrent.duration._
    info(s"$settings")

    lastKnownHeight = context.blockchain.height
    //TODO wait until node is synchronized
    val generatingBalance = context.blockchain.generatingBalance(minerPublicKey.toAddress)

    if (settings.notifications.startStop) {
      info(
        s"Started at $lastKnownHeight height for miner ${minerPublicKey.toAddress.stringRepr}. " +
          s"Generating balance: ${acryl(generatingBalance)} Acryl")
    }

    if (context.settings.minerSettings.enable) {
      if (generatingBalance < 100 * 100000000)
        warn(
          s"Node doesn't mine blocks!" +
            s" Generating balance is ${acryl(generatingBalance)} Acryl but must be at least 100 Acryl")
      if (context.blockchain.hasScript(minerPublicKey.toAddress))
        warn(
          s"Node doesn't mine blocks! Account ${minerPublicKey.toAddress.stringRepr} is scripted." +
            s" Send SetScript transaction with null script or use another account for mining")

      Observable
        .interval(1 seconds) // blocks are mined no more than once every 5 seconds
        .doOnNext(_ => Task.now(checkNextBlock()))
        .subscribe
    } else {
      err("Mining is disabled! Enable this (acryl.miner.enable) in the Node config and restart node")
      shutdown()
    }
  }

  override def shutdown(): Future[Unit] = Future {
    info(s"Turned off at $lastKnownHeight height for miner ${minerPublicKey.toAddress.stringRepr}")
  }
}
