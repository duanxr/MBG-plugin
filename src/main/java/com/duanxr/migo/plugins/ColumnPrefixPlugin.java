package com.duanxr.migo.plugins;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.List;
import java.util.Map;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * add table name as prefix for column
 * @author Duanran 2019/2/28 0028
 */
public class ColumnPrefixPlugin extends PluginAdapter {

  private static final String DOT = ".";
  private static final String COLUMN = "column";
  private BiMap<String, String> map;

  @Override
  public boolean validate(List<String> warnings) {
    return true;
  }

  @Override
  public void initialized(IntrospectedTable introspectedTable) {
    List<IntrospectedColumn> allColumns = introspectedTable.getAllColumns();
    String tableName = introspectedTable.getTableConfiguration().getTableName();
    map = HashBiMap.create(allColumns.size());
    introspectedTable.getAllColumns().forEach(column -> {
      String actualColumnName = column.getActualColumnName();
      String actualColumnNameWithTableName = tableName + DOT + column.getActualColumnName();
      map.put(actualColumnName, actualColumnNameWithTableName);
    });
    addTableName(introspectedTable);
  }

  @Override
  public boolean sqlMapResultMapWithoutBLOBsElementGenerated(
      XmlElement element, IntrospectedTable introspectedTable) {
    fixTableName(element, introspectedTable);
    return true;
  }

  @Override
  public boolean sqlMapResultMapWithBLOBsElementGenerated(XmlElement element,
      IntrospectedTable introspectedTable) {
    fixTableName(element, introspectedTable);
    return true;
  }

  private void fixTableName(XmlElement element, IntrospectedTable introspectedTable) {
    BiMap<String, String> inverse = map.inverse();
    element.getElements().forEach(e -> {
      if (e instanceof XmlElement) {
        List<Attribute> attributeList = ((XmlElement) e).getAttributes();
        for (int i = 0; i < attributeList.size(); i++) {
          Attribute attribute = attributeList.get(i);
          if (attribute.getName().equals(COLUMN) && inverse.containsKey(attribute.getValue())) {
            Attribute fixAttribute = new Attribute(COLUMN, inverse.get(attribute.getValue()));
            attributeList.set(i, fixAttribute);
          }
        }
      }
    });
  }

  private void addTableName(IntrospectedTable introspectedTable) {
    introspectedTable.getAllColumns().forEach(column -> {
      if (map.containsKey(column.getActualColumnName())) {
        column.setActualColumnName(map.get(column.getActualColumnName()));
      }
    });
  }

  private void removeTableName(IntrospectedTable introspectedTable) {
    introspectedTable.getAllColumns().forEach(column -> {
      if (map.inverse().containsKey(column.getActualColumnName())) {
        column.setActualColumnName(map.inverse().get(column.getActualColumnName()));
      }
    });
  }
}
