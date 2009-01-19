// CarsTableFrame.java

 package jmri.jmrit.operations.rollingstock.cars;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;

/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision: 1.8 $
 */
public class CarsTableFrame extends OperationsFrame implements PropertyChangeListener{
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");

	CarsTableModel carsModel = new CarsTableModel();
	JTable carsTable = new JTable(carsModel);
	JScrollPane carsPane;
	
	// labels
	JLabel numCars = new JLabel();
	JLabel textCars = new JLabel();
	JLabel textSort = new JLabel();
	JLabel textSep1 = new JLabel();
	JLabel textSep2 = new JLabel();
	
	// radio buttons
	
    JRadioButton sortByNumber = new JRadioButton(rb.getString("Number"));
    JRadioButton sortByRoad = new JRadioButton(rb.getString("Road"));
    JRadioButton sortByType = new JRadioButton(rb.getString("Type"));
    JRadioButton sortByColor = new JRadioButton(rb.getString("Color"));
    JRadioButton sortByLoad = new JRadioButton(rb.getString("Load"));
    JRadioButton sortByKernel = new JRadioButton(rb.getString("Kernel"));
    JRadioButton sortByLocation = new JRadioButton(rb.getString("Location"));
    JRadioButton sortByDestination = new JRadioButton(rb.getString("Destination"));
    JRadioButton sortByTrain = new JRadioButton(rb.getString("Train"));
    JRadioButton sortByMoves = new JRadioButton(rb.getString("Moves"));
    ButtonGroup group = new ButtonGroup();
    
	// major buttons
	JButton addButton = new JButton();
	JButton findButton = new JButton();
	
	JTextField findCarTextBox = new JTextField(6);

    public CarsTableFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle").getString("TitleCarsTable"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

    	// Set up the jtable in a Scroll Pane..
    	carsPane = new JScrollPane(carsTable);
    	carsPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
       	carsModel.initTable(carsTable);
     	getContentPane().add(carsPane);
     	
     	// Set up the control panel
    	JPanel controlPanel = new JPanel();
    	controlPanel.setLayout(new FlowLayout());
    	numCars.setText(Integer.toString(CarManager.instance().getNumEntries()));
    	CarManager.instance().addPropertyChangeListener(this);
    	textCars.setText(rb.getString("cars"));
    	controlPanel.add(numCars);
    	controlPanel.add(textCars);
       	textSep1.setText("          ");
    	controlPanel.add(textSep1);
    	
    	textSort.setText(rb.getString("SortBy"));
    	controlPanel.add(textSort);
    	controlPanel.add(sortByNumber);
    	sortByNumber.setSelected(true);
    	controlPanel.add(sortByRoad);
    	controlPanel.add(sortByType);
    	controlPanel.add(sortByColor);
    	controlPanel.add(sortByLoad);
    	controlPanel.add(sortByKernel);
    	controlPanel.add(sortByLocation);
    	controlPanel.add(sortByDestination);
    	controlPanel.add(sortByTrain);
    	controlPanel.add(sortByMoves);
    	textSep2.setText("          ");
    	controlPanel.add(textSep2);

		addButton.setText(rb.getString("Add"));
		addButton.setVisible(true);
		controlPanel.add (addButton);
		
		findButton.setText(rb.getString("Find"));
		findButton.setToolTipText(rb.getString("findCar"));
		findButton.setVisible(true);
		findCarTextBox.setToolTipText(rb.getString("findCar"));
		controlPanel.add (findButton);
		controlPanel.add (findCarTextBox);
		controlPanel.setMaximumSize(new Dimension(Control.panelWidth, 50));
		
	   	getContentPane().add(controlPanel);
	   	
		// setup buttons
		addButtonAction(addButton);
		addButtonAction(findButton);
		
		addRadioButtonAction (sortByNumber);
		addRadioButtonAction (sortByRoad);
		addRadioButtonAction (sortByType);
		addRadioButtonAction (sortByColor);
		addRadioButtonAction (sortByLoad);
		addRadioButtonAction (sortByKernel);
		addRadioButtonAction (sortByLocation);
		addRadioButtonAction (sortByDestination);
		addRadioButtonAction (sortByTrain);
		addRadioButtonAction (sortByMoves);
		
		group.add(sortByNumber);
		group.add(sortByRoad);
		group.add(sortByType);
		group.add(sortByColor);
		group.add(sortByLoad);
		group.add(sortByKernel);
		group.add(sortByLocation);
		group.add(sortByDestination);
		group.add(sortByTrain);
		group.add(sortByMoves);
    	
 		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu("Tools");
		toolMenu.add(new CarRosterMenu("Roster", CarRosterMenu.MAINMENU, this));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Cars", true);
    	
    	pack();
    	if ((getWidth()<Control.panelWidth)) setSize(Control.panelWidth, getHeight());
    	
    }
    
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button actived");
		if (ae.getSource() == sortByNumber){
			carsModel.setSort(carsModel.SORTBYNUMBER);
		}
		if (ae.getSource() == sortByRoad){
			carsModel.setSort(carsModel.SORTBYROAD);
		}
		if (ae.getSource() == sortByType){
			carsModel.setSort(carsModel.SORTBYTYPE);
		}
		if (ae.getSource() == sortByColor){
			carsModel.setSort(carsModel.SORTBYCOLOR);
		}
		if (ae.getSource() == sortByLoad){
			carsModel.setSort(carsModel.SORTBYLOAD);
		}
		if (ae.getSource() == sortByKernel){
			carsModel.setSort(carsModel.SORTBYKERNEL);
		}
		if (ae.getSource() == sortByLocation){
			carsModel.setSort(carsModel.SORTBYLOCATION);
		}
		if (ae.getSource() == sortByDestination){
			carsModel.setSort(carsModel.SORTBYDESTINATION);
		}
		if (ae.getSource() == sortByTrain){
			carsModel.setSort(carsModel.SORTBYTRAIN);
		}
		if (ae.getSource() == sortByMoves){
			carsModel.setSort(carsModel.SORTBYMOVES);
		}
	}
	
	public List<String> getSortByList(){
		return carsModel.getSelectedCarList();
	}
    
	CarsEditFrame f = null;
	
	// add or find button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
//		log.debug("car button actived");
		if (ae.getSource() == findButton){
			int rowindex = carsModel.findCarByRoadNumber(findCarTextBox.getText());
			if (rowindex < 0){
				JOptionPane.showMessageDialog(this,
						"Car with road number "+ findCarTextBox.getText()+ " not found", "Could not find car!",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			carsTable.changeSelection(rowindex, 0, false, false);
			return;
		}
		if (ae.getSource() == addButton){
			if (f != null)
				f.dispose();
			f = new CarsEditFrame();
			f.initComponents();
			f.setTitle(rb.getString("TitleCarAdd"));
			f.setVisible(true);
		}
	}

    public void dispose() {
    	carsModel.dispose();
    	if (f != null)
    		f.dispose();
        super.dispose();
    }
    
    public void propertyChange(PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(CarManager.LISTLENGTH_CHANGED_PROPERTY)) {
    		numCars.setText(Integer.toString(CarManager.instance().getNumEntries()));
    	}
    }
    
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(CarsTableFrame.class.getName());
}
