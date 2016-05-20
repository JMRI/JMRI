// DecoderIndexCreateAction.java
package jmri.jmrit.decoderdefn;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update the decoder index and store
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2011
 * @version	$Revision$
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

    @Override
    public void actionPerformed(ActionEvent e) {
        DecoderIndexFile.forceCreationOfNewIndex();
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    // initialize logging
    static Logger log = LoggerFactory.getLogger(DecoderIndexCreateAction.class.getName());
}
