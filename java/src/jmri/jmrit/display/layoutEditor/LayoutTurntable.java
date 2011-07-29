package jmri.jmrit.display.layoutEditor;

import jmri.util.JmriJFrame;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.util.ArrayList;

import java.util.ResourceBundle;

import javax.swing.*;

/**
 * A LayoutTurntable is a representation used by LayoutEditor to display a 
 *		turntable.
 * <P>
 * A LayoutTurntable has a variable number of connection points, called RayTracks, 
 *		each radiating from the center of the turntable. Each of these points should 
 *		be connected to a TrackSegment. 
 * <P>
 * Each radiating segment (RayTrack) gets its Block information from its connected 
 *		track segment.
 * <P>
 * Each radiating segment (RayTrack) has a unique connection index. The connection
 *		index is set when the RayTrack is created, and cannot be changed. This 
 *		connection index is used to maintain the identity of the radiating segment
 *		to its connected Track Segment as ray tracks are added and deleted by the user.
 * <P>
 * The radius of the turntable circle is variable by the user.
 * <P>
 * Each radiating segment (RayTrack) connecting point is a fixed distance from 
 *		the center of the turntable. The user may vary the angle of the radiating 
 *		segment. Angles are measured from the vertical (12 o'clock) position in a 
 *		clockwise manner. For example, 30 degrees is 1 o'clock, 60 degrees is 2
 *		o'clock, 90 degrees is 3 o'clock, etc.
 * <P>
 * Each radiating segment is drawn from its connection point to the turntable circle 
 *		in the direction of the turntable center.
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @version $Revision$
 */

public class LayoutTurntable
{

	// Defined text resource
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

	// defined constants 

	// operational instance variables (not saved between sessions)
	private LayoutTurntable instance = null;
	private LayoutEditor layoutEditor = null;
	
	// persistent instance variables (saved between sessions)
	private String ident = "";
	private double radius = 25.0;
	private Point2D center = new Point2D.Double(50.0,50.0);	
	private ArrayList<RayTrack> rayList = new ArrayList<RayTrack>(); // list of Ray Track objects.
    
	/** 
	 * constructor method
	 */  
	public LayoutTurntable (String id, Point2D c, LayoutEditor myPanel) {
		instance = this;
		layoutEditor = myPanel;
		ident = id;
		center = c;
		radius = 25.0;
	}	

