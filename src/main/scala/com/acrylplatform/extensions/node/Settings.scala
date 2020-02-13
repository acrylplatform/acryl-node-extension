package com.acrylplatform.extensions.node

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.{NameMapper, ValueReader}

case class Settings(
    webhook: WebhookSettings,
    blockUrl: String,
    notifications: NotificationsSettings
)

case class WebhookSettings(
    url: String,
    method: String,
    headers: Seq[String],
    body: String
)

case class NotificationsSettings(
    startStop: Boolean,
    acrylReceived: Boolean,
    leasing: Boolean
)

object Settings {
  implicit val chosenCase: NameMapper = net.ceedubs.ficus.readers.namemappers.implicits.hyphenCase

  implicit val valueReader: ValueReader[Settings] =
    (cfg, path) => fromConfig(cfg.getConfig(path))

  private[this] def fromConfig(config: Config): Settings = {
    val webhookSettings       = config.as[WebhookSettings]("webhook")
    val blockUrl              = config.as[String]("block-url")
    val notificationsSettings = config.as[NotificationsSettings]("notifications")
    Settings(
      webhook = webhookSettings,
      blockUrl = blockUrl,
      notifications = notificationsSettings
    )
  }
}

object WebhookSettings {
  implicit val valueReader: ValueReader[WebhookSettings] =
    (cfg, path) => fromConfig(cfg.getConfig(path))

  private[this] def fromConfig(config: Config): WebhookSettings = {
    val url     = config.as[String]("url")
    val method  = config.as[String]("method")
    val headers = config.as[Seq[String]]("headers")
    val body    = config.as[String]("body")
    WebhookSettings(
      url = url,
      method = method,
      headers = headers,
      body = body
    )
  }
}

object NotificationsSettings {
  implicit val valueReader: ValueReader[NotificationsSettings] =
    (cfg, path) => fromConfig(cfg.getConfig(path))

  private[this] def fromConfig(config: Config): NotificationsSettings = {
    val startStop     = config.as[Boolean]("start-stop")
    val acrylReceived = config.as[Boolean]("acryl-received")
    val leasing       = config.as[Boolean]("leasing")
    NotificationsSettings(
      startStop = startStop,
      acrylReceived = acrylReceived,
      leasing = leasing
    )
  }
}
