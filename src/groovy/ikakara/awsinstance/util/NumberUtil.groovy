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

import java.security.SecureRandom
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 *
 * @author Allen
 */
@Slf4j("LOG")
@CompileStatic
public class NumberUtil {

  static public int getRandomInt(int min, int max) {
    SecureRandom rand = new SecureRandom()
    int random_int = rand.nextInt(max - min + 1) + min
    return random_int
  }

  static public void shuffleArray(int[] array) {
    int index, temp
    SecureRandom random = new SecureRandom()
    for (int i = array.length - 1; i > 0; i--) {
      index = random.nextInt(i + 1)
      temp = array[index]
      array[index] = array[i]
      array[i] = temp
    }
  }

}
