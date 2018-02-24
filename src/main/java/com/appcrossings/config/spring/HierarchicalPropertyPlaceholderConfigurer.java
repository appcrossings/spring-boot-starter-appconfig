package com.appcrossings.config.spring;

import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

import com.appcrossings.config.Config;
import com.appcrossings.config.EnvironmentUtil;
import com.appcrossings.config.file.FilesystemSource;

/**
 * 
 * @author Krzysztof Karski
 *
 */
public class HierarchicalPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer
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

	public final static String DEFAULT_PROPERTIES_FILE_NAME = "default.properties";
	private final static Logger log = LoggerFactory.getLogger(HierarchicalPropertyPlaceholderConfigurer.class);

	public final static boolean SEARCH_CLASSPATH = true;

	private final ConvertUtilsBean bean = new ConvertUtilsBean();

	private StandardPBEStringEncryptor encryptor = null;

	protected EnvironmentUtil envUtil = new EnvironmentUtil();

	private PropertyPlaceholderHelper helper;

	protected final String hostsFilePath;

	private ValueInjector injector;

	private final AtomicReference<Properties> loadedProperties = new AtomicReference<>();

	protected String propertiesFileName = DEFAULT_PROPERTIES_FILE_NAME;

	protected boolean searchClasspath = SEARCH_CLASSPATH;

	private Timer timer = new Timer(true);

	/**
	 * 
	 * @param path
	 *            The path of the hosts.properties file
	 * @throws Exception
	 */
	public HierarchicalPropertyPlaceholderConfigurer(String path) throws Exception {
		setLocalOverride(true);
		helper = new PropertyPlaceholderHelper(this.placeholderPrefix, this.placeholderSuffix, this.valueSeparator,
				this.ignoreUnresolvablePlaceholders);
		this.hostsFilePath = path;
	}

	/**
	 * 
	 * @param path
	 *            - The path of the hosts.properties file
	 * @param refresh
	 *            - The period in seconds at which the config properties should be
	 *            refreshed. 0 indicates no automated timer
	 * @throws Exception
	 */
	public HierarchicalPropertyPlaceholderConfigurer(String path, int refresh) throws Exception {
		this(path);
		setRefreshRate(refresh);
	}

	@Override
	public <T> T getProperty(String key, Class<T> clazz) {

		String property;
		try {
			property = helper.replacePlaceholders(placeholderPrefix + key + placeholderSuffix, loadedProperties.get());

			if (property.equals(key))
				return null;

		} catch (IllegalArgumentException e) {
			return null;
		}

		if (clazz.equals(String.class))
			return (T) property;
		else if (property != null)
			return (T) bean.convert(property, clazz);
		else
			return null;

	}

	public <T> T getProperty(String key, Class<T> clazz, T value) {

		T val = getProperty(key, clazz);

		if (val != null)
			return val;

		return value;

	}

	protected void init() {

		String environmentName = envUtil.detectEnvironment();
		String hostName = envUtil.detectHostName();

		logger.info("Loading property files...");

		Properties hosts = FilesystemSource.loadHosts(hostsFilePath, encryptor);

		String startPath = hosts.getProperty(hostName);

		// Attempt environment as a backup
		if (!StringUtils.hasText(startPath) && StringUtils.hasText(environmentName)) {

			startPath = hosts.getProperty(environmentName);

		} else if (!StringUtils.hasText(startPath)) {

			startPath = hosts.getProperty("*");// catch all

		}

		if (StringUtils.hasText(startPath)) {

			log.debug("Searching for properties beginning at: " + startPath);

			Properties ps = FilesystemSource.loadProperties(startPath, propertiesFileName, searchClasspath, encryptor);

			if (ps.isEmpty()) {
				log.warn("Counldn't find any properties for host " + hostName + " or environment " + environmentName);
			}

			// Finally, propagate properties to PropertyPlaceholderConfigurer
			loadedProperties.set(ps);
			super.setProperties(loadedProperties.get());

		} else {
			log.warn("Counldn't find any properties for host " + hostName + " or environment " + environmentName);
		}

		try {
			mergeProperties();
		} catch (IOException io) {
			// nothing
		}
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

		init();
		super.postProcessBeanFactory(beanFactory);

	}

	public void reload() {

		init();
		injector.reloadBeans(loadedProperties.get(), this.placeholderPrefix, this.placeholderSuffix);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (injector == null)
			this.injector = new ValueInjector(applicationContext);
	}

	@Override
	public void setEnvironment(Environment environment) {
		super.setEnvironment(environment);
		envUtil.setEnvironment(environment);
	}

	public void setEnvironment(String environmentName) {
		envUtil.setEnvironmentName(environmentName);
	}

	public void setFileName(String fileName) {
		this.propertiesFileName = fileName;
	}

	public void setHostName(String hostName) {
		envUtil.setHostName(hostName);
	}

	/**
	 * Set password on the encryptor. If an encryptor isn't configured, a
	 * BasicTextEncryptor will be initialized and the password set on it. The basic
	 * assumed encryption algorithm is PBEWithMD5AndDES. This can be changed by
	 * setting the StandardPBEStringEncryptor.
	 * 
	 * @param password
	 */
	public void setPassword(String password) {

		this.encryptor = new StandardPBEStringEncryptor();
		EnvironmentStringPBEConfig configurationEncryptor = new EnvironmentStringPBEConfig();
		configurationEncryptor.setAlgorithm("PBEWithMD5AndDES");
		configurationEncryptor.setPassword(password);
		encryptor.setConfig(configurationEncryptor);

	};

	/**
	 * Override default text encryptor (StandardPBEStringEncryptor). Enables
	 * overriding both password and algorithm.
	 * 
	 * @param encryptor
	 */
	public void setTextEncryptor(StandardPBEStringEncryptor config) {
		this.encryptor = config;
	}

	public void setRefreshRate(Integer refresh) {

		if (refresh == 0L || refresh == null) {
			timer.cancel();
			return;
		}

		synchronized (timer) {
			timer.cancel();
			timer = new Timer(true);
			timer.schedule(new ReloadTask(), refresh * 1000, refresh * 1000);
		}
	}

	public void setSearchClasspath(boolean searchClasspath) {
		this.searchClasspath = searchClasspath;
	}
}
