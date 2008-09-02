// CarsTableFrame.java

 package jmri.jmrit.operations.cars;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jmri.jmrit.operations.setup.Control;
import jmri.util.JmriJFrame;

/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision: 1.2 $
 */
public class CarsTableFrame extends JmriJFrame implements PropertyChangeListener{
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.cars.JmritOperationsCarsBundle");

	CarsTableModel carsModel = new CarsTableModel();
	javax.swing.JTable carsTable = new javax.swing.JTable(carsModel);
	JScrollPane carsPane;
	
	// labels
	javax.swing.JLabel numCars = new javax.swing.JLabel();
	javax.swing.JLabel textCars = new javax.swing.JLabel();
	javax.swing.JLabel textSort = new javax.swing.JLabel();
	javax.swing.JLabel textSep1 = new javax.swing.JLabel();
	javax.swing.JLabel textSep2 = new javax.swing.JLabel();
	
	// radio buttons
	
    javax.swing.JRadioButton sortByNumber = new javax.swing.JRadioButton(rb.getString("Number"));
    javax.swing.JRadioButton sortByRoad = new javax.swing.JRadioButton(rb.getString("Road"));
    javax.swing.JRadioButton sortByType = new javax.swing.JRadioButton(rb.getString("Type"));
    javax.swing.JRadioButton sortByKernel = new javax.swing.JRadioButton(rb.getString("Kernel"));
    javax.swing.JRadioButton sortByLocation = new javax.swing.JRadioButton(rb.getString("Location"));
    javax.swing.JRadioButton sortByDestination = new javax.swing.JRadioButton(rb.getString("Destination"));
    javax.swing.JRadioButton sortByTrain = new javax.swing.JRadioButton(rb.getString("Train"));
    javax.swing.JRadioButton sortByMoves = new javax.swing.JRadioButton(rb.getString("Moves"));
    ButtonGroup group = new ButtonGroup();
    
	// major buttons
	javax.swing.JButton addButton = new javax.swing.JButton();
	javax.swing.JButton findButton = new javax.swing.JButton();
	
	javax.swing.JTextField findCarTextBox = new javax.swing.JTextField(6);

    public CarsTableFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.cars.JmritOperationsCarsBundle").getString("TitleCarsTable"));
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
    	textCars.setText("cars");
    	controlPanel.add(numCars);
    	controlPanel.add(textCars);
       	textSep1.setText("          ");
    	controlPanel.add(textSep1);
    	
    	textSort.setText("Sort by");
    	controlPanel.add(textSort);
    	controlPanel.add(sortByNumber);
    	sortByNumber.setSelected(true);
    	controlPanel.add(sortByRoad);
    	controlPanel.add(sortByType);
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
		
	   	getContentPane().add(controlPanel);
	   	
		// setup buttons
		addButtonAction(addButton);
		addButtonAction(findButton);
		
		addRadioButtonAction (sortByNumber);
		addRadioButtonAction (sortByRoad);
		addRadioButtonAction (sortByType);
		addRadioButtonAction (sortByKernel);
		addRadioButtonAction (sortByLocation);
		addRadioButtonAction (sortByDestination);
		addRadioButtonAction (sortByTrain);
		addRadioButtonAction (sortByMoves);
		
		group.add(sortByNumber);
		group.add(sortByRoad);
		group.add(sortByType);
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
    
	private void addRadioButtonAction(JRadioButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				radioButtonActionPerformed(e);
			}
		});
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
	
	public List getSortByList(){
		return carsModel.getSelectedCarList();
	}
    
	private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
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
				
			}else{
				carsTable.changeSelection(rowindex, 0, false, false);
			}
			return;
		}
		if (ae.getSource() == addButton){
			if (f == null){
				f = new CarsEditFrame();
				f.initComponents();
				f.setTitle("Add Car");
			}
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
    	if (e.getPropertyName().equals(CarManager.LISTLENGTH)) {
    		numCars.setText(Integer.toString(CarManager.instance().getNumEntries()));
    	}
    }
    
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(CarsTableFrame.class.getName());
}
