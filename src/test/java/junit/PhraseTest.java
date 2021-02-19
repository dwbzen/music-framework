package junit;

import static org.junit.Assert.*;

import org.dwbzen.music.instrument.Instrument;
import org.dwbzen.util.music.InstrumentMaker;
import org.junit.Test;

public class PhraseTest {
	
	static String instruments = "Flute,Piano";
	static InstrumentMaker instrumentMaker = new InstrumentMaker(instruments);

	@Test
	public void testCreatePhrase() {
		Instrument instrument = instrumentMaker.getInstrument("Flute");
		// TODO
	}

}
