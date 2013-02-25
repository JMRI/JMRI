// DecoderIndexCreateAction.java

package jmri.jmrit.decoderdefn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.ActionEvent;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

/**
 * Update the decoder index and store
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2011
 * @version	$Revision$
 * @see         jmri.jmrit.XmlFile
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

    public void actionPerformed(ActionEvent e) {
        DecoderIndexFile.forceCreationOfNewIndex();
    }
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    
    // initialize logging
    static Logger log = LoggerFactory.getLogger(DecoderIndexCreateAction.class.getName());

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
