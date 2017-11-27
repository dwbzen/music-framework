package music.element.rhythm;

import java.util.ArrayList;
import java.util.List;

import music.element.IRhythmExpression;
import music.element.TextureType;

public class RhythmTextureMap extends BaseRhythmTextureMap {

	private static final long serialVersionUID = -8651838440123201434L;
	
	public RhythmTextureMap() {
		super();
		textureMap.put(TextureType.CHORDAL,	new ArrayList<IRhythmExpression>());
	}
	
	@Override
	public List<IRhythmExpression> getRhythmExpressions(TextureType tt) {
		return textureMap.get(tt);
	}
	
	@Override
	public boolean addRhythmExpression(TextureType tt, IRhythmExpression re) {
		return textureMap.get(tt).add(re);
	}

}
