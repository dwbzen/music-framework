package org.dwbzen.util.music;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;

import org.apache.log4j.Logger;
import org.dwbzen.music.IScoreFactory;
import org.dwbzen.music.ScoreFactory;
import org.dwbzen.music.ScorePart;
import org.dwbzen.music.element.Barline;
import org.dwbzen.music.element.Duration;
import org.dwbzen.music.element.IRhythmScale;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Measurable;
import org.dwbzen.music.element.Measure;
import org.dwbzen.music.element.Note;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Scale;
import org.dwbzen.music.element.ScaleFormula;
import org.dwbzen.music.element.ScaleType;
import org.dwbzen.music.element.Score;
import org.dwbzen.music.element.ScorePartEntity;
import org.dwbzen.music.element.Tempo;
import org.dwbzen.music.element.direction.Metronome;
import org.dwbzen.music.element.direction.ScoreDirection;
import org.dwbzen.music.element.direction.Words;
import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.music.instrument.Piano;
import org.dwbzen.music.musicxml.DisplayInfo;
import org.dwbzen.util.Configuration;

/**
 * Creates a Score consisting of scales for the scale formulas and roots specified.<br>
 * The Score can then be Marshalled to MusicXML file using MusicXMLHelper.</p>
 * When scoring on the so-called Grand Staff, each Measure has the<br>
 * notes for the top staff (staff=1) first, followed by the staff 2 notes.
 * 
 * @author don_bacon
 *
 */
public class ScoreScaleCreator implements BiFunction<List<ScaleFormula>, List<Pitch>, Score> {
	
	static final org.apache.log4j.Logger log = Logger.getLogger(ScoreScaleCreator.class);
	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss.SSSXXX");

	public static final String CONFIG_FILENAME = "/config.properties";
	public static final String ORCHESTRA_CONFIG_FILENAME="/orchestra.properties";
	public static final String instrumentName = "Piano";
	
	private Properties configProperties = null;
	private Configuration configuration = null;
    private Score score = null;
    private ScorePartEntity scorePartEntity = null;
    private ScorePart scorePart = null;
    private Tempo tempo = null;
    private int unitsPerMeasure;
    private int unitsPerNote;
	private Key scoreKey;
	private IRhythmScale rhythmScale;
	
	private List<ScaleFormula> scaleFormulas = new ArrayList<>();
	private List<Pitch> rootPitches = new ArrayList<>();
	private String scoreTitle = null;
	private Map<String, Instrument> instruments = new HashMap<>();
	
	public ScoreScaleCreator(String title) {
		scoreTitle = title;
		configure();
	}
	
	public ScoreScaleCreator(String title, List<ScaleFormula> scaleFormulas, List<Pitch> rootPitches) {
		this(title);
		this.scaleFormulas = scaleFormulas;
		this.rootPitches = rootPitches;
	}
	
	private void configure() {
		configuration = Configuration.getInstance(CONFIG_FILENAME);
		configuration.addConfiguration(Configuration.getInstance(ORCHESTRA_CONFIG_FILENAME));
		configProperties = configuration.getProperties();
	}
	
	public List<ScaleFormula> getScaleFormulas() {
		return scaleFormulas;
	}

	public void setScaleFormulas(List<ScaleFormula> scaleFormulas) {
		this.scaleFormulas = scaleFormulas;
	}

	public List<Pitch> getRootPitches() {
		return rootPitches;
	}

	public void setRootPitches(List<Pitch> rootPitches) {
		this.rootPitches = rootPitches;
	}


	@Override
	public Score apply(List<ScaleFormula> t, List<Pitch> u) {
		scaleFormulas.addAll(t);
		rootPitches.addAll(u);
		createScore();
		return score;
	}
	
