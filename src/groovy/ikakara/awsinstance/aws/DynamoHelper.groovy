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

import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.document.Index
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.TableCollection
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.CreateGlobalSecondaryIndexAction
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndexDescription
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import com.amazonaws.services.dynamodbv2.model.ListTablesResult
import com.amazonaws.services.dynamodbv2.model.Projection
import com.amazonaws.services.dynamodbv2.model.ProjectionType
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType
import com.amazonaws.services.dynamodbv2.model.TableDescription

import com.amazonaws.services.dynamodbv2.util.Tables

import ikakara.awsinstance.dao.dynamo.ADynamoObject

/**
 * @author Allen
 */
@Slf4j("LOG")
@CompileStatic
class DynamoHelper {

  static final String DEFAULT_DATASOURCE = "dataSource_config"

  // Holders.getFlatConfig() doesn't seem to get grailsApplication.mergedConfig
  static getConfigProperty(String str) {
    String[] keys = str.split("\\.")

    def co = (ConfigObject)Holders.config.get(keys[0])
    if (co) {
      for (int i = 1; i < keys.length; i++) {
        co = ((ConfigObject) co).get(keys[i])
        if (!(co instanceof ConfigObject)) {
          break
        }
      }
    }

    return co
  }

  static String getTableName(Class<?> clazz) {
    return getTableName(clazz, DEFAULT_DATASOURCE)
  }

  static String getTableName(Class<?> clazz, String dataSource) {

    String className = clazz.simpleName

    String tableName = clazz.getAnnotation(DynamoDBTable)?.tableName() ?: className

    String dbPrefix = (String) getConfigProperty(dataSource + ".dbPrefix")
    String retTableName = dbPrefix + "_" + tableName

    LOG.info("class:$className tableName:$retTableName")

    return retTableName
  }

  static Map initTable(String tableName, ADynamoObject obj, String dataSource) {
    Map map

    String dbCreate = (String) getConfigProperty(dataSource + ".dbCreate")
    try {
      // TBD -
      if (Tables.doesTableExist(AWSInstance.DYNAMO_CLIENT(), tableName)) {
        LOG.debug("Table $tableName is already ACTIVE")
        if ("create-drop" == dbCreate) {
          // TBD: we should drop and recreate the db
        }
      } else if ("create-drop" == dbCreate || "create" == dbCreate) {
        map = obj.initTable()
        LOG.debug("Table $tableName is not ACTIVE, created $map")
      }
    } catch (e) {
      LOG.error("Exception:$tableName $e.message")
    }
    return map
  }

  static List<Map> getTableNames() {
    return AWSInstance.DYNAMO_DB().listTables().collect { Table table ->
      return [name: table.tableName, description: table.description]
    }
  }

  static Map getTableInformation(String tableName) {
    try {
      TableDescription tableDescription = AWSInstance.DYNAMO_CLIENT().describeTable(
        new DescribeTableRequest().withTableName(tableName)).getTable()
      return tableDescriptionToMap(tableDescription)
    } catch (ResourceNotFoundException ignored) {
      LOG.debug(ignored.message, ignored)
    }
  }

  static Map createTableGlobalSecondaryIndex(
    Table table,
    String indexName,
    String idxHashName, ScalarAttributeType hashType,
    String idxRangeName, ScalarAttributeType rangeType) {

    TableDescription desc = table.describe()

    if(!haveGlobalSecondaryIndex(desc, indexName)) {
      ProvisionedThroughput THRUPUT = new ProvisionedThroughput(1L, 1L)
      Projection PROJECTION = new Projection().withProjectionType(ProjectionType.ALL)

      CreateGlobalSecondaryIndexAction req = new CreateGlobalSecondaryIndexAction()
      .withIndexName(indexName)
      .withProjection(PROJECTION)
      .withProvisionedThroughput(THRUPUT)

      Index index
      if(idxRangeName) {
        req.withKeySchema(new KeySchemaElement(idxHashName, KeyType.HASH),
          new KeySchemaElement(idxRangeName, KeyType.RANGE))
        index = table.createGSI(
          req,
          new AttributeDefinition(idxHashName, hashType),
          new AttributeDefinition(idxRangeName, rangeType))
      } else {
        req.withKeySchema(new KeySchemaElement(idxHashName, KeyType.HASH))
        index = table.createGSI(
          req,
          new AttributeDefinition(idxHashName, hashType))
      }

      try {
        // Wait for the table to become active
        desc = index.waitForActive()
      } catch (InterruptedException ie) {
        LOG.error(ie.message, ie)
        desc = table.describe()
      }
    }

    return DynamoHelper.tableDescriptionToMap(desc)
  }

  static boolean haveGlobalSecondaryIndex(TableDescription desc, String indexName) {
    final List<GlobalSecondaryIndexDescription> list = desc.getGlobalSecondaryIndexes();
    if (list != null) {
      for (GlobalSecondaryIndexDescription d: list) {
        if (d.getIndexName().equals(indexName)) {
          return true
        }
      }
    }

    return false
  }

  static Map tableDescriptionToMap(TableDescription tableDescription) {
    if (tableDescription) {
      return [
        name: tableDescription.tableName,
        status: tableDescription.tableStatus,
        created: tableDescription.creationDateTime,
        size_bytes: tableDescription.tableSizeBytes,
        item_count: tableDescription.itemCount,
        read_capacity: tableDescription.provisionedThroughput.readCapacityUnits,
        write_capacity: tableDescription.provisionedThroughput.writeCapacityUnits,
      ]
    }
  }
}
