package org.dwbzen.music.element;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.dwbzen.common.util.IJson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ScaleFormulas implements IJson {

	static final org.apache.log4j.Logger log = Logger.getLogger(ScaleFormulas.class);
	
	@JsonIgnore	private  ObjectMapper mapper = new ObjectMapper();
	@JsonPropertyOrder({"collection","scaleFormulas"})
	
	@JsonProperty("collection")		private String collectionName = "My Formulas";
	@JsonProperty("scaleFormulas")	private Map<String, ScaleFormula> scaleFormulas = new TreeMap<String, ScaleFormula>();
	
	public ScaleFormulas(String collectionName) {
		this.collectionName = collectionName;
	}

	public ScaleFormula addScaleFormula(ScaleFormula sf) {
		return scaleFormulas.put(sf.getName(), sf);
	}
	
	public ScaleFormulas searchBySize(int size) {
		ScaleFormulas sf = new ScaleFormulas("Size " + size);
		scaleFormulas.entrySet().stream().filter(sff -> sff.getValue().getSize() == size).forEach(formula -> sf.addScaleFormula(formula.getValue()));
		return sf;
	}
	
	public ScaleFormulas searchByGroup(String aGroup) {
		ScaleFormulas sf = new ScaleFormulas("Group: " + aGroup);
		scaleFormulas.entrySet().stream().filter(sff -> sff.getValue().getGroups().contains(aGroup)).forEach(formula -> sf.addScaleFormula(formula.getValue()));
		return sf;
	}

	public static void main(String...strings) {
		
		ScaleFormulas formulas = new ScaleFormulas("mySample");
		formulas.addScaleFormula(IScaleFormula.BLUES_DIMINISHED_SCALE_FORMULA);
		formulas.addScaleFormula(IScaleFormula.PENTATONIC_MINOR_SCALE_FORMULA);
		formulas.addScaleFormula(IScaleFormula.HIRAJOSHI_SCALE_FORMULA);
		formulas.addScaleFormula(IScaleFormula.BLUES_SCALE_FORMULA);
		formulas.addScaleFormula(IScaleFormula.PENTATONIC_MAJOR_SCALE_FORMULA);
		formulas.addScaleFormula(IScaleFormula.WHOLE_TONE_SCALE_FORMULA);
		formulas.addScaleFormula(IScaleFormula.MAJOR_SCALE_FORMULA);
		formulas.addScaleFormula(IScaleFormula.CHROMATIC_SCALE_FORMULA);
		
		System.out.println(formulas.toJson(true));
		
		ScaleFormulas sf = formulas.searchBySize(5);
		System.out.println(sf.toJson(true));
		
		sf = formulas.searchByGroup("blues");
		System.out.println(sf.toJson(true));

	}
	
}
