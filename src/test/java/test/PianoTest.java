package test;

import org.dwbzen.music.action.PitchScaler;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.instrument.Piano;
import org.dwbzen.music.instrument.PianoRH;

public class PianoTest {
	
	public static void main(String[] args) {
		PianoRH piano = new PianoRH();
		for(Pitch p : piano.getNotes()) {
			System.out.print(p.toString() + " ");
		}
		System.out.println("\n");
		PitchScaler ps = piano.getPitchScaler();
		ps.setMaxVal(.9);
		ps.setMinVal(.2);
		double d1 = 0.2;	// A0
		double d2 = 0.5;	// 
		double d3 = 0.9;	// C8
		double d4 = 0.35;	// E2
		
		Pitch p1 = piano.scale(d1);
		Pitch p2 = piano.scale(d2);
		Pitch p3 = piano.scale(d3);
		Pitch p4 = piano.scale(d4);
		System.out.println(d1 + " scales to: " + p1);
		System.out.println(d2 + " scales to: " + p2);
		System.out.println(d3 + " scales to: " + p3);
		System.out.println(d4 + " scales to: " + p4);

	}
}
