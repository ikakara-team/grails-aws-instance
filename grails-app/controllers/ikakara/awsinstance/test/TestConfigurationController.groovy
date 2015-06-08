package ikakara.awsinstance.test

import java.nio.charset.Charset
import java.nio.ByteBuffer

import grails.converters.JSON

import com.amazonaws.services.cloudformation.model.Parameter

class TestConfigurationController {

  def awsConfigurationService

  static int count_create = 0

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

  def index() {
    render awsConfigurationService.listStack()
  }

  def find() {
    render awsConfigurationService.findStack(params.id)
  }

  def create() {
    count_create++

    def template = getTemplate(params.id)

    render awsConfigurationService.createStack(params.id, template, null)
  }

  def check () {
    render awsConfigurationService.waitForCompletion(params.id)
  }

  def delete() {
    render awsConfigurationService.deleteStack(params.id)
  }
}
