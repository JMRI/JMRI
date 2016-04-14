package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.ShutDownTask;
import jmri.implementation.swing.SwingShutDownTask;
import jmri.jmrit.XmlFile;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.FunctionLabelPane;
import jmri.jmrit.roster.PrintRosterEntry;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntryPane;
import jmri.jmrit.roster.RosterMediaPane;
import jmri.jmrit.symbolicprog.CsvExportAction;
import jmri.jmrit.symbolicprog.CsvImportAction;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.DccAddressVarHandler;
import jmri.jmrit.symbolicprog.EnumVariableValue;
import jmri.jmrit.symbolicprog.FactoryResetAction;
import jmri.jmrit.symbolicprog.IndexedCvTableModel;
import jmri.jmrit.symbolicprog.LokProgImportAction;
import jmri.jmrit.symbolicprog.Pr1ExportAction;
import jmri.jmrit.symbolicprog.Pr1ImportAction;
import jmri.jmrit.symbolicprog.Pr1WinExportAction;
import jmri.jmrit.symbolicprog.PrintAction;
import jmri.jmrit.symbolicprog.PrintCvAction;
import jmri.jmrit.symbolicprog.ProgrammerConfigManager;
import jmri.jmrit.symbolicprog.Qualifier;
import jmri.jmrit.symbolicprog.QualifierAdder;
import jmri.jmrit.symbolicprog.QuantumCvMgrImportAction;
import jmri.jmrit.symbolicprog.ResetTableModel;
import jmri.jmrit.symbolicprog.SymbolicProgBundle;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrit.symbolicprog.VariableValue;
import jmri.managers.DefaultProgrammerManager;
import jmri.util.BusyGlassPane;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame providing a command station programmer from decoder definition files.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2004, 2005, 2008, 2014
 * @author D Miller Copyright 2003, 2005
 * @author Howard G. Penny Copyright (C) 2005
 */
