package music.transform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import music.element.Measure;
import music.element.PitchRange;
import music.element.Score;
import music.element.ScorePartEntity;
import music.instrument.Instrument;

import org.apache.log4j.Logger;
/**
 * Gathers up Notes in a measure into a Chord having the same overall duration.
 * Sort of a reverse arpeggiator (which creates a melodic line
 * or arpeggio from a Chord).
 * 
 * TODO complete this class
 * @author don_bacon
 *
 */
public class ChordTransformer extends Transformer  {
	protected static final org.apache.log4j.Logger log = Logger.getLogger(ChordTransformer.class);

	private 	PitchRange pitchRange = null;	// set for each Instrument

	@Override
	public void transform(Layer layer) {
		Score score = layer.getScore();
		Map<String, ScorePartEntity> scoreParts = null;
		ScorePartEntity scorePartEntity = null;
		if(score != null) {	// if Score layer
			scoreParts = score.getParts();
		}
		else {				// if ScorePartEntity layer
			scoreParts = new HashMap<String, ScorePartEntity>();
			scorePartEntity = layer.getScorePartEntity();
			scoreParts.put(scorePartEntity.getPartName(), scorePartEntity);
		}
		for(String partname : scoreParts.keySet()) {
			scorePartEntity = scoreParts.get(partname);
			Instrument instrument = scorePartEntity.getInstrument();
			/*
			 * Check if transformer is instrument-specific
			 */
			if(getInstrument() != null && !getInstrument().equals(instrument)) {
				continue;	// to next part as this transformer doesn't apply
			}
			pitchRange = instrument.getPitchRange();

			List<Measure> measures = scorePartEntity.getMeasures();
			int len = measures.size();
			log.info("transforming " + partname + " " + len + " measures.");
			/*
			 * This creates new Measure instance with Notes "gathered up" into a Chord
			 * 
			 */
			for(Measure measure : measures) {
				// TODO finish this code
			}
		}
		
	}

	@Override
	public void configure(Properties props) {
		// TODO Auto-generated method stub
		
	}

}
