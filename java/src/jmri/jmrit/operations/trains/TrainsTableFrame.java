// TrainsTableFrame.java

package jmri.jmrit.operations.trains;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
//import javax.swing.table.TableColumnModel;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.setup.AutoSave;
import jmri.jmrit.operations.setup.BuildReportOptionAction;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OptionAction;
import jmri.jmrit.operations.setup.PrintOptionAction;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.com.sun.TableSorter;

/**
 * Frame for adding and editing the train roster for operations.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2011
 * @version $Revision$
 */
public class TrainsTableFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	// public static final String NAME = Bundle.getString("Name"); // Sort by choices
	// public static final String TIME = Bundle.getString("Time");
	// public static final String DEPARTS = Bundle.getString("Departs");
	// public static final String TERMINATES = Bundle.getString("Terminates");
	// public static final String ROUTE = Bundle.getString("Route");
	// public static final String STATUS = Bundle.getString("Status");
	// public static final String ID = Bundle.getString("Id");

	public static final String MOVE = Bundle.getString("Move");
	public static final String TERMINATE = Bundle.getString("Terminate");
	public static final String RESET = Bundle.getString("Reset");
	public static final String CONDUCTOR = Bundle.getString("Conductor");

	CarManagerXml carManagerXml = CarManagerXml.instance(); // load cars
	EngineManagerXml engineManagerXml = EngineManagerXml.instance(); // load engines
	TrainManager trainManager = TrainManager.instance();
	TrainManagerXml trainManagerXml = TrainManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();

	TrainsTableModel trainsModel;
	TableSorter sorter;
	JTable trainsTable;
	JScrollPane trainsPane;

	// radio buttons
	JRadioButton showTime = new JRadioButton(Bundle.getString("Time"));
	JRadioButton showId = new JRadioButton(Bundle.getString("Id"));

	JRadioButton moveRB = new JRadioButton(MOVE);
	JRadioButton terminateRB = new JRadioButton(TERMINATE);
	JRadioButton resetRB = new JRadioButton(RESET);
	JRadioButton conductorRB = new JRadioButton(CONDUCTOR);

	// major buttons
	JButton addButton = new JButton(Bundle.getString("Add"));
	JButton buildButton = new JButton(Bundle.getString("Build"));
	JButton printButton = new JButton(Bundle.getString("Print"));
	JButton openFileButton = new JButton(Bundle.getString("OpenFile"));
	JButton printSwitchButton = new JButton(Bundle.getString("SwitchLists"));
	JButton terminateButton = new JButton(Bundle.getString("Terminate"));
	JButton saveButton = new JButton(Bundle.getString("SaveBuilds"));

	// check boxes
	JCheckBox buildMsgBox = new JCheckBox(Bundle.getString("BuildMessages"));
	JCheckBox buildReportBox = new JCheckBox(Bundle.getString("BuildReport"));
	JCheckBox printPreviewBox = new JCheckBox(Bundle.getString("Preview"));
	JCheckBox openFileBox = new JCheckBox(Bundle.getString("OpenFile"));
	JCheckBox showAllBox = new JCheckBox(Bundle.getString("ShowAllTrains"));

	public TrainsTableFrame() {
		super();

		updateTitle();

		// create ShutDownTasks
		createShutDownTask();
		// always check for dirty operations files
		setModifiedFlag(true);

		// general GUI configuration
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Set up the jtable in a Scroll Pane..
		trainsModel = new TrainsTableModel();
		sorter = new TableSorter(trainsModel);
		trainsTable = new JTable(sorter);
		sorter.setTableHeader(trainsTable.getTableHeader());
		trainsPane = new JScrollPane(trainsTable);
		trainsModel.initTable(trainsTable, this);

		// Set up the control panel
		// row 1
		JPanel cp1 = new JPanel();
		cp1.setLayout(new BoxLayout(cp1, BoxLayout.X_AXIS));

		JPanel show = new JPanel();
		show.setBorder(BorderFactory.createTitledBorder(Bundle.getString("ShowClickToSort")));
		show.add(showTime);
		show.add(showId);

		JPanel messages = new JPanel();
		messages.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Options")));
		messages.add(showAllBox);
		messages.add(buildMsgBox);
		messages.add(buildReportBox);
		messages.add(printPreviewBox);
		messages.add(openFileBox);

		JPanel action = new JPanel();
		action.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Action")));
		action.add(moveRB);
		action.add(conductorRB);
		action.add(terminateRB);
		action.add(resetRB);

		cp1.add(show);
		cp1.add(messages);
		cp1.add(action);

		// tool tips, see setPrintButtonText() for more tool tips
		addButton.setToolTipText(Bundle.getString("AddTrain"));
		buildButton.setToolTipText(Bundle.getString("BuildSelectedTip"));
		printSwitchButton.setToolTipText(Bundle.getString("PreviewPrintSwitchLists"));

		terminateButton.setToolTipText(Bundle.getString("TerminateSelectedTip"));
		saveButton.setToolTipText(Bundle.getString("SaveBuildsTip"));
		openFileButton.setToolTipText(Bundle.getString("OpenFileButtonTip"));
		buildMsgBox.setToolTipText(Bundle.getString("BuildMessagesTip"));
		printPreviewBox.setToolTipText(Bundle.getString("PreviewTip"));
		openFileBox.setToolTipText(Bundle.getString("OpenFileTip"));
		showAllBox.setToolTipText(Bundle.getString("ShowAllTrainsTip"));

		moveRB.setToolTipText(Bundle.getString("MoveTip"));
		terminateRB.setToolTipText(Bundle.getString("TerminateTip"));
		resetRB.setToolTipText(Bundle.getString("ResetTip"));

		// row 2
		JPanel cp2 = new JPanel();
		cp2.setBorder(BorderFactory.createTitledBorder(""));
		cp2.add(addButton);
		cp2.add(buildButton);
		cp2.add(printButton);
		cp2.add(openFileButton);
		cp2.add(printSwitchButton);
		cp2.add(terminateButton);
		cp2.add(saveButton);

		// place controls in scroll pane
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.add(cp1);
		controlPanel.add(cp2);

		JScrollPane controlPane = new JScrollPane(controlPanel);
		// make sure control panel is the right size
		controlPane.setMinimumSize(new Dimension(500, 130));
		controlPane.setMaximumSize(new Dimension(2000, 200));
		controlPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

		getContentPane().add(trainsPane);
		getContentPane().add(controlPane);

		// setup buttons
		addButtonAction(addButton);
		addButtonAction(buildButton);
		addButtonAction(printButton);
		addButtonAction(openFileButton);
		addButtonAction(printSwitchButton);
		addButtonAction(terminateButton);
		addButtonAction(saveButton);

		ButtonGroup showGroup = new ButtonGroup();
		showGroup.add(showTime);
		showGroup.add(showId);
		showTime.setSelected(true);

		ButtonGroup actionGroup = new ButtonGroup();
		actionGroup.add(moveRB);
		actionGroup.add(conductorRB);
		actionGroup.add(terminateRB);
		actionGroup.add(resetRB);

		addRadioButtonAction(showTime);
		addRadioButtonAction(showId);

		addRadioButtonAction(moveRB);
		addRadioButtonAction(terminateRB);
		addRadioButtonAction(resetRB);
		addRadioButtonAction(conductorRB);

		buildMsgBox.setSelected(trainManager.isBuildMessagesEnabled());
		buildReportBox.setSelected(trainManager.isBuildReportEnabled());
		printPreviewBox.setSelected(trainManager.isPrintPreviewEnabled());
		openFileBox.setSelected(trainManager.isOpenFileEnabled());
		showAllBox.setSelected(trainsModel.isShowAll());

		// show open files only if create csv is enabled
		openFileBox.setVisible(Setup.isGenerateCsvManifestEnabled());
		openFileButton.setVisible(Setup.isGenerateCsvManifestEnabled());

		addCheckBoxAction(buildMsgBox);
		addCheckBoxAction(buildReportBox);
		addCheckBoxAction(printPreviewBox);
		addCheckBoxAction(showAllBox);
		addCheckBoxAction(openFileBox);

		// Set the button text to Print or Preview
		setPrintButtonText();
		// Set the train action button text to Move or Terminate
		setTrainActionButton();

		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(Bundle.getString("Tools"));
		toolMenu.add(new OptionAction(Bundle.getString("TitleOptions")));
		toolMenu.add(new PrintOptionAction());
		toolMenu.add(new BuildReportOptionAction());
		toolMenu.add(new TrainsByCarTypeAction(Bundle.getString("TitleModifyTrains")));
		toolMenu.add(new TrainsScheduleAction(Bundle.getString("TitleTimeTableTrains")));
		toolMenu.add(new TrainCopyAction(Bundle.getString("TitleTrainCopy")));
		toolMenu.add(new TrainsScriptAction(Bundle.getString("MenuItemScripts"), this));
		toolMenu.add(new PrintTrainsAction(Bundle.getString("MenuItemPrint"), new Frame(), false, this));
		toolMenu.add(new PrintTrainsAction(Bundle.getString("MenuItemPreview"), new Frame(), true, this));

		menuBar.add(toolMenu);
		menuBar.add(new jmri.jmrit.operations.OperationsMenu());
		setJMenuBar(menuBar);

		// add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true);

		pack();

		// listen for timetable changes
		trainManager.addPropertyChangeListener(this);
		// listen for location switch list changes
		addPropertyChangeLocations();

		// auto save
		new AutoSave();
	}

	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button activated");
		if (ae.getSource() == showId) {
			trainsModel.setSort(trainsModel.SORTBYID);
		}
		if (ae.getSource() == showTime) {
			trainsModel.setSort(trainsModel.SORTBYTIME);
		}
		if (ae.getSource() == moveRB) {
			trainManager.setTrainsFrameTrainAction(MOVE);
		}
		if (ae.getSource() == terminateRB) {
			trainManager.setTrainsFrameTrainAction(TERMINATE);
		}
		if (ae.getSource() == resetRB) {
			trainManager.setTrainsFrameTrainAction(RESET);
		}
		if (ae.getSource() == conductorRB) {
			trainManager.setTrainsFrameTrainAction(CONDUCTOR);
		}
	}

	TrainSwitchListEditFrame tslef;

	// add, build, print, switch lists, terminate, and save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		// log.debug("train button activated");
		if (ae.getSource() == addButton) {
			TrainEditFrame f = new TrainEditFrame();
			f.setTitle(Bundle.getString("TitleTrainAdd"));
			f.initComponents(null);
		}
		if (ae.getSource() == buildButton) {
			// use a thread to allow table updates during build
			Thread build = new Thread(new Runnable() {
				public void run() {
					buildTrains();
				}
			});
			build.setName("Build Trains");
			build.start();
		}
		if (ae.getSource() == printButton) {
			List<String> trains = getSortByList();
			for (int i = 0; i < trains.size(); i++) {
				Train train = trainManager.getTrainById(trains.get(i));
				if (train.isBuildEnabled() && !train.printManifestIfBuilt()
						&& trainManager.isBuildMessagesEnabled()) {
					JOptionPane.showMessageDialog(null, MessageFormat.format(
							Bundle.getString("NeedToBuildBeforePrinting"), new Object[] {
									train.getName(),
									(trainManager.isPrintPreviewEnabled() ? Bundle.getString("preview")
											: Bundle.getString("print")) }), MessageFormat.format(
							Bundle.getString("CanNotPrintManifest"), new Object[] { trainManager
									.isPrintPreviewEnabled() ? Bundle.getString("preview")
									: Bundle.getString("print") }), JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		if (ae.getSource() == openFileButton) {
			// Processes the CSV Manifest files using an external custom program.
			CustomManifest customManifest = new CustomManifest();
 
			List<String> trains = getSortByList();
			for (int i = 0; i < trains.size(); i++) {
				Train train = trainManager.getTrainById(trains.get(i));
				if (train.isBuildEnabled()) {
					if (!train.isBuilt() && trainManager.isBuildMessagesEnabled()) {
						JOptionPane
								.showMessageDialog(
										null,
										MessageFormat
												.format(Bundle.getString("NeedToBuildBeforeOpenFile"),
														new Object[] {
																train.getName(),
																(trainManager
																		.isPrintPreviewEnabled() ? Bundle.getString("preview")
																		: Bundle.getString("print")) }),
										MessageFormat
												.format(Bundle.getString("CanNotPrintManifest"),
														new Object[] { trainManager
																.isPrintPreviewEnabled() ? Bundle.getString("preview")
																: Bundle.getString("print") }),
										JOptionPane.ERROR_MESSAGE);
					} else {
						//train.openFile();
						
						// Make sure our csv manifest file exists for this Train. 
						File csvFile = train.createCSVManifestFile();
						
						// Add it to our collection to be processed.
						customManifest.AddCVSFile(csvFile);						
					}
				}
			}
			
			// Now run the user specified custom Manifest processor program
			customManifest.Process();
		}
		if (ae.getSource() == printSwitchButton) {
			if (tslef != null)
				tslef.dispose();
			tslef = new TrainSwitchListEditFrame();
			tslef.initComponents();
		}
		if (ae.getSource() == terminateButton) {
			List<String> trains = getSortByList();
			for (int i = 0; i < trains.size(); i++) {
				Train train = trainManager.getTrainById(trains.get(i));
				if (train.isBuildEnabled() && train.isBuilt() && train.isPrinted()) {
					train.terminate();
				} else if (train.isBuildEnabled() && train.isBuilt() && !train.isPrinted()) {
					int status = JOptionPane.showConfirmDialog(
							null,
							Bundle.getString("WarningTrainManifestNotPrinted"),
							MessageFormat.format(Bundle.getString("TerminateTrain"),
									new Object[] { train.getName(), train.getDescription() }),
							JOptionPane.YES_NO_OPTION);
					if (status == JOptionPane.YES_OPTION)
						train.terminate();
					// Quit?
					if (status == JOptionPane.CLOSED_OPTION)
						return;
				}
			}
		}
		if (ae.getSource() == saveButton) {
			storeValues();
		}
	}

	/**
	 * A thread is used to allow train table updates during builds.
	 */
	private void buildTrains() {
		List<String> trains = getSortByList();
		for (int i = 0; i < trains.size(); i++) {
			Train train = trainManager.getTrainById(trains.get(i));
			train.buildIfSelected();
		}
	}

	int _status = TableSorter.ASCENDING;

	protected String getSortBy() {
		// set the defaults
		String sortBy = TrainsTableModel.TIMECOLUMNNAME;
		_status = TableSorter.ASCENDING;
		// now look to see if a sort is active
		for (int i = 0; i < sorter.getColumnCount(); i++) {
			String name = sorter.getColumnName(i);
			int status = sorter.getSortingStatus(i);
			log.debug("Column " + name + " status " + status);
			if (status != TableSorter.NOT_SORTED && !name.equals("")) {
				sortBy = name;
				_status = status;
				break;
			}
		}
		return sortBy;
	}

	public List<String> getSortByList() {
		List<String> sysList;
		String sortBy = getSortBy();
		if (sortBy.equals(TrainsTableModel.IDCOLUMNNAME))
			sysList = trainManager.getTrainsByIdList();
		else if (sortBy.equals(TrainsTableModel.TIMECOLUMNNAME))
			sysList = trainManager.getTrainsByTimeList();
		else if (sortBy.equals(TrainsTableModel.DEPARTSCOLUMNNAME))
			sysList = trainManager.getTrainsByDepartureList();
		else if (sortBy.equals(TrainsTableModel.TERMINATESCOLUMNNAME))
			sysList = trainManager.getTrainsByTerminatesList();
		else if (sortBy.equals(TrainsTableModel.ROUTECOLUMNNAME))
			sysList = trainManager.getTrainsByRouteList();
		else if (sortBy.equals(TrainsTableModel.STATUSCOLUMNNAME))
			sysList = trainManager.getTrainsByStatusList();
		else
			sysList = trainManager.getTrainsByNameList();
		return sysList;
	}

	// Modifies button text and tool tips
	private void setPrintButtonText() {
		if (printPreviewBox.isSelected()) {
			printButton.setText(Bundle.getString("Preview"));
			printButton.setToolTipText(Bundle.getString("PreviewSelectedTip"));
			buildReportBox.setToolTipText(Bundle.getString("BuildReportPreviewTip"));
		} else {
			printButton.setText(Bundle.getString("Print"));
			printButton.setToolTipText(Bundle.getString("PrintSelectedTip"));
			buildReportBox.setToolTipText(Bundle.getString("BuildReportPrintTip"));
		}
	}

	private void setTrainActionButton() {
		moveRB.setSelected(trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.MOVE));
		terminateRB.setSelected(trainManager.getTrainsFrameTrainAction().equals(
				TrainsTableFrame.TERMINATE));
		resetRB.setSelected(trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.RESET));
		conductorRB.setSelected(trainManager.getTrainsFrameTrainAction().equals(
				TrainsTableFrame.CONDUCTOR));
	}

	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == buildMsgBox) {
			trainManager.setBuildMessagesEnabled(buildMsgBox.isSelected());
		}
		if (ae.getSource() == buildReportBox) {
			trainManager.setBuildReportEnabled(buildReportBox.isSelected());
		}
		if (ae.getSource() == printPreviewBox) {
			trainManager.setPrintPreviewEnabled(printPreviewBox.isSelected());
			setPrintButtonText(); // set the button text for Print or Preview
		}
		if (ae.getSource() == openFileBox) {
			trainManager.setOpenFileEnabled(openFileBox.isSelected());
		}
		if (ae.getSource() == showAllBox) {
			trainsModel.setShowAll(showAllBox.isSelected());
		}
	}

	private void updateTitle() {
		String title = Bundle.getString("TitleTrainsTable");
		TrainSchedule sch = TrainScheduleManager.instance().getScheduleById(
				trainManager.getTrainScheduleActiveId());
		if (sch != null)
			title = title + " (" + sch.getName() + ")";
		setTitle(title);
	}

	private void updateSwitchListButton() {
		log.debug("update switch list button");
		List<String> locations = locationManager.getLocationsByIdList();
		for (int i = 0; i < locations.size(); i++) {
			Location location = locationManager.getLocationById(locations.get(i));
			if (location != null && location.isSwitchListEnabled()
					&& location.getStatus().equals(Location.MODIFIED)) {
				printSwitchButton.setBackground(Color.RED);
				return;
			}
		}
		printSwitchButton.setBackground(Color.GREEN);
	}

	private synchronized void addPropertyChangeLocations() {
		List<String> locations = locationManager.getLocationsByIdList();
		for (int i = 0; i < locations.size(); i++) {
			Location location = locationManager.getLocationById(locations.get(i));
			if (location != null)
				location.addPropertyChangeListener(this);
		}
	}

	private synchronized void removePropertyChangeLocations() {
		List<String> locations = locationManager.getLocationsByIdList();
		for (int i = 0; i < locations.size(); i++) {
			Location location = locationManager.getLocationById(locations.get(i));
			if (location != null)
				location.removePropertyChangeListener(this);
		}
	}

	public void dispose() {
		/*
		 * all JMRI window position and size are now saved in user preference file
		 * trainManager.setTrainsFrameTableColumnWidths(getCurrentTableColumnWidths()); // save column widths
		 * trainManager.setTrainsFrame(null);
		 */
		trainsModel.dispose();
		trainManager.runShutDownScripts();
		trainManager.removePropertyChangeListener(this);
		removePropertyChangeLocations();
		super.dispose();
	}

	protected void handleModified() {
		if (OperationsXml.areFilesDirty()) {
			int result = javax.swing.JOptionPane.showOptionDialog(
					this,
					Bundle.getString("PromptQuitWindowNotWritten"),
					Bundle.getString("PromptSaveQuit"),
					javax.swing.JOptionPane.YES_NO_OPTION,
					javax.swing.JOptionPane.WARNING_MESSAGE,
					null, // icon
					new String[] {
							ResourceBundle.getBundle("jmri.util.UtilBundle").getString(
									"WarnYesSave"),
							ResourceBundle.getBundle("jmri.util.UtilBundle").getString(
									"WarnNoClose") },
					ResourceBundle.getBundle("jmri.util.UtilBundle").getString("WarnYesSave"));
			if (result == javax.swing.JOptionPane.NO_OPTION) {
				return;
			}
			// user wants to save
			storeValues();
		}
	}

	protected void storeValues() {
		super.storeValues();
		saveTableDetails(trainsTable);
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue());
		if (e.getPropertyName().equals(TrainManager.ACTIVE_TRAIN_SCHEDULE_ID))
			updateTitle();
		if (e.getPropertyName().equals(Location.STATUS_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Location.SWITCHLIST_CHANGED_PROPERTY))
			updateSwitchListButton();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainsTableFrame.class
			.getName());
}
