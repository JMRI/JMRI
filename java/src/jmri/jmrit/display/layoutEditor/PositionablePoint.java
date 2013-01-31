package jmri.jmrit.display.layoutEditor;

import java.awt.geom.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import jmri.NamedBeanHandle;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.jmrit.signalling.SignallingGuiTools;

/**
 * PositionablePoint is a Point defining a node in the Track that can be dragged around the
 * inside of the enclosing LayoutEditor panel using a right-drag (drag with meta key).
 * <P>
 * Two types of Positionable Point are supported:
 *		Anchor - point on track - two track connections
 *		End Bumper - end of track point - one track connection
 * <P>
 * Note that a PositionablePoint exists for specifying connectivity and drawing position
 * only.  The Track Segments connected to a PositionablePoint may belong to the same block
 * or to different blocks.  Since each Track Segment may only belong to one block, a 
 * PositionablePoint may function as a Block Boundary.
 * <P>
 * Signal names are saved here at a Block Boundary anchor point by the tool Set Signals at
 * Block Boundary. PositionablePoint does nothing with these signal head names; it only 
 * serves as a place to store them.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @version $Revision$
 */

public class PositionablePoint
{

	// Defined text resource
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

	// defined constants
	public static final int ANCHOR = 1;
	public static final int END_BUMPER = 2;
	
	// operational instance variables (not saved between sessions)
	private PositionablePoint instance = null;
	private LayoutEditor layoutEditor = null;
	
	// persistent instances variables (saved between sessions)
	private String ident = "";
	private int type = 0;
	private TrackSegment connect1 = null;
	private TrackSegment connect2 = null;
	private Point2D coords = new Point2D.Double(10.0,10.0);
	private String eastBoundSignalName = ""; // signal head for east (south) bound trains
	private String westBoundSignalName = ""; // signal head for west (north) bound trains
    
    private NamedBeanHandle<SignalMast> eastBoundSignalMastNamed = null;
    private NamedBeanHandle<SignalMast> westBoundSignalMastNamed = null;
    /* We use a namedbeanhandle for the the sensors, even though we only store the name here, 
    this is so that we can keep up with moves and changes of userNames */
    private NamedBeanHandle<Sensor> eastBoundSensorNamed = null;
    private NamedBeanHandle<Sensor> westBoundSensorNamed = null;

	
    public PositionablePoint(String id, int t, Point2D p, LayoutEditor myPanel) {
		instance = this;
		layoutEditor = myPanel;
		if ( (t==ANCHOR) || (t==END_BUMPER) ) {
			type = t;
		}
		else {
			log.error("Illegal type of PositionablePoint - "+t);
			type = ANCHOR;
		}
		ident = id;
		coords = p;
    }

	/**
	 * Accessor methods
	*/
	public String getID() {return ident;}
	public int getType() {return type;}
	public TrackSegment getConnect1() {return connect1;}
	public TrackSegment getConnect2() {return connect2;}
	public Point2D getCoords() {return coords;}
	public void setCoords(Point2D p) {coords = p;}
	public String getEastBoundSignal() {return eastBoundSignalName;}
	public void setEastBoundSignal(String signalName) {eastBoundSignalName = signalName;}
	public String getWestBoundSignal() {return westBoundSignalName;}
	public void setWestBoundSignal(String signalName) {westBoundSignalName = signalName;}
    
    public String getEastBoundSensorName() {
        if(eastBoundSensorNamed!=null)
            return eastBoundSensorNamed.getName();
        return "";
    }
    
    public Sensor getEastBoundSensor(){
        if(eastBoundSensorNamed!=null)
            return eastBoundSensorNamed.getBean();
        return null;
    }
    
	public void setEastBoundSensor(String sensorName) {
        if(sensorName==null || sensorName.equals("")){
            eastBoundSensorNamed=null;
            return;
        }
            
        Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        if (sensor != null) {
            eastBoundSensorNamed = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } else {
            eastBoundSensorNamed=null;
        }
    }
    
	public String getWestBoundSensorName() {
        if(westBoundSensorNamed!=null)
            return westBoundSensorNamed.getName();
        return "";
    }
    
    public Sensor getWestBoundSensor(){
        if(westBoundSensorNamed!=null)
            return westBoundSensorNamed.getBean();
        return null;
    }
    
	public void setWestBoundSensor(String sensorName) {
        if(sensorName==null || sensorName.equals("")){
            westBoundSensorNamed=null;
            return;
        }
        Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        if (sensor != null) {
            westBoundSensorNamed = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } else {
            westBoundSensorNamed=null;
        }
    }
    
    public String getEastBoundSignalMastName(){
        if(eastBoundSignalMastNamed!=null)
            return eastBoundSignalMastNamed.getName();
        return "";
    }
    
