package org.dwbzen.music.element;


public interface IMeasurableContainer<T extends Measurable> {
	/**
	 * Adds a Measurable to a container
	 * @param n T instance to add
	 * @return true if added
	 */
	boolean addNote(T measurable);
	
	int size();
	
	boolean containsNote(T measurable);
	
	boolean removeNote(T measurable);
	
	boolean containsPitch(Pitch pitch);
	
	int countPitches(Pitch pitch);
	
	String toString();
}
