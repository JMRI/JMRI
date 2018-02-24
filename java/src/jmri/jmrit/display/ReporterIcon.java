package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPopupMenu;
import jmri.InstanceManager;
import jmri.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display info from a Reporter, e.g. transponder or RFID reader.<P>
 *
 * @author Bob Jacobsen Copyright (c) 2004
 */
public class ReporterIcon extends PositionableLabel implements java.beans.PropertyChangeListener {

    public ReporterIcon(Editor editor) {
        // super ctor call to make sure this is a String label
        super("???", editor);
        setText("???");
        setPopupUtility(new ReporterPopupUtil(this, this));
    }

    // suppress inappropriate menu items
    static class ReporterPopupUtil extends PositionablePopupUtil {

        ReporterPopupUtil(Positionable parent, javax.swing.JComponent textComp) {
            super(parent, textComp);
        }

        @Override
        public void setTextJustificationMenu(JPopupMenu popup) {
        }

        @Override
        public void setFixedTextMenu(JPopupMenu popup) {
        }

        @Override
        public void setTextMarginMenu(JPopupMenu popup) {
        }
    }
    // the associated Reporter object
    Reporter reporter = null;

    @Override
    public Positionable deepClone() {
        ReporterIcon pos = new ReporterIcon(_editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(ReporterIcon pos) {
        pos.setReporter(reporter.getSystemName());
        return super.finishClone(pos);
    }

    /**
     * Attached a named Reporter to this display item
     *
     * @param pName Used as a system/user name to lookup the Reporter object
     */
    public void setReporter(String pName) {
        if (InstanceManager.getNullableDefault(jmri.ReporterManager.class) != null) {
            try {
                reporter = InstanceManager.getDefault(jmri.ReporterManager.class).
                    provideReporter(pName);
                setReporter(reporter);
            } catch (IllegalArgumentException e) {
                log.error("Reporter '" + pName + "' not available, icon won't see changes");
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
        }
    }

    public Reporter getReporter() {
        return reporter;
    }

    // update icon as state changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("property change: "
                    + e.getPropertyName()
                    + " is now " + e.getNewValue());
        }
        displayState();
    }

    @Override
    public String getNameString() {
        String name;
        if (reporter == null) {
            name = Bundle.getMessage("NotConnected");
        } else if (reporter.getUserName() != null) {
            name = reporter.getUserName() + " (" + reporter.getSystemName() + ")";
        } else {
            name = reporter.getSystemName();
        }
        return name;
    }

    /**
     * Drive the current state of the display from the state of the Reporter.
     */
    void displayState() {
        if (reporter.getCurrentReport() != null) {
            if (reporter.getCurrentReport().equals("")) {
                setText(Bundle.getMessage("Blank"));
            } else {
                setText(reporter.getCurrentReport().toString());
            }
        } else {
            setText(Bundle.getMessage("NoReport"));
        }
        updateSize();
        return;
    }

    @Override
    protected void edit() {
        makeIconEditorFrame(this, "Reporter", true, null);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.reporterPickModelInstance());
        ActionListener addIconAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                editReporter();
            }
        };
        _iconEditor.complete(addIconAction, false, true, true);
        _iconEditor.setSelection(reporter);

    }

    void editReporter() {
        setReporter((Reporter) _iconEditor.getTableSelection());
        setSize(getPreferredSize().width, getPreferredSize().height);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        invalidate();
    }

    @Override
    public void dispose() {
        reporter.removePropertyChangeListener(this);
        reporter = null;

        super.dispose();
    }

    @Override
    public int maxHeight() {
        return ((javax.swing.JLabel) this).getMaximumSize().height;  // defer to superclass
    }

    @Override
    public int maxWidth() {
        return ((javax.swing.JLabel) this).getMaximumSize().width;  // defer to superclass
    }

    private final static Logger log = LoggerFactory.getLogger(ReporterIcon.class);
}
