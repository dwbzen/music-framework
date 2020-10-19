package org.dwbzen.util.music;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import org.dwbzen.music.element.Measure;
import org.dwbzen.music.element.Note;
import org.dwbzen.music.element.NoteType;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Scale;
import org.dwbzen.music.element.ScaleFormula;
import org.dwbzen.music.element.ScaleType;
import org.dwbzen.music.element.Scales;
import org.dwbzen.music.element.Score;
import org.dwbzen.music.element.ScorePartEntity;
import org.dwbzen.music.element.Tempo;
import org.dwbzen.music.element.direction.DirectionType;
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
 * When scoring on the so-called Grand Staff for piano, each Measure has the<br>
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

		int tempoBPM = Integer.parseInt(configProperties.getProperty("score.tempo", "80"));
		tempo = new Tempo(tempoBPM);
        Key scoreKey = new Key(configProperties.getProperty("score.key", "C-Major"));
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
        Metronome metronomeDirectionType = new Metronome();		// defaults to Tempo setting - add this only once to the first measure
        ScoreDirection metronomeScoreDirection = new ScoreDirection(1, metronomeDirectionType);
        metronomeScoreDirection.setPlacement("above");
        
        Barline lightLight = new Barline("right", "light-light");		// add after each scale
        Barline lightHeavy = new Barline("right", "light-heavy");		// add at last measure of score
        
		int[] staff = {1,2};	// 1=top staff, 2=bottom staff
		unitsPerMeasure = scorePart.getDivsPerMeasure();		// default is 480
		unitsPerNote = unitsPerMeasure/8;						// units in eighth note (60)
		Duration duration = new Duration(unitsPerNote);
		/*
		 * Add the desired Scales to the Score - 2 octaves starting at C2, C4 (or root provided)
		 */

		int measureNumber = 1;
		int scaleCounter = 0;	// the number of scales added counter
		for(Pitch pitch : rootPitches) {
			for(ScaleFormula formula : scaleFormulas) {
				Measure measure = null;
				String name = formula.getName();
				ScaleType scaleType = ScaleExportManager.getScaleType(formula);
				Scale scale = new Scale(name, null, scaleType, pitch, formula, Key.C_MAJOR);
				/*
				 * scalePitches are octave neutral (i.e. octave = -1) and includes a repeat of the root note.
				 * For example: C, D, Eb, F, Gb, A, C  formula=[2, 1, 2, 1, 3, 3] ("Pyramid Hexatonic")
				 */
				List<Pitch> scalePitches = scale.getPitches();
				int scaleLength = scalePitches.size();
				/*
				 * Staff 1 is the top staff (G-clef), staff 2 is the bottom staff (F-clef)
				 */
				for(int staffNumber: staff) {
					int unitsThisMeasure = 0;
					measure = createNewMeasure(measureNumber);
					if(staffNumber == 1) {
						wordsDirectionType = new Words();
						wordsDirectionType.setText(scale.getName() + ": " + formula.toString());
						wordsScoreDirection = new ScoreDirection(1, wordsDirectionType);
						if(scaleCounter == 0) {
							// Add metronome and words to first measure on staff 1
							measure.addScoreDirection(metronomeScoreDirection);
							measure.getDisplayInfo().add(displayInfo);
							measure.addScoreDirection(wordsScoreDirection);
							displayInfo.setNewSystem(true);
						}
						else {
							measure.getDisplayInfo().add(displayInfo);	// adds the system break
						}
					}

					/*
					 * Add 2 octaves of scale notes on this staff 
					 */
					Note note = null;
					for(int noctave = 0; noctave <=1; noctave++) {
						int nnotes = 1;
						for(Pitch p : scalePitches) {
							// skip the last note in the first octave
							if(nnotes >= scaleLength && noctave == 0) {
								break;
							}
							// need to add the octave
							Pitch notePitch = new Pitch(p);
							notePitch.setOctave(staffNumber == 1 ? 4 + noctave : 2 + noctave);
							note = new Note(notePitch, duration);
							note.setStaff(staffNumber);
							if(unitsThisMeasure < unitsPerMeasure) {
								measure.accept(staffNumber, note);
								unitsThisMeasure = unitsThisMeasure + unitsPerNote;
							}
							else {
								scorePartEntity.getMeasures().add(measure);
								measure = createNewMeasure(++measureNumber);
								measure.accept(staffNumber, note);
								unitsThisMeasure = unitsPerNote;
							}
							nnotes++;
						}
					}
					/*
					 * if there are units remaining for this staff measure need to
					 * pad the duration of the last note
					 */
					if(unitsThisMeasure < unitsPerMeasure) {
						int unitsRemaining = unitsPerMeasure - unitsThisMeasure;
						Duration d = new Duration(unitsRemaining + unitsPerNote);
						note.setDuration(d);
						measure.accept(staffNumber, note);
						scorePartEntity.getMeasures().add(measure);
					}
					else {
						scorePartEntity.getMeasures().add(measure);
					}
				}
				// add a barline to indicate end of this scale
				scaleCounter++;
				measure.setBarline(scaleCounter == scaleFormulas.size() ? lightLight : lightHeavy);
			}
		}

		return score;
	}
	
	/*
	 * Set score instrument to Piano on Grand Scale using the default rhythm scale.<br>
	 * This also sets the number of units per measure to 480.
	 */
    private void configureInstrument() {
    	String rhythmScaleName = RhythmScaleFactory.DEFAULT_RHYTHM_SCALE_NAME;
		IRhythmScaleFactory factory = RhythmScaleFactory.getRhythmScaleFactory(rhythmScaleName);
		IRhythmScale allRhythmScale = factory.createRhythmScale(rhythmScaleName);
		Instrument piano = new Piano();
		piano.setRhythmScale(allRhythmScale);
		instruments.put(instrumentName, piano);
		
	}

	private Measure createNewMeasure(int measureNumber) {
    	Measure newMeasure = Measure.createInstance(scorePart);
    	newMeasure.setTempo(tempo);
		newMeasure.setDivisions(unitsPerMeasure);
		newMeasure.setNumber(measureNumber);
		newMeasure.setNumberOfStaves(2);
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
