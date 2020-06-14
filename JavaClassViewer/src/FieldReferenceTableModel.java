import javax.swing.table.AbstractTableModel;

/**
 * TableModel for a FieldInfo
 * 
 * @author Seongho Lee
 *
 */
public class FieldReferenceTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private FieldInfo fieldInfo;
	private String[] columnName = { "Name", "Method" }; // names of the columns
	private Object[][] data; // data to show

	/**
	 * This constructor build model and initialize data with the informations in the
	 * info.
	 */
	public FieldReferenceTableModel(FieldInfo info) {
		fieldInfo = info;
		initData();
	}

	/**
	 * Initialize data with information in the FieldInfo
	 */
	private void initData() {
		int columnCount = getColumnCount();
		int rowCount = getRowCount();

		// Initialize Data
		data = new Object[rowCount][];
		for (int i = 0; i < rowCount; i++) {
			data[i] = new Object[columnCount];
			data[i][0] = fieldInfo.getName();
			data[i][1] = fieldInfo.getReference(i);
		}
	}

	@Override
	public int getRowCount() {
		return fieldInfo.getReferenceListSize();
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
