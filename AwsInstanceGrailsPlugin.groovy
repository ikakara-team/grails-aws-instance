import grails.util.Holders

class AwsInstanceGrailsPlugin {
  def version = "0.1"
  def grailsVersion = "2.0 > *"
  def pluginExcludes = [
    "grails-app/views/index.gsp",
    "grails-app/views/error.gsp",
    "grails-app/i18n/*",
    "web-app/**/*"
  ]

  def title = "AWS Instance"
  def author = "Allen Arakaki"
  def authorEmail = ""
  def description = '''
Grails plugin to utilize a single (thread-safe) AWS Client Instance.  Provides 
clients/services to access AWS Services: SES, SQS, DynamoDB, S3, Mobile Analytics.
'''
  def documentation = "http://grails.org/plugin/aws-instance"
  def license = "APACHE"
  def issueManagement = [url: 'https://github.com/ikakara-team/grails-aws-instance/issues']
  def scm = [url: 'https://github.com/ikakara-team/grails-aws-instance']


  // merge config ...
  def doWithApplicationContext = { appCtx ->
    println 'Configuring AwsInstance config ...' + application.mergedConfig.conf.grails.plugin.awsinstance

    println '... finished configuring AwsInstance config'
  }

  def afterConfigMerge = {config, ctx ->
    // let's put the mergedConfig in ctx
    def awsinstance = config.grails.plugin.awsinstance
    ctx.appConfig.grails.plugin.awsinstance.putAll(awsinstance)

    // check Holders.config 
    Holders.config.grails.plugin.awsinstance.each { println "awsinstance afterConfigMerge " + it }
  }
}
