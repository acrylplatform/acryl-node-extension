package com.acrylplatform.extensions

import com.acrylplatform.extensions.node.Settings
import com.acrylplatform.utils.ScorexLogging
import scalaj.http.Http

class Logger(settings: Settings) extends ScorexLogging {
  private[this] def sendNotification(text: String): Unit = {
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
}
