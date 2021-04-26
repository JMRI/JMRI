package jmri.jmrit.beantable.oblock;

import jmri.*;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * Defines a GUI for editing OBlock - Signal objects in the tabbed Table interface.
 * Adapted from AudioSourceFrame.
 * Compare to CPE CircuitBuilder Signal Config frame {@link jmri.jmrit.display.controlPanelEditor.EditSignalFrame}
 *
 * @author Matthew Harris copyright (c) 2009
 * @author Egbert Broerse (C) 2020
 */
public class SignalEditFrame extends JmriJFrame {

    JPanel main = new JPanel();

    SignalTableModel model;
    NamedBean signal;
    PortalManager pm;
    OBlockManager obm;
    private final SignalEditFrame frame = this;
    private Portal _portal;
    SignalTableModel.SignalRow _sr;
    //private final Object lock = new Object();

    // UI components for Add/Edit Signal (head or mast)
    JLabel portalLabel = new JLabel(Bundle.getMessage("AtPortalLabel"), JLabel.TRAILING);

    JLabel signalMastLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSignalMast")));
    JLabel signalHeadLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSignalHead")));
    JLabel fromBlockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("FromBlockName")));
    JLabel fromBlock = new JLabel();
    JLabel toBlockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ToBlockName")));
    JLabel toBlock = new JLabel();
    JLabel mastName = new JLabel();
    JLabel headName = new JLabel();
    String[] p0 = {""};
    private final JComboBox<String> portalComboBox = new JComboBox<>(p0);
    private final NamedBeanComboBox<SignalMast> sigMastComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalMastManager.class),
            null, NamedBean.DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> sigHeadComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalHeadManager.class),
            null, NamedBean.DisplayOptions.DISPLAYNAME);
//    private final NamedBeanComboBox<OBlock> fromBlockComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(OBlockManager.class),
//            null, NamedBean.DisplayOptions.DISPLAYNAME);
//    private final NamedBeanComboBox<OBlock> toBlockComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(OBlockManager.class),
//            null, NamedBean.DisplayOptions.DISPLAYNAME);
    private final JButton flipButton = new JButton(Bundle.getMessage("ButtonFlipBlocks"));
    // the following 3 items copied from beanedit, place in separate static method?
    JSpinner lengthSpinner = new JSpinner(); // 2 digit decimal format field, initialized later as instance
    JRadioButton inch = new JRadioButton(Bundle.getMessage("LengthInches"));
    JRadioButton cm = new JRadioButton(Bundle.getMessage("LengthCentimeters"));
    JLabel statusBar = new JLabel(Bundle.getMessage("AddXStatusInitial1",
            (Bundle.getMessage("BeanNameSignalMast") + "/" + Bundle.getMessage("BeanNameSignalHead")),
            Bundle.getMessage("ButtonOK")));

    private boolean _newSignal;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public SignalEditFrame(@Nonnull String title,
                           @CheckForNull NamedBean signal,
                           @CheckForNull SignalTableModel.SignalRow sr,
                           @CheckForNull SignalTableModel model) {
        super(title, true, true);
        this.model = model;
        this.signal = signal;
        if (signal == null) {
            _newSignal = true;
        }
        log.debug("SR == {}", (sr == null ? "null" : "not null"));
        obm = InstanceManager.getDefault(OBlockManager.class);
        pm = InstanceManager.getDefault(PortalManager.class);
        for (Portal pi : pm.getPortalSet()) {
            portalComboBox.addItem(pi.getName());
        }
        layoutFrame();
        if (sr != null) {
            _sr = sr;
            _portal = sr.getPortal();
            populateFrame(_sr);
        } else {
            resetFrame();
        }
        addCloseListener(this);
    }

    public void layoutFrame() {
        frame.addHelpMenu("package.jmri.jmrit.beantable.OBlockTable", true);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
        frame.setSize(300, 200);
        main.setLayout(new BoxLayout(main, BoxLayout.PAGE_AXIS));

        JPanel configGrid = new JPanel();
        GridLayout layout = new GridLayout(4, 2, 10, 0); // (int rows, int cols, int hgap, int vgap)
        configGrid.setLayout(layout);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));

        // row 1
        JPanel p1 = new JPanel();
        p1.add(signalMastLabel);
        p1.add(sigMastComboBox);
        sigMastComboBox.setAllowNull(true);
        p1.add(mastName);
        configGrid.add(p1);

        p1 = new JPanel();
        p1.add(signalHeadLabel);
        p1.add(sigHeadComboBox);
        sigHeadComboBox.setAllowNull(true);
        p1.add(headName);
        configGrid.add(p1);
        sigMastComboBox.addActionListener(e -> {
            if ((sigMastComboBox.getSelectedIndex() > 0) && (sigHeadComboBox.getItemCount() > 0)) {
                sigHeadComboBox.setSelectedIndex(0); // either one
                model.checkDuplicateSignal(sigMastComboBox.getSelectedItem());
            }
        });
        sigHeadComboBox.addActionListener(e -> {
            if ((sigHeadComboBox.getSelectedIndex() > 0) && (sigMastComboBox.getItemCount() > 0)) {
                sigMastComboBox.setSelectedIndex(0); // either one
                model.checkDuplicateSignal(sigHeadComboBox.getSelectedItem());
            }
        });

        // row 2
        p1 = new JPanel();
        p1.add(portalLabel);
        p1.add(portalComboBox); // combo has a blank first item
        portalComboBox.addActionListener(e -> {
            if (portalComboBox.getSelectedIndex() > 0) {
                fromBlock.setText(pm.getPortal((String) portalComboBox.getSelectedItem()).getFromBlockName());
                toBlock.setText(pm.getPortal((String) portalComboBox.getSelectedItem()).getToBlockName());
            }
        });
        configGrid.add(p1);
        flipButton.addActionListener(e -> {
            String left = fromBlock.getText();
            fromBlock.setText(toBlock.getText());
            toBlock.setText(left);
        });
        p1 = new JPanel();
        p1.add(flipButton);
        flipButton.setToolTipText(Bundle.getMessage("FlipToolTip"));
        configGrid.add(p1);

        // row 3
        p1 = new JPanel();
        p1.add(fromBlockLabel);
        p1.add(fromBlock);
