package music;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mathlib.BaseJSONObject;
import mathlib.CommandMessage;
import mathlib.JSONObject;
import mathlib.Point2D;
import mathlib.PointSet;
import music.action.DurationScaler;
import music.action.ExpressionSelector;
import music.action.PitchScaler;
import music.element.Duration;
import music.element.IRhythmExpression;
import music.element.IRhythmScale;
import music.element.Key;
import music.element.Label;
import music.element.Measurable;
import music.element.Measurable.TupletType;
import music.element.Measure;
import music.element.Note;
import music.element.Pitch;
import music.element.RhythmicUnitType;
import music.element.Score;
import music.element.ScorePartEntity;
import music.element.Tempo;
import music.element.TextureType;
import music.instrument.Instrument;
import music.transform.ITransformer.Preference;
import util.Configuration;
import util.Ratio;
import util.messaging.SessionImpl;

/**
 * 
 * @author don_bacon
 *
 */
public class ScorePart implements Serializable, Runnable {

	private static final long serialVersionUID = -8550433867242770122L;
	protected static final Logger log = LogManager.getLogger(ProductionFlow.class);

	private ScorePartEntity	scorePartEntity = null;
	private Measure currentMeasure;
	private PointSet<Number> scorePartData = null;
	
	// raw notes used to populate Measures
	private List<Note> notes = new ArrayList<Note>();
	private Iterator<Note> noteIterator = null;
	
	private String partName;
	private Instrument instrument;
	private IRhythmScale rhythmScale = null;
	private int maxMeasures = 0;	// max# of measures to create, always set to !=0 value
	private int divsPerMeasure = 0;		// root from RhythmScale

	private Configuration configuration = null;
	private Destination destination = null;
	private Connection connection = null;
	private Session session = null;
	private MessageConsumer consumer = null;
	private ScorePartMessageListener messageListener = null;
	private State state = State.UNKNOWN;
	private Tempo tempo = null;		// set from config as in score.tempo=90
	private Key scoreKey = null;	// set from configuration as in: score.key=F-Major
	public enum State {UNKNOWN, INIT, WORKING, COMPLETE};
	
	public ScorePart(Score score, String pname, Instrument instr) {
		state = State.INIT;
		scorePartEntity = new ScorePartEntity(score, pname, instr);
		scorePartEntity.setPartId("P-" + pname);
		configuration = score.getConfiguration();
		partName = pname;
		instrument = instr;
		rhythmScale = instrument.getRhythmScale();
		divsPerMeasure = rhythmScale.getRoot();
	}
	
	@Override
	public void run() {
		state = State.WORKING;
		log.info("Running ScorePart " + scorePartEntity.getPartName());
		try {
			configure();
			collectScorePartData();
			log.info("collectScorePartData completed. Closing connection");
			connection.close();
		} catch (Exception e) {
			log.error("exception: " + e.toString());
			e.printStackTrace();
		}
		/*
		 * Turn score part data into music
		 */
		log.info("ScorePart " + partName);
		createScorePart();
		log.info("ScorePart " + partName + " complete");
		state = State.COMPLETE;
		return;
	}
	
	/**
	 * Turns the PointSet in scorePartData into actual music for this instrument
	 * Use configured RhythmScale for this instrument to set duration units
	 */
	public void createScorePart() {
    	List<Point2D<Number>> points = scorePartData.getPoints();
    	PitchScaler ps = instrument.getPitchScaler();
    	ps.setMinVal(scorePartData.getMinXValue());
    	ps.setMaxVal(scorePartData.getMaxXValue());
    	
    	DurationScaler durationScaler = instrument.getDurationScaler();
    	durationScaler.setMinVal(scorePartData.getMinYValue());
    	durationScaler.setMaxVal(scorePartData.getMaxYValue());
    	for(Point2D<Number> point: points) {
    		Pitch pitch = instrument.scale(point.getX().doubleValue());
    		Duration duration = durationScaler.scaleToRhythmScale(point.getY().doubleValue());
    		double rawUnits = duration.getRawDuration();
    		
    		// scale raw point value to RhythmScale units
    		// set dots after determining the expression to use for these units
    		int units = rhythmScale.findClosestUnits(rawUnits, Preference.Up);

    		// use RhythmScale factors to get the Durations (units & dots) for scaled units
    		List<Duration> factors =  rhythmScale.getFactors(units);
    		for(Duration df : factors) {
        		Note note = new Note(pitch, df);
        		note.setPoint(point);
        		log.trace(instrument.getAbreviation() + ": " + note);
        		notes.add(note);
    		}
    	}
    	scoreInstrument();
    	
	}
	
