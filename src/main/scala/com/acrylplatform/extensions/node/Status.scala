package com.acrylplatform.extensions.node

import java.lang.management.ManagementFactory.getRuntimeMXBean

import com.acrylplatform.Version
import com.acrylplatform.extensions.{Context => ExtensionContext}

class Status(context: ExtensionContext, settings: Settings) {
  private[this] val logger = new Logger(settings)

  def start(): Unit = {
    val pid = getRuntimeMXBean.getName.split("@")(0)

    val address = context.wallet.privateKeyAccounts.head.toAddress
    val balance = context.blockchain.balance(address) / 100000000

    val effectiveBalance                = context.blockchain.effectiveBalance(address, 1000)
    val effectiveBalanceNoConfirmations = context.blockchain.effectiveBalance(address, 0)

    val heightLocal = context.blockchain.height

    val version = Version.VersionString

    logger.info(s"""
               |Node status: Running (pid $pid)
               |Address: $address
               |Balance: $balance
               |Effective balance (1000 confirmations): $effectiveBalance
               |Effective balance (0 confirmations): $effectiveBalanceNoConfirmations
               |Height Local: $heightLocal
               |Local node version: $version
               |""".stripMargin)
  }
}
