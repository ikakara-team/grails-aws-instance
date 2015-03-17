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

import java.io.UnsupportedEncodingException
import java.io.IOException
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.SecureRandom

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import com.github.slugify.Slugify

/**
 *
 * @author Allen
 */
@Slf4j("LOG")
@CompileStatic
public class StringUtil {
  static final String RANDOM_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  static final int RANDOM_CHARS_LENGTH = RANDOM_CHARS.length();

  public static String slugify(String str) throws IOException {
    Slugify slg = new Slugify();
    String s = slg.slugify(str);
    return s;
  }

  public static String getRandomNumbers(int numChar) {
    return getRandomChars(numChar, 10);
  }

  public static String getRandomChars(int numChar) {
    return getRandomChars(numChar, RANDOM_CHARS_LENGTH);
  }

  public static String getRandomCharsLength(int min, int max) {
    int num = NumberUtil.getRandomInt(min, max);
    return getRandomChars(num, RANDOM_CHARS_LENGTH);
  }

  public static String getRandomChars(int numChar, int range) {
    // assert range <= RANDOM_CHARS_LENGTH
    SecureRandom rand = new SecureRandom();
    StringBuilder sbStr = new StringBuilder();
    for (int i = 0; i < numChar; i++) {
      sbStr.append(RANDOM_CHARS.charAt(rand.nextInt(range)));
    }
    return sbStr.toString();
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
  static public String[] splitFirst(String source, String splitter) {
    String[] ret_array = null;
    int last = 0;
    int next = 0;

    // find first splitter in source
    next = source.indexOf(splitter, last);
    if (next != -1) {
      ret_array = new String[2];
      // isolate from last thru before next
      ret_array[0] = source.substring(last, next);
      last = next + splitter.length();

      if (last < source.length()) {
        ret_array[1] = source.substring(last, source.length());
      }
    } else {
      // didn't find splitter
      ret_array = new String[1];
      ret_array[0] = source;
    }

    return ret_array;
  }

  // This is a hack to encode periods
  static public String urlEncodeExt(String str) {
    String encoded_str = "";
    try {
      encoded_str = str.replaceAll("\\.", "%2E"); // replace period
      encoded_str = URLEncoder.encode(encoded_str, "UTF8");
    } catch (UnsupportedEncodingException e) {
      StringBuilder msg = new StringBuilder("urlEncode:").append(str);
      LOG.error(msg.toString(), e);
    }
    return encoded_str;
  }

  static public String urlEncode(String str) {
    String encoded_str = "";
    try {
      encoded_str = URLEncoder.encode(str, "UTF8");
    } catch (UnsupportedEncodingException e) {
      StringBuilder msg = new StringBuilder("urlEncode:").append(str);
      LOG.error(msg.toString(), e);
    }
    return encoded_str;
  }

  static public String urlDecode(String str) {
    String decoded_str = "";
    try {
      decoded_str = URLDecoder.decode(str, "UTF8");
    } catch (UnsupportedEncodingException e) {
      StringBuilder msg = new StringBuilder("urlDecode:").append(str);
      LOG.error(msg.toString(), e);
    }
    return decoded_str;
  }

}
