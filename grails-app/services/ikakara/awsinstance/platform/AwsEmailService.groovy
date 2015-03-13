/* Copyright 2014-2015 Allen Arakaki.  All Rights Reserved.
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

import static org.springframework.util.StringUtils.hasText

import grails.util.Holders;

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException

import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import com.amazonaws.services.simpleemail.model.VerifyEmailAddressRequest

import ikakara.awsinstance.EmailException
import ikakara.awsinstance.aws.AWSInstance;
import ikakara.awsinstance.command.EmailCommand
import ikakara.awsinstance.command.EmailStatsCommand


class AwsEmailService {
  static transactional = false

  private String mailFrom() {
    return Holders.config.grails.plugin.awsinstance?.ses?.mailFrom
  }

  public String send(String from, EmailCommand email) throws EmailException {
    return privateSend(from, email);
  }

  public String send(EmailCommand email) throws EmailException {
    return privateSend(mailFrom(), email);
  }

  private String privateSend(String from, EmailCommand email) throws EmailException {

    if(email == null) {
      throw new EmailException("Target obj cannot be null.");
    }

    if(email.to==null || email.to.isEmpty()) {
      throw new EmailException("To must be specified.");
    }

    if(!hasText(email.html) && !hasText(email.text)) {
      throw new EmailException("You cannot send an email without a body.");
    }

    if(email.subject == null) {
      throw new EmailException("You must provide a message subject.");
    }

    def destination = new Destination()
    destination.toAddresses = email.to

    def mailBody = new Body()
    mailBody.html = email.html ? new Content(email.html) : null
    mailBody.text = email.text ? new Content(email.text) : null

    def message = new Message()
    message.subject = email.subject ? new Content(email.subject) : null
    message.body = mailBody

    def emailRequest = new SendEmailRequest(from, destination, message);

    def emailResult = null;

    try {
      emailResult = AWSInstance.SES_CLIENT().sendEmail(emailRequest)
    } catch(AmazonServiceException e) {
      throw new EmailException(e);
    } catch(AmazonClientException e) {
      throw new EmailException(e);
    }

    return emailResult.messageId
  }

  /**
   * Pass an email address to this method to verify it for use with AWS SES
   */
  public void verifyEmailAddress(String emailToVerify) {
    AWSInstance.SES_CLIENT().verifyEmailAddress(new VerifyEmailAddressRequest().withEmailAddress(emailToVerify))
  }

  /**
   * Returns a list of verified email addresses for use with AWS SES
   */
  public List<String> getVerifiedEmailAddresses() {
    List<String> verifiedAddresses = new ArrayList<>();

    def verifiedEmails = AWSInstance.SES_CLIENT().listVerifiedEmailAddresses()
    if (verifiedEmails.getVerifiedEmailAddresses()) {
      verifiedEmails.getVerifiedEmailAddresses().eachWithIndex { email, index ->
        verifiedAddresses.add(email);
      }
    }
    return verifiedAddresses;
  }

  /**
   * Return email service statistics
   */
  public List<EmailStatsCommand> getStatistics() {
    List<EmailStatsCommand> statistics = new ArrayList<EmailStatsCommand>();

    def sendStatisticsResult = AWSInstance.SES_CLIENT().getSendStatistics()
    if (sendStatisticsResult.getSendDataPoints().size() > 0) {
      def intervals = sendStatisticsResult.getSendDataPoints().sort { it.timestamp }
      intervals.each { dp ->
        def _timestamp = dp.timestamp?.format('yyyy/MM/dd HH:mm')
        def _attempts = dp.deliveryAttempts?.toString()?.center(7, " ")
        def _rejects = dp.rejects?.toString()?.center(13, " ")
        def _complaints = dp.complaints?.toString()?.center(22, " ")
        def _bounces = dp.bounces?.toString()?.center(7, " ")
        statistics.add(new EmailStatsCommand(_timestamp,_attempts,_rejects,_complaints,_bounces))
      }
    }
    return statistics
  }
}
