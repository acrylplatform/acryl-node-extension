package com.acrylplatform.extensions.node.logger

case class Schema(
    address: String,
    nodeName: String,
    declaredAddress: String,
    timestamp: Long,
    version: String,
    typeData: String,
    data: Data
)

trait Data

case class Info(message: String) extends Data

case class Warn(message: String) extends Data

case class Error(message: String) extends Data

case class Statistics(
    balance: Int,
    effectiveBalance: Int,
    effectiveBalance1000: Int,
    height: Int,
    version: String,
    peers: Seq[String],
    feature: Map[String, String]
) extends Data
