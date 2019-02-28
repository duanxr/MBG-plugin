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
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;


/**
 * row exists method
 * @author Duanxr
 */
public class IsExistsPlugin extends PluginAdapter {

  private static final String METHOD_IS_EXISTS_BY_PRIMARY_KEY = "isExistsByPrimaryKey";
  private static final String METHOD_IS_EXISTS_BY_EXAMPLE = "isExistsByExample";

  @Override
  public boolean validate(List<String> warnings) {
    return true;
  }

  @Override
  public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
      IntrospectedTable introspectedTable) {
    generateIsExistsByPrimaryKeyMethod(method, interfaze);
    return true;
  }

  @Override
  public boolean clientCountByExampleMethodGenerated(Method method, Interface interfaze,
      IntrospectedTable introspectedTable) {
    generateIsExistsByExampleMethod(method, interfaze);
    return true;
  }

  @Override
  public boolean sqlMapDocumentGenerated(Document document,
      IntrospectedTable introspectedTable) {
    generateIsExistsByPrimaryKeySqlXml(document, introspectedTable);
    generateIsExistsByExampleSqlXml(document, introspectedTable);
    return true;
  }

  private void generateIsExistsByPrimaryKeyMethod(Method method, Interface interfaze) {
    Method isExistsByPrimaryKey = new Method(METHOD_IS_EXISTS_BY_PRIMARY_KEY);
    isExistsByPrimaryKey.setVisibility(JavaVisibility.PUBLIC);
    isExistsByPrimaryKey.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
    List<Parameter> parameters = method.getParameters();
    if (parameters != null) {
      for (Parameter parameter : parameters) {
        isExistsByPrimaryKey.addParameter(parameter);
      }
    }
    interfaze.addMethod(isExistsByPrimaryKey);
  }

  private void generateIsExistsByExampleMethod(Method method, Interface interfaze) {
    Method isExistsByExample = new Method(METHOD_IS_EXISTS_BY_EXAMPLE);
    isExistsByExample.setVisibility(JavaVisibility.PUBLIC);
    isExistsByExample.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
    List<Parameter> parameters = method.getParameters();
    if (parameters != null) {
      for (Parameter parameter : parameters) {
        isExistsByExample.addParameter(parameter);
      }
    }
    interfaze.addMethod(isExistsByExample);
  }

  private void generateIsExistsByPrimaryKeySqlXml(Document document,
      IntrospectedTable introspectedTable) {
    XmlElement isExistsByPrimaryKey = new XmlElement("select");
    isExistsByPrimaryKey.addAttribute(new Attribute("id", METHOD_IS_EXISTS_BY_PRIMARY_KEY));
    isExistsByPrimaryKey
        .addAttribute(new Attribute("resultType",
            FullyQualifiedJavaType.getBooleanPrimitiveInstance().getPrimitiveTypeWrapper()
                .getFullyQualifiedName()));
    List<IntrospectedColumn> introspectedColumnList = introspectedTable.getPrimaryKeyColumns();
    if (introspectedColumnList.size() == 1) {
      isExistsByPrimaryKey
          .addAttribute(new Attribute("parameterType",
              introspectedColumnList.get(0).getFullyQualifiedJavaType()
                  .getFullyQualifiedNameWithoutTypeParameters()));
    } else {
      isExistsByPrimaryKey
          .addAttribute(new Attribute("parameterType", introspectedTable.getPrimaryKeyType()));
    }
    StringBuilder sb = new StringBuilder();
    sb.append("select exists(select 1 from ");
    sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
    boolean and = false;
    for (IntrospectedColumn introspectedColumn : introspectedColumnList) {
      if (and) {
        sb.append("  and ");
      } else {
        sb.append(" where ");
        and = true;
      }
      sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
      sb.append(" = ");
      sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, null));
    }
    sb.append(')');
    isExistsByPrimaryKey.addElement(new TextElement(sb.toString()));
    document.getRootElement().addElement(isExistsByPrimaryKey);
  }

  private void generateIsExistsByExampleSqlXml(Document document,
      IntrospectedTable introspectedTable) {
    XmlElement isExistsByExample = new XmlElement("select");
    isExistsByExample.addAttribute(new Attribute("id", METHOD_IS_EXISTS_BY_EXAMPLE));
    isExistsByExample
        .addAttribute(new Attribute("resultType",
            FullyQualifiedJavaType.getBooleanPrimitiveInstance().getPrimitiveTypeWrapper()
                .getFullyQualifiedName()));
    isExistsByExample
        .addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
    String sb = "select exists(select 1 from "
        + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
    isExistsByExample.addElement(new TextElement(sb));
    XmlElement ifElement = new XmlElement("if");
    ifElement.addAttribute(new Attribute("test", "_parameter != null"));
    XmlElement includeElement = new XmlElement("include");
    includeElement.addAttribute(new Attribute("refid", "Example_Where_Clause"));
    ifElement.addElement(includeElement);
    isExistsByExample.addElement(ifElement);
    isExistsByExample.addElement(new TextElement(")"));
    document.getRootElement().addElement(isExistsByExample);
  }
}
