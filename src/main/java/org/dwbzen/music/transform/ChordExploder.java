package org.dwbzen.music.transform;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dwbzen.common.math.IntegerPair;
import org.dwbzen.music.element.Measurable;
import org.dwbzen.music.element.Measure;

/**
 * This explodes a single Measurable (Chord) into a List of Chords using
 * a formula such that the overall duration remains the same.
 * It essentially explodes each note in the chord using the formula
 * resulting in #new chords == size of the formula
 * There is an ExploderType.ARPEGIO version of this that
 * selects on of the notes in the chord and explodes it
 * as a NoteExploder would.
 * 
 * NOTE - implementation is incomplete. This is just a stub.
 * Do not configure anything to use ChordExploder until complete.
 * 
 * TODO: finish this
 */
public class ChordExploder extends AbstractExploder {

	protected static final org.apache.log4j.Logger log = Logger.getLogger(ExplodeTransformer.class);

	public ChordExploder(List<IntegerPair> formula) {
		super(ExploderType.CHORD, formula, IExploder.ONE_TO_ONE, 0);
	}
	
	public ChordExploder(List<IntegerPair> formula, IntegerPair ratio) {
		super(ExploderType.CHORD, formula, ratio, 0);
	}
	
	public ChordExploder(List<IntegerPair> formula, IntegerPair ratio, int freq) {
		super(ExploderType.CHORD, formula, ratio, freq);
	}
	
	@Override
	public List<Measurable> explode(Measurable m, Measure measure) {
		List<Measurable> chords = new ArrayList<Measurable>();
		chords.add(m);
		return chords;
	}
	
}
