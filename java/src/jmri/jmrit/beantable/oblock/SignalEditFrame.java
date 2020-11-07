package jmri.jmrit.beantable.oblock;

import jmri.*;
import jmri.jmrit.beantable.beanedit.BeanEditItem;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

/**
 * Defines a GUI for editing OBlock - Signal objects in the tabbed Table interface.
 * Based on AudioSourceFrame.
 *
 * @author Matthew Harris copyright (c) 2009
 * @author Egbert Broerse (C) 2020
 */
public class SignalEditFrame extends JmriJFrame {

    JPanel main = new JPanel();
//    private final JScrollPane scroll
//            = new JScrollPane(main,
//            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
//            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    SignalTableModel model;
    NamedBean signal;
    PortalManager pm;
    //SignalHeadManager shm;

    //private final Object lock = new Object();

    // UI components for Add/Edit Signal (head or mast)
    JLabel signalMastLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSignalMast")));
    JLabel signalHeadLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSignalHead")));
    JLabel fromBlockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("FromBlockName")));
    JLabel toBlockLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("OppBlockName")));
    String[] p0 = {""};
    private final JComboBox<String> portalComboBox = new JComboBox<>(p0);
    private final NamedBeanComboBox<SignalMast> sigMastComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalMastManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<SignalHead> sigHeadComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SignalHeadManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<OBlock> fromBlockComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(OBlockManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<OBlock> toBlockComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(OBlockManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    JLabel portalLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNamePortal")));
    // the following 3 items copied from beanedit, place in separate static method?
    JSpinner lengthSpinner = new JSpinner(); // 2 digit decimal format field, initialized later as instance
    JRadioButton inch = new JRadioButton(Bundle.getMessage("LengthInches"));
    JRadioButton cm = new JRadioButton(Bundle.getMessage("LengthCentimeters"));
    JLabel statusBar = new JLabel(Bundle.getMessage("AddBeanStatusEnter"), JLabel.LEADING);

    private final SignalEditFrame frame = this;
    private Portal _portal;
    SignalTableModel.SignalRow _sr;
    private boolean _newSignal;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public SignalEditFrame(String title, NamedBean signal, SignalTableModel.SignalRow sr, SignalTableModel model) {
        super(title);
        this.model = model;
        this.signal = signal;
        if (signal == null) {
            _newSignal = true;
        }
        _sr = sr;
        pm = InstanceManager.getDefault(PortalManager.class);
        for (Portal pi : pm.getPortalSet()) {
            portalComboBox.addItem(pi.getName());
        }
        layoutFrame();
        if (_sr != null) {
            _portal = sr.getPortal();
            populateFrame(_sr);
        } else {
            resetFrame();
        }
    }

    public void layoutFrame() {
        frame.addHelpMenu("package.jmri.jmrit.beantable.OBlockTable", true);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
        main.setLayout(new BoxLayout(main, BoxLayout.PAGE_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));

        JPanel p1 = new JPanel();
        p1.add(signalMastLabel);
        p1.add(sigMastComboBox);
        sigMastComboBox.setAllowNull(true);
        p1.add(signalHeadLabel);
        p1.add(sigHeadComboBox);
        sigHeadComboBox.setAllowNull(true);
        p.add(p1);
        sigMastComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sigMastComboBox.getSelectedIndex() > 0) {
                    sigHeadComboBox.setSelectedIndex(0); // either one
                    model.checkDuplicateSignal(sigMastComboBox.getSelectedItem());
                }
            }
        });
        sigHeadComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sigHeadComboBox.getSelectedIndex() > 0) {
                    sigMastComboBox.setSelectedIndex(0); // either one
                    model.checkDuplicateSignal(sigMastComboBox.getSelectedItem());
                }
            }
        });

        p1 = new JPanel();
        p1.add(fromBlockLabel);
        p1.add(fromBlockComboBox);
        fromBlockComboBox.setAllowNull(true);
        p1.add(toBlockLabel);
        p1.add(toBlockComboBox);
        toBlockComboBox.setAllowNull(true);
        p.add(p1);

        p1 = new JPanel();
        p1.add(portalLabel);
        p1.add(portalComboBox); // has a black first item
        p.add(p1);

        // copied from beanedit
        lengthSpinner.setModel(
                new SpinnerNumberModel(Float.valueOf(0f), Float.valueOf(0f), Float.valueOf(1000f), Float.valueOf(0.01f)));
        lengthSpinner.setEditor(new JSpinner.NumberEditor(lengthSpinner, "###0.00"));
        lengthSpinner.setPreferredSize(new JTextField(8).getPreferredSize());
        lengthSpinner.setValue(0f); // reset from possible previous use
        p1 = new JPanel();
        p1.add(new JLabel(Bundle.getMessage("Offset")));
        p1.add(lengthSpinner);
        lengthSpinner.setToolTipText(Bundle.getMessage("OffsetToolTip"));

        ButtonGroup bg = new ButtonGroup();
        bg.add(inch);
        bg.add(cm);
        p.add(p1);

        p1 = new JPanel();
        p1.add(inch);
        p1.add(cm);
        p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));
        inch.setSelected(true);
        inch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cm.setSelected(!inch.isSelected());
                updateLength();
            }
        });
        cm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inch.setSelected(!cm.isSelected());
                updateLength();
            }
        });
        p1.add(new JLabel("Unit")); //Bundle.getMessage("LengthUnits")));
        p.add(p1);

        JPanel p2 = new JPanel();
        p2.add(statusBar);
        p.add(p2);

        p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
        JButton cancel;
        p2.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((ActionEvent e) -> {
            frame.dispose();
        });
        JButton apply;
        p2.add(apply = new JButton(Bundle.getMessage("ButtonApply")));
        apply.addActionListener(this::applyPressed);
        JButton ok;
        p2.add(ok = new JButton(Bundle.getMessage("ButtonOK")));
        ok.addActionListener((ActionEvent e) -> {
            applyPressed(e);
            frame.dispose();
        });
        p.add(p2);

        //main.add(p);
        frame.getContentPane().add(p);

        //frame.add(scroll);
    }

    /**
     * Populate the Edit Signal frame with default values.
     */
    public void resetFrame() {
        if (sigMastComboBox.getItemCount() > 0) {
            sigMastComboBox.setSelectedIndex(0);
        }
        if (sigHeadComboBox.getItemCount() > 0) {
            sigHeadComboBox.setSelectedIndex(0);
        }
        lengthSpinner.setValue(0f);
        // reset statusBar text
        statusBar.setText(Bundle.getMessage("AddBeanStatusEnter"));
        statusBar.setForeground(Color.gray);
        _newSignal = true;
    }

    /**
     * Populate the Edit Signal frame with current values.
     */
    public void populateFrame(SignalTableModel.SignalRow sr) {
        if (sr == null) {
            throw new IllegalArgumentException("Null Signal object");
        }
        fromBlockComboBox.setSelectedItemByName(sr.getFromBlock().getDisplayName());
        toBlockComboBox.setSelectedItemByName(sr.getToBlock().getDisplayName());
        if (signal instanceof SignalMast) {
            sigMastComboBox.setSelectedItemByName(sr.getSignal().getDisplayName());
        } else if (signal instanceof SignalHead) {
            sigHeadComboBox.setSelectedItemByName(sr.getSignal().getDisplayName());
        }
        portalComboBox.setSelectedItem(_portal);
        lengthSpinner.setValue(sr.getLength());
        _newSignal = false;
    }

    private void applyPressed(ActionEvent e) {
        if (sigMastComboBox.getSelectedIndex() > 0) {
            signal = sigMastComboBox.getSelectedItem();
        } else if (sigHeadComboBox.getSelectedIndex() > 0) {
            signal = sigHeadComboBox.getSelectedItem();
        } else {
            signal = null;
        }
        _portal = pm.getPortal((String) portalComboBox.getSelectedItem());
        if (_portal == null) {
            statusBar.setText(Bundle.getMessage("AddBeanStatusEnter"));
            statusBar.setForeground(Color.red);
            return;
        } else {
            statusBar.setForeground(Color.gray);
        }
        if (_portal.setProtectSignal(signal, (float) lengthSpinner.getValue(), toBlockComboBox.getSelectedItem())) {
            if ((fromBlockComboBox.getSelectedIndex() == 0) && (toBlockComboBox.getSelectedIndex() > 0)) {
                _portal.setFromBlock(_portal.getOpposingBlock(Objects.requireNonNull(toBlockComboBox.getSelectedItem())), true);
            }

        }
        //            sr = new SignalTableModel.SignalRow(signal, fromBlockComboBox.getSelectedItem(),
        //            _portal, toBlockComboBox.getSelectedItem(), (float) lengthSpinner.getValue(),
        //            cm.isSelected());
//            if (portal.setProtectSignal(sr.getSignal(), length, sr.getToBlock())) {
//                if (sr.getFromBlock() == null) {
//                    sr.setFromBlock(portal.getOpposingBlock(sr.getToBlock()));
//                }
//            }

        // Notify changes
        model.fireTableDataChanged();
    }

    // copied from beanedit
    private void updateLength() {
        float len = (float) lengthSpinner.getValue();
        if (inch.isSelected()) {
            lengthSpinner.setValue(len/2.54f);
        } else {
            lengthSpinner.setValue(len*2.54f);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(SignalEditFrame.class);

}
