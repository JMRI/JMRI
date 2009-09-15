// AudioTableAction.java

package jmri.jmrit.beantable;

import jmri.jmrit.audio.swing.AudioSourceFrame;
import jmri.jmrit.audio.swing.AudioListenerFrame;
import jmri.jmrit.audio.swing.AudioBufferFrame;
import jmri.InstanceManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;

import jmri.Audio;
import jmri.AudioManager;
import jmri.NamedBean;

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
 * @version     $Revision: 1.4 $
 */

public class AudioTableAction extends AbstractTableAction {

    AudioTableDataModel listener;
    AudioTableDataModel buffers;
    AudioTableDataModel sources;

    AudioSourceFrame sourceFrame;
    AudioBufferFrame bufferFrame;
    AudioListenerFrame listenerFrame;

    AudioTableFrame atf;

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
    public AudioTableAction() { this(rb.getString("TitleAudioTable"));}

    @Override
    public void actionPerformed(ActionEvent e) {
        // ensure that the AudioFactory has been initialised
        if(InstanceManager.audioManagerInstance().getActiveAudioFactory()==null) {
            InstanceManager.audioManagerInstance().init();
        }

        // create the JTable model, with changes for specific NamedBean
        createModel();

        // create the frame
        atf = new AudioTableFrame(listener, buffers, sources, helpTarget())
        {
            /**
             * Include "Add Source..." and "Add Buffer..." buttons
             */
            @Override
            void extras() {
                JButton addSourceButton = new JButton(rba.getString("ButtonAddSource"));
                addToBottomBox(addSourceButton);
                addSourceButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addSourcePressed(e);
                    }
                });
                JButton addBufferButton = new JButton(rba.getString("ButtonAddBuffer"));
                addToBottomBox(addBufferButton);
                addBufferButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addBufferPressed(e);
                    }
                });
            }
        };
        setTitle();
        atf.pack();
        atf.setVisible(true);
    }

    /**
     * Create the JTable DataModels, along with the changes
     * for the specific case of Audio objects
     */
    void createModel() {

        listener = new AudioTableDataModel(Audio.LISTENER);
        buffers = new AudioTableDataModel(Audio.BUFFER);
        sources = new AudioTableDataModel(Audio.SOURCE);
    }

    void setTitle() {
        atf.setTitle(AudioTableFrame.rb.getString("TitleAudioTable"));
    }

    @Override
    String helpTarget() {
        return "package.jmri.jmrit.beantable.AudioTable";
    }
    
    void addPressed(ActionEvent e) {
        log.warn("This should not have happened");
    }

    void addSourcePressed(ActionEvent e) {
        if (sourceFrame==null) {
            sourceFrame = new AudioSourceFrame(rb.getString("TitleAddAudioSource"), sources);
        }
        sourceFrame.updateBufferList();
        sourceFrame.resetFrame();
        sourceFrame.pack();
        sourceFrame.setVisible(true);
    }

    void addBufferPressed(ActionEvent e) {
        if (bufferFrame==null) {
            bufferFrame = new AudioBufferFrame(rb.getString("TitleAddAudioBuffer"), buffers);
        }
        bufferFrame.resetFrame();
        bufferFrame.pack();
        bufferFrame.setVisible(true);
    }

    protected void editAudio(Audio a) {
        Thread t;
        switch (a.getSubType()) {
            case Audio.LISTENER:
                if (listenerFrame==null) {
                    listenerFrame = new AudioListenerFrame(rb.getString("TitleAddAudioListener"), listener);
                }
                listenerFrame.populateFrame(a);
                t = new Thread() {
                    @Override
                    public void run() {
                        listenerFrame.pack();
                        listenerFrame.setVisible(true);
                    }
                };
                javax.swing.SwingUtilities.invokeLater(t);
                break;
            case Audio.BUFFER:
                if (bufferFrame==null) {
                    bufferFrame = new AudioBufferFrame(rb.getString("TitleAddAudioBuffer"), buffers);
                }
                bufferFrame.populateFrame(a);
                t = new Thread() {
                    @Override
                    public void run() {
                        bufferFrame.pack();
                        bufferFrame.setVisible(true);
                    }
                };
                javax.swing.SwingUtilities.invokeLater(t);
                break;
            case Audio.SOURCE:
                if (sourceFrame==null) {
                    sourceFrame = new AudioSourceFrame(rb.getString("TitleAddAudioBuffer"), sources);
                }
                sourceFrame.updateBufferList();
                sourceFrame.populateFrame(a);
                t = new Thread() {
                    @Override
                    public void run() {
                        sourceFrame.pack();
                        sourceFrame.setVisible(true);
                    }
                };
                javax.swing.SwingUtilities.invokeLater(t);
                break;
        }
    }

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AudioTableAction.class.getName());

    public class AudioTableDataModel extends BeanTableDataModel implements PropertyChangeListener {

        int subType;

        public static final int EDITCOL = NUMCOLUMN;

        public AudioTableDataModel(int subType) {
            super();
            this.subType = subType;
            getManager().addPropertyChangeListener(this);
            updateNameList();
        }

        public AudioManager getManager() { return InstanceManager.audioManagerInstance(); }

        public Audio getBySystemName(String name) { return InstanceManager.audioManagerInstance().getBySystemName(name); }

        public Audio getByUserName(String name) { return InstanceManager.audioManagerInstance().getByUserName(name); }

        @Override
        synchronized void updateNameList() {
            // first, remove listeners from the individual objects
            if (sysNameList != null) {
                for (int i = 0; i< sysNameList.size(); i++) {
                    // if object has been deleted, it's not here; ignore it
                    NamedBean b = getBySystemName(sysNameList.get(i));
                    if (b!=null)
                        b.removePropertyChangeListener(this);
                }
            }
            sysNameList = getManager().getSystemNameList(subType);
            // and add them back in
            for (int i = 0; i< sysNameList.size(); i++) {
                getBySystemName(sysNameList.get(i)).addPropertyChangeListener(this);
            }
        }

        @Override
        public int getColumnCount( ){ return EDITCOL+1;}

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case VALUECOL:
                    return "Description";
                case EDITCOL:
                    return "";
                default:
                    return super.getColumnName(col);
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case VALUECOL:
                    return String.class;
                case EDITCOL:
                    return JButton.class;
                case DELETECOL:
                    return (subType!=Audio.LISTENER) ? JButton.class : String.class;
                default:
                    return super.getColumnClass(col);
            }
        }

        public String getValue(String systemName) {
            Object m = InstanceManager.audioManagerInstance().getBySystemName(systemName);
            return (m!=null) ? m.toString(): "";
        }

        @Override
        public Object getValueAt(int row, int col) {
            Audio a;
            switch (col) {
                case SYSNAMECOL:  // slot number
                    return sysNameList.get(row);
                case USERNAMECOL:  // return user name
                    // sometimes, the TableSorter invokes this on rows that no longer exist, so we check
                    a = getBySystemName(sysNameList.get(row));
                    return (a!=null) ? a.getUserName() : null;
                case VALUECOL:
                    a = getBySystemName(sysNameList.get(row));
                    return (a!=null) ? getValue(a.getSystemName()) : null;
                case COMMENTCOL:
                    a = getBySystemName(sysNameList.get(row));
                    return (a!=null) ? a.getComment() : null;
                case DELETECOL:
                    return (subType!=Audio.LISTENER) ? AbstractTableAction.rb.getString("ButtonDelete") : "";
                case EDITCOL:
                    return AbstractTableAction.rb.getString("ButtonEdit");
                default:
                    log.error("internal state inconsistent with table requst for "+row+" "+col);
                    return null;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            Audio a;
            switch (col) {
                case EDITCOL:
                    a = getBySystemName(sysNameList.get(row));
                    editAudio(a);
                    break;
                default:
                    super.setValueAt(value, row, col);
            }
        }

        @Override
        public int getPreferredWidth(int col) {
            switch (col) {
                case VALUECOL:
                    return new JTextField(50).getPreferredSize().width;
                case EDITCOL:
                    return new JButton(AbstractTableAction.rb.getString("ButtonEdit")).getPreferredSize().width;
                default:
                    return super.getPreferredWidth(col);
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case DELETECOL:
                    return (subType!=Audio.LISTENER) ? true : false;
                case VALUECOL:
                    return false;
                case EDITCOL:
                    return true;
                default:
                    return super.isCellEditable(row, col);
            }
        }

        @Override
        void clickOn(NamedBean t) {
            // Do nothing
        }

        @Override
        void configValueColumn(JTable table) {
            // Do nothing
        }

        void configEditColumn(JTable table) {
            // have the edit column hold a button
            setColumnToHoldButton(table, EDITCOL,
                    new JButton(AbstractTableAction.rb.getString("ButtonEdit")));
        }
    }
}

/* @(#)AudioTableAction.java */
