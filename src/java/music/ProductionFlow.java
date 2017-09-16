package music;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import music.ScorePart.State;
import music.action.ScoreAnalyzer;
import music.element.IRhythmScale;
import music.element.Key;
import music.element.Measure;
import music.element.Score;
import music.element.ScorePartEntity;
import music.instrument.IInstrument;
import music.instrument.Instrument;
import music.musicxml.MusicXMLHelper;
import music.transform.ITransformer;
import music.transform.Layer;
import util.Configuration;
import util.messaging.MessageProducerImpl;
import util.messaging.SessionImpl;
import util.mongo.MongoDBDataSource;
import util.music.DataSource;
import util.music.FileDataSource;
import util.music.IRhythmScaleFactory;
import util.music.RandomDataSource;
import util.music.RhythmScaleFactory;

/**
 * This creates and executes a logical workflow that to produce a MusicXML file.
 * 
 * Example usage: ProductionFlow -measures 20 -score -xml  -file "score0814.xml" -analyze -random
 *  -measures   : number of measures to create
 *  -analyze	: display score analysis upon completion
 *  -rand		: random data selection
 *  -xml		: create musicXML scores
 *  -file		: output XML file
 * 
 * Analysis usage: ProductionFlow -measures 20 -analyze -analyzeFile "C:\\data\\music\\testScore.csv" -rand
 * This will create a Score instance and analyze pitches and durations.
 * If -analyzeFile not specified, results sent to stdout.
 * @see music.action.ScoreAnalysis
 * 
 * @author donbacon
 *
 */
public class ProductionFlow implements Runnable {
	protected static final Logger log = LogManager.getLogger(ProductionFlow.class);
	
	public static final String CONFIG_FILENAME = "/config.properties";
	public static final String ORCHESTRA_CONFIG_FILENAME="/orchestra.properties";
	public static final String defaultHost = "localhost";
	public static final int defaultPort = 27017;
	
	private Properties configProperties = null;
	private Configuration configuration = null;
    
	private String collectionName;
	private String dbname = "music";
	private String recordType;
	private String recordName;
    private boolean saveScore;
    private boolean analyzeMode;
	private Map<String, Thread> scorePartThreads = new HashMap<String, Thread>();
	private Map<String, ScorePart> scoreParts = new HashMap<String, ScorePart>();
    private ConnectionFactory connectionFactory = null;
    private Connection connection = null;
    private String xmlFileName = null;
    private boolean createXML = false;
    private String xmlBaseFileName = null;
    private boolean randomSelection = false;
    private int measures = 0;
    private String analyzeFileName = null;
    private boolean saveIntermediateXML = true;
    
    /*
     * Transport attributes
     */
    private String user;				// activeMQ.user
    private String password;			// activeMQ.password
    private String url;					// activeMQ.url
    private Map<String, Destination> destinations = new HashMap<String, Destination>();
    private Map<String, MessageProducer> producers = new HashMap<String, MessageProducer>();
    private Session session = null;

    /**
     * Transformers specified in config.properties
     */
    private List<ITransformer> transformers = new ArrayList<ITransformer>();
    /**
     * Transformers mapped by Instrument
     */
    private Map<Instrument, ITransformer> transformerMap = new HashMap<Instrument, ITransformer>();
    
