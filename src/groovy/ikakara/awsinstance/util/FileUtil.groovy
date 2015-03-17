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

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j


/**
 *
 * @author Allen
 */
@Slf4j("LOG")
@CompileStatic
class FileUtil {

  static public final long IMAGE_SIZE_LIMIT = 5242880   // 5mb
  static public final long VIDEO_SIZE_LIMIT = 20971520  // 20mb

  static public final int RANDOMCHARS_LENGTH = 10  // 10 digits

  static public final List ACCEPTABLE_ASSETFILE_TYPES = [
    'png',
    'gif',
    'jpg',
    'jpeg',
    'css',
    'js',
    'mp4',
  ]
  static public final List ACCEPTABLE_IMAGEFILE_TYPES = [
    'png',
    'gif',
    'jpg',
    'jpeg',
  ]
  static public final List ACCEPTABLE_VIDEOFILE_TYPES = [
    'mp4',
  ]
  static public final List ACCEPTABLE_JSFILE_TYPES = [
    'js',
  ]
  static public final List ACCEPTABLE_CSSFILE_TYPES = [
    'css',
  ]

  // requires Java7
  static byte[] readAllBytes(File file) {
    byte[] data = Files.readAllBytes(file.toPath());
    return data
  }

  static String generateRandomFileName(String prefix, String ext = null) {
    String random_chars = StringUtil.getRandomChars(RANDOMCHARS_LENGTH);
    return (prefix ? "${prefix}_" : '') + CalendarUtil.getStringFromDate_CONCISE(new Date()) + "_${random_chars}" + (ext ? ".${ext}" : '')
  }

  static boolean isValidExtension(String filename, List ACCEPTABLE_EXTENSIONS) {
    List ret = getValidExtension(filename, ACCEPTABLE_EXTENSIONS)
    return ret[0]
  }

  static List getValidExtension(String filename, List ACCEPTABLE_EXTENSIONS) {
    def extension = null
    def isValid = true

    int dotPos = filename.lastIndexOf(".")
    if (dotPos != -1) {
      extension = filename.substring(dotPos + 1).toLowerCase()
      if(!ACCEPTABLE_EXTENSIONS?.contains(extension)) {
        LOG.warn "File extension ${extension} is not valid."
        isValid = false
      }
    } else {
      LOG.warn "No extension found on file"
      isValid = false
    }

    return [isValid, extension]
  }
}
