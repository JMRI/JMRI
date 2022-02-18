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
import jmri.jmrit.beantable.Bundle;
import jmri.jmrit.logixng.*;
import jmri.util.JmriJFrame;


import jmri.jmrit.logixng.tools.swing.AbstractLogixNGEditor;
import jmri.jmrit.logixng.tools.swing.LogixNGEditor;

import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Swing action to create and register a LogixNG Table.
 * <p>
 Also contains the panes to create, edit, and delete a LogixNG.
 <p>
 * Most of the text used in this GUI is in BeanTableBundle.properties, accessed
 * via Bundle.getMessage().
 * <p>
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
    protected AbstractLogixNGEditor<LogixNG> getEditor(BeanTableFrame<LogixNG> f, BeanTableDataModel<LogixNG> m, String sName) {
        return new LogixNGEditor(f, m, sName);
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
        logixNG.setEnabled(true);
        return logixNG;
    }

    @Override
    protected LogixNG createBean(String systemName, String userName) {
        LogixNG logixNG =
                InstanceManager.getDefault(LogixNG_Manager.class)
                        .createLogixNG(systemName, userName);
        logixNG.setEnabled(true);
        return logixNG;
    }

    @Override
    public void deleteBean(LogixNG logixNG) {
        logixNG.setEnabled(false);
        try {
            InstanceManager.getDefault(LogixNG_Manager.class).deleteBean(logixNG, "DoDelete");
        } catch (PropertyVetoException e) {
            //At this stage the DoDelete shouldn't fail, as we have already done a can delete, which would trigger a veto
            log.error(e.getMessage());
        }
    }

    private void copyConditionalNGToLogixNG(
            @Nonnull ConditionalNG sourceConditionalNG,
            @Nonnull LogixNG targetBean) {

            // Create ConditionalNG
            String sysName = InstanceManager.getDefault(ConditionalNG_Manager.class).getAutoSystemName();
            ConditionalNG targetConditionalNG =
                    InstanceManager.getDefault(ConditionalNG_Manager.class)
                            .createConditionalNG(targetBean, sysName, sourceConditionalNG.getUserName());

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
                log.error(ex.getMessage(), ex);
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
    protected String getBeanText(LogixNG e) {
        StringWriter writer = new StringWriter();
        _curNamedBean.printTree(_printTreeSettings, new PrintWriter(writer), "    ", new MutableInt(0));
        return writer.toString();
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGTableAction.class);

}
