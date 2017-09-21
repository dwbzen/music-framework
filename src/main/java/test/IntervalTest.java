package music.test;

import music.element.Interval;

public class IntervalTest {
	
	public static void main(String[] args) {
		
		for(int i=0; i<=24; i++) {
			Interval interval = new Interval(i);
			System.out.println(i + ": " + interval.toString() + " == " + interval.toSteps());
		}
		for(int i=0; i>=-24; i--) {
			Interval interval = new Interval(i);
			System.out.println(i + ": " + interval.toString() + " == " + interval.toSteps());
		}	}

}
