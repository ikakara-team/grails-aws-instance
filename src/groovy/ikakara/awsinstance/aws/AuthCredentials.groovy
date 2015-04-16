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

import groovy.transform.CompileStatic

import com.amazonaws.auth.AWSCredentials

/**
 * @author Allen
 */
@CompileStatic
class AuthCredentials implements AWSCredentials {

  static AuthCredentials instance

  final String AWSAccessKeyId
  final String AWSSecretKey

  private AuthCredentials(String accessKey, String secretKey) {
    AWSAccessKeyId = accessKey
    AWSSecretKey = secretKey
  }

  static void init(String accessKey, String secretKey) {
    instance = new AuthCredentials(accessKey, secretKey)
  }
}
