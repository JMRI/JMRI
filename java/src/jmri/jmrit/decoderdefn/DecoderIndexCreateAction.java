package jmri.jmrit.decoderdefn;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Update the decoder index and store
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2011
 * @see jmri.jmrit.XmlFile
 */
public class DecoderIndexCreateAction extends JmriAbstractAction {

    public DecoderIndexCreateAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public DecoderIndexCreateAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public DecoderIndexCreateAction(String s) {
        super(s);
    }

    boolean increment = false;

    public void setIncrement(boolean increment) {
        this.increment = increment;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DecoderIndexFile.forceCreationOfNewIndex(increment);
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
