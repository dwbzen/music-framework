package music;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import music.element.Key;
import music.element.Measure;
import music.instrument.IInstrument;
import music.instrument.Instrument;
import util.Configuration;
import util.ConfigurationException;
import util.Configurator;

public class ProductionFlowConfigurator implements Configurator {
	private ProductionFlow productionFlow = null;
	private List<String> instrumentNames = null;
	private Map<String, Instrument> instruments = null;
	protected static final Logger log = LogManager.getLogger(ProductionFlow.class);
	
	public ProductionFlowConfigurator(ProductionFlow productionFlow) {
		this.productionFlow = productionFlow;
	}
	
	@Override
    @SuppressWarnings("unchecked")
	public void configure(Configuration configuration) throws ConfigurationException {
		Properties configProperties = configuration.getProperties();
		instrumentNames = productionFlow.getInstrumentNames();
		instruments = productionFlow.getInstruments();
	   	/*
    	 * score properties and Instruments
    	 * The Key of each Instrument is set to the score.key as an initial value
    	 */
		productionFlow.setTitle(configProperties.getProperty("score.title", "Title"));
		productionFlow.setWorkNumber(configProperties.getProperty("score.opus", "Opus 1"));
		productionFlow.setScoreName(configProperties.getProperty("score.name", "Score1"));
    	String[] instNames = configProperties.getProperty("score.instruments").split(",");
    	instrumentNames.addAll(Arrays.asList(instNames));
  
    	for(String name:instrumentNames) {
    		try {
	    		String classname = configProperties.getProperty("score.instruments." + name + ".class");
	    		Class<Instrument> instrumentClass = (Class<Instrument>) Class.forName(classname);
	    		Instrument instrument = (music.instrument.Instrument)instrumentClass.getDeclaredConstructor().newInstance();
	    		instrument.setPitchRange(IInstrument.getConfiguredPitchRange(configProperties, classname));
	    		instrument.setName(name);
	    		instrument.setInstrumentName(configProperties.getProperty("score.instruments." + name + "instrument-name", name));
	    		instrument.configure(configuration);
	    		instrument.setKey(new Key(configProperties.getProperty("score.key", "C-Major")));
	    		instruments.put(name, instrument);
    		} 
    		catch(Exception e) {
    			String errorMessage = "Could not create Instrument " + name;
    			log.error(errorMessage);
    			log.error(e.toString() );
    			e.printStackTrace();
    			throw new ConfigurationException(errorMessage);
    		}
    		
    	}
    	/*
    	 * global divisions per measure for Measure and Transformers
    	 */
    	int divisionsPerMeasure = Integer.parseInt(configProperties.getProperty("score.measure.divisions", "16"));
    	Measure.setDivisionsPerMeasure(divisionsPerMeasure);
    	
    	/*
    	 * Data source defaults to file
    	 * transport defaults to activeMQ
    	 */
    	productionFlow.setDataSourceName(configProperties.getProperty("dataSource", "file"));
    	productionFlow.setDataSourceTransport(configProperties.getProperty("dataSource.transport", "activeMQ"));
    	
    	Map<String, String[]> fieldNames = productionFlow.getFieldNames();
   		fieldNames.put("stats",configProperties.getProperty("dataSource.fields.stats").split(","));
   		fieldNames.put("point",configProperties.getProperty("dataSource.fields.point").split(","));

	}

}
