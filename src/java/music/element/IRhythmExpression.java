package music.element;

import java.util.List;
import java.util.Set;

import util.Ratio;

public interface IRhythmExpression {

	int getUnits();
	void setUnits(int units);

	Ratio getRatio();
	void setRatio(Ratio r);

	TextureType getTextureType();
	void setTextureType(TextureType expressionType);

	Set<Integer> getChordalDepth();

	 RhythmicUnitType getRhythmicUnitType();
	 void setRhythmicUnitType(RhythmicUnitType rhythmicUnitType) ;
	 
	 List<Duration> getFactors();
	 
	 IRhythmScale getRhythmScale();
	 void setRhythmScale(IRhythmScale rhythmScale);
}