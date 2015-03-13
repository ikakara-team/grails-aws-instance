# grails-aws-instance

Description:
--------------
Grails plugin to utilize a single (thread-safe) AWS Client Instance.  Provides
clients/services to access AWS Services for email and storage.

Installation:
--------------
```
  plugins {
...
    compile ':aws-instance:0.1'
...
  }
```

Configuration:
--------------

Add the following to grails-app/conf/Config.groovy:
```
grails {
  plugin {
    awsinstance {
      accessKey='AWS_ACCESSKEY'
      secretKey='AWS_SECRETKEY'
      s3.bucketName='AWS_S3_BUCKETNAME'
      ses.mailFrom='AWS_SES_MAILFROM'
    }
  }
}
```

Usage:
--------------
Example sending an email:
```
 def binding = [
     firstname : "Grace",
     lastname  : "Hopper",
     accepted  : true,
     title     : 'Groovy for COBOL programmers'
 ]
 def text = '''\
 Dear <%= firstname %> $lastname,

 We <% if (accepted) print 'are pleased' else print 'regret' %> \
 to inform you that your paper entitled
 '$title' was ${ accepted ? 'accepted' : 'rejected' }.

 The conference committee.
 '''
def html = '''
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <meta name="viewport" content="width=device-width" />
</head>
<body>
  <p>Dear <%= firstname %> $lastname,</p>
  <p>We <% if (accepted) print 'are pleased' else print 'regret' %> \
to inform you that your paper entitled '$title' was ${ accepted ? 'accepted' : 'rejected' }.</p>
  <br/>
  <p>The conference committee.</p>
</body>
</html>
'''

def email = new EmailCommand()
              .withTo('to@example.com')
              .withSubject('Testing 1, 2, 3 ...')
              .withText(text, binding)
              .withHtml(html, binding)
try {
  awsEmailService.send(email);
} catch(EmailException e) {
  log.error("Exception while sending email: " + e);
}
```

Example of storing an image file in the S3 bucket:
```
def USERS_FOLDERS
def fullKey = user_id + "/" + filename;
File imgFile // input 

if (awsStorageService.putPublicBytes(USERS_FOLDER, fullKey, imgFile.getBytes(), imgFile.getContentType(), [date:(new Date()).toString()])) {
  def uploadedFullFileUrl = awsStorageService.getPublicObjectURL(USERS_FOLDER, fullKey)
  log.info("Drink: Uploaded full size image: ${uploadedFullFileUrl}")
} else {
  log.error("save_imgFile failed: ${fullKey} ${filename}")
}
```

Copyright & License:
--------------
Copyright 2014-2015 Allen Arakaki.  All Rights Reserved.

```
Apache 2 License - http://www.apache.org/licenses/LICENSE-2.0
```

History:
--------------
0.1 - initial checkin