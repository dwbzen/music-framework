package org.dwbzen.util.music;

import java.util.ArrayList;
import java.util.List;

import org.dwbzen.common.util.IJson;
import org.dwbzen.music.element.PitchElement;

import com.fasterxml.jackson.annotation.JsonProperty;


public class PitchCollection implements IJson, Cloneable {
	
	@JsonProperty	private List<PitchElement> pitchElements = new ArrayList<>();
	
	public PitchCollection() {
	}
	
	public int size() {
		return pitchElements.size();
	}
	
	/**
	 * Adjusts (transposes) each PitchElement by the number of steps indicated.
	 * @param numberOfSteps
	 */
	public void transpose(int numberOfSteps) {
		for(PitchElement p : pitchElements) {
			p.adjustPitch(numberOfSteps);
		}
	}
	
	public void addPitchElement(PitchElement pe) {
		pitchElements.add(pe);
	}
	
	public PitchElement addNewPitchElement(PitchElement pe) {
		PitchElement addedElement = PitchElement.clone(pe);
		return addedElement;
	}
	
	
	/**
	 * Get the pitch at the specified index.
	 * @param index
	 * @return Pitch
	 * @throws IndexOutOfBoundsException if the index is out of bounds
	 */
	public PitchElement getPitch(int index) {
		return pitchElements.get(index);
	}
	
	public boolean isOctaveNeutral() {
		boolean octaveNeutral = true;
		for(PitchElement pe : pitchElements) {
			octaveNeutral &= pe.isOctaveNeutral();
		}
		return octaveNeutral;
	}
	
}
