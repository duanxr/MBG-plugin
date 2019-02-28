/*
 * Copyright (c) 2017.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duanxr.mgb.plugins;

import java.util.List;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;


/**
 * make insert method call model setId method to set generated key.
 * @author Duanxr
 */
public class UseGeneratedKeysPlugin extends PluginAdapter {

  private static final String USE_GENERATED_KEYS = "useGeneratedKeys";
  private static final String TRUE = "true";
  private static final String KEY_PROPERTY = "keyProperty";
  private static final String RECORD = "record.";

  @Override
  public boolean validate(List<String> warnings) {
    return true;
  }

  @Override
  public boolean sqlMapInsertElementGenerated(XmlElement element,
      IntrospectedTable introspectedTable) {
    Attribute attribute = new Attribute(USE_GENERATED_KEYS, TRUE);
    element.addAttribute(attribute);
    return true;
  }

  @Override
  public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element,
      IntrospectedTable introspectedTable) {
    Attribute useGeneratedKeys = new Attribute(USE_GENERATED_KEYS, TRUE);
    element.addAttribute(useGeneratedKeys);
    if (introspectedTable.hasPrimaryKeyColumns()) {
      String name = introspectedTable.getPrimaryKeyColumns().get(0).getJavaProperty();
      Attribute keyProperty = new Attribute(KEY_PROPERTY, RECORD + name);
      element.addAttribute(keyProperty);
    }
    return true;
  }


}
