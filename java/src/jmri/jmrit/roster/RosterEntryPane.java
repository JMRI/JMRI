package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Display and edit a RosterEntry.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Dennis Miller Copyright 2004, 2005
 */
public class RosterEntryPane extends javax.swing.JPanel {

    // Field sizes expanded to 30 from 12 to match comment
    // fields and allow for more text to be displayed
    JTextField id = new JTextField(30);
    JTextField roadName = new JTextField(30);
    JTextField maxSpeed = new JTextField(3);
    JSpinner maxSpeedSpinner = new JSpinner(); // percentage stored as fraction

    JTextField roadNumber = new JTextField(30);
    JTextField mfg = new JTextField(30);
    JTextField model = new JTextField(30);
    JTextField owner = new JTextField(30);
    DccLocoAddressSelector addrSel = new DccLocoAddressSelector();

    JTextArea comment = new JTextArea(3, 30);
    //JScrollPanes are defined with scroll bars on always to avoid undesirable resizing behavior
    //Without this the field will shrink to minimum size any time the scroll bars become needed and
    //the scroll bars are inside, not outside the field area, obscuring their contents.
    //This way the shrinking does not happen and the scroll bars are outside the field area,
    //leaving the contents visible
    JScrollPane commentScroller = new JScrollPane(comment, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    JLabel dateUpdated = new JLabel();
    JLabel decoderModel = new JLabel();
    JLabel decoderFamily = new JLabel();
    JTextArea decoderComment = new JTextArea(3, 30);
    JScrollPane decoderCommentScroller = new JScrollPane(decoderComment, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    Component pane = null;
    RosterEntry re = null;

    final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");

    public RosterEntryPane(RosterEntry r) {

        maxSpeedSpinner.setModel(new SpinnerNumberModel(1.00d, 0.00d, 1.00d, 0.01d));
        maxSpeedSpinner.setEditor(new JSpinner.NumberEditor(maxSpeedSpinner, "# %"));
        id.setText(r.getId());

        if (r.getDccAddress().equals("")) {
            // null address, so clear selector
            addrSel.reset();
        } else {
            // non-null address, so load
            DccLocoAddress tempAddr = new DccLocoAddress(
                    Integer.parseInt(r.getDccAddress()), r.getProtocol());
            addrSel.setAddress(tempAddr);
        }

        // fill contents
        RosterEntryPane.this.updateGUI(r);

        pane = this;
        re = r;

        // add options
        id.setToolTipText(rb.getString("ToolTipID"));

        addrSel.setEnabled(false);
        addrSel.setLocked(false);

        if ((InstanceManager.getNullableDefault(jmri.ThrottleManager.class) != null)
                && !InstanceManager.throttleManagerInstance().addressTypeUnique()) {
            // This goes through to find common protocols between the command station and the decoder
            // and will set the selection box list to match those that are common.
            jmri.ThrottleManager tm = InstanceManager.throttleManagerInstance();
            List<LocoAddress.Protocol> protocoltypes = new ArrayList<>();
            protocoltypes.addAll(Arrays.asList(tm.getAddressProtocolTypes()));

            if (!protocoltypes.contains(LocoAddress.Protocol.DCC_LONG) && !protocoltypes.contains(LocoAddress.Protocol.DCC_SHORT)) {
                //Multi protocol systems so far are not worried about dcc long vs dcc short
                List<DecoderFile> l = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, r.getDecoderFamily(), null, null, null, r.getDecoderModel());
                if (log.isDebugEnabled()) {
                    log.debug("found {} matched", l.size());
                }
                if (l.isEmpty()) {
                    log.debug("Loco uses {} {} decoder, but no such decoder defined", decoderFamily, decoderModel);
                    // fall back to use just the decoder name, not family
                    l = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, null, null, null, null, r.getDecoderModel());
                    if (log.isDebugEnabled()) {
                        log.debug("found {} matches without family key", l.size());
                    }
                }
                DecoderFile d;
                if (l.size() > 0) {
                    d = l.get(0);
                    if (d != null && d.getSupportedProtocols().length > 0) {
                        ArrayList<String> protocols = new ArrayList<>(d.getSupportedProtocols().length);

                        for (LocoAddress.Protocol i : d.getSupportedProtocols()) {
                            if (protocoltypes.contains(i)) {
                                protocols.add(tm.getAddressTypeString(i));
                            }
                        }
                        addrSel = new DccLocoAddressSelector(protocols.toArray(new String[protocols.size()]));
                        DccLocoAddress tempAddr = new DccLocoAddress(
                                Integer.parseInt(r.getDccAddress()), r.getProtocol());
                        addrSel.setAddress(tempAddr);
                        addrSel.setEnabled(false);
                        addrSel.setLocked(false);
                        addrSel.setEnabledProtocol(true);
                    }
                }
            }
        }

        JPanel selPanel = addrSel.getCombinedJPanel();
        selPanel.setToolTipText(rb.getString("ToolTipDccAddress"));
        decoderModel.setToolTipText(rb.getString("ToolTipDecoderModel"));
        decoderFamily.setToolTipText(rb.getString("ToolTipDecoderFamily"));
        dateUpdated.setToolTipText(rb.getString("ToolTipDateUpdated"));
        id.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (checkDuplicate()) {
                    JOptionPane.showMessageDialog(pane, rb.getString("ErrorDuplicateID"));
                }
            }
        });

        // New GUI to allow multiline Comment and Decoder Comment fields
        //Set up constraints objects for convenience in GridBagLayout alignment
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        GridBagConstraints cR = new GridBagConstraints();
        Dimension minFieldDim = new Dimension(150, 20);
        Dimension minScrollerDim = new Dimension(165, 42);
        super.setLayout(gbLayout);

        cL.gridx = 0;
        cL.gridy = 0;
        cL.ipadx = 3;
        cL.anchor = GridBagConstraints.NORTHWEST;
        cL.insets = new Insets(0, 0, 0, 15);
        JLabel row0Label = new JLabel(rb.getString("FieldID") + ":");
        gbLayout.setConstraints(row0Label, cL);
        super.add(row0Label);

        cR.gridx = 1;
        cR.gridy = 0;
        cR.anchor = GridBagConstraints.WEST;
        id.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(id, cR);
        super.add(id);

        cL.gridy++;
        JLabel row1Label = new JLabel(rb.getString("FieldRoadName") + ":");
        gbLayout.setConstraints(row1Label, cL);
        super.add(row1Label);

        cR.gridy = cL.gridy;
        roadName.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadName, cR);
        super.add(roadName);

        cL.gridy++;
        JLabel row2Label = new JLabel(rb.getString("FieldRoadNumber") + ":");
        gbLayout.setConstraints(row2Label, cL);
        super.add(row2Label);

        cR.gridy = cL.gridy;
        roadNumber.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadNumber, cR);
        super.add(roadNumber);

        cL.gridy++;
        JLabel row3Label = new JLabel(rb.getString("FieldManufacturer") + ":");
        gbLayout.setConstraints(row3Label, cL);
        super.add(row3Label);

        cR.gridy = cL.gridy;
        mfg.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(mfg, cR);
        super.add(mfg);

        cL.gridy++;
        JLabel row4Label = new JLabel(rb.getString("FieldOwner") + ":");
        gbLayout.setConstraints(row4Label, cL);
        super.add(row4Label);

        cR.gridy = cL.gridy;
        owner.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(owner, cR);
        super.add(owner);

        cL.gridy++;
        JLabel row5Label = new JLabel(rb.getString("FieldModel") + ":");
        gbLayout.setConstraints(row5Label, cL);
        super.add(row5Label);

        cR.gridy = cL.gridy;
        model.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(model, cR);
        super.add(model);

        cL.gridy++;
        JLabel row6Label = new JLabel(rb.getString("FieldDCCAddress") + ":");
        gbLayout.setConstraints(row6Label, cL);
        super.add(row6Label);

        cR.gridy = cL.gridy;
        gbLayout.setConstraints(selPanel, cR);
        super.add(selPanel);

        cL.gridy++;
        JLabel row7Label = new JLabel(rb.getString("FieldSpeedLimit") + ":");
        gbLayout.setConstraints(row7Label, cL);
        super.add(row7Label);

        cR.gridy = cL.gridy; // JSpinner is initialised in RosterEntryPane()
        gbLayout.setConstraints(maxSpeedSpinner, cR);
        super.add(maxSpeedSpinner);

        cL.gridy++;
        JLabel row8Label = new JLabel(rb.getString("FieldComment") + ":");
        gbLayout.setConstraints(row8Label, cL);
        super.add(row8Label);

        cR.gridy = cL.gridy;
        commentScroller.setMinimumSize(minScrollerDim);
        gbLayout.setConstraints(commentScroller, cR);
        super.add(commentScroller);

        cL.gridy++;
        JLabel row9Label = new JLabel(rb.getString("FieldDecoderFamily") + ":");
        gbLayout.setConstraints(row9Label, cL);
        super.add(row9Label);

        cR.gridy = cL.gridy;
        decoderFamily.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderFamily, cR);
        super.add(decoderFamily);

        cL.gridy++;
        JLabel row10Label = new JLabel(rb.getString("FieldDecoderModel") + ":");
        gbLayout.setConstraints(row10Label, cL);
        super.add(row10Label);

        cR.gridy = cL.gridy;
        decoderModel.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderModel, cR);
        super.add(decoderModel);

        cL.gridy++;
        JLabel row11Label = new JLabel(rb.getString("FieldDecoderComment") + ":");
        gbLayout.setConstraints(row11Label, cL);
        super.add(row11Label);

        cR.gridy = cL.gridy;
        decoderCommentScroller.setMinimumSize(minScrollerDim);
        gbLayout.setConstraints(decoderCommentScroller, cR);
        super.add(decoderCommentScroller);

        cL.gridy++;
        JLabel row13Label = new JLabel(rb.getString("FieldDateUpdated") + ":");
        gbLayout.setConstraints(row13Label, cL);
        super.add(row13Label);

        cR.gridy = cL.gridy;
        dateUpdated.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(dateUpdated, cR);
        super.add(dateUpdated);
    }

    double maxSet;

    /**
     * Does the GUI contents agree with a RosterEntry?
     *
     * @param r the entry to compare
     * @return true if entry in GUI does not match r; false otherwise
     */
    public boolean guiChanged(RosterEntry r) {
        if (!r.getRoadName().equals(roadName.getText())) {
            return true;
        }
        if (!r.getRoadNumber().equals(roadNumber.getText())) {
            return true;
        }
        if (!r.getMfg().equals(mfg.getText())) {
            return true;
        }
        if (!r.getOwner().equals(owner.getText())) {
            return true;
        }
        if (!r.getModel().equals(model.getText())) {
            return true;
        }
        if (!r.getComment().equals(comment.getText())) {
            return true;
        }
        if (!r.getDecoderFamily().equals(decoderFamily.getText())) {
            return true;
        }
        if (!r.getDecoderModel().equals(decoderModel.getText())) {
            return true;
        }
        if (!r.getDecoderComment().equals(decoderComment.getText())) {
            return true;
        }
        if (!r.getId().equals(id.getText())) {
            return true;
        }
        maxSet = (Double) maxSpeedSpinner.getValue();
        if (r.getMaxSpeedPCT() != (int) Math.round(100 * maxSet)) {
            log.debug("check: {}|{}", r.getMaxSpeedPCT(), (int) Math.round(100 * maxSet));
            return true;
        }
        DccLocoAddress a = addrSel.getAddress();
        if (a == null) {
            if (!r.getDccAddress().equals("")) {
                return true;
            }
        } else {

            if (r.getProtocol() != a.getProtocol()) {
                return true;
            }
            if (!r.getDccAddress().equals("" + a.getNumber())) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return true if the value in the id JTextField is a duplicate of some
     *         other RosterEntry in the roster
     */
    public boolean checkDuplicate() {
        // check it's not a duplicate
        List<RosterEntry> l = Roster.getDefault().matchingList(null, null, null, null, null, null, id.getText());
        boolean oops = false;
        for (int i = 0; i < l.size(); i++) {
            if (re != l.get(i)) {
                oops = true;
            }
        }
        return oops;
    }

    /**
     * Fill a RosterEntry object from GUI contents.
     *
     * @param r the roster entry to display
     */
    public void update(RosterEntry r) {
        r.setId(id.getText());
        r.setRoadName(roadName.getText());
        r.setRoadNumber(roadNumber.getText());
        r.setMfg(mfg.getText());
        r.setOwner(owner.getText());
        r.setModel(model.getText());
        DccLocoAddress a = addrSel.getAddress();
        if (a != null) {
            r.setDccAddress("" + a.getNumber());
            r.setProtocol(a.getProtocol());
        }
        r.setComment(comment.getText());

        maxSet = (Double) maxSpeedSpinner.getValue();
        log.debug("maxSet saved: {}", maxSet);
        r.setMaxSpeedPCT((int) Math.round(100 * maxSet));
        log.debug("maxSet read from config: {}", r.getMaxSpeedPCT());
        r.setDecoderFamily(decoderFamily.getText());
        r.setDecoderModel(decoderModel.getText());
        r.setDecoderComment(decoderComment.getText());
    }

    /**
     * File GUI from roster contents.
     *
     * @param r the roster entry to display
     */
    public void updateGUI(RosterEntry r) {
        roadName.setText(r.getRoadName());
        roadNumber.setText(r.getRoadNumber());
        mfg.setText(r.getMfg());
        owner.setText(r.getOwner());
        model.setText(r.getModel());
        comment.setText(r.getComment());
        decoderModel.setText(r.getDecoderModel());
        decoderFamily.setText(r.getDecoderFamily());
        decoderComment.setText(r.getDecoderComment());
        dateUpdated.setText((r.getDateModified() != null)
                ? DateFormat.getDateTimeInstance().format(r.getDateModified())
                : r.getDateUpdated());
        // retrieve MaxSpeed from r
        double maxSpeedSet = r.getMaxSpeedPCT() / 100d; // why resets to 100?
        log.debug("Max Speed set to: {}", maxSpeedSet);
        maxSpeedSpinner.setValue(maxSpeedSet);
        log.debug("Max Speed in spinner: {}", maxSpeedSpinner.getValue());
    }

    public void setDccAddress(String a) {
        DccLocoAddress addr = addrSel.getAddress();
        LocoAddress.Protocol protocol = addr.getProtocol();
        addrSel.setAddress(new DccLocoAddress(Integer.parseInt(a), protocol));
    }

    public void setDccAddressLong(boolean m) {
        DccLocoAddress addr = addrSel.getAddress();
        int n = 0;
        if (addr != null) {
            //If the protocol is already set to something other than DCC, then do not try to configure it as DCC long or short.
            if (addr.getProtocol() != LocoAddress.Protocol.DCC_LONG
                    && addr.getProtocol() != LocoAddress.Protocol.DCC_SHORT
                    && addr.getProtocol() != LocoAddress.Protocol.DCC) {
                return;
            }
            n = addr.getNumber();
        }
        addrSel.setAddress(new DccLocoAddress(n, m));
    }

    public void dispose() {
        log.debug("dispose");
    }

    private final static Logger log = LoggerFactory.getLogger(RosterEntryPane.class);

}
