package jmri.jmrit.roster.swing;

import apps.AppsBase;
import apps.gui3.Apps3;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.help.SwingHelpUtilities;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import jmri.AddressedProgrammerManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ShutDownManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.progsupport.ProgModeSelector;
import jmri.jmrit.progsupport.ProgServiceModeComboBox;
import jmri.jmrit.roster.CopyRosterItemAction;
import jmri.jmrit.roster.DeleteRosterItemAction;
import jmri.jmrit.roster.ExportRosterItemAction;
import jmri.jmrit.roster.IdentifyLoco;
import jmri.jmrit.roster.PrintRosterEntry;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntrySelector;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.jmrit.symbolicprog.ProgrammerConfigManager;
import jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgFrame;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
import jmri.jmrit.symbolicprog.tabbedframe.PaneServiceProgFrame;
import jmri.jmrit.throttle.LargePowerManagerButton;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.jmrix.ActiveSystemsMenu;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.ConnectionStatus;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.swing.JTablePersistenceManager;
import jmri.swing.RowSorterUtil;
import jmri.util.FileUtil;
import jmri.util.HelpUtil;
import jmri.util.WindowMenu;
import jmri.util.datatransfer.RosterEntrySelection;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.ResizableImagePanel;
import jmri.util.swing.WindowInterface;
import jmri.util.swing.multipane.TwoPaneTBWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A window for Roster management.
 * <p>
 * TODO: Several methods are copied from PaneProgFrame and should be refactored
 * No programmer support yet (dummy object below). Color only covering borders.
 * No reset toolbar support yet. No glass pane support (See DecoderPro3Panes
 * class and usage below). Special panes (Roster entry, attributes, graphics)
 * not included. How do you pick a programmer file? (hardcoded) Initialization
 * needs partial deferal, too for 1st pane to appear.
 *
 * @see jmri.jmrit.symbolicprog.tabbedframe.PaneSet
 *
 * @author Bob Jacobsen Copyright (C) 2010, 2016
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Randall Wood Copyright (C) 2012
 */
public class RosterFrame extends TwoPaneTBWindow implements RosterEntrySelector, RosterGroupSelector {

    static ArrayList<RosterFrame> frameInstances = new ArrayList<>();
    protected boolean allowQuit = true;
    protected String baseTitle = "Roster";
    protected JmriAbstractAction newWindowAction;

    public RosterFrame() {
        this("Roster");
    }

    public RosterFrame(String name) {
        this(name,
                "xml/config/parts/jmri/jmrit/roster/swing/RosterFrameMenu.xml",
                "xml/config/parts/jmri/jmrit/roster/swing/RosterFrameToolBar.xml");
    }

    public RosterFrame(String name, String menubarFile, String toolbarFile) {
        super(name, menubarFile, toolbarFile);
        this.allowInFrameServlet = false;
        this.setBaseTitle(name);
        this.buildWindow();
    }

    int clickDelay = 0;
    JRadioButtonMenuItem contextEdit = new JRadioButtonMenuItem(Bundle.getMessage("ButtonEdit"));
    JRadioButtonMenuItem contextOps = new JRadioButtonMenuItem(Bundle.getMessage("ProgrammingOnMain"));
    JRadioButtonMenuItem contextService = new JRadioButtonMenuItem(Bundle.getMessage("ProgrammingTrack"));
    JTextPane dateUpdated = new JTextPane();
    JTextPane dccAddress = new JTextPane();
    JTextPane decoderFamily = new JTextPane();
    JTextPane decoderModel = new JTextPane();
    JRadioButton edit = new JRadioButton(Bundle.getMessage("EditOnly"));
    JTextPane filename = new JTextPane();
    JLabel firstHelpLabel;
    //int firstTimeAddedEntry = 0x00;
    int groupSplitPaneLocation = 0;
    RosterGroupsPanel groups;
    boolean hideGroups = false;
    boolean hideRosterImage = false;
    JTextPane id = new JTextPane();
    boolean inStartProgrammer = false;
    ResizableImagePanel locoImage;
    JTextPane maxSpeed = new JTextPane();
    JTextPane mfg = new JTextPane();
    ProgModeSelector modePanel = new ProgServiceModeComboBox();
    JTextPane model = new JTextPane();
    JLabel operationsModeProgrammerLabel = new JLabel();
    JRadioButton ops = new JRadioButton(Bundle.getMessage("ProgrammingOnMain"));
    ConnectionConfig opsModeProCon = null;
    JTextPane owner = new JTextPane();
    UserPreferencesManager prefsMgr;
    JButton prog1Button = new JButton(Bundle.getMessage("Program"));
    JButton prog2Button = new JButton(Bundle.getMessage("BasicProgrammer"));
    ActionListener programModeListener;

    // These are the names of the programmer _files_, not what should be displayed to the user
    String programmer1 = "Comprehensive"; // NOI18N
    String programmer2 = "Basic"; // NOI18N

    java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("apps.AppsBundle");
    //current selected loco
    transient RosterEntry re;
    JTextPane roadName = new JTextPane();
    JTextPane roadNumber = new JTextPane();
    JPanel rosterDetailPanel = new JPanel();
    PropertyChangeListener rosterEntryUpdateListener;
    JSplitPane rosterGroupSplitPane;
    JButton rosterMedia = new JButton(Bundle.getMessage("LabelsAndMedia"));
    RosterTable rtable;
    ConnectionConfig serModeProCon = null;
    JRadioButton service = new JRadioButton(Bundle.getMessage("ProgrammingTrack"));
    JLabel serviceModeProgrammerLabel = new JLabel();
    JLabel statusField = new JLabel();
    Dimension summaryPaneDim = new Dimension(0, 170);
    JButton throttleLabels = new JButton(Bundle.getMessage("ThrottleLabels"));
    JButton throttleLaunch = new JButton(Bundle.getMessage("Throttle"));

    void additionsToToolBar() {
        //This value may return null if the DP3 window has been called from a the traditional JMRI menu frame
        if (Apps3.buttonSpace() != null) {
            getToolBar().add(Apps3.buttonSpace());
        }
        getToolBar().add(new LargePowerManagerButton(true));
        getToolBar().add(modePanel);
    }

    /**
     * For use when the DP3 window is called from another JMRI instance, set
     * this to prevent the DP3 from shutting down JMRI when the window is
     * closed.
     *
     * @param quitAllowed true if closing window should quit application; false
     *                    otherwise
     */
    protected void allowQuit(boolean quitAllowed) {
        if (allowQuit != quitAllowed) {
            newWindowAction = null;
            allowQuit = quitAllowed;
            groups.setNewWindowMenuAction(this.getNewWindowAction());
        }

        firePropertyChange("quit", "setEnabled", allowQuit);
        //if we are not allowing quit, ie opened from JMRI classic
        //then we must at least allow the window to be closed
        if (!allowQuit) {
            firePropertyChange("closewindow", "setEnabled", true);
        }
    }

    JPanel bottomRight() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        ButtonGroup progMode = new ButtonGroup();
        progMode.add(service);
        progMode.add(ops);
        progMode.add(edit);
        service.setEnabled(false);
        ops.setEnabled(false);
        edit.setEnabled(true);
        firePropertyChange("setprogservice", "setEnabled", false);
        firePropertyChange("setprogops", "setEnabled", false);
        firePropertyChange("setprogedit", "setEnabled", true);
        ops.setOpaque(false);
        service.setOpaque(false);
        edit.setOpaque(false);
        JPanel progModePanel = new JPanel();
        GridLayout buttonLayout = new GridLayout(3, 1, 0, 0);
        progModePanel.setLayout(buttonLayout);
        progModePanel.add(service);
        progModePanel.add(ops);
        progModePanel.add(edit);
        programModeListener = (ActionEvent e) -> {
            updateProgMode();
        };
        service.addActionListener(programModeListener);
        ops.addActionListener(programModeListener);
        edit.addActionListener(programModeListener);
        service.setVisible(false);
        ops.setVisible(false);
        panel.add(progModePanel);
        JPanel buttonHolder = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.gridx = 0;
        c.ipady = 20;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridy = 0;
        c.insets = new Insets(2, 2, 2, 2);
        buttonHolder.add(prog1Button, c);
        c.weightx = 1;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.ipady = 0;
        buttonHolder.add(rosterMedia, c);
        c.weightx = 1.0;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.ipady = 0;
        buttonHolder.add(throttleLaunch, c);
        //buttonHolder.add(throttleLaunch);
        panel.add(buttonHolder);
        prog1Button.setEnabled(false);
        prog1Button.addActionListener((ActionEvent e) -> {
            log.debug("Open programmer pressed");
            startProgrammer(null, re, programmer1);
        });

