package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.AddressedProgrammerManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.ShutDownTask;
import jmri.UserPreferencesManager;
import jmri.implementation.swing.SwingShutDownTask;
import jmri.jmrit.XmlFile;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.*;
import jmri.jmrit.symbolicprog.*;
import jmri.util.BusyGlassPane;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Frame providing a command station programmer from decoder definition files.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2004, 2005, 2008, 2014, 2018, 2019
 * @author D Miller Copyright 2003, 2005
 * @author Howard G. Penny Copyright (C) 2005
 */
abstract public class PaneProgFrame extends JmriJFrame
        implements java.beans.PropertyChangeListener, PaneContainer {

    // members to contain working variable, CV values
    JLabel progStatus = new JLabel(Bundle.getMessage("StateIdle"));
    CvTableModel cvModel;
    VariableTableModel variableModel;

    ResetTableModel resetModel;
    JMenu resetMenu = null;

    ArrayList<ExtraMenuTableModel> extraMenuModelList;
    ArrayList<JMenu> extraMenuList = new ArrayList<>();

    Programmer mProgrammer;
    boolean noDecoder = false;

    JMenuBar menuBar = new JMenuBar();

    JPanel tempPane; // passed around during construction

    boolean _opsMode;

    boolean maxFnNumDirty = false;
    String maxFnNumOld = "";
    String maxFnNumNew = "";

    RosterEntry _rosterEntry;
    RosterEntryPane _rPane = null;
    FunctionLabelPane _flPane = null;
    RosterMediaPane _rMPane = null;
    String _frameEntryId;

    List<JPanel> paneList = new ArrayList<>();
    int paneListIndex;

    List<Element> decoderPaneList;

    BusyGlassPane glassPane;
    List<JComponent> activeComponents = new ArrayList<>();

    String filename;
    String programmerShowEmptyPanes = "";
    String decoderShowEmptyPanes = "";
    String decoderAllowResetDefaults = "";
    String suppressFunctionLabels = "";
    String suppressRosterMedia = "";

    // GUI member declarations
    JTabbedPane tabPane = new JTabbedPane();
    JToggleButton readChangesButton = new JToggleButton(Bundle.getMessage("ButtonReadChangesAllSheets"));
    JToggleButton writeChangesButton = new JToggleButton(Bundle.getMessage("ButtonWriteChangesAllSheets"));
    JToggleButton readAllButton = new JToggleButton(Bundle.getMessage("ButtonReadAllSheets"));
    JToggleButton writeAllButton = new JToggleButton(Bundle.getMessage("ButtonWriteAllSheets"));

    ItemListener l1;
    ItemListener l2;
    ItemListener l3;
    ItemListener l4;

    ShutDownTask decoderDirtyTask;
    ShutDownTask fileDirtyTask;

    public RosterEntryPane getRosterPane() { return _rPane;}
    public FunctionLabelPane getFnLabelPane() { return _flPane;}

    /**
     * Abstract method to provide a JPanel setting the programming mode, if
     * appropriate.
     * <p>
     * A null value is ignored (?)
     * @return new mode panel for inclusion in the GUI
     */
    abstract protected JPanel getModePane();

    protected void installComponents() {

        // create ShutDownTasks
        if (decoderDirtyTask == null) {
            decoderDirtyTask = new SwingShutDownTask("DecoderPro Decoder Window Check",
                    Bundle.getMessage("PromptQuitWindowNotWrittenDecoder"), null, this) {
                @Override
                public boolean checkPromptNeeded() {
                    return !checkDirtyDecoder();
                }
            };
        }
        jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(decoderDirtyTask);
        if (fileDirtyTask == null) {
            fileDirtyTask = new SwingShutDownTask("DecoderPro Decoder Window Check",
                    Bundle.getMessage("PromptQuitWindowNotWrittenConfig"),
                    Bundle.getMessage("PromptSaveQuit"), this) {
                @Override
                public boolean checkPromptNeeded() {
                    return !checkDirtyFile();
                }

                @Override
                public boolean doPrompt() {
                    // storeFile returns false if failed, so abort shutdown
                    return storeFile();
                }
            };
        }
        jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(fileDirtyTask);

        // Create a menu bar
        setJMenuBar(menuBar);

        // add a "File" menu
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        menuBar.add(fileMenu);

        // add a "Factory Reset" menu
        resetMenu = new JMenu(Bundle.getMessage("MenuReset"));
        menuBar.add(resetMenu);
        resetMenu.add(new FactoryResetAction(Bundle.getMessage("MenuFactoryReset"), resetModel, this));
        resetMenu.setEnabled(false);

        // Add a save item
        JMenuItem menuItem = new JMenuItem(Bundle.getMessage("MenuSaveNoDots"));
        menuItem.addActionListener(e -> storeFile()

        );
        menuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.KeyEvent.META_DOWN_MASK));
        fileMenu.add(menuItem);

        JMenu printSubMenu = new JMenu(Bundle.getMessage("MenuPrint"));
        printSubMenu.add(new PrintAction(Bundle.getMessage("MenuPrintAll"), this, false));
        printSubMenu.add(new PrintCvAction(Bundle.getMessage("MenuPrintCVs"), cvModel, this, false, _rosterEntry));
        fileMenu.add(printSubMenu);

        JMenu printPreviewSubMenu = new JMenu(Bundle.getMessage("MenuPrintPreview"));
        printPreviewSubMenu.add(new PrintAction(Bundle.getMessage("MenuPrintPreviewAll"), this, true));
        printPreviewSubMenu.add(new PrintCvAction(Bundle.getMessage("MenuPrintPreviewCVs"), cvModel, this, true, _rosterEntry));
        fileMenu.add(printPreviewSubMenu);

        // add "Import" submenu; this is hierarchical because
        // some of the names are so long, and we expect more formats
        JMenu importSubMenu = new JMenu(Bundle.getMessage("MenuImport"));
        fileMenu.add(importSubMenu);
        importSubMenu.add(new CsvImportAction(Bundle.getMessage("MenuImportCSV"), cvModel, this, progStatus));
        importSubMenu.add(new Pr1ImportAction(Bundle.getMessage("MenuImportPr1"), cvModel, this, progStatus));
        importSubMenu.add(new LokProgImportAction(Bundle.getMessage("MenuImportLokProg"), cvModel, this, progStatus));
        importSubMenu.add(new QuantumCvMgrImportAction(Bundle.getMessage("MenuImportQuantumCvMgr"), cvModel, this, progStatus));
        importSubMenu.add(new TcsImportAction(Bundle.getMessage("MenuImportTcsFile"), cvModel, variableModel, this, progStatus, _rosterEntry));
        if (TcsDownloadAction.willBeEnabled()) {
            importSubMenu.add(new TcsDownloadAction(Bundle.getMessage("MenuImportTcsCS"), cvModel, variableModel, this, progStatus, _rosterEntry));
        }

        // add "Export" submenu; this is hierarchical because
        // some of the names are so long, and we expect more formats
        JMenu exportSubMenu = new JMenu(Bundle.getMessage("MenuExport"));
        fileMenu.add(exportSubMenu);
        exportSubMenu.add(new CsvExportAction(Bundle.getMessage("MenuExportCSV"), cvModel, this));
        exportSubMenu.add(new CsvExportModifiedAction(Bundle.getMessage("MenuExportCSVModified"), cvModel, this));
        exportSubMenu.add(new Pr1ExportAction(Bundle.getMessage("MenuExportPr1DOS"), cvModel, this));
        exportSubMenu.add(new Pr1WinExportAction(Bundle.getMessage("MenuExportPr1WIN"), cvModel, this));
        exportSubMenu.add(new CsvExportVariablesAction(Bundle.getMessage("MenuExportVariables"), variableModel, this));
        exportSubMenu.add(new TcsExportAction(Bundle.getMessage("MenuExportTcsFile"), cvModel, variableModel, _rosterEntry, this));
        if (TcsDownloadAction.willBeEnabled()) {
            exportSubMenu.add(new TcsUploadAction(Bundle.getMessage("MenuExportTcsCS"), cvModel, variableModel, _rosterEntry, this));
        }

        // add "Import" submenu; this is hierarchical because
        // some of the names are so long, and we expect more formats
        JMenu speedTableSubMenu = new JMenu(Bundle.getMessage("MenuSpeedTable"));
        fileMenu.add(speedTableSubMenu);
        ButtonGroup SpeedTableNumbersGroup = new ButtonGroup();
        UserPreferencesManager upm = InstanceManager.getDefault(UserPreferencesManager.class);
        Object speedTableNumbersSelectionObj = upm.getProperty(SpeedTableNumbers.class.getName(), "selection");

        SpeedTableNumbers speedTableNumbersSelection =
                speedTableNumbersSelectionObj != null
                ? SpeedTableNumbers.valueOf(speedTableNumbersSelectionObj.toString())
                : null;

        for (SpeedTableNumbers speedTableNumbers : SpeedTableNumbers.values()) {
            JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(speedTableNumbers.toString());
            rbMenuItem.addActionListener((ActionEvent event) -> {
                rbMenuItem.setSelected(true);
                upm.setProperty(SpeedTableNumbers.class.getName(), "selection", speedTableNumbers.name());
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("MenuSpeedTable_CloseReopenWindow"));
            });
            rbMenuItem.setSelected(speedTableNumbers == speedTableNumbersSelection);
            speedTableSubMenu.add(rbMenuItem);
            SpeedTableNumbersGroup.add(rbMenuItem);
        }

        // to control size, we need to insert a single
        // JPanel, then have it laid out with BoxLayout
        JPanel pane = new JPanel();
        tempPane = pane;

        // general GUI config
        pane.setLayout(new BorderLayout());

        // configure GUI elements
        // set read buttons enabled state, tooltips
        enableReadButtons();

        readChangesButton.addItemListener(l1 = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                prepGlassPane(readChangesButton);
                readChangesButton.setText(Bundle.getMessage("ButtonStopReadChangesAll"));
                readChanges();
            } else {
                if (_programmingPane != null) {
                    _programmingPane.stopProgramming();
                }
                paneListIndex = paneList.size();
                readChangesButton.setText(Bundle.getMessage("ButtonReadChangesAllSheets"));
            }
        });

        readAllButton.addItemListener(l3 = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                prepGlassPane(readAllButton);
                readAllButton.setText(Bundle.getMessage("ButtonStopReadAll"));
                readAll();
            } else {
                if (_programmingPane != null) {
                    _programmingPane.stopProgramming();
                }
                paneListIndex = paneList.size();
                readAllButton.setText(Bundle.getMessage("ButtonReadAllSheets"));
            }
        });

        writeChangesButton.setToolTipText(Bundle.getMessage("TipWriteHighlightedValues"));
        writeChangesButton.addItemListener(l2 = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                prepGlassPane(writeChangesButton);
                writeChangesButton.setText(Bundle.getMessage("ButtonStopWriteChangesAll"));
                writeChanges();
            } else {
                if (_programmingPane != null) {
                    _programmingPane.stopProgramming();
                }
                paneListIndex = paneList.size();
                writeChangesButton.setText(Bundle.getMessage("ButtonWriteChangesAllSheets"));
            }
        });

        writeAllButton.setToolTipText(Bundle.getMessage("TipWriteAllValues"));
        writeAllButton.addItemListener(l4 = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                prepGlassPane(writeAllButton);
                writeAllButton.setText(Bundle.getMessage("ButtonStopWriteAll"));
                writeAll();
            } else {
                if (_programmingPane != null) {
                    _programmingPane.stopProgramming();
                }
                paneListIndex = paneList.size();
                writeAllButton.setText(Bundle.getMessage("ButtonWriteAllSheets"));
            }
        });

        // most of the GUI is done from XML in readConfig() function
        // which configures the tabPane
        pane.add(tabPane, BorderLayout.CENTER);

        // and put that pane into the JFrame
        getContentPane().add(pane);

    }

    void setProgrammingGui(JPanel bottom) {
        // see if programming mode is available
        JPanel tempModePane = null;
        if (!noDecoder) {
            tempModePane = getModePane();
        }
        if (tempModePane != null) {
            // if so, configure programming part of GUI
            // add buttons
            JPanel bottomButtons = new JPanel();
            bottomButtons.setLayout(new BoxLayout(bottomButtons, BoxLayout.X_AXIS));

            bottomButtons.add(readChangesButton);
            bottomButtons.add(writeChangesButton);
            bottomButtons.add(readAllButton);
            bottomButtons.add(writeAllButton);
            bottom.add(bottomButtons);

            // add programming mode
            bottom.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
            JPanel temp = new JPanel();
            bottom.add(temp);
            temp.add(tempModePane);
        } else {
            // set title to Editing
            super.setTitle(Bundle.getMessage("TitleEditPane", _frameEntryId));
        }

        // add space for (programming) status message
        bottom.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
        progStatus.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        bottom.add(progStatus);
    }

    // ================== Search section ==================

    // create and add the Search GUI
    void setSearchGui(JPanel bottom) {
        // search field
        searchBar = new jmri.util.swing.SearchBar(searchForwardTask, searchBackwardTask, searchDoneTask);
        searchBar.setVisible(false); // start not visible
        searchBar.configureKeyModifiers(this);
        bottom.add(searchBar);
    }

    jmri.util.swing.SearchBar searchBar;
    static class SearchPair {
        WatchingLabel label;
        JPanel tab;
        SearchPair(WatchingLabel label, @Nonnull JPanel tab) {
            this.label = label;
            this.tab = tab;
        }
    }

    ArrayList<SearchPair> searchTargetList;
    int nextSearchTarget = 0;

    // Load the array of search targets
    protected void loadSearchTargets() {
        if (searchTargetList != null) return;

        searchTargetList = new ArrayList<>();

        for (JPanel p : getPaneList()) {
            for (Component c : p.getComponents()) {
                loadJPanel(c, p);
            }
        }

        // add the panes themselves
        for (JPanel tab : getPaneList()) {
            searchTargetList.add( new SearchPair( null, tab ));
        }
    }

    // Recursive load of possible search targets
    protected void loadJPanel(Component c, JPanel tab) {
        if (c instanceof JPanel) {
            for (Component d : ((JPanel)c).getComponents()) {
                loadJPanel(d, tab);
            }
        } else if (c instanceof JScrollPane) {
            loadJPanel( ((JScrollPane)c).getViewport().getView(), tab);
        } else if (c instanceof WatchingLabel) {
            searchTargetList.add( new SearchPair( (WatchingLabel)c, tab));
        }
    }

    // Search didn't find anything at all
    protected void searchDidNotFind() {
         java.awt.Toolkit.getDefaultToolkit().beep();
    }

    // Search succeeded, go to the result
    protected void searchGoesTo(SearchPair result) {
        tabPane.setSelectedComponent(result.tab);
        if (result.label != null) {
            SwingUtilities.invokeLater(() -> result.label.getWatched().requestFocus());
        } else {
            log.trace("search result set to tab {}", result.tab);
        }
    }

    // Check a single case to see if its search match
    // @return true for matched
    private boolean checkSearchTarget(int index, String target) {
        boolean result = false;
        if (searchTargetList.get(index).label != null ) {
            // match label text
            if ( ! searchTargetList.get(index).label.getText().toUpperCase().contains(target.toUpperCase() ) ) {
                return false;
            }
            // only match if showing
            return searchTargetList.get(index).label.isShowing();
        } else {
            // Match pane label.
            // Finding the tab requires a search here.
            // Could have passed a clue along in SwingUtilities
            for (int i = 0; i < tabPane.getTabCount(); i++) {
                if (tabPane.getComponentAt(i) == searchTargetList.get(index).tab) {
                    result = tabPane.getTitleAt(i).toUpperCase().contains(target.toUpperCase());
                }
            }
        }
        return result;
    }

    // Invoked by forward search operation
    private final Runnable searchForwardTask = new Runnable() {
        @Override
        public void run() {
            log.trace("start forward");
            loadSearchTargets();
            String target = searchBar.getSearchString();

            nextSearchTarget++;
            if (nextSearchTarget < 0 ) nextSearchTarget = 0;
            if (nextSearchTarget >= searchTargetList.size() ) nextSearchTarget = 0;

            int startingSearchTarget = nextSearchTarget;

            while (nextSearchTarget < searchTargetList.size()) {
                if ( checkSearchTarget(nextSearchTarget, target)) {
                    // hit!
                    searchGoesTo(searchTargetList.get(nextSearchTarget));
                    return;
                }
                nextSearchTarget++;
            }

            // end reached, wrap
            nextSearchTarget = 0;
            while (nextSearchTarget < startingSearchTarget) {
                if ( checkSearchTarget(nextSearchTarget, target)) {
                    // hit!
                    searchGoesTo(searchTargetList.get(nextSearchTarget));
                    return;
                }
                nextSearchTarget++;
            }
            // not found
            searchDidNotFind();
        }
    };

    // Invoked by backward search operation
    private final Runnable searchBackwardTask = new Runnable() {
        @Override
        public void run() {
            log.trace("start backward");
            loadSearchTargets();
            String target = searchBar.getSearchString();

            nextSearchTarget--;
            if (nextSearchTarget < 0 ) nextSearchTarget = searchTargetList.size()-1;
            if (nextSearchTarget >= searchTargetList.size() ) nextSearchTarget = searchTargetList.size()-1;

            int startingSearchTarget = nextSearchTarget;

            while (nextSearchTarget > 0) {
                if ( checkSearchTarget(nextSearchTarget, target)) {
                    // hit!
                    searchGoesTo(searchTargetList.get(nextSearchTarget));
                    return;
                }
                nextSearchTarget--;
            }

            // start reached, wrap
            nextSearchTarget = searchTargetList.size() - 1;
            while (nextSearchTarget > startingSearchTarget) {
                if ( checkSearchTarget(nextSearchTarget, target)) {
                    // hit!
                    searchGoesTo(searchTargetList.get(nextSearchTarget));
                    return;
                }
                nextSearchTarget--;
            }
            // not found
            searchDidNotFind();
        }
    };

    // Invoked when search bar Done is pressed
    private final Runnable searchDoneTask = new Runnable() {
        @Override
        public void run() {
            log.debug("done with search bar");
            searchBar.setVisible(false);
        }
    };

    // =================== End of search section ==================

    public List<JPanel> getPaneList() {
        return paneList;
    }

    void addHelp() {
        addHelpMenu("package.jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame", true);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension screen = getMaximumSize();
        int width = Math.min(super.getPreferredSize().width, screen.width);
        int height = Math.min(super.getPreferredSize().height, screen.height);
        return new Dimension(width, height);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension screen = getToolkit().getScreenSize();
        return new Dimension(screen.width, screen.height - 35);
    }

    /**
     * Enable the [Read all] and [Read changes] buttons if possible. This checks
     * to make sure this is appropriate, given the attached programmer's
     * capability.
     */
    void enableReadButtons() {
        readChangesButton.setToolTipText(Bundle.getMessage("TipReadChanges"));
        readAllButton.setToolTipText(Bundle.getMessage("TipReadAll"));
        // check with CVTable programmer to see if read is possible
        if (cvModel != null && cvModel.getProgrammer() != null
                && !cvModel.getProgrammer().getCanRead()
                || noDecoder) {
            // can't read, disable the button
            readChangesButton.setEnabled(false);
            readAllButton.setEnabled(false);
            readChangesButton.setToolTipText(Bundle.getMessage("TipNoRead"));
            readAllButton.setToolTipText(Bundle.getMessage("TipNoRead"));
        } else {
            readChangesButton.setEnabled(true);
            readAllButton.setEnabled(true);
        }
    }

    /**
     * Initialization sequence:
     * <ul>
     * <li> Ask the RosterEntry to read its contents
     * <li> If the decoder file is specified, open and load it, otherwise get
     * the decoder filename from the RosterEntry and load that. Note that we're
     * assuming the roster entry has the right decoder, at least w.r.t. the loco
     * file.
     * <li> Fill CV values from the roster entry
     * <li> Create the programmer panes
     * </ul>
     *
     * @param pDecoderFile    XML file defining the decoder contents; if null,
     *                        the decoder definition is found from the
     *                        RosterEntry
     * @param pRosterEntry    RosterEntry for information on this locomotive
     * @param pFrameEntryId   Roster ID (entry) loaded into the frame
     * @param pProgrammerFile Name of the programmer file to use
     * @param pProg           Programmer object to be used to access CVs
     * @param opsMode         true for opsMode, else false.
     */
    public PaneProgFrame(DecoderFile pDecoderFile, @Nonnull RosterEntry pRosterEntry,
            String pFrameEntryId, String pProgrammerFile, Programmer pProg, boolean opsMode) {
        super(Bundle.getMessage("TitleProgPane", pFrameEntryId));

        _rosterEntry = pRosterEntry;
        _opsMode = opsMode;
        filename = pProgrammerFile;
        mProgrammer = pProg;
        _frameEntryId = pFrameEntryId;

        // create the tables
        cvModel = new CvTableModel(progStatus, mProgrammer);

        variableModel = new VariableTableModel(progStatus, new String[] {"Name", "Value"},
                cvModel);

        resetModel = new ResetTableModel(progStatus, mProgrammer);
        extraMenuModelList = new ArrayList<>();

        // handle the roster entry
        _rosterEntry.setOpen(true);

        installComponents();

        if (_rosterEntry.getFileName() != null) {
            // set the loco file name in the roster entry
            _rosterEntry.readFile();  // read, but don't yet process
        }

        if (pDecoderFile != null) {
            loadDecoderFile(pDecoderFile, _rosterEntry);
        } else {
            loadDecoderFromLoco(pRosterEntry);
        }

        // save default values
        saveDefaults();

        // finally fill the Variable and CV values from the specific loco file
        if (_rosterEntry.getFileName() != null) {
            _rosterEntry.loadCvModel(variableModel, cvModel);
        }

        // mark file state as consistent
        variableModel.setFileDirty(false);

        // if the Reset Table was used lets enable the menu item
        if (!_opsMode || resetModel.hasOpsModeReset()) {
            if (resetModel.getRowCount() > 0) {
                resetMenu.setEnabled(true);
            }
        }

        // if there are extra menus defined, enable them
        log.trace("enabling {} {}", extraMenuModelList.size(), extraMenuModelList);
        for (int i = 0; i<extraMenuModelList.size(); i++) {
            log.trace("enabling {} {}", _opsMode, extraMenuModelList.get(i).hasOpsModeReset());
            if ( !_opsMode || extraMenuModelList.get(i).hasOpsModeReset()) {
                if (extraMenuModelList.get(i).getRowCount() > 0) {
                    extraMenuList.get(i).setEnabled(true);
                }
            }
        }

        // set the programming mode
        if (pProg != null) {
            if (InstanceManager.getOptionalDefault(AddressedProgrammerManager.class).isPresent()
                    || InstanceManager.getOptionalDefault(GlobalProgrammerManager.class).isPresent()) {
                // go through in preference order, trying to find a mode
                // that exists in both the programmer and decoder.
                // First, get attributes. If not present, assume that
                // all modes are usable
                Element programming = null;
                if (decoderRoot != null
                        && (programming = decoderRoot.getChild("decoder").getChild("programming")) != null) {

                    // add a verify-write facade if configured
                    Programmer pf = mProgrammer;
                    if (getDoConfirmRead()) {
                        pf = new jmri.implementation.VerifyWriteProgrammerFacade(pf);
                        log.debug("adding VerifyWriteProgrammerFacade, new programmer is {}", pf);
                    }
                    // add any facades defined in the decoder file
                    pf = jmri.implementation.ProgrammerFacadeSelector
                            .loadFacadeElements(programming, pf, getCanCacheDefault(), pProg);
                    log.debug("added any other FacadeElements, new programmer is {}", pf);
                    mProgrammer = pf;
                    cvModel.setProgrammer(pf);
                    resetModel.setProgrammer(pf);
                    for (var model : extraMenuModelList) {
                        model.setProgrammer(pf);
                    }
                    log.debug("Found programmer: {}", cvModel.getProgrammer());
                }

                // done after setting facades in case new possibilities appear
                if (programming != null) {
                    pickProgrammerMode(programming);
                    // reset the read buttons if the mode changes
                    enableReadButtons();
                    if (noDecoder) {
                        writeChangesButton.setEnabled(false);
                        writeAllButton.setEnabled(false);
                    }
                } else {
                    log.debug("Skipping programmer setup because found no programmer element");
                }

            } else {
                log.error("Can't set programming mode, no programmer instance");
            }
        }

        // and build the GUI (after programmer mode because it depends on what's available)
        loadProgrammerFile(pRosterEntry);

        // optionally, add extra panes from the decoder file
        Attribute a;
        if ((a = programmerRoot.getChild("programmer").getAttribute("decoderFilePanes")) != null
                && a.getValue().equals("yes")) {
            if (decoderRoot != null) {
                if (log.isDebugEnabled()) {
                    log.debug("will process {} pane definitions from decoder file", decoderPaneList.size());
                }
                for (Element element : decoderPaneList) {
                    // load each pane
                    String pname = jmri.util.jdom.LocaleSelector.getAttribute(element, "name");

                    // handle include/exclude
                    if (isIncludedFE(element, modelElem, _rosterEntry, "", "")) {
                        newPane(pname, element, modelElem, true, false);  // show even if empty not a programmer pane
                        log.debug("PaneProgFrame init - pane {} added", pname); // these are MISSING in RosterPrint
                    }
                }
            }
        }

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        tempPane.add(bottom, BorderLayout.SOUTH);

        // now that programmer is configured, set the programming GUI
        setProgrammingGui(bottom);

        // add the search GUI
        setSearchGui(bottom);

        pack();

        if (log.isDebugEnabled()) {  // because size elements take time
            log.debug("PaneProgFrame \"{}\" constructed for file {}, unconstrained size is {}, constrained to {}",
                    pFrameEntryId, _rosterEntry.getFileName(), super.getPreferredSize(), getPreferredSize());
        }
    }

    /**
     * Front end to DecoderFile.isIncluded()
     * <ul>
     * <li>Retrieves "productID" and "model attributes from the "model" element
     * and "family" attribute from the roster entry. </li>
     * <li>Then invokes DecoderFile.isIncluded() with the retrieved values.</li>
     * <li>Deals gracefully with null or missing elements and
     * attributes.</li>
     * </ul>
     *
     * @param e             XML element with possible "include" and "exclude"
     *                      attributes to be checked
     * @param aModelElement "model" element from the Decoder Index, used to get
     *                      "model" and "productID".
     * @param aRosterEntry  The current roster entry, used to get "family".
     * @param extraIncludes additional "include" terms
     * @param extraExcludes additional "exclude" terms.
     * @return true if front ended included, else false.
     */
    public static boolean isIncludedFE(Element e, Element aModelElement, RosterEntry aRosterEntry, String extraIncludes, String extraExcludes) {

        String pID;
        try {
            pID = aModelElement.getAttribute("productID").getValue();
        } catch (Exception ex) {
            pID = null;
        }

        String modelName;
        try {
            modelName = aModelElement.getAttribute("model").getValue();
        } catch (Exception ex) {
            modelName = null;
        }

        String familyName;
        try {
            familyName = aRosterEntry.getDecoderFamily();
        } catch (Exception ex) {
            familyName = null;
        }
        return DecoderFile.isIncluded(e, pID, modelName, familyName, extraIncludes, extraExcludes);
    }

    protected void pickProgrammerMode(@Nonnull Element programming) {
        log.debug("pickProgrammerMode starts");
        boolean paged = true;
        boolean directbit = true;
        boolean directbyte = true;
        boolean register = true;

        Attribute a;

        // set the programming attributes for DCC
        if ((a = programming.getAttribute("nodecoder")) != null) {
            if (a.getValue().equals("yes")) {
                noDecoder = true;   // No decoder in the loco
            }
        }
        if ((a = programming.getAttribute("paged")) != null) {
            if (a.getValue().equals("no")) {
                paged = false;
            }
        }
        if ((a = programming.getAttribute("direct")) != null) {
            if (a.getValue().equals("no")) {
                directbit = false;
                directbyte = false;
            } else if (a.getValue().equals("bitOnly")) {
                //directbit = true;
                directbyte = false;
            } else if (a.getValue().equals("byteOnly")) {
                directbit = false;
                //directbyte = true;
            //} else { // items already have these values
                //directbit = true;
                //directbyte = true;
            }
        }
        if ((a = programming.getAttribute("register")) != null) {
            if (a.getValue().equals("no")) {
                register = false;
            }
        }

        // find an accepted mode to set it to
        List<ProgrammingMode> modes = mProgrammer.getSupportedModes();

        if (log.isDebugEnabled()) {
            log.debug("XML specifies modes: P {} DBi {} Dby {} R {} now {}", paged, directbit, directbyte, register, mProgrammer.getMode());
            log.debug("Programmer supports:");
            for (ProgrammingMode m : modes) {
                log.debug(" mode: {} {}", m.getStandardName(), m);
            }
        }

        StringBuilder desiredModes = new StringBuilder();
        // first try specified modes
        for (Element el1 : programming.getChildren("mode")) {
            String name = el1.getText();
            if (desiredModes.length() > 0) desiredModes.append(", ");
            desiredModes.append(name);
            log.debug(" mode {} was specified", name);
            for (ProgrammingMode m : modes) {
                if (name.equals(m.getStandardName())) {
                    log.info("Programming mode selected: {} ({})", m, m.getStandardName());
                    mProgrammer.setMode(m);
                    return;
                }
            }
        }

        // go through historical modes
        if (modes.contains(ProgrammingMode.DIRECTMODE) && directbit && directbyte) {
            mProgrammer.setMode(ProgrammingMode.DIRECTMODE);
            log.debug("Set to DIRECTMODE");
        } else if (modes.contains(ProgrammingMode.DIRECTBITMODE) && directbit) {
            mProgrammer.setMode(ProgrammingMode.DIRECTBITMODE);
            log.debug("Set to DIRECTBITMODE");
        } else if (modes.contains(ProgrammingMode.DIRECTBYTEMODE) && directbyte) {
            mProgrammer.setMode(ProgrammingMode.DIRECTBYTEMODE);
            log.debug("Set to DIRECTBYTEMODE");
        } else if (modes.contains(ProgrammingMode.PAGEMODE) && paged) {
            mProgrammer.setMode(ProgrammingMode.PAGEMODE);
            log.debug("Set to PAGEMODE");
        } else if (modes.contains(ProgrammingMode.REGISTERMODE) && register) {
            mProgrammer.setMode(ProgrammingMode.REGISTERMODE);
            log.debug("Set to REGISTERMODE");
        } else if (noDecoder) {
            log.debug("No decoder");
        } else {
            JmriJOptionPane.showMessageDialog(
                    this,
                    Bundle.getMessage("ErrorCannotSetMode", desiredModes.toString()),
                    Bundle.getMessage("ErrorCannotSetModeTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
            log.warn("No acceptable mode found, leave as found");
        }
    }

    /**
     * Data element holding the 'model' element representing the decoder type.
     */
    Element modelElem = null;

    Element decoderRoot = null;

    protected void loadDecoderFromLoco(RosterEntry r) {
        // get a DecoderFile from the locomotive xml
        String decoderModel = r.getDecoderModel();
        String decoderFamily = r.getDecoderFamily();
        log.debug("selected loco uses decoder {} {}", decoderFamily, decoderModel);

        // locate a decoder like that.
        List<DecoderFile> l = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, decoderFamily, null, null, null, decoderModel);
        log.debug("found {} matches", l.size());
        if (l.size() == 0) {
            log.debug("Loco uses {} {} decoder, but no such decoder defined", decoderFamily, decoderModel);
            // fall back to use just the decoder name, not family
            l = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, null, null, null, null, decoderModel);
            if (log.isDebugEnabled()) {
                log.debug("found {} matches without family key", l.size());
            }
        }
        if (l.size() > 0) {
            DecoderFile d = l.get(0);
            loadDecoderFile(d, r);
        } else {
            if (decoderModel.equals("")) {
                log.debug("blank decoderModel requested, so nothing loaded");
            } else {
                log.warn("no matching \"{}\" decoder found for loco, no decoder info loaded", decoderModel);
            }
        }
    }

    protected void loadDecoderFile(@Nonnull DecoderFile df, @Nonnull RosterEntry re) {
        if (log.isDebugEnabled()) {
            log.debug("loadDecoderFile from {} {}", DecoderFile.fileLocation, df.getFileName());
        }

        try {
            decoderRoot = df.rootFromName(DecoderFile.fileLocation + df.getFileName());
        } catch (org.jdom2.JDOMException e) {
            log.error("Exception while parsing decoder XML file: {}", df.getFileName(), e);
            return;
        } catch (java.io.IOException e) {
            log.error("Exception while reading decoder XML file: {}", df.getFileName(), e);
            return;
        }
        // load variables from decoder tree
        df.getProductID();
        df.loadVariableModel(decoderRoot.getChild("decoder"), variableModel);

        // load reset from decoder tree
        df.loadResetModel(decoderRoot.getChild("decoder"), resetModel);

        // load extra menus from decoder tree
        df.loadExtraMenuModel(decoderRoot.getChild("decoder"), extraMenuModelList, progStatus, mProgrammer);

        // add extra menus
        log.trace("add menus {} {}", extraMenuModelList.size(), extraMenuList);
        for (int i=0; i < extraMenuModelList.size(); i++ ) {
            String name = extraMenuModelList.get(i).getName();
            JMenu menu = new JMenu(name);
            extraMenuList.add(i, menu);
            menuBar.add(menu);
            menu.add(new ExtraMenuAction(name, extraMenuModelList.get(i), this));
            menu.setEnabled(false);
        }

        // add Window and Help menu items (_after_ the extra menus)
        addHelp();

        // load function names from family
        re.loadFunctions(decoderRoot.getChild("decoder").getChild("family").getChild("functionlabels"), "family");

        // load sound names from family
        re.loadSounds(decoderRoot.getChild("decoder").getChild("family").getChild("soundlabels"), "family");

        // get the showEmptyPanes attribute, if yes/no update our state
        if (decoderRoot.getAttribute("showEmptyPanes") != null) {
            log.debug("Found in decoder showEmptyPanes={}", decoderRoot.getAttribute("showEmptyPanes").getValue());
            decoderShowEmptyPanes = decoderRoot.getAttribute("showEmptyPanes").getValue();
        } else {
            decoderShowEmptyPanes = "";
        }
        log.debug("decoderShowEmptyPanes={}", decoderShowEmptyPanes);

        // get the suppressFunctionLabels attribute, if yes/no update our state
        if (decoderRoot.getAttribute("suppressFunctionLabels") != null) {
            log.debug("Found in decoder suppressFunctionLabels={}", decoderRoot.getAttribute("suppressFunctionLabels").getValue());
            suppressFunctionLabels = decoderRoot.getAttribute("suppressFunctionLabels").getValue();
        } else {
            suppressFunctionLabels = "";
        }
        log.debug("suppressFunctionLabels={}", suppressFunctionLabels);

        // get the suppressRosterMedia attribute, if yes/no update our state
        if (decoderRoot.getAttribute("suppressRosterMedia") != null) {
            log.debug("Found in decoder suppressRosterMedia={}", decoderRoot.getAttribute("suppressRosterMedia").getValue());
            suppressRosterMedia = decoderRoot.getAttribute("suppressRosterMedia").getValue();
        } else {
            suppressRosterMedia = "";
        }
        log.debug("suppressRosterMedia={}", suppressRosterMedia);

        // get the allowResetDefaults attribute, if yes/no update our state
        if (decoderRoot.getAttribute("allowResetDefaults") != null) {
            log.debug("Found in decoder allowResetDefaults={}", decoderRoot.getAttribute("allowResetDefaults").getValue());
            decoderAllowResetDefaults = decoderRoot.getAttribute("allowResetDefaults").getValue();
        } else {
            decoderAllowResetDefaults = "yes";
        }
        log.debug("decoderAllowResetDefaults={}", decoderAllowResetDefaults);

        // save the pointer to the model element
        modelElem = df.getModelElement();

        // load function names from model
        re.loadFunctions(modelElem.getChild("functionlabels"), "model");

        // load sound names from model
        re.loadSounds(modelElem.getChild("soundlabels"), "model");

        // load maxFnNum from model
        Attribute a;
        if ((a = modelElem.getAttribute("maxFnNum")) != null) {
            maxFnNumOld = re.getMaxFnNum();
            maxFnNumNew = a.getValue();
            if (!maxFnNumOld.equals(maxFnNumNew)) {
                if (!re.getId().equals(Bundle.getMessage("LabelNewDecoder"))) {
                    maxFnNumDirty = true;
                    log.info("maxFnNum for \"{}\" changed from {} to {}", re.getId(), maxFnNumOld, maxFnNumNew);
                    String message = java.text.MessageFormat.format(
                            SymbolicProgBundle.getMessage("StatusMaxFnNumUpdated"),
                            re.getDecoderFamily(), re.getDecoderModel(), maxFnNumNew);
                    progStatus.setText(message);
                }
                re.setMaxFnNum(maxFnNumNew);
            }
        }
    }

    protected void loadProgrammerFile(RosterEntry r) {
        // Open and parse programmer file
        XmlFile pf = new XmlFile() {
        };  // XmlFile is abstract
        try {
            programmerRoot = pf.rootFromName(filename);

            // get the showEmptyPanes attribute, if yes/no update our state
            if (programmerRoot.getChild("programmer").getAttribute("showEmptyPanes") != null) {
                programmerShowEmptyPanes = programmerRoot.getChild("programmer").getAttribute("showEmptyPanes").getValue();
                log.debug("Found in programmer {}", programmerShowEmptyPanes);
            } else {
                programmerShowEmptyPanes = "";
            }

            // get extra any panes from the programmer file
            Attribute a;
            if ((a = programmerRoot.getChild("programmer").getAttribute("decoderFilePanes")) != null
                    && a.getValue().equals("yes")) {
                if (decoderRoot != null) {
                    decoderPaneList = decoderRoot.getChildren("pane");
                }
            }

            // load programmer config from programmer tree
            readConfig(programmerRoot, r);

        } catch (org.jdom2.JDOMException e) {
            log.error("exception parsing programmer file: {}", filename, e);
        } catch (java.io.IOException e) {
            log.error("exception reading programmer file: {}", filename, e);
        }
    }

    Element programmerRoot = null;

    /**
     * @return true if decoder needs to be written
     */
    protected boolean checkDirtyDecoder() {
        if (log.isDebugEnabled()) {
            log.debug("Checking decoder dirty status. CV: {} variables:{}", cvModel.decoderDirty(), variableModel.decoderDirty());
        }
        return (getModePane() != null && (cvModel.decoderDirty() || variableModel.decoderDirty()));
    }

    /**
     * @return true if file needs to be written
     */
    protected boolean checkDirtyFile() {
        return (variableModel.fileDirty() || _rPane.guiChanged(_rosterEntry) || _flPane.guiChanged(_rosterEntry) || _rMPane.guiChanged(_rosterEntry) || maxFnNumDirty);
    }

    protected void handleDirtyFile() {
    }

    /**
     * Close box has been clicked; handle check for dirty with respect to
     * decoder or file, then close.
     *
     * @param e Not used
     */
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {

        // Don't want to actually close if we return early
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // check for various types of dirty - first table data not written back
        if (log.isDebugEnabled()) {
            log.debug("Checking decoder dirty status. CV: {} variables:{}", cvModel.decoderDirty(), variableModel.decoderDirty());
        }
        if (!noDecoder && checkDirtyDecoder()) {
            if (JmriJOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("PromptCloseWindowNotWrittenDecoder"),
                    Bundle.getMessage("PromptChooseOne"),
                    JmriJOptionPane.OK_CANCEL_OPTION) != JmriJOptionPane.OK_OPTION) {
                return;
            }
        }
        if (checkDirtyFile()) {
            int option = JmriJOptionPane.showOptionDialog(this, Bundle.getMessage("PromptCloseWindowNotWrittenConfig"),
                    Bundle.getMessage("PromptChooseOne"),
                    JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.WARNING_MESSAGE, null,
                    new String[]{Bundle.getMessage("PromptSaveAndClose"), Bundle.getMessage("PromptClose"), Bundle.getMessage("ButtonCancel")},
                    Bundle.getMessage("PromptSaveAndClose"));
            if (option == 0) { // array position 0 PromptSaveAndClose
                // save requested
                if (!storeFile()) {
                    return;   // don't close if failed
                }
            } else if (option == 2 || option == JmriJOptionPane.CLOSED_OPTION ) {
                // cancel requested or Dialog closed
                return; // without doing anything
            }
        }
        if(maxFnNumDirty && !maxFnNumOld.equals("")){
            _rosterEntry.setMaxFnNum(maxFnNumOld);
        }
        // Check for a "<new loco>" roster entry; if found, remove it
        List<RosterEntry> l = Roster.getDefault().matchingList(null, null, null, null, null, null, Bundle.getMessage("LabelNewDecoder"));
        if (l.size() > 0 && log.isDebugEnabled()) {
            log.debug("Removing {} <new loco> entries", l.size());
        }
        int x = l.size() + 1;
        while (l.size() > 0) {
            Roster.getDefault().removeEntry(l.get(0));
            l = Roster.getDefault().matchingList(null, null, null, null, null, null, Bundle.getMessage("LabelNewDecoder"));
            x--;
            if (x == 0) {
                log.error("We have tried to remove all the entries, however an error has occurred which has resulted in the entries not being deleted correctly");
                l = new ArrayList<>();
            }
        }

        // OK, continue close
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // deregister shutdown hooks
        jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(decoderDirtyTask);
        decoderDirtyTask = null;
        jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(fileDirtyTask);
        fileDirtyTask = null;

        // do the close itself
        super.windowClosing(e);
    }

    void readConfig(Element root, RosterEntry r) {
         // check for "programmer" element at start
        Element base;
        if ((base = root.getChild("programmer")) == null) {
            log.error("xml file top element is not programmer");
            return;
        }

        // add the Info tab
        if (root.getChild("programmer").getAttribute("showRosterPane") != null) {
            if (root.getChild("programmer").getAttribute("showRosterPane").getValue().equals("no")) {
                makeInfoPane(r);
            } else {
                tabPane.addTab(Bundle.getMessage("ROSTER ENTRY"), makeInfoPane(r));
            }
        } else {
            tabPane.addTab(Bundle.getMessage("ROSTER ENTRY"), makeInfoPane(r));
        }

        // add the Function Label tab
        if (root.getChild("programmer").getAttribute("showFnLanelPane").getValue().equals("yes")
                && !suppressFunctionLabels.equals("yes")
            ) {
            tabPane.addTab(Bundle.getMessage("FUNCTION LABELS"), makeFunctionLabelPane(r));
        } else {
            // make it, just don't make it visible
            makeFunctionLabelPane(r);
        }

        // add the Media tab
        if (root.getChild("programmer").getAttribute("showRosterMediaPane").getValue().equals("yes")
                && !suppressRosterMedia.equals("yes")
            ) {
            tabPane.addTab(Bundle.getMessage("ROSTER MEDIA"), makeMediaPane(r));
        } else {
            // create it, just don't make it visible
            makeMediaPane(r);
        }

        // for all "pane" elements in the programmer
        List<Element> progPaneList = base.getChildren("pane");
        if (log.isDebugEnabled()) {
            log.debug("will process {} pane definitions", progPaneList.size());
        }
        for (Element temp : progPaneList) {
            // load each programmer pane
            List<Element> pnames = temp.getChildren("name");
            boolean isProgPane = true;
            if ((pnames.size() > 0) && (decoderPaneList != null) && (decoderPaneList.size() > 0)) {
                String namePrimary = (pnames.get(0)).getValue(); // get non-localised name

                // check if there is a same-name pane in decoder file
                // start at end to prevent concurrentmodification exception on remove
                for (int j = decoderPaneList.size() - 1; j >= 0; j--) {
                    List<Element> dnames = decoderPaneList.get(j).getChildren("name");
                    if (dnames.size() > 0) {
                        String namePrimaryDecoder = (dnames.get(0)).getValue(); // get non-localised name
                        if (namePrimary.equals(namePrimaryDecoder)) {
                            // replace programmer pane with same-name decoder pane
                            temp = decoderPaneList.get(j);
                            decoderPaneList.remove(j); // safe, not suspicious as we work end - front
                            isProgPane = false;
                        }
                    }
                }
            }
            String name = jmri.util.jdom.LocaleSelector.getAttribute(temp, "name");

            // handle include/exclude
            if (isIncludedFE(temp, modelElem, _rosterEntry, "", "")) {
                newPane(name, temp, modelElem, false, isProgPane);  // don't force showing if empty
                log.debug("readConfig - pane {} added", name); // these are also in RosterPrint
            }
        }
    }

    /**
     * Reset all CV values to defaults stored earlier.
     * <p>
     * This will in turn update the variables.
     */
    protected void resetToDefaults() {
        int n = defaultCvValues.length;
        for (int i = 0; i < n; i++) {
            CvValue cv = cvModel.getCvByNumber(defaultCvNumbers[i]);
            if (cv == null) {
                log.warn("Trying to set default in CV {} but didn't find the CV object", defaultCvNumbers[i]);
            } else {
                cv.setValue(defaultCvValues[i]);
            }
        }
    }

    int[] defaultCvValues = null;
    String[] defaultCvNumbers = null;

    /**
     * Save all CV values.
     * <p>
     * These stored values are used by {link #resetToDefaults()}
     */
    protected void saveDefaults() {
        int n = cvModel.getRowCount();
        defaultCvValues = new int[n];
        defaultCvNumbers = new String[n];

        for (int i = 0; i < n; i++) {
            CvValue cv = cvModel.getCvByRow(i);
            defaultCvValues[i] = cv.getValue();
            defaultCvNumbers[i] = cv.number();
        }
    }

    protected JPanel makeInfoPane(RosterEntry r) {
        // create the identification pane (not configured by programmer file now; maybe later?)

        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(body);

        // add roster info
        _rPane = new RosterEntryPane(r);
        _rPane.setMaximumSize(_rPane.getPreferredSize());
        body.add(_rPane);

        // add the store button
        JButton store = new JButton(Bundle.getMessage("ButtonSave"));
        store.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        store.addActionListener(e -> storeFile());

        // add the reset button
        JButton reset = new JButton(Bundle.getMessage("ButtonResetDefaults"));
        reset.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        if (decoderAllowResetDefaults.equals("no")) {
            reset.setEnabled(false);
            reset.setToolTipText(Bundle.getMessage("TipButtonResetDefaultsDisabled"));
        } else {
            reset.setToolTipText(Bundle.getMessage("TipButtonResetDefaults"));
            reset.addActionListener(e -> resetToDefaults());
        }

        int sizeX = Math.max(reset.getPreferredSize().width, store.getPreferredSize().width);
        int sizeY = Math.max(reset.getPreferredSize().height, store.getPreferredSize().height);
        store.setPreferredSize(new Dimension(sizeX, sizeY));
        reset.setPreferredSize(new Dimension(sizeX, sizeY));

        store.setToolTipText(_rosterEntry.getFileName());

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        buttons.add(store);
        buttons.add(reset);

        body.add(buttons);
        outer.add(scrollPane);

        // arrange for the dcc address to be updated
        java.beans.PropertyChangeListener dccNews = e -> updateDccAddress();
        primaryAddr = variableModel.findVar("Short Address");
        if (primaryAddr == null) {
            log.debug("DCC Address monitor didn't find a Short Address variable");
        } else {
            primaryAddr.addPropertyChangeListener(dccNews);
        }
        extendAddr = variableModel.findVar("Long Address");
        if (extendAddr == null) {
            log.debug("DCC Address monitor didn't find an Long Address variable");
        } else {
            extendAddr.addPropertyChangeListener(dccNews);
        }
        addMode = (EnumVariableValue) variableModel.findVar("Address Format");
        if (addMode == null) {
            log.debug("DCC Address monitor didn't find an Address Format variable");
        } else {
            addMode.addPropertyChangeListener(dccNews);
        }

        // get right address to start
        updateDccAddress();

        return outer;
    }

    protected JPanel makeFunctionLabelPane(RosterEntry r) {
        // create the identification pane (not configured by programmer file now; maybe later?)

        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(body);

        // add tab description
        JLabel title = new JLabel(Bundle.getMessage("UseThisTabCustomize"));
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        body.add(title);
        body.add(new JLabel(" ")); // some padding

        // add roster info
        _flPane = new FunctionLabelPane(r);
        //_flPane.setMaximumSize(_flPane.getPreferredSize());
        body.add(_flPane);

        // add the store button
        JButton store = new JButton(Bundle.getMessage("ButtonSave"));
        store.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        store.addActionListener(e -> storeFile());

        store.setToolTipText(_rosterEntry.getFileName());

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        buttons.add(store);

        body.add(buttons);
        outer.add(scrollPane);
        return outer;
    }

    protected JPanel makeMediaPane(RosterEntry r) {
        // create the identification pane (not configured by programmer file now; maybe later?)
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(body);

        // add tab description
        JLabel title = new JLabel(Bundle.getMessage("UseThisTabMedia"));
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        body.add(title);
        body.add(new JLabel(" ")); // some padding

        // add roster info
        _rMPane = new RosterMediaPane(r);
        _rMPane.setMaximumSize(_rMPane.getPreferredSize());
        body.add(_rMPane);

        // add the store button
        JButton store = new JButton(Bundle.getMessage("ButtonSave"));
        store.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        store.addActionListener(e -> storeFile());

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        buttons.add(store);

        body.add(buttons);
        outer.add(scrollPane);
        return outer;
    }

    // hold refs to variables to check dccAddress
    VariableValue primaryAddr = null;
    VariableValue extendAddr = null;
    EnumVariableValue addMode = null;

    boolean longMode = false;
    String newAddr = null;

    void updateDccAddress() {

        if (log.isDebugEnabled()) {
            log.debug("updateDccAddress: short {} long {} mode {}", primaryAddr == null ? "<null>" : primaryAddr.getValueString(), extendAddr == null ? "<null>" : extendAddr.getValueString(), addMode == null ? "<null>" : addMode.getValueString());
        }

        new DccAddressVarHandler(primaryAddr, extendAddr, addMode) {
            @Override
            protected void doPrimary() {
                // short address mode
                longMode = false;
                if (primaryAddr != null && !primaryAddr.getValueString().equals("")) {
                    newAddr = primaryAddr.getValueString();
                }
            }

            @Override
            protected void doExtended() {
                // long address
                if (extendAddr != null && !extendAddr.getValueString().equals("")) {
                    longMode = true;
                    newAddr = extendAddr.getValueString();
                }
            }
        };
        // update if needed
        if (newAddr != null) {
            // store DCC address, type
            _rPane.setDccAddress(newAddr);
            _rPane.setDccAddressLong(longMode);
        }
    }

    public void newPane(String name, Element pane, Element modelElem, boolean enableEmpty, boolean programmerPane) {
        if (log.isDebugEnabled()) {
            log.debug("newPane with enableEmpty {} showEmptyPanes {}", enableEmpty, isShowingEmptyPanes());
        }
        // create a panel to hold columns
        PaneProgPane p = new PaneProgPane(this, name, pane, cvModel, variableModel, modelElem, _rosterEntry, programmerPane);
        p.setOpaque(true);
        if (noDecoder) {
            p.setNoDecoder();
            cvModel.setNoDecoder();
        }
        // how to handle the tab depends on whether it has contents and option setting
        int index;
        if (enableEmpty || !p.cvList.isEmpty() || !p.varList.isEmpty()) {
            tabPane.addTab(name, p);  // always add if not empty
            index = tabPane.indexOfTab(name);
            tabPane.setToolTipTextAt(index, p.getToolTipText());
        } else if (isShowingEmptyPanes()) {
            // here empty, but showing anyway as disabled
            tabPane.addTab(name, p);
            index = tabPane.indexOfTab(name);
            tabPane.setEnabledAt(index, true); // need to enable the pane so user can see message
            tabPane.setToolTipTextAt(index,
                    Bundle.getMessage("TipTabEmptyNoCategory"));
        } else {
            // here not showing tab at all
            index = -1;
        }

        // remember it for programming
        paneList.add(p);

        // if visible, set qualifications
        if (index >= 0) {
            processModifierElements(pane, p, variableModel, tabPane, index);
        }
    }

    /**
     * If there are any modifier elements, process them.
     *
     * @param e Process the contents of this element
     * @param pane Destination of any visible items
     * @param model Used to locate any needed variables
     * @param tabPane For overall GUI navigation
     * @param index Which pane in the overall window
     */
    protected void processModifierElements(Element e, final PaneProgPane pane, VariableTableModel model, final JTabbedPane tabPane, final int index) {
        QualifierAdder qa = new QualifierAdder() {
            @Override
            protected Qualifier createQualifier(VariableValue var, String relation, String value) {
                return new PaneQualifier(pane, var, Integer.parseInt(value), relation, tabPane, index);
            }

            @Override
            protected void addListener(java.beans.PropertyChangeListener qc) {
                pane.addPropertyChangeListener(qc);
            }
        };

        qa.processModifierElements(e, model);
    }

    @Override
    public BusyGlassPane getBusyGlassPane() {
        return glassPane;
    }

    /**
     * Create a BusyGlassPane transparent layer over the panel blocking any
     * other interaction, excluding a supplied button.
     *
     * @param activeButton a button to put on top of the pane
     */
    @Override
    public void prepGlassPane(AbstractButton activeButton) {
        List<Rectangle> rectangles = new ArrayList<>();

        if (glassPane != null) {
            glassPane.dispose();
        }
        activeComponents.clear();
        activeComponents.add(activeButton);
        if (activeButton == readChangesButton || activeButton == readAllButton
                || activeButton == writeChangesButton || activeButton == writeAllButton) {
            if (activeButton == readChangesButton) {
                for (JPanel jPanel : paneList) {
                    assert jPanel instanceof PaneProgPane;
                    activeComponents.add(((PaneProgPane) jPanel).readChangesButton);
                }
            } else if (activeButton == readAllButton) {
                for (JPanel jPanel : paneList) {
                    assert jPanel instanceof PaneProgPane;
                    activeComponents.add(((PaneProgPane) jPanel).readAllButton);
                }
            } else if (activeButton == writeChangesButton) {
                for (JPanel jPanel : paneList) {
                    assert jPanel instanceof PaneProgPane;
                    activeComponents.add(((PaneProgPane) jPanel).writeChangesButton);
                }
            } else { // (activeButton == writeAllButton) {
                for (JPanel jPanel : paneList) {
                    assert jPanel instanceof PaneProgPane;
                    activeComponents.add(((PaneProgPane) jPanel).writeAllButton);
                }
            }

            for (int i = 0; i < tabPane.getTabCount(); i++) {
                rectangles.add(tabPane.getUI().getTabBounds(tabPane, i));
            }
        }
        glassPane = new BusyGlassPane(activeComponents, rectangles, this.getContentPane(), this);
        this.setGlassPane(glassPane);
    }

    @Override
    public void paneFinished() {
        log.debug("paneFinished with isBusy={}", isBusy());
        if (!isBusy()) {
            if (glassPane != null) {
                glassPane.setVisible(false);
                glassPane.dispose();
                glassPane = null;
            }
            setCursor(Cursor.getDefaultCursor());
            enableButtons(true);
        }
    }

    /**
     * Enable the read/write buttons.
     * <p>
     * In addition, if a programming mode pane is present, its "set" button is
     * enabled.
     *
     * @param stat Are reads possible? If false, so not enable the read buttons.
     */
    @Override
    public void enableButtons(boolean stat) {
        log.debug("enableButtons({})", stat);
        if (noDecoder) {
            // If we don't have a decoder, no read or write is possible
            stat = false;
        }
        if (stat) {
            enableReadButtons();
        } else {
            readChangesButton.setEnabled(false);
            readAllButton.setEnabled(false);
        }
        writeChangesButton.setEnabled(stat);
        writeAllButton.setEnabled(stat);
        
        var tempModePane = getModePane();
        if (tempModePane != null) {
            tempModePane.setEnabled(stat);
        }
    }

    boolean justChanges;

    @Override
    public boolean isBusy() {
        return _busy;
    }
    private boolean _busy = false;

    private void setBusy(boolean stat) {
        log.debug("setBusy({})", stat);
        _busy = stat;

        for (JPanel jPanel : paneList) {
            assert jPanel instanceof PaneProgPane;
            ((PaneProgPane) jPanel).enableButtons(!stat);
        }
        if (!stat) {
            paneFinished();
        }
    }

    /**
     * Invoked by "Read Changes" button, this sets in motion a continuing
     * sequence of "read changes" operations on the panes.
     * <p>
     * Each invocation of this method reads one pane; completion of that request
     * will cause it to happen again, reading the next pane, until there's
     * nothing left to read.
     *
     * @return true if a read has been started, false if the operation is
     *         complete.
     */
    public boolean readChanges() {
        log.debug("readChanges starts");
        justChanges = true;
        for (JPanel jPanel : paneList) {
            assert jPanel instanceof PaneProgPane;
            ((PaneProgPane) jPanel).setToRead(justChanges, true);
        }
        setBusy(true);
        enableButtons(false);
        readChangesButton.setEnabled(true);
        glassPane.setVisible(true);
        paneListIndex = 0;
        // start operation
        return doRead();
    }

    /**
     * Invoked by the "Read All" button, this sets in motion a continuing
     * sequence of "read all" operations on the panes.
     * <p>
     * Each invocation of this method reads one pane; completion of that request
     * will cause it to happen again, reading the next pane, until there's
     * nothing left to read.
     *
     * @return true if a read has been started, false if the operation is
     *         complete.
     */
    public boolean readAll() {
        log.debug("readAll starts");
        justChanges = false;
        for (JPanel jPanel : paneList) {
            assert jPanel instanceof PaneProgPane;
            ((PaneProgPane) jPanel).setToRead(justChanges, true);
        }
        setBusy(true);
        enableButtons(false);
        readAllButton.setEnabled(true);
        glassPane.setVisible(true);
        paneListIndex = 0;
        // start operation
        return doRead();
    }

    boolean doRead() {
        _read = true;
        while (paneListIndex < paneList.size()) {
            log.debug("doRead on {}", paneListIndex);
            _programmingPane = (PaneProgPane) paneList.get(paneListIndex);
            // some programming operations are instant, so need to have listener registered at readPaneAll
            _programmingPane.addPropertyChangeListener(this);
            boolean running;
            if (justChanges) {
                running = _programmingPane.readPaneChanges();
            } else {
                running = _programmingPane.readPaneAll();
            }

            paneListIndex++;

            if (running) {
                // operation in progress, stop loop until called back
                log.debug("doRead expecting callback from readPane {}", paneListIndex);
                return true;
            } else {
                _programmingPane.removePropertyChangeListener(this);
            }
        }
        // nothing to program, end politely
        _programmingPane = null;
        enableButtons(true);
        setBusy(false);
        readChangesButton.setSelected(false);
        readAllButton.setSelected(false);
        log.debug("doRead found nothing to do");
        return false;
    }

    /**
     * Invoked by "Write All" button, this sets in motion a continuing sequence
     * of "write all" operations on each pane. Each invocation of this method
     * writes one pane; completion of that request will cause it to happen
     * again, writing the next pane, until there's nothing left to write.
     *
     * @return true if a write has been started, false if the operation is
     *         complete.
     */
    public boolean writeAll() {
        log.debug("writeAll starts");
        justChanges = false;
        for (JPanel jPanel : paneList) {
            assert jPanel instanceof PaneProgPane;
            ((PaneProgPane) jPanel).setToWrite(justChanges, true);
        }
        setBusy(true);
        enableButtons(false);
        writeAllButton.setEnabled(true);
        glassPane.setVisible(true);
        paneListIndex = 0;
        return doWrite();
    }

    /**
     * Invoked by "Write Changes" button, this sets in motion a continuing
     * sequence of "write changes" operations on each pane.
     * <p>
     * Each invocation of this method writes one pane; completion of that
     * request will cause it to happen again, writing the next pane, until
     * there's nothing left to write.
     *
     * @return true if a write has been started, false if the operation is
     *         complete
     */
    public boolean writeChanges() {
        log.debug("writeChanges starts");
        justChanges = true;
        for (JPanel jPanel : paneList) {
            assert jPanel instanceof PaneProgPane;
            ((PaneProgPane) jPanel).setToWrite(justChanges, true);
        }
        setBusy(true);
        enableButtons(false);
        writeChangesButton.setEnabled(true);
        glassPane.setVisible(true);
        paneListIndex = 0;
        return doWrite();
    }

    boolean doWrite() {
        _read = false;
        while (paneListIndex < paneList.size()) {
            log.debug("doWrite starts on {}", paneListIndex);
            _programmingPane = (PaneProgPane) paneList.get(paneListIndex);
            // some programming operations are instant, so need to have listener registered at readPane
            _programmingPane.addPropertyChangeListener(this);
            boolean running;
            if (justChanges) {
                running = _programmingPane.writePaneChanges();
            } else {
                running = _programmingPane.writePaneAll();
            }

            paneListIndex++;

            if (running) {
                // operation in progress, stop loop until called back
                log.debug("doWrite expecting callback from writePane {}", paneListIndex);
                return true;
            } else {
                _programmingPane.removePropertyChangeListener(this);
            }
        }
        // nothing to program, end politely
        _programmingPane = null;
        enableButtons(true);
        setBusy(false);
        writeChangesButton.setSelected(false);
        writeAllButton.setSelected(false);
        log.debug("doWrite found nothing to do");
        return false;
    }

    /**
     * Prepare a roster entry to be printed, and display a selection list.
     *
     * @see jmri.jmrit.roster.PrintRosterEntry#doPrintPanes(boolean)
     * @param preview true if output should go to a Preview pane on screen,
     *                false to output to a printer (dialog)
     */
    public void printPanes(final boolean preview) {
        PrintRosterEntry pre = new PrintRosterEntry(_rosterEntry, paneList, _flPane, _rMPane, this);
        pre.printPanes(preview);
    }

    boolean _read = true;
    PaneProgPane _programmingPane = null;

    /**
     * Get notification of a variable property change in the pane, specifically
     * "busy" going to false at the end of a programming operation.
     *
     * @param e Event, used to find source
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // check for the right event
        if (_programmingPane == null) {
            log.warn("unexpected propertyChange: {}", e);
            return;
        } else if (log.isDebugEnabled()) {
            log.debug("property changed: {} new value: {}", e.getPropertyName(), e.getNewValue());
        }
        log.debug("check valid: {} {} {}", e.getSource() == _programmingPane, !e.getPropertyName().equals("Busy"), e.getNewValue().equals(Boolean.FALSE));
        if (e.getSource() == _programmingPane
                && e.getPropertyName().equals("Busy")
                && e.getNewValue().equals(Boolean.FALSE)) {

            log.debug("end of a programming pane operation, remove");
            // remove existing listener
            _programmingPane.removePropertyChangeListener(this);
            _programmingPane = null;
            // restart the operation
            if (_read && readChangesButton.isSelected()) {
                log.debug("restart readChanges");
                doRead();
            } else if (_read && readAllButton.isSelected()) {
                log.debug("restart readAll");
                doRead();
            } else if (writeChangesButton.isSelected()) {
                log.debug("restart writeChanges");
                doWrite();
            } else if (writeAllButton.isSelected()) {
                log.debug("restart writeAll");
                doWrite();
            } else {
                log.debug("read/write end because button is lifted");
                setBusy(false);
            }
        }
    }

    /**
     * Store the locomotives information in the roster (and a RosterEntry file).
     *
     * @return false if store failed
     */
    public boolean storeFile() {
        log.debug("storeFile starts");

        if (_rPane.checkDuplicate()) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorDuplicateID"));
            return false;
        }

        // reload the RosterEntry
        updateDccAddress();
        _rPane.update(_rosterEntry);
        _flPane.update(_rosterEntry);
        _rMPane.update(_rosterEntry);

        // id has to be set!
        if (_rosterEntry.getId().equals("") || _rosterEntry.getId().equals(Bundle.getMessage("LabelNewDecoder"))) {
            log.debug("storeFile without a filename; issued dialog");
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("PromptFillInID"));
            return false;
        }

        // if there isn't a filename, store using the id
        _rosterEntry.ensureFilenameExists();
        String filename = _rosterEntry.getFileName();

        // create the RosterEntry to its file
        _rosterEntry.writeFile(cvModel, variableModel);

        // mark this as a success
        variableModel.setFileDirty(false);
        maxFnNumDirty = false;

        // and store an updated roster file
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        Roster.getDefault().writeRoster();

        // save date changed, update
        _rPane.updateGUI(_rosterEntry);

        // show OK status
        progStatus.setText(java.text.MessageFormat.format(
                Bundle.getMessage("StateSaveOK"), filename));
        return true;
    }

    /**
     * Local dispose, which also invokes parent. Note that we remove the
     * components (removeAll) before taking those apart.
     */
    @Override
    public void dispose() {
        log.debug("dispose local");

        // remove listeners (not much of a point, though)
        readChangesButton.removeItemListener(l1);
        writeChangesButton.removeItemListener(l2);
        readAllButton.removeItemListener(l3);
        writeAllButton.removeItemListener(l4);
        if (_programmingPane != null) {
            _programmingPane.removePropertyChangeListener(this);
        }

        // dispose the list of panes
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < paneList.size(); i++) {
            PaneProgPane p = (PaneProgPane) paneList.get(i);
            p.dispose();
        }
        paneList.clear();

        // dispose of things we owned, in order of dependence
        _rPane.dispose();
        _flPane.dispose();
        _rMPane.dispose();
        variableModel.dispose();
        cvModel.dispose();
        if (_rosterEntry != null) {
            _rosterEntry.setOpen(false);
        }

        // remove references to everything we remember
        progStatus = null;
        cvModel = null;
        variableModel = null;
        _rosterEntry = null;
        _rPane = null;
        _flPane = null;
        _rMPane = null;

        paneList.clear();
        paneList = null;
        _programmingPane = null;

        tabPane = null;
        readChangesButton = null;
        writeChangesButton = null;
        readAllButton = null;
        writeAllButton = null;

        log.debug("dispose superclass");
        removeAll();
        super.dispose();
    }

    /**
     * Set value of Preference option to show empty panes.
     *
     * @param yes true if empty panes should be shown
     */
    public static void setShowEmptyPanes(boolean yes) {
        if (InstanceManager.getNullableDefault(ProgrammerConfigManager.class) != null) {
            InstanceManager.getDefault(ProgrammerConfigManager.class).setShowEmptyPanes(yes);
        }
    }

    /**
     * Get value of Preference option to show empty panes.
     *
     * @return value from programmer config. manager, else true.
     */
    public static boolean getShowEmptyPanes() {
        return InstanceManager.getNullableDefault(ProgrammerConfigManager.class) == null ||
                InstanceManager.getDefault(ProgrammerConfigManager.class).isShowEmptyPanes();
    }

    /**
     * Get value of whether current item should show empty panes.
     */
    private boolean isShowingEmptyPanes() {
        boolean temp = getShowEmptyPanes();
        if (programmerShowEmptyPanes.equals("yes")) {
            temp = true;
        } else if (programmerShowEmptyPanes.equals("no")) {
            temp = false;
        }
        if (decoderShowEmptyPanes.equals("yes")) {
            temp = true;
        } else if (decoderShowEmptyPanes.equals("no")) {
            temp = false;
        }
        return temp;
    }

    /**
     * Option to control appearance of CV numbers in tool tips.
     *
     * @param yes true is CV numbers should be shown
     */
    public static void setShowCvNumbers(boolean yes) {
        if (InstanceManager.getNullableDefault(ProgrammerConfigManager.class) != null) {
            InstanceManager.getDefault(ProgrammerConfigManager.class).setShowCvNumbers(yes);
        }
    }

    public static boolean getShowCvNumbers() {
        return InstanceManager.getNullableDefault(ProgrammerConfigManager.class) == null ||
                InstanceManager.getDefault(ProgrammerConfigManager.class).isShowCvNumbers();
    }

    public static void setCanCacheDefault(boolean yes) {
        if (InstanceManager.getNullableDefault(ProgrammerConfigManager.class) != null) {
            InstanceManager.getDefault(ProgrammerConfigManager.class).setCanCacheDefault(yes);
        }
    }

    public static boolean getCanCacheDefault() {
        return InstanceManager.getNullableDefault(ProgrammerConfigManager.class) == null ||
                InstanceManager.getDefault(ProgrammerConfigManager.class).isCanCacheDefault();
    }

    public static void setDoConfirmRead(boolean yes) {
        if (InstanceManager.getNullableDefault(ProgrammerConfigManager.class) != null) {
            InstanceManager.getDefault(ProgrammerConfigManager.class).setDoConfirmRead(yes);
        }
    }

    public static boolean getDoConfirmRead() {
        return InstanceManager.getNullableDefault(ProgrammerConfigManager.class) == null ||
                InstanceManager.getDefault(ProgrammerConfigManager.class).isDoConfirmRead();
    }

    public RosterEntry getRosterEntry() {
        return _rosterEntry;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaneProgFrame.class);

}
