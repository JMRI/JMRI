package jmri.jmrix.lenz.swing.lv102;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register an LV102Frame object.
 * <p>
 * The {@link LV102Frame} is a configuration tool for the LV102 booster, and the
 * booster portion of an LZV100 command station.
 *
 * @author Paul Bender Copyright (C) 2004
 */
public class LV102Action extends AbstractAction {

    public LV102Action(String s) {
        super(s);
    }

    public LV102Action() {

        this(Bundle.getMessage("MenuItemLV102ConfigurationManager"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create an LV102Frame
        LV102Frame f = new LV102Frame(Bundle.getMessage("MenuItemLV102ConfigurationManager"));
        f.setVisible(true);
    }
}

