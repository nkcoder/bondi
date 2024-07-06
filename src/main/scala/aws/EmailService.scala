package io.daniel
package aws

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Properties
import javax.activation.DataHandler
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

import cats.effect.IO
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.RawMessage
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest

object EmailService {

  /** Using AWS Java SDK v2 to send an email with an attachment.
    *
    * @see
    *   <a href="https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/java_ses_code_examples.html">Amazon SES Java SDK v2 examples</a>
    *
    * @param sender
    *   sender
    * @param recipient
    *   recipient
    * @param subject
    *   subject
    * @param body
    *   body
    * @param fileName
    *   attachment file name
    * @return
    */
  def sendEmailWithAttachment(
      sender: String,
      recipient: String,
      subject: String,
      body: String,
      fileName: String
  ): IO[Unit] = IO {

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

    println(s"Email sent to: $recipient, fileName: $fileName")
  }

}
