package util;


public class Index {
	
	private int x = 0;
	private int y = 0;

	public Index() {
	}
	public Index(int xi, int yi) {
		assert xi >=0;
		assert yi >=0;
		x=xi;
		y=yi;
	}
	
	public boolean isValid() {
		return x>=0 && y>=0;
	}
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
