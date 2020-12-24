package org.dwbzen.util.music;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dwbzen.music.IScoreFactory;
import org.dwbzen.music.ScoreFactory;
import org.dwbzen.music.ScorePart;
import org.dwbzen.music.element.Alteration;
import org.dwbzen.music.element.Barline;
import org.dwbzen.music.element.Chord;
import org.dwbzen.music.element.Duration;
import org.dwbzen.music.element.IRhythmScale;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Measurable;
import org.dwbzen.music.element.Measure;
import org.dwbzen.music.element.Note;
import org.dwbzen.music.element.Phrase;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.PitchElement;
import org.dwbzen.music.element.Scale;
import org.dwbzen.music.element.ScaleFormula;
import org.dwbzen.music.element.ScaleType;
import org.dwbzen.music.element.Score;
import org.dwbzen.music.element.ScorePartEntity;
import org.dwbzen.music.element.Tempo;
import org.dwbzen.music.element.direction.Metronome;
import org.dwbzen.music.element.direction.ScoreDirection;
import org.dwbzen.music.element.direction.Words;
import org.dwbzen.music.element.song.ChordFormula;
import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.music.musicxml.DisplayInfo;
import org.dwbzen.util.Configuration;

/**
 * Creates a Score consisting of scales for the scale formulas and roots specified.<br>
 * The Score can then be Marshalled to MusicXML file using MusicXMLHelper.</p>
 * When scoring on the Grand Staff, each Measure has the<br>
 * notes for the top staff (staff=1) first, followed by the staff 2 notes.</p>
 * Currently this supports scoring for a Grand Scale instrument such as Piano or Harpsichord.<br>
 * A future enhancement will allow multiple valid, pitched instruments.</p>
 * A scale formula may appear multiple times under different names. For example [1, 1, 1, 2, 2, 1, 2, 2]
 * appears twice as "Phrygian Aeolian 2b 4b 6b" and "Phrygian Aeolian 3b1#"<br>
 * This is because of different spellings for the same formula. "Phrygian Aeolian 2b 4b 6b" is [C, Db, D, Eb, F, G, Ab, Bb, C]<br>
 * "Phrygian Aeolian 3b1#" is [C, C#, D, Eb, F, G, Ab, Bb, C]<br>
 * For more examples, see ScaleGroups.xlsx in the doc folder.<br>
 * By default duplicates do not appear in the created score. If this is not the desired behavior<br>
 * set unique = true when invoking the constructor or set "-unique true" when running ScaleExportManager.<p>
 * TODO add chord names. When a Chord is created, determine the intervals. For example given ["C4", "Eb4", "G4", "B4"]<br>
 * the formula is [3, 4, 4] intervals {"m3", "M3", "M3"}. Find the corresponding ChordFormula<br>
 * by formula or intervals. In this example the chord name is "Minor major seventh", the chord symbol is "Cm(M7)"<br>
 * The chord symbol can then be added to the score. It would be omitted if there is no corresponding chord formula.<br>
 * 
 * @author don_bacon
 *
 */
public class ScoreScaleCreator  {
	
	static final org.apache.log4j.Logger log = Logger.getLogger(ScoreScaleCreator.class);
	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss.SSSXXX");

	public static final String CONFIG_FILENAME = "/config.properties";
	public static final String ORCHESTRA_CONFIG_FILENAME="/orchestra.properties";
	public static final String defaultInstrumentName = "Piano";
	static ChordManager chordManager = new ChordManager();	// used to determine chord formula and symbol
	
	private Properties configProperties = null;
	private Configuration configuration = null;
    private Score score = null;
    private ScorePartEntity scorePartEntity = null;
    private ScorePart scorePart = null;
    private Tempo tempo = null;
    private int unitsPerMeasure;
    private int unitsPerNote;
    private int notesPerMeasure = 8;	// eighth notes, 4/4 time signature
    private int measureCounter = 0;	// global counter of measures added
	private Key scoreKey;
	private IRhythmScale rhythmScale = null;	// deteremined by the Intrument
	
