import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Java
 * @author Seongho Lee
 *
 */
public class JavaClassViewer extends JFrame {
	private String rawCode;
	private ClassParser classParser;
	private int viewerWidth;
	private int viewerHeight;

	private JTree classInfoTree;
	private JTextArea display;

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
	 * This method initializes components. Add a left and right panel, tree view, and
	 * so on.
	 */
	private void initComponents() {
		// Initialize classInfoTree
		classInfoTree = new JTree();
		classInfoTree.setModel(null); // set null model to show nothing
		
		// Initialize display
		display = new JTextArea(3, 20);
		
		/* Do Layout */
		// Set the main panel with the left and right panel.
		JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainPanel.setDividerLocation((int) (viewerWidth * 0.33));

		// Set the left panel.
		JSplitPane leftPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		leftPanel.setTopComponent(new JScrollPane(classInfoTree)); // add classInfoTree
		leftPanel.setBottomComponent(new JScrollPane(display)); // add display
		leftPanel.setDividerLocation((int) (viewerHeight * 0.66));
		mainPanel.setLeftComponent(leftPanel);

		// Set the right panel.
		JPanel rightPanel = new JPanel(new BorderLayout());
		mainPanel.setRightComponent(rightPanel);

		getContentPane().add(mainPanel);
		/* --------- */
	}

	/**
	 * Open the 'Java class file open dialog' and store the java class file.
	 *
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

	public void openClassFile() {
		// Load class file
		loadClassFile();

		// Parse the class file
		classParser = new ClassParser(rawCode);
		try {
			classParser.parse();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Set Tree View
		ClassInfoTreeModel model = new ClassInfoTreeModel(classParser.getClassInfo());
		classInfoTree.setModel(model);
	}

	public static void main(String[] args) {
		JavaClassViewer viewer = new JavaClassViewer(800, 400);
	}
}
