package music.action;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import music.element.IRhythmExpression;
import music.element.IRhythmScale;
import music.element.TextureType;

public class ExpressionSelector {
	private IRhythmScale rhythmScale = null;
	private ThreadLocalRandom random = ThreadLocalRandom.current();
	protected static final Logger log = LogManager.getLogger(ExpressionSelector.class);

	/**
	 * Probability of TextureType for a given #units
	 */
	private Map<Integer, Map<TextureType, Double>> textureProbabilityMap = new HashMap<Integer, Map<TextureType, Double>>();
	
	/**
	 * Probabilities of METRIC and EXTRAMETRIC RhythmExpressions for a given #units
	 */
	private Map<Integer, Map<IRhythmExpression, Double>> rhythmicUnitTypeProbabilityMap = new HashMap<Integer, Map<IRhythmExpression, Double>>();
	
	private double tieAcrossBarlineProbability = 0.0;	// configurable as music.instrument.<instrument>.tieAcrossBarline
	
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
			log.error("NULL? " + rand + " units " + units );
		}
		return tt;
	}
	
	public IRhythmExpression selectRhythmExpression(int units) {
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
				double prob = rep.get(r).doubleValue();
				if(rand >= cumProb && rand < cumProb + prob) {
					re = r;
					break;
				}
				cumProb += prob;
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
	
	
}
