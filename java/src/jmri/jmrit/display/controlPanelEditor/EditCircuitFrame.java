package jmri.jmrit.display.controlPanelEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Sensor;

import jmri.jmrit.display.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jmri.jmrit.logix.*;
import jmri.jmrit.display.palette.ItemPalette;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.picker.PickPanel;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2011
 * 
 */

public class EditCircuitFrame extends jmri.util.JmriJFrame {

    private OBlock _block;
    private CircuitBuilder _parent;

    private JTextField  _blockName = new JTextField();
    private JTextField  _detectorSensorName = new JTextField();
    private JTextField  _errorSensorName = new JTextField();
    private JTextField  _blockState  = new JTextField();
    private JTextField  _numTrackSeg = new JTextField();
    private JTextField  _numTurnouts = new JTextField();

    // Sensor list  
    private JFrame      _pickFrame;
    private JButton     _openPicklistButton;

    static int STRUT_SIZE = 10;
    static boolean _firstInstance = true;
    static Point _loc = null;
    static Dimension _dim = null;

    public EditCircuitFrame(String title, CircuitBuilder parent, OBlock block) {
        _block = block;
        setTitle(java.text.MessageFormat.format(title, _block.getDisplayName()));
        addHelpMenu("package.jmri.jmrit.display.CircuitBuilder", true);
        _parent = parent;
        makeContentPanel();
        updateContentPanel();
    }

