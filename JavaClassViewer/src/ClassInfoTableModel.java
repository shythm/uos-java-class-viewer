import javax.swing.table.AbstractTableModel;

/**
 * TableModel for a ClassInfo
 * 
 * @author Seongho Lee
 *
 */
public class ClassInfoTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private ClassInfo classInfo;
	private String[] columnName = { "Name", "Type", "Access" };
	private Object[][] data;

	/**
	 * This constructor build model and initialize data with the informations in the
	 * info.
	 * 
	 * @param info 
	 */
	public ClassInfoTableModel(ClassInfo info) {
		classInfo = info;
		initData();
	}

	/**
	 * Initialize data with information in the classInfo
	 */
	private void initData() {
		int columnCount = getColumnCount();
		int rowCount = getRowCount();

		// Initialize Data
		data = new Object[rowCount][];
		for (int i = 0; i < rowCount; i++) {
			MemberInfo m = classInfo.getMemberInfo(i);
			data[i] = new Object[columnCount];
			data[i][0] = m.getName();
			data[i][1] = m.getType();
			data[i][2] = m.getAccessModifier();
		}
	}

	@Override
	public int getRowCount() {
		return classInfo.getMemberInfoSize();
	}

	@Override
	public int getColumnCount() {
		return columnName.length;
	}

	public String getColumnName(int columnIndex) {
		return columnName[columnIndex];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data[rowIndex][columnIndex];
	}
}
