package jmri.jmrit.roster;

import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.ResetTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrit.symbolicprog.tabbedframe.PaneContainer;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgPane;
import jmri.util.BusyGlassPane;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.davidflanagan.HardcopyWriter;
import org.jdom2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintRosterEntry implements PaneContainer {

    RosterEntry _rosterEntry;

    /**
     * List of {@link jmri.jmrit.symbolicprog.tabbedframe.PaneProgPane} JPanels.
     * Built up at line 158 or passed as argument paneList in line 196 via
     * {link #PrintRosterEntry(RosterEntry, List, FunctionLabelPane, RosterMediaPane, JmriJFrame)}
     */
    List<JPanel> _paneList = new ArrayList<JPanel>();
    FunctionLabelPane _flPane = null;
    RosterMediaPane _rMPane = null;
    JmriJFrame _parent = null;

    /**
     * Constructor for a Print roster item (programmer tabs) selection pane.
     *
     * @param rosterEntry Roster item, either as a selection or object
     * @param parent window over which this dialog will be centered
     * @param programmerFilename xml file name for programmer used in printing.
     */
    public PrintRosterEntry(RosterEntry rosterEntry, JmriJFrame parent, String programmerFilename) {
        _rosterEntry = rosterEntry;
        _flPane = new FunctionLabelPane(rosterEntry);
        _rMPane = new RosterMediaPane(rosterEntry);
        _parent = parent;
        JLabel progStatus = new JLabel(Bundle.getMessage("StateIdle"));
        ResetTableModel resetModel = new ResetTableModel(progStatus, null);  // no programmer

        log.debug("Try PrintRosterEntry {} from file {}", _rosterEntry.getDisplayName(), programmerFilename);
        XmlFile pf = new XmlFile() {
        };
        Element base = null;  // base of programmer file
        try {
            Element root = pf.rootFromName(programmerFilename);
            if (root == null) {
                log.error("Programmer file name incorrect {}", programmerFilename);
                return;
            }
            if ((base = root.getChild("programmer")) == null) {
                log.error("xml file top element is not 'programmer'");
                return;
            }
            log.debug("Success: xml file top element is 'programmer'");
        } catch (JDOMException | java.io.IOException e) {
            log.error("exception reading programmer file {}", programmerFilename, e);
            return;
        }

        CvTableModel cvModel = new CvTableModel(progStatus, null); // no programmer

        VariableTableModel variableModel = new VariableTableModel(progStatus, new String[]{"Name", "Value"}, cvModel); // NOI18N
        
        String decoderModel = _rosterEntry.getDecoderModel();
        String decoderFamily = _rosterEntry.getDecoderFamily();

        log.debug("selected loco uses decoder {} {}", decoderFamily, decoderModel);
        // locate a decoder like that.
        List<DecoderFile> l = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, decoderFamily, null, null, null, decoderModel);
        log.debug("found {} matches", l.size());
        if (l.isEmpty()) {
            log.debug("Loco uses {} {} decoder, but no such decoder defined", decoderFamily, decoderModel);
            // fall back to use just the decoder name, not family
            l = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, null, null, null, null, decoderModel);
            log.debug("found {} matches without family key", l.size());
        }
        DecoderFile decoderFile = null;
        if (l.size() > 0) {
            decoderFile = l.get(0);
        } else {
            if (decoderModel.equals("")) {
                log.debug("blank decoderModel requested, so nothing loaded");
            } else {
                log.warn("no matching \"" + decoderModel + "\" decoder found for loco, no decoder info loaded");
            }
        }

        if (decoderFile == null) {
            log.warn("no decoder file found for this loco");
            return;
        }
        Element decoderRoot;
        log.debug("Try to read decoder root from {} {}", DecoderFile.fileLocation, decoderFile.getFileName());
        try {
            decoderRoot = decoderFile.rootFromName(DecoderFile.fileLocation + decoderFile.getFileName());

        } catch (org.jdom2.JDOMException exj) {
            log.error("could not parse " + decoderFile.getFileName() + ": " + exj.getMessage());
            return;
        } catch (java.io.IOException exj) {
            log.error("could not read " + decoderFile.getFileName() + ": " + exj.getMessage());
            return;
        }

        // load defaults
        decoderFile.loadVariableModel(decoderRoot.getChild("decoder"), variableModel);
        decoderFile.loadResetModel(decoderRoot.getChild("decoder"), resetModel);
        
        // load the specific contents for this entry
        rosterEntry.readFile();
        rosterEntry.loadCvModel(variableModel, cvModel);

        List<Element> rawPaneList = base.getChildren("pane");
        log.debug("rawPaneList size = {}", rawPaneList.size());
        for (Element elPane : rawPaneList) {
            // load each pane to store in _paneList and fetch its name element (i18n) to show in Select items pane
            Element _name = elPane.getChild("name"); // multiple languages
            // There is no name attribute of pane in Basic.xml nor (for the Comprehensive programmer) in include.parts.Basic.xml
            // Instead, it's a separate element inside programmer.pane, fixed 4.7.2
            String name = "Tab name"; // temporary fake name
            if (_name != null) {
                name = _name.getText(); // because it's used in line 163, can't translate it now, TODO I18N
                log.debug("Tab '{}' added", name);
            } else {
                log.debug("Did not find name element in pane");
            }
            PaneProgPane p = new PaneProgPane(this, name, elPane, cvModel, variableModel, decoderFile.getModelElement(), _rosterEntry);
            // Tab names _paneList.get(i).getName() show up when PrintRosterEntry is called from RosterFrame (entered here, applied in line 278)
            _paneList.add(p);
            log.debug("_paneList size = {}", _paneList.size());
        }
    }

    @Override
    public BusyGlassPane getBusyGlassPane() {
        return null;
    }

    @Override
    public void prepGlassPane(javax.swing.AbstractButton activeButton) {
    }

    @Override
    public void enableButtons(boolean enable) {
    }

    @Override
    public void paneFinished() {
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    /**
     * Configure variable fields and create a PrintRosterEntry instance while doing so.
     *
     * @param rosterEntry an item in the Roster
     * @param paneList list of programmer tabs, including all properties
     * @param flPane extra pane w/checkbox to select printing of "Function List"
     * @param rMPane pane containing roster media (image)
     * @param parent window over which this dialog will be centered
     */
    public PrintRosterEntry(RosterEntry rosterEntry, List<JPanel> paneList, FunctionLabelPane flPane, RosterMediaPane rMPane, JmriJFrame parent) {
        _rosterEntry = rosterEntry;
        _paneList = paneList;
        _flPane = flPane;
        _rMPane = rMPane;
        _parent = parent;
    }

    /**
     * Write a series of 'pages' to graphic output using HardcopyWriter.
     *
     * @param preview true if output sould got to the Preview panel, false to output to a printer
     */
    public void doPrintPanes(boolean preview) {
        HardcopyWriter w = null;
        try {
            w = new HardcopyWriter(_parent, _rosterEntry.getId(), 10, .8, .5, .5, .5, preview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        printInfoSection(w);

        if (_flPane.includeInPrint()) {
            _flPane.printPane(w);
        }
        log.debug("List size length: {}", _paneList.size());
        for (int i = 0; i < _paneList.size(); i++) {
            log.debug("start printing page {}", i);
            PaneProgPane pane = (PaneProgPane) _paneList.get(i);
            if (pane.includeInPrint()) {
                pane.printPane(w);
            }
        }
        w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(), w.getCharactersPerLine() + 1);
        w.close();
    }

    /**
     * Ceate and display a pane to the user to select which Programmer tabs to include in printout.
     *
     * @param preview true if output should got to a Preview pane on screen, false to output to a printer (dialog)
     */
    public void printPanes(final boolean preview) {
        final JFrame frame = new JFrame(Bundle.getMessage("TitleSelectItemsToPrint"));
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));

        JPanel instruct = new JPanel();
        JLabel l1 = new JLabel(Bundle.getMessage("LabelSelectLine1"));
        instruct.add(l1);
        l1 = new JLabel(Bundle.getMessage("LabelSelectLine2"));
        instruct.add(l1);
        p1.add(instruct);

        JPanel select = new JPanel();
        select.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ItemsLabel")));
        // add checkboxes for all items
        final Hashtable<JCheckBox, PaneProgPane> printList = new Hashtable<JCheckBox, PaneProgPane>();
        select.setLayout(new BoxLayout(select, BoxLayout.PAGE_AXIS));
        final JCheckBox funct = new JCheckBox(Bundle.getMessage("LabelFunctionList"));
        funct.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _flPane.includeInPrint(funct.isSelected());
            }
        });
        _flPane.includeInPrint(false);
        select.add(funct);

        log.debug("List size length: {}", _paneList.size());
        for (int i = 0; i < _paneList.size(); i++) {
            final PaneProgPane pane = (PaneProgPane) _paneList.get(i);
            pane.includeInPrint(false);
            final JCheckBox item = new JCheckBox(_paneList.get(i).getName());
            // Tab names _paneList.get(i).getName() show up when called from RosterFrame (are entered in line 146)
            printList.put(item, pane);
            item.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    pane.includeInPrint(item.isSelected());
                }
            });
            select.add(item);
        }
        p1.add(select);

        // Add "Select All" checkbox below titled set of item boxes
        JPanel selectAllBox = new JPanel();
        final JCheckBox selectAll = new JCheckBox(Bundle.getMessage("SelectAll"));
        selectAll.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _flPane.includeInPrint(selectAll.isSelected());
                funct.setSelected(selectAll.isSelected());
                Enumeration<JCheckBox> en = printList.keys();
                while (en.hasMoreElements()) {
                    JCheckBox check = en.nextElement();
                    printList.get(check).includeInPrint(selectAll.isSelected());
                    check.setSelected(selectAll.isSelected());
                }
            }
        });
        selectAllBox.add(selectAll);
        p1.add(selectAllBox);

        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
        JButton ok = new JButton(Bundle.getMessage("ButtonOK"));
        cancel.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frame.dispose();
            }
        });
        ok.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doPrintPanes(preview);
                frame.dispose();
            }
        });
        JPanel buttons = new JPanel();
        buttons.add(cancel);
        buttons.add(ok);
        p1.add(buttons);

        frame.add(p1);
        frame.pack();
        frame.setVisible(true);

    }

    /**
     * Write the page header to graphic output, using HardcopyWriter w.
     * <p>
     * Includes the DecoderPro logo image at top right.
     *
     * @param w the active HardcopyWriter instance to be used
     */
    public void printInfoSection(HardcopyWriter w) {
        ImageIcon icon = new ImageIcon(FileUtil.findURL("resources/decoderpro.gif", FileUtil.Location.INSTALLED));
        // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
        w.write(icon.getImage(), new JLabel(icon));
        w.setFontStyle(Font.BOLD);
        //Add a number of blank lines
        int height = icon.getImage().getHeight(null);
        int blanks = (height - w.getLineAscent()) / w.getLineHeight();

        try {
            for (int i = 0; i < blanks; i++) {
                String s = "\n";
                w.write(s, 0, s.length());
            }
        } catch (IOException e) {
            log.warn("error during printing: ", e);
        }
        _rosterEntry.printEntry(w);
        w.setFontStyle(Font.PLAIN);
    }

    private final static Logger log = LoggerFactory.getLogger(PrintRosterEntry.class);
}
