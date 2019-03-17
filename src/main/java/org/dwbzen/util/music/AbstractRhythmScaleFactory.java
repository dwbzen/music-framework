package org.dwbzen.util.music;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.dwbzen.music.action.ExpressionSelector;
import org.dwbzen.music.element.RhythmScale;
import org.dwbzen.music.element.rhythm.IRhythmTextureMap;
import org.dwbzen.util.Ratio;

/**
 * Each RhythmScale is created by a factory class that extends this abstract class
 * and implements the abstract methods as needed.
 * 
 * @author bacond6
 *
 */
public abstract class AbstractRhythmScaleFactory  implements IRhythmScaleFactory {

	protected SortedSet<Integer> baseUnits = null;
	protected double metricProbability = 0.8;
	protected Map<Ratio, Double> extrametricProbabilityMap = new HashMap<Ratio, Double>();
	protected boolean chordal = false;
	
	protected AbstractRhythmScaleFactory() {
		
	}

	
	@Override
	public RhythmScale createRhythmScale(String name) {
		RhythmScale rhythmScale = new RhythmScale();
		rhythmScale.setName(name);
		
		SortedSet<Integer> baseUnits = createBaseUnits(rhythmScale);
		rhythmScale.getBaseUnits().addAll(baseUnits);
		rhythmScale.setRange(baseUnits.last() - baseUnits.first());
		/*
		 * factor map must be created prior to any RhythmicExpressions
		 */
		createFactorMap(rhythmScale);
		
		SortedSet<Integer> bunits = rhythmScale.getBaseUnits();
		int range = bunits.last() - bunits.first();
		rhythmScale.setRange(range);
		
		createMetricExpressions(bunits, rhythmScale);
		
		addExtrametricExpressions(rhythmScale);
		
		addChordalExpressions(rhythmScale);
		
		createRhythmScaleSelector(rhythmScale);
		return rhythmScale;
	}

	/**
	 * Creates the base units for this RhythmScale. Max unit == rootMetric.
	 * example for rootMetric = 16: {1, 2, 3, 4, 6, 8, 10, 12, 14, 16}
	 * 
	 * @param rhythmScale
	 * @return
	 */
	abstract SortedSet<Integer> createBaseUnits(RhythmScale rhythmScale);
	
	abstract void createFactorMap(RhythmScale rhythmScale);

	
	/**
	 * Create the appropriate IRhythmTextureMap instance:
	 * BaseRhythmTextureMap - for MONOPHONIC texture only
	 * RhythmTextureMap - for CHORDAL and MONOPHONIC textures
	 * 
	 * @param rhythmScale
	 * @return IRhythmTextureMap
	 */
	abstract IRhythmTextureMap createRhythmTextureMap(RhythmScale rhythmScale);
	
	/**
	 * Create metric Expressions for this RhythmScale using the appropriate IRhythmTextureMap instance
	 * 
	 * @param baseUnits
	 * @param rhythmScale
	 * @param textureMap
	 */
	abstract void createMetricExpressions(SortedSet<Integer> baseUnits, RhythmScale rhythmScale);
	
	abstract void addExtrametricExpressions(RhythmScale rhythmScale);
	
	abstract void addChordalExpressions(RhythmScale rhythmScale);
	
	abstract ExpressionSelector createRhythmScaleSelector(RhythmScale rs);


	public double getMetricProbability() {
		return metricProbability;
	}

	public void setMetricProbability(double mp) {
		metricProbability = mp;
	}

	public Map<Ratio, Double> getExtrametricProbabilityMap() {
		return extrametricProbabilityMap;
	}

	public void setExtrametricProbabilityMap(Ratio ratio, double prob) {
		extrametricProbabilityMap.put(ratio, prob);
	}

	public SortedSet<Integer> getBaseUnits() {
		return baseUnits;
	}


	public boolean isChordal() {
		return chordal;
	}


	public void setChordal(boolean chordal) {
		this.chordal = chordal;
	}
	
}
