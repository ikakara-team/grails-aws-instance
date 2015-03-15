/* Copyright 2014-2015 Allen Arakaki.  All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ikakara.awsinstance.platform

import org.springframework.beans.factory.InitializingBean

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ListObjectsRequest
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.S3Object

import ikakara.awsinstance.aws.AWSInstance
import ikakara.awsinstance.util.PrintlnUtil

class AwsStorageService implements InitializingBean {

  static transactional = false

  public static final int CHUNK_BYTESIZE = 4096

  public static String PUBLIC_BUCKET

  def grailsApplication

  boolean putPublicBytes(String rootfolder, String path, byte[] _bytes, String contentType, Map metadata = null) {
    return putBytes(PUBLIC_BUCKET, rootfolder, path, _bytes, contentType, metadata)
  }

  def getPublicBytes(String rootfolder, String path) {
    return getBytes(PUBLIC_BUCKET, rootfolder, path)
  }

  def getPublicText(String rootfolder, String path) {

    def (InputStream content, metadata) = getBytes(PUBLIC_BUCKET, rootfolder, path)
    String text = content ? inputStreamToString(content) : ''

    return [text, metadata]
  }

  String getPublicObjectURL(String rootfolder, String path) {
    return getObjectURL(PUBLIC_BUCKET, rootfolder, path)
  }

  String getPublicURL(String key = null) {
    return getURL(PUBLIC_BUCKET, key)
  }

  ObjectListing getPublicObjectList(String rootfolder, String path) {
    return getObjectList(PUBLIC_BUCKET, rootfolder, path)
  }

  String deletePublicObject(String rootfolder, String path) {
    return deleteObject(PUBLIC_BUCKET, rootfolder, path)
  }

  def deleteObject(String lobBucketName, String rootfolder, String path) {
    def _key = "${rootfolder}/${path}"

    try {
      AWSInstance.S3_CLIENT().deleteObject(lobBucketName, _key)
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("deleteObject ${lobBucketName}/${_key}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("deleteObject ${lobBucketName}/${_key}", ace)
    }
  }

  boolean putBytes(String lobBucketName, String rootfolder, String path, byte[] _bytes, String contentType, Map metadata = null) {

    def _key = "${rootfolder}/${path}"

    try {
      if(_bytes) {
        InputStream bais = new ByteArrayInputStream(_bytes)
        ObjectMetadata meta = new ObjectMetadata(contentType: contentType, contentLength: _bytes.length)
        if(metadata) {
          meta.userMetadata = metadata
        }

        AWSInstance.S3_CLIENT().putObject(new PutObjectRequest(lobBucketName, _key, bais, meta))
        return true
      }
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("putBytes ${lobBucketName}/${_key}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("putBytes ${lobBucketName}/${_key}", ace)
    }

    return false
  }

  def getBytes(String lobBucketName, String rootfolder, String path) {
    S3Object object

    def _key = "${rootfolder}/${path}"

    try {
      object = AWSInstance.S3_CLIENT().getObject(new GetObjectRequest(lobBucketName, _key))
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("getBytes ${lobBucketName}/${_key}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("getBytes ${lobBucketName}/${_key}", ace)
    }

    return [object?.objectContent, object?.objectMetadata]
  }

  String getURL(String lobBucketName, String key = null, hostForm = true) {
    return hostForm ? "${lobBucketName}.s3.amazonaws.com/" : "s3.amazonaws.com/${lobBucketName}/" + (key ?: '')
  }

  String getObjectURL(String lobBucketName, String rootfolder, String path) {
    return getURL(lobBucketName, "${rootfolder}/${path}")
  }

  ObjectListing getObjectList(String lobBucketName, String rootfolder, String path) {

    def _key = "${rootfolder}/${path}"

    try {
      def request = new ListObjectsRequest()
      .withBucketName(lobBucketName)
      .withPrefix(_key)

      return AWSInstance.S3_CLIENT().listObjects(request)

    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("getObjectList ${lobBucketName}/${_key}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("getObjectList ${lobBucketName}/${_key}", ace)
    }
  }

  private static String inputStreamToString(InputStream input) throws IOException {
    StringBuffer sb = new StringBuffer()
    BufferedReader reader = new BufferedReader(new InputStreamReader(input))
    while (true) {
      String line = reader.readLine()
      if (line == null) {
        break
      }
      sb << line << '\n'
    }
    return sb
  }

  void afterPropertiesSet() throws Exception {
    PUBLIC_BUCKET = grailsApplication.config.grails.plugin.awsinstance?.s3.bucketName
  }
}
