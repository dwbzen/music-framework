package music.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;

import util.IJson;
import util.IMapped;

/**
 * A ScaleFormula is exactly that - a sequence of steps (a chromatic step)
 *  that can be used to create a Scale.
 * A Scale is a specific realization of a ScaleFormula that has a root (starting note)
 * and other features.
 * 
 * @author don_bacon
 *
 */
@Entity(value="ScaleFormula", noClassnameStored=true)
public class ScaleFormula implements IScaleFormula, IJson, IMapped<String> {
	private static final long serialVersionUID = 8075575845123712068L;
	private static Morphia morphia = new Morphia();
	
	@Id ObjectId id;
	@Property	protected String name;
	@Property	protected List<String> alternateNames = null;
	@Property	protected List<String> groups = new ArrayList<String>();
	@Property	private double[] formula;
	@Transient  private int[] _formula;		// because int[] values come back as double[] in Morphia
	@Property	private double size;
	@Property	protected String description = null;	// optional descriptive text
	@Property	private List<String> intervals = new ArrayList<String>();	// optional
	/**
	 * formulaNumber is a 3-byte binary (12 bits) where each bit corresponds to the scale degree-1
	 * Works for a scale or a chord.
	 */
	@Property	private int formulaNumber = 0x0FFF;

	/**
	 * Create a null ScaleFormula. Used for "silent" chords.
	 */
	public ScaleFormula() {
		name = "0";
		size = 0;
		formulaNumber = 0;
	}
	
	public ScaleFormula(String name, String group, int[] frmla, String[] altNames, String[] intvls) {
		this.name = name;
		groups.add(group);
		if(altNames != null && altNames.length > 0 ) { alternateNames = Arrays.asList(altNames); }
		if(intvls != null && intvls.length > 0) { intervals = Arrays.asList(intvls); }
		size = frmla.length;
		setFormula(frmla);
	}
	public ScaleFormula(String name, String[] group, int[] frmla, String[] altNames, String[] intvls) {
		this.name = name;
		if(group != null && group.length > 0) { groups = Arrays.asList(group); }
		if(altNames != null && altNames.length > 0 ) { alternateNames = Arrays.asList(altNames); }
		if(intvls != null && intvls.length > 0) { intervals = Arrays.asList(intvls); }
		size = frmla.length;
		setFormula(frmla);
	}
	
	public ScaleFormula(String name, String group, int[] frmla) {
		this.name = name;
		groups.add(group);
		size = frmla.length;
		setFormula(frmla);
	}
	
	private void setFormula(int[] frmla) {
		formula = new double[frmla.length];
		_formula = new int[formula.length];
		for(int i =0; i<size; i++) {
			formula[i] = frmla[i];
			_formula[i] = frmla[i];
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addGroup(String group) {
		groups.add(group);
	}
	
	public List<String> getGroups() {
		return groups;
	}

	public int[] getFormula() {
		if(_formula == null) {
			_formula = new int[formula.length];
			for(int i=0; i<formula.length; i++) {
				_formula[i] = (int)formula[i];
			}
		}
		return _formula;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getAlternateNames() {
		if(alternateNames == null) {
			alternateNames = new ArrayList<String>();
		}
		return alternateNames;
	}
	
	public List<String> getIntervals() {
		return intervals;
	}

	public void setIntervals(List<String> intervals) {
		this.intervals = intervals;
	}

	public int getFormulaNumber() {
		return formulaNumber;
	}

	public void setFormulaNumber(int formulaNumber) {
		this.formulaNumber = formulaNumber;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public double getSize() {
		return size;
	}

	public String toJSON() {
		return morphia.toDBObject(this).toString();
	}
	
	@Override
	public List<Pitch> createPitches(Pitch root) {
		return IScaleFormula.createPitches(getFormula(), root, Key.C_MAJOR);
	}
	
	public List<Pitch> createPitches(Pitch root, Key key) {
		return IScaleFormula.createPitches(getFormula(), root, key);
	}

	/**
	 * Creates pitch set notation of this formula.
	 * For example given: {2, 2, 1, 2, 2, 2, 1}
	 * pitch set is: {0, 2, 4, 5, 7, 9, 11}
	 */
	public List<Integer> formulaToPitchSet() {
		return formulaToPitchSet(this._formula);
	}
	
	public String getMode() {
		String mode = null;
		String name = getName().toLowerCase();
		if(name.indexOf("major") >= 0) {
			mode = Scales.MAJOR;
		}
		else if(name.indexOf("minor") >= 0) {
			mode = Scales.MINOR;
		}
		else if(name.indexOf("mode") >= 0) {
			mode = Scales.MODE;
		}
		else if(formula.length == 5) {
			mode = Scales.PENTATONIC;
		}
		else if(formula.length == 6) {
			mode = Scales.HEXATONIC;
		}
		return mode;
	}

	public ScaleType getScaleType() {
		ScaleType st = null;
		int n = getFormula().length;
		switch(n) {
			case 1: st = ScaleType.MONOTONIC;
					break;
			case 2: st = ScaleType.DITONIC;
					break;
			case 3: st=ScaleType.TRITONIC;
					break;
			case 4: st = ScaleType.TETRATONIC;
					break;
			case 5: st = ScaleType.PENTATONIC;
					break;
			case 6: st = ScaleType.HEXATONIC;
					break;
			case 7: st = ScaleType.DIATONIC;
					break;
			case 8: st = ScaleType.OCTATONIC;
					break;
			case 9: st = ScaleType.NONATONIC;
					break;
			default: st = ScaleType.CHROMATIC;
		}
		return st;
	}
	
	public static List<Integer> formulaToPitchSet(int[] forml) {
		List<Integer> pitchSet = new ArrayList<Integer>();
		pitchSet.add(0);
		for(int i = 0; i<forml.length; i++) {
			pitchSet.add(forml[i] + pitchSet.get(i) );
		}
		return pitchSet;
	}
	
	public static List<Integer> formulaToPitchSet(List<Integer> forml) {
		int[] farray = new int[forml.size()];
		int i = 0;
		for(Integer n : forml) {
			farray[i++] = n;
		}
		return formulaToPitchSet(farray);
	}
	
	public static  List<Integer> pitchSetToFormula(int[] pitchSet) {
		List<Integer> psFormula = new ArrayList<Integer>();
		for(int i = 1; i<pitchSet.length; i++) {
			psFormula.add(pitchSet[i] - pitchSet[i-1]);
		}
		psFormula.add(12 - pitchSet[pitchSet.length-1]);
		return psFormula;
	}

	/**
	 * Alternate names are the key set.
	 */
	@Override
	public Set<String> keySet() {
		Set<String> keyset = new HashSet<String>();
		if(alternateNames != null && alternateNames.size() > 0) {
			keyset.addAll(alternateNames);
		}
		return keyset;
	}

	@Override
	public String rollJSON() {
		// TODO Auto-generated method stub
		return null;
	}

}
