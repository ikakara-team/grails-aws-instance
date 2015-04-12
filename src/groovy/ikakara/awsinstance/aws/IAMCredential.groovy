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

class IAMCredential {
  String user
  String arn
  String user_creation_time
  String password_enabled
  String password_last_used
  String password_last_changed
  String password_next_rotation
  Boolean mfa_active
  Boolean access_key_1_active
  String access_key_1_last_rotated
  Boolean access_key_2_active
  String access_key_2_last_rotated
  Boolean cert_1_active
  String cert_1_last_rotated
  Boolean cert_2_active
  String cert_2_last_rotated
}

