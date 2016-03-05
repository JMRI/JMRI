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
public class DrawEllipse extends DrawFrame {

    /**
     *
     */
    private static final long serialVersionUID = 4225546649083339942L;
    int _width;
    int _height;
    JTextField _widthText;
    JTextField _heightText;

    public DrawEllipse(String which, String title, ShapeDrawer parent) {
        super(which, title, parent);
    }

    protected JPanel makeParamsPanel() {
        JPanel panel = super.makeParamsPanel();
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JPanel pp = new JPanel();
        _widthText = new JTextField(6);
        _widthText.setText(Integer.toString(_width));
        _widthText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_widthText);
        pp.add(new JLabel(Bundle.getMessage("width")));
        p.add(pp);
        p.add(Box.createHorizontalStrut(STRUT_SIZE));

        pp = new JPanel();
        _heightText = new JTextField(6);
        _heightText.setText(Integer.toString(_height));
        _heightText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_heightText);
        pp.add(new JLabel(Bundle.getMessage("height")));
        p.add(pp);

        panel.add(p);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        return panel;
    }

    /**
     * Create a new PositionableShape
     */
    protected boolean makeFigure(MouseEvent event) {
        ControlPanelEditor ed = _parent.getEditor();
        Rectangle r = ed.getSelectRect();
        if (r != null) {
            _width = r.width;
            _height = r.height;
            Ellipse2D.Double rr = new Ellipse2D.Double(0, 0, _width, _height);
            PositionableEllipse ps = new PositionableEllipse(ed, rr);
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
        PositionableEllipse pos = (PositionableEllipse) p;
        pos.setWidth(_width);
        pos.setHeight(_height);
    }

    /**
     * Set parameters on the popup that will edit the PositionableShape
     */
    protected void setDisplayParams(PositionableShape p) {
        super.setDisplayParams(p);
        PositionableEllipse pos = (PositionableEllipse) p;
        _width = pos.getWidth();
        _height = pos.getHeight();
    }

    /**
     * Editing is done. Update the existing PositionableShape
     */
    protected void updateFigure(PositionableShape p) {
        PositionableEllipse pos = (PositionableEllipse) p;
        p._width = getInteger(_widthText, p._width);
        p._height = getInteger(_heightText, p._height);
        pos.makeShape();
        setPositionableParams(pos);
    }
}
