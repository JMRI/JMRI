// DecoderIndexCreateAction.java

package jmri.jmrit.decoderdefn;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Check the names in an XML decoder file against the names.xml definitions
 *
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version	$Revision: 1.6 $
 * @see         jmri.jmrit.XmlFile
 */
public class DecoderIndexCreateAction extends AbstractAction {

    public DecoderIndexCreateAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        DecoderIndexFile.forceCreationOfNewIndex();
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DecoderIndexCreateAction.class.getName());

}
