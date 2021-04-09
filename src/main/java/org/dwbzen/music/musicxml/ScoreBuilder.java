package org.dwbzen.music.musicxml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dwbzen.music.IScoreFactory;
import org.dwbzen.music.ScoreFactory;
import org.dwbzen.music.ScorePart;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Measure;
import org.dwbzen.music.element.Phrase;
import org.dwbzen.music.element.Score;
import org.dwbzen.music.element.ScorePartEntity;
import org.dwbzen.music.element.Tempo;
import org.dwbzen.music.element.direction.Metronome;
import org.dwbzen.music.element.direction.ScoreDirection;
import org.dwbzen.music.element.direction.Words;
import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.util.Configuration;
import org.dwbzen.util.music.InstrumentMaker;

/**
 * Creates and builds a musicXML Score given:<br>
 * <dl>
 * <dt>title</dt><dd>- the score title. Default is "Title"</dd>
 * <dt>instrument</dt><dd>- the name(s) of the score instrument(s), default is "Piano" </dd>
 * <dt>time signature</dt><dd>- the initial time signature, for example "4/4" or "6/8", default is "4/4"</dd>
 * <dt>key</dt><dd>- the initial key signature, as in "E-Minor" or "F#-Major", default is "C-Major"</dd>
 * <dt>tempo</dt><dd>- the inital tempo in beats/minute, default is 90</dd>
 * </dl>
 * Once a ScoreBuilder instance is created, there are methods to<br>
 * add measures, change key or time signature, and change/add instruments.<br>
 * The client optionally provides the complete output file path. 
 * The default if not specified $HOMEPATH/"Title_"date-time.musicxml, <br>
 * where date-time has the format "yyyyMMdd'T'HH:mm:ss.SSSXXX" <p>
 * Usage pseudocode:<p>
 * <code>
 * 	ScoreBuilder scoreBuilder = new ScoreBuilder("My Title", "Piano")	// default key, tempo, time signature, and output file  <br>
 *  // create an empty Score instance
 *  scoreBuilder.createScore();
 * 	Measure measure = getMeasure(); 	// client method to create/get a Measure instance  <br>
 *  scoreBuilder.add(measure);  <br>
 *  // after completing adding measures, output the score  <br>
 *  Score score = scoreBuilder.getScore();  <br>
 *  scoreBuilder.outputMusicXMLScore();  <br>
 * </code>
 * <p>
 * The ScoreBuilderRunner class can be used or sub-classed to get client input and create/output a score.<br>
 * Note - Measure(s) are not checked for errors (except null) before adding<br>
 * as they are assumed to be complete and correct.<p/>
 * Note - currently this supports building a score for a single instrument only!<p/>
 * Note - currently supports a "4/4" time signature only.
 *
 * @author don_bacon
 * @see org.dwbzen.music.ScoreFactory
 * @see org.dwbsen.music.musicxml.ScoreBuilderRunner
 *
 */
public class ScoreBuilder {
	
	static final org.apache.log4j.Logger log = Logger.getLogger(ScoreBuilder.class);
	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss.SSSXXX");

	public static final String CONFIG_FILENAME = "/config.properties";
	public static final String ORCHESTRA_CONFIG_FILENAME="/orchestra.properties";
	public static final String defaultInstrumentName = "Piano";
	public static final String defaultTitle = "Title";
	public static final String defaultTimeSignature = "4/4";
	public static final String defaultKeyName = "C-Major";
	
	private Configuration configuration = null;
    private Score score = null;
    private ScorePart scorePart = null;
    /*
     * Since the Score will have a single instrument,
     * there is only one ScorePartEntity.
     */
    private ScorePartEntity scorePartEntity = null;
	private String scoreTitle = null;
	private Map<String, Instrument> instruments = null;
	private String instrumentName = null;
	private Instrument scoreInstrument = null;
	private String outputFileName = null;	// the base output file path set by the user
	private String fileName = null;			// the complete output file name
    private int tempoBpm = 80;
    private String timeSignature = defaultTimeSignature;
    private Tempo tempo = null;
    private String keyName = defaultKeyName;
    private Key scoreKey = null;
    private int numberOfMeasures = 0;		// count of measures added
    private Metronome metronomeDirectionType = null;
    private ScoreDirection metronomeScoreDirection = null;
	
	public ScoreBuilder() {
		this(defaultTitle);
	}
	
    public ScoreBuilder(String title) {
		scoreTitle = title;
		instrumentName = defaultInstrumentName;
    }
    
    public ScoreBuilder(String title, String instrumentname) {
		scoreTitle = title;
		instrumentName = instrumentname==null ? defaultInstrumentName : instrumentname;
    }
    
	public void configure() {
		configuration = Configuration.getInstance(CONFIG_FILENAME);
		configuration.addConfiguration(Configuration.getInstance(ORCHESTRA_CONFIG_FILENAME));
		String date = dateFormat.format(new Date());
		if(outputFileName != null) {
			fileName = outputFileName + date + ".musicxml";
		}
		else {
			outputFileName =  getHomePath() + "\\" + scoreTitle.strip().replace(' ', '_');
			fileName = outputFileName + "_" + date + ".musicxml";
		}
	}
	
	public Score createScore() {
		configure();
		score = createMusicXmlScore();
		return score;
	}
	
