package jmri.jmrit.vsdecoder;

/**
 * class VSDecoderPane
 *
 * GUI pane for a Virtual Sound Decoder (VSDecoder).
 */

/*
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author   Mark Underwood Copyright (C) 2011
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.util.FileUtil;
import jmri.util.swing.JmriPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtual Sound Decoder for playing sounds off of LocoNet messages. Based on
 * the LocoMon tool by Bob Jacobsen
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class VSDecoderPane extends JmriPanel {

    public static enum PropertyChangeID {

        ADDRESS_CHANGE, PROFILE_SELECT, MUTE, VOLUME_CHANGE
    }

    public static final Map<PropertyChangeID, String> PCIDMap;

    static {
        Map<PropertyChangeID, String> aMap = new HashMap<PropertyChangeID, String>();
        aMap.put(PropertyChangeID.ADDRESS_CHANGE, "AddressChange");
        aMap.put(PropertyChangeID.PROFILE_SELECT, "ProfileSelect");
        aMap.put(PropertyChangeID.MUTE, "Mute");
        aMap.put(PropertyChangeID.VOLUME_CHANGE, "VolumeChange");
        PCIDMap = Collections.unmodifiableMap(aMap);
    }

    String decoder_id;
    VSDecoderManager decoder_mgr;

    final static String BASICPANEL = "Basic";
    final static String COMBOPANEL = "Sounds";
    final static String OPTIONPANEL = "Options";

    // GUI Components
    private VSDecoderFrame parent;

    private JTabbedPane tabbedPane;
    private VSDConfigPanel configPanel;
    private JPanel soundsPanel;
    private JPanel optionPanel;
    private JPanel volumePanel;
    private jmri.util.swing.StatusBar statusBar;

    private static String VSDecoderFileLocation = null;

    //private List<JMenu> menuList;
    /**
     * Create a new VSDecoderPane
     */
    public VSDecoderPane(VSDecoderFrame p) {
        super();
        parent = p;
        decoder_mgr = VSDecoderManager.instance();
    }

    /**
     * getFrame() Get this Pane's parent Frame
     */
    public VSDecoderFrame getFrame() {
        return (parent);
    }

    /**
     * getHelpTarget()
     *
     * Return a reference to the help file
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrit.vsdecoder.VSDecoderPane";
    }

    /**
     * getTitle()
     *
     * Return a suggested title for the enclosing frame.
     */
    @Override
    public String getTitle() {
        return Bundle.getMessage("WindowTitle");
    }

    /**
     * getDefaultVSDecoderFolder()
     *
     * Return a string default default path for the VSD files
     */
    public static String getDefaultVSDecoderFolder() {
        if (VSDecoderFileLocation == null) {
            return FileUtil.getUserFilesPath() + "vsdecoder" + File.separator;
        }
        return VSDecoderFileLocation;
    }

    /**
     * init() : does nothing. Here to satisfy the parent class
     */
    public void init() {
        // Does nothing.  Here for completeness.
    }

    /**
     * initContext() : does nothing. Here to satisfy the parent class
     */
    @Override
    public void initContext(Object context) {
        // Does nothing.  Here for completeness.
    }

    /**
     * initComponents()
     *
     * initialzies the GUI components.
     */
    @Override
    public void initComponents() {
        log.debug("initComponents()");
        //buildMenu();

        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add the tabbed pane to the VSDecoderPane.  The tabbedPane will contain all the other panes.
        tabbedPane = new JTabbedPane();
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.fill = GridBagConstraints.BOTH;
        gbc1.anchor = GridBagConstraints.CENTER;
        gbc1.weightx = 1.0;
        gbc1.weighty = 1.0;
        this.add(tabbedPane, gbc1);

        //-------------------------------------------------------------------
        // configPanel
        // The configPanel holds the stuff for addressing and configuration.
        configPanel = new VSDConfigPanel(decoder_id, this);

        tabbedPane.addTab("Config", configPanel);

        //-------------------------------------------------------------------
        // soundsPanel
        // The optionPanel holds controls for selecting sound options.
        optionPanel = new VSDOptionPanel(decoder_id, this);

        tabbedPane.addTab("Options", optionPanel);

        //-------------------------------------------------------------------
        // soundsPanel
        // The soundsPanel holds buttons for specific sounds.
        soundsPanel = new VSDSoundsPanel(decoder_id, this);
        tabbedPane.addTab("Sounds", soundsPanel);

        //-------------------------------------------------------------------
        // volumePanel
        // The volumePanel holds the master volume and mute controls.
        volumePanel = new JPanel();
        volumePanel.setLayout(new BorderLayout(10, 0));
        TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Volume");
        title.setTitlePosition(TitledBorder.DEFAULT_POSITION);
        volumePanel.setBorder(title);

        JSlider volume = new JSlider(0, 100);
        volume.setMinorTickSpacing(10);
        volume.setPaintTicks(true);
        volume.setValue(80);
        volume.setPreferredSize(new Dimension(200, 20));
        volume.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                volumeChange(e);
            }
        });
        volumePanel.add(volume, BorderLayout.LINE_START);

        JToggleButton mute_button = new JToggleButton("Mute");

        mute_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                muteButtonPressed(e);
            }

        });
        volumePanel.add(mute_button, BorderLayout.LINE_END);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.anchor = GridBagConstraints.CENTER;
        gbc2.weightx = 1.0;
        gbc2.weighty = 0.1;
        this.add(volumePanel, gbc2);

        //-------------------------------------------------------------------
        // statusBar
        // The statusBar shows decoder status.
        statusBar = new jmri.util.swing.StatusBar();
        statusBar.setMessage("Status: No decoder assigned");
        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.gridx = 0;
        gbc3.gridy = 2;
        gbc3.fill = GridBagConstraints.BOTH;
        gbc3.anchor = GridBagConstraints.PAGE_END;
        gbc3.weightx = 1.0;
        gbc3.weighty = 0.1;
        this.add(statusBar, gbc3);

        //-------------------------------------------------------------------
        // Pack and set visible
        parent.pack();
        parent.setVisible(true);
    }

    // PROPERTY CHANGE EVENT FUNCTIONS
    /**
     * Handle a mute button press event
     */
    // NOTE: should this be public???
    public void muteButtonPressed(ActionEvent e) {
        JToggleButton b = (JToggleButton) e.getSource();
        log.debug("Mute button pressed. value = " + b.isSelected());
        firePropertyChange(PropertyChangeID.MUTE, !b.isSelected(), b.isSelected());
        // do something.
    }

    /**
     * Handle a volume slider change
     */
    // NOTE: should this be public???
    public void volumeChange(ChangeEvent e) {
        JSlider v = (JSlider) e.getSource();
        log.debug("Volume slider moved. value = " + v.getValue());
        firePropertyChange(PropertyChangeID.VOLUME_CHANGE, v.getValue(), v.getValue());
    }

    // VSDecoderManager Events
    /**
     * Add a listener for this Pane's property change events
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        List<PropertyChangeListener> l = Arrays.asList(listenerList.getListeners(PropertyChangeListener.class));
        if (!l.contains(listener)) {
            listenerList.add(PropertyChangeListener.class, listener);
        }
    }

    /**
     * Remove a listener
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerList.remove(PropertyChangeListener.class, listener);
    }

    /**
     * Fire a property change from this object
     */
    // NOTE: should this be public???
    public void firePropertyChange(PropertyChangeID id, Object oldProp, Object newProp) {
        String pcname;

        // map the property change ID
        pcname = PCIDMap.get(id);
        // Fire the actual PropertyChangeEvent
        firePropertyChange(new PropertyChangeEvent(this, pcname, oldProp, newProp));
    }

    /**
     * Fire a property change from this object
     */
    void firePropertyChange(PropertyChangeEvent evt) {
        //Object[] listeners = listenerList.getListenerList();

        for (PropertyChangeListener l : listenerList.getListeners(PropertyChangeListener.class)) {
            l.propertyChange(evt);
        }
    }

    /**
     * getDecoder()
     *
     * Looks up the currently referenced decoder and returns it.
     */
    public VSDecoder getDecoder() {
        VSDecoder d = VSDecoderManager.instance().getVSDecoderByID(decoder_id);
        addPropertyChangeListener(d);
        return (d);
    }

    /**
     * Looks up a decoder profile by name and returns that decoder.
     * 
     * @param profile name of the profile to get
     * @return the decoder for the profile
     */
    public VSDecoder getDecoder(String profile) {
        @SuppressWarnings("deprecation")
        VSDecoder d = VSDecoderManager.instance().getVSDecoder(profile);
        addPropertyChangeListener(d);
        return (d);
    }

    /**
     * setDecoder()
     *
     * set the Decoder ID and update the soundsPanel
     */
    public void setDecoder(VSDecoder dec) {
        if (dec != null) {
            // Store the new decoder
            decoder_id = dec.getId();
            log.debug("Decoder ID = " + decoder_id + " Decoder = " + dec);
            // Register the decoder as a listener on our frame... so it can react
            // to the window closing
            parent.addWindowListener(new WindowListener() {
                @Override
                public void windowActivated(WindowEvent e) {
                }

                @Override
                public void windowClosed(WindowEvent e) {
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    VSDecoderManager.instance().getVSDecoderByID(decoder_id).windowChange(e);
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                }

                @Override
                public void windowDeiconified(WindowEvent e) {
                }

                @Override
                public void windowIconified(WindowEvent e) {
                }

                @Override
                public void windowOpened(WindowEvent e) {
                }
            });
            // Register ourselves as an event listener to the decoder
            dec.addEventListener(new VSDecoderListener() {
                @Override
                public void eventAction(VSDecoderEvent e) {
                    decoderEventAction(e);
                }
            });
            // Update the status bar
            if (dec.getPosition() != null) {
                statusBar.setMessage("Location: " + dec.getPosition().toString());
            } else {
                statusBar.setMessage("Location: unknown");
            }
            // Update the sounds pane
            tabbedPane.remove(soundsPanel);
            soundsPanel = new VSDSoundsPanel(decoder_id, this);
            tabbedPane.addTab("Sounds", soundsPanel);
            tabbedPane.revalidate();
            tabbedPane.repaint();
        }

    }

    /**
     * Handle an event from the VSDecoder
     */
    protected void decoderEventAction(VSDecoderEvent e) {
        // Update the status bar...
        if (e.getType() == VSDecoderEvent.EventType.LOCATION_CHANGE) {
            if (e.getData() != null) {
                jmri.util.PhysicalLocation p = (jmri.util.PhysicalLocation) e.getData();
                statusBar.setMessage("Location:" + p.toString());
            } else {
                statusBar.setMessage("Location: unknown");
            }
        }
    }

    /**
     * setAddress()
     *
     * Update the Decoder's address...
     */
    public void setAddress(LocoAddress a) {
        if (a != null) {
            log.debug("Pane Set Address: " + a);
            firePropertyChange(PropertyChangeID.ADDRESS_CHANGE, null, a);

            //VSDecoder decoder = VSDecoderManager.instance().getVSDecoderByID(decoder_id);
            //if (decoder != null) {
            //decoder.setAddress(a);
            //decoder.enable();
            ///}
            this.setTitle(a);
        }
    }

    /**
     * setTitle();
     *
     * Update the window title with the given address.
     */
    // SHould this be public?
    @Deprecated
    public void setTitle(DccLocoAddress a) {
        if (a != null) {
            parent.setTitle("VSDecoder - " + a.toString());
        }
    }

    // New version
    public void setTitle(LocoAddress a) {
        if (a != null) {
            parent.setTitle("VSDecoder - " + a.toString());
        }
    }

    /**
     * Handle window close event
     */
    public void windowClosing(WindowEvent e) {
        log.debug("VSDecoderPane windowClosing() called...");
    }

    private static final Logger log = LoggerFactory.getLogger(VSDecoderPane.class);
}
