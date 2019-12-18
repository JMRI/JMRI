package jmri.jmrit.display.controlPanelEditor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.Sensor;
import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.display.IndicatorTurnoutIcon;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.ToolTip;
import jmri.jmrit.display.TurnoutIcon;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pete Cressman Copyright: Copyright (c) 2011
 */
public class EditCircuitFrame extends EditFrame implements PropertyChangeListener {

    private JTextField _blockName;
    private JTextField _detectorSensorName;
    private JTextField _errorSensorName;
    private JTextField _blockState;
    private JTextField _numTrackSeg;
    private JTextField _numTurnouts;
    private LengthPanel _lengthPanel;

    // Sensor list
    OpenPickListButton<Sensor> _pickTable;

    public EditCircuitFrame(String title, CircuitBuilder parent, OBlock block) {
        super(title, parent, block);
        updateContentPanel();
        _homeBlock.addPropertyChangeListener("deleted", this);
        pack();
    }

    @Override
    protected JPanel makeContentPanel() {
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        _blockName = new JTextField();
        _detectorSensorName = new JTextField();
        _errorSensorName = new JTextField();
        _blockState = new JTextField();
        _numTrackSeg = new JTextField();
        _numTurnouts = new JTextField();
        JPanel p = new JPanel();
        
        p.add(new JLabel(Bundle.getMessage("AddRemoveIcons")));
        contentPane.add(p);
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        // panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(CircuitBuilder.makeTextBoxPanel(false, _blockState, "blockState", false, null));
        _blockState.setPreferredSize(new Dimension(150, _blockState.getPreferredSize().height));
        contentPane.add(panel);

        panel = new JPanel();
        _blockName.setText(_homeBlock.getDisplayName());
        panel.add(CircuitBuilder.makeTextBoxPanel(
                false, _blockName, "blockName", true, "TooltipBlockName"));
        _blockName.setPreferredSize(new Dimension(300, _blockName.getPreferredSize().height));
        contentPane.add(panel);

        contentPane.add(makeButtonPanel());
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        p = new JPanel();
        p.add(new JLabel(Bundle.getMessage("numTrackElements")));
        contentPane.add(p);

        panel = new JPanel();
        // panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
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
        // panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        _detectorSensorName.setPreferredSize(new Dimension(300, _detectorSensorName.getPreferredSize().height));
        panel.add(CircuitBuilder.makeTextBoxPanel(
                false, _detectorSensorName, "DetectionSensor", true, "detectorSensorName"));
        _detectorSensorName.setToolTipText(Bundle.getMessage("detectorSensorName"));
        contentPane.add(panel);

        panel = new JPanel();
        // panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        _errorSensorName.setPreferredSize(new Dimension(300, _errorSensorName.getPreferredSize().height));
        panel.add(CircuitBuilder.makeTextBoxPanel(
                false, _errorSensorName, "ErrorSensor", true, "detectorErrorName"));
        _errorSensorName.setToolTipText(Bundle.getMessage("detectorErrorName"));
        contentPane.add(panel);

        String[] blurbLines = { Bundle.getMessage("DragOccupancySensor", Bundle.getMessage("DetectionSensor")),
                                Bundle.getMessage("DragErrorName", Bundle.getMessage("ErrorSensor"))};
        _pickTable = new OpenPickListButton<Sensor>(blurbLines, PickListModel.sensorPickModelInstance(), this);
        contentPane.add(_pickTable.getButtonPanel());
//        contentPane.add(makePickListPanel());
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        _lengthPanel = new LengthPanel(_homeBlock, "blockLength");
        _lengthPanel.changeUnits();
        _lengthPanel.setLength(_homeBlock.getLengthMm());
        contentPane.add(_lengthPanel);
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        contentPane.add(makeDoneButtonPanel());
        return contentPane;
    }
    private JPanel makeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton changeButton = new JButton(Bundle.getMessage("buttonChangeName"));
        changeButton.addActionListener((ActionEvent a) -> {
            changeBlockName();
        });
        changeButton.setToolTipText(Bundle.getMessage("ToolTipChangeName"));
        panel.add(changeButton);

        JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
        deleteButton.addActionListener((ActionEvent a) -> {
            deleteCircuit();
        });
        deleteButton.setToolTipText(Bundle.getMessage("ToolTipDeleteCircuit"));
        panel.add(deleteButton);
        buttonPanel.add(panel);

