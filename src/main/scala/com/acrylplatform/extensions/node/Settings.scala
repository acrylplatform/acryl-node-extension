package com.acrylplatform.extensions.node

import net.ceedubs.ficus.readers.ArbitraryTypeReader.arbitraryTypeValueReader
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
  implicit val valueReader: ValueReader[Settings] = arbitraryTypeValueReader
}

object WebhookSettings {
  implicit val valueReader: ValueReader[WebhookSettings] = arbitraryTypeValueReader
}

object NotificationsSettings {
  implicit val valueReader: ValueReader[NotificationsSettings] = arbitraryTypeValueReader
}
