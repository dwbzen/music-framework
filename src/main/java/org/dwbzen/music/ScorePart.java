package org.dwbzen.music;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.dwbzen.common.math.*;
import org.dwbzen.common.math.ifs.IteratedFunctionSystem;
import org.dwbzen.music.action.DurationScaler;
import org.dwbzen.music.action.ExpressionSelector;
import org.dwbzen.music.action.PitchScaler;
import org.dwbzen.music.element.Chord;
import org.dwbzen.music.element.Duration;
import org.dwbzen.music.element.IRhythmExpression;
import org.dwbzen.music.element.IRhythmScale;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Label;
import org.dwbzen.music.element.Measurable;
import org.dwbzen.music.element.Measurable.TupletType;
import org.dwbzen.music.element.Measure;
import org.dwbzen.music.element.Note;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.RhythmicUnitType;
import org.dwbzen.music.element.Score;
import org.dwbzen.music.element.ScorePartEntity;
import org.dwbzen.music.element.Tempo;
import org.dwbzen.music.element.TextureType;
import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.music.transform.ITransformer.Preference;
import org.dwbzen.util.Configuration;
import org.dwbzen.util.Ratio;
import org.dwbzen.util.messaging.SessionImpl;

/**
 * 
 * @author don_bacon
 *
 */
public class ScorePart implements Serializable, Runnable {

	private static final long serialVersionUID = -8550433867242770122L;
	protected static final Logger log = LogManager.getLogger(ProductionFlow.class);
	
	private ThreadLocalRandom random = ThreadLocalRandom.current();
	private ScorePartEntity	scorePartEntity = null;
	private Measure currentMeasure;
	private PointSet<Double> scorePartData = null;
	
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
	public enum State {UNKNOWN, INIT, WORKING, COMPLETE, ERROR};
	
