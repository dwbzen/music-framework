package music.element;

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
	
	public static List<Integer> formulaToPitchSet(int[] forml) {
		List<Integer> pitchSet = new ArrayList<Integer>();
		pitchSet.add(0);
		for(int i = 0; i<forml.length; i++) {
			pitchSet.add(forml[i] + pitchSet.get(i) );
		}
		return pitchSet;
	}
	
	public static List<Integer> formulaToPitchSet(List<Integer> forml) {
		int[] farray = new int[forml.size()];
		int i = 0;
		for(Integer n : forml) {
			farray[i++] = n;
		}
		return formulaToPitchSet(farray);
	}
	
	public static  List<Integer> pitchSetToFormula(int[] pitchSet) {
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
	 * 
	 * @param formula an int[] formula
	 * @param root root Pitch of the scale
	 * @param key optional associated Key. If != null, it determines accidental preference - # or b
	 * @param pref optional Alteration to use (overrides key setting)
	 * @return List<Pitch>
	 */
	public static List<Pitch> createPitches(List<Integer>formula, Pitch root, Key key, Alteration altpref) {

		List<Pitch> plist = new ArrayList<Pitch>();
		plist.add(root);
		Pitch next = null;
		int stepIncrement = 0;
		int preference = (altpref != null) ? altpref.value() : 
			(key != null && key.getSignature() != null && key.getSignature().length > 0) ? key.getSignature()[0].getAlteration() : 0;
		for(int i: formula) {
			stepIncrement += i;
			next = root.increment(stepIncrement, preference);
			plist.add(next);
		}
		return plist;
	}
}
