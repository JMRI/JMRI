package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Reporter;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.*;

import java.util.ResourceBundle;

/**
 * An icon to display info from a Reporter, e.g. transponder or RFID reader.<P>
 *   This routine is almost identical to ReporterIcon.java, written by Bob Jacobsen.  
 *   Differences are related to the hard interdependence between ReporterIconXml.java and 
 *   PanelEditor.java, which made it impossible to use ReporterIcon.java directly with 
 *   LayoutEditor. Rectifying these differences is especially important when storing and
 *   loading a saved panel. 
 * <P>
 * This module has been chaanged (from ReporterIcon.java) to use a resource bundle for 
 *	its user-seen text, like other Layout Editor modules.
 *
 * @author Dave Duchamp  Copyright (c) 2008
 * @version $Revision: 1.1 $
 */

public class LayoutReporterIcon extends LayoutPositionableLabel implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.LayoutEditorBundle");

    public LayoutReporterIcon() {
        // super ctor call to make sure this is a String label
        super("???");
        setDisplayLevel(LayoutEditor.LABELS);
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

    // update icon as state changes
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
		popup = new JPopupMenu();
		popup.add(new JMenuItem(getNameString()));
            
		// add x and y coordinates
		if (getViewCoordinates()) {
			popup.add("x= " + this.getX());
			popup.add("y= " + this.getY());
			popup.add(new AbstractAction(rb.getString("SetXY")) {
				public void actionPerformed(ActionEvent e) {
					String name = getNameString();
					displayCoordinateEdit(name);
				}
			});
		}

		popup.add(makeFontSizeMenu());

		popup.add(makeFontStyleMenu());

		popup.add(makeFontColorMenu());

		popup.add(new AbstractAction(rb.getString("Remove")) {
			public void actionPerformed(ActionEvent e) {
				remove();
				dispose();
			}
		});
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
        return ((javax.swing.JLabel)this).getMaximumSize().height;  // defer to superclass
    }
    protected int maxWidth() {
        return ((javax.swing.JLabel)this).getMaximumSize().width;  // defer to superclass
    }


    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutReporterIcon.class.getName());
}
