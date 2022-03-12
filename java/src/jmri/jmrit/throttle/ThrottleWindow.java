package jmri.jmrit.throttle;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.jython.Jynstrument;
import jmri.jmrit.jython.JynstrumentFactory;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.iharder.dnd.URIDrop;

import org.jdom2.Element;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Should be named ThrottleFrame, but ThrottleFrame already exit, hence ThrottleWindow
public class ThrottleWindow extends JmriJFrame {

    private JPanel throttlesPanel;
    private ThrottleFrame currentThrottleFrame;
    private CardLayout throttlesLayout;

    private JCheckBoxMenuItem viewControlPanel;
    private JCheckBoxMenuItem viewFunctionPanel;
    private JCheckBoxMenuItem viewAddressPanel;
    private JCheckBoxMenuItem viewSpeedPanel;
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

    private final ThrottleWindowInputsListener myInputsListener;
    private final ThrottleWindowActionsFactory myActionFactory;

    private HashMap<String, ThrottleFrame> throttleFrames = new HashMap<>(5);
    private int cardCounterID = 0; // to generate unique names for each card
    private int cardCounterNB = 1; // real counter

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    /**
     * Default constructor
     */
    public ThrottleWindow() {
        super();
        if (jmri.InstanceManager.getNullableDefault(ThrottlesPreferences.class) == null) {
            log.debug("Creating new ThrottlesPreference Instance");
            jmri.InstanceManager.store(new ThrottlesPreferences(), ThrottlesPreferences.class);
        }
        myInputsListener = new ThrottleWindowInputsListener(this);
        myActionFactory = new ThrottleWindowActionsFactory(this);
        powerMgr = InstanceManager.getNullableDefault(PowerManager.class);
        if (powerMgr == null) {
            log.info("No power manager instance found, panel not active");
        }
        initGUI();
        applyPreferences();
    }

