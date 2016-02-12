//ComboOffRadioButton.java
package jmri.jmrit.symbolicprog;

import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Represents a JComboBox as a JPanel containing just the "off" button
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision$
 */
public class ComboOffRadioButton extends ComboRadioButtons {

    /**
     *
     */
    private static final long serialVersionUID = 8850743261536717555L;

    ComboOffRadioButton(JComboBox<String> box, EnumVariableValue var) {
        super(box, var);
    }

    ComboOffRadioButton(JComboBox<String> box, IndexedEnumVariableValue var) {
        super(box, var);
    }

    /**
     * Make only the "on" button visible
     */
    void addToPanel(JRadioButton b, int i) {
        if (i == 0) {
            add(b);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ComboOffRadioButton.class.getName());

}
