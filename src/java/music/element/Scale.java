package music.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import music.element.Key.Mode;
import util.IJson;
import util.INameable;

import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;

/**
 *  A Scale is a specific realization of a ScaleFormula that has a root (starting note)
 *  and optionally may be associated with a Key.
 *  
 * @author don_bacon
 *
 */
@Entity(value="Scale", noClassnameStored=true)
public class Scale implements IJson, INameable  {

	private static final long serialVersionUID = -6449893042332225583L;
	
	public static final String MAJOR = "major";
	public static final String MINOR = "minor";
	public static final String MODE = "mode";		// a modal scale or mode of some scale: Dorian, etc.
	public static final String WHOLE_TONE = "whole tone";
	public static final String CHROMATIC_12TONE = "chromatic 12-tone";
	public static final String PENTATONIC = "pentatonic";
	public static final String DISCRETE = "discrete";	// unpitched instruments
	public static final String HEXATONIC = "hexatonic";
	public static final String ASCENDING = " ascending";
	
	private static Morphia morphia = new Morphia();
	/**
	 * All the defined scales in this Map
	 */
	private static Map<String, Scale> SCALE_MAP = new HashMap<String, Scale>();

	@Id ObjectId id;
	@Property("name")		private String name = null;
	@Property("mode")		private String mode = null;			// valid values: MAJOR, MINOR, MODE, can be null if N/A
	@Embedded("type")		private ScaleType scaleType = null;
	@Embedded("root")		private Pitch root = null;
	@Property("rootPitch")	private String rootPitch = null; 	// C, C#, Bb etc.
	@Embedded("key")		private Key key = null;				// if there is an associated Key, could be null
	@Embedded("pitches")	private List<Pitch> pitches = new ArrayList<Pitch>();
	@Property("notes")		private String notes = null;		// the toString(this) for readability
	@Transient				private ScaleFormula scaleFormula = null;
	@Property("formulaName")	private String formulaName;
	@Property("description")	private String description;		// optional descriptive text
	
	public Scale() {
	}
	
	/**
	 * Constructs a scale from an array of Pitch. Key is defaulted to Key.C_MAJOR
	 * It that's not what you want - use a constructor and set to (Key)null.
	 * 
	 * @param name any non-null name for this scale
	 * @param mode MAJOR, MINOR, MODE can be null if N/A or DISCRETE if unpitched
	 * @param type associated ScaleType
	 * @param root the root Pitch
	 * @param pitchs an array of Pitch
	 */
	public Scale(String name, String mode, ScaleType type, Pitch root, Pitch...pitchs ) {
		this(name, mode, type, root, Key.C_MAJOR);
		for(int i=0; i<pitchs.length; i++) {
			pitches.add(pitchs[i]);
		}
		rootPitch = root.toString();
		this.notes = toString();
	}
	
	/**
	 * Constructs a scale from an array of Pitch.
	 * This constructor needed to instance a Scale from Mongo JSON record.
	 * 
	 * @param name any non-null name for this scale
	 * @param mode MAJOR, MINOR, MODE can be null if N/A
	 * @param type associated ScaleType
	 * @param root the root Pitch
	 * @param pitchs an array of Pitch
	 * @param scaleFormulaName the String formulaName
	 */
	public Scale(String name, String mode, ScaleType type, Pitch root, Key key, Pitch[] pitchs,  String notes, String scaleFormulaName ) {
		this(name, mode, type, root, pitchs);
		this.key = key;
		this.notes = notes;
		this.formulaName = scaleFormulaName;
	}
	
	public Scale(String name, String mode, ScaleType type, String rootPitch, Key key, Pitch[] pitchs,  String notes, String scaleFormulaName ) {
		this.name = name;
		this.mode = mode;
		this.scaleType = type;
		this.rootPitch = rootPitch;
		this.root = new Pitch(rootPitch);
		this.key = key;
		this.notes = notes;
		for(int i=0; i<pitchs.length; i++) {
			pitches.add(pitchs[i]);
		}
		this.formulaName = scaleFormulaName;
	}
	
