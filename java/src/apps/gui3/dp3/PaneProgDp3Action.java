// PaneProgAction.java
package apps.gui3.dp3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import jmri.jmrit.XmlFile;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.AbstractValue;
import jmri.jmrit.symbolicprog.CombinedLocoSelTreePane;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.DccAddressPanel;
import jmri.jmrit.symbolicprog.DccAddressVarHandler;
import jmri.jmrit.symbolicprog.EnumVariableValue;
import jmri.jmrit.symbolicprog.IndexedCvTableModel;
import jmri.jmrit.symbolicprog.SymbolicProgBundle;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrit.symbolicprog.VariableValue;
import jmri.jmrit.symbolicprog.tabbedframe.PaneContainer;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgPane;
import jmri.jmrit.symbolicprog.tabbedframe.PaneServiceProgFrame;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriPanel;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a frame for selecting the information
 * needed to open a PaneProgFrame in service mode.
 * <P>
 * The name is a historical accident, and probably should have included
 * "ServiceMode" or something.
 * <P>
 * The resulting JFrame is constructed on the fly here, and has no specific
 * type.
 *
 * @see jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 17977 $
 */
public class PaneProgDp3Action extends jmri.util.swing.JmriAbstractAction implements jmri.ProgListener, jmri.jmrit.symbolicprog.tabbedframe.PaneContainer {

    /**
     *
     */
    private static final long serialVersionUID = 5579779133820749548L;
    Object o1, o2, o3, o4;
    JLabel statusLabel;
    jmri.jmrit.progsupport.ProgModeSelector modePane = new jmri.jmrit.progsupport.ProgServiceModeComboBox();

    public PaneProgDp3Action(String s, jmri.util.swing.WindowInterface wi) {
        super(s, wi);
        init();
    }

    public PaneProgDp3Action(String s, javax.swing.Icon i, jmri.util.swing.WindowInterface wi) {
        super(s, i, wi);
        init();
    }

    public PaneProgDp3Action() {
        this(Bundle.getMessage("Name"));  // NOI18N
    }

    public PaneProgDp3Action(String s) {
        super(s);
        init();

    }

    void init() {
        statusLabel = new JLabel(SymbolicProgBundle.getMessage("StateIdle")); // NOI18N
    }

    JmriJFrame f;
    CombinedLocoSelTreePane combinedLocoSelTree;