	public Score createScore() {
		log.debug("createScore()");
		int nmeasures = -1;	// don't limit the number of measures
		String workNumber = dateFormat.format(new Date());
		configureInstrument();
		IScoreFactory scoreFactory = new ScoreFactory(configuration, instruments, nmeasures, scoreTitle, workNumber);
		score = scoreFactory.createScore(false);
		/*
		 * set the tempo to quarter=80 and key to C Major
		 */
		scorePartEntity = score.getScorePartEntityForInstrument(instrumentName);
		scorePart = score.getScoreParts().get(instrumentName);

		int tempoBPM = Integer.parseInt(configProperties.getProperty("score.scales.tempo", "80"));
		tempo = new Tempo(tempoBPM);
        scoreKey = new Key(configProperties.getProperty("score.key", "C-Major"));
        scorePartEntity.setScoreKey(scoreKey);
        /*
         * Create DisplayInfo. This is associated with Measure 1, staff 2
         */
        DisplayInfo displayInfo = new DisplayInfo();	// defaults are okay, corresponds to musicXML <print>
		/*
		 * Create Directions for the scale name and steps (Words) and the metronome setting
		 * Words are attached to staff 1 for on the first measure of each scale.
		 * Metronome setting appears once in Measure 1
		 */
        ScoreDirection wordsScoreDirection = null;				// created for the start of each scale on staff 1
        Words wordsDirectionType = null;
        Metronome metronomeDirectionType = new Metronome(tempoBPM);		// defaults to Tempo setting - add this only once to the first measure
        ScoreDirection metronomeScoreDirection = new ScoreDirection(1, metronomeDirectionType);
        metronomeScoreDirection.setPlacement("above");
        
        Barline lightLight = new Barline("right", "light-light");		// add after each scale
        Barline lightHeavy = new Barline("right", "light-heavy");		// add at last measure of score
        
		unitsPerMeasure = scorePart.getDivsPerMeasure();	// default is 480
		int notesPerMeasure = 8;
		unitsPerNote = unitsPerMeasure/notesPerMeasure; 	// units in eighth note (60)
		Duration duration = new Duration(unitsPerNote);
		/*
		 * Add the desired Scales to the Score - 2 octaves starting at C2, C4 (or root provided)
		 * For each measure the notes on staff 1 come first, followed by the same notes 
		 * 2 octaves lower on staff 2.
		 */

		int measureNumber = 1;
		int scaleCounter = 0;	// the number of scales added counter
		int measureCounter = 0;	// global counter of measures added
		for(Pitch pitch : rootPitches) {
			for(ScaleFormula formula : scaleFormulas) {
				String name = formula.getName();
				// some scales are descending, like the natural Minor, full name Melodic minor descending
				boolean descending = name.contains("Descending");
				for(String aname : formula.getAlternateNames()) {
					descending |= aname.contains("escending");
				}

				ScaleType scaleType = ScaleExportManager.getScaleType(formula);
				Scale scale = new Scale(name, null, scaleType, pitch, formula, Key.C_MAJOR);
				/*
				 * scalePitches are octave neutral (i.e. octave = -1) and includes a repeat of the root note.
				 * For example: C, D, Eb, F, Gb, A, C  formula=[2, 1, 2, 1, 3, 3] ("Pyramid Hexatonic")
				 */
				int scaleLength = formula.getSize();	// size is #notes in the scale (no repeats)
				List<Pitch> scalePitches = scale.getPitches(descending).subList(0, scaleLength);
				Pitch scaleRootPitch = scalePitches.get(0);
				int notesPerStaff = (scaleLength * 2) + 1;
				/*
				 * Create the 2-octave-length range of pitches to use in the Score
				 * Octave # is set for staff 1 and adjusted later for staff 2 
				 * to be 2 octaves lower.
				 */
				List<Pitch> scorePitches = new ArrayList<Pitch>();
				int octaveNumber = descending ? 6 : 4;
				for(int octnum = 0; octnum <=1; octnum++) {
					int pc = 0;
					for(Pitch p : scalePitches) {
						Pitch scorePitch = new Pitch(p);
						if(descending) {
							scorePitch.setOctave(octaveNumber);
							if(pc == 0) {
								octaveNumber--;
							}
							pc++;
						}
						else {
							if(pc < scaleLength) {
								scorePitch.setOctave(octaveNumber);
								pc++;
							}
							else {
								pc = 0;
								octaveNumber = octaveNumber + 1;
								scorePitch.setOctave(octaveNumber);
							}
						}
						scorePitches.add(scorePitch);
					}
					if(!descending) {
						octaveNumber++;
					}
				}
				scorePitches.add(new Pitch(scaleRootPitch.getStep(), octaveNumber));
				
				/*
				 * compute how many measures are needed, and any units left over
				 */
				int numberOfMeasuresNeeded = (int) Math.ceil(notesPerStaff / (double)notesPerMeasure);
				int unitsNeeded = notesPerStaff * unitsPerNote;
				int unitsAvailable = numberOfMeasuresNeeded * unitsPerMeasure;
				int remainingUnits = unitsAvailable - unitsNeeded;	// needed to pad the last measure of the scale
				
				String directionTypeText = scale.getName() + ": " + formula.toString();
				log.info("Working on " + directionTypeText);
				/*
				 * Staff 1 is the top staff (G-clef), staff 2 is the bottom staff (F-clef)
				 */
				List<Measure> scaleMeasures = new ArrayList<>();
				for(int i=0; i<numberOfMeasuresNeeded; i++) {
					Measure measure = createNewMeasure(measureNumber++);
					if(i == 0) {	// add the scale name & formula
						wordsDirectionType = new Words();
						wordsDirectionType.setText(directionTypeText );
						wordsScoreDirection = new ScoreDirection(1, wordsDirectionType);
						measure.addScoreDirection(wordsScoreDirection);
						measure.getDisplayInfo().add(displayInfo);
						if(measureCounter == 0) {
							measure.addScoreDirection(metronomeScoreDirection);
						}
						displayInfo.setNewSystem(true);
					}
					scaleMeasures.add(measure);
					measureCounter++;
				}

				Note note = null;
				Measure lastMeasure = null;
				int staffNumber = 1;
				int nnotes;
				Iterator<Pitch> pitchIt = scorePitches.iterator();
				for(Measure measure : scaleMeasures) {
					nnotes = 1;
					lastMeasure = measure;
					while(nnotes <= notesPerMeasure && pitchIt.hasNext()) {
						Pitch p = pitchIt.next();
						Pitch notePitch = new Pitch(p);
						note = new Note(notePitch, duration);
						note.setStaff(staffNumber);
						note.setNoteType("eighth");
						measure.accept(staffNumber, note);
						nnotes++;
					}
				}
				/*
				 * if there are units remaining for this staff measure need to
				 * pad the duration of the last note
				 */
				if(remainingUnits > 0) {
					int units = remainingUnits + unitsPerNote;
					List<Duration> factors =  rhythmScale.getFactors(units);
					int nFactors = factors.size();
					Note previousNote = note;
					Pitch p = note.getPitch();
					IRhythmScale rhythmScale = instruments.get(instrumentName).getRhythmScale();	// use to determine noteType
					String noteType = null;

					for(int i = 0; i<nFactors; i++) {
		    			Duration df = factors.get(i);
		    			if(i == 0) {
		    				note.setDuration(df);
		    				noteType = rhythmScale.getNoteType(note);
		    				note.setNoteType(noteType);
		    				previousNote = note;
		    			}
		    			else {
		    				Note newNote = new Note(p, df);
		    				noteType = rhythmScale.getNoteType(note);
		    				note.setNoteType(noteType);
		    				newNote.setTiedFrom(previousNote);
		    				previousNote.setTiedTo(note);
		    				lastMeasure.accept(staffNumber, newNote);
		    			}
					}

				}
				
				staffNumber++;
				for(Measure measure : scaleMeasures) {
					for(Measurable measurable : measure.getMeasureables(1)) {
						Note s1note = (Note)measurable;
						Note s2note = new Note(s1note);
						Pitch p = s1note.getPitch();
						Pitch notePitch = new Pitch(p.getStep(), p.getOctave() - 2, p.getAlteration());
						s2note.setPitch(notePitch);
						s2note.setStaff(staffNumber);
						measure.accept(staffNumber, s2note);
					}
				}

				// add an empty measure and a barline to indicate end of this scale
				scaleCounter++;
				scorePartEntity.getMeasures().addAll(scaleMeasures);
				lastMeasure = createNewMeasure(measureNumber++);
				Note s1note = new Note(Pitch.SILENT, new Duration(unitsPerMeasure));		// essentially a rest
				s1note.setNoteType("whole");
				Note s2note = new Note(s1note);
				s2note.setStaff(2);
				s2note.setNoteType("whole");
				lastMeasure.accept(1, s1note);
				lastMeasure.accept(2, s2note);
				lastMeasure.setBarline(scaleCounter == scaleFormulas.size() ? lightHeavy : lightLight);
				scorePartEntity.getMeasures().add(lastMeasure);
				measureCounter++;
				
			}	// scaleFormulas
				
		} // rootPitches

		return score;
	}
	
