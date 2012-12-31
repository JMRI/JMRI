// SetTrainIconPositionFrame.java

package jmri.jmrit.operations.routes;
 
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainIcon;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ComponentListener;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Frame for setting train icon coordinates for a location.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version             $Revision$
 */
public class SetTrainIconPositionFrame extends OperationsFrame {
	
	RouteManager routeManager = RouteManager.instance();
	
	// labels
	JLabel textEastX = new JLabel("   X  ");
	JLabel textEastY = new JLabel("   Y  ");
	JLabel textWestX = new JLabel("   X  ");
	JLabel textWestY = new JLabel("   Y  ");
	JLabel textNorthX = new JLabel("   X  ");
	JLabel textNorthY = new JLabel("   Y  ");
	JLabel textSouthX = new JLabel("   X  ");
	JLabel textSouthY = new JLabel("   Y  ");

	// text field
	
	// check boxes

	// major buttons
	JButton placeButton = new JButton(Bundle.getString("PlaceTestIcon"));
	JButton applyButton = new JButton(Bundle.getString("UpdateRoutes"));
	JButton saveButton = new JButton(Bundle.getString("Save"));
	
	
	// combo boxes
	javax.swing.JComboBox locationBox = LocationManager.instance().getComboBox();
	
    //Spinners	 
    JSpinner spinTrainIconEastX = new JSpinner(new SpinnerNumberModel(0,0,10000,1));
    JSpinner spinTrainIconEastY = new JSpinner(new SpinnerNumberModel(0,0,10000,1));
    JSpinner spinTrainIconWestX = new JSpinner(new SpinnerNumberModel(0,0,10000,1));
    JSpinner spinTrainIconWestY = new JSpinner(new SpinnerNumberModel(0,0,10000,1));
    JSpinner spinTrainIconNorthX = new JSpinner(new SpinnerNumberModel(0,0,10000,1));
    JSpinner spinTrainIconNorthY = new JSpinner(new SpinnerNumberModel(0,0,10000,1));
    JSpinner spinTrainIconSouthX = new JSpinner(new SpinnerNumberModel(0,0,10000,1));
    JSpinner spinTrainIconSouthY = new JSpinner(new SpinnerNumberModel(0,0,10000,1));
    
	// Four test train icons
	TrainIcon _tIonEast;
	TrainIcon _tIonWest;
	TrainIcon _tIonNorth;
	TrainIcon _tIonSouth;
    
