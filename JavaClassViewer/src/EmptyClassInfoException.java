
public class EmptyClassInfoException extends Exception {
	private static final long serialVersionUID = 1L;

	EmptyClassInfoException() {
		super("The class information has not been generated. Call parse() method before using this method.");
	}
}
