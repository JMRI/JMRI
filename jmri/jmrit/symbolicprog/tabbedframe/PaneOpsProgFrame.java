// PaneOpsProgFrame.java

package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import java.io.*;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;

import jmri.Programmer;
import jmri.ProgListener;
import jmri.ProgModePane;
import jmri.jmrit.symbolicprog.*;
import jmri.jmrit.decoderdefn.*;
import jmri.jmrit.roster.*;
import jmri.jmrit.XmlFile;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.DocType;
import org.jdom.output.XMLOutputter;
import org.jdom.JDOMException;

/**
 * Extend the PaneProgFrame to handle ops mode operations
 *
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Revision: 1.1 $
 */
public class PaneOpsProgFrame extends PaneProgFrame
							implements java.beans.PropertyChangeListener  {

    // commented out for now, as we only have the one ops mode
    // jmri.ProgModePane   modePane        = new jmri.ProgModePane(BoxLayout.X_AXIS);

    /**
     * Provide no programming-mode panel to the parent class.
     */
    JPanel getModePane() { return null; }

    /**
     * This invokes the parent ctor to do the real work. That will
     * call back to get the programming mode panel (none) and to
     * hear if there is read mode (no)
     *
     * @param decoderFile XML file defining the decoder contents
     * @param r RosterEntry for information on this locomotive
     * @param name
     * @param file
     */
    public PaneOpsProgFrame(DecoderFile decoderFile, RosterEntry r,
                            String name, String file, Programmer p) {
        super(decoderFile, r, name, file, p);

        if (log.isDebugEnabled()) log.debug("PaneOpsProgFrame \""+name
                                            +"\" constructed");
    }

    /**
     * local dispose, which also invokes parent. Note that
     * we remove the components (removeAll) before taking those
     * apart.
     */
    public void dispose() {

        if (log.isDebugEnabled()) log.debug("dispose local");

        super.dispose();

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneOpsProgFrame.class.getName());

}

