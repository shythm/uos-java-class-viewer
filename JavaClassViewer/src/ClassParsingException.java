/**
 * This Exception represents there occurs an error while parsing the code.
 * 
 * @author Seongho Lee
 *
 */
public class ClassParsingException extends Exception {
	private static final long serialVersionUID = 1L;

	public ClassParsingException(String code) {
		super("There occurs an error while parsing the code. The code is below.\n" + code);
	}
}
