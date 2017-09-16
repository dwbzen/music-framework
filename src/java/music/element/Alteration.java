package music.element;


public enum Alteration {
	UP_ONE(1), UP_TWO(2), DOWN_ONE(-1), DOWN_TWO(-2), NONE(0), NATURAL(0);
	Alteration(int val) { this.value = val;}
	private final int value;
    public int value() { return value; }
}
