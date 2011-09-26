// TrainConductorFrame.java

package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;


/**
 * Conductor Frame.  Shows work at one location at a time.
 * 
 * @author Dan Boudreau Copyright (C) 2011
 * @version $Revision: 18630 $
 */

public class TrainConductorFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	static final ResourceBundle rbr = ResourceBundle.getBundle("jmri.jmrit.operations.routes.JmritOperationsRoutesBundle");

	Train _train = null;
	CarManager carManager = CarManager.instance();
	
	JScrollPane pickupPane;
	JScrollPane setoutPane;

	// labels
	JLabel textRailRoadName = new JLabel();
	JLabel textTrainName = new JLabel();
	JLabel textTrainDescription = new JLabel();
	JLabel textTrainComment = new JLabel();
	JLabel textTrainRouteComment = new JLabel();
	JLabel textTrainRouteLocationComment = new JLabel();
	JLabel textLocationComment = new JLabel();
	JLabel textLocationName = new JLabel();
	JLabel textNextLocationName = new JLabel();

	// major buttons
	JButton moveButton = new JButton(rb.getString("Move"));
	JButton selectButton = new JButton(rb.getString("Select"));
	JButton clearButton = new JButton(rb.getString("Clear"));

	// radio buttons
	
	// text field
	
	// combo boxes
	
	// panels
	JPanel pPickups = new JPanel();
	JPanel pSetouts = new JPanel();
	JPanel pTrainRouteLocationComment = new JPanel();
	JPanel pLocationComment = new JPanel();
	
	// check boxes
	List<JCheckBox> carCheckBoxes = new ArrayList<JCheckBox>();


	public TrainConductorFrame() {
		super();
	}

	public void initComponents(Train train) {
		_train = train;

	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	    
       	pickupPane = new JScrollPane(pPickups);
       	pickupPane.setBorder(BorderFactory.createTitledBorder(rb.getString("Pickup")));
       	pickupPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
       	pickupPane.setPreferredSize(new Dimension(200, 300));
       	
      	setoutPane = new JScrollPane(pSetouts);
      	setoutPane.setBorder(BorderFactory.createTitledBorder(rb.getString("SetOut")));
      	setoutPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      	setoutPane.setPreferredSize(new Dimension(200, 300));

	    //      Set up the panels
				
		// Layout the panel by rows
		
       	// row 2
       	JPanel pRow2 = new JPanel();
       	pRow2.setLayout(new BoxLayout(pRow2,BoxLayout.X_AXIS));
       	
		// row 2a (train name)
       	JPanel pTrainName = new JPanel();
       	pTrainName.setBorder(BorderFactory.createTitledBorder(rb.getString("Train")));
       	pTrainName.add(textTrainName);
       	
		// row 2b (train description)
       	JPanel pTrainDescription = new JPanel();
       	pTrainDescription.setBorder(BorderFactory.createTitledBorder(rb.getString("Description")));
       	pTrainDescription.add(textTrainDescription);
       	
		// row 2c (railroad name)
       	JPanel pRailRoadName = new JPanel();
       	pRailRoadName.setBorder(BorderFactory.createTitledBorder(rb.getString("RailroadName")));
       	pRailRoadName.add(textRailRoadName);
       	
       	textRailRoadName.setText(Setup.getRailroadName());
       	
       	pRow2.add(pTrainName);
       	pRow2.add(pTrainDescription);
       	pRow2.add(pRailRoadName);
       	
       	// row 4 (train comment)
       	JPanel pTrainComment = new JPanel();
       	pTrainComment.setBorder(BorderFactory.createTitledBorder("Train Comment"));
       	pTrainComment.add(textTrainComment);
       	
       	// row 6
       	JPanel pRow6 = new JPanel();
       	pRow6.setLayout(new BoxLayout(pRow6,BoxLayout.X_AXIS));
       	
       	// row 6a (train route comment)
       	JPanel pTrainRouteComment = new JPanel();
       	pTrainRouteComment.setBorder(BorderFactory.createTitledBorder("Route Comment"));
       	pTrainRouteComment.add(textTrainRouteComment);
       		
       	// row 6b (train route location comment)
       	pTrainRouteLocationComment.setBorder(BorderFactory.createTitledBorder("Route Location Comment"));
       	pTrainRouteLocationComment.add(textTrainRouteLocationComment);
       	
       	pRow6.add(pTrainRouteComment);
       	pRow6.add(pTrainRouteLocationComment);
       	
       	// row 10
       	JPanel pRow10 = new JPanel();
       	pRow10.setLayout(new BoxLayout(pRow10,BoxLayout.X_AXIS));
       	
       	// row 10a (location name)
       	JPanel pLocationName = new JPanel();
       	pLocationName.setBorder(BorderFactory.createTitledBorder("Location"));
       	pLocationName.add(textLocationName);
       	
       	// row 10b (location comment)
       	pLocationComment.setBorder(BorderFactory.createTitledBorder("Location Comment"));
       	pLocationComment.add(textLocationComment);
       	
      	// row 10c (next location name)
       	JPanel pNextLocationName = new JPanel();
       	pNextLocationName.setBorder(BorderFactory.createTitledBorder("Next Location"));
       	pNextLocationName.add(textNextLocationName);
       	
       	pRow10.add(pLocationName);
       	pRow10.add(pLocationComment);
       	pRow10.add(pNextLocationName);
       	
       	// row 12
       	JPanel pRow12 = new JPanel();
       	pRow12.setLayout(new BoxLayout(pRow12,BoxLayout.X_AXIS));

       	pPickups.setLayout(new BoxLayout(pPickups,BoxLayout.Y_AXIS));
       	//pPickups.setPreferredSize(new Dimension(200, 200));
       	pSetouts.setLayout(new BoxLayout(pSetouts,BoxLayout.Y_AXIS));
       	//pSetouts.setPreferredSize(new Dimension(200, 200));
       	pRow12.add(pickupPane);
       	pRow12.add(setoutPane);
       	
       	// row 14
       	JPanel pRow14 = new JPanel();
       	pRow14.setLayout(new BoxLayout(pRow14,BoxLayout.X_AXIS));
       	
       	// row 14a
      	JPanel pWork = new JPanel();
      	pWork.setLayout(new GridBagLayout());
      	pWork.setBorder(BorderFactory.createTitledBorder("Work"));      	
       	addItem(pWork, selectButton, 0, 0);
       	addItem(pWork, clearButton, 1, 0);
       	
       	// row 14b
      	JPanel pButtons = new JPanel();
      	pButtons.setLayout(new GridBagLayout());
      	pButtons.setBorder(BorderFactory.createTitledBorder("Train"));
       	addItem(pButtons, moveButton, 1, 0);
       	
       	pRow14.add(pWork);
       	pRow14.add(pButtons);
       	
       	update();
		
		getContentPane().add(pRow2);
		getContentPane().add(pTrainComment);
		getContentPane().add(pRow6);
		getContentPane().add(pRow10);
		getContentPane().add(pRow12);
		//getContentPane().add(pickupPane);
		getContentPane().add(pRow14);
		
		// setup buttons
       	addButtonAction(selectButton);
       	addButtonAction(clearButton);
		addButtonAction(moveButton);
		
		// tool tips
		
		
		if (_train != null){
			textTrainName.setText(_train.getName());
			textTrainDescription.setText(_train.getDescription());
			// show train comment box only if there's a comment
			if (_train.getComment().equals(""))
				pTrainComment.setVisible(false);
			else
				textTrainComment.setText(_train.getComment());
			// show route comment box only if there's a route comment
			if (_train.getRoute() != null)
				if (_train.getRoute().getComment().equals(""))
					pTrainRouteComment.setVisible(false);
				else
					textTrainRouteComment.setText(_train.getRoute().getComment());
			
			// Does this train have a unique railroad name?
			if (!_train.getRailroadName().equals(""))
				textRailRoadName.setText(_train.getRailroadName());
			
			setTitle(rb.getString("TitleTrainConductor") + " ("+_train.getName()+")");

			// listen for train changes
			_train.addPropertyChangeListener(this);
		} 
		

		//	build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu(rb.getString("Tools"));
			
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true);
		
		packFrame();
    	setVisible(true);
		
	}
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == selectButton)
			selectCheckboxes(true);
		if (ae.getSource() == clearButton)
			selectCheckboxes(false);
		if (ae.getSource() == moveButton)
			_train.move();
	}
	
	protected void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		// confirm that all work is done
		for (int i=0; i<carCheckBoxes.size(); i++){
			JCheckBox checkBox = carCheckBoxes.get(i);
			if (!checkBox.isSelected()){
				moveButton.setEnabled(false);
				return;
			}
		}
		// all selected, work done!
		moveButton.setEnabled(true);
	}
	
	private void selectCheckboxes(boolean enable){
		for (int i=0; i < carCheckBoxes.size(); i++){
			JCheckBox checkBox = carCheckBoxes.get(i);
			checkBox.setSelected(enable);
		}
		moveButton.setEnabled(enable);
	}
	
	private void update(){
		carCheckBoxes.clear();
		if (_train != null && _train.getRoute() != null){
			RouteLocation rl = _train.getCurrentLocation();
			if (rl != null){
				pTrainRouteLocationComment.setVisible(!rl.getComment().equals(""));
				textTrainRouteLocationComment.setText(rl.getComment());
				textLocationName.setText(rl.getLocation().getName());
				pLocationComment.setVisible(!rl.getLocation().getComment().equals(""));
				textLocationComment.setText(rl.getLocation().getComment());				
				textNextLocationName.setText(_train.getNextLocationName());
				
				// now update the car pick ups and set outs
				List<String> carList = carManager.getByTrainDestinationList(_train);
				List<String> routeList = _train.getRoute().getLocationsBySequenceList();
				pPickups.removeAll();
				TrainCommon tc = new TrainCommon();
				// block pick ups by destination
				for (int j = 0; j < routeList.size(); j++) {
					RouteLocation rld = _train.getRoute().getLocationById(routeList.get(j));
					for (int k = 0; k < carList.size(); k++) {
						Car car = carManager.getById(carList.get(k));
						if (car.getRouteLocation() == rl && !car.getTrackName().equals("")
								&& car.getRouteDestination() == rld) {
							JCheckBox checkBox = new javax.swing.JCheckBox(tc.pickupCar(car));
							addCheckBoxAction(checkBox);
							pPickups.add(checkBox);
							carCheckBoxes.add(checkBox);
							moveButton.setEnabled(false);
						}
					}
				}
				pPickups.repaint();
				// set outs
				pSetouts.removeAll();
				for (int j=0; j<carList.size(); j++){
					Car car = carManager.getById(carList.get(j));
					if (car.getRouteDestination() == rl){
						JCheckBox checkBox = new javax.swing.JCheckBox(tc.dropCar(car));
						addCheckBoxAction(checkBox);
						pSetouts.add(checkBox);
						carCheckBoxes.add(checkBox);
						moveButton.setEnabled(false);
					}
				}
				pSetouts.repaint();
			}
		}
	}
    
    private void packFrame(){
    	setVisible(false);
 		pack();
		if (getWidth()<600)
			setSize(600, getHeight());
		if (getHeight()<Control.panelHeight)
			setSize(getWidth(), Control.panelHeight);
		setVisible(true);
    }
	
	public void dispose() {
		if (_train != null){
			_train.removePropertyChangeListener(this);
		}
		super.dispose();
	}

 	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()
				+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		update();
	}
 	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainConductorFrame.class.getName());
}
