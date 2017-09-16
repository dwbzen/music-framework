package music.junit;

import junit.framework.TestCase;
import music.element.Duration;

public class DurationTest  extends TestCase {
	
	public void testDurationUnits() {
		Duration d8_2 = new Duration(8, 2);		// 8 units + 2 dots = 14 durationUnits
		assertEquals(14, d8_2.getDurationUnits());
		
		Duration d4_1 = new Duration(4, 1);		// 4 units + 1 dot = 6 durationUnits
		assertEquals(6, d4_1.getDurationUnits());
		
		Duration d4_0 = new Duration(4);
		assertEquals(4, d4_0.getDurationUnits());
	}

}
