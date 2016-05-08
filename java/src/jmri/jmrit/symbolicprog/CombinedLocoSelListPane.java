package jmri.jmrit.symbolicprog;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.progsupport.ProgModeSelector;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide GUI controls to select a known loco and/or new decoder.
 * <P>
 * This is an extension of the CombinedLocoSelPane class to use a JList instead
 * of a JComboBox for the decoder selection. Also, this uses separate JLists for
 * manufacturer and decoder model. The loco selection (Roster manipulation)
 * parts are unchanged.
 * <P>
 * The JComboBox implementation always had to have selected entries, so we added
 * dummy "select from .." items at the top & used those to indicate that there
 * was no selection in that box. Here, the lack of a selection indicates there's
 * no selection.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 */
public class CombinedLocoSelListPane extends CombinedLocoSelPane {

    public CombinedLocoSelListPane(JLabel s, ProgModeSelector selector) {
        super(s, selector);
    }

    /**
     * Create the panel used to select the decoder
     */
    protected JPanel layoutDecoderSelection() {
        JPanel pane1a = new JPanel();
        pane1a.setLayout(new BoxLayout(pane1a, BoxLayout.X_AXIS));
        pane1a.add(new JLabel("Decoder installed: "));
        // create the list of manufacturers
        mMfgList = new JList<>();
        updateMfgListContents(null);
        mMfgList.clearSelection();
        mMfgList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mMfgListener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!mMfgList.isSelectionEmpty()) {
                    // manufacturer selected, update decoder list
                    String vMfg = mMfgList.getSelectedValue();
                    try {
                        int vMfgID = Integer.parseInt(
                                DecoderIndexFile.instance().mfgIdFromName(vMfg));

                        listDecodersFromMfg(vMfgID, vMfg);
                    } catch (java.lang.NumberFormatException ex) {
                        // mfg number lookup failed for some reason
                    }
                } else {
                    // no manufacturer selected, do nothing
                }
            }
        };
        mMfgList.addListSelectionListener(mMfgListener);

        mDecoderList = new JList<String>(DecoderIndexFile.instance()
                .matchingComboBox(null, null, null, null, null, null).getModel());
        mDecoderList.clearSelection();
        mDecoderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mDecoderListener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!mDecoderList.isSelectionEmpty()) {
                    // decoder selected - reset and disable loco selection
                    locoBox.setSelectedIndex(0);
                    go2.setEnabled(true);
                    go2.setToolTipText("Click to open the programmer");
                    updateMfgListToSelectedDecoder();
                } else {
                    // decoder not selected - require one
                    go2.setEnabled(false);
                    go2.setToolTipText("Select a locomotive or decoder to enable");
                }
            }
        };
        mDecoderList.addListSelectionListener(mDecoderListener);

        pane1a.add(new JScrollPane(mMfgList));
        pane1a.add(new JScrollPane(mDecoderList));
        iddecoder = new JToggleButton("Ident");
        iddecoder.setToolTipText("Read the decoders mfg and version, then attempt to select its type");
        if (jmri.InstanceManager.programmerManagerInstance() != null
                && jmri.InstanceManager.programmerManagerInstance().getGlobalProgrammer() != null
                && !jmri.InstanceManager.programmerManagerInstance().getGlobalProgrammer().getCanRead()) {
            // can't read, disable the button
            iddecoder.setEnabled(false);
            iddecoder.setToolTipText("Button disabled because configured command station can't read CVs");
        }
        iddecoder.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("identify decoder pressed");
                }
                startIdentifyDecoder();
            }
        });
        pane1a.add(iddecoder);
        pane1a.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        return pane1a;
    }

    /**
     * Update the contents of the manufacturer list to make sure it contains a
     * specific value. Normally the list does not contain mfgs with no defined
     * decoders; this allows you to also show a specific mfg that's of interest,
     * even though there's no definitions for it. This is protected against
     * invoking any listeners, as the change is meant to be transparent; the
     * original selection is set back.
     */
    void updateMfgListContents(String specific) {
        if (mMfgListener != null) {
            mMfgList.removeListSelectionListener(mMfgListener);
        }
        String currentValue = mMfgList.getSelectedValue();

        List<String> allMfgList = DecoderIndexFile.instance().getMfgNameList();
        List<String> theMfgList = new ArrayList<>();

        for (int i = 0; i < allMfgList.size(); i++) {
            // see if this qualifies; either a non-zero set of decoders, or
            // matches the specific name
            if ((specific != null && (allMfgList.get(i).equals(specific)))
                    || (0 != DecoderIndexFile.instance()
                    .matchingDecoderList(allMfgList.get(i), null, null, null, null, null)
                    .size())) {
                theMfgList.add(allMfgList.get(i));
            }
        }
        mMfgList.setListData(theMfgList.toArray(new String[0]));

        mMfgList.setSelectedValue(currentValue, true);
        if (mMfgListener != null) {
            mMfgList.addListSelectionListener(mMfgListener);
        }

    }

    /**
     * Force the manufacturer list to select the mfg of the currently selected
     * decoder. Note that this is complicated by the need to not trigger an
     * update of the decoder list.
     */
    void updateMfgListToSelectedDecoder() {
        // update to point at this mfg, _without_ changing the decoder list
        DecoderFile df = DecoderIndexFile.instance()
                .fileFromTitle(mDecoderList.getSelectedValue());
        if (log.isDebugEnabled()) {
            log.debug("decoder selection changed to "
                    + mDecoderList.getSelectedValue());
        }
        if (df != null) {
            if (log.isDebugEnabled()) {
                log.debug("matching mfg is "
                        + df.getMfg());
            }
            updateMfgListWithoutTrigger(df.getMfg());
        }
    }

    /**
     * Set a selection in the manufacturer list, without triggering an update of
     * the decoder panel.
     */
    void updateMfgListWithoutTrigger(String mfg) {
        mMfgList.removeListSelectionListener(mMfgListener);
        mMfgList.setSelectedValue(mfg, true);
        mMfgList.addListSelectionListener(mMfgListener);
    }

    /**
     * Decoder identify has matched one or more specific types
     */
    void updateForDecoderTypeID(List<DecoderFile> pModelList) {
        // use a DefaultComboBoxModel to get the efficient ctor
        mDecoderList.setModel(DecoderIndexFile.jComboBoxModelFromList(pModelList));
        mDecoderList.setSelectedIndex(0);
    }

    /**
     * Decoder identify has not matched specific types, but did find
     * manufacturer match
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
        // ensure manufacturer shows & is selected
        updateMfgListContents(pMfg);
        updateMfgListWithoutTrigger(pMfg);
        // list all other decoders available, without a selection
        listDecodersFromMfg(pMfgID, pMfg);
    }

    void listDecodersFromMfg(int pMfgID, String pMfg) {
        // try to select all decoders from that MFG
        JComboBox<String> temp = DecoderIndexFile.instance().matchingComboBox(null, null, Integer.toString(pMfgID), null, null, null);
        if (log.isDebugEnabled()) {
            log.debug("mfg-only selectDecoder found " + temp.getItemCount() + " matches");
        }
        // install all those in the JComboBox in place of the longer, original list
        mDecoderList.setModel(temp.getModel());
        mDecoderList.clearSelection();
        updateMfgListWithoutTrigger(pMfg);
    }

    /**
     * Decoder identify did not match anything, warn and show all
     */
    void updateForDecoderNotID(int pMfgID, int pModelID) {
        String msg = "Found mfg " + pMfgID + " version " + pModelID + "; no such manufacterer defined";
        log.warn(msg);
        _statusLabel.setText(msg);
        mMfgList.setSelectedIndex(1);
        mMfgList.clearSelection();
        JComboBox<String> temp = DecoderIndexFile.instance().matchingComboBox(null, null, null, null, null, null);
        mDecoderList.setModel(temp.getModel());
        mDecoderList.clearSelection();
    }

    /**
     * Set the decoder selection to a specific decoder from a selected Loco
     */
    void setDecoderSelectionFromLoco(String loco) {
        // if there's a valid loco entry...
        RosterEntry locoEntry = Roster.instance().entryFromTitle(loco);
        if (locoEntry == null) {
            return;
        }
        // get the decoder type, it has to be there (assumption!),
        String modelString = locoEntry.getDecoderModel();
        // find the decoder mfg
        String mfgString = DecoderIndexFile.instance().fileFromTitle(modelString)
                .getMfg();

        // then select it
        updateMfgListWithoutTrigger(mfgString);

        // decoder has to be there (assumption!)
        // so load it into the list directly
        String[] tempArray = new String[1];
        tempArray[0] = modelString;
        mDecoderList.setListData(tempArray);
        // select the entry you just put in, but don't trigger anything!
        mDecoderList.removeListSelectionListener(mDecoderListener);
        mDecoderList.setSelectedIndex(0);
        mDecoderList.addListSelectionListener(mDecoderListener);
    }

    /**
     * Has the user selected a decoder type, either manually or via a successful
     * event?
     *
     * @return true if a decoder type is selected
     */
    boolean isDecoderSelected() {
        return !mDecoderList.isSelectionEmpty();
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
            return mDecoderList.getSelectedValue();
        }
    }

    JList<String> mDecoderList;
    ListSelectionListener mDecoderListener;

    JList<String> mMfgList;
    ListSelectionListener mMfgListener;

    private final static Logger log = LoggerFactory.getLogger(CombinedLocoSelListPane.class.getName());

}
