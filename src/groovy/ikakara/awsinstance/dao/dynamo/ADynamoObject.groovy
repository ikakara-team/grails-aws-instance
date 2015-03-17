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

import java.util.List
import java.util.ArrayList
import java.util.Map

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException

import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.ItemCollection
import com.amazonaws.services.dynamodbv2.document.Expected
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import com.amazonaws.services.dynamodbv2.document.LowLevelResultListener
import com.amazonaws.services.dynamodbv2.document.QueryOutcome
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition

import ikakara.awsinstance.aws.AWSInstance

/**
 *
 * @author Allen
 */
@Slf4j("LOG")
@CompileStatic
abstract public class ADynamoObject implements IDynamoTable {

  @Override
  abstract public String tableName()

  @Override
  abstract public Map initTable()

  @Override
  abstract public Object valueHashKey() // future: return can be number or binary

  @Override
  abstract public Object valueRangeKey()// future: return can be number or binary

  @Override
  abstract public String nameHashKey()

  @Override
  abstract public String nameRangeKey()

  @Override
  abstract public void marshalAttributesIN(Item item)

  @Override
  abstract public Item marshalItemOUT(boolean bRemoveAttributeNull)

  @Override
  abstract public ADynamoObject newInstance(Item item)

  // http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/LowLevelJavaItemCRUD.html
  public boolean load() {
    boolean bRet = false

    if (valueHashKey() != null) {
      try {
        Item item = null

        if (nameRangeKey() != null && !"".equals(nameRangeKey())) {
          item = AWSInstance.DYNAMO_TABLE(tableName()).getItem(nameHashKey(), valueHashKey(), nameRangeKey(), valueRangeKey())
        } else {
          item = AWSInstance.DYNAMO_TABLE(tableName()).getItem(nameHashKey(), valueHashKey())
        }

        LOG.info("load " + tableName() + ":" + (item != null ? item.asMap() : null))
        if (item != null) {
          marshalAttributesIN(item)
          bRet = true
        }
      } catch (IllegalArgumentException e) {
        LOG.error("load:" + e.getMessage())
      }
    }

    return bRet
  }

  public boolean create() {
    boolean bRet = false
    try {
      // Save the item
      Item item = marshalItemOUT(true)
      setKey(item)
      LOG.info("create item:" + item)

      Expected expected = setExpectedNotExist()
      PutItemOutcome result = AWSInstance.DYNAMO_TABLE(tableName()).putItem(item, expected)

      bRet = true
    } catch (ConditionalCheckFailedException ccfe) {
      LOG.warn("create " + tableName() + ":" + ccfe.getMessage())
    } catch (Exception e) {
      LOG.error("create:" + e.getMessage())
    }
    return bRet
  }

  // Save all attributes; will remove attributes w/ null
  public boolean save() {
    boolean bRet = false
    try {
      // Save the item
      Item item = marshalItemOUT(true)
      setKey(item)

      PutItemOutcome result = AWSInstance.DYNAMO_TABLE(tableName()).putItem(item)

      LOG.info("save:" + result)
      bRet = true
    } catch (Exception e) {
      LOG.error("save " + tableName() + ":" + e.getMessage())
    }
    return bRet
  }

  // only save attributes that are non-null
  public boolean update() {
    boolean bRet = false
    try {
      // Save the item
      Item item = marshalItemOUT(false)
      setKey(item)

      PutItemOutcome result = AWSInstance.DYNAMO_TABLE(tableName()).putItem(item)

      LOG.info("update:" + result)
      bRet = true
    } catch (Exception e) {
      LOG.error("update " + tableName() + ":" + e.getMessage())
    }
    return bRet
  }

  public boolean delete() {
    boolean bRet = false

    try {
      DeleteItemOutcome result = null

      if (nameRangeKey() != null && !"".equals(nameRangeKey())) {
        result = AWSInstance.DYNAMO_TABLE(tableName()).deleteItem(nameHashKey(), valueHashKey(), nameRangeKey(), valueRangeKey())
      } else {
        result = AWSInstance.DYNAMO_TABLE(tableName()).deleteItem(nameHashKey(), valueHashKey())
      }

      LOG.info("delete:" + result)

      bRet = true
    } catch (Exception e) {
      LOG.error("delete " + tableName() + ":" + e.getMessage())
    }

    return bRet
  }

  public List<ADynamoObject> scan() {
    return scan(null, null)
  }

  public List<ADynamoObject> scan(String WHERE, ValueMap valueMap) {
    List<ADynamoObject> list = null

    try {
      ItemCollection<?> col = AWSInstance.DYNAMO_TABLE(tableName()).scan(
        // filter expression
        WHERE,
        // no attribute name substitution
        null,
        // attribute value substitution
        valueMap
      )

      list = processRequestResults(col)
    } catch (Exception e) {
      LOG.error("scan " + tableName() + ":" + e.getMessage())
    }

    return list
  }

