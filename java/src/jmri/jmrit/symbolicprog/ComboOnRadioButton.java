//ComboOnRadioButton.java
package jmri.jmrit.symbolicprog;

import javax.swing.JComboBox;
import javax.swing.JRadioButton;

/* Represents a JComboBox as a JPanel containing just the "on" button
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */
public class ComboOnRadioButton extends ComboRadioButtons {

    /**
     *
     */
    private static final long serialVersionUID = -1301681065479961160L;

    ComboOnRadioButton(JComboBox<String> box, EnumVariableValue var) {
        super(box, var);
    }

    ComboOnRadioButton(JComboBox<String> box, IndexedEnumVariableValue var) {
        super(box, var);
    }

    /**
     * Make only the "on" button visible
     */
    void addToPanel(JRadioButton b, int i) {
        if (i == 1) {
            add(b);
        }
    }

}
