import javax.swing.table.AbstractTableModel;

public class FieldReferenceTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	
	private FieldInfo fieldInfo;
	private String[] columnName = { "Name", "Method" };
	private Object[][] data;
	
	public FieldReferenceTableModel(FieldInfo info) {
		fieldInfo = info;
		initData();
	}
	
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
