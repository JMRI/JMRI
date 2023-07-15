package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;

/**
 * Swing action to create and register a SignalHeadTable GUI.
 *
 * @author Bob Jacobsen Copyright (C) 2003,2006,2007, 2008, 2009
 * @author Petr Koud'a Copyright (C) 2007
 * @author Egbert Broerse Copyright (C) 2016
 */
public class SignalHeadTableAction extends AbstractTableAction<SignalHead> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s title of the action
     */
    public SignalHeadTableAction(String s) {
        super(s);
        // disable ourself if there is no primary Signal Head manager available
        if (InstanceManager.getNullableDefault(SignalHeadManager.class) == null) {
            super.setEnabled(false);
        }
    }

    public SignalHeadTableAction() {
        this(Bundle.getMessage("TitleSignalTable"));
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of SignalHeads.
     */
    @Override
    protected void createModel() {
        m = new SignalHeadTableModel();
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleSignalTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalHeadTable";
    }

    private SignalHeadAddEditFrame addFrame = null;

    /**
     * Provide GUI for adding a new SignalHead.
     * <p>
     * Because there are multiple options, each of which requires different
     * inputs, we directly manipulate which parts of the GUI are displayed when
     * the selected type is changed.
     *
     * @param e name of the event heard
     */
    @Override
    protected void addPressed(ActionEvent e) {
        if (addFrame == null) {
            addFrame = new SignalHeadAddEditFrame(null);
            addFrame.initComponents();
            addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    addFrame = null;
                }
            });
        } else {
            // clear older entries
            addFrame.setVisible(true);
            addFrame.resetAddressFields();
        }
    }

    @Override
    public void dispose() {
        if (addFrame !=null){
            addFrame.setVisible(false);
            addFrame.dispose();
            addFrame = null;
        }
        super.dispose();
    }

    @Override
    protected String getClassName() {
        return SignalHeadTableAction.class.getName();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleSignalTable");
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalHeadTableAction.class);

}
