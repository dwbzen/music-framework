package org.dwbzen.util.music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.instrument.IInstrument;
import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.util.Configuration;
import org.dwbzen.util.ConfigurationException;


public class InstrumentMaker  implements Supplier<Map<String, Instrument>> {
	
	protected static final Logger log = LogManager.getLogger(InstrumentMaker.class);
	public static final String CONFIG_FILENAME = "/config.properties";
	public static final String ORCHESTRA_CONFIG_FILENAME="/orchestra.properties";
	
	private Configuration configuration = null;
	private Properties configProperties = null;
	private List<String> instrumentNames = null;
	private Map<String, Instrument> instruments = new TreeMap<>();
	
	protected InstrumentMaker() {
		configuration = Configuration.getInstance(CONFIG_FILENAME);
		configuration.addConfiguration(Configuration.getInstance(ORCHESTRA_CONFIG_FILENAME));
		configProperties = configuration.getProperties();
	}
	
	public InstrumentMaker(String names, Configuration config) {
		configuration =  config;
		configProperties = configuration.getProperties();
		instrumentNames = Arrays.asList( names.split(","));		// could be a single instrument
	}
	
	public InstrumentMaker(List<String> names, Configuration config) {
		configuration =  config;
		configProperties = configuration.getProperties();
		instrumentNames = names;
	}
	
	public InstrumentMaker(String names) {
		this();
		instrumentNames = Arrays.asList( names.split(","));
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public List<String> getInstrumentNames() {
		if(instrumentNames == null) {
			instrumentNames = new ArrayList<>();
		}
		return instrumentNames;
	}

	public Map<String, Instrument> getInstruments() {
		return get();
	}

	@Override
	public Map<String, Instrument> get() {
		createInstruments();
		return instruments;
	}
	
	@SuppressWarnings("unchecked")
	private void createInstruments() throws ConfigurationException {
		for(String name : getInstrumentNames() ) {
    		try {
	    		String classname = configProperties.getProperty("score.instruments." + name + ".class");
	    		Class<Instrument> instrumentClass = (Class<Instrument>) Class.forName(classname);
	    		Instrument instrument = (org.dwbzen.music.instrument.Instrument)instrumentClass.getDeclaredConstructor().newInstance();
	    		instrument.setPitchRange(IInstrument.getConfiguredPitchRange(configProperties, classname));
	    		instrument.setName(name);
	    		instrument.setInstrumentName(configProperties.getProperty("score.instruments." + name + "instrument-name", name));
	    		instrument.configure(configuration);
	    		// if not set by the Instrument when initialized (transposing instruments should do this), default key is C-Major
	    		if(instrument.getKey() == null) {
	    			instrument.setKey(new Key(configProperties.getProperty("score.key", "C-Major")));
	    		}
	    		instruments.put(name, instrument);
    		} 
    		catch(Exception e) {
    			String errorMessage = "Could not create Instrument " + name;
    			log.error(errorMessage);
    			log.error(e.toString() );
    			e.printStackTrace();
    			System.err.println(errorMessage);
    			throw new ConfigurationException(errorMessage);
    		}
		}
	}

}
