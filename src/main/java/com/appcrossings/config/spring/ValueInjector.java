package com.appcrossings.config.spring;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

public class ValueInjector {

  public ValueInjector(ApplicationContext context) {
    this.context = context;
  }

  private final ApplicationContext context;

  public void reloadBeans(Properties props, String prefix, String suffix) {

    String[] beanNames = context.getBeanDefinitionNames();

    PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper(prefix, suffix, ":", false);

    for (String name : beanNames) {

      Object o = context.getBean(name);

      if (o.getClass().equals(getClass()))
        continue;

      Field[] values = o.getClass().getFields();

      for (Field f : values) {

        if (!f.isAnnotationPresent(Value.class)) {
          continue;
        } else {

          String key = f.getAnnotation(Value.class).value();
          String value = helper.replacePlaceholders(key, props);

          try {

            if (!StringUtils.isEmpty(value) && !value.equals(f.get(o))) {
              f.set(o, value);
            }
          } catch (Exception e) {
            // nothing
          }
        }
      }

      Method[] methods = o.getClass().getMethods();

      for (Method f : methods) {

        if (!f.isAnnotationPresent(Value.class)) {
          continue;
        } else if (f.getName().startsWith("set") && f.getParameterCount() == 1) {

          String key = f.getAnnotation(Value.class).value();
          String value = helper.replacePlaceholders(key, props);

          if (!StringUtils.isEmpty(value)) {
            try {
              f.invoke(o, value);
            } catch (Exception e) {

            }
          }
        }
      }
    }
  }
}
