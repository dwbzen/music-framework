package music.instrument;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import util.Configurable;
import util.Configuration;
import math.Point2D;
import music.action.DurationScaler;
import music.action.PitchScaler;
import music.element.Cleff;
import music.element.Duration;
import music.element.IRhythmScale;
import music.element.Interval;
import music.element.Key;
import music.element.Pitch;
import music.element.PitchClass;
import music.element.PitchRange;

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
 * 
 * See https://en.wikipedia.org/wiki/General_MIDI  for Midi programs
 * See https://en.wikipedia.org/wiki/Scientific_pitch_notation
 * @author dbacon
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
	protected double[] durationRangeSeconds = IInstrument.durationRangeSeconds;	// raw, practical limit
	
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
	protected Interval transposeInterval = null;
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
			setPartName(props.getProperty(partNameKey));
		}
		if(props.containsKey(partNameDisplayKey)) {
			setPartNameDisplay(props.getProperty(partNameDisplayKey));
		}
		if(props.containsKey(partAbbreviationKey)) {
			setAbreviation(props.getProperty(partAbbreviationKey));
		}
		if(props.containsKey(partAbbreviationDisplayKey)) {
			setAbbreviationDisplay(props.getProperty(partAbbreviationDisplayKey));
		}
		if(props.containsKey(midiProgramKey)) {
			int midiProgram = Integer.parseInt(props.getProperty(midiProgramKey));
			setMidiProgram(midiProgram);
			midiInstrument = new MidiInstrument("", 1, getName());
			midiInstrument.setMidiProgram(midiProgram);
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
			setPartName(getName()); // a sensible default
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

	private void setupConfigKeys() {
		/*
		 * Set up config keys
		 */
		String classname = getClass().getName();
		instrumentNameKey = classname + ".instrument-name";
		instrumentSoundKey = classname + ".instrument-sound";
		virtualLibraryKey = classname + ".virtual-library";
		virtualNameKey = classname + ".virtual-name";
		midiProgramKey = classname + ".midiProgram";
		partNameKey = classname + ".part-name";
		partNameDisplayKey = classname + ".part-name-display";
		partAbbreviationKey = classname + ".part-abbreviation";
		partAbbreviationDisplayKey = classname + ".part-abbreviation-display";
		
	}
	
	/**
	 * Default PitchScaler. @Override if needed.
	 */
	protected void createPitchScaler() {
		setPitchScaler(new PitchScaler(this));
	}
	protected void createDurationScaler() {
		DurationScaler ds = new DurationScaler(this);
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

	public Interval getTransposeInterval() {
		return transposeInterval;
	}

	public void setTransposeInterval(Interval transposeInterval) {
		this.transposeInterval = transposeInterval;
		transposes = transposeInterval.isZero() ? false : true;
	}
	
}
