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
package ikakara.awsinstance.dao.dynamo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome
import com.amazonaws.services.dynamodbv2.document.Expected
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.ItemCollection
import com.amazonaws.services.dynamodbv2.document.LowLevelResultListener
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome
import com.amazonaws.services.dynamodbv2.document.QueryOutcome
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException

import ikakara.awsinstance.aws.AWSInstance

/**
 * @author Allen
 */
@CompileStatic
@Slf4j("LOG")
abstract class ADynamoObject implements IDynamoTable {

  // http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/LowLevelJavaItemCRUD.html
  boolean load() {

    if (valueHashKey()) {
      try {
        Item item

        if (nameRangeKey()) {
          item = AWSInstance.DYNAMO_TABLE(tableName()).getItem(nameHashKey(), valueHashKey(), nameRangeKey(), valueRangeKey())
        } else {
          item = AWSInstance.DYNAMO_TABLE(tableName()).getItem(nameHashKey(), valueHashKey())
        }

        LOG.info("load ${tableName()}:${(item ? item.asMap() : null)}")
        if (item) {
          marshalAttributesIN(item)
          return true
        }
      } catch (IllegalArgumentException e) {
        LOG.error("load:$e.message")
      }
    }

    return false
  }

  boolean create() {
    try {
      // Save the item
      Item item = marshalItemOUT(true)
      setKey(item)
      LOG.info("create item:$item")

      Expected expected = setExpectedNotExist()
      PutItemOutcome result = AWSInstance.DYNAMO_TABLE(tableName()).putItem(item, expected)

      return true
    } catch (ConditionalCheckFailedException ccfe) {
      LOG.error("create ${tableName()}:$ccfe.message")
    } catch (e) {
      LOG.error("create:$e.message")
    }
    return false
  }

  // Save all attributes; will remove attributes w/ null
  boolean save() {
    try {
      // Save the item
      Item item = marshalItemOUT(true)
      setKey(item)

      PutItemOutcome result = AWSInstance.DYNAMO_TABLE(tableName()).putItem(item)

      LOG.info("save:$result")
      return true
    } catch (e) {
      LOG.error("save ${tableName()}:$e.message")
    }
    return false
  }

  // only save attributes that are non-null
  boolean update() {
    try {
      // Save the item
      Item item = marshalItemOUT(false)
      setKey(item)

      PutItemOutcome result = AWSInstance.DYNAMO_TABLE(tableName()).putItem(item)

      LOG.info("update:$result")
      return true
    } catch (e) {
      LOG.error("update ${tableName()}:$e.message")
    }
    return false
  }

  boolean delete() {
    try {
      DeleteItemOutcome result

      if (nameRangeKey()) {
        result = AWSInstance.DYNAMO_TABLE(tableName()).deleteItem(nameHashKey(), valueHashKey(), nameRangeKey(), valueRangeKey())
      } else {
        result = AWSInstance.DYNAMO_TABLE(tableName()).deleteItem(nameHashKey(), valueHashKey())
      }

      LOG.info("delete:$result")

      return true
    } catch (e) {
      LOG.error("delete ${tableName()}:$e.message")
    }

    return false
  }

  List<ADynamoObject> scan() {
    return scan(null, null)
  }

  List<ADynamoObject> scan(String WHERE, ValueMap valueMap) {
    try {
      ItemCollection<?> col = AWSInstance.DYNAMO_TABLE(tableName()).scan(
        // filter expression
        WHERE,
        // no attribute name substitution
        null,
        // attribute value substitution
        valueMap
      )

      return processRequestResults(col)
    } catch (e) {
      LOG.error("scan ${tableName()}:$e.message")
    }
  }

  List<ADynamoObject> queryIndex(String indexName, String hashKeyName, Object hashKeyValue) {
    try {
      ItemCollection<QueryOutcome> col = AWSInstance.DYNAMO_INDEX(tableName(), indexName).query(
        hashKeyName, hashKeyValue)
      // can be notified of the low level result if needed
      col.registerLowLevelResultListener(new LowLevelResultListener<QueryOutcome>() {
          void onLowLevelResult(QueryOutcome outcome) {
            LOG.info(outcome.toString())
          }
        })
      return processRequestResults(col)
    } catch (e) {
      LOG.error("queryIndex ${tableName()} $hashKeyName=hashKeyValue:$e.message")
    }
  }

