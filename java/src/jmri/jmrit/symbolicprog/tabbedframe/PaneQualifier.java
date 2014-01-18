// PaneQualifier.java

package jmri.jmrit.symbolicprog.tabbedframe;


import jmri.jmrit.symbolicprog.*;
import javax.swing.JTabbedPane;

/**
 * Qualify a variable on greater than or equal a number
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

    protected void setWatchedAvailable(boolean enable) {
        tabs.setEnabledAt(index, enable);
    }

    protected boolean currentAvailableState() {
        return tabs.isEnabledAt(index);
    }

}