	private Note getNextNote() {
		Note n = noteIterator.hasNext() ? noteIterator.next() : null;
		return n;
	}

    /**
     * This takes creates Measures from the  List<Note> notes
     * created by  createScorePart().
     * Note durations are now in units, converted using the instrument's RhythmScale.
     * First determine the RhythmExpression to use for the number of units in the Note.
     * If 1-1 we're done - just determine the dots and then add the Note to the measure.
     * If expression is EXTRAMETRIC, as in a tuplet, need to gather more notes to
     * create that tuplet. It uses the Pitch in each note added (to the tuplet),
     * but not the #units (so it's somewhat wasteful in that regard).
     * 
     */
    public void scoreInstrument() {
    	Measure measure = createNewMeasure();	// measure #1
    	int unitsPerMeasure = measure.getDivisions();
    	int unitsCount = 0;	// number of divisions (units) added so far
    	Note note = null;
    	noteIterator = notes.iterator();
    	int measureCounter = 1;
    	ExpressionSelector selector = rhythmScale.getExpressionSelector();
    	List<Measurable> tupletGroup = null;		// assigned when creating tuplet group
    	Note lastNote = null;
    	
    	do {
	    	if((note = getNextNote()) != null) {
	    		int units = note.getDuration().getDurationUnits();
	    		TextureType tt = selector.selectTextureType(units);
	    		if(tt == null) {
	    			System.out.println(units);
	    		}
	    		/*
	    		 * Select the Expression for this note
	    		 * If a single note (METRIC), we are done.
	    		 * If EXTRAMETRIC, read additional notes to fulfill tuplet
	    		 * If CHORDAL, read additional notes to add to the chord
	    		 */
	    		boolean chordal = tt.equals(TextureType.CHORDAL);
	    		log.debug("-->measure: " + measureCounter + " units: " + units + " units count: " + unitsCount + " textureType: " + tt);
	    		IRhythmExpression rhythmExpression = selector.selectRhythmExpression(units);
	    		log.debug("   " + rhythmExpression);
	    		if(rhythmExpression == null) {
	    			log.warn("no rhythmExpression for " + units);
	    		}
	    		else {
	    			log.debug("   rhythmExpression units: " + units + " " + rhythmExpression.getRhythmicUnitType());
	    		}
	    		RhythmicUnitType rut = rhythmExpression.getRhythmicUnitType();
	    		/*
	    		 *  do not allow tuplets across the bar line.
	    		 *  If adding this EXTRAMETIC tuplet would cause that to happen, revert to METRIC
	    		 */
	    		if(rut.equals(RhythmicUnitType.METRIC)  || unitsCount + units > unitsPerMeasure) {
	    			// nothing more to do - can use the Note as is
	    			log.debug("   add " + RhythmicUnitType.METRIC + " note: " + note.toString() );
	    			tupletGroup = null;
	    		}
	    		else if(rut.equals(RhythmicUnitType.EXTRAMETRIC)) {

	    			log.debug("   add " + rut + " note: " + note.toString() );
	    			tupletGroup = new ArrayList<Measurable>();
	    			Ratio ratio =  rhythmExpression.getRatio();
	    			int notesInThisGroup = ratio.getNumberOfNotes();
	    			int groupUnits = rhythmExpression.getUnits();
	    			note.getDuration().setRatio(new Ratio(ratio));
	    			note.getDuration().setDurationUnits(groupUnits);
	    			String tupletNoteType = rhythmScale.getNoteType(note);
	    			note.setNoteType(tupletNoteType);
	    			note.setTupletType(TupletType.START);
	    			tupletGroup.add(new Note(note));	// clone it
	    			lastNote = note;
	    			// get notesInThisGroup-1 more notes
	    			Note groupNote = null;
	    			for(int i=1; i<notesInThisGroup; i++) {
	    				groupNote = new Note(getNextNote());		// clone it
	    				groupNote.setDuration(note.getDuration());	// keep the pitch, copy the Duration
	    				groupNote.setNoteType(tupletNoteType);
	    				tupletGroup.add(groupNote);
	    			}
	    			groupNote.setTupletType(TupletType.STOP);
	    		}
	    		
	    		if(unitsCount + units <= unitsPerMeasure) {
	    			if(tupletGroup != null && tupletGroup.size() > 0 ) {
	    				measure.addMeasurables(tupletGroup);
	    				tupletGroup = null;
	    			}
	    			else {
	    				note.setNoteType(rhythmScale.getNoteType(note));
	    				measure.addMeasureable(new Note(note));
	    				lastNote = note;
	    			}
	    			unitsCount += units;
	    			if(unitsCount == unitsPerMeasure) {
	    				measureCounter++;
	    				unitsCount = 0;
	    				measure = createNewMeasure();
	    			}
	    		}
	    		else {
	    			/*
	    			 * units exceed what's available in the Measure 
	    			 * need to create a new measure and note tied to the previous
	 				 * Tuplets are not allowed to span measures
	    			 * for example, unitsCount = 14, units = 8
	    			 * unitsNextMeasure is (unitsCount + units) - unitsPerMeasure = (14 + 8) - 16 = 6
	    			 * unitsThisMeasure is units - unitsNextMeasure = 8 - 6 = 2
	    			 */
	    			int unitsNextMeasure = (unitsCount + units) - unitsPerMeasure;
	    			int unitsThisMeasure = units - unitsNextMeasure;
	    			log.debug("   Tie notes measure " + measure.getNumber() + 	",  unitsCount, units: " + unitsCount + ", " + units + " unitsNextMeasure: " + unitsNextMeasure + " unitsThisMeasure: " + unitsThisMeasure );
	    			note.setTupletType(TupletType.NONE);
	    			tupletGroup = null;
	    			Note lastNoteAdded = addFactorsNotes(unitsThisMeasure, note, measure, false);
	    			measure = createNewMeasure();
	    			measureCounter++;
	    			addFactorsNotes(unitsNextMeasure, lastNoteAdded, measure, true);
	    			log.debug("   note, lastNoteAdded: " + note + " " + lastNoteAdded);
	    			unitsCount = unitsNextMeasure;
	    		}
	    	}	// end if(Notes)
    	} while(measureCounter <= maxMeasures);
    	
    	if(unitsCount < unitsPerMeasure) {
    		// fill out the last note to complete the measure - but don't tie to previous
    		int remaining = unitsPerMeasure - unitsCount;
    		log.debug(remaining + " units remaining in measure ");
    		addFactorsNotes(remaining, lastNote, measure, false);
    	}
    }
    
