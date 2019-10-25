package org.dwbzen.music.element.song;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.dwbzen.common.util.IJson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ChordFormulas implements IJson {

	static final org.apache.log4j.Logger log = Logger.getLogger(ChordFormula.class);
	
	@JsonIgnore	private  ObjectMapper mapper = new ObjectMapper();
	@JsonProperty("chordFormulas")	private Collection<ChordFormula> chordFormulas = new HashSet<>();
	
	public ChordFormulas() {
		
	}
	
	public boolean addChordFormula(ChordFormula f) {
		return chordFormulas.add(f);
	}
	
	public Collection<ChordFormula> getChordFormulas() {
		return chordFormulas;
	}
	
	public static void main(String...strings) {
		ChordFormula f = ChordLibrary.HALF_DIMINISHED_SEVENTH;
		ChordFormula f2 = ChordLibrary.MINOR_SEVENTH_FLAT_FIFTH;
		ChordFormulas formulas = new ChordFormulas();
		formulas.addChordFormula(f);
		formulas.addChordFormula(f2);
		String jstr = formulas.toJson(true);
		System.out.println(jstr);
		
		ObjectMapper mapper = new ObjectMapper();

		formulas = null;
		try {
			formulas = mapper.readValue(jstr, ChordFormulas.class);
		} catch (IOException e) {
			log.error("Cannot deserialize " + jstr + "\nbecause " + e.toString());
		}
		if(formulas != null) {
			System.out.println(formulas.toJson());
		}
	}
	
}