	/**
	 * Constructs a scale from an array of Pitch.
	 * This constructor needed to instance a Scale from Mongo JSON record.
	 */
	public Scale(String name, ScaleType type, Pitch root, Key key, Pitch[] pitchs,  String notes, String scaleFormulaName) {
		this(name, null, type, root, key, pitchs, notes, scaleFormulaName);
	}

	/**
	 * Construct a Scale from a scale formula. Key is defaulted to Key.C_MAJOR
	 * It that's not what you want - use a constructor and set to (Key)null.
	 * @param name any non-null name for this scale
	 * @param mode MAJOR, MINOR, MODE
	 * @param type associated ScaleType
	 * @param root the root Pitch
	 * @param formula ScaleFormula of intervals
	 */
	public Scale(String name, String mode, ScaleType type, Pitch root, ScaleFormula formula) {
		this(name, mode, type, root, formula, Key.C_MAJOR);
	}
	
	/**
	 * Construct a Scale from a scale formula. Also adds to SCALE_MAP by name
	 * so all those static definitions are automatically added.
	 * @param name any non-null name for this scale
	 * @param mode MAJOR, MINOR, MODE
	 * @param type associated ScaleType
	 * @param root the root Pitch
	 * @param formula ScaleFormula of intervals
	 * @param key associated Key
	 */
	public Scale(String name, String mode, ScaleType type, Pitch root, ScaleFormula formula, Key key) {
		this(name, mode, type, root, key);
		pitches = IScaleFormula.createPitches(formula.getFormula(), root, key);
		this.scaleFormula = formula;
		this.formulaName = formula.getName();
		this.notes = toString();
		SCALE_MAP.put(name, this);
	}

	/**
	 * Construct a Scale from a scale formula.
	 * @param name any non-null name for this scale
	 * @param mode MAJOR, MINOR, MODE
	 * @param type associated ScaleType
	 * @param root the root Pitch
	 * @param formula ScaleFormula of intervals
	 * @param key associated Key
	 * @param pref Alteration preference in creating scale: UP_ONE or DOWN_ONE
	 */
	public Scale(String name, String mode, ScaleType type, Pitch root, ScaleFormula formula, Key key, Alteration pref) {
		this(name, mode, type, root, key);
		pitches = IScaleFormula.createPitches(formula.getFormula(), root, key, pref);
		this.scaleFormula = formula;
		this.formulaName = formula.getName();
	}

	protected Scale(String name, String mode, ScaleType type, Pitch root, Key key) {
		this.name = name;
		this.mode = mode;
		this.scaleType = type;
		this.root = root;
		this.key = key;
		this.rootPitch = root.toString();
	}

	/**
	 * Makes a deep copy of a Scale
	 * @param scale the Scale to copy
	 * @return Scale
	 */
	public static Scale copyScale(Scale scale) {
		Pitch[] pitches = new Pitch[scale.pitches.size()];
		scale.pitches.toArray(pitches);
		Scale copy = new Scale(scale.name, scale.mode, scale.scaleType, scale.root, scale.key, pitches,  scale.notes, scale.formulaName);
		copy.scaleFormula = scale.getScaleFormula();
		copy.description = scale.description != null ? scale.description : "copy";
		return copy;
	}


