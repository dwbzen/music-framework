package org.dwbzen.util.music;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dwbzen.music.action.ExpressionSelector;
import org.dwbzen.music.element.Duration;
import org.dwbzen.music.element.IRhythmExpression;
import org.dwbzen.music.element.RhythmExpression;
import org.dwbzen.music.element.RhythmScale;
import org.dwbzen.music.element.RhythmicUnitType;
import org.dwbzen.music.element.TextureType;
import org.dwbzen.music.element.rhythm.BaseRhythmTextureMap;
import org.dwbzen.music.element.rhythm.IRhythmTextureMap;
import org.dwbzen.music.transform.ITransformer.Preference;
import org.dwbzen.util.Ratio;

public class Monophonic16StandardRhythmScaleFactory  extends AbstractRhythmScaleFactory {
	
	protected final static int standardRoot = 480;
	public final static int[] standardBaseUnits = 
		{30, 60, 90, 180, 120, 150, 210, 240, 270, 300, 330, 360, 390, 420, 450, 480};
	
	public Monophonic16StandardRhythmScaleFactory() {
		super();
	}
	
	public static Monophonic16StandardRhythmScaleFactory getInstance() {
		return new Monophonic16StandardRhythmScaleFactory();
	}
	private boolean debugFlag = false;
	
	@Override
	SortedSet<Integer> createBaseUnits(RhythmScale rhythmScale) {
		SortedSet<Integer> baseUnits = new TreeSet<Integer>();
		Arrays.stream(standardBaseUnits).forEach(s -> baseUnits.add(s));
		rhythmScale.setRoot(standardRoot);
		return baseUnits;
	}

	@Override
	/**
	 * Create the factor map for ALL units in the range, even if not represented
	 * in the base units. Why - because may need to fill out a measure with remaining
	 * units that may not be in the base units.<br>
	 * This takes the place of real-time factoring units into Duration.<br>
	 * For example, 450 units = 360 + 90 or dotted half + dotted eighth OR
	 * 420 + 30, double-dotted half + eighth. The RhythmScale designer makes the call.
	 * Either way, the factorMap for 450 units would have 2 Duration elements in the List
	 * <p>
	 * Duration.durationUnits includes units from dots (i.e. total effective duration)
	 * For example, new Duration(8, 2) = 60 + 30 + 15 = 105 (double-dotted 8th)
	 * 
	 */
	void createFactorMap(RhythmScale rs) {
		rs.addFactor(30, new Duration(30));		// SIXTEENTH
		rs.addFactor(60, new Duration(60));		// EIGHTH
		rs.addFactor(120, new Duration(120));	// QUARTER
		rs.addFactor(240, new Duration(240));	// HALF
		rs.addFactor(480, new Duration(480));	// WHOLE
		
		rs.addFactor(90, new Duration(60, 1));		// dotted EIGHTH
		rs.addFactor(180, new Duration(120, 1));	// dotted QUARTER
		rs.addFactor(360, new Duration(240, 1));	// dotted HALF
		
		rs.addFactor(210, new Duration(120, 2));		// double dotted QUARTER
		rs.addFactor(420, new Duration(240, 2));		// double dotted HALF
		
		rs.addFactor(450, new Duration(240, 3));		// triple dotted HALF
		/*
		 * factors representing ties between 2 notes
		 */
		rs.addFactor(150, new Duration(120)).add(new Duration(30));		// 150
		rs.addFactor(270, new Duration(120,1)).add(new Duration(60,1));	// 270
		rs.addFactor(300, new Duration(240)).add(new Duration(60));		// 300
		rs.addFactor(330, new Duration(240)).add(new Duration(60,1));	// 330
		rs.addFactor(390, new Duration(240,1)).add(new Duration(30)); 	// 390
	}

	@Override
	IRhythmTextureMap createRhythmTextureMap(RhythmScale rhythmScale) {
		return new BaseRhythmTextureMap();
	}

