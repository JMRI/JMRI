package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
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
 * @version $Revision: 1 $
 *
 */
public class DrawRoundRect extends DrawRectangle {

    /**
     *
     */
    private static final long serialVersionUID = -2900780978857411283L;
    JTextField _radiusText;
    int _radius;			// corner radius

    public DrawRoundRect(String which, String title, ShapeDrawer parent) {
        super(which, title, parent);
        _radius = 40;
    }

    @Override
    protected JPanel makeParamsPanel() {
        JPanel panel = super.makeParamsPanel();
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JPanel pp = new JPanel();
        _radiusText = new JTextField(6);
        _radiusText.setText(Integer.toString(_radius));
        _radiusText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_radiusText);
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
            _width = r.width;
            _height = r.height;
            RoundRectangle2D.Double rr = new RoundRectangle2D.Double(0, 0, r.width, r.height, _radius, _radius);
            PositionableRoundRect ps = new PositionableRoundRect(ed, rr);
            ps.setLocation(r.x, r.y);
            ps.setDisplayLevel(ControlPanelEditor.MARKERS);
            setPositionableParams(ps);
            ps.updateSize();
            ed.putItem(ps);
        }
        return true;
    }

    @Override
    protected void setPositionableParams(PositionableShape p) {
        super.setPositionableParams(p);
        ((PositionableRoundRect) p).setCornerRadius(_radius);
    }

    /**
     * Set parameters on the popup that will edit the PositionableShape
     */
    @Override
    protected void setDisplayParams(PositionableShape p) {
        super.setDisplayParams(p);
        PositionableRoundRect pos = (PositionableRoundRect) p;
        _radius = pos.getCornerRadius();
    }

    /**
     * Editing is done. Update the existing PositionableShape
     */
    @Override
    protected void updateFigure(PositionableShape p) {
        PositionableRoundRect pos = (PositionableRoundRect) p;
        _radius = getInteger(_radiusText, _radius);
        p._width = getInteger(_widthText, p._width);
        p._height = getInteger(_heightText, p._height);
        pos.makeShape();
        setPositionableParams(pos);
    }
}
