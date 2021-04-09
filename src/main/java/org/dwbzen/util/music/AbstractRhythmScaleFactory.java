package org.dwbzen.util.music;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dwbzen.music.action.ExpressionSelector;
import org.dwbzen.music.element.RhythmScale;
import org.dwbzen.music.element.rhythm.IRhythmTextureMap;
import org.dwbzen.util.Ratio;

/**
 * Each RhythmScale is created by a factory class that extends this abstract class
 * and implements the abstract methods as needed.<br>
 * This must include setting the standardBaseUnits if different than defaultBaseUnits (480).<br>
 * And creating the baseUnits SortedSet<Integer>.<br>
 * NOTE - the factorMap, which expresses a given number of units as a List<Duration> must include <br>
 * a factor for every multiple of the smallest element of the baseUnits list.<br>
 * A full explanation can be found in the RhythmScales.md readme file.
 * 
 * @author don_bacon
 *
 */
public abstract class AbstractRhythmScaleFactory  implements IRhythmScaleFactory {
	
	protected SortedSet<Integer> baseUnits = new TreeSet<Integer>();
	private int unitsPerMeasure = RhythmScale.defaultUnitsPerMeasure;
	private double metricProbability = 1.0;
	private boolean chordal = false;
	
	protected Map<Ratio, Double> extrametricProbabilityMap = new HashMap<Ratio, Double>();
	
	protected AbstractRhythmScaleFactory() {
	}

	
	@Override
	public RhythmScale createRhythmScale(String name) {
		RhythmScale rhythmScale = new RhythmScale();
		rhythmScale.setName(name);
		
		baseUnits = createBaseUnits(rhythmScale);
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
	 * Creates the base units for this RhythmScale. Max unit == rootMetric.<br>
	 * example for rootMetric = 480: {30, 60, 90, 180, 120, 150, 210, 240, 270, 300, 330, 360, 390, 420, 450, 480}
	 * 
	 * @param rhythmScale
	 * @return
	 */
	public abstract SortedSet<Integer> createBaseUnits(RhythmScale rhythmScale);
	
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
	
	public void addExtrametricExpressions(RhythmScale rhythmScale) {
		// no extrametric expressions by default. Override in derived classes.
	}
	
	public void addChordalExpressions(RhythmScale rhythmScale) {
		// no Chordal expressions by default. Override in derived classes.
	}
	
	public abstract ExpressionSelector createRhythmScaleSelector(RhythmScale rs);

	public void setUnitsPerMeasure(int unitsPerMeasure) {
		this.unitsPerMeasure = unitsPerMeasure;
	}
	
	public int  getUnitsPerMeasure() {
		return unitsPerMeasure;
	}

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
