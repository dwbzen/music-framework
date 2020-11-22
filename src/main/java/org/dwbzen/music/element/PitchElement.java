package org.dwbzen.music.element;

import org.dwbzen.common.util.IJson;

public abstract class PitchElement implements IJson, Cloneable, IAdjustable  {

	public static enum PitchElementType { PITCH, PITCH_SET };
	protected PitchElementType pitchElementType;
	
	protected abstract void setPitchElementType();
	
	public abstract boolean isOctaveNeutral();
	
	public abstract PitchElement getRetrograde();
	public abstract PitchElement getInversion();
	public abstract PitchElement getTransposition(int numberOfSteps);
	public abstract PitchElement getInversion(Pitch startingPitch);
	
	
	public static PitchElement clone(PitchElement pe) {
		PitchElement clonedElement = pe.getPitchElementType() == PitchElementType.PITCH ?
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
}
