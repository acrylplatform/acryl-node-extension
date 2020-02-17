package com.acrylplatform.extensions.node

import com.acrylplatform.extensions.{Context => ExtensionContext}
import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods.parse
import scalaj.http.{Http, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

class Antifork(context: ExtensionContext, settings: Settings) {
  private[this] val logger    = new Logger(settings)
  private[this] val remoteAPI = settings.remoteAPI

  private[this] def rollback(height: Int): Unit = {
    logger.warn("Rollback blockchain: Starting the blockchain rollback process")

    val localAPIAddress = context.settings.restAPISettings.bindAddress
    val localAPIPort    = context.settings.restAPISettings.port

    val headers =
      if (settings.localAPIKey.isEmpty)
        Seq(("Content-Type", "application/json"))
      else
        Seq(("Content-Type", "application/json"), ("X-API-Key", settings.localAPIKey))

    try {
      val response: HttpResponse[String] = Http(s"""http://$localAPIAddress:$localAPIPort/debug/rollback""")
        .timeout(10000, 10000)
        .headers(headers)
        .postData(s"""{
                     |  "rollbackTo": $height,
                     |  "returnTransactionsToUtx": true
                     |}""".stripMargin)
        .method("POST")
        .asString

      if (response.is2xx)
        logger.info("Rollback blockchain: Blockchain rollback process was successful")
      else
        logger.info("Rollback blockchain: Blockchain rollback process failed")
    } catch {
      case NonFatal(e) => logger.err("Rollback blockchain: " + e.getMessage)
    }
  }

  private[this] def getBlockSig(height: Int): Future[Option[String]] = Future {
    val response: HttpResponse[String] = Http(s"$remoteAPI/blocks/headers/at/$height").asString
    val json                           = parse(response.body)
    json \ "signature" match {
      case JString(s) => Some(s)
      case _          => None
    }
  }

  def check(): Unit = {
    val height     = context.blockchain.height - 1
    val localBlock = context.blockchain.blockAt(height)

    getBlockSig(height).onComplete {
      case Success(value) =>
        value match {
          case Some(remoteSig) =>
            localBlock match {
              case Some(block) =>
                val localSig = block.getHeader.signerData.signature.toString

                if (localSig != remoteSig) {
                  logger.warn("Anti-fork: Fork detected!")
                  rollback(height - 3000)
                } else {
                  logger.info("Anti-fork: Fork not detected")
                }
              case None =>
                logger.err("Anti-fork: Not found block on local blockchain")
            }
          case None => logger.err("Anti-fork: Failed to get signature")
        }
      case Failure(_) => logger.err("Anti-fork: Remote block request failed")
    }
  }
}
