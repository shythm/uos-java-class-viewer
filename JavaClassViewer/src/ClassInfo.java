import java.util.ArrayList;

public class ClassInfo {
	private String name; // A string that stores the name of a class.
	private ArrayList<MemberInfo> memberInfos; // An ArrayList that stores member informations.
	
	public ClassInfo() {
		memberInfos = new ArrayList<MemberInfo>();
	}

	public String toString() {		
		return name;
	}

	public void addMethodInfo(MethodInfo info) {
		memberInfos.add(info);
	}

	public void addFieldInfo(FieldInfo info) {
		memberInfos.add(info);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public MemberInfo getMemberInfo(int index) {
		return memberInfos.get(index);
	}

	public int getMemberInfoSize() {
		return memberInfos.size();
	}

	public int getMemberIndex(MemberInfo member) {
		return 0;
	}
}
