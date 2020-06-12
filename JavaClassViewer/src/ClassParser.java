import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * Parsing a simple java class code. It has several limits below. First, it can
 * only parse the basic members which are field and method. Second, it can only
 * parse one class. Third, only the basic modifiers(which are access modifier)
 * are parsed. Fourth, only the basic data types are parsed. Fifth, it ignores
 * an inner class.
 * 
 * @author Seongho Lee
 *
 */
public class ClassParser {
	private final String delimiter = " \r\n\t"; // for StringTokenizer delimiter
	private final String rawCode; // for saving original code
	private ClassInfo classInfo; // for parsed result

	public ClassParser(String code) {
		rawCode = code;
		classInfo = new ClassInfo();
	}

	public void parse() throws Exception {
		int startIndex = 0;
		ArrayList<Integer> stackOfBlockPos = new ArrayList<Integer>(); // for saving block position

		for (int i = 0; i < rawCode.length(); i++) {
			if (rawCode.charAt(i) == '{') {
				// If the character is '{', then push startIndex to the stack,
				// because it means the start of the block.
				stackOfBlockPos.add(startIndex);

				if (stackOfBlockPos.size() == 1) {
					// If the size of the stack is 1, startIndex is the start of the class.
					// Thus we can get the name of the class.
					classInfo.setName(parseClassName(rawCode.substring(startIndex, i)));

					// Reset startIndex for the next element of the stack.
					startIndex = i + 1;
				}
				// If the size of the stack is 2, this block is a method block.
				// Thus we have to add the startIndex to the stack.
				// If the size of the stack is bigger than 2, this block is ignored.

				// (*important*) Meanwhile we must reset startIndex at the end '}'.
				// (***********) See the else if statement below.

			} else if (rawCode.charAt(i) == '}') {
				// If the character is '}', then pop the stack and parse the member block,
				// because it means the end of the block.
				String subCode = "";
				if (stackOfBlockPos.size() == 0) {
					// If the size of the stack is 0, it mean an error has been occurred!
					throw new Exception();
				} else {
					// The below substring(subCode) contains '}'.
					subCode = rawCode.substring(stackOfBlockPos.remove(stackOfBlockPos.size() - 1), i + 1);
				}

				// If the size of the stack is 0, this block is class.
				if (stackOfBlockPos.size() == 1) {
					// If the size of the stack is 1, this block is method.
					classInfo.addMethodInfo(parseMethod(subCode));
				}
				// If the size of the stack is bigger than 1, this block is ignored.
				// (We do not care about local block)

				// Reset startIndex to get the next start index of a **member**.
				startIndex = i + 1;

			} else if (rawCode.charAt(i) == ';') {
				// If the character is ';', then parse the field block.

				String subCode = rawCode.substring(startIndex, i); // ';' is removed.

				// If the size of the stack is 0, this block is "import" block.
				if (stackOfBlockPos.size() == 1) {
					// If the size of the stack is 1, this block is field block.
					classInfo.addFieldInfo(parseField(subCode));
				}
				// If the size of the stack is bigger than 1, this block is ignored.
				// (We do not care about local variable)

				// Reset startIndex to get the next start index of a **member**.
				startIndex = i + 1;
			}
		}
	}

	/**
	 * After call parse() method, you can get the ClassInfo instance which has the
	 * class name and the member informations(methods and fields).
	 * 
	 * @return ClassInfo
	 */
	public ClassInfo getClassInfo() {
		return classInfo;
	}

	private String parseClassName(String code) {
		// parse "class CLASSNAME"

		StringTokenizer st = new StringTokenizer(code, delimiter);
		String name = "";

		// the name of a class is the end of tokens before '{'
		while (st.hasMoreTokens()) {
			name = st.nextToken();
		}

		return name;
	}

	public MethodInfo parseMethod(final String code) throws Exception {
		// example: "public void push(int v) { }"
		String name = "";
		String accessModifier = "default";
		String returnType = "constructor";

		String token = null;

		// get the start of inner code
		int startInnerCode = 0;
		while (code.charAt(startInnerCode) != '{') {
			startInnerCode++;
			if (code.length() == startInnerCode)
				throw new Exception();
		}

		// get the start of argument section
		String subCode = code.substring(0, startInnerCode);
		int startArgumentCode = 0;
		while (subCode.charAt(startArgumentCode) != '(') {
			startArgumentCode++;
			if (subCode.length() == startArgumentCode)
				throw new Exception();
		}

		String metaInfo = subCode.substring(0, startArgumentCode);
		String arguments = subCode.substring(startArgumentCode);

		// parse meta data (name, accessModifier, returnType)
		StringTokenizer metaST = new StringTokenizer(metaInfo, delimiter);
		while (metaST.hasMoreTokens()) {
			token = metaST.nextToken();
			if (isAccessModifier(token)) { // parse access modifier
				accessModifier = token;
			} else if (isTypeKeyword(token)) { // parse return type
				returnType = token;
			}
		}
		name = token; // parse the name

		// make methodInfo
		MethodInfo result = new MethodInfo(accessModifier, returnType, name, code);

		// parse arguments data
		StringTokenizer argumentST = new StringTokenizer(arguments, delimiter + "(),");
		while (argumentST.hasMoreTokens()) {
			token = argumentST.nextToken();
			if (isTypeKeyword(token)) {
				result.addArgumentType(token);
			}
		}
		return result;
	}

	public FieldInfo parseField(String code) {
		// example: "private int[] data"
		String name = "";
		String accessModifier = "default";
		String type = "";

		// get the start of '='.
		int startEqual = 0;
		while (code.charAt(startEqual) != '=') {
			startEqual++;
			if (code.length() == startEqual)
				break; // this is not error. '=' is optional.
		}
		String subCode = code.substring(0, startEqual);

		// parse field data
		StringTokenizer st = new StringTokenizer(subCode, delimiter);
		String token = null;
		while (st.hasMoreTokens()) {
			token = st.nextToken();
			if (isAccessModifier(token)) { // parse access modifier
				accessModifier = token;
			} else if (isTypeKeyword(token)) { // parse type
				type = token;
			}
		}
		name = token;

		FieldInfo result = new FieldInfo(accessModifier, type, name);

		return result;
	}

	private boolean isAccessModifier(String str) {
		switch (str) {
		case "default":
		case "public":
		case "private":
		case "protected":
			return true;
		}

		return false;
	}

	private boolean isTypeKeyword(String str) {
		switch (str) {
		case "void":
		case "int":
		case "int[]":
		case "boolean":
		case "boolean[]":
		case "double":
		case "double[]":
		case "float":
		case "float[]":
		case "String":
		case "String[]":
			return true;
		}

		return false;
	}
}