    private void initGUI() {
        setTitle(Bundle.getMessage("ThrottleTitle"));
        setLayout(new BorderLayout());
        throttlesLayout = new CardLayout();
        throttlesPanel = new JPanel(throttlesLayout);
        throttlesPanel.setDoubleBuffered(true);

        initializeToolbar();
        initializeMenu();

        setCurrentThrottleFrame(new ThrottleFrame(this));
        getCurrentThrottleFrame().setTitle("default");
        throttlesPanel.add(getCurrentThrottleFrame(), "default");
        throttleFrames.put("default", getCurrentThrottleFrame());
        add(throttlesPanel, BorderLayout.CENTER);

        installInputsListenerOnAllComponents(this);
        // to get something to put focus on
        getRootPane().setFocusable(true);

        ActionMap am = myActionFactory.buildActionMap();
        for (Object k : am.allKeys()) {
            getRootPane().getActionMap().put(k, am.get(k));
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ThrottleWindow me = (ThrottleWindow) e.getSource();
                InstanceManager.getDefault(ThrottleFrameManager.class).requestThrottleWindowDestruction(me);
                if (throttleToolBar != null) {
                    Component[] cmps = throttleToolBar.getComponents();
                    if (cmps != null) {
                        for (Component cmp : cmps) {
                            if (cmp instanceof Jynstrument) {
                                ((Jynstrument) cmp).exit();
                            }
                        }
                    }
                }
            }@Override
            public void windowOpened(WindowEvent e) {
                try { // on initial open, force selection of address panel
                    getCurrentThrottleFrame().getAddressPanel().setSelected(true);
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }            
        });
        updateGUI();
    }

    public void updateGUI() {
        // title bar
        getCurrentThrottleFrame().setFrameTitle();
        // menu items
        viewAddressPanel.setEnabled(isEditMode);
        viewControlPanel.setEnabled(isEditMode);
        viewFunctionPanel.setEnabled(isEditMode);
        viewSpeedPanel.setEnabled(isEditMode);
        if (isEditMode) {
            viewAddressPanel.setSelected(getCurrentThrottleFrame().getAddressPanel().isVisible());
            viewControlPanel.setSelected(getCurrentThrottleFrame().getControlPanel().isVisible());
            viewFunctionPanel.setSelected(getCurrentThrottleFrame().getFunctionPanel().isVisible());
            viewSpeedPanel.setSelected(getCurrentThrottleFrame().getSpeedPanel().isVisible());
        }
        fileMenuSave.setEnabled(getCurrentThrottleFrame().getLastUsedSaveFile() != null || getCurrentThrottleFrame().getRosterEntry() != null);
        editMenuExportRoster.setEnabled(getCurrentThrottleFrame().getRosterEntry() != null);
        // toolbar items
        if (jbPrevious != null) // means toolbar enabled
        {
            if (cardCounterNB > 1) {
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
        //    nouveau.setText(Bundle.getMessage("ThrottleToolBarNew"));
        jbNew.setIcon(new NamedIcon("resources/icons/throttles/add.png", "resources/icons/throttles/add.png"));
        jbNew.setToolTipText(Bundle.getMessage("ThrottleToolBarNewToolTip"));
        jbNew.setVerticalTextPosition(JButton.BOTTOM);
        jbNew.setHorizontalTextPosition(JButton.CENTER);
        jbNew.addActionListener(e -> addThrottleFrame());
        throttleToolBar.add(jbNew);

        jbClose = new JButton();
//     close.setText(Bundle.getMessage("ThrottleToolBarClose"));
        jbClose.setIcon(new NamedIcon("resources/icons/throttles/remove.png", "resources/icons/throttles/remove.png"));
        jbClose.setToolTipText(Bundle.getMessage("ThrottleToolBarCloseToolTip"));
        jbClose.setVerticalTextPosition(JButton.BOTTOM);
        jbClose.setHorizontalTextPosition(JButton.CENTER);
        jbClose.addActionListener(e -> removeThrottleFrame());
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
        //    next.setText(Bundle.getMessage("ThrottleToolBarNext"));
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

    public void setEditMode(boolean mode) {
        if (mode == isEditMode)
            return;
        isEditMode = mode;
        if (!throttleFrames.isEmpty()) {
            throttleFrames.values().forEach((throttleFrame) -> {
                throttleFrame.setEditMode(isEditMode);
            });
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
        ThrottleFrame.setTransparent(it, true);
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
                getCurrentThrottleFrame().loadThrottle();
            }
        });
        fileMenuSave = new JMenuItem(Bundle.getMessage("ThrottleFileMenuSaveThrottle"));
        fileMenuSave.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getCurrentThrottleFrame().saveThrottle();
            }
        });
        JMenuItem fileMenuSaveAs = new JMenuItem(Bundle.getMessage("ThrottleFileMenuSaveAsThrottle"));
        fileMenuSaveAs.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                getCurrentThrottleFrame().saveThrottleAs();
            }
        });

        fileMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction(Bundle.getMessage("MenuItemNewThrottle")));
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
        viewAddressPanel.addItemListener(e -> getCurrentThrottleFrame().getAddressPanel().setVisible(e.getStateChange() == ItemEvent.SELECTED));

        viewControlPanel = new JCheckBoxMenuItem(Bundle.getMessage("ThrottleMenuViewControlPanel"));
        viewControlPanel.setSelected(true);
        viewControlPanel.addItemListener(e -> getCurrentThrottleFrame().getControlPanel().setVisible(e.getStateChange() == ItemEvent.SELECTED));
        viewFunctionPanel = new JCheckBoxMenuItem(Bundle.getMessage("ThrottleMenuViewFunctionPanel"));
        viewFunctionPanel.setSelected(true);
        viewFunctionPanel.addItemListener(e -> getCurrentThrottleFrame().getFunctionPanel().setVisible(e.getStateChange() == ItemEvent.SELECTED));
        viewSpeedPanel = new JCheckBoxMenuItem(Bundle.getMessage("ThrottleMenuViewSpeedPanel"));
        viewSpeedPanel.setSelected(false);
        viewSpeedPanel.addItemListener(e -> getCurrentThrottleFrame().getSpeedPanel().setVisible(e.getStateChange() == ItemEvent.SELECTED));

        viewAllButtons = new JMenuItem(Bundle.getMessage("ThrottleMenuViewAllFunctionButtons"));
        viewAllButtons.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                getCurrentThrottleFrame().getFunctionPanel().resetFnButtons();
                getCurrentThrottleFrame().getFunctionPanel().setEnabled();
            }
        });

        JMenuItem makeAllComponentsInBounds = new JMenuItem(Bundle.getMessage("ThrottleMenuViewMakeAllComponentsInBounds"));
        makeAllComponentsInBounds.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                getCurrentThrottleFrame().makeAllComponentsInBounds();
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
        editMenuExportRoster.addActionListener(e -> getCurrentThrottleFrame().saveRosterChanges());
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
        if ((throttleFrames != null) && (!throttleFrames.isEmpty())) {
            throttleFrames.values().forEach((throttleFrame) -> {
                throttleFrame.dispose();
            });
        }
        throttleFrames = null;
        throttlesPanel.removeAll();
        removeAll();
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

    public ThrottleFrame getCurrentThrottleFrame() {
        return currentThrottleFrame;
    }

    public void setCurrentThrottleFrame(ThrottleFrame tf) {
        if (getCurrentThrottleFrame() != null) {
            log.debug("setCurrentThrottleFrame from {} to {}", getCurrentThrottleFrame().getAddressPanel().getCurrentAddress(), tf.getAddressPanel().getCurrentAddress());
        }
        pcs.firePropertyChange("ThrottleFrame", getCurrentThrottleFrame(), tf);
        currentThrottleFrame = tf;
    }

    public void removeThrottleFrame(ThrottleFrame tf) {
        if (cardCounterNB > 1) // we don't like empty ThrottleWindow
        {
            cardCounterNB--;
            if (getCurrentThrottleFrame() == tf) {
                log.debug("Closing last created");
            }
            throttlesPanel.remove(tf);
            throttleFrames.remove(tf.getTitle());
            tf.dispose();
            throttlesLayout.invalidateLayout(throttlesPanel);
        }
        updateGUI();
    }

    public void nextThrottleFrame() {
        throttlesLayout.next(throttlesPanel);
        updateGUI();
    }

    public void previousThrottleFrame() {
        throttlesLayout.previous(throttlesPanel);
        updateGUI();
    }

    public void previousRunningThrottleFrame() {
        if (!throttleFrames.isEmpty()) {
            ThrottleFrame cf = this.getCurrentThrottleFrame();
            ThrottleFrame nf = null;
            boolean passed = false;
            for (ThrottleFrame tf : throttleFrames.values()) {
                if (tf != cf) {
                    if ((tf.getAddressPanel() != null) && (tf.getAddressPanel().getThrottle() != null) && (tf.getAddressPanel().getThrottle().getSpeedSetting() > 0)) {
                        if (passed) { // if we found something and passed current value, then break
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
            }
        }
    }

    public void nextRunningThrottleFrame() {
        if (!throttleFrames.isEmpty()) {
            ThrottleFrame cf = this.getCurrentThrottleFrame();
            ThrottleFrame nf = null;
            for (ThrottleFrame tf : throttleFrames.values()) {
                if ((tf != cf) && (tf.getAddressPanel() != null) && (tf.getAddressPanel().getThrottle() != null) && (tf.getAddressPanel().getThrottle().getSpeedSetting() > 0)) {
                    nf = tf;
                }
                if ((tf == cf) && (nf != null)) { // if we found something, then break, else go to end
                    break;
                }
            }
            if (nf != null) {
                nf.toFront();
            }
        }
    }

    public void removeThrottleFrame() {
        removeThrottleFrame(getCurrentThrottleFrame());
    }

    public void addThrottleFrame(ThrottleFrame tp) {
        cardCounterID++;
        cardCounterNB++;
        String txt = "Card-" + cardCounterID;
        tp.setTitle(txt);
        throttleFrames.put(txt, tp);
        throttlesPanel.add(tp, txt);
        throttlesLayout.show(throttlesPanel, txt);
        if (!isEditMode) {
            tp.setEditMode(isEditMode);
        }
        updateGUI();
    }

    public ThrottleFrame addThrottleFrame() {
        setCurrentThrottleFrame(new ThrottleFrame(this));
        installInputsListenerOnAllComponents(getCurrentThrottleFrame());
        addThrottleFrame(getCurrentThrottleFrame());
        return getCurrentThrottleFrame();
    }

    public void toFront(String throttleFrameTitle) {
        throttlesLayout.show(throttlesPanel, throttleFrameTitle);
        setVisible(true);
        requestFocus();
        toFront();
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
        me.setAttribute("title", titleText);
        me.setAttribute("titleType", titleTextType);
        me.setAttribute("isEditMode",  String.valueOf(isEditMode));

        java.util.ArrayList<Element> children = new java.util.ArrayList<>(1);
        children.add(WindowPreferences.getPreferences(this));
        if (!throttleFrames.isEmpty()) {
            ThrottleFrame cf = this.getCurrentThrottleFrame();
            for (ThrottleFrame tf : throttleFrames.values()) {
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

    public void setXml(Element e) {
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
        if ((tfes != null) && (tfes.size() > 0)) {
            for (int i = 0; i < tfes.size(); i++) {
                ThrottleFrame tf;
                if (i == 0) {
                    tf = getCurrentThrottleFrame();
                } else {
                    tf = addThrottleFrame();
                }
                tf.setXml(tfes.get(i));
                tf.setEditMode(isEditMode);
            }
        }

        List<Element> jinsts = e.getChildren("Jynstrument");
        if ((jinsts != null) && (jinsts.size() > 0)) {
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
            c.addMouseWheelListener(myInputsListener);
            c.setFocusable(false);
        }
        for (Component component : c.getComponents()) {
            if (component instanceof Container) {
                installInputsListenerOnAllComponents( (Container) component);
            } else {
                if (! ( component instanceof JTextField)) {
                    component.addMouseWheelListener(myInputsListener);
                    component.setFocusable(false);
                }
            }
        }
    }

    public void applyPreferences() {
        ThrottlesPreferences preferences = InstanceManager.getDefault(ThrottlesPreferences.class);

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

        throttleToolBar.setVisible ( preferences.isUsingExThrottle() && preferences.isUsingToolBar() );

        if (smallPowerMgmtButton != null) {
            smallPowerMgmtButton.setVisible( (!preferences.isUsingExThrottle()) || (!preferences.isUsingToolBar()) );
        }

        throttleFrames.values().forEach(tf -> {
            tf.applyPreferences();
        });
    }

    private final static Logger log = LoggerFactory.getLogger(ThrottleWindow.class);
}
