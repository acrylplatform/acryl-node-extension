package com.acrylplatform.extensions.node

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader

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
  implicit val valueReader: ValueReader[Settings] =
    (cfg: Config, path: String) => fromConfig(cfg.getConfig(path))

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