    public void actionPerformed(ActionEvent e) {

        log.debug("Pane programmer requested"); // NOI18N

        if (f == null) {
            log.debug("found f==null");
            // create the initial frame that steers
            f = new JmriJFrame(apps.gui3.dp3.Bundle.getMessage("FrameProgrammerSetup")); // NOI18N
            f.getContentPane().setLayout(new BorderLayout());
            // ensure status line is cleared on close so it is normal if re-opened
            f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    statusLabel.setText(SymbolicProgBundle.getMessage("StateIdle")); // NOI18N
                    f.windowClosing(we);
                }
            });

            // add the Roster menu
            JMenuBar menuBar = new JMenuBar();
            JMenu j = new JMenu(SymbolicProgBundle.getMessage("MenuFile")); // NOI18N
            j.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(SymbolicProgBundle.getMessage("MenuPrintDecoderDefinitions"), f, false)); // NOI18N
            j.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(SymbolicProgBundle.getMessage("MenuPrintPreviewDecoderDefinitions"), f, true)); // NOI18N
            menuBar.add(j);
            menuBar.add(new jmri.jmrit.roster.swing.RosterMenu(SymbolicProgBundle.getMessage("MenuRoster"), jmri.jmrit.roster.swing.RosterMenu.MAINMENU, f)); // NOI18N
            f.setJMenuBar(menuBar);
            final JPanel bottomPanel = new JPanel(new BorderLayout());
            // new Loco on programming track
            combinedLocoSelTree = new CombinedLocoSelTreePane(statusLabel, modePane) {
                /**
                 *
                 */
                private static final long serialVersionUID = 587815634507269784L;

                @Override
                protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
                        String filename) {
                    log.debug("startProgrammer");
                    String title = java.text.MessageFormat.format(SymbolicProgBundle.getMessage("FrameServiceProgrammerTitle"), // NOI18N
                            new Object[]{Bundle.getMessage("NewDecoder")}); // NOI18N
                    if (re != null) {
                        title = java.text.MessageFormat.format(SymbolicProgBundle.getMessage("FrameServiceProgrammerTitle"), // NOI18N
                                new Object[]{re.getId()});
                    }
                    JFrame p = new PaneServiceProgFrame(decoderFile, re,
                            title, "programmers" + File.separator + "Comprehensive.xml", // NOI18N
                            modePane.getProgrammer());
                    if (modePane == null || !modePane.isSelected() || modePane.getProgrammer() == null) {
                        p = new PaneProgFrame(decoderFile, re,
                                title, "programmers" + File.separator + "Comprehensive.xml", // NOI18N
                                null, false) {
                                    /**
                                     *
                                     */
                                    private static final long serialVersionUID = -4335497929570046467L;

                                    protected JPanel getModePane() {
                                        return null;
                                    }
                                };
                    }
                    p.pack();
                    p.setVisible(true);
                }

                protected void openNewLoco() {
                    log.debug("openNewLoco");
                    // find the decoderFile object
                    DecoderFile decoderFile = DecoderIndexFile.instance().fileFromTitle(selectedDecoderType());
                    if (log.isDebugEnabled()) {
                        log.debug("decoder file: " + decoderFile.getFilename()); // NOI18N
                    }
                    if (rosterIdField.getText().equals(SymbolicProgBundle.getMessage("LabelNewDecoder"))) { // NOI18N
                        re = new RosterEntry();
                        re.setDecoderFamily(decoderFile.getFamily());
                        re.setDecoderModel(decoderFile.getModel());
                        re.setId(SymbolicProgBundle.getMessage("LabelNewDecoder")); // NOI18N
                        //re.writeFile(cvModel, iCvModel, variableModel );
                        // note that we're leaving the filename null
                        // add the new roster entry to the in-memory roster
                        Roster.instance().addEntry(re);
                    } else {
                        try {
                            saveRosterEntry();
                        } catch (jmri.JmriException ex) {
                            log.warn("Exception while saving roster entry", ex); // NOI18N
                            return;
                        }
                    }
                    // create a dummy RosterEntry with the decoder info
                    startProgrammer(decoderFile, re, null);
                    //Set our roster entry back to null so that a fresh roster entry can be created if needed
                    re = null;
                }

                @Override
                protected JPanel layoutRosterSelection() {
                    log.debug("layoutRosterSelection");
                    return null;
                }

                @Override
                protected JPanel layoutDecoderSelection() {
                    log.debug("layoutDecoderSelection");
                    JPanel pan = super.layoutDecoderSelection();
                    dTree.removeTreeSelectionListener(dListener);
                    dTree.addTreeSelectionListener(dListener = new TreeSelectionListener() {
                        public void valueChanged(TreeSelectionEvent e) {
                            if (!dTree.isSelectionEmpty() && dTree.getSelectionPath() != null
                                    && // check that this isn't just a model
                                    ((TreeNode) dTree.getSelectionPath().getLastPathComponent()).isLeaf()
                                    && // can't be just a mfg, has to be at least a family
                                    dTree.getSelectionPath().getPathCount() > 2
                                    && // can't be a multiple decoder selection
                                    dTree.getSelectionCount() < 2) {
                                log.debug("Selection event with " + dTree.getSelectionPath().toString());
                                //if (locoBox != null) locoBox.setSelectedIndex(0);
                                go2.setEnabled(true);
                                go2.setRequestFocusEnabled(true);
                                go2.requestFocus();
                                go2.setToolTipText(SymbolicProgBundle.getMessage("TipClickToOpen")); // NOI18N
                                decoderFile = DecoderIndexFile.instance().fileFromTitle(selectedDecoderType());
                                setUpRosterPanel();
                            } else {
                                decoderFile = null;
                                // decoder not selected - require one
                                go2.setEnabled(false);
                                go2.setToolTipText(SymbolicProgBundle.getMessage("TipSelectLoco")); // NOI18N
                                setUpRosterPanel();
                            }
                        }
                    });
                    return pan;
                }

                protected void selectDecoder(int mfgID, int modelID, int productID) {
                    log.debug("selectDecoder");
                    //On selecting a new decoder start a fresh with a new roster entry
                    super.selectDecoder(mfgID, modelID, productID);
                    findDecoderAddress();
                }

                @Override
                protected JPanel createProgrammerSelection() {
                    log.debug("createProgrammerSelection");

                    JPanel pane3a = new JPanel();
                    pane3a.setLayout(new BoxLayout(pane3a, BoxLayout.Y_AXIS));

                    go2 = new JButton(Bundle.getMessage("OpenProgrammer")); // NOI18N
                    go2.addActionListener(new ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            log.debug("Open programmer pressed"); // NOI18N
                            openButton();
                            // close this window to prevent having
                            // two windows processing at the same time
                            //
                            // Alternately, could have just cleared out a 
                            // bunch of stuff to force starting over, but 
                            // that seems to be an even more confusing
                            // user experience.
                            log.debug("Closing f {}", f);
                            WindowEvent wev = new WindowEvent(f, WindowEvent.WINDOW_CLOSING);
                            java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
                        }
                    });
                    go2.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
                    go2.setEnabled(false);
                    go2.setToolTipText(SymbolicProgBundle.getMessage("SELECT A LOCOMOTIVE OR DECODER TO ENABLE")); // NOI18N
                    bottomPanel.add(go2, BorderLayout.EAST);

                    return pane3a;
                }
            };

            // load primary frame
            JPanel topPanel = new JPanel();
            topPanel.add(modePane);
            topPanel.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
            f.getContentPane().add(topPanel, BorderLayout.NORTH);
            //f.getContentPane().add(modePane);
            //f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

            combinedLocoSelTree.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            f.getContentPane().add(combinedLocoSelTree, BorderLayout.CENTER);

            //f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
            //basicRoster.setEnabled(false);
            statusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            bottomPanel.add(statusLabel, BorderLayout.SOUTH);
            f.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

            f.pack();
            if (log.isDebugEnabled()) {
                log.debug("Tab-Programmer setup created"); // NOI18N
            }
        } else {
            re = null;
            combinedLocoSelTree.resetSelections();
        }
        f.setVisible(true);
    }

    String lastSelectedProgrammer = this.getClass().getName() + ".SelectedProgrammer"; // NOI18N

    // never invoked, because we overrode actionPerformed above
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked"); // NOI18N
    }

    JTextField rosterIdField = new JTextField(20);
    JTextField rosterAddressField = new JTextField(10);

    RosterEntry re;

    int teststatus = 0;

    synchronized void findDecoderAddress() {
        teststatus = 1;
        readCV(29);
    }

    DecoderFile decoderFile;
    boolean shortAddr = false;
    int cv29 = 0;
    int cv17 = -1;
    int cv18 = -1;
    int cv19 = 0;
    int cv1 = 0;
    int longAddress;
    String address = "3";

    synchronized public void programmingOpReply(int value, int status) {
        switch (teststatus) {
            case 1:
                teststatus = 2;
                cv29 = value;
                readCV(1);
                break;
            case 2:
                teststatus = 3;
                cv1 = value;
                readCV(17);
                break;
            case 3:
                teststatus = 4;
                cv17 = value;
                readCV(18);
                break;
            case 4:
                teststatus = 5;
                cv18 = value;
                readCV(19);
                break;
            case 5:
                cv19 = value;
                finishRead();
                break;
            default:
                log.error("unknown test state " + teststatus);
                break;
        }
    }

    synchronized void finishRead() {
        if ((cv29 & 0x20) == 0) {
            shortAddr = true;
            address = "" + cv1;
        }
        if (cv17 != -1 || cv18 != -1) {
            longAddress = (cv17 & 0x3f) * 256 + cv18;
            address = "" + longAddress;
        }
        if (progPane != null) {
            progPane.setVariableValue("Short Address", cv1); // NOI18N
            progPane.setVariableValue("Long Address", longAddress); // NOI18N
            progPane.setCVValue("29", cv29);
            progPane.setCVValue("19", cv19);
        }
    }

    protected void readCV(int cv) {
        jmri.Programmer p = jmri.InstanceManager.programmerManagerInstance().getGlobalProgrammer();
        if (p == null) {
            //statusUpdate("No programmer connected");
        } else {
            try {
                p.readCV(cv, this);
            } catch (jmri.ProgrammerException ex) {
                //statusUpdate(""+ex);
            }
        }
    }
    JPanel rosterPanel = null;//new JPanel();
    jmri.Programmer mProgrammer;
    CvTableModel cvModel = null;
    IndexedCvTableModel iCvModel = null;
    VariableTableModel variableModel;
    DccAddressPanel dccAddressPanel;
    Element modelElem = null;
    ThisProgPane progPane = null;

    synchronized void setUpRosterPanel() {
        re = null;
        if (rosterPanel == null) {
            rosterPanel = new JPanel();
            rosterPanel.setLayout(new BorderLayout());
            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("RosterId"))); // NOI18N
            p.add(rosterIdField);
            rosterPanel.add(p, BorderLayout.NORTH);
            rosterIdField.setText(SymbolicProgBundle.getMessage("LabelNewDecoder")); // NOI18N
            saveBasicRoster = new JButton(Bundle.getMessage("Save")); // NOI18N
            saveBasicRoster.addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        log.debug("saveBasicRoster button pressed, calls saveRosterEntry");
                        saveRosterEntry();
                    } catch (jmri.JmriException ex) {
                        return;
                    }
                }
            });
            TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
            border.setTitle(Bundle.getMessage("CreateBasicRosterEntry")); // NOI18N
            rosterPanel.setBorder(border);
            rosterPanel.setVisible(false);
            f.getContentPane().add(rosterPanel, BorderLayout.EAST);
        } else {
            rosterIdField.setText(SymbolicProgBundle.getMessage("LabelNewDecoder")); // NOI18N
        }
        if (progPane != null) {
            progPane.dispose();
            rosterPanel.remove(progPane);
            progPane = null;
            rosterPanel.revalidate();
            f.getContentPane().repaint();
            f.repaint();
            f.pack();
        }
        if (jmri.InstanceManager.programmerManagerInstance() != null
                && jmri.InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()) {
            this.mProgrammer = jmri.InstanceManager.programmerManagerInstance().getGlobalProgrammer();
        }

        cvModel = new CvTableModel(statusLabel, mProgrammer);
        iCvModel = new IndexedCvTableModel(statusLabel, mProgrammer);

        variableModel = new VariableTableModel(statusLabel, new String[]{"Name", "Value"},
                cvModel, iCvModel);
        if (decoderFile != null) {
            Element decoderRoot = null;
            try {
                decoderRoot = decoderFile.rootFromName(DecoderFile.fileLocation + decoderFile.getFilename());
            } catch (Exception e) {
                log.error("Exception while loading decoder XML file: " + decoderFile.getFilename(), e);
                return;
            } // NOI18N
            modelElem = decoderFile.getModelElement();
            decoderFile.loadVariableModel(decoderRoot.getChild("decoder"), variableModel); // NOI18N
            rosterPanel.setVisible(true);
        } else {
            rosterPanel.setVisible(false);
            return;
        }
        Element programmerRoot;
        XmlFile pf = new XmlFile() {
        };  // XmlFile is abstract

        java.beans.PropertyChangeListener dccNews = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                updateDccAddress();
            }
        };
        primaryAddr = variableModel.findVar("Short Address"); // NOI18N

        if (primaryAddr == null) {
            log.debug("DCC Address monitor didnt find a Short Address variable"); // NOI18N
        } else {
            primaryAddr.addPropertyChangeListener(dccNews);
        }
        extendAddr = variableModel.findVar("Long Address"); // NOI18N
        if (extendAddr == null) {
            log.debug("DCC Address monitor didnt find an Long Address variable"); // NOI18N
        } else {
            extendAddr.addPropertyChangeListener(dccNews);
        }
        addMode = (EnumVariableValue) variableModel.findVar("Address Format"); // NOI18N
        if (addMode == null) {
            log.debug("DCC Address monitor didnt find an Address Format variable"); // NOI18N
        } else {
            addMode.addPropertyChangeListener(dccNews);
        }

        try {
            programmerRoot = pf.rootFromName("programmers" + File.separator + "Basic.xml"); // NOI18N
            Element base;
            if ((base = programmerRoot.getChild("programmer")) == null) { // NOI18N
                log.error("xml file top element is not programmer"); // NOI18N
                return;
            }
            // for all "pane" elements in the programmer
            List<Element> paneList = base.getChildren("pane"); // NOI18N
            if (log.isDebugEnabled()) {
                log.debug("will process " + paneList.size() + " pane definitions"); // NOI18N
            }
            String name = jmri.util.jdom.LocaleSelector.getAttribute(paneList.get(0), "name");
            progPane = new ThisProgPane(this, name, paneList.get(0), cvModel, iCvModel, variableModel, modelElem);

            progPane.setVariableValue("Short Address", cv1); // NOI18N
            progPane.setVariableValue("Long Address", longAddress); // NOI18N
            progPane.setCVValue("29", cv29); // NOI18N
            progPane.setCVValue("19", cv19); // NOI18N
            rosterPanel.add(progPane, BorderLayout.CENTER);
            rosterPanel.revalidate();
            rosterPanel.setVisible(true);
            f.getContentPane().repaint();
            f.repaint();
            f.pack();
            return;
        } catch (Exception e) {
            log.error("exception reading programmer file: ", e); // NOI18N
        }
    }

    boolean longMode = false;
    String newAddr = null;

    void updateDccAddress() {

        if (log.isDebugEnabled()) {
            log.debug("updateDccAddress: short " + (primaryAddr == null ? "<null>" : primaryAddr.getValueString()) + // NOI18N
                    " long " + (extendAddr == null ? "<null>" : extendAddr.getValueString()) + // NOI18N
                    " mode " + (addMode == null ? "<null>" : addMode.getValueString())); // NOI18N
        }
        new DccAddressVarHandler(primaryAddr, extendAddr, addMode) {
            protected void doPrimary() {
                longMode = false;
                if (primaryAddr != null && !primaryAddr.getValueString().equals("")) {
                    newAddr = primaryAddr.getValueString();
                }
            }

            protected void doExtended() {
                // long address
                if (!extendAddr.getValueString().equals("")) {
                    longMode = true;
                    newAddr = extendAddr.getValueString();
                }
            }
        };
        // update if needed
        if (newAddr != null) {
            synchronized (this) {
                // store DCC address, type
                address = newAddr;
                shortAddr = !longMode;
            }
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "IS2_INCONSISTENT_SYNC", // NOI18N
            justification = "saveBasicRoster protected by ctor invocation, Swing thread when needed")  // NOI18N
    JButton saveBasicRoster;

    /**
     *
     * @return true if the value in the id JTextField is a duplicate of some
     *         other RosterEntry in the roster
     */
    boolean checkDuplicate() {
        // check its not a duplicate
        List<RosterEntry> l = Roster.instance().matchingList(null, null, null, null, null, null, rosterIdField.getText());
        boolean oops = false;
        for (int i = 0; i < l.size(); i++) {
            if (re != l.get(i)) {
                oops = true;
            }
        }
        return oops;
    }

    void saveRosterEntry() throws jmri.JmriException {
        log.debug("saveRosterEntry");
        if (rosterIdField.getText().equals(SymbolicProgBundle.getMessage("LabelNewDecoder"))) { // NOI18N
            synchronized (this) {
                JOptionPane.showMessageDialog(progPane, SymbolicProgBundle.getMessage("PromptFillInID")); // NOI18N
            }
            throw new jmri.JmriException("No Roster ID"); // NOI18N
        }
        if (checkDuplicate()) {
            synchronized (this) {
                JOptionPane.showMessageDialog(progPane, SymbolicProgBundle.getMessage("ErrorDuplicateID")); // NOI18N
            }
            throw new jmri.JmriException("Duplicate ID"); // NOI18N
        }

        if (re == null) {
            log.debug("re null, creating RosterEntry");
            re = new RosterEntry();
            re.setDecoderFamily(decoderFile.getFamily());
            re.setDecoderModel(decoderFile.getModel());
            re.setId(rosterIdField.getText());
            Roster.instance().addEntry(re);
        }

        updateDccAddress();

        // if there isn't a filename, store using the id
        re.ensureFilenameExists();
        String filename = re.getFileName();

        // create the RosterEntry to its file
        log.debug("setting DCC address {} {}", address, shortAddr);
        synchronized (this) {
            re.setDccAddress("" + address);  // NOI18N
            re.setLongAddress(!shortAddr);
            re.writeFile(cvModel, iCvModel, variableModel);

            // mark this as a success
            variableModel.setFileDirty(false);
        }
        // and store an updated roster file
        FileUtil.createDirectory(FileUtil.getUserFilesPath());
        Roster.writeRosterFile();

        // show OK status
        statusLabel.setText(java.text.MessageFormat.format(
                SymbolicProgBundle.getMessage("StateSaveOK"), // NOI18N
                new Object[]{filename}));
    }

    // hold refs to variables to check dccAddress
    VariableValue primaryAddr = null;
    VariableValue extendAddr = null;
    EnumVariableValue addMode = null;

    public boolean isBusy() {
        return false;
    }

    public void paneFinished() {
    }

    /**
     * Enable the read/write buttons.
     * <p>
     * In addition, if a programming mode pane is present, it's "set" button is
     * enabled.
     *
     * @param enable Are reads possible? If false, so not enable the read
     *               buttons.
     */
    public void enableButtons(boolean enable) {
    }

    public void prepGlassPane(javax.swing.AbstractButton activeButton) {
    }

    synchronized public jmri.util.BusyGlassPane getBusyGlassPane() {
        return new jmri.util.BusyGlassPane(new ArrayList<JComponent>(),
                new ArrayList<Rectangle>(),
                rosterPanel, f);
    }

    class ThisProgPane extends PaneProgPane {

        /**
         *
         */
        private static final long serialVersionUID = 8855795459526662034L;

        public ThisProgPane(PaneContainer parent, String name, Element pane, CvTableModel cvModel, IndexedCvTableModel icvModel, VariableTableModel varModel, Element modelElem) {
            super(parent, name, pane, cvModel, icvModel, varModel, modelElem, re);
            bottom.remove(readChangesButton);
            bottom.remove(writeChangesButton);
            writeAllButton.setText(SymbolicProgBundle.getMessage("ButtonWrite")); // NOI18N
            readAllButton.setText(SymbolicProgBundle.getMessage("ButtonRead")); // NOI18N
            bottom.add(saveBasicRoster);
            bottom.revalidate();
            readAllButton.removeItemListener(l2);
            readAllButton.addItemListener(l2 = new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        readAllButton.setText(SymbolicProgBundle.getMessage("ButtonStopReadSheet")); // NOI18N
                        if (container.isBusy() == false) {
                            prepReadPane(false);
                            prepGlassPane(readAllButton);
                            container.getBusyGlassPane().setVisible(true);
                            readPaneAll();
                        }
                    } else {
                        stopProgramming();
                        readAllButton.setText(SymbolicProgBundle.getMessage("ButtonRead")); // NOI18N
                        if (container.isBusy()) {
                            readAllButton.setEnabled(false);
                        }
                    }
                }
            });
            writeAllButton.removeItemListener(l4);
            writeAllButton.addItemListener(l4 = new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        writeAllButton.setText(SymbolicProgBundle.getMessage("ButtonStopWriteSheet")); // NOI18N
                        if (container.isBusy() == false) {
                            prepWritePane(false);
                            prepGlassPane(writeAllButton);
                            container.getBusyGlassPane().setVisible(true);
                            writePaneAll();
                        }
                    } else {
                        stopProgramming();
                        writeAllButton.setText(SymbolicProgBundle.getMessage("ButtonWrite")); // NOI18N
                        if (container.isBusy()) {
                            writeAllButton.setEnabled(false);
                        }
                    }
                }
            });
            if (_cvModel.getProgrammer() == null) {
                bottom.remove(readAllButton);
                bottom.remove(writeAllButton);
                bottom.revalidate();
                add(bottom);
            }
        }

        public void setCVValue(String cv, int value) {
            if (_cvModel.getCvByNumber(cv) != null) {
                (_cvModel.getCvByNumber(cv)).setValue(value);
                (_cvModel.getCvByNumber(cv)).setState(AbstractValue.READ);
            }
        }

        public void setVariableValue(String variable, int value) {
            if (_varModel.findVar(variable) != null) {
                _varModel.findVar(variable).setIntValue(value);
                _varModel.findVar(variable).setState(AbstractValue.READ);
            }
        }

        public void dispose() {
            bottom.remove(saveBasicRoster);
            super.dispose();
        }

    }

    private final static Logger log = LoggerFactory.getLogger(PaneProgDp3Action.class.getName());

}

/* @(#)PaneProgAction.java */
