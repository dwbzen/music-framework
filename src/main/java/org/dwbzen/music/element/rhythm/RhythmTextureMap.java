package org.dwbzen.music.element.rhythm;

import java.util.ArrayList;
import java.util.List;

import org.dwbzen.music.element.IRhythmExpression;
import org.dwbzen.music.element.TextureType;

public class RhythmTextureMap extends BaseRhythmTextureMap {
	
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
