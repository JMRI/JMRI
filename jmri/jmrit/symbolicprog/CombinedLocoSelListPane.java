// CombinedLocoSelListPane.java

package jmri.jmrit.symbolicprog;

import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.*;
import jmri.jmrit.decoderdefn.*;

import java.awt.*;
import java.io.File;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import com.sun.java.util.collections.List;

/**
 * Provide GUI controls to select a known loco and/or new decoder.
 * <P>
 * This is an extension of the CombinedLocoSelPane class to use
 * a JList instead of a JComboBox for the decoder selection.
 * Also, this uses separate JLists for manufacturer and decoder model.
 * The loco selection (Roster manipulation) parts are unchanged.
 * <P>
 * The JComboBox implementation always had to have selected entries, so
 * we added dummy "select from .." items at the top & used those to
 * indicate that there was no selection in that box.
 * Here, the lack of a selection indicates there's no selection.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.1 $
 */
public class CombinedLocoSelListPane extends CombinedLocoSelPane  {

	public CombinedLocoSelListPane(JLabel s) {
		super(s);
	}

	public CombinedLocoSelListPane() {

		super();
	}

    /**
     * Create the panel used to select the decoder
     */
    protected JPanel layoutDecoderSelection() {
        JPanel pane1a = new JPanel();
        pane1a.setLayout(new BoxLayout(pane1a, BoxLayout.X_AXIS));
        pane1a.add(new JLabel("Decoder installed: "));
        // create the list of manufacturers
        List l = DecoderIndexFile.instance().getMfgNameList();
        mMfgList = new JList();
        String [] tempString = new String[l.size()];
        for (int i=0; i<l.size(); i++) {
            tempString[i] = (String)l.get(i);
        }
        mMfgList = new JList(tempString);
        mMfgList.clearSelection();
        mMfgList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mMfgListener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!mMfgList.isSelectionEmpty()) {
                    // manufacturer selected, update decoder list
                    String vMfg = (String) mMfgList.getSelectedValue();
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
            }};
        mMfgList.addListSelectionListener(mMfgListener);

        mDecoderList = new JList(DecoderIndexFile.instance()
                            .matchingComboBox(null, null, null, null, null).getModel());
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
            }};
        mDecoderList.addListSelectionListener(mDecoderListener);

        pane1a.add(new JScrollPane(mMfgList));
        pane1a.add(new JScrollPane(mDecoderList));
        iddecoder= new JToggleButton("Ident");
        idloco.setToolTipText("Read the decoders mfg and version, then attempt to select it's type");
        iddecoder.addActionListener( new ActionListener() {
        	public void actionPerformed(java.awt.event.ActionEvent e) {
        		if (log.isInfoEnabled()) log.info("identify decoder pressed");
        		startIdentifyDecoder();
        	}
        });
        pane1a.add(iddecoder);
        pane1a.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        return pane1a;
    }

    /**
     * Force the manufacturer list to select the mfg of the
     * currently selected decoder.  Note that this is
     * complicated by the need to not trigger an update
     * of the decoder list.
     */
    void updateMfgListToSelectedDecoder() {
        // update to point at this mfg, _without_ changing the decoder list
        DecoderFile df = DecoderIndexFile.instance()
                        .fileFromTitle((String)mDecoderList.getSelectedValue());
        if (log.isDebugEnabled()) log.debug("decoder selection changed to "
                                            +(String)mDecoderList.getSelectedValue());
        if (df!=null) {
            if (log.isDebugEnabled()) log.debug("matching mfg is "
                                                +df.getMfg());
                updateMfgListWithoutTrigger(df.getMfg());
            }
    }

    /**
     * Set a selection in the manufacturer list, without
     * triggering an update of the decoder panel.
     */
    void updateMfgListWithoutTrigger(String mfg) {
        mMfgList.removeListSelectionListener(mMfgListener);
        mMfgList.setSelectedValue(mfg, true);
        mMfgList.addListSelectionListener(mMfgListener);
    }
    /**
     * Decoder identify has matched one or more specific types
     */
    void updateForDecoderTypeID(List pModelList) {
            // use a DefaultComboBoxModel to get the efficient ctor
			mDecoderList.setModel(DecoderIndexFile.jComboBoxModelFromList(pModelList));
			mDecoderList.setSelectedIndex(0);
    }
    /**
     * Decoder identify has not matched specific types, but did
     * find manufacturer match
     * @param pMfg Manufacturer name. This is passed to save time,
     *              as it has already been determined once.
     * @param pMfgID Manufacturer ID number (CV8)
     * @param pModelID Model ID number (CV7)
     */
    void updateForDecoderMfgID(String pMfg, int pMfgID, int pModelID) {
        String msg = "Found mfg "+pMfgID+" ("+pMfg+") version "+pModelID+"; no such decoder defined";
        log.warn(msg);
        _statusLabel.setText(msg);
        listDecodersFromMfg(pMfgID, pMfg);
    }

    void listDecodersFromMfg(int pMfgID, String pMfg) {
        // try to select all decoders from that MFG
        JComboBox temp = DecoderIndexFile.instance().matchingComboBox(null, null, Integer.toString(pMfgID), null, null);
        if (log.isDebugEnabled()) log.debug("mfg-only selectDecoder found "+temp.getItemCount()+" matches");
        // install all those in the JComboBox in place of the longer, original list
        mDecoderList.setModel(temp.getModel());
        mDecoderList.clearSelection();
        updateMfgListWithoutTrigger(pMfg);
    }

    /**
     * Decoder identify did not match anything, warn and show all
     */
    void updateForDecoderNotID(int pMfgID, int pModelID) {
        String msg = "Found mfg "+pMfgID+" version "+pModelID+"; no such manufacterer defined";
        log.warn(msg);
        _statusLabel.setText(msg);
        mMfgList.setSelectedIndex(1);
        mMfgList.clearSelection();
        JComboBox temp = DecoderIndexFile.instance().matchingComboBox(null, null, null, null, null);
        mDecoderList.setModel(temp.getModel());
        mDecoderList.clearSelection();
    }

    /**
     *  Set the decoder GUI back to having no selection
     */
    void resetDecoderSelection() {
        mDecoderList.clearSelection();
    }

    /**
     * Has the user selected a decoder type, either manually or
     * via a successful event?
     * @return true if a decoder type is selected
     */
    boolean isDecoderSelected() {
     return !mDecoderList.isSelectionEmpty();
    }

    /**
     * Convert the decoder selection UI result into a name.
     * @return The selected decoder type name, or null if none selected.
     */
    String selectedDecoderType() {
        if (!isDecoderSelected()) return null;
        else return (String)mDecoderList.getSelectedValue();
    }

    JList mDecoderList;
    ListSelectionListener mDecoderListener;

    JList mMfgList;
    ListSelectionListener mMfgListener;


	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CombinedLocoSelListPane.class.getName());

}
