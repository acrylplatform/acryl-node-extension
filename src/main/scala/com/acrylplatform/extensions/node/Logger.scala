package com.acrylplatform.extensions.node

import java.util.Date

import com.acrylplatform.extensions.{Context => ExtensionContext}
import com.acrylplatform.extensions.node.logger._
import com.acrylplatform.utils.ScorexLogging
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import scalaj.http.Http

import scala.util.control.NonFatal

class Logger(context: ExtensionContext, settings: Settings) extends ScorexLogging {

  private[this] val address         = context.wallet.privateKeyAccounts.head.toAddress.toString
  private[this] val nodeName        = context.settings.networkSettings.nodeName
  private[this] val declaredAddress = context.settings.networkSettings.declaredAddress.toString
  private[this] val version         = "1.0"

  implicit val formats: DefaultFormats.type = DefaultFormats

  private[this] def sendNotification(json: String): Unit =
    try {
      Http(settings.webhook.url)
        .headers(settings.webhook.headers.flatMap(s =>
          s.split(":") match {
            case Array(a, b) =>
              Seq(("Content-Type", "application/json"), (a.trim, b.trim))
            case _ =>
              log.error(s"""Can't parse "$s" header! Please check "webhook.headers" config. Its values must be in the "name: value" format""")
              Seq(("Content-Type", "application/json"))
        }))
        .postData(json)
        .method("POST")
        .asString
    } catch {
      case NonFatal(e) => log.error(e.getMessage, e)
    }

  private[this] def getTimestamp: Long = {
    val date = new Date()
    date.getTime
  }

  def info(message: String): Unit = {
    log.info(message)
    val json = write(
      Schema(address = address,
             nodeName = nodeName,
             declaredAddress = declaredAddress,
             timestamp = getTimestamp,
             typeData = "INFO",
             version = version,
             data = Info(message)))
    sendNotification(json)
  }

  def warn(message: String): Unit = {
    log.warn(message)
    val json = write(
      Schema(address = address,
             nodeName = nodeName,
             declaredAddress = declaredAddress,
             timestamp = getTimestamp,
             typeData = "WARN",
             version = version,
             data = Warn(message)))
    sendNotification(json)
  }

  def err(message: String): Unit = {
    log.error(message)
    val json = write(
      Schema(address = address,
             nodeName = nodeName,
             declaredAddress = declaredAddress,
             timestamp = getTimestamp,
             typeData = "ERROR",
             version = version,
             data = Error(message)))
    sendNotification(json)
  }

  def statistics(statistics: Statistics): Unit = {
    val json = write(
      Schema(address = address,
             nodeName = nodeName,
             declaredAddress = declaredAddress,
             timestamp = getTimestamp,
             typeData = "STATISTICS",
             version = version,
             data = statistics))
    sendNotification(json)
  }
}