	/**
	 * Accessor methods
	*/
	public String getID() {return ident;}
	public Point2D getCoordsCenter() {return center;}
	public double getRadius() {return radius;}
	public void setRadius(double r) {radius = r;}
	protected RayTrack addRay(double angle) {
		RayTrack ray = new RayTrack(angle,getNewIndex());
		// (ray!=null) {
		rayList.add(ray);
		//}
		return ray;
    }
	private int getNewIndex() {
		int index = -1;
		if (rayList.size()==0) return 0;
		boolean found = true;
		while (found) {
			index ++;
			found = false;
			for (int i = 0; (i<rayList.size()) && !found; i++) {
				if (index==rayList.get(i).getConnectionIndex()) 
					found = true;
			}
		}
		return index;
	}		
	// the following method is only for use in loading layout turntables
	public void addRayTrack(double angle,int index,String name) {
		RayTrack ray = new RayTrack(angle,index);
		//if (ray!=null) {
		rayList.add(ray);
		ray.connectName = name;
		//}
    }
	public TrackSegment getRayConnectIndexed(int index) {
		RayTrack ray = null;
		for (int i=0; (i<rayList.size()) && (ray==null); i++) {
			RayTrack r = rayList.get(i);
			if (r.getConnectionIndex() == index) ray = r;
		}
		if (ray==null) return null;
		return ray.getConnect();
	}
	public TrackSegment getRayConnectOrdered(int i) {
		if (i>=rayList.size()) return null;
		RayTrack ray = rayList.get(i);
		if (ray==null) return null;
		return ray.getConnect();
	}
	public void setRayConnect(TrackSegment tr, int index) {
		RayTrack ray = null;
		for (int i=0; (i<rayList.size()) && (ray==null); i++) {
			RayTrack r = rayList.get(i);
			if (r.getConnectionIndex() == index) ray = r;
		}
		if (ray==null) return;
		ray.setConnect(tr);
	}
	public int getNumberRays() {return rayList.size();}
	public int getRayIndex(int i) {
		if (i>=rayList.size()) return 0; 
		RayTrack ray = rayList.get(i);
		return ray.getConnectionIndex();
	}
	public double getRayAngle(int i) {
		if (i>=rayList.size()) return 0.0; 
		RayTrack ray = rayList.get(i);
		return ray.getAngle();
	}		
	public Point2D getRayCoordsIndexed(int index) {
		RayTrack ray = null;
		for (int i=0; (i<rayList.size()) && (ray==null); i++) {
			RayTrack r = rayList.get(i);
			if (r.getConnectionIndex() == index) ray = r;
		}
		if (ray==null) return new Point2D.Double(0.0,0.0);
		double angle = (ray.getAngle()/180.0)*Math.PI;
		// calculate coordinates
		return (new Point2D.Double(
			(center.getX()+((1.25*radius)*Math.sin(angle))),
				(center.getY()-((1.25*radius)*Math.cos(angle)))));
	}
	public Point2D getRayCoordsOrdered(int i) {
		if (i>=rayList.size()) return new Point2D.Double(0.0,0.0); 
		RayTrack ray = rayList.get(i);
		if (ray==null) return new Point2D.Double(0.0,0.0);
		double angle = (ray.getAngle()/180.0)*Math.PI;
		// calculate coordinates
		return (new Point2D.Double(
			(center.getX()+((1.25*radius)*Math.sin(angle))),
				(center.getY()-((1.25*radius)*Math.cos(angle)))));
	}
	public void setRayCoordsIndexed(double x, double y, int index) {
		RayTrack ray = null;
		for (int i=0; (i<rayList.size()) && (ray==null); i++) {
			RayTrack r = rayList.get(i);
			if (r.getConnectionIndex() == index) ray = r;
		}
		if (ray==null) {
			log.error("Attempt to move a non-existant ray track");
			return;
		}
		// convert these coordinates to an angle
		double angle = Math.toDegrees(Math.atan(Math.abs(x-center.getX())/
								Math.abs(y-center.getY())));
		if (x>=center.getX()) {
			if (y>center.getY()) {
				angle = 180.0 - angle;
			}
		}	
		else {
			if (y>center.getY()) {
				angle = 180.0 + angle;
			}
			else {
				angle = 360.0 - angle;
			}
		}
		ray.setAngle(angle);
	}
	
	/** 
	 * Methods to test if ray is a mainline track or not
	 *  Returns true if connecting track segment is mainline
	 *  Defaults to not mainline if connecting track segment is missing
	 */
	public boolean isMainlineIndexed(int index) {
		RayTrack ray = null;
		for (int i=0; (i<rayList.size()) && (ray==null); i++) {
			RayTrack r = rayList.get(i);
			if (r.getConnectionIndex() == index) ray = r;
		}
		if (ray==null) return false;
		TrackSegment tr = ray.getConnect();
		if (tr==null) return false;
		return tr.getMainline();
	}
	public boolean isMainlineOrdered(int i) {
		if (rayList.size()<=i) return false;
		RayTrack ray = rayList.get(i);
		if (ray==null) return false;
		TrackSegment tr = ray.getConnect();
		if (tr==null) return false;
		return tr.getMainline();
	}
	
	/**
	 * Modify coordinates methods
	 */
	public void setCoordsCenter(Point2D p) {
		center = p;
	}
	public void scaleCoords(float xFactor, float yFactor) {
		Point2D pt = new Point2D.Double(round(center.getX()*xFactor),
										round(center.getY()*yFactor));
		center = pt;
	}
	double round (double x) {
		int i = (int)(x+0.5);
		return i;
	}
		
	/**
	 * Initialization method
	 *   The name of each track segment connected to a ray track is initialized by
	 *		by LayoutTurntableXml, then the following method is called after the 
	 *		entire LayoutEditor is loaded to set the specific
	 *      TrackSegment objects.
	 */
	public void setObjects(LayoutEditor p) {
		for (int i = 0; i<rayList.size(); i++) {
			RayTrack ray = rayList.get(i);
			ray.setConnect(p.findTrackSegmentByName(ray.connectName));
		}
	}
    JPopupMenu popup = null;
    /**
     * Display popup menu for information and editing
     */
    protected void showPopUp(MouseEvent e) {
        if (popup != null ) {
			popup.removeAll();
		}
		else {
            popup = new JPopupMenu();
		}
		popup.add(rb.getString("Turntable"));
		popup.add(new JSeparator(JSeparator.HORIZONTAL));
		popup.add(new AbstractAction(rb.getString("Edit")) {
				public void actionPerformed(ActionEvent e) {
					editTurntable(instance);
				}
			});
		popup.add(new AbstractAction(rb.getString("Remove")) {
				public void actionPerformed(ActionEvent e) {
					if (layoutEditor.removeTurntable(instance)) {
						// Returned true if user did not cancel
						remove();
						dispose();
					}
				}
			});
        layoutEditor.setShowAlignmentMenu(popup);
		popup.show(e.getComponent(), e.getX(), e.getY());
    }

