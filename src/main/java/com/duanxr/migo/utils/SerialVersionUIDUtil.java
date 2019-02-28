package com.duanxr.migo.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InitializationBlock;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.PrimitiveTypeWrapper;

/**
 * generate SerialVersionUID
 *
 * @author Duanxr
 */
public class SerialVersionUIDUtil {

  public long getSerialVersionUID(InnerClass cl) {
    try {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      DataOutputStream dout = new DataOutputStream(bout);

      dout.writeUTF(cl.getType().getShortNameWithoutTypeArguments());

      int classMods =
          getModifiers(cl)
              & (Modifier.PUBLIC | Modifier.FINAL | Modifier.INTERFACE | Modifier.ABSTRACT);

      List<Method> methods = getMethod(cl);
      if ((classMods & Modifier.INTERFACE) != 0) {
        classMods =
            (methods.size() > 0) ? (classMods | Modifier.ABSTRACT)
                : (classMods & ~Modifier.ABSTRACT);
      }

      dout.writeInt(classMods);

      Set<FullyQualifiedJavaType> interfaces = cl.getSuperInterfaceTypes();
      List<String> ifaceNames = new ArrayList<String>();

      interfaces.forEach((s) -> {
        ifaceNames.add(s.getShortNameWithoutTypeArguments());
      });

      Collections.sort(ifaceNames);

      for (String ifaceName : ifaceNames) {
        dout.writeUTF(ifaceName);
      }

      List<Field> fields = cl.getFields();
      List<FieldSignature> fieldSigs = new ArrayList<FieldSignature>();
      for (Field field : fields) {
        fieldSigs.add(new FieldSignature(field));
      }
      FieldSignature[] fieldSigsarray = new FieldSignature[fieldSigs.size()];
      fieldSigsarray = fieldSigs.toArray(fieldSigsarray);
      try {
        Arrays.sort(fieldSigsarray, Comparator.comparing(ms -> ms.name));
      } catch (Exception ignored) {
      }

      for (FieldSignature sig : fieldSigsarray) {
        if (sig == null) {
          continue;
        }
        int mods =
            getModifiers(sig.member)
                & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC
                | Modifier.FINAL | Modifier.VOLATILE | Modifier.TRANSIENT);
        if (((mods & Modifier.PRIVATE) == 0)
            || ((mods & (Modifier.STATIC | Modifier.TRANSIENT)) == 0)) {
          dout.writeUTF(sig.name);
          dout.writeInt(mods);
          dout.writeUTF(sig.signature);
        }
      }
      if (hasStaticInitializer(cl)) {
        dout.writeUTF("<clinit>");
        dout.writeInt(Modifier.STATIC);
        dout.writeUTF("()V");
      }

      List<Method> cons = getDeclaredConstructors(cl);
      List<MethodSignature> consSigs = new ArrayList<>();
      for (Method con : cons) {
        consSigs.add(new MethodSignature(con));
      }
      MethodSignature[] consSigsarray = new MethodSignature[fieldSigs.size()];
      consSigsarray = consSigs.toArray(consSigsarray);
      try {
        Arrays.sort(consSigsarray, Comparator.comparing(ms -> ms.signature));
      } catch (Exception ignored) {
      }
      for (MethodSignature sig : consSigsarray) {
        if (sig == null) {
          continue;
        }
        int mods =
            getModifiers(sig.member)
                & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC
                | Modifier.FINAL | Modifier.SYNCHRONIZED | Modifier.NATIVE | Modifier.ABSTRACT
                | Modifier.STRICT);
        if ((mods & Modifier.PRIVATE) == 0) {
          dout.writeUTF("<init>");
          dout.writeInt(mods);
          dout.writeUTF(sig.signature.replace('/', '.'));
        }
      }
      List<MethodSignature> methodSigs = new ArrayList<MethodSignature>();
      for (Method method : methods) {
        methodSigs.add(new MethodSignature(method));
      }
      MethodSignature[] methodSigsarray = new MethodSignature[methodSigs.size()];
      methodSigsarray = methodSigs.toArray(methodSigsarray);
      try {
        Arrays.sort(methodSigsarray,
            Comparator.comparing((MethodSignature ms) -> ms.name)
                .thenComparing(ms -> ms.signature));
      } catch (Exception ignored) {
      }
      for (MethodSignature sig : methodSigsarray) {
        if (sig == null) {
          continue;
        }
        int mods =
            getModifiers(sig.member)
                & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC
                | Modifier.FINAL | Modifier.SYNCHRONIZED | Modifier.NATIVE | Modifier.ABSTRACT
                | Modifier.STRICT);
        if ((mods & Modifier.PRIVATE) == 0) {
          dout.writeUTF(sig.name);
          dout.writeInt(mods);
          dout.writeUTF(sig.signature.replace('/', '.'));
        }
      }
      dout.flush();
      MessageDigest md = MessageDigest.getInstance("SHA");
      byte[] hashBytes = md.digest(bout.toByteArray());
      long hash = 1L;
      for (int i = Math.min(hashBytes.length, 8) - 1; i >= 0; i--) {
        hash = (hash << 8) | (hashBytes[i] & 0xFF);
      }
      return hash;

    } catch (Exception e) {
      e.printStackTrace();
      return 1L;
    }
  }

  /**
   * 获取所有构造器 2018年2月8日 上午11:25:31
   */
  private List<Method> getDeclaredConstructors(InnerClass cl) {
    List<Method> constructors = new ArrayList<Method>();
    for (Method method : cl.getMethods()) {
      if (method.isConstructor()) {
        constructors.add(method);
      }
    }
    return constructors;
  }

  /**
   * 获取所有非构造器方法 2018年2月8日 下午12:17:14
   */
  private List<Method> getMethod(InnerClass cl) {
    List<Method> methods = new ArrayList<Method>();
    for (Method method : cl.getMethods()) {
      if (!method.isConstructor()) {
        methods.add(method);
      }
    }
    return methods;
  }

  /**
   * 是否存在静态构造器 2018年2月8日 上午11:22:05
   */
  private Boolean hasStaticInitializer(InnerClass cl) {
    List<InitializationBlock> initializationBlocks = cl.getInitializationBlocks();
    for (InitializationBlock initializationBlock : initializationBlocks) {
      if (initializationBlock.isStatic()) {
        return true;
      }
    }
    return false;
  }

  /**
   * 生成变量标识 2018年2月8日 上午11:21:50
   */
  private int getModifiers(Field filed) {
    int mod = 0;
    try {
      mod += getVisibilityMods(filed.getVisibility());
      mod += getModifierMods(filed);
    } catch (Exception e) {
    }
    return mod;
  }

  /**
   * 生成方法标识 2018年2月8日 下午12:10:57
   */
  private int getModifiers(Method method) {
    int mod = 0;
    try {
      mod += getVisibilityMods(method.getVisibility());
      mod += getModifierMods(method);
    } catch (Exception e) {
    }
    return mod;
  }

  /**
   * 生成类标识 2018年2月8日 上午11:21:44
   */
  private int getModifiers(InnerClass cl) {
    int mod = 0;
    try {
      mod += getVisibilityMods(cl.getVisibility());
      mod += getModifierMods(cl);
    } catch (Exception e) {
    }
    return mod;
  }

  /**
   * 生成方法的final.static等修饰符标识 2018年2月8日 下午12:11:34
   */
  private int getModifierMods(Method method) {
    int mod = 0;
    if (method.isFinal()) {
      mod += Modifier.FINAL;
    }
    if (method.isStatic()) {
      mod += Modifier.STATIC;
    }
    if (method.isNative()) {
      mod += Modifier.NATIVE;
    }
    if (method.isSynchronized()) {
      mod += Modifier.SYNCHRONIZED;
    }
    return mod;
  }

  /**
   * 生成变量的final.static等修饰符标识 2018年2月8日 上午11:21:23
   */
  private int getModifierMods(Field filed) {
    int mod = 0;
    if (filed.isFinal()) {
      mod += Modifier.FINAL;
    }
    if (filed.isStatic()) {
      mod += Modifier.STATIC;
    }
    if (filed.isTransient()) {
      mod += Modifier.TRANSIENT;
    }
    if (filed.isVolatile()) {
      mod += Modifier.VOLATILE;
    }
    return mod;
  }

  /**
   * 生成类的final.static等修饰符标识 2018年2月8日 上午11:21:05
   */
  private int getModifierMods(InnerClass cl) {
    int mod = 0;
    if (cl.isAbstract()) {
      mod += Modifier.ABSTRACT;
    }
    if (cl.isFinal()) {
      mod += Modifier.FINAL;
    }
    if (cl.isStatic()) {
      mod += Modifier.STATIC;
    }
    if (!cl.getSuperInterfaceTypes().isEmpty()) {
      mod += Modifier.INTERFACE;
    }
    return mod;
  }

  /**
   * 生成权限修饰符标识 2018年2月8日 上午11:20:47
   */
  private int getVisibilityMods(JavaVisibility visibility) {
    try {
      if (visibility.compareTo(JavaVisibility.PUBLIC) == 0) {
        return Modifier.PUBLIC;
      } else if (visibility.compareTo(JavaVisibility.PRIVATE) == 0) {
        return Modifier.PRIVATE;
      } else if (visibility.compareTo(JavaVisibility.PROTECTED) == 0) {
        return Modifier.PROTECTED;
      } else {
        return 0;
      }
    } catch (Exception e) {
      return 0;
    }
  }

  /**
   * 生成成员变量标识
   *
   * @author Duanxr 2018年2月8日 上午11:20:35
   */
  private static class FieldSignature {

    public final Field member;
    public final String name;
    public final String signature;

    public FieldSignature(Field field) {
      this.member = field;
      name = field.getName();
      signature = getClassSignature(field);
    }

    private String getClassSignature(Field filed) {
      StringBuilder sbuf = new StringBuilder();
      FullyQualifiedJavaType type = filed.getType();
      if (type == null || "void".equals(type.getShortName())) {
        sbuf.append('V');
      } else {
        String typewarpper = type.getFullyQualifiedName();

        if (type.isArray()
            || type.getShortName()
            .contains(PrimitiveTypeWrapper.getNewListInstance().getFullyQualifiedName())
            || type.getShortName()
            .contains(PrimitiveTypeWrapper.getNewListInstance().getFullyQualifiedName())
            || type.getShortName()
            .contains(PrimitiveTypeWrapper.getNewIteratorInstance().getFullyQualifiedName())) {
          sbuf.append('[');
        }
        if (typewarpper.equals(PrimitiveTypeWrapper.getIntInstance().getFullyQualifiedName())
            || typewarpper
            .equals(PrimitiveTypeWrapper.getIntegerInstance().getFullyQualifiedName())) {
          sbuf.append('I');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getByteInstance()
            .getFullyQualifiedName())) {
          sbuf.append('B');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getLongInstance()
            .getFullyQualifiedName())) {
          sbuf.append('J');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getFloatInstance()
            .getFullyQualifiedName())) {
          sbuf.append('F');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getDoubleInstance()
            .getFullyQualifiedName())) {
          sbuf.append('D');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getShortInstance()
            .getFullyQualifiedName())) {
          sbuf.append('S');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getCharacterInstance()
            .getFullyQualifiedName())) {
          sbuf.append('C');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getBooleanInstance()
            .getFullyQualifiedName())
            || typewarpper.equals(PrimitiveTypeWrapper.getBooleanPrimitiveInstance()
            .getFullyQualifiedName())) {
          sbuf.append('Z');
        } else {
          sbuf.append('L').append(type.getFullyQualifiedName().replace('.', '/')).append(';');
        }
      }
      return sbuf.toString();
    }
  }

  private static class MethodSignature {

    public final Method member;
    public final String name;
    public final String signature;

    public MethodSignature(Method method) {
      this.member = method;
      name = method.getName();
      signature = getClassSignature(method);
    }

    private String getClassSignature(Method method) {
      StringBuilder sbuf = new StringBuilder();
      FullyQualifiedJavaType type = method.getReturnType();
      if (type == null || "void".equals(type.getShortName())) {
        sbuf.append('V');
      } else {
        String typewarpper = type.getFullyQualifiedName();

        if (type.isArray()
            || type.getShortName()
            .contains(PrimitiveTypeWrapper.getNewListInstance().getFullyQualifiedName())
            || type.getShortName()
            .contains(PrimitiveTypeWrapper.getNewListInstance().getFullyQualifiedName())
            || type.getShortName()
            .contains(PrimitiveTypeWrapper.getNewIteratorInstance().getFullyQualifiedName())) {
          sbuf.append('[');
        }
        if (typewarpper.equals(PrimitiveTypeWrapper.getIntInstance().getFullyQualifiedName())
            || typewarpper
            .equals(PrimitiveTypeWrapper.getIntegerInstance().getFullyQualifiedName())) {
          sbuf.append('I');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getByteInstance()
            .getFullyQualifiedName())) {
          sbuf.append('B');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getLongInstance()
            .getFullyQualifiedName())) {
          sbuf.append('J');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getFloatInstance()
            .getFullyQualifiedName())) {
          sbuf.append('F');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getDoubleInstance()
            .getFullyQualifiedName())) {
          sbuf.append('D');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getShortInstance()
            .getFullyQualifiedName())) {
          sbuf.append('S');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getCharacterInstance()
            .getFullyQualifiedName())) {
          sbuf.append('C');
        } else if (typewarpper.equals(PrimitiveTypeWrapper.getBooleanInstance()
            .getFullyQualifiedName())
            || typewarpper.equals(PrimitiveTypeWrapper.getBooleanPrimitiveInstance()
            .getFullyQualifiedName())) {
          sbuf.append('Z');
        } else {
          sbuf.append('L').append(type.getFullyQualifiedName().replace('.', '/')).append(';');
        }
      }
      return sbuf.toString();
    }
  }
}
