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

import java.util.Map
import java.util.Date

import groovy.transform.ToString
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore
import com.amazonaws.services.dynamodbv2.document.Item

import ikakara.awsinstance.dao.ICommandObject
import ikakara.awsinstance.util.CalendarUtil

/**
 *
 * @author Allen
 */
@ToString(includePackage=false, ignoreNulls=true, includeSuper=true)
@Slf4j("LOG")
@CompileStatic
abstract public class ACreatedUpdatedObject extends ADynamoObject implements ICommandObject {

  String created_time // YYMMddHHmmss
  String updated_time // YYMMddHHmmss
  // transient
  protected Date createdDate = null
  protected Date updatedDate = null

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
  abstract public String getId()

  @Override
  abstract public void setId(String s)

  @Override
  public void initParameters(Map params) {
    //if (params != null && !params.isEmpty()) {
    created_time = (String) params.get("created_time")
    updated_time = (String) params.get("updated_time")
    //}
  }

  @Override
  public boolean validate() {
    return true // needed to be used as "command object"
  }

  @Override
  abstract public ADynamoObject newInstance(Item item)

  @Override
  public void marshalAttributesIN(Item item) {
    //if (map != null && !map.isEmpty()) {
    if (item.isPresent("CreatedTime")) {
      created_time = item.getString("CreatedTime")
    }
    if (item.isPresent("UpdatedTime")) {
      updated_time = item.getString("UpdatedTime")
    }
    //}
  }

  @Override
  public Item marshalItemOUT(boolean bRemoveAttributeNull) {
    Item outItem = new Item()

    if (created_time != null && !"".equals(created_time)) {
      outItem = outItem.withString("CreatedTime", created_time)
    } else if (bRemoveAttributeNull) {
      outItem = outItem.removeAttribute("CreatedTime")
    }
    if (updated_time != null && !"".equals(updated_time)) {
      outItem = outItem.withString("UpdatedTime", updated_time)
    } else if (bRemoveAttributeNull) {
      outItem = outItem.removeAttribute("UpdatedTime")
    }

    return outItem
  }

  public ACreatedUpdatedObject withCreatedUpdated() {
    Date now = new Date()

    setCreatedDate(now)
    setUpdatedDate(now)
    return this
  }

  public ACreatedUpdatedObject withCreated(Date date) {
    setCreatedDate(date)
    return this
  }

  public ACreatedUpdatedObject withUpdated(Date date) {
    setUpdatedDate(date)
    return this
  }

  public ACreatedUpdatedObject withUpdated() {
    Date now = new Date()

    setUpdatedDate(now)
    return this
  }

  @DynamoDBAttribute(attributeName = "CreatedTime")
  public String getCreatedTime() {
    return created_time
  }

  public void setCreatedTime(String d) {
    created_time = d
  }

  @DynamoDBAttribute(attributeName = "UpdatedTime")
  public String getUpdatedTime() {
    return updated_time
  }

  public void setUpdatedTime(String d) {
    updated_time = d
  }

  @DynamoDBIgnore
  public Date getCreatedDate() {
    if (createdDate == null) {
      if (created_time != null) {
        createdDate = CalendarUtil.getDateFromString_CONCISE_MS(created_time)
      } else {
        setCreatedDate(new Date())
      }
    }
    return createdDate
  }

  public void setCreatedDate(Date d) {
    createdDate = d
    created_time = CalendarUtil.getStringFromDate_CONCISE_MS(d)
  }

  @DynamoDBIgnore
  public Date getUpdatedDate() {
    if (updatedDate == null) {
      if (updated_time != null) {
        updatedDate = CalendarUtil.getDateFromString_CONCISE_MS(updated_time)
      } else {
        setUpdatedDate(new Date())
      }
    }
    return createdDate
  }

  public void setUpdatedDate(Date d) {
    updatedDate = d
    updated_time = CalendarUtil.getStringFromDate_CONCISE_MS(d)
  }

}
