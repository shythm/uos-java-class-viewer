import java.util.ArrayList;

/**
 * The information of the method of a class.
 * 
 * @author Seongho Lee
 *
 */
public class MethodInfo extends MemberInfo {
	private String innerCode;
	private ArrayList<String> arguments; // it stores the informations of type of argument

	public MethodInfo(String accessModifier, String returnType, String name, String innerCode) {
		super(name, accessModifier, returnType);
		arguments = new ArrayList<String>();
		this.setInnerCode(innerCode);
	}

	/**
	 * Add the information of type of argument. (Example: int, int[], ..)
	 */
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
		sb.append(": ");
		sb.append(getType()); // append the return type.

		return sb.toString();
	}

	/**
	 * This method gives you the name of this method with the informations of type
	 * of the argument.
	 */
	public String getName() {
		StringBuilder name = new StringBuilder();

		name.append(super.getName());
		// append the arguments type
		name.append('(');
		int count = arguments.size();
		for (int i = 0; i < count; i++) {
			name.append(arguments.get(i));

			if (i != count - 1) {
				name.append(", ");
			}
		}
		name.append(')');

		return name.toString();
	}

	/**
	 * Get the inner code of this method.
	 */
	public String getInnerCode() {
		return innerCode;
	}

	/**
	 * Set the inner code of this method.
	 */
	public void setInnerCode(String innerCode) {
		this.innerCode = innerCode;
	}

	public String toString() {
		return getSimpleInfo();
	}
}
