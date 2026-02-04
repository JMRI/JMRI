package jmri.jmrit.beantable;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.beans.PropertyVetoException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeCellEditor;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.tools.swing.AbstractLogixNGEditor;
import jmri.jmrit.logixng.tools.swing.LogixNGEditor;
import jmri.util.JmriJFrame;
import jmri.util.swing.TriStateJCheckBox;
import jmri.util.swing.XTableColumnModel;

import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Swing action to create and register a LogixNG Table.
 * <p>
 Also contains the panes to create, edit, and delete a LogixNG.
 <p>
 * Most of the text used in this GUI is in BeanTableBundle.properties, accessed
 * via Bundle.getMessage().
 *
 * @author Dave Duchamp Copyright (C) 2007 (LogixTableAction)
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011 (LogixTableAction)
 * @author Matthew Harris copyright (c) 2009 (LogixTableAction)
 * @author Dave Sand copyright (c) 2017 (LogixTableAction)
 * @author Daniel Bergqvist copyright (c) 2019
 * @author Dave Sand copyright (c) 2021
 */
public class LogixNGTableAction extends AbstractLogixNGTableAction<LogixNG> {

    /**
     * Create a LogixNGTableAction instance.
     *
     * @param s the Action title, not the title of the resulting frame. Perhaps
     *          this should be changed?
     */
    public LogixNGTableAction(String s) {
        super(s);
    }

    /**
     * Create a LogixNGTableAction instance with default title.
     */
    public LogixNGTableAction() {
        this(Bundle.getMessage("TitleLogixNGTable"));
    }

    @Override
    protected void createModel() {
        m = new TableModel();
        m.setFilter((LogixNG t) -> !t.isInline());
    }

    @Override
    protected AbstractLogixNGEditor<LogixNG> getEditor(BeanTableDataModel<LogixNG> m, String sName) {
        return new LogixNGEditor(m, sName);
    }

    @Override
    protected Manager<LogixNG> getManager() {
        return InstanceManager.getDefault(LogixNG_Manager.class);
    }

    @Override
    protected void setEnabled(LogixNG logixNG, boolean enable) {
        logixNG.setEnabled(enable);
    }

    @Override
    protected boolean isEnabled(LogixNG logixNG) {
        return logixNG.isEnabled();
    }

    @Override
    protected void enableAll(boolean enable) {
        for (LogixNG x : getManager().getNamedBeanSet()) {
            x.setEnabled(enable);
        }
        m.fireTableDataChanged();
    }

    @Override
    protected LogixNG createBean(String userName) {
        LogixNG logixNG =
                InstanceManager.getDefault(LogixNG_Manager.class)
                        .createLogixNG(userName);
        logixNG.activate();
        logixNG.setEnabled(true);
        logixNG.clearStartup();
        return logixNG;
    }

    @Override
    protected LogixNG createBean(String systemName, String userName) {
        LogixNG logixNG =
                InstanceManager.getDefault(LogixNG_Manager.class)
                        .createLogixNG(systemName, userName);
        logixNG.activate();
        logixNG.setEnabled(true);
        logixNG.clearStartup();
        return logixNG;
    }

    @Override
    public void deleteBean(LogixNG logixNG) {
        logixNG.setEnabled(false);
        try {
            InstanceManager.getDefault(LogixNG_Manager.class).deleteBean(logixNG, "DoDelete");
        } catch (PropertyVetoException e) {
            //At this stage the DoDelete shouldn't fail, as we have already done a can delete, which would trigger a veto
            log.error("{} : Could not Delete.", e.getMessage());
        }
    }

    private void copyConditionalNGToLogixNG(
            @Nonnull ConditionalNG sourceConditionalNG,
            @Nonnull LogixNG targetBean) {

            // Create ConditionalNG
            String sysName = InstanceManager.getDefault(ConditionalNG_Manager.class).getAutoSystemName();
            String oldUserName = sourceConditionalNG.getUserName();
            String userName = oldUserName != null ? Bundle.getMessage("CopyOfConditionalNG", oldUserName) : null;
            ConditionalNG targetConditionalNG =
                    InstanceManager.getDefault(ConditionalNG_Manager.class)
                            .createConditionalNG(targetBean, sysName, userName);

            sourceConditionalNG.getFemaleSocket().unregisterListeners();
            targetConditionalNG.getFemaleSocket().unregisterListeners();
            Map<String, String> systemNames = new HashMap<>();
            Map<String, String> userNames = new HashMap<>();
            try {
                FemaleSocket femaleSourceSocket = sourceConditionalNG.getFemaleSocket();
                if (femaleSourceSocket.isConnected()) {
                    targetConditionalNG.getFemaleSocket().connect(
                            (MaleSocket) femaleSourceSocket.getConnectedSocket()
                                    .getDeepCopy(systemNames, userNames));
                }
            } catch (JmriException ex) {
                log.error("Could not Copy ConditionalNG.", ex);
            }
            sourceConditionalNG.getFemaleSocket().registerListeners();
            targetConditionalNG.getFemaleSocket().registerListeners();
    }