	/**
	 * Mapped Diatonic Scales - Major and relative minor including harmonic minor
	 * Scales for all defined flat Keys
	 * TODO add melodic ascending for all keys
	 */
	public static final Scale C_MAJOR = new Scale(Key.C_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.C, IScaleFormula.MAJOR_SCALE_FORMULA, Key.C_MAJOR);
	public static final Scale A_MINOR = new Scale(Key.A_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.A, IScaleFormula.MINOR_SCALE_FORMULA, Key.A_MINOR);
	public static final Scale A_MEDLODIC_MINOR_ASCENDING = new Scale(Key.A_MINOR_NAME + ASCENDING, MINOR, ScaleType.DIATONIC, Pitch.A, IScaleFormula.MELODIC_MINOR_ASCENDING_SCALE_FORMULA, Key.A_MINOR);
	public static final Scale A_HARMONIC_MINOR = new Scale("A-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.A, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.A_MINOR);
	
	public static final Scale F_MAJOR = new Scale(Key.F_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.F, IScaleFormula.MAJOR_SCALE_FORMULA, Key.F_MAJOR);
	public static final Scale D_MINOR = new Scale(Key.D_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.D, IScaleFormula.MINOR_SCALE_FORMULA, Key.D_MINOR);
	public static final Scale D_HARMONIC_MINOR = new Scale("D-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.D, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.D_MINOR);

	public static final Scale BFlat_MAJOR = new Scale(Key.BFlat_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.BFlat, IScaleFormula.MAJOR_SCALE_FORMULA, Key.BFlat_MAJOR);
	public static final Scale G_MINOR = new Scale(Key.G_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.G, IScaleFormula.MINOR_SCALE_FORMULA, Key.G_MINOR);
	public static final Scale G_HARMONIC_MINOR = new Scale("G-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.G, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.G_MINOR);

	public static final Scale EFlat_MAJOR = new Scale(Key.EFlat_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.EFlat, IScaleFormula.MAJOR_SCALE_FORMULA, Key.EFlat_MAJOR);
	public static final Scale C_MINOR = new Scale(Key.C_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.C, IScaleFormula.MINOR_SCALE_FORMULA, Key.C_MINOR);
	public static final Scale C_HARMONIC_MINOR = new Scale("C-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.C, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.C_MINOR);

	public static final Scale AFlat_MAJOR = new Scale(Key.AFlat_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.AFlat, IScaleFormula.MAJOR_SCALE_FORMULA, Key.AFlat_MAJOR);
	public static final Scale F_MINOR = new Scale(Key.F_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.F, IScaleFormula.MINOR_SCALE_FORMULA, Key.F_MINOR);
	public static final Scale F_HARMONIC_MINOR = new Scale("F-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.F, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.F_MINOR);

	public static final Scale DFlat_MAJOR = new Scale(Key.DFlat_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.DFlat, IScaleFormula.MAJOR_SCALE_FORMULA, Key.DFlat_MAJOR);
	public static final Scale BFlat_MINOR = new Scale(Key.BFlat_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.BFlat, IScaleFormula.MINOR_SCALE_FORMULA, Key.BFlat_MINOR);
	public static final Scale BFlat_HARMONIC_MINOR = new Scale("Bb-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.BFlat, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.BFlat_MINOR);

	public static final Scale GFlat_MAJOR = new Scale(Key.GFlat_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.GFlat, IScaleFormula.MAJOR_SCALE_FORMULA, Key.GFlat_MAJOR);
	public static final Scale EFlat_MINOR = new Scale(Key.EFlat_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.EFlat, IScaleFormula.MINOR_SCALE_FORMULA, Key.EFlat_MINOR);
	public static final Scale EFlat_HARMONIC_MINOR = new Scale("Eb-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.EFlat, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.EFlat_MINOR);

	public static final Scale CFlat_MAJOR = new Scale(Key.CFlat_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.CFlat, IScaleFormula.MAJOR_SCALE_FORMULA, Key.CFlat_MAJOR);
	public static final Scale AFlat_MINOR = new Scale(Key.AFlat_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.EFlat, IScaleFormula.MINOR_SCALE_FORMULA, Key.AFlat_MINOR);
	public static final Scale AFlat_HARMONIC_MINOR = new Scale("Ab-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.AFlat, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.AFlat_MINOR);

	/**
	 * Scales for all defined sharp keys
	 * TODO add ascending minor for all KEYS
	 */
	public static final Scale G_MAJOR = new Scale(Key.G_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.G, IScaleFormula.MAJOR_SCALE_FORMULA, Key.G_MAJOR);
	public static final Scale E_MINOR = new Scale(Key.E_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.E, IScaleFormula.MINOR_SCALE_FORMULA, Key.E_MINOR);
	public static final Scale E_HARMONIC_MINOR = new Scale("E-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.E, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.E_MINOR);

	public static final Scale D_MAJOR = new Scale(Key.D_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.D, IScaleFormula.MAJOR_SCALE_FORMULA, Key.D_MAJOR);
	public static final Scale B_MINOR = new Scale(Key.B_MINOR_NAME , MINOR, ScaleType.DIATONIC, Pitch.B, IScaleFormula.MINOR_SCALE_FORMULA, Key.B_MINOR);
	public static final Scale B_HARMONIC_MINOR = new Scale("B-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.B, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.B_MINOR);

	public static final Scale A_MAJOR = new Scale(Key.A_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.A, IScaleFormula.MAJOR_SCALE_FORMULA, Key.A_MAJOR);
	public static final Scale FSharp_MINOR = new Scale(Key.FSharp_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.FSharp, IScaleFormula.MINOR_SCALE_FORMULA, Key.FSharp_MINOR);
	public static final Scale FSharp_HARMONIC_MINOR = new Scale("F#-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.FSharp, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.FSharp_MINOR);

	public static final Scale E_MAJOR = new Scale(Key.E_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.E, IScaleFormula.MAJOR_SCALE_FORMULA, Key.E_MAJOR);
	public static final Scale CSharp_MINOR = new Scale(Key.CSharp_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.CSharp, IScaleFormula.MINOR_SCALE_FORMULA, Key.CSharp_MINOR);
	public static final Scale CSharp_HARMONIC_MINOR = new Scale("C#-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.CSharp, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.CSharp_MINOR);

	public static final Scale B_MAJOR = new Scale(Key.B_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.B, IScaleFormula.MAJOR_SCALE_FORMULA, Key.B_MAJOR);
	public static final Scale GSharp_MINOR = new Scale(Key.GSharp_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.GSharp, IScaleFormula.MINOR_SCALE_FORMULA, Key.GSharp_MINOR);
	public static final Scale GSharp_HARMONIC_MINOR = new Scale("G#-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.GSharp, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.GSharp_MINOR);

	public static final Scale FSharp_MAJOR = new Scale(Key.FSharp_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.FSharp, IScaleFormula.MAJOR_SCALE_FORMULA, Key.FSharp_MAJOR);
	public static final Scale DSharp_MINOR = new Scale(Key.DSharp_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.DSharp, IScaleFormula.MINOR_SCALE_FORMULA, Key.DSharp_MINOR);
	public static final Scale DSharp_HARMONIC_MINOR = new Scale("D#-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.DSharp, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.DSharp_MINOR);

	public static final Scale CSharp_MAJOR = new Scale(Key.CSharp_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.CSharp, IScaleFormula.MAJOR_SCALE_FORMULA, Key.CSharp_MAJOR);
	public static final Scale ASharp_MINOR = new Scale(Key.ASharp_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.ASharp, IScaleFormula.MINOR_SCALE_FORMULA, Key.ASharp_MINOR);
	public static final Scale ASharp_HARMONIC_MINOR = new Scale("A#-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.ASharp, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.ASharp_MINOR);

	/**
	 * Mapped non-Diatonic scales
	 */
	public static final Scale C_WHOLETONE = 
			new Scale("C-WholeTone", WHOLE_TONE, ScaleType.WHOLE_TONE, Pitch.C, IScaleFormula.WHOLE_TONE_SCALE_FORMULA, Key.C_MAJOR);
	public static final Scale GFlat_MAJOR_PENTATONIC = 
			new Scale("GFlat-Major-Pentatonic", PENTATONIC, ScaleType.PENTATONIC, Pitch.GFlat, IScaleFormula.PENTATONIC_MAJOR_SCALE_FORMULA, Key.DFlat_MAJOR);
	public static final Scale EFlat_MINOR_PENTATONIC = 
			new Scale("EFlat-Minor-Pentatonic", PENTATONIC, ScaleType.PENTATONIC, Pitch.EFlat, IScaleFormula.PENTATONIC_MINOR_SCALE_FORMULA, Key.BFlat_MINOR);
	public static final Scale BLUES_SCALE =
			new Scale("Blues in C", HEXATONIC, ScaleType.HEXATONIC, Pitch.C, IScaleFormula.BLUES_SCALE_FORMULA);
	public static final Scale HIRAJOSHI_SCALE =
			new Scale("Hirajoshi Japan in D", PENTATONIC, ScaleType.PENTATONIC, Pitch.D, IScaleFormula.HIRAJOSHI_SCALE_FORMULA);
	public static final Scale JEWISH_AHAVOH_RABBOH_SCALE =
			new Scale("Jewish Ahavoh-Rabboh in C", MODE, ScaleType.HEPTATONIC, Pitch.C, IScaleFormula.JEWISH_AHAVOH_RABBOH_SCALE_FORMULA);
	
	public static final Scale UNPITCHED_5_STEP_SCALE =
			new Scale("5-Line Unpitched Percussion", DISCRETE, ScaleType.DISCRETE_5LINE, Pitch.E, IScaleFormula.UNPITCHED_5_SCALE_FORMULA);
	public static final Scale UNPITCHED_4_STEP_SCALE =
			new Scale("4-Line Unpitched Percussion", DISCRETE, ScaleType.DISCRETE_4LINE, Pitch.E, IScaleFormula.UNPITCHED_4_SCALE_FORMULA);
	public static final Scale UNPITCHED_3_STEP_SCALE =
			new Scale("3-Line Unpitched Percussion", DISCRETE, ScaleType.DISCRETE_3LINE, Pitch.E, IScaleFormula.UNPITCHED_3_SCALE_FORMULA);
	public static final Scale UNPITCHED_2_STEP_SCALE =
			new Scale("2-Line Unpitched Percussion", DISCRETE, ScaleType.DISCRETE_2LINE, Pitch.B, IScaleFormula.UNPITCHED_2_SCALE_FORMULA);
	
	static int[] chromaticSteps = {1,1,1,1,1,1,1,1,1,1,1};
	static int[] fullRangeChromaticSteps = new int[Pitch.C0.difference(Pitch.C9) ];		// 108 pitches
	static final ScaleFormula chromaticScaleFormula =  new ScaleFormula("12-Tone Chromatic", "chromatic", chromaticSteps);
	static ScaleFormula fullRangeChromaticScaleFormula = null;
	
	/**
	 * 12-note Chromatic scale stating at A, does not repeat the root 
	 */
	public static final Scale CHROMATIC_12TONE_SCALE =
			new Scale("Chromatic 12-tone", CHROMATIC_12TONE, ScaleType.CHROMATIC, Pitch.A, chromaticScaleFormula);
	
	/**
	 * Full range chromatic scale: C0 to C9 in Scientific Pitch Notation
	 */
	public static Scale FULL_RANGE_CHROMATIC_SCALE = null;
	
	static {
		for(int i=0; i<fullRangeChromaticSteps.length; i++) {
			fullRangeChromaticSteps[i] = 1;
		}
		fullRangeChromaticScaleFormula = new ScaleFormula("Full range Chromatic", "chromatic", fullRangeChromaticSteps);
		FULL_RANGE_CHROMATIC_SCALE = new Scale("Full Range Chromatic", CHROMATIC_12TONE, ScaleType.CHROMATIC, Pitch.C0, fullRangeChromaticScaleFormula);
	}
	
	/**
	 * Maps of chromatic step number (1 - 12) to scale degree (1 - 7 with accidental)
	 * There are four - for sharp keys (MAJOR and HARMONIC_MINOR) and flat keys
	 * Can be indexed directly by step number
	 */
	static String[] major_sharp_to_scale_degree = {
		"0", "1", "1#", "2", "2#", "3", "4", "4#", "5", "5#", "6", "6#", "7"
	};
	static String[] minor_sharp_to_scale_degree = {
		"0", "1", "1#", "2", "3", "3#", "4", "4#", "5", "6", "6#", "6##", "7"
	};
	static String[] major_flat_to_scale_degree = {
		"0", "1", "2b", "2", "3b", "3", "4", "5b", "5", "6b", "6", "7b", "7"
	};
	static String[] minor_flat_to_scale_degree = {
		"0", "1", "2b", "2", "3", "4b", "4", "5b", "5", "6", "7bb", "7b", "7"
	};

	public String toJSON() {
		return morphia.toDBObject(this).toString();
	}
	
	/**
	 * Comma-delimited pitches in this scale
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		Iterator<Pitch> pit = pitches.iterator();
		sb.append(pit.next().toString());
		while(pit.hasNext()) {
			sb.append(", ");
			sb.append(pit.next().toString());
		}
		return sb.toString();
	}
	
	/**
	 * Removes the last Pitch which is the same Pitch as the first note an octave higher,
	 * or for octave-neutral scale, same as the first note.
	 * @return a new Scale instance (copy of this) sans last note/pitch
	 * 
	 * @see IScaleFormula
	 */
	public Scale truncate() {
		Scale truncatedScale = Scale.copyScale(this);
		truncatedScale.removeLastPitch();
		return truncatedScale;
	}
	
	protected void removeLastPitch() {
		int index = pitches.size() - 1;
		pitches.remove(index);
	}

	/**
	 * Creates a new scale from this one, transposed with given root
	 * @param nroot the new root Pitch
	 * @return Scale
	 */
	public Scale transpose(Pitch nroot) {
		ScaleType st = this.getScaleType();
		String sm = this.getMode();
		ScaleFormula formula = (sm.equalsIgnoreCase(MAJOR)) ? IScaleFormula.MAJOR_SCALE_FORMULA : IScaleFormula.MINOR_SCALE_FORMULA;
		String sname = nroot.toString() + "-" + ((sm.equalsIgnoreCase(MAJOR)) ? MAJOR : MINOR);
		Mode mode = (sm.equalsIgnoreCase(MAJOR)) ? Mode.MAJOR : Mode.MINOR;
		Key nkey = Key.getKey(nroot, mode);
		int altPref = nkey.getAlterationPreference();
		Alteration alt = (altPref == 0) ? Alteration.NONE : (altPref<0) ? Alteration.DOWN_ONE : Alteration.UP_ONE;
		Scale scale = new Scale(sname, sm, st, nroot, formula, nkey, alt);
		return scale;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public ScaleType getScaleType() {
		return scaleType;
	}

	public void setScaleType(ScaleType scaleType) {
		this.scaleType = scaleType;
	}

	public Pitch getRoot() {
		return root;
	}

	public void setRoot(Pitch root) {
		this.root = root;
	}

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	public List<Pitch> getPitches() {
		return pitches;
	}
	
	public String getRootPitch() {
		return rootPitch;
	}

	public ScaleFormula getScaleFormula() {
		return scaleFormula;
	}

	public String getFormulaName() {
		return formulaName;
	}

	public static Scale getScale(String name) {
		return SCALE_MAP.get(name);
	}
	public static Map<String, Scale> getScaleMap() {
		return SCALE_MAP;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNotes() {
		if(notes == null) {
			notes = toString();
		}
		return notes;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public int size() {
		return pitches.size();
	}

	public static String getScaleDegreeFromChromaticStep(Key key, int step) {
		String sd = null;
		Mode kmode = key.getMode();
		if(step >= 1 && step <= 12) {
			if(kmode.equals(Mode.MAJOR)) {
				if(key.isFlatKey()) {
					sd = major_flat_to_scale_degree[step];
				}
				else {
					sd = major_sharp_to_scale_degree[step];
				}
			}
			else if(kmode.equals(Mode.MINOR)) {
				if(key.isFlatKey()) {
					sd = minor_flat_to_scale_degree[step];
				}
				else {
					sd = minor_sharp_to_scale_degree[step];
				}
			}
		}
		return sd;
	}
	
	public static void main(String... args) {
		Scale dmScale = Scale.D_MINOR;
		System.out.println(dmScale.toJSON());
		
		Scale unpitched = Scale.UNPITCHED_5_STEP_SCALE;
		System.out.println(unpitched.toJSON());
	}
}
