// PaneProgFrame.java

package jmri.jmrit.symbolicprog.tabbedframe;

import jmri.Programmer;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.jmrit.XmlFile;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntryPane;
import jmri.jmrit.symbolicprog.*;
import jmri.util.JmriJFrame;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Frame providing a command station programmer from decoder definition files.
 * @author	Bob Jacobsen Copyright (C) 2001, 2004, 2005
 * @author  D Miller Copyright 2003
 * @version $Revision: 1.47 $
 */
abstract public class PaneProgFrame extends JmriJFrame
							implements java.beans.PropertyChangeListener  {

    // members to contain working variable, CV values
    JLabel              progStatus     	= new JLabel("idle");
    CvTableModel        cvModel         = null;
    VariableTableModel  variableModel;
    Programmer          mProgrammer;

    RosterEntry         _rosterEntry    = null;
    RosterEntryPane     _rPane          = null;

    List                paneList        = new ArrayList();

    String              filename        = null;

    // GUI member declarations
    JTabbedPane tabPane = new JTabbedPane();
    JToggleButton readChangesButton = new JToggleButton("Read changes on all sheets");
    JToggleButton writeChangesButton = new JToggleButton("Write changes on all sheets");
    JToggleButton readAllButton = new JToggleButton("Read all sheets");
    JToggleButton writeAllButton = new JToggleButton("Write all sheets");

    ActionListener l1;
    ActionListener l2;
    ActionListener l3;
    ActionListener l4;

    /**
     * Abstract method to provide a JPanel setting the programming
     * mode, if appropriate. A null value is ignored.
     */
    abstract JPanel getModePane();

    protected void installComponents() {

        // Create a menu bar
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // add a "File" menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // Add a save item
        fileMenu.add(new AbstractAction("Save...") {
            public void actionPerformed(ActionEvent e) {
                storeFile();
            }
        });

        JMenu printSubMenu = new JMenu("Print");
        printSubMenu.add(new PrintAction("All ...", this));
        printSubMenu.add(new PrintCvAction("CVs ...", cvModel, this));
        fileMenu.add(printSubMenu);

        // add "Import" submenu; this is heirarchical because
        // some of the names are so long, and we expect more formats
        JMenu importSubMenu = new JMenu("Import");
        fileMenu.add(importSubMenu);
        importSubMenu.add(new Pr1ImportAction("PR1 file...", cvModel, this));

        // add "Export" submenu; this is heirarchical because
        // some of the names are so long, and we expect more formats
        JMenu exportSubMenu = new JMenu("Export");
        fileMenu.add(exportSubMenu);
        exportSubMenu.add(new CsvExportAction("CSV file...", cvModel, this));
        exportSubMenu.add(new Pr1ExportAction("PR1DOS file...", cvModel, this));
        exportSubMenu.add(new Pr1WinExportAction("PR1WIN file...", cvModel, this));

        // to control size, we need to insert a single
        // JPanel, then have it laid out with BoxLayout
        JPanel pane = new JPanel();

        // general GUI config
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        // configure GUI elements
        readChangesButton.setToolTipText("Read highlighted values on all sheets from decoder. Warning: may take a long time!");
        // check with CVTable programmer to see if read is possible
        if (cvModel!= null && cvModel.getProgrammer()!= null
            && !cvModel.getProgrammer().getCanRead()) {
            // can't read, disable the button
            readChangesButton.setEnabled(false);
            readChangesButton.setToolTipText("Button disabled because configured command station can't read CVs");
        }
        readChangesButton.addActionListener( l1 = new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (readChangesButton.isSelected()) readChanges();
                }
            });
        readAllButton.setToolTipText("Read all values on all sheets from decoder. Warning: may take a long time!");
        // check with CVTable programmer to see if read is possible
        if (cvModel!= null && cvModel.getProgrammer()!= null
            && !cvModel.getProgrammer().getCanRead()) {
            // can't read, disable the button
            readAllButton.setEnabled(false);
            readAllButton.setToolTipText("Button disabled because configured command station can't read CVs");
        }
        readAllButton.addActionListener( l3 = new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (readAllButton.isSelected()) {
                        readAll();
                    }
                }
            });
        writeChangesButton.setToolTipText("Write highlighted values on all sheets to decoder");
        writeChangesButton.addActionListener( l2 = new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (writeChangesButton.isSelected()) writeChanges();
                }
            });
        writeAllButton.setToolTipText("Write all values on all sheets to decoder");
        writeAllButton.addActionListener( l4 = new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (writeAllButton.isSelected()) {
                        writeAll();
                    }
                }
            });

        // most of the GUI is done from XML in readConfig() function
        // which configures the tabPane
        pane.add(tabPane);

        // add buttons
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(readChangesButton);
        bottom.add(writeChangesButton);
        bottom.add(readAllButton);
        bottom.add(writeAllButton);
        pane.add(bottom);

        JPanel modePane = getModePane();
        if (modePane!=null) {
            pane.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
            pane.add(modePane);
        }

        pane.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
        progStatus.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        pane.add(progStatus);

        // and put that pane into the JFrame
        getContentPane().add(pane);
    }

    public Dimension getPreferredSize() {
        Dimension screen = getMaximumSize();
        int width = Math.min(super.getPreferredSize().width, screen.width);
        int height = Math.min(super.getPreferredSize().height, screen.height);
        return new Dimension(width, height);
    }

    public Dimension getMaximumSize() {
        Dimension screen = getToolkit().getScreenSize();
        return new Dimension(screen.width, screen.height-35);
    }

    /**
     * Initialization sequence:
     * <UL>
     * <LI> Ask the RosterEntry to read its contents
     * <LI> If the decoder file is specified, open and load it, otherwise
     *		get the decoder filename from the RosterEntry and load that.
     *		Note that we're assuming the roster entry has the right decoder,
     *		at least w.r.t. the loco file.
     * <LI> Fill CV values from the roster entry
     * <LI> Create the programmer panes
     * </UL>
     * @param pDecoderFile       XML file defining the decoder contents
     * @param pRosterEntry      RosterEntry for information on this locomotive
     * @param pFrameTitle       Name/title for the frame
     * @param pProgrammerFile   Name of the programmer file to use
     * @param pProg             Programmer object to be used to access CVs
     */
    public PaneProgFrame(DecoderFile pDecoderFile, RosterEntry pRosterEntry,
                        String pFrameTitle, String pProgrammerFile, Programmer pProg) {
        super(pFrameTitle);

        // create the tables
        mProgrammer     = pProg;
        cvModel         = new CvTableModel(progStatus, mProgrammer);
        variableModel	= new VariableTableModel(progStatus,
                                                                 new String[]  {"Name", "Value"},
                                                                 cvModel);

        // handle the roster entry
        _rosterEntry =  pRosterEntry;
        if (_rosterEntry == null) log.error("null RosterEntry pointer");
        filename = pProgrammerFile;
        installComponents();

        if (_rosterEntry.getFileName() != null) {
            // set the loco file name in the roster entry
            _rosterEntry.readFile();  // read, but don't yet process
        }

        if (pDecoderFile != null) loadDecoderFile(pDecoderFile);
        else			 loadDecoderFromLoco(pRosterEntry);

        // save default values
        saveDefaults();

        // finally fill the CV values from the specific loco file
        if (_rosterEntry.getFileName() != null) _rosterEntry.loadCvModel(cvModel);

        // mark file state as consistent
        variableModel.setFileDirty(false);

        // and build the GUI
        loadProgrammerFile(pRosterEntry);

        // optionally, add extra panes from the decoder file
        Attribute a;
        if ( (a = programmerRoot.getChild("programmer").getAttribute("decoderFilePanes")) != null
             && a.getValue().equals("yes")) {
	    if (decoderRoot != null) {
	        List paneList = decoderRoot.getChildren("pane");
                if (log.isDebugEnabled()) log.debug("will process "+paneList.size()+" pane definitions from decoder file");
		for (int i=0; i<paneList.size(); i++) {
		    // load each pane
                    String pname = ((Element)(paneList.get(i))).getAttribute("name").getValue();
                    newPane( pname, ((Element)(paneList.get(i))), modelElem, true);  // show even if empty
                }
            }
        }

        // ensure cleanup at end
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    thisWindowClosing(e);
                }
            });

        pack();

        if (log.isDebugEnabled()) log.debug("PaneProgFrame \""+pFrameTitle
                                            +"\" constructed for file "+_rosterEntry.getFileName()
                                            +", unconstrained size is "+super.getPreferredSize()
                                            +", constrained to "+getPreferredSize());
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
        if (log.isDebugEnabled()) log.debug("selected loco uses decoder "+decoderFamily+" "+decoderModel);
        // locate a decoder like that.
        List l = DecoderIndexFile.instance().matchingDecoderList(null, decoderFamily, null, null, decoderModel);
        if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches");
        if (l.size() == 0) {
            log.debug("Loco uses "+decoderFamily+" "+decoderModel+" decoder, but no such decoder defined");
            // fall back to use just the decoder name, not family
            l = DecoderIndexFile.instance().matchingDecoderList(null, null, null, null, decoderModel);
            if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches without family key");
        }
        if (l.size() > 0) {
            DecoderFile d = (DecoderFile)l.get(0);
            loadDecoderFile(d);
        } else {
            if (decoderModel.equals(""))
                log.debug("blank decoderModel requested, so nothing loaded");
            else
                log.warn("no matching \""+decoderModel+"\" decoder found for loco, no decoder info loaded");
        }
    }

    protected void loadDecoderFile(DecoderFile df) {
        if (df == null) {
            log.warn("loadDecoder file invoked with null object");
            return;
        }
        if (log.isDebugEnabled()) log.debug("loadDecoderFile from "+df.fileLocation
                                        +" "+df.getFilename());

        try {
            decoderRoot = df.rootFromName(df.fileLocation+df.getFilename());
        } catch (Exception e) { log.error("Exception while loading decoder XML file: "+df.getFilename()+" exception: "+e); }
        // load variables from decoder tree
        df.loadVariableModel(decoderRoot.getChild("decoder"), variableModel);

        // save the pointer to the model element
        modelElem = df.getModelElement();
    }

    protected void loadProgrammerFile(RosterEntry r) {
        // Open and parse programmer file
        XmlFile pf = new XmlFile(){};  // XmlFile is abstract
        try {
            programmerRoot = pf.rootFromName(filename);

            // load programmer config from programmer tree
            readConfig(programmerRoot, r);
        }
        catch (Exception e) {
            log.error("exception reading programmer file: "+filename+" exception: "+e);
            // provide traceback too
            e.printStackTrace();
        }
    }

    Element programmerRoot = null;

    // handle resizing when first shown
    private boolean mShown = false;
    public void addNotify() {
        super.addNotify();
        if (mShown)
            return;
        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }

    /**
     * Close box has been clicked; handle check for dirty with respect to
     * decoder or file, then close.
     * @param e Not used
     */
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        // check for various types of dirty - first table data not written back
        if (log.isDebugEnabled()) log.debug("Checking decoder dirty status. CV: "+cvModel.decoderDirty()+" variables:"+variableModel.decoderDirty());
        if (cvModel.decoderDirty() || variableModel.decoderDirty() ) {
            if (JOptionPane.showConfirmDialog(null,
                                              "Some changes have not been written to the decoder. They will be lost. Close window?",
                                              "choose one", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) return;
        }
        if (variableModel.fileDirty() || _rPane.guiChanged(_rosterEntry)) {
            int option = JOptionPane.showOptionDialog(null,"Some changes have not been written to a configuration file. Close window?",
                        "Choose one",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
                        new String[]{"Save and Close", "Close", "Cancel"}, "Save and Close");
            if (option==0) {
                // save requested
                if (!storeFile()) return;   // don't close if failed
            } else if (option ==2) {
                // cancel requested
                return; // without doing anything
            }
        }
        // Check for a "<new loco>" roster entry; if found, remove it
        List l = Roster.instance().matchingList(null, null, null, null, null, null, "<new loco>");
        if (l.size() > 0 && log.isDebugEnabled()) log.debug("Removing "+l.size()+" <new loco> entries");
        while (l.size() > 0 ) {
            Roster.instance().removeEntry((RosterEntry)l.get(0));
            l = Roster.instance().matchingList(null, null, null, null, null, null, "<new loco>");
        }
        //OK, close
        setVisible(false);
        dispose();
    }

    void readConfig(Element root, RosterEntry r) {
        // check for "programmer" element at start
        Element base;
        if ( (base = root.getChild("programmer")) == null) {
            log.error("xml file top element is not programmer");
            return;
        }

        // add the Info tab
        tabPane.addTab("Roster Entry", makeInfoPane(r));

        // for all "pane" elements in the programmer
        List paneList = base.getChildren("pane");
        if (log.isDebugEnabled()) log.debug("will process "+paneList.size()+" pane definitions");
        for (int i=0; i<paneList.size(); i++) {
            // load each pane
            String name = ((Element)(paneList.get(i))).getAttribute("name").getValue();
            newPane( name, ((Element)(paneList.get(i))), modelElem, false);  // dont force showing if empty
        }

    }

    /**
     * reset all CV values to defaults stored earlier.  This will in turn update
     * the variables
     */
    protected void resetToDefaults() {
        int n = defaultCvValues.length;
        for (int i=0; i<n; i++) {
            CvValue cv = cvModel.getCvByNumber(defaultCvNumbers[i]);
            if (cv == null) log.warn("Trying to set default in CV "+defaultCvNumbers[i]
                                     +" but didn't find the CV object");
            else cv.setValue(defaultCvValues[i]);
        }
    }

    int defaultCvValues[] = null;
    int defaultCvNumbers[] = null;

    /**
     * Save all CV values.  These stored values are used by
     * resetToDefaults
     */
    protected void saveDefaults() {
        int n = cvModel.getRowCount();
        defaultCvValues = new int[n];
        defaultCvNumbers = new int[n];

        for (int i=0; i<n; i++) {
            CvValue cv = cvModel.getCvByRow(i);
            defaultCvValues[i] = cv.getValue();
            defaultCvNumbers[i] = cv.number();
        }
    }

    protected JPanel makeInfoPane(RosterEntry r) {
        // create the identification pane (not configured by file now; maybe later?
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // add roster info
        _rPane = new RosterEntryPane(r);
        _rPane.setMaximumSize(_rPane.getPreferredSize());
        body.add(_rPane);

        // add the store button
        JButton store = new JButton("Save");
        store.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        store.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    storeFile();
                }
            });

        // add the reset button
        JButton reset = new JButton(" Reset to defaults ");
        reset.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        store.setPreferredSize(reset.getPreferredSize());
        reset.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    resetToDefaults();
                }
            });

        store.setPreferredSize(reset.getPreferredSize());
        body.add(store);
        body.add(reset);

        // arrange for the dcc address to be updated
        java.beans.PropertyChangeListener dccNews = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) { updateDccAddress(); }
            };
        primaryAddr = variableModel.findVar("Short Address");
        if (primaryAddr==null) log.debug("DCC Address monitor didnt find a Short Address variable");
        else primaryAddr.addPropertyChangeListener(dccNews);
        extendAddr = variableModel.findVar("Long Address");
        if (extendAddr==null) log.debug("DCC Address monitor didnt find an Long Address variable");
        else extendAddr.addPropertyChangeListener(dccNews);
        addMode = variableModel.findVar("Address Format");
        if (addMode==null) log.debug("DCC Address monitor didnt find an Address Format variable");
        else addMode.addPropertyChangeListener(dccNews);

        return body;
    }

    // hold refs to variables to check dccAddress
    VariableValue primaryAddr = null;
    VariableValue extendAddr = null;
    VariableValue addMode = null;

    void updateDccAddress() {
        if (log.isDebugEnabled())
            log.debug("updateDccAddress: short "+(primaryAddr==null?"<null>":primaryAddr.getValueString())+
                      " long "+(extendAddr==null?"<null>":extendAddr.getValueString())+
                      " mode "+(addMode==null?"<null>":addMode.getValueString()));
        String newAddr = null;
        if (addMode == null || extendAddr == null || !addMode.getValueString().equals("1")) {
            // short address mode
            if (primaryAddr != null && !primaryAddr.getValueString().equals(""))
                newAddr = primaryAddr.getValueString();
        }
        else {
            // long address
            if (extendAddr != null && !extendAddr.getValueString().equals(""))
                newAddr = extendAddr.getValueString();
        }
        // update if needed
        if (newAddr!=null) _rPane.setDccAddress(newAddr);
    }

    public void newPane(String name, Element pane, Element modelElem, boolean enableEmpty) {

        // create a panel to hold columns
        PaneProgPane p = new PaneProgPane(name, pane, cvModel, variableModel, modelElem);

        // how to handle the tab depends on whether it has contents and option setting
        if ( enableEmpty || (p.cvList.size()!=0) || (p.varList.size()!=0) ) {
            tabPane.addTab(name, p);  // always add if not empty
        } else if (getShowEmptyPanes()) {
            // here empty, but showing anyway as disabled
            tabPane.addTab(name, p);
            int index = tabPane.indexOfTab(name);
            tabPane.setEnabledAt(index, false);
            jmri.util.JTabbedPaneUtil.setToolTipTextAt(tabPane, index,
                    "Tab disabled because there are no options in this category");
        } else {
            // here not showing tab at all
        }

        // and remember it for programming
        paneList.add(p);
    }

    boolean justChanges;

    /**
     * invoked by "Read Changes" button, this sets in motion a
     * continuing sequence of "read changes" operations on the
     * panes. Each invocation of this method reads one pane; completion
     * of that request will cause it to happen again, reading the next pane, until
     * there's nothing left to read.
     * <P>
     * @return true if a read has been started, false if the operation is complete.
     */
    public boolean readChanges() {
        if (log.isDebugEnabled()) log.debug("readChanges starts");
        justChanges = true;
        return doRead();
    }

    /**
     * invoked by "Read All" button, this sets in motion a
     * continuing sequence of "read all" operations on the
     * panes. Each invocation of this method reads one pane; completion
     * of that request will cause it to happen again, reading the next pane, until
     * there's nothing left to read.
     * <P>
     * @return true if a read has been started, false if the operation is complete.
     */
    public boolean readAll() {
        if (log.isDebugEnabled()) log.debug("readAll starts");
        justChanges = false;
        
        // prepare for common reads by doing a compare on all panes
        for (int i=0; i<paneList.size(); i++) {
            if (log.isDebugEnabled()) log.debug("doPrep on "+i);
            ((PaneProgPane)paneList.get(i)).prepReadPaneAll();
        }
        // start operation
        return doRead();
    }

    boolean doRead() {
        _read = true;
        for (int i=0; i<paneList.size(); i++) {
            if (log.isDebugEnabled()) log.debug("doRead on "+i);
            _programmingPane = (PaneProgPane)paneList.get(i);
            // some programming operations are instant, so need to have listener registered at readPaneAll
            _programmingPane.addPropertyChangeListener(this);
            boolean running;
            if (justChanges)
                running = _programmingPane.readPaneChanges();
            else
                running = _programmingPane.readPanesFull();

            if (running) {
				// operation in progress, stop loop until called back
                if (log.isDebugEnabled()) log.debug("doRead expecting callback from readPane "+i);
                return true;
            }
            else
                _programmingPane.removePropertyChangeListener(this);
        }
        // nothing to program, end politely
        _programmingPane = null;
        readChangesButton.setSelected(false);
        readAllButton.setSelected(false);
        if (log.isDebugEnabled()) log.debug("doRead found nothing to do");
        return false;
    }

    /**
     * invoked by "Write All" button, this sets in motion a
     * continuing sequence of "write all" operations on each pane.
     * Each invocation of this method writes one pane; completion
     * of that request will cause it to happen again, writing the next pane, until
     * there's nothing left to write.
     * <P>
     * @return true if a write has been started, false if the operation is complete.
     */
    public boolean writeAll() {
        if (log.isDebugEnabled()) log.debug("writeAll starts");
        justChanges = false;

        // prepare for common writes by doing a compare on all panes
        for (int i=0; i<paneList.size(); i++) {
            if (log.isDebugEnabled()) log.debug("doPrep on "+i);
            ((PaneProgPane)paneList.get(i)).prepWritePaneAll();
        }
        return doWrite();
    }

    /**
     * invoked by "Write Changes" button, this sets in motion a
     * continuing sequence of "write changes" operations on each pane.
     * Each invocation of this method writes one pane; completion
     * of that request will cause it to happen again, writing the next pane, until
     * there's nothing left to write.
     * <P>
     * @return true if a write has been started, false if the operation is complete.
     */
    public boolean writeChanges() {
        if (log.isDebugEnabled()) log.debug("writeChanges starts");
        justChanges = true;
        return doWrite();
    }

    boolean doWrite() {
        _read = false;
        for (int i=0; i<paneList.size(); i++) {
            if (log.isDebugEnabled()) log.debug("writeChanges calls writePaneAll on "+i);
            _programmingPane = (PaneProgPane)paneList.get(i);
            // some programming operations are instant, so need to have listener registered at readPane
            _programmingPane.addPropertyChangeListener(this);
           boolean running;
            if (justChanges)
                running = _programmingPane.writePaneChanges();
            else
                running = _programmingPane.writePanesFull();

            if (running) {
				// operation in progress, stop loop until called back
                if (log.isDebugEnabled()) log.debug("writeChanges expecting callback from writePane "+i);
                return true;
            }
            else
                _programmingPane.removePropertyChangeListener(this);
        }
        // nothing to program, end politely
        _programmingPane = null;
        writeChangesButton.setSelected(false);
        writeAllButton.setSelected(false);
        if (log.isDebugEnabled()) log.debug("writeChanges found nothing to do");
        return false;
    }

    public void printPanes(HardcopyWriter w) {
        printInfoSection(w);
        try {
           String s = "\n\n";
           w.write(s, 0, s.length());
         }
         catch (IOException e) {
           log.error("Error printing Info Section: " + e);
         }

        for (int i=0; i<paneList.size(); i++) {
            if (log.isDebugEnabled()) log.debug("start printing page "+i);
            PaneProgPane pane = (PaneProgPane)paneList.get(i);
            pane.printPane(w);
        }
        w.write(w.getCurrentLineNumber(),0,w.getCurrentLineNumber(),w.getCharactersPerLine() + 1);
    }

    public void printInfoSection(HardcopyWriter w) {
        ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("resources/decoderpro.gif"));
        // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
        w.write(icon.getImage(), new JLabel(icon));
        w.setFontStyle(Font.BOLD);
        _rosterEntry.printEntry(w);
        w.setFontStyle(Font.PLAIN);
    }

    boolean _read = true;
    PaneProgPane _programmingPane = null;

    /**
     * get notification of a variable property change in the pane, specifically "busy" going to
     * false at the end of a programming operation
     * @param e Event, used to find source
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // check for the right event
        if (_programmingPane == null) {
            log.warn("unexpected propertChange: "+e);
            return;
        } else if (log.isDebugEnabled()) log.debug("property changed: "+e.getPropertyName()
                                                   +" new value: "+e.getNewValue());
        log.debug("check valid: "+(e.getSource() == _programmingPane)+" "+(!e.getPropertyName().equals("Busy"))+" "+(((Boolean)e.getNewValue()).equals(Boolean.FALSE)));
        if (e.getSource() == _programmingPane &&
            e.getPropertyName().equals("Busy") &&
            ((Boolean)e.getNewValue()).equals(Boolean.FALSE) )  {

            if (log.isDebugEnabled()) log.debug("end of a programming pane operation, remove");

            // remove existing listener
            _programmingPane.removePropertyChangeListener(this);
            _programmingPane = null;
            // restart the operation
            if (_read && readChangesButton.isSelected()) {
                if (log.isDebugEnabled()) log.debug("restart readChanges");
                doRead();
            }
            else if (_read && readAllButton.isSelected()) {
                if (log.isDebugEnabled()) log.debug("restart readAll");
                doRead();
            }
            else if (writeChangesButton.isSelected()) {
                if (log.isDebugEnabled()) log.debug("restart writeChanges");
                doWrite();
            }
            else if (writeAllButton.isSelected()) {
                if (log.isDebugEnabled()) log.debug("restart writeAll");
                doWrite();
            }
            else if (log.isDebugEnabled()) log.debug("read/write end because button is lifted");
        }
    }

    /**
     * Store the locomotives information in the roster (and a RosterEntry file).
     * @return false if store failed
     */
    public boolean storeFile() {
        log.debug("storeFile starts");

        // reload the RosterEntry
        updateDccAddress();
        _rPane.update(_rosterEntry);

        // id has to be set!
        if (_rosterEntry.getId().equals("") || _rosterEntry.getId().equals("<new loco>")) {
            log.debug("storeFile without a filename; issued dialog");
            JOptionPane.showMessageDialog(this, "Please fill in the ID field first");
            return false;
        }
        // if there isn't a filename, store using the id
        _rosterEntry.ensureFilenameExists();
        String filename = _rosterEntry.getFileName();

        // create the RosterEntry to its file
        _rosterEntry.writeFile(cvModel, variableModel );

        // mark this as a success
        variableModel.setFileDirty(false);

        //and store an updated roster file
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        Roster.writeRosterFile();

        // show OK status
        progStatus.setText("Roster file "+filename+" saved OK");

        return true;
    }

    /**
     * local dispose, which also invokes parent. Note that
     * we remove the components (removeAll) before taking those
     * apart.
     */
    public void dispose() {

        if (log.isDebugEnabled()) log.debug("dispose local");

        // remove listeners (not much of a point, though)
        readChangesButton.removeActionListener(l1);
        writeChangesButton.removeActionListener(l2);
        readAllButton.removeActionListener(l3);
        writeAllButton.removeActionListener(l4);
        if (_programmingPane != null) _programmingPane.removePropertyChangeListener(this);

        // dispose the list of panes
        for (int i=0; i<paneList.size(); i++) {
            PaneProgPane p = (PaneProgPane) paneList.get(i);
            p.dispose();
        }
        paneList.clear();

        // dispose of things we owned, in order of dependence
        _rPane.dispose();
        variableModel.dispose();
        cvModel.dispose();

        // remove references to everything we remember
        progStatus = null;
        cvModel = null;
        variableModel = null;
        _rosterEntry = null;
        _rPane = null;

        paneList.clear();
        paneList = null;
        _programmingPane = null;

        tabPane = null;
        readChangesButton = null;
        writeChangesButton = null;
        readAllButton = null;
        writeAllButton = null;

        if (log.isDebugEnabled()) log.debug("dispose superclass");
        removeAll();
        super.dispose();

    }

    /**
     * Option to control appearance of empty panes
     */
    public static void setShowEmptyPanes(boolean yes) {
        showEmptyPanes = yes;
    }
    public static boolean getShowEmptyPanes() {
        return showEmptyPanes;
    }
    static boolean showEmptyPanes = true;

    public RosterEntry getRosterEntry() { return _rosterEntry; }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgFrame.class.getName());

}

