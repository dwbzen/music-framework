package util.music;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import music.action.ExpressionSelector;
import music.element.Duration;
import music.element.IRhythmExpression;
import music.element.RhythmExpression;
import music.element.RhythmScale;
import music.element.RhythmicUnitType;
import music.element.TextureType;
import music.element.rhythm.BaseRhythmTextureMap;
import music.element.rhythm.IRhythmTextureMap;
import util.Ratio;

public class Monophonic16StandardRhythmScaleFactory  extends AbstractRhythmScaleFactory {
	
	protected final static int standardRoot = 16;
	public final static int[] standardBaseUnits = {1, 2, 3, 4, 6, 8, 10, 12, 14, 16};
	
	public Monophonic16StandardRhythmScaleFactory() {
		super();
	}
	
	public static Monophonic16StandardRhythmScaleFactory getInstance() {
		return new Monophonic16StandardRhythmScaleFactory();
	}
	
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
	 * units that may not be in the base units (like 13 or 15).
	 * This takes the place of real-time factoring units into Duration.
	 * For example, 15 units = 12 + 3 or dotted half + dotted eighth OR
	 * 14 + 1, double-dotted half + eighth. The RhythmScale designer makes the call.
	 * Either way, the factorMap for 15 units would have 2 Duration elements in the List
	 * 
	 * Duration.durationUnits includes units from dots (i.e. total effective duration)
	 * For example, new Duration(8, 2) = 8 + 4 + 2 = 14
	 * 
	 */
	void createFactorMap(RhythmScale rs) {
		rs.addFactor(1, new Duration(1));
		rs.addFactor(2, new Duration(2));
		rs.addFactor(3, new Duration(2,1));
		rs.addFactor(4, new Duration(4));
		rs.addFactor(6, new Duration(4, 1));
		rs.addFactor(8, new Duration(8));
		rs.addFactor(10, new Duration(8)).add(new Duration(2));
		rs.addFactor(12, new Duration(8, 1));
		rs.addFactor(14, new Duration(8, 2));
		rs.addFactor(16, new Duration(16));
		/*
		 * units not in the base units
		 */
		rs.addFactor(5, new Duration(4)).add(new Duration(1));
		rs.addFactor(7, new Duration(4,2));
		rs.addFactor(9, new Duration(4,1)).add(new Duration(2,1));
		rs.addFactor(11, new Duration(8)).add(new Duration(2,1));
		rs.addFactor(13, new Duration(4,2)).add(new Duration(4,1)); 
		rs.addFactor(15, new Duration(8)).add(new Duration(4,2));
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
		expressions.get(4).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(2, Ratio.THREE_TO_TWO, rhythmScale));
		expressions.get(4).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(1, Ratio.FIVE_TO_FOUR, rhythmScale));
		expressions.get(8).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(4, Ratio.THREE_TO_TWO, rhythmScale));
		expressions.get(8).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(2, Ratio.FIVE_TO_FOUR, rhythmScale));
		expressions.get(16).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(8, Ratio.THREE_TO_TWO, rhythmScale));
		expressions.get(16).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(4, Ratio.FIVE_TO_FOUR, rhythmScale));
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

		// set MONOPONIC texture probablilities to 1
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
			if(units == 4 || units == 8 || units == 16) {
				if(unitExpression.getRhythmicUnitType().equals(RhythmicUnitType.METRIC)) {
					selector.setRhythmicUnitTypeProbability(units, unitExpression, .8);
				}
				else {	// EXTRAMETRIC
					int nnotes = unitExpression.getRatio().getNumberOfNotes();
					double prob = (nnotes==3) ? 0.15 : 0.05;
					selector.setRhythmicUnitTypeProbability(units, unitExpression, prob);
				}
			}
			else {
				selector.setRhythmicUnitTypeProbability(units, unitExpression, 1.0);
			}
		}
	}

	public static void main(String[] args) {
		
		Monophonic16StandardRhythmScaleFactory factory = Monophonic16StandardRhythmScaleFactory.getInstance();
		RhythmScale rs = factory.createRhythmScale("Rhythm Scale - no chords");
		System.out.println(rs.toJSON());
		
		IRhythmScaleFactory rsFactory = RhythmScaleFactory.getRhythmScaleFactory("Monophonic16StandardRhythmScale");
		rs = rsFactory.createRhythmScale("Rhythm Scale - no chords");
		System.out.println(rs.toJSON());
		
		List<Duration> factors13 = rs.getFactors(13);
		for(Duration d : factors13) {
			System.out.println(d.toJSON());
		}
	}
	
}
