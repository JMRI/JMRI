package jmri.jmrit.symbolicprog;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.*;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.decoderdefn.IdentifyDecoder;
import jmri.jmrit.progsupport.ProgModeSelector;
import jmri.jmrit.roster.IdentifyLoco;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterEntrySelector;
import jmri.jmrit.roster.swing.GlobalRosterEntryComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide GUI controls to select a known loco and/or new decoder.
 * <p>
 * When the "open programmer" button is pushed, i.e. the user is ready to
 * continue, the startProgrammer method is invoked. This should be overridden
 * (e.g. in a local anonymous class) to create the programmer frame you're
 * interested in.
 * <p>
 * To override this class to use a different decoder-selection GUI, replace
 * members:
 * <ul>
 * <li>layoutDecoderSelection
 * <li>updateForDecoderTypeID
 * <li>updateForDecoderMfgID
 * <li>updateForDecoderNotID
 * <li>resetDecoder
 * <li>isDecoderSelected
 * <li>selectedDecoderName
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
public class CombinedLocoSelPane extends LocoSelPane implements PropertyChangeListener {

    /**
     * Provide GUI controls to select a known loco and/or new decoder.
     *
     * @param s        Reference to a JLabel that should be updated with status
     *                 information as identification happens.
     *
     * @param selector Reference to a
     *                 {@link jmri.jmrit.progsupport.ProgModeSelector} panel
     *                 that configures the programming mode.
     */
    public CombinedLocoSelPane(JLabel s, ProgModeSelector selector) {
        _statusLabel = s;
        this.selector = selector;
        init();
    }

    ProgModeSelector selector;

