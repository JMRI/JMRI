//ComboOffRadioButton.java

package jmri.jmrit.symbolicprog;

import java.awt.Component;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.beans.PropertyChangeEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

/* Represents a JComboBox as a JPanel containing just the "off" button
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.2 $
 */
public class ComboOffRadioButton extends ComboRadioButtons {

    ComboOffRadioButton(JComboBox box, EnumVariableValue var) {
        super(box, var);
    }

    /**
     * Make only the "on" button visible
     */
    void addToPanel(JRadioButton b, int i) {
        if (i==0) add(b);
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ComboOffRadioButton.class.getName());

}