	public ScorePart(Score score, String pname, Instrument instr) {
		state = State.INIT;
		scorePartEntity = new ScorePartEntity(score, pname, instr);
		scorePartEntity.setPartId("P-" + pname);
		scorePartData = new PointSet<Double>();
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
			log.info("collectScorePartData completed.");
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
    	List<Point2D<Double>> points = scorePartData.getPoints();
    	PitchScaler ps = instrument.getPitchScaler();
    	ps.setMinVal(scorePartData.getMinXValue());
    	ps.setMaxVal(scorePartData.getMaxXValue());
    	
    	DurationScaler durationScaler = instrument.getDurationScaler();
    	durationScaler.setMinVal(scorePartData.getMinYValue());
    	durationScaler.setMaxVal(scorePartData.getMaxYValue());
    	for(Point2D<Double> point: points) {
    		Pitch pitch = instrument.scale(point.getX().doubleValue());
    		Duration duration = durationScaler.scaleToRhythmScale(point.getY().doubleValue());
    		double rawUnits = duration.getRawDuration();
    		
    		// scale raw point value to RhythmScale units
    		// set dots after determining the expression to use for these units
    		int units = rhythmScale.findClosestUnits(rawUnits, Preference.Up);

    		// use RhythmScale factors to get the Durations (units & dots) for scaled units
    		List<Duration> factors =  rhythmScale.getFactors(units);
    		if(factors == null) {
    			System.err.println("Null factors for " + units + " units, rawUnits= " + rawUnits);
    			continue;
    		}
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
    	Chord chord = null;
    	noteIterator = notes.iterator();
    	int measureCounter = 1;
    	ExpressionSelector selector = rhythmScale.getExpressionSelector();
    	double tieProbability = selector.getTieAcrossBarlineProbability();
    	int notesInThisChord = 1;	// if CHORDAL texture
 
    	// assigned when creating tuplet group
    	// Each Measurable can be a single Note or a Chord
    	List<Measurable> tupletGroup = null;
    	Note lastNote = null;
		Chord lastChord = null;		// TODO finish chord implementation
    	do {
	    	if((note = getNextNote()) != null) {
	    		int units = note.getDuration().getDurationUnits();
	    		TextureType tt = selector.selectTextureType(units);
	    		if(tt == null) {
	    			log.error(units);
	    		}
	    		/*
	    		 * Select the Expression for this note
	    		 * If a single note (METRIC), we are done.
	    		 * If EXTRAMETRIC, read additional notes to fulfill tuplet
	    		 * If CHORDAL, read additional notes to add to the chord
	    		 */
	    		boolean chordal = false;
	    		IRhythmExpression rhythmExpression = selector.selectRhythmExpression(units, tt);
	    		RhythmicUnitType rut = rhythmExpression.getRhythmicUnitType();

	    		log.debug("   rhythmExpression units: " + units + " " + rut);
	    		if(tt.equals(TextureType.CHORDAL)) {
	    			notesInThisChord = selector.getNumberOfNotesInChord(rhythmExpression);
	    			chordal = notesInThisChord > 1;
	    			log.debug("measure: " + measureCounter + " units: " + units + " units count: " + unitsCount + " textureType: " + tt);
	    			log.debug("   " + rhythmExpression.toString());
	    			log.debug("   chord notes " + notesInThisChord);
	    		}

    			String noteType = rhythmScale.getNoteType(note);
	    		/*
	    		 *  do not allow EXTRAMETIC (tuplets) to tie across the bar line.
	    		 *  If adding this tuplet would cause that to happen, revert to METRIC
	    		 */
	    		if(rut.equals(RhythmicUnitType.METRIC)  || unitsCount + units > unitsPerMeasure) {
	    			// nothing more to do - can use the Note as is
	    			log.trace("   add " + RhythmicUnitType.METRIC + " note: " + note.toString() );
	    			tupletGroup = null;
	    			if(chordal) {
	    				// create a chord having the number notesInThisChord # of notes
	    				chord = createChord(note, notesInThisChord, noteType);
	    			}
	    		}
	    		else if(rut.equals(RhythmicUnitType.EXTRAMETRIC)) {

	    			log.trace("   add " + rut + " note: " + note.toString() );
	    			tupletGroup = new ArrayList<Measurable>();
	    			Ratio ratio =  rhythmExpression.getRatio();
	    			int notesInThisGroup = ratio.getNumberOfNotes();
	    			int groupUnits = rhythmExpression.getUnits();
	    			note.getDuration().setRatio(new Ratio(ratio));
	    			note.getDuration().setDurationUnits(groupUnits);
	    			String tupletNoteType = rhythmScale.getNoteType(note);
	    			note.setNoteType(tupletNoteType);
	    			lastNote = note;
	    			note.setTupletType(TupletType.START);
	    			Note groupNote = null;
	    			Chord groupChord = null;
	    			Measurable mNoteOrChord = null;
	    			
	    			if(chordal) {
	    				groupChord = createChord(note, notesInThisChord, noteType);
    					groupChord.setTupletType(TupletType.START);
	    				tupletGroup.add(groupChord);
	    			}
	    			else {
	    				tupletGroup.add(new Note(note));
	    			}
	    			// get notesInThisGroup-1 more notes or more Chords if CHORDAL
	    			for(int i=1; i<notesInThisGroup; i++) {
    					groupNote = new Note(getNextNote());
	    				if(chordal) {
	    					groupChord = createChord(groupNote, notesInThisChord, noteType);
	    					tupletGroup.add(groupChord);
	    					mNoteOrChord = groupChord;
	    				}
	    				else {
	    					groupNote.setDuration(note.getDuration());	// keep the pitch, copy the Duration
	    					groupNote.setNoteType(tupletNoteType);
	    					tupletGroup.add(groupNote);
	    					mNoteOrChord = groupNote;
	    				}
	    			}
	    			mNoteOrChord.setTupletType(TupletType.STOP);
	    		}
	    		
	    		if(unitsCount + units <= unitsPerMeasure) {	// units fit in the measure
	    			if(tupletGroup != null && tupletGroup.size() > 0 ) {
	    				measure.addMeasurables(tupletGroup);
	    				tupletGroup = null;
	    			}
	    			else {
	    				if(chordal) {
	    					measure.addMeasureable(chord);
	    					lastChord = chord;
	    				}
	    				else {
	    					note.setNoteType(rhythmScale.getNoteType(note));
	    					measure.addMeasureable(new Note(note));
	    					lastNote = note;
	    				}
	    			}
	    			unitsCount += units;
	    			if(unitsCount == unitsPerMeasure) {
	    				measureCounter++;
	    				unitsCount = 0;
	    				measure = createNewMeasure();
	    			}
	    		}
	    		else {		// units exceed what's available in the Measure 
	    			/*
	    			 * Create a new measure and note tied to the previous
	 				 * Tuplets are not allowed to span across measures
	    			 * for example, unitsCount = 14, units = 8
	    			 * unitsNextMeasure is (unitsCount + units) - unitsPerMeasure = (14 + 8) - 16 = 6
	    			 * unitsThisMeasure is units - unitsNextMeasure = 8 - 6 = 2
	    			 */
	    			int unitsNextMeasure = (unitsCount + units) - unitsPerMeasure;
	    			int unitsThisMeasure = units - unitsNextMeasure;
	    			Note lastNoteAdded = null;
	    			Chord lastChordAdded = null;
	    			log.trace("   Tie notes measure " + measure.getNumber() + 	",  unitsCount, units: " + unitsCount + ", " 
	    					+ units + " unitsNextMeasure: " + unitsNextMeasure 
	    					+ " unitsThisMeasure: " + unitsThisMeasure );
	    			
	    			// tie across the bar line determined by tieProbability for this instrument
	    			boolean tieToNote = random.nextDouble() <= tieProbability;
	    			
	    			if(chordal) {
	    				chord.setTupletType(TupletType.NONE);
	    				lastChordAdded = addFactorsChords(unitsThisMeasure, chord, measure);
		    			measure = createNewMeasure();
		    			measureCounter++;
		    			addFactorsChords(unitsNextMeasure, lastChordAdded, measure);
	    				log.trace("   chord, lastChordAdded: " + chord + " " + lastChordAdded);
	    			}
	    			else {
		    			note.setTupletType(TupletType.NONE);
		    			lastNoteAdded = addFactorsNotes(unitsThisMeasure, note, measure, false);
		    			measure = createNewMeasure();
		    			measureCounter++;
		    			addFactorsNotes(unitsNextMeasure, lastNoteAdded, measure, tieToNote);
		    			log.trace("   note, lastNoteAdded: " + note + " " + lastNoteAdded);
	    			}
	    			tupletGroup = null;
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
    
    public Measurable addFactors(int units, Measurable aMeasurable, Measure measure, boolean tieToNote) {
    	Measurable noteOrChord = null;
    	if(aMeasurable instanceof Note) {
    		noteOrChord = addFactorsNotes(units, (Note) aMeasurable, measure, tieToNote);
    	}
    	else if(aMeasurable instanceof Chord) {
    		noteOrChord = addFactorsChords(units, (Chord) aMeasurable, measure);
    	}
    	return noteOrChord;
    }
    
	/**
     * Adds Chord(s) to a Measure based on units provided, using the Notes of a given Chord
     * If the Pitch or any of the Notes happens to be null, the Chord provided is a rest - and so are all the
     * factor notes.
     * @param units units to factor - may be 1 to n Durations depending on the RhythmScale
     * @param note the Chord providing the Notes to use
     * @param measure the Measure to add Chords to
     * @return the last Chord added to the Measure
     */
    private Chord addFactorsChords(int units, Chord aChord, Measure measure) {
    	List<Duration> factors =  rhythmScale.getFactors(units);
    	Chord chord = null;
		for(Duration duration : factors) {
			chord = Chord.createChord(aChord, duration);
			measure.addMeasureable(chord);
		}
		return chord;
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
    private Note addFactorsNotes(int units, Note aNote, Measure measure, boolean tieToNote) {
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
    
    private Chord createChord(Note firstNote, int notesInThisChord, String chordNoteType) {
		// create a chord having notesInThisChord notes
    	Chord chord = new Chord();
		chord.addNote(firstNote);
		Duration duration = firstNote.getDuration();
		Note chordNote = null;
		for(int i=1; i<notesInThisChord; i++) {
			chordNote = new Note(getNextNote());
			chordNote.setDuration(duration);
			chordNote.setNoteType(chordNoteType);
			chord.addNote(chordNote);
		}
		chord.setDuration(duration);
		chord.setNoteType(chordNoteType);
		return chord;
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
	public void processMessage(JsonObject jsonObj) {
    	String type = jsonObj.getType();
       	if(type.equals(CommandMessage.objectType)) {			// "message"
       		CommandMessage cm = (CommandMessage)jsonObj;
       		log.trace(" command message: " + cm.getCommand());
       	}
       	else if(type.equals(Point2D.ObjectType)) {				// "Point2D"
       		Point2D<Double> point = (Point2D<Double>)jsonObj;
       		log.trace(" point for " + getPartName() + ": " + point.toJson());
       		if(scorePartData != null) {
       			scorePartData.add(point);
       		}
       		else {
       			System.err.println("PointSet is null!!");
       		}
       	}
       	else if(type.equals(PointSetStats.objectType)) {		// "stats"
       		PointSetStats<Double> stats = (PointSetStats<Double>)jsonObj;
       		scorePartData.setStats(stats);
       		log.debug("pointSet for " + getPartName() + ": " + scorePartData.toJson());
       	}
       	else if(type.equals(IteratedFunctionSystem.objectType)) {	// "IFS"
       		IteratedFunctionSystem ifs = (IteratedFunctionSystem)jsonObj;
       		scorePartData.setIteratedFunctionSystem(ifs);
       	}
       	else if(type.equalsIgnoreCase(JsonObject.UNKNOWN)) {	// "unknown"	
       		BaseJsonObject base = (BaseJsonObject)jsonObj;
        	log.error("Unknown message type " + base.toJson());
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

	public Instrument getInstrument() {
		return instrument;
	}
	
	public String getInstrumentName() {
		return instrument.getName();
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

	public PointSet<Double> getScorePartData() {
		return scorePartData;
	}

	public List<Note> getNotes() {
		return notes;
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
