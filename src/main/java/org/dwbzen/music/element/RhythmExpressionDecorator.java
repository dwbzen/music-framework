package org.dwbzen.music.element;

import java.util.List;
import java.util.Set;

import org.dwbzen.util.Ratio;

/**
 * NOTE - have not found a use for this class yet.
 * 
 * @author donbacon
 *
 */
public class RhythmExpressionDecorator implements IRhythmExpression {
	private IRhythmExpression rhythmExpression = null;	// RhythmExpression to be decorated
	
	public RhythmExpressionDecorator(IRhythmExpression re) {
		rhythmExpression = re;
	}

	@Override
	public int getUnits() {
		return rhythmExpression.getUnits();
	}

	@Override
	public void setUnits(int units) {
		rhythmExpression.setUnits(units);
	}

	@Override
	public Ratio getRatio() {
		return rhythmExpression.getRatio();
	}

	@Override
	public TextureType getTextureType() {
		return rhythmExpression.getTextureType();
	}

	@Override
	public Set<Integer> getChordalDepth() {
		return rhythmExpression.getChordalDepth();
	}

	@Override
	public RhythmicUnitType getRhythmicUnitType() {
		return rhythmExpression.getRhythmicUnitType();
	}

	@Override
	public void setRatio(Ratio r) {
		rhythmExpression.setRatio(r);
	}

	@Override
	public void setTextureType(TextureType expressionType) {
		rhythmExpression.setTextureType(expressionType);
	}

	@Override
	public void setRhythmicUnitType(RhythmicUnitType rhythmicUnitType) {
		rhythmExpression.setRhythmicUnitType(rhythmicUnitType);
	}

	@Override
	public IRhythmScale getRhythmScale() {
		return rhythmExpression.getRhythmScale();
	}

	@Override
	public void setRhythmScale(IRhythmScale rhythmScale) {
		rhythmExpression.setRhythmScale(rhythmScale);
		
	}

	@Override
	public List<Duration> getFactors() {
		return rhythmExpression.getFactors();
	}

}
