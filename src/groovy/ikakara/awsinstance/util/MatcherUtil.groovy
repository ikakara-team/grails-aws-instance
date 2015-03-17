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
package ikakara.awsinstance.util

//import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 *
 * @author Allen
 */
@Slf4j("LOG")
//@CompileStatic
public class MatcherUtil {
  //@com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable(tableName=ConfigRDS)
  static public final ANNOTATION_DynamoDBTable = /@*DynamoDBTable(\(tableName=(\w+)\))?/
  //@com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey(attributeName=name)
  static public final ANNOTATION_DynamoDBHashKey = /@*DynamoDBHashKey(\(attributeName=(\w+)\))?/
  //@com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey(attributeName=name)
  static public final ANNOTATION_DynamoDBRangeKey = /@*DynamoDBRangeKey(\(attributeName=(\w+)\))?/
  //@com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute(attributeName=name)
  static public final ANNOTATION_DynamoDBAttribute = /@*DynamoDBAttribute(\(attributeName=(\w+)\))?/

  static public String extractAnnotation(String annotation, def pattern) {
    String tableName = null;
    def matcher = ( annotation =~ pattern )
    if(matcher && matcher[0]) {
      tableName = "";
      if(matcher[0].size() > 2) {
        tableName = matcher[0][2];
      }
    }
    return tableName;
  }

}

