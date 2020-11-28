package org.dwbzen.util.music;

import java.util.ArrayList;
import java.util.List;

import org.dwbzen.common.util.IJson;
import org.dwbzen.music.element.PitchElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * PitchCollection aggregates PitchElements (an individual Pitch and/or a PitchSet) as a List.
 * 
 * @author don_bacon
 *
 */
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
	
	/**
	 * Creates a new PitchCollection that is the PitchElements of this in reverse order.<br>
	 * Each PitchElement added is the retrograde of the original.<br>
	 * For a Pitch, this is simply a copy of the original Pitch.<br>
	 * For a PitchSet it is the retrograde of the Pitches in the PitchSet.
	 * 
	 * @return PitchCollection
	 */
	public PitchCollection getRetrograde() {
		PitchCollection pc = new PitchCollection();
		for(int i = size()-1; i>=0; i--) {
			PitchElement pe = pitchElements.get(i).getRetrograde();
			pc.addPitchElement(pe);
		}
		return pc;
	}
	
	public void addPitchElement(PitchElement pe) {
		pitchElements.add(pe);
	}
	
	public PitchElement addNewPitchElement(PitchElement pe) {
		PitchElement addedElement = PitchElement.clone(pe);
		return addedElement;
	}
	
	public void add(PitchCollection pc) {
		pitchElements.addAll(pc.pitchElements);
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

	public List<PitchElement> getPitchElements() {
		return pitchElements;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(PitchElement pe: pitchElements) {
			sb.append(pe.toString());
			sb.append(", ");
		}
		sb.deleteCharAt(sb.length()-2);
		return sb.toString();
	}
	
}
