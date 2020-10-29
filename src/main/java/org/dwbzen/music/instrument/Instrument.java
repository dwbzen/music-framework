package org.dwbzen.music.instrument;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.dwbzen.common.math.Point2D;
import org.dwbzen.music.action.DurationScaler;
import org.dwbzen.music.action.PitchScaler;
import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.Duration;
import org.dwbzen.music.element.IRhythmScale;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.PitchClass;
import org.dwbzen.music.element.PitchRange;
import org.dwbzen.util.Configurable;
import org.dwbzen.util.Configuration;

/**
 * Base class for all Instruments.
 * Note that music notation software typically has an option to
 * display in concert pitch (as it sounds) or transposed pitches (as written).
 * For example Sibelius as an option to switch to transposing pitch.
 * This shows the notes for the performer to read. A clarinet would
 * be automatically transposed up a whole tone and the key adjusted accordingly.
 * If importing into music notation software that does this, set the
 * transposes flag to false (even if it really is a transposing instrument).
 * However, make sure getTranspositionSteps() returns the correct value
 * so the flag can be adjusted as needed. For a clarinet, this would be = 2
 * which means the written notes transposes UP a whole step.
 * 
 * Added RhythmScale association. Every instrument must have one!
 * This is set up in the configuration.
 * <p>
 * See <a href="https://en.wikipedia.org/wiki/General_MIDI">General MIDI</a> on Wikipedia for Midi programs<br>
 * and <a href="https://en.wikipedia.org/wiki/Scientific_pitch_notation">Scientific Pitch Notation</a>
 * 
 * <p>MuseScore 3 instrument properties are in resources/data/music/instruments.xml</p>
 * 
 * @author don_bacon
 *
 */
public abstract class Instrument implements Configurable, IInstrument {

	private static final long serialVersionUID = 3307819732691895227L;
	public static enum Range {
		LOW(0), HIGH(1);
		Range(int val) { this.value = val;}
		private final int value;
	    public int value() { return value; }
	};
	
	protected PitchRange pitchRange = null;
	
	/**
	 * There is a practical limit to how long a note can last for a given instrument.
	 * Which is also dependent on the tempo (beats per minute) and dynamics (loud, soft).
	 * For example you wouldn't score a flute part for 8 full counts in largo tempo,
	 * but that would be okay in presto.
	 * The range is expressed in seconds and at the moment is not used in music generation
	 * as it has been replaced with RhythmScale discreet note durations. But some day...
	 * 
	 * @see https://www.midi.org/specifications/item/gm-level-1-sound-set
	 * 
	 * 
	 */
	protected double[] durationRangeSeconds = {0.166, 2.66};	// raw, practical limit
	
	/**
	 * The key for this instrument - C, Bb, Eb etc.
	 * Establishes the transposition if any
	 * Non-transposing instrument key is (Step.C, 4, 0) - octave 4 is by convention.
	 * Example, Bb transposes up 1 whole step, so key would be (Step.B, 4, -1)
	 * Example, an English horn is in F and notated a perfect fifth above the sounding pitch. Key is (Step.F, 3, 0)
	 */
	protected Key key = null;
	protected String name;
	protected String abreviation;			//<part-abbreviation>Pno.</part-abbreviation>
	protected String abbreviationDisplay;
	protected String partName;				// <part-name>Piano</part-name>
	protected String partNameDisplay;
	protected int numberOfStaves = 1;		// how many staves for this instrument - Piano and Harpsichord requires 2, a PipeOrgan and Pedal Harpsichord needs 3
	/**
	 * MusicXML specific especially for Sibelius
	 * <instrument-name> configured and may be different than name
	 * for example,  <score-instrument id="P7-I1">
     *					<instrument-name>Cowbell [2 lines]</instrument-name>
     * Configured as music.instrument.percussion.<name>.instrument-name
	 */
	protected String instrumentName;
	/**
	 * Number of notes in the maximal range for this instrument.
	 */
	protected int length = 0;
	protected PitchScaler pitchScaler = null;
	protected DurationScaler durationScaler = null;
	protected MidiInstrument midiInstrument = null;	// associated MidiInstrument if any
	protected int midiProgram = 0;
	protected boolean transposes = false;
	protected int transposeDiatonicSteps = 0;
	protected int transposeChromaticSteps = 0;
	protected int transposeOctaveChange = 0;
	protected IRhythmScale rhythmScale = null;
	/**
	 * Unpitched instruments such as drums, cymbals, wood blocks etc. should override this to UNPITCHED_5LINE
	 * Instruments like cowbells use UNPITCHED_2LINE
	 */
	protected PitchClass pitchClass = PitchClass.PITCHED;
	