	private List<ScaleFormula> scaleFormulas = new ArrayList<>();
	private List<Pitch> rootPitches = new ArrayList<>();
	private String scoreTitle = null;
	private Map<String, Instrument> instruments = null;
	private String instrumentName = null;
	private Instrument scoreInstrument = null;
	private boolean uniqueFormulas = true;		//  eliminates duplicate formulas by default
	private boolean createTriadChords = false;
	private boolean create7thChords = false;
	
	public ScoreScaleCreator(String title) {
		scoreTitle = title;
		instrumentName = defaultInstrumentName;
		configure();
	}
	
	public ScoreScaleCreator(String title, String name, boolean unique) {
		scoreTitle = title;
		instrumentName = name==null ? defaultInstrumentName : name;
		this.uniqueFormulas = unique;
		configure();
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


	public Score createScore(List<ScaleFormula> scaleFormulas, List<Pitch> rp) {
		addScaleFormulas(scaleFormulas);
		rootPitches.addAll(rp);
		createMusicXmlScore();
		return score;
	}
	
	private void addScaleFormulas(List<ScaleFormula> formulas) {
		if(uniqueFormulas) {
			scaleFormulas.addAll(getUniqueFormulas(formulas));
		}
		else {
			scaleFormulas.addAll(formulas);
		}
	}

	private List<ScaleFormula> getUniqueFormulas(List<ScaleFormula> formulas) {
		List<ScaleFormula> sflist = new ArrayList<>();
		List<String> formulaStrings = new ArrayList<>();
		for(ScaleFormula sf : formulas) {
			String sfstring = sf.getFormulaString();
			if(!formulaStrings.contains(sfstring)) {
				sflist.add(sf);
				formulaStrings.add(sfstring);
			}
		}
		return sflist;
	}
	
	private Score createMusicXmlScore() {
		
		log.debug("createScore()");
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
		 * set the tempo to quarter=80 and key to C Major
		 */
		scorePartEntity = score.getScorePartEntityForInstrument(instrumentName);
		scorePart = score.getScoreParts().get(instrumentName);

		int tempoBPM = Integer.parseInt(configProperties.getProperty("score.scales.tempo", "90"));
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
		unitsPerNote = unitsPerMeasure/notesPerMeasure; 	// units in eighth note (60)
		Duration duration = new Duration(unitsPerNote);
		/*
		 * Add the desired Scales to the Score - 2 octaves starting at C2, C4 (or root provided)
		 * For each measure the notes on staff 1 come first, followed by the same notes 
		 * 2 octaves lower on staff 2.
		 */

		int measureNumber = 1;
		int scaleCounter = 0;	// the number of scales added counter
		for(Pitch pitch : rootPitches) {
			for(ScaleFormula formula : scaleFormulas) {
				String name = formula.getName();
				// some scales are descending, like the natural Minor, full name Melodic minor descending
				boolean descending = name.contains("Descending");
				for(String aname : formula.getAlternateNames()) {
					descending |= aname.contains("escending");
				}

				ScaleType scaleType = ScaleExportManager.getScaleType(formula);
				Scale scale = new Scale(name, null, scaleType, pitch, formula, Key.C_MAJOR, Alteration.FLAT);
				/*
				 * scalePitches are octave neutral (i.e. octave = -1) and includes a repeat of the root note.
				 * For example: C, D, Eb, F, Gb, A, C  formula=[2, 1, 2, 1, 3, 3] ("Pyramid Hexatonic")
				 */
				int scaleLength = formula.getSize();	// size is #notes in the scale (no repeats)
				int notesPerStaff = (scaleLength * 2) + 1;
				/*
				 * Create the 2-octave-length range of pitches to use in the Score
				 * Octave # is set for staff 1 and adjusted later for staff 2 
				 * to be 2 octaves lower.
				 */
				List<Pitch> scorePitches = createScorePitches(scale, 2, descending);
				
				/*
				 * compute how many measures are needed, and any units left over
				 */
				int numberOfMeasuresNeeded = (int) Math.ceil(notesPerStaff / (double)notesPerMeasure);
				int unitsNeeded = notesPerStaff * unitsPerNote;
				int unitsAvailable = numberOfMeasuresNeeded * unitsPerMeasure;
				int remainingUnits = unitsAvailable - unitsNeeded;	// needed to pad the last measure of the scale
				
				String directionTypeText = scale.getName() + ": " + formula.getFormulaString();
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
					padMeasure_lastNote(note, staffNumber, lastMeasure, remainingUnits);
				}
				
				staffNumber++;
				/*
				 * Each note on the Bass staff is 2 octaves below the treble staff notes
				 */
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
				scaleCounter++;
				scorePartEntity.addMeasures(scaleMeasures);
				/*
				 * Create a Phrase and add the Retrograde
				 */
				Phrase phrase = new Phrase(scoreInstrument, scaleMeasures);
				Phrase retroPhrase = phrase.getRetrograde(true);
				scorePartEntity.addPhrase(retroPhrase);
				
				/*
				 * Add optional chords - triads & 7th
				 */
				if(isCreate7thChords() || isCreateTriadChords()) {
					Phrase chordsPhrase = createChordsPhrase(scale, descending, 4, measureNumber);
					if(chordsPhrase != null && chordsPhrase.size() > 0) {
						scorePartEntity.addPhrase(chordsPhrase);
					}
				}
				
				/* 
				 * add an empty measure and a barline to indicate end of this scale
				 */
				lastMeasure = createNewMeasure(measureNumber++);
				Note s1note = new Note(Pitch.SILENT, new Duration(unitsPerMeasure));		// essentially a rest
				Note s2note = new Note(s1note);
				s2note.setStaff(2);
				lastMeasure.accept(1, s1note);
				lastMeasure.accept(2, s2note);
				lastMeasure.setBarline(scaleCounter == scaleFormulas.size() ? lightHeavy : lightLight);
				scorePartEntity.addMeasure(lastMeasure);
				measureCounter++;
				
			}	// scaleFormulas
				
		} // rootPitches

		return score;
	}
	

