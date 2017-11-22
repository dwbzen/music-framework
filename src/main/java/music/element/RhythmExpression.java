package music.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import mathlib.util.IJson;
import util.Ratio;

public class RhythmExpression implements IJson, IRhythmExpression {

	private static final long serialVersionUID = -1537702606983613050L;
	
	@JsonProperty("units")			private int units;
	@JsonProperty("ratio")			private Ratio ratio = Ratio.ONE_TO_ONE;
	@JsonProperty("texture") 		private TextureType textureType = TextureType.MONOPHONIC;	// MONMOPHONIC or CHORDAL
	@JsonProperty("rhythmicUnit")	private RhythmicUnitType rhythmicUnitType = RhythmicUnitType.METRIC;	// METRIC or EXTRAMETRIC
	@JsonProperty("depth") 			private Set<Integer> chordalDepth = new TreeSet<Integer>();	// for CHORDAL expression, #notes permitted in a chord
	@JsonProperty("factors")		private List<Duration> factors = new ArrayList<Duration>();
	@JsonIgnore		private IRhythmScale rhythmScale = null;
	
	/*
	 * some common ratios
	 */
	static Ratio ONE_TO_ONE = Ratio.ONE_TO_ONE;
	static Ratio THREE_TO_TWO = Ratio.THREE_TO_TWO;
	static Ratio FIVE_TO_FOUR = Ratio.FIVE_TO_FOUR;
	
	/**
	 * Creates a RhythmExpression for Chordal texture
	 * @param units
	 * @param exp
	 * @param pd
	 * @param rs
	 */
	public RhythmExpression(int units, Ratio exp, int[] pd, IRhythmScale rs) {
		this.units = units;
		ratio = exp;
		textureType = TextureType.CHORDAL;
		rhythmicUnitType = exp.equals(ONE_TO_ONE) ? RhythmicUnitType.METRIC : RhythmicUnitType.EXTRAMETRIC;
		setChordalDepth(pd);
		rhythmScale = rs;
		/*
		 * Add factors from the RhythmScale
		 */
		factors.addAll(rs.getFactors(units));
	}
	
	/**
	 * Creates a RhythmExpression for MONOPHONIC texture - METRIC or EXTRAMETRIC
	 * @param units
	 * @param exp
	 * @param rs
	 */
	public RhythmExpression(int units, Ratio exp, IRhythmScale rs) {
		this.units = units;
		ratio = exp;
		rhythmicUnitType = exp.equals(ONE_TO_ONE) ? RhythmicUnitType.METRIC : RhythmicUnitType.EXTRAMETRIC;
		textureType = TextureType.MONOPHONIC;
		rhythmScale = rs;
		/*
		 * Add factors from the RhythmScale
		 */
		factors.addAll(rs.getFactors(units));
	}
	
	/**
	 * Creates a RhythmExpression for MONOPHONIC texture - METRIC rhythmic unit type
	 * @param units
	 * @param rs
	 */
	public RhythmExpression(int units, IRhythmScale rs) {
		this(units, ONE_TO_ONE, rs);
	}
	
	public boolean isTuplet() {
		return !ratio.equals(ONE_TO_ONE);
	}

	/* (non-Javadoc)
	 * @see music.element.IRhythmExpression#getUnits()
	 */
	@Override
	public int getUnits() {
		return units;
	}

	/* (non-Javadoc)
	 * @see music.element.IRhythmExpression#setUnits(int)
	 */
	@Override
	public void setUnits(int units) {
		this.units = units;
	}

	/* (non-Javadoc)
	 * @see music.element.IRhythmExpression#getExpression()
	 */
	@Override
	public Ratio getRatio() {
		return ratio;
	}

	@Override
	public void setRatio(Ratio r) {
		ratio = r;
	}

	/* (non-Javadoc)
	 * @see music.element.IRhythmExpression#getTextureType()
	 */
	@Override
	public TextureType getTextureType() {
		return textureType;
	}

	/* (non-Javadoc)
	 * @see music.element.IRhythmExpression#getChordalDepth()
	 */
	@Override
	public Set<Integer> getChordalDepth() {
		return chordalDepth;
	}

	@Override
	public void setTextureType(TextureType expressionType) {
		this.textureType = expressionType;
	}

	public void setChordalDepth(Set<Integer> pd) {
		chordalDepth.addAll(pd);
	}
	
	public void setChordalDepth(int[] pd) {
		Arrays.stream(pd).forEach(s -> chordalDepth.add(s) );
	}
	
	public void setChordalDepth(int pd) {
		chordalDepth.add(pd);
	}

	@Override
	public RhythmicUnitType getRhythmicUnitType() {
		return rhythmicUnitType;
	}

	@Override
	public void setRhythmicUnitType(RhythmicUnitType rhythmicUnitType) {
		this.rhythmicUnitType = rhythmicUnitType;
	}

	@Override
	public IRhythmScale getRhythmScale() {
		return rhythmScale;
	}

	@Override
	public void setRhythmScale(IRhythmScale rhythmScale) {
		this.rhythmScale = rhythmScale;
	}
	
	@Override
	public List<Duration> getFactors() {
		return factors;
	}

	public String toString() {
		return toJson();
	}
}
