// DccAddressPanel.java

package jmri.jmrit.symbolicprog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import jmri.jmrit.symbolicprog.*;
import com.sun.java.util.collections.List;

/** 
 * Provide a graphical representation of the DCC address, either long or short
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: DccAddressPanel.java,v 1.2 2001-12-06 16:16:26 jacobsen Exp $
 */
public class DccAddressPanel extends JPanel {

	JTextField val = new JTextField(6);
	
	VariableValue primaryAddr = null;
	VariableValue extendAddr = null;
	EnumVariableValue addMode = null;
	
	VariableTableModel variableModel = null;
	
	public DccAddressPanel(VariableTableModel mod) {
		variableModel = mod;
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		add(new JLabel("DCC address: "));
		add(val);
		
		// arrange for the field to be updated when any of the variables change
		java.beans.PropertyChangeListener dccNews = new java.beans.PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent e) { updateDccAddress(); }
		};

		// connect to variables
		primaryAddr = variableModel.findVar("Primary Address");
		if (primaryAddr==null) log.error("DCC Address monitor did not find a Primary Address variable");
		else primaryAddr.addPropertyChangeListener(dccNews);
		extendAddr = variableModel.findVar("Extended Address");
		if (extendAddr==null) log.warn("DCC Address monitor did not find an Extended Address variable");
		else extendAddr.addPropertyChangeListener(dccNews);
		addMode = (EnumVariableValue)variableModel.findVar("Address Format");
		if (addMode==null) log.warn("DCC Address monitor didnt find an Address Format variable");
		else addMode.addPropertyChangeListener(dccNews);
		
		// connect this text field to the variables
		val.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatedTextField();
			}
		});
		
		// show the selection
		add(new JLabel("  extended addressing: "));
		add(addMode.getRep("checkbox"));
		
		// update initial contents & color
		if (addMode == null || extendAddr == null || addMode.getIntValue()==0) {
			// short address
			val.setBackground(primaryAddr.getValue().getBackground());
			val.setText(primaryAddr.getValueString());
		} else {
			// long address
			val.setBackground(extendAddr.getValue().getBackground());
			val.setText(extendAddr.getValueString());
		}
		
	}

	void updatedTextField() {
		// check whether this is long or short address
		if (addMode == null || extendAddr == null || addMode.getIntValue()==0) {
			// short address
			primaryAddr.setIntValue(Integer.valueOf(val.getText()).intValue());
			// now that value is set, get color
			val.setBackground(primaryAddr.getValue().getBackground());
		} else {
			// long address
			extendAddr.setIntValue(Integer.valueOf(val.getText()).intValue());
			// now that value is set, get color
			val.setBackground(extendAddr.getValue().getBackground());
		}
	}
	
	void updateDccAddress() {
		if (log.isDebugEnabled()) 
			log.debug("updateDccAddress: primary "+(primaryAddr==null?"<null>":primaryAddr.getValueString())+
						" extended "+(extendAddr==null?"<null>":extendAddr.getValueString())+
						" mode "+(addMode==null?"<null>":addMode.getValueString()));
		String newAddr = null;
		if (addMode == null || extendAddr == null || !addMode.getValueString().equals("1")) {
			// short address mode
			if (primaryAddr != null && !primaryAddr.getValueString().equals(""))
				newAddr = primaryAddr.getValueString();
		}
		else {
			// long address
			if (extendAddr != null && !extendAddr.getValueString().equals(""))
				newAddr = extendAddr.getValueString();
		}
		// update if needed
		if (newAddr!=null) val.setText(newAddr);
	}
				
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DccAddressPanel.class.getName());
		
}