        rosterMedia.setEnabled(false);
        rosterMedia.addActionListener((ActionEvent e) -> {
            log.debug("Open programmer pressed");
            edit.setSelected(true);
            startProgrammer(null, re, "dp3" + File.separator + "MediaPane");
        });
        throttleLaunch.setEnabled(false);
        throttleLaunch.addActionListener((ActionEvent e) -> {
            log.debug("Launch Throttle pressed");
            if (!checkIfEntrySelected()) {
                return;
            }
            ThrottleFrame tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame();
            tf.toFront();
            tf.getAddressPanel().setRosterEntry(re);
        });
        return panel;
    }

    protected final void buildWindow() {
        //Additions to the toolbar need to be added first otherwise when trying to hide bits up during the initialisation they remain on screen
        additionsToToolBar();
        frameInstances.add(this);
        prefsMgr = InstanceManager.getDefault(UserPreferencesManager.class);
        getTop().add(createTop());
        getBottom().setMinimumSize(summaryPaneDim);
        getBottom().add(createBottom());
        statusBar();
        systemsMenu();
        helpMenu(getMenu(), this);
        if ((!prefsMgr.getSimplePreferenceState(this.getClass().getName() + ".hideGroups")) && !Roster.getDefault().getRosterGroupList().isEmpty()) {
            hideGroupsPane(false);
        } else {
            hideGroupsPane(true);
        }
        if (prefsMgr.getSimplePreferenceState(this.getClass().getName() + ".hideSummary")) {
            //We have to set it to display first, then we can hide it.
            hideBottomPane(false);
            hideBottomPane(true);
        }
        PropertyChangeListener propertyChangeListener = (PropertyChangeEvent changeEvent) -> {
            JSplitPane sourceSplitPane = (JSplitPane) changeEvent.getSource();
            String propertyName = changeEvent.getPropertyName();
            if (propertyName.equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY)) {
                int current = sourceSplitPane.getDividerLocation() + sourceSplitPane.getDividerSize();
                int panesize = (int) (sourceSplitPane.getSize().getHeight());
                hideBottomPane = panesize - current <= 1;
                //p.setSimplePreferenceState(DecoderPro3Window.class.getName()+".hideSummary",hideSummary);
            }
        };
        updateProgrammerStatus(null);
        ConnectionStatus.instance().addPropertyChangeListener((PropertyChangeEvent e) -> {
            if ((e.getPropertyName().equals("change")) || (e.getPropertyName().equals("add"))) {
                log.debug("Received property {} with value {} ", e.getPropertyName(), e.getNewValue());
                updateProgrammerStatus(e);
            }
        });
        InstanceManager.addPropertyChangeListener(InstanceManager.getListPropertyName(AddressedProgrammerManager.class),
                (PropertyChangeEvent evt) -> {
                    log.debug("Received property {} with value {} ", evt.getPropertyName(), evt.getNewValue());
                    updateProgrammerStatus(evt);
                });
        InstanceManager.addPropertyChangeListener(InstanceManager.getListPropertyName(GlobalProgrammerManager.class),
                (PropertyChangeEvent evt) -> {
                    log.debug("Received property {} with value {} ", evt.getPropertyName(), evt.getNewValue());
                    updateProgrammerStatus(evt);
                });
        getSplitPane().addPropertyChangeListener(propertyChangeListener);
        if (this.getProgrammerConfigManager().getDefaultFile() != null) {
            programmer1 = this.getProgrammerConfigManager().getDefaultFile();
        }
        this.getProgrammerConfigManager().addPropertyChangeListener(ProgrammerConfigManager.DEFAULT_FILE, (PropertyChangeEvent evt) -> {
            if (this.getProgrammerConfigManager().getDefaultFile() != null) {
                programmer1 = this.getProgrammerConfigManager().getDefaultFile();
            }
        });

        String lastProg = (String) prefsMgr.getProperty(getWindowFrameRef(), "selectedProgrammer");
        if (lastProg != null) {
            if (lastProg.equals("service") && service.isEnabled()) {
                service.setSelected(true);
                updateProgMode();
            } else if (lastProg.equals("ops") && ops.isEnabled()) {
                ops.setSelected(true);
                updateProgMode();
            } else if (lastProg.equals("edit") && edit.isEnabled()) {
                edit.setSelected(true);
                updateProgMode();
            }
        }
        if (frameInstances.size() > 1) {
            firePropertyChange("closewindow", "setEnabled", true);
            allowQuit(frameInstances.get(0).isAllowQuit());
        } else {
            firePropertyChange("closewindow", "setEnabled", false);
        }
    }

    boolean checkIfEntrySelected() {
        return this.checkIfEntrySelected(false);
    }

    boolean checkIfEntrySelected(boolean allowMultiple) {
        if ((re == null && !allowMultiple) || (this.getSelectedRosterEntries().length < 1)) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorNoSelection"));
            return false;
        }
        return true;
    }

    //@TODO The disabling of the closewindow menu item doesn't quite work as this in only invoked on the closing window, and not the one that is left
    void closeWindow(WindowEvent e) {
        saveWindowDetails();
        //Save any changes made in the roster entry details
        Roster.getDefault().writeRoster();
        if (allowQuit && frameInstances.size() == 1 && !InstanceManager.getDefault(ShutDownManager.class).isShuttingDown()) {
            handleQuit(e);
        } else {
            //As we are not the last window open or we are not allowed to quit the application then we will just close the current window
            frameInstances.remove(this);
            super.windowClosing(e);
            if ((frameInstances.size() == 1) && (allowQuit)) {
                frameInstances.get(0).firePropertyChange("closewindow", "setEnabled", false);
            }
            dispose();
        }
    }

    protected void copyLoco() {
        CopyRosterItem act = new CopyRosterItem("Copy", this, re);
        act.actionPerformed(null);
    }

    JComponent createBottom() {
        locoImage = new ResizableImagePanel(null, 240, 160);
        locoImage.setBorder(BorderFactory.createLineBorder(Color.blue));
        locoImage.setOpaque(true);
        locoImage.setRespectAspectRatio(true);
        rosterDetailPanel.setLayout(new BorderLayout());
        rosterDetailPanel.add(locoImage, BorderLayout.WEST);
        rosterDetailPanel.add(rosterDetails(), BorderLayout.CENTER);
        rosterDetailPanel.add(bottomRight(), BorderLayout.EAST);
        if (prefsMgr.getSimplePreferenceState(this.getClass().getName() + ".hideRosterImage")) {
            locoImage.setVisible(false);
            hideRosterImage = true;
        }
        rosterEntryUpdateListener = (PropertyChangeEvent e) -> {
            updateDetails();
        };
        return rosterDetailPanel;
    }

    JComponent createTop() {
        Object selectedRosterGroup = prefsMgr.getProperty(getWindowFrameRef(), SELECTED_ROSTER_GROUP);
        groups = new RosterGroupsPanel((selectedRosterGroup != null) ? selectedRosterGroup.toString() : null);
        groups.setNewWindowMenuAction(this.getNewWindowAction());
        setTitle(groups.getSelectedRosterGroup());
        final JPanel rosters = new JPanel();
        rosters.setLayout(new BorderLayout());
        // set up roster table
        rtable = new RosterTable(true, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        rtable.setRosterGroup(this.getSelectedRosterGroup());
        rtable.setRosterGroupSource(groups);
        rosters.add(rtable, BorderLayout.CENTER);
        // add selection listener
        rtable.getTable().getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            JTable table = rtable.getTable();
            if (!e.getValueIsAdjusting()) {
                if (rtable.getSelectedRosterEntries().length == 1 && table.getSelectedRow() >= 0) {
                    log.debug("Selected row {}", table.getSelectedRow());
                    locoSelected(rtable.getModel().getValueAt(table.getRowSorter().convertRowIndexToModel(table.getSelectedRow()), RosterTableModel.IDCOL).toString());
                } else if (rtable.getSelectedRosterEntries().length > 1 || table.getSelectedRow() < 0) {
                    locoSelected(null);
                } // leave last selected item visible if no selection
            }
        });

        //Set all the sort and width details of the table first.
        String rostertableref = getWindowFrameRef() + ":roster";
        rtable.getTable().setName(rostertableref);

        // Allow only one column to be sorted at a time -
        // Java allows multiple column sorting, but to effectly persist that, we
        // need to be intelligent about which columns can be meaningfully sorted
        // with other columns; this bypasses the problem by only allowing the
        // last column sorted to affect sorting
        RowSorterUtil.addSingleSortableColumnListener(rtable.getTable().getRowSorter());

        // Reset and then persist the table's ui state
        JTablePersistenceManager tpm = InstanceManager.getNullableDefault(JTablePersistenceManager.class);
        if (tpm != null) {
            tpm.resetState(rtable.getTable());
            tpm.persist(rtable.getTable());
        }
        rtable.getTable().setDragEnabled(true);
        rtable.getTable().setTransferHandler(new TransferHandler() {

            @Override
            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY;
            }

            @Override
            public Transferable createTransferable(JComponent c) {
                JTable table = rtable.getTable();
                ArrayList<String> Ids = new ArrayList<>(table.getSelectedRowCount());
                for (int i = 0; i < table.getSelectedRowCount(); i++) {
                    Ids.add(rtable.getModel().getValueAt(table.getRowSorter().convertRowIndexToModel(table.getSelectedRows()[i]), RosterTableModel.IDCOL).toString());
                }
                return new RosterEntrySelection(Ids);
            }

            @Override
            public void exportDone(JComponent c, Transferable t, int action) {
                // nothing to do
            }
        });
        MouseListener rosterMouseListener = new RosterPopupListener();
        rtable.getTable().addMouseListener(rosterMouseListener);
        try {
            clickDelay = ((Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval"));
        } catch (RuntimeException e) {
            clickDelay = 500;
            log.debug("Unable to get the double click speed, Using JMRI default of half a second {}", e.getMessage());
        }

        // assemble roster/groups splitpane
        rosterGroupSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, groups, rosters);
        rosterGroupSplitPane.setOneTouchExpandable(true);
        rosterGroupSplitPane.setResizeWeight(0); // emphasis rosters
        Object w = prefsMgr.getProperty(getWindowFrameRef(), "rosterGroupPaneDividerLocation");
        if (w != null) {
            groupSplitPaneLocation = (Integer) w;
            rosterGroupSplitPane.setDividerLocation(groupSplitPaneLocation);
        }
        if (!Roster.getDefault().getRosterGroupList().isEmpty()) {
            if (prefsMgr.getSimplePreferenceState(this.getClass().getName() + ".hideGroups")) {
                hideGroupsPane(true);
            }
        } else {
            enableRosterGroupMenuItems(false);
        }
        PropertyChangeListener propertyChangeListener = (PropertyChangeEvent changeEvent) -> {
            JSplitPane sourceSplitPane = (JSplitPane) changeEvent.getSource();
            String propertyName = changeEvent.getPropertyName();
            if (propertyName.equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY)) {
                int current = sourceSplitPane.getDividerLocation();
                hideGroups = current <= 1;
                Integer last = (Integer) changeEvent.getNewValue();
                if (current >= 2) {
                    groupSplitPaneLocation = current;
                } else if (last >= 2) {
                    groupSplitPaneLocation = last;
                }
            }
        };
        groups.addPropertyChangeListener(SELECTED_ROSTER_GROUP, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                prefsMgr.setProperty(this.getClass().getName(), SELECTED_ROSTER_GROUP, pce.getNewValue());
                setTitle((String) pce.getNewValue());
            }
        });
        rosterGroupSplitPane.addPropertyChangeListener(propertyChangeListener);
        Roster.getDefault().addPropertyChangeListener((PropertyChangeEvent e) -> {
            if (e.getPropertyName().equals("RosterGroupAdded") && Roster.getDefault().getRosterGroupList().size() == 1) {
                // if the pane is hidden, show it when 1st group is created
                hideGroupsPane(false);
                enableRosterGroupMenuItems(true);
            } else if (!rtable.isVisible() && (e.getPropertyName().equals("saved"))) {
                if (firstHelpLabel != null) {
                    firstHelpLabel.setVisible(false);
                }
                rtable.setVisible(true);
                rtable.resetColumnWidths();
            }
        });
        if (Roster.getDefault().numEntries() == 0) {
            try {
                BufferedImage myPicture = ImageIO.read(FileUtil.findURL(("resources/" + Bundle.getMessage("ThrottleFirstUseImage")), FileUtil.Location.INSTALLED));
                //rosters.add(new JLabel(new ImageIcon( myPicture )), BorderLayout.CENTER);
                firstHelpLabel = new JLabel(new ImageIcon(myPicture));
                rtable.setVisible(false);
                rosters.add(firstHelpLabel, BorderLayout.NORTH);
                //tableArea.add(firstHelpLabel);
                rtable.setVisible(false);
            } catch (IOException ex) {
                // handle exception...
            }
        }
        return rosterGroupSplitPane;
    }

    protected void deleteLoco() {
        DeleteRosterItemAction act = new DeleteRosterItemAction("Delete", (WindowInterface) this);
        act.actionPerformed(null);
    }

    void editMediaButton() {
        //Because of the way that programmers work, we need to use edit mode for displaying the media pane, so that the read/write buttons do not appear.
        boolean serviceSelected = service.isSelected();
        boolean opsSelected = ops.isSelected();
        edit.setSelected(true);
        startProgrammer(null, re, "dp3" + File.separator + "MediaPane");
        service.setSelected(serviceSelected);
        ops.setSelected(opsSelected);
    }

    protected void enableRosterGroupMenuItems(boolean enable) {
        firePropertyChange("groupspane", "setEnabled", enable);
        firePropertyChange("grouptable", "setEnabled", enable);
        firePropertyChange("deletegroup", "setEnabled", enable);
    }

    protected void exportLoco() {
        ExportRosterItem act = new ExportRosterItem(Bundle.getMessage("Export"), this, re);
        act.actionPerformed(null);
    }

    void formatTextAreaAsLabel(JTextPane pane) {
        pane.setOpaque(false);
        pane.setEditable(false);
        pane.setBorder(null);
    }

    /*=============== Getters and Setters for core properties ===============*/

    /**
     * @return Will closing the window quit JMRI?
     */
    public boolean isAllowQuit() {
        return allowQuit;
    }

    /**
     * @param allowQuit Set state to either close JMRI or just the roster window
     */
    public void setAllowQuit(boolean allowQuit) {
        allowQuit(allowQuit);
    }

    /**
     * @return the baseTitle
     */
    protected String getBaseTitle() {
        return baseTitle;
    }

    /**
     * @param baseTitle the baseTitle to set
     */
    protected final void setBaseTitle(String baseTitle) {
        String title = null;
        if (this.baseTitle == null) {
            title = this.getTitle();
        }
        this.baseTitle = baseTitle;
        if (title != null) {
            this.setTitle(title);
        }
    }

    /**
     * @return the newWindowAction
     */
    protected JmriAbstractAction getNewWindowAction() {
        if (newWindowAction == null) {
            newWindowAction = new RosterFrameAction("newWindow", this, allowQuit);
        }
        return newWindowAction;
    }

    /**
     * @param newWindowAction the newWindowAction to set
     */
    protected void setNewWindowAction(JmriAbstractAction newWindowAction) {
        this.newWindowAction = newWindowAction;
        this.groups.setNewWindowMenuAction(newWindowAction);
    }

    @Override
    public void setTitle(String title) {
        if (title == null || title.isEmpty()) {
            title = Roster.ALLENTRIES;
        }
        if (this.baseTitle != null) {
            if (!title.equals(this.baseTitle) && !title.startsWith(this.baseTitle)) {
                super.setTitle(this.baseTitle + ": " + title);
            }
        } else {
            super.setTitle(title);
        }
    }

    @Override
    public Object getProperty(String key) {
        if (key.equalsIgnoreCase(SELECTED_ROSTER_GROUP)) {
            return getSelectedRosterGroup();
        } else if (key.equalsIgnoreCase("hideSummary")) {
            return hideBottomPane;
        }
        // call parent getProperty method to return any properties defined
        // in the class heirarchy.
        return super.getProperty(key);
    }

    public Object getRemoteObject(String value) {
        return getProperty(value);
    }

    @Override
    public RosterEntry[] getSelectedRosterEntries() {
        RosterEntry[] entries = rtable.getSelectedRosterEntries();
        return Arrays.copyOf(entries, entries.length);
    }

    @Override
    public String getSelectedRosterGroup() {
        return groups.getSelectedRosterGroup();
    }

    protected ProgrammerConfigManager getProgrammerConfigManager() {
        return InstanceManager.getDefault(ProgrammerConfigManager.class);
    }

    void handleQuit(WindowEvent e) {
        if (e != null && frameInstances.size() == 1) {
            final String rememberWindowClose = this.getClass().getName() + ".closeDP3prompt";
            if (!prefsMgr.getSimplePreferenceState(rememberWindowClose)) {
                JPanel message = new JPanel();
                JLabel question = new JLabel(rb.getString("MessageLongCloseWarning"));
                final JCheckBox remember = new JCheckBox(rb.getString("MessageRememberSetting"));
                remember.setFont(remember.getFont().deriveFont(10.0F));
                message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
                message.add(question);
                message.add(remember);
                int result = JOptionPane.showConfirmDialog(null,
                        message,
                        rb.getString("MessageShortCloseWarning"),
                        JOptionPane.YES_NO_OPTION);
                if (remember.isSelected()) {
                    prefsMgr.setSimplePreferenceState(rememberWindowClose, true);
                }
                if (result == JOptionPane.YES_OPTION) {
                    AppsBase.handleQuit();
                }
            } else {
                AppsBase.handleQuit();
            }
        } else if (frameInstances.size() > 1) {
            final String rememberWindowClose = this.getClass().getName() + ".closeMultipleDP3prompt";
            if (!prefsMgr.getSimplePreferenceState(rememberWindowClose)) {
                JPanel message = new JPanel();
                JLabel question = new JLabel(rb.getString("MessageLongMultipleCloseWarning"));
                final JCheckBox remember = new JCheckBox(rb.getString("MessageRememberSetting"));
                remember.setFont(remember.getFont().deriveFont(10.0F));
                message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
                message.add(question);
                message.add(remember);
                int result = JOptionPane.showConfirmDialog(null,
                        message,
                        rb.getString("MessageShortCloseWarning"),
                        JOptionPane.YES_NO_OPTION);
                if (remember.isSelected()) {
                    prefsMgr.setSimplePreferenceState(rememberWindowClose, true);
                }
                if (result == JOptionPane.YES_OPTION) {
                    AppsBase.handleQuit();
                }
            } else {
                AppsBase.handleQuit();
            }
            //closeWindow(null);
        }
    }

    protected void helpMenu(JMenuBar menuBar, final JFrame frame) {
        // create menu and standard items
        JMenu helpMenu = HelpUtil.makeHelpMenu("package.apps.gui3.dp3.DecoderPro3", true);
        // tell help to use default browser for external types
        SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");
        // use as main help menu
        menuBar.add(helpMenu);
    }

    protected void hideGroups() {
        boolean boo = !hideGroups;
        hideGroupsPane(boo);
    }

    public void hideGroupsPane(boolean hide) {
        if (hideGroups == hide) {
            return;
        }
        hideGroups = hide;
        if (hide) {
            groupSplitPaneLocation = rosterGroupSplitPane.getDividerLocation();
            rosterGroupSplitPane.setDividerLocation(1);
            rosterGroupSplitPane.getLeftComponent().setMinimumSize(new Dimension());
            if (Roster.getDefault().getRosterGroupList().isEmpty()) {
                rosterGroupSplitPane.setOneTouchExpandable(false);
                rosterGroupSplitPane.setDividerSize(0);
            }
        } else {
            rosterGroupSplitPane.setDividerSize(UIManager.getInt("SplitPane.dividerSize"));
            rosterGroupSplitPane.setOneTouchExpandable(true);
            if (groupSplitPaneLocation >= 2) {
                rosterGroupSplitPane.setDividerLocation(groupSplitPaneLocation);
            } else {
                rosterGroupSplitPane.resetToPreferredSizes();
            }
        }
    }

    protected void hideRosterImage() {
        hideRosterImage = !hideRosterImage;
        //p.setSimplePreferenceState(DecoderPro3Window.class.getName()+".hideRosterImage",hideRosterImage);
        if (hideRosterImage) {
            locoImage.setVisible(false);
        } else {
            locoImage.setVisible(true);
        }
    }

    protected void hideSummary() {
        boolean boo = !hideBottomPane;
        hideBottomPane(boo);
    }

    /**
     * An entry has been selected in the Roster Table, activate the bottom part
     * of the window.
     *
     * @param id ID of the selected roster entry
     */
    void locoSelected(String id) {
        if (id != null) {
            log.debug("locoSelected ID {}", id);
            if (re != null) {
                //We remove the propertychangelistener if we had a previoulsy selected entry;
                re.removePropertyChangeListener(rosterEntryUpdateListener);
            }
            // convert to roster entry
            re = Roster.getDefault().entryFromTitle(id);
            re.addPropertyChangeListener(rosterEntryUpdateListener);
        } else {
            log.debug("Multiple selection");
            re = null;
        }
        updateDetails();
    }

    protected void newWindow() {
        this.newWindow(this.getNewWindowAction());
    }

    protected void newWindow(JmriAbstractAction action) {
        action.setWindowInterface(this);
        action.actionPerformed(null);
        firePropertyChange("closewindow", "setEnabled", true);
    }

    /**
     * Prepare a roster entry to be printed, and display a selection list.
     *
     * @see jmri.jmrit.roster.PrintRosterEntry#printPanes(boolean)
     * @param preview true if output should got to a Preview pane on screen, false
     *            to output to a printer (dialog)
     */
    protected void printLoco(boolean preview) {
        log.debug("Selected entry: {}", re.getDisplayName());
        PrintRosterEntry pre = new PrintRosterEntry(re, this, "programmers" + File.separator + programmer2 + ".xml");
        // uses Basic programmer (programmer2) when printing a selected entry from (this) top Roster frame
        // compare with: jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame#printPanes(boolean)
        pre.printPanes(preview);
    }

    /**
     * Match the first argument in the array against a locally-known method.
     *
     * @param args Array of arguments, we take with element 0
     */
    @Override
    public void remoteCalls(String[] args) {
        args[0] = args[0].toLowerCase();
        switch (args[0]) {
            case "identifyloco":
                startIdentifyLoco();
                break;
            case "printloco":
                if (checkIfEntrySelected()) {
                    printLoco(false);
                }
                break;
            case "printpreviewloco":
                if (checkIfEntrySelected()) {
                    printLoco(true);
                }
                break;
            case "exportloco":
                if (checkIfEntrySelected()) {
                    exportLoco();
                }
                break;
            case "basicprogrammer":
                if (checkIfEntrySelected()) {
                    startProgrammer(null, re, programmer2);
                }
                break;
            case "comprehensiveprogrammer":
                if (checkIfEntrySelected()) {
                    startProgrammer(null, re, programmer1);
                }
                break;
            case "editthrottlelabels":
                if (checkIfEntrySelected()) {
                    startProgrammer(null, re, "dp3" + File.separator + "ThrottleLabels");
                }
                break;
            case "editrostermedia":
                if (checkIfEntrySelected()) {
                    startProgrammer(null, re, "dp3" + File.separator + "MediaPane");
                }
                break;
            case "hiderosterimage":
                hideRosterImage();
                break;
            case "summarypane":
                hideSummary();
                break;
            case "copyloco":
                if (checkIfEntrySelected()) {
                    copyLoco();
                }
                break;
            case "deleteloco":
                if (checkIfEntrySelected(true)) {
                    deleteLoco();
                }
                break;
            case "setprogservice":
                service.setSelected(true);
                break;
            case "setprogops":
                ops.setSelected(true);
                break;
            case "setprogedit":
                edit.setSelected(true);
                break;
            case "groupspane":
                hideGroups();
                break;
            case "quit":
                saveWindowDetails();
                handleQuit(new WindowEvent(this, frameInstances.size()));
                break;
            case "closewindow":
                closeWindow(null);
                break;
            case "newwindow":
                newWindow();
                break;
            case "resettablecolumns":
                rtable.resetColumnWidths();
                break;
            default:
                log.error("method {} not found", args[0]);
                break;
        }
    }

    JPanel rosterDetails() {
        JPanel panel = new JPanel();
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        GridBagConstraints cR = new GridBagConstraints();
        Dimension minFieldDim = new Dimension(30, 20);
        cL.gridx = 0;
        cL.gridy = 0;
        cL.ipadx = 3;
        cL.anchor = GridBagConstraints.EAST;
        cL.insets = new Insets(0, 0, 0, 15);
        JLabel row0Label = new JLabel(Bundle.getMessage("FieldID") + ":", JLabel.LEFT);
        gbLayout.setConstraints(row0Label, cL);
        panel.setLayout(gbLayout);
        panel.add(row0Label);
        cR.gridx = 1;
        cR.gridy = 0;
        cR.anchor = GridBagConstraints.WEST;
        id.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(id, cR);
        formatTextAreaAsLabel(id);
        panel.add(id);
        cL.gridy = 1;
        JLabel row1Label = new JLabel(Bundle.getMessage("FieldRoadName") + ":", JLabel.LEFT);
        gbLayout.setConstraints(row1Label, cL);
        panel.add(row1Label);
        cR.gridy = 1;
        roadName.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadName, cR);
        formatTextAreaAsLabel(roadName);
        panel.add(roadName);
        cL.gridy = 2;
        JLabel row2Label = new JLabel(Bundle.getMessage("FieldRoadNumber") + ":");
        gbLayout.setConstraints(row2Label, cL);
        panel.add(row2Label);
        cR.gridy = 2;
        roadNumber.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadNumber, cR);
        formatTextAreaAsLabel(roadNumber);
        panel.add(roadNumber);
        cL.gridy = 3;
        JLabel row3Label = new JLabel(Bundle.getMessage("FieldManufacturer") + ":");
        gbLayout.setConstraints(row3Label, cL);
        panel.add(row3Label);
        cR.gridy = 3;
        mfg.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(mfg, cR);
        formatTextAreaAsLabel(mfg);
        panel.add(mfg);
        cL.gridy = 4;
        JLabel row4Label = new JLabel(Bundle.getMessage("FieldOwner") + ":");
        gbLayout.setConstraints(row4Label, cL);
        panel.add(row4Label);
        cR.gridy = 4;
        owner.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(owner, cR);
        formatTextAreaAsLabel(owner);
        panel.add(owner);
        cL.gridy = 5;
        JLabel row5Label = new JLabel(Bundle.getMessage("FieldModel") + ":");
        gbLayout.setConstraints(row5Label, cL);
        panel.add(row5Label);
        cR.gridy = 5;
        model.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(model, cR);
        formatTextAreaAsLabel(model);
        panel.add(model);
        cL.gridy = 6;
        JLabel row6Label = new JLabel(Bundle.getMessage("FieldDCCAddress") + ":");
        gbLayout.setConstraints(row6Label, cL);
        panel.add(row6Label);
        cR.gridy = 6;
        dccAddress.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(dccAddress, cR);
        formatTextAreaAsLabel(dccAddress);
        panel.add(dccAddress);
        cL.gridy = 7;
        cR.gridy = 7;
        cL.gridy = 8;
        cR.gridy = 8;
        cL.gridy = 9;
        JLabel row9Label = new JLabel(Bundle.getMessage("FieldDecoderFamily") + ":");
        gbLayout.setConstraints(row9Label, cL);
        panel.add(row9Label);
        cR.gridy = 9;
        decoderFamily.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderFamily, cR);
        formatTextAreaAsLabel(decoderFamily);
        panel.add(decoderFamily);
        cL.gridy = 10;
        JLabel row10Label = new JLabel(Bundle.getMessage("FieldDecoderModel") + ":");
        gbLayout.setConstraints(row10Label, cL);
        panel.add(row10Label);
        cR.gridy = 10;
        decoderModel.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderModel, cR);
        formatTextAreaAsLabel(decoderModel);
        panel.add(decoderModel);
        cL.gridy = 11;
        cR.gridy = 11;
        cL.gridy = 12;
        JLabel row12Label = new JLabel(Bundle.getMessage("FieldFilename") + ":");
        gbLayout.setConstraints(row12Label, cL);
        panel.add(row12Label);
        cR.gridy = 12;
        filename.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(filename, cR);
        formatTextAreaAsLabel(filename);
        panel.add(filename);
        cL.gridy = 13;
        /*
         * JLabel row13Label = new
         * JLabel(Bundle.getMessage("FieldDateUpdated")+":");
         * gbLayout.setConstraints(row13Label,cL); panel.add(row13Label);
         */
        cR.gridy = 13;
        /*
         * filename.setMinimumSize(minFieldDim);
         * gbLayout.setConstraints(dateUpdated,cR); panel.add(dateUpdated);
         */
        formatTextAreaAsLabel(dateUpdated);
        JPanel retval = new JPanel(new FlowLayout(FlowLayout.LEFT));
        retval.add(panel);
        return retval;
    }

    void saveWindowDetails() {
        prefsMgr.setSimplePreferenceState(this.getClass().getName() + ".hideSummary", hideBottomPane);
        prefsMgr.setSimplePreferenceState(this.getClass().getName() + ".hideGroups", hideGroups);
        prefsMgr.setSimplePreferenceState(this.getClass().getName() + ".hideRosterImage", hideRosterImage);
        prefsMgr.setProperty(getWindowFrameRef(), SELECTED_ROSTER_GROUP, groups.getSelectedRosterGroup());
        String selectedProgMode = "edit";
        if (service.isSelected()) {
            selectedProgMode = "service";
        }
        if (ops.isSelected()) {
            selectedProgMode = "ops";
        }
        prefsMgr.setProperty(getWindowFrameRef(), "selectedProgrammer", selectedProgMode);

        if (rosterGroupSplitPane.getDividerLocation() > 2) {
            prefsMgr.setProperty(getWindowFrameRef(), "rosterGroupPaneDividerLocation", rosterGroupSplitPane.getDividerLocation());
        } else if (groupSplitPaneLocation > 2) {
            prefsMgr.setProperty(getWindowFrameRef(), "rosterGroupPaneDividerLocation", groupSplitPaneLocation);
        }
    }

    /**
     * Identify locomotive complete, act on it by setting the GUI. This will
     * fire "GUI changed" events which will reset the decoder GUI.
     *
     * @param dccAddress address of locomotive
     * @param isLong     true if address is long; false if short
     * @param mfgId      manufacturer id as in decoder
     * @param modelId    model id as in decoder
     */
    protected void selectLoco(int dccAddress, boolean isLong, int mfgId, int modelId) {
        // raise the button again
        // idloco.setSelected(false);
        // locate that loco
        inStartProgrammer = false;
        if (re != null) {
            //We remove the propertychangelistener if we had a previoulsy selected entry;
            re.removePropertyChangeListener(rosterEntryUpdateListener);
        }
        List<RosterEntry> l = Roster.getDefault().matchingList(null, null, Integer.toString(dccAddress), null, null, null, null);
        log.debug("selectLoco found {} matches", l.size());
        if (l.size() > 0) {
            if (l.size() > 1) {
                //More than one possible loco, so check long flag
                List<RosterEntry> l2 = new ArrayList<>();
                for (RosterEntry _re : l) {
                    if (_re.isLongAddress() == isLong) {
                        l2.add(_re);
                    }
                }
                if (l2.size() == 1) {
                    re = l2.get(0);
                } else {
                    if (l2.isEmpty()) {
                        l2 = l;
                    }
                    // Still more than one possible loco, so check against the decoder family
                    log.trace("Checking against decoder family with mfg {} model {}", mfgId, modelId);
                    List<RosterEntry> l3 = new ArrayList<>();
                    List<DecoderFile> temp = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, null, "" + mfgId, "" + modelId, null, null);
                    log.trace("found {}", temp.size());
                    ArrayList<String> decoderFam = new ArrayList<>();
                    for (DecoderFile f : temp) {
                        if (!decoderFam.contains(f.getModel())) {
                            decoderFam.add(f.getModel());
                        }
                    }
                    log.trace("matched {} times", decoderFam.size());
                    
                    for (RosterEntry _re : l2) {
                        if (decoderFam.contains(_re.getDecoderModel())) {
                            l3.add(_re);
                        }
                    }
                    if (l3.isEmpty()) {
                        //Unable to determine the loco against the manufacture therefore will be unable to further identify against decoder.
                        re = l2.get(0);
                    } else {
                        //We have no other options to match against so will return the first one we come across;
                        re = l3.get(0);
                    }
                }
            } else {
                re = l.get(0);
            }
            re.addPropertyChangeListener(rosterEntryUpdateListener);
            rtable.setSelection(re);
            updateDetails();
            rtable.moveTableViewToSelected();
        } else {
            log.warn("Read address {}, but no such loco in roster", dccAddress); //"No roster entry found"
            JOptionPane.showMessageDialog(this, "No roster entry found", "Address " + dccAddress + " was read from the decoder\nbut has not been found in the Roster", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Simple method to change over the programmer buttons.
     * <p>
     * TODO This should be implemented with the buttons in their own class etc.
     * but this will work for now.
     *
     * @param buttonId   1 or 2; use 1 for basic programmer; 2 for comprehensive
     *                   programmer
     * @param programmer name of programmer
     * @param buttonText button title
     */
    public void setProgrammerLaunch(int buttonId, String programmer, String buttonText) {
        if (buttonId == 1) {
            programmer1 = programmer;
            prog1Button.setText(buttonText);
        } else if (buttonId == 2) {
            programmer2 = programmer;
            prog2Button.setText(buttonText);
        }
    }

    public void setSelectedRosterGroup(String rosterGroup) {
        groups.setSelectedRosterGroup(rosterGroup);
    }

    protected void showPopup(MouseEvent e) {
        int row = rtable.getTable().rowAtPoint(e.getPoint());
        if (!rtable.getTable().isRowSelected(row)) {
            rtable.getTable().changeSelection(row, 0, false, false);
        }
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(Bundle.getMessage("Program"));
        menuItem.addActionListener((ActionEvent e1) -> {
            startProgrammer(null, re, programmer1);
        });
        if (re == null) {
            menuItem.setEnabled(false);
        }
        popupMenu.add(menuItem);
        ButtonGroup group = new ButtonGroup();
        group.add(contextService);
        group.add(contextOps);
        group.add(contextEdit);
        JMenu progMenu = new JMenu(Bundle.getMessage("ProgrammerType"));
        contextService.addActionListener((ActionEvent e1) -> {
            service.setSelected(true);
            updateProgMode();
        });
        progMenu.add(contextService);
        contextOps.addActionListener((ActionEvent e1) -> {
            ops.setSelected(true);
            updateProgMode();
        });
        progMenu.add(contextOps);
        contextEdit.addActionListener((ActionEvent e1) -> {
            edit.setSelected(true);
            updateProgMode();
        });
        if (service.isSelected()) {
            contextService.setSelected(true);
        } else if (ops.isSelected()) {
            contextOps.setSelected(true);
        } else {
            contextEdit.setSelected(true);
        }
        progMenu.add(contextEdit);
        popupMenu.add(progMenu);
        popupMenu.addSeparator();
        menuItem = new JMenuItem(Bundle.getMessage("LabelsAndMedia"));
        menuItem.addActionListener((ActionEvent e1) -> {
            editMediaButton();
        });
        if (re == null) {
            menuItem.setEnabled(false);
        }
        popupMenu.add(menuItem);
        menuItem = new JMenuItem(Bundle.getMessage("Throttle"));
        menuItem.addActionListener((ActionEvent e1) -> {
            ThrottleFrame tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame();
            tf.toFront();
            tf.getAddressPanel().getRosterEntrySelector().setSelectedRosterGroup(getSelectedRosterGroup());
            tf.getAddressPanel().setRosterEntry(re);
        });
        if (re == null) {
            menuItem.setEnabled(false);
        }
        popupMenu.add(menuItem);
        popupMenu.addSeparator();

        menuItem = new JMenuItem(Bundle.getMessage("PrintSelection"));
        menuItem.addActionListener((ActionEvent e1) -> {
            printLoco(false);
        });
        if (re == null) {
            menuItem.setEnabled(false);
        }
        popupMenu.add(menuItem);
        menuItem = new JMenuItem(Bundle.getMessage("PreviewSelection"));
        menuItem.addActionListener((ActionEvent e1) -> {
            printLoco(true);
        });
        if (re == null) {
            menuItem.setEnabled(false);
        }
        popupMenu.add(menuItem);
        popupMenu.addSeparator();

        menuItem = new JMenuItem(Bundle.getMessage("Duplicateddd"));
        menuItem.addActionListener((ActionEvent e1) -> {
            copyLoco();
        });
        if (re == null) {
            menuItem.setEnabled(false);
        }
        popupMenu.add(menuItem);
        menuItem = new JMenuItem(this.getSelectedRosterGroup() != null ? Bundle.getMessage("DeleteFromGroup") : Bundle.getMessage("DeleteFromRoster")); // NOI18N
        menuItem.addActionListener((ActionEvent e1) -> {
            deleteLoco();
        });
        popupMenu.add(menuItem);
        menuItem.setEnabled(this.getSelectedRosterEntries().length > 0);

        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Start the identify operation after [Identify Loco] button pressed.
     * <p>
     * This defines what happens when the identify is done.
     */
    //taken out of CombinedLocoSelPane
    protected void startIdentifyLoco() {
        final RosterFrame me = this;
        Programmer programmer = null;
        if (modePanel.isSelected()) {
            programmer = modePanel.getProgrammer();
        }
        if (programmer == null) {
            GlobalProgrammerManager gpm = InstanceManager.getNullableDefault(GlobalProgrammerManager.class);
            if (gpm != null) {
                programmer = gpm.getGlobalProgrammer();
                log.warn("Selector did not provide a programmer, attempt to use GlobalProgrammerManager default: {}", programmer);
            } else {
                log.warn("Selector did not provide a programmer, and no ProgramManager found in InstanceManager");
            }
        }

        // if failed to get programmer, tell user and stop
        if (programmer == null) {
            log.error("Identify loco called when no service mode programmer is available; button should have been disabled");
            JOptionPane.showMessageDialog(null, Bundle.getMessage("IdentifyError"));
            return;
        }

        // and now do the work
        IdentifyLoco ident = new IdentifyLoco(programmer) {
            private final RosterFrame who = me;

            @Override
            protected void done(int dccAddress) {
                // if Done, updated the selected decoder
                // on the GUI thread, right now
                jmri.util.ThreadingUtil.runOnGUI(() -> {
                    who.selectLoco(dccAddress, !shortAddr, cv8val, cv7val);
                });
            }

            @Override
            protected void message(String m) {
                // on the GUI thread, right now
                jmri.util.ThreadingUtil.runOnGUI(() -> {
                    statusField.setText(m);
                });
            }

            @Override
            protected void error() {
                // raise the button again
                //idloco.setSelected(false);
            }
        };
        ident.start();
    }

    protected void startProgrammer(DecoderFile decoderFile, RosterEntry re, String filename) {
        if (inStartProgrammer) {
            log.debug("Call to start programmer has been called twice when the first call hasn't opened");
            return;
        }
        if (!checkIfEntrySelected()) {
            return;
        }
        try {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            inStartProgrammer = true;
            String title = re.getId();
            JFrame progFrame = null;
            if (edit.isSelected()) {
                progFrame = new PaneProgFrame(decoderFile, re, title, "programmers" + File.separator + filename + ".xml", null, false) {
                    @Override
                    protected JPanel getModePane() {
                        return null;
                    }
                };
            } else if (service.isSelected()) {
                progFrame = new PaneServiceProgFrame(decoderFile, re, title, "programmers" + File.separator + filename + ".xml", modePanel.getProgrammer());
            } else if (ops.isSelected()) {
                int address = Integer.parseInt(re.getDccAddress());
                boolean longAddr = re.isLongAddress();
                Programmer pProg = InstanceManager.getDefault(AddressedProgrammerManager.class).getAddressedProgrammer(longAddr, address);
                progFrame = new PaneOpsProgFrame(decoderFile, re, title, "programmers" + File.separator + filename + ".xml", pProg);
            }
            if (progFrame == null) {
                return;
            }
            progFrame.pack();
            progFrame.setVisible(true);
        } finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        inStartProgrammer = false;
    }

    /**
     * Create and display a status bar along the bottom edge of the Roster main
     * pane.
     * <p>
     * TODO This status bar needs sorting out properly
     */
    protected void statusBar() {
        addToStatusBox(serviceModeProgrammerLabel, null);
        addToStatusBox(operationsModeProgrammerLabel, null);
        JLabel programmerStatusLabel = new JLabel(Bundle.getMessage("ProgrammerStatus"));
        statusField.setText(Bundle.getMessage("StateIdle"));
        addToStatusBox(programmerStatusLabel, statusField);
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        if (profile != null) {
            addToStatusBox(new JLabel(Bundle.getMessage("ActiveProfile", profile.getName())), null);
        }
    }

    protected void systemsMenu() {
        ActiveSystemsMenu.addItems(getMenu());
        getMenu().add(new WindowMenu(this));
    }

    void updateDetails() {
        if (re == null) {
            String value = (rtable.getTable().getSelectedRowCount() > 1) ? "Multiple Items Selected" : "";
            filename.setText(value);
            dateUpdated.setText(value);
            decoderModel.setText(value);
            decoderFamily.setText(value);
            id.setText(value);
            roadName.setText(value);
            dccAddress.setText(value);
            roadNumber.setText(value);
            mfg.setText(value);
            model.setText(value);
            owner.setText(value);
            locoImage.setImagePath(null);
        } else {
            filename.setText(re.getFileName());
            dateUpdated.setText((re.getDateModified() != null)
                    ? DateFormat.getDateTimeInstance().format(re.getDateModified())
                    : re.getDateUpdated());
            decoderModel.setText(re.getDecoderModel());
            decoderFamily.setText(re.getDecoderFamily());
            dccAddress.setText(re.getDccAddress());
            id.setText(re.getId());
            roadName.setText(re.getRoadName());
            roadNumber.setText(re.getRoadNumber());
            mfg.setText(re.getMfg());
            model.setText(re.getModel());
            owner.setText(re.getOwner());
            locoImage.setImagePath(re.getImagePath());
            if (hideRosterImage) {
                locoImage.setVisible(false);
            } else {
                locoImage.setVisible(true);
            }
            prog1Button.setEnabled(true);
            prog2Button.setEnabled(true);
            throttleLabels.setEnabled(true);
            rosterMedia.setEnabled(true);
            throttleLaunch.setEnabled(true);
            updateProgMode();
        }
    }

    void updateProgMode() {
        String progMode;
        if (service.isSelected()) {
            progMode = "setprogservice";
        } else if (ops.isSelected()) {
            progMode = "setprogops";
        } else {
            progMode = "setprogedit";
        }
        firePropertyChange(progMode, "setSelected", true);
    }

    /**
     * Handle setting up and updating the GUI for the types of programmer
     * available.
     *
     * @param evt the triggering event; if not null and if a removal of a
     *            ProgrammerManager, care will be taken not to trigger the
     *            automatic creation of a new ProgrammerManager
     */
    protected void updateProgrammerStatus(@CheckForNull PropertyChangeEvent evt) {
        log.debug("Updating Programmer Status");
        ConnectionConfig oldServMode = serModeProCon;
        ConnectionConfig oldOpsMode = opsModeProCon;
        GlobalProgrammerManager gpm = null;
        AddressedProgrammerManager apm = null;

        // Find the connection that goes with the global programmer
        // test that IM has a default GPM, or that event is not the removal of a GPM
        if (InstanceManager.containsDefault(GlobalProgrammerManager.class)
                || (evt != null
                && evt.getPropertyName().equals(InstanceManager.getDefaultsPropertyName(GlobalProgrammerManager.class))
                && evt.getNewValue() == null)) {
            gpm = InstanceManager.getNullableDefault(GlobalProgrammerManager.class);
            log.trace("found global programming manager {}", gpm);
        }
        if (gpm != null) {
            String serviceModeProgrammerName = gpm.getUserName();
            log.debug("GlobalProgrammerManager found of class {} name {} ", gpm.getClass(), serviceModeProgrammerName);
            InstanceManager.getOptionalDefault(ConnectionConfigManager.class).ifPresent((ccm) -> {
                for (ConnectionConfig connection : ccm) {
                    log.debug("Checking connection name {}", connection.getConnectionName());
                    if (connection.getConnectionName() != null && connection.getConnectionName().equals(serviceModeProgrammerName)) {
                        log.debug("Connection found for GlobalProgrammermanager");
                        serModeProCon = connection;
                    }
                }
            });
        }

        // Find the connection that goes with the addressed programmer
        // test that IM has a default APM, or that event is not the removal of an APM
        if (InstanceManager.containsDefault(AddressedProgrammerManager.class)
                || (evt != null
                && evt.getPropertyName().equals(InstanceManager.getDefaultsPropertyName(AddressedProgrammerManager.class))
                && evt.getNewValue() == null)) {
            apm = InstanceManager.getNullableDefault(AddressedProgrammerManager.class);
        }
        if (apm != null) {
            String opsModeProgrammerName = apm.getUserName();
            log.debug("AddressedProgrammerManager found of class {} name {} ", apm.getClass(), opsModeProgrammerName);
            InstanceManager.getOptionalDefault(ConnectionConfigManager.class).ifPresent((ccm) -> {
                for (ConnectionConfig connection : ccm) {
                    log.debug("Checking connection name {}", connection.getConnectionName());
                    if (connection.getConnectionName() != null && connection.getConnectionName().equals(opsModeProgrammerName)) {
                        log.debug("Connection found for AddressedProgrammermanager");
                        opsModeProCon = connection;
                    }
                }
            });
        }

        log.trace("start global check with {}, {}, {}", serModeProCon, gpm, (gpm != null ? gpm.isGlobalProgrammerAvailable() : "<none>"));
        if (serModeProCon != null && gpm != null && gpm.isGlobalProgrammerAvailable()) {
            if (ConnectionStatus.instance().isConnectionOk(serModeProCon.getConnectionName(), serModeProCon.getInfo())) {
                log.debug("GPM Connection online 1");
                serviceModeProgrammerLabel.setText(
                        Bundle.getMessage("ServiceModeProgOnline", serModeProCon.getConnectionName()));
                serviceModeProgrammerLabel.setForeground(new Color(0, 128, 0));
            } else {
                log.debug("GPM Connection onffline");
                serviceModeProgrammerLabel.setText(
                        Bundle.getMessage("ServiceModeProgOffline", serModeProCon.getConnectionName()));
                serviceModeProgrammerLabel.setForeground(Color.red);
            }
            if (oldServMode == null) {
                contextService.setEnabled(true);
                contextService.setVisible(true);
                service.setEnabled(true);
                service.setVisible(true);
                firePropertyChange("setprogservice", "setEnabled", true);
            }
        } else if (gpm != null && gpm.isGlobalProgrammerAvailable()) {
            if (ConnectionStatus.instance().isSystemOk(gpm.getUserName())) {
                log.debug("GPM Connection online 2");
                serviceModeProgrammerLabel.setText(
                        Bundle.getMessage("ServiceModeProgOnline", gpm.getUserName()));
                serviceModeProgrammerLabel.setForeground(new Color(0, 128, 0));
            } else {
                log.debug("GPM Connection onffline");
                serviceModeProgrammerLabel.setText(
                        Bundle.getMessage("ServiceModeProgOffline", gpm.getUserName()));
                serviceModeProgrammerLabel.setForeground(Color.red);
            }
            if (oldServMode == null) {
                contextService.setEnabled(true);
                contextService.setVisible(true);
                service.setEnabled(true);
                service.setVisible(true);
                firePropertyChange("setprogservice", "setEnabled", true);
            }
        } else {
            // No service programmer available, disable interface sections not available
            log.debug("no service programmer");
            serviceModeProgrammerLabel.setText(Bundle.getMessage("NoServiceProgrammerAvailable"));
            serviceModeProgrammerLabel.setForeground(Color.red);
            if (oldServMode != null) {
                contextService.setEnabled(false);
                contextService.setVisible(false);
                service.setEnabled(false);
                service.setVisible(false);
                firePropertyChange("setprogservice", "setEnabled", false);
            }
            // Disable Identify in toolBar
            // This relies on it being the 2nd item in the tool bar, as defined in xml//config/parts/jmri/jmrit/roster/swing/RosterFrameToolBar.xml
            // Because of I18N, we don't look for a particular Action name here
            getToolBar().getComponents()[1].setEnabled(false);
        }

        if (opsModeProCon != null && apm != null && apm.isAddressedModePossible()) {
            if (ConnectionStatus.instance().isConnectionOk(opsModeProCon.getConnectionName(), opsModeProCon.getInfo())) {
                log.debug("Ops Mode Connection online");
                operationsModeProgrammerLabel.setText(
                        Bundle.getMessage("OpsModeProgOnline", opsModeProCon.getConnectionName()));
                operationsModeProgrammerLabel.setForeground(new Color(0, 128, 0));
            } else {
                log.debug("Ops Mode Connection offline");
                operationsModeProgrammerLabel.setText(
                        Bundle.getMessage("OpsModeProgOffline", opsModeProCon.getConnectionName()));
                operationsModeProgrammerLabel.setForeground(Color.red);
            }
            if (oldOpsMode == null) {
                contextOps.setEnabled(true);
                contextOps.setVisible(true);
                ops.setEnabled(true);
                ops.setVisible(true);
                firePropertyChange("setprogops", "setEnabled", true);
            }
        } else if (apm != null && apm.isAddressedModePossible()) {
            if (ConnectionStatus.instance().isSystemOk(apm.getUserName())) {
                log.debug("Ops Mode Connection online");
                operationsModeProgrammerLabel.setText(
                        Bundle.getMessage("OpsModeProgOnline", apm.getUserName()));
                operationsModeProgrammerLabel.setForeground(new Color(0, 128, 0));
            } else {
                log.debug("Ops Mode Connection offline");
                operationsModeProgrammerLabel.setText(
                        Bundle.getMessage("OpsModeProgOffline", apm.getUserName()));
                operationsModeProgrammerLabel.setForeground(Color.red);
            }
            if (oldOpsMode == null) {
                contextOps.setEnabled(true);
                contextOps.setVisible(true);
                ops.setEnabled(true);
                ops.setVisible(true);
                firePropertyChange("setprogops", "setEnabled", true);
            }
        } else {
            operationsModeProgrammerLabel.setText(Bundle.getMessage("NoOpsProgrammerAvailable"));
            operationsModeProgrammerLabel.setForeground(Color.red);
            if (oldOpsMode != null) {
                contextOps.setEnabled(false);
                contextOps.setVisible(false);
                ops.setEnabled(false);
                ops.setVisible(false);
                firePropertyChange("setprogops", "setEnabled", false);
            }
        }
        String strProgMode;
        if (service.isEnabled()) {
            contextService.setSelected(true);
            service.setSelected(true);
            strProgMode = "setprogservice";
            modePanel.setVisible(true);
        } else if (ops.isEnabled()) {
            contextOps.setSelected(true);
            ops.setSelected(true);
            strProgMode = "setprogops";
            modePanel.setVisible(false);
        } else {
            contextEdit.setSelected(true);
            edit.setSelected(true);
            modePanel.setVisible(false);
            strProgMode = "setprogedit";
        }
        firePropertyChange(strProgMode, "setSelected", true);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        closeWindow(e);
    }

    /**
     * Displays a context (right-click) menu for a roster entry.
     */
    private class RosterPopupListener extends MouseAdapter {

        // does clickTimer still actually do anything in this code?
        // it looks like it just starts and stops, without
        // invoking anything
        javax.swing.Timer clickTimer = null;

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopup(e);
                return;
            }
            if (clickTimer == null) {
                clickTimer = new Timer(clickDelay, (ActionEvent e1) -> {
                    //Single click item is handled else where.
                });
                clickTimer.setRepeats(false);
            }
            if (e.getClickCount() == 1) {
                clickTimer.start();
            } else if (e.getClickCount() == 2) {
                clickTimer.stop();
                startProgrammer(null, re, programmer1);
            }
        }
    }

    private static class ExportRosterItem extends ExportRosterItemAction {

        ExportRosterItem(String pName, Component pWho, RosterEntry re) {
            super(pName, pWho);
            super.setExistingEntry(re);
        }

        @Override
        protected boolean selectFrom() {
            return true;
        }
    }

    private static class CopyRosterItem extends CopyRosterItemAction {

        CopyRosterItem(String pName, Component pWho, RosterEntry re) {
            super(pName, pWho);
            super.setExistingEntry(re);
        }

        @Override
        protected boolean selectFrom() {
            return true;
        }
    }
    private final static Logger log = LoggerFactory.getLogger(RosterFrame.class);
}
