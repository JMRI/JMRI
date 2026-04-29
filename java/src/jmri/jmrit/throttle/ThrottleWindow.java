package jmri.jmrit.throttle;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.jython.Jynstrument;
import jmri.jmrit.jython.JynstrumentFactory;
import jmri.jmrit.throttle.actions.ThrottleWindowActionsFactory;
import jmri.jmrit.throttle.actions.ThrottleWindowInputsListener;
import jmri.jmrit.throttle.preferences.ThrottlesPreferences;
import jmri.jmrit.throttle.buttons.LargePowerManagerButton;
import jmri.jmrit.throttle.buttons.SmallPowerManagerButton;
import jmri.jmrit.throttle.buttons.StopAllButton;
import jmri.jmrit.throttle.implementation.ThrottleFrame;
import jmri.jmrit.throttle.implementation.ThrottleFramePropertyEditor;
import jmri.jmrit.throttle.implementation.WindowPreferences;
import jmri.jmrit.throttle.interfaces.ThrottleControllerUI;
import jmri.jmrit.throttle.interfaces.ThrottleControllersUIContainer;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.iharder.dnd.URIDrop;
import jmri.util.swing.TransparencyUtils;

import org.jdom2.Element;
import org.jdom2.Attribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JMRI throttle window.
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Lionel Jeanson 2007-2026
 * 
 */

public class ThrottleWindow extends JmriJFrame implements ThrottleControllersUIContainer, PropertyChangeListener {

    private final jmri.jmrix.ConnectionConfig connectionConfig;
    private final ThrottleManager throttleManager;
    private final ThrottleFrameManager throttleFrameManager = InstanceManager.getDefault(ThrottleFrameManager.class);

    private JPanel throttlesPanel;
    private ThrottleFrame currentThrottleFrame;
    private CardLayout throttlesLayout;

    private JCheckBoxMenuItem viewControlPanel;
    private JCheckBoxMenuItem viewFunctionPanel;
    private JCheckBoxMenuItem viewAddressPanel;
    private JCheckBoxMenuItem viewSpeedPanel;
    private JCheckBoxMenuItem viewLocoIconPanel;
    private JCheckBoxMenuItem viewConsistFunctionsPanel;
    private JMenuItem viewAllButtons;
    private JMenuItem fileMenuSave;
    private JMenuItem editMenuExportRoster;

    private JButton jbPrevious = null;
    private JButton jbNext = null;
    private JButton jbPreviousRunning = null;
    private JButton jbNextRunning = null;
    private JButton jbThrottleList = null;
    private JButton jbNew = null;
    private JButton jbClose = null;
    private JButton jbMode = null;
    private JToolBar throttleToolBar;

    private String titleText = "";
    private String titleTextType = "rosterID";
    private boolean isEditMode = true;

    private final PowerManager powerMgr;
    private SmallPowerManagerButton smallPowerMgmtButton;

    private final ThrottleWindowActionsFactory myActionFactory;

    private ArrayList<ThrottleFrame> throttleFrames = new ArrayList<>(5);

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    /**
     * Default constructor
     */
    public ThrottleWindow() {
        this((jmri.jmrix.ConnectionConfig) null);
    }

    /**
     * Constructor
     * @param connectionConfig the connection config
     */
    public ThrottleWindow(jmri.jmrix.ConnectionConfig connectionConfig) {
        super(Bundle.getMessage("ThrottleTitle"));
        this.connectionConfig = connectionConfig;
        if (connectionConfig != null) {
            this.throttleManager = connectionConfig.getAdapter().getSystemConnectionMemo().get(jmri.ThrottleManager.class);
        } else {
            this.throttleManager = InstanceManager.getDefault(jmri.ThrottleManager.class);
        }
        myActionFactory = new ThrottleWindowActionsFactory(this);
        powerMgr = InstanceManager.getNullableDefault(PowerManager.class);
        if (powerMgr == null) {
            log.info("No power manager instance found, panel not active");
        }
        pcs.addPropertyChangeListener(throttleFrameManager.getThrottlesListPanel().getTableModel());        
        initGUI();
        InstanceManager.getDefault(ThrottlesPreferences.class).addPropertyChangeListener(this);
        applyPreferences();
    }

    /**
     * Create a ThrottleWindow
     * @param e the xml element for the throttle window
     * @return the throttle window
     */
    public static ThrottleWindow createThrottleWindow(Element e) {
        jmri.jmrix.ConnectionConfig connectionConfig = null;

        Attribute systemPrefixAttr = e.getAttribute("systemPrefix");
        if (systemPrefixAttr != null) {
            String systemPrefix = systemPrefixAttr.getValue();
            // Set connectionConfig to null in case the systemPrefix
            // points to a connection that doesn't exist anymore.

            for (jmri.jmrix.ConnectionConfig c : InstanceManager.getDefault(jmri.jmrix.ConnectionConfigManager.class)) {
                if (c.getAdapter().getSystemPrefix().equals(systemPrefix)) {
                    connectionConfig = c;
                }
            }
        }

        ThrottleWindow tw = new ThrottleWindow(connectionConfig);
        tw.setXml(e);
        return tw;
    }

