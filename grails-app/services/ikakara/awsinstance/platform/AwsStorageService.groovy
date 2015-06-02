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

import grails.compiler.GrailsCompileStatic
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Slf4j

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

@GrailsCompileStatic
@Slf4j
class AwsStorageService implements InitializingBean {
  static transactional = false

  public static final int CHUNK_BYTESIZE = 4096

  public static String PUBLIC_BUCKET
  public static String PUBLIC_BUCKET_HOST

  def grailsApplication

  @GrailsCompileStatic(TypeCheckingMode.SKIP)
  void afterPropertiesSet() {
    // We should throw an exception if the PUBLIC_BUCKET contains any dots
    PUBLIC_BUCKET = grailsApplication.config.grails.plugin.awsinstance?.s3.bucketName

    // can't do this 'sub.domain.com.s3.amazonaws.com' because wildcard ssl doesn't do dots
    // using dashes, like sub-domain-com.s3.amazonaws.com works
    // cname like sub.domain.com -> sub-domain-com.s3.amazonaws.com requires
    // http://aws.amazon.com/cloudfront/custom-ssl-domains/ - $600!!!
    PUBLIC_BUCKET_HOST = PUBLIC_BUCKET + '.s3.amazonaws.com'
    // discovered this https://bryce.fisher-fleig.org/blog/setting-up-ssl-on-aws-cloudfront-and-s3/
    // will investigate how to integrate this w/ this plugin
  }

  String getPublicBucketHost() {
    return PUBLIC_BUCKET_HOST
  }

  boolean putPublicBytes(String rootfolder, String path, byte[] _bytes, String contentType, Map metadata = null) {
    return putBytes(PUBLIC_BUCKET, rootfolder, path, _bytes, contentType, metadata)
  }

  def getPublicBytes(String rootfolder, String path) {
    return getBytes(PUBLIC_BUCKET, rootfolder, path)
  }

  @GrailsCompileStatic(TypeCheckingMode.SKIP)
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

  void deletePublicObject(String rootfolder, String path) {
    deleteObject(PUBLIC_BUCKET, rootfolder, path)
  }

  void deletePublicURL(String key) {
    deleteObject(PUBLIC_BUCKET, key)
  }

  /////////////////////////////////////////////////////////////////////////////

  def listBuckets() {
    AWSInstance.S3_CLIENT().listBuckets()
  }

  Boolean createBucket(String bucketName) {
    try {
      if(!(AWSInstance.S3_CLIENT().doesBucketExist(bucketName))) {
        // Note that CreateBucketRequest does not specify region. So bucket is
        // created in the region specified in the client.
        AWSInstance.S3_CLIENT().createBucket(bucketName)
        return true
      }
      return null
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("createBucket ${bucketName}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("createBucket ${bucketName}", ace)
    }

    return false
  }

  Boolean deleteEmptyBucket(String bucketName) {
    try {
      if(AWSInstance.S3_CLIENT().doesBucketExist(bucketName)) {
        AWSInstance.S3_CLIENT().deleteBucket(bucketName)
        return true
      }
      return null // doesn't exist
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("createBucket ${bucketName}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("createBucket ${bucketName}", ace)
    }

    return false
  }

  void deleteObject(String lobBucketName, String rootfolder, String path) {
    def _key = "${rootfolder}/${path}"
    deleteObject(lobBucketName, _key)
  }

  void deleteObject(String lobBucketName, String key) {
    try {
      AWSInstance.S3_CLIENT().deleteObject(lobBucketName, key)
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("deleteObject ${lobBucketName}/${key}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("deleteObject ${lobBucketName}/${key}", ace)
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

  @GrailsCompileStatic(TypeCheckingMode.SKIP)
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
    return (hostForm ? "${lobBucketName}.s3.amazonaws.com/" : "s3.amazonaws.com/${lobBucketName}/") + (key ?: '')
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
    return null
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
}

