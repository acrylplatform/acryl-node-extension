package com.acrylplatform.extensions.node

import com.acrylplatform.utils.ScorexLogging
import scalaj.http.Http

import scala.util.control.NonFatal

class Logger(settings: Settings) extends ScorexLogging {
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
}