	private String instrumentSound = null;	// <instrument-sound>
	private String virtualLibrary = null;	// <virtual-library>
	private String virtualName = null;		// <virtual-name>
	private Configuration configuration = null;
	
	private static String instrumentNameKey;
	private static String instrumentSoundKey;
	private static String virtualLibraryKey;
	private static String virtualNameKey;
	private static String midiProgramKey;
	private static String partNameKey;
	private static String partNameDisplayKey;
	private static String partAbbreviationKey;
	private static String partAbbreviationDisplayKey;
	private static String instrumentTransposeChromaticStepsKey;
	private static String instrumentTransposeDiatonicStepsKey;
	private static String instrumentTransposeOctaveChangeKey;

	/**
	 * Default clef(s) for this instrument
	 */
	protected List<Cleff> cleffs = new ArrayList<Cleff>();
	/**
	 * All the pitches that are in (playing) range for this instrument, untransposed
	 */
	protected List<Pitch> notes = null;

	public Instrument() {
	}
	
	public Instrument(Pitch low, Pitch high) {
		pitchRange = new PitchRange(low, high);
		initialize();
	}

	public Instrument(PitchRange pr) {
		this.pitchRange = pr;
		initialize();
	}

	
	/**
	 * Configure the instrument from Configuration instance
	 * 
	 */
	@Override
	public void configure() {
		if(configuration == null) {
			throw new IllegalArgumentException("Null Configuration is not allowed");
		}
		
		Properties props = configuration.getProperties();
		if(props.containsKey(instrumentNameKey)) {
			setInstrumentName(props.getProperty(instrumentNameKey));
		}
		if(props.containsKey(instrumentSoundKey)) {
			setInstrumentSound(props.getProperty(instrumentSoundKey));
		}
		if(props.containsKey(virtualLibraryKey)) {
			setVirtualLibrary(props.getProperty(virtualLibraryKey));
		}
		if(props.containsKey(virtualNameKey)) {
			setVirtualName(props.getProperty(virtualNameKey));
		}
		if(props.containsKey(partNameKey)) {
			setPartName(props.getProperty(partNameKey)); // TODO add to orchestra.properties for this instrument if needed
		}
		if(props.containsKey(partNameDisplayKey)) {
			setPartNameDisplay(props.getProperty(partNameDisplayKey)); // TODO add to orchestra.properties for this instrument if needed
		}
		if(props.containsKey(partAbbreviationKey)) {
			setAbreviation(props.getProperty(partAbbreviationKey)); // TODO add to orchestra.properties for this instrument if needed
		}
		if(props.containsKey(partAbbreviationDisplayKey)) {
			setAbbreviationDisplay(props.getProperty(partAbbreviationDisplayKey)); // TODO add to orchestra.properties for this instrument if needed
		}
		if(props.containsKey(midiProgramKey)) {
			int midiProgram = Integer.parseInt(props.getProperty(midiProgramKey));
			setMidiProgram(midiProgram);
			midiInstrument = new MidiInstrument("", 1, getName());
			midiInstrument.setMidiProgram(midiProgram);
		}
		if(props.containsKey(instrumentTransposeChromaticStepsKey)) {
			setTransposeChromaticSteps(Integer.parseInt(props.getProperty(instrumentTransposeChromaticStepsKey)));
			setTransposeDiatonicSteps(Integer.parseInt(props.getProperty(instrumentTransposeDiatonicStepsKey)));
		}
		if(props.containsKey(instrumentTransposeOctaveChangeKey)) {
			setTransposeOctaveChange(Integer.parseInt(props.getProperty(instrumentTransposeOctaveChangeKey)));
		}
	}
	
