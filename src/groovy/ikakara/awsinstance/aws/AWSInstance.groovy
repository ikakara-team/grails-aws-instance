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

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient

/**
 * @author Allen
 */
class AWSInstance {

  private static AmazonSimpleEmailServiceClient _sesClient = new AmazonSimpleEmailServiceClient(AuthCredentials.instance)
  private static AmazonS3 _s3Client = new AmazonS3Client(AuthCredentials.instance)

  static AmazonSimpleEmailServiceClient SES_CLIENT() {
    return _sesClient
  }

  static AmazonS3 S3_CLIENT() {
    return _s3Client
  }
}
