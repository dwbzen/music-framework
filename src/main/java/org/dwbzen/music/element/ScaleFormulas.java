package org.dwbzen.music.element;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.dwbzen.common.util.IJson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ScaleFormulas implements IJson {

	private static final long serialVersionUID = -549037646756549231L;

	
	static final org.apache.log4j.Logger log = Logger.getLogger(ScaleFormulas.class);
	
	@JsonIgnore	private  ObjectMapper mapper = new ObjectMapper();
	@JsonPropertyOrder({"collection","scaleFormulas"})
	
	@JsonProperty("collection")		private String collection = null;
	@JsonProperty("scaleFormulas")	private Collection<ScaleFormula> scaleFormulas = new HashSet<>();
	
	public ScaleFormulas(String collectionName) {
		this.collection = collectionName;
	}

	public boolean addScaleFormula(ScaleFormula sf) {
		return scaleFormulas.add(sf);
	}

	public static void main(String...strings) {
		ScaleFormula sf1 = IScaleFormula.BLUES_DIMINISHED_SCALE_FORMULA;
		ScaleFormula sf2 = IScaleFormula.PENTATONIC_MINOR_SCALE_FORMULA;
		ScaleFormula sf3 = IScaleFormula.HIRAJOSHI_SCALE_FORMULA;
		
		ScaleFormulas formulas = new ScaleFormulas("sample");
		formulas.addScaleFormula(sf1);
		formulas.addScaleFormula(sf2);
		formulas.addScaleFormula(sf3);
		
		System.out.println(formulas.toJson(true));
		
	}
	
}
