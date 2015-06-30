/* Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ikakara.awsinstance.platform

import grails.compiler.GrailsCompileStatic
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendDataPoint
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import com.amazonaws.services.simpleemail.model.VerifyEmailAddressRequest

import ikakara.awsinstance.EmailException
import ikakara.awsinstance.aws.AWSInstance
import ikakara.awsinstance.command.EmailCommand
import ikakara.awsinstance.command.EmailStatsCommand

@GrailsCompileStatic
@Slf4j
class AwsEmailService {
  static transactional = false

  def grailsApplication

  @GrailsCompileStatic(TypeCheckingMode.SKIP)
  private String mailFrom() {
    return grailsApplication.config.grails.plugin.awsinstance?.ses?.mailFrom
  }

  String send(String from, EmailCommand email) throws EmailException {
    return privateSend(from, email)
  }

  String send(EmailCommand email) throws EmailException {
    return privateSend(mailFrom(), email)
  }

  private String privateSend(String from, EmailCommand email) throws EmailException {

    if(!email) {
      throw new EmailException("Target obj cannot be null.")
    }

    if(!email.to) {
      throw new EmailException("To must be specified.")
    }

    if(!email.html && !email.text) {
      throw new EmailException("You cannot send an email without a body.")
    }

    if(!email.subject) {
      throw new EmailException("You must provide a message subject.")
    }

    def destination = new Destination().withToAddresses(email.to)

    def mailBody = new Body(
      html: email.html ? new Content(email.html) : null,
      text: email.text ? new Content(email.text) : null)

    def message = new Message(subject: email.subject ? new Content(email.subject) : null, body: mailBody)

    def emailRequest = new SendEmailRequest(from, destination, message)

    try {
      def emailResult = AWSInstance.SES_CLIENT().sendEmail(emailRequest)
      return emailResult.messageId
    } catch(AmazonServiceException e) {
      throw new EmailException(e)
    } catch(AmazonClientException e) {
      throw new EmailException(e)
    }
  }

  /**
   * Pass an email address to this method to verify it for use with AWS SES
   */
  void verifyEmailAddress(String emailToVerify) {
    AWSInstance.SES_CLIENT().verifyEmailAddress(new VerifyEmailAddressRequest().withEmailAddress(emailToVerify))
  }

  /**
   * Returns a list of verified email addresses for use with AWS SES
   */
  List<String> getVerifiedEmailAddresses() {
    return AWSInstance.SES_CLIENT().listVerifiedEmailAddresses().verifiedEmailAddresses ?: []
  }

  /**
   * Return email service statistics
   */
  List<EmailStatsCommand> getStatistics() {
    return AWSInstance.SES_CLIENT().sendStatistics.sendDataPoints.sort { it.timestamp }.collect { SendDataPoint dp ->
      def _timestamp = dp.timestamp?.format('yyyy/MM/dd HH:mm')
      def _attempts = dp.deliveryAttempts?.toString()?.center(7, " ")
      def _rejects = dp.rejects?.toString()?.center(13, " ")
      def _complaints = dp.complaints?.toString()?.center(22, " ")
      def _bounces = dp.bounces?.toString()?.center(7, " ")
      new EmailStatsCommand(_timestamp,_attempts,_rejects,_complaints,_bounces)
    }
  }
}