	/**
	 * Initializes pitches from pitch range set in the constructor
	 * Configures optional config parameters: instrument-name, instrument-sound, virtual-name, virtual-library
	 * Defaults for abreviation, abreviationDisplay and partNameDisplay are all set
	 * and will be overridden if these properties are in the instrument configuration.
	 */
	protected void initialize() {
		setupConfigKeys();
		establishKey();
		if(partName == null) {
			setPartName(getName());
		}
		createPitchScaler();
		createDurationScaler();
		PitchRange pr = getPitchRange();
		length = pr.getStepRange();
		generateNotes();
		if(abreviation == null) {
			setAbreviation(getName());
		}
		if(abbreviationDisplay == null) {
			setAbbreviationDisplay(abreviation);
		}
		if(partNameDisplay == null) {
			setPartNameDisplay(partName);
		}
	}

	protected void setupConfigKeys() {
		/*
		 * Set up config keys
		 */
		String keyPrefix = "music.instrument." + getClass().getSimpleName();
		instrumentNameKey = keyPrefix + ".instrument-name";
		instrumentSoundKey = keyPrefix + ".instrument-sound";
		virtualLibraryKey = keyPrefix + ".virtual-library";
		virtualNameKey = keyPrefix + ".virtual-name";
		midiProgramKey = keyPrefix + ".midiProgram";
		partNameKey = keyPrefix + ".part-name";
		partNameDisplayKey = keyPrefix + ".part-name-display";
		partAbbreviationKey = keyPrefix + ".part-abbreviation";
		partAbbreviationDisplayKey = keyPrefix + ".part-abbreviation-display";
		instrumentTransposeChromaticStepsKey = keyPrefix + ".transpose.chromatic";
		instrumentTransposeDiatonicStepsKey = keyPrefix + ".transpose.diatonic";
		instrumentTransposeOctaveChangeKey = keyPrefix  + ".transpose.octaveChange";
	}
	
	/**
	 * Default PitchScaler.
	 */
	protected void createPitchScaler() {
		setPitchScaler(new PitchScaler(this));
	}
	protected void createDurationScaler() {
		DurationScaler ds = new DurationScaler(this);
		// these values are default and will be overwritten with PointSetStats maxY and minY
		// if creating a score from a fractal file
		ds.setMinVal(durationRangeSeconds[0]);
		ds.setMaxVal(durationRangeSeconds[1]);
		setDurationScaler(ds);
		ds.setRhythmScale(rhythmScale);
	}

	/**
	 * Generate all the notes for this instrument
	 * Default implementation starts at the low pitch of the range
	 * and adds notes for the entire range.
	 * Steps numbered starting at 1. So the List index is step#-1
	 * NOTE that notes applies to pitched instruments only, even though it is generated for all.
	 */
	public void generateNotes() {
		notes = new ArrayList<Pitch>();
		Pitch sp = new Pitch(getPitchRange().getLow());
		notes.add(sp); // index 0
		for(int i = 1; i<=length; i++) {
			sp = sp.upOneStep();
			notes.add(sp);
		}
		return;
	}


	/**
	 * Translates an absolute step number to a Pitch for this instrument.
	 * @param int step  1 <= step <= stepRange
	 * @return Pitch
	 * @throws IllegalArgumentException if step is out of range
	 */
	public Pitch stepNumberToPitch(int step) {
		int stepRange = getPitchRange().getStepRange();
		if(step <= 0 || step > stepRange) {
			throw new IllegalArgumentException("Step out of range for this instrument " + step);
		}
		return getNotes().get(step -1);
	}
	
	public Pitch scale(Point2D<Double> dval) {
		return getPitchScaler().scale(dval.getX());
	}
	
	public Pitch scale(double dval) {
		return getPitchScaler().scale(Double.valueOf(dval));
	}

	public Duration scaleDuration(Point2D<Double> dval) {
		return getDurationScaler().scale(dval.getY());
	}
	public Duration scaleDuration(double dval) {
		return getDurationScaler().scale(Double.valueOf(dval));
	}
	public PitchRange getPitchRange() {
		return pitchRange;
	}

	public void setPitchRange(PitchRange range) {
		if(range != null) {
			this.pitchRange = range;
			length = range.getStepRange() + 1;
			generateNotes();
		}
	}

