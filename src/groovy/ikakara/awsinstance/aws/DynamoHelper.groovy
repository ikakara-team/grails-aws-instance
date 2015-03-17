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
package ikakara.awsinstance.aws;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import grails.util.Holders;
import groovy.util.ConfigObject;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.amazonaws.services.dynamodbv2.model.UpdateTableRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateTableResult;
import com.amazonaws.services.dynamodbv2.util.Tables;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.document.Table;

import ikakara.awsinstance.aws.AWSInstance;
import ikakara.awsinstance.util.AnnotationUtil;
import ikakara.awsinstance.dao.dynamo.ADynamoObject;

/**
 *
 * @author Allen
 */
@Slf4j("LOG")
@CompileStatic
public class DynamoHelper {

  static final String DEFAULT_DATASOURCE = "dataSource_config";

  // Holders.getFlatConfig() doesn't seem to get grailsApplication.mergedConfig
  static public Object getConfigProperty(String str) {
    String[] keys = str.split("\\.");

    Object co = (ConfigObject) Holders.getConfig().get(keys[0]);
    if (co != null) {
      for (int i = 1; i < keys.length; i++) {
        co = ((ConfigObject) co).get(keys[i]);
        if (co instanceof ConfigObject) {
        } else {
          break;
        }
      }
    }

    return co;
  }

  static public String getTableName(Class<?> clazz) {
    return getTableName(clazz, DEFAULT_DATASOURCE);
  }

  static public String getTableName(Class<?> clazz, String dataSource) {
    String retTableName = null;

    String className = clazz.getSimpleName();
    String tableName = AnnotationUtil.findDynamoDBTable(clazz);
    if (tableName == null || "".equals(tableName)) {
      // Use the class name
      tableName = className;
    }

    String dbPrefix = (String) getConfigProperty(dataSource + ".dbPrefix");
    retTableName = dbPrefix + "_" + tableName;

    LOG.info("class:" + className + " tableName:" + retTableName);

    return retTableName;
  }

  static public Map initTable(String tableName, ADynamoObject obj, String dataSource) {
    Map map = null;

    String dbCreate = (String) getConfigProperty(dataSource + ".dbCreate");
    try {
      // TBD -
      if (Tables.doesTableExist(AWSInstance.DYNAMO_CLIENT(), tableName)) {
        LOG.debug("Table " + tableName + " is already ACTIVE");
        if ("create-drop".equals(dbCreate)) {
          // TBD: we should drop and recreate the db
        }
      } else if ("create-drop".equals(dbCreate) || "create".equals(dbCreate)) {
        map = obj.initTable();
        LOG.debug("Table " + tableName + " is not ACTIVE, created " + map);
      }
    } catch (Exception e) {
      LOG.error("Exception:" + tableName + " " + e.getMessage());
    }
    return map;
  }

  static public List<Map> getTableNames() {
    List list = new ArrayList();

    TableCollection<ListTablesResult> ret = AWSInstance.DYNAMO_DB().listTables();
    if (ret != null) {
      Iterator iterator = ret.iterator();
      while (iterator.hasNext()) {
        Table table = (Table) iterator.next();
        Map map = new HashMap();
        map.put("name", table.getTableName());
        map.put("description", table.getDescription());
        list.add(map);
      }
    }

    return list;
  }

  static public Map getTableInformation(String tableName) {
    Map map = null;

    try {
      TableDescription tableDescription = AWSInstance.DYNAMO_CLIENT().describeTable(
              new DescribeTableRequest().withTableName(tableName)).getTable();
      map = tableDescriptionToMap(tableDescription);
    } catch (ResourceNotFoundException rnfe) {

    }

    return map;
  }

  static public Map tableDescriptionToMap(TableDescription tableDescription) {
    HashMap map = null;

    if (tableDescription != null) {
      map = new HashMap();
      map.put("name", tableDescription.getTableName());
      map.put("status", tableDescription.getTableStatus());
      map.put("read_capacity", tableDescription.getProvisionedThroughput().getReadCapacityUnits());
      map.put("write_capacity", tableDescription.getProvisionedThroughput().getWriteCapacityUnits());
    }

    return map;
  }

}
