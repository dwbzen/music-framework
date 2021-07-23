package org.dwbzen.util.music;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dwbzen.music.action.ExpressionSelector;
import org.dwbzen.music.element.Duration;
import org.dwbzen.music.element.IRhythmExpression;
import org.dwbzen.music.element.Note;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.RhythmExpression;
import org.dwbzen.music.element.RhythmScale;
import org.dwbzen.music.element.RhythmicUnitType;
import org.dwbzen.music.element.TextureType;
import org.dwbzen.music.element.rhythm.BaseRhythmTextureMap;
import org.dwbzen.music.element.rhythm.IRhythmTextureMap;
import org.dwbzen.util.Ratio;

/**
 * A slimmed-down version of Monophonic16StandardRhythmScaleFactory that includes only whole, half, quarter, eighth, <br>
 * dotted quarter and eighth, double-dotted half, and half + eighth tie.<br>
 * No extrametric expressions (triplets, 5-tuplets) or tripple-dotted notes are included.
 *
 * @author don_bacon
 *
 */
public class StandardRhythmScaleFactory extends AbstractRhythmScaleFactory {

	public final static int[] standardBaseUnits = {60, 120, 180, 240, 300, 360, 420, 480};
	
	protected double monophonicProbability = 1.0;
	protected double chordalProbablility = 0.0;
	
	public StandardRhythmScaleFactory() {
		super();
	}
	
	public static StandardRhythmScaleFactory getInstance() {
		return new StandardRhythmScaleFactory();
	}
	
	@Override
	public SortedSet<Integer> createBaseUnits(RhythmScale rhythmScale) {
		baseUnits = new TreeSet<Integer>();
		Arrays.stream(standardBaseUnits).forEach(s -> baseUnits.add(s));
		rhythmScale.setRoot(RhythmScale.defaultUnitsPerMeasure);		// 480
		return baseUnits;
	}

	@Override
	void createFactorMap(RhythmScale rhythmScale) {
		rhythmScale.addFactor(60, new Duration(60));		// EIGHTH
		rhythmScale.addFactor(120, new Duration(120));		// QUARTER
		rhythmScale.addFactor(240, new Duration(240));		// HALF
		rhythmScale.addFactor(480, new Duration(480));		// WHOLE
		
		rhythmScale.addFactor(180, new Duration(120, 1));	// dotted QUARTER
		rhythmScale.addFactor(360, new Duration(240, 1));	// dotted HALF
		rhythmScale.addFactor(420, new Duration(240, 2));	// double-dotted HALF
		/*
		 * factors representing ties between 2 or more notes
		 */
		rhythmScale.addFactor(300, new Duration(240)).add(new Duration(60));		// 300 = half + eighth
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
	public void addExtrametricExpressions(RhythmScale rhythmScale) {
		Map<Integer, IRhythmTextureMap> expressions = rhythmScale.getExpressions();
		
		expressions.get(240).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(120, Ratio.THREE_TO_TWO, rhythmScale));	// quarter
		expressions.get(120).addRhythmExpression(TextureType.MONOPHONIC, new RhythmExpression(60, Ratio.THREE_TO_TWO, rhythmScale));	// eighth
	}

	@Override
	public ExpressionSelector createRhythmScaleSelector(RhythmScale rhythmScale) {
		ExpressionSelector selector = new ExpressionSelector(rhythmScale);
		Map<Integer, IRhythmTextureMap> expressions = rhythmScale.getExpressions();

		// set MONOPONIC texture probabilities to 1 since there are no chords
		for(Integer units : rhythmScale.getBaseUnits()) {
			selector.setTextureTypeProbability(units, TextureType.MONOPHONIC, 1.0);
			// 
			setMonophonicExpressionSelector(selector, expressions, units);
		}
		return selector;
	}

	/**
	 * Assigns probabilities to METRIC expressions, and EXTRAMETRIC for 240 units (3 quarter notes in the time of 2)
	 * @param selector
	 * @param expressions
	 * @param units
	 */
	protected void setMonophonicExpressionSelector(ExpressionSelector selector, Map<Integer, IRhythmTextureMap> expressions, Integer units) {
		List<IRhythmExpression> exps = expressions.get(units).getRhythmExpressions(TextureType.MONOPHONIC);
		for(IRhythmExpression unitExpression : exps) {
			if(units%60 == 0) {		// eighth, quarter, half, whole notes
				if(unitExpression.getRhythmicUnitType().equals(RhythmicUnitType.METRIC)) {
					selector.setRhythmicUnitTypeProbability(units, unitExpression, 0.8);
				}
				else { // EXTRAMETRIC
					int nnotes = unitExpression.getRatio().getNumberOfNotes();
					double prob = (nnotes==3) ? 0.2 : 0.1;
					selector.setRhythmicUnitTypeProbability(units, unitExpression, prob);
				}
			}
			else {
				selector.setRhythmicUnitTypeProbability(units, unitExpression, 1.0);
			}
		}	
	}

	public double getMonophonicProbability() {
		return monophonicProbability;
	}

	public void setMonophonicProbability(double monophonicProbability) {
		this.monophonicProbability = monophonicProbability;
	}
	
	public static void main(String[] args) {
		
		IRhythmScaleFactory rsFactory = RhythmScaleFactory.getRhythmScaleFactory("StandardRhythmScale");
		RhythmScale rs = rsFactory.createRhythmScale("StandardRhythmScale");
		// System.out.println(rs.toJson(true));
		int rootUnits = rs.getRoot();
		Set<Integer> baseUnits = rs.getBaseUnits();
		Note note = null;
		for(Integer i : baseUnits) {
			Duration dur = new Duration(i);
			note = new Note(Pitch.A, dur);
			String nt = RhythmScale.determineNoteType(note, rootUnits);
			System.out.println("units: " + i + " note type: " + nt);
		}
	}
}
