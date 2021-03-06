package org.dwbzen.music.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dwbzen.common.util.INameable;


/**
 * Any music.element that has a formula of intervals.
 * 
 * @author don_bacon
 *
 */
public interface IFormula extends INameable, Serializable {
	
	public List<Integer> getFormula();
	public List<String> getAlternateNames();
	
	/**
	 * Creates a List of relative pitch indexes from a given IFormula.<br>
	 * Each element is the number of steps from the root.<br>
	 * For example given the chord formula [4, 3, 3, 4, 7] (a 13th chord)<br>
	 * the resulting pitch set is [0, 4, 7, 10, 14, 21]<br>
	 * If the root Pitch is C, the corresponding Pitches are { C, E, G, Bb, D, A }
	 * @param forml
	 * @return
	 */
	public static List<Integer> formulaToPitchIndexes(Integer[] forml) {
		List<Integer> pitchSet = new ArrayList<Integer>();
		pitchSet.add(0);
		for(int i = 0; i<forml.length; i++) {
			pitchSet.add(forml[i] + pitchSet.get(i) );
		}
		return pitchSet;
	}
	
	public static List<Integer> formulaToPitchIndexes(int[] forml) {
		List<Integer> pitchSet = new ArrayList<Integer>();
		pitchSet.add(0);
		for(int i = 0; i<forml.length; i++) {
			pitchSet.add(forml[i] + pitchSet.get(i) );
		}
		return pitchSet;
	}

	public static List<Integer> formulaToPitchIndexes(List<Integer> forml) {
		return formulaToPitchIndexes(forml.toArray(new Integer[0]));
	}
	
	public static  List<Integer> pitchIndexesToFormula(int[] pitchSet) {
		List<Integer> psFormula = new ArrayList<Integer>();
		for(int i = 1; i<pitchSet.length; i++) {
			psFormula.add(pitchSet[i] - pitchSet[i-1]);
		}
		psFormula.add(12 - pitchSet[pitchSet.length-1]);
		return psFormula;
	}
	
	public static List<Pitch> createPitches(List<Integer>formula, Pitch root, Key key) {
		return createPitches(formula, root, key, null);
	}
	
	
	/**
	 * Creates a List of Pitch for a Scale/Chord with a given formula and root
	 * A Scale may consist of a single note. In that case the formula is [0].
	 * If the root note is octave neutral (i.e. octave = -1), the resulting pitches will also be.
	 * 
	 * @param formula an int[] formula
	 * @param root root Pitch of the scale
	 * @param key optional associated Key. If != null, it determines accidental preference - # or b
	 * @param pref optional Alteration to use (overrides key setting)
	 * @return List<Pitch>
	 */
	public static List<Pitch> createPitches(List<Integer>formula, Pitch root, Key key, Alteration altpref) {

		List<Pitch> plist = new ArrayList<Pitch>();
		Pitch nroot = new Pitch(root);
		plist.add(nroot);
		Pitch next = null;
		int stepIncrement = 0;
		boolean octaveNeutral = (root.getOctave() < 0);
		int preference = (altpref != null) ? altpref.value() : 
			(key != null && key.getSignature() != null && key.getSignature().length > 0) ? key.getSignature()[0].getAlteration() : 0;
		for(int i: formula) {
			stepIncrement += i;
			if(octaveNeutral) {
				next = nroot.incrementPitchOnly(stepIncrement, preference);
			}
			else {
				next = nroot.increment(stepIncrement, preference);
			}
			plist.add(next);
		}
		return plist;
	}
}
