// DccAddressPanel.java

package jmri.jmrit.symbolicprog;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import jmri.jmrit.symbolicprog.*;
import com.sun.java.util.collections.List;

/** 
 * Provide a graphical representation of the DCC address, either long or short
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: DccAddressPanel.java,v 1.5 2001-12-10 06:30:25 jacobsen Exp $
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
		
		add(new JLabel("DCC Address: "));
		val.setToolTipText("This field shows the DCC address currently in use. CV1 provides the short address; CV17 & 18 provide the long address");
		add(val);
		
		// arrange for the field to be updated when any of the variables change
		java.beans.PropertyChangeListener dccNews = new java.beans.PropertyChangeListener() {
			public void propertyChange(java.beans.PropertyChangeEvent e) { updateDccAddress(); }
		};

		// connect to variables
		primaryAddr = variableModel.findVar("Primary Address");
		if (primaryAddr==null) log.error("DCC Address monitor did not find a Primary Address variable");

		extendAddr = variableModel.findVar("Extended Address");
		if (extendAddr==null) log.warn("DCC Address monitor did not find an Extended Address variable");

		addMode = (EnumVariableValue)variableModel.findVar("Address Format");
		if (addMode==null) log.warn("DCC Address monitor didnt find an Address Format variable");
		else addMode.addPropertyChangeListener(dccNews);
				
		// show the selection
		add(new JLabel("  Extended Addressing: "));
		add(addMode.getRep("checkbox"));
		
		// update initial contents & color
		if (addMode == null || extendAddr == null || addMode.getIntValue()==0) {
			// short address
			val.setBackground(primaryAddr.getValue().getBackground());
			val.setDocument( ((JTextField)primaryAddr.getValue()).getDocument());
		} else {
			// long address
			val.setBackground(extendAddr.getValue().getBackground());
			val.setDocument( ((JTextField)extendAddr.getValue()).getDocument());
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
			val.setDocument( ((JTextField)primaryAddr.getValue()).getDocument());
		}
		else {
			// long address
			val.setDocument( ((JTextField)extendAddr.getValue()).getDocument());
		}
		// update if needed
		if (newAddr!=null) val.setText(newAddr);
	}
				
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DccAddressPanel.class.getName());
		
}
