package ikakara.awsinstance.test

import java.nio.charset.Charset
import java.nio.ByteBuffer

import grails.converters.JSON

class TestConfigurationController {

  def awsConfigurationService

  def getTemplate(name) {
    return """
{
    "Resources" : {
        "${name}" : {
            "Type" : "AWS::S3::Bucket",
            "Properties" : {
               "AccessControl" : "PublicRead"
            }
        }
    }
}
    """
  }

  static int count_create = 0


  def index() {
    render awsConfigurationService.listStack()
  }

  def find() {
    render awsConfigurationService.findStack(params.id)
  }

  def create() {
    count_create++

    def template = getTemplate(params.id)

    render awsConfigurationService.createStack(params.id, template)
  }

  def check () {
    render awsConfigurationService.waitForCompletion(params.id)
  }

  def delete() {
    render awsConfigurationService.deleteStack(params.id)
  }
}
