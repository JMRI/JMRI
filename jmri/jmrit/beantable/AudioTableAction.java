// AudioTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.AudioException;
import jmri.jmrit.audio.AudioBuffer;
import jmri.jmrit.audio.AudioListener;
import jmri.jmrit.audio.AudioSource;
import jmri.util.JmriJFrame;

/**
 * Swing action to create and register an
 * AudioTable GUI.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @author      Matthew Harris  copyright (c) 2009
 * @version     $Revision: 1.1 $
 */

public class AudioTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public AudioTableAction(String actionName) {
	super(actionName);

        // disable ourself if there is no primary Audio manager available
        if (jmri.InstanceManager.audioManagerInstance()==null) {
            setEnabled(false);
        }

    }

    /**
     * Default constructor
     */
    public AudioTableAction() { this("Audio Table");}

    /**
     * Create the JTable DataModels, along with the changes
     * for the specific case of Audio objects
     */
    void createModel() {
        m = new BeanTableDataModel() {
            public String getValue(String name) {
            	Object m = InstanceManager.audioManagerInstance().getBySystemName(name);
            	if (m!=null && m instanceof AudioBuffer) {
                    return "Buffer: " + m.toString();
                } else if (m!=null && m instanceof AudioSource) {
                    return "Source: " + m.toString();
                } else if (m!=null && m instanceof AudioListener) {
                    return "Listener: " + m.toString();
                } else
                    return "";
            }
            public Manager getManager() { return InstanceManager.audioManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.audioManagerInstance().getBySystemName(name); }
            public NamedBean getByUserName(String name) { return InstanceManager.audioManagerInstance().getByUserName(name); }

            public void clickOn(NamedBean t) {
            	// don't do anything on click; not used in this class, because 
            	// we override setValueAt
            }
            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col==VALUECOL) {
                // Do nothing
                //Audio t = (Audio)getBySystemName(sysNameList.get(row));
                                //t.setValue(value);
                //fireTableRowsUpdated(row,row);
                } else super.setValueAt(value, row, col);
            }
            @Override
            public String getColumnName(int col) {
                if (col==VALUECOL) return "Value";
                return super.getColumnName(col);
            }
            @Override
            public Class<?> getColumnClass(int col) {
                if (col==VALUECOL) return String.class;
                else return super.getColumnClass(col);
            }
            @Override
            public void configValueColumn(JTable table) {
                // value column isn't button, so config is null
            }
            @Override
            boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                return true;
                // return (e.getPropertyName().indexOf("alue")>=0);
            }
            @SuppressWarnings("static-access")
            @Override
            public JButton configureButton() {
                super.log.error("configureButton should not have been called");
                return null;
            }
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleAudioTable"));
    }

    @Override
    String helpTarget() {
        return "package.jmri.jmrit.beantable.AudioTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JmriJFrame(rb.getString("TitleAddAudio"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.AudioAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p;
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(sysNameLabel);
            p.add(sysName);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(userNameLabel);
            p.add(userName);
            addFrame.getContentPane().add(p);

            JButton ok;
            addFrame.getContentPane().add(ok = new JButton(rb.getString("ButtonOK")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) user=null;
        String sName = sysName.getText().toUpperCase();
        try {
            InstanceManager.audioManagerInstance().newAudio(sName, user);
        } catch (AudioException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), rb.getString("AudioCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
    }
    //private boolean noWarn = false;

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AudioTableAction.class.getName());
}

/* @(#)AudioBufferTableAction.java */
