package music.element;

/**
 * For background information see the Wikipedia article on <a href="https://en.wikipedia.org/wiki/Texture_(music)">Texture</a>
 * @author don_bacon
 *
 */
public enum TextureType {
	MONOPHONIC(0), CHORDAL(1);
	
	TextureType(int val) { this.value = val;}
	private final int value;
    public int value() { return value; }
}
