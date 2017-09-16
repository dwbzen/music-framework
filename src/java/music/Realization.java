package music;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import music.transform.Layer;

/**
 * A Realization is a combination of Layers
 * @author don_bacon
 *
 */
public class Realization implements Serializable {
	
	private static final long serialVersionUID = 2830334516334491266L;
	private List<Layer> layers = new ArrayList<Layer>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

	public List<Layer> getLayers() {
		return layers;
	}

}
