import java.util.ArrayList;

/**
 * This class stores an information of class
 * 
 * @author Seongho Lee
 *
 */
public class ClassInfo {

	private String name; // A string that stores the name of a class.
	private ArrayList<MemberInfo> memberInfos; // An ArrayList that stores member informations.

	public ClassInfo() {
		memberInfos = new ArrayList<MemberInfo>();
	}

	public String toString() {
		return name;
	}

	/**
	 * Add an instance of MethodInfo.
	 */
	public void addMethodInfo(MethodInfo info) {
		memberInfos.add(info);
	}

	/**
	 * Add an instance of FieldInfo.
	 */
	public void addFieldInfo(FieldInfo info) {
		memberInfos.add(info);
	}

	/**
	 * Set the name of this class.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the name of this class.
	 */
	public String getName() {
		return name;
	}

	/**
	 * This method gives you the instance of MemberInfo by index. You must check
	 * what member it is.
	 */
	public MemberInfo getMemberInfo(int index) {
		return memberInfos.get(index);
	}

	/**
	 * This method gives you the size of the members that this class stores.
	 */
	public int getMemberInfoSize() {
		return memberInfos.size();
	}

	/**
	 * This method gives you the index of the given member.
	 */
	public int getMemberIndex(MemberInfo member) {
		return 0;
	}
}
