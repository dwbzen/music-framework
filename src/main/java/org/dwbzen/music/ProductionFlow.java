package org.dwbzen.music;

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
import org.bson.Document;
import org.dwbzen.music.ScorePart.State;
import org.dwbzen.music.action.ScoreAnalyzer;
import org.dwbzen.music.element.IRhythmScale;
import org.dwbzen.music.element.Score;
import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.music.musicxml.MusicXMLHelper;
import org.dwbzen.music.transform.ITransformer;
import org.dwbzen.music.transform.Layer;
import org.dwbzen.util.Configuration;
import org.dwbzen.util.ConfigurationException;
import org.dwbzen.util.messaging.MessageProducerImpl;
import org.dwbzen.util.messaging.SessionImpl;
import org.dwbzen.util.music.DataLoadException;
import org.dwbzen.util.music.IRhythmScaleFactory;
import org.dwbzen.util.music.RhythmScaleFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * This creates and executes a workflow to produce a MusicXML file.</p>
 * 
 * Example usage: </p>
 * <code>ProductionFlow -measures 20 -score -xml "score20200910.xml" -analyze -random</code></p>
 * <code>ProductionFlow -measures 30 -rand  -xml "C:\\Music\\Scores\\musicXML\\score20200909.xml" -analyze</code></p>
 * <dl>
 * <dt>-measures</dt> <dd>number of measures to create</dd>
 * <dt>-analyze</dt> <dd>display score analysis upon completion</dd>
 * <dt>-analyzeFile filename</dt>  <dd>output analysis to this file.</dd>
 * <dt>-rand[dom]</dt> <dd>random selection of data from the specified file</dd>
 * <dt>-xml filename</dt> <dd>writes musicXML score to filename</dd>
 * <dt>-save</dt> <dd>save JSON score for import into MongoDB</dd>
 * <dt>-score  true|false</dt>  <dd>if false, do not produce score files. Default is true.</dd>
 * <dt>-load  true|false</dt>  <dd>if false, do not load data. Default is true.</dd>
 * <dt>-file</dt> <dd>output MusicXML file</dd>
 * </dl>
 * <p>
 * Normally data points are selected from the data set sequentially.
 * Set -rand to select points at random (skips 2 to 20 points each iteration).
 * </p>
 * Analysis usage: </p>
 * <code>
 * ProductionFlow -measures 20 -analyze -analyzeFile "C:\\data\\music\\testScore.csv" -rand
 * </code></p>
 * 
 * This will create a Score instance and analyze pitches and durations.<br>
 * Note that if -analyzeFile is specified, the analyze flag is assumed and can be omitted.
 * If -analyzeFile not specified, results sent to stdout.</p>
 * 
 * @see music.action.ScoreAnalysis
 * @author don_bacon
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
    	String dataSourceName = null;
    	if(args.length > 0) {
    		for(int i = 0; i<args.length; i++) {
    			if(args[i].equalsIgnoreCase("-load")) {
    				loadData = args[++i].equalsIgnoreCase("true");
    			}
    			else if(args[i].equalsIgnoreCase("-score")) {
    				createScore = args[++i].equalsIgnoreCase("true");
    			}
    			else  if(args[i].equalsIgnoreCase("-xml")) {
    				createXML = true;
    				xmlFileName = args[++i];
    			}
    			else  if(args[i].startsWith("-rand")) {
    				randomSelection = true;
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
		try {
			connection.close();
		} catch (JMSException e) {
			log.error("JMS Exception on close");
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
	 * Save the Json score to a MongoDB Collection
	 * TODO complete the implementation
	 * 
	 */
	public void saveCollection() {
		MongoDatabase database = null;
		MongoCollection<Document> collection = null;
		try {
			mongoClient = new MongoClient(host, port);
			database = mongoClient.getDatabase("test");
			collection = database.getCollection("scores");
		} catch (Exception e) {
			log.error("saveCollection Exception: " + e.toString());
			e.printStackTrace();
			return;
		}
		score.setName(scoreName);
		Document scoreDoc = new Document(scoreName, score.toJson());
		collection.insertOne(scoreDoc);
		mongoClient.close();
	}

	/**
	 * Create a Score with configured and command-line parameters
	 * Each ScorePart corresponds to an Instrument.
	 * @return Score instance
	 */
	public Score createScore() {
		log.debug("createScore()");
		IScoreFactory scoreFactory = new ScoreFactory(configuration, instruments, measures, title, workNumber);				
		score = scoreFactory.createScore();

		return score;
	}

	/**
	 * Reads point data from configured DataSource
	 * MongoDB data source uses queries for configured instruments.
	 * 
	 * File data source streams data JSON data from a file.
	 * Random data source creates point data in real time
	 * DataSource is set globally in the config.properties file but can be overridden
	 * for an individual instrument by setting dataSource.<instrument-name>
	 * All DataSources return data is the same JSON format.
	 * To give some variety to data sets, DataSource can be configured to select points randomly
	 * This depends on setting of sequentialSelection (if false, use random selection)
	 * @throws DataLoadException (RuntimeException)
	 * 
	 * TODO - make Async
	 */
	public void loadData() throws DataLoadException  {
		log.debug("loadData()");
		DataLoader dataLoader = null;
		try {
			for(String instrumentName : instrumentNames) {
				MessageProducer producer = producers.get(instrumentName);
				dataLoader = new DataLoader(instrumentName, configuration, producer, dataSourceName, session);
				Thread thread = new Thread(dataLoader);
				thread.start();
			}
		} catch (Exception e) {
			log.error("loadData exception: " +e.toString());
			throw(new DataLoadException("loadData exception: " +e.toString()));
		}
		/*
		 * wait for threads to complete
		 */
		boolean complete;
		do {
			complete = true;
			for(String instrumentName : instrumentNames) {
				State state = dataLoader.getState();
				log.trace(instrumentName + " state: " + state);
				complete &= (state.equals(State.COMPLETE) || state.equals(State.ERROR));
				if(state.equals(State.ERROR)) {
					log.error("ERROR loading data for " + instrumentName);
				}
				else if(state.equals(State.COMPLETE)) {
					log.info("data for " + instrumentName + " loaded");
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					log.error("Thread interrupted");
				}
			}
		} while (!complete);
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
    void configure() throws ConfigurationException {
    	ProductionFlowConfigurator configurator = new ProductionFlowConfigurator(this);
    	configurator.configure(configuration);
		configProperties = configuration.getProperties();
		
		// properties passed on the command line
		configProperties.setProperty("measures", "" + measures);
    	
    	/*
    	 * Transformers
    	 */
    	createTransformers();
    	
    	/*
    	 * RhythmScales
    	 */
    	createRhythmScales();
   		
    	/*
    	 * Transport
    	 */
   		configureTransport();
   		
   		/*
   		 * make it globally accessible
   		 */
        Configuration.setConfiguration(this.configuration);
    }

    private void configureTransport()  throws ConfigurationException {
    	try {
	   		if(dataSourceTransport.equalsIgnoreCase("activemq")) {
		    	user=configProperties.getProperty("activeMQ.user");
		    	password = configProperties.getProperty("activeMQ.password");
		    	url = configProperties.getProperty("activeMQ.url" );
		
		        /*
		         *  Set up the ActiveMQ JMS Components
		         */
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
	    catch(JMSException e) {
	    	 log.error("JMSException: " + e.toString());
	    	 throw new ConfigurationException(e.toString());
	    }
	}

	/**
     * Creates and assigns the appropriate RhythmScales and ExpressionSelector for each instrument.
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
    		String tiekey = "music.instrument." + instrumentName + ".tieAcrossBarline";
    		double tieProbablilty =  configProperties.containsKey(tiekey) ? Double.parseDouble(configProperties.getProperty(tiekey)) : 0.0;
    		
    		String ckey = "music.instrument." + instrumentName + ".chordalProbablility";
    		double chordalProbability = (configProperties.containsKey(ckey)) ? Double.parseDouble(configProperties.getProperty(ckey)) : 0.0;
    		
    		String key = "score.rhythmScale.instrument." + instrumentName;
    		if(configProperties.containsKey(key)) {
    			String rsName = configProperties.getProperty(key);
    			factory = RhythmScaleFactory.getRhythmScaleFactory(rsName);
    			IRhythmScale rs = factory.createRhythmScale(rsName);
    			instrument.setRhythmScale(rs);
    			if(rs.isChordal()) {
    				rs.setChordalTextureProbability(chordalProbability);
    			}
    			rs.getExpressionSelector().setTieAcrossBarlineProbability(tieProbablilty);
    		}
    		else {
    			allRhythmScale.getExpressionSelector().setTieAcrossBarlineProbability(tieProbablilty);
    			if(allRhythmScale.isChordal()) {
    				allRhythmScale.setChordalTextureProbability(chordalProbability);
    			}
    			instrument.setRhythmScale(allRhythmScale);
    		}
    	}
	}

	@SuppressWarnings("unchecked")
	void createTransformers() throws ConfigurationException {
		Class<ITransformer> tclass = null;
		ITransformer transformer = null;
		String[] tforms = null;
		try {
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
	        			transformer = tclass.getDeclaredConstructor().newInstance();
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
		    			transformer = tclass.getDeclaredConstructor().newInstance();
		    			transformer.configure(configProperties, instrument);
		    			transformers.add(transformer);
		    		}
	    		}
	    	}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw new ConfigurationException(ex.toString());
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

	public Map<String, String[]> getFieldNames() {
		return fieldNames;
	}
    
}
