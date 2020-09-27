package org.dwbzen.util.music;

import java.util.List;
import java.util.Map;

import org.dwbzen.music.action.ExpressionSelector;
import org.dwbzen.music.element.IRhythmExpression;
import org.dwbzen.music.element.RhythmExpression;
import org.dwbzen.music.element.RhythmScale;
import org.dwbzen.music.element.RhythmicUnitType;
import org.dwbzen.music.element.TextureType;
import org.dwbzen.music.element.rhythm.IRhythmTextureMap;
import org.dwbzen.music.element.rhythm.RhythmTextureMap;
import org.dwbzen.util.Ratio;

/**
 * PolyphonicRhythmScaleFactory adds CHORDAL texture to StandardRhythmScaleFactory
 * 
 * @author don_bacon
 *
 */
public class PolyphonicRhythmScaleFactory extends StandardRhythmScaleFactory {
	
	public PolyphonicRhythmScaleFactory() {
		super();
		setChordalProbablility(0.6);
		setMonophonicProbability(0.4);
	}
	
	public static PolyphonicRhythmScaleFactory getInstance() {
		return new PolyphonicRhythmScaleFactory();
	}
	

	@Override
	IRhythmTextureMap createRhythmTextureMap(RhythmScale rhythmScale) {
		return new RhythmTextureMap();
	}

	@Override
	public void addChordalExpressions(RhythmScale rhythmScale) {
		setChordal(true);
		rhythmScale.setChordal(true);
		Map<Integer, IRhythmTextureMap> expressions = rhythmScale.getExpressions();
		/*
		 * chordal expression includes chordal depth which gives the possible #notes in a chord
		 */
		int[] pd1 = {2};
		int[] pd2 = {2, 3};
		int[] pd4 = {2, 3, 4};
		int[] pd8 = {2, 3, 4, 5};
		int[] pd12 = {3, 4, 5};
		expressions.get(60).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(60, Ratio.ONE_TO_ONE, pd1, rhythmScale));
		expressions.get(120).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(120, Ratio.ONE_TO_ONE, pd2, rhythmScale));
		expressions.get(180).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(180, Ratio.ONE_TO_ONE, pd2, rhythmScale));
		expressions.get(240).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(240, Ratio.ONE_TO_ONE, pd4, rhythmScale));
		
		expressions.get(240).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(240, Ratio.ONE_TO_ONE, pd12, rhythmScale));
		expressions.get(360).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(360, Ratio.ONE_TO_ONE,  pd8, rhythmScale));
		expressions.get(420).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(420, Ratio.ONE_TO_ONE,  pd4, rhythmScale));
		
		expressions.get(480).addRhythmExpression(TextureType.CHORDAL, new RhythmExpression(480, Ratio.ONE_TO_ONE,  pd12, rhythmScale));

	}

	@Override
	/**
	 * Creates an ExpressionSelector for standard rhythm scale.
	 * Set CHORDAL texture probabilities to the chordalProbability default value.
	 * Set MONOPONIC texture probabilities to the monophonicProbability default value.<br>
	 * Either can be changed by instrument configuration.
	 * 
	 * @param RhythmScale rhythmScale
	 */
	public ExpressionSelector createRhythmScaleSelector(RhythmScale rhythmScale) {
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

	public double getChordalProbablility() {
		return chordalProbablility;
	}

	public void setChordalProbablility(double chordalProbablility) {
		this.chordalProbablility = chordalProbablility;
	}

	public static void main(String[] args) {
		
		IRhythmScaleFactory rsFactory = RhythmScaleFactory.getRhythmScaleFactory("PolyphonicRhythmScale");
		RhythmScale rs = rsFactory.createRhythmScale("PolyphonicRhythmScale");
		System.out.println(rs.toJson(true));
	}
}
