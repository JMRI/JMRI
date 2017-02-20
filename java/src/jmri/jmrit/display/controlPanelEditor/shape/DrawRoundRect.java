package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.RoundRectangle2D;
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
public class DrawRoundRect extends DrawRectangle {

    JTextField _radiusText;

    public DrawRoundRect(String which, String title, ShapeDrawer parent) {
        super(which, title, parent);
    }

    @Override
    protected JPanel makeParamsPanel(PositionableShape ps) {
        JPanel panel = super.makeParamsPanel(ps);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JPanel pp = new JPanel();
        _radiusText = new JTextField(6);
        _radiusText.setText(Integer.toString(((PositionableRoundRect)ps).getCornerRadius()));
        _radiusText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_radiusText);
        _radiusText.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((PositionableRoundRect)_shape).setCornerRadius(
                        Integer.parseInt(_radiusText.getText()));
                updateShape();
            }
        });
        _radiusText.addMouseMotionListener( new MouseMotionListener() {
            @Override
            public void mouseDragged( MouseEvent e) {               
                updateShape();
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                ((PositionableRoundRect)_shape).setCornerRadius(
                        Integer.parseInt(_radiusText.getText()));
                updateShape();
            }
        });
        pp.add(new JLabel(Bundle.getMessage("cornerRadius")));
        p.add(pp);
        panel.add(p);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        return panel;
    }

    /**
     * Create a new PositionableShape
     */
    @Override
    protected boolean makeFigure(MouseEvent event) {
        ControlPanelEditor ed = _parent.getEditor();
        Rectangle r = ed.getSelectRect();
        if (r != null) {
            RoundRectangle2D.Double rr = new RoundRectangle2D.Double(0, 0, r.width, r.height, 40, 40);
            PositionableRoundRect ps = new PositionableRoundRect(ed, rr);
            ps.setLocation(r.x, r.y);
            ps.updateSize();
            setDisplayParams(ps);
            ps.setEditFrame(this);
            ed.putItem(ps);            
        }
        return true;
    }

/*    @Override
/*    protected void setPositionableParams(PositionableShape p) {
        super.setPositionableParams(p);
        ((PositionableRoundRect) p).setCornerRadius(_radius);
    }*/

    /**
     * Set parameters on the popup that will edit the PositionableShape
     *
    @Override
    protected void setDisplayParams(PositionableShape p) {
        super.setDisplayParams(p);
        PositionableRoundRect pos = (PositionableRoundRect) p;
        _radius = pos.getCornerRadius();
    }*/
}