	/**
	 * Create a Phrase consisting of triads and/or 7th chords constructed from a Scale.<br>
	 * Chord progression is ascending only.
	 * @param scale the Scale to use
	 * @param descending - set to true to create chords in descending order, from the top octave. This is determined by the Scale
	 * @param startingOctaveNumber
	 * @param measureNumber - starting measure number
	 * @return Phrase a Phrase of Chords spanning 2 octaves
	 */
	public Phrase createChordsPhrase(Scale scale, boolean descending, int startingOctaveNumber, int measureNumber) {
		List<Pitch> scalePitches = createScorePitches(scale, 3, descending);	// 3 octaves needed to cover 2 octaves of chords
		Phrase chordsPhrase = new Phrase(scoreInstrument);
		int scaleLength = scale.size();
		/*
		 * This method is used stand-alone so need to recompute measures & units
		 */
		int notesPerStaff = 2*(scaleLength-1) + 1;
		int numberOfMeasuresNeeded = (int) Math.ceil(notesPerStaff / (double)notesPerMeasure);
		int unitsNeeded = notesPerStaff * unitsPerNote;
		int unitsAvailable = numberOfMeasuresNeeded * unitsPerMeasure;
		int remainingUnits = unitsAvailable - unitsNeeded;	// needed to pad the last chord in the last measure of the Phrase
		/*
		 * First create the Pitches for the treble clef
		 */
		int octave = startingOctaveNumber;
		PitchCollection pitchCollection = new PitchCollection();
		StringBuilder directionTypeText = new StringBuilder("Chords:");
		/*
		 * TODO FIX - 7th and triads work independently but not when both are specified.
		 */
		if(isCreateTriadChords()) {
			pitchCollection.add(Scale.createScaleTriads(scalePitches, octave, scaleLength));
			pitchCollection.add(Scale.createScaleTriads(scalePitches, octave+1, scaleLength));
			PitchElement pe = PitchElement.clone(pitchCollection.getPitch(0));
			pe.setOctave(octave + 2);
			pitchCollection.addPitchElement(pe);
			directionTypeText.append("triads ");
		}
		if(isCreate7thChords()) {
			pitchCollection.add(Scale.createScaleSevenths(scalePitches, octave, scaleLength));
			pitchCollection.add(Scale.createScaleSevenths(scalePitches, octave+1, scaleLength));
			PitchElement pe = PitchElement.clone(pitchCollection.getPitch(0));
			pe.setOctave(octave + 2);
			pitchCollection.addPitchElement(pe);
			directionTypeText.append("7th ");
		}
		/*
		 * Create empty measures to accommodate the PitchElements
		 * Add system text for chords
		 */
		List<Measure> scaleMeasures = new ArrayList<>();
		DisplayInfo displayInfo = new DisplayInfo();
        ScoreDirection wordsScoreDirection = null;
        Words wordsDirectionType = null;
		for(int i=0; i<numberOfMeasuresNeeded; i++) {
			Measure measure = createNewMeasure(measureNumber++);
			if(i == 0) {	// add the "Chords" string
				wordsDirectionType = new Words();
				wordsDirectionType.setText(directionTypeText.toString() );
				wordsScoreDirection = new ScoreDirection(1, wordsDirectionType);
				measure.addScoreDirection(wordsScoreDirection);
				measure.getDisplayInfo().add(displayInfo);
				displayInfo.setNewSystem(true);
			}
			scaleMeasures.add(measure);
			measureCounter++;
		}
		/*
		 * Create Chords from the PitchElements and assign a Duration of unitsPerNote
		 * Add chords to the empty measures
		 */
		Measure lastMeasure = null;
		Chord chord = null;
		int staffNumber = 1;
		int index = 0;		// indexes pitchCollection PitchElements
		String chordSymbol = null;
		for(Measure measure : scaleMeasures) {
			int nChords = 0;
			while(nChords++ < notesPerMeasure && index < pitchCollection.size()) {
				PitchElement pe = pitchCollection.getPitch(index);
				chord = Chord.createChord(pe, unitsPerNote);
				chord.setNoteType("eighth");
				chord.setStaff(staffNumber);
				chordManager.addChordFormulaToChord(chord);		// finds and adds the ChordFormula if there is one
				if(chord.getChordFormula() != null) {
					chordSymbol = chord.toString(true);
					// add the symbol as a system direction below staff 1 (instead of a harmony element)
					addScoreDirection(1, measure, chordSymbol, "below", false);
				}
				measure.accept(staffNumber, chord);
				index++;
			}
			lastMeasure = measure;
		}
		/*
		 * if there are units remaining for this staff measure need to
		 * pad the Measure with rest notes
		 */
		if(remainingUnits > 0) {
			List<Measurable> restNotes = createNotes(remainingUnits);
			lastMeasure.addMeasurables(restNotes);
			// TODO: padMeasure_lastChord(chord, staffNumber, lastMeasure, remainingUnits); instead of rests
		}
		
		staffNumber++;
		Duration duration = new Duration(unitsPerNote);
		/*
		 * Each note on the Bass staff is 2 octaves below the treble staff notes
		 */
		for(Measure measure : scaleMeasures) {
			for(Measurable measurable : measure.getMeasureables(1)) {	// get Measurables on staff 1
				if(measurable.getType().equals(Measurable.CHORD)) {
					Chord s1chord = (Chord)measurable;
					Chord s2chord = Chord.createChord(s1chord, duration);
					s2chord.octaveTranspose(-2);
					s2chord.setStaff(staffNumber);
					measure.accept(staffNumber, s2chord);
				}
				else {
					Note n1note = (Note)measurable;
					Note n2note = new Note(n1note);
					n2note.setStaff(staffNumber);
					measure.accept(staffNumber, n2note);
				}

			}
			chordsPhrase.addMeasure(measure);
		}
		/*
		 * TODO create Retrograde phrase: 
		 * Phrase retroPhrase = phrase.getRetrograde(true);
		 * chordPhrase.
		 */
		return chordsPhrase;
	}
	