  public List<ADynamoObject> queryIndex(String indexName, String hashKeyName, Object hashKeyValue) {
    List<ADynamoObject> list = null

    try {
      ItemCollection<QueryOutcome> col = AWSInstance.DYNAMO_INDEX(tableName(), indexName).query(
        hashKeyName, hashKeyValue)
      // can be notified of the low level result if needed
      col.registerLowLevelResultListener(new LowLevelResultListener<QueryOutcome>() {
          @Override
          public void onLowLevelResult(QueryOutcome outcome) {
            LOG.info(outcome.toString())
          }
        })
      list = processRequestResults(col)
    } catch (Exception e) {
      LOG.error("queryIndex " + tableName() + " " + hashKeyName + "=" + hashKeyValue + ":" + e.getMessage())
    }

    return list
  }

  public List<ADynamoObject> queryIndex(String indexName, String hashKeyName, Object hashKeyValue, RangeKeyCondition rangeKeyCondition) {
    List<ADynamoObject> list = null

    try {
      ItemCollection<QueryOutcome> col = AWSInstance.DYNAMO_INDEX(tableName(), indexName).query(
        hashKeyName, hashKeyValue,
        rangeKeyCondition)
      // can be notified of the low level result if needed
      col.registerLowLevelResultListener(new LowLevelResultListener<QueryOutcome>() {
          @Override
          public void onLowLevelResult(QueryOutcome outcome) {
            LOG.info(outcome.toString())
          }
        })
      list = processRequestResults(col)
    } catch (Exception e) {
      LOG.error("queryIndex " + tableName() + " " + hashKeyName + "=" + hashKeyValue + ":" + e.getMessage())
    }

    return list
  }

  public List<ADynamoObject> query(String hashKeyName, Object hashKeyValue) {
    List<ADynamoObject> list = null

    try {
      ItemCollection<QueryOutcome> col = AWSInstance.DYNAMO_TABLE(tableName()).query(hashKeyName, hashKeyValue)
      // can be notified of the low level result if needed
      col.registerLowLevelResultListener(new LowLevelResultListener<QueryOutcome>() {
          @Override
          public void onLowLevelResult(QueryOutcome outcome) {
            LOG.info(outcome.toString())
          }
        })
      list = processRequestResults(col)
    } catch (Exception e) {
      LOG.error("query " + tableName() + " " + hashKeyName + "=" + hashKeyValue + ":" + e.getMessage())
    }

    return list
  }

  public List<ADynamoObject> query(String hashKeyName, Object hashKeyValue, RangeKeyCondition rangeKeyCondition) {
    List<ADynamoObject> list = null

    try {
      ItemCollection<QueryOutcome> col = AWSInstance.DYNAMO_TABLE(tableName()).query(
        hashKeyName, hashKeyValue,
        rangeKeyCondition)
      // can be notified of the low level result if needed
      col.registerLowLevelResultListener(new LowLevelResultListener<QueryOutcome>() {
          @Override
          public void onLowLevelResult(QueryOutcome outcome) {
            LOG.info(outcome.toString())
          }
        })
      list = processRequestResults(col)
    } catch (Exception e) {
      LOG.error("query " + tableName() + " " + hashKeyName + "=" + hashKeyValue + ":" + e.getMessage())
    }

    return list
  }

  public List<ADynamoObject> query(String hashKeyName, Object hashKeyValue, RangeKeyCondition rangeKeyCondition, String WHERE, ValueMap valueMap) {
    List<ADynamoObject> list = null

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
          @Override
          public void onLowLevelResult(QueryOutcome outcome) {
            LOG.info(outcome.toString())
          }
        })
      list = processRequestResults(col)
    } catch (Exception e) {
      LOG.error("query " + tableName() + " " + hashKeyName + "=" + hashKeyValue + ":" + e.getMessage())
    }

    return list
  }

  private Expected setExpectedNotExist() {
    Expected expected = null

    if (nameRangeKey() != null && !"".equals(nameRangeKey())) {
      expected = new Expected(nameRangeKey()).notExist()
    } else {
      expected = new Expected(nameHashKey()).notExist()
    }

    return expected
  }

  private Item setKey(Item item) {
    item = item.with(nameHashKey(), valueHashKey())
    if (nameRangeKey() != null && !"".equals(nameRangeKey())) {
      item = item.with(nameRangeKey(), valueRangeKey())
    }

    return item
  }

  private List<ADynamoObject> processRequestResults(ItemCollection<?> col) {
    List<ADynamoObject> list = null

    if (col != null) {
      list = new ArrayList<>()

      for (Item item : col) {
        try {
          ADynamoObject obj = newInstance(item)
          list.add(obj)
        } catch (Exception e) {
          LOG.error("processRequestResults", e)
        }
      }
    }

    return list
  }

}
