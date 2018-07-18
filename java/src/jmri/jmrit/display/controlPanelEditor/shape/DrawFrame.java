package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;//
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import jmri.InstanceManager;
import jmri.SensorManager;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.swing.JmriBeanComboBox;
import jmri.util.swing.JmriColorChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame to create/edit a Control Panel shape PositionableShape object.
 *
 * @author Pete Cressman Copyright (c) 2012
 */
abstract public class DrawFrame extends jmri.util.JmriJFrame {

    private final Editor _editor;
    protected PositionableShape _shape;       // for use while editing
    private PositionableShape _originalShape; // saved for use if cancelled
    protected boolean _create;

    int _lineWidth;
    Color _lineColor;
    Color _fillColor;
    JColorChooser _chooser;
    JRadioButton _lineColorButon;
    JRadioButton _fillColorButon;
    JSlider _lineSlider;
    JSlider _alphaSlider;
    private transient JmriBeanComboBox _sensorBox = new JmriBeanComboBox(
        InstanceManager.getDefault(SensorManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    JRadioButton _hideShape;
    JRadioButton _changeLevel;
    JComboBox<String> _levelComboBox;
    JPanel _contentPanel;

    public DrawFrame(String which, String title, PositionableShape ps, Editor ed, boolean create) {
        super(title, false, false);
        _shape = ps;
        _editor = ed;
        _create = create;
        super.setTitle(Bundle.getMessage(which, Bundle.getMessage(title)));

        _lineWidth = 1;
        _lineColor = Color.black;

        _contentPanel = new JPanel();
        _contentPanel.setLayout(new java.awt.BorderLayout(10, 10));
        _contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));

        if (_shape == null) {
            _contentPanel.add(makeCreatePanel(title));
        } else {
            // closingEvent will re-establish listener
            _shape.removeListener();            
            _contentPanel.add(makeEditPanel());
        }

        JScrollPane scrollPane = new JScrollPane(_contentPanel);
        super.setContentPane(scrollPane);

        super.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closingEvent(true);
            }
        });
        super.pack();
        Point edLoc = _editor.getLocationOnScreen();
        Point loc;
        if (_shape == null) {
            loc = new Point(edLoc.x + 200, edLoc.y);                                
        } else {
            Dimension screen = getToolkit().getScreenSize();
            Dimension edDim = _editor.getSize();
            Dimension frDim = getPreferredSize();
            Point shapeLoc = _shape.getLocationOnScreen();
            // try alongside entire frame
            loc = _shape.getLocation();
            int xr = edLoc.x + edDim.width - 20;
            int xl = edLoc.x - frDim.width + 20;
            if (xr + frDim.width <= screen.width) {
                loc = new Point(xr, shapeLoc.y);                                
            } else if (xl >= 0) {    
                loc = new Point(xl, shapeLoc.y);                                
            } else {
                // try below/above frame
                int yb = edLoc.y + edDim.height - 20;
                int ya = edLoc.y - frDim.height; 
                if (yb + frDim.height -20 < screen.height) {
                    loc = new Point(shapeLoc.x, yb);
                } else if (yb + frDim.height -20 < screen.height) {
                        loc = new Point(shapeLoc.x, ya);                                
                } else {
                    // try along side of shape 
                    xr = shapeLoc.x + _shape.getWidth() + 20;
                    xl = shapeLoc.x - frDim.width - 20;
                    if ((xr + frDim.width <= screen.width)) {
                        loc = new Point(xr, edLoc.y);                                
                    } else if (xl >= 0) {    
                        loc = new Point(xl, edLoc.y);                                
                    } else {
                        yb = shapeLoc.y + _shape.getHeight() + 20;
                        ya = shapeLoc.y - frDim.height;
                        if (yb + frDim.height <= screen.height) {
                            loc = new Point(shapeLoc.x, yb);
                        } else if (ya >= 0) {
                            loc = new Point(shapeLoc.x, ya);
                        } else {
                            loc = new Point(screen.width - frDim.width, screen.height - frDim.height);
                        }
                    }
                }
            }
        }
        setLocation(loc);
        super.setVisible(true);
        setAlwaysOnTop(true);
    }

    private void addLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(label);
    }
    private final JPanel makeCreatePanel(String type) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        java.awt.Dimension dim = new java.awt.Dimension(250, 8);
        panel.add(Box.createRigidArea(dim));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
        p.add(Box.createHorizontalStrut(10));
        JPanel pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.PAGE_AXIS));
        if (type != null && type.equals("Polygon")) {
            addLabel(pp, Bundle.getMessage("drawInstructions2a"));
            addLabel(pp, Bundle.getMessage("drawInstructions2b"));
        } else {
            addLabel(pp, Bundle.getMessage("drawInstructions2", type));
       }
        p.add(pp);
        p.add(Box.createHorizontalStrut(10));
        panel.add(p);
        panel.add(Box.createRigidArea(dim));
        setVisible(false);
        setUndecorated(true);
        setBackground(new Color(0.8f, 0.8f, 0.8f, 1.0f));
        return panel;
    }

    private final JPanel makeEditPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel(Bundle.getMessage("lineWidth")));
        JPanel pp = new JPanel();
        pp.add(new JLabel(Bundle.getMessage("thin")));
        _lineSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 30, _lineWidth);
        _lineSlider.addChangeListener((ChangeEvent e) -> {
            widthChange();
        });
        pp.add(_lineSlider);
        pp.add(new JLabel(Bundle.getMessage("thick")));
        p.add(pp);
        panel.add(p);
        p = new JPanel();
        ButtonGroup bg = new ButtonGroup();
        _lineColorButon = new JRadioButton(Bundle.getMessage("lineColor"));
        p.add(_lineColorButon);
        bg.add(_lineColorButon);
        _fillColorButon = new JRadioButton(Bundle.getMessage("fillColor"));
        p.add(_fillColorButon);
        bg.add(_fillColorButon);
        _lineColorButon.setSelected(true);
        panel.add(p);
        _chooser = new JColorChooser(Color.LIGHT_GRAY);
        _chooser.getSelectionModel().addChangeListener((ChangeEvent e) -> {
            colorChange();
        });
        _chooser.setPreviewPanel(new JPanel());
        _chooser = JmriColorChooser.extendColorChooser(_chooser);
        panel.add(_chooser);
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel(Bundle.getMessage("transparency")));
        pp = new JPanel();
        pp.add(new JLabel(Bundle.getMessage("transparent")));
        _alphaSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 255, _lineColor.getAlpha());
        _alphaSlider.addChangeListener((ChangeEvent e) -> {
            alphaChange();
        });
        pp.add(_alphaSlider);
        _lineColorButon.addChangeListener((ChangeEvent e) -> {
            buttonChange();
        });
        pp.add(new JLabel(Bundle.getMessage("opaque")));
        p.add(pp);
        panel.add(p);
        return panel;
    }

    protected final JPanel makeSensorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage("SensorMsg")));
        panel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("VisibleSensor"))));
        _sensorBox.setFirstItemBlank(true); // already filled with names of all existing sensors
        _sensorBox.addActionListener((ActionEvent e) -> {
            String msg = _shape.setControlSensor(_sensorBox.getDisplayName());
            log.debug("Setting sensor to {} after action", _sensorBox.getDisplayName());
            if (msg != null) {
                JOptionPane.showMessageDialog(null, msg, Bundle.getMessage("ErrorSensor"),
                        JOptionPane.INFORMATION_MESSAGE); // NOI18N
                _sensorBox.setText("");
            }
            updateShape();
        });
        JPanel p = new JPanel();
        p.add(_sensorBox);
        p.add(Box.createVerticalGlue());
        panel.add(p);
        panel.add(Box.createVerticalGlue());

        _hideShape = new JRadioButton(Bundle.getMessage("HideOnSensor"));
        _changeLevel = new JRadioButton(Bundle.getMessage("ChangeLevel"));
        ButtonGroup bg = new ButtonGroup();
        bg.add(_hideShape);
        bg.add(_changeLevel);
        _levelComboBox = new JComboBox<>();
        _levelComboBox.addItem(Bundle.getMessage("SameLevel"));
        for (int i = 1; i < 11; i++) {
            _levelComboBox.addItem(Bundle.getMessage("Level") + " " + i);
        }
        _levelComboBox.addActionListener((ActionEvent evt) -> {
            int level = _levelComboBox.getSelectedIndex();
            _shape.setChangeLevel(level);
        });
        _hideShape.addActionListener((ActionEvent a) -> {
            _shape.setHide(true);
            _levelComboBox.setEnabled(false);
        });
        _changeLevel.addActionListener((ActionEvent a) -> {
            _shape.setHide(false);
            _levelComboBox.setEnabled(true);
        });
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        p1.add(_hideShape);
        p1.add(_changeLevel);

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p = new JPanel();
        p.add(_levelComboBox);
        p2.add(p);
        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
        p3.add(p1);
        p3.add(Box.createHorizontalGlue());
        p3.add(p2);

        panel.add(Box.createVerticalGlue());
        panel.add(p3);
        panel.add(Box.createVerticalGlue());
        
        JPanel pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.X_AXIS));
        pp.add(Box.createHorizontalStrut(80));
        pp.add(Box.createHorizontalGlue());
        pp.add(panel, java.awt.BorderLayout.CENTER);
        pp.add(Box.createHorizontalGlue());
        return pp;
    }

    /**
     * Create a panel for setting parameters for the PositionableShape.
     *
     * @return a parameters panel
     */
    protected JPanel makeParamsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    /**
     * Set parameters on the popup that will edit the PositionableShape.
     * Called both for creation and editing by the PositionableShape
     * @param ps a PositionableShape
     */
    protected void setDisplayParams(PositionableShape ps) {
        if (!_create) {
            makeCopy(ps);
        }
        ShapeDrawer sd = ((ControlPanelEditor)ps.getEditor()).getShapeDrawer();
        if (!sd.setDrawFrame(this)) {
            closingEvent(true);
            return;
        }
        _contentPanel.removeAll();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(makeEditPanel());
        panel.add(makeParamsPanel());
        javax.swing.JTabbedPane tPanel = new javax.swing.JTabbedPane();
        tPanel.addTab(Bundle.getMessage("attributeTab"), null,
                panel, Bundle.getMessage("drawInstructions1"));
        
        _lineWidth = _shape.getLineWidth();
        _lineSlider.setValue(_lineWidth);
        _lineColor = _shape.getLineColor();
        _fillColor = _shape.getFillColor();
        if (_lineColor.getAlpha() >= _fillColor.getAlpha()) {
            _alphaSlider.setValue(_lineColor.getAlpha());
            _lineColorButon.setSelected(true);
        } else {
            int alpha = _fillColor.getAlpha();
            if (alpha < 2) {
                alpha = 255;
            }
            _alphaSlider.setValue(alpha);
            _fillColorButon.setSelected(true);
        }

        tPanel.addTab(Bundle.getMessage("advancedTab"), null,
                makeSensorPanel(), Bundle.getMessage("drawInstructions3a"));
        _sensorBox.setText(_shape.getSensorName());
        _levelComboBox.setSelectedIndex(_shape.getChangeLevel());
        if (_shape.isHideOnSensor()) {
            _hideShape.setSelected(true);
            _levelComboBox.setEnabled(false);
        } else {
            _changeLevel.setSelected(true);
        }
        _contentPanel.add(tPanel);
        _contentPanel.add(makeDoneButtonPanel());
        pack();
    }

    /**
     * Editing an existing shape (only make copy for cancel of edits).
     *
     * @param ps shape
     */
    private void makeCopy(PositionableShape ps) {
        // make a copy, but keep it out of editor's content
        _originalShape = (PositionableShape) ps.deepClone();
        // cloning adds to editor's targetPane - (maybe fix needed in editor)
        _originalShape.remove();
        // closingEvent will re-establish listener
        _originalShape.removeListener();
        log.debug("_originalShape made");
    }

    private JPanel makeDoneButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