	public void addScoreDirection(int staffNumber, Measure measure, String directionTypeText, String placement, boolean newSystem) {
		DisplayInfo displayInfo = new DisplayInfo();
        ScoreDirection wordsScoreDirection = null;
        
		Words wordsDirectionType = new Words();
		wordsDirectionType.setText(directionTypeText.toString() );
		wordsScoreDirection = new ScoreDirection(staffNumber, wordsDirectionType);
		if(placement != null) {			
			wordsScoreDirection.setPlacement(placement);	// "above" or "below" or null
		}
		measure.addScoreDirection(wordsScoreDirection);
		measure.getDisplayInfo().add(displayInfo);
		displayInfo.setNewSystem(newSystem);
		return;
	}

	/**
	 * 
	 * @param scale -  the source Scale
	 * @param nOctaves - the number of octaves to span
	 * @param descending - set to true to create pitches in descending order, from the top octave.
	 * @return List<Pitch>
	 */
	public List<Pitch> createScorePitches(Scale scale, int nOctaves, boolean descending) {
		int scaleLength = scale.size() - 1;
		List<Pitch> scalePitches = scale.getPitches(descending).subList(0, scaleLength);
		Pitch scaleRootPitch = scalePitches.get(0);
		
		List<Pitch> scorePitches = new ArrayList<Pitch>();
		int octaveNumber = descending ? 6 : 4;
		for(int octnum = 0; octnum <nOctaves; octnum++) {
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
		return scorePitches;
	}

	/**
	 * Set score instrument to Piano (default) or specified instrument on Grand Scale using the<br>
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
		if(scoreInstrument == null || scoreInstrument.getCleffs().size() != 2) {
			throw new RuntimeException(instrumentName + " is invalid - must be Grand Staff");
		}
		rhythmScale = scoreInstrument.getRhythmScale();
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
	
	private void padMeasure_lastNote(Note note, int staffNumber, Measure lastMeasure, int remainingUnits) {
		int units = remainingUnits + unitsPerNote;
		List<Duration> factors =  rhythmScale.getFactors(units);		// use to determine noteType
		int nFactors = factors.size();
		Note previousNote = note;
		Pitch pitch =  note.getPitch();
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
				Note newNote = new Note(pitch, df);
				noteType = rhythmScale.getNoteType(note);
				note.setNoteType(noteType);
				newNote.setTiedFrom(previousNote);
				previousNote.setTiedTo(note);
				lastMeasure.accept(staffNumber, newNote);
			}
		}
	}
	
	private void padMeasure_lastChord(Chord chord, int staffNumber, Measure lastMeasure, int remainingUnits) {
		// TODO finish me
	}

	/**
	 * 
	 */
	public List<Measurable> createNotes(int remainingUnits) {
		int units = remainingUnits;
		List<Duration> factors =  rhythmScale.getFactors(units);		// use to determine noteType
		int nFactors = factors.size();
		Note previousNote = null;
		Note note = null;
		String noteType = null;
		List<Measurable> notesToAdd = new ArrayList<>();
		for(int i = 0; i<nFactors; i++) {
			Duration dur = factors.get(i);
			if(i == 0) {
				note = new Note(Pitch.SILENT, dur);
				note.setDuration(dur);
				noteType = rhythmScale.getNoteType(note);
				note.setNoteType(noteType);
				previousNote = note;
				notesToAdd.add(note);
			}
			else {
				Note newNote = new Note(Pitch.SILENT, dur);
				noteType = rhythmScale.getNoteType(note);
				note.setNoteType(noteType);
				newNote.setTiedFrom(previousNote);
				previousNote.setTiedTo(note);
				notesToAdd.add(newNote);
			}
		}
		return notesToAdd;
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

	public String getInstrumentName() {
		return instrumentName;
	}

	public void setInstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}

	public Map<String, Instrument> getInstruments() {
		return instruments;
	}

	public boolean isUniqueFormulas() {
		return uniqueFormulas;
	}

	public void setUniqueFormulas(boolean uniqueFormulas) {
		this.uniqueFormulas = uniqueFormulas;
	}

	public ScorePart getScorePart() {
		return scorePart;
	}

	public Instrument getScoreInstrument() {
		return scoreInstrument;
	}

	public boolean isCreateTriadChords() {
		return createTriadChords;
	}

	public void setCreateTriadChords(boolean createTriadChords) {
		this.createTriadChords = createTriadChords;
	}

	public boolean isCreate7thChords() {
		return create7thChords;
	}

	public void setCreate7thChords(boolean create7thChords) {
		this.create7thChords = create7thChords;
	}

}