        return buttonPanel;
    }

    @Override
    protected JPanel makeDoneButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton convertButton = new JButton(Bundle.getMessage("ButtonConvertIcon"));
        convertButton.addActionListener((ActionEvent a) -> {
            convertIcons();
        });
        convertButton.setToolTipText(Bundle.getMessage("ToolTipConvertIcon"));
        panel.add(convertButton);

        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        doneButton.addActionListener((ActionEvent a) -> {
            closingEvent(false);
        });
        panel.add(doneButton);
        buttonPanel.add(panel);

        return buttonPanel;
    }

    private void convertIcons() {
        _parent.setIconGroup(_homeBlock);
        _parent.queryConvertTrackIcons(_homeBlock, "PortalOrPath");
        this.toFront();
    }

    /*
     * *********************** end setup *************************
     */

    private void changeBlockName() {
        String name = _blockName.getText();
        if (name == null || name.trim().length() == 0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("changeBlockName"),
                    Bundle.getMessage("editCiruit"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        _homeBlock.setUserName(name);
        // block user name change will change portal names.  Change PortalIcon names to match
        for (Positionable p : _parent.getCircuitIcons(_homeBlock)) {
            if (p instanceof PortalIcon) {
                PortalIcon icon = (PortalIcon)p;
                Portal portal = icon.getPortal();
                icon.setName(portal.getName());
                icon.setToolTip(new ToolTip(portal.getDescription(), 0, 0));
            }
        }
    }

    private void deleteCircuit() {
        int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("confirmBlockDelete"),
                Bundle.getMessage("editCiruit"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            _parent.removeBlock(_homeBlock);
            closingEvent(true);
        }
    }

    private void updateContentPanel() {
        updateIconList(_parent._editor.getSelectionGroup());
        String name = "";
        Sensor sensor = _homeBlock.getSensor();
        if (sensor != null) {
            name = sensor.getDisplayName();
        }
        _detectorSensorName.setText(name);

        sensor = _homeBlock.getErrorSensor();
        if (sensor != null) {
            name = sensor.getDisplayName();
        } else {
            name = "";
        }
        _errorSensorName.setText(name);

        int state = _homeBlock.getState();
        StringBuilder stateText = new StringBuilder();
        if ((state & OBlock.UNKNOWN) != 0) {
            stateText.append("Unknown ");
        }
        if ((state & OBlock.OCCUPIED) != 0) {
            stateText.append("Occupied ");
        }
        if ((state & OBlock.UNOCCUPIED) != 0) {
            stateText.append("Unoccupied ");
        }
        if ((state & OBlock.INCONSISTENT) != 0) {
            stateText.append("Inconsistent ");
        }
        if ((state & OBlock.ALLOCATED) != 0) {
            stateText.append("Allocated ");
        }
        if ((state & OBlock.RUNNING) != 0) {
            stateText.append("Positioned ");
        }
        if ((state & OBlock.OUT_OF_SERVICE) != 0) {
            stateText.append("OutOf Service ");
        }
        if ((state & OBlock.UNDETECTED) != 0) {
            stateText.append("Dark ");
        }
        if ((state & OBlock.TRACK_ERROR) != 0) {
            stateText.append("TrackError ");
        }
        if (state == 0) {
            stateText.append("Not Initialized");
        }
        if (log.isDebugEnabled()) {
            log.debug("updateContentPanel: state= {}", stateText);
        }
        _blockState.setText(stateText.toString());
    }

    @Override
    protected void closingEvent(boolean close) {
        _parent.setIconGroup(_homeBlock);
        String msg = _parent.checkForTrackIcons(_homeBlock, "PortalOrPath");
        if(msg == null) {
            _homeBlock.setLength(_lengthPanel.getLength());
        }
        // check Sensors
        if (msg == null) {
            msg = checkForSensors();
        }
        closingEvent(close, msg);
        if (_pickTable != null) {
            _pickTable.closePickList();
        }
    }
    
    private String checkForSensors() {
        String name = _detectorSensorName.getText();
        String errName = _errorSensorName.getText();
        String msg = null;
        if (!_homeBlock.setSensor(name)) {
            msg = java.text.MessageFormat.format(Bundle.getMessage("badSensorName"), name);
        }
        if (msg == null) {
            if (errName.length() > 0) {
                if (_homeBlock.getSensor() == null) {
                    int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("mixedSensors"),
                            Bundle.getMessage("noSensor"), JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        if (!_homeBlock.setSensor(errName)) {
                            msg = java.text.MessageFormat.format(Bundle.getMessage("badSensorName"), errName);
                        } else {
                            _homeBlock.setErrorSensor(null);
                            _detectorSensorName.setText(_homeBlock.getSensor().getDisplayName());
                        }
                    } else {
                        if (!_homeBlock.setErrorSensor(errName)) {
                            msg = java.text.MessageFormat.format(Bundle.getMessage("badSensorName"), errName);
                        }
                    }
                } else {
                    if (!_homeBlock.setErrorSensor(errName)) {
                        msg = java.text.MessageFormat.format(Bundle.getMessage("badSensorName"), errName);
                    }
                }
            }
        }
        if (msg == null && _homeBlock.getSensor() == null) {
            msg = Bundle.getMessage("noDetecterSensor");
        }
        return msg;
    }

    protected void updateIconList(java.util.List<Positionable> icons) {
        //if (log.isDebugEnabled()) log.debug(
        int segments = 0;
        int turnouts = 0;
        if (icons != null) {
            if (log.isDebugEnabled()) {
                log.debug("updateIconList: icons.size()= {}", icons.size());
            }
            for (int i = 0; i < icons.size(); i++) {
                Positionable pos = icons.get(i);
                if (pos instanceof IndicatorTurnoutIcon) {
                    turnouts++;
                } else if (pos instanceof IndicatorTrackIcon) {
                    segments++;
                } else if (pos instanceof TurnoutIcon) {
                    turnouts++;
                } else {
                    segments++;
                }
            }
        }
        _numTrackSeg.setText(String.valueOf(segments));
        _numTurnouts.setText(String.valueOf(turnouts));
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("deleted")) {
            closingEvent(true);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EditCircuitFrame.class);
}
