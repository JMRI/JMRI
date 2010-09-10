// OptionFrame.java

package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JPanel;

import jmri.jmrit.operations.OperationsFrame;


/**
 * Frame for user edit of setup options
 * 
 * @author Dan Boudreau Copyright (C) 2010
 * @version $Revision: 1.4 $
 */

public class OptionFrame extends OperationsFrame{

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
	
	// labels

	// major buttons	
	JButton saveButton = new JButton(rb.getString("Save"));

	// radio buttons		
    
    // check boxes
	JCheckBox routerCheckBox = new JCheckBox(rb.getString("EnableCarRouting"));
	JCheckBox rfidCheckBox = new JCheckBox(rb.getString("EnableRfid"));
	JCheckBox carLoggerCheckBox = new JCheckBox(rb.getString("EnableCarLogging"));
	JCheckBox engineLoggerCheckBox = new JCheckBox(rb.getString("EnableEngineLogging"));
	
	// text field
	
	// combo boxes

	public OptionFrame() {
		super(ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle").getString("TitleOptions"));
	}

	public void initComponents() {
		
		// the following code sets the frame's initial state

		// load checkboxes	
		rfidCheckBox.setSelected(Setup.isRfidEnabled());
		routerCheckBox.setSelected(Setup.isCarRoutingEnabled());
		carLoggerCheckBox.setSelected(Setup.isCarLoggerEnabled());
		engineLoggerCheckBox.setSelected(Setup.isEngineLoggerEnabled());

		// add tool tips
		saveButton.setToolTipText(rb.getString("SaveToolTip"));
			
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		
		// Router panel
		JPanel pRouter = new JPanel();
		pRouter.setLayout(new GridBagLayout());
		pRouter.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutRouterOptions")));	
		addItem (pRouter, routerCheckBox, 1,0);
		
		// Logger panel
		JPanel pLogger = new JPanel();
		pLogger.setLayout(new GridBagLayout());
		pLogger.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutLoggerOptions")));		
		addItem (pLogger, engineLoggerCheckBox, 1,0);
		addItem (pLogger, carLoggerCheckBox, 1,1);
		
		JPanel pOption = new JPanel();
		pOption.setLayout(new GridBagLayout());
		pOption.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptions")));		
		addItem (pOption, rfidCheckBox, 1,0);
		
		// row 11
		JPanel pControl = new JPanel();
		pControl.setLayout(new GridBagLayout());
		addItem(pControl, saveButton, 3, 9);
		
		getContentPane().add(pRouter);
		getContentPane().add(pLogger);
		getContentPane().add(pOption);
		getContentPane().add(pControl);

		// setup buttons
		addButtonAction(saveButton);

		//	build menu		
		addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true);

		pack();
		setSize(getWidth(), getHeight()+25);	// pad out a bit
		setVisible(true);
	}
	
	// Save button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton){
			// Car routing enabled?
			Setup.setCarRoutingEnabled(routerCheckBox.isSelected());
			// RFID enabled?
			Setup.setRfidEnabled(rfidCheckBox.isSelected());
			// Logging enabled?		
			Setup.setEngineLoggerEnabled(engineLoggerCheckBox.isSelected());
			Setup.setCarLoggerEnabled(carLoggerCheckBox.isSelected());
			OperationsSetupXml.instance().writeOperationsFile();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(OptionFrame.class.getName());
}
