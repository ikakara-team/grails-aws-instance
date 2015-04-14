/* Copyright 2014-2015 Allen Arakaki.  All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import java.nio.charset.Charset

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.cognitoidentity.model.CreateIdentityPoolRequest
import com.amazonaws.services.cognitoidentity.model.DeleteIdentityPoolRequest
import com.amazonaws.services.cognitoidentity.model.DescribeIdentityPoolRequest
import com.amazonaws.services.cognitoidentity.model.GetIdentityPoolRolesRequest
import com.amazonaws.services.cognitoidentity.model.GetOpenIdTokenForDeveloperIdentityRequest
import com.amazonaws.services.cognitoidentity.model.ListIdentitiesRequest
import com.amazonaws.services.cognitoidentity.model.ListIdentityPoolsRequest
import com.amazonaws.services.cognitoidentity.model.LookupDeveloperIdentityRequest
import com.amazonaws.services.cognitoidentity.model.SetIdentityPoolRolesRequest

import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.CsvToBean

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import ikakara.awsinstance.aws.AWSInstance
import ikakara.awsinstance.aws.IAMCredential
import ikakara.awsinstance.util.PrintlnUtil

@CompileStatic
@Slf4j
class AwsIdentityService {
  static transactional = false

  static final int MAX_LIST_SIZE = 60
  static final int REPORT_CSV_POSITION = 280
  static final String[] IAM_REPORT_COLUMNS = [
      'user','arn','user_creation_time','password_enabled','password_last_used',
      'password_last_changed','password_next_rotation','mfa_active','access_key_1_active',
      'access_key_1_last_rotated','access_key_2_active','access_key_2_last_rotated',
      'cert_1_active','cert_1_last_rotated','cert_2_active','cert_2_last_rotated'] // the fields to bind do in your JavaBean

  def grailsApplication

  def generateReport() {
    try {
      return AWSInstance.IAM_CLIENT().generateCredentialReport()
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("generateReport: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("generateReport: ", ace)
    } catch(e) {
      log.error "generateReport? $e.message"
    }
  }

  def getReport() {
    try {
      def response = AWSInstance.IAM_CLIENT().credentialReport
      if(response?.content) {
        def content = Charset.forName("UTF-8").decode(response.content).position(REPORT_CSV_POSITION)

        ColumnPositionMappingStrategy strat = new ColumnPositionMappingStrategy(
              type: IAMCredential, columnMapping: IAM_REPORT_COLUMNS)

        return new CsvToBean().parse(strat, new StringReader(content.toString()))
      }
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("getReport: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("getReport: ", ace)
    } catch(e) {
      log.error "getReport? $e.message"
    }
  }

  def listPool(int max = MAX_LIST_SIZE) {
    try {
      def req = new ListIdentityPoolsRequest().withMaxResults(max)
      def response = AWSInstance.COGNITO_CLIENT().listIdentityPools(req)
      return response?.identityPools
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("listPool: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("listPool: ", ace)
    } catch(e) {
      log.error "listPool? $e.message"
    }
  }

  def describePool(String poolArn) {
    try {
      def req = new DescribeIdentityPoolRequest().withIdentityPoolId(poolArn)
      return AWSInstance.COGNITO_CLIENT().describeIdentityPool(req)
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("describePool: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("describePool: ", ace)
    } catch(e) {
      log.error "describePool? $e.message"
    }
  }

  def createPool(String poolName, String providerDomain, boolean allowUnauthenticated=false) {
    try {
      def req = new CreateIdentityPoolRequest()
      .withAllowUnauthenticatedIdentities(allowUnauthenticated)
      .withIdentityPoolName(poolName)
      .withDeveloperProviderName(providerDomain)
      return AWSInstance.COGNITO_CLIENT().createIdentityPool(req)
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("createPool: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("createPool: ", ace)
    } catch(e) {
      log.error "createPool? $e.message"
    }
  }

  def updatePool(String poolArn) {
    try {
      // TBD
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("updatePool: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("updatePool: ", ace)
    } catch(e) {
      log.error "updatePool? $e.message"
    }
  }

  void deletePool(String poolArn) {
    try {
      def req = new DeleteIdentityPoolRequest().withIdentityPoolId(poolArn)
      AWSInstance.COGNITO_CLIENT().deleteIdentityPool(req)
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("deletePool: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("deletePool: ", ace)
    } catch(e) {
      log.error "deletePool? $e.message"
    }
  }

  def listRole(String poolArn) {
    try {
      def req = new GetIdentityPoolRolesRequest().withIdentityPoolId(poolArn)
      def response = AWSInstance.COGNITO_CLIENT().getIdentityPoolRoles(req)
      return response?.roles
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("listRole: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("listRole: ", ace)
    } catch(e) {
      log.error "listRole? $e.message"
    }
  }

  void setRoles(String poolArn, String authenticated, String unauthenticated) {
    setRoles(poolArn, ['authenticated': authenticated, 'unauthenticated': unauthenticated])
  }

  void setRoles(String poolArn, Map<String, String> roles) {
    try {
      def req = new SetIdentityPoolRolesRequest().withIdentityPoolId(poolArn).withRoles(roles)
      AWSInstance.COGNITO_CLIENT().setIdentityPoolRoles(req)
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("setRole: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("setRole: ", ace)
    } catch(e) {
      log.error "setRole? $e.message"
    }
  }

  def listIdentity(String poolArn, int max = MAX_LIST_SIZE) {
    try {
      def req = new ListIdentitiesRequest().withIdentityPoolId(poolArn).withMaxResults(max)
      def response = AWSInstance.COGNITO_CLIENT().listIdentities(req)
      return response?.identities
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("listIdentity: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("listIdentity: ", ace)
    } catch(e) {
      log.error "listIdentity? $e.message"
    }
  }

  String getDeveloperId(String poolArn, String developerUserId, int max = MAX_LIST_SIZE) {
    try {
      def req = new LookupDeveloperIdentityRequest()
      .withIdentityPoolId(poolArn)
      .withDeveloperUserIdentifier(developerUserId)
      .withMaxResults(MAX_LIST_SIZE)
      def response = AWSInstance.COGNITO_CLIENT().lookupDeveloperIdentity(req)
      return response?.identityId
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("getDeveloperId: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("getDeveloperId: ", ace)
    } catch(e) {
      log.error "getDeveloperId? $e.message"
    }
  }

  def getDeveloperToken(String poolArn, String developerId, String userId) {
    try {
      Map mapLogin = [(developerId): userId]
      def req = new GetOpenIdTokenForDeveloperIdentityRequest().withIdentityPoolId(poolArn).withLogins(mapLogin)
      def response = AWSInstance.COGNITO_CLIENT().getOpenIdTokenForDeveloperIdentity(req)
      return [response.identityId, response.token]
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("getDeveloperToken: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("getDeveloperToken: ", ace)
    } catch(e) {
      log.error "getDeveloperToken? $e.message"
    }
  }
}
