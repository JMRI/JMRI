// TrainsTableFrame.java

package jmri.jmrit.operations.trains;
 
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.ScrollPaneConstants;

import jmri.implementation.swing.SwingShutDownTask;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;

/**
 * Frame for adding and editing the train roster for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision: 1.28 $
 */
public class TrainsTableFrame extends OperationsFrame {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	public static SwingShutDownTask trainDirtyTask;
	
	public static final String NAME = rb.getString("Name");	// Sort by choices
	public static final String TIME = rb.getString("Time");
	public static final String ID = rb.getString("Id");

	CarManagerXml carMangerXml = CarManagerXml.instance();			
	EngineManagerXml engineMangerXml = EngineManagerXml.instance();
	TrainManager trainManager = TrainManager.instance();
	TrainManagerXml trainManagerXml = TrainManagerXml.instance();
	LocationManagerXml locationManagerXml = LocationManagerXml.instance();
	RouteManagerXml routeManagerXml = RouteManagerXml.instance();

	TrainsTableModel trainsModel = new TrainsTableModel();
	javax.swing.JTable trainsTable = new javax.swing.JTable(trainsModel);
	JScrollPane trainsPane;
	
	// labels
	JLabel textSort = new JLabel(rb.getString("SortBy"));
	JLabel textSep1 = new JLabel("          ");
	JLabel textSep2 = new JLabel();
	
	// radio buttons
    JRadioButton sortByName = new JRadioButton(NAME);
    JRadioButton sortByTime = new JRadioButton(TIME);
    JRadioButton sortById = new JRadioButton(ID);
     
	// major buttons
	JButton addButton = new JButton(rb.getString("Add"));
	JButton buildButton = new JButton(rb.getString("Build"));
	JButton printButton = new JButton(rb.getString("Print"));
	JButton printSwitchButton = new JButton(rb.getString("SwitchLists"));
	JButton terminateButton = new JButton(rb.getString("Terminate"));
	JButton saveButton = new JButton(rb.getString("SaveBuilds"));
	
	// check boxes
	javax.swing.JCheckBox buildMsgBox = new javax.swing.JCheckBox(rb.getString("BuildMessages"));
	javax.swing.JCheckBox buildReportBox = new javax.swing.JCheckBox(rb.getString("BuildReport"));
	javax.swing.JCheckBox printPreviewBox = new javax.swing.JCheckBox(rb.getString("PrintPreview"));

    public TrainsTableFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle").getString("TitleTrainsTable"));
        
        // create ShutDownTasks
        if (jmri.InstanceManager.shutDownManagerInstance() != null) {
			if (trainDirtyTask == null) {
				trainDirtyTask = new SwingShutDownTask(
						"Operations Train Window Check", rb.getString("PromptQuitWindowNotWritten"),
						rb.getString("PromptSaveQuit"), this) {
					public boolean checkPromptNeeded() {
						return !getModifiedFlag();
					}

					public boolean doPrompt() {
						storeValues();
						return true;
					}
				};
			}
            jmri.InstanceManager.shutDownManagerInstance().register(trainDirtyTask);
        }
        
        // general GUI configuration
        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

    	// Set up the jtable in a Scroll Pane..
    	trainsPane = new JScrollPane(trainsTable);
    	trainsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	trainsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
       	trainsModel.initTable(trainsTable, this);
     	
    	// Set up the control panel
    	
    	//row 1
    	JPanel cp1 = new JPanel();
    	cp1.add(textSort);
    	cp1.add(sortByName);
    	cp1.add(sortByTime);
    	cp1.add(sortById);
    	cp1.add(textSep1);
    	cp1.add(buildMsgBox);
    	cp1.add(buildReportBox);
    	cp1.add(printPreviewBox);
    	
    	//row 2
    	//tool tips
    	addButton.setToolTipText(rb.getString("AddTrain"));
		buildButton.setToolTipText(rb.getString("BuildSelected"));
		printButton.setToolTipText(rb.getString("PrintSelected"));
		printSwitchButton.setToolTipText(rb.getString("PreviewPrintSwitchLists"));
		terminateButton.setToolTipText(rb.getString("TerminateSelected"));
		saveButton.setToolTipText(rb.getString("SaveBuildsTip"));

    	JPanel cp2 = new JPanel();
		cp2.add (addButton);
		cp2.add (buildButton);
		cp2.add (printButton);
		cp2.add (printSwitchButton);
		cp2.add (terminateButton);
		cp2.add (saveButton);
		
