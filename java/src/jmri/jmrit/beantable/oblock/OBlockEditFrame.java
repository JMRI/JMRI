package jmri.jmrit.beantable.oblock;

import jmri.*;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.swing.NamedBeanComboBox;
import jmri.util.JmriJFrame;

import javax.swing.*;
//import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to edit OBlock objects in tabbed table interface.
 * Adapted from AbstractAudioFrame + -ListenerFrame.
 *
 * @author Matthew Harris copyright (c) 2009
 * @author Egbert Broerse copyright (c) 2020
 */
public class OBlockEditFrame extends JmriJFrame {

    OBlockEditFrame frame = this;
    OBlockManager obm;

    JPanel main = new JPanel();
    private final JScrollPane scroll
            = new JScrollPane(main,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    OBlockTableModel _model;
    String _oblock;
    TableFrames _core;

    // Common UI components for Add/Edit OBlock
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    JLabel lengthLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("Length")));
    JLabel curveLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BlockCurveColName")));
    JLabel currentLabel = new JLabel(Bundle.getMessage("Current"));
    //        tempRow[PERMISSIONCOL] = Bundle.getMessage("Permissive");
    JLabel sensorLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameSensor")));
    JLabel errorSensorLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ErrorSensorCol")));
    JLabel sysName = new JLabel();
    JTextField userName = new JTextField(15);
    JTextField length = new JTextField(5);
    JToggleButton unit = new JToggleButton("cm");
    JComboBox<String> curveBox = new JComboBox<>(OBlockTableModel.curveOptions);
    JTextField current = new JTextField(15);
    JTextArea comment  = new JTextArea();
    private final NamedBeanComboBox<Sensor> sensorBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SensorManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Sensor> errorSensorBox = new NamedBeanComboBox<>(InstanceManager.getDefault(SensorManager.class), null, NamedBean.DisplayOptions.DISPLAYNAME);
    JButton addButton;
    JLabel statusBarLabel = new JLabel(Bundle.getMessage("HardwareAddStatusEnter"), JLabel.LEADING);
    java.text.DecimalFormat twoDigit = new java.text.DecimalFormat("0.00");
    JTable pathTable;
    private final static String PREFIX = "OB";

    /**
     * Standard constructor
     *
     * @param title Title of this OBlockEditFrame
     * @param model OBlockTableModel holding OBlock data
     */
    public OBlockEditFrame(String title, String oblock, OBlockTableModel model, TableFrames parent) {
        super(title);
        _oblock = oblock;
        _model = model;
        _core = parent;
        obm = InstanceManager.getDefault(OBlockManager.class);
        layoutFrame();
        populateFrame(obm.getOBlock(oblock));
    }

    /**
     * Layout the frame.
     */
    public void layoutFrame() {
        frame.addHelpMenu("package.jmri.jmrit.beantable.OBlockTable", true);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        frame.setSize(350, 400);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
        //p.setLayout(new FlowLayout());
        //comment

        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(sysNameLabel);
        sysNameLabel.setLabelFor(sysName);
        p1.add(sysName);
        p.add(p1);
        p1 = new JPanel();
        p1.add(userNameLabel);
        userNameLabel.setLabelFor(userName);
        p1.add(userName);
        p.add(p1);
        frame.getContentPane().add(p);

        p = new JPanel();
        //p.setLayout(new FlowLayout());
        p1 = new JPanel();
        p1.add(lengthLabel);
        lengthLabel.setLabelFor(length);
        p1.add(length);
        p1.add(unit);
        p.add(p1);

        p1 = new JPanel();
        p1.add(curveLabel);
        curveLabel.setLabelFor(curveBox);
        p1.add(curveBox);
        p.add(p1);

        p1 = new JPanel();
        p.add(currentLabel);
        currentLabel.setLabelFor(current);
        p.add(current);
        p.add(p1);

        p1 = new JPanel();
        p1.add(pathTable);
        p.add(p1);

        p1 = new JPanel();
        p1.add(sensorLabel);
        sensorLabel.setLabelFor(sensorBox);
        p1.add(sensorBox);
        p.add(p1);
        frame.getContentPane().add(p);
        p1 = new JPanel();
        p1.add(errorSensorLabel);
        errorSensorLabel.setLabelFor(errorSensorBox);
        p1.add(errorSensorBox);
        p.add(p1);
        frame.getContentPane().add(p);
        p = new JPanel();
        JButton cancel;
        p.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener((ActionEvent e) -> {
            frame.dispose();
        });
        JButton apply;
        p.add(apply = new JButton(Bundle.getMessage("ButtonApply")));
        apply.addActionListener(this::applyPressed);
        JButton ok;
        p.add(ok = new JButton(Bundle.getMessage("ButtonOK")));
        ok.addActionListener((ActionEvent e) -> {
            applyPressed(e);
            frame.dispose();
        });
        frame.getContentPane().add(p);

        //frame.add(scroll);
        pack();
    }

    /**
     * Populate the Edit OBlock frame with default values.
     */
    public void resetFrame() {
        sysName.setText(null);
        userName.setText(null);

        //this.newOBlock = true;
    }

    /**
     * Populate the OBlock frame with current values.
     *
     * @param ob OBlock object to use
     */
    public void populateFrame(OBlock ob) {
        sysName.setText(ob.getSystemName());
        userName.setText(ob.getUserName());
        length.setText(twoDigit.format(ob.getLengthCm()));
        curveBox.setSelectedItem(ob.getCurvature());
        sensorBox.setSelectedItem(ob.getSensor());

        BlockPathTableModel pathModel = new BlockPathTableModel(ob, _core);
        pathTable = new JTable(pathModel);
        // formatting?

        //permission
//        tempRow[PERMISSIONCOL] = Bundle.getMessage("Permissive");
//        tempRow[SPEEDCOL] = "";

        unit.addActionListener((ActionEvent e) -> unit.setText(unit.getText().equals(Bundle.getMessage("cm")) ? Bundle.getMessage("in") : Bundle.getMessage("cm")));
    }

    private void applyPressed(ActionEvent e) {
        String user = userName.getText().trim();
        if (user.equals("")) {
            user = null;
        }
        OBlock ob = obm.getOBlock(_oblock);
        if (ob != null) {
            ob.setUserName(user);
//            //      try {
//            OBlock block = fromBlockComboBox.getItemAt(fromBlockComboBox.getSelectedIndex());
//            if (block != null) {
//                portal.setFromBlock(block, true);
//            }
//            block = toBlockComboBox.getItemAt(toBlockComboBox.getSelectedIndex());
//            if (block != null) {
//                ob.setToBlock(block, true);
//                //  } catch (IllegalArgumentException ex) {
//                //     JOptionPane.showMessageDialog(null, ex.getMessage(), Bundle.getMessage("PortalCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
//            }
        }

        // Notify changes
        _model.fireTableDataChanged();
    }

    /**
     * Check System Name user input.
     *
     * @param entry string retrieved from text field
     * @param counter index of all similar (OBlock) items
     * @param prefix (Oblock/Portal/path/signal) system name prefix string to compare entry against
     * @return true if prefix doesn't match
     */
    protected boolean entryError(String entry, String prefix, String counter) {
        if (!entry.startsWith(prefix)) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("OBlockCreateError", prefix),
                    Bundle.getMessage("OBlockCreateErrorTitle"), JOptionPane.ERROR_MESSAGE);
            sysName.setText(prefix + counter);
            return true;
        }
        return false;
    }

    //private static final Logger log = LoggerFactory.getLogger(OBlockEditFrame.class);

}
