package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;

/**
 * <P>
 * @author Pete Cressman Copyright: Copyright (c) 2012
 *
 */
public class DrawCircle extends DrawFrame {

    JTextField _diameterText;

    public DrawCircle(String which, String title, ShapeDrawer parent) {
        super(which, title, parent);
    }

    /**
     * Create a new PositionableShape
     */
    @Override
    protected JPanel makeParamsPanel(PositionableShape ps) {
        JPanel panel = super.makeParamsPanel(ps);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel(Bundle.getMessage("Circle")));
        JPanel pp = new JPanel();
        _diameterText = new JTextField(6);
        _diameterText.setText(Integer.toString(_shape.getWidth()));
        _diameterText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_diameterText);
        _diameterText.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged( MouseEvent e) {               
                updateShape();
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                _shape.setWidth(Integer.parseInt(_diameterText.getText()));
                updateShape();
            }
        });
        _diameterText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _shape.setWidth(Integer.parseInt(_diameterText.getText()));
                updateShape();
            }
        });
        pp.add(new JLabel(Bundle.getMessage("circleRadius")));
        p.add(pp);
        panel.add(p);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        return panel;
    }

    @Override
    protected boolean makeFigure(MouseEvent event) {
        ControlPanelEditor ed = _parent.getEditor();
        Rectangle r = ed.getSelectRect();
        if (r != null) {
            int dia = Math.max(r.width, r.height);
            Ellipse2D.Double rr = new Ellipse2D.Double(0, 0, dia, dia);
            PositionableCircle ps = new PositionableCircle(ed, rr);
            ps.setLocation(r.x, r.y);
            ps.updateSize();
            setDisplayParams(ps);
            ps.setEditFrame(this);
            ed.putItem(ps);            
        }
        return true;
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
