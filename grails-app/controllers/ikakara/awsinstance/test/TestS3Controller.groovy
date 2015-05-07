package ikakara.awsinstance.test

import grails.converters.JSON

import ikakara.awsinstance.aws.AWSInstance

class TestS3Controller {

  def awsStorageService

  static int count_create = 0

  def index() {
    def list = awsStorageService.listBuckets()
    render list ? list as JSON : "No buckets found"
  }

  def create() {
    def ret = awsStorageService.createBucket(params.id)
    render ret ? "Created bucket: ${params.id}" : "Failed to create bucket: ${params.id}"
  }

  def delete() {
    def ret = awsStorageService.deleteEmptyBucket(params.id)
    render ret ? "Deleted bucket: ${params.id}" : "Failed to delete bucket: ${params.id}"
  }
}
