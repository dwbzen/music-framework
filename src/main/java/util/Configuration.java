package util;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Singleton Configuration
 * Usage example: 
 * Configuration config = Configuration.getInstance("/config.properties");
 * Properties properties = config.getProperties();
 * 
 * @author don_bacon
 *
 */
public class Configuration {

	private String configurationFilename = null;
	private Properties properties = null;
	
	protected Configuration() {}
	
	protected Configuration(String configFilename) {
		properties = new Properties();
		setConfigurationFilename(configFilename);
	}
	
	public static Configuration getInstance(String configFile) {
		Configuration configuration = null;
		synchronized(Configuration.class){
			configuration = new Configuration(configFile);
			configuration.loadProperties();
		}
		return configuration;
	}
	
	private void loadProperties() {
        URL url = getClass().getResource(configurationFilename);
        if(url == null) {
            throw new IllegalArgumentException("Could not load resource: \"" + configurationFilename + "\"");
        }
        try {
        	InputStream stream = url.openStream();
        	properties.load(stream);
            stream.close();
        }
        catch(Exception e) {
        	System.err.println("Could not load " + configurationFilename + " " + e.toString());
        }
	}
	
	public void addConfiguration(Configuration someOtherConfiguration) {
		properties.putAll(someOtherConfiguration.getProperties());
	}
	
	public Properties getProperties() {
		return this.properties;
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public String getConfigurationFilename() {
		return configurationFilename;
	}
	public void setConfigurationFilename(String configurationFilename) {
		this.configurationFilename = configurationFilename;
	}
	
}
