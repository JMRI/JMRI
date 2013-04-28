// UpdateDecoderDefinitionAction.java
package jmri.jmrit.decoderdefn;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update the decoder definitions in the roster
 *
 * @author	Bob Jacobsen Copyright (C) 2013
 * @version	$Revision$
 * @see jmri.jmrit.XmlFile
 */
public class UpdateDecoderDefinitionAction extends JmriAbstractAction {

    public UpdateDecoderDefinitionAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public UpdateDecoderDefinitionAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public UpdateDecoderDefinitionAction(String s) {
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
    static Logger log = LoggerFactory.getLogger(UpdateDecoderDefinitionAction.class.getName());
}
