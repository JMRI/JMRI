// JComponentQualifier.java

package jmri.jmrit.symbolicprog.tabbedframe;


import jmri.jmrit.symbolicprog.*;
import javax.swing.*;

/**
 * Qualify a JComponent on a numerical test
 * by setting the JComponent's visibility
 *
 * @author			Bob Jacobsen   Copyright (C) 2010, 2014
 * @version			$Revision$
 *
 */
public class JComponentQualifier extends ArithmeticQualifier {

    JComponent component;
    
    public JComponentQualifier(JComponent component, VariableValue watchedVal, int value, String relation) {
        super(watchedVal, value, relation);
        
        this.component = component;
        
        setWatchedAvailable(currentDesiredState());
    }

    public void setWatchedAvailable(boolean enable) {
        component.setVisible(enable);
    }

    protected boolean currentAvailableState() {
        return component.isVisible();
    }

}