    /**
     * Adds Note(s) to a Measure based on units provided, using the Pitch of a given Note
     * If the Pitch happens to be null, the Note provided is a rest - and so are all the
     * factor notes.
     * @param units units to factor - may be 1 to n Durations depending on the RhythmScale
     * @param note the Note providing the Pitch to use
     * @param measure the Measure to add Notes to
     * @param tieToNote if true, tie the first factor note to the Note provided
     * @return the last Note added to the Measure
     */
    public Note addFactorsNotes(int units, Note aNote, Measure measure, boolean tieToNote) {
		List<Duration> factors =  rhythmScale.getFactors(units);
		Pitch pitch = aNote.getPitch();
		Note note = null;
		for(Duration df : factors) {
    		note = new Note(pitch, df);
    		note.setNoteType(rhythmScale.getNoteType(note));
    		if(tieToNote) {
    			aNote.setTiedTo(note);
    			note.setTiedFrom(aNote);
    			tieToNote = false;
    		}
    		measure.addMeasureable(note);
		}
		return note;
    }
    
    /**
     * Creates a new Measure. If non-null, it sets
     * attributes from currentMeasure which is treated as previous.
     * Divisions set from Measure.divisionsPerMeasure configured value which is
     * also the root of the RhythmScale
     * @return Measure
     */
    public Measure createNewMeasure() {
    	Measure newMeasure = null;
    	if(currentMeasure != null) {
    		newMeasure = Measure.createInstance(currentMeasure);
    	}
    	else {
    		newMeasure = Measure.createInstance(this);
    		newMeasure.setTempo(tempo);
    		newMeasure.setDivisions(divsPerMeasure);
			if(instrument.isTransposes()) {
				newMeasure.setKey(instrument.getKey(scoreKey));	// use the instrument key instead for transposing
			}
    	}
    	currentMeasure = newMeasure;
    	Label l = new Label(newMeasure.getNumber(), "");
    	addMeasure(l, newMeasure);
    	log.debug("created measure# " + newMeasure.getNumber());
    	return newMeasure;
    }
    
