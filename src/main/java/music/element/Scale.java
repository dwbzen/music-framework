package music.element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import music.element.Key.Mode;
import music.element.Scales;
import mathlib.util.IJson;
import util.INameable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *  A Scale is a specific realization of a ScaleFormula that has a root (starting note)
 *  and optionally may be associated with a Key.
 *  Scale is immutable.
 *  
 * @author don_bacon
 *
 */
public final class Scale implements IJson, INameable, Cloneable  {

	private static final long serialVersionUID = -6449893042332225583L;

	@JsonProperty("name")		private final String name;
	@JsonProperty("mode")		private final String mode;			// valid values: MAJOR, MINOR, MODE, can be null if N/A
	@JsonProperty("type")		private final ScaleType scaleType;
	@JsonProperty("root")		private final Pitch root;
	@JsonProperty("rootPitch")	private String rootPitch; 	// C, C#, Bb etc.
	@JsonProperty("key")		private Key key;				// if there is an associated Key, could be null
	@JsonProperty("pitches")	private List<Pitch> pitches = new ArrayList<Pitch>();
	@JsonProperty("notes")		private String notes = null;		// the toString(this) for readability
	@JsonIgnore					private ScaleFormula scaleFormula = null;
	@JsonProperty("formulaName")	private String formulaName;
	@JsonProperty("description")	private String description;		// optional descriptive text
	
	
	/**
	 * Construct a Scale from a scale formula. Also adds to SCALE_MAP by name
	 * so all those static definitions are automatically added.
	 * 
	 * @param name any non-null name for this scale
	 * @param mode Scales.MAJOR, Scales.MINOR, Scales.MODE
	 * @param type associated ScaleType
	 * @param root the root Pitch
	 * @param formula ScaleFormula of intervals
	 * @param key associated Key, if null Key is defaulted to Key.C_MAJOR
	 */
	public Scale(String name, String mode, ScaleType type, Pitch root, ScaleFormula formula, Key key) {
		this(name, mode, type, root, key);
		pitches = IScaleFormula.createPitches(formula.getFormula(), root, key);
		scaleFormula = formula;
		formulaName = formula.getName();
		notes = toString();
		Scales.addScaleToScaleMap(name, this);
	}
	
	/**
	 * Construct a Scale from a scale formula. Key is defaulted to Key.C_MAJOR
	 *
	 * @param name any non-null name for this scale
	 * @param mode Scales.MAJOR, Scales.MINOR, Scales.MODE
	 * @param type associated ScaleType
	 * @param root the root Pitch
	 * @param formula ScaleFormula of intervals
	 */
	public Scale(String name, String mode, ScaleType type, Pitch root, ScaleFormula formula) {
		this(name, mode, type, root, formula, Key.C_MAJOR);
	}
	
	/**
	 * Constructs a scale from an array of Pitch. Key is defaulted to Key.C_MAJOR
	 * 
	 * @param name any non-null name for this scale
	 * @param mode MAJOR, MINOR, MODE can be null if N/A or DISCRETE if unpitched
	 * @param type associated ScaleType
	 * @param root the root Pitch
	 * @param pitchs an array of Pitch
	 */
	private Scale(String name, String mode, ScaleType type, Pitch root, Pitch...pitchs ) {
		this(name, mode, type, root, Key.C_MAJOR);
		for(int i=0; i<pitchs.length; i++) {
			pitches.add(pitchs[i]);
		}
		rootPitch = root.toString();
		this.notes = toString();
	}
	
	/**
	 * Constructs a scale from an array of Pitch.
	 * 
	 * @param name any non-null name for this scale
	 * @param mode MAJOR, MINOR, MODE can be null if N/A
	 * @param type associated ScaleType
	 * @param root the root Pitch
	 * @param pitchs an array of Pitch
	 * @param scaleFormulaName the String formulaName
	 */
	private Scale(String name, String mode, ScaleType type, Pitch root, Key key, Pitch[] pitchs,  String notes, String scaleFormulaName ) {
		this(name, mode, type, root, pitchs);
		this.key = key;
		this.notes = notes;
		this.formulaName = scaleFormulaName;
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
	private Scale(String name, String mode, ScaleType type, Pitch root, ScaleFormula formula, Key key, Alteration pref) {
		this(name, mode, type, root, key);
		pitches = IFormula.createPitches(formula.getFormula(), root, key, pref);
		scaleFormula = formula;
		formulaName = formula.getName();
	}

	private Scale(String name, String mode, ScaleType type, Pitch root, Key key) {
		this.name = name;
		this.mode = mode;
		scaleType = type;
		this.root = root;
		this.key = (key == null) ? Key.C_MAJOR : key;
;
		rootPitch = root.toString();
	}

	/**
	 * Makes a deep copy of a Scale
	 * @param scale the Scale to copy
	 * @return a new Scale that is a deep copy of the input Scale
	 */
	public static Scale copyScale(Scale scale) {
		Pitch[] pitches = new Pitch[scale.pitches.size()];
		scale.pitches.toArray(pitches);
		Scale copy = new Scale(scale.name, scale.mode, scale.scaleType, scale.root, scale.key, pitches,  scale.notes, scale.formulaName);
		copy.scaleFormula = scale.getScaleFormula();
		copy.description = scale.description != null ? scale.description : "copy";
		return copy;
	}
	
	@Override
	public Scale clone() {
		return copyScale(this);
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
	
	private void removeLastPitch() {
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
		ScaleFormula formula = (sm.equalsIgnoreCase(Scales.MAJOR)) ? IScaleFormula.MAJOR_SCALE_FORMULA : IScaleFormula.MINOR_SCALE_FORMULA;
		String sname = nroot.toString() + "-" + ((sm.equalsIgnoreCase(Scales.MAJOR)) ? Scales.MAJOR : Scales.MINOR);
		Mode mode = (sm.equalsIgnoreCase(Scales.MAJOR)) ? Mode.MAJOR : Mode.MINOR;
		Key nkey = Key.getKey(nroot, mode);
		int altPref = nkey.getAlterationPreference();
		Alteration alt = (altPref == 0) ? Alteration.NONE : (altPref<0) ? Alteration.DOWN_ONE : Alteration.UP_ONE;
		Scale scale = new Scale(sname, sm, st, nroot, formula, nkey, alt);
		return scale;
	}

	public String getName() {
		return name;
	}

	public String getMode() {
		return mode;
	}

	/**
	 * 
	 * @return immutable ScaleType
	 */
	public ScaleType getScaleType() {
		return scaleType;
	}

	public Pitch getRoot() {
		return new Pitch(root);
	}

	public Key getKey() {
		return key;
	}

	/**
	 * Gets a copy of scale pitches
	 * @return new List<Pitch> of Scale pitches
	 */
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
		return Scales.getScale(name);
	}
	public static Map<String, Scale> getScaleMap() {
		return Scales.getScaleMap();
	}
	
	public String getDescription() {
		return description;
	}

	public String getNotes() {
		if(notes == null) {
			notes = toString();
		}
		return notes;
	}

	public int size() {
		return pitches.size();
	}

}