	@Override
	void createMetricExpressions(SortedSet<Integer> baseUnits, RhythmScale rhythmScale) {
		Map<Integer, IRhythmTextureMap> expressions = rhythmScale.getExpressions();
		/*
		 * METRIC expression
		 */
		for(Integer units : baseUnits) {
			IRhythmTextureMap textureMap =  createRhythmTextureMap(rhythmScale);
			RhythmExpression re = new RhythmExpression(units, rhythmScale);		// also adds the factors
			textureMap.addRhythmExpression(TextureType.MONOPHONIC, re);
			expressions.put(units, textureMap );
		}
	}
	
	@Override
	void addExtrametricExpressions(RhythmScale rhythmScale) {
		Map<Integer, IRhythmTextureMap> expressions = rhythmScale.getExpressions();
		/*
		 * EXTRAMETRIC expression
		 */
		expressions.get(60).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(30, Ratio.THREE_TO_TWO, rhythmScale));		// 16th
		expressions.get(120).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(30, Ratio.FIVE_TO_FOUR, rhythmScale));
		
		RhythmExpression re120_3in2 =  new RhythmExpression(60, Ratio.THREE_TO_TWO, rhythmScale);										// 8th
		expressions.get(120).addRhythmExpression(TextureType.MONOPHONIC, re120_3in2);		
		expressions.get(240).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(60, Ratio.FIVE_TO_FOUR, rhythmScale));
		
		expressions.get(240).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(120, Ratio.THREE_TO_TWO, rhythmScale));	// quarter
		expressions.get(480).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(120, Ratio.FIVE_TO_FOUR, rhythmScale));
		
		expressions.get(480).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(240, Ratio.THREE_TO_TWO, rhythmScale));	// half
	}
	
	@Override
	void addChordalExpressions(RhythmScale rhythmScale) {
		// Nothing to do - Override in derived classes
	}
	
	@Override
	/**
	 * Creates an ExpressionSelector for standard MONOPHONIC rhythm scale (no chords)
	 * TODO - need a better way to do this
	 * @param RhythmScale rhythmScale
	 */
	ExpressionSelector createRhythmScaleSelector(RhythmScale rhythmScale) {
		ExpressionSelector selector = new ExpressionSelector(rhythmScale);
		Map<Integer, IRhythmTextureMap> expressions = rhythmScale.getExpressions();

		// set MONOPONIC texture probabilities to 1
		for(Integer units : rhythmScale.getBaseUnits()) {
			selector.setTextureTypeProbability(units, TextureType.MONOPHONIC, 1.0);
			// 
			setMonophonicExpressionSelector(selector, expressions, units);
		}
		return selector;
	}

	/**
	 * Assigns probabilities to METRIC expressions, and EXTRAMETRIC for 4, 8, 16 units
	 * @param selector
	 * @param expressions
	 * @param units
	 */
	protected void setMonophonicExpressionSelector(ExpressionSelector selector, Map<Integer, IRhythmTextureMap> expressions, Integer units) {
		List<IRhythmExpression> exps = expressions.get(units).getRhythmExpressions(TextureType.MONOPHONIC);
		for(IRhythmExpression unitExpression : exps) {
			if(units%60 == 0) {		// 8th, quarter, half, whole notes
				if(unitExpression.getRhythmicUnitType().equals(RhythmicUnitType.METRIC)) {
					selector.setRhythmicUnitTypeProbability(units, unitExpression, .6);
				}
				else {	// EXTRAMETRIC
					int nnotes = unitExpression.getRatio().getNumberOfNotes();
					double prob = (nnotes==3) ? 0.4 : 0.2;
					selector.setRhythmicUnitTypeProbability(units, unitExpression, prob);
				}
			}
			else {
				selector.setRhythmicUnitTypeProbability(units, unitExpression, 1.0);
			}
		}
		if(debugFlag) {
			System.out.println(selector.toJson());
		}	
	}

	public static void main(String[] args) {
		
		IRhythmScaleFactory rsFactory = RhythmScaleFactory.getRhythmScaleFactory("Monophonic16StandardRhythmScale");
		RhythmScale rs = rsFactory.createRhythmScale("Rhythm Scale - no chords");

		System.out.println(rs.toJson(true));

	}
	
}
