// SetPhysicalLocationFrame.java

package jmri.jmrit.beantable;

import jmri.jmrit.operations.OperationsFrame;
import jmri.util.PhysicalLocationPanel;
import jmri.util.PhysicalLocation;
import jmri.Reporter;
import jmri.ReporterManager;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.Frame;

import javax.swing.AbstractAction;

/**
 * Swing action to create a SetPhysicalLocation dialog.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @author Mark Underwood Copyright (C) 2011
 * @version $Revision: 18711 $
 */
public class SetPhysicalLocationAction extends AbstractAction {

    Reporter _reporter;

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.JmritBeantablePhysicalLocationBundle");

    public SetPhysicalLocationAction(String s, Reporter reporter) {
    	super(s);
	_reporter = reporter;
    }

    SetPhysicalLocationFrame f = null;
    public void actionPerformed(ActionEvent e) {
        // create a copy route frame
    	if (f == null || !f.isVisible()){
    		f = new SetPhysicalLocationFrame(_reporter);
    	}
    	f.setExtendedState(Frame.NORMAL);
    }

    /**
     * Frame for setting train physical location coordinates for a Reporter.
     * 
     * @author Bob Jacobsen Copyright (C) 2001
     * @author Daniel Boudreau Copyright (C) 2010
     * @author Mark Underwood Copyright (C) 2011
     * @version $Revision: 20246 $
     */
    class SetPhysicalLocationFrame extends OperationsFrame {

	
	Reporter _reporter;

	List<Reporter> _reporterList = new ArrayList<Reporter>();
	
	// labels
	
	// text field
	
	// check boxes
	
	// major buttons
	JButton saveButton = new JButton(rb.getString("Save"));
	JButton closeButton = new JButton(rb.getString("Close"));
	
	// combo boxes
	javax.swing.JComboBox reporterBox = getReporterComboBox();
	
	// Spinners
	PhysicalLocationPanel physicalLocation;
	
	public SetPhysicalLocationFrame(Reporter l) {
	    super(rb.getString("MenuSetPhysicalLocation"));
	    
	    // Store the location (null if called from the list view)
	    _reporter = l;
	    
	    // general GUI config
	    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
	    
	    // set tool tips
	    saveButton.setToolTipText(rb.getString("TipSaveButton"));
	    closeButton.setToolTipText(rb.getString("TipCloseButton"));
	    
	    // Set up the panels
	    JPanel pLocation = new JPanel();
	    pLocation.setBorder(BorderFactory.createTitledBorder(rb.getString("ReporterName")));
	    pLocation.setToolTipText(rb.getString("TipSelectReporter"));
	    pLocation.add(reporterBox);
	    
	    physicalLocation = new PhysicalLocationPanel(rb.getString("PhysicalLocation"));
	    physicalLocation.setToolTipText(rb.getString("TipPhysicalLocation"));
	    physicalLocation.setVisible(true);
	    
	    JPanel pControl = new JPanel();
	    pControl.setLayout(new GridBagLayout());
	    pControl.setBorder(BorderFactory.createTitledBorder(""));
	    addItem(pControl, saveButton, 1, 0);
	    addItem(pControl, closeButton, 2, 0);
	    
	    getContentPane().add(pLocation);
	    getContentPane().add(physicalLocation);
	    getContentPane().add(pControl);
	    
	    // add help menu to window
	    addHelpMenu("package.jmri.jmrit.operations.Operations_SetTrainIconCoordinates",	true); // fix this later
	    
	    // setup buttons
	    addButtonAction(saveButton);
	    addButtonAction(closeButton);
	    
	    // setup combo box
	    addComboBoxAction(reporterBox);
	    reporterBox.setSelectedIndex(0);
	    
	    if (_reporter != null){
		reporterBox.setSelectedItem(_reporter);
	    }
	    
	    pack();
	    /*
	     * if (getWidth()<350) setSize(350, getHeight()); // height has to be
	     * tall enough for four train directions if (getHeight()<400)
	     * setSize(getWidth(), 400);
	     */
	    setPreferredSize(new Dimension(350, 200));
	    setMaximumSize(new Dimension(350, getHeight()));
	    setVisible(true);
	}

