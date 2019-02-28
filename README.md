# Mi-Go-Plugin
Some MyBatis Generator plugins used in the work
## How to use 
Download the code and install it using maven,If you don't know how to use maven, please google it.
Add the following dependency to your MyBatis Generator configuration.
```
<dependency>
      <groupId>com.duanxr</groupId>
      <artifactId>mi-go-plugin</artifactId>
      <version>1.0.0-SNAPSHOT</version>
</dependency>
```
## IsExistsPlugin
This plugin adds two new methods to your Mapper：
* public boolean isExistsByExample(Example example);
* public boolean isExistsByPrimaryKey(Long id);

They return a boolean that has at least one row of data based on criteria.

If you want to enable this plugin, add the following text to the context tag in your configuration file.
```
<plugin type="com.duanxr.migo.plugins.IsExistsPlugin"/>
```
## SerializablePlugin
This plugin add Serializable interface to the model objects,compared with the official plugin, it can generate serialVersionUID based on class hash value.

If you want to enable this plugin, add the following text to the context tag in your configuration file.
```
<plugin type="com.duanxr.migo.plugins.SerializablePlugin">
    <property name="addGWTInterface" value="false"/>
    <property name="toJsonRuleList" value="false"/>
</plugin>
```

This plugin accepts two properties:
* addGWTInterface True/False
If true, the plugin will add the Google Web Toolkit (GWT) IsSerializable interface to the model objects. The default is false.
* suppressJavaInterface True/False
If true, the plugin will NOT add the java.io.Serializable interface. This is for the case where the objects should be serializable for GWT, but not in the strict Java sense. The default is false.

## JsonStringPlugin
This plugin adds two new methods to your Mapper：
* public String toJsonString();
    This method serializes model object into a Json string.
* public static T parseJsonString(String jsonString);
    This method deserializes a Json string into a model object.
    
These methods are based on [fastjson](https://github.com/alibaba/fastjson), ensuring that the target project already contains it.

If you want to enable this plugin, add the following text to the context tag in your configuration file.
```
<plugin type="com.duanxr.migo.plugins.JsonStringPlugin">  
    <property name="addToExample" value="true"/>  
    <property name="toJsonRuleList" value="WriteNullListAsEmpty,IgnoreNonFieldGetter"/>  
    <property name="parseJsonRuleList" value=""/>
</plugin>
```
This plugin accepts three properties:
* addToExample True/False
If true, the plugin will add the method to example model objects. The default is false.
* toJsonRuleList/parseJsonRuleList
If you want to change the serialization/deserialization parameters, use [the fastjson SerializerFeature](https://github.com/alibaba/fastjson) as the value and multiple values with the comma.

## UseGeneratedKeysPlugin
This plugin makes your insert method will backfill the database generated key to the model object, so you can call the object getId() method to get the inserted row primary key.

If you want to enable this plugin, add the following text to the context tag in your configuration file.
```
<plugin type="com.duanxr.migo.plugins.UseGeneratedKeysPlugin"/>
```