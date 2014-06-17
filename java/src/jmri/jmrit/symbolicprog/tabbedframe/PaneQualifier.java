// PaneQualifier.java

package jmri.jmrit.symbolicprog.tabbedframe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.symbolicprog.*;
import javax.swing.JTabbedPane;

/**
 * Qualify a JMRI DecoderPro pane on a numerical relation
 * by enabling/disabling the tab
 *
 * @author			Bob Jacobsen   Copyright (C) 2010, 2014
 * @version			$Revision$
 *
 */
public class PaneQualifier extends ArithmeticQualifier {

    PaneProgPane pane;
    JTabbedPane tabs;
    int index;
    
    public PaneQualifier(PaneProgPane qualifiedPane, VariableValue watchedVal, int value, String relation, JTabbedPane tabPane, int index) {
        super(watchedVal, value, relation);
        
        this.pane = qualifiedPane;
        this.tabs = tabPane;
        this.index = index;
        
        setWatchedAvailable(currentDesiredState());
    }

    public void setWatchedAvailable(boolean enable) {
        log.info("setWatchedAvailable with "+enable+" on "+index);
        tabs.setEnabledAt(index, enable);
    }

    protected boolean currentAvailableState() {
        return tabs.isEnabledAt(index);
    }

    static Logger log = LoggerFactory.getLogger(PaneQualifier.class.getName());

}
