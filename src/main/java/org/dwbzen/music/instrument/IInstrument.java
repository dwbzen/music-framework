package org.dwbzen.music.instrument;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import org.dwbzen.music.element.Cleff;
import org.dwbzen.music.element.IRhythmScale;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Key.Mode;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.PitchClass;
import org.dwbzen.music.element.PitchRange;
import org.dwbzen.common.util.INameable;

public interface IInstrument extends INameable, Serializable {

	List<Cleff> getCleffs();

	String getName();
	void setName(String instrumentName);
	
	PitchRange getPitchRange();
	void setPitchRange(PitchRange range);
	
	Key getKey();
	void setKey(Key key);
	
	public String getInstrumentName();
	public void setInstrumentName(String instrumentName);

	public String getInstrumentSound();
	public void setInstrumentSound(String instrumentSound);

	public String getVirtualName();
	public void setVirtualName(String virtualName);

	boolean isTransposes();
	void setTransposes(boolean transposes);
	
	PitchClass getPitchClass();
	void setPitchClass(PitchClass pc);
	
	/**
	 * Gets the Key of this instrument relative to the Key of the score (or measure)
	 * For non-transposing instrument, this is just the score Key.
	 * For transposing instrument the key is score Key at the instrument Key.
	 * For example, the key for a Bb clarinet scored in G-minor is A-minor
	 * i.e. the root of G-minor key raised a step.
	 * @param scoreKey
	 * @return adjusted Key for transposes instrument or scoreKey otherwise
	 */
	default Key getKey(Key scoreKey) {
		Key key = scoreKey;
		if(isTransposes()) {
			Integer p = scoreKey.getRoot().getStep().getValue().intValue() + getTranspositionSteps();
			Mode m = scoreKey.getMode();
			key = (m.equals(Mode.MAJOR)) ? Key.majorKeys.get(p) : Key.minorKeys.get(p);
		}
		return key;
	}
	
	default int getTranspositionSteps() {
		// defaults to non-transposing
		return 0;
	}
	
	default void establishKey() {
		setKey(Key.C_MAJOR);
	}
	
	
	public static PitchRange getConfiguredPitchRange(Properties configProperties, String classname) {
		PitchRange pr = null;
		String range = configProperties.getProperty(classname + ".range");
		if(range != null && range.length()>0) {
			String[] ra = range.split(",");
			Pitch low = new Pitch(ra[0]);
			Pitch high = new Pitch(ra[1]);
			pr=new PitchRange(low,high);
		}
		return pr;
	}
	
	IRhythmScale getRhythmScale();
	void setRhythmScale(IRhythmScale rhythmScale);
	
	
}
