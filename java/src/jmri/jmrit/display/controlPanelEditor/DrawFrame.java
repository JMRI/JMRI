package jmri.jmrit.display.controlPanelEditor;

import java.awt.Dimension;
import java.awt.Point;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2012
 * @version $Revision: 1 $
 * 
 */

public class DrawFrame  extends jmri.util.JmriJFrame {
	
    private ShapeDrawer _parent;
    
    static java.util.ResourceBundle rbcp = ControlPanelEditor.rbcp;
    static int STRUT_SIZE = 10;
    static Point _loc = new Point(100,100);
    static Dimension _dim = new Dimension(500,500);
	
	public DrawFrame(String title, ShapeDrawer parent) {
		super(title);
        _parent = parent;
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                closingEvent();
            }
        });
        setLocation(_loc);
        setSize(_dim);
        setVisible(true);
   }
    protected void closingEvent() {
      _parent.closeDrawFrame(this);
      _loc = getLocation(_loc);
      _dim = getSize(_dim);
      dispose();
  }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DrawFrame.class.getName());
}