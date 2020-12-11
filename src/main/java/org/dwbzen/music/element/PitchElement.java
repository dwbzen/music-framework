package org.dwbzen.music.element;

import org.dwbzen.common.util.IJson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class PitchElement implements IJson, Cloneable, IAdjustable  {

	public static enum PitchElementType { PITCH, PITCH_SET };
	@JsonProperty("pitchElementType")	protected PitchElementType pitchElementType;
	
	protected abstract void setPitchElementType();
	
	@JsonIgnore		public abstract boolean isOctaveNeutral();
	
	@JsonIgnore 	public abstract PitchElement getRetrograde();
	@JsonIgnore 	public abstract PitchElement getInversion();
	@JsonIgnore 	public abstract PitchElement getTransposition(int numberOfSteps);
	@JsonIgnore 	public abstract PitchElement getInversion(Pitch startingPitch);
	@JsonIgnore		public abstract void setOctave(int octave);
	
	public static PitchElement clone(PitchElement pe) { 
		PitchElement  clonedElement = pe.getPitchElementType() == PitchElementType.PITCH ?
				((Pitch)pe).clone() : 
				((PitchSet)pe).clone(); 
		return clonedElement; 
	} 
	
	public PitchElement() {
		setPitchElementType();
	}
	
	public PitchElementType getPitchElementType() {
		return pitchElementType;
	}
	
	public abstract int size();
}
