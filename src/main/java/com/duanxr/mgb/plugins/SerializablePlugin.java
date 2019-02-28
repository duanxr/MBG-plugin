package com.duanxr.mgb.plugins;

import com.duanxr.mgb.plugins.util.SerialVersionUIDUtil;
import java.util.List;
import java.util.Properties;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.PrimitiveTypeWrapper;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * add serializable interface and generate SerialVersionUID
 * @author Duanxr
 */
public class SerializablePlugin extends PluginAdapter {

  private static final String INTERFACE_SERIALIZABLE = "java.io.Serializable";
  private static final String INTERFACE_GOOGLE_SERIALIZABLE = "com.google.gwt.user.client.rpc.IsSerializable";
  private FullyQualifiedJavaType serializable;
  private FullyQualifiedJavaType gwtSerializable;
  private boolean addGWTInterface;
  private boolean suppressJavaInterface;

  @Override
  public void initialized(IntrospectedTable introspectedTable) {
    super.initialized(introspectedTable);
    serializable = new FullyQualifiedJavaType(INTERFACE_SERIALIZABLE);
    gwtSerializable = new FullyQualifiedJavaType(INTERFACE_GOOGLE_SERIALIZABLE);
  }

  @Override
  public boolean validate(List<String> warnings) {
    return true;
  }

  @Override
  public void setProperties(Properties properties) {
    super.setProperties(properties);
    addGWTInterface = Boolean.parseBoolean(properties.getProperty("addGWTInterface"));
    suppressJavaInterface = Boolean.parseBoolean(properties.getProperty("suppressJavaInterface"));
  }

  @Override
  public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    makeSerializable(topLevelClass, introspectedTable);
    return true;
  }

  @Override
  public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    makeSerializable(topLevelClass, introspectedTable);
    return true;
  }

  @Override
  public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    makeSerializable(topLevelClass, introspectedTable);
    return true;
  }

  /**
   * 添加给 Example 类序列化的方法
   */
  @Override
  public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    makeSerializable(topLevelClass, introspectedTable);

    for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
      if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) {
        innerClass.addSuperInterface(serializable);

        Field field = new Field();
        field.setFinal(true);

        field.setInitializationString(getSerialVersionUID(innerClass));
        field.setName("serialVersionUID");
        field.setStatic(true);
        field.setType(new FullyQualifiedJavaType("long"));
        field.setVisibility(JavaVisibility.PRIVATE);
        innerClass.addField(field);

        PrimitiveTypeWrapper booleanWrapper =
            PrimitiveTypeWrapper.getBooleanPrimitiveInstance().getPrimitiveTypeWrapper();

        Field valid = new Field();
        valid.setName("valid");
        valid.setVisibility(JavaVisibility.PRIVATE);
        valid.setType(booleanWrapper);
        valid.addAnnotation("@SuppressWarnings(\"unused\")");
        innerClass.addField(valid);

        Method setValid = new Method();
        setValid.setVisibility(JavaVisibility.PUBLIC);
        setValid.setName("setValid");
        setValid.addParameter(new Parameter(booleanWrapper, "valid"));
        setValid.addBodyLine("this.valid = valid;");
        innerClass.addMethod(setValid);

      }
      if ("Criteria".equals(innerClass.getType().getShortName())) {
        innerClass.addSuperInterface(serializable);

        Method criteria = new Method();
        criteria.setVisibility(JavaVisibility.PUBLIC);
        criteria.setConstructor(true);
        criteria.setName("Criteria");
        criteria.addBodyLine("super();");
        innerClass.addMethod(criteria);

        Field field = new Field();
        field.setFinal(true);
        field.setInitializationString(getSerialVersionUID(innerClass));
        field.setName("serialVersionUID");
        field.setStatic(true);
        field.setType(new FullyQualifiedJavaType("long"));
        field.setVisibility(JavaVisibility.PRIVATE);
        innerClass.addField(field);
      }
      if ("Criterion".equals(innerClass.getType().getShortName())) {
        innerClass.addSuperInterface(serializable);

        Method criterion = new Method();
        criterion.setVisibility(JavaVisibility.PUBLIC);
        criterion.setConstructor(true);
        criterion.setName("Criterion");
        criterion.addBodyLine("super();");
        innerClass.addMethod(criterion);

        Field field = new Field();
        field.setFinal(true);
        field.setInitializationString(getSerialVersionUID(innerClass));
        field.setName("serialVersionUID");
        field.setStatic(true);
        field.setType(new FullyQualifiedJavaType("long"));
        field.setVisibility(JavaVisibility.PRIVATE);
        innerClass.addField(field);
      }
    }
    return true;
  }

  private void makeSerializable(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    if (addGWTInterface) {
      topLevelClass.addImportedType(gwtSerializable);
      topLevelClass.addSuperInterface(gwtSerializable);
    }

    if (!suppressJavaInterface) {
      topLevelClass.addImportedType(serializable);
      topLevelClass.addSuperInterface(serializable);
      Field field = new Field();
      field.setFinal(true);
      field.setInitializationString(getSerialVersionUID(topLevelClass));
      field.setName("serialVersionUID");
      field.setStatic(true);
      field.setType(new FullyQualifiedJavaType("long"));
      field.setVisibility(JavaVisibility.PRIVATE);
      context.getCommentGenerator().addFieldComment(field, introspectedTable);

      topLevelClass.addField(field);
    }
  }

  private String getSerialVersionUID(InnerClass cl) {
    try {
      SerialVersionUIDUtil svu = new SerialVersionUIDUtil();
      long serialID = svu.getSerialVersionUID(cl);
      return String.valueOf(serialID) + 'L';
    } catch (Exception e) {
      return "1L";
    }
  }

}