//        fromBlockComboBox.setAllowNull(true);
//        fromBlockComboBox.addActionListener(e -> {
//            if (fromBlockComboBox.getSelectedIndex() == toBlockComboBox.getSelectedIndex()) {
//                toBlockComboBox.setSelectedIndex(0);
//            }
//        });
        configGrid.add(p1);

        p1 = new JPanel();
        p1.add(toBlockLabel);
        p1.add(toBlock);
//        toBlockComboBox.setAllowNull(true);
//        toBlockComboBox.addActionListener(e -> {
//            if (fromBlockComboBox.getSelectedIndex() == toBlockComboBox.getSelectedIndex()) {
//                fromBlockComboBox.setSelectedIndex(0);
//            }
//        });
        configGrid.add(p1);

        // row 4
        // copied from beanedit, also in BlockPathEditFrame
        p1 = new JPanel();
        p1.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Offset"))));
        lengthSpinner.setModel(
                new SpinnerNumberModel(Float.valueOf(0f), Float.valueOf(-2000f), Float.valueOf(2000f), Float.valueOf(0.01f)));
        lengthSpinner.setEditor(new JSpinner.NumberEditor(lengthSpinner, "###0.00"));
        lengthSpinner.setPreferredSize(new JTextField(8).getPreferredSize());
        lengthSpinner.setValue(0f); // reset from possible previous use
        lengthSpinner.setToolTipText(Bundle.getMessage("OffsetToolTip"));
        p1.add(lengthSpinner);
        configGrid.add(p1);

        ButtonGroup bg = new ButtonGroup();
        bg.add(inch);
        bg.add(cm);

        p1 = new JPanel();
        p1.add(inch);
        p1.add(cm);
        p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));
        inch.setSelected(true);
        inch.addActionListener(e -> {
            cm.setSelected(!inch.isSelected());
            updateLength();
        });
        cm.addActionListener(e -> {
            inch.setSelected(!cm.isSelected());
            updateLength();
        });
        configGrid.add(p1);
        p.add(configGrid);

        p.add(Box.createHorizontalGlue());

        JPanel p2 = new JPanel();
        statusBar.setFont(statusBar.getFont().deriveFont(0.9f * signalMastLabel.getFont().getSize())); // a bit smaller
        statusBar.setForeground(Color.gray);
        p2.add(statusBar);
        p.add(p2);

        p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
        JButton cancel;
        p2.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((ActionEvent e) -> closeFrame());
        JButton ok;
        p2.add(ok = new JButton(Bundle.getMessage("ButtonOK")));
        ok.addActionListener(this::applyPressed);
        p.add(p2);

        //main.add(p);
        frame.getContentPane().add(p);
        
        frame.setEscapeKeyClosesWindow(true);
        frame.getRootPane().setDefaultButton(ok);
        
        //frame.add(scroll);
        frame.pack();
    }

    /**
     * Reset the Edit Signal frame with default values.
     */
    public void resetFrame() {
        if (sigMastComboBox.getItemCount() > 0) {
            sigMastComboBox.setSelectedIndex(0);
        }
        if (sigHeadComboBox.getItemCount() > 0) {
            sigHeadComboBox.setSelectedIndex(0);
        }
        if (portalComboBox.getItemCount() > 0) {
            portalComboBox.setSelectedIndex(0);
        }
        lengthSpinner.setValue(0f);
        // reset statusBar text
        if ((sigMastComboBox.getItemCount() == 0) && (sigHeadComboBox.getItemCount() == 0)) {
            status(Bundle.getMessage("NoSignalWarning"), true);
        } else if (portalComboBox.getItemCount() > 1) {
            status(Bundle.getMessage("AddXStatusInitial1",
                    (Bundle.getMessage("BeanNameSignalMast")+"/"+Bundle.getMessage("BeanNameSignalHead")),
                    Bundle.getMessage("ButtonOK")), false); // I18N to include original button name in help string
        } else {
            status(Bundle.getMessage("NoSignalPortal"), true);
        }
        mastName.setVisible(false);
        headName.setVisible(false);
        sigMastComboBox.setVisible(true);
        sigHeadComboBox.setVisible(true);
        frame.pack();
    }

    /**
     * Populate the Edit Signal frame with current values from a SignalRow in the SignalTable.
     *
     * @param sr existing SignalRow to copy the attributes from
     */
    public void populateFrame(SignalTableModel.SignalRow sr) {
        if (sr == null) {
            throw new IllegalArgumentException("Null Signal object");
        }
        status(Bundle.getMessage("AddXStatusInitial3", sr.getSignal().getDisplayName(),
                Bundle.getMessage("ButtonOK")), false);
        fromBlock.setText(sr.getFromBlock().getDisplayName());
        toBlock.setText(sr.getToBlock().getDisplayName());
        if (signal instanceof SignalMast) {
            mastName.setText(sr.getSignal().getDisplayName());
            headName.setText("-");
            //sigMastComboBox.setSelectedItemByName(sr.getSignal().getDisplayName()); // combo hidden for Edits
        } else if (signal instanceof SignalHead) {
            mastName.setText("-");
            headName.setText(sr.getSignal().getDisplayName());
            //sigHeadComboBox.setSelectedItemByName(sr.getSignal().getDisplayName()); // combo hidden for Edits
        }
        portalComboBox.setSelectedItem(_portal.getName());
        cm.setSelected(sr._isMetric); // before filling in value in spinner prevent recalc
        if (sr.isMetric()) {
            lengthSpinner.setValue(sr.getLength()/10);
        } else {
            lengthSpinner.setValue(sr.getLength()/25.4f);
        }
        mastName.setVisible(true);
        headName.setVisible(true);
        sigMastComboBox.setVisible(false);
        sigHeadComboBox.setVisible(false);
        frame.pack();
        _newSignal = false;
    }

    private void applyPressed(ActionEvent e) {
        if (_newSignal) { // can't change an existing mast, easy to delete and recreate
            if (sigMastComboBox.getSelectedIndex() > 0) {
                signal = sigMastComboBox.getSelectedItem();
            } else if (sigHeadComboBox.getSelectedIndex() > 0) {
                signal = sigHeadComboBox.getSelectedItem();
            } else {
                status(Bundle.getMessage("WarnNoSignal"), true);
                return;
            }
            String msg = model.checkDuplicateSignal(signal);
            if (msg != null) {
                status(msg, true);
                return;
            }
        }
        _portal = pm.getPortal((String) portalComboBox.getSelectedItem());
        if (_portal == null || portalComboBox.getSelectedIndex() < 1) {
            status(Bundle.getMessage("WarnNoPortal"), true);
            return;
        }
        if (!_newSignal) {
            model.deleteSignal(_sr);    // delete old in Portal if it was set
            _sr.setPortal(_portal);
        }
        // fetch physical details
        float length;
        if (cm.isSelected()) {
            length = (float) lengthSpinner.getValue()*10.0f;
        } else {
            length = (float) lengthSpinner.getValue()*25.4f;
        }

        if (_portal.setProtectSignal(signal, length, obm.getOBlock(toBlock.getText()))) {
            if ((fromBlock.getText() == null) && (toBlock.getText() != null)) { // could be read from old panels?
                _portal.setFromBlock(_portal.getOpposingBlock(obm.getOBlock(Objects.requireNonNull(toBlock.getText()))), true);
            }
        }
        // update Metric choice in ProtectedBlock
        if (toBlock.getText() != null) {
            Objects.requireNonNull(obm.getOBlock(toBlock.getText())).setMetricUnits(cm.isSelected());
        }
        // Notify changes
        model.fireTableDataChanged();

        closeFrame();
    }

    protected void closeFrame(){
        // remind to save, if Turnout was created or edited
        //        if (isDirty) {
        //            showReminderMessage();
        //            isDirty = false;
        //        }
        // hide frame
        setVisible(false);

        model.setEditMode(false);
        log.debug("SignalEditFrame.closeFrame signalEdit=False");
        frame.dispose();
    }

    // copied from beanedit, also in BlockPathEditFrame
    private void updateLength() {
        float len = (float) lengthSpinner.getValue();
        if (inch.isSelected()) {
            lengthSpinner.setValue(len/2.54f);
        } else {
            lengthSpinner.setValue(len*2.54f);
        }
    }

    void status(String message, boolean warn){
        statusBar.setText(message);
        statusBar.setForeground(warn ? Color.red : Color.gray);
    }

    // listen for frame closing
    void addCloseListener(JmriJFrame frame) {
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                model.setEditMode(false);
                log.debug("SignalEditFrame.closeFrame signalEdit=False");
                frame.dispose();
            }
        });
    }

    private static final Logger log = LoggerFactory.getLogger(SignalEditFrame.class);

}
