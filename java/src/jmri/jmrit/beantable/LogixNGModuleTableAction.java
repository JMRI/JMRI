package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import javax.swing.*;
import javax.swing.table.TableColumn;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.UserPreferencesManager;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;


import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;

import jmri.jmrit.logixng.tools.swing.AbstractLogixNGEditor;
import jmri.jmrit.logixng.tools.swing.ModuleEditor;
import jmri.jmrit.logixng.ModuleManager;

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
public class LogixNGModuleTableAction extends AbstractLogixNGTableAction<jmri.jmrit.logixng.Module> {

    JComboBox<FemaleSocketManager.SocketType> _femaleSocketType = new JComboBox<>();

    /**
     * Create a LogixNGTableAction instance.
     *
     * @param s the Action title, not the title of the resulting frame. Perhaps
     *          this should be changed?
     */
    public LogixNGModuleTableAction(String s) {
        super(s);
    }

    /**
     * Create a LogixNGTableAction instance with default title.
     */
    public LogixNGModuleTableAction() {
        this(Bundle.getMessage("TitleLogixNGModuleTable"));
    }

    @Override
    protected AbstractLogixNGEditor<Module> getEditor(BeanTableFrame<Module> f, BeanTableDataModel<Module> m, String sName) {
        ModuleEditor editor = new ModuleEditor(f, m, sName);
        editor.initComponents();
        return editor;
    }

    @Override
    protected Manager<Module> getManager() {
        return InstanceManager.getDefault(ModuleManager.class);
    }

    @Override
    protected void enableAll(boolean enable) {
        // Not used by the module table
    }

    @Override
    protected void setEnabled(Module bean, boolean enable) {
        // Not used by the module table
    }

    @Override
    protected boolean isEnabled(Module bean) {
        return true;
    }

    @Override
    protected Module createBean(String userName) {
        Module module = InstanceManager.getDefault(ModuleManager.class)
                .createModule(userName, _femaleSocketType.getItemAt(_femaleSocketType.getSelectedIndex()));
        return module;
    }

    @Override
    protected Module createBean(String systemName, String userName) {
        Module module = InstanceManager.getDefault(ModuleManager.class)
                .createModule(systemName, userName,
                        _femaleSocketType.getItemAt(_femaleSocketType.getSelectedIndex()));
        return module;
    }

    @Override
    protected void deleteBean(Module bean) {
        InstanceManager.getDefault(ModuleManager.class).deleteModule(bean);
    }

    @Override
    protected String getBeanText(Module bean) {
        StringWriter writer = new StringWriter();
        _curNamedBean.printTree(_printTreeSettings, new PrintWriter(writer), "    ", new MutableInt(0));
        return writer.toString();
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.logixng.LogixNGModuleTable";  // NOI18N
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
                "package.jmri.jmrit.logixng.LogixNGModuleTable", true);     // NOI18N
        addLogixNGFrame.setLocation(50, 30);
        Container contentPane = addLogixNGFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        List<FemaleSocketManager.SocketType> list
                = new ArrayList<>(InstanceManager.getDefault(FemaleSocketManager.class).getSocketTypes().values());
        Collections.sort(list, (FemaleSocketManager.SocketType o1, FemaleSocketManager.SocketType o2) -> o1.getDescr().compareTo(o2.getDescr()));

        for (FemaleSocketManager.SocketType socketType : list) {
            _femaleSocketType.addItem(socketType);
            if ("DefaultFemaleDigitalActionSocket".equals(socketType.getName())) {
                _femaleSocketType.setSelectedItem(socketType);
            }
        }

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
        c.gridy = 2;
        p.add(new JLabel(Bundle.getMessage("LogixNG_ModuleSocketType")), c);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(_systemName, c);
        c.gridy = 1;
        p.add(_addUserName, c);
        c.gridy = 2;
        c.gridwidth = 2;
        p.add(_femaleSocketType, c);
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
        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N

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

}
