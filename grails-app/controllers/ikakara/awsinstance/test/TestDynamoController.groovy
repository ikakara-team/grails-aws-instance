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

import ikakara.awsinstance.test.TestMemberGroup

class TestDynamoController {

  static int count_create = 0

  def index() {
    def list = AWSInstance.DYNAMO_CLIENT().listTables()
    render list ? list.tableNames as JSON : "No tables found"
  }

  def describe() {
    def map = AWSInstance.DYNAMO_CLIENT().describeTable(new DescribeTableRequest().withTableName(params.id)).getTable()

    //def map = DynamoHelper.getTableInformation(params.id)
    render map ? map as JSON : "Table not found: '${params.id}'"
  }

  def gsi() {
    def table = AWSInstance.DYNAMO_TABLE(params.id)

    def map = DynamoHelper.createTableGlobalSecondaryIndex(
      table,
      'Idx_Awesome',
      'Awesome', ScalarAttributeType.S,
      'Stuff', ScalarAttributeType.S)

    //def map = DynamoHelper.getTableInformation(params.id)
    render map ? map as JSON : "Table not found: '${params.id}'"
  }


  def item() {
    def test = new TestMemberGroup().withUpdated()
    test.memberId = 'memid'
    test.groupId = 'grpid'

    test.save()

    println "test: ${test.writeOverCreated}"

    render test
  }

}
