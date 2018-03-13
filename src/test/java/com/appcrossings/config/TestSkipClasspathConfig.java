package com.appcrossings.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.appcrossings.config.TestSkipClasspathConfig.SampleApplicationContext;

@DirtiesContext
@ContextConfiguration(classes = SampleApplicationContext.class)
public class TestSkipClasspathConfig extends AbstractTestNGSpringContextTests {

  @Autowired
  private ConfigClient config;

  @Test
  public void testDoNotIncludeClasspathProperty() throws Exception {

    String value = config.getProperty("property.5.name", String.class);
    Assert.assertNull(value);

  }

  @Configuration
  public static class SampleApplicationContext {

    static {
      System.setProperty("env", "QA");
      System.setProperty("hostname", "michelangello-custom");
    }

    @Value("${property.3.name:none}")
    private String otherProperty;

    @Bean
    public static ConfigClient createConfig() throws Exception {
      ConfigClient c =
          new ConfigClient("classpath:/env/hosts.properties");
      c.setPassword("secret");
      c.setTraverseClasspath(false);
      return c;
    }

    @Bean
    public SampleClass createSample() {
      SampleClass c = new SampleClass();
      c.setSomeOtherValue(otherProperty);
      return c;
    }

  }


}
