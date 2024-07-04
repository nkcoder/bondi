package io.daniel
package aws

import cats.effect.IO
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.model.{RawMessage, SendRawEmailRequest}
import software.amazon.awssdk.services.ses.SesClient

import java.io.ByteArrayOutputStream
import java.nio.file.{Files, Paths}
import java.nio.ByteBuffer
import java.util.Properties
import javax.activation.DataHandler
import javax.mail.{Message, Session}
import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart}
import javax.mail.util.ByteArrayDataSource

object EmailService {

  def send(sender: String, recipient: String, subject: String, body: String, fileName: String): IO[Unit] = IO {

    val fileContent = Files.readAllBytes(Paths.get(fileName))

    val session     = Session.getDefaultInstance(new Properties())
    val mimeMessage = new MimeMessage(session)
    mimeMessage.setFrom(new InternetAddress(sender))
    mimeMessage.setSubject(subject)
    mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient))

    val messageBody = new MimeMultipart("alternative")
    val wrap        = new MimeBodyPart()

    val textPart = new MimeBodyPart()
    textPart.setContent(body, "text/plain; charset=utf-8")

    val htmlPart = new MimeBodyPart()
    htmlPart.setContent(body, "text/html; charset=utf-8")

    messageBody.addBodyPart(textPart)
    messageBody.addBodyPart(htmlPart)

    wrap.setContent(messageBody)

    val mimeMultipart = new MimeMultipart("mixed")
    mimeMessage.setContent(mimeMultipart)
    mimeMultipart.addBodyPart(wrap)

    // attachment
    val attachmentPart = new MimeBodyPart()
    val datasource     = new ByteArrayDataSource(fileContent, "application/octet-stream")
    attachmentPart.setDataHandler(new DataHandler(datasource))
    attachmentPart.setFileName(fileName)

    mimeMultipart.addBodyPart(attachmentPart)

    // send email
    val outputStream = new ByteArrayOutputStream()
    mimeMessage.writeTo(outputStream)

    val data       = SdkBytes.fromByteBuffer(ByteBuffer.wrap(outputStream.toByteArray))
    val rawMessage = RawMessage.builder().data(data).build()
    val request    = SendRawEmailRequest.builder().rawMessage(rawMessage).build()

    val region = Region.AP_SOUTHEAST_2
    val client = SesClient.builder.region(region).build

    client.sendRawEmail(request)
  }

}
