package com.acrylplatform.extensions.node

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.{NameMapper, ValueReader}

case class Settings(
    webhook: WebhookSettings,
    statisticsPeriod: Int,
    blockUrl: String,
    antiFork: Boolean,
    remoteAPI: String,
    localAPIKey: String,
    notifications: NotificationsSettings
)

case class WebhookSettings(
    url: String,
    headers: Seq[String]
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
    val statisticsPeriod      = config.as[Int]("statistics-period")
    val blockUrl              = config.as[String]("block-url")
    val antiFork              = config.as[Boolean]("anti-fork")
    val remoteAPI             = config.as[String]("remote-api")
    val localAPIKey           = config.as[String]("local-api-key")
    val notificationsSettings = config.as[NotificationsSettings]("notifications")
    Settings(
      webhook = webhookSettings,
      statisticsPeriod = statisticsPeriod,
      blockUrl = blockUrl,
      antiFork = antiFork,
      remoteAPI = remoteAPI,
      localAPIKey = localAPIKey,
      notifications = notificationsSettings
    )
  }
}

object WebhookSettings {
  implicit val valueReader: ValueReader[WebhookSettings] =
    (cfg, path) => fromConfig(cfg.getConfig(path))

  private[this] def fromConfig(config: Config): WebhookSettings = {
    val url     = config.as[String]("url")
    val headers = config.as[Seq[String]]("headers")
    WebhookSettings(
      url = url,
      headers = headers
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
