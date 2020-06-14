import java.util.ArrayList;

/**
 * The information of the member of a class.
 * 
 * @author Seongho Lee
 *
 */
public class MemberInfo {
	private String name; // the name of a member
	private String accessModifier; // the accessModifier of a member
	private String type; // the type of a member

	// for saving the member informations which refer or are referred this member.
	private ArrayList<MemberInfo> refList;

	/**
	 * 
	 * @param name           the string that represents the name of the member.
	 * @param accessModifier the string that represents the access modifier(private,
	 *                       public, default, protected) of the member.
	 * @param type           the string that represents the type of the member.
	 */
	public MemberInfo(String name, String accessModifier, String type) {
		this.name = name;
		this.accessModifier = accessModifier;
		this.type = type;

		refList = new ArrayList<MemberInfo>();
	}

	/**
	 * Set the name of this member.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the name of this member.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the access modifier of this member.
	 */
	public void setAccessModifier(String am) {
		accessModifier = am;
	}

	/**
	 * Get the access modifier of this member.
	 */
	public String getAccessModifier() {
		return accessModifier;
	}

	/**
	 * Set the type of this member.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Get the type of this member.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Add the given reference information.
	 */
	public void addReference(MemberInfo ref) {
		refList.add(ref);
	}

	/**
	 * This method gives you the size of reference list size.
	 */
	public int getReferenceListSize() {
		return refList.size();
	}

	/**
	 * This method gives you the reference of MemberInfo instance by index.
	 */
	public MemberInfo getReference(int index) {
		return refList.get(index);
	}
}