	public void setName(String aName) {
		name = aName;
	}
	public String getName() {
		return name;
	}
	
	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}
	
	public Key getKey() {
		return this.key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public List<Cleff> getCleffs() {
		return cleffs;
	}

	public PitchScaler getPitchScaler() {
		return pitchScaler;
	}

	public void setPitchScaler(PitchScaler pitchScaler) {
		this.pitchScaler = pitchScaler;
	}

	public int getLength() {
		return length;
	}

	public List<Pitch> getNotes() {
		return notes;
	}

	public String getAbreviation() {
		return abreviation;
	}

	public void setAbreviation(String abreviation) {
		this.abreviation = abreviation;
	}

	public double[] getDurationRangeSeconds() {
		return durationRangeSeconds;
	}

	public void setDurationRangeSeconds(double[] durationRangeSeconds) {
		this.durationRangeSeconds = durationRangeSeconds;
	}

	public DurationScaler getDurationScaler() {
		return durationScaler;
	}

	public void setDurationScaler(DurationScaler durationScaler) {
		this.durationScaler = durationScaler;
	}

	public void setNotes(List<Pitch> notes) {
		this.notes = notes;
	}

	public int getMidiProgram() {
		return midiProgram;
	}

	public void setMidiProgram(int midiprogram) {
		this.midiProgram = midiprogram;
	}

	public MidiInstrument getMidiInstrument() {
		return midiInstrument;
	}

	public void setMidiInstrument(MidiInstrument midiInstrument) {
		this.midiInstrument = midiInstrument;
	}

	public boolean isTransposes() {
		return transposes;
	}

	public void setTransposes(boolean transposes) {
		this.transposes = transposes;
	}

	public String getInstrumentName() {
		return instrumentName;
	}

	public void setInstrumentName(String instrumentName) {
		this.instrumentName = instrumentName;
	}

	public String getInstrumentSound() {
		return instrumentSound;
	}

	public void setInstrumentSound(String instrumentSound) {
		this.instrumentSound = instrumentSound;
	}

	public String getVirtualName() {
		return virtualName;
	}

	public void setVirtualName(String virtualName) {
		this.virtualName = virtualName;
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public String getVirtualLibrary() {
		return virtualLibrary;
	}

	public void setVirtualLibrary(String virtualLibrary) {
		this.virtualLibrary = virtualLibrary;
	}

	public String getAbbreviationDisplay() {
		return abbreviationDisplay;
	}

	public void setAbbreviationDisplay(String abbreviationDisplay) {
		this.abbreviationDisplay = abbreviationDisplay;
	}

	public String getPartNameDisplay() {
		return partNameDisplay;
	}

	public void setPartNameDisplay(String partNameDisplay) {
		this.partNameDisplay = partNameDisplay;
	}

	@Override
	public IRhythmScale getRhythmScale() {
		return rhythmScale;
	}
	
	@Override
	/**
	 * Sets the RhythmScale for this Instrument and also the DurationScaler.
	 */
	public void setRhythmScale(IRhythmScale rs) {
		rhythmScale = rs;
		if(durationScaler != null) {
			durationScaler.setRhythmScale(rs);
		}
	}

	public PitchClass getPitchClass() {
		return pitchClass;
	}

	public void setPitchClass(PitchClass pitchClass) {
		this.pitchClass = pitchClass;
	}

	public int getTransposeDiatonicSteps() {
		return transposeDiatonicSteps;
	}

	public void setTransposeDiatonicSteps(int transposeDiatonicSteps) {
		this.transposeDiatonicSteps = transposeDiatonicSteps;
	}

	public int getTransposeChromaticSteps() {
		return transposeChromaticSteps;
	}

	public void setTransposeChromaticSteps(int transposeChromaticSteps) {
		this.transposeChromaticSteps = transposeChromaticSteps;
	}

	public int getTransposeOctaveChange() {
		return transposeOctaveChange;
	}

	public void setTransposeOctaveChange(int transposeOctaveChange) {
		this.transposeOctaveChange = transposeOctaveChange;
	}

	public int getNumberOfStaves() {
		return numberOfStaves;
	}

	public void setNumberOfStaves(int numberOfStaves) {
		this.numberOfStaves = numberOfStaves;
	}
	
}
