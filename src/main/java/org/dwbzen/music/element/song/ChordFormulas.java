package org.dwbzen.music.element.song;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.dwbzen.common.util.IJson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChordFormulas implements IJson {

	private static final long serialVersionUID = 1364359049363338964L;
	static final org.apache.log4j.Logger log = Logger.getLogger(ChordFormula.class);
	
	@JsonIgnore	private  ObjectMapper mapper = new ObjectMapper();
	@JsonProperty("chordFormulas")	private Collection<ChordFormula> chordFormulas = new HashSet<>();
	
	public ChordFormulas() {
		
	}
	
	public boolean addChordFormula(ChordFormula f) {
		return chordFormulas.add(f);
	}
	
	
	public static void main(String...strings) {
		int[] cf = {5, 2, 3, 4};
		String[] intervals = {"P4", "M2", "m3", "M3"};
		ChordFormula f = new ChordFormula("9Sus4", "9sus4", "suspended", cf, intervals);
		ChordFormula f2 = ChordLibrary.MINOR_SEVENTH_FLAT_FIFTH;
		ChordFormulas formulas = new ChordFormulas();
		formulas.addChordFormula(f);
		formulas.addChordFormula(f2);
		System.out.println(formulas.toJson(true));
	}
	
}
