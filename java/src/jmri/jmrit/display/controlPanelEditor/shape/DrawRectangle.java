package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrit.display.Editor;

/**
 * @author Pete Cressman Copyright (c) 2012
 */
public class DrawRectangle extends DrawFrame {

    int _width;
    int _height;
    JTextField _widthText;
    JTextField _heightText;

    public DrawRectangle(String which, String title, PositionableShape ps) {
        super(which, title, ps);
        _lineWidth = 3;
    }

    @Override
    protected JPanel makeParamsPanel() {
        JPanel panel = super.makeParamsPanel();
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JPanel pp = new JPanel();
        _widthText = new JTextField(6);
        _width = _shape.getWidth();
        _widthText.setText(Integer.toString(_width));
        _widthText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_widthText);
        pp.add(new JLabel(Bundle.getMessage("width")));

        p.add(pp);
//        p.add(Box.createHorizontalStrut(STRUT_SIZE));
        _widthText.addActionListener((ActionEvent e) -> {
            _shape.setWidth(Integer.parseInt(_widthText.getText()));
            updateShape();
        });

        pp = new JPanel();
        _heightText = new JTextField(6);
        _height = _shape.getHeight();
        _heightText.setText(Integer.toString(_height));
        _heightText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_heightText);
        pp.add(new JLabel(Bundle.getMessage("height")));
        p.add(pp);
        _heightText.addActionListener((ActionEvent e) -> {
            _shape.setHeight(Integer.parseInt(_heightText.getText()));
            updateShape();
        });
        p.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                updateShape();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int w = Integer.parseInt(_widthText.getText());
                int h = Integer.parseInt(_heightText.getText());
                if (w != _shape.getWidth() || h != _shape.getHeight()) {
                    _shape.setHeight(h);
                    _shape.setWidth(w);
                    updateShape();
                }
            }
        });

        panel.add(p);
//        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        return panel;
    }

    @Override
    protected void makeFigure(MouseEvent event, Editor ed) {
        Rectangle r = ed.getSelectRect();
        if (r != null) {
            _width = r.width;
            _height = r.height;
            Rectangle2D.Double rr = new Rectangle2D.Double(0, 0, _width, _height);
            _shape = new PositionableRectangle(ed, rr);
            _shape.setLocation(r.x, r.y);
            _shape.updateSize();
            _shape.setEditFrame(this);
            setDisplayParams();
            ed.putItem(_shape);
        }
    }

    /**
     * Set parameters on the popup that will edit the PositionableShape.
     */
    @Override
    protected void setDisplayParams() {
        _width = _shape.getWidth();
        _height = _shape.getHeight();
        super.setDisplayParams();
    }

    @Override
    void setDisplayWidth(int w) {
        _widthText.setText(Integer.toString(w));
    }

    @Override
    void setDisplayHeight(int h) {
        _heightText.setText(Integer.toString(h));
    }
}