//        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        panel.add(Box.createHorizontalGlue());
        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        doneButton.addActionListener((ActionEvent a) -> {
            closingEvent(false);
        });
        JPanel p =new JPanel();
        p.add(doneButton);
        panel.add(p);
        panel.add(Box.createHorizontalGlue());

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener((ActionEvent a) -> {
            closingEvent(true);
        });
        p =new JPanel();
        p.add(cancelButton);
        panel.add(p);
//        p1.add(Box.createHorizontalGlue());
//        p1.add(panel);
//        p1.add(Box.createHorizontalGlue());
        panel.add(Box.createHorizontalGlue());
        return panel;
    }

    private void buttonChange() {
        if (_lineColorButon.isSelected()) {
            JmriColorChooser.addRecentColor(_fillColor);
            _chooser.getSelectionModel().setSelectedColor(_lineColor);
            _chooser.setColor(_lineColor);
//            _alphaSlider.setValue(_lineColor.getAlpha());
        } else if (_fillColor != null) {
            JmriColorChooser.addRecentColor(_lineColor);
            _chooser.setColor(_fillColor);
//            _chooser.getSelectionModel().setSelectedColor(_fillColor);
            _alphaSlider.setValue(_fillColor.getAlpha());
        } else {
            _alphaSlider.setValue(255);
        }
        _alphaSlider.revalidate();
        _alphaSlider.repaint();
    }

    private void widthChange() {
        _lineWidth = _lineSlider.getValue();
        if (_shape == null) {
            return;
        }
        _shape.setLineWidth(_lineWidth);
        updateShape();
    }

    private void colorChange() {
        Color c = _chooser.getColor();
        int alpha = _alphaSlider.getValue();
        if (_lineColorButon.isSelected()) {
            _lineColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), _lineColor.getAlpha());
            if (_shape != null) {
                _shape.setLineColor(_lineColor);
            }
        } else {
            _fillColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
            if (_shape != null) {
                _shape.setFillColor(_fillColor);                
            }
        }
        updateShape();
    }

    private void alphaChange() {
        int alpha = _alphaSlider.getValue();
        if (_lineColorButon.isSelected()) {
            _lineColor = new Color(_lineColor.getRed(), _lineColor.getGreen(), _lineColor.getBlue(), alpha);
            if (_shape != null) {
                _shape.setLineColor(_lineColor);
            }
        } else if (_fillColorButon.isSelected() && _fillColor != null) {
            _fillColor = new Color(_fillColor.getRed(), _fillColor.getGreen(), _fillColor.getBlue(), alpha);
            if (_shape != null) {
                _shape.setFillColor(_fillColor);
            }
        }
        updateShape();
    }

    protected void closingEvent(boolean cancel) {
        log.debug("closingEvent for {}", getTitle());
        if (_shape != null) {
            if (cancel) {
                _shape.remove();
                if (_originalShape != null) {
                    _originalShape.getEditor().putItem(_originalShape);
                    _originalShape.setListener();
                }
            } else {
                _shape.setListener();               
            }
            _shape.removeHandles();
            if(_shape instanceof PositionablePolygon) {
                ((PositionablePolygon)_shape).editing(false);
            }
        }
        ((ControlPanelEditor)_editor).getShapeDrawer().setDrawFrame(null);
        _create = false;
        _shape = null;  // tells ShapeDrawer creation and editing is finished. 
        dispose();
    }

    protected int getInteger(JTextField field, int value) {
        try {
            int i = Integer.parseInt(field.getText());
            if (i > 0) {
                if (i < PositionableShape.SIZE) {
                    i = PositionableShape.SIZE;
                }
                return i;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, nfe,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        }
        field.setText(Integer.toString(value));
        return value;
    }

    protected void updateShape() {
        if (_shape == null) {
            return;
        }
        _shape.removeHandles();
        _shape.drawHandles();
        _shape.updateSize();
        _shape.getEditor().getTargetPanel().repaint();
    }

    // these 2 methods update the JTextfields when mouse moves handles
    abstract void setDisplayWidth(int w);
    abstract void setDisplayHeight(int h);

    abstract protected PositionableShape makeFigure(Rectangle r, Editor ed); 

    private final static Logger log = LoggerFactory.getLogger(DrawFrame.class);
}
