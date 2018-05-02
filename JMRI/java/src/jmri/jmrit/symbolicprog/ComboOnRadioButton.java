package jmri.jmrit.symbolicprog;

import javax.swing.JComboBox;
import javax.swing.JRadioButton;

/* Represents a JComboBox as a JPanel containing just the "on" button
 *
 * @author   Bob Jacobsen   Copyright (C) 2001
 */
public class ComboOnRadioButton extends ComboRadioButtons {

    ComboOnRadioButton(JComboBox<String> box, EnumVariableValue var) {
        super(box, var);
    }

    /**
     * Make only the "on" button visible
     */
    @Override
    void addToPanel(JRadioButton b, int i) {
        if (i == 1) {
            add(b);
        }
    }

}
