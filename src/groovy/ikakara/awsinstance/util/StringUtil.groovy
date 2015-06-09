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

import groovy.text.GStringTemplateEngine
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import com.github.slugify.Slugify

@CompileStatic
@Slf4j("LOG")
class StringUtil {
  static final String RANDOM_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
  static final int RANDOM_CHARS_LENGTH = RANDOM_CHARS.length()

  static String renderTemplate(Reader reader, Map binding) {
    return new GStringTemplateEngine().createTemplate(reader).make(binding)
  }

  static String slugify(String str) throws IOException {
    return new Slugify().slugify(str)
  }

  static String getRandomNumbers(int numChar) {
    return getRandomChars(numChar, 10)
  }

  static String getRandomChars(int numChar) {
    return getRandomChars(numChar, RANDOM_CHARS_LENGTH)
  }

  static String getRandomCharsLength(int min, int max) {
    return getRandomChars(NumberUtil.getRandomInt(min, max), RANDOM_CHARS_LENGTH)
  }

  static String getRandomChars(int numChar, int range) {
    // assert range <= RANDOM_CHARS_LENGTH
    SecureRandom rand = new SecureRandom()
    StringBuilder sbStr = new StringBuilder()
    numChar.times { sbStr << RANDOM_CHARS.charAt(rand.nextInt(range)) }
    return sbStr.toString()
  }

  /**
   * Split the source into two strings at the first occurrence of the splitter
   * Subsequent occurrences are not treated specially, and may be part of the
   * second string.
   *
   * @param source The string to split
   * @param splitter The string that forms the boundary between the two strings
   * returned.
   * @return An array of two strings split from source by splitter.
   */
  static String[] splitFirst(String source, String splitter) {
    String[] ret_array
    int last = 0

    // find first splitter in source
    int next = source.indexOf(splitter, last)
    if (next != -1) {
      ret_array = new String[2]
      // isolate from last thru before next
      ret_array[0] = source.substring(last, next)
      last = next + splitter.length()

      if (last < source.length()) {
        ret_array[1] = source.substring(last, source.length())
      }
    } else {
      // didn't find splitter
      ret_array = new String[1]
      ret_array[0] = source
    }

    return ret_array
  }

  // This is a hack to encode periods
  static String urlEncodeExt(String str) {
    try {
      return URLEncoder.encode(str.replaceAll("\\.", "%2E") /*replace period*/, "UTF8")
    } catch (UnsupportedEncodingException e) {
      LOG.error("urlEncode:$str", e)
      return ''
    }
  }

  static String urlEncode(String str) {
    try {
      return URLEncoder.encode(str, "UTF8")
    } catch (UnsupportedEncodingException e) {
      LOG.error("urlEncode:$str", e)
      return ''
    }
  }

  static String urlDecode(String str) {
    try {
      return URLDecoder.decode(str, "UTF8")
    } catch (UnsupportedEncodingException e) {
      LOG.error("urlDecode:$str", e)
    }
    return ''
  }
}
