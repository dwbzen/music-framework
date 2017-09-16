package music.junit;
import music.element.Duration;
import music.element.Note;
import music.element.Pitch;

import org.mongodb.morphia.Morphia;
import com.mongodb.DBObject;

public class NoteTest {
	public static void main(String[] args) {
		if(args.length > 0) {
			Morphia morphia = new Morphia();
			morphia.map(Pitch.class);
			for(String s:args) {
				Pitch p = Pitch.fromString(s);
				Duration dur = new Duration(24);
				Note note = new Note(p, dur);
				DBObject dbo = morphia.toDBObject(note);
				System.out.println(dbo.toString());
			}
		}
	}
}
