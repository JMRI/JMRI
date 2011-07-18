// DecoderIndexCreateAction.java

package jmri.jmrit.decoderdefn;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Check the names in an XML decoder file against the names.xml definitions
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2011
 * @version	$Revision$
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

    // main entry point to run standalone
    static public void main(String[] args) {
        String logFile = "default.lcf";
        try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure("default.lcf");
            } else {
                org.apache.log4j.BasicConfigurator.configure();
            }
        }
        catch (java.lang.NoSuchMethodError e) { System.out.println("Exception starting logging: "+e); }

        (new DecoderIndexCreateAction(null)).actionPerformed(null);
    }

}