  List<ADynamoObject> queryIndex(String indexName, String hashKeyName, Object hashKeyValue, RangeKeyCondition rangeKeyCondition) {
    try {
      ItemCollection<QueryOutcome> col = AWSInstance.DYNAMO_INDEX(tableName(), indexName).query(
        hashKeyName, hashKeyValue,
        rangeKeyCondition)
      // can be notified of the low level result if needed
      col.registerLowLevelResultListener(new LowLevelResultListener<QueryOutcome>() {
          void onLowLevelResult(QueryOutcome outcome) {
            LOG.info(outcome.toString())
          }
        })
      return processRequestResults(col)
    } catch (e) {
      LOG.error("queryIndex ${tableName()} ${hashKeyName}=$hashKeyValue:$e.message")
    }
  }

  List<ADynamoObject> query(String hashKeyName, Object hashKeyValue) {
    try {
      ItemCollection<QueryOutcome> col = AWSInstance.DYNAMO_TABLE(tableName()).query(hashKeyName, hashKeyValue)
      // can be notified of the low level result if needed
      col.registerLowLevelResultListener(new LowLevelResultListener<QueryOutcome>() {
          void onLowLevelResult(QueryOutcome outcome) {
            LOG.info(outcome.toString())
          }
        })
      return processRequestResults(col)
    } catch (e) {
      LOG.error("query ${tableName()} $hashKeyName=$hashKeyValue:$e.message")
    }
  }

  List<ADynamoObject> query(String hashKeyName, Object hashKeyValue, RangeKeyCondition rangeKeyCondition) {
    try {
      ItemCollection<QueryOutcome> col = AWSInstance.DYNAMO_TABLE(tableName()).query(
        hashKeyName, hashKeyValue,
        rangeKeyCondition)
      // can be notified of the low level result if needed
      col.registerLowLevelResultListener(new LowLevelResultListener<QueryOutcome>() {
          void onLowLevelResult(QueryOutcome outcome) {
            LOG.info(outcome.toString())
          }
        })
      return processRequestResults(col)
    } catch (e) {
      LOG.error("query ${tableName()} $hashKeyName =$hashKeyValue:$e.message")
    }
  }

  List<ADynamoObject> query(String hashKeyName, Object hashKeyValue, RangeKeyCondition rangeKeyCondition, String WHERE, ValueMap valueMap) {
    try {
      ItemCollection<QueryOutcome> col = AWSInstance.DYNAMO_TABLE(tableName()).query(
        hashKeyName, hashKeyValue,
        rangeKeyCondition,
        // filter expression
        WHERE,
        // no attribute name substitution
        null,
        // attribute value substitution
        valueMap)
      // can be notified of the low level result if needed
      col.registerLowLevelResultListener(new LowLevelResultListener<QueryOutcome>() {
          void onLowLevelResult(QueryOutcome outcome) {
            LOG.info(outcome.toString())
          }
        })
      return processRequestResults(col)
    } catch (e) {
      LOG.error("query ${tableName()} $hashKeyName=$hashKeyValue:$e.message")
    }
  }

  private Expected setExpectedNotExist() {
    nameRangeKey() ? new Expected(nameRangeKey()).notExist() : new Expected(nameHashKey()).notExist()
  }

  private Item setKey(Item item) {
    item = item.with(nameHashKey(), valueHashKey())
    if (nameRangeKey()) {
      item = item.with(nameRangeKey(), valueRangeKey())
    }

    return item
  }

  private List<ADynamoObject> processRequestResults(ItemCollection<?> col) {

    if (col == null) {
      return null
    }

    List<ADynamoObject> list = []
    col.each { Item item ->
      try {
        list << newInstance(item)
      } catch (e) {
        LOG.error("processRequestResults", e)
      }
    }
    return list
  }
}
