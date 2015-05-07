package ikakara.awsinstance.test

import java.nio.charset.Charset
import java.nio.ByteBuffer

import grails.converters.JSON

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType
import com.amazonaws.services.dynamodbv2.document.Item

import ikakara.awsinstance.aws.AWSInstance
import ikakara.awsinstance.aws.DynamoHelper
import ikakara.awsinstance.dao.dynamo.ACreatedUpdatedObject
import ikakara.awsinstance.dao.dynamo.ADynamoObject

@DynamoDBTable(tableName = "MemberGroups")
class TestMemberGroup extends ACreatedUpdatedObject {
  private static String TABLE_NAME

  protected String memberId // mobile, email, hash
  protected String groupId

  @Override
  synchronized String tableName() {
    if (!TABLE_NAME) {
      TABLE_NAME = DynamoHelper.getTableName(TestMemberGroup, "grails.plugin.awsorguserteam.dataSource")
      DynamoHelper.initTable(TABLE_NAME, this, "grails.plugin.awsorguserteam.dataSource")
    }
    return TABLE_NAME
  }

  @Override
  Map initTable() {
    Map map = DynamoHelper.getTableInformation(tableName())

    return map
  }

  @Override
  def valueHashKey() {
    memberId
  }

  @Override
  String nameHashKey() {
    return "MemberId"
  }

  @Override
  def valueRangeKey() {
    groupId
  }

  @Override
  String nameRangeKey() {
    return "GroupId"
  }

  @Override
  void marshalAttributesIN(Item item) {
    super.marshalAttributesIN(item)
    //if (map) {
    if (item.isPresent("MemberId")) {
      memberId = item.getString("MemberId")
    }
    if (item.isPresent("GroupId")) {
      groupId = item.getString("GroupId")
    }
    //}
  }

  @Override
  String getId() {
    return memberId + "_" + groupId
  }

  @Override
  void setId(String id) {
    if (!id) {
      return
    }

    String[] ids = id.split("_")
    if (ids.length < 2) {
      return
    }

    groupId = ids[1]
    // fix broken contactId
    String str = ids[0]
    if (str.startsWith(" ")) {
      str = str.replaceFirst(" ", "+")
    }
    memberId = str
  }

  @Override
  ADynamoObject newInstance(Item item) {
    return this
  }

  TestMemberGroup() {
  }

  TestMemberGroup(Map params) {
    initParameters(params)
  }

}
