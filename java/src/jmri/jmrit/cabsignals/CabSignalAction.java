package jmri.jmrit.cabsignals;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register a CabSignalPanel object.
 *
 * @author Paul Bender Copyright (C) 2003
 */
public class CabSignalAction extends JmriAbstractAction {

    public CabSignalAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public CabSignalAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public CabSignalAction(String s) {
        super(s);
    }

    public CabSignalAction() {
        this(Bundle.getMessage("MenuItemCabSignalPane"));
    }

    @Override
    public jmri.util.swing.JmriPanel makePanel() {
       CabSignalPane retval = new CabSignalPane();
       retval.initComponents();
       return retval;
    }

}
