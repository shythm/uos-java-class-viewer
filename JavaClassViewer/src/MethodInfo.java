import java.util.ArrayList;

/**
 * The information of the method of a class.
 * 
 * @author Seongho Lee
 *
 */
public class MethodInfo extends MemberInfo {
	private String innerCode;
	private ArrayList<String> arguments;

	public MethodInfo(String accessModifier, String returnType, String name, String innerCode) {
		super(name, accessModifier, returnType);
		arguments = new ArrayList<String>();
		this.innerCode = innerCode;
	}

	public void addArgumentType(String arg) {
		arguments.add(arg);
	}

	/**
	 * Return a string that is the simplified information of this method. (Example:
	 * "addArgumentType(String): void")
	 * 
	 * @return The simplified information of this method.
	 */
	public String getSimpleInfo() {
		StringBuilder sb = new StringBuilder();

		sb.append(getName()); // append the name of this method
		sb.append('(');
		// append the arguments type
		int count = arguments.size();
		for (int i = 0; i < count; i++) {
			sb.append(arguments.get(i));

			if (i != count - 1) {
				sb.append(", ");
			}
		}
		sb.append(')');
		sb.append(": ");
		sb.append(getType()); // append the return type.

		return sb.toString();
	}

	public String toString() {
		return getSimpleInfo();
	}
}
