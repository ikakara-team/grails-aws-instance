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

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.*

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j("LOG")
@CompileStatic
public class AnnotationUtil {

  static public Annotation[] findClassAnnotation(Class<?> clazz) {
    return clazz.getAnnotations()
  }

  static public Annotation[] findMethodAnnotation(Class<?> clazz, String methodName) {

    Annotation[] annotations = null
    try {
      Class<?>[] params = null
      Method method = clazz.getDeclaredMethod(methodName, params)
      if (method != null) {
        annotations = method.getAnnotations()
      }
    } catch (SecurityException e) {
      e.printStackTrace()
    } catch (NoSuchMethodException e) {
      e.printStackTrace()
    }
    return annotations
  }

  static public Annotation[] findFieldAnnotation(Class<?> clazz, String fieldName) {
    Annotation[] annotations = null
    try {
      Field field = clazz.getDeclaredField(fieldName)
      if (field != null) {
        annotations = field.getAnnotations()
      }
    } catch (SecurityException e) {
      e.printStackTrace()
    } catch (NoSuchFieldException e) {
      e.printStackTrace()
    }
    return annotations
  }

  /**
   * @param args
   */
  /*
  static public void main(String[] args) {
  AnnotationUtil ai = new AnnotationUtil()
  Annotation[] annotations
  Class<User> userClass = User.class
  String methodDoStuff = "doStuff"
  String fieldId = "id"
  String fieldAddress = "address"

  // Find class annotations
  annotations = ai.findClassAnnotation(be.fery.annotation.User.class)
  System.out.println("Annotation on class '" + userClass.getName()
  + "' are:")
  showAnnotations(annotations)

  // Find method annotations
  annotations = ai.findMethodAnnotation(User.class, methodDoStuff)
  System.out.println("Annotation on method '" + methodDoStuff + "' are:")
  showAnnotations(annotations)

  // Find field annotations
  annotations = ai.findFieldAnnotation(User.class, fieldId)
  System.out.println("Annotation on field '" + fieldId + "' are:")
  showAnnotations(annotations)

  annotations = ai.findFieldAnnotation(User.class, fieldAddress)
  System.out.println("Annotation on field '" + fieldAddress + "' are:")
  showAnnotations(annotations)

  }
   */
  static public String findDynamoDBTable(Class<?> clazz) {
    String tableName = null

    Annotation[] ann = clazz.getAnnotations()
    if (ann != null) {
      for (Annotation a : ann) {
        tableName = MatcherUtil.extractAnnotation(a.toString(), MatcherUtil.ANNOTATION_DynamoDBTable)
        if (tableName != null) {
          break
        }
      }
    }

    return tableName
  }

  static public Method[] findKeys(Class<?> clazz) {
    Method[] keys = new Method[2]

    Method[] m = clazz.getDeclaredMethods()

    for (int i = 0; i < m.length; i++) {
      Annotation[] ann = m[i].getDeclaredAnnotations()
      if (ann != null) {
        for (Annotation a : ann) {
          String key1 = MatcherUtil.extractAnnotation(a.toString(), MatcherUtil.ANNOTATION_DynamoDBHashKey)
          if (key1 != null) {
            keys[0] = m[i]
            break
          }

          String key2 = MatcherUtil.extractAnnotation(a.toString(), MatcherUtil.ANNOTATION_DynamoDBRangeKey)
          if (key2 != null) {
            keys[1] = m[i]
            break
          }
        }

        if (keys[0] != null && keys[1] != null) {
          break
        }
      }
    }

    return keys
  }

  static public void showAnnotations(Annotation[] ann) {
    if (ann != null) {
      for (Annotation a : ann) {
        System.out.println(a.toString()
          + " " + MatcherUtil.extractAnnotation(a.toString(), MatcherUtil.ANNOTATION_DynamoDBTable)
          + " " + MatcherUtil.extractAnnotation(a.toString(), MatcherUtil.ANNOTATION_DynamoDBHashKey)
          + " " + MatcherUtil.extractAnnotation(a.toString(), MatcherUtil.ANNOTATION_DynamoDBRangeKey)
          + " " + MatcherUtil.extractAnnotation(a.toString(), MatcherUtil.ANNOTATION_DynamoDBAttribute)
        )
      }
    }
  }

  static public void showDeclaredMethods(Class<?> c) {
    Method[] m = c.getDeclaredMethods()
    for (int i = 0; i < m.length; i++) {
      System.out.println(m[i].getName())
      showAnnotations(m[i].getDeclaredAnnotations())
    }
  }

  static public void showDeclaredFields(Class<?> c) {
    Field[] m = c.getDeclaredFields()
    for (int i = 0; i < m.length; i++) {
      System.out.println(m[i].getName())
      showAnnotations(m[i].getDeclaredAnnotations())
    }
  }

  static public void showDeclaredAnnotations(Class<?> c) {
    Annotation[] m = c.getDeclaredAnnotations()
    for (int i = 0; i < m.length; i++) {
      System.out.println(m[i].toString())
    }
  }

}
