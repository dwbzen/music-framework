package org.dwbzen.util.music;

import java.util.SortedSet;

import org.dwbzen.music.element.RhythmScale;

public interface IRhythmScaleFactory {

	RhythmScale createRhythmScale(String name);
	
	void setUnitsPerMeasure(int unitsPerMeasure);
	int  getUnitsPerMeasure();
	
	SortedSet<Integer> getBaseUnits();
	SortedSet<Integer>  createBaseUnits(RhythmScale rhythmScale);
}
