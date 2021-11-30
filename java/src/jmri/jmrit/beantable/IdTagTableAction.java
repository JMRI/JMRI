package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.managers.DefaultRailComManager;
import jmri.managers.ProxyIdTagManager;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a IdTagTable GUI.
 *
 * @author  Bob Jacobsen Copyright (C) 2003
 * @author  Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class IdTagTableAction extends AbstractTableAction<IdTag> implements PropertyChangeListener {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName title of the action
     */
    public IdTagTableAction(String actionName) {
        super(actionName);
        init();
    }
    
    final void init(){
        tagManager.addPropertyChangeListener(this);
    }
    
    @Nonnull
    protected IdTagManager tagManager = InstanceManager.getDefault(IdTagManager.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void setManager(@Nonnull Manager<IdTag> t) {
        tagManager.removePropertyChangeListener(this);
        if (t instanceof IdTagManager) {
            tagManager = (IdTagManager) t;
            if (m != null) {
                m.setManager(tagManager);
            }
        }
        // if t is not an instance of IdTagManager, tagManager may not change.
        tagManager.addPropertyChangeListener(this);
    }

    public IdTagTableAction() {
        this(Bundle.getMessage("TitleIdTagTable"));
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of IdTag objects.
     */
    @Override
    protected void createModel() {
        m = new IdTagTableDataModel(tagManager);
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleIdTagTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.IdTagTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(12);
    JTextField userName = new JTextField(15);
    JCheckBox isStateStored = new JCheckBox(Bundle.getMessage("IdStoreState"));
    JCheckBox isFastClockUsed = new JCheckBox(Bundle.getMessage("IdUseFastClock"));

    @Override
    protected void addPressed(ActionEvent e) {
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddIdTag"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.IdTagAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener okListener = (ActionEvent ev) -> {
                okPressed(ev);
            };
            ActionListener cancelListener = (ActionEvent ev) -> {
                cancelPressed(ev);
            };
            addFrame.setEscapeKeyClosesWindow(true);
            addFrame.add(new AddNewDevicePanel(sysName, userName, "ButtonOK", okListener, cancelListener));
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.isEmpty()) {
            user = null;
        }
        String sName = sysName.getText();
        try {
            tagManager.newIdTag(sName, user);
        } catch (IllegalArgumentException ex) {
            // user input no good
            handleCreateException(sName, ex);
        }
    }
    //private boolean noWarn = false;

    void handleCreateException(String sysName, IllegalArgumentException ex) {
        JOptionPane.showMessageDialog(addFrame,
                Bundle.getMessage("ErrorIdTagAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck")
                + "\n" + ex.getLocalizedMessage() ,
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleIdTagTable");
    }

    @Override
    public void addToFrame(BeanTableFrame<IdTag> f) {
        f.addToBottomBox(isStateStored, this.getClass().getName());
        isStateStored.setSelected(tagManager.isStateStored());
        isStateStored.addActionListener((ActionEvent e) -> {
            tagManager.setStateStored(isStateStored.isSelected());
        });
        f.addToBottomBox(isFastClockUsed, this.getClass().getName());
        isFastClockUsed.setSelected(tagManager.isFastClockUsed());
        isFastClockUsed.addActionListener((ActionEvent e) -> {
            tagManager.setFastClockUsed(isFastClockUsed.isSelected());
        });
        log.debug("Added CheckBox in addToFrame method");
    }

    @Override
    public void addToPanel(AbstractTableTabAction<IdTag> f) {
        String connectionName = tagManager.getMemo().getUserName();
        if (tagManager instanceof ProxyIdTagManager) {
            connectionName = "All";
        } else if (connectionName == null && (tagManager instanceof DefaultRailComManager)) {
            connectionName = "RailCom"; // NOI18N (proper name).
        }
        f.addToBottomBox(isStateStored, connectionName);
        isStateStored.setSelected(tagManager.isStateStored());
        isStateStored.addActionListener((ActionEvent e) -> {
            tagManager.setStateStored(isStateStored.isSelected());
        });
        f.addToBottomBox(isFastClockUsed, connectionName);
        isFastClockUsed.setSelected(tagManager.isFastClockUsed());
        isFastClockUsed.addActionListener((ActionEvent e) -> {
            tagManager.setFastClockUsed(isFastClockUsed.isSelected());
        });
        log.debug("Added CheckBox in addToPanel method for system {}", connectionName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("StateStored")) {
           isStateStored.setSelected(tagManager.isStateStored());
        } else if (e.getPropertyName().equals("UseFastClock")) {
           isFastClockUsed.setSelected(tagManager.isFastClockUsed()); 
        }
    }

    @Override
    protected String getClassName() {
        return IdTagTableAction.class.getName();
    }
    
    @Override
    public void dispose(){
        tagManager.removePropertyChangeListener(this);
        super.dispose();
    }
    
    private static final Logger log = LoggerFactory.getLogger(IdTagTableAction.class);

}