	javax.swing.JComboBox getReporterComboBox() {
	    ReporterManager mgr = jmri.InstanceManager.reporterManagerInstance();
	    String[] nameArray = mgr.getSystemNameArray();
	    List<String> displayList = new ArrayList<String>();
	    for (String name : nameArray) {
		Reporter r = mgr.getBySystemName(name);
		if (r != null) {
		    _reporterList.add(r);
		    displayList.add(r.getDisplayName());
		}
	    }
	    String[] sa = new String[displayList.size()];
	    displayList.toArray(sa);
	    javax.swing.JComboBox retv = new javax.swing.JComboBox(sa);
	    return(retv);
	    
	}
	
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
	    // check to see if a location has been selected
	    if (reporterBox.getSelectedItem() == null
		|| reporterBox.getSelectedItem().equals("")) {
		JOptionPane.showMessageDialog(null,
					      rb.getString("SelectLocationToEdit"),
					      rb.getString("NoLocationSelected"),
					      JOptionPane.ERROR_MESSAGE);
		return;
	    }
	    Reporter l = getReporterFromList();
	    if (l == null)
		return;
	    if (ae.getSource() == saveButton) {
		int value = JOptionPane.showConfirmDialog(null, MessageFormat.format(rb.getString("UpdatePhysicalLocation"),
										     new Object[] { l.getDisplayName() }), rb.getString("SaveLocation?"), JOptionPane.YES_NO_OPTION);
		if (value == JOptionPane.YES_OPTION)
		    saveSpinnerValues(l);
	    }
	    if (ae.getSource() == closeButton) {
		dispose();
	    }
	}
	
	Reporter getReporterFromList() {
	    String s = (String)reporterBox.getSelectedItem();
	    // Since we don't have "getByDisplayName()" we need to do this in two steps
	    Reporter r = jmri.InstanceManager.reporterManagerInstance().getByDisplayName(s);
	    //Reporter r = jmri.InstanceManager.reporterManagerInstance().getByUserName(s);
	    //if (r == null) {// no such user name
	    //r = jmri.InstanceManager.reporterManagerInstance().getBySystemName(s);
	    //}
	    // (if getBySystemName() returns null, just return null.)
	    return(r);
	}

	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
	    if (reporterBox.getSelectedItem() != null) {
		if (reporterBox.getSelectedItem().equals("")) {
		    resetSpinners();
		} else {
		    Reporter l = getReporterFromList();
		    loadSpinners(l);
		}
	    }
	}
	
	public void spinnerChangeEvent(javax.swing.event.ChangeEvent ae) {
	    if (ae.getSource() == physicalLocation) {
		Reporter l = getReporterFromList();
		if (l != null)
		    //l.setPhysicalLocation(physicalLocation.getValue());
		    physicalLocation.getValue().setBeanPhysicalLocation(l);
	    }
	}
	
	private void resetSpinners() {
	    // Reset spinners to zero.
	    physicalLocation.setValue(new PhysicalLocation());
	}
	
	private void loadSpinners(Reporter r) {
	    log.debug("Load spinners Reporter location " + r.getSystemName());
	    //physicalLocation.setValue(r.getPhysicalLocation());
	    physicalLocation.setValue(PhysicalLocation.getBeanPhysicalLocation(r));
	}
	
	// Unused. Carried over from SetTrainIconPosition or whatever it was
	// called...
	/*
	 * private void spinnersEnable(boolean enable){
	 * physicalLocation.setEnabled(enable); }
	 */
	
	private void saveSpinnerValues(Reporter r) {
	    log.debug("Save train icons coordinates for location " + r.getSystemName());
	    //r.setPhysicalLocation(physicalLocation.getValue());
	    physicalLocation.getValue().setBeanPhysicalLocation(r);
	}
	
	public void dispose() {
	    super.dispose();
	}
	
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(SetPhysicalLocationAction.class.getName());
}

/* @(#)SetPhysicalLocationAction.java */
