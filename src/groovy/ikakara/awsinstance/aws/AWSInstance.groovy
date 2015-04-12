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
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentity
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient

//import com.amazonaws.services.mobileanalytics.AmazonMobileAnalyticsClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.Index

/**
 *
 * @author Allen
 */
@Slf4j("LOG")
@CompileStatic
public class AWSInstance {
  private static final lock_dynamo_table = new Object()
  private static final lock_dynamo_index = new Object()

  private static HashMap<String, Table> _dynamoTables = new HashMap<>()
  @Synchronized("lock_dynamo_table")
  static public Table DYNAMO_TABLE(String tableName) {
    Table table = _dynamoTables.get(tableName)
    if(table == null) {
      table = DYNAMO_DB().getTable(tableName)
      _dynamoTables.put(tableName, table)
    }
    return table
  }

  private static HashMap<String, Index> _dynamoIndexes = new HashMap<>()
  @Synchronized("lock_dynamo_index")
  static public Index DYNAMO_INDEX(String tableName, String indexName) {
    String key = tableName + "-" + indexName
    Index index = _dynamoIndexes.get(key)
    if(index == null) {
      LOG.info "tbl: ${tableName} idx: ${indexName}"
      index = DYNAMO_TABLE(tableName).getIndex(indexName)
      _dynamoIndexes.put(key, index)
    }
    return index
  }

  private static  AmazonDynamoDB _dynamoClient = new AmazonDynamoDBClient(AuthCredentials.instance)
  static public AmazonDynamoDB DYNAMO_CLIENT() {
    return _dynamoClient
  }

  private static AmazonSimpleEmailServiceClient _sesClient = new AmazonSimpleEmailServiceClient(AuthCredentials.instance)
  static public AmazonSimpleEmailServiceClient SES_CLIENT() {
    return _sesClient
  }

  private static AmazonS3 _s3Client = new AmazonS3Client(AuthCredentials.instance)
  static public AmazonS3 S3_CLIENT() {
    return _s3Client
  }

  private static AmazonSQS _sqsClient = new AmazonSQSClient(AuthCredentials.instance)
  static public AmazonSQS SQS_CLIENT() {
    return _sqsClient
  }

  private static AmazonCognitoIdentity _cognitoClient = new AmazonCognitoIdentityClient(AuthCredentials.instance)
  static public AmazonCognitoIdentity COGNITO_CLIENT() {
    return _cognitoClient
  }

  private static AmazonIdentityManagement _iamClient = new AmazonIdentityManagementClient(AuthCredentials.instance)
  static public AmazonIdentityManagement IAM_CLIENT() {
    return _iamClient
  }

  private static  DynamoDB _dynamoDB = null
  static public DynamoDB DYNAMO_DB() {
    if(_dynamoDB == null) {
      init_dynamo_db()
    }
    return _dynamoDB
  }
  /*
  private static AmazonMobileAnalyticsClient _analyticsClient = null //new AmazonMobileAnalyticsClient(AuthCredentials.instance)
  static public AmazonMobileAnalyticsClient ANALYTICS_CLIENT() {
  if(_analyticsClient == null) {
  init_analytics_client()
  }
  return _analyticsClient
  }
   */
  @Synchronized()
  static private init_dynamo_db() {
    if(_dynamoDB == null) {
      try {
        LOG.info "AWSInstance - DynamoDB created ================================================="
        _dynamoDB = new DynamoDB(DYNAMO_CLIENT())
      } catch(e) {
        LOG.error e.message
      }
    }
  }
  /*
  @Synchronized()
  static private init_analytics_client() {
  if(_analyticsClient == null) {
  try {
  LOG.info "AWSInstance - AmazonMobileAnalyticsClient created ================================================="
  _analyticsClient = new AmazonMobileAnalyticsClient(AuthCredentials.instance)
  } catch(e) {
  LOG.error e.message
  }
  }
  }
   */
  protected void finalize() throws Throwable {
    try {
      // clean indexes
      _dynamoIndexes?.clear()
      // clean tables
      _dynamoTables?.clear()
      _dynamoDB = null
      // shutdown clients
      _dynamoClient?.shutdown()
      _dynamoClient = null
      _sesClient?.shutdown()
      _sesClient = null
      ((AmazonS3Client)_s3Client)?.shutdown()
      _s3Client = null
      _sqsClient?.shutdown()
      _sqsClient = null
      _cognitoClient?.shutdown()
      _cognitoClient = null
      _iamClient?.shutdown()
      _iamClient = null
      //_analyticsClient?.shutdown()
      //_analyticsClient = null
    } finally {
      super.finalize()
    }
  }
}
