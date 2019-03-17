package org.dwbzen.music.element.rhythm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.dwbzen.music.element.IRhythmExpression;
import org.dwbzen.music.element.TextureType;
import org.dwbzen.common.util.IJson;

public class BaseRhythmTextureMap  implements IJson, IRhythmTextureMap {

	private static final long serialVersionUID = 9179677215188237234L;
	@JsonProperty	protected Map<TextureType, List<IRhythmExpression>> textureMap = new HashMap<TextureType, List<IRhythmExpression>>();
	
	public BaseRhythmTextureMap() {
		textureMap.put(TextureType.MONOPHONIC,	new ArrayList<IRhythmExpression>());
	}
	
	@Override
	public List<IRhythmExpression> getRhythmExpressions() {
		return textureMap.get(TextureType.MONOPHONIC);
	}
	
	@Override
	public boolean addRhythmExpression(IRhythmExpression re) {
		return textureMap.get(TextureType.MONOPHONIC).add(re);
	}

	@Override
	public List<IRhythmExpression> getRhythmExpressions(TextureType tt) {
		return tt.equals(TextureType.MONOPHONIC) ? textureMap.get(TextureType.MONOPHONIC) : null;
	}

	@Override
	public boolean addRhythmExpression(TextureType tt, IRhythmExpression re) {
		// TODO Auto-generated method stub
		return tt.equals(TextureType.MONOPHONIC) ? textureMap.get(TextureType.MONOPHONIC).add(re) : false;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(TextureType.MONOPHONIC.toString() + "\n");
		for(IRhythmExpression exp : getRhythmExpressions()) {
			sb.append(" " + exp.toString() + "\n");
		}
		
		return sb.toString();
	}

}
