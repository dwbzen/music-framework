package music.transform;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import mathlib.IntegerPair;
import music.element.PitchRange;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import mathlib.util.IJson;

public abstract class AbstractExploder implements IJson, IExploder {

	private static final long serialVersionUID = -3056868476406574952L;
	protected static final org.apache.log4j.Logger log = Logger.getLogger(ExplodeTransformer.class);
		
	@JsonIgnore				private ThreadLocalRandom random = ThreadLocalRandom.current();
	@JsonProperty("name")		protected String name;
	@JsonProperty("type")		protected ExploderType exploderType = null;	// ARPEGIO or CHORD
	@JsonProperty("formula")	protected List<IntegerPair> formula = null;
	@JsonProperty("ratio")		protected IntegerPair ratio = null;
	@JsonProperty("frequency")	protected int frequency;
	/*
	 * Defines the pitch limits.
	 */
	@JsonIgnore				protected PitchRange pitchRange = null;
	
	protected  AbstractExploder(ExploderType et, List<IntegerPair> formula, IntegerPair ratio, int freq) {
		this.exploderType = et;
		this.formula = formula;
		this.ratio = ratio;
		this.frequency = freq;
	}
	

	@Override
	/**
	 * size is the number of "normal notes" in the explode formula/ratio
	 * For a ratio of 1:1, this is just the size of the formula.
	 * Otherwise for a tuplet, this is the "in the time of" element of the ratio.
	 * So for a triplet, which has a ratio of 3:2 (3 notes in the time of 2) the size is 2.
	 * For a CHORD the operational size is just 1.
	 */
	public int size() {
		int size = (exploderType.equals(ExploderType.CHORD)) ? 1 : formula.size();
		if(!ratio.same()) {
			size = ratio.getY();
		}
		return size;
	}
	
	
	public List<IntegerPair> getFormula() {
		return formula;
	}
	public void setFormula(List<IntegerPair> formula) {
		this.formula = formula;
	}
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	public PitchRange getPitchRange() {
		return pitchRange;
	}
	public void setPitchRange(PitchRange pitchRange) {
		this.pitchRange = pitchRange;
	}
	
	public ThreadLocalRandom getRandom() {
		return random;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ExploderType getExploderType() {
		return exploderType;
	}
	
}
