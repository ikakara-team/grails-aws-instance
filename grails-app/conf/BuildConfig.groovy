grails.project.work.dir = 'target'

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {

  inherits("global")
  log "warn"

  repositories {
    grailsCentral()
    mavenLocal()
    mavenCentral()
  }

  dependencies {
    compile ('com.amazonaws:aws-java-sdk:1.9.24') { // http://aws.amazon.com/releasenotes/Java?browse=1
      export = false
    }

    // http://mvnrepository.com/artifact/com.fasterxml.jackson.core
    compile 'com.fasterxml.jackson.core:jackson-core:2.5.1'
    compile 'com.fasterxml.jackson.core:jackson-annotations:2.5.1'
    compile 'com.fasterxml.jackson.core:jackson-databind:2.5.1'

    compile 'com.github.slugify:slugify:2.1.3' // https://github.com/slugify/slugify
  }

  plugins {
    // needed for testing
    build (":tomcat:8.0.20" ){ // 8.0.18 has issues - https://jira.grails.org/browse/GPTOMCAT-29
      export = false
    }

    // needed for config management
    compile ':plugin-config:0.2.0'

    build(":release:3.0.1",
          ":rest-client-builder:1.0.3") {
      export = false
    }
  }
}
