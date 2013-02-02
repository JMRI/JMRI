//ComboOffRadioButton.java

package jmri.jmrit.symbolicprog;

import org.apache.log4j.Logger;
import javax.swing.*;

/* Represents a JComboBox as a JPanel containing just the "off" button
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision$
 */
public class ComboOffRadioButton extends ComboRadioButtons {

    ComboOffRadioButton(JComboBox box, EnumVariableValue var) {
        super(box, var);
    }

    ComboOffRadioButton(JComboBox box, IndexedEnumVariableValue var) {
        super(box, var);
    }

    /**
     * Make only the "on" button visible
     */
    void addToPanel(JRadioButton b, int i) {
        if (i==0) add(b);
    }

    // initialize logging
    static Logger log = Logger.getLogger(ComboOffRadioButton.class.getName());

}
