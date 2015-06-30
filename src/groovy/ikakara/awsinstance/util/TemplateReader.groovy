/* Copyright 2014-2015 the original author or authors.
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

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import ikakara.awsinstance.util.StringUtil

@CompileStatic
@Slf4j("LOG")
class TemplateReader {
  BufferedReader reader
  boolean closeReader // close reader after render or toString

  TemplateReader(InputStream input, boolean close = true) {
    reader = new BufferedReader(new InputStreamReader(input))
    closeReader = close
  }

  TemplateReader(String input) {
    reader = new BufferedReader(new StringReader(input))
  }

  void finalize() {
    close()
  }

  void close() {
    if(reader) {
      reader.close()
      reader = null
    }
  }

  String render(Map binding) {
    String str = StringUtil.renderTemplate(reader, binding)
    if(closeReader) {
      close()
    }
    return str
  }

  // Convert a stream into a single, newline separated string
  String toString() throws Exception {
    StringBuilder stringbuilder = new StringBuilder()
    String line = null
    while ((line = reader.readLine()) != null) {
      stringbuilder.append(line + "\n")
    }

    if(closeReader) {
      close()
    }

    return stringbuilder.toString()
  }
}

