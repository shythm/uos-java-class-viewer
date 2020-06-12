/**
 * The information of the field of a class.
 * 
 * @author Seongho Lee
 *
 */
public class FieldInfo extends MemberInfo {

	public FieldInfo(String accessModifier, String type, String name) {
		super(name, accessModifier, type);
	}

	/**
	 * Return a string that is the simplified information of this field. (Example:
	 * "name: String")
	 * 
	 * @return The simplified information of this field.
	 */
	public String getSimpleInfo() {
		StringBuilder sb = new StringBuilder();

		sb.append(getName()); // append the name of this field
		sb.append(": ");
		sb.append(getType());

		return sb.toString();
	}

	public String toString() {
		return getSimpleInfo();
	}
}
