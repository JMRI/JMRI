package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Reporter;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.*;

/**
 * An icon to display info from a Reporter, e.g. transponder or RFID reader.<P>
 *
 * @author Bob Jacobsen  Copyright (c) 2004
 * @version $Revision: 1.2 $
 */

public class ReporterIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public ReporterIcon() {
        // super ctor call to make sure this is a String label
        super("???");
        setText("???");
    }

    // the associated Reporter object
    Reporter reporter = null;

    /**
     * Attached a named Reporter to this display item
     * @param pName Used as a system/user name to lookup the Reporter object
     */
    public void setReporter(String pName) {
        if (InstanceManager.reporterManagerInstance()!=null) {
            reporter = InstanceManager.reporterManagerInstance().
                provideReporter(pName);
            if (reporter != null) {
                displayState();
                reporter.addPropertyChangeListener(this);
                setProperToolTip();
            } else {
                log.error("Reporter '"+pName+"' not available, icon won't see changes");
            }
        } else {
            log.error("No ReporterManager for this protocol, icon won't see changes");
        }
    }

    public Reporter getReporter() { return reporter; }

    // update icon as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property change: "
                                            +e.getPropertyName()
                                            +" is now "+e.getNewValue());
        displayState();
    }

    public void setProperToolTip() {
        setToolTipText(getNameString());
    }

    String getNameString() {
        String name;
        if (reporter == null) name = "<Not connected>";
        else if (reporter.getUserName()!=null)
            name = reporter.getUserName()+" ("+reporter.getSystemName()+")";
        else
            name = reporter.getSystemName();
        return name;
    }


    /**
     * Pop-up displays the turnout name, allows you to rotate the icons
     */
    protected void showPopUp(MouseEvent e) {
        if (!getEditable()) return;
        ours = this;
        if (popup==null) {
            popup = new JPopupMenu();
            popup.add(new JMenuItem(getNameString()));
            popup.add(new AbstractAction("Remove") {
                public void actionPerformed(ActionEvent e) {
                    remove();
                    dispose();
                }
            });

        } // end creation of pop-up menu

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Drive the current state of the display from the state of the
     * Reporter.
     */
    void displayState() {
        if (reporter.getCurrentReport()!=null) {
        	if (reporter.getCurrentReport().equals(""))
        		setText("<blank>");
        	else
        	 	setText(reporter.getCurrentReport().toString());
        } else {
        	setText("<no report>");
		}
		updateSize();
        return;
    }

    public void dispose() {
        reporter.removePropertyChangeListener(this);
        reporter = null;
        
        super.dispose();
    }

    protected int maxHeight() {
        return (new JLabel(this.getText())).getPreferredSize().height;
    }
    protected int maxWidth() {
        return (new JLabel(this.getText())).getPreferredSize().width;
    }


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ReporterIcon.class.getName());
}