    public SetTrainIconPositionFrame() {
        super(Bundle.getString("MenuSetTrainIcon"));
        
        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        // set tool tips
        placeButton.setToolTipText(Bundle.getString("TipPlaceButton") +" "+ Setup.getPanelName());
        applyButton.setToolTipText(Bundle.getString("TipApplyAllButton"));
        saveButton.setToolTipText(Bundle.getString("TipSaveButton"));
	    
        //      Set up the panels
        JPanel pLocation = new JPanel();
        pLocation.setBorder(BorderFactory.createTitledBorder(Bundle.getString("Location")));
        pLocation.add(locationBox);
        
        JPanel pEast = new JPanel();
        pEast.setLayout(new GridBagLayout());
        pEast.setBorder(BorderFactory.createTitledBorder(Bundle.getString("EastTrainIcon")));
        addItem(pEast, textEastX, 0, 0);
        addItem(pEast, spinTrainIconEastX, 1, 0);
        addItem(pEast, textEastY, 2, 0);
        addItem(pEast, spinTrainIconEastY, 3, 0);     
        
        JPanel pWest = new JPanel();
        pWest.setLayout(new GridBagLayout());
        pWest.setBorder(BorderFactory.createTitledBorder(Bundle.getString("WestTrainIcon")));
        addItem(pWest, textWestX, 0, 0);
        addItem(pWest, spinTrainIconWestX, 1, 0);
        addItem(pWest, textWestY, 2, 0);
        addItem(pWest, spinTrainIconWestY, 3, 0);    
        
        JPanel pNorth = new JPanel();
        pNorth.setLayout(new GridBagLayout());
        pNorth.setBorder(BorderFactory.createTitledBorder(Bundle.getString("NorthTrainIcon")));
        addItem(pNorth, textNorthX, 0, 0);
        addItem(pNorth, spinTrainIconNorthX, 1, 0);
        addItem(pNorth, textNorthY, 2, 0);
        addItem(pNorth, spinTrainIconNorthY, 3, 0);    
        
        JPanel pSouth = new JPanel();
        pSouth.setLayout(new GridBagLayout());
        pSouth.setBorder(BorderFactory.createTitledBorder(Bundle.getString("SouthTrainIcon")));
        addItem(pSouth, textSouthX, 0, 0);
        addItem(pSouth, spinTrainIconSouthX, 1, 0);
        addItem(pSouth, textSouthY, 2, 0);
        addItem(pSouth, spinTrainIconSouthY, 3, 0); 
        
        JPanel pControl = new JPanel();
        pControl.setLayout(new GridBagLayout());
        pControl.setBorder(BorderFactory.createTitledBorder(""));
        addItem(pControl, placeButton, 0, 0);
        addItem(pControl, applyButton, 1, 0);
        addItem(pControl, saveButton, 2, 0);
        
        // only show valid directions

        pEast.setVisible((Setup.getTrainDirection() & Setup.EAST) > 0);
        pWest.setVisible((Setup.getTrainDirection() & Setup.WEST) > 0);
        pNorth.setVisible((Setup.getTrainDirection() & Setup.NORTH) > 0);
        pSouth.setVisible((Setup.getTrainDirection() & Setup.SOUTH) > 0);

        getContentPane().add(pLocation);
        getContentPane().add(pNorth);
        getContentPane().add(pSouth);
        getContentPane().add(pEast);
        getContentPane().add(pWest);
        getContentPane().add(pControl);
    	
        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.operations.Operations_SetTrainIconCoordinates", true);
    	
       	// setup buttons
		addButtonAction(placeButton);
		addButtonAction(applyButton);
		addButtonAction(saveButton);
		
		// setup combo box
		addComboBoxAction(locationBox);
		
		// setup spinners
		spinnersEnable(false);
		addSpinnerChangeListerner(spinTrainIconEastX);
		addSpinnerChangeListerner(spinTrainIconEastY);
		addSpinnerChangeListerner(spinTrainIconWestX);
		addSpinnerChangeListerner(spinTrainIconWestY);
		addSpinnerChangeListerner(spinTrainIconNorthX);
		addSpinnerChangeListerner(spinTrainIconNorthY);
		addSpinnerChangeListerner(spinTrainIconSouthX);
		addSpinnerChangeListerner(spinTrainIconSouthY);
		
    	pack();
     	if (getWidth()<350) 
    		setSize(350, getHeight());
     	// height has to be tall enough for four train directions
    	if (getHeight()<400)
    		setSize(getWidth(), 400);
       	setVisible(true);
    }
     
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
    	// check to see if a location has been selected 
    	if (locationBox.getSelectedItem() == null || locationBox.getSelectedItem().equals("")){
    		JOptionPane.showMessageDialog(null, Bundle.getString("SelectLocationToEdit"), Bundle.getString("NoLocationSelected"), JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	Location l = (Location)locationBox.getSelectedItem();
    	if (l == null)
    		return;		
    	if (ae.getSource() == placeButton){		
    		placeTestIcons();		
    	}
    	if (ae.getSource() == applyButton){
    		// update all routes?
    		int value = JOptionPane.showConfirmDialog(null,
    				MessageFormat.format(Bundle.getString("UpdateTrainIcon"),new Object[]{l.getName()}),
    				Bundle.getString("DoYouWantAllRoutes"), 
					JOptionPane.YES_NO_OPTION);
    		if (value == JOptionPane.YES_OPTION){
    			saveSpinnerValues(l);
    			updateTrainIconCoordinates(l);
    		}  			
    	}
    	if (ae.getSource() == saveButton){
   	  		int value = JOptionPane.showConfirmDialog(null,
	  				MessageFormat.format(Bundle.getString("UpdateTrainIcon"),new Object[]{l.getName()}),
    				Bundle.getString("UpdateDefaults"), 
					JOptionPane.YES_NO_OPTION);
	   		if (value == JOptionPane.YES_OPTION)
	   			saveSpinnerValues(l);
	   		OperationsXml.save();	// save location and route files
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
    	}
    }
	
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (locationBox.getSelectedItem() != null){
			if (locationBox.getSelectedItem().equals("")){
				resetSpinners();
				removeIcons();
			} else {
				Location l = (Location)locationBox.getSelectedItem();
				loadSpinners(l);
			}
		}
	}
	
