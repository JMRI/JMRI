// SamplePane.java

package jmri.util.swing;

import javax.swing.*;
import java.util.*;

/**
 * Sample Pane class for tests
 * @author			Bob Jacobsen  Copyright 2010
 * @version         $Revision: 1.1 $
 */

// sample class
public class SamplePane extends jmri.util.swing.JmriPanel {
    public SamplePane() {
    }
    public void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        num = ++index;

        JButton b;
        b = new JButton(new JmriNamedPaneAction("Next"+num,
                                getWindowInterface(),
                                jmri.util.swing.SamplePane.class.getName()));
        add(b);

        JmriNamedPaneAction act = new JmriNamedPaneAction("Extend"+num,
                                getWindowInterface(),
                                jmri.util.swing.SamplePane.class.getName());
        act.setHint(WindowInterface.Hint.EXTEND);
        b = new JButton(act);
        add(b);        

        b = new JButton("Close"+num);
        add(b);        
    }
    public String getTitle() { return "SamplePane "+num; }
    
    public void dispose() {
        disposed.add(new Integer(num));
        super.dispose();
    }
    
    int num;
    
    static public ArrayList<Integer> disposed;
    static public int index = 0;

}
