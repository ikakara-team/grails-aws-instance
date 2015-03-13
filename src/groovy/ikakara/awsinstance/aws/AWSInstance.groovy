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
package ikakara.awsinstance.aws

import groovy.transform.Synchronized

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;

//import com.amazonaws.services.mobileanalytics.AmazonMobileAnalyticsClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Index;

/**
 *
 * @author Allen
 */
public class AWSInstance {

  private static AmazonSimpleEmailServiceClient _sesClient = new AmazonSimpleEmailServiceClient(new AuthCredentials());
  static public AmazonSimpleEmailServiceClient SES_CLIENT() {
    return _sesClient;
  }

  private static AmazonS3 _s3Client = new AmazonS3Client(new AuthCredentials());
  static public AmazonS3 S3_CLIENT() {
    return _s3Client;
  }

}

