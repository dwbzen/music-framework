package test;

import music.transform.IExploder.ExploderType;
import music.transform.NoteExploder;

public class ExploderTest {

	public static void main(String... args) {
		
		NoteExploder noteExploder = new NoteExploder(ExploderType.ARPEGIO, NoteExploder.QUINTUPLET_RANDOM, NoteExploder.FIVE_FOUR);
		noteExploder.setName("QUINTUPLET_RANDOM");
		String js = noteExploder.toJson();
		System.out.println( js);
		
		noteExploder = new NoteExploder(ExploderType.ARPEGIO, NoteExploder.EIGHT_RANDOM);
		noteExploder.setName("EIGHT_RANDOM");
		System.out.println(noteExploder.toJson());
		
		noteExploder = new NoteExploder(ExploderType.ARPEGIO, NoteExploder.TRIPLET_RANDOM);
		noteExploder.setName("TRIPLET_RANDOM");
		System.out.println(noteExploder.toJson());
		
		noteExploder = new NoteExploder(ExploderType.CHORD, NoteExploder.QUAD_RANDOM_CHORD, NoteExploder.ONE_TO_ONE, 20);
		noteExploder.setName("QUAD_RANDOM_CHORD");
		System.out.println(noteExploder.toJson());
	}
	
	
}
