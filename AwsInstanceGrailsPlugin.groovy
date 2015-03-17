import ikakara.awsinstance.aws.AuthCredentials

class AwsInstanceGrailsPlugin {
  def version = "0.2"
  def grailsVersion = "2.0 > *"
  def pluginExcludes = [
    "web-app/**"
  ]
  def title = "AWS Instance"
  def author = "Allen Arakaki"
  def description = 'Utilizes a single (thread-safe) AWS Client Instance to access AWS Services: SES, SQS, DynamoDB, S3, Mobile Analytics'
  def documentation = "http://grails.org/plugin/aws-instance"
  def license = "APACHE"
  def issueManagement = [url: 'https://github.com/ikakara-team/grails-aws-instance/issues']
  def scm = [url: 'https://github.com/ikakara-team/grails-aws-instance']

  // merge config ...
  def doWithApplicationContext = { appCtx ->
    println 'Configuring AwsInstance config ...' + application.mergedConfig.conf.grails.plugin.awsinstance

    def conf = appCtx.grailsApplication.config.grails.plugin.awsinstance
    AuthCredentials.instance.init(conf.accessKey, conf.secretKey)

    println '... finished configuring AwsInstance config'
  }

  def afterConfigMerge = {config, ctx ->
    // let's put the mergedConfig in ctx
    ctx.appConfig.grails.plugin.awsinstance.putAll(config.grails.plugin.awsinstance)
  }
}