		// place controls in scroll pane
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridBagLayout());
		addItem(controlPanel, cp1, 0, 0 );
		addItem(controlPanel, cp2, 0, 1);
		
	    JScrollPane controlPane = new JScrollPane(controlPanel);
	    // make sure panel doesn't get too short
	    controlPane.setMinimumSize(new Dimension(50,90));
	    controlPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		
    	getContentPane().add(trainsPane);
	   	getContentPane().add(controlPane);
	   	
		// setup buttons
		addButtonAction(addButton);
		addButtonAction(buildButton);
		addButtonAction(printButton);
		addButtonAction(printSwitchButton);
		addButtonAction(terminateButton);
		addButtonAction(saveButton);
		
	   	ButtonGroup sortGroup = new ButtonGroup();
    	sortGroup.add(sortByName);
    	sortGroup.add(sortByTime);
    	sortGroup.add(sortById);
    	sortByName.setSelected(true);
		addRadioButtonAction(sortByName);
		addRadioButtonAction(sortByTime);
		addRadioButtonAction(sortById);
		
		buildMsgBox.setSelected(trainManager.getBuildMessages());
    	buildReportBox.setSelected(trainManager.getBuildReport());
    	printPreviewBox.setSelected(trainManager.getPrintPreview()); 	
    	addCheckBoxAction(buildMsgBox);
		addCheckBoxAction(buildReportBox);
		addCheckBoxAction(printPreviewBox);
    	
		//	build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(rb.getString("Tools"));
		toolMenu.add(new PrintTrainsAction(rb.getString("MenuItemPrint"), new Frame(), false, this));
		toolMenu.add(new PrintTrainsAction(rb.getString("MenuItemPreview"), new Frame(), true, this));
		toolMenu.add(new ModifyTrainsAction(rb.getString("TitleModifyTrains")));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
    
        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true);
    		
    	pack();
    	setSize(trainManager.getTrainFrameSize());
    	setLocation(trainManager.getTrainFramePosition());
    	setSortBy(trainManager.getTrainFrameSortBy());
    	
    }
    
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button actived");
		if (ae.getSource() == sortByName){
			trainsModel.setSort(trainsModel.SORTBYNAME);
		}
		if (ae.getSource() == sortById){
			trainsModel.setSort(trainsModel.SORTBYID);
		}
		if (ae.getSource() == sortByTime){
			trainsModel.setSort(trainsModel.SORTBYTIME);
		}
	}
 
	// add, build, print, switch lists, terminate, and save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
//		log.debug("train button activated");
		if (ae.getSource() == addButton){
			TrainEditFrame f = new TrainEditFrame();
			f.setTitle(rb.getString("TitleTrainAdd"));
			f.initComponents(null);
		}
		if (ae.getSource() == buildButton){
			// use a thread to allow table updates during build
			Thread build = new Thread(new Runnable() {
				public void run() {
					buildTrains();
				}
			});
			build.setName("Build Trains");		
			build.start();		
		}
		if (ae.getSource() == printButton){
			List<String> trains = getTrainList();
			for (int i=0; i<trains.size(); i++){
				Train train = trainManager.getTrainById(trains.get(i));
				if(train.getBuild()){
					if(!train.printManifest()){
						String string = "Need to build train (" +train.getName()+ ") before printing manifest";
						JOptionPane.showMessageDialog(null, string,
								"Can not print manifest",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		if (ae.getSource() == printSwitchButton){
			TrainSwitchListEditFrame f = new TrainSwitchListEditFrame();
			f.initComponents();
			f.setTitle(rb.getString("TitleSwitchLists"));
			f.setVisible(true);
		}
		if (ae.getSource() == terminateButton){
			List<String> trains = getTrainList();
			for (int i=0; i<trains.size(); i++){
				Train train = trainManager.getTrainById(trains.get(i));
				if (train.getBuild() && train.getBuilt() && !train.getPrinted())
					if (JOptionPane.showConfirmDialog(null,
							"Warning, train manifest hasn't been printed!",
							"Terminate Train ("+train.getName()+")?", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
						return;
					}
				train.terminateIfSelected();
			}
		}
		
		if (ae.getSource() == saveButton){
			storeValues();
		}
	}
	
	/**
	 * A thread is used to allow train table updates during builds.
	 */
	private void buildTrains(){
		List<String> trains = getTrainList();
		for (int i=0; i<trains.size(); i++){
			Train train = trainManager.getTrainById(trains.get(i));
			boolean build = train.buildIfSelected();
			if (build)
				setModifiedFlag(true);
		}
	}
	
	private List<String> getTrainList(){
		List<String> trains;
		if (sortById.isSelected())
			trains = trainManager.getTrainsByIdList();
		else if (sortByTime.isSelected())
			trains = trainManager.getTrainsByTimeList();
		else
			trains = trainManager.getTrainsByNameList();
		return trains;
	}
	
	private void setSortBy(String sortBy){
		if(sortBy.equals(TIME)){
			sortByTime.setSelected(true);
			trainsModel.setSort(trainsModel.SORTBYTIME);
		}
		if(sortBy.equals(ID)){
			sortById.setSelected(true);
			trainsModel.setSort(trainsModel.SORTBYID);
		}
	}
	
	public List<String> getSortByList(){
		return trainsModel.getSelectedTrainList();
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		setModifiedFlag(true);
		if (ae.getSource() == buildMsgBox){
			trainManager.setBuildMessages(buildMsgBox.isSelected());
		}
		if (ae.getSource() == buildReportBox){
			trainManager.setBuildReport(buildReportBox.isSelected());
		}
		if (ae.getSource() == printPreviewBox){
			trainManager.setPrintPreview(printPreviewBox.isSelected());
		}
	}
	
	protected void storeValues(){
		trainManager.setTrainFrame(this);					//save frame size and location
		String sortBy = NAME;
		if (sortById.isSelected())
			sortBy = ID;
		else if (sortByTime.isSelected())
			sortBy = TIME;
		trainManager.setTrainFrameSortBy(sortBy);			//save how the table is sorted
		engineMangerXml.writeOperationsEngineFile();		//Need to save train assignments
		carMangerXml.writeOperationsCarFile();				//Need to save train assignments
		trainManagerXml.writeOperationsTrainFile();			//Need to save train status
		locationManagerXml.writeFileIfDirty();				//Need to save "moves" for track location 
		routeManagerXml.writeFileIfDirty(); 				//Only if user used setX&Y
		setModifiedFlag(false);
	}

    public void dispose() {
    	trainsModel.dispose();
    	trainManager.setTrainFrame(null);
        super.dispose();
    }
      
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainsTableFrame.class.getName());
}