    public SignalMast getEastBoundSignalMast(){
        if(eastBoundSignalMastNamed!=null)
            return eastBoundSignalMastNamed.getBean();
        return null;
    }
    
	public void setEastBoundSignalMast(String signalMast){
        if(signalMast==null || signalMast.equals("")){
            eastBoundSignalMastNamed=null;
            return;
        }
        
        SignalMast mast = InstanceManager.signalMastManagerInstance().provideSignalMast(signalMast);
        if (mast != null) {
            eastBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            eastBoundSignalMastNamed=null;
        }
    }
    
	public String getWestBoundSignalMastName() {
        if(westBoundSignalMastNamed!=null)
            return westBoundSignalMastNamed.getName();
        return "";
    }
    
    public SignalMast getWestBoundSignalMast(){
        if(westBoundSignalMastNamed!=null)
            return westBoundSignalMastNamed.getBean();
        return null;
    }
    
	public void setWestBoundSignalMast(String signalMast){
        if(signalMast==null || signalMast.equals("")){
            westBoundSignalMastNamed=null;
            return;
        }
        
        SignalMast mast = InstanceManager.signalMastManagerInstance().provideSignalMast(signalMast);
        if (mast != null) {
            westBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            westBoundSignalMastNamed=null;
        }
    }
    
	// initialization instance variables (used when loading a LayoutEditor)
	public String trackSegment1Name = "";
	public String trackSegment2Name = "";
	/**
	 * Initialization method
	 *   The above variables are initialized by PositionablePointXml, then the following
	 *        method is called after the entire LayoutEditor is loaded to set the specific
	 *        TrackSegment objects.
	 */
	public void setObjects(LayoutEditor p) {
		connect1 = p.findTrackSegmentByName(trackSegment1Name);
		connect2 = p.findTrackSegmentByName(trackSegment2Name);
	}
		
	/**
	 * Setup and remove connections to track
	 */
	public void setTrackConnection (TrackSegment track) {
		if (track==null) {
			return;
		}
		if ( (connect1!=track) && (connect2!=track) ) {
			// not connected to this track
			if (connect1==null) {
				connect1 = track;
			}
			else if ( (type!=END_BUMPER) && (connect2==null) ) {
				connect2 = track;
                if(connect1.getLayoutBlock()==connect2.getLayoutBlock()){
                    westBoundSignalMastNamed=null;
                    eastBoundSignalMastNamed=null;
                    setWestBoundSensor("");
                    setEastBoundSensor("");
                }
			}
			else {
				log.error ("Attempt to assign more than allowed number of connections");
			}
		}
	}
    
	public void removeTrackConnection (TrackSegment track) {
		if (track==connect1) {
			connect1 = null;
            reCheckBlockBoundary();
		}
		else if (track==connect2) {
			connect2 = null;
            reCheckBlockBoundary();
		}
		else {
			log.error ("Attempt to remove non-existant track connection");
		}
	}
    
    public void reCheckBlockBoundary(){
        if(type==END_BUMPER)
            return;
        if(connect1==null && connect2==null){
            //This is no longer a block boundary, therefore will remove signal masts and sensors if present
            if(westBoundSignalMastNamed!=null)
                removeSML(getWestBoundSignalMast());
            if(eastBoundSignalMastNamed!=null)
                removeSML(getEastBoundSignalMast());
            westBoundSignalMastNamed=null;
            eastBoundSignalMastNamed=null;
            setWestBoundSensor("");
            setEastBoundSensor("");
            //May want to look at a method to remove the assigned mast from the panel and potentially any SignalMast logics generated
        }  else if(connect1==null || connect2==null){
            //could still be in the process of rebuilding the point details
            return;
        } else if (connect1.getLayoutBlock()==connect2.getLayoutBlock()){
            //We are no longer a block bounardy
            if(westBoundSignalMastNamed!=null)
                removeSML(getWestBoundSignalMast());
            if(eastBoundSignalMastNamed!=null)
                removeSML(getEastBoundSignalMast());
            westBoundSignalMastNamed=null;
            eastBoundSignalMastNamed=null;
            setWestBoundSensor("");
            setEastBoundSensor("");
            //May want to look at a method to remove the assigned mast from the panel and potentially any SignalMast logics generated
        }
    }
    
    void removeSML(SignalMast signalMast){
        if(signalMast==null)
            return;
        if(jmri.InstanceManager.layoutBlockManagerInstance().isAdvancedRoutingEnabled() && InstanceManager.signalMastLogicManagerInstance().isSignalMastUsed(signalMast)){
            SignallingGuiTools.removeSignalMastLogic(null, signalMast);
        }
    }

