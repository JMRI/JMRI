// DecoderIndexCreateAction.java

package jmri.jmrit.decoderdefn;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Check the names in an XML decoder file against the names.xml definitions
 *
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version	$Revision: 1.5 $
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
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderIndexCreateAction.class.getName());

}
