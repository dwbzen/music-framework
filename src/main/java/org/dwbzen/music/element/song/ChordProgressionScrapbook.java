package org.dwbzen.music.element.song;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * A Set of ChordProgressions like in a reference book.
 * 
 * @author don_bacon
 *
 */
public class ChordProgressionScrapbook implements Set<ChordProgression>, Serializable {

	private static final long serialVersionUID = -5787984714984611704L;
	private Set<ChordProgression> chordProgressions = null;

	/**
	 * Creates a new ChordProgressionScrapbook with elements appearing in the order added.
	 */
	public ChordProgressionScrapbook() {
		chordProgressions = new LinkedHashSet<ChordProgression>();
	}
	
	/**
	 * Creates a new ChordProgressionScrapbook with elements with elements sorted or not as specified.
	 * @param sorted
	 */
	public ChordProgressionScrapbook(Boolean sorted) {
		chordProgressions = sorted ? new TreeSet<ChordProgression>() : new LinkedHashSet<ChordProgression>();
	}
	
	@Override
	public int size() {
		return chordProgressions.size();
	}

	@Override
	public boolean isEmpty() {
		return chordProgressions.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return chordProgressions.contains(o);
	}

	@Override
	public Iterator<ChordProgression> iterator() {
		return chordProgressions.iterator();
	}

	@Override
	public Object[] toArray() {
		return chordProgressions.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return chordProgressions.toArray(a);
	}

	@Override
	public boolean add(ChordProgression e) {
		return chordProgressions.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return chordProgressions.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return chordProgressions.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends ChordProgression> c) {
		return chordProgressions.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return chordProgressions.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return chordProgressions.removeAll(c);
	}

	@Override
	public void clear() {
		chordProgressions.clear();
	}

}
