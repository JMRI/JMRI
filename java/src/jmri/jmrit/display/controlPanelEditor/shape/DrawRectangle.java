package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
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
public class DrawRectangle extends DrawFrame {

    int _width;
    int _height;
    JTextField _widthText;
    JTextField _heightText;

    public DrawRectangle(String which, String title, ShapeDrawer parent) {
        super(which, title, parent);
        _lineWidth = 3;
    }

    @Override
    protected JPanel makeParamsPanel(PositionableShape ps) {
        JPanel panel = super.makeParamsPanel(ps);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JPanel pp = new JPanel();
        _widthText = new JTextField(6);
        _width = ps.getWidth();
        _widthText.setText(Integer.toString(_width));
        _widthText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_widthText);
        pp.add(new JLabel(Bundle.getMessage("width")));
        
        p.add(pp);
        p.add(Box.createHorizontalStrut(STRUT_SIZE));
        _widthText.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _shape.setWidth(Integer.parseInt(_widthText.getText()));                
                updateShape();
            }
        });

        pp = new JPanel();
        _heightText = new JTextField(6);
        _height = ps.getHeight();
        _heightText.setText(Integer.toString(_height));
        _heightText.setHorizontalAlignment(JTextField.RIGHT);
        pp.add(_heightText);
        pp.add(new JLabel(Bundle.getMessage("height")));
        p.add(pp);
        _heightText.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _shape.setHeight(Integer.parseInt(_heightText.getText()));
                updateShape();
            }
        });
        p.addMouseMotionListener( new MouseMotionListener() {
            @Override
            public void mouseDragged( MouseEvent e) {               
                updateShape();
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                int w = Integer.parseInt(_widthText.getText());
                int h = Integer.parseInt(_heightText.getText());
                if (w!=_shape.getWidth() || h!=_shape.getHeight()) {
                    _shape.setHeight(h);
                    _shape.setWidth(w);
                    updateShape();                    
                }
            }
        });

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
            Rectangle2D.Double rr = new Rectangle2D.Double(0, 0, _width, _height);
            PositionableShape ps = new PositionableRectangle(ed, rr);
            ps.setLocation(r.x, r.y);
            ps.updateSize();
            setDisplayParams(ps);
            ps.setEditFrame(this);
            ed.putItem(ps);            
        }
        return true;
    }

    /**
     * Set parameters on a new or updated PositionableShape
     *
    @Override
    protected void setPositionableParams(PositionableShape ps) {
        super.setPositionableParams(ps);
        ps.setWidth(_width);
        ps.setHeight(_height);
    }*/

    /**
     * Set parameters on the popup that will edit the PositionableShape
     */
    @Override
    protected void setDisplayParams(PositionableShape p) {
        super.setDisplayParams(p);
        PositionableRectangle pos = (PositionableRectangle) p;
        _width = pos.getWidth();
        _height = pos.getHeight();
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
