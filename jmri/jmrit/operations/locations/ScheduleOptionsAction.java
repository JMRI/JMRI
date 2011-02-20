//ScheduleOptionsAction.java

package jmri.jmrit.operations.locations;

import java.awt.GridBagLayout;
import java.awt.event.*;

import javax.swing.*;

import java.util.ResourceBundle;

import jmri.jmrit.operations.OperationsFrame;


/**
 * Action to launch schedule options.
 * @author Daniel Boudreau Copyright (C) 2010
 * @version     $Revision: 1.3 $
 */
public class ScheduleOptionsAction extends AbstractAction {
		
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	private ScheduleEditFrame _sef;
	
	public ScheduleOptionsAction(ScheduleEditFrame sef){
		super(rb.getString("MenuItemScheduleOptions"));
		_sef = sef;
	}
	
	 public void actionPerformed(ActionEvent e) {
		new ScheduleOptionsFrame(_sef);
	 }
	
}

class ScheduleOptionsFrame extends OperationsFrame{
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	// text field
	JTextField factorTextField = new JTextField(5);
	
	// radio buttons
	JRadioButton sequentialRadioButton = new JRadioButton(rb.getString("Sequential"));
	JRadioButton matchRadioButton = new JRadioButton(rb.getString("Match"));
	
    // major buttons
    JButton saveButton = new JButton(rb.getString("Save"));
    
    Track _track;
	
	public ScheduleOptionsFrame(ScheduleEditFrame sef){
		super();
		
		// the following code sets the frame's initial state
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	    
	    _track = sef._track;
		
		// load the panel
	   	// row 1
    	JPanel pFactor = new JPanel();
		pFactor.setLayout(new GridBagLayout());
		pFactor.setBorder(BorderFactory.createTitledBorder(rb.getString("ScheduleFactor")));
		addItem(pFactor, factorTextField, 0, 0);
		
		factorTextField.setToolTipText(rb.getString("TipScheduleFactor"));
		factorTextField.setText(Integer.toString(_track.getReservationFactor()));
		
		// row 2
		JPanel pMode = new JPanel();
		pMode.setLayout(new GridBagLayout());
		pMode.setBorder(BorderFactory.createTitledBorder(rb.getString("ScheduleMode")));
		addItem(pMode, sequentialRadioButton, 0, 0);
		addItem(pMode, matchRadioButton, 1, 0);
		
		sequentialRadioButton.setToolTipText(rb.getString("TipSequential"));
		matchRadioButton.setToolTipText(rb.getString("TipMatch"));
		ButtonGroup group = new ButtonGroup();
		group.add(sequentialRadioButton);
		group.add(matchRadioButton);
		
		sequentialRadioButton.setSelected(_track.getScheduleMode() == Track.SEQUENTIAL);
		matchRadioButton.setSelected(_track.getScheduleMode() == Track.MATCH);
		
		JPanel pControls = new JPanel();
		pControls.add(saveButton);
  	
    	// button action
    	addButtonAction(saveButton);
    	
    	getContentPane().add(pFactor);
    	getContentPane().add(pMode);
    	getContentPane().add(pControls);
    	
    	setTitle(rb.getString("MenuItemScheduleOptions"));
    	pack();
    	if (getWidth() < 300 || getHeight() < 200)
    		setSize(300, 200);
    	setVisible(true); 	
	}
	
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton){
			// confirm that factor is between 0 and 1000
			try {
				int factor = Integer.parseInt(factorTextField.getText());
				if (factor < 0 || factor > 1000){
					JOptionPane.showMessageDialog(this,
							rb.getString("FactorMustBeNumber"), rb.getString("ErrorFactor"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			} catch (NumberFormatException e){
				JOptionPane.showMessageDialog(this,
						rb.getString("FactorMustBeNumber"), rb.getString("ErrorFactor"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			_track.setReservationFactor(Integer.parseInt(factorTextField.getText()));
			if (sequentialRadioButton.isSelected())
				_track.setScheduleMode(Track.SEQUENTIAL);
			else
				_track.setScheduleMode(Track.MATCH);
			LocationManagerXml.instance().writeOperationsFile();
		}		
	}
	
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrackEditFrame.class.getName());
}
