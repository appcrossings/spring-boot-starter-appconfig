package com.appcrossings.config.spring;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import com.appcrossings.config.Config;
import com.appcrossings.config.ConfigClient;
import com.appcrossings.config.ConfigClient.Method;
import com.appcrossings.config.spring.TestBeanAppConfigSpringConfigurer.SampleApplicationContext;

@ContextConfiguration(classes = SampleApplicationContext.class)
public class TestBeanAppConfigSpringConfigurer extends AbstractTestNGSpringContextTests {

	@Autowired
	private Config config;

	@Autowired
	public SampleClass clazz;

	@Test
	public void testProperties() throws Exception {

		assertNotNull(clazz.getBonus1());
		assertNotNull(clazz.getBonus1(), "bonus2");

		assertNotNull(config.getProperty("property.1.name", String.class));
		assertEquals(config.getProperty("property.1.name", String.class), "simple");
	}

	@Configuration
	@PropertySource("classpath:/bonus.properties")
	public static class SampleApplicationContext {

		static {
			System.setProperty("env", "");
			System.setProperty("hostname", "michelangello-custom2");
		}

		@Value("${property.3.name:none}")
		private String otherProperty;

		@Bean(initMethod="init")
		public static ConfigClient createConfig() throws Exception {
			ConfigClient c = new ConfigClient("classpath:/env/hosts.properties", Method.DISCOVER);
			c.setPassword("secret");
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
