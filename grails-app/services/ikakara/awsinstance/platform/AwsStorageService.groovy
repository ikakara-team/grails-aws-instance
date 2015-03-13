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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import grails.util.Holders;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ListObjectsRequest
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3Object

import ikakara.awsinstance.aws.AWSInstance
import ikakara.awsinstance.util.PrintlnUtil

class AwsStorageService {

  static transactional = false

  public final int CHUNK_BYTESIZE = 4096
  public final String PUBLIC_BUCKET = Holders.config.grails.plugin.awsinstance?.s3.bucketName;

  public boolean putPublicBytes(String rootfolder, String path, byte[] _bytes, String contentType, Map metadata = null) {
    return putBytes(PUBLIC_BUCKET, rootfolder, path, _bytes, contentType, metadata);
  }

  public def getPublicBytes(String rootfolder, String path) {
    return getBytes(PUBLIC_BUCKET, rootfolder, path);
  }

  public def getPublicText(String rootfolder, String path) {
    def text = ''

    def (content, metadata) = getBytes(PUBLIC_BUCKET, rootfolder, path);
    if(content) {
      text = inputStreamToString(content)
    }

    return [text, metadata]
  }

  public String getPublicObjectURL(String rootfolder, String path) {
    return getObjectURL(PUBLIC_BUCKET, rootfolder, path);
  }

  public String getPublicURL(String key = null) {
    return getURL(PUBLIC_BUCKET, key);
  }

  public ObjectListing getPublicObjectList(String rootfolder, String path) {
    return getObjectList(PUBLIC_BUCKET, rootfolder, path);
  }

  public String deletePublicObject(String rootfolder, String path) {
    return deleteObject(PUBLIC_BUCKET, rootfolder, path);
  }

  public def deleteObject(String lobBucketName, String rootfolder, String path) {
    def _key = "${rootfolder}/${path}";

    try {
      AWSInstance.S3_CLIENT().deleteObject(lobBucketName, _key);
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("deleteObject ${lobBucketName}/${_key}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("deleteObject ${lobBucketName}/${_key}", ace)
    }
  }

  public boolean putBytes(String lobBucketName, String rootfolder, String path, byte[] _bytes, String contentType, Map metadata = null) {
    boolean bSuccess = false;

    def _key = "${rootfolder}/${path}";

    try {
      if(_bytes) {
        InputStream bais = new ByteArrayInputStream(_bytes);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(contentType);
        meta.setContentLength(_bytes.length);
        if(metadata) {
          meta.setUserMetadata(metadata);
        }

        def request = new PutObjectRequest(lobBucketName, _key, bais, meta)
        AWSInstance.S3_CLIENT().putObject(request);
        bSuccess = true;
      }
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("putBytes ${lobBucketName}/${_key}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("putBytes ${lobBucketName}/${_key}", ace)
    }

    return bSuccess;
  }

  public def getBytes(String lobBucketName, String rootfolder, String path) {
    S3Object object = null;

    def _key = "${rootfolder}/${path}";

    try {
      def request = new GetObjectRequest(lobBucketName, _key);
      object = AWSInstance.S3_CLIENT().getObject(request);
    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("getBytes ${lobBucketName}/${_key}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("getBytes ${lobBucketName}/${_key}", ace)
    }

    return [object?.getObjectContent(), object?.getObjectMetadata()];
  }

  public String getURL(String lobBucketName, String key = null, bHostForm = true) {
    String objurl

    if(bHostForm) {
      objurl = "${lobBucketName}.s3.amazonaws.com/"
    } else {
      objurl = "s3.amazonaws.com/${lobBucketName}/"
    }

    if(key) {
      objurl = objurl + key;
    }

    return objurl;
  }

  public String getObjectURL(String lobBucketName, String rootfolder, String path) {
    def _key = "${rootfolder}/${path}";
    return getURL(lobBucketName, _key);
  }

  public ObjectListing getObjectList(String lobBucketName, String rootfolder, String path) {
    ObjectListing objectListing = null;

    def _key = "${rootfolder}/${path}";

    try {
      def request = new ListObjectsRequest()
      .withBucketName(lobBucketName)
      .withPrefix(_key)

      objectListing = AWSInstance.S3_CLIENT().listObjects(request);

    } catch (AmazonServiceException ase) {
      PrintlnUtil.AmazonServiceException("getObjectList ${lobBucketName}/${_key}", ase)
    } catch (AmazonClientException ace) {
      PrintlnUtil.AmazonClientException("getObjectList ${lobBucketName}/${_key}", ace)
    }
    return objectListing;
  }

  private static String inputStreamToString(InputStream input) throws IOException {
    StringBuffer sb = new StringBuffer()
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      sb.append(line).append('\n')
    }
    return sb;
  }

}
