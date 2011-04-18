package jmri.jmrit.display.controlPanelEditor;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.util.NamedBeanHandle;

import jmri.jmrit.display.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jmri.jmrit.logix.*;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2011
 * 
 */

public class EditCircuitFrame extends JFrame {

    private OBlock _block;
    private CircuitBuilder _parent;
    private boolean _needConversion;

    private JTextField  _detectorSensorName = new JTextField();
    private JTextField  _errorSensorName = new JTextField();
    private JTextField  _numTrackSeg = new JTextField();
    private JTextField  _numTurnouts = new JTextField();

    static java.util.ResourceBundle rbcp = ControlPanelEditor.rbcp;
    static int STRUT_SIZE = 10;

    public EditCircuitFrame(String title, CircuitBuilder parent, String sysName) {
        _block = InstanceManager.oBlockManagerInstance().getBySystemName(sysName);
        setTitle(java.text.MessageFormat.format(title, _block.getDisplayName()));
        _parent = parent;
        makeContentPanel();
        _parent.setEditColors();
        updateContentPanel();
    }

    private void makeContentPanel() {
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                ClosingEvent();
            }
        });
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel p = new JPanel();
        p.add(new JLabel(rbcp.getString("AddRemoveIcons")));
        contentPane.add(p);
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        p = new JPanel();
        p.add(new JLabel(rbcp.getString("numTrackElements")));
        contentPane.add(p);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalGlue());
        panel.add(CircuitBuilder.makeTextBoxPanel(
                    false, _numTrackSeg, "Segments", false, null));
        _numTrackSeg.setPreferredSize(new Dimension(20, _numTrackSeg.getPreferredSize().height));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));

        panel.add(CircuitBuilder.makeTextBoxPanel(
                    false, _numTurnouts, "Turnouts", false, null));
        _numTurnouts.setPreferredSize(new Dimension(20, _numTurnouts.getPreferredSize().height));
        panel.add(Box.createHorizontalStrut(STRUT_SIZE));
        contentPane.add(panel);
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        _detectorSensorName.setPreferredSize(new Dimension(300, _detectorSensorName.getPreferredSize().height));
        panel.add(CircuitBuilder.makeTextBoxPanel(
                    false, _detectorSensorName, "DetectionSensor", true, "detectorSensorName"));
        _detectorSensorName.setToolTipText(rbcp.getString("detectorSensorName"));
        contentPane.add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        _errorSensorName.setPreferredSize(new Dimension(300, _errorSensorName.getPreferredSize().height));
        panel.add(CircuitBuilder.makeTextBoxPanel(
                    false, _errorSensorName, "ErrorSensor", true, "detectorErrorName"));
        _errorSensorName.setToolTipText(rbcp.getString("detectorErrorName"));
        contentPane.add(panel);

        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        contentPane.add(MakeButtonPanel());
        JPanel border = new JPanel();
        border.setLayout(new java.awt.BorderLayout(10,10));
        border.add(contentPane);
        setContentPane(border);
        setSize(500, 500);
        pack();
        setVisible(true);
    }

    private JPanel MakeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton convertButton = new JButton(rbcp.getString("ButtonConvertIcon"));
        convertButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    _parent.convertIcons();
                }
        });
        convertButton.setToolTipText(rbcp.getString("ToolTipConvertIcon"));
        panel.add(convertButton);

        JButton doneButton = new JButton(rbcp.getString("ButtonDone"));
        doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    ClosingEvent();
                }
        });
        panel.add(doneButton);

        JButton deleteButton = new JButton(rbcp.getString("ButtonDelete"));
        deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    deleteCircuit();
                 }
        });
        deleteButton.setToolTipText(rbcp.getString("ToolTipDeleteCircuit"));
        panel.add(deleteButton);
        buttonPanel.add(panel);

        return buttonPanel;
    }

    /************************* end setup **************************/

    private void deleteCircuit() {
        _parent.removeBlock(_block);
        _parent.closeCircuitFrame(null);
        dispose();
    }

    private void updateContentPanel() {
        updateIconList(_parent.getSelectionGroup());
        String name = "";
        Sensor sensor = _block.getSensor();
        if (sensor!=null) {
            name = sensor.getDisplayName();
        }
        _detectorSensorName.setText(name);        
        sensor = _block.getErrorSensor();
        if (sensor!=null) {
            name = sensor.getDisplayName();
        } else {
            name = "";
        }
        _errorSensorName.setText(name);        
    }

    private Sensor getDetectorSensor() {
        String sensorName = _detectorSensorName.getText();
        Sensor sensor = null;
        if (sensorName!=null && sensorName.trim().length()>0) {
            return InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        }
        return null;
    }

    private void ClosingEvent() {
        // check Sensors
        boolean close = true;
        Sensor sensor = getDetectorSensor();
        if (sensor==null) {
            JOptionPane.showMessageDialog(this, rbcp.getString("noDetecterSensor"), 
                            rbcp.getString("noSensor"), JOptionPane.INFORMATION_MESSAGE);
        }
        _block.setSensor(sensor);

        String errorName = _errorSensorName.getText();
        Sensor errSensor = null;
        if (errorName!=null && errorName.trim().length()>0) {
            errSensor =InstanceManager.sensorManagerInstance().provideSensor(errorName);
        }
        _block.setErrorSensor(errSensor);
        if (errSensor!=null && sensor==null) {
            int result = JOptionPane.showConfirmDialog(this, rbcp.getString("mixedSensors"), 
                            rbcp.getString("noSensor"), JOptionPane.YES_NO_OPTION, 
                            JOptionPane.QUESTION_MESSAGE);
            if (result==JOptionPane.YES_OPTION) {
                _block.setSensor(errSensor);
                _block.setErrorSensor(null);
                _detectorSensorName.setText(errSensor.getDisplayName());
            }
        }

        // check icons to be indicator type
        if (_parent.conversionNeeded(_parent.getSelectionGroup())) {
            int result = JOptionPane.showConfirmDialog(this, rbcp.getString("notIndicatorIcon"), 
                            rbcp.getString("incompleteCircuit"), JOptionPane.YES_NO_OPTION, 
                            JOptionPane.QUESTION_MESSAGE);
            if (result==JOptionPane.YES_OPTION) {
                setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
                close = false;
                _parent.convertIcons();
            }
        }
        // set the sensors
        java.util.List<Positionable> list = _parent.getSelectionGroup();
        if (list!=null) {
            NamedBeanHandle<OBlock> handle = new NamedBeanHandle<OBlock>(_block.getSystemName(), _block);
            for (int i=0; i<list.size(); i++) {
                Positionable pos = list.get(i);
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack)pos).setOccBlockHandle(handle);
                }
                /*
                if (pos instanceof IndicatorTurnoutIcon) {
                    IndicatorTurnoutIcon track = (IndicatorTurnoutIcon)pos;
                    track.setOccBlockHandle(handle);
                } else if (pos instanceof IndicatorTrackIcon) {
                    IndicatorTrackIcon track = (IndicatorTrackIcon)pos;
                    track.setOccBlockHandle(handle);
                } */
            }
        } else {
            _parent.addBlock(_block);
        }
        if (close) {
            _parent.closeCircuitFrame(_block);
            dispose();
        }
    }

    protected OBlock getBlock() {
        return _block;
    }

    protected void updateIconList(java.util.List<Positionable> icons) {
        //if (log.isDebugEnabled()) log.debug( 
        int segments = 0;
        int turnouts = 0;
        _needConversion =false;
        if (icons!=null) {
            System.out.println("updateIconList: icons.size()= "+icons.size());
            for (int i=0; i<icons.size(); i++) {
                Positionable pos = icons.get(i);
                if (pos instanceof IndicatorTurnoutIcon) {
                    turnouts++;
                } else if (pos instanceof IndicatorTrackIcon ) {
                    segments++;
                } else if (pos instanceof TurnoutIcon) {
                    turnouts++;
                    _needConversion = true;
                } else {
                    segments++;
                    _needConversion = true;
                }
            }
        }
        _numTrackSeg.setText(String.valueOf(segments));
        _numTurnouts.setText(String.valueOf(turnouts));
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditCircuitFrame.class.getName());
}