    /**
     * Create the panel used to select the decoder.
     *
     * @return a JPanel for handling the decoder-selection GUI
     */
    protected JPanel layoutDecoderSelection() {
        JPanel pane1a = new JPanel();
        pane1a.setLayout(new BoxLayout(pane1a, BoxLayout.X_AXIS));
        pane1a.add(new JLabel("Decoder installed: "));
        decoderBox = InstanceManager.getDefault(DecoderIndexFile.class).matchingComboBox(null, null, null, null, null, null);
        decoderBox.insertItemAt("<from locomotive settings>", 0);
        decoderBox.setSelectedIndex(0);
        decoderBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (decoderBox.getSelectedIndex() != 0) {
                    // reset and disable loco selection
                    locoBox.setSelectedIndex(0);
                    go2.setEnabled(true);
                    go2.setRequestFocusEnabled(true);
                    go2.requestFocus();
                    go2.setToolTipText(Bundle.getMessage("TipClickToOpen"));
                } else {
                    go2.setEnabled(false);
                    go2.setToolTipText(Bundle.getMessage("TipSelectLoco"));
                }
            }
        });
        pane1a.add(decoderBox);
        iddecoder = addDecoderIdentButton();
        if (iddecoder != null) {
            pane1a.add(iddecoder);
        }
        pane1a.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        return pane1a;
    }

    /**
     * Add a decoder identification button.
     *
     * @return the button
     */
    JToggleButton addDecoderIdentButton() {
        JToggleButton button = new JToggleButton(Bundle.getMessage("ButtonReadType"));
        button.setToolTipText(Bundle.getMessage("TipSelectType"));
        if (InstanceManager.getNullableDefault(GlobalProgrammerManager.class) != null) {
            Programmer p = InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
            if (p != null && !p.getCanRead()) {
                // can't read, disable the button
                button.setEnabled(false);
                button.setToolTipText(Bundle.getMessage("TipNoRead"));
            }
        }
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startIdentifyDecoder();
            }
        });
        return button;
    }

    /**
     * Set the decoder GUI back to having no selection.
     *
     * @param loco the loco name
     */
    void setDecoderSelectionFromLoco(String loco) {
        decoderBox.setSelectedIndex(0);
    }

    /**
     * Has the user selected a decoder type, either manually or via a successful
     * event?
     *
     * @return true if a decoder type is selected
     */
    boolean isDecoderSelected() {
        return decoderBox.getSelectedIndex() != 0;
    }

    /**
     * Convert the decoder selection UI result into a name.
     *
     * @return The selected decoder type name, or null if none selected.
     */
    protected String selectedDecoderType() {
        if (!isDecoderSelected()) {
            return null;
        } else {
            return (String) decoderBox.getSelectedItem();
        }
    }

    /**
     * Create the panel used to select an existing entry.
     *
     * @return a JPanel for handling the entry-selection GUI
     */
    protected JPanel layoutRosterSelection() {
        JPanel pane2a = new JPanel();
        pane2a.setLayout(new BoxLayout(pane2a, BoxLayout.X_AXIS));
        pane2a.add(new JLabel(Bundle.getMessage("USE LOCOMOTIVE SETTINGS FOR:")));
        locoBox.setNonSelectedItem(Bundle.getMessage("<NONE - NEW LOCO>"));
        Roster.getDefault().addPropertyChangeListener(this);
        pane2a.add(locoBox);
        locoBox.addPropertyChangeListener(RosterEntrySelector.SELECTED_ROSTER_ENTRIES, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (locoBox.getSelectedRosterEntries().length != 0) {
                    // reset and disable decoder selection
                    setDecoderSelectionFromLoco(locoBox.getSelectedRosterEntries()[0].titleString());
                    go2.setEnabled(true);
                    go2.setRequestFocusEnabled(true);
                    go2.requestFocus();
                    go2.setToolTipText(Bundle.getMessage("TipClickToOpen"));
                } else {
                    go2.setEnabled(false);
                    go2.setToolTipText(Bundle.getMessage("TipSelectLoco"));
                }
            }
        });
        idloco = new JToggleButton(Bundle.getMessage("IDENT"));
        idloco.setToolTipText(Bundle.getMessage("READ THE LOCOMOTIVE'S ADDRESS AND ATTEMPT TO SELECT THE RIGHT SETTINGS"));
        if (InstanceManager.getNullableDefault(GlobalProgrammerManager.class) != null) {
            Programmer p = InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
            if (p != null && !p.getCanRead()) {
                // can't read, disable the button
                idloco.setEnabled(false);
                idloco.setToolTipText(Bundle.getMessage("BUTTON DISABLED BECAUSE CONFIGURED COMMAND STATION CAN'T READ CVS"));
            }
        }
        idloco.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("Identify locomotive pressed");
                }
                startIdentifyLoco();
            }
        });
        pane2a.add(idloco);
        pane2a.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        return pane2a;
    }

    /**
     * Initialize the GUI.
     */
    protected void init() {
        //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setLayout(new BorderLayout());

        JPanel pane2a = layoutRosterSelection();
        if (pane2a != null) {
            add(pane2a, BorderLayout.NORTH);
        }

        add(layoutDecoderSelection(), BorderLayout.CENTER);

        add(createProgrammerSelection(), BorderLayout.SOUTH);
        setBorder(new EmptyBorder(6, 6, 6, 6));
    }

    /**
     * Creates a Programmer Selection panel.
     *
     * @return the panel
     */
    protected JPanel createProgrammerSelection() {
        JPanel pane3a = new JPanel();
        pane3a.setLayout(new BoxLayout(pane3a, BoxLayout.Y_AXIS));
        // create the programmer box
        JPanel progFormat = new JPanel();
        progFormat.setLayout(new BoxLayout(progFormat, BoxLayout.X_AXIS));
        progFormat.add(new JLabel(Bundle.getMessage("ProgrammerFormat")));
        progFormat.setAlignmentX(JLabel.RIGHT_ALIGNMENT);

        programmerBox = new JComboBox<>(ProgDefault.findListOfProgFiles());
        programmerBox.setSelectedIndex(0);
        if (ProgDefault.getDefaultProgFile() != null) {
            programmerBox.setSelectedItem(ProgDefault.getDefaultProgFile());
        }
        progFormat.add(programmerBox);

        go2 = new JButton(Bundle.getMessage("OpenProgrammer"));
        go2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("Open programmer pressed");
                }
                openButton();
            }
        });
        go2.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        go2.setEnabled(false);
        go2.setToolTipText(Bundle.getMessage("TipSelectLoco"));
        pane3a.add(progFormat);
        pane3a.add(go2);
        return pane3a;
    }

    /**
     * Reference to an external (not in this pane) JLabel that should be updated
     * with status information as identification happens.
     */
    JLabel _statusLabel = null;

    /**
     * Identify loco button pressed, start the identify operation This defines
     * what happens when the identify is done.
     */
    protected void startIdentifyLoco() {
        // start identifying a loco
        Programmer p = null;
        if (selector != null && selector.isSelected()) {
            p = selector.getProgrammer();
        }
        if (p == null) {
            log.warn("Selector did not provide a programmer, use default");
            p = InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        }
        IdentifyLoco id = new IdentifyLoco(p) {

            @Override
            protected void done(int dccAddress) {
                // if Done, updated the selected decoder
                CombinedLocoSelPane.this.selectLoco(dccAddress);
            }

            @Override
            protected void message(String m) {
                if (_statusLabel != null) {
                    _statusLabel.setText(m);
                }
            }

            @Override
            protected void error() {
                // raise the button again
                idloco.setSelected(false);
            }
        };
        id.start();
    }

    /**
     * Identify loco button pressed, start the identify operation. This defines
     * what happens when the identify is done.
     */
    protected void startIdentifyDecoder() {
        // start identifying a decoder
        Programmer p = null;
        if (selector != null && selector.isSelected()) {
            p = selector.getProgrammer();
        }
        if (p == null) {
            log.warn("Selector did not provide a programmer, use default");
            p = InstanceManager.getDefault(GlobalProgrammerManager.class).getGlobalProgrammer();
        }
        IdentifyDecoder id = new IdentifyDecoder(p) {

            @Override
            protected void done(int mfg, int model, int productID) {
                // if Done, updated the selected decoder
                CombinedLocoSelPane.this.selectDecoder(mfg, model, productID);
            }

            @Override
            protected void message(String m) {
                if (_statusLabel != null) {
                    _statusLabel.setText(m);
                }
            }

            @Override
            protected void error() {
                // raise the button again
                iddecoder.setSelected(false);
            }
        };
        id.start();
    }

    /**
     * Notification that the Roster has changed, so the locomotive selection
     * list has to be changed.
     *
     * @param ev Ignored.
     */
    @Override
    public void propertyChange(PropertyChangeEvent ev) {
        locoBox.update();
    }

    /**
     * Identify locomotive complete, act on it by setting the GUI. This will
     * fire "GUI changed" events which will reset the decoder GUI.
     *
     * @param dccAddress the address to select
     */
    protected void selectLoco(int dccAddress) {
        // raise the button again
        idloco.setSelected(false);
        // locate that loco
        List<RosterEntry> l = Roster.getDefault().matchingList(null, null, Integer.toString(dccAddress),
                null, null, null, null);
        if (log.isDebugEnabled()) {
            log.debug("selectLoco found " + l.size() + " matches");
        }
        if (l.size() > 0) {
            RosterEntry r = l.get(0);
            if (log.isDebugEnabled()) {
                log.debug("Loco id is " + r.getId());
            }
            locoBox.setSelectedItem(r);
        } else {
            log.warn("Read address {}, but no such loco in roster", dccAddress);
            _statusLabel.setText(Bundle.getMessage("ReadNoSuchLoco",dccAddress));
        }
    }

    /**
     * Identify decoder complete, act on it by setting the GUI This will fire
     * "GUI changed" events which will reset the locomotive GUI.
     *
     * @param mfgID     the decoder's manufacturer ID value from CV8
     * @param modelID   the decoder's model ID value from CV7
     * @param productID the decoder's product ID
     */
    protected void selectDecoder(int mfgID, int modelID, int productID) {
        // raise the button again
        iddecoder.setSelected(false);
        List<DecoderFile> temp = null;

        // if productID present, try with that
        if (productID != -1) {
            String sz_productID = Integer.toString(productID);
            temp = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, null, Integer.toString(mfgID), Integer.toString(modelID), sz_productID, null);
            if (temp.isEmpty()) {
                log.debug("selectDecoder found no items with product ID {}", productID);
                temp = null;
            } else {
                log.debug("selectDecoder found {} matches with productID {}", temp.size(), productID);
            }
        }

        // try without product ID if needed
        if (temp == null) {  // i.e. if no match previously
            temp = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, null, Integer.toString(mfgID), Integer.toString(modelID), null, null);
            if (log.isDebugEnabled()) {
                log.debug("selectDecoder without productID found " + temp.size() + " matches");
            }
        }

        // remove unwanted matches
        int tempOriginalSize = temp.size(); // save size of unfiltered list
        String theFamily = "";
        String theModel = "";
        String lastWasFamily = "";

        ListIterator<DecoderFile> it = temp.listIterator();
        while (it.hasNext()) {
            log.debug("Match List size is currently {}, scanning for unwanted entries", temp.size());
            DecoderFile t = it.next();
            theFamily = t.getFamily();
            theModel = t.getModel();
            if (t.getFamily().equals(theModel)) {
                log.debug("Match List index={} is family entry '{}'", it.previousIndex(), theFamily);
                lastWasFamily = theFamily;
            } else if (lastWasFamily.equals(theFamily)) {
                log.debug("Match List index={} is first model '{}' in family '{}'", it.previousIndex(), theModel, theFamily);
                log.debug("Removing family entry '{}'", theFamily);
                t = it.previous();
                t = it.previous();
                it.remove();
                lastWasFamily = "";
            } else if ((t.getModelElement().getAttribute("show") != null)
                    && (t.getModelElement().getAttribute("show").getValue().equals("no"))) {
                log.debug("Match List index={} is legacy model '{}' in family '{}'", it.previousIndex(), theModel, theFamily);
                log.debug("Removing legacy model '{}'", theModel);
                t = it.previous();
                it.remove();
                lastWasFamily = "";
            } else {
                log.debug("Match List index={} is model '{}' in family '{}'", it.previousIndex(), theModel, theFamily);
                lastWasFamily = "";
            }
        }

        log.debug("Final Match List size is {}", temp.size());

        // If we had match(es) previously but have lost them in filtering
        // pretend we have no product ID so we get a coarse match
        if (tempOriginalSize > 0 && temp.isEmpty()) {
            log.debug("Filtering removed all matches so reverting to coarse match with mfgID='{}' & modelID='{}'",
                    Integer.toString(mfgID), Integer.toString(modelID));
            temp = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, null, Integer.toString(mfgID), Integer.toString(modelID), null, null);
            log.debug("selectDecoder without productID found {} matches", temp.size());
        }

        // install all those in the JComboBox in place of the longer, original list
        if (temp.size() > 0) {
            updateForDecoderTypeID(temp);
        } else {
            String mfg = InstanceManager.getDefault(DecoderIndexFile.class).mfgNameFromId(Integer.toString(mfgID));
            if (mfg == null) {
                updateForDecoderNotID(mfgID, modelID);
            } else {
                updateForDecoderMfgID(mfg, mfgID, modelID);
            }
        }
    }

    /**
     * Decoder identify has matched one or more specific types.
     *
     * @param pList a list of decoders
     */
    void updateForDecoderTypeID(List<DecoderFile> pList) {
        decoderBox.setModel(DecoderIndexFile.jComboBoxModelFromList(pList));
        decoderBox.insertItemAt("<from locomotive settings>", 0);
        decoderBox.setSelectedIndex(1);
    }

    /**
     * Decoder identify has not matched specific types, but did find
     * manufacturer match.
     *
     * @param pMfg     Manufacturer name. This is passed to save time, as it has
     *                 already been determined once.
     * @param pMfgID   Manufacturer ID number (CV8)
     * @param pModelID Model ID number (CV7)
     */
    void updateForDecoderMfgID(String pMfg, int pMfgID, int pModelID) {
        String msg = "Found mfg " + pMfgID + " (" + pMfg + ") version " + pModelID + "; no such decoder defined";
        log.warn(msg);
        _statusLabel.setText(msg);
        // try to select all decoders from that MFG
        JComboBox<String> temp = InstanceManager.getDefault(DecoderIndexFile.class).matchingComboBox(null, null, Integer.toString(pMfgID), null, null, null);
        if (log.isDebugEnabled()) {
            log.debug("mfg-only selectDecoder found " + temp.getItemCount() + " matches");
        }
        // install all those in the JComboBox in place of the longer, original list
        if (temp.getItemCount() > 0) {
            decoderBox.setModel(temp.getModel());
            decoderBox.insertItemAt("<from locomotive settings>", 0);
            decoderBox.setSelectedIndex(1);
        } else {
            // if there are none from this mfg, go back to showing everything
            temp = InstanceManager.getDefault(DecoderIndexFile.class).matchingComboBox(null, null, null, null, null, null);
            decoderBox.setModel(temp.getModel());
            decoderBox.insertItemAt("<from locomotive settings>", 0);
            decoderBox.setSelectedIndex(1);
        }
    }

    /**
     * Decoder identify did not match anything, warn and show all.
     *
     * @param pMfgID   Manufacturer ID number (CV8)
     * @param pModelID Model ID number (CV7)
     */
    void updateForDecoderNotID(int pMfgID, int pModelID) {
        log.warn("Found mfg {} version {}; no such manufacturer defined", pMfgID, pModelID);
        JComboBox<String> temp = InstanceManager.getDefault(DecoderIndexFile.class).matchingComboBox(null, null, null, null, null, null);
        decoderBox.setModel(temp.getModel());
        decoderBox.insertItemAt("<from locomotive settings>", 0);
        decoderBox.setSelectedIndex(1);
    }

    protected GlobalRosterEntryComboBox locoBox = new GlobalRosterEntryComboBox();
    private JComboBox<String> decoderBox = null;       // private because children will override this
    protected JComboBox<String> programmerBox = null;
    protected JToggleButton iddecoder;
    protected JToggleButton idloco;
    protected JButton go2;

    /**
     * handle pushing the open programmer button by finding names, then calling
     * a template method.
     */
    protected void openButton() {
        // figure out which we're dealing with
        if (locoBox.getSelectedRosterEntries().length != 0) {
            // known loco
            openKnownLoco();
        } else if (isDecoderSelected()) {
            // new loco
            openNewLoco();
        } else {
            // should not happen, as the button should be disabled!
            log.error("openButton with neither combobox nonzero");
        }
    }

    /**
     * Start with a locomotive selected, so we're opening an existing
     * RosterEntry.
     */
    protected void openKnownLoco() {

        if (locoBox.getSelectedRosterEntries().length != 0) {
            RosterEntry re = locoBox.getSelectedRosterEntries()[0];
            if (log.isDebugEnabled()) {
                log.debug("loco file: " + re.getFileName());
            }

            startProgrammer(null, re, (String) programmerBox.getSelectedItem());
        } else {
            log.error("No roster entry was selected to open.");
        }
    }

    /**
     * Start with a decoder selected, so we're going to create a new
     * RosterEntry.
     */
    protected void openNewLoco() {
        // find the decoderFile object
        DecoderFile decoderFile = InstanceManager.getDefault(DecoderIndexFile.class).fileFromTitle(selectedDecoderType());
        if (log.isDebugEnabled()) {
            log.debug("decoder file: " + decoderFile.getFileName());
        }

        // create a dummy RosterEntry with the decoder info
        RosterEntry re = new RosterEntry();
        re.setDecoderFamily(decoderFile.getFamily());
        re.setDecoderModel(decoderFile.getModel());
        re.setId(Bundle.getMessage("LabelNewDecoder"));
        // note that we're leaving the filename null
        // add the new roster entry to the in-memory roster
        Roster.getDefault().addEntry(re);

        startProgrammer(decoderFile, re, (String) programmerBox.getSelectedItem());
    }

    /**
     * Start the desired type of programmer.
     *
     * @param decoderFile defines the type of decoder installed; if null, check
     *                    the RosterEntry re for that
     * @param r           Existing roster entry defining this locomotive
     * @param progName    name of the programmer (Layout connection) being used
     */
    // TODO: Fix inheritance.  This is both a base class (where startProgrammer really isn't part of the contract_
    //       and a first implementation (where this method is needed).  Because it's part of the contract, it can't be
    //       made abstract:  CombinedLocoSelListPane and CombinedLocoSelTreePane have no need for it.
    protected void startProgrammer(@CheckForNull DecoderFile decoderFile, @Nonnull RosterEntry r, @Nonnull String progName) {
        log.error("startProgrammer method in CombinedLocoSelPane should have been overridden");
    }

    private final static Logger log = LoggerFactory.getLogger(CombinedLocoSelPane.class);

}
