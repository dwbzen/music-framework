package junit;

import junit.framework.TestCase;
import music.element.Alteration;
import music.element.Key;
import music.element.Key.Mode;
import music.element.Pitch;
import music.element.Scale;
import music.element.ScaleFormula;
import music.element.ScaleType;
import music.element.Scales;
import music.element.Step;

import org.junit.Test;
import org.mongodb.morphia.Morphia;
import com.mongodb.DBObject;

public class ScaleTest extends TestCase {
	private Morphia morphia = new Morphia();
	
	public ScaleTest() {
		morphia.map(ScaleFormula.class);
		morphia.map(Scale.class);
		morphia.map(Pitch.class);
		morphia.map(ScaleType.class);
		morphia.map(Step.class);
		morphia.map(Key.class);
		morphia.map(Mode.class);
	}

	/**
	 * @param args
	 */
	public static void main(String... args) {
	}
	
	@Test
	public void testPitchMapping() {
		Pitch pitch = new Pitch(Step.G, 3, Alteration.NONE);
		DBObject pitchdbObject = morphia.toDBObject(pitch);
		System.out.println("pitch: " + pitchdbObject.toString());
		Pitch pitch2 = morphia.fromDBObject(null, Pitch.class, pitchdbObject);
		System.out.println(pitch2.toString());
		assertTrue(pitch.equals(pitch2));
	}
	
	@Test
	public void testScaleMapping() {
		Scale gmajor = Scales.G_MAJOR;
		System.out.println(gmajor.toJSON());
		DBObject dbObject = morphia.toDBObject(gmajor);
		Scale scale = morphia.fromDBObject(null, Scale.class, dbObject);
		System.out.println(scale.toString());
		assertTrue(gmajor.getNotes().equals(scale.getNotes()));
	}
}