    private void makeContentPanel() {
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                closingEvent();
            }
        });
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel p = new JPanel();
        p.add(new JLabel(Bundle.getMessage("AddRemoveIcons")));
        contentPane.add(p);
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(CircuitBuilder.makeTextBoxPanel(false, _blockState, "blockState", false, null));
        _blockState.setPreferredSize(new Dimension(150, _blockState.getPreferredSize().height));
        contentPane.add(panel);

        panel = new JPanel();
        _blockName.setText(_block.getDisplayName());
        panel.add(CircuitBuilder.makeTextBoxPanel(
                    false, _blockName, "blockName", true, "TooltipBlockName"));
        _blockName.setPreferredSize(new Dimension(300, _blockName.getPreferredSize().height));
        contentPane.add(panel);

        contentPane.add(MakeButtonPanel());
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        p = new JPanel();
        p.add(new JLabel(Bundle.getMessage("numTrackElements")));
        contentPane.add(p);

        panel = new JPanel();
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
        _detectorSensorName.setToolTipText(Bundle.getMessage("detectorSensorName"));
        contentPane.add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        _errorSensorName.setPreferredSize(new Dimension(300, _errorSensorName.getPreferredSize().height));
        panel.add(CircuitBuilder.makeTextBoxPanel(
                    false, _errorSensorName, "ErrorSensor", true, "detectorErrorName"));
        _errorSensorName.setToolTipText(Bundle.getMessage("detectorErrorName"));
        contentPane.add(panel);
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        contentPane.add(MakePickListPanel());
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        contentPane.add(MakeDoneButtonPanel());
        JPanel border = new JPanel();
        border.setLayout(new java.awt.BorderLayout(20,20));
        border.add(contentPane);
        setContentPane(border);
        pack();
        if (_firstInstance) {
            setLocationRelativeTo(_parent._editor);
            _firstInstance = false;
        } else {
            setLocation(_loc);
            setSize(_dim);
        }
        setVisible(true);
    }

    private JPanel MakePickListPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        _openPicklistButton = new JButton(Bundle.getMessage("OpenSensorPicklist"));
        _openPicklistButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (_pickFrame==null) {
                        openPickList();
                    } else {
                        closePickList();
                    }
                }
        });
        _openPicklistButton.setToolTipText(Bundle.getMessage("ToolTipPickLists"));
        panel.add(_openPicklistButton);
        panel.setToolTipText(Bundle.getMessage("ToolTipPickLists"));

        buttonPanel.add(panel);
        return buttonPanel;
    }

    void openPickList() {
        _pickFrame = new JFrame();
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("DragOccupancyName")));
        blurb.add(new JLabel(Bundle.getMessage("DragErrorName")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        content.add(panel);
        PickListModel[] models = {PickListModel.sensorPickModelInstance()};
        content.add(new PickPanel(models));

        _pickFrame.setContentPane(content);
        _pickFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    closePickList();                   
                }
            });
        _pickFrame.setLocationRelativeTo(this);
        _pickFrame.toFront();
        _pickFrame.setVisible(true);
        _pickFrame.pack();
        _openPicklistButton.setText(Bundle.getMessage("ClosePicklist"));
    }

    void closePickList() {
        if (_pickFrame!=null) {
            _pickFrame.dispose();
            _pickFrame = null;
            _openPicklistButton.setText(Bundle.getMessage("OpenSensorPicklist"));
        }
    }

    private JPanel MakeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton changeButton = new JButton(Bundle.getMessage("buttonChangeName"));
        changeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    changeBlockName();
                }
        });
        changeButton.setToolTipText(Bundle.getMessage("ToolTipChangeName"));
        panel.add(changeButton);
 

        JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
        deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    deleteCircuit();
                 }
        });
        deleteButton.setToolTipText(Bundle.getMessage("ToolTipDeleteCircuit"));
        panel.add(deleteButton);
        buttonPanel.add(panel);

        return buttonPanel;
    }

    private JPanel MakeDoneButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton convertButton = new JButton(Bundle.getMessage("ButtonConvertIcon"));
        convertButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	convertIcons();
                }
        });
        convertButton.setToolTipText(Bundle.getMessage("ToolTipConvertIcon"));
        panel.add(convertButton);

        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    closingEvent();
                }
        });
        panel.add(doneButton);
        buttonPanel.add(panel);

        return buttonPanel;
    }
    
    private void convertIcons() {
        _parent.convertIcons(_parent._editor.getSelectionGroup());
        this.toFront();
    }

    /************************* end setup **************************/

    private void changeBlockName() {
        String name = _blockName.getText();
        if (name==null || name.trim().length()==0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("changeBlockName"), 
                            Bundle.getMessage("editCiruit"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        _block.setUserName(name);
        // block user name change will change portal names.  Change PortalIcon names to match
        java.util.List<Positionable> list = _parent.getCircuitIcons(_block);
        if (list!=null) {
            for (int i=0; i<list.size(); i++) {
                if (list.get(i) instanceof PortalIcon) {
                    PortalIcon icon = (PortalIcon)list.get(i);
                    Portal portal = icon.getPortal();
                    icon.setName(portal.getName());
                    icon.setTooltip(new ToolTip(portal.getDescription(), 0, 0));
                }
            }
        }
    }

    private void deleteCircuit() {
        int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("confirmBlockDelete"), 
                        Bundle.getMessage("editCiruit"), JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE);
        if (result==JOptionPane.YES_OPTION) {
            _parent.removeBlock(_block);
            _parent.closeCircuitFrame();
            dispose();
        }
    }

    protected void updateContentPanel() {
        updateIconList(_parent._editor.getSelectionGroup());
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

        int state = _block.getState();
        StringBuffer stateText = new StringBuffer();
        if ((state & OBlock.UNKNOWN) > 0) {
            stateText.append("Unknown ");
        }
        if ((state & OBlock.OCCUPIED) > 0) {
            stateText.append("Occupied ");
        }
        if ((state & OBlock.UNOCCUPIED) > 0) {
            stateText.append("Unoccupied ");
        }
        if ((state & OBlock.INCONSISTENT) > 0) {
            stateText.append("Inconsistent ");
        }
        if ((state & OBlock.ALLOCATED) > 0) {
            stateText.append("Allocated ");
        }
        if ((state & OBlock.RUNNING) > 0) {
            stateText.append("Positioned ");
        }
        if ((state & OBlock.OUT_OF_SERVICE) > 0) {
            stateText.append("OutOf Service ");
        }
        if ((state & OBlock.DARK) > 0) {
            stateText.append("Dark ");
        }
        if ((state & OBlock.TRACK_ERROR) > 0) {
            stateText.append("TrackError ");
        }
        if (state==0) {
            stateText.append("Not Initialized");
        }
        if (log.isDebugEnabled()) log.debug("updateContentPanel: state= "+stateText.toString()); 
        _blockState.setText(stateText.toString());
    }

    protected void closingEvent() {
        // check Sensors
        String name = _detectorSensorName.getText();
        if (name==null ||name.trim().length()==0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("noDetecterSensor"), 
                            Bundle.getMessage("noSensor"), JOptionPane.INFORMATION_MESSAGE);
        }
        if (!_block.setSensor(name)) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(Bundle.getMessage("badSensorName"),name),
            		Bundle.getMessage("noSensor"), JOptionPane.INFORMATION_MESSAGE);        		
        }
        name = _errorSensorName.getText();
        if (name!=null && name.trim().length()>0) {
            if (_block.getSensor()==null) {
                int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("mixedSensors"), 
                                Bundle.getMessage("noSensor"), JOptionPane.YES_NO_OPTION, 
                                JOptionPane.QUESTION_MESSAGE);
                if (result==JOptionPane.YES_OPTION) {
                	if (!_block.setSensor(name)) {
                        JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(Bundle.getMessage("badSensorName"),name),
                        		Bundle.getMessage("noSensor"), JOptionPane.INFORMATION_MESSAGE);        		                		
                	} else {
                        _block.setErrorSensor(null);
                        _detectorSensorName.setText(_block.getSensor().getDisplayName());                		
                	}
                }
            } else {
            	if (!_block.setErrorSensor(name)) {
                    JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(Bundle.getMessage("badSensorName"),name),
                    		Bundle.getMessage("noSensor"), JOptionPane.INFORMATION_MESSAGE);
            	}
            }
        } else {
        	_block.setErrorSensor(null);
        }
        closePickList();
        
        _parent.checkCircuitFrame(_block);
        _loc = getLocation(_loc);
        _dim = getSize(_dim);
        dispose();
    }

    protected OBlock getBlock() {
        return _block;
    }

    protected void updateIconList(java.util.List<Positionable> icons) {
        //if (log.isDebugEnabled()) log.debug( 
        int segments = 0;
        int turnouts = 0;
        if (icons!=null) {
            if (log.isDebugEnabled()) log.debug("updateIconList: icons.size()= "+icons.size());
            for (int i=0; i<icons.size(); i++) {
                Positionable pos = icons.get(i);
                if (pos instanceof IndicatorTurnoutIcon) {
                    turnouts++;
                } else if (pos instanceof IndicatorTrackIcon ) {
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

    static Logger log = LoggerFactory.getLogger(EditCircuitFrame.class.getName());
}

