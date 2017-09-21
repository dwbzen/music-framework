package util.music;

public class MissingElementException extends RuntimeException {

	private static final long serialVersionUID = -7230564409534781142L;

	public MissingElementException() {
		super();
	}
	
	public MissingElementException(String message) {
		super(message);
	}
	
	public MissingElementException(String message, Throwable cause) {
		super(message, cause);
	}
}
