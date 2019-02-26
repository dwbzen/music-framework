package util.music;

public class DataLoadException  extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public DataLoadException() {
		super();
	}
	
	public DataLoadException(String message) {
		super(message);
	}
	
	public DataLoadException(String message, Throwable cause) {
		super(message, cause);
	}

}
