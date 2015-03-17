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
package ikakara.awsinstance.dao.dynamo;

import java.util.Map;

import com.amazonaws.services.dynamodbv2.document.Item;

/**
 *
 * @author Allen
 */
public interface IDynamoTable {

  String tableName();

  Map initTable();

  Object valueHashKey(); // future: return can be number or binary

  Object valueRangeKey();// future: return can be number or binary

  String nameHashKey();

  String nameRangeKey();

  void marshalAttributesIN(Item item);

  Item marshalItemOUT(boolean bRemoveAttributeNull);

  ADynamoObject newInstance(Item item);

}
