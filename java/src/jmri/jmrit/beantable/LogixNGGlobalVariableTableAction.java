package jmri.jmrit.beantable;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.beans.PropertyVetoException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.JmriJFrame;


import jmri.jmrit.logixng.tools.swing.AbstractLogixNGEditor;
// import jmri.jmrit.logixng.tools.swing.GlobalVariableEditor;

/**
 * Swing action to create and register a LogixNG Global Variables Table.
 * <p>
 * Also contains the panes to create, edit, and delete a LogixNG Global Variable.
 * <p>
 * Most of the text used in this GUI is in BeanTableBundle.properties, accessed
 * via Bundle.getMessage().
 * <p>
 *
 * @author Dave Duchamp Copyright (C) 2007 (LogixTableAction)
 * @author Pete Cressman Copyright (C) 2009, 2010, 2011 (LogixTableAction)
 * @author Matthew Harris copyright (c) 2009 (LogixTableAction)
 * @author Dave Sand copyright (c) 2017 (LogixTableAction)
 * @author Daniel Bergqvist copyright (c) 2022
 */
public class LogixNGGlobalVariableTableAction extends AbstractLogixNGTableAction<GlobalVariable> {

    /**
     * Create a LogixNGGlobalVariablesTableAction instance.
     *
     * @param s the Action title, not the title of the resulting frame. Perhaps
     *          this should be changed?
     */
    public LogixNGGlobalVariableTableAction(String s) {
        super(s);
    }

    /**
     * Create a LogixNGTableAction instance with default title.
     */
    public LogixNGGlobalVariableTableAction() {
        this(Bundle.getMessage("TitleLogixNGGlobalVariablesTable"));
    }

    @Override
    protected AbstractLogixNGEditor<GlobalVariable> getEditor(BeanTableFrame<GlobalVariable> f, BeanTableDataModel<GlobalVariable> m, String sName) {
//        return new LogixNGEditor(f, m, sName);
        return null;
    }

    @Override
    protected Manager<GlobalVariable> getManager() {
        return InstanceManager.getDefault(GlobalVariableManager.class);
    }

    @Override
    public void setEnabled(GlobalVariable globalVariable, boolean enable) {
    }

    @Override
    protected boolean isEnabled(GlobalVariable globalVariable) {
        return true;
    }

    @Override
    public void enableAll(boolean enable) {
    }

    @Override
    protected GlobalVariable createBean(String userName) {
        GlobalVariable globalVariable =
                InstanceManager.getDefault(GlobalVariableManager.class)
                        .createGlobalVariable(userName);
        return globalVariable;
    }

    @Override
    protected GlobalVariable createBean(String systemName, String userName) {
        GlobalVariable globalVariable =
                InstanceManager.getDefault(GlobalVariableManager.class)
                        .createGlobalVariable(systemName, userName);
        return globalVariable;
    }

    @Override
    public void deleteBean(GlobalVariable globalVariable) {
        try {
            InstanceManager.getDefault(GlobalVariableManager.class).deleteBean(globalVariable, "DoDelete");
        } catch (PropertyVetoException e) {
            //At this stage the DoDelete shouldn't fail, as we have already done a can delete, which would trigger a veto
            log.error(e.getMessage());
        }
    }
/*
    @Override
    protected void copyBean(@Nonnull LogixNG sourceBean, @Nonnull LogixNG targetBean) {
        for (int i = 0; i < sourceBean.getNumConditionalNGs(); i++) {
            copyConditionalNGToLogixNG(sourceBean.getConditionalNG(i), targetBean);
        }
    }
*/
    @Override
    protected boolean isCopyBeanSupported() {
        return false;
    }

    @Override
    protected String getBeanText(GlobalVariable e) {
        return e.toString();
    }

    @Override
    protected String getAddTitleKey() {
        return "TitleAddLogixNGGlobalVariable";
    }

    @Override
    protected String getCreateButtonHintKey() {
        return "LogixNGCreateGlobalVariableButtonHint";
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
    protected void getListenerRefsIncludingChildren(GlobalVariable globalVariable, java.util.List<String> list) {
        throw new UnsupportedOperationException("Not supported yet");
//        logixNG.getListenerRefsIncludingChildren(list);
    }

    @Override
    protected boolean hasChildren(GlobalVariable globalVariable) {
        return false;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGGlobalVariableTableAction.class);

}
