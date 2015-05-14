package ikakara.awsinstance.test

import java.nio.charset.Charset
import java.nio.ByteBuffer

import grails.converters.JSON

class TestPolicyController {

  def awsIdentityService

  static int count_create = 0

  def list() {
    def resp = awsIdentityService.listLocalPolicies(params.id)

    render resp ? resp as JSON : null
  }

  def create() {
    count_create++

    def policyName = "policy${count_create}"
    def description = "description${count_create}"

    def policy = '''
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "s3:*",
      "Resource": "*"
    }
  ]
}
    '''

    policy = policy.replaceAll("\\s+","")

    def resp = awsIdentityService.createLocalPolicy(null, policyName, policy, description)

    render resp ? resp as JSON : null
  }

  def delete() {
    def info = awsIdentityService.getUserInfo()

    def resp = awsIdentityService.deleteLocalPolicy(info[1], null, params.id)

    render resp

  }
}
