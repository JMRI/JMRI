// GuiUtilBase.java

package jmri.util.swing;

import javax.swing.*;
import org.jdom.*;
import jmri.util.swing.*;

/**
 * Common utility methods for working with GUI items
 *
 * @author Bob Jacobsen  Copyright 2010
 * @version $Revision: 1.2 $
 */

public class GuiUtilBase {

    static Action actionFromNode(Element child, WindowInterface wi) {
        String name = null;
        Icon icon = null;
        
        if (child.getChild("name") != null) {
            name = child.getChild("name").getText();
        }
        if (child.getChild("icon") != null) {
            icon = new ImageIcon(child.getChild("icon").getText());
        }
        
        if (child.getChild("adapter") != null) {
            String classname = child.getChild("adapter").getText();
            JmriAbstractAction a = null;
            try {
                //JmriAbstractAction a =
                //    (JmriAbstractAction)Class.forName(classname).newInstance();
                //if (wi != null)
                //    a.setWindowInterface(wi);
                Class c = Class.forName(classname);
                for (java.lang.reflect.Constructor ct : c.getConstructors()) {
                    // look for one with right arguments
                    if (icon == null) {
                        Class[] parms = ct.getParameterTypes();
                        if (parms.length != 2) continue;
                        if (parms[0] != String.class) continue;
                        if (parms[1] != WindowInterface.class) continue;
                        // found it!
                        a = (JmriAbstractAction) ct.newInstance(new Object[]{name, wi});
                        a.setName(name);
                        return a;
                    } else {
                        Class[] parms = ct.getParameterTypes();
                        if (parms.length != 3) continue;
                        if (parms[0] != String.class) continue;
                        if (parms[1] != Icon.class) continue;
                        if (parms[2] != WindowInterface.class) continue;
                        // found it!
                        a = (JmriAbstractAction) ct.newInstance(new Object[]{name, icon, wi});
                        a.setName(name);
                        return a;
                    }
                }
                log.warn("Did not find suitable ctor for "+classname+(icon!=null?" with":" without")+" icon");
                return null;
            } catch (Exception e) {
                log.warn("failed to load GUI adapter class: "+classname+" due to: "+e);
                return null;
            }
        } else if ( child.getChild("panel") != null) {
            try {
                JmriNamedPaneAction act;
                if (icon == null)
                     act = new JmriNamedPaneAction(name, wi, child.getChild("panel").getText());
                else
                     act = new JmriNamedPaneAction(name, icon, wi, child.getChild("panel").getText());
                return act;
            } catch (Exception ex) {
                log.warn("could not load toolbar adapter class: "+child.getChild("panel").getText()
                        +" due to "+ex);
                return null;
            }
        } else if ( child.getChild("help") != null) {
            String reference = child.getChild("help").getText();
            return jmri.util.HelpUtil.getHelpAction(name,icon, reference);
        } else { // make from icon or text
            if (icon != null) 
                return new AbstractAction(name, icon){
                    public void actionPerformed(java.awt.event.ActionEvent e) {}
                    public String toString() { return (String) getValue(javax.swing.Action.NAME); }
                };
            else // then name must be present
                return new AbstractAction(name){
                    public void actionPerformed(java.awt.event.ActionEvent e) {}
                    public String toString() { return (String) getValue(javax.swing.Action.NAME); }
                };
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GuiUtilBase.class.getName());
}