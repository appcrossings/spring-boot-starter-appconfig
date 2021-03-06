package com.appcrossings.config.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.appcrossings.config.ConfigClient;
import com.appcrossings.config.ConfigClient.Method;
import com.appcrossings.config.spring.TestCustomConfig.SampleApplicationContext;

@DirtiesContext
@ContextConfiguration(classes = SampleApplicationContext.class)
public class TestCustomConfig extends AbstractTestNGSpringContextTests {


  @Autowired
  private ConfigClient config;

  @Test
  public void testDetectHost() {
    String hostName = config.getEnvironment().detectHostName();
    Assert.assertNotNull(hostName);
    Assert.assertEquals(hostName, "michelangello-vendor");
  }

  @Test
  public void testPropertiesCascade() throws Exception {

    String value = config.getProperty("property.1.name", String.class);
    Assert.assertNotNull(value);
    Assert.assertEquals(value, "vendor");

    value = config.getProperty("property.2.name", String.class);
    Assert.assertNotNull(value);
    Assert.assertEquals(value, "value2");

  }

  @Test
  public void testPropertiesCascadeOverride() throws Exception {

    String value = config.getProperty("property.1.name", String.class);
    Assert.assertNotNull(value);
    Assert.assertEquals(value, "vendor");

  }

  @Test
  public void testIncludeClasspathProperty() throws Exception {

    String value = config.getProperty("property.5.name", String.class);
    Assert.assertNotNull(value);
    Assert.assertEquals(value, "classpath");

  }

  @Test
  public void testGetNonExistingProperty() throws Exception {

    String value = config.getProperty("property.not-exists", String.class);
    Assert.assertNull(value);

  }

  @Test
  public void testGetEncryptedProperty() throws Exception {

    String value = config.getProperty("property.6.name", String.class);
    Assert.assertNotNull(value);
    Assert.assertNotEquals(value, "NvuRfrVnqL8yDunzmutaCa6imIzh6QFL");
    Assert.assertEquals(value, "password");
  }

  @Configuration
  public static class SampleApplicationContext {

    static {
      System.setProperty("env", "QA");
      System.setProperty("hostname", "michelangello-vendor");
    }

    @Bean(initMethod = "init")
    public static ConfigClient createConfig() throws Exception {
      ConfigClient c = new ConfigClient("classpath:/env/hosts.properties", Method.DISCOVER);
      c.setPassword("secret");
      return c;
    }

  }


}
