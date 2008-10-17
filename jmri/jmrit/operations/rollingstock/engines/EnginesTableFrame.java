// EnginesTableFrame.java

 package jmri.jmrit.operations.rollingstock.engines;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
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
 * Frame for adding and editing the engine roster for operations.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision: 1.1 $
 */
public class EnginesTableFrame extends JmriJFrame implements PropertyChangeListener{
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");

	EnginesTableModel enginesModel = new EnginesTableModel();
	javax.swing.JTable enginesTable = new javax.swing.JTable(enginesModel);
	JScrollPane enginesPane;
	
	// labels
	javax.swing.JLabel numEngines = new javax.swing.JLabel();
	javax.swing.JLabel textEngines = new javax.swing.JLabel();
	javax.swing.JLabel textSort = new javax.swing.JLabel();
	javax.swing.JLabel textSep1 = new javax.swing.JLabel();
	javax.swing.JLabel textSep2 = new javax.swing.JLabel();
	
	// radio buttons
	
    javax.swing.JRadioButton sortByNumber = new javax.swing.JRadioButton(rb.getString("Number"));
    javax.swing.JRadioButton sortByRoad = new javax.swing.JRadioButton(rb.getString("Road"));
    javax.swing.JRadioButton sortByModel = new javax.swing.JRadioButton(rb.getString("Model"));
    javax.swing.JRadioButton sortByConsist = new javax.swing.JRadioButton(rb.getString("Consist"));
    javax.swing.JRadioButton sortByLocation = new javax.swing.JRadioButton(rb.getString("Location"));
    javax.swing.JRadioButton sortByDestination = new javax.swing.JRadioButton(rb.getString("Destination"));
    javax.swing.JRadioButton sortByTrain = new javax.swing.JRadioButton(rb.getString("Train"));
    javax.swing.JRadioButton sortByMoves = new javax.swing.JRadioButton(rb.getString("Moves"));
    ButtonGroup group = new ButtonGroup();
    
	// major buttons
	javax.swing.JButton addButton = new javax.swing.JButton();
	javax.swing.JButton findButton = new javax.swing.JButton();
	
	javax.swing.JTextField findEngineTextBox = new javax.swing.JTextField(6);

    public EnginesTableFrame() {
        super(ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle").getString("TitleEnginesTable"));
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

    	// Set up the jtable in a Scroll Pane..
    	enginesPane = new JScrollPane(enginesTable);
    	enginesPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
       	enginesModel.initTable(enginesTable);
     	getContentPane().add(enginesPane);
     	
     	// Set up the control panel
    	JPanel controlPanel = new JPanel();
    	controlPanel.setLayout(new FlowLayout());
    	numEngines.setText(Integer.toString(EngineManager.instance().getNumEntries()));
    	EngineManager.instance().addPropertyChangeListener(this);
    	textEngines.setText(rb.getString("engines"));
    	controlPanel.add(numEngines);
    	controlPanel.add(textEngines);
       	textSep1.setText("          ");
    	controlPanel.add(textSep1);
    	
    	textSort.setText("Sort by");
    	controlPanel.add(textSort);
    	controlPanel.add(sortByNumber);
    	sortByNumber.setSelected(true);
    	controlPanel.add(sortByRoad);
    	controlPanel.add(sortByModel);
    	controlPanel.add(sortByConsist);
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
		findButton.setToolTipText(rb.getString("findEngine"));
		findButton.setVisible(true);
		findEngineTextBox.setToolTipText(rb.getString("findEngine"));
		controlPanel.add (findButton);
		controlPanel.add (findEngineTextBox);
		
	   	getContentPane().add(controlPanel);
	   	
		// setup buttons
		addButtonAction(addButton);
		addButtonAction(findButton);
		
		addRadioButtonAction (sortByNumber);
		addRadioButtonAction (sortByRoad);
		addRadioButtonAction (sortByModel);
		addRadioButtonAction (sortByConsist);
		addRadioButtonAction (sortByLocation);
		addRadioButtonAction (sortByDestination);
		addRadioButtonAction (sortByTrain);
		addRadioButtonAction (sortByMoves);
		
		group.add(sortByNumber);
		group.add(sortByRoad);
		group.add(sortByModel);
		group.add(sortByConsist);
		group.add(sortByLocation);
		group.add(sortByDestination);
		group.add(sortByTrain);
		group.add(sortByMoves);
    	
 		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu("Tools");
		toolMenu.add(new EngineRosterMenu("Roster", EngineRosterMenu.MAINMENU, this));
		toolMenu.add(new NceConsistEngineAction(rb.getString("MenuItemNceSync"), this));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
    	addHelpMenu("package.jmri.jmrit.operations.Operations_Engines", true);
    	
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
			enginesModel.setSort(enginesModel.SORTBYNUMBER);
		}
		if (ae.getSource() == sortByRoad){
			enginesModel.setSort(enginesModel.SORTBYROAD);
		}
		if (ae.getSource() == sortByModel){
			enginesModel.setSort(enginesModel.SORTBYTYPE);
		}
		if (ae.getSource() == sortByConsist){
			enginesModel.setSort(enginesModel.SORTBYCONSIST);
		}
		if (ae.getSource() == sortByLocation){
			enginesModel.setSort(enginesModel.SORTBYLOCATION);
		}
		if (ae.getSource() == sortByDestination){
			enginesModel.setSort(enginesModel.SORTBYDESTINATION);
		}
		if (ae.getSource() == sortByTrain){
			enginesModel.setSort(enginesModel.SORTBYTRAIN);
		}
		if (ae.getSource() == sortByMoves){
			enginesModel.setSort(enginesModel.SORTBYMOVES);
		}
	}
	
	public List getSortByList(){
		return enginesModel.getSelectedEngineList();
	}
    
	private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}
	
	EnginesEditFrame f = null;
	
	// add or find button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
//		log.debug("engine button actived");
		if (ae.getSource() == findButton){
			int rowindex = enginesModel.findEngineByRoadNumber(findEngineTextBox.getText());
			if (rowindex < 0){
				JOptionPane.showMessageDialog(this,
						"Engine with road number "+ findEngineTextBox.getText()+ " not found", "Could not find engine!",
						JOptionPane.INFORMATION_MESSAGE);
				return;
				
			}else{
				enginesTable.changeSelection(rowindex, 0, false, false);
			}
			return;
		}
		if (ae.getSource() == addButton){
			if (f != null)
				f.dispose();
			f = new EnginesEditFrame();
			f.initComponents();
			f.setTitle("Add Engine");
			f.setVisible(true);
		}
	}

    public void dispose() {
    	enginesModel.dispose();
    	if (f != null)
    		f.dispose();
        super.dispose();
    }
    
    public void propertyChange(PropertyChangeEvent e) {
    	if(Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(EngineManager.LISTLENGTH)) {
    		numEngines.setText(Integer.toString(EngineManager.instance().getNumEntries()));
    	}
    }
    
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(EnginesTableFrame.class.getName());
}