    private String title;
    private String workNumber;
    private String scoreName;
    private Score score;
    /**
     * Instrument names
     */
    protected List<String> instrumentNames = new ArrayList<String>();
    protected Map<String, Instrument> instruments = new HashMap<String, Instrument>();
    private Map<String, String[]> fieldNames = new HashMap<String, String[]>();
    /*
     * DataSource stuff
     */
    private String dataSourceName = null;
    private String dataSourceTransport = null;
	private MongoClient mongoClient  = null;
	private Datastore ds = null;
	private String host;
	private int port;
    
    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String... args) throws Exception {
    	boolean loadData = true;
    	boolean createScore = true;
    	boolean createXML = false;
    	boolean saveScore = false;	// save Score to MongoDB
    	boolean analyze = false;	// analyze generated score and produce statistics
    	String analyzeFileName = null;
    	String host = defaultHost;
    	String xmlFileName = null;
    	boolean randomSelection = false;
    	int port = defaultPort;
    	int measures = 0;
    	if(args.length > 0) {
    		for(int i = 0; i<args.length; i++) {
    			if(args[i].equalsIgnoreCase("-noload")) {
    				loadData = false;
    			}
    			else if(args[i].equalsIgnoreCase("-noscore")) {
    				createScore = false;
    			}
    			else  if(args[i].equalsIgnoreCase("-xml")) {
    				createXML = true;
    			}
    			else  if(args[i].startsWith("-rand")) {
    				randomSelection = true;
    			}
    			else if(args[i].equalsIgnoreCase("-file")) {
    				xmlFileName = args[++i];
    			}
    			else  if(args[i].equalsIgnoreCase("-save")) {
    				saveScore = true;
    			}
    			else  if(args[i].equalsIgnoreCase("-host")) {
    				host = args[++i];
    			}
    			else  if(args[i].equalsIgnoreCase("-port")) {
    				port = Integer.parseInt(args[++i]);
    			}
    			else  if(args[i].equalsIgnoreCase("-measures")) {
    				// number of measures to generate
    				measures = Integer.parseInt(args[++i]);
    			}
    			else  if(args[i].equalsIgnoreCase("-analyze")) {
    				analyze = true;
    			}
    			else if(args[i].equalsIgnoreCase("-analyzeFile")) {
    				analyze = true;
    				analyzeFileName = args[++i];
    			}
    		}	
    	}

    	ProductionFlow pf = new ProductionFlow();
    	pf.setHost(host);
    	pf.setPort(port);
    	pf.setSaveScore(saveScore);
    	if(xmlFileName != null && createXML) {
    		pf.setXmlFileName(xmlFileName);
    	}
     	pf.setRandomSelection(randomSelection);
    	if(measures > 0) { pf.setMeasures(measures); }
    	pf.setAnalyzeMode(analyze);
    	pf.setAnalyzeFileName(analyzeFileName);
    	pf.setCreateXML(createXML);
    	pf.run(loadData, createScore, createXML);
    }
    
	public ProductionFlow() throws Exception {
		configuration = Configuration.getInstance(CONFIG_FILENAME);
		configuration.addConfiguration(Configuration.getInstance(ORCHESTRA_CONFIG_FILENAME));
		configure();
	}

	@Override
	public void run() {
		run(true, true, true);
	}
	
	public void run(boolean loadData, boolean createScore, boolean createXML) {
		if(loadData) {
			loadData();
			log.info("*** Data loaded ***");
		}
		Layer layer = null;
		if(createScore) {
			layer = new Layer("Layer 1");
			score = createScore();
			score.getInstrumentNames().addAll(instrumentNames);
			layer.setScore(score);
			log.info("*** Score created ***");
			/*
			 * Do any transformation(s)
			 */
			applyTransformations(layer);
		}
		if(createXML) {
			createXML(xmlFileName);
		}
		if(saveScore) {
			saveCollection();
			log.info("*** score saved to " + scoreName + " ***");
		}
		if(analyzeMode) {
			// analyze and output as comma-separated values
			log.info("*** Analyze Score ***");
			PrintStream printStream = System.out;
			if(analyzeFileName != null) {
				try {
					printStream = new PrintStream(new FileOutputStream(analyzeFileName));
				}
				catch(FileNotFoundException e) {
					log.warn(analyzeFileName + " not available. Writing to System.out");
				}			
			}
			ScoreAnalyzer analyzer = new ScoreAnalyzer(this.score);
			analyzer.analyze();
			analyzer.displayAnalysis(printStream);
		}
	}

	private void createXML(String filename) {
		MusicXMLHelper helper = new MusicXMLHelper(score, configProperties);
		helper.convert();	// creates and returns a com.audiveris.proxymusic.ScorePartwise
		PrintStream ps = System.out;
		if(xmlFileName != null) {
			try {
				ps = new PrintStream(new FileOutputStream(filename));
			}
			catch(FileNotFoundException e) {
				log.warn(filename + " not available. Writing to System.out");
			}
		}
		helper.marshall(ps);	// marshals the ScorePartwise instance to an XML file
		if(filename != null) {
			ps.close();
		}
		log.info("*** MusicXML created *** " + filename);
	}

	public void applyTransformations(Layer layer) {
		/*
		 * Save XML pre-transformers
		 */
		if(saveIntermediateXML && createXML) {
			createXML(xmlBaseFileName + "_raw.xml");
		}
		/*
		 * Apply instrument-specific Transformers
		 */
		for(ITransformer t : transformerMap.values()) {
			t.accept(layer);
		}
		if(saveIntermediateXML && createXML) {
			createXML(xmlBaseFileName + "_t1.xml");
		}
		/*
		 * Apply the Transformers that apply to ALL instruments
		 */
		for(ITransformer t : transformers) {
			log.info("Apply transformer: " + t.getClass().getName());
			t.accept(layer);
		}
	}

	/**
	 * Save the score to a MongoDB Collection
	 * 
	 */
	public void saveCollection() {
		Morphia morphia = new Morphia();
		try {
			mongoClient = new MongoClient(host, port);
			ds = morphia.createDatastore(mongoClient, dbname);
		} catch (Exception e) {
			log.error("saveCollection Exception: " + e.toString());
			e.printStackTrace();
			return;
		}
		Map<String, ScorePartEntity> parts = score.getParts();
		for(String partname: parts.keySet()) {
			ScorePartEntity sp = parts.get(partname);
			List<Measure> measures = sp.getMeasures();
			for(Measure measure : measures) {
				DBObject dbo = morphia.toDBObject(measure);
				log.debug("save measure: " + measure.getNumber() + "\n" + dbo.toString());
				ds.save(measure);
				log.debug("measure " + measure.getNumber() + " saved");
			}
			ds.save(sp);
		}
		score.setName(scoreName);
		ds.save(score);
		mongoClient.close();
	}

	/**
	 * Create a Score with configured and command-line parameters
	 * Each Instrument corresponds to a ScorePart
	 * A thread is created for each ScorePart
	 * @return Score instance
	 */
	public Score createScore() {
		log.debug("createScore()");
		score = new Score(configuration, title);
		score.setWorkNumber(workNumber);
		for(String instrumentName : instrumentNames) {
			Instrument instrument = instruments.get(instrumentName);
			String partName = configProperties.getProperty("score.parts."+ instrumentName + ".partName", instrumentName);
			ScorePart scorePart = new ScorePart(score, partName, instrument);
			// set the max# measures to generate if specified
			scorePart.setMaxMeasures(measures);
			score.addPart(scorePart);
			Thread thread = new Thread(scorePart);
			scorePartThreads.put(instrumentName, thread);
			scoreParts.put(instrumentName, scorePart);
			thread.start();
		}
		/*
		 * wait for threads to complete
		 */
		boolean complete;
		do {
			complete = true;
			for(String instrumentName : instrumentNames) {
				State state = scoreParts.get(instrumentName).getState();
				log.trace(instrumentName + " state: " + state);
				complete &= state.equals(ScorePart.State.COMPLETE);
			}
		} while (!complete);
		return score;
	}

	/**
	 * Reads point data from configured DataSource
	 * MongoDB data source uses queries for configured instruments.
	 * 
	 * File data source streams data JSON data from a file.
	 * Random data source creates point data on the fly.
	 * All DataSources return data is the same JSON format.
	 * To give some variety to data sets, DataSource can be configured to select points randomly
	 * This depends on setting of sequentialSelection (if false, use random selection)
	 */
	public void loadData() {
		log.debug("loadData()");
		try {
			for(String instrumentName : instrumentNames) {
				log.info("loadData for " + instrumentName);

				MessageProducer producer = producers.get(instrumentName);
				DataSource ds = null;
				
				/********************************************************
				 * Load source data from a file
				 ********************************************************/
				if(dataSourceName.equalsIgnoreCase("file")) {
					ds = new FileDataSource(configuration, instrumentName);
				}
				
				/**********************************************************
				 * Load data from random source
				 **********************************************************/
				else if(dataSourceName.equalsIgnoreCase("random")) {
					ds = new RandomDataSource(configuration, instrumentName);
				}
				
		    	/***************************************************
		    	 * Load data from MongoDB
		    	 ***************************************************/
				else if(dataSourceName.equalsIgnoreCase("mongodb")) {
					ds = new MongoDBDataSource(configuration, instrumentName);
				}
				ds.stream().forEach(rec ->{
					try {
						producer.send(session.createTextMessage(rec));
					} catch (JMSException e) {
						// this is bad, this is VERY bad
						log.error("JMSException " + e.toString() + " rec: " + rec);
						log.error(" instrument: " + instrumentName);
						return;
					}
				});
				ds.close();
			}
			connection.close();
		} catch (Exception e) {
			log.error("loadData exception: " +e.toString());
		}
		return;
	}
	
	/**
	 * Reads configuration Property file and populates score parameters
	 * Also creates and starts ActiveMQ connection and creates a queue
	 * for each Instrument (appropriately having the same name as the Instrument)
	 * Each instrument is created and configured.
	 * 
	 * @throws Exception if it can't instantiate any of the transformers or Instruments
	 */
    @SuppressWarnings("unchecked")
	void configure() throws Exception {
		configProperties = configuration.getProperties();
		
		// properties passed on the command line
		configProperties.setProperty("measures", "" + measures);
		
    	/*
    	 * score properties and Instruments
    	 * The Key of each Instrument is set to the score.key as an initial value
    	 */
    	title = configProperties.getProperty("score.title", "Title");
    	workNumber = configProperties.getProperty("score.opus", "Opus 1");
    	scoreName = configProperties.getProperty("score.name", "Score1");
    	String[] instNames = configProperties.getProperty("score.instruments").split(",");
    	instrumentNames.addAll(Arrays.asList(instNames));
    	for(String name:instrumentNames) {
    		String classname = configProperties.getProperty("score.instruments." + name + ".class");
    		Class<Instrument> instrumentClass = (Class<Instrument>) Class.forName(classname);
    		Instrument instrument = (music.instrument.Instrument)instrumentClass.newInstance();
    		instrument.setPitchRange(IInstrument.getConfiguredPitchRange(configProperties, classname));
    		instrument.setName(name);
    		instrument.setInstrumentName(configProperties.getProperty("score.instruments." + name + "instrument-name", name));
    		instrument.configure(configuration);
    		instrument.setKey(new Key(configProperties.getProperty("score.key", "C-Major")));
    		instruments.put(name, instrument);
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
    	dataSourceName = configProperties.getProperty("dataSource", "file");
    	dataSourceTransport = configProperties.getProperty("dataSource.transport", "activeMQ");
    	
    	/*
    	 * Transformers
    	 */
    	createTransformers();
    	
    	/*
    	 * RhythmScales
    	 */
    	createRhythmScales();
   		
   		fieldNames.put("stats",configProperties.getProperty("dataSource.fields.stats").split(","));
   		fieldNames.put("point",configProperties.getProperty("dataSource.fields.point").split(","));

    	/******************************************
    	 * configure Transport
    	 ******************************************/
   		configureTransport();
        
    }

    private void configureTransport()  throws JMSException {
   		if(dataSourceTransport.equalsIgnoreCase("activemq")) {
	    	user=configProperties.getProperty("activeMQ.user");
	    	password = configProperties.getProperty("activeMQ.password");
	    	url = configProperties.getProperty("activeMQ.url" );
	
	        /*
	         *  Set up the ActiveMQ JMS Components
	         */
	    	try {
		        connectionFactory = new ActiveMQConnectionFactory(user, password, url);
		        connection = connectionFactory.createConnection();
		        connection.start();
		
		        /*
		         * Create the MQ session
		         * and a queue for each instrument: piano.queue etc.
		         * Assign a MessageProducer for each queue
		         */
		        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		        for(String name:instrumentNames) {
		        	Destination dest = session.createQueue(name + ".queue");
		        	destinations.put(name, dest);
		        	MessageProducer mp = session.createProducer(dest);
		        	MessageProducerImpl mpImpl = new MessageProducerImpl(mp, dest);
		        	producers.put(name, mpImpl);
		        }
	    	}
		    catch(JMSException e) {
		    	 log.error("JMSException: " + e.toString());
		    	 throw e;
		    }
   		}
   		else {
   			session = new SessionImpl();
	        for(String name:instrumentNames) {
	        	Destination dest = session.createQueue(name + ".queue");
	        	destinations.put(name, dest);
	        	MessageProducer mp = session.createProducer(dest);
	        	producers.put(name, mp);
	        }
   		}
	}

	/**
     * Creates and assigns the appropriate RhythmScales for each instrument.
     */
	void createRhythmScales() {
		IRhythmScale allRhythmScale = null;
		String rhythmScaleName = null;
		
		if(configProperties.containsKey("score.rhythmScale.all")) {
			rhythmScaleName = configProperties.getProperty("score.rhythmScale.all");
		}
		else {	// use the default
			rhythmScaleName = RhythmScaleFactory.DEFAULT_RHYTHM_SCALE_NAME;
		}
		
		IRhythmScaleFactory factory = RhythmScaleFactory.getRhythmScaleFactory(rhythmScaleName);
		allRhythmScale = factory.createRhythmScale(rhythmScaleName);
		
		/*
		 * configure individual instruments
		 */
    	for(String instrumentName:instrumentNames) {
    		Instrument instrument = instruments.get(instrumentName);
    		String key = "score.rhythmScale.instrument." + instrumentName;
    		if(configProperties.containsKey(key)) {
    			String rsName = configProperties.getProperty(key);
    			factory = RhythmScaleFactory.getRhythmScaleFactory(rsName);
    			IRhythmScale rs = factory.createRhythmScale(rsName);
    			instrument.setRhythmScale(rs);
    		}
    		else {
    			instrument.setRhythmScale(allRhythmScale);
    		}
    	}
	}

	@SuppressWarnings("unchecked")
	void createTransformers() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<ITransformer> tclass = null;
		ITransformer transformer = null;
		String[] tforms = null;
    	/*
    	 * Get & configure transformers for individual instruments
    	 * Each instrument gets its own ScaleTransformer if needed so it knows the Range
    	 */
    	for(String instrumentName:instrumentNames) {
    		String key = "score.transformers." + instrumentName;
    		if(configProperties.containsKey(key)) {
    			tforms = configProperties.getProperty(key).split(",");
        		for(String tclassname : tforms) {
        			tclass = (Class<ITransformer>)Class.forName(tclassname);
        			transformer = tclass.newInstance();
        			Instrument instrument = instruments.get(instrumentName);
        			transformer.configure(configProperties, instrument);
        			transformer.setInstrument(instrument);
        			transformer.setTransformerClassName(tclassname);
        			transformerMap.put(instrument, transformer);
        		}
    		}
    	}
    	
		/*
		 * Get & configure default transformers - these apply to instruments
		 * not configured with their own transformer
		 * Each instrument gets its own Transformer so it knows the Range
		 */
		if(configProperties.containsKey("score.transformers.default")) {
    		tforms = configProperties.getProperty("score.transformers.default").split(",");	// can specify more than one
    		for(Instrument instrument : instruments.values()) {
	    		for(String tclassname : tforms) {
	    			/*
	    			 * If there's already a transformer of the same class for this instrument
	    			 * then don't configure the default
	    			 */
	    			if(transformerMap.containsKey(instrument)) {
	    				String tcname = transformerMap.get(instrument).getTransformerClassName();
	    				continue;
	    			}
	    			tclass = (Class<ITransformer>)Class.forName(tclassname);
	    			transformer = tclass.newInstance();
	    			transformer.configure(configProperties, instrument);
	    			transformers.add(transformer);
	    		}
    		}
    	}

	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public String getRecordName() {
		return recordName;
	}

	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}

	public Properties getConfigProperties() {
		return configProperties;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getWorkNumber() {
		return workNumber;
	}

	public void setWorkNumber(String workNumber) {
		this.workNumber = workNumber;
	}

	public List<String> getInstrumentNames() {
		return instrumentNames;
	}

	public void setInstrumentNames(String[] instNames) {
		instrumentNames.addAll(Arrays.asList(instNames));
	}

	public Map<String, Instrument> getInstruments() {
		return instruments;
	}

	public void setInstruments(Map<String, Instrument> instruments) {
		this.instruments = instruments;
	}


	public boolean isSaveScore() {
		return saveScore;
	}

	public void setSaveScore(boolean saveScore) {
		this.saveScore = saveScore;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public List<ITransformer> getTransformers() {
		return transformers;
	}

	public String getXmlFileName() {
		return xmlFileName;
	}

	public void setXmlFileName(String xmlFileName) {
		this.xmlFileName = xmlFileName;
		xmlBaseFileName = xmlFileName.substring(0, xmlFileName.length()-4);
	}

	public boolean isRandomSelection() {
		return randomSelection;
	}

	/**
	 * Sets random selection value
	 * Also sets "selectionMode" property in the configuration
	 * @param randomSelection
	 */
	public void setRandomSelection(boolean randomSelection) {
		this.randomSelection = randomSelection;
		configuration.getProperties().setProperty("selectionMode", randomSelection ? "random" : "sequential");
	}

	public int getMeasures() {
		return measures;
	}

	/**
	 * Sets the value of measures.
	 * Also sets the same value in configuration property for "measures"
	 * @param measures
	 */
	public void setMeasures(int measures) {
		configuration.getProperties().setProperty("measures", String.valueOf(measures));
		this.measures = measures;
	}

	public String getScoreName() {
		return scoreName;
	}

	public void setScoreName(String scoreName) {
		this.scoreName = scoreName;
	}

	public boolean isAnalyzeMode() {
		return analyzeMode;
	}

	public void setAnalyzeMode(boolean analyzeMode) {
		this.analyzeMode = analyzeMode;
	}

	public String getAnalyzeFileName() {
		return analyzeFileName;
	}

	public void setAnalyzeFileName(String analyzeFileName) {
		this.analyzeFileName = analyzeFileName;
	}

	public boolean isCreateXML() {
		return createXML;
	}

	public void setCreateXML(boolean createXML) {
		this.createXML = createXML;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getDataSourceTransport() {
		return dataSourceTransport;
	}

	public void setDataSourceTransport(String dataSourceTransport) {
		this.dataSourceTransport = dataSourceTransport;
	}
    
}
