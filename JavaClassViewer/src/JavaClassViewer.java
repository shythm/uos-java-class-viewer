import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Java Class Viewer for Assignment #2 of 객체지향프로그래밍및실습
 * 
 * @author Seongho Lee
 *
 */
public class JavaClassViewer extends JFrame {
	private static final long serialVersionUID = 1L;

	private String rawCode; // an original code
	private ClassParser classParser; // a class parser
	private int viewerWidth; // a width of this window
	private int viewerHeight; // a height of this window

	private JSplitPane mainPanel; // a main view
	private JSplitPane leftPanel; // a left partition
	private JPanel rightPanel; // a right partition

	private JTree classInfoTree; // a tree view of the class information
	private JTable infoTable; // a table view of the information
	private JTextArea usageDisplay; // a display for usage of a field
	private JTextArea sourceCodeDisplay; // a display for the source code of a method

	/**
	 * This is JavaClassViewer Initializer
	 * 
	 * @param width  the width of this viewer
	 * @param height the height of this viewer
	 */
	public JavaClassViewer(int width, int height) {
		// Initialize
		rawCode = null;
		classParser = null;
		viewerWidth = width;
		viewerHeight = height;

		// Set Default GUI properties
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(viewerWidth, viewerHeight);
		setTitle("UOS Java Class Viewer");

		initMenuBar(); // Initialize Menu Bar
		initComponents(); // Initialize Components
		initEventListener(); // Initialize Event Listeners;

		setVisible(true);
	}

	/**
	 * This method initializes a menu bar.
	 */
	private void initMenuBar() {
		JMenuBar menuBar;
		JMenu menuFile;
		JMenuItem menuItemOpen, menuItemExit;

		menuBar = new JMenuBar();
		menuFile = new JMenu("File");
		menuItemOpen = new JMenuItem("Open");
		menuItemExit = new JMenuItem("Exit");

		// Open and parse the java class file.
		menuItemOpen.addActionListener(e -> {
			openClassFile();
		});

		// Exit
		menuItemExit.addActionListener(e -> {
			System.exit(0);
		});

		menuFile.add(menuItemOpen);
		menuFile.add(menuItemExit);
		menuBar.add(menuFile);

		setJMenuBar(menuBar);
	}

	/**
	 * This method initializes components. Add a left and right panel, tree view,
	 * and so on.
	 */
	private void initComponents() {
		/* Initialize Components */
		// Initialize classInfoTree
		classInfoTree = new JTree();
		classInfoTree.setModel(null); // set null model to show nothing

		// Initialize display
		usageDisplay = new JTextArea(3, 20);
		sourceCodeDisplay = new JTextArea(30, 30);

		// Initialize infoTable
		infoTable = new JTable();
		/* --------- */

		/* Do Layout */
		// Set the main panel with the left and right panel.
		mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainPanel.setDividerLocation((int) (viewerWidth * 0.33));

		// Set the left panel.
		leftPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		leftPanel.setTopComponent(new JScrollPane(classInfoTree)); // add classInfoTree
		leftPanel.setBottomComponent(new JScrollPane(usageDisplay)); // add display
		leftPanel.setDividerLocation((int) (viewerHeight * 0.50));
		mainPanel.setLeftComponent(leftPanel);

		// Set the right panel.
		rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(new JPanel(), BorderLayout.CENTER); // add empty panel
		mainPanel.setRightComponent(rightPanel);

		getContentPane().add(mainPanel);
		/* --------- */
	}

