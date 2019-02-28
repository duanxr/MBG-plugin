package com.duanxr.migo.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * add method toJsonString and parseJsonString
 *
 * @author Duanxr
 */
public class JsonStringPlugin extends PluginAdapter {

  private static final String CLASS_ALIBABA_FASTJSON_JSON = "com.alibaba.fastjson.JSON";
  private static final String CLASS_ALIBABA_FASTJSON_JSON_FIELD = "com.alibaba.fastjson.annotation.JSONField";
  private static final String CLASS_ALIBABA_FASTJSON_JSON_SERIALIZER_FEATURE = "com.alibaba.fastjson.serializer.SerializerFeature";
  private FullyQualifiedJavaType json;
  private FullyQualifiedJavaType jSONField;
  private FullyQualifiedJavaType serializerFeature;
  private boolean addToExample;
  private List<String> toJsonRuleList;
  private List<String> parseJsonRuleList;

  public JsonStringPlugin() {
    super();
    toJsonRuleList = new ArrayList<>();
    parseJsonRuleList = new ArrayList<>();
    json = new FullyQualifiedJavaType(CLASS_ALIBABA_FASTJSON_JSON);
    jSONField = new FullyQualifiedJavaType(CLASS_ALIBABA_FASTJSON_JSON_FIELD);
    serializerFeature =
        new FullyQualifiedJavaType(CLASS_ALIBABA_FASTJSON_JSON_SERIALIZER_FEATURE);
  }

  @Override
  public boolean validate(List<String> warnings) {
    return true;
  }

  @Override
  public void setProperties(Properties properties) {
    super.setProperties(properties);
    addToExample = Boolean.valueOf(properties.getProperty("addToExample"));
    String tojsonrules = properties.getProperty("toJsonRuleList");
    if (tojsonrules != null && tojsonrules.length() > 0) {
      String[] tojsonrulearray = tojsonrules.split(",");
      toJsonRuleList.addAll(Arrays.asList(tojsonrulearray));
    }

    String parsejsonrules = properties.getProperty("parseJsonRuleList");
    if (parsejsonrules != null && parsejsonrules.length() > 0) {
      String[] parsejsonrulearray = parsejsonrules.split(",");
      parseJsonRuleList.addAll(Arrays.asList(parsejsonrulearray));
    }
  }

  @Override
  public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    if (addToExample) {
      generateJsonComponent(topLevelClass);
    }
    return true;
  }

  @Override
  public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    generateJsonComponent(topLevelClass);
    return true;
  }

  @Override
  public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    generateJsonComponent(topLevelClass);
    return true;
  }

  @Override
  public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    generateJsonComponent(topLevelClass);
    return true;
  }

  private void generateJSONField(TopLevelClass topLevelClass) {
    try {
      List<InnerClass> innerclases = topLevelClass.getInnerClasses();
      for (InnerClass innerclass : innerclases) {
        List<Method> methods = innerclass.getMethods();
        for (Method method : methods) {
          if ("getAllCriteria".equals(method.getName())) {
            addImport(topLevelClass, jSONField);
            method.addAnnotation("@JSONField(serialize=false)");
          }
        }
      }
    } catch (Exception ignored) {
    }
  }

  private void generateJsonComponent(TopLevelClass topLevelClass) {
    try {
      generateJsonImport(topLevelClass);
      generateToJsonString(topLevelClass);
      generateParseJsonString(topLevelClass);
    } catch (Exception ignored) {
    }
  }

  private void generateJsonImport(TopLevelClass topLevelClass) {
    addImport(topLevelClass, json);
    if (toJsonRuleList.size() > 0 || parseJsonRuleList.size() > 0) {
      addImport(topLevelClass, serializerFeature);
    }
  }

  private void addImport(TopLevelClass topLevelClass,
      FullyQualifiedJavaType addimport) {
    Set<FullyQualifiedJavaType> list = topLevelClass.getImportedTypes();
    for (FullyQualifiedJavaType fullyQualifiedJavaType : list) {
      if (fullyQualifiedJavaType.getFullyQualifiedName()
          .equals(addimport.getFullyQualifiedName())) {
        return;
      }
    }
    topLevelClass.addImportedType(addimport);
  }

  private void generateToJsonString(TopLevelClass topLevelClass) {
    Method method = new Method();
    method.setVisibility(JavaVisibility.PUBLIC);
    method.setReturnType(FullyQualifiedJavaType.getStringInstance());
    method.setName("toJsonString");
    StringBuilder str = new StringBuilder();
    str.append("return JSON.toJSONString(this");
    for (String rule : toJsonRuleList) {
      str.append(",SerializerFeature.");
      str.append(rule);
    }
    str.append(");");
    method.addBodyLine(str.toString());
    topLevelClass.addMethod(method);
  }

  private void generateParseJsonString(TopLevelClass topLevelClass) {
    Method method = new Method();
    method.setVisibility(JavaVisibility.PUBLIC);
    method.setName("parseJsonString");
    method.setStatic(true);
    Parameter parameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "json");
    method.addParameter(parameter);
    FullyQualifiedJavaType type = topLevelClass.getType();
    method.setReturnType(type);
    StringBuilder str = new StringBuilder();
    str.append("return JSON.parseObject(json,");
    str.append(type.getFullyQualifiedName());
    str.append(".class");
    for (String rule : parseJsonRuleList) {
      str.append(",SerializerFeature.");
      str.append(rule);
    }
    str.append(");");
    method.addBodyLine(str.toString());

    topLevelClass.addMethod(method);
  }

}