    @Override
    protected void copyBean(@Nonnull LogixNG sourceBean, @Nonnull LogixNG targetBean) {
        for (int i = 0; i < sourceBean.getNumConditionalNGs(); i++) {
            copyConditionalNGToLogixNG(sourceBean.getConditionalNG(i), targetBean);
        }
    }

    @Override
    protected boolean isCopyBeanSupported() {
        return true;
    }

    @Override
    protected boolean isExecuteSupported() {
        return true;
    }

    @Override
    protected void execute(@Nonnull LogixNG logixNG) {
        if (!logixNG.isActivated() && !logixNG.isEnabled()) {
            JOptionPane.showMessageDialog(f, Bundle.getMessage("LogixNG_CantExecuteLogixNG_InactiveAndNotEnabled"), Bundle.getMessage("LogixNG_Error"), JOptionPane.ERROR_MESSAGE);
        } else if (!logixNG.isActivated()) {
            JOptionPane.showMessageDialog(f, Bundle.getMessage("LogixNG_CantExecuteLogixNG_Inactive"), Bundle.getMessage("LogixNG_Error"), JOptionPane.ERROR_MESSAGE);
        } else if (!logixNG.isEnabled()) {
            JOptionPane.showMessageDialog(f, Bundle.getMessage("LogixNG_CantExecuteLogixNG_NotEnabled"), Bundle.getMessage("LogixNG_Error"), JOptionPane.ERROR_MESSAGE);
        } else {
            logixNG.execute();
        }
    }

    @Override
    protected String getBeanText(LogixNG e, Base.PrintTreeSettings printTreeSettings) {
        StringWriter writer = new StringWriter();
        _curNamedBean.printTree(printTreeSettings, new PrintWriter(writer), "    ", new MutableInt(0));
        return writer.toString();
    }

    @Override
    protected String getBrowserTitle() {
        return Bundle.getMessage("LogixNG_Browse_Title");
    }

    @Override
    protected String getAddTitleKey() {
        return "TitleAddLogixNG";
    }

    @Override
    protected String getCreateButtonHintKey() {
        return "LogixNGCreateButtonHint";
    }

    /**
     * Create or copy bean frame.
     *
     * @param titleId   property key to fetch as title of the frame (using Bundle)
     * @param startMessageId part 1 of property key to fetch as user instruction on
     *                  pane, either 1 or 2 is added to form the whole key
     * @return the button JPanel
     */
    @Override
    protected JPanel makeAddFrame(String titleId, String startMessageId) {
        addLogixNGFrame = new JmriJFrame(Bundle.getMessage(titleId));
        addLogixNGFrame.addHelpMenu(
                "package.jmri.jmrit.beantable.LogixNGTable", true);     // NOI18N
        addLogixNGFrame.setLocation(50, 30);
        Container contentPane = addLogixNGFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JPanel p;
        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        p.add(_sysNameLabel, c);
        _sysNameLabel.setLabelFor(_systemName);
        c.gridy = 1;
        p.add(_userNameLabel, c);
        _userNameLabel.setLabelFor(_addUserName);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(_systemName, c);
        c.gridy = 1;
        p.add(_addUserName, c);
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        c.gridy = 0;
        p.add(_autoSystemName, c);
        _addUserName.setToolTipText(Bundle.getMessage("LogixNGUserNameHint"));    // NOI18N
        _systemName.setToolTipText(Bundle.getMessage("LogixNGSystemNameHint"));   // NOI18N
        contentPane.add(p);
        // set up message
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());
        JLabel message1 = new JLabel(Bundle.getMessage(startMessageId + "LogixNGMessage1"));  // NOI18N
        panel31.add(message1);
        JPanel panel32 = new JPanel();
        JLabel message2 = new JLabel(Bundle.getMessage(startMessageId + "LogixNGMessage2"));  // NOI18N
        panel32.add(message2);
        panel3.add(panel31);
        panel3.add(panel32);
        contentPane.add(panel3);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(cancel);
        cancel.addActionListener(this::cancelAddPressed);
        cancel.setToolTipText(Bundle.getMessage("CancelLogixNGButtonHint"));      // NOI18N

        addLogixNGFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelAddPressed(null);
            }
        });
        contentPane.add(panel5);

        _autoSystemName.addItemListener((ItemEvent e) -> {
            autoSystemName();
        });
        return panel5;
    }

    @Override
    protected void getListenerRefsIncludingChildren(LogixNG logixNG, java.util.List<String> list) {
        logixNG.getListenerRefsIncludingChildren(list);
    }

    @Override
    protected boolean hasChildren(LogixNG logixNG) {
        return logixNG.getNumConditionalNGs() > 0;
    }


    protected class TableModel extends AbstractLogixNGTableAction<LogixNG>.TableModel {

        // overlay the state column with the edit column
        static public final int STARTUP_COL = NUMCOLUMN;

        /** {@inheritDoc} */
        @Override
        public void configureTable(JTable table) {
            super.configureTable(table);

            table.setDefaultRenderer(TriStateJCheckBox.State.class, new EnablingTriStateCheckboxRenderer());

            TriStateJCheckBox startupCheckBox = new TriStateJCheckBox();
            TableColumn col = ((XTableColumnModel)table.getColumnModel())
                    .getColumnByModelIndex(TableModel.STARTUP_COL);
            col.setCellEditor(new CellEditor(startupCheckBox));
        }

        /** {@inheritDoc} */
        @Override
        public int getColumnCount() {
            return STARTUP_COL + 1;
        }

        @Override
        public String getColumnName(int col) {
            if (col == STARTUP_COL) {
                return Bundle.getMessage("ColumnLogixNGStartup");
            }
            return super.getColumnName(col);
        }

        @Override
        public Class<?> getColumnClass(int col) {
            if (col == STARTUP_COL) {
                return TriStateJCheckBox.State.class;
            }
            return super.getColumnClass(col);
        }

        @Override
        public int getPreferredWidth(int col) {
            // override default value for SystemName and UserName columns
            if (col == STARTUP_COL) {
                return new JTextField(5).getPreferredSize().width;
            }
            return super.getPreferredWidth(col);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == STARTUP_COL) {
                return true;
            }
            return super.isCellEditable(row, col);
        }

        @SuppressWarnings("unchecked")  // Unchecked cast from Object to E
        @Override
        public Object getValueAt(int row, int col) {
            if (col == STARTUP_COL) {
                LogixNG x = (LogixNG) getValueAt(row, SYSNAMECOL);
                if (x == null) {
                    return null;
                }
                boolean anyTrue = false;
                boolean anyFalse = false;
                for (int i=0; i < x.getNumConditionalNGs(); i++) {
                    ConditionalNG cng = x.getConditionalNG(i);
                    if (cng.isExecuteAtStartup()) anyTrue = true;
                    else anyFalse = true;
                }
                if (anyTrue && anyFalse) {
                    return TriStateJCheckBox.State.PARTIAL;
                } else if (anyTrue) {
                    return TriStateJCheckBox.State.CHECKED;
                } else {
                    return TriStateJCheckBox.State.UNCHECKED;
                }
            } else {
                return super.getValueAt(row, col);
            }
        }

        @SuppressWarnings("unchecked")  // Unchecked cast from Object to E
        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == STARTUP_COL) {
                // alternate
                LogixNG x = (LogixNG) getValueAt(row, SYSNAMECOL);

                for (int i=0; i < x.getNumConditionalNGs(); i++) {
                    ConditionalNG cng = x.getConditionalNG(i);
                    cng.setExecuteAtStartup(value == TriStateJCheckBox.State.CHECKED);
                }
            } else {
                super.setValueAt(value, row, col);
            }
        }

        @Override
        public String getValue(String s) {
            return "";
        }
    }

    private static class CellEditor extends AbstractCellEditor
            implements TableCellEditor, TreeCellEditor {

        TriStateJCheckBox _tristateCheckBox;

        public CellEditor(TriStateJCheckBox tristateCheckBox) {
            this._tristateCheckBox = tristateCheckBox;
        }

        @Override
        public Object getCellEditorValue() {
            return _tristateCheckBox.getState();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return _tristateCheckBox;
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            return _tristateCheckBox;
        }

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGTableAction.class);

}
