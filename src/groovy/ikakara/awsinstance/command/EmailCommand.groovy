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
package ikakara.awsinstance.command;

import org.codehaus.groovy.grails.web.util.StreamCharBuffer
import groovy.text.GStringTemplateEngine

/**
 *
 * @author Allen
 */
@grails.validation.Validateable(nullable=true)
public class EmailCommand {
  List<String> to = new ArrayList<String>()
  String subject = ''
  String html = ''
  String text = ''

  public EmailCommand() {}

  //to
  public EmailCommand withTo(String str) {
    to.add(str)
    return this
  }

  public EmailCommand withTo(List<String> list) {
    to = list
    return this
  }

  //subject
  public EmailCommand withSubject(String str) {
    subject = str
    return this
  }

  //text
  public EmailCommand withText(String str) {
    text = str
    return this
  }

  public EmailCommand withText(Reader reader, Map binding) {
    return withText(renderTemplate(reader, binding))
  }

  public EmailCommand withText(String template, Map binding) {
    return withText(new StringReader(template), binding)
  }

  //html
  public EmailCommand withHtml(String str) {
    html = str
    return this
  }

  public EmailCommand withHtml(StreamCharBuffer scb) {
    html = scb.toString()
    return this
  }

  public EmailCommand withHtml(Reader reader, Map binding) {
    return withHtml(renderTemplate(reader, binding))
  }

  public EmailCommand withHtml(String template, Map binding) {
    return withHtml(new StringReader(template), binding)
  }

  private String renderTemplate(Reader reader, Map binding) {
    def engine = new GStringTemplateEngine()
    def template = engine.createTemplate(reader).make(binding)
    return template.toString()
  }
}
