package org.dwbzen.music.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dwbzen.common.util.IJson;
import org.dwbzen.music.element.IRhythmExpression;
import org.dwbzen.music.element.IRhythmScale;
import org.dwbzen.music.element.TextureType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExpressionSelector implements Serializable, IJson, Comparable<Object> {

	private static final long serialVersionUID = 5907780760878594744L;
	protected static final Logger log = LogManager.getLogger(ExpressionSelector.class);

	@JsonIgnore	private IRhythmScale rhythmScale = null;
	@JsonIgnore	private ThreadLocalRandom random = ThreadLocalRandom.current();

	/**
	 * Probability of TextureType for a given #units
	 */
	@JsonProperty("textureProbabilityMap")	private Map<Integer, Map<TextureType, Double>> textureProbabilityMap = new HashMap<Integer, Map<TextureType, Double>>();
	
	/**
	 * Probabilities of METRIC and EXTRAMETRIC RhythmExpressions for a given #units
	 */
	@JsonProperty("rhythmicUnitTypeProbabilityMap")	private Map<Integer, Map<IRhythmExpression, Double>> rhythmicUnitTypeProbabilityMap = new HashMap<Integer, Map<IRhythmExpression, Double>>();
	
	@JsonProperty("tieAcrossBarline")		private double tieAcrossBarlineProbability = 0.0;	// configurable as music.instrument.<instrument>.tieAcrossBarline
	
	public ExpressionSelector(IRhythmScale rs) {
		rhythmScale = rs;
		rs.setExpressionSelector(this);
	}

	public TextureType selectTextureType(int units) {
		TextureType tt = null;
		 double rand = 0;
		 Map<TextureType, Double> pm = textureProbabilityMap.get(units);
		 if(pm == null) {
			 tt = TextureType.MONOPHONIC;	// sensible default
		 }
		 else {
			 if(pm.size() == 1) {
				 tt = pm.keySet().iterator().next();
			 }
			 else{
				 rand = random.nextDouble();
				 double cumProb = 0;
				 for(TextureType mtt : pm.keySet()) {
					 double prob = pm.get(mtt).doubleValue();
					 if(rand >= cumProb && rand < cumProb + prob) {
						 tt = mtt;
						 break;
					 }
					 cumProb += prob;
				 }
			 }
		}
		if(tt == null) {
			log.error("ExpressionSelector null TextureType " + rand + " units " + units );
		}
		return tt;
	}
	
	public IRhythmExpression selectRhythmExpression(int units, TextureType textureType) {
		IRhythmExpression re = null;
		double rand = 0;
		Map<IRhythmExpression, Double> rep = rhythmicUnitTypeProbabilityMap.get(units);
		if(rep == null) {
			// this should not happen unless RhythmScaleFactory has a bug
			throw new NullPointerException("No RhythmExpression for " + units + " units");
		}
		if(rep.size() == 1) {
			re = rep.keySet().iterator().next();
		}
		else {
			rand = random.nextDouble();
			double cumProb = 0;
			for(IRhythmExpression r : rep.keySet()) {
				if(r.getTextureType().equals(textureType)) {
					double prob = rep.get(r).doubleValue();
					if(rand >= cumProb && rand < cumProb + prob) {
						re = r;
						break;
					}
					cumProb += prob;
				}
			}
		}
		if(re == null) {
			log.error("NULL? " + rand + " units " + units );
		}
		return re;
	}
	
	public void setTextureTypeProbability(int units, TextureType tt, double prob) {
		 Map<TextureType, Double> ttp = textureProbabilityMap.get(units);
		 if(ttp == null) {
			 ttp = new HashMap<TextureType, Double>();
			 textureProbabilityMap.put(units, ttp);
		 }
		 ttp.put(tt, prob);
	}
	
	public Double getTextureTypeProbability(int units, TextureType tt) {
		Double prob = 0.0;
		Map<TextureType, Double> ttp = textureProbabilityMap.get(units);
		if(ttp != null && ttp.containsKey(tt)) {
			prob = ttp.get(tt);
		}
		
		return prob;
	}
	
	public void setRhythmicUnitTypeProbability(int units, IRhythmExpression re, double prob) {
		Map<IRhythmExpression, Double> rep = rhythmicUnitTypeProbabilityMap.get(units);
		if(rep == null) {
			rep = new HashMap<IRhythmExpression, Double>();
			rhythmicUnitTypeProbabilityMap.put(units, rep);
		}
		rep.put(re, prob);
	}
	
	public double getRhythmicUnitTypeProbability(int units, IRhythmExpression re) {
		Double prob = 0.0;
		Map<IRhythmExpression, Double> rep = rhythmicUnitTypeProbabilityMap.get(units);
		if(rep != null && rep.containsKey(re)) {
			prob = rep.get(re);
		}
		return prob;
	}

	public IRhythmScale getRhythmScale() {
		return rhythmScale;
	}

	public void setRhythmScale(IRhythmScale rhythmScale) {
		this.rhythmScale = rhythmScale;
	}

	public Map<Integer, Map<TextureType, Double>> getTextureProbabilityMap() {
		return textureProbabilityMap;
	}

	public Map<Integer, Map<IRhythmExpression, Double>> getRhythmicUnitTypeProbabilityMap() {
		return rhythmicUnitTypeProbabilityMap;
	}

	public double getTieAcrossBarlineProbability() {
		return tieAcrossBarlineProbability;
	}

	public void setTieAcrossBarlineProbability(double tieAcrossBarlineProbability) {
		this.tieAcrossBarlineProbability = tieAcrossBarlineProbability;
	}

	public int getNumberOfNotesInChord(IRhythmExpression rhythmExpression) {
		Object[] depthArray = rhythmExpression.getChordalDepth().toArray();
		int index = random.nextInt(depthArray.length);
		return (Integer)depthArray[index];
	}

	@Override
	public int compareTo(Object o) {
		ExpressionSelector selector = (ExpressionSelector)o;
		int s1 = selector.textureProbabilityMap.keySet().size();
		int s2 = textureProbabilityMap.keySet().size();
		return  (s1==s2) ? 0 : (s1 < s2) ? -1 : 1;
	}
	
	
}