	private Score createMusicXmlScore() {
		log.debug("createMusicXmlScore()");
		int nmeasures = -1;	// don't limit the number of measures
		String workNumber = dateFormat.format(new Date());
		/*
		 *  configure the instrument specified, including creating the associated RhythmScale
		 */
		configureInstrument();
		
		/*
		 * Create the Score, ScorePart and ScorePartEntity
		 */
		IScoreFactory scoreFactory = new ScoreFactory(configuration, instruments, nmeasures, scoreTitle, workNumber);
		score = scoreFactory.createScore(false);
		/*
		 * set the tempo and key
		 */
		scorePartEntity = score.getScorePartEntityForInstrument(instrumentName);
		scorePart = score.getScoreParts().get(instrumentName);
		tempo = new Tempo(tempoBpm);
		scoreKey = new Key(keyName);
        scorePartEntity.setScoreKey(scoreKey);
        
		/*
		 * Create Directions for the scale name and steps (Words) and the metronome setting
		 * Words are attached to staff 1 for on the first measure of each scale.
		 * Metronome setting appears once in Measure 1
		 */
        metronomeDirectionType = new Metronome(tempoBpm);
        metronomeScoreDirection = new ScoreDirection(1, metronomeDirectionType);
        metronomeScoreDirection.setPlacement("above");
        
		return score;
	}
	
	/**
	 * Set score instrument to Piano (default) or specified instrument using the<br>
	 * RhythmScale configured for this instrument otherwise the default rhythm scale.<br>
	 * This also sets the number of units per measure to 480 (or the root of RhythmScale).
	 * @throws RuntimeException if the specified instrument is invalid or doesn't exist.
	 */
    public void configureInstrument() {
		InstrumentMaker instrumentMaker = new InstrumentMaker(instrumentName, configuration);
		instruments = instrumentMaker.get();
		if(instruments == null || instruments.isEmpty()) { 
			// invalid instrument was specified
			throw new RuntimeException("No such instrument exists: " + instrumentName);
		}
		scoreInstrument = instruments.get(instrumentName);
		if(scoreInstrument == null) {
			throw new RuntimeException(instrumentName + " is invalid");
		}
	}
    
    public boolean add(Measure measure) {
    	ScorePartEntity spe = getScorePartEntity();
    	if(numberOfMeasures == 0) {
    		measure.addScoreDirection(metronomeScoreDirection);
    	}
    	numberOfMeasures++;
    	return spe.addMeasure(measure);
    }
    
    
    public boolean add(List<Measure> measures) {
    	ScorePartEntity spe = getScorePartEntity();
    	int nmeasures = measures.size();
    	if(numberOfMeasures == 0 && nmeasures >0) {
    		measures.get(0).addScoreDirection(metronomeScoreDirection);
    	}
    	numberOfMeasures += nmeasures;
    	return spe.addMeasures(measures);
    }
    
    public boolean add(Phrase phrase) {
    	ScorePartEntity spe = getScorePartEntity();
    	int nmeasures = phrase.size();
    	if(numberOfMeasures == 0 && nmeasures >0) {
    		phrase.getMeasures().get(0).addScoreDirection(metronomeScoreDirection);
    	}
    	numberOfMeasures += nmeasures;
    	return spe.addPhrase(phrase);
    }
    
	public Score getScore() {
		return score;
	}

	public void setScore(Score score) {
		this.score = score;
	}

	public String getScoreTitle() {
		return scoreTitle;
	}

	public void setScoreTitle(String scoreTitle) {
		this.scoreTitle = scoreTitle;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}
	
	public int getTempoBpm() {
		return tempoBpm;
	}

	public void setTempoBpm(int tempoBpm) {
		this.tempoBpm = tempoBpm;
		tempo = new Tempo(tempoBpm);
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
		scoreKey = new Key(keyName);
	}

	public ScorePart getScorePart() {
		return scorePart;
	}

	public Tempo getTempo() {
		return tempo;
	}

	public Key getScoreKey() {
		return scoreKey;
	}
	
    public ScorePartEntity getScorePartEntity() {
    	if(scorePartEntity == null && score != null) {
    		scorePartEntity = score.getScorePartEntityForInstrument(instrumentName);
    	}
		return scorePartEntity;
	}

	public int getNumberOfMeasures() {
		return numberOfMeasures;
	}
	
	public int size() {
		return getNumberOfMeasures();
	}

	public void outputMusicXMLScore() {
    	createXML(fileName, score, configuration);
    }

	public static void createXML(String filename, Score score,  Configuration config) {
		MusicXMLHelper helper = new MusicXMLHelper(score, config.getProperties());
		helper.setSuppressTempoMarking(true);
		helper.convert();	// creates and returns a com.audiveris.proxymusic.ScorePartwise
		PrintStream ps = System.out;
		if(filename != null) {
			try {
				ps = new PrintStream(new FileOutputStream(filename));
			}
			catch(FileNotFoundException e) {
				log.warn(filename + " not available. Marshall to System.out");
			}
		}
		helper.marshall(ps);	// marshals the ScorePartwise instance to an XML file
		if(filename != null) {
			ps.close();
		}
	}
	
	public static String getHomePath() {
		 String value = System.getenv("HOMEPATH");
		 return value == null ? "C:" : value;
	}
	
}
