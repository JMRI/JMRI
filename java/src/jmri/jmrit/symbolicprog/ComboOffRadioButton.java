package jmri.jmrit.symbolicprog;

import javax.swing.JComboBox;
import javax.swing.JRadioButton;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/* Represents a JComboBox as a JPanel containing just the "off" button
 *
 * @author   Bob Jacobsen   Copyright (C) 2001, 2002
 */
@API(status = MAINTAINED)
public class ComboOffRadioButton extends ComboRadioButtons {

    ComboOffRadioButton(JComboBox<String> box, EnumVariableValue var) {
        super(box, var);
    }

    /**
     * Make only the "on" button visible
     */
    @Override
    void addToPanel(JRadioButton b, int i) {
        if (i == 0) {
            add(b);
        }
    }

}