	public void spinnerChangeEvent(javax.swing.event.ChangeEvent ae) {
		if (ae.getSource() == spinTrainIconEastX && _tIonEast != null){
			 _tIonEast.setLocation((Integer)spinTrainIconEastX.getValue(), _tIonEast.getLocation().y);
		}
		if (ae.getSource() == spinTrainIconEastY && _tIonEast != null){
			 _tIonEast.setLocation(_tIonEast.getLocation().x, (Integer)spinTrainIconEastY.getValue());
		}
		if (ae.getSource() == spinTrainIconWestX && _tIonWest != null){
			 _tIonWest.setLocation((Integer)spinTrainIconWestX.getValue(), _tIonWest.getLocation().y);
		}
		if (ae.getSource() == spinTrainIconWestY && _tIonWest != null){
			 _tIonWest.setLocation(_tIonWest.getLocation().x, (Integer)spinTrainIconWestY.getValue());
		}
		if (ae.getSource() == spinTrainIconNorthX && _tIonNorth != null){
			 _tIonNorth.setLocation((Integer)spinTrainIconNorthX.getValue(), _tIonNorth.getLocation().y);
		}
		if (ae.getSource() == spinTrainIconNorthY && _tIonNorth != null){
			 _tIonNorth.setLocation(_tIonNorth.getLocation().x, (Integer)spinTrainIconNorthY.getValue());
		}
		if (ae.getSource() == spinTrainIconSouthX && _tIonSouth != null){
			 _tIonSouth.setLocation((Integer)spinTrainIconSouthX.getValue(), _tIonSouth.getLocation().y);
		}
		if (ae.getSource() == spinTrainIconSouthY && _tIonSouth != null){
			 _tIonSouth.setLocation(_tIonSouth.getLocation().x, (Integer)spinTrainIconSouthY.getValue());
		}
	}
	
	private void resetSpinners(){
		spinnersEnable(false);
		spinTrainIconEastX.setValue(0); 
		spinTrainIconEastY.setValue(0);
		spinTrainIconWestX.setValue(0);
		spinTrainIconWestY.setValue(0);
		spinTrainIconNorthX.setValue(0);
		spinTrainIconNorthY.setValue(0);
		spinTrainIconSouthX.setValue(0);
		spinTrainIconSouthY.setValue(0);
	}
	
	private void loadSpinners(Location l){
		log.debug("Load spinners location "+l.getName());
		spinnersEnable(true);
		spinTrainIconEastX.setValue(l.getTrainIconEast().x); 
		spinTrainIconEastY.setValue(l.getTrainIconEast().y);
		spinTrainIconWestX.setValue(l.getTrainIconWest().x);
		spinTrainIconWestY.setValue(l.getTrainIconWest().y);
		spinTrainIconNorthX.setValue(l.getTrainIconNorth().x);
		spinTrainIconNorthY.setValue(l.getTrainIconNorth().y);
		spinTrainIconSouthX.setValue(l.getTrainIconSouth().x);
		spinTrainIconSouthY.setValue(l.getTrainIconSouth().y);
	}
	
	private void spinnersEnable(boolean enable){
		spinTrainIconEastX.setEnabled(enable); 
		spinTrainIconEastY.setEnabled(enable);
		spinTrainIconWestX.setEnabled(enable);
		spinTrainIconWestY.setEnabled(enable);
		spinTrainIconNorthX.setEnabled(enable);
		spinTrainIconNorthY.setEnabled(enable);
		spinTrainIconSouthX.setEnabled(enable);
		spinTrainIconSouthY.setEnabled(enable);
	}
	
	private void saveSpinnerValues(Location l){
		log.debug("Save train icons coordinates for location "+l.getName());
		l.setTrainIconEast(new Point((Integer)spinTrainIconEastX.getValue(), (Integer)spinTrainIconEastY.getValue()));
		l.setTrainIconWest(new Point((Integer)spinTrainIconWestX.getValue(), (Integer)spinTrainIconWestY.getValue()));
		l.setTrainIconNorth(new Point((Integer)spinTrainIconNorthX.getValue(), (Integer)spinTrainIconNorthY.getValue()));
		l.setTrainIconSouth(new Point((Integer)spinTrainIconSouthX.getValue(), (Integer)spinTrainIconSouthY.getValue()));	
	}
	
