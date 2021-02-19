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

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.IndicatorTrack;
import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.display.IndicatorTurnoutIcon;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.ToolTip;
import jmri.jmrit.display.TurnoutIcon;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.picker.PickListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pete Cressman Copyright: Copyright (c) 2011
 */
public class EditCircuitFrame extends EditFrame implements PropertyChangeListener {

    private JTextField _systemName;
    private JTextField _blockName;
    private JTextField _detectorSensorName;
    private JTextField _errorSensorName;
    private JTextField _blockState;
    private JTextField _numTrackSeg;
    private JTextField _numTurnouts;
    private LengthPanel _lengthPanel;
    private JPanel _namePanel;
    private boolean _create;

    // Sensor list
    OpenPickListButton<Sensor> _pickTable;

    public EditCircuitFrame(String title, CircuitBuilder parent, OBlock block) {
        super(title, parent, block);
        _create = (block == null);
        updateContentPanel(_create);
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

        _namePanel = new JPanel();
        _namePanel.setLayout(new BoxLayout(_namePanel, BoxLayout.Y_AXIS));
        contentPane.add(_namePanel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel _buttonPanel = new JPanel();
        _buttonPanel.setLayout(new FlowLayout());
        panel.add(_buttonPanel);
        contentPane.add(panel);
        
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
        _pickTable = new OpenPickListButton<>(blurbLines, PickListModel.sensorPickModelInstance(),
                this, Bundle.getMessage("OpenPicklist", Bundle.getMessage("BeanNameSensor")));
        contentPane.add(_pickTable.getButtonPanel());
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        _lengthPanel = new LengthPanel(_homeBlock, LengthPanel.BLOCK_LENGTH, "TooltipPathLength");
        _lengthPanel.changeUnits();
        _lengthPanel.setLength(_homeBlock.getLengthMm());
        contentPane.add(_lengthPanel);
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        contentPane.add(makeDoneButtonPanel());
        return contentPane;
    }
    private JPanel makeCreateBlockPanel() {
        _systemName = new JTextField();
        _systemName.setText(_homeBlock.getSystemName());
        _blockName.setText(_homeBlock.getUserName());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(CircuitBuilder.makeTextBoxPanel(
                false, _systemName, "ColumnSystemName", true, "TooltipBlockName"));
        _systemName.setPreferredSize(new Dimension(300, _systemName.getPreferredSize().height));
        panel.add(CircuitBuilder.makeTextBoxPanel(
                false, _blockName, "blockName", true, "TooltipBlockName"));
        _blockName.setPreferredSize(new Dimension(300, _blockName.getPreferredSize().height));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        JButton createButton = new JButton(Bundle.getMessage("buttonCreate"));
        createButton.addActionListener((ActionEvent a) -> createBlock());
        createButton.setToolTipText(Bundle.getMessage("createOBlock"));
        buttonPanel.add(createButton);
        
        panel.add(buttonPanel);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        return panel;
    }

    private JPanel makeEditBlockPanel() {
        _blockName.setText(_homeBlock.getUserName());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(CircuitBuilder.makeTextBoxPanel(
                false, _blockName, "blockName", true, "TooltipBlockName"));
        _blockName.setPreferredSize(new Dimension(300, _blockName.getPreferredSize().height));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton changeButton = new JButton(Bundle.getMessage("buttonChangeName"));
        changeButton.addActionListener((ActionEvent a) -> changeBlockName());
        changeButton.setToolTipText(Bundle.getMessage("ToolTipChangeName"));
        buttonPanel.add(changeButton);

        JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
        deleteButton.addActionListener((ActionEvent a) -> deleteCircuit());
        deleteButton.setToolTipText(Bundle.getMessage("ToolTipDeleteCircuit"));
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        return panel;
     }

    @Override
    protected JPanel makeDoneButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton convertButton = new JButton(Bundle.getMessage("ButtonConvertIcon"));
        convertButton.addActionListener((ActionEvent a) -> convertIcons());
        convertButton.setToolTipText(Bundle.getMessage("ToolTipConvertIcon"));
        panel.add(convertButton);

        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        doneButton.addActionListener((ActionEvent a) -> {
            if (_create) {
                closeCreate();
            } else {
                closingEvent(false);
            }
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

    private void createBlock() {
        String userName = _blockName.getText().trim();
        String systemName = _systemName.getText().trim();
        OBlockManager mgr = InstanceManager.getDefault(OBlockManager.class);
        StringBuilder  sb = new StringBuilder ();
        if (userName.length() > 0) {
             OBlock block = mgr.getByUserName(userName);
            if (block != null) {
                sb.append(Bundle.getMessage("duplicateName", userName, block.getSystemName()));
                sb.append("\n");
            }
        }
        if (!mgr.isValidSystemNameFormat(systemName)) {
            sb.append(Bundle.getMessage("sysnameOBlock"));
            sb.append("\n");
        } else {
            OBlock block = mgr.getBySystemName(systemName);
            if (block != null) {
                sb.append(Bundle.getMessage("duplicateName", systemName, block.getUserName()));
                sb.append("\n");
            }
        }
        if (sb.toString().length() > 0) {
            JOptionPane.showMessageDialog(this, sb.toString(),
                    Bundle.getMessage("editCiruit"), JOptionPane.INFORMATION_MESSAGE);
            _systemName.setText(_homeBlock.getSystemName());
            return;
        }
        _homeBlock = mgr.createNewOBlock(systemName, userName);
        updateContentPanel(false);
    }

    private void changeBlockName() {
        String name = _blockName.getText().trim();
        String msg = null;
        if (name.length() == 0) {
            msg = Bundle.getMessage("TooltipBlockName");
        } else {
            OBlock block = InstanceManager.getDefault(OBlockManager.class).getByUserName(name);
            if (block != null) {
                msg = Bundle.getMessage("duplicateName", name, block.getDisplayName(DisplayOptions.QUOTED_USERNAME_SYSTEMNAME));
            }
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
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
            closingEvent(true, null);   // No Messages, just close
        }
    }

    private void updateContentPanel(boolean create) {
        updateIconList(_parent._editor.getSelectionGroup());
        String name = "";
        Sensor sensor = _homeBlock.getSensor();
        if (sensor != null) {
            name = sensor.getDisplayName();
            _detectorSensorName.setText(name);
        }

        sensor = _homeBlock.getErrorSensor();
        if (sensor != null) {
            name = sensor.getDisplayName();
            _errorSensorName.setText(name);
        }

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

        JPanel panel;
        if (create) {
            panel = makeCreateBlockPanel();
        } else {
            panel = makeEditBlockPanel();
            _create = false;
        }
        _namePanel.removeAll();
        _namePanel.add(panel);
        _namePanel.invalidate();
        pack();
    }

    private void closeCreate() {
        StringBuilder  sb = new StringBuilder ();
        String sysName = _homeBlock.getSystemName();
        OBlock block = InstanceManager.getDefault(OBlockManager.class).getBySystemName(sysName);
        if (block == null) {
            // get rid of icon selections
            for (Positionable pos : _parent.getCircuitIcons(_homeBlock)) {
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack) pos).setOccBlockHandle(null);
                }
            }
            _parent._editor.getSelectionGroup().clear();
            sb.append( Bundle.getMessage("notCreated", _systemName.getText().trim()));
            closingEvent(false, sb.toString());
            if (_pickTable != null) {
                _pickTable.closePickList();
            }
        } else {
            closingEvent(false);
        }
    }

