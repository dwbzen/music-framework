package util.music;

import java.util.List;
import java.util.Map;

import music.action.ExpressionSelector;
import music.element.IRhythmExpression;
import music.element.RhythmExpression;
import music.element.RhythmScale;
import music.element.RhythmicUnitType;
import music.element.TextureType;
import music.element.rhythm.IRhythmTextureMap;
import music.element.rhythm.RhythmTextureMap;
import util.Ratio;

/**
 * Standard16RhythmScaleFactory adds CHORDAL texture to Monophonic16StandardRhythmScaleFactory
 * 
 * @author bacond6
 *
 */
public class Standard16RhythmScaleFactory extends Monophonic16StandardRhythmScaleFactory {

	protected double monophonicProbability = 1.0;
	protected double chordalProbablility = 0.0;
	
	public Standard16RhythmScaleFactory() {
		super();
	}
	
	public static Standard16RhythmScaleFactory getInstance() {
		return new Standard16RhythmScaleFactory();
	}
	

	@Override
	IRhythmTextureMap createRhythmTextureMap(RhythmScale rhythmScale) {
		return new RhythmTextureMap();
	}

	@Override
	void addChordalExpressions(RhythmScale rhythmScale) {
		setChordal(true);
		rhythmScale.setChordal(true);
		Map<Integer, IRhythmTextureMap> expressions = rhythmScale.getExpressions();
		/*
		 * chordal expression includes chordal depth which gives the possible #notes in a chord
		 */
		int[] pd0 = {0};
		int[] pd1 = {2};
		int[] pd2 = {2,3};
		int[] pd4 = {2,3,4};
		int[] pd8 = {2, 3, 4, 5};
		int[] pd12 = {3,4,5};
		expressions.get(1).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(1, Ratio.ONE_TO_ONE, pd1, rhythmScale));
		expressions.get(2).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(2, Ratio.ONE_TO_ONE, pd2, rhythmScale));
		expressions.get(4).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(4, Ratio.ONE_TO_ONE, pd4, rhythmScale));
		expressions.get(4).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(2, Ratio.THREE_TO_TWO, pd4, rhythmScale));
		expressions.get(4).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(1, Ratio.FIVE_TO_FOUR, pd4, rhythmScale));
		
		RhythmExpression re6 = new RhythmExpression(6, Ratio.ONE_TO_ONE, pd4, rhythmScale);
		expressions.get(6).addRhythmExpression(TextureType.CHORDAL, re6);
		
		expressions.get(8).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(8, Ratio.ONE_TO_ONE, pd8, rhythmScale));
		expressions.get(8).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(4, Ratio.THREE_TO_TWO, pd8, rhythmScale));
		expressions.get(8).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(2, Ratio.FIVE_TO_FOUR, pd8, rhythmScale));
		
		RhythmExpression re12 = new RhythmExpression(12, Ratio.ONE_TO_ONE, pd12, rhythmScale);
		expressions.get(12).addRhythmExpression(TextureType.CHORDAL, re12);
		
		expressions.get(16).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(16, Ratio.ONE_TO_ONE,  pd12, rhythmScale));
		expressions.get(16).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(8, Ratio.THREE_TO_TWO,  pd12, rhythmScale));
		expressions.get(16).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(4, Ratio.FIVE_TO_FOUR, pd12, rhythmScale));
		
		/*
		 * units with no chordal expression
		 */
		RhythmExpression re3 = new RhythmExpression(3, Ratio.ONE_TO_ONE, pd0, rhythmScale);
		expressions.get(3).addRhythmExpression(TextureType.CHORDAL, re3);
		expressions.get(10).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(10, Ratio.ONE_TO_ONE, pd0, rhythmScale));
		RhythmExpression re14 = new RhythmExpression(14, Ratio.ONE_TO_ONE, pd0, rhythmScale);
		expressions.get(14).addRhythmExpression(TextureType.CHORDAL, re14);
	}

	@Override
	/**
	 * Creates an ExpressionSelector for standard rhythm scale.
	 * Set CHORDAL texture probablilities to the chordalProbability default value
	 * Set MONOPONIC texture probablilities to the monophonicProbability default value
	 * Either can be changed by instrument configuration.
	 * 
	 * @param RhythmScale rhythmScale
	 */
	ExpressionSelector createRhythmScaleSelector(RhythmScale rhythmScale) {
		ExpressionSelector selector = new ExpressionSelector(rhythmScale);
		Map<Integer, IRhythmTextureMap> expressions = rhythmScale.getExpressions();
		
		for(Integer units : rhythmScale.getBaseUnits()) {
			selector.setTextureTypeProbability(units, TextureType.MONOPHONIC, monophonicProbability);
			selector.setTextureTypeProbability(units, TextureType.CHORDAL, chordalProbablility);
	
			setMonophonicExpressionSelector(selector, expressions, units);
			
			setChordalExpressionSelector(selector, expressions, units);
		}
		return selector;
	}

	void setChordalExpressionSelector(ExpressionSelector selector, Map<Integer, IRhythmTextureMap> expressions, Integer units) {
		List<IRhythmExpression> exps = expressions.get(units).getRhythmExpressions(TextureType.CHORDAL);
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
		return;
	}

	public double getMonophonicProbability() {
		return monophonicProbability;
	}

	public void setMonophonicProbability(double monophonicProbability) {
		this.monophonicProbability = monophonicProbability;
	}

	public double getChordalProbablility() {
		return chordalProbablility;
	}

	public void setChordalProbablility(double chordalProbablility) {
		this.chordalProbablility = chordalProbablility;
	}

	public static void main(String[] args) {
		//Standard16RhythmScaleFactory factory = Standard16RhythmScaleFactory.getInstance();
		//RhythmScale rs1 = factory.createRhythmScale("Standard Rhythm Scale");
		//System.out.println(rs1.toJSON());
		
		IRhythmScaleFactory rsFactory = RhythmScaleFactory.getRhythmScaleFactory("Standard16RhythmScale");
		RhythmScale rs = rsFactory.createRhythmScale("Standard Rhythm Scale");
		System.out.println(rs.toJson(true));
	}
}