	// place test markers on panel
	private void placeTestIcons(){
		removeIcons();
		if (locationBox.getSelectedItem() == null || locationBox.getSelectedItem().equals(""))
			return;
		Editor editor = PanelMenu.instance().getEditorByName(Setup.getPanelName());
		if (editor == null) {
			JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getString("LoadPanel"), new Object[]{Setup.getPanelName()}),
					Bundle.getString("PanelNotFound"), JOptionPane.ERROR_MESSAGE);
			return;
		}
        Location l = (Location)locationBox.getSelectedItem();
		if (l != null) { 
			// East icon
			if ((Setup.getTrainDirection() & Setup.EAST) > 0){
				_tIonEast = editor.addTrainIcon(Bundle.getString("East"));
				_tIonEast.getTooltip().setText(l.getName());
				_tIonEast.getTooltip().setBackgroundColor(Color.white);
				_tIonEast.setLocoColor(Setup.getTrainIconColorEast());
				_tIonEast.setLocation((Integer)spinTrainIconEastX.getValue(), (Integer)spinTrainIconEastY.getValue());
				addIconListener(_tIonEast);
			}
			// West icon
			if ((Setup.getTrainDirection() & Setup.WEST) > 0){
				_tIonWest = editor.addTrainIcon(Bundle.getString("West"));
				_tIonWest.getTooltip().setText(l.getName());
				_tIonWest.getTooltip().setBackgroundColor(Color.white);
				_tIonWest.setLocoColor(Setup.getTrainIconColorWest());
				_tIonWest.setLocation((Integer)spinTrainIconWestX.getValue(), (Integer)spinTrainIconWestY.getValue());
				addIconListener(_tIonWest);
			}
			// North icon
			if ((Setup.getTrainDirection() & Setup.NORTH) > 0){
				_tIonNorth = editor.addTrainIcon(Bundle.getString("North"));
				_tIonNorth.getTooltip().setText(l.getName());
				_tIonNorth.getTooltip().setBackgroundColor(Color.white);
				_tIonNorth.setLocoColor(Setup.getTrainIconColorNorth());
				_tIonNorth.setLocation((Integer)spinTrainIconNorthX.getValue(), (Integer)spinTrainIconNorthY.getValue());
				addIconListener(_tIonNorth);
			}
			// South icon
			if ((Setup.getTrainDirection() & Setup.SOUTH) > 0){
				_tIonSouth = editor.addTrainIcon(Bundle.getString("South"));
				_tIonSouth.getTooltip().setText(l.getName());
				_tIonSouth.getTooltip().setBackgroundColor(Color.white);
				_tIonSouth.setLocoColor(Setup.getTrainIconColorSouth());
				_tIonSouth.setLocation((Integer)spinTrainIconSouthX.getValue(), (Integer)spinTrainIconSouthY.getValue());
				addIconListener(_tIonSouth);
			}
		}
	}
	
	public void updateTrainIconCoordinates(Location l){
		List<String> routes = RouteManager.instance().getRoutesByIdList();
		for (int i=0; i<routes.size(); i++){
			Route route = RouteManager.instance().getRouteById(routes.get(i));
			List<String> routeLocs = route.getLocationsBySequenceList();
			for (int j=0; j<routeLocs.size(); j++){
				RouteLocation rl = route.getLocationById(routeLocs.get(j));
				if (rl.getName().equals(l.getName())){
					log.debug("Updating train icon for route location "+rl.getName()+" in route "+route.getName());
					rl.setTrainIconCoordinates();
				}
			}
		}
	}
	
	private void removeIcons(){
    	if (_tIonEast != null)
    		_tIonEast.remove();
    	if (_tIonWest != null)
    		_tIonWest.remove();
    	if (_tIonNorth != null)
    		_tIonNorth.remove();
    	if (_tIonSouth != null)
    		_tIonSouth.remove();
	}
	
	private void addIconListener(TrainIcon tI) {
		tI.addComponentListener(new ComponentListener(){
			public void componentHidden(java.awt.event.ComponentEvent e) {}
			public void componentShown(java.awt.event.ComponentEvent e) {}
			public void componentMoved(java.awt.event.ComponentEvent e) {
				trainIconMoved(e);
			}
			public void componentResized(java.awt.event.ComponentEvent e) {}
		});
	}
	
	protected void trainIconMoved(java.awt.event.ComponentEvent ae) {
		if (ae.getSource() == _tIonEast){
			log.debug("East train icon X: "+_tIonEast.getLocation().x+" Y: "+_tIonEast.getLocation().y );	
			spinTrainIconEastX.setValue(_tIonEast.getLocation().x);
			spinTrainIconEastY.setValue(_tIonEast.getLocation().y);
		}
		if (ae.getSource() == _tIonWest){
			log.debug("West train icon X: "+_tIonWest.getLocation().x+" Y: "+_tIonWest.getLocation().y );
			spinTrainIconWestX.setValue(_tIonWest.getLocation().x);
			spinTrainIconWestY.setValue(_tIonWest.getLocation().y);
		}
		if (ae.getSource() == _tIonNorth){
			log.debug("North train icon X: "+_tIonNorth.getLocation().x+" Y: "+_tIonNorth.getLocation().y );
			spinTrainIconNorthX.setValue(_tIonNorth.getLocation().x);
			spinTrainIconNorthY.setValue(_tIonNorth.getLocation().y);
		}
		if (ae.getSource() == _tIonSouth){
			log.debug("South train icon X: "+_tIonSouth.getLocation().x+" Y: "+_tIonSouth.getLocation().y );
			spinTrainIconSouthX.setValue(_tIonSouth.getLocation().x);
			spinTrainIconSouthY.setValue(_tIonSouth.getLocation().y);
		}
	}

    public void dispose() {
    	removeIcons();
        super.dispose();
    }
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(SetTrainIconPositionFrame.class.getName());
}