	/*
	 * Set score instrument to Piano on Grand Scale using the default rhythm scale.<br>
	 * This also sets the number of units per measure to 480.
	 */
    private void configureInstrument() {
    	String rhythmScaleName = RhythmScaleFactory.DEFAULT_RHYTHM_SCALE_NAME;
		IRhythmScaleFactory factory = RhythmScaleFactory.getRhythmScaleFactory(rhythmScaleName);
		rhythmScale = factory.createRhythmScale(rhythmScaleName);
		Instrument piano = new Piano();
		piano.setRhythmScale(rhythmScale);
		instruments.put(instrumentName, piano);
		
	}

	private Measure createNewMeasure(int measureNumber) {
    	Measure newMeasure = Measure.createInstance(scorePart);
    	newMeasure.setTempo(tempo);
		newMeasure.setDivisions(unitsPerMeasure);
		newMeasure.setNumber(measureNumber);
		newMeasure.setNumberOfStaves(2);
		newMeasure.setKey(scoreKey);
    	return newMeasure;
    }

	public String getScoreTitle() {
		return scoreTitle;
	}

	public void setScoreTitle(String scoreTitle) {
		this.scoreTitle = scoreTitle;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public Properties getConfigProperties() {
		return configProperties;
	}

	public Score getScore() {
		return score;
	}

	public Map<String, Instrument> getInstruments() {
		return instruments;
	}

}
