// TrainsScheduleTableFrame.java

package jmri.jmrit.operations.trains;
 
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableColumnModel;

import jmri.implementation.swing.SwingShutDownTask;
import jmri.jmrit.operations.OperationsFrame;


/**
 * Frame for adding and editing train schedules for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version             $Revision: 1.2 $
 */
public class TrainsScheduleTableFrame extends OperationsFrame {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	public static SwingShutDownTask trainDirtyTask;
	
	public static final String NAME = rb.getString("Name");	// Sort by choices
	public static final String TIME = rb.getString("Time");
		
	TrainManager trainManager = TrainManager.instance();
	TrainScheduleManager scheduleManager = TrainScheduleManager.instance();
	TrainManagerXml trainManagerXml = TrainManagerXml.instance();

	TrainsScheduleTableModel trainsScheduleModel = new TrainsScheduleTableModel();
	javax.swing.JTable trainsScheduleTable = new javax.swing.JTable(trainsScheduleModel);
	JScrollPane trainsPane;
	
	// labels
	JLabel textSort = new JLabel(rb.getString("SortBy"));
	JLabel textSep1 = new JLabel("          ");
	JLabel textSep2 = new JLabel("          ");
	
	// radio buttons
    JRadioButton sortByName = new JRadioButton(NAME);
    JRadioButton sortByTime = new JRadioButton(TIME);
    
    // radio button groups
   	ButtonGroup schGroup = new ButtonGroup();
        
	// major buttons
	JButton applyButton = new JButton(rb.getString("Apply"));
	JButton saveButton = new JButton(rb.getString("Save"));
	
	// check boxes
	
    public TrainsScheduleTableFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle").getString("TitleTimeTableTrains"));

        // general GUI configuration
        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

    	// Set up the jtable in a Scroll Pane..
    	trainsPane = new JScrollPane(trainsScheduleTable);
    	trainsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	trainsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
       	trainsScheduleModel.initTable(trainsScheduleTable, this);
     	
    	// Set up the control panel
    	
    	//row 1
    	JPanel cp1 = new JPanel();
    	cp1.add(textSort);
    	cp1.add(sortByTime);
    	cp1.add(sortByName);
    	
       	//row 2
    	JPanel cp2 = new JPanel();
    	List<String> l = scheduleManager.getSchedulesByIdList();
    	for (int i=0; i<l.size(); i++){
    		TrainSchedule ts = scheduleManager.getScheduleById(l.get(i));
    		JRadioButton b = new JRadioButton();
    		b.setText(ts.getName());
    		cp2.add(b);
    		schGroup.add(b);
    	}
    	
    	//row 3
    	//tool tips, see setPrintButtonText() for more tool tips
    	applyButton.setToolTipText(rb.getString("ApplyButtonTip"));
		//saveButton.setToolTipText(rb.getString("SaveBuildsTip"));
			
    	JPanel cp3 = new JPanel();
		cp3.add (applyButton);
		cp3.add (saveButton);
		
		// place controls in scroll pane
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridBagLayout());
		addItem(controlPanel, cp1, 0, 0 );
		addItem(controlPanel, cp2, 0, 1);
		addItem(controlPanel, cp3, 0, 2);
		
	    JScrollPane controlPane = new JScrollPane(controlPanel);
	    // make sure panel doesn't get too short
	    controlPane.setMinimumSize(new Dimension(50,90));
	    controlPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		
    	getContentPane().add(trainsPane);
	   	getContentPane().add(controlPane);
	   	
		// setup buttons
		addButtonAction(applyButton);
		addButtonAction(saveButton);
		
	   	ButtonGroup sortGroup = new ButtonGroup();
	   	sortGroup.add(sortByTime);
    	sortGroup.add(sortByName);
    	sortByName.setSelected(true);
    	
    	addRadioButtonAction(sortByTime);
		addRadioButtonAction(sortByName);	
    	
		//	build menu
		JMenuBar menuBar = new JMenuBar();
		//JMenu toolMenu = new JMenu(rb.getString("Tools"));
		//menuBar.add(toolMenu);
		setJMenuBar(menuBar);
    
        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true);
    		
    	pack();
    	
    	setSize(trainManager.getTrainScheduleFrameSize());
    	setLocation(trainManager.getTrainScheduleFramePosition());
    	setSortBy(trainManager.getTrainsFrameSortBy());
    }
    
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button actived");
		if (ae.getSource() == sortByName){
			trainsScheduleModel.setSort(trainsScheduleModel.SORTBYNAME);
		}
		if (ae.getSource() == sortByTime){
			trainsScheduleModel.setSort(trainsScheduleModel.SORTBYTIME);
		}
	}
	
	TrainSwitchListEditFrame tslef;
 
	// add, build, print, switch lists, terminate, and save buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("schedule train button activated");
		if (ae.getSource() == applyButton){
			applySchedule();
		}
		if (ae.getSource() == saveButton){
			storeValues();
		}
	}
	
	private void setSortBy(String sortBy){
		if(sortBy.equals(TIME)){
			sortByTime.setSelected(true);
			trainsScheduleModel.setSort(trainsScheduleModel.SORTBYTIME);
		}
	}

	public List<String> getSortByList(){
		return trainsScheduleModel.getSelectedTrainList();
	}

	private void applySchedule(){
		AbstractButton b;
		Enumeration<AbstractButton> en = schGroup.getElements();
		for (int i=0; i<schGroup.getButtonCount(); i++){
			b = en.nextElement();
			if (b.isSelected()){
				log.debug("schedule radio button "+b.getText());
				TrainSchedule ts = TrainScheduleManager.instance().getScheduleByName(b.getText());
				List<String> trains = trainManager.getTrainsByIdList();
				for (int j=0; j<trains.size(); j++){
					log.debug("train id: "+trains.get(j));
					Train train = trainManager.getTrainById(trains.get(j));
					train.setBuild(ts.containsTrainId(trains.get(j)));
				}
			}
		}
	}
	
	protected void storeValues(){
		trainManager.setTrainScheduleFrame(this);
		trainManager.setTrainScheduleFrameTableColumnWidths(getCurrentTableColumnWidths()); // save column widths
		trainManager.save();
	}
	
	protected int[] getCurrentTableColumnWidths(){
		TableColumnModel tcm = trainsScheduleTable.getColumnModel();
		int[] widths = new int[tcm.getColumnCount()];
		for (int i=0; i<tcm.getColumnCount(); i++)
			widths[i] = tcm.getColumn(i).getWidth();
		return widths;
	}
	
    public void dispose() {
    	trainsScheduleModel.dispose();
        super.dispose();
    }
      
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainsScheduleTableFrame.class.getName());
}
