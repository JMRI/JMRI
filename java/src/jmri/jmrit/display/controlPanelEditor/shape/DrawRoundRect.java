package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrit.display.Editor;

/**
 * @author Pete Cressman Copyright (c) 2012
 */
public class DrawRoundRect extends DrawRectangle {

    JTextField _radiusText;

    public DrawRoundRect(String which, String title, PositionableShape ps) {
        super(which, title, ps);
    }

    @Override
    protected JPanel makeParamsPanel() {
        JPanel panel = super.makeParamsPanel();
        PositionableRoundRect shape = (PositionableRoundRect)_shape;
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JPanel pp = new JPanel();
        _radiusText = new JTextField(6);
        _radiusText.setText(Integer.toString(shape.getCornerRadius()));
        _radiusText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_radiusText);
        _radiusText.addActionListener((ActionEvent e) -> {
            shape.setCornerRadius(Integer.parseInt(_radiusText.getText()));
            updateShape();
        });
        _radiusText.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                updateShape();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                shape.setCornerRadius(Integer.parseInt(_radiusText.getText()));
                updateShape();
            }
        });
        pp.add(new JLabel(Bundle.getMessage("cornerRadius")));
        p.add(pp);
        panel.add(p);
        return panel;
    }

    @Override
    protected void makeFigure(MouseEvent event, Editor ed) {
        Rectangle r = ed.getSelectRect();
        if (r != null) {
            RoundRectangle2D.Double rr = new RoundRectangle2D.Double(0, 0, r.width, r.height, 40, 40);
            _shape = new PositionableRoundRect(ed, rr);
            _shape.setLocation(r.x, r.y);
            _shape.updateSize();
            _shape.setEditFrame(this);
            setDisplayParams();
            ed.putItem(_shape);
        }
    }

}
