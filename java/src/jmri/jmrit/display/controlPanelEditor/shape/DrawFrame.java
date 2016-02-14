package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.jmrit.display.Editor.TargetPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.swing.JRadioButton;
/**
 * <P>
 * @author Pete Cressman Copyright: Copyright (c) 2012
 * @version $Revision: 1 $
 *
 */
public abstract class DrawFrame extends jmri.util.JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = -2448915417340063804L;
    protected ShapeDrawer _parent;
    protected boolean _editing;

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
        _editing = (_parent == null);		// i.e. constructor called from editItem popup
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
            if (title.equals("polygon")) {
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
        l = new JLabel(Bundle.getMessage("drawInstructions3b"));
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
                closingEvent();
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
//	       _chooser = new JColorChooser(_parent.getEditor().getTargetPanel().getBackground());
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
        p.add(new JLabel(Bundle.getMessage("VisibleSensor")));
        p.add(_sensorName);
        panel.add(p);
        JPanel p0 = new JPanel();
        p0.add(new JLabel(Bundle.getMessage("SensorMsg")));
        panel.add(p0);

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

    protected JPanel makeParamsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        return panel;
    }

    /**
     * Set parameters on the popup that will edit the PositionableShape
     */
    protected void setDisplayParams(PositionableShape ps) {
        _lineWidth = ps.getLineWidth();
        _lineSlider.setValue(_lineWidth);
        _lineColor = ps.getLineColor();
        _fillColor = ps.getFillColor();
        if (_lineColor.getAlpha() > _fillColor.getAlpha()) {
            _alphaSlider.setValue(_lineColor.getAlpha());
            _lineColorButon.setSelected(true);
        } else {
            _alphaSlider.setValue(_fillColor.getAlpha());
            _fillColorButon.setSelected(true);
        }
        NamedBeanHandle<Sensor> handle = ps.getControlSensorHandle();
        if (handle != null) {
            _sensorName.setText(handle.getName());
            if (ps.isHideOnSensor()) {
                _hideShape.setSelected(true);
                _levelComboBox.setEnabled(false);
            } else {
                _changeLevel.setSelected(true);
            }
            int level = ps.getChangeLevel();
            if (level < 0) {
                level = 0;
            }
            _levelComboBox.setSelectedIndex(level);
        }
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

    private void colorChange() {
        if (_lineColorButon.isSelected()) {
            Color c = _chooser.getColor();
            _lineColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), _lineColor.getAlpha());
        } else {
            Color c = _chooser.getColor();
            int alpha;
            if (_fillColor!=null) {
                alpha = _fillColor.getAlpha();
            } else {
                alpha = _alphaSlider.getValue();
            }
            _fillColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
        }
    }

    private void alphaChange() {
        int alpha = _alphaSlider.getValue();
        if (_lineColorButon.isSelected()) {
            _lineColor = new Color(_lineColor.getRed(), _lineColor.getGreen(), _lineColor.getBlue(), alpha);
        } else if (_fillColorButon.isSelected() && _fillColor!=null) {
            _fillColor = new Color(_fillColor.getRed(), _fillColor.getGreen(), _fillColor.getBlue(), alpha);
        }
    }

    protected void setPositionableParams(PositionableShape ps) {
        ps.setLineColor(_lineColor);
        ps.setFillColor(_fillColor);
        ps.setLineWidth(_lineSlider.getValue());
        String text = _sensorName.getText().trim();
        String levelStr = (String) _levelComboBox.getSelectedItem();
        int level = -1;
        if (!Bundle.getMessage("SameLevel").equals(levelStr)) {
            levelStr = levelStr.substring(Bundle.getMessage(Bundle.getMessage("Level")).length() + 1);
            level = Integer.valueOf(levelStr);
        }
        if (text.length() > 0) {
            ps.setControlSensor(text, _hideShape.isSelected(), level);
        } else {
            ps.dispose();
        }
    }

    protected void setDrawParams() {
        TargetPane targetPane = (TargetPane) _parent.getEditor().getTargetPanel();
        Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f);
        targetPane.setSelectRectStroke(stroke);
        targetPane.setSelectRectColor(Color.green);
    }

    protected void closingEvent() {
        if (_parent != null) {
            _parent.closeDrawFrame(this);
            _parent.getEditor().resetEditor();
        }
        _loc = getLocation(_loc);
        _dim = getSize(_dim);
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
                    Bundle.getMessage("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return value;
        }
    }

    abstract protected boolean makeFigure(MouseEvent event);

    abstract protected void updateFigure(PositionableShape pos);

    private final static Logger log = LoggerFactory.getLogger(DrawFrame.class.getName());
}
