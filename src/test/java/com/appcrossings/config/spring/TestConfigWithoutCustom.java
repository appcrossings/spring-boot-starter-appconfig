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
import com.appcrossings.config.spring.TestConfigWithoutCustom.SampleApplicationContext;

@DirtiesContext
@ContextConfiguration(classes = SampleApplicationContext.class)
public class TestConfigWithoutCustom extends AbstractTestNGSpringContextTests {

  @Autowired
  private ConfigClient config;

  @Test
  public void testLoadHostFile() {

    String value = config.getProperty("property.1.name", String.class);
    Assert.assertNotNull(value);
    Assert.assertEquals(value, "value1");

  }

  @Configuration
  public static class SampleApplicationContext {

    static {
      System.setProperty("env", "QA");
      System.setProperty("hostname", "michelangello");
    }

    @Bean(initMethod="init")
    public static ConfigClient createConfig() throws Exception {
      ConfigClient c =
          new ConfigClient("classpath:/env/hosts.properties", Method.DISCOVER);
      c.setPassword("secret");
      return c;
    }

  }


}
