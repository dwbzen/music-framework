package org.dwbzen.music.element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dwbzen.common.util.IJson;
import org.dwbzen.util.IMapped;

/**
 * A ScaleFormula is a sequence of steps (a chromatic step) that can be used to create a Scale from a starting root pitch. 
 * A Scale is a specific realization of a ScaleFormula that has a root (starting note) and other features.</p>
 * For example, the formula for a major scale is [2,2,1,2,2,2,1]</p>
 * Starting with a root of C, and applying the formula produces the scale:</p>
 * C, D (C + 2 steps), E (D + 2 steps), F (E + 1 step), G, A, B, C</p>
 * 
 * @author don_bacon
 *
 */
public final class ScaleFormula implements IScaleFormula, IJson, IMapped<String> {
	
	private static final long serialVersionUID = 8075575845123712068L;
	static final Logger log = LogManager.getLogger(ScaleFormula.class);
	static ObjectMapper mapper = new ObjectMapper();
	
	private final String name;
	@JsonInclude(Include.NON_EMPTY)
	private List<String> alternateNames = new ArrayList<String>();
	private List<String> groups = new ArrayList<String>();
	private List<Integer> formula = new ArrayList<Integer>();
	private int size;

	static {
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
	}

	/**
	 * Create a null ScaleFormula. Used for "silent" chords.
	 */
	public ScaleFormula() {
		name = "0";
		size = 0;
	}
	
	public ScaleFormula(String name, String group, int[] frmla, String[] altNames) {
		this.name = name;
		groups.add(group);
		if(altNames != null && altNames.length > 0 ) { alternateNames = Arrays.asList(altNames); }
		size = frmla.length;
		setFormula(frmla);
	}
	public ScaleFormula(String name, String[] group, int[] frmla, String[] altNames) {
		this.name = name;
		if(group != null && group.length > 0) { groups = Arrays.asList(group); }
		if(altNames != null && altNames.length > 0 ) { alternateNames = Arrays.asList(altNames); }
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
		for(int i : frmla) {
			formula.add(i);
		}
	}

	public String getName() {
		return name;
	}

	public List<String> getGroups() {
		return groups;
	}

	public List<Integer> getFormula() {
		return formula;
	}

	public List<String> getAlternateNames() {
		if(alternateNames == null) {
			alternateNames = new ArrayList<String>();
		}
		return alternateNames;
	}

	public int getSize() {
		return size;
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
		return IFormula.formulaToPitchSet(formula);
	}
	
	@JsonIgnore 
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
		else if(formula.size() == 5) {
			mode = Scales.PENTATONIC;
		}
		else if(formula.size() == 6) {
			mode = Scales.HEXATONIC;
		}
		return mode;
	}

	@JsonIgnore 
	public ScaleType getScaleType() {
		ScaleType st = null;
		int n = getFormula().size();
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
	
	
	public static ScaleFormula deserialize(String formulaString) {
		ScaleFormula scaleFormula = null;
		try {
			scaleFormula = mapper.readValue(formulaString, ScaleFormula.class);
		} catch (IOException e) {
			log.error("Cannot deserialize " + formulaString + "\nbecause " + e.toString());
		}
		return scaleFormula;
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
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}


}
