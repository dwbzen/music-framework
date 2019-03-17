package org.dwbzen.util;

public interface Configurable {

	/**
	 * Configure using the Configuration provided and sets the Configuration instance.
	 * @param configuration A non-null Configuration
	 * @throws IllegalArgumentException if configuration is null
	 */
	default void configure(Configuration configuration) {
		if(configuration == null) {
			throw new IllegalArgumentException("Null Configuration is not allowed");
		}
		setConfiguration(configuration);
		configure();
	}
	
	/**
	 * Configure using the current Configuration instance (if not null)
	 * 
	 */
	void configure();
	
	
	Configuration getConfiguration();
	void setConfiguration(Configuration config);
}
