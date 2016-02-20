package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
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
 * @version $Revision: 1 $
 *
 */
public class DrawCircle extends DrawFrame {

    /**
     *
     */
    private static final long serialVersionUID = 3871500332284884080L;
    JTextField _radiusText;
    int _radius;			// corner radius

    public DrawCircle(String which, String title, ShapeDrawer parent) {
        super(which, title, parent);
        _radius = 100;
    }

    /**
     * Create a new PositionableShape
     */
    protected JPanel makeParamsPanel() {
        JPanel panel = super.makeParamsPanel();

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel(Bundle.getMessage("circle")));
        JPanel pp = new JPanel();
        _radiusText = new JTextField(6);
        _radiusText.setText(Integer.toString(_radius));
        _radiusText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_radiusText);
        pp.add(new JLabel(Bundle.getMessage("circleRadius")));
        p.add(pp);
        panel.add(p);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        return panel;
    }

    protected boolean makeFigure(MouseEvent event) {
        ControlPanelEditor ed = _parent.getEditor();
        Rectangle r = ed.getSelectRect();
        if (r != null) {
            _radius = Math.max(r.width, r.height);
            Ellipse2D.Double rr = new Ellipse2D.Double(0, 0, _radius, _radius);
            PositionableCircle ps = new PositionableCircle(ed, rr);
            ps.setLocation(r.x, r.y);
            ps.setDisplayLevel(ControlPanelEditor.MARKERS);
            setPositionableParams(ps);
            ps.updateSize();
            ed.putItem(ps);
        }
        return true;
    }

    /**
     * Set parameters on a new or updated PositionableShape
     */
    protected void setPositionableParams(PositionableShape p) {
        super.setPositionableParams(p);
        ((PositionableCircle) p).setRadius(_radius);
    }

    /**
     * Set parameters on the popup that will edit the PositionableShape
     */
    protected void setDisplayParams(PositionableShape p) {
        super.setDisplayParams(p);
        PositionableCircle pos = (PositionableCircle) p;
        _radius = pos.getRadius();
    }

    /**
     * Editing is done. Update the existing PositionableShape
     */
    protected void updateFigure(PositionableShape p) {
        PositionableCircle pos = (PositionableCircle) p;
        _radius = getInteger(_radiusText, _radius);
        p._width = _radius;
        p._height = _radius;
        pos.makeShape();
        setPositionableParams(pos);
    }
}
