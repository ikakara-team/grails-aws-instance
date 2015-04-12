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
package ikakara.awsinstance.util

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException

/**
 * @author Allen
 */
@Slf4j("LOG")
@CompileStatic
class PrintlnUtil {

  static AmazonServiceException(String header, AmazonServiceException ase) {
    StringBuilder sb = new StringBuilder(header)
    sb << "\nCaught an AmazonServiceException, which means your request made it " +
          "to Amazon, but was rejected with an error response for some reason.\n"
    sb << "Error Message:    " << ase.message << '\n'
    sb << "HTTP Status Code: " << ase.statusCode << '\n'
    sb << "AWS Error Code:   " << ase.errorCode << '\n'
    sb << "Error Type:       " << ase.errorType << '\n'
    sb << "Request ID:       " << ase.requestId
    println sb
  }

  static AmazonClientException(String header, AmazonClientException ace) {
    StringBuilder sb = new StringBuilder(header)
    sb << "\nCaught an AmazonClientException, which means the client encountered " +
          "a serious internal problem while trying to communicate with Amazon, " +
          "such as not being able to access the network.\n"
    sb << "Error Message: " << ace.message
    println sb
  }
}
