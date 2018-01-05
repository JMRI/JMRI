package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrit.display.Editor;

/**
 * @author Pete Cressman Copyright (c) 2012
 */
public class DrawCircle extends DrawFrame {

    JTextField _diameterText;

    public DrawCircle(String which, String title, PositionableShape ps) {
        super(which, title, ps);
    }

    @Override
    protected JPanel makeParamsPanel() {
        JPanel panel = super.makeParamsPanel();

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
//        p.add(new JLabel(Bundle.getMessage("Circle")));
        JPanel pp = new JPanel();
        _diameterText = new JTextField(6);
        _diameterText.setText(Integer.toString(_shape.getWidth()));
        _diameterText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_diameterText);
        _diameterText.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                updateShape();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                _shape.setWidth(Integer.parseInt(_diameterText.getText()));
                updateShape();
            }
        });
        _diameterText.addActionListener((ActionEvent e) -> {
            _shape.setWidth(Integer.parseInt(_diameterText.getText()));
            updateShape();
        });
        pp.add(new JLabel(Bundle.getMessage("circleRadius")));
        p.add(pp);
        panel.add(p);
        return panel;
    }

    @Override
    protected void makeFigure(MouseEvent event, Editor ed) {
        Rectangle r = ed.getSelectRect();
        if (r != null) {
            int dia = Math.max(r.width, r.height);
            Ellipse2D.Double rr = new Ellipse2D.Double(0, 0, dia, dia);
            _shape = new PositionableCircle(ed, rr);
            _shape.setLocation(r.x, r.y);
            _shape.updateSize();
            _shape.setEditFrame(this);
            setDisplayParams();
            ed.putItem(_shape);
        }
    }

    @Override
    void setDisplayWidth(int w) {
        _diameterText.setText(Integer.toString(w));
    }

    @Override
    void setDisplayHeight(int h) {
        _diameterText.setText(Integer.toString(h));
    }
}
