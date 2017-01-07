package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.jmrit.display.Editor.TargetPane;

//import javax.swing.JRadioButton;
/**
 * <P>
 * @author Pete Cressman Copyright: Copyright (c) 2012
 *
 */
public abstract class DrawFrame extends jmri.util.JmriJFrame {

    protected ShapeDrawer _parent;
    protected PositionableShape _shape;       // for use while editing
    private PositionableShape _originalShape;       // saved for use if cancelled

    static int STRUT_SIZE = 10;
    static Point _loc = new Point(100, 100);
    static Dimension _dim = new Dimension(500, 500);

    int _lineWidth;
    Color _lineColor;
    Color _fillColor;
    int _lineAlpha = 255;
    int _fillAlpha = 0;
    JColorChooser _chooser;
    JRadioButton _lineColorButon;
    JRadioButton _fillColorButon;
    JSlider _lineSlider;
    JSlider _alphaSlider;
    JTextField _sensorName = new JTextField(30);
    JRadioButton _hideShape;
    JRadioButton _changeLevel;
    JComboBox<String> _levelComboBox;

    public DrawFrame(String which, String title, ShapeDrawer parent) {
        super(title, false, false);
        _parent = parent;
        setTitle(Bundle.getMessage(which, Bundle.getMessage(title)));

        _lineWidth = 1;
        _lineColor = Color.black;

        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.BorderLayout(10, 10));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        if (which.equals("newShape")) {
            panel.add(Box.createVerticalStrut(STRUT_SIZE));
            JLabel l = new JLabel(Bundle.getMessage("drawInstructions1"));
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            p.add(l);
            if (title.equals("Polygon")) {
                l = new JLabel(Bundle.getMessage("drawInstructions2a"));
                l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                p.add(l);
                l = new JLabel(Bundle.getMessage("drawInstructions2b"));
            } else {
                l = new JLabel(Bundle.getMessage("drawInstructions2"));
            }
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            p.add(l);
        }
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        JLabel l = new JLabel(Bundle.getMessage("drawInstructions3a"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        p.add(l);
        l = new JLabel(Bundle.getMessage("drawInstructions3b", Bundle.getMessage("VisibleSensor")));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        p.add(l);

        JPanel pp = new JPanel();
        pp.add(p);
        panel.add(pp);

        panel.add(makePanel());
        // PositionableShape adds buttons at the bottom
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeSensorPanel());
        panel.add(Box.createVerticalStrut(STRUT_SIZE));

        setContentPane(panel);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                closingEvent(true);
            }
        });
        pack();
        setLocation(_loc);
        setVisible(true);
        setAlwaysOnTop(true);
    }

    protected JPanel makePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel(Bundle.getMessage("lineWidth")));
        JPanel pp = new JPanel();
        pp.add(new JLabel(Bundle.getMessage("thin")));
        _lineSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 30, _lineWidth);
        _lineSlider.addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        widthChange();
                    }
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
        _chooser.setColor(Color.green);
        _chooser.getSelectionModel().addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        colorChange();
                    }
                });
        _chooser.setPreviewPanel(new JPanel());
        panel.add(_chooser);
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel(Bundle.getMessage("transparency")));
        pp = new JPanel();
        pp.add(new JLabel(Bundle.getMessage("transparent")));
        _alphaSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 255, _lineColor.getAlpha());
        _alphaSlider.addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        alphaChange();
                    }
                });
        pp.add(_alphaSlider);
        _lineColorButon.addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        buttonChange();
                    }
                });
        pp.add(new JLabel(Bundle.getMessage("opaque")));
        p.add(pp);
        panel.add(p);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        return panel;
    }

    protected JPanel makeSensorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        JPanel p0 = new JPanel();
        p0.add(new JLabel(Bundle.getMessage("SensorMsg")));
        panel.add(p0);
        p.add(new JLabel(Bundle.getMessage("VisibleSensor") + ":"));
        p.add(_sensorName);
        _sensorName.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String msg =_shape.setControlSensor(_sensorName.getText(), _hideShape.isSelected(), _shape.getChangeLevel());                
                if (msg != null) {
                    JOptionPane.showMessageDialog(null, msg, Bundle.getMessage("MakeLabel", Bundle.getMessage("ErrorSensor")), JOptionPane.INFORMATION_MESSAGE); // NOI18N
                    _sensorName.setText("");
                }
            }
        });
        _sensorName.addMouseMotionListener( new MouseMotionListener() {
            public void mouseDragged( MouseEvent e) {               
                updateShape();
            }
            public void mouseMoved(MouseEvent e) {
                String msg =_shape.setControlSensor(_sensorName.getText(), _hideShape.isSelected(), _shape.getChangeLevel());                
                if (msg != null) {
                    JOptionPane.showMessageDialog(null, msg, Bundle.getMessage("MakeLabel", Bundle.getMessage("ErrorSensor")), JOptionPane.INFORMATION_MESSAGE); // NOI18N
                    _sensorName.setText("");
                }
            }
        });
        panel.add(p);

        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        _hideShape = new JRadioButton(Bundle.getMessage("HideOnSensor"));
        _changeLevel = new JRadioButton(Bundle.getMessage("ChangeLevel"));
        ButtonGroup bg = new ButtonGroup();
        bg.add(_hideShape);
        bg.add(_changeLevel);
        _levelComboBox = new JComboBox<String>();
        _levelComboBox.addItem(Bundle.getMessage("SameLevel"));
        for (int i = 1; i < 11; i++) {
            _levelComboBox.addItem(Bundle.getMessage("Level") + " " + Integer.valueOf(i));
        }
        _levelComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int level = _levelComboBox.getSelectedIndex();
                _shape.setChangeLevel(level);
            }
            
        });
        _hideShape.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                _levelComboBox.setEnabled(false);
            }
        });
        _changeLevel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                _levelComboBox.setEnabled(true);
            }
        });
        p1.add(_hideShape);
        p1.add(_changeLevel);

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        JPanel p3 = new JPanel();
//        p3.add(Box.createRigidArea(_levelComboBox.getPreferredSize()));
        p2.add(p3);
        JPanel p4 = new JPanel();
        p4.add(_levelComboBox);
        p2.add(p4);

        p0 = new JPanel();
        p0.setLayout(new BoxLayout(p0, BoxLayout.X_AXIS));
        p0.add(Box.createHorizontalGlue());
        p0.add(p1);
        p0.add(p2);
        p0.add(Box.createHorizontalGlue());
        panel.add(p0);
        return panel;
    }

    protected JPanel makeParamsPanel(PositionableShape ps) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        return panel;
    }

    /**
     * Set parameters on the popup that will edit the PositionableShape
     * Called both for creation and editing. (don't make a copy for Cancel)
     * @param ps Shape being created or edited
     */
    protected void setDisplayParams(PositionableShape ps) {
        _shape = ps;
        _lineWidth = _shape.getLineWidth();
        _lineSlider.setValue(_lineWidth);
        _lineColor = _shape.getLineColor();
        _fillColor = _shape.getFillColor();
        if (_lineColor.getAlpha() > _fillColor.getAlpha()) {
            _alphaSlider.setValue(_lineColor.getAlpha());
            _lineColorButon.setSelected(true);
        } else {
            _alphaSlider.setValue(_fillColor.getAlpha());
            _fillColorButon.setSelected(true);
        }
        _sensorName.setText(_shape.getSensorName());
        _levelComboBox.setSelectedIndex(ps.getChangeLevel());
        if (ps.isHideOnSensor()) {
            _hideShape.setSelected(true);
            _levelComboBox.setEnabled(false);
        } else {
            _changeLevel.setSelected(true);
        }

        add(makeParamsPanel(_shape));
        add(makeDoneButtonPanel());
        pack();
    }

    /**
     * Editing an existing shape (only make copy for cancel of edits)
     * @param ps shape
     */
    protected void makeCopy(PositionableShape ps) {
        // make a copy, but keep it out of editor's content
        _originalShape = (PositionableShape)ps.deepClone();
        // cloning adds to editor's targetPane - (fix needed in editor)
        _originalShape.remove();        
    }

    private JPanel makeDoneButtonPanel() {
        JPanel panel0 = new JPanel();
        panel0.setLayout(new FlowLayout());
        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        doneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                closingEvent(false);
            }
        });
        panel0.add(doneButton);

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                closingEvent(true);
            }
        });
        panel0.add(cancelButton);
        return panel0;
    }
    
    private void buttonChange() {
        if (_lineColorButon.isSelected()) {
            _chooser.getSelectionModel().setSelectedColor(_lineColor);
            _alphaSlider.setValue(_lineColor.getAlpha());
        } else if (_fillColor!=null){
            _chooser.getSelectionModel().setSelectedColor(_fillColor);
            _alphaSlider.setValue(_fillColor.getAlpha());
        }
        _alphaSlider.revalidate();
        _alphaSlider.repaint();
    }
    
    private void widthChange() {
        if (_shape==null) {
            return;
        }
        _lineWidth = _lineSlider.getValue();
        _shape.setLineWidth(_lineWidth);
        updateShape();
    }

    private void colorChange() {
        if (_shape==null) {
            return;
        }
        if (_lineColorButon.isSelected()) {
            Color c = _chooser.getColor();
            _lineColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), _lineColor.getAlpha());
            _shape.setLineColor(_lineColor);
        } else {
            Color c = _chooser.getColor();
            int alpha;
            if (_fillColor!=null) {
                alpha = _fillColor.getAlpha();
            } else {
                alpha = _alphaSlider.getValue();
            }
            _fillColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
            _shape.setFillColor(_fillColor);
        }
        updateShape();
    }

    private void alphaChange() {
        if (_shape==null) {
            return;
        }
        int alpha = _alphaSlider.getValue();
        if (_lineColorButon.isSelected()) {
            _lineColor = new Color(_lineColor.getRed(), _lineColor.getGreen(), _lineColor.getBlue(), alpha);
            _shape.setLineColor(_lineColor);
        } else if (_fillColorButon.isSelected() && _fillColor!=null) {
            _fillColor = new Color(_fillColor.getRed(), _fillColor.getGreen(), _fillColor.getBlue(), alpha);
            _shape.setFillColor(_fillColor);
        }
        updateShape();
    }

    protected void setDrawParams() {
        TargetPane targetPane = (TargetPane) _parent.getEditor().getTargetPanel();
        Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f);
        targetPane.setSelectRectStroke(stroke);
        targetPane.setSelectRectColor(Color.green);
    }

    protected void closingEvent(boolean cancel) {
        _loc = getLocation(_loc);
        _dim = getSize(_dim);
        if (_shape!=null) {
            if (cancel) {
                _shape.remove();
                if (_originalShape!=null) {
                    _originalShape.getEditor().putItem(_originalShape);
                }
            }
            _shape.closeEditFrame();            
        }
        if (_parent != null) {
            _parent.closeDrawFrame(this);
        }
        dispose();
    }

    protected int getInteger(JTextField field, int value) {
        try {
            int i = Integer.parseInt(field.getText());
            if (i < 0) {
                return value;
            }
            if (i < PositionableShape.SIZE) {
                i = PositionableShape.SIZE;
            }
            return i;
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, nfe,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return value;
        }
    }
    
    protected void updateShape() {
        _shape.removeHandles();
        _shape.makeShape();
        _shape.drawHandles();
        _shape.updateSize();
        _shape.getEditor().getTargetPanel().repaint();
;
    }

    // these 2 methods update the JTextfields when mouse moves handles
    void setDisplayWidth(int w) {       
    }
    void setDisplayHeight(int h) {
    }

    abstract protected boolean makeFigure(MouseEvent event);
}
