package junit;

import org.apache.log4j.Logger;
import org.dwbzen.music.element.Duration;
import org.dwbzen.music.element.Note;
import org.dwbzen.music.element.Pitch;
import org.junit.Test;

import junit.framework.TestCase;

public class NoteTest  extends TestCase  {
	static final org.apache.log4j.Logger log = Logger.getLogger(SongNoteTest.class);

	Pitch c4 = new Pitch("C4");
	Pitch c5 = new Pitch("C5");
	Pitch a5 = new Pitch("A5");
	Duration dur60 = new Duration(60);
	Duration dur30 = new Duration(30);
	
	@Test
	public void testCompareAndClone() {
		Note c4_60 = new Note(c4, dur60);
		Note c4_60a = new Note(c4, 60);
		Note c5_60 = new Note(c5, dur60);
		// tie the next two together
		Note a5_60a = new Note(a5, 60);
		Note a5_60b = new Note(a5, 30);
		a5_60a.setTiedTo(a5_60b);
		
		assertEquals(0, c4_60.compareTo(c4_60a));
		assertEquals(0, a5_60a.compareTo(a5_60b, true));
		assertEquals(-1, c4_60.compareTo(c5_60));
		
		Note c4cloned = c4_60.clone();
		assertTrue(c4cloned.equals(c4_60));
		
	}
	
}
