// PositionableJPanel.java

package jmri.jmrit.display;

import java.util.List;
import java.util.ResourceBundle;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JComponent;
import javax.swing.JCheckBoxMenuItem;

/**
 * <p> </p>
 *
 * @author  Bob Jacobsen copyright (C) 2009
 * @version $Revision: 1.7 $
 */
abstract class PositionableJPanel extends JPanel
                        implements MouseMotionListener, MouseListener,
                                    Positionable {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

    public PositionableJPanel() {
        debug = log.isDebugEnabled();
        setProperToolTip();
        connect(this);
    }

    /**
     * For over-riding in the using classes:
     */
    public void setProperToolTip() { }

    /**
     * Connect listeners
     */
    void connect(JComponent j) {
        j.addMouseMotionListener(this);
        j.addMouseListener(this);
    }

    void disconnect() {
        removeMouseMotionListener(this);
        removeMouseListener(this);
    }

   	LayoutEditor layoutPanel = null;
    /**
     * Set panel (called from Layout Editor)
     */
    protected void setPanel(LayoutEditor panel) {
		layoutPanel = panel;
    }

    PanelEditor panelEditor = null;
    /**
     * Set panel (called from Panel Editor)
     * @param panel
     */
    protected void setPanel(PanelEditor panel){
    	panelEditor = panel;
    }

	private Integer displayLevel;
    public void setDisplayLevel(Integer l) { displayLevel = l; }
    public void setDisplayLevel(int l) { setDisplayLevel(new Integer(l)); }
    public Integer getDisplayLevel() { return displayLevel; }

    // cursor location reference for this move (relative to object)
    int xClick = 0;
    int yClick = 0;

    public void mousePressed(MouseEvent e) {
		// if using LayoutEditor, let LayoutEditor handle the mouse pressed event
        if (debug) log.debug("mousePressed: "+where(e));
		if (layoutPanel!=null) {
			layoutPanel.handleMousePressed(e,this.getX(),this.getY());
			return;
		}
        // remember where we are
        xClick = e.getX();
        yClick = e.getY();
        if (debug) log.debug("Pressed: "+where(e));
        if (e.isPopupTrigger()) {
            if (debug) log.debug("show popup");
            showPopUp(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
		// if using LayoutEditor, let LayoutEditor handle the mouse released event
		if (layoutPanel!=null) {
			layoutPanel.handleMouseReleased(e,getX(),getY());
			return;
		}
        if (debug) log.debug("Release: "+where(e));
        if (e.isPopupTrigger()) {
            if (debug) log.debug("show popup");
            showPopUp(e);
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (debug) log.debug("Clicked: "+where(e));
        if (debug && e.isMetaDown()) log.debug("meta down");
        if (debug && e.isAltDown()) log.debug(" alt down");
        if (e.isPopupTrigger()) {
            if (debug) log.debug("show popup");
            showPopUp(e);
        }
    }
    public void mouseExited(MouseEvent e) {
        if (debug) log.debug("Exited:  "+where(e));
    }
    public void mouseEntered(MouseEvent e) {
        if (debug) log.debug("Entered: "+where(e));
    }

    public void mouseMoved(MouseEvent e) {
		if (layoutPanel!=null) layoutPanel.setLoc((int)((getX()+e.getX())/layoutPanel.getZoomScale()),
							(int)((getY()+e.getY())/layoutPanel.getZoomScale())); 
        //if (debug) log.debug("Moved:   "+where(e));
    }
    public void mouseDragged(MouseEvent e) {
		// if using LayoutEditor, let LayoutEditor handle the mouse dragged event
		if (layoutPanel!=null) {
			layoutPanel.handleMouseDragged(e,getX(),getY());
			return;
		}
        if (e.isMetaDown()) {
            List <JComponent> list = panelEditor.getSelections();
            int deltaX = e.getX() - xClick;
            int deltaY = e.getY() - yClick;
            if (!list.contains(this)) {
                movePanel(deltaX, deltaY);
            } else if (getPositionable()) {
                for (int i=0; i<list.size(); i++){
                    JComponent comp = list.get(i);
                    if (comp instanceof PositionableLabel) {
                        ((PositionableLabel)comp).moveLabel(deltaX, deltaY);
                    } else if (comp instanceof PositionableJPanel) {
                        ((PositionableJPanel)comp).movePanel(deltaX, deltaY);
                    }
                }
            }
        }
    }

    // update object postion by how far dragged
    void movePanel(int deltaX, int deltaY) {
        if (getPositionable()) {
            int xObj = getX() + deltaX;
            int yObj = getY() + deltaY;
            // don't allow negative placement, icon can become unreachable
            if (xObj < 0) xObj = 0;
            if (yObj < 0) yObj = 0;
            this.setLocation(xObj, yObj);
            // and show!
            this.repaint();
        }
    }

    String where(MouseEvent e) {
        return ""+e.getX()+","+e.getY();
    }

    protected JPopupMenu popup = null;
    protected JPanel ours;

    /**
     * For over-riding in the using classes: add item specific menu choices
     */
    protected void addToPopup() { }
    protected String getNameString() {return "name?";}

    /**
     * Because this class can change between icon and text forms, 
     * we recreate the popup object each time.
     */
    protected void showPopUp(MouseEvent e) {
//        if (!getPopupEnabled()) return;  // We need to distinguish between popup and editable
        if (!getEditable()) return;
        popup = new JPopupMenu();

		popup.add(new JMenuItem(getNameString()));

        popup.add(lock = newLockMenuItem(new AbstractAction("Lock Position") {
            public void actionPerformed(ActionEvent e) {
                if (lock.isSelected()) {
                    setPositionable(false);
                } else {
                    setPositionable(true);
                }
            }
        }));
        if (getPositionable()) {
            lock.setSelected(false);
        } else {
            lock.setSelected(true);
        }

        this.addToPopup();

        popup.add(new AbstractAction("Remove") {
            public void actionPerformed(ActionEvent e) {
                remove();
                dispose();
            }
        });
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    public JMenuItem newLockMenuItem(AbstractAction a) {
      JCheckBoxMenuItem k = new JCheckBoxMenuItem((String)a.getValue(AbstractAction.NAME));
      k.addActionListener(a);
      if (!getPositionable()) k.setSelected(true);
      return k;
    }

    JMenuItem italic = null;
    JMenuItem bold = null;
    JMenuItem lock = null;
    boolean debug = false;

    public void setPositionable(boolean enabled) {positionable = enabled;}
    public boolean getPositionable() { return positionable; }
    private boolean positionable = true;

    public void setEditable(boolean enabled) {editable = enabled;}
    public boolean getEditable() { return editable; }
    private boolean editable = true;
     
    public void setViewCoordinates(boolean enabled) { viewCoordinates = enabled; }
    public boolean getViewCoordinates() { return viewCoordinates; }
    private boolean viewCoordinates = false;

    public void setControlling(boolean enabled) {controlling = enabled;}
    public boolean getControlling() { return controlling; }
    private boolean controlling = true;

//    public void setPopupEnabled(boolean enabled) {popupEnabled = enabled;}
//    public boolean getPopupEnabled() { return popupEnabled; }
//    private boolean popupEnabled = true;

    /**
    * Generic items for editing an Icon from the panel Popup
    */
    JFrame _editorFrame;
    IconAdder _editor;
    void makeAddIconFrame(String title, String select1, String select2, 
                                IconAdder editor) {
        _editorFrame = new JFrame(rb.getString(title));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        if (select1 != null) p.add(new JLabel(rb.getString(select1)));
        if (select2 != null) p.add(new JLabel(rb.getString(select2)));
        _editorFrame.getContentPane().add(p,BorderLayout.NORTH);
        if (editor != null) {
            _editorFrame.getContentPane().add(editor);
            editor.setParent(_editorFrame);
        } else {
            p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            JButton button = new JButton("Done");
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    setSize(getPreferredSize().width, getPreferredSize().height);
                    _editorFrame.dispose();
                    _editorFrame = null;
                }
            });
            p.add(button);
            _editorFrame.getContentPane().add(p);
        }

        _editorFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
                    _editorFrame.dispose();
                    _editorFrame = null;
                }
            });
        _editorFrame.setLocationRelativeTo(this);
        _editorFrame.setVisible(true);
        _editorFrame.pack();
    }

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    public void dispose() {
        if (popup != null) popup.removeAll();
        popup = null;
        ours = null;
        disconnect();
    }

    /**
     * Removes this object from display and persistance
     */
    void remove() {
		if (layoutPanel!=null) layoutPanel.removeObject(this);
		if (panelEditor!=null) panelEditor.remove(this);
        // cleanup before "this" is removed
        cleanup();
        Point p = this.getLocation();
        int w = this.getWidth();
        int h = this.getHeight();
        Container parent = this.getParent();
        parent.remove(this);
        // force redisplay
        parent.validate();
        parent.repaint(p.x,p.y,w,h);

        // remove from persistance by flagging inactive
        active = false;
    }

    /**
     * To be overridden if any special work needs to be done
     */
    void cleanup() {}

    boolean active = true;
    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionableJPanel.class.getName());
}
