// DecoderIndexCreateAction.java

package jmri.jmrit.decoderdefn;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.io.*;
import javax.swing.*;
import org.jdom.*;
import org.jdom.input.*;
import com.sun.java.util.collections.List;

import jmri.jmrit.XmlFile;

/**
 * Check the names in an XML decoder file against the names.xml definitions
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.4 $
 * @see             jmri.jmrit.XmlFile
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
