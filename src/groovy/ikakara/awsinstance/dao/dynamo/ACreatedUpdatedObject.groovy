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
import groovy.transform.ToString
import groovy.util.logging.Slf4j

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore
import com.amazonaws.services.dynamodbv2.document.Item

import ikakara.awsinstance.dao.ICommandObject
import ikakara.awsinstance.util.CalendarUtil

/**
 * @author Allen
 */
@CompileStatic
@Slf4j("LOG")
@ToString(includePackage=false, includeNames=true, ignoreNulls=true)
abstract class ACreatedUpdatedObject extends ADynamoObject implements ICommandObject {

  @DynamoDBAttribute(attributeName = "CreatedTime")
  String createdTime // YYMMddHHmmss

  @DynamoDBAttribute(attributeName = "UpdatedTime")
  String updatedTime // YYMMddHHmmss

  // transient
  protected Date createdDate
  protected Date updatedDate
  protected boolean writeOverCreated = false

  void initParameters(Map params) {
    //if (params) {
    createdTime = (String) params.createdTime
    updatedTime = (String) params.updatedTime
    //}
  }

  boolean validate() {
    return true // needed to be used as "command object"
  }

  abstract ADynamoObject newInstance(Item item)

  void marshalAttributesIN(Item item) {
    //if (map) {
    if (item.isPresent("CreatedTime")) {
      createdTime = item.getString("CreatedTime")
    }
    if (item.isPresent("UpdatedTime")) {
      updatedTime = item.getString("UpdatedTime")
    }
    //}
  }

  Item marshalItemOUT(boolean removeAttributeNull) {
    Item outItem = new Item()
    if(writeOverCreated) {
      if (createdTime) {
        outItem = outItem.withString("CreatedTime", createdTime)
      } else if (removeAttributeNull) {
        outItem = outItem.removeAttribute("CreatedTime")
      }
    }
    if (updatedTime) {
      outItem = outItem.withString("UpdatedTime", updatedTime)
    } else if (removeAttributeNull) {
      outItem = outItem.removeAttribute("UpdatedTime")
    }

    return outItem
  }

  ACreatedUpdatedObject withCreatedUpdated() {
    Date now = new Date()
    return withCreated(now).withUpdated(now)
  }

  ACreatedUpdatedObject withCreated(Date date) {
    setCreatedDate(date)
    return this
  }

  ACreatedUpdatedObject withCreated() {
    return withCreated(new Date())
  }

  ACreatedUpdatedObject withUpdated(Date date) {
    setUpdatedDate(date)
    return this
  }

  ACreatedUpdatedObject withUpdated() {
    return withUpdated(new Date())
  }

  ACreatedUpdatedObject withWriteOverCreated(boolean b) {
    writeOverCreated = b
    return this
  }

  @DynamoDBIgnore
  Date getCreatedDate() {
    if (!createdDate) {
      if (createdTime) {
        createdDate = CalendarUtil.getDateFromString_CONCISE_MS(createdTime)
      } else {
        // preserve value of writeOverCreated
        setDateCreated(new Date())
      }
    }
    return createdDate
  }

  void setCreatedDate(Date d) {
    writeOverCreated = true
    setDateCreated(d)
  }

  protected void setDateCreated(Date d) {
    createdDate = d
    createdTime = CalendarUtil.getStringFromDate_CONCISE_MS(d)
  }

  @DynamoDBIgnore
  Date getUpdatedDate() {
    if (!updatedDate) {
      if (updatedTime) {
        updatedDate = CalendarUtil.getDateFromString_CONCISE_MS(updatedTime)
      } else {
        setUpdatedDate(new Date())
      }
    }
    return updatedDate
  }

  void setUpdatedDate(Date d) {
    updatedDate = d
    updatedTime = CalendarUtil.getStringFromDate_CONCISE_MS(d)
  }

  @DynamoDBIgnore
  long getCreatedDaysAgo() {
    return CalendarUtil.getDateDiff(new Date(), getCreatedDate(), Calendar.DATE)
  }

  @DynamoDBIgnore
  long getUpdatedDaysAgo() {
    return CalendarUtil.getDateDiff(new Date(), getUpdatedDate(), Calendar.DATE)
  }
}