    protected int maxWidth(){
        return 5;
    }
    protected int maxHeight(){
        return 5;
    }
    // cursor location reference for this move (relative to object)
    int xClick = 0;
    int yClick = 0;

    public void mousePressed(MouseEvent e) {
        // remember where we are
        xClick = e.getX();
        yClick = e.getY();
        // if (debug) log.debug("Pressed: "+where(e));
        if (e.isPopupTrigger()) {
            showPopUp(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        // if (debug) log.debug("Release: "+where(e));
        if (e.isPopupTrigger()) {
            showPopUp(e);
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopUp(e);
        }
    }

    JPopupMenu popup = null;
	LayoutEditorTools tools = null;
    /**
     * For editing: only provides remove
     */
    protected void showPopUp(MouseEvent e) {
        if (popup != null ) {
			popup.removeAll();
		}
		else {
            popup = new JPopupMenu();
		}
		boolean blockBoundary = false;
        boolean endBumper = false;
		switch (getType()) {
			case ANCHOR:
				popup.add(rb.getString("Anchor"));
				LayoutBlock block1 = null;
				LayoutBlock block2 = null;
				if (connect1!=null) block1 = connect1.getLayoutBlock();
				if (connect2!=null) block2 = connect2.getLayoutBlock();
				if ( (block1!=null) && (block1==block2) ) {
					popup.add(rb.getString("Block")+": "+block1.getID());
				}
				else if ( (block1!=null) && (block2!=null) && (block1!=block2) ) {
					popup.add(rb.getString("BlockDivider"));
					popup.add(" "+rb.getString("Block1ID")+": "+block1.getID());
					popup.add(" "+rb.getString("Block2ID")+": "+block2.getID());
					blockBoundary = true;
				}
				break;
			case END_BUMPER:
				popup.add(rb.getString("EndBumper"));
				LayoutBlock blockEnd = null;
				if (connect1!=null) blockEnd = connect1.getLayoutBlock();
				if (blockEnd!=null) {
					popup.add(rb.getString("BlockID")+": "+blockEnd.getID());
				}
                endBumper = true;
				break;
            default : break;
		}
		popup.add(new JSeparator(JSeparator.HORIZONTAL));
		popup.add(new AbstractAction(rb.getString("Remove")) {
				public void actionPerformed(ActionEvent e) {
					if (layoutEditor.removePositionablePoint(instance)) {
						// user is serious about removing this point from the panel
						remove();
						dispose();
					}
				}
			});
		if (blockBoundary) {
			popup.add(new AbstractAction(rb.getString("SetSignals")) {
					public void actionPerformed(ActionEvent e) {
					if (tools == null) {
						tools = new LayoutEditorTools(layoutEditor);
					}
					// bring up signals at level crossing tool dialog
					tools.setSignalsAtBlockBoundaryFromMenu(instance,
						layoutEditor.signalIconEditor,layoutEditor.signalFrame);						
					}
				});
            popup.add(new AbstractAction(rb.getString("SetSensors")) {
                public void actionPerformed(ActionEvent event) {
					if (tools == null) {
						tools = new LayoutEditorTools(layoutEditor);
					}
					// bring up signals at block boundary tool dialog
					tools.setSensorsAtBlockBoundaryFromMenu(instance,
                        layoutEditor.sensorIconEditor,layoutEditor.sensorFrame);
                }
            });
            popup.add(new AbstractAction(rb.getString("SetSignalMasts")) {
                public void actionPerformed(ActionEvent event) {
					if (tools == null) {
						tools = new LayoutEditorTools(layoutEditor);
					}
					// bring up signals at block boundary tool dialog
					tools.setSignalMastsAtBlockBoundaryFromMenu(instance);
                }
            });
		}
        if (endBumper){
            popup.add(new AbstractAction(rb.getString("SetSensors")) {
                public void actionPerformed(ActionEvent event) {
					if (tools == null) {
						tools = new LayoutEditorTools(layoutEditor);
					}
					// bring up signals at block boundary tool dialog
					tools.setSensorsAtBlockBoundaryFromMenu(instance,
                        layoutEditor.sensorIconEditor,layoutEditor.sensorFrame);
                }
            });
            popup.add(new AbstractAction(rb.getString("SetSignalMasts")) {
                public void actionPerformed(ActionEvent event) {
					if (tools == null) {
						tools = new LayoutEditorTools(layoutEditor);
					}
					// bring up signals at block boundary tool dialog
					tools.setSignalMastsAtBlockBoundaryFromMenu(instance);
                }
            });
        }
        layoutEditor.setShowAlignmentMenu(popup);
		popup.show(e.getComponent(), e.getX(), e.getY());
	}

    String where(MouseEvent e) {
        return ""+e.getX()+","+e.getY();
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionablePoint.class.getName());

}