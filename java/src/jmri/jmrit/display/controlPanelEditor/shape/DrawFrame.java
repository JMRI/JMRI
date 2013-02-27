package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Stroke;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.NamedBeanHandle;
import jmri.jmrit.display.Editor.TargetPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.swing.JRadioButton;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2012
 * @version $Revision: 1 $
 * 
 */

public abstract class DrawFrame  extends jmri.util.JmriJFrame implements ChangeListener  {
	
    protected ShapeDrawer _parent;
    
    static int STRUT_SIZE = 10;
    static Point _loc = new Point(100,100);
    static Dimension _dim = new Dimension(500,500);

    int		_lineWidth;
	Color 	_lineColor;
	Color 	_fillColor;
	int 	_alpha;
//	Stroke 	_stroke;
    JColorChooser _chooser;
    JRadioButton _lineColorButon;
    JRadioButton _fillColorButon;
    JSlider 	_lineSlider;
    JSlider		_fillSlider;
	JTextField	_sensorName = new JTextField(30);
	PositionableShape _pShape;
    
	public DrawFrame(String which, String title, ShapeDrawer parent) {
		super(title, false, false);
        _parent = parent;
        setTitle(Bundle.getMessage(which, Bundle.getMessage(title)));       
 
        _lineWidth = 1;
        _lineColor = Color.black;
        _alpha = 127;
      
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.BorderLayout(10,10));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        if (which.equals("newShape")) {
            panel.add(Box.createVerticalStrut(STRUT_SIZE));
            JLabel l = new JLabel(Bundle.getMessage("drawInstructions1"));
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            p.add(l);
            if (title.equals("polygon")) {
                l = new JLabel(Bundle.getMessage("drawInstructions2a"));            	            	
                l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
                p.add(l);
                l = new JLabel(Bundle.getMessage("drawInstructions2b"));            	            	
            }else {
                l = new JLabel(Bundle.getMessage("drawInstructions2"));            	
            }
            l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            p.add(l);
        }
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        JLabel l = new JLabel(Bundle.getMessage("drawInstructions3a"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        p.add(l);
        l = new JLabel(Bundle.getMessage("drawInstructions3b"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        p.add(l);
        
        JPanel pp = new JPanel();
        pp.add(p);
        panel.add(pp);
        
        panel.add(makePanel());
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel.add(makeSensorPanel());
        panel.add(Box.createVerticalStrut(STRUT_SIZE));

        setContentPane(panel);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
              public void windowClosing(java.awt.event.WindowEvent e) {
                  closingEvent();
              }
        });
        pack();
        setLocation(_loc);
        setVisible(true);
	}

	protected JPanel makePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
	    p.add(new JLabel(Bundle.getMessage("lineWidth")));
	    JPanel pp = new JPanel();
	    pp.add(new JLabel(Bundle.getMessage("thin")));
	    _lineSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 30, _lineWidth);
	    pp.add(_lineSlider);
	    pp.add(new JLabel(Bundle.getMessage("thick")));
	    p.add(pp);
	    panel.add(p);
	    p = new JPanel();
	    ButtonGroup bg = new ButtonGroup();
	    _lineColorButon = new JRadioButton(Bundle.getMessage("lineColor"));
	    p.add(_lineColorButon);
	    bg.add(_lineColorButon);
	    _fillColorButon = new JRadioButton(Bundle.getMessage("fillColor"));
	    p.add(_fillColorButon);
	    bg.add(_fillColorButon);
	    _lineColorButon.setSelected(true);
	    panel.add(p);
//	       _chooser = new JColorChooser(_parent.getEditor().getTargetPanel().getBackground());
	    _chooser = new JColorChooser(Color.LIGHT_GRAY);
	    _chooser.setColor(Color.green);
	    _chooser.getSelectionModel().addChangeListener(this);
	    _chooser.setPreviewPanel(new JPanel());
	    panel.add(_chooser);
	    p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
	    p.add(new JLabel(Bundle.getMessage("transparency")));
	    pp = new JPanel();
	    pp.add(new JLabel(Bundle.getMessage("transparent")));
	    _fillSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 255, _alpha);
	    pp.add(_fillSlider);
	    pp.add(new JLabel(Bundle.getMessage("opaque")));
	    p.add(pp);
	    panel.add(p);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
	    return panel;
	}
	
    protected JPanel makeSensorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());  //new BoxLayout(p, BoxLayout.Y_AXIS)
        panel.add(new JLabel(Bundle.getMessage("VisibleSensor")));
        panel.add(_sensorName);
	    return panel;
    }
	
    protected JPanel makeParamsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
    	return panel;
    }
	
    /**
     * Set parameters on the popup that will edit the PositionableShape
     */
	protected void setDisplayParams(PositionableShape ps) {
		_alpha = ps.getAlpha();
		_fillSlider.setValue(_alpha);  
		_lineWidth = ps.getLineWidth();
		_lineSlider.setValue(_lineWidth);
		_lineColor = ps.getLineColor();
		_fillColor = ps.getFillColor();
		NamedBeanHandle handle = ps.getControlSensorHandle();
		if (handle!=null) {
			_sensorName.setText(handle.getName());			
		}
	}

	protected void setPositionableParams(PositionableShape ps) {
        ps.setAlpha(_fillSlider.getValue());		//set before setting colors
        ps.setLineColor(_lineColor);
        ps.setFillColor(_fillColor);
        ps.setLineWidth(_lineSlider.getValue());
        String text = _sensorName.getText().trim();
        if (text.length()>0) {
        	ps.setControlSensor(text);
        }
	}

	protected void setDrawParams() {
		TargetPane targetPane = (TargetPane)_parent.getEditor().getTargetPanel();
		Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10f);
		targetPane.setSelectRectStroke(stroke);
		targetPane.setSelectRectColor(Color.green);
	}

	protected void setParent(ShapeDrawer parent) {
		_parent = parent;		
	}
	protected void closingEvent() {
    	if (_parent!=null) {   		
    		_parent.closeDrawFrame(this);
    		_parent.getEditor().resetEditor();
    	}
    	if (_pShape!=null) {
    		_pShape.setVisible(true);
    	}
    	_loc = getLocation(_loc);
    	_dim = getSize(_dim);
    	dispose();
	}

	public void stateChanged(ChangeEvent e) {
		if (_lineColorButon.isSelected()) {
			_lineColor = _chooser.getColor();    		   
		} else if (_fillColorButon.isSelected()) {
			_fillColor = _chooser.getColor();    		   
		}
	}
	
	protected void moveTo(int x, int y) {		
	}
	protected void anchorPoint(int x, int y) {
	}
	protected void drawShape(Graphics g) {		
	}
	protected boolean dragTo(int x, int y) {
		return false;
	}
	
	abstract protected boolean makeFigure();
	abstract protected void updateFigure(PositionableShape pos);
 
    static Logger log = LoggerFactory.getLogger(DrawFrame.class.getName());
}
