package com.acrylplatform.extensions.node

import com.acrylplatform.Version
import com.acrylplatform.extensions.node.logger.Statistics
import com.acrylplatform.extensions.{Context => ExtensionContext}
import com.acrylplatform.features.BlockchainFeatures
import com.acrylplatform.features.FeatureProvider._
import org.json4s.JsonAST.{JArray, JString}
import org.json4s.jackson.JsonMethods.parse
import scalaj.http.{Http, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class Statistic(context: ExtensionContext, settings: Settings) {
  private[this] val logger = new Logger(context, settings)

  private[this] val localAPIAddress = context.settings.restAPISettings.bindAddress
  private[this] val localAPIPort    = context.settings.restAPISettings.port

  private[this] def getPeers: Future[Seq[String]] = Future {
    val response: HttpResponse[String] = Http(s"http://$localAPIAddress:$localAPIPort/peers/connected").asString
    val json                           = parse(response.body)
    json \ "peers" match {
      case JArray(list) =>
        list.map(item => {
          item \ "address" match {
            case JString(s) => s.replaceAll("/", "")
            case _          => ""
          }
        })
      case _ => Seq.empty
    }
  }

  def start(): Unit = {
    val address         = context.wallet.privateKeyAccounts.head.toAddress
    val balance: Double = context.blockchain.balance(address) / 100000000

    val effectiveBalance                = context.blockchain.effectiveBalance(address, 1000)
    val effectiveBalanceNoConfirmations = context.blockchain.effectiveBalance(address, 0)

    val heightLocal = context.blockchain.height

    val version = Version.VersionString

    val feature: Map[String, String] =
      (context.blockchain.featureVotes(heightLocal).keySet ++
        context.blockchain.approvedFeatures.keySet ++
        BlockchainFeatures.implemented).toSeq.sorted.map(id => {
        val status = context.blockchain.featureStatus(id, heightLocal)
        "Feature: " + id.toString -> status.toString
      })(collection.breakOut)

    getPeers.onComplete {
      case Success(peers) =>
        logger.statistics(
          Statistics(
            balance = balance,
            effectiveBalance = effectiveBalance,
            effectiveBalanceNoConfirmations = effectiveBalanceNoConfirmations,
            height = heightLocal,
            version = version,
            peers = peers,
            feature = feature
          ))
      case Failure(_) => logger.err("Statistic: Local API request failed")
    }
  }
}
