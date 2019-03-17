package org.dwbzen.music.element.rhythm;

import java.util.List;

import org.dwbzen.music.element.IRhythmExpression;
import org.dwbzen.music.element.TextureType;

public interface IRhythmTextureMap {

	List<IRhythmExpression> getRhythmExpressions();
	boolean addRhythmExpression(IRhythmExpression re);
	
	List<IRhythmExpression> getRhythmExpressions(TextureType tt);
	boolean addRhythmExpression(TextureType tt, IRhythmExpression re);
}
