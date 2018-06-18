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

    public DrawRectangle(String which, String title, PositionableShape ps, Editor ed, boolean create) {
        super(which, title, ps, ed, create);
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
        _widthText.addActionListener((ActionEvent e) -> {
            _shape.setWidth(getInteger(_widthText, _width));
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
            _shape.setHeight(getInteger(_heightText, _height));
            updateShape();
        });
        p.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                updateShape();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                _shape.setWidth(getInteger(_widthText, _width));
                _shape.setHeight(getInteger(_heightText, _height));
                updateShape();
            }
        });

        panel.add(p);
        return panel;
    }

    @Override
    protected PositionableShape makeFigure(Rectangle r, Editor ed) {
        if (r != null) {
            Rectangle2D.Double rr = new Rectangle2D.Double(0, 0, _width, _height);
            _shape = new PositionableRectangle(ed, rr);
        }
        return _shape;
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