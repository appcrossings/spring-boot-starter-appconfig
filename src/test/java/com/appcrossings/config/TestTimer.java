package com.appcrossings.config;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@DirtiesContext
@ActiveProfiles("QA")
@ContextConfiguration("classpath:META-INF/spring/test-spring-configurer-timer.xml")
public class TestTimer extends AbstractTestNGSpringContextTests {

  @Value("${bonus.1.property}")
  String bonusProperty;

  @Value("${property.5.name}")
  String property5;
  
  @Autowired
  SampleClass sample;
  
  static {
    System.setProperty("env", "");
    System.setProperty("hostname", "michelangello-custom");
  }

  @Test
  public void testTimerReload() throws Exception {
    
    assertNotNull(sample.getSomeValue1());
    assertNotNull(sample.getSomeValue2());
    assertNotNull(sample.getSomeValue4());
    assertNotNull(sample.getSomeOtherValue());
    assertNotNull(sample.getBonus1());

    assertEquals(sample.getSomeValue1(), "custom");
    assertEquals(sample.getSomeValue2(), "value2");
    assertEquals(sample.getSomeValue4(), "custom-custom2");
    assertEquals(sample.getSomeOtherValue(), "custom2");
    
    Assert.assertNotNull(property5);
    Assert.assertEquals(property5, "classpath");

    Assert.assertNotNull(bonusProperty);
    Assert.assertEquals(bonusProperty, "bonus1");

    //To test reload
    Thread.sleep(4000);
    
  }

}
