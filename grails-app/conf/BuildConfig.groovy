grails.project.work.dir = 'target'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

  inherits 'global'
  log 'warn'

  repositories {
    mavenLocal()
    grailsCentral()
    mavenCentral()
  }

  dependencies {
    compile ('com.amazonaws:aws-java-sdk:1.10.2') { // http://aws.amazon.com/releasenotes/Java?browse=1
      export = false // allow user to use another version
    }

    // http://mvnrepository.com/artifact/com.fasterxml.jackson.core
    compile 'com.fasterxml.jackson.core:jackson-core:2.5.2'
    compile 'com.fasterxml.jackson.core:jackson-annotations:2.5.2'
    compile 'com.fasterxml.jackson.core:jackson-databind:2.5.2'

    compile 'com.opencsv:opencsv:3.3' // http://opencsv.sourceforge.net/

    compile 'com.github.slugify:slugify:2.1.3' // https://github.com/slugify/slugify
  }

  plugins {
    // needed for testing
    build (":tomcat:8.0.22") {
      export = false
    }

    // needed for config management
    compile ':plugin-config:0.2.1'

    build(':release:3.1.1', ':rest-client-builder:2.1.1') {
      export = false
    }
  }
}
