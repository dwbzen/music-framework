package org.dwbzen.music.element;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.dwbzen.music.action.ExpressionSelector;
import org.dwbzen.music.element.rhythm.IRhythmTextureMap;
import org.dwbzen.music.transform.ITransformer.Preference;
import org.dwbzen.common.util.IJson;
import org.dwbzen.common.util.INameable;

public interface IRhythmScale extends IJson, INameable {
	
	int getRoot();
	void setRoot(int root);
	
	int getRootUnits();
	void setRootUnits(int rootUnits);
	
	void setName(String name);
	String getName();
	
	SortedSet<Integer> getBaseUnits();
	
	Map<Integer, IRhythmTextureMap> getExpressions();
	IRhythmTextureMap getRhythmTextureMap(Integer units);
	
	int getRange();
	void setRange(int r);
	
	ExpressionSelector getExpressionSelector();
	void setExpressionSelector(ExpressionSelector expressionSelector);
	
	int findClosestUnits(double rawUnits, Preference pref);

	 Map<Integer, List<Duration>> getFactorMap();
	 List<Duration> getFactors(Integer units);
	 String getNoteType(Note note);
	 
	 /**
	  * 
	  * @return true if the RhythmScale supports Chordal texture, false otherwise
	  */
	 boolean isChordal();
	 void setChordal(boolean chordal);
	 
	 default void setChordalTextureProbability(double chordalProbability) {
		 ExpressionSelector selector = getExpressionSelector();
		 for(Integer units : getBaseUnits()) {
			 selector.setTextureTypeProbability(units, TextureType.CHORDAL, chordalProbability);
			 selector.setTextureTypeProbability(units, TextureType.MONOPHONIC, 1-chordalProbability);
		 }
	 }
	 
}
