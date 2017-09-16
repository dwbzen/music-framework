package music.element.rhythm;

import java.util.List;

import music.element.IRhythmExpression;
import music.element.TextureType;

public interface IRhythmTextureMap {

	List<IRhythmExpression> getRhythmExpressions();
	boolean addRhythmExpression(IRhythmExpression re);
	
	List<IRhythmExpression> getRhythmExpressions(TextureType tt);
	boolean addRhythmExpression(TextureType tt, IRhythmExpression re);
}
