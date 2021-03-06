package bifrost.forging

import io.circe.syntax._
import scorex.core.settings.Settings

import scala.concurrent.duration._

trait ForgingSettings extends Settings with ForgingConstants {

  val InitialDifficulty = 15000000L
  val MinimumDifficulty = 100L

  lazy val offlineGeneration = settingsJSON.get("offlineGeneration").flatMap(_.asBoolean).getOrElse(false)

  lazy val posAttachmentSize = settingsJSON.get("posAttachmentSize").flatMap(_.asNumber).flatMap(_.toInt)
    .getOrElse(DefaultPosAttachmentSize)

  lazy val targetBlockTime: FiniteDuration = settingsJSON.get("targetBlockTime").flatMap(_.asNumber).flatMap(_.toLong)
    .map(x => FiniteDuration(x, MILLISECONDS)).getOrElse(30.second)

  val DefaultPosAttachmentSize = 1024

  override def toString: String = (Map("BlockGenerationDelay" -> blockGenerationDelay.length.asJson) ++
    settingsJSON.map(s => s._1 -> s._2)).asJson.spaces2
}
