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
 * @version			$Revision: 1.3 $
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
        primaryAddr = variableModel.findVar("Short Address");
        if (primaryAddr==null) log.debug("DCC Address monitor did not find a Short Address variable");
        else primaryAddr.addPropertyChangeListener(dccNews);

        extendAddr = variableModel.findVar("Long Address");
        if (extendAddr==null) log.debug("DCC Address monitor did not find an Long Address variable");
        else extendAddr.addPropertyChangeListener(dccNews);

        addMode = (EnumVariableValue)variableModel.findVar("Address Format");
        if (addMode==null) log.debug("DCC Address monitor didnt find an Address Format variable");
        else addMode.addPropertyChangeListener(dccNews);

        // show the selection
        if (addMode != null) {
            add(new JLabel("  Extended Addressing: "));
            add(addMode.getRep("checkbox"));
        }

        // update initial contents & color
        if (addMode == null || extendAddr == null || addMode.getIntValue()==0) {
            if (primaryAddr!=null) {
                // short address
                val.setBackground(primaryAddr.getValue().getBackground());
                val.setDocument( ((JTextField)primaryAddr.getValue()).getDocument());
            }
        } else {
            // long address
            val.setBackground(extendAddr.getValue().getBackground());
            val.setDocument( ((JTextField)extendAddr.getValue()).getDocument());
        }

        // start listening for changes to this value
        val.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (addMode == null || extendAddr == null || !addMode.getValueString().equals("1")) {
                        if (primaryAddr!=null) {
                            // short address mode
                            ((DecVariableValue)primaryAddr).updatedTextField();
                            val.setBackground(primaryAddr.getValue().getBackground());
                            if (log.isDebugEnabled()) log.debug("set color: "+primaryAddr.getValue().getBackground());
                        }
                    }
                    else {
                        // long address
                        ((LongAddrVariableValue)extendAddr).updatedTextField();
                        val.setBackground(extendAddr.getValue().getBackground());
                        if (log.isDebugEnabled()) log.debug("set color: "+extendAddr.getValue().getBackground());
                    }
                }
            });
        val.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    if (log.isDebugEnabled()) log.debug("focusGained");
                    enterField();
                }
                public void focusLost(FocusEvent e) {
                    if (log.isDebugEnabled()) log.debug("focusLost");
                    exitField();
                }
            });

    }

    String oldContents = "";

    void enterField() {
        oldContents = val.getText();
    }
    void exitField() {
        if (!oldContents.equals(val.getText())) {
            if (addMode == null || extendAddr == null || !addMode.getValueString().equals("1")) {
                if (primaryAddr!=null) {
                    // short address mode
                    ((DecVariableValue)primaryAddr).updatedTextField();
                    val.setBackground(primaryAddr.getValue().getBackground());
                    if (log.isDebugEnabled()) log.debug("set color: "+primaryAddr.getValue().getBackground());
                }
            }
            else {
                // long address
                ((LongAddrVariableValue)extendAddr).updatedTextField();
                val.setBackground(extendAddr.getValue().getBackground());
                if (log.isDebugEnabled()) log.debug("set color: "+extendAddr.getValue().getBackground());
            }
        }
    }

    void updateDccAddress() {
        if (log.isDebugEnabled())
            log.debug("updateDccAddress: short "+(primaryAddr==null?"<null>":primaryAddr.getValueString())+
                      " long "+(extendAddr==null?"<null>":extendAddr.getValueString())+
                      " mode "+(addMode==null?"<null>":addMode.getValueString()));
        if (addMode == null || extendAddr == null || !addMode.getValueString().equals("1")) {
            if (primaryAddr!=null) {
                // short address mode
                val.setDocument( ((JTextField)primaryAddr.getValue()).getDocument());
                val.setBackground(primaryAddr.getValue().getBackground());
                if (log.isDebugEnabled()) log.debug("set color: "+primaryAddr.getValue().getBackground());
            }
        }
        else {
            // long address
            val.setDocument( ((JTextField)extendAddr.getValue()).getDocument());
            val.setBackground(extendAddr.getValue().getBackground());
            if (log.isDebugEnabled()) log.debug("set color: "+extendAddr.getValue().getBackground());
        }
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DccAddressPanel.class.getName());

}