	// variables for Edit Turntable pane
	JmriJFrame editTurntableFrame = null;
	JTextField radiusField = new JTextField(8);
	JTextField angleField = new JTextField(8);
	JButton turntableEditDone;
	JButton turntableEditCancel;
	JButton addRayTrack;
	JButton deleteRayTrack;
	String oldRadius = "";
	boolean editOpen = false;
	boolean needsRedraw = false;
	
    /**
     * Edit a Turntable
     */
	protected void editTurntable(LayoutTurntable x) {
		if (editOpen) {
			editTurntableFrame.setVisible(true);
			return;
		}
		needsRedraw = false;
		// Initialize if needed
		if (editTurntableFrame == null) {
            editTurntableFrame = new JmriJFrame( rb.getString("EditTurntable"), false, true );
            editTurntableFrame.addHelpMenu("package.jmri.jmrit.display.EditTurntable", true);
            editTurntableFrame.setLocation(50,30);
            Container contentPane = editTurntableFrame.getContentPane();        
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			// setup radius
            JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
			JLabel radiusLabel = new JLabel( rb.getString("TurntableRadius") );
            panel1.add(radiusLabel);
            panel1.add(radiusField);
            radiusField.setToolTipText( rb.getString("TurntableRadiusHint") );
            contentPane.add(panel1);
			// setup add ray track
            JPanel panel2 = new JPanel(); 
            panel2.setLayout(new FlowLayout());
			JLabel rayAngleLabel = new JLabel( rb.getString("RayAngle"));
            panel2.add(rayAngleLabel);
            panel2.add(angleField);
            angleField.setToolTipText( rb.getString("RayAngleHint") );
            contentPane.add(panel2);
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
			panel3.add(addRayTrack = new JButton(rb.getString("AddRayTrack")));
			addRayTrack.setToolTipText( rb.getString("AddRayTrackHint") );
            addRayTrack.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addRayTrackPressed(e);
                }
            });
			panel3.add(deleteRayTrack = new JButton(rb.getString("DeleteRayTrack")));
			deleteRayTrack.setToolTipText( rb.getString("DeleteRayTrackHint") );
            deleteRayTrack.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    deleteRayTrackPressed(e);
                }
            });
            contentPane.add(panel3);
			// set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(turntableEditDone = new JButton(rb.getString("Done")));
            turntableEditDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    turntableEditDonePressed(e);
                }
            });
            turntableEditDone.setToolTipText( rb.getString("DoneHint") );
			// Cancel
            panel5.add(turntableEditCancel = new JButton(rb.getString("Cancel")));
            turntableEditCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    turntableEditCancelPressed(e);
                }
            });
            turntableEditCancel.setToolTipText( rb.getString("CancelHint") );
            contentPane.add(panel5);		
		}
		// Set up for Edit
		radiusField.setText(" "+radius);
		oldRadius = radiusField.getText();
		angleField.setText("0");
		editTurntableFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					turntableEditCancelPressed(null);
				}
			});
        editTurntableFrame.pack();
        editTurntableFrame.setVisible(true);	
		editOpen = true;	
	}
	private void addRayTrackPressed(ActionEvent a) {
		double ang = 0.0;
		try {
			ang = Float.parseFloat(angleField.getText());
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(editTurntableFrame,rb.getString("EntryError")+": "+
					e+rb.getString("TryAgain"),rb.getString("Error"),
						JOptionPane.ERROR_MESSAGE);
            return;
		}
		addRay(ang);
		layoutEditor.redrawPanel();
		layoutEditor.setDirty();
		needsRedraw = false;
	}
	void deleteRayTrackPressed(ActionEvent a) {
		double ang = 0.0;
		try {
			ang = Float.parseFloat(angleField.getText());
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(editTurntableFrame,rb.getString("EntryError")+": "+
					e+rb.getString("TryAgain"),rb.getString("Error"),
						JOptionPane.ERROR_MESSAGE);
            return;
		}
		// scan rays to find the one to delete
		RayTrack closest = null;
		int closestIndex = -1;
		double bestDel = 360.0;
		for (int i = 0; i<rayList.size(); i++) {
			double del = diffAngle((rayList.get(i)).getAngle(),ang);
			if (del<bestDel) {
				bestDel = del;
				closest = rayList.get(i);
				closestIndex = i;
			}
		}
		if (bestDel>30.0) {
			JOptionPane.showMessageDialog(editTurntableFrame,rb.getString("Error13"),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
            return;
		}
		// delete the closest - first delete any connected Track Segment
		TrackSegment t = null;
		if (closest == null){
			log.error("closest is null!");
		}else{
			t = closest.getConnect();
		}
		if (t!=null) layoutEditor.removeTrackSegment(t);
		rayList.remove(closestIndex);	
		// update the panel
		layoutEditor.redrawPanel();
		layoutEditor.setDirty();
		needsRedraw = false;
	}
	void turntableEditDonePressed(ActionEvent a) {
		// check if new radius was entered
		String str = radiusField.getText();
		if (!str.equals(oldRadius)) {
			double rad = 0.0;
			try {
				rad = Float.parseFloat(str);
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(editTurntableFrame,rb.getString("EntryError")+": "+
						e+rb.getString("TryAgain"),rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
				return;
			}
			radius = rad;
			needsRedraw = true;
		}
		// clean up	
		editOpen = false;
		editTurntableFrame.setVisible(false);
		editTurntableFrame.dispose();
		editTurntableFrame = null;
		if (needsRedraw) {
			layoutEditor.redrawPanel();
			layoutEditor.setDirty();
		}
	}
	void turntableEditCancelPressed(ActionEvent a) {
		editOpen = false;
		editTurntableFrame.setVisible(false);
		editTurntableFrame.dispose();
		editTurntableFrame = null;
		if (needsRedraw) {
			layoutEditor.redrawPanel();
			layoutEditor.setDirty();
		}
	}

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    void dispose() {
        if (popup != null) popup.removeAll();
        popup = null;
    }

    /**
     * Removes this object from display and persistance
     */
    void remove() {
        // remove from persistance by flagging inactive
        active = false;
    }

    boolean active = true;
    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }
	
	static class RayTrack
	{
		public RayTrack(double angle, int index) 
		{ 
			rayAngle = normalizeAngle(angle);
			connect = null;
			connectionIndex = index;
		}
		
		// persistant instance variables
		double rayAngle = 0.0;
		TrackSegment connect = null;
		int connectionIndex = -1;
		
		// accessor routines
		public TrackSegment getConnect() {return connect;}
		public void setConnect(TrackSegment tr) {connect = tr;}
		public double getAngle() {return rayAngle;}
		public void setAngle(double an) {
			rayAngle = normalizeAngle(an);
		}
		public int getConnectionIndex() {return connectionIndex;}
		
		// initialization instance variable (used when loading a LayoutEditor)
		public String connectName = "";
		
		public double normalizeAngle (double a) {
			double angle = a;
			while (angle<0.0) angle += 360.0;
			while (angle>=360.0) angle -= 360.0;
			return angle;
		}
		public double diffAngle (double a, double b) {
			double anA = normalizeAngle (a);
			double anB = normalizeAngle (b);
			if (anA>=anB) {
				if ((anA-anB)<=180.0) return (anA-anB);
				else return (anB+360.0-anA);
			}
			else {
				if ((anB-anA)<=180.0) return (anB-anA);
				else return (anA+360.0-anB);
			}
		}
	}
	public double normalizeAngle (double a) {
		double angle = a;
		while (angle<0.0) angle += 360.0;
		while (angle>=360.0) angle -= 360.0;
		return angle;
	}
	public double diffAngle (double a, double b) {
		double anA = normalizeAngle (a);
		double anB = normalizeAngle (b);
		if (anA>=anB) {
			if ((anA-anB)<=180.0) return (anA-anB);
			else return (anB+360.0-anA);
		}
		else {
			if ((anB-anA)<=180.0) return (anB-anA);
			else return (anA+360.0-anB);
		}
	}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutTurntable.class.getName());

}