	/**
	 * This method initializes event listeners.
	 */
	private void initEventListener() {
		/* Add Event Listener */
		// Add TreeSelectionListener
		classInfoTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				Object o = e.getPath().getLastPathComponent(); // get leaf node
				clearDisplays(); // clear the displays

				if (o instanceof MethodInfo) {
					methodSelectionHandler((MethodInfo) o); // subhandler
				} else if (o instanceof FieldInfo) {
					fieldSelectionHandler((FieldInfo) o); // subhandler
				} else {
					classSelectionHandler(); // subhandler
				}
				validate();
			}
		});
		/* --------- */
	}

	/**
	 * This method clears displays to show other informations.
	 */
	private void clearDisplays() {
		rightPanel.removeAll(); // remove all components of the rightPanel
//		infoTable.setModel(null); // clear infoTable (Error occurred.)
		usageDisplay.setText(""); // clear usageDisplay
		sourceCodeDisplay.setText(""); // clear sourceCodeDisplay
	}

	/**
	 * This event handler for a selection of a class.
	 */
	private void classSelectionHandler() {
		// STEP 1: When select a class, show the name, the type, and the access modifier
		// of a method or a field in the class.
		ClassInfoTableModel model = null;
		try {
			model = new ClassInfoTableModel(classParser.getClassInfo());
		} catch (EmptyClassInfoException e) {
			e.printStackTrace();
			return;
		}
		infoTable.setModel(model); // set model
		rightPanel.add(new JScrollPane(infoTable), BorderLayout.CENTER);
	}

	/**
	 * This event handler for a selection of a method.
	 * 
	 * @param info a method information which will be displayed.
	 */
	private void methodSelectionHandler(MethodInfo info) {
		// STEP 2: Show the source code of a method.
		sourceCodeDisplay.setText(info.getInnerCode());
		rightPanel.add(new JScrollPane(sourceCodeDisplay), BorderLayout.CENTER);

		// STEP 2: Show the fields which a method uses.
		StringBuilder sb = new StringBuilder();
		sb.append(info.getName());
		sb.append(" uses the field(s) below.\n");
		if (info.getReferenceListSize() == 0) {
			sb.append("(Nothing to show)");
		} else {
			for (int i = 0; i < info.getReferenceListSize(); i++) {
				sb.append(info.getReference(i).getName() + '\n');
			}
		}
		usageDisplay.setText(sb.toString());
	}

	/**
	 * This event handler for a selection of a field.
	 * 
	 * @param info a field information which will be displayed.
	 */
	private void fieldSelectionHandler(FieldInfo info) {
		// STEP 3: Show the methods which reference a field.
		FieldReferenceTableModel model = new FieldReferenceTableModel(info);
		infoTable.setModel(model); // set model
		rightPanel.add(new JScrollPane(infoTable), BorderLayout.CENTER);
	}

	/**
	 * Open the 'Java class file open dialog' and store the java class file.
	 */
	private void loadClassFile() {
		// Open the FileDialog and store class file path
		FileDialog fileOpen = new FileDialog(this, "Choose Java Class File", FileDialog.LOAD);
		fileOpen.setVisible(true);
		String path = fileOpen.getDirectory() + fileOpen.getFile();

		// Read class file with StringBuffer
		int b = 0;
		StringBuffer buffer = new StringBuffer();
		FileInputStream file = null;
		try {
			file = new FileInputStream(path);
			while ((b = file.read()) != -1) {
				buffer.append((char) b);
			}

			file.close();
			rawCode = buffer.toString(); // store rawCode
		} catch (FileNotFoundException e) {
			System.out.println("Oops! FileNotFoundException");
		} catch (IOException e) {
			System.out.println("Input error");
		}
	}

	/**
	 * This method is for "open" menu item. Load a class file with a dialog and
	 * parse the class file. When the parsing has been done successfully, set the
	 * tree view.
	 */
	public void openClassFile() {
		// Load class file
		loadClassFile();

		// Parse the class file
		classParser = new ClassParser(rawCode);
		try {
			classParser.parse(); // parse this class
			classParser.findReferenceRelation(); // find the reference relations
		} catch (Exception e) {
			e.printStackTrace();
		}

		ClassInfo c = null;
		try {
			c = classParser.getClassInfo(); // get class information
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Set the tree view
		if (c != null) {
			ClassInfoTreeModel treeModel = new ClassInfoTreeModel(c);
			classInfoTree.setModel(treeModel);
		}
	}

	public static void main(String[] args) {
		new JavaClassViewer(800, 400);
	}
}
