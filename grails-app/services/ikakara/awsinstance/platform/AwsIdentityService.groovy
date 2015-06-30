/* Copyright 2014-2015 the original author or authors.
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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

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

import com.amazonaws.services.identitymanagement.model.ListPoliciesRequest
import com.amazonaws.services.identitymanagement.model.CreatePolicyRequest
import com.amazonaws.services.identitymanagement.model.DeletePolicyRequest
import com.amazonaws.services.identitymanagement.model.GetRoleRequest
import com.amazonaws.services.identitymanagement.model.User
import com.amazonaws.services.identitymanagement.model.ListRolesRequest

import com.opencsv.bean.ColumnPositionMappingStrategy
import com.opencsv.bean.CsvToBean

import ikakara.awsinstance.aws.AWSInstance
import ikakara.awsinstance.aws.IAMCredential
import ikakara.awsinstance.util.PrintlnUtil

@CompileStatic
@Slf4j
class AwsIdentityService {
  static transactional = false

  static final int MAX_LIST_SIZE_POOL = 60
  static final int MAX_LIST_SIZE_POLICY = 1000
  static final int REPORT_CSV_POSITION = 280
  static final String[] IAM_REPORT_COLUMNS = [
      'user','arn','user_creation_time','password_enabled','password_last_used',
      'password_last_changed','password_next_rotation','mfa_active','access_key_1_active',
      'access_key_1_last_rotated','access_key_2_active','access_key_2_last_rotated',
      'cert_1_active','cert_1_last_rotated','cert_2_active','cert_2_last_rotated'] // the fields to bind do in your JavaBean

  static final String DEFAULT_COGNITO_POLICY = '''
  {
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "mobileanalytics:PutEvents",
        "cognito-sync:*"
      ],
      "Resource": [
        "*"
      ]
    }
  ]
}
'''

  def grailsApplication

  // http://stackoverflow.com/questions/10197784/how-can-i-deduce-the-aws-account-id-from-available-basicawscredentials
  def getUserInfo() {
    try {
      User user = (User)AWSInstance.IAM_CLIENT().getUser()?.user
      if(user) {
        return parseArn(user.arn)
      }
    } catch (AmazonServiceException ase) {
      if (ase.getErrorCode() == "AccessDenied") {
        String arn
        String msg = ase.getMessage()
        // User: arn:aws:iam::123456789012:user/division_abc/subdivision_xyz/Bob is not authorized to perform: iam:GetUser on resource: arn:aws:iam::123456789012:user/division_abc/subdivision_xyz/Bob
        int arnIdx = msg.indexOf("arn:aws")
        if (arnIdx != -1) {
          int arnSpace = msg.indexOf(" ", arnIdx)
          arn = msg.substring(arnIdx, arnSpace)
        }
        return parseArn(arn)
      } else {
        PrintlnUtil.AmazonServiceException("getUserInfo: ", ase)
      }
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("getUserInfo: ", ace)
    } catch(e) {
      log.error "getUserInfo: $e.message"
    }

    return null
  }

  private parseArn(String arn) {
    def m = arn =~ /arn:aws:iam::([0-9]+):user\\/(.*)/ //
    return m[0]
  }

  def getUser() {
    try {
      def response = AWSInstance.IAM_CLIENT().getUser()
      return response.user
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("getUser: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("getUser: ", ace)
    } catch(e) {
      log.error "getUser: $e.message"
    }
  }

  def generateReport() {
    try {
      return AWSInstance.IAM_CLIENT().generateCredentialReport()
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("generateReport: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("generateReport: ", ace)
    } catch(e) {
      log.error "generateReport: $e.message"
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
      log.error "getReport: $e.message"
    }
  }

  def getRole(String role) {
    try {
      def req = new GetRoleRequest().withRoleName(role)
      def response = AWSInstance.IAM_CLIENT().getRole(req)
      return response
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("getRole: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("getRole: ", ace)
    } catch(e) {
      log.error "getRole: $e.message"
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  def listPool() {
    try {
      def req = new ListIdentityPoolsRequest().withMaxResults(MAX_LIST_SIZE_POOL)
      def response = AWSInstance.COGNITO_CLIENT().listIdentityPools(req)
      return response?.identityPools
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("listPool: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("listPool: ", ace)
    } catch(e) {
      log.error "listPool: $e.message"
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
      log.error "describePool: $e.message"
    }
  }

  def createPool(String poolName, String providerDomain, boolean allowUnauthenticated=false) {
    try {
      def req = new CreateIdentityPoolRequest()
      .withAllowUnauthenticatedIdentities(allowUnauthenticated)
      .withIdentityPoolName(poolName)

      if(providerDomain) {
        req.developerProviderName = providerDomain
      }

      def resp = AWSInstance.COGNITO_CLIENT().createIdentityPool(req)

      return resp
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("createPool: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("createPool: ", ace)
    } catch(e) {
      log.error "createPool: $e.message"
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
      log.error "updatePool: $e.message"
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
      log.error "deletePool: $e.message"
    }
  }

  def listRolePool(String poolArn) {
    try {
      def req = new GetIdentityPoolRolesRequest().withIdentityPoolId(poolArn)
      def response = AWSInstance.COGNITO_CLIENT().getIdentityPoolRoles(req)
      return response?.roles
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("listRolePool: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("listRolePool: ", ace)
    } catch(e) {
      log.error "listRolePool: $e.message"
    }
  }

  void setRolePool(String poolArn, String authenticated, String unauthenticated) {
    def roles = [:]

    if(authenticated) {
      roles['authenticated'] = authenticated
    }

    if(unauthenticated) {
      roles['unauthenticated'] = unauthenticated
    }

    setRolePool(poolArn, roles)
  }

  void setRolePool(String poolArn, Map<String, String> roles) {
    try {
      def req = new SetIdentityPoolRolesRequest().withIdentityPoolId(poolArn).withRoles(roles)
      AWSInstance.COGNITO_CLIENT().setIdentityPoolRoles(req)
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("setRolePool: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("setRolePool: ", ace)
    } catch(e) {
      log.error "setRolePool: $e.message"
    }
  }

  def listIdentity(String poolArn) {
    try {
      def req = new ListIdentitiesRequest().withIdentityPoolId(poolArn).withMaxResults(MAX_LIST_SIZE_POOL)
      def response = AWSInstance.COGNITO_CLIENT().listIdentities(req)
      return response?.identities
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("listIdentity: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("listIdentity: ", ace)
    } catch(e) {
      log.error "listIdentity: $e.message"
    }
  }

  String getDeveloperId(String poolArn, String developerUserId) {
    try {
      def req = new LookupDeveloperIdentityRequest()
      .withIdentityPoolId(poolArn)
      .withDeveloperUserIdentifier(developerUserId)
      .withMaxResults(MAX_LIST_SIZE_POOL)
      def response = AWSInstance.COGNITO_CLIENT().lookupDeveloperIdentity(req)
      return response?.identityId
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("getDeveloperId: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("getDeveloperId: ", ace)
    } catch(e) {
      log.error "getDeveloperId: $e.message"
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
      log.error "getDeveloperToken: $e.message"
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // Policies
  /////////////////////////////////////////////////////////////////////////////

  def listLocalPolicies(pathPrefix = null) {
    try {
      def req = new ListPoliciesRequest().withScope('Local').withMaxItems(MAX_LIST_SIZE_POLICY)
      if(pathPrefix) {
        req.pathPrefix = '/' + pathPrefix + '/'
      }

      def response = AWSInstance.IAM_CLIENT().listPolicies(req)
      return response.policies
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("listLocalPolicies: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("listLocalPolicies: ", ace)
    } catch(e) {
      log.error "listLocalPolicies: $e.message"
    }
  }

  def createLocalPolicy(String path, String name, String document, String description) {
    try {
      def req = new CreatePolicyRequest().withPolicyName(name).withPolicyDocument(document).withDescription(description)

      if(path) {
        req.path = path
      }

      def response = AWSInstance.IAM_CLIENT().createPolicy(req)
      return response
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("createLocalPolicy: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("createLocalPolicy: ", ace)
    } catch(e) {
      log.error "createLocalPolicy: $e.message"
    }
  }

  boolean deleteLocalPolicy(String accountId, String path, String name) {
    try {
      def arn = "arn:aws:iam::${accountId}:policy/"
      if(path) {
        arn += path + '/'
      }
      arn += name

      def req = new DeletePolicyRequest().withPolicyArn(arn)
      AWSInstance.IAM_CLIENT().deletePolicy(req)
      return true
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("deleteLocalPolicy: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("deleteLocalPolicy: ", ace)
    } catch(e) {
      log.error "deleteLocalPolicy: $e.message"
    }
    return false
  }

  /////////////////////////////////////////////////////////////////////////////
  // Roles
  /////////////////////////////////////////////////////////////////////////////

  def listRoles(String pathPrefix) {
    try {
      def req = new ListRolesRequest().withMaxItems(MAX_LIST_SIZE_POLICY)

      if(pathPrefix) {
        req.pathPrefix = pathPrefix
      }

      def response = AWSInstance.IAM_CLIENT().listRoles(req)
      return response
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("listRoles: ", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("listRoles: ", ace)
    } catch(e) {
      log.error "listRoles: $e.message"
    }
  }

}
