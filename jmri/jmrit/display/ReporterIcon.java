package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Reporter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.*;

/**
 * An icon to display info from a Reporter, e.g. transponder or RFID reader.<P>
 *
 * @author Bob Jacobsen  Copyright (c) 2004
 * @version $Revision: 1.18 $
 */

public class ReporterIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public ReporterIcon() {
        // super ctor call to make sure this is a String label
        super("???");
        setDisplayLevel(PanelEditor.LABELS);
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
                 setReporter(reporter);
             } else {
                 log.error("Reporter '"+pName+"' not available, icon won't see changes");
             }
         } else {
             log.error("No ReporterManager for this protocol, icon won't see changes");
         }
     }

    public void setReporter(Reporter r) {
        if (reporter != null) {
            reporter.removePropertyChangeListener(this);
        }
        reporter = r;
        if (reporter != null) {
            displayState();
            reporter.addPropertyChangeListener(this);
            setProperToolTip();
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

    public String getNameString() {
        String name;
        if (reporter == null) name = rb.getString("NotConnected");
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

        checkLocationEditable(popup, getNameString());
        addFixedItem(popup);

        popup.add(makeFontSizeMenu());

        popup.add(makeFontStyleMenu());

        popup.add(makeFontColorMenu());
        
        popup.add(new AbstractAction(rb.getString("EditIcon")) {
                public void actionPerformed(ActionEvent e) {
                    edit();
                }
            });

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
        		setText(rb.getString("Blank"));
        	else
        	 	setText(reporter.getCurrentReport().toString());
        } else {
        	setText(rb.getString("NoReport"));
		}
		updateSize();
        return;
    }

    void edit() {
        if (_editorFrame != null) {
            _editorFrame.setLocationRelativeTo(null);
            _editorFrame.toFront();
            return;
        }
        _editor = new IconAdder();
        ActionListener addIconAction = new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                editReporter();
            }
        };
        makeAddIconFrame("EditReporter", "addReportValueToPanel", 
                                     "SelectReporter", _editor);
        _editor.setPickList(PickListModel.reporterPickModelInstance());
        _editor.complete(addIconAction, null, true, true);
        _editor.setSelection(reporter);

    }
    void editReporter() {
        setReporter((Reporter)_editor.getTableSelection());
        setSize(getPreferredSize().width, getPreferredSize().height);
        _editorFrame.dispose();
        _editorFrame = null;
        _editor = null;
        invalidate();
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


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReporterIcon.class.getName());
}