abstract public class PaneProgFrame extends JmriJFrame
        implements java.beans.PropertyChangeListener, PaneContainer {

    // members to contain working variable, CV values, Indexed CV values
    JLabel progStatus = new JLabel(SymbolicProgBundle.getMessage("StateIdle"));
    CvTableModel cvModel = null;
    IndexedCvTableModel iCvModel = null;
    VariableTableModel variableModel;

    ResetTableModel resetModel = null;
    JMenu resetMenu = null;

    Programmer mProgrammer;
    JPanel modePane = null;

    JPanel tempPane; // passed around during construction

    boolean _opsMode;

    RosterEntry _rosterEntry = null;
    RosterEntryPane _rPane = null;
    FunctionLabelPane _flPane = null;
    RosterMediaPane _rMPane = null;

    List<JPanel> paneList = new ArrayList<JPanel>();
    int paneListIndex;

    List<Element> decoderPaneList;

    BusyGlassPane glassPane;
    List<JComponent> activeComponents = new ArrayList<JComponent>();

    String filename = null;
    String programmerShowEmptyPanes = "";
    String decoderShowEmptyPanes = "";

    // GUI member declarations
    JTabbedPane tabPane = new JTabbedPane();
    JToggleButton readChangesButton = new JToggleButton(SymbolicProgBundle.getMessage("ButtonReadChangesAllSheets"));
    JToggleButton writeChangesButton = new JToggleButton(SymbolicProgBundle.getMessage("ButtonWriteChangesAllSheets"));
    JToggleButton readAllButton = new JToggleButton(SymbolicProgBundle.getMessage("ButtonReadAllSheets"));
    JToggleButton writeAllButton = new JToggleButton(SymbolicProgBundle.getMessage("ButtonWriteAllSheets"));

    ItemListener l1;
    ItemListener l2;
    ItemListener l3;
    ItemListener l4;

    ShutDownTask decoderDirtyTask;
    ShutDownTask fileDirtyTask;

    /**
     * Abstract method to provide a JPanel setting the programming mode, if
     * appropriate. A null value is ignored.
     */
    abstract protected JPanel getModePane();

    protected void installComponents() {

        // create ShutDownTasks
        if (jmri.InstanceManager.shutDownManagerInstance() != null) {

            if (decoderDirtyTask == null) {
                decoderDirtyTask
                        = new SwingShutDownTask("DecoderPro Decoder Window Check",
                                SymbolicProgBundle.getMessage("PromptQuitWindowNotWrittenDecoder"),
                                (String) null, this
                        ) {
                            public boolean checkPromptNeeded() {
                                return !checkDirtyDecoder();
                            }
                        };
            }
            jmri.InstanceManager.shutDownManagerInstance().register(decoderDirtyTask);
            if (fileDirtyTask == null) {
                fileDirtyTask
                        = new SwingShutDownTask("DecoderPro Decoder Window Check",
                                SymbolicProgBundle.getMessage("PromptQuitWindowNotWrittenConfig"),
                                SymbolicProgBundle.getMessage("PromptSaveQuit"), this
                        ) {
                            public boolean checkPromptNeeded() {
                                return !checkDirtyFile();
                            }

                            public boolean doPrompt() {
                                boolean result = storeFile(); // storeFile false if failed, abort shutdown
                                return result;
                            }
                        };
            }
            jmri.InstanceManager.shutDownManagerInstance().register(fileDirtyTask);
        }

        // Create a menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // add a "File" menu
        JMenu fileMenu = new JMenu(SymbolicProgBundle.getMessage("MenuFile"));
        menuBar.add(fileMenu);

        // add a "Factory Reset" menu
        resetMenu = new JMenu(SymbolicProgBundle.getMessage("MenuReset"));
        menuBar.add(resetMenu);
        resetMenu.add(new FactoryResetAction(SymbolicProgBundle.getMessage("MenuFactoryReset"), resetModel, this));
        resetMenu.setEnabled(false);

        // Add a save item
        fileMenu.add(new AbstractAction(SymbolicProgBundle.getMessage("MenuSave")) {
            public void actionPerformed(ActionEvent e) {
                storeFile();
            }
        });

        JMenu printSubMenu = new JMenu(SymbolicProgBundle.getMessage("MenuPrint"));
        printSubMenu.add(new PrintAction(SymbolicProgBundle.getMessage("MenuPrintAll"), this, false));
        printSubMenu.add(new PrintCvAction(SymbolicProgBundle.getMessage("MenuPrintCVs"), cvModel, this, false, _rosterEntry));
        fileMenu.add(printSubMenu);

        JMenu printPreviewSubMenu = new JMenu(SymbolicProgBundle.getMessage("MenuPrintPreview"));
        printPreviewSubMenu.add(new PrintAction(SymbolicProgBundle.getMessage("MenuPrintPreviewAll"), this, true));
        printPreviewSubMenu.add(new PrintCvAction(SymbolicProgBundle.getMessage("MenuPrintPreviewCVs"), cvModel, this, true, _rosterEntry));
        fileMenu.add(printPreviewSubMenu);

        // add "Import" submenu; this is heirarchical because
        // some of the names are so long, and we expect more formats
        JMenu importSubMenu = new JMenu(SymbolicProgBundle.getMessage("MenuImport"));
        fileMenu.add(importSubMenu);
        importSubMenu.add(new CsvImportAction(SymbolicProgBundle.getMessage("MenuImportCSV"), cvModel, this, progStatus));
        importSubMenu.add(new Pr1ImportAction(SymbolicProgBundle.getMessage("MenuImportPr1"), cvModel, this, progStatus));
        importSubMenu.add(new LokProgImportAction(SymbolicProgBundle.getMessage("MenuImportLokProg"), cvModel, this, progStatus));
        importSubMenu.add(new QuantumCvMgrImportAction(SymbolicProgBundle.getMessage("MenuImportQuantumCvMgr"), cvModel, this, progStatus));

        // add "Export" submenu; this is heirarchical because
        // some of the names are so long, and we expect more formats
        JMenu exportSubMenu = new JMenu(SymbolicProgBundle.getMessage("MenuExport"));
        fileMenu.add(exportSubMenu);
        exportSubMenu.add(new CsvExportAction(SymbolicProgBundle.getMessage("MenuExportCSV"), cvModel, this));
        exportSubMenu.add(new Pr1ExportAction(SymbolicProgBundle.getMessage("MenuExportPr1DOS"), cvModel, this));
        exportSubMenu.add(new Pr1WinExportAction(SymbolicProgBundle.getMessage("MenuExportPr1WIN"), cvModel, this));

        // to control size, we need to insert a single
        // JPanel, then have it laid out with BoxLayout
        JPanel pane = new JPanel();
        tempPane = pane;

        // general GUI config
        pane.setLayout(new BorderLayout());

        // configure GUI elements
        // set read buttons enabled state, tooltips
        enableReadButtons();

        readChangesButton.addItemListener(l1 = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    prepGlassPane(readChangesButton);
                    readChangesButton.setText(SymbolicProgBundle.getMessage("ButtonStopReadChangesAll"));
                    readChanges();
                } else {
                    if (_programmingPane != null) {
                        _programmingPane.stopProgramming();
                    }
                    paneListIndex = paneList.size();
                    readChangesButton.setText(SymbolicProgBundle.getMessage("ButtonReadChangesAllSheets"));
                }
            }
        });

        readAllButton.addItemListener(l3 = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    prepGlassPane(readAllButton);
                    readAllButton.setText(SymbolicProgBundle.getMessage("ButtonStopReadAll"));
                    readAll();
                } else {
                    if (_programmingPane != null) {
                        _programmingPane.stopProgramming();
                    }
                    paneListIndex = paneList.size();
                    readAllButton.setText(SymbolicProgBundle.getMessage("ButtonReadAllSheets"));
                }
            }
        });

        writeChangesButton.setToolTipText(SymbolicProgBundle.getMessage("TipWriteHighlightedValues"));
        writeChangesButton.addItemListener(l2 = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    prepGlassPane(writeChangesButton);
                    writeChangesButton.setText(SymbolicProgBundle.getMessage("ButtonStopWriteChangesAll"));
                    writeChanges();
                } else {
                    if (_programmingPane != null) {
                        _programmingPane.stopProgramming();
                    }
                    paneListIndex = paneList.size();
                    writeChangesButton.setText(SymbolicProgBundle.getMessage("ButtonWriteChangesAllSheets"));
                }
            }
        });

        writeAllButton.setToolTipText(SymbolicProgBundle.getMessage("TipWriteAllValues"));
        writeAllButton.addItemListener(l4 = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    prepGlassPane(writeAllButton);
                    writeAllButton.setText(SymbolicProgBundle.getMessage("ButtonStopWriteAll"));
                    writeAll();
                } else {
                    if (_programmingPane != null) {
                        _programmingPane.stopProgramming();
                    }
                    paneListIndex = paneList.size();
                    writeAllButton.setText(SymbolicProgBundle.getMessage("ButtonWriteAllSheets"));
                }
            }
        });

        // most of the GUI is done from XML in readConfig() function
        // which configures the tabPane
        pane.add(tabPane, BorderLayout.CENTER);

        // and put that pane into the JFrame
        getContentPane().add(pane);

        // add help
        addHelp();
    }

    void setProgrammingGui(JPanel pane) {
        // see if programming mode is available
        modePane = getModePane();
        if (modePane != null) {
            // if so, configure programming part of GUI
            JPanel bottom = new JPanel();
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
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
            temp.add(modePane);

            // add programming status message
            bottom.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
            progStatus.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            bottom.add(progStatus);
            pane.add(bottom, BorderLayout.SOUTH);
        }
    }

    public List<JPanel> getPaneList() {
        return paneList;
    }

    void addHelp() {
        addHelpMenu("package.jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame", true);
    }

    public Dimension getPreferredSize() {
        Dimension screen = getMaximumSize();
        int width = Math.min(super.getPreferredSize().width, screen.width);
        int height = Math.min(super.getPreferredSize().height, screen.height);
        return new Dimension(width, height);
    }

    public Dimension getMaximumSize() {
        Dimension screen = getToolkit().getScreenSize();
        return new Dimension(screen.width, screen.height - 35);
    }

    /**
     * Enable the read all and read changes button if possible. This checks to
     * make sure this is appropriate, given the attached programmer's
     * capability.
     */
    void enableReadButtons() {
        readChangesButton.setToolTipText(SymbolicProgBundle.getMessage("TipReadChanges"));
        readAllButton.setToolTipText(SymbolicProgBundle.getMessage("TipReadAll"));
        // check with CVTable programmer to see if read is possible
        if (cvModel != null && cvModel.getProgrammer() != null
                && !cvModel.getProgrammer().getCanRead()) {
            // can't read, disable the button
            readChangesButton.setEnabled(false);
            readAllButton.setEnabled(false);
            readChangesButton.setToolTipText(SymbolicProgBundle.getMessage("TipNoRead"));
            readAllButton.setToolTipText(SymbolicProgBundle.getMessage("TipNoRead"));
        } else {
            readChangesButton.setEnabled(true);
            readAllButton.setEnabled(true);
        }
    }

    /**
     * Initialization sequence:
     * <UL>
     * <LI> Ask the RosterEntry to read its contents
     * <LI> If the decoder file is specified, open and load it, otherwise get
     * the decoder filename from the RosterEntry and load that. Note that we're
     * assuming the roster entry has the right decoder, at least w.r.t. the loco
     * file.
     * <LI> Fill CV values from the roster entry
     * <LI> Create the programmer panes
     * </UL>
     *
     * @param pDecoderFile    XML file defining the decoder contents; if null,
     *                        the decoder definition is found from the
     *                        RosterEntry
     * @param pRosterEntry    RosterEntry for information on this locomotive
     * @param pFrameTitle     Name/title for the frame
     * @param pProgrammerFile Name of the programmer file to use
     * @param pProg           Programmer object to be used to access CVs
     */
    public PaneProgFrame(DecoderFile pDecoderFile, @Nonnull RosterEntry pRosterEntry,
            String pFrameTitle, String pProgrammerFile, Programmer pProg, boolean opsMode) {
        super(pFrameTitle);

        _rosterEntry = pRosterEntry;
        _opsMode = opsMode;
        filename = pProgrammerFile;
        mProgrammer = pProg;

        // create the tables
        cvModel = new CvTableModel(progStatus, mProgrammer);
        iCvModel = new IndexedCvTableModel(progStatus, mProgrammer);

        variableModel = new VariableTableModel(progStatus, new String[]{"Name", "Value"},
                cvModel, iCvModel);

        resetModel = new ResetTableModel(progStatus, mProgrammer);

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

        // finally fill the CV values from the specific loco file
        if (_rosterEntry.getFileName() != null) {
            _rosterEntry.loadCvModel(cvModel, iCvModel);
        }

        // mark file state as consistent
        variableModel.setFileDirty(false);

        // if the Reset Table was used lets enable the menu item
        if (!_opsMode || resetModel.hasOpsModeReset()) {
            if (resetModel.getRowCount() > 0) {
                resetMenu.setEnabled(true);
            }
        }
        // and build the GUI
        loadProgrammerFile(pRosterEntry);

        // optionally, add extra panes from the decoder file
        Attribute a;
        if ((a = programmerRoot.getChild("programmer").getAttribute("decoderFilePanes")) != null
                && a.getValue().equals("yes")) {
            if (decoderRoot != null) {
                if (log.isDebugEnabled()) {
                    log.debug("will process " + decoderPaneList.size() + " pane definitions from decoder file");
                }
                for (int i = 0; i < decoderPaneList.size(); i++) {
                    // load each pane
                    String pname = jmri.util.jdom.LocaleSelector.getAttribute(decoderPaneList.get(i), "name");

                    // handle include/exclude
                    if (isIncludedFE(decoderPaneList.get(i), modelElem, _rosterEntry, "", "")) {
                        newPane(pname, decoderPaneList.get(i), modelElem, true, false);  // show even if empty not a programmer pane
                    }
                }
            }
        }

        // set the programming mode
        if (pProg != null) {
            if (jmri.InstanceManager.programmerManagerInstance() != null) {
                // go through in preference order, trying to find a mode
                // that exists in both the programmer and decoder.
                // First, get attributes. If not present, assume that
                // all modes are usable
                Element programming = null;
                if (decoderRoot != null
                        && (programming = decoderRoot.getChild("decoder").getChild("programming")) != null) {

                    // add any needed facades
                    Programmer pf = jmri.implementation.ProgrammerFacadeSelector.loadFacadeElements(programming, mProgrammer);
                    log.debug("new programmer " + pf);
                    mProgrammer = pf;
                    cvModel.setProgrammer(pf);
                    iCvModel.setProgrammer(pf);
                    resetModel.setProgrammer(pf);
                    log.debug("Found programmers: " + cvModel.getProgrammer() + " " + iCvModel.getProgrammer());

                }

                // done after setting facades in case new possibilities appear
                if (programming != null) {
                    pickProgrammerMode(programming);
                } else {
                    log.debug("Skipping programmer setup because found no programmer element");
                }

            } else {
                log.error("Can't set programming mode, no programmer instance");
            }
        }

        // now that programmer is configured, set the programming GUI
        setProgrammingGui(tempPane);

        pack();

        if (log.isDebugEnabled()) {
            log.debug("PaneProgFrame \"" + pFrameTitle
                    + "\" constructed for file " + _rosterEntry.getFileName()
                    + ", unconstrained size is " + super.getPreferredSize()
                    + ", constrained to " + getPreferredSize());
        }
    }

    /**
     * Front end to DecoderFile.isIncluded()
     * <ul>
     * <li>Retrieves "productID" and "model attributes from the "model" element
     * and "family" attribute from the roster entry. </li>
     * <li>Then invokes DecoderFile.isIncluded() with the retrieved values.</li>
     * <li>Deals deals gracefully with null or missing elements and
     * attributes.</li>
     * </ul>
     *
     * @param e             XML element with possible "include" and "exclude"
     *                      attributes to be checked
     * @param aModelElement "model" element from the Decoder Index, used to get
     *                      "model" and "productID".
     * @param aRosterEntry  The current roster entry, used to get "family".
     * @param extraIncludes additional "include" terms
     * @param extraExcludes additional "exclude" terms
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
                directbit = true;
                directbyte = false;
            } else if (a.getValue().equals("byteOnly")) {
                directbit = false;
                directbyte = true;
            } else {
                directbit = true;
                directbyte = true;
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
            log.debug("XML specifies modes: P " + paged + " DBi " + directbit + " Dby " + directbyte + " R " + register + " now " + mProgrammer.getMode());
            log.debug("Programmer supports:");
            for (ProgrammingMode m : modes) {
                log.debug("   {} {}", m.getStandardName(), m.toString());
            }
        }

        // first try specified modes
        for (Element el1 : programming.getChildren("mode")) {
            String name = el1.getText();
            if (log.isDebugEnabled()) log.debug(" mode {} was specified", name);
            for (ProgrammingMode m : modes) {
                if (name.equals(m.getStandardName())) {
                    log.info("Programming mode selected: {} ({})", m.toString(), m.getStandardName());
                    mProgrammer.setMode(m);
                    return;
                }
            }
        }

        // go through historical modes

        if (modes.contains(DefaultProgrammerManager.DIRECTMODE) && directbit && directbyte) {
            mProgrammer.setMode(DefaultProgrammerManager.DIRECTMODE);
            log.debug("Set to DIRECTMODE");
        } else if (modes.contains(DefaultProgrammerManager.DIRECTBITMODE) && directbit) {
            mProgrammer.setMode(DefaultProgrammerManager.DIRECTBITMODE);
            log.debug("Set to DIRECTBITMODE");
        } else if (modes.contains(DefaultProgrammerManager.DIRECTBYTEMODE) && directbyte) {
            mProgrammer.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
            log.debug("Set to DIRECTBYTEMODE");
        } else if (modes.contains(DefaultProgrammerManager.PAGEMODE) && paged) {
            mProgrammer.setMode(DefaultProgrammerManager.PAGEMODE);
            log.debug("Set to PAGEMODE");
        } else if (modes.contains(DefaultProgrammerManager.REGISTERMODE) && register) {
            mProgrammer.setMode(DefaultProgrammerManager.REGISTERMODE);
            log.debug("Set to REGISTERMODE");
        } else {
            log.warn("No acceptable mode found, leave as found");
        }

    }

    /**
     * Data element holding the 'model' element representing the decoder type
     */
    Element modelElem = null;

    Element decoderRoot = null;

    protected void loadDecoderFromLoco(RosterEntry r) {
        // get a DecoderFile from the locomotive xml
        String decoderModel = r.getDecoderModel();
        String decoderFamily = r.getDecoderFamily();
        log.debug("selected loco uses decoder {} {}",decoderFamily, decoderModel);

        // locate a decoder like that.
        List<DecoderFile> l = DecoderIndexFile.instance().matchingDecoderList(null, decoderFamily, null, null, null, decoderModel);
        log.debug("found {} matches", l.size());
        if (l.size() == 0) {
            log.debug("Loco uses " + decoderFamily + " " + decoderModel + " decoder, but no such decoder defined");
            // fall back to use just the decoder name, not family
            l = DecoderIndexFile.instance().matchingDecoderList(null, null, null, null, null, decoderModel);
            if (log.isDebugEnabled()) {
                log.debug("found " + l.size() + " matches without family key");
            }
        }
        if (l.size() > 0) {
            DecoderFile d = l.get(0);
            loadDecoderFile(d, r);
        } else {
            if (decoderModel.equals("")) {
                log.debug("blank decoderModel requested, so nothing loaded");
            } else {
                log.warn("no matching \"" + decoderModel + "\" decoder found for loco, no decoder info loaded");
            }
        }
    }

    protected void loadDecoderFile(@Nonnull DecoderFile df, @Nonnull RosterEntry re) {
        if (df == null) {
            throw new IllegalArgumentException("loadDecoder file invoked with null object");
        }
        if (log.isDebugEnabled()) {
            log.debug("loadDecoderFile from " + DecoderFile.fileLocation
                    + " " + df.getFilename());
        }

        try {
            decoderRoot = df.rootFromName(DecoderFile.fileLocation + df.getFilename());
        } catch (org.jdom2.JDOMException e) {
            log.error("Exception while parsing decoder XML file: " + df.getFilename(), e);
            return;
        } catch (java.io.IOException  e) {
            log.error("Exception while reading decoder XML file: " + df.getFilename(), e);
            return;
        }
        // load variables from decoder tree
        df.getProductID();
        df.loadVariableModel(decoderRoot.getChild("decoder"), variableModel);

        // load reset from decoder tree
        if (!variableModel.piCv().equals("")) {
            resetModel.setPiCv(variableModel.piCv());
        }
        if (!variableModel.siCv().equals("")) {
            resetModel.setSiCv(variableModel.siCv());
        }
        df.loadResetModel(decoderRoot.getChild("decoder"), resetModel);

        // load function names from family
        re.loadFunctions(decoderRoot.getChild("decoder").getChild("family").getChild("functionlabels"), "family");

        // load sound names from family
        re.loadSounds(decoderRoot.getChild("decoder").getChild("family").getChild("soundlabels"), "family");

        // get the showEmptyPanes attribute, if yes/no update our state
        if (decoderRoot.getAttribute("showEmptyPanes") != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found in decoder " + decoderRoot.getAttribute("showEmptyPanes").getValue());
            }
            decoderShowEmptyPanes = decoderRoot.getAttribute("showEmptyPanes").getValue();
        } else {
            decoderShowEmptyPanes = "";
        }
        log.debug("decoderShowEmptyPanes={}", decoderShowEmptyPanes);

        // save the pointer to the model element
        modelElem = df.getModelElement();

        // load function names from model
        re.loadFunctions(modelElem.getChild("functionlabels"), "model");

        // load sound names from model
        re.loadSounds(modelElem.getChild("soundlabels"), "model");

    }

    protected void loadProgrammerFile(RosterEntry r) {
        // Open and parse programmer file
        XmlFile pf = new XmlFile() {
        };  // XmlFile is abstract
        try {
            programmerRoot = pf.rootFromName(filename);

            // get the showEmptyPanes attribute, if yes/no update our state
            if (programmerRoot.getChild("programmer").getAttribute("showEmptyPanes") != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found in programmer " + programmerRoot.getChild("programmer").getAttribute("showEmptyPanes").getValue());
                }
                programmerShowEmptyPanes = programmerRoot.getChild("programmer").getAttribute("showEmptyPanes").getValue();
            } else {
                programmerShowEmptyPanes = "";
            }
            if (log.isDebugEnabled()) {
                log.debug("programmerShowEmptyPanes=" + programmerShowEmptyPanes);
            }

            // get extra any panes from the decoder file
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
            log.error("exception parsing programmer file: " + filename, e);
            // provide traceback too
            e.printStackTrace();
        } catch (java.io.IOException e) {
            log.error("exception reading programmer file: " + filename, e);
            // provide traceback too
            e.printStackTrace();
        }
    }

    Element programmerRoot = null;

    /**
     * @return true if decoder needs to be written
     */
    protected boolean checkDirtyDecoder() {
        if (log.isDebugEnabled()) {
            log.debug("Checking decoder dirty status. CV: " + cvModel.decoderDirty() + " variables:" + variableModel.decoderDirty());
        }
        return (getModePane() != null && (cvModel.decoderDirty() || variableModel.decoderDirty()));
    }

    /**
     * @return true if file needs to be written
     */
    protected boolean checkDirtyFile() {
        return (variableModel.fileDirty() || _rPane.guiChanged(_rosterEntry) || _flPane.guiChanged(_rosterEntry) || _rMPane.guiChanged(_rosterEntry));
    }

    protected void handleDirtyFile() {
    }

    /**
     * Close box has been clicked; handle check for dirty with respect to
     * decoder or file, then close.
     *
     * @param e Not used
     */
    public void windowClosing(java.awt.event.WindowEvent e) {

        // Don't want to actually close if we return early
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // check for various types of dirty - first table data not written back
        if (log.isDebugEnabled()) {
            log.debug("Checking decoder dirty status. CV: " + cvModel.decoderDirty() + " variables:" + variableModel.decoderDirty());
        }
        if (checkDirtyDecoder()) {
            if (JOptionPane.showConfirmDialog(null,
                    SymbolicProgBundle.getMessage("PromptCloseWindowNotWrittenDecoder"),
                    SymbolicProgBundle.getMessage("PromptChooseOne"),
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        if (checkDirtyFile()) {
            int option = JOptionPane.showOptionDialog(null, SymbolicProgBundle.getMessage("PromptCloseWindowNotWrittenConfig"),
                    SymbolicProgBundle.getMessage("PromptChooseOne"),
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
                    new String[]{SymbolicProgBundle.getMessage("PromptSaveAndClose"), SymbolicProgBundle.getMessage("PromptClose"), SymbolicProgBundle.getMessage("PromptCancel")},
                    SymbolicProgBundle.getMessage("PromptSaveAndClose"));
            if (option == 0) {
                // save requested
                if (!storeFile()) {
                    return;   // don't close if failed
                }
            } else if (option == 2) {
                // cancel requested
                return; // without doing anything
            }
        }
        // Check for a "<new loco>" roster entry; if found, remove it
        List<RosterEntry> l = Roster.instance().matchingList(null, null, null, null, null, null, SymbolicProgBundle.getMessage("LabelNewDecoder"));
        if (l.size() > 0 && log.isDebugEnabled()) {
            log.debug("Removing " + l.size() + " <new loco> entries");
        }
        int x = l.size() + 1;
        while (l.size() > 0) {
            Roster.instance().removeEntry(l.get(0));
            l = Roster.instance().matchingList(null, null, null, null, null, null, SymbolicProgBundle.getMessage("LabelNewDecoder"));
            x--;
            if (x == 0) {
                log.error("We have tried to remove all the entries, however an error has occured which has result in the entries not being deleted correctly");
                l = new ArrayList<RosterEntry>();
            }
        }

        // OK, continue close
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // deregister shutdown hooks
        if (jmri.InstanceManager.shutDownManagerInstance() != null) {
            jmri.InstanceManager.shutDownManagerInstance().deregister(decoderDirtyTask);
        }
        decoderDirtyTask = null;
        if (jmri.InstanceManager.shutDownManagerInstance() != null) {
            jmri.InstanceManager.shutDownManagerInstance().deregister(fileDirtyTask);
        }
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
                tabPane.addTab(SymbolicProgBundle.getMessage("ROSTER ENTRY"), makeInfoPane(r));
            }
        } else {
            tabPane.addTab(SymbolicProgBundle.getMessage("ROSTER ENTRY"), makeInfoPane(r));
        }

        // add the Function Label tab
        if (root.getChild("programmer").getAttribute("showFnLanelPane").getValue().equals("yes")) {
            tabPane.addTab(SymbolicProgBundle.getMessage("FUNCTION LABELS"), makeFunctionLabelPane(r));
        } else {
            // make it, just don't make it visible
            makeFunctionLabelPane(r);
        }

        // add the Media tab
        if (root.getChild("programmer").getAttribute("showRosterMediaPane").getValue().equals("yes")) {
            tabPane.addTab(SymbolicProgBundle.getMessage("ROSTER MEDIA"), makeMediaPane(r));
        } else {
            // make it, just don't make it visible
            makeMediaPane(r);
        }

        // for all "pane" elements in the programmer
        List<Element> progPaneList = base.getChildren("pane");
        if (log.isDebugEnabled()) {
            log.debug("will process " + progPaneList.size() + " pane definitions");
        }
        for (int i = 0; i < progPaneList.size(); i++) {
            // load each programmer pane
            Element temp = progPaneList.get(i);
            List<Element> pnames = temp.getChildren("name");
            boolean isProgPane = true;
            if ((pnames.size() > 0) && (decoderPaneList != null) && (decoderPaneList.size() > 0)) {
                String namePrimary = (pnames.get(0)).getValue(); // get non-localised name

                // check if there is a same-name pane in decoder file
                for (int j = 0; j < decoderPaneList.size(); j++) {
                    List<Element> dnames = decoderPaneList.get(j).getChildren("name");
                    if (dnames.size() > 0) {
                        String namePrimaryDecoder = (dnames.get(0)).getValue(); // get non-localised name
                        if (namePrimary.equals(namePrimaryDecoder)) {
                            // replace programmer pane with same-name decoder pane
                            temp = decoderPaneList.get(j);
                            decoderPaneList.remove(j);
                            isProgPane = false;
                        }
                    }
                }
            }
            String name = jmri.util.jdom.LocaleSelector.getAttribute(temp, "name");

            // handle include/exclude
            if (isIncludedFE(temp, modelElem, _rosterEntry, "", "")) {
                newPane(name, temp, modelElem, false, isProgPane);  // dont force showing if empty
            }
        }
    }

    /**
     * reset all CV values to defaults stored earlier. This will in turn update
     * the variables
     */
    protected void resetToDefaults() {
        int n = defaultCvValues.length;
        for (int i = 0; i < n; i++) {
            CvValue cv = cvModel.getCvByNumber(defaultCvNumbers[i]);
            if (cv == null) {
                log.warn("Trying to set default in CV " + defaultCvNumbers[i]
                        + " but didn't find the CV object");
            } else {
                cv.setValue(defaultCvValues[i]);
            }
        }
        n = defaultIndexedCvValues.length;
        for (int i = 0; i < n; i++) {
            CvValue cv = iCvModel.getCvByRow(i);
            if (cv == null) {
                log.warn("Trying to set default in indexed CV from row " + i
                        + " but didn't find the CV object");
            } else {
                cv.setValue(defaultIndexedCvValues[i]);
            }
        }
    }

    int defaultCvValues[] = null;
    String defaultCvNumbers[] = null;
    int defaultIndexedCvValues[] = null;

    /**
     * Save all CV values. These stored values are used by resetToDefaults
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

        n = iCvModel.getRowCount();
        defaultIndexedCvValues = new int[n];

        for (int i = 0; i < n; i++) {
            CvValue cv = iCvModel.getCvByRow(i);
            defaultIndexedCvValues[i] = cv.getValue();
        }
    }

    protected JPanel makeInfoPane(RosterEntry r) {
        // create the identification pane (not configured by programmer file now; maybe later?

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
        JButton store = new JButton(SymbolicProgBundle.getMessage("ButtonSave"));
        store.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        store.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                storeFile();
            }
        });

        // add the reset button
        JButton reset = new JButton(SymbolicProgBundle.getMessage("ButtonResetDefaults"));
        reset.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        reset.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                resetToDefaults();
            }
        });

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
        java.beans.PropertyChangeListener dccNews = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                updateDccAddress();
            }
        };
        primaryAddr = variableModel.findVar("Short Address");
        if (primaryAddr == null) {
            log.debug("DCC Address monitor didnt find a Short Address variable");
        } else {
            primaryAddr.addPropertyChangeListener(dccNews);
        }
        extendAddr = variableModel.findVar("Long Address");
        if (extendAddr == null) {
            log.debug("DCC Address monitor didnt find an Long Address variable");
        } else {
            extendAddr.addPropertyChangeListener(dccNews);
        }
        addMode = (EnumVariableValue) variableModel.findVar("Address Format");
        if (addMode == null) {
            log.debug("DCC Address monitor didnt find an Address Format variable");
        } else {
            addMode.addPropertyChangeListener(dccNews);
        }

        // get right address to start
        updateDccAddress();

        return outer;
    }

    protected JPanel makeFunctionLabelPane(RosterEntry r) {
        // create the identification pane (not configured by programmer file now; maybe later?

        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(body);

        // add tab description
        JLabel title = new JLabel(SymbolicProgBundle.getMessage("UseThisTabCustomize"));
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        body.add(title);
        body.add(new JLabel(" "));	// some padding

        // add roster info
        _flPane = new FunctionLabelPane(r);
        //_flPane.setMaximumSize(_flPane.getPreferredSize());
        body.add(_flPane);

        // add the store button
        JButton store = new JButton(SymbolicProgBundle.getMessage("ButtonSave"));
        store.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        store.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                storeFile();
            }
        });

        store.setToolTipText(_rosterEntry.getFileName());

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        buttons.add(store);

        body.add(buttons);
        outer.add(scrollPane);
        return outer;
    }

    protected JPanel makeMediaPane(RosterEntry r) {
        // create the identification pane (not configured by programmer file now; maybe later?

        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(body);

        // add tab description
        JLabel title = new JLabel(SymbolicProgBundle.getMessage("UseThisTabMedia"));
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        body.add(title);
        body.add(new JLabel(" "));	// some padding

        // add roster info
        _rMPane = new RosterMediaPane(r);
        _rMPane.setMaximumSize(_rMPane.getPreferredSize());
        body.add(_rMPane);

        // add the store button
        JButton store = new JButton(SymbolicProgBundle.getMessage("ButtonSave"));
        store.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        store.addActionListener(new ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent e) {
                storeFile();
            }
        });

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
            log.debug("updateDccAddress: short " + (primaryAddr == null ? "<null>" : primaryAddr.getValueString())
                    + " long " + (extendAddr == null ? "<null>" : extendAddr.getValueString())
                    + " mode " + (addMode == null ? "<null>" : addMode.getValueString()));
        }

        new DccAddressVarHandler(primaryAddr, extendAddr, addMode) {
            protected void doPrimary() {
                // short address mode
                longMode = false;
                if (primaryAddr != null && !primaryAddr.getValueString().equals("")) {
                    newAddr = primaryAddr.getValueString();
                }
            }

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
            log.debug("newPane with enableEmpty " + enableEmpty + " showEmptyPanes " + isShowingEmptyPanes());
        }
        // create a panel to hold columns
        PaneProgPane p = new PaneProgPane(this, name, pane, cvModel, iCvModel, variableModel, modelElem, _rosterEntry, programmerPane);
        p.setOpaque(true);
        // how to handle the tab depends on whether it has contents and option setting
        int index;
        if (enableEmpty || !p.cvList.isEmpty() || !p.varList.isEmpty() || !p.indexedCvList.isEmpty()) {
            tabPane.addTab(name, p);  // always add if not empty
            index = tabPane.indexOfTab(name);
            tabPane.setToolTipTextAt(index, p.getToolTipText());
        } else if (isShowingEmptyPanes()) {
            // here empty, but showing anyway as disabled
            tabPane.addTab(name, p);
            index = tabPane.indexOfTab(name);
            tabPane.setEnabledAt(index, true); // need to enable the pane so user can see message
            tabPane.setToolTipTextAt(index,
                    SymbolicProgBundle.getMessage("TipTabEmptyNoCategory"));
        } else {
            // here not showing tab at all
            index = -1;
        }

        // remember it for programming
        paneList.add(p);

        // if visible, set qualications
        if (index >= 0) {
            processModifierElements(pane, p, variableModel, tabPane, index);
        }
    }

    /**
     * If there are any modifier elements, process them
     */
    protected void processModifierElements(Element e, final PaneProgPane pane, VariableTableModel model, final JTabbedPane tabPane, final int index) {
        QualifierAdder qa = new QualifierAdder() {
            protected Qualifier createQualifier(VariableValue var, String relation, String value) {
                return new PaneQualifier(pane, var, Integer.parseInt(value), relation, tabPane, index);
            }

            protected void addListener(java.beans.PropertyChangeListener qc) {
                pane.addPropertyChangeListener(qc);
            }
        };

        qa.processModifierElements(e, model);
    }

    public BusyGlassPane getBusyGlassPane() {
        return glassPane;
    }

    /**
     *
     */
    public void prepGlassPane(AbstractButton activeButton) {
        List<Rectangle> rectangles = new ArrayList<Rectangle>();

        if (glassPane != null) {
            glassPane.dispose();
        }
        activeComponents.clear();
        activeComponents.add(activeButton);
        if (activeButton == readChangesButton || activeButton == readAllButton
                || activeButton == writeChangesButton || activeButton == writeAllButton) {
            if (activeButton == readChangesButton) {
                for (int i = 0; i < paneList.size(); i++) {
                    activeComponents.add(((PaneProgPane) paneList.get(i)).readChangesButton);
                }
            } else if (activeButton == readAllButton) {
                for (int i = 0; i < paneList.size(); i++) {
                    activeComponents.add(((PaneProgPane) paneList.get(i)).readAllButton);
                }
            } else if (activeButton == writeChangesButton) {
                for (int i = 0; i < paneList.size(); i++) {
                    activeComponents.add(((PaneProgPane) paneList.get(i)).writeChangesButton);
                }
            } else if (activeButton == writeAllButton) {
                for (int i = 0; i < paneList.size(); i++) {
                    activeComponents.add(((PaneProgPane) paneList.get(i)).writeAllButton);
                }
            }
            for (int i = 0; i < tabPane.getTabCount(); i++) {
                rectangles.add(tabPane.getUI().getTabBounds(tabPane, i));
            }
        }
        glassPane = new BusyGlassPane(activeComponents, rectangles, this.getContentPane(), this);
        this.setGlassPane(glassPane);
    }

    public void paneFinished() {
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
     * In addition, if a programming mode pane is present, it's "set" button is
     * enabled.
     *
     * @param stat Are reads possible? If false, so not enable the read buttons.
     */
    public void enableButtons(boolean stat) {
        if (stat) {
            enableReadButtons();
        } else {
            readChangesButton.setEnabled(false);
            readAllButton.setEnabled(false);
        }
        writeChangesButton.setEnabled(stat);
        writeAllButton.setEnabled(stat);
        if (modePane != null) {
            modePane.setEnabled(stat);
        }
    }

    boolean justChanges;

    public boolean isBusy() {
        return _busy;
    }
    private boolean _busy = false;

    private void setBusy(boolean stat) {
        _busy = stat;

        for (int i = 0; i < paneList.size(); i++) {
            if (stat) {
                ((PaneProgPane) paneList.get(i)).enableButtons(false);
            } else {
                ((PaneProgPane) paneList.get(i)).enableButtons(true);
            }
        }
        if (!stat) {
            paneFinished();
        }
    }

    /**
     * invoked by "Read Changes" button, this sets in motion a continuing
     * sequence of "read changes" operations on the panes. Each invocation of
     * this method reads one pane; completion of that request will cause it to
     * happen again, reading the next pane, until there's nothing left to read.
     * <P>
     * @return true if a read has been started, false if the operation is
     *         complete.
     */
    public boolean readChanges() {
        if (log.isDebugEnabled()) {
            log.debug("readChanges starts");
        }
        justChanges = true;
        for (int i = 0; i < paneList.size(); i++) {
            ((PaneProgPane) paneList.get(i)).setToRead(justChanges, true);
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
     * invoked by "Read All" button, this sets in motion a continuing sequence
     * of "read all" operations on the panes. Each invocation of this method
     * reads one pane; completion of that request will cause it to happen again,
     * reading the next pane, until there's nothing left to read.
     * <P>
     * @return true if a read has been started, false if the operation is
     *         complete.
     */
    public boolean readAll() {
        if (log.isDebugEnabled()) {
            log.debug("readAll starts");
        }
        justChanges = false;
        for (int i = 0; i < paneList.size(); i++) {
            ((PaneProgPane) paneList.get(i)).setToRead(justChanges, true);
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
            if (log.isDebugEnabled()) {
                log.debug("doRead on " + paneListIndex);
            }
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
                if (log.isDebugEnabled()) {
                    log.debug("doRead expecting callback from readPane " + paneListIndex);
                }
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
        if (log.isDebugEnabled()) {
            log.debug("doRead found nothing to do");
        }
        return false;
    }

    /**
     * invoked by "Write All" button, this sets in motion a continuing sequence
     * of "write all" operations on each pane. Each invocation of this method
     * writes one pane; completion of that request will cause it to happen
     * again, writing the next pane, until there's nothing left to write.
     * <P>
     * @return true if a write has been started, false if the operation is
     *         complete.
     */
    public boolean writeAll() {
        if (log.isDebugEnabled()) {
            log.debug("writeAll starts");
        }
        justChanges = false;
        for (int i = 0; i < paneList.size(); i++) {
            ((PaneProgPane) paneList.get(i)).setToWrite(justChanges, true);
        }
        setBusy(true);
        enableButtons(false);
        writeAllButton.setEnabled(true);
        glassPane.setVisible(true);
        paneListIndex = 0;
        return doWrite();
    }

    /**
     * invoked by "Write Changes" button, this sets in motion a continuing
     * sequence of "write changes" operations on each pane. Each invocation of
     * this method writes one pane; completion of that request will cause it to
     * happen again, writing the next pane, until there's nothing left to write.
     * <P>
     * @return true if a write has been started, false if the operation is
     *         complete.
     */
    public boolean writeChanges() {
        if (log.isDebugEnabled()) {
            log.debug("writeChanges starts");
        }
        justChanges = true;
        for (int i = 0; i < paneList.size(); i++) {
            ((PaneProgPane) paneList.get(i)).setToWrite(justChanges, true);
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
            if (log.isDebugEnabled()) {
                log.debug("doWrite starts on " + paneListIndex);
            }
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
                if (log.isDebugEnabled()) {
                    log.debug("doWrite expecting callback from writePane " + paneListIndex);
                }
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
        if (log.isDebugEnabled()) {
            log.debug("doWrite found nothing to do");
        }
        return false;
    }

    public void printPanes(final boolean preview) {
        PrintRosterEntry pre = new PrintRosterEntry(_rosterEntry, paneList, _flPane, _rMPane, this);
        pre.printPanes(preview);

    }

    boolean _read = true;
    PaneProgPane _programmingPane = null;

    /**
     * get notification of a variable property change in the pane, specifically
     * "busy" going to false at the end of a programming operation
     *
     * @param e Event, used to find source
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // check for the right event
        if (_programmingPane == null) {
            log.warn("unexpected propertyChange: " + e);
            return;
        } else if (log.isDebugEnabled()) {
            log.debug("property changed: " + e.getPropertyName()
                    + " new value: " + e.getNewValue());
        }
        log.debug("check valid: " + (e.getSource() == _programmingPane) + " " + (!e.getPropertyName().equals("Busy")) + " " + (((Boolean) e.getNewValue()).equals(Boolean.FALSE)));
        if (e.getSource() == _programmingPane
                && e.getPropertyName().equals("Busy")
                && ((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {

            if (log.isDebugEnabled()) {
                log.debug("end of a programming pane operation, remove");
            }

            // remove existing listener
            _programmingPane.removePropertyChangeListener(this);
            _programmingPane = null;
            // restart the operation
            if (_read && readChangesButton.isSelected()) {
                if (log.isDebugEnabled()) {
                    log.debug("restart readChanges");
                }
                doRead();
            } else if (_read && readAllButton.isSelected()) {
                if (log.isDebugEnabled()) {
                    log.debug("restart readAll");
                }
                doRead();
            } else if (writeChangesButton.isSelected()) {
                if (log.isDebugEnabled()) {
                    log.debug("restart writeChanges");
                }
                doWrite();
            } else if (writeAllButton.isSelected()) {
                if (log.isDebugEnabled()) {
                    log.debug("restart writeAll");
                }
                doWrite();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "read/write end because button is lifted");
                }
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
            JOptionPane.showMessageDialog(this, SymbolicProgBundle.getMessage("ErrorDuplicateID"));
            return false;
        }

        // reload the RosterEntry
        updateDccAddress();
        _rPane.update(_rosterEntry);
        _flPane.update(_rosterEntry);
        _rMPane.update(_rosterEntry);

        // id has to be set!
        if (_rosterEntry.getId().equals("") || _rosterEntry.getId().equals(SymbolicProgBundle.getMessage("LabelNewDecoder"))) {
            log.debug("storeFile without a filename; issued dialog");
            JOptionPane.showMessageDialog(this, SymbolicProgBundle.getMessage("PromptFillInID"));
            return false;
        }

        // if there isn't a filename, store using the id
        _rosterEntry.ensureFilenameExists();
        String filename = _rosterEntry.getFileName();

        // create the RosterEntry to its file
        _rosterEntry.writeFile(cvModel, iCvModel, variableModel);

        // mark this as a success
        variableModel.setFileDirty(false);

        // and store an updated roster file
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        Roster.writeRosterFile();

        // save date changed, update
        _rPane.updateGUI(_rosterEntry);

        // show OK status
        progStatus.setText(java.text.MessageFormat.format(
                SymbolicProgBundle.getMessage("StateSaveOK"),
                new Object[]{filename}));
        return true;
    }

    /**
     * local dispose, which also invokes parent. Note that we remove the
     * components (removeAll) before taking those apart.
     */
    public void dispose() {

        if (log.isDebugEnabled()) {
            log.debug("dispose local");
        }

        // remove listeners (not much of a point, though)
        readChangesButton.removeItemListener(l1);
        writeChangesButton.removeItemListener(l2);
        readAllButton.removeItemListener(l3);
        writeAllButton.removeItemListener(l4);
        if (_programmingPane != null) {
            _programmingPane.removePropertyChangeListener(this);
        }

        // dispose the list of panes
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
        iCvModel.dispose();
        if (_rosterEntry != null) {
            _rosterEntry.setOpen(false);
        }

        // remove references to everything we remember
        progStatus = null;
        cvModel = null;
        iCvModel = null;
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

        if (log.isDebugEnabled()) {
            log.debug("dispose superclass");
        }
        removeAll();
        super.dispose();

    }

    /**
     * Set value of Preference option to show empty panes
     *
     * @param yes true if empty panes should be shown
     */
    public static void setShowEmptyPanes(boolean yes) {
        if (InstanceManager.getDefault(ProgrammerConfigManager.class) != null)
            InstanceManager.getDefault(ProgrammerConfigManager.class).setShowEmptyPanes(yes);
    }

    /**
     * get value of Preference option to show empty panes
     */
    public static boolean getShowEmptyPanes() {
        return (InstanceManager.getDefault(ProgrammerConfigManager.class) == null ) ?
            true :
            InstanceManager.getDefault(ProgrammerConfigManager.class).isShowEmptyPanes();
    }

    /**
     * Get value of whether current item should show empty panes
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
     * Option to control appearance of CV numbers in tool tips
     *
     * @param yes true is CV numbers should be shown
     */
    public static void setShowCvNumbers(boolean yes) {
        if (InstanceManager.getDefault(ProgrammerConfigManager.class) != null)
            InstanceManager.getDefault(ProgrammerConfigManager.class).setShowCvNumbers(yes);
    }

    public static boolean getShowCvNumbers() {
        return (InstanceManager.getDefault(ProgrammerConfigManager.class) == null ) ?
            true :
            InstanceManager.getDefault(ProgrammerConfigManager.class).isShowCvNumbers();
    }

    public RosterEntry getRosterEntry() {
        return _rosterEntry;
    }

    private final static Logger log = LoggerFactory.getLogger(PaneProgFrame.class.getName());

}