    @Override
    protected void closingEvent(boolean close) {
        StringBuffer sb = new StringBuffer();
        String msg = checkForSensors();
        if (msg != null) {
            sb.append(msg);
            sb.append("\n");
        }
        String name = _blockName.getText().trim();
        if (name.length() == 0) {
            msg = Bundle.getMessage("blankUserName");
            if (msg != null) {
                sb.append(msg);
                sb.append("\n");
            }
        } else if (!name.equals(_homeBlock.getUserName())) {
            msg = Bundle.getMessage("changeBlockName", name, _homeBlock.getDisplayName(DisplayOptions.QUOTED_USERNAME_SYSTEMNAME));
            if (msg != null) {
                sb.append(msg);
                sb.append("\n");
            }
        }
        _parent.setIconGroup(_homeBlock);
        msg = _parent.checkForTrackIcons(_homeBlock, "PortalOrPath");
        if (msg.length() > 0) {
            sb.append(msg);
            sb.append("\n");
        }
        if (_lengthPanel.getLength() <= 0.001) {
            msg = Bundle.getMessage("noBlockLength");
            if (msg != null) {
                sb.append(msg);
                sb.append("\n");
            }
        } else {
            _homeBlock.setLength(_lengthPanel.getLength());
        }

        closingEvent(close, sb.toString());
        if (_pickTable != null) {
            _pickTable.closePickList();
        }
    }
    
    private String checkForSensors() {
        String name = _detectorSensorName.getText();
        String errName = _errorSensorName.getText();
        if (!_homeBlock.setSensor(name)) {
           return java.text.MessageFormat.format(Bundle.getMessage("badSensorName"), name);
        }
        if (errName.length() > 2) {
            if (_homeBlock.getSensor() == null) {
                int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("mixedSensors"),
                        Bundle.getMessage("noSensor"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    if (!_homeBlock.setSensor(errName)) {
                        return java.text.MessageFormat.format(Bundle.getMessage("badSensorName"), errName);
                    } else {
                        _homeBlock.setErrorSensor(null);
                        _detectorSensorName.setText(_homeBlock.getSensor().getDisplayName());
                        _errorSensorName.setText(null);
                    }
                } else {
                    if (!_homeBlock.setErrorSensor(errName)) {
                        return java.text.MessageFormat.format(Bundle.getMessage("badSensorName"), errName);
                    }
                }
            } else {
                if (!_homeBlock.setErrorSensor(errName)) {
                    return java.text.MessageFormat.format(Bundle.getMessage("badSensorName"), errName);
                }
            }
        } else if (errName.trim().length() == 0){ {
            _homeBlock.setErrorSensor(null);
        }
            
        }
        Sensor sensor = _homeBlock.getSensor();
        if (sensor == null) {
            return Bundle.getMessage("noDetecterSensor");
        } else if (sensor.equals(_homeBlock.getErrorSensor())) {
            _homeBlock.setErrorSensor(null);
            _errorSensorName.setText(null);
            return java.text.MessageFormat.format(Bundle.getMessage("DuplSensorRemoved"),
                    sensor.getDisplayName(DisplayOptions.QUOTED_DISPLAYNAME));
        }
        return null;
    }

    protected void updateIconList(java.util.List<Positionable> icons) {
        //if (log.isDebugEnabled()) log.debug(
        int segments = 0;
        int turnouts = 0;
        if (icons != null) {
            if (log.isDebugEnabled()) {
                log.debug("updateIconList: icons.size()= {}", icons.size());
            }
            for (Positionable pos : icons) {
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

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("deleted")) {
            closingEvent(true, null);   // No Messages, just close
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EditCircuitFrame.class);
}
