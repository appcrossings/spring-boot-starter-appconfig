package com.appcrossings.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


@DirtiesContext
@ContextConfiguration("classpath:META-INF/spring/test-spring-configurer.xml")
public class TestEnvBasedConfig extends AbstractTestNGSpringContextTests {

  static {
    System.setProperty("env", "QA");
    System.setProperty("hostname", "");
  }
  
  @Autowired
  private Config config;

  @Autowired
  public SampleClass clazz;

  @Test
  public void testProperties() throws Exception {

    assertNotNull(clazz.getSomeValue1());
    assertNotNull(clazz.getSomeValue2());
    assertNotNull(clazz.getSomeValue4());
    assertNotNull(clazz.getSomeOtherValue());

    assertEquals(clazz.getSomeValue1(), "custom");
    assertEquals(clazz.getSomeValue2(), "value2");
    assertEquals(clazz.getSomeValue4(), "custom-custom2");
    assertEquals(clazz.getSomeOtherValue(), "custom2");

    assertNotNull(config.getProperty("property.1.name", String.class));
    assertEquals(config.getProperty("property.1.name", String.class), "custom");
  }

}
