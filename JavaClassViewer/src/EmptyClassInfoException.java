/**
 * This Exception represents the class information has not been generated.
 * 
 * @author Seongho Lee
 *
 */
public class EmptyClassInfoException extends Exception {
	private static final long serialVersionUID = 1L;

	public EmptyClassInfoException() {
		super("The class information has not been generated. Call parse() method before using this method.");
	}
}