    public String toString() {
    	return scorePartEntity.toString();
    }
    
	private void collectScorePartData() {
    	try {
    		messageListener = new ScorePartMessageListener(this, consumer);
    		messageListener.run();
		} catch (Exception e) {
			log.error("collectScorePartData exception: " + e.toString());
			e.printStackTrace();
		}
	}

    @SuppressWarnings("unchecked")
	public void processMessage(JSONObject jsonObj) {
    	String type = jsonObj.getType();
       	if(type.equals("message")) {
       		CommandMessage cm = (CommandMessage)jsonObj;
       		log.info(" command message: " + cm.getCommand());
       	}
       	else if(type.equals("point")) {
       		Point2D<Number> point = (Point2D<Number>)jsonObj;
       		log.trace(" point for " + getPartName() + ": " + point.toJSON());
       		if(scorePartData != null) {
       			scorePartData.add(point);
       		}
       		else {
       			System.err.println("PointSet is null!!");
       		}
       	}
       	else if(type.equals("stats")) {
       		scorePartData = (PointSet<Number>)jsonObj;
       		log.trace("pointSet for " + getPartName() + ": " + scorePartData.toJSON());
       	}
       	else if(type.equalsIgnoreCase(JSONObject.UNKNOWN)) {
       		BaseJSONObject base = (BaseJSONObject)jsonObj;
        	log.error("Unknown message type " + base.toJSON());
      		 // nothing to see here
       	}
    }
    
	public void configure() throws Exception {
		Properties configProperties = configuration.getProperties();
		/*
		 * Set tempo and score Key
		 */
		int tempoBPM = Integer.parseInt(configProperties.getProperty("score.tempo", "90"));
		tempo = new Tempo(tempoBPM);
        scoreKey = new Key(configProperties.getProperty("score.key", "C-Major"));
        scorePartEntity.setScoreKey(scoreKey);
        configureTransport(configProperties);

    }
	
	private void configureTransport(Properties configProperties) throws Exception {
		String dataSourceTransport = configProperties.getProperty("dataSource.transport", "activeMQ");
		if(dataSourceTransport.equalsIgnoreCase("activemq")) {
	    	/*
	    	 * configure Active MQ
	    	 */
	    	String user=configProperties.getProperty("activeMQ.user");
	    	String password = configProperties.getProperty("activeMQ.password");
	    	String url = configProperties.getProperty("activeMQ.url" );
	        /*
	         *  Set up the JMS Components
	         */
	        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
	        connection = connectionFactory.createConnection();
	        connection.start();
	        
	        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	        destination = session.createQueue( getPartName() + ".queue");
	        consumer = session.createConsumer(destination);
		}
   		else {
   			session = new SessionImpl();
   			destination = session.createQueue(getPartName() + ".queue");
   			consumer = session.createConsumer(destination);
   		}
	}

	public String getPartName() {
		return partName;
	}
	public void setPartName(String partName) {
		this.partName = partName;
	}
	public Instrument getInstrument() {
		return instrument;
	}
	public String getInstrumentName() {
		return instrument.getName();
	}
	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}
	
	public void addMeasure(Label label, Measure measure) {
		measure.setLabel(label);
		scorePartEntity.getMeasures().add(measure);
	}
	
	public List<Measure> getMeasures() {
		return scorePartEntity.getMeasures();
	}

	public Measure getCurrentMeasure() {
		return currentMeasure;
	}

	public PointSet<Number> getScorePartData() {
		return scorePartData;
	}

	public List<Note> getNotes() {
		return notes;
	}

	public void setScorePartData(PointSet<Number> scorePartData) {
		this.scorePartData = scorePartData;
	}

	public ScorePartEntity getScorePartEntity() {
		return scorePartEntity;
	}

	public State getState() {
		return state;
	}

	public int getMaxMeasures() {
		return maxMeasures;
	}

	public void setMaxMeasures(int maxMeasures) {
		this.maxMeasures = maxMeasures;
	}

	public Key getScoreKey() {
		return scoreKey;
	}

	public void setScoreKey(Key scoreKey) {
		this.scoreKey = scoreKey;
	}

}
