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
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param am
	 */
	public void setAccessModifier(String am) {
		accessModifier = am;
	}

	/**
	 * 
	 * @return
	 */
	public String getAccessModifier() {
		return accessModifier;
	}

	/**
	 * 
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * 
	 * @return
	 */
	public String getType() {
		return type;
	}

	public void addReference(MemberInfo ref) {
		refList.add(ref);
	}

	public ArrayList<MemberInfo> getReferenceList() {
		return refList;
	}
}
