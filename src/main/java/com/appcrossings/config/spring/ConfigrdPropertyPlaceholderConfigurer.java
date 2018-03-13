package com.appcrossings.config.spring;

import java.io.IOException;
import java.util.Properties;
import java.util.TimerTask;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import com.appcrossings.config.Config;
import com.appcrossings.config.ConfigClient;
import com.appcrossings.config.ConfigClient.Method;


/**
 * 
 * @author Krzysztof Karski
 *
 */
public class ConfigrdPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer
    implements Config, EnvironmentAware, ApplicationContextAware {

  private class ReloadTask extends TimerTask {

    @Override
    public void run() {
      try {

        reload();

      } catch (Exception e) {
        logger.error("Error refreshing configs", e);
      }
    }
  }

  private ValueInjector injector;

  private final ConfigClient client;

  /**
   * 
   * @param path The path of the hosts.properties file
   * @throws Exception
   */
  public ConfigrdPropertyPlaceholderConfigurer(String path) throws Exception {
    setLocalOverride(true);
    client = new ConfigClient(path, Method.HOST_FILE);
  }

  /**
   * 
   * @param path - The path of the hosts.properties file
   * @param refresh - The period in seconds at which the config properties should be refreshed. 0
   *        indicates no automated timer
   * @throws Exception
   */
  public ConfigrdPropertyPlaceholderConfigurer(String path, int refresh) throws Exception {
    client = new ConfigClient(path, refresh, Method.HOST_FILE);
  }

  @Override
  public <T> T getProperty(String key, Class<T> clazz) {

    return client.getProperty(key, clazz);

  }

  public <T> T getProperty(String key, Class<T> clazz, T value) {

    return client.getProperty(key, clazz, value);

  }

  protected void init() {

    client.init();
    Properties props = client.getProperties();
    super.setProperties(props);

    try {
      mergeProperties();
    } catch (IOException io) {
      // nothing
    }
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {

    init();
    super.postProcessBeanFactory(beanFactory);

  }

  public void reload() {

    init();
    injector.reloadBeans(client.getProperties(), this.placeholderPrefix, this.placeholderSuffix);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    if (injector == null)
      this.injector = new ValueInjector(applicationContext);
  }

  @Override
  public void setEnvironment(Environment environment) {
    super.setEnvironment(environment);

    String env = null;

    if (environment != null && environment.getActiveProfiles() != null
        && environment.getActiveProfiles().length > 0) {
      env = environment.getActiveProfiles()[0];
    }

    setEnvironment(env);
  }

  public void setEnvironment(String environmentName) {
    client.getEnvironment().setEnvironmentName(environmentName);
  }

  public void setFileNamePattern(String fileName) {
    client.setFileNamePattern(fileName);
  }

  public void setHostName(String hostName) {
    client.getEnvironment().setHostName(hostName);
  }

  /**
   * Set password on the encryptor. If an encryptor isn't configured, a BasicTextEncryptor will be
   * initialized and the password set on it. The basic assumed encryption algorithm is
   * PBEWithMD5AndDES. This can be changed by setting the StandardPBEStringEncryptor.
   * 
   * @param password
   */
  public void setPassword(String password) {
    client.setPassword(password);
  }

  /**
   * Override default text encryptor (StandardPBEStringEncryptor). Enables overriding both password
   * and algorithm.
   * 
   * @param encryptor
   */
  public void setTextEncryptor(StandardPBEStringEncryptor config) {
    client.setTextEncryptor(config);
  }

  @Override
  public Properties getProperties() {
    return client.getProperties();
  }

  public void traverseClasspath(boolean traverse) {
    this.client.setTraverseClasspath(traverse);
  }
}
