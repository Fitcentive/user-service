package io.fitcentive.user.infrastructure.utils

import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps
import org.apache.batik.transcoder.{TranscoderInput, TranscoderOutput}
import org.apache.batik.transcoder.image.JPEGTranscoder

import java.security.MessageDigest

trait ImageSupport {

  private val initialProfileBackgroundColourOptions =
    List("#6dccb1", "#79aad9", "#ee789d", "#a987d1", "#e4a6c7", "#f1d86f", "#d2c0a0", "#f5a35c", "#c47c6c", "#ff7e62")

  // Example taken from https://xmlgraphics.apache.org/batik/using/transcoder.html
  def generateJpgWithUserInitials(firstName: String, lastName: String): File = {
    val svgFile = generateSvgWithUserInitials(firstName, lastName)
    val outputFile = File.createTempFile(UUID.randomUUID().toString, ".jpg")
    new JPEGTranscoder().pipe { jpegTranscoder =>
      jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, .8f)
      val input = new TranscoderInput(svgFile.toURI.toString)

      val oStream = new FileOutputStream(outputFile.getPath)
      val output = new TranscoderOutput(oStream)

      jpegTranscoder.transcode(input, output)

      oStream.flush()
      oStream.close()
    }
    svgFile.delete()
    outputFile
  }

  def generateSvgWithUserInitials(firstName: String, lastName: String): File =
    File.createTempFile(UUID.randomUUID().toString, ".svg").tap { file =>
      val backgroundColour = generateBackgroundColourForInitialProfilePicture(firstName, lastName)
      val textColour = generateTextColour(backgroundColour)
      Files.write(
        Paths.get(file.getPath),
        svgStringWithInitials(
          firstName.headOption.getOrElse(' ').toUpper,
          lastName.headOption.getOrElse(' ').toUpper,
          backgroundColour,
          textColour
        ).getBytes(StandardCharsets.UTF_8)
      )
    }

  private def svgStringWithInitials(
    initial1: Char,
    initial2: Char,
    hexBackgroundColour: String,
    hexTextColour: String
  ): String =
    s"""
       |<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.1" width="400" height="400" viewBox="0 0 400 400">
       |<g transform="matrix(5 0 0 5 200 200)" >
       |	<circle style="fill: $hexBackgroundColour;"  cx="0" cy="0" r="40" />
       |</g>
       |<g transform="matrix(1 0 0 1 200 200)" >
       |	<text style="fill: $hexTextColour;" font-size="180"><tspan x="0" y="65" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" >$initial1$initial2</tspan></text>
       |</g>
       |</svg>
       |""".stripMargin

  private def generateBackgroundColourForInitialProfilePicture(firstName: String, lastName: String): String = {
    val nameLength = firstName.length + lastName.length
    initialProfileBackgroundColourOptions(
      scala.math.floor(nameLength % initialProfileBackgroundColourOptions.length).toInt
    )
  }

  /**
    * Returns white as text colour if `hexColour` is dark
    * Returns black otherwise
    */
  private def generateTextColour(hexColour: String): String = {
    val red = Integer.parseInt(hexColour.slice(1, 3), 16)
    val green = Integer.parseInt(hexColour.slice(3, 5), 16)
    val blue = Integer.parseInt(hexColour.slice(5, 7), 16)
    List(red, green, blue).map(_ / 255.0d).map { c =>
      if (c <= 0.03928d) (c / 12.92d)
      else scala.math.pow(((c + 0.055d) / 1.055d), 2.4d)
    } match {
      case newRed :: newGreen :: newBlue :: Nil =>
        if (((0.2126d * newRed) + (0.7152d * newGreen) + (0.0722d * newBlue)) <= 0.179d) "#ffffff"
        else "#000000"
    }
  }

  def calculateHash(file: File): String = {
    val buffer: Array[Byte] = new Array[Byte](8192)
    val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
    val bis: BufferedInputStream = new BufferedInputStream(new FileInputStream(file))

    LazyList.continually(bis.read(buffer)).takeWhile(_ > 0).foreach { bytesRead => digest.update(buffer, 0, bytesRead) }
    bis.close()

    String.format("%064x", new java.math.BigInteger(1, digest.digest))
  }
}
