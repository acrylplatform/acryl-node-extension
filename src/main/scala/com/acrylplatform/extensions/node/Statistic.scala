package com.acrylplatform.extensions.node

import com.acrylplatform.Version
import com.acrylplatform.extensions.node.logger.Statistics
import com.acrylplatform.extensions.{Context => ExtensionContext}

class Statistic(context: ExtensionContext, settings: Settings) {
  private[this] val logger = new Logger(context, settings)

  def start(): Unit = {
    val address         = context.wallet.privateKeyAccounts.head.toAddress
    val balance: Double = context.blockchain.balance(address) / 100000000

    val effectiveBalance                = context.blockchain.effectiveBalance(address, 1000)
    val effectiveBalanceNoConfirmations = context.blockchain.effectiveBalance(address, 0)

    val heightLocal = context.blockchain.height

    val version = Version.VersionString

    logger.statistics(
      Statistics(
        balance = balance,
        effectiveBalance = effectiveBalance,
        effectiveBalanceNoConfirmations = effectiveBalanceNoConfirmations,
        height = heightLocal,
        version = version,
        peers = Seq.empty,
        feature = Map.empty
      ))
  }
}