    private void initGUI() {
        setTitle(Bundle.getMessage("ThrottleTitle"));
        setLayout(new BorderLayout());
        throttlesLayout = new CardLayout();
        throttlesPanel = new JPanel(throttlesLayout);
        throttlesPanel.setDoubleBuffered(true);

        initializeToolbar();
        initializeMenu();

        String txt = "ThrottleJDesktopPane-" + throttleFrameManager.generateUniqueFrameID();
        setCurrentThrottleFrame(new ThrottleFrame(this, throttleManager));
        getCurentThrottleController().setTitle(txt);
        throttlesPanel.add(txt, getCurentThrottleController());
        throttleFrames.add(getCurentThrottleController());
        add(throttlesPanel, BorderLayout.CENTER);

        installInputsListenerOnAllComponents(this);
        // to get something to put focus on
        getRootPane().setFocusable(true);

        ActionMap am = myActionFactory.buildActionMap();
        for (Object k : am.allKeys()) {
            getRootPane().getActionMap().put(k, am.get(k));
        }
        
        addMouseWheelListener( new ThrottleWindowInputsListener(this) );

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
              // on initial open, force selection of address panel  
              getCurentThrottleController().forceAddressPanelSelected();
            }
        });
        updateGUI();
    }

    public void updateGUI() {
        if (getCurentThrottleController() == null) {
            return;
        }
        // title bar
        getCurentThrottleController().updateFrameTitle();
        // menu items
        viewAddressPanel.setEnabled(isEditMode);
        viewControlPanel.setEnabled(isEditMode);
        viewFunctionPanel.setEnabled(isEditMode);
        viewSpeedPanel.setEnabled(isEditMode);
        viewLocoIconPanel.setEnabled(isEditMode);
        viewConsistFunctionsPanel.setEnabled(isEditMode);
        if (isEditMode) {
            viewAddressPanel.setSelected(getCurentThrottleController().isAddressPanelVisible());
            viewControlPanel.setSelected(getCurentThrottleController().isControlPanelVisible());
            viewFunctionPanel.setSelected(getCurentThrottleController().isFunctionPanelVisible());
            viewSpeedPanel.setSelected(getCurentThrottleController().isSpeedPanelVisible());
            viewLocoIconPanel.setSelected(getCurentThrottleController().isLocoIconPanelVisible());
            viewConsistFunctionsPanel.setSelected(getCurentThrottleController().isConsistFunctionsPanelVisible());
        }
        fileMenuSave.setEnabled(getCurentThrottleController().isKnownLastUsedSaveFile() || getCurentThrottleController().isKnownRosterEntry());
        editMenuExportRoster.setEnabled(getCurentThrottleController().isKnownRosterEntry());
        // toolbar items
        if (jbPrevious != null) // means toolbar enabled
        {
            if (throttleFrames.size() > 1) {
                jbPrevious.setEnabled(true);
                jbNext.setEnabled(true);
                jbClose.setEnabled(true);
                jbPreviousRunning.setEnabled(true);
                jbNextRunning.setEnabled(true);
            } else {
                jbPrevious.setEnabled(false);
                jbNext.setEnabled(false);
                jbClose.setEnabled(false);
                jbPreviousRunning.setEnabled(false);
                jbNextRunning.setEnabled(false);
            }
        }
        getRootPane().requestFocusInWindow();
    }

    private void initializeToolbar() {
        throttleToolBar = new JToolBar("Throttles toolbar");

        jbNew = new JButton();
        jbNew.setIcon(new NamedIcon("resources/icons/throttles/add.png", "resources/icons/throttles/add.png"));
        jbNew.setToolTipText(Bundle.getMessage("ThrottleToolBarNewToolTip"));
        jbNew.setVerticalTextPosition(JButton.BOTTOM);
        jbNew.setHorizontalTextPosition(JButton.CENTER);
        jbNew.addActionListener(e -> newThrottleController());
        throttleToolBar.add(jbNew);

        jbClose = new JButton();
        jbClose.setIcon(new NamedIcon("resources/icons/throttles/remove.png", "resources/icons/throttles/remove.png"));
        jbClose.setToolTipText(Bundle.getMessage("ThrottleToolBarCloseToolTip"));
        jbClose.setVerticalTextPosition(JButton.BOTTOM);
        jbClose.setHorizontalTextPosition(JButton.CENTER);
        jbClose.addActionListener(e -> removeAndDisposeCurentThrottleFrame());
        throttleToolBar.add(jbClose);

        throttleToolBar.addSeparator();

        jbPreviousRunning = new JButton();
        jbPreviousRunning.setIcon(new NamedIcon("resources/icons/throttles/previous-jump.png", "resources/icons/throttles/previous-jump.png"));
        jbPreviousRunning.setVerticalTextPosition(JButton.BOTTOM);
        jbPreviousRunning.setHorizontalTextPosition(JButton.CENTER);
        jbPreviousRunning.setToolTipText(Bundle.getMessage("ThrottleToolBarPrevRunToolTip"));
        jbPreviousRunning.addActionListener(e -> previousRunningThrottleFrame());
        throttleToolBar.add(jbPreviousRunning);

        jbPrevious = new JButton();
        jbPrevious.setIcon(new NamedIcon("resources/icons/throttles/previous.png", "resources/icons/throttles/previous.png"));
        jbPrevious.setVerticalTextPosition(JButton.BOTTOM);
        jbPrevious.setHorizontalTextPosition(JButton.CENTER);
        jbPrevious.setToolTipText(Bundle.getMessage("ThrottleToolBarPrevToolTip"));
        jbPrevious.addActionListener(e -> previousThrottleFrame());
        throttleToolBar.add(jbPrevious);

        jbNext = new JButton();
        jbNext.setIcon(new NamedIcon("resources/icons/throttles/next.png", "resources/icons/throttles/next.png"));
        jbNext.setToolTipText(Bundle.getMessage("ThrottleToolBarNextToolTip"));
        jbNext.setVerticalTextPosition(JButton.BOTTOM);
        jbNext.setHorizontalTextPosition(JButton.CENTER);
        jbNext.addActionListener(e -> nextThrottleFrame());
        throttleToolBar.add(jbNext);

        jbNextRunning = new JButton();
        jbNextRunning.setIcon(new NamedIcon("resources/icons/throttles/next-jump.png", "resources/icons/throttles/next-jump.png"));
        jbNextRunning.setToolTipText(Bundle.getMessage("ThrottleToolBarNextRunToolTip"));
        jbNextRunning.setVerticalTextPosition(JButton.BOTTOM);
        jbNextRunning.setHorizontalTextPosition(JButton.CENTER);
        jbNextRunning.addActionListener(e -> nextRunningThrottleFrame());
        throttleToolBar.add(jbNextRunning);

        throttleToolBar.addSeparator();

        throttleToolBar.add(new StopAllButton());

        if (powerMgr != null) {
            throttleToolBar.add(new LargePowerManagerButton(false));
        }

        throttleToolBar.addSeparator();

        jbMode = new JButton();
        jbMode.setIcon(new NamedIcon("resources/icons/throttles/edit-view.png", "resources/icons/throttles/edit-view.png"));
        jbMode.setToolTipText(Bundle.getMessage("ThrottleToolBarEditToolTip"));
        jbMode.setVerticalTextPosition(JButton.BOTTOM);
        jbMode.setHorizontalTextPosition(JButton.CENTER);
        jbMode.addActionListener(e -> setEditMode( !isEditMode ));
        throttleToolBar.add(jbMode);

        throttleToolBar.addSeparator();

        jbThrottleList = new JButton();
        jbThrottleList.setIcon(new NamedIcon("resources/icons/throttles/list.png", "resources/icons/throttles/list.png"));
        jbThrottleList.setToolTipText(Bundle.getMessage("ThrottleToolBarOpenThrottleListToolTip"));
        jbThrottleList.setVerticalTextPosition(JButton.BOTTOM);
        jbThrottleList.setHorizontalTextPosition(JButton.CENTER);
        jbThrottleList.addActionListener(new ThrottlesListAction());
        throttleToolBar.add(jbThrottleList);

        // Receptacle for Jynstruments
        new URIDrop(throttleToolBar, uris -> {
                for (URI uri : uris ) {
                    ynstrument(new File(uri).getPath());
                }
            });

        add(throttleToolBar, BorderLayout.PAGE_START);
    }
    
    /**
     * Return the number of active thottle frames.
     *
     * @return the number of active thottle frames.
     */
    @Override
    public  int getNbThrottlesControllers() {
        return throttleFrames.size() ;
    }
        
    /**
     * Return the nth thottle frame of that throttle window
     *
     * @param n index of thr throtle frame
     * @return the nth thottle frame of that throttle window
     */
    @Override
    public ThrottleControllerUI getThrottleControllerAt(int n) {
        if (! (n < throttleFrames.size())) {
            return null;
        }
        return throttleFrames.get(n);
    }
    
    /**
     * Get the number of usages of a particular Loco Address.
     * @param la the Loco Address, can be null.
     * @return 0 if no usages, else number of AddressPanel usages.
     */
    @Override
    public int getNumberOfEntriesFor(@CheckForNull DccLocoAddress la) {
        if (la == null) { 
            return 0; 
        }
        int ret = 0;
        for (ThrottleFrame tf: throttleFrames) {
            if (tf.isUsingAddress(la)) {
                ret++; // in use, increment count.
            }
        }
        return ret;
    }
       
    @Override
    public void emergencyStopAll() {
        if (!throttleFrames.isEmpty()) {
            for (ThrottleFrame tf: throttleFrames) {
                tf.eStop();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setTitle(String title) {
        if (connectionConfig != null) {
            super.setTitle(Bundle.getMessage("ThrottleTitleWithConnection", title, connectionConfig.getConnectionName()));
        } else {
            super.setTitle(title);
        }
    }

    public void setEditMode(boolean mode) {
        if (mode == isEditMode)
            return;
        isEditMode = mode;
        if (!throttleFrames.isEmpty()) {
            for (ThrottleFrame tf: throttleFrames) {
                tf.setEditMode(isEditMode);
            }
        }
        updateGUI();
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public Jynstrument ynstrument(String path) {
        Jynstrument it = JynstrumentFactory.createInstrument(path, this);
        if (it == null) {
            log.error("Error while creating Jynstrument {}", path);
            return null;
        }
        TransparencyUtils.setOpacityRec(it, true);
        it.setVisible(true);
        throttleToolBar.add(it);
        throttleToolBar.repaint();
        return it;
    }

    /**
     * Set up View, Edit and Power Menus
     */
    private void initializeMenu() {
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));

        JMenuItem fileMenuLoad = new JMenuItem(Bundle.getMessage("ThrottleFileMenuLoadThrottle"));
        fileMenuLoad.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getCurentThrottleController().loadThrottleFile(null);
            }
        });
        fileMenuSave = new JMenuItem(Bundle.getMessage("ThrottleFileMenuSaveThrottle"));
        fileMenuSave.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getCurentThrottleController().saveThrottle();
            }
        });
        JMenuItem fileMenuSaveAs = new JMenuItem(Bundle.getMessage("ThrottleFileMenuSaveAsThrottle"));
        fileMenuSaveAs.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getCurentThrottleController().saveThrottleAs();
            }
        });

        jmri.jmrit.throttle.ThrottleCreationAction.addNewThrottleItemsToThrottleMenu(fileMenu);
        fileMenu.add(fileMenuLoad);
        fileMenu.add(fileMenuSave);
        fileMenu.add(fileMenuSaveAs);
        fileMenu.addSeparator();

        fileMenu.add(new jmri.jmrit.throttle.LoadXmlThrottlesLayoutAction(Bundle.getMessage("MenuItemLoadThrottleLayout")));
        fileMenu.add(new jmri.jmrit.throttle.StoreXmlThrottlesLayoutAction(Bundle.getMessage("MenuItemSaveThrottleLayout")));
        fileMenu.addSeparator();
        fileMenu.add(new jmri.jmrit.throttle.LoadDefaultXmlThrottlesLayoutAction(Bundle.getMessage("MenuItemLoadDefaultThrottleLayout")));
        fileMenu.add(new jmri.jmrit.throttle.StoreDefaultXmlThrottlesLayoutAction(Bundle.getMessage("MenuItemSaveAsDefaultThrottleLayout")));
        fileMenu.addSeparator();
        fileMenu.add(new jmri.jmrit.withrottle.WiThrottleCreationAction(Bundle.getMessage("MenuItemStartWiThrottle")));

        JMenu viewMenu = new JMenu(Bundle.getMessage("ThrottleMenuView"));
        viewAddressPanel = new JCheckBoxMenuItem(Bundle.getMessage("ThrottleMenuViewAddressPanel"));
        viewAddressPanel.setSelected(true);
        viewAddressPanel.addItemListener(e -> getCurentThrottleController().setAddressPanelVisible(e.getStateChange() == ItemEvent.SELECTED));

        viewControlPanel = new JCheckBoxMenuItem(Bundle.getMessage("ThrottleMenuViewControlPanel"));
        viewControlPanel.setSelected(true);
        viewControlPanel.addItemListener(e -> getCurentThrottleController().setControlPanelVisible(e.getStateChange() == ItemEvent.SELECTED));
        viewFunctionPanel = new JCheckBoxMenuItem(Bundle.getMessage("ThrottleMenuViewFunctionPanel"));
        viewFunctionPanel.setSelected(true);
        viewFunctionPanel.addItemListener(e -> getCurentThrottleController().setFunctionPanelVisible(e.getStateChange() == ItemEvent.SELECTED));
        viewSpeedPanel = new JCheckBoxMenuItem(Bundle.getMessage("ThrottleMenuViewSpeedPanel"));
        viewSpeedPanel.setSelected(false);
        viewSpeedPanel.addItemListener(e -> getCurentThrottleController().setSpeedPanelVisible(e.getStateChange() == ItemEvent.SELECTED));
        viewLocoIconPanel = new JCheckBoxMenuItem(Bundle.getMessage("ThrottleMenuViewLocoIconPanel"));
        viewLocoIconPanel.setSelected(false);
        viewLocoIconPanel.addItemListener(e -> getCurentThrottleController().setLocoIconPanelVisible(e.getStateChange() == ItemEvent.SELECTED));
        viewConsistFunctionsPanel = new JCheckBoxMenuItem(Bundle.getMessage("ThrottleMenuViewConsistFunctionsPanel"));
        viewConsistFunctionsPanel.setSelected(false);
        viewConsistFunctionsPanel.addItemListener(e -> getCurentThrottleController().setConsistFunctionsPanelVisible(e.getStateChange() == ItemEvent.SELECTED));

        viewAllButtons = new JMenuItem(Bundle.getMessage("ThrottleMenuViewAllFunctionButtons"));
        viewAllButtons.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                getCurentThrottleController().resetFunctionPanelButton();
            }
        });

        JMenuItem makeAllComponentsInBounds = new JMenuItem(Bundle.getMessage("ThrottleMenuViewMakeAllComponentsInBounds"));
        makeAllComponentsInBounds.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                getCurentThrottleController().makeAllComponentsInBounds();
            }
        });

        JMenuItem switchViewMode = new JMenuItem(Bundle.getMessage("ThrottleMenuViewSwitchMode"));
        switchViewMode.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                setEditMode(!isEditMode);
            }
        });
        JMenuItem viewThrottlesList = new JMenuItem(Bundle.getMessage("ThrottleMenuViewViewThrottleList"));
        viewThrottlesList.addActionListener(new ThrottlesListAction());

        viewMenu.add(viewAddressPanel);
        viewMenu.add(viewControlPanel);
        viewMenu.add(viewFunctionPanel);
        viewMenu.add(viewSpeedPanel);
        viewMenu.add(viewLocoIconPanel);
        viewMenu.add(viewConsistFunctionsPanel);
        viewMenu.addSeparator();
        viewMenu.add(viewAllButtons);
        viewMenu.add(makeAllComponentsInBounds);
        viewMenu.addSeparator();
        viewMenu.add(switchViewMode);
        viewMenu.add(viewThrottlesList);

        JMenu editMenu = new JMenu(Bundle.getMessage("MenuEdit"));
        JMenuItem preferencesItem = new JMenuItem(Bundle.getMessage("ThrottleMenuEditFrameProperties"));
        editMenu.add(preferencesItem);
        preferencesItem.addActionListener(e -> editPreferences());
        editMenuExportRoster = new JMenuItem(Bundle.getMessage("ThrottleMenuEditSaveCustoms"));
        editMenu.add(editMenuExportRoster);
        editMenuExportRoster.addActionListener(e -> getCurentThrottleController().saveRosterChanges());
        editMenu.addSeparator();
        editMenu.add(new jmri.jmrit.throttle.ThrottlesPreferencesAction(Bundle.getMessage("MenuItemThrottlesPreferences"))); // now in tabbed preferences

        this.setJMenuBar(new JMenuBar());
        this.getJMenuBar().add(fileMenu);
        this.getJMenuBar().add(editMenu);
        this.getJMenuBar().add(viewMenu);

        if (powerMgr != null) {
            JMenu powerMenu = new JMenu(Bundle.getMessage("ThrottleMenuPower"));
            JMenuItem powerOn = new JMenuItem(Bundle.getMessage("ThrottleMenuPowerOn"));
            powerMenu.add(powerOn);
            powerOn.addActionListener(e -> {
                try {
                    powerMgr.setPower(PowerManager.ON);
                } catch (JmriException e1) {
                    log.error("Error when setting power: ", e1);
                }
            });

            JMenuItem powerOff = new JMenuItem(Bundle.getMessage("ThrottleMenuPowerOff"));
            powerMenu.add(powerOff);
            powerOff.addActionListener(e -> {
                try {
                    powerMgr.setPower(PowerManager.OFF);
                } catch (JmriException e1) {
                    log.error("Error when setting power: ", e1);
                }
            });

            this.getJMenuBar().add(powerMenu);

            smallPowerMgmtButton = new SmallPowerManagerButton();
            this.getJMenuBar().add(smallPowerMgmtButton);
        }

        // add help selection
        addHelpMenu("package.jmri.jmrit.throttle.ThrottleFrame", true);
    }

    private void editPreferences() {
        ThrottleFramePropertyEditor editor = new ThrottleFramePropertyEditor(this);
        editor.setVisible(true);
    }

    /**
     * Handle my own destruction.
     * <ol>
     * <li> dispose of sub windows.
     * <li> notify my manager of my demise.
     * </ol>
     *
     */
    @Override
    public void dispose() {        
        InstanceManager.getDefault(ThrottlesPreferences.class).removePropertyChangeListener(this);
        if (throttleToolBar != null) {
            URIDrop.remove(throttleToolBar);
            Component[] cmps = throttleToolBar.getComponents();
            if (cmps != null) {
                for (Component cmp : cmps) {
                    if (cmp instanceof Jynstrument) {
                        ((Jynstrument) cmp).exit();
                    }
                }
            }
        }
        if ((throttleFrames != null) && (!throttleFrames.isEmpty())) {
            for (ThrottleFrame tf: throttleFrames) {
                tf.dispose();
            }
            throttleFrames.clear();
        }
        throttleFrames = null;
        currentThrottleFrame  = null;
        for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
            pcs.removePropertyChangeListener(pcl);
        }
        for (MouseWheelListener mwl : getMouseWheelListeners()) {
            removeMouseWheelListener(mwl);
        }
        getRootPane().getActionMap().clear();
        throttlesPanel.removeAll();        
        removeAll();
        throttleFrameManager.requestThrottleWindowDestruction(this);
        super.dispose();
    }

    public JCheckBoxMenuItem getViewControlPanel() {
        return viewControlPanel;
    }

    public JCheckBoxMenuItem getViewFunctionPanel() {
        return viewFunctionPanel;
    }

    public JCheckBoxMenuItem getViewAddressPanel() {
        return viewAddressPanel;
    }

    public JCheckBoxMenuItem getViewSpeedPanel() {
        return viewSpeedPanel;
    }

    public JCheckBoxMenuItem getViewLocoIconPanel() {
        return viewLocoIconPanel;
    }

    public JCheckBoxMenuItem getViewConsistFunctionsPanel() {
        return viewConsistFunctionsPanel;
    }
    
    private void updateCurentThrottleFrame() {
        currentThrottleFrame = null;
        for (Component comp : throttlesPanel.getComponents()) {
            if (comp instanceof ThrottleFrame && comp.isVisible()) {
                currentThrottleFrame = (ThrottleFrame) comp;
            }
        }
    }

    @Override
    public ThrottleFrame getCurentThrottleController() {
        return currentThrottleFrame;
    }

    public void setCurrentThrottleFrame(ThrottleFrame tf) {
        pcs.firePropertyChange("ThrottleFrameChanged", getCurentThrottleController(), tf);
        currentThrottleFrame = tf;
    }

    @Override
    public void removeThrottleController(ThrottleControllerUI tf) {
        if (!(tf instanceof ThrottleFrame)) {
            throw new IllegalArgumentException("Only ThrottleFrame can be removed from ThrottleWindow");
        }
        if (getCurentThrottleController() == tf) {
            log.debug("Closing currently active throttle frame");
            throttlesLayout.previous(throttlesPanel);
        }
        throttlesPanel.remove((ThrottleFrame)tf);
        throttleFrames.remove(tf);
        throttlesLayout.invalidateLayout(throttlesPanel);
        updateGUI();
        updateCurentThrottleFrame();
        pcs.firePropertyChange("ThrottleFrameRemoved", tf, getCurentThrottleController());
    }

    /**
     * Set next throttle frame as current frame. If the current frame is the only one, then do nothing.
     * 
     */
    public void nextThrottleFrame() {
        ThrottleFrame otf = getCurentThrottleController();
        throttlesLayout.next(throttlesPanel);
        updateCurentThrottleFrame();
        updateGUI();
        pcs.firePropertyChange("ThrottleFrameChanged", otf, getCurentThrottleController());
    }

    /**
     * Set previous throttle frame as current frame. If the current frame is the only one, then do nothing.
     * 
     */
    public void previousThrottleFrame() {
        ThrottleFrame otf = getCurentThrottleController();
        throttlesLayout.previous(throttlesPanel);
        updateCurentThrottleFrame();
        updateGUI();
        pcs.firePropertyChange("ThrottleFrameChanged", otf, getCurentThrottleController());
    }


    /**
     * Set next running (with non null speed) throttle frame as current frame. If the current frame is the only one running, then do nothing.
     *
     */
    public void nextRunningThrottleFrame() {
        if (!throttleFrames.isEmpty()) {
            ThrottleFrame cf = this.getCurentThrottleController();
            ThrottleFrame nf = null;
            boolean passed = false;
            for (ThrottleFrame tf : throttleFrames) {
                if (tf != cf) {
                    if (tf.isRunning()) {
                        if (passed) { // if we passed the curent one, and found something then return it
                            nf = tf;
                            break;
                        } else if (nf == null) {
                            nf = tf;
                        }
                    }
                } else {
                    passed = true;
                }
            }
            if (nf != null) {
                nf.toFront();
                updateCurentThrottleFrame();
                pcs.firePropertyChange("ThrottleFrameChanged", cf, nf);
            }
        }
    }

    /**
     * Set previous running (with non null speed) throttle frame as current frame. If the current frame is the only one running, then do nothing.
     *
     */
    public void previousRunningThrottleFrame() {
        if (!throttleFrames.isEmpty()) {
            ThrottleFrame cf = this.getCurentThrottleController();
            ThrottleFrame nf = null;            
            for (ThrottleFrame tf : throttleFrames) {
                if (tf.isRunning()) {
                    nf = tf;
                }
                if ((tf == cf) && (nf != null)) { // return the last one found before the curent one
                    break;
                }
            }
            if (nf != null) {
                nf.toFront();
                updateCurentThrottleFrame();
                pcs.firePropertyChange("ThrottleFrameChanged", cf, nf);
            }
        }
    }

    /**
     * Set next throttle frame with active function (at least one function ON) or non null speed as current frame. If the current frame is the only one with active function, then do nothing.
     * 
     */
    public void nextThrottleFrameWithActiveFunction() {
        if (!throttleFrames.isEmpty()) {
            ThrottleFrame cf = this.getCurentThrottleController();
            ThrottleFrame nf = null;
            boolean passed = false;
            for (ThrottleFrame tf : throttleFrames) {
                if (tf != cf) {
                    if (tf.isActive()) {
                        if (passed) { // if we passed the curent one, and found something then return it
                            nf = tf;
                            break;
                        } else if (nf == null) {
                            nf = tf;
                        }
                    }
                } else {
                    passed = true;
                }
            }
            if (nf != null) {
                nf.toFront();
                updateCurentThrottleFrame();
                pcs.firePropertyChange("ThrottleFrameChanged", cf, nf);
            }
        }
    }

    /**
     * Set previous throttle frame with active function (at least one function ON) or non null speed as current frame. If the current frame is the only one with active function, then do nothing.
     * 
     */
    public void previousThrottleFrameWithActiveFunction() {
        if (!throttleFrames.isEmpty()) {
            ThrottleFrame cf = this.getCurentThrottleController();
            ThrottleFrame nf = null;            
            for (ThrottleFrame tf : throttleFrames) {
                if ((tf != cf) && tf.isActive()) {
                    nf = tf;
                }
                if ((tf == cf) && (nf != null)) { // return the last one found before the curent one
                    break;
                }
            }
            if (nf != null) {
                nf.toFront();
                updateCurentThrottleFrame();
                pcs.firePropertyChange("ThrottleFrameChanged", cf, nf);
            }
        }
    }
    
    private void removeAndDisposeCurentThrottleFrame() {
        ThrottleFrame tf = getCurentThrottleController();
        removeThrottleController(getCurentThrottleController());
        tf.dispose();
    }

    @Override
    public void addThrottleControllerAt(ThrottleControllerUI tp, int idx) {
        if (!(tp instanceof ThrottleFrame)) {
            throw new IllegalArgumentException("Only ThrottleFrame supported in ThrottleWindow");
        }

        String txt = "ThrottleJDesktopPane-" + throttleFrameManager.generateUniqueFrameID();
        ((ThrottleFrame)tp).setTitle(txt);
        if (idx>throttleFrames.size()) {
            idx = throttleFrames.size();
        }
        throttleFrames.add(idx,(ThrottleFrame)tp);
        throttlesPanel.add((ThrottleFrame)tp,txt,idx);
        ((ThrottleFrame)tp).setEditMode(isEditMode); // sync with window     
        updateGUI();
        pcs.firePropertyChange("ThrottleFrameAdded", null, this);
    }

    @Override
    public ThrottleControllerUI newThrottleController() {
        ThrottleFrame otf = getCurentThrottleController();
        ThrottleFrame tf = new ThrottleFrame(this, throttleManager);
        throttleFrames.add(tf);
        String txt = "ThrottleJDesktopPane-" + throttleFrameManager.generateUniqueFrameID();
        tf.setTitle(txt);        
        throttlesPanel.add(tf, txt);  
        tf.setEditMode(isEditMode); // sync with window                
        installInputsListenerOnAllComponents(tf);
        throttlesLayout.show(throttlesPanel, txt);
        setCurrentThrottleFrame(tf);
        updateGUI();
        pcs.firePropertyChange("ThrottleFrameNew", otf, getCurentThrottleController());
        return getCurentThrottleController();
    }

    public void toFront(String throttleFrameTitle) {
        ThrottleFrame otf = getCurentThrottleController();
        throttlesLayout.show(throttlesPanel, throttleFrameTitle);
        updateCurentThrottleFrame();
        setVisible(true);
        requestFocus();
        toFront();
        pcs.firePropertyChange("ThrottleFrameChanged", otf, getCurentThrottleController());
    }

    public String getTitleTextType() {
        return titleTextType;
    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public void setTitleTextType(String titleTextType) {
        this.titleTextType = titleTextType;
    }

    public Element getXml() {
        Element me = new Element("ThrottleWindow");
        if (connectionConfig != null) {
            me.setAttribute("systemPrefix", connectionConfig.getAdapter().getSystemPrefix());
        }
        me.setAttribute("title", titleText);
        me.setAttribute("titleType", titleTextType);
        me.setAttribute("isEditMode",  String.valueOf(isEditMode));

        java.util.ArrayList<Element> children = new java.util.ArrayList<>(1);
        children.add(WindowPreferences.getPreferences(this));
        if (!throttleFrames.isEmpty()) {
            ThrottleFrame cf = this.getCurentThrottleController();
            for (ThrottleFrame tf : throttleFrames) {
                if ((InstanceManager.getDefault(ThrottlesPreferences.class).isUsingExThrottle()) && (InstanceManager.getDefault(ThrottlesPreferences.class).isSavingThrottleOnLayoutSave())) {
                    tf.toFront();
                    tf.saveThrottle();
                }
                Element tfe = tf.getXmlFile();
                if (tfe == null) {
                    tfe = tf.getXml();
                }
                children.add(tfe);
            }
            if (cf != null) {
                cf.toFront();
            }
        }

        // Save Jynstruments
        if (throttleToolBar != null) {
            Component[] cmps = throttleToolBar.getComponents();
            if (cmps != null) {
                for (Component cmp : cmps) {
                    try {
                        if (cmp instanceof Jynstrument) {
                            Jynstrument jyn = (Jynstrument) cmp;
                            Element elt = new Element("Jynstrument");
                            elt.setAttribute("JynstrumentFolder", FileUtil.getPortableFilename(jyn.getFolder()));
                            Element je = jyn.getXml();
                            if (je != null) {
                                java.util.ArrayList<Element> jychildren = new java.util.ArrayList<>(1);
                                jychildren.add(je);
                                elt.setContent(jychildren);
                            }
                            children.add(elt);
                        }

                    } catch (Exception ex) {
                        log.debug("Got exception (no panic): ", ex);
                    }
                }
            }
        }
        me.setContent(children);
        return me;
    }

    private void setXml(Element e) {
        if (e.getAttribute("title") != null) {
            setTitle(e.getAttribute("title").getValue());
        }
        if (e.getAttribute("title") != null) {
            setTitleText(e.getAttribute("title").getValue());
        }
        if (e.getAttribute("titleType") != null) {
            setTitleTextType(e.getAttribute("titleType").getValue());
        }
        if (e.getAttribute("isEditMode") != null) {
            isEditMode = Boolean.parseBoolean(e.getAttribute("isEditMode").getValue());
        }

        Element window = e.getChild("window");
        if (window != null) {
            WindowPreferences.setPreferences(this, window);
        }

        List<Element> tfes = e.getChildren("ThrottleFrame");
        if ((tfes != null) && (!tfes.isEmpty())) {
            for (int i = 0; i < tfes.size(); i++) {
                ThrottleFrame tf;
                if (i == 0) {
                    tf = getCurentThrottleController();
                } else {
                    tf = (ThrottleFrame) newThrottleController();
                }
                tf.setXml(tfes.get(i));
                tf.setEditMode(isEditMode);
            }
        }

        List<Element> jinsts = e.getChildren("Jynstrument");
        if ((jinsts != null) && (!jinsts.isEmpty())) {
            jinsts.forEach((jinst) -> {
                Jynstrument jyn = ynstrument(FileUtil.getExternalFilename(jinst.getAttributeValue("JynstrumentFolder")));
                if (jyn != null) {
                    jyn.setXml(jinst);
                }
            });
        }

        updateGUI();
    }

    @Override
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private void installInputsListenerOnAllComponents(Container c) {
        c.setFocusTraversalKeysEnabled(false); // make tab and shift tab available
        if (! ( c instanceof JTextField)) {
            c.setFocusable(false);
        }
        for (Component component : c.getComponents()) {
            if (component instanceof Container) {
                installInputsListenerOnAllComponents( (Container) component);
            } else {
                if (! ( component instanceof JTextField)) {
                    component.setFocusable(false);
                }
            }
        }
    }

    private void applyPreferences() {
        ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);
        // inputs
        ComponentInputMap im = new ComponentInputMap(getRootPane());
        for (Object k : this.getRootPane().getActionMap().allKeys()) {
            KeyStroke[] kss = preferences.getThrottlesKeyboardControls().getKeyStrokes((String)k);
            if (kss !=null) {
                for (KeyStroke keystroke : kss) {
                    if (keystroke != null) {
                        im.put(keystroke, k);
                    }
                }
            }
        }
        getRootPane().setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW,im);
        // UI elements
        throttleToolBar.setVisible ( preferences.isUsingExThrottle() && preferences.isUsingToolBar() );
        if (smallPowerMgmtButton != null) {
            smallPowerMgmtButton.setVisible( (!preferences.isUsingExThrottle()) || (!preferences.isUsingToolBar()) );
        }
        if (! preferences.isUsingExThrottle()) {        
            setEditMode(true);            
        } 
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ThrottlesPreferences.prefPopertyName.compareTo(evt.getPropertyName()) == 0) {
            applyPreferences();
        }        
    }

    private static final Logger log = LoggerFactory.getLogger(ThrottleWindow.class);
}
