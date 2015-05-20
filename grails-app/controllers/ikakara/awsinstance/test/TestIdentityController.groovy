package ikakara.awsinstance.test

import java.nio.charset.Charset
import java.nio.ByteBuffer

import grails.converters.JSON

class TestIdentityController {

  def awsIdentityService

  static int count_create = 0

  def generateReport() {
    def resp = awsIdentityService.generateReport()

    render resp ? resp as JSON : null
  }

  def getReport() {
    def resp = awsIdentityService.getReport()

    render resp ? resp as JSON : null
  }

  def list() {
    def resp = awsIdentityService.listPool()

    render resp ? resp as JSON : null
  }

  def describe() {
    def resp = awsIdentityService.describePool(params.id)

    render resp ? resp as JSON : null
  }

  def create() {
    count_create++

    def poolName = "pool${count_create}"
    def domain = "domain${count_create}"
    def allow = (boolean)count_create % 2

    def resp = awsIdentityService.createPool(poolName, domain, allow)

    render resp ? resp as JSON : null
  }

  def delete() {
    def resp = awsIdentityService.describePool(params.id)
    if(resp) {
      awsIdentityService.deletePool(params.id)
      render "Deleted pool: ${resp}"
    } else {
      render "Failed to delete: ${params.id}"
    }
  }

  def listRolePool() {
    def resp = awsIdentityService.listRolePool(params.id)

    render resp ? resp as JSON : null
  }

  def identities() {
    def resp = awsIdentityService.listIdentity(params.id)

    render resp ? resp as JSON : null
  }

  def id() {
    def resp = awsIdentityService.getDeveloperId(params.id, params.userId)

    render resp
  }

  def token() {
    def resp = awsIdentityService.getDeveloperToken(params.id, params.developerId, params.userId)

    render resp ? resp as JSON : null
  }

  def user() {
    def resp = awsIdentityService.getUser()

    render resp ? resp as JSON : null
  }

  def account() {
    def resp = awsIdentityService.getUserInfo()

    render resp ? resp as JSON : null
  }

  def roles() {
    def resp
    if(params.id) {
      resp = awsIdentityService.getRole(params.id)
    } else {
      resp = awsIdentityService.listRoles()
    }
    render resp ? resp as JSON : null
  }

  def setRolePool() {
    def resp = awsIdentityService.describePool(params.id)
    if(resp) {
      println resp
      awsIdentityService.setRolePool(params.id, 'arn:aws:iam::908857450283:role/Cognito_goofyappAuth_DefaultRole', null)

      resp = awsIdentityService.listRolePool(params.id)

      render "setRolePool on pool: ${resp}"
    } else {
      render "Failed to setRolePool: ${params.id}"
    }
  }

}
