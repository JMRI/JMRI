package jmri.jmrit.display.layoutEditor;

import jmri.util.JmriJFrame;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.SignalHead;
import jmri.NamedBeanHandle;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.*;
import jmri.util.swing.JmriBeanComboBox;
import jmri.jmrit.signalling.SignallingGuiTools;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import java.util.ResourceBundle;
import java.util.ArrayList;

import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutTurnout corresponds to a turnout on the layout. A LayoutTurnout is an
 *      extension of the standard Turnout object with drawing and connectivity
 *      information added. 
 * <P> 
 *  Six types are supported:
 *		right-hand, left-hand, wye, double crossover, right-handed single crossover,
 *      and left-handed single crossover.  Note that double-slip
 *      turnouts can be handled as two turnouts, throat to throat, and three-way
 *		turnouts can be handles as two turnouts, left-hand and right-hand, 
 *      arranged throat to continuing route.
 * <P>
 * A LayoutTurnout has three or four connection points, designated A, B, C, and D.
 *		For right-handed or left-handed turnouts, A corresponds to the throat.
 *		At the crossing, A-B (and C-D for crossovers) is a straight segment
 *		(continuing route).  A-C (and B-D for crossovers) is the diverging
 *		route.  B-C (and A-D for crossovers) is an illegal condition.
 * <P>     
 * A LayoutTurnout carries Block information.  For right-handed, left-handed, and wye
 *      turnouts, the entire turnout is in one block,however, a block border may occur 
 *      at any connection (A,B,C,D). For a double crossover turnout, up to four blocks
 *      may be assigned, one for each connection point, but if only one block is assigned,
 *      that block applies to the entire turnout.
 * <P>
 * For drawing purposes, each LayoutTurnout carries a center point and displacements
 *		for B and C. For right-handed or left-handed turnouts, the displacement for 
 *		A = - the displacement for B, and the center point is at the junction of the
 *		diverging route and the straight through continuing route.  For double 
 *		crossovers, the center point is at the center of the turnout, and the 
 *		displacement for A = - the displacement for C and the displacement for D = 
 *		- the displacement for B.  The center point and these displacements may be 
 *		adjusted by the user when in edit mode.  For double crossovers, AB and BC
 *      are constrained to remain perpendicular.  For single crossovers, AB and CD 
 *		are constrained to remain parallel, and AC and BD are constrained to remain 
 *      parallel.
 * <P>
 * When LayoutTurnouts are first created, a rotation (degrees) is provided.
 *		For 0.0 rotation, the turnout lies on the east-west line with A facing
 *		east.  Rotations are performed in a clockwise direction.
 * <P>
 * When LayoutTurnouts are first created, there are no connections.  Block information
 *		and connections may be added when available.
 * <P>  
 * When a LayoutTurnout is first created, it is enabled for control of an assigned
 *		actual turnout. Clicking on the turnout center point will toggle the turnout.
 *		This can be disabled via the popup menu.
 * <P>
 * Signal Head names are saved here to keep track of where signals are. LayoutTurnout 
 *		only serves as a storage place for signal head names. The names are placed here
 *		by tools, e.g., Set Signals at Turnout, and Set Signals at Double Crossover.
 * <P>
 * A LayoutTurnout may be linked to another LayoutTurnout to form a turnout pair. 
 *		Throat-To-Throat Turnouts - Two turnouts connected closely at their throats, 
 *			so closely that signals are not appropriate at the their throats. This is the 
 *			situation when two RH, LH, or WYE turnouts are used to model a double slip.
 *		3-Way Turnout - Two turnouts modeling a 3-way turnout, where the throat of the 
 *			second turnout is closely connected to the continuing track of the first
 *			turnout.  The throat will have three heads, or one head.
 * A link is required to be able to correctly interpret the use of signal heads.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @version $Revision$
 */

public class LayoutTurnout
{

	// Defined text resource
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

	// defined constants - turnout types
	public static final int RH_TURNOUT = 1;
	public static final int LH_TURNOUT = 2;
	public static final int WYE_TURNOUT = 3;
	public static final int DOUBLE_XOVER = 4;
	public static final int RH_XOVER = 5;
	public static final int LH_XOVER = 6;
    public final static int SINGLE_SLIP = 7; //used in LayoutSlip which extends this class
    public final static int DOUBLE_SLIP = 8; //used in LayoutSlip which extends this class
	// defined constants - link types
	public static final int NO_LINK = 0;
	public static final int FIRST_3_WAY = 1;       // this turnout is the first turnout of a 3-way
													// turnout pair (closest to the throat)
	public static final int SECOND_3_WAY = 2;      // this turnout is the second turnout of a 3-way
													// turnout pair (furthest from the throat)
	public static final int THROAT_TO_THROAT = 3;  // this turnout is one of two throat-to-throat 
													// turnouts - no signals at throat

	// operational instance variables (not saved between sessions)
	//private Turnout turnout = null;
    protected NamedBeanHandle<Turnout> namedTurnout = null;
    //Second turnout is used to either throw a second turnout in a cross over or if one turnout address is used to throw two physical ones
    protected NamedBeanHandle<Turnout> secondNamedTurnout = null;
	protected LayoutBlock block = null;
	private LayoutBlock blockB = null;  // Xover - second block, if there is one
	private LayoutBlock blockC = null;  // Xover - third block, if there is one
	private LayoutBlock blockD = null;  // Xover - fourth block, if there is one
	protected LayoutTurnout instance = null;
	protected LayoutEditor layoutEditor = null;
	private java.beans.PropertyChangeListener mTurnoutListener = null;
	
	// persistent instances variables (saved between sessions)
	public String ident;   // name of this layout turnout (hidden from user)
	public String turnoutName = "";   // should be the name (system or user) of
								//	an existing physical turnout
    public String secondTurnoutName = "";   /* should be the name (system or user) of
								an existing physical turnout. Second turnout is
                                used to allow the throwing of two different turnout
                                to control one cross-over
                                */
	public String blockName = "";  // name for block, if there is one
	public String blockBName = "";  // Xover - name for second block, if there is one
	public String blockCName = "";  // Xover - name for third block, if there is one
	public String blockDName = "";  // Xover - name for fourth block, if there is one
    
	public String signalA1Name = ""; // signal 1 (continuing) (throat for RH, LH, WYE)
	public String signalA2Name = ""; // signal 2 (diverging) (throat for RH, LH, WYE)
	public String signalA3Name = ""; // signal 3 (second diverging) (3-way turnouts only)
	public String signalB1Name = ""; // continuing (RH, LH, WYE) signal 1 (double crossover)
	public String signalB2Name = ""; // LH_Xover and double crossover only
	public String signalC1Name = ""; // diverging (RH, LH, WYE) signal 1 (double crossover)
	public String signalC2Name = ""; // RH_Xover and double crossover only
	public String signalD1Name = ""; // single or double crossover only
	public String signalD2Name = ""; // LH_Xover and double crossover only
    protected NamedBeanHandle<SignalHead> signalA1HeadNamed = null; // signal 1 (continuing) (throat for RH, LH, WYE)
    protected NamedBeanHandle<SignalHead> signalA2HeadNamed = null; // signal 2 (diverging) (throat for RH, LH, WYE)
    protected NamedBeanHandle<SignalHead> signalA3HeadNamed = null; // signal 3 (second diverging) (3-way turnouts only)
    protected NamedBeanHandle<SignalHead> signalB1HeadNamed = null; // continuing (RH, LH, WYE) signal 1 (double crossover)
    protected NamedBeanHandle<SignalHead> signalB2HeadNamed = null; // LH_Xover and double crossover only
    protected NamedBeanHandle<SignalHead> signalC1HeadNamed = null; // diverging (RH, LH, WYE) signal 1 (double crossover)
    protected NamedBeanHandle<SignalHead> signalC2HeadNamed = null; // RH_Xover and double crossover only
    protected NamedBeanHandle<SignalHead> signalD1HeadNamed = null; // single or double crossover only
    protected NamedBeanHandle<SignalHead> signalD2HeadNamed = null; // LH_Xover and double crossover only
    
	/*public String signalAMast = ""; // Throat
	public String signalBMast = ""; // Continuing 
	public String signalCMast = ""; // diverging
	public String signalDMast = ""; // single or double crossover only*/
    
    protected NamedBeanHandle<SignalMast> signalAMastNamed = null; // Throat
    protected NamedBeanHandle<SignalMast> signalBMastNamed = null; // Continuing 
    protected NamedBeanHandle<SignalMast> signalCMastNamed = null; // diverging
    protected NamedBeanHandle<SignalMast> signalDMastNamed = null; // single or double crossover only
    
    protected NamedBeanHandle<Sensor> sensorANamed = null; // Throat
    protected NamedBeanHandle<Sensor> sensorBNamed = null; // Continuing 
    protected NamedBeanHandle<Sensor> sensorCNamed = null; // diverging
    protected NamedBeanHandle<Sensor> sensorDNamed = null; // single or double crossover only
    
	public int type = RH_TURNOUT;
	public Object connectA = null;		// throat of LH, RH, RH Xover, LH Xover, and WYE turnouts
	public Object connectB = null;		// straight leg of LH and RH turnouts
	public Object connectC = null;		
	public Object connectD = null;		// double xover, RH Xover, LH Xover only
	public int continuingSense = Turnout.CLOSED;
	public boolean disabled = false;
    public boolean disableWhenOccupied = false;
	public Point2D center = new Point2D.Double(50.0,50.0);
	public Point2D dispB = new Point2D.Double(20.0,0.0);
	public Point2D dispC = new Point2D.Double(20.0,10.0);
    public Point2D pointA = new Point2D.Double(0,0);
    public Point2D pointB = new Point2D.Double(40,0);
    public Point2D pointC = new Point2D.Double(60,20);
    public Point2D pointD = new Point2D.Double(20,20);
    
    private int version = 1;
    
	public String linkedTurnoutName = ""; // name of the linked Turnout (as entered in tool)
	public int linkType = NO_LINK;
    
    private boolean hidden = false;
    
    private boolean useBlockSpeed = false;
    
    protected LayoutTurnout() {}
    
    public LayoutTurnout(String id, int t, Point2D c, double rot, 
								double xFactor, double yFactor, LayoutEditor myPanel) {
        this(id, t, c, rot, xFactor, yFactor, myPanel, 1);
    }
    
	/** 
	 * constructor method
	 */  
    public LayoutTurnout(String id, int t, Point2D c, double rot, 
								double xFactor, double yFactor, LayoutEditor myPanel, int v) {
		instance = this;
		namedTurnout = null;
		turnoutName = "";
		mTurnoutListener = null;
		disabled = false;
        disableWhenOccupied = false;
		block = null;
		blockName = "";
		layoutEditor = myPanel;
		ident = id;
		type = t;
		center = c;
        version = v;
		// adjust initial coordinates
        if (type==LH_TURNOUT) {
            dispB.setLocation(layoutEditor.getTurnoutBX(),0.0);
            dispC.setLocation(layoutEditor.getTurnoutCX(),-layoutEditor.getTurnoutWid());
        }
        else if (type==RH_TURNOUT) {
            dispB.setLocation(layoutEditor.getTurnoutBX(),0.0);
            dispC.setLocation(layoutEditor.getTurnoutCX(),layoutEditor.getTurnoutWid());
        }
        else if (type==WYE_TURNOUT) {
            dispB.setLocation(layoutEditor.getTurnoutBX(),0.5*layoutEditor.getTurnoutWid());
            dispC.setLocation(layoutEditor.getTurnoutBX(),-0.5*layoutEditor.getTurnoutWid());
        }
        else if (type==DOUBLE_XOVER) {
            if(version ==2){
                center = new Point2D.Double(layoutEditor.getXOverLong(),layoutEditor.getXOverHWid());
                pointB.setLocation(layoutEditor.getXOverLong()*2,0);
                pointC.setLocation(layoutEditor.getXOverLong()*2,(layoutEditor.getXOverHWid()*2));
                pointD.setLocation(0,(layoutEditor.getXOverHWid()*2));
                setCoordsCenter(c);
            } else {
                dispB.setLocation(layoutEditor.getXOverLong(),-layoutEditor.getXOverHWid());
                dispC.setLocation(layoutEditor.getXOverLong(),layoutEditor.getXOverHWid());
            }
            blockB = null;
            blockBName = "";
            blockC = null;
            blockCName = "";
            blockD = null;
            blockDName = "";
        }
        else if (type==RH_XOVER) {
            if(version ==2){
                center = new Point2D.Double(layoutEditor.getXOverLong(),layoutEditor.getXOverHWid());
                pointB.setLocation((layoutEditor.getXOverShort()+layoutEditor.getXOverLong()),0);
                pointC.setLocation(layoutEditor.getXOverLong()*2,(layoutEditor.getXOverHWid()*2));
                pointD.setLocation((center.getX()-layoutEditor.getXOverShort()),(layoutEditor.getXOverHWid()*2));
                setCoordsCenter(c);
            } else {
                dispB.setLocation(layoutEditor.getXOverShort(),-layoutEditor.getXOverHWid());
                dispC.setLocation(layoutEditor.getXOverLong(),layoutEditor.getXOverHWid());
            }
            blockB = null;
            blockBName = "";
            blockC = null;
            blockCName = "";
            blockD = null;
            blockDName = "";
        }
        else if (type==LH_XOVER) {
            if(version ==2){
                center = new Point2D.Double(layoutEditor.getXOverLong(),layoutEditor.getXOverHWid());
                pointA.setLocation(20,0);
                pointB.setLocation(60,0);
                pointC.setLocation(40,20);
                pointD.setLocation(0,20);
                
                pointA.setLocation((center.getX()-layoutEditor.getXOverShort()),0);
                pointB.setLocation((layoutEditor.getXOverLong()*2),0);
                pointC.setLocation(layoutEditor.getXOverLong()+layoutEditor.getXOverShort(),(layoutEditor.getXOverHWid()*2));
                pointD.setLocation(0, (layoutEditor.getXOverHWid()*2));
                
                
                setCoordsCenter(c);
            } else {
                dispB.setLocation(layoutEditor.getXOverLong(),-layoutEditor.getXOverHWid());
                dispC.setLocation(layoutEditor.getXOverShort(),layoutEditor.getXOverHWid());
            }
            blockB = null;
            blockBName = "";
            blockC = null;
            blockCName = "";
            blockD = null;
            blockDName = "";
        }
		rotateCoords(rot);
		// adjust size of new turnout
		Point2D pt = new Point2D.Double(round(dispB.getX()*xFactor),
										round(dispB.getY()*yFactor));
		dispB = pt;
		pt = new Point2D.Double(round(dispC.getX()*xFactor),
										round(dispC.getY()*yFactor));
		dispC = pt;		
	}
    
	private double round (double x) {
		int i = (int)(x+0.5);
		return i;
	}
		
	protected void rotateCoords(double rot) {
		// rotate coordinates
		double sineAng = Math.sin(rot*Math.PI/180.0);
		double cosineAng = Math.cos(rot*Math.PI/180.0);

        if(version == 2){
            pointA = rotatePoint(pointA, sineAng, cosineAng);
            pointB = rotatePoint(pointB, sineAng, cosineAng);
            pointC = rotatePoint(pointC, sineAng, cosineAng);
            pointD = rotatePoint(pointD, sineAng, cosineAng);
        } else {
            double x = (cosineAng*dispB.getX()) - (sineAng*dispB.getY());
            double y = (sineAng*dispB.getX()) + (cosineAng*dispB.getY());
            dispB = new Point2D.Double(x,y);
            x = (cosineAng*dispC.getX()) - (sineAng*dispC.getY());
            y = (sineAng*dispC.getX()) + (cosineAng*dispC.getY());
            dispC = new Point2D.Double(x,y);
        }
    }
    
    protected Point2D rotatePoint(Point2D p, double sineAng, double cosineAng){
        double cX = center.getX();
        double cY = center.getY();
        double x = cX + cosineAng * (p.getX()-cX) - sineAng * (p.getY()-cY);
        double y = cY + sineAng * (p.getX()-cX) + cosineAng * (p.getY()-cY);
        return new Point2D.Double(x,y);
    }

	/**
	 * Accessor methods
	*/
    public int getVersion() { return version;}
    public void setVersion(int v) { version = v; }
	public String getName() {return ident;}
    public boolean useBlockSpeed() { return useBlockSpeed; }
	public String getTurnoutName() {
        if (namedTurnout!=null)
            return namedTurnout.getName();
        return turnoutName;
    }
    public String getSecondTurnoutName() {
        if (secondNamedTurnout!=null)
            return secondNamedTurnout.getName();
        return secondTurnoutName;
    }
    
    public boolean getHidden() {return hidden;}
	public void setHidden(boolean hide) {hidden = hide;} 
	public String getBlockName() {return blockName;}
	public String getBlockBName() {return blockBName;}
	public String getBlockCName() {return blockCName;}
	public String getBlockDName() {return blockDName;}
    
    public String getSignalA1Name(){
        if(signalA1HeadNamed!=null)
            return signalA1HeadNamed.getName();
        return "";
    }
    
    public SignalHead getSignalA1(){
        if(signalA1HeadNamed!=null)
            return signalA1HeadNamed.getBean();
        return null;
    }
    
    public void setSignalA1Name(String signalHead){
        if(signalHead==null || signalHead.equals("")){
            signalA1HeadNamed=null;
            return;
        }
        
        SignalHead head = InstanceManager.signalHeadManagerInstance().getSignalHead(signalHead);
        if (head != null) {
            signalA1HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalA1HeadNamed=null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }
    
    public String getSignalA2Name(){
        if(signalA2HeadNamed!=null)
            return signalA2HeadNamed.getName();
        return "";
    }
    
    public SignalHead getSignalA2(){
        if(signalA2HeadNamed!=null)
            return signalA2HeadNamed.getBean();
        return null;
    }
    
    public void setSignalA2Name(String signalHead){
        if(signalHead==null || signalHead.equals("")){
            signalA2HeadNamed=null;
            return;
        }
        
        SignalHead head = InstanceManager.signalHeadManagerInstance().getSignalHead(signalHead);
        if (head != null) {
            signalA2HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalA2HeadNamed=null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }
    public String getSignalA3Name(){
        if(signalA3HeadNamed!=null)
            return signalA3HeadNamed.getName();
        return "";
    }
    
    public SignalHead getSignalA3(){
        if(signalA3HeadNamed!=null)
            return signalA3HeadNamed.getBean();
        return null;
    }
    
    public void setSignalA3Name(String signalHead){
        if(signalHead==null || signalHead.equals("")){
            signalA3HeadNamed=null;
            return;
        }
        
        SignalHead head = InstanceManager.signalHeadManagerInstance().getSignalHead(signalHead);
        if (head != null) {
            signalA3HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalA3HeadNamed=null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }
    
    public String getSignalB1Name(){
        if(signalB1HeadNamed!=null)
            return signalB1HeadNamed.getName();
        return "";
    }
    
    public SignalHead getSignalB1(){
        if(signalB1HeadNamed!=null)
            return signalB1HeadNamed.getBean();
        return null;
    }
    
    public void setSignalB1Name(String signalHead){
        if(signalHead==null || signalHead.equals("")){
            signalB1HeadNamed=null;
            return;
        }
        
        SignalHead head = InstanceManager.signalHeadManagerInstance().getSignalHead(signalHead);
        if (head != null) {
            signalB1HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalB1HeadNamed=null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }
    public String getSignalB2Name(){
        if(signalB2HeadNamed!=null)
            return signalB2HeadNamed.getName();
        return "";
    }
    
    public SignalHead getSignalB2(){
        if(signalB1HeadNamed!=null)
            return signalB2HeadNamed.getBean();
        return null;
    }
    
    public void setSignalB2Name(String signalHead){
        if(signalHead==null || signalHead.equals("")){
            signalB2HeadNamed=null;
            return;
        }
        
        SignalHead head = InstanceManager.signalHeadManagerInstance().getSignalHead(signalHead);
        if (head != null) {
            signalB2HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalB2HeadNamed=null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }
    
    public String getSignalC1Name(){
        if(signalC1HeadNamed!=null)
            return signalC1HeadNamed.getName();
        return "";
    }
    
    public SignalHead getSignalC1(){
        if(signalC1HeadNamed!=null)
            return signalC1HeadNamed.getBean();
        return null;
    }
    
    public void setSignalC1Name(String signalHead){
        if(signalHead==null || signalHead.equals("")){
            signalC1HeadNamed=null;
            return;
        }
        
        SignalHead head = InstanceManager.signalHeadManagerInstance().getSignalHead(signalHead);
        if (head != null) {
            signalC1HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalC1HeadNamed=null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }
    public String getSignalC2Name(){
        if(signalC2HeadNamed!=null)
            return signalC2HeadNamed.getName();
        return "";
    }
    
    public SignalHead getSignalC2(){
        if(signalC2HeadNamed!=null)
            return signalC2HeadNamed.getBean();
        return null;
    }
    
    public void setSignalC2Name(String signalHead){
        if(signalHead==null || signalHead.equals("")){
            signalC2HeadNamed=null;
            return;
        }
        
        SignalHead head = InstanceManager.signalHeadManagerInstance().getSignalHead(signalHead);
        if (head != null) {
            signalC2HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalC2HeadNamed=null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }
    
    public String getSignalD1Name(){
        if(signalD1HeadNamed!=null)
            return signalD1HeadNamed.getName();
        return "";
    }
    
    public SignalHead getSignalD1(){
        if(signalD1HeadNamed!=null)
            return signalD1HeadNamed.getBean();
        return null;
    }
    
    public void setSignalD1Name(String signalHead){
        if(signalHead==null || signalHead.equals("")){
            signalD1HeadNamed=null;
            return;
        }
        
        SignalHead head = InstanceManager.signalHeadManagerInstance().getSignalHead(signalHead);
        if (head != null) {
            signalD1HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalD1HeadNamed=null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }
    public String getSignalD2Name(){
        if(signalD2HeadNamed!=null)
            return signalD2HeadNamed.getName();
        return "";
    }
    
    public SignalHead getSignalD2(){
        if(signalD2HeadNamed!=null)
            return signalD2HeadNamed.getBean();
        return null;
    }
    
    public void setSignalD2Name(String signalHead){
        if(signalHead==null || signalHead.equals("")){
            signalD2HeadNamed=null;
            return;
        }
        
        SignalHead head = InstanceManager.signalHeadManagerInstance().getSignalHead(signalHead);
        if (head != null) {
            signalD2HeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalD2HeadNamed=null;
            log.error("Signal Head " + signalHead + " Not found for turnout " + getTurnoutName());
        }
    }
    
    public void removeBeanReference(jmri.NamedBean nb){
        if(nb==null)
            return;
        if(nb instanceof SignalMast){
            if(nb.equals(getSignalAMast())){
                setSignalAMast(null);
                return;
            }
            if(nb.equals(getSignalBMast())){
                setSignalBMast(null);
                return;
            }
            if(nb.equals(getSignalCMast())){
                setSignalCMast(null);
                return;
            }
            if(nb.equals(getSignalDMast())){
                setSignalDMast(null);
                return;
            }
        } else if(nb instanceof Sensor) {
            if(nb.equals(getSensorA())){
                setSensorA(null);
                return;
            }
            if(nb.equals(getSensorB())){
                setSensorB(null);
                return;
            }
            if(nb.equals(getSensorC())){
                setSensorC(null);
                return;
            }
            if(nb.equals(getSensorB())){
                setSensorD(null);
                return;
            }
        } else if(nb instanceof SignalHead) {
            if(nb.equals(getSignalA1())){
                setSignalA1Name(null);
            }
            if(nb.equals(getSignalA2())){
                setSignalA2Name(null);
            }
            if(nb.equals(getSignalA3())){
                setSignalA3Name(null);
            }
            if(nb.equals(getSignalB1())){
                setSignalB1Name(null);
            }
            if(nb.equals(getSignalB2())){
                setSignalB2Name(null);
            }
            if(nb.equals(getSignalC1())){
                setSignalC1Name(null);
            }
            if(nb.equals(getSignalC2())){
                setSignalC2Name(null);
            }
            if(nb.equals(getSignalD1())){
                setSignalD1Name(null);
            }
            if(nb.equals(getSignalD2())){
                setSignalD2Name(null);
            }
        }
    }
    
    public String getSignalAMastName(){
        if(signalAMastNamed!=null)
            return signalAMastNamed.getName();
        return "";
    }
    
    public SignalMast getSignalAMast(){
        if(signalAMastNamed!=null)
            return signalAMastNamed.getBean();
        return null;
    }
    
	public void setSignalAMast(String signalMast){
        if(signalMast==null || signalMast.equals("")){
            signalAMastNamed=null;
            return;
        }
        
        SignalMast mast = InstanceManager.signalMastManagerInstance().getSignalMast(signalMast);
        if (mast != null) {
            signalAMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            signalAMastNamed=null;
            log.error("Signal Mast " + signalMast + " Not found for turnout " + getTurnoutName());
        }
    }
    
	public String getSignalBMastName() {
        if(signalBMastNamed!=null)
            return signalBMastNamed.getName();
        return "";
    }
    
    public SignalMast getSignalBMast(){
        if(signalBMastNamed!=null)
            return signalBMastNamed.getBean();
        return null;
    }
    
	public void setSignalBMast(String signalMast){
        if(signalMast==null || signalMast.equals("")){
            signalBMastNamed=null;
            return;
        }
        
        SignalMast mast = InstanceManager.signalMastManagerInstance().getSignalMast(signalMast);
        if (mast != null) {
            signalBMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            signalBMastNamed=null;
            log.error("Signal Mast " + signalMast + " Not found for turnout " + getTurnoutName());
        }
    }
    
	public String getSignalCMastName() {
        if(signalCMastNamed!=null)
            return signalCMastNamed.getName();
        return "";
    }
    
    public SignalMast getSignalCMast(){
        if(signalCMastNamed!=null)
            return signalCMastNamed.getBean();
        return null;
    }
    
	public void setSignalCMast(String signalMast){
        if(signalMast==null || signalMast.equals("")){
            signalCMastNamed=null;
            return;
        }
        
        SignalMast mast = InstanceManager.signalMastManagerInstance().getSignalMast(signalMast);
        if (mast != null) {
            signalCMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            log.error("Signal Mast " + signalMast + " Not found for turnout " + getTurnoutName());
            signalCMastNamed=null;
        }
    }
    
	public String getSignalDMastName() {
        if(signalDMastNamed!=null)
            return signalDMastNamed.getName();
        return "";
    }
    
    public SignalMast getSignalDMast(){
        if(signalDMastNamed!=null)
            return signalDMastNamed.getBean();
        return null;
    }
    
	public void setSignalDMast(String signalMast){
        if(signalMast==null || signalMast.equals("")){
            signalDMastNamed=null;
            return;
        }
        
        SignalMast mast = InstanceManager.signalMastManagerInstance().getSignalMast(signalMast);
        if (mast != null) {
            signalDMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            log.error("Signal Mast " + signalMast + " Not found for turnout " + getTurnoutName());
            signalDMastNamed=null;
        }
    }
    
    public String getSensorAName() {
        if(sensorANamed!=null)
            return sensorANamed.getName();
        return "";
    }
    
    public Sensor getSensorA(){
        if(sensorANamed!=null)
            return sensorANamed.getBean();
        return null;
    }
    
	public void setSensorA(String sensorName) {
        if(sensorName==null || sensorName.equals("")){
            sensorANamed=null;
            return;
        }
        
        Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        if (sensor != null) {
            sensorANamed =InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } else {
            sensorANamed=null;
        }
    }
    
	public String getSensorBName() {
        if(sensorBNamed!=null)
            return sensorBNamed.getName();
        return "";
    }
    
    public Sensor getSensorB(){
        if(sensorBNamed!=null)
            return sensorBNamed.getBean();
        return null;
    }
    
	public void setSensorB(String sensorName) {
        if(sensorName==null || sensorName.equals("")){
            sensorBNamed=null;
            return;
        }
        
        Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        if (sensor != null) {
            sensorBNamed =InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } else {
            sensorBNamed=null;
        }
    }
    
	public String getSensorCName() {
        if(sensorCNamed!=null)
            return sensorCNamed.getName();
        return "";
    }
    
    public Sensor getSensorC(){
        if(sensorCNamed!=null)
            return sensorCNamed.getBean();
        return null;
    }
    
	public void setSensorC(String sensorName) {
        if(sensorName==null || sensorName.equals("")){
            sensorCNamed=null;
            return;
        }
        
        Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        if (sensor != null) {
            sensorCNamed =InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } else {
            sensorCNamed=null;
        }
    }
    
	public String getSensorDName() {
        if(sensorDNamed!=null)
            return sensorDNamed.getName();
        return "";
    }
    
    public Sensor getSensorD(){
        if(sensorDNamed!=null)
            return sensorDNamed.getBean();
        return null;
    }
    
	public void setSensorD(String sensorName) {
        if(sensorName==null || sensorName.equals("")){
            sensorDNamed=null;
            return;
        }
        
        Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        if (sensor != null) {
            sensorDNamed =InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } else {
            sensorDNamed=null;
        }
    }
    
	public String getLinkedTurnoutName() {return linkedTurnoutName;}
	public void setLinkedTurnoutName(String s) {linkedTurnoutName = s;}  //Could be done with changing over to a NamedBeanHandle
    
	public int getLinkType() {return linkType;}
	public void setLinkType(int type) {linkType = type;}
	public int getTurnoutType() {return type;}
	public Object getConnectA() {return connectA;}
	public Object getConnectB() {return connectB;}
	public Object getConnectC() {return connectC;}
	public Object getConnectD() {return connectD;}
    
	public Turnout getTurnout() {
		if (namedTurnout==null) {
			// set physical turnout if possible and needed
			setTurnout(turnoutName);
            if (namedTurnout==null)
                return null;
		}
		return namedTurnout.getBean();
	}
    
	public int getContinuingSense() {return continuingSense;}
	
    public void setTurnout(String tName) {
		if (namedTurnout!=null) deactivateTurnout();
		turnoutName = tName;
        Turnout turnout = null;
        if(turnoutName!=null && !turnoutName.equals("")){
            turnout =InstanceManager.turnoutManagerInstance().
                            getTurnout(turnoutName);
        }
		if (turnout!=null) {
            namedTurnout =InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutName, turnout);
			activateTurnout();
		}
		else {
			turnoutName = "";
            namedTurnout = null;
		}
	}
    
    public Turnout getSecondTurnout() {
		if (secondNamedTurnout==null) {
			// set physical turnout if possible and needed
			setSecondTurnout(secondTurnoutName);
            
            if (secondNamedTurnout==null){
                return null;
            }
		}
		return secondNamedTurnout.getBean();
	}
    
    public void setSecondTurnout(String tName) {
        
        if(tName!=null && tName.equals(secondTurnoutName)){
            return;
        }
        
		if (secondNamedTurnout!=null) deactivateTurnout();
        String oldSecondTurnoutName = secondTurnoutName;
		secondTurnoutName = tName;
        Turnout turnout = null;
        if(tName!=null){
            turnout =InstanceManager.turnoutManagerInstance().
                            getTurnout(secondTurnoutName);
        }
		if (turnout!=null) {
            secondNamedTurnout =InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(secondTurnoutName, turnout);
			activateTurnout();
		}
		else {
			secondTurnoutName = "";
            secondNamedTurnout = null;
		}
        if ( (type == RH_TURNOUT) || (type ==LH_TURNOUT) || (type == WYE_TURNOUT) ){
            if(oldSecondTurnoutName!=null && !oldSecondTurnoutName.equals("")){
                Turnout oldTurnout =InstanceManager.turnoutManagerInstance().
                            getTurnout(oldSecondTurnoutName);
                LayoutTurnout oldLinked = layoutEditor.getFinder().findLayoutTurnoutByTurnoutName(oldTurnout.getSystemName());
                if(oldLinked==null)
                    oldLinked = layoutEditor.getFinder().findLayoutTurnoutByTurnoutName(oldTurnout.getUserName());
                if((oldLinked!=null) && oldLinked.getSecondTurnout()==getTurnout())
                    oldLinked.setSecondTurnout(null);
            }
            if(turnout!=null){
                LayoutTurnout newLinked = layoutEditor.getFinder().findLayoutTurnoutByTurnoutName(turnout.getSystemName());
                if(newLinked==null)
                    newLinked = layoutEditor.getFinder().findLayoutTurnoutByTurnoutName(turnout.getUserName());
                if(newLinked!=null){
                    newLinked.setSecondTurnout(turnoutName);
                }
            }
        }
	}
    
	public void setContinuingSense(int sense) {continuingSense=sense;}
	public void setDisabled(boolean state) {disabled = state;}
	public boolean isDisabled() {return disabled;}
    
    public void setDisableWhenOccupied(boolean state) {disableWhenOccupied = state;}
	public boolean isDisabledWhenOccupied() {return disableWhenOccupied;}
    
    public Object getConnection(int location) throws jmri.JmriException {
        switch (location) {
            case LayoutEditor.TURNOUT_A: return connectA;
            case LayoutEditor.TURNOUT_B: return connectB;
            case LayoutEditor.TURNOUT_C: return connectC;
            case LayoutEditor.TURNOUT_D: return connectD;
        }
        log.error("Invalid Point Type " + location); //I18IN
        throw new jmri.JmriException("Invalid Point");
    }
    
    public void setConnection(int location, Object o, int type) throws jmri.JmriException {
        if ( (type!=LayoutEditor.TRACK) && (type!=LayoutEditor.NONE) ) {
			log.error("unexpected type of connection to layoutturnout - "+type);
            throw new jmri.JmriException("unexpected type of connection to layoutturnout - "+type);
		}
        switch (location) {
            case LayoutEditor.TURNOUT_A: connectA = o;
                                         break;
            case LayoutEditor.TURNOUT_B: connectB = o;
                                        break;
            case LayoutEditor.TURNOUT_C: connectC=o;
                                        break;
            case LayoutEditor.TURNOUT_D: connectD=o;
                                        break;
            default : log.error("Invalid Point Type " + location); //I18IN
                throw new jmri.JmriException("Invalid Point");
        }
    }
    
	public void setConnectA(Object o,int type) {
		connectA = o;
		if ( (type!=LayoutEditor.TRACK) && (type!=LayoutEditor.NONE) ) {
			log.error("unexpected type of A connection to layoutturnout - "+type);
		}
	}
	public void setConnectB(Object o,int type) {
		connectB = o;
		if ( (type!=LayoutEditor.TRACK) && (type!=LayoutEditor.NONE) ) {
			log.error("unexpected type of B connection to layoutturnout - "+type);
		}
	}
	public void setConnectC(Object o,int type) {
		connectC = o;
		if ( (type!=LayoutEditor.TRACK) && (type!=LayoutEditor.NONE) ) {
			log.error("unexpected type of C connection to layoutturnout - "+type);
		}
	}
	public void setConnectD(Object o,int type) {
		connectD = o;
		if ( (type!=LayoutEditor.TRACK) && (type!=LayoutEditor.NONE) ) {
			log.error("unexpected type of D connection to layoutturnout - "+type);
		}
	}
	public LayoutBlock getLayoutBlock() {return block;}
	public LayoutBlock getLayoutBlockB() {
			if (blockB!=null) return blockB;
			return block;
	}
	public LayoutBlock getLayoutBlockC() {
			if (blockC!=null) return blockC;
			return block;
	}
	public LayoutBlock getLayoutBlockD(){
			if (blockD!=null) return blockD;
			return block;
	}
	public Point2D getCoordsCenter() {return center;}
	public Point2D getCoordsA() {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
            if (version == 2){
                return pointA;
            }
			double x = center.getX() - dispC.getX();
			double y = center.getY() - dispC.getY();
			return new Point2D.Double(x,y);
		}
		else if (type==WYE_TURNOUT) {
			double x = center.getX() - (0.5*(dispB.getX() + dispC.getX()));
			double y = center.getY() - (0.5*(dispB.getY() + dispC.getY()));
			return new Point2D.Double(x,y);
		}
		else {
			double x = center.getX() - dispB.getX();
			double y = center.getY() - dispB.getY();
			return new Point2D.Double(x,y);
		}
	}
	public Point2D getCoordsB() {
        if ((version == 2) && ((type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER))){
            return pointB;
        }
		double x = center.getX() + dispB.getX();
		double y = center.getY() + dispB.getY();
		return new Point2D.Double(x,y);
	}
	public Point2D getCoordsC() {
        if ((version == 2) && ((type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER))){
            return pointC;
        }
		double x = center.getX() + dispC.getX();
		double y = center.getY() + dispC.getY();
		return new Point2D.Double(x,y);
	}
	public Point2D getCoordsD() {
        if ((version == 2) && ((type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER))){
            return pointD;
        }
		// only allowed for single and double crossovers
		double x = center.getX() - dispB.getX();
		double y = center.getY() - dispB.getY();
		return new Point2D.Double(x,y);
	}

	// updates connectivity for blocks assigned to this turnout and connected track segments
	private void updateBlockInfo() {
		LayoutBlock bA = null;
		LayoutBlock bB = null;
		LayoutBlock bC = null;
		LayoutBlock bD = null;
		layoutEditor.auxTools.setBlockConnectivityChanged();
		if (block!=null) block.updatePaths();
		if (connectA!=null) {
			bA = ((TrackSegment)connectA).getLayoutBlock();
			if ((bA!=null) && (bA!=block)) bA.updatePaths();
		}
		if ((blockB!=null) && (blockB!=block) && (blockB!=bA)) blockB.updatePaths();
		if (connectB!=null) {
			bB = ((TrackSegment)connectB).getLayoutBlock();
			if ((bB!=null) && (bB!=block) && (bB!=bA) && (bB!=blockB)) bB.updatePaths();
		}
		if ((blockC!=null) && (blockC!=block) && (blockC!=bA) &&
				(blockC!=bB) && (blockC!=blockB)) blockC.updatePaths();
		if (connectC!=null) {
			bC = ((TrackSegment)connectC).getLayoutBlock();
			if ((bC!=null) && (bC!=block) && (bC!=bA) && (bC!=blockB) && (bC!=bB) &&
					(bC!=blockC)) bC.updatePaths();
		}
		if ((blockD!=null) && (blockD!=block) && (blockD!=bA) &&
				(blockD!=bB) && (blockD!=blockB) && (blockD!=bC) &&
					(blockD!=blockC)) blockD.updatePaths();
		if (connectD!=null) {
			bD = ((TrackSegment)connectD).getLayoutBlock();
			if ((bD!=null) && (bD!=block) && (bD!=bA) && (bD!=blockB) && (bD!=bB) &&
				(bD!=blockC) && (bD!=bC) && (bD!=blockD)) bD.updatePaths();
		}
	}	
	
	/**
	 * Set default size parameters to correspond to this turnout's size
	 */
	private void setUpDefaultSize() {
		// remove the overall scale factor
		double bX = dispB.getX()/layoutEditor.getXScale();
		double bY = dispB.getY()/layoutEditor.getYScale();
		double cX = dispC.getX()/layoutEditor.getXScale();
		double cY = dispC.getY()/layoutEditor.getYScale();
		// calculate default parameters according to type of turnout
		double lenB = Math.sqrt((bX*bX) + (bY*bY));
		double lenC = Math.sqrt((cX*cX) + (cY*cY));
		double distBC = Math.sqrt(((bX-cX)*(bX-cX)) + ((bY-cY)*(bY-cY)));
		if ( (type == LH_TURNOUT) || (type == RH_TURNOUT) ) {
			layoutEditor.setTurnoutBX(round(lenB+0.1));
			double xc = ((bX*cX)+(bY*cY))/lenB;
			layoutEditor.setTurnoutCX(round(xc+0.1));
			layoutEditor.setTurnoutWid(round(Math.sqrt((lenC*lenC)-(xc*xc))+0.1));
		}
		else if (type == WYE_TURNOUT) {
			double xx = Math.sqrt((lenB*lenB)-(0.25*(distBC*distBC)));
			layoutEditor.setTurnoutBX(round(xx+0.1));
			layoutEditor.setTurnoutCX(round(xx+0.1));
			layoutEditor.setTurnoutWid(round(distBC+0.1));
		}
		else {
            if(version == 2){
                double aX = pointA.getX()/layoutEditor.getXScale();
                double aY = pointA.getY()/layoutEditor.getYScale();
                bX = pointB.getX()/layoutEditor.getXScale();
                bY = pointB.getY()/layoutEditor.getYScale();
                cX = pointC.getX()/layoutEditor.getXScale();
                cY = pointC.getY()/layoutEditor.getYScale();
                double lenAB = Math.sqrt(((bX-aX)*(bX-aX))+((bY-aY)*(bY-aY)));
                if (type == DOUBLE_XOVER){
                    double lenBC = Math.sqrt(((bX-cX)*(bX-cX))+((bY-cY)*(bY-cY)));
                    layoutEditor.setXOverLong(round(lenAB/2)); //set to half to be backwardly compatible
                    layoutEditor.setXOverHWid(round(lenBC/2));
                    layoutEditor.setXOverShort(round((0.5*lenAB)/2));
                }
                else if (type == RH_XOVER) {
                    lenAB = lenAB/3;
                    layoutEditor.setXOverShort(round(lenAB));
                    layoutEditor.setXOverLong(round(lenAB*2));
                    double opp = (aY-bY);
                    double ang = Math.asin(opp/(lenAB*3));
                    opp = Math.sin(ang)*lenAB;
                    bY = bY+opp;
                    double adj = Math.cos(ang)*lenAB;
                    bX = bX+adj;
                    double lenBC = Math.sqrt(((bX-cX)*(bX-cX))+((bY-cY)*(bY-cY)));
                    layoutEditor.setXOverHWid(round(lenBC/2));
                    
                }
                else if (type == LH_XOVER) {
                    double dY = pointD.getY()/layoutEditor.getYScale();
                    lenAB = lenAB/3;
                    layoutEditor.setXOverShort(round(lenAB));
                    layoutEditor.setXOverLong(round(lenAB*2));
                    double opp = (dY-cY);
                    double ang = Math.asin(opp/(lenAB*3)); //Lenght of AB should be the same as CD
                    opp = Math.sin(ang)*lenAB;
                    cY = cY+opp;
                    double adj = Math.cos(ang)*lenAB;
                    cX = cX+adj;
                    double lenBC = Math.sqrt(((bX-cX)*(bX-cX))+((bY-cY)*(bY-cY)));
                    layoutEditor.setXOverHWid(round(lenBC/2));
                }
            } 
            else if (type == DOUBLE_XOVER) {
                double lng = Math.sqrt((lenB*lenB)-(0.25*(distBC*distBC)));
                layoutEditor.setXOverLong(round(lng+0.1));			
                layoutEditor.setXOverHWid(round((0.5*distBC)+0.1));
                layoutEditor.setXOverShort(round((0.5*lng)+0.1));
            }
            else if (type == RH_XOVER) {
                double distDC = Math.sqrt(((bX+cX)*(bX+cX)) + ((bY+cY)*(bY+cY)));
                layoutEditor.setXOverShort(round((0.25*distDC)+0.1));
                layoutEditor.setXOverLong(round((0.75*distDC)+0.1));
                double hwid = Math.sqrt((lenC*lenC)-(0.5625*distDC*distDC));
                layoutEditor.setXOverHWid(round(hwid+0.1));
            }
            else if (type == LH_XOVER) {
                double distDC = Math.sqrt(((bX+cX)*(bX+cX)) + ((bY+cY)*(bY+cY)));
                layoutEditor.setXOverShort(round((0.25*distDC)+0.1));
                layoutEditor.setXOverLong(round((0.75*distDC)+0.1));
                double hwid = Math.sqrt((lenC*lenC)-(0.0625*distDC*distDC));
                layoutEditor.setXOverHWid(round(hwid+0.1));
            }
        }
	}

	/**
	 * Set Up a Layout Block(s) for this Turnout
	 */
	public void setLayoutBlock (LayoutBlock b) {
		block = b;
		if (b!=null) blockName = b.getID();
		else blockName = "";
	}
	public void setLayoutBlockB (LayoutBlock b) {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			blockB = b;
			if (b!=null) blockBName = b.getID();
			else blockBName = "";
		}
		else {
			log.error ("Attempt to set block B, but not a crossover");
		}
	}
	public void setLayoutBlockC (LayoutBlock b) {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			blockC = b;
			if (b!=null) blockCName = b.getID();
			else blockCName = "";
		}
		else {
			log.error ("Attempt to set block C, but not a crossover");
		}
	}
	public void setLayoutBlockD (LayoutBlock b) {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			blockD = b;
			if (b!=null) blockDName = b.getID();
			else blockDName = "";
		}
		else {
			log.error ("Attempt to set block D, but not a crossover");
		}
	}
	public void setLayoutBlockByName (String name) {
		blockName = name;
	}
	public void setLayoutBlockBByName (String name) {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			blockBName = name;
		}
		else {
			log.error ("Attempt to set block B name, but not a crossover");
		}
	}
	public void setLayoutBlockCByName (String name) {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			blockCName = name;
		}
		else {
			log.error ("Attempt to set block C name, but not a crossover");
		}
	}
	public void setLayoutBlockDByName (String name) {
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			blockDName = name;
		}
		else {
			log.error ("Attempt to set block D name, but not a crossover");
		}
	}
	
	/** 
	 * Methods to test if turnout legs are mainline track or not
	 *  Returns true if connecting track segment is mainline
	 *  Defaults to not mainline if connecting track segment is missing
	 */
	public boolean isMainlineA() {
		if (connectA != null) 
			return ((TrackSegment)connectA).getMainline();
		else {
			// if no connection, depends on type of turnout
			if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
				// All crossovers - straight continuing is B
				if (connectB != null) 
					return ((TrackSegment)connectB).getMainline();
			}
			// must be RH, LH, or WYE turnout - A is the switch throat
			else if ( ((connectB != null) && 
					(((TrackSegment)connectB).getMainline())) ||
						((connectC != null) && 
							(((TrackSegment)connectC).getMainline())) )
				return true;	
		}	
		return false;
	}
	public boolean isMainlineB() {
		if (connectB != null) 
			return ((TrackSegment)connectB).getMainline();
		else {
			// if no connection, depends on type of turnout
			if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
				// All crossovers - straight continuing is A
				if (connectA != null) 
					return ((TrackSegment)connectA).getMainline();
			}
			// must be RH, LH, or WYE turnout - A is the switch throat,
			//		B is normally the continuing straight
			else if (continuingSense == Turnout.CLOSED) {
				// user hasn't changed the continuing turnout state 
				if (connectA != null) 
					// if throat is mainline, this leg must be also 
					return ((TrackSegment)connectA).getMainline();
			}
		}	
		return false;
	}
	public boolean isMainlineC() {
		if (connectC != null) 
			return ((TrackSegment)connectC).getMainline();
		else {
			// if no connection, depends on type of turnout
			if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
				// All crossovers - straight continuing is D
				if (connectD != null) 
					return ((TrackSegment)connectD).getMainline();
			}
			// must be RH, LH, or WYE turnout - A is the switch throat,
			//		B is normally the continuing straight
			else if (continuingSense == Turnout.THROWN) {
				// user has changed the continuing turnout state 
				if (connectA != null) 
					// if throat is mainline, this leg must be also 
					return ((TrackSegment)connectA).getMainline();
			}
		}	
		return false;
	}
	public boolean isMainlineD() {
		// this is a crossover turnout
		if (connectD != null) 
			return ((TrackSegment)connectD).getMainline();
		else if (connectC != null) 
			return ((TrackSegment)connectC).getMainline();
		return false;
	}
	
	/**
	 * Modify coordinates methods
	 */
	public void setCoordsCenter(Point2D p) {
        if(version == 2) {
            Point2D oldC = center;
            double offsety = oldC.getY()-p.getY();
            double offsetx = oldC.getX()-p.getX();
            pointA = new Point2D.Double(pointA.getX()-offsetx, pointA.getY()-offsety);
            pointB = new Point2D.Double(pointB.getX()-offsetx, pointB.getY()-offsety);
            pointC = new Point2D.Double(pointC.getX()-offsetx, pointC.getY()-offsety);
            pointD = new Point2D.Double(pointD.getX()-offsetx, pointD.getY()-offsety);
        }
		center = p;
	}
    
    private void reCalculateCenter(){
        double centreX = (pointC.getX()-pointA.getX())/2;
        double centreY = (pointC.getY()-pointA.getY())/2;
        centreX = pointA.getX()+centreX;
        centreY = pointA.getY()+centreY;
        center = new Point2D.Double(centreX,centreY);
    }
    
	public void setCoordsA(Point2D p) {
        pointA = p;
        if(version == 2)
            reCalculateCenter();
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		if (type == DOUBLE_XOVER) {
			dispC = new Point2D.Double(x,y);
			// adjust to maintain rectangle
			double oldLength = Math.sqrt( (dispB.getX()*dispB.getX()) + 
													(dispB.getY()*dispB.getY()) );
			double newLength = Math.sqrt( (x*x) + (y*y) );
			x = dispB.getX()*newLength/oldLength;
			y = dispB.getY()*newLength/oldLength;
			dispB = new Point2D.Double(x,y);
		}
		else if ( (type == RH_XOVER) || (type == LH_XOVER) ) {
			dispC = new Point2D.Double(x,y);
			// adjust to maintain the parallelogram
			double a = 0.0;
			double b = -y;
			double xi = 0.0;
			double yi = b;
			if ((dispB.getX() + x)!=0.0) {
				a = (dispB.getY() + y)/(dispB.getX() + x);
				b = -y + (a*x);
				xi = -b/(a + (1.0/a));
				yi = (a*xi) + b;
			}
			if (type == RH_XOVER) {
				x = xi - (0.333333*(-x - xi));
				y = yi - (0.333333*(-y - yi));
			}
			else if (type == LH_XOVER) {
				x = xi - (3.0*(-x - xi));
				y = yi - (3.0*(-y - yi));
			}
			dispB = new Point2D.Double(x,y);
		}
		else if (type == WYE_TURNOUT) {
			// modify both to maintain same angle at wye
			double temX = (dispB.getX() + dispC.getX());
			double temY = (dispB.getY() + dispC.getY());
			double temXx = (dispB.getX() - dispC.getX());
			double temYy = (dispB.getY() - dispC.getY());
			double tan = Math.sqrt( ((temX*temX)+(temY*temY))/
								((temXx*temXx)+(temYy*temYy)) );
			double xx = x + (y/tan);
			double yy = y - (x/tan);
			dispC = new Point2D.Double(xx,yy);
			xx = x - (y/tan);
			yy = y + (x/tan);
			dispB = new Point2D.Double(xx,yy);
		}
		else {
			dispB = new Point2D.Double(x,y);
		}
	}
	public void setCoordsB(Point2D p) {
        pointB = p;
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispB = new Point2D.Double(-x,-y);
		if ((type == DOUBLE_XOVER) || (type == WYE_TURNOUT)) {
			// adjust to maintain rectangle or wye shape
			double oldLength = Math.sqrt( (dispC.getX()*dispC.getX()) + 
													(dispC.getY()*dispC.getY()) );
			double newLength = Math.sqrt( (x*x) + (y*y) );
			x = dispC.getX()*newLength/oldLength;
			y = dispC.getY()*newLength/oldLength;
			dispC = new Point2D.Double(x,y);
		}
		else if ( (type == RH_XOVER) || (type == LH_XOVER) ) {
			// adjust to maintain the parallelogram
			double a = 0.0;
			double b = y;
			double xi = 0.0;
			double yi = b;
			if ((dispC.getX() - x)!=0.0) {
				a = (dispC.getY() - y)/(dispC.getX() - x);
				b = y - (a*x);
				xi = -b/(a + (1.0/a));
				yi = (a*xi) + b;
			}
			if (type == LH_XOVER) {
				x = xi - (0.333333*(x - xi));
				y = yi - (0.333333*(y - yi));
			}
			else if (type == RH_XOVER) {
				x = xi - (3.0*(x - xi));
				y = yi - (3.0*(y - yi));
			}
			dispC = new Point2D.Double(x,y);
		}
	}
	public void setCoordsC(Point2D p) {
        pointC = p;
        if(version == 2)
            reCalculateCenter();
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispC = new Point2D.Double(-x,-y);
		if ((type == DOUBLE_XOVER) || (type == WYE_TURNOUT)) {
			// adjust to maintain rectangle or wye shape
			double oldLength = Math.sqrt( (dispB.getX()*dispB.getX()) + 
													(dispB.getY()*dispB.getY()) );
			double newLength = Math.sqrt( (x*x) + (y*y) );
			x = dispB.getX()*newLength/oldLength;
			y = dispB.getY()*newLength/oldLength;
			dispB = new Point2D.Double(x,y);
		}
		else if ( (type == RH_XOVER) || (type == LH_XOVER) ) {
			double a = 0.0;
			double b = -y;
			double xi = 0.0;
			double yi = b;
			if ((dispB.getX() + x)!=0.0) {
				a = (-dispB.getY() + y)/(-dispB.getX() + x);
				b = -y + (a*x);
				xi = -b/(a + (1.0/a));
				yi = (a*xi) + b;
			}
			if (type == RH_XOVER) {
				x = xi - (0.333333*(-x - xi));
				y = yi - (0.333333*(-y - yi));
			}
			else if (type == LH_XOVER) {
				x = xi - (3.0*(-x - xi));
				y = yi - (3.0*(-y - yi));
			}
			dispB = new Point2D.Double(-x,-y);
		}
	}
	public void setCoordsD(Point2D p) {
        pointD = p;

		// only used for crossovers
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispB = new Point2D.Double(x,y);
		if (type == DOUBLE_XOVER) {
			// adjust to maintain rectangle
			double oldLength = Math.sqrt( (dispC.getX()*dispC.getX()) + 
													(dispC.getY()*dispC.getY()) );
			double newLength = Math.sqrt( (x*x) + (y*y) );
			x = dispC.getX()*newLength/oldLength;
			y = dispC.getY()*newLength/oldLength;
			dispC = new Point2D.Double(x,y);
		}
		else if ( (type == RH_XOVER) || (type == LH_XOVER) ) {
			// adjust to maintain the parallelogram
			double a = 0.0;
			double b = y;
			double xi = 0.0;
			double yi = b;
			if ((dispC.getX() + x)!=0.0) {
				a = (dispC.getY() + y)/(dispC.getX() + x);
				b = -y + (a*x);
				xi = -b/(a + (1.0/a));
				yi = (a*xi) + b;
			}
			if (type == LH_XOVER) {
				x = xi - (0.333333*(-x - xi));
				y = yi - (0.333333*(-y - yi));
			}
			else if (type == RH_XOVER) {
				x = xi - (3.0*(-x - xi));
				y = yi - (3.0*(-y - yi));
			}
			dispC = new Point2D.Double(x,y);
		}
	}	
	public void scaleCoords(float xFactor, float yFactor) {
		Point2D pt = new Point2D.Double(round(center.getX()*xFactor),
										round(center.getY()*yFactor));
		center = pt;

        if(version == 2){
            pointA = new Point2D.Double(round(pointA.getX()*xFactor),
                                        round(pointA.getY()*yFactor));
            pointB = new Point2D.Double(round(pointB.getX()*xFactor),
                                        round(pointB.getY()*yFactor));
            pointC = new Point2D.Double(round(pointC.getX()*xFactor),
                                        round(pointC.getY()*yFactor));
            pointD = new Point2D.Double(round(pointD.getX()*xFactor),
                                        round(pointD.getY()*yFactor));
        } else {
            pt = new Point2D.Double(round(dispB.getX()*xFactor),
                                            round(dispB.getY()*yFactor));
            dispB = pt;
            pt = new Point2D.Double(round(dispC.getX()*xFactor),
                                            round(dispC.getY()*yFactor));
            dispC = pt;        
        }
	}
	
	/**
	 * Activate/Deactivate turnout to redraw when turnout state changes
	 */
	private void activateTurnout() {
		if (namedTurnout!=null) {
			namedTurnout.getBean().addPropertyChangeListener(mTurnoutListener =
								new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					layoutEditor.redrawPanel();
				}
			}, namedTurnout.getName(), "Layout Editor Turnout");
		}
		if (secondNamedTurnout!=null) {
			secondNamedTurnout.getBean().addPropertyChangeListener(mTurnoutListener, secondNamedTurnout.getName(), "Layout Editor Turnout");
		}
	}
	private void deactivateTurnout() {
		if (mTurnoutListener!=null) {
			namedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
            if(secondNamedTurnout!=null){
                secondNamedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
            }
			mTurnoutListener = null;
		}
	}

	/**
	 * Toggle turnout if clicked on, physical turnout exists, and
	 *    not disabled
	 */
	public void toggleTurnout() {
        // toggle turnout
        if (getTurnout().getKnownState()==jmri.Turnout.CLOSED){
            setState(jmri.Turnout.THROWN);
            /*if(getSecondTurnout()!=null)
                getSecondTurnout().setState(jmri.Turnout.THROWN);*/
        }
        else {
            setState(jmri.Turnout.CLOSED);
            /*if(getSecondTurnout()!=null)
                getSecondTurnout().setState(jmri.Turnout.CLOSED);*/
            
        }
    }
    
    public void setState(int state){
        if ((getTurnout()!=null) && (!disabled)) {
            if (disableWhenOccupied){
                if(disableOccupiedTurnout()){
                    log.debug("Turnout not changed as Block is Occupied");
                    return;
                }
            }
            getTurnout().setCommandedState(state);
            if(getSecondTurnout()!=null){
                getSecondTurnout().setCommandedState(state);
            }
            
            
        }
    }
    
    private boolean disableOccupiedTurnout(){
        if ((type==RH_TURNOUT) || (type==LH_TURNOUT) || (type==WYE_TURNOUT)){
            if(block.getOccupancy()==LayoutBlock.OCCUPIED){
                log.debug("Block " + blockName + "is Occupied");
                return true;
            }
        }
        if ((type==DOUBLE_XOVER)||(type==RH_XOVER)||(type==LH_XOVER)){
            //If the turnout is set for straigh over, we need to deal with the straight over connecting blocks
            if (getTurnout().getKnownState()==jmri.Turnout.CLOSED){
                if ((block.getOccupancy()==LayoutBlock.OCCUPIED) && (blockB.getOccupancy()==LayoutBlock.OCCUPIED)){
                    log.debug("Blocks " + blockName + " & " + blockBName + " are Occupied");
                    return true;
                }
                if ((blockC.getOccupancy()==LayoutBlock.OCCUPIED) && (blockD.getOccupancy()==LayoutBlock.OCCUPIED)){
                    log.debug("Blocks " + blockCName + " & " + blockDName + " are Occupied");
                    return true;
                }
            }
        
        }
        if ((type==DOUBLE_XOVER)||(type==LH_XOVER)){
            if (getTurnout().getKnownState()==jmri.Turnout.THROWN){
                if ((blockB.getOccupancy()==LayoutBlock.OCCUPIED) && (blockD.getOccupancy()==LayoutBlock.OCCUPIED)){
                    log.debug("Blocks " + blockBName + " & " + blockDName + " are Occupied");
                    return true;
                }
            }
        }
        
        if ((type==DOUBLE_XOVER)||(type==RH_XOVER)){
            if (getTurnout().getKnownState()==jmri.Turnout.THROWN){
                if ((block.getOccupancy()==LayoutBlock.OCCUPIED) && (blockC.getOccupancy()==LayoutBlock.OCCUPIED)) {
                    log.debug("Blocks " + block + " & " + blockCName + " are Occupied");
                    return true;
                }
            }
        }
        return false;
    }
    
			
	// initialization instance variables (used when loading a LayoutEditor)
	public String connectAName = "";
	public String connectBName = "";
	public String connectCName = "";
	public String connectDName = "";
	public String tBlockName = "";
	public String tBlockBName = "";
	public String tBlockCName = "";
	public String tBlockDName = "";
	public String tTurnoutName = "";
	public String tSecondTurnoutName = "";
	/**
	 * Initialization method
	 *   The above variables are initialized by PositionablePointXml, then the following
	 *        method is called after the entire LayoutEditor is loaded to set the specific
	 *        TrackSegment objects.
	 */
	public void setObjects(LayoutEditor p) {
		connectA = p.getFinder().findTrackSegmentByName(connectAName);
		connectB = p.getFinder().findTrackSegmentByName(connectBName);
		connectC = p.getFinder().findTrackSegmentByName(connectCName);
		connectD = p.getFinder().findTrackSegmentByName(connectDName);
		if (tBlockName.length()>0) {
			block = p.getLayoutBlock(tBlockName);
			if (block!=null) {
				blockName = tBlockName;
				block.incrementUse();
			}
			else {
				log.error("bad blockname '"+tBlockName+"' in layoutturnout "+ident);
			}
		}
		if (tBlockBName.length()>0) {
			blockB = p.getLayoutBlock(tBlockBName);
			if (blockB!=null) {
				blockBName = tBlockBName;
				if (block!=blockB) blockB.incrementUse();
			}
			else {
				log.error("bad blockname '"+tBlockBName+"' in layoutturnout "+ident);
			}
		}
		if (tBlockCName.length()>0) {
			blockC = p.getLayoutBlock(tBlockCName);
			if (blockC!=null) {
				blockCName = tBlockCName;
				if ( (block!=blockC) && (blockB!=blockC) ) blockC.incrementUse();
			}
			else {
				log.error("bad blockname '"+tBlockCName+"' in layoutturnout "+ident);
			}
		}
		if (tBlockDName.length()>0) {
			blockD = p.getLayoutBlock(tBlockDName);
			if (blockD!=null) {
				blockDName = tBlockDName;
				if ( (block!=blockD) && (blockB!=blockD) &&
						(blockC!=blockD) ) blockD.incrementUse();
			}
			else {
				log.error("bad blockname '"+tBlockDName+"' in layoutturnout "+ident);
			}
		}
        //Do the second one first then the activate is only called the once
        if (tSecondTurnoutName.length()>0) {
            Turnout turnout =InstanceManager.turnoutManagerInstance().
                                getTurnout(tSecondTurnoutName);
            if (turnout!=null) {
                secondNamedTurnout =InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(tSecondTurnoutName, turnout);
                secondTurnoutName = tSecondTurnoutName;
            }
            else {
				log.error("bad turnoutname '"+tSecondTurnoutName+"' in layoutturnout "+ident);
                secondTurnoutName = "";
                secondNamedTurnout = null;
            }
		}
		if (tTurnoutName.length()>0) {
            Turnout turnout =InstanceManager.turnoutManagerInstance().
                                getTurnout(tTurnoutName);
            if (turnout!=null) {
                namedTurnout =InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(tTurnoutName, turnout);
                turnoutName = tTurnoutName;
                activateTurnout();
            }
            else {
				log.error("bad turnoutname '"+tTurnoutName+"' in layoutturnout "+ident);
                turnoutName = "";
                namedTurnout = null;
            }
		}
	}

    JPopupMenu popup = null;
    JCheckBoxMenuItem disableItem = null;
    JCheckBoxMenuItem disableWhenOccupiedItem = null;
	LayoutEditorTools tools = null;
    /**
     * Display popup menu for information and editing
     */
    protected void showPopUp(MouseEvent e, boolean editable) {
        if (popup != null ) {
			popup.removeAll();
		}
		else {
            popup = new JPopupMenu();
		}
        if(editable){
            switch (getTurnoutType()) {
                case RH_TURNOUT:
                    popup.add(rb.getString("RHTurnout"));
                    break;
                case LH_TURNOUT:
                    popup.add(rb.getString("LHTurnout"));
                    break;
                case WYE_TURNOUT:
                    popup.add(rb.getString("WYETurnout"));
                    break;
                case DOUBLE_XOVER:
                    popup.add(rb.getString("XOverTurnout"));
                    break;
                case RH_XOVER:
                    popup.add(rb.getString("RHXOverTurnout"));
                    break;
                case LH_XOVER:
                    popup.add(rb.getString("LHXOverTurnout"));
                    break;
                default : break;
            }
            popup.add(ident);
            if (getTurnout()==null) popup.add(rb.getString("NoTurnout"));
            else popup.add(rb.getString("Turnout")+": "+turnoutName);
            // Rotate if there are no track connections
            if ( (connectA==null) && (connectB==null) &&
                        (connectC==null) && (connectD==null) ) {
                JMenuItem rotateItem = new JMenuItem(rb.getString("Rotate")+"...");
                popup.add(rotateItem);
                rotateItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        boolean entering = true;
                        boolean error = false;
                        String newAngle = "";
                        while (entering) {
                            // prompt for rotation angle
                            error = false;
                            newAngle = JOptionPane.showInputDialog(layoutEditor, 
                                                rb.getString("EnterRotation")+" :");
                            if (newAngle.length()<1) return;  // cancelled
                            double rot = 0.0;
                            try {
                                rot = Double.parseDouble(newAngle);
                            }
                            catch (Exception e) {
                                JOptionPane.showMessageDialog(layoutEditor,rb.getString("Error3")+
                                    " "+e,rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
                                error = true;
                                newAngle = "";
                            }
                            if (!error) {
                                entering = false;
                                if (rot!=0.0) {
                                   rotateCoords(rot);
                                   layoutEditor.redrawPanel();
                                }
                            }
                        }
                    }
                });
            }
            if (disableItem==null)
                disableItem = new JCheckBoxMenuItem(rb.getString("Disabled"));
            disableItem.setSelected(disabled);
            popup.add(disableItem);
            disableItem.addActionListener(new ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        disabled = disableItem.isSelected();
                    }
                });
            if (disableWhenOccupiedItem==null)
                disableWhenOccupiedItem = new JCheckBoxMenuItem(rb.getString("DisabledWhenOccupied"));
            disableWhenOccupiedItem.setSelected(disableWhenOccupied);
            popup.add(disableWhenOccupiedItem);
            disableWhenOccupiedItem.addActionListener(new ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        disableWhenOccupied = disableWhenOccupiedItem.isSelected();
                    }
                });
            if (blockName.equals("")) popup.add(rb.getString("NoBlock"));
            else popup.add(rb.getString("Block")+": "+getLayoutBlock().getID());
            if ( (type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER) ) {
                // check if extra blocks have been entered
                if (blockB!=null) popup.add(rb.getString("Block2ID")+": "+blockBName);
                if (blockC!=null) popup.add(rb.getString("Block3ID")+": "+blockCName);
                if (blockD!=null) popup.add(rb.getString("Block4ID")+": "+blockDName);
            }
            if (hidden) popup.add(rb.getString("Hidden"));
            else popup.add(rb.getString("NotHidden"));
            popup.add(new JSeparator(JSeparator.HORIZONTAL));
            popup.add(new AbstractAction(rb.getString("UseSizeAsDefault")) {
                    public void actionPerformed(ActionEvent e) {
                        setUpDefaultSize();
                    }
                });
            popup.add(new AbstractAction(rb.getString("Edit")) {
                    public void actionPerformed(ActionEvent e) {
                        editLayoutTurnout();
                    }
                });
            popup.add(new AbstractAction(rb.getString("Remove")) {
                    public void actionPerformed(ActionEvent e) {
                        if (layoutEditor.removeLayoutTurnout(instance)) {
                            // Returned true if user did not cancel
                            remove();
                            dispose();
                        }
                    }
                });
            if (getTurnout()!=null) {
                popup.add(new AbstractAction(rb.getString("SetSignals")) {
                    public void actionPerformed(ActionEvent e) {
                        if (tools == null) {
                            tools = new LayoutEditorTools(layoutEditor);
                        }
                        if ( (getTurnoutType()==DOUBLE_XOVER) || (getTurnoutType()==RH_XOVER) ||
                                                (getTurnoutType()==LH_XOVER) ) {
                            tools.setSignalsAtXoverTurnoutFromMenu(instance,
                                layoutEditor.signalIconEditor,layoutEditor.signalFrame);
                        }
                        else if (linkType==NO_LINK) {
                            tools.setSignalsAtTurnoutFromMenu(instance,
                                layoutEditor.signalIconEditor,layoutEditor.signalFrame);
                        }
                        else if (linkType==THROAT_TO_THROAT) {
                            tools.setThroatToThroatFromMenu(instance,linkedTurnoutName,
                                layoutEditor.signalIconEditor,layoutEditor.signalFrame);
                        }
                        else if (linkType==FIRST_3_WAY) {
                            tools.set3WayFromMenu(turnoutName, linkedTurnoutName,
                                layoutEditor.signalIconEditor,layoutEditor.signalFrame);
                        }
                        else if (linkType==SECOND_3_WAY) {
                            tools.set3WayFromMenu(linkedTurnoutName, turnoutName,
                                layoutEditor.signalIconEditor,layoutEditor.signalFrame);
                        }
                    }
                });
            }
            if (!blockName.equals("")){
                final String[] boundaryBetween = getBlockBoundaries();
                boolean blockBoundaries = false;
                for (int i = 0; i<4; i++){
                    if(boundaryBetween[i]!=null)
                        blockBoundaries=true;
                }
                if (InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()){
                    
                    if(blockBName.equals("") && blockCName.equals("") && blockDName.equals("")){
                        popup.add(new AbstractAction(rb.getString("ViewBlockRouting")) {
                            public void actionPerformed(ActionEvent e) {
                                AbstractAction  routeTableAction = new  LayoutBlockRouteTableAction("ViewRouting", getLayoutBlock());
                                routeTableAction.actionPerformed(e);
                            }
                        });
                    } else {
                        JMenu viewRouting = new JMenu(rb.getString("ViewBlockRouting"));
                        viewRouting.add(new AbstractAction(blockName) {
                            public void actionPerformed(ActionEvent e) {
                                AbstractAction  routeTableAction = new  LayoutBlockRouteTableAction(blockName, getLayoutBlock());
                                routeTableAction.actionPerformed(e);
                            }
                        });
                        if(!blockBName.equals("") && !blockBName.equals(blockName)){
                            viewRouting.add(new AbstractAction(blockBName) {
                                public void actionPerformed(ActionEvent e) {
                                    AbstractAction  routeTableAction = new  LayoutBlockRouteTableAction(blockBName, getLayoutBlockB());
                                    routeTableAction.actionPerformed(e);
                                }
                            });
                        }
                        
                        if(!blockCName.equals("") && !blockCName.equals(blockName) && !blockCName.equals(blockBName)){
                            viewRouting.add(new AbstractAction(blockCName) {
                                public void actionPerformed(ActionEvent e) {
                                    AbstractAction  routeTableAction = new  LayoutBlockRouteTableAction(blockCName, getLayoutBlockC());
                                    routeTableAction.actionPerformed(e);
                                }
                            });
                        }
                        
                        if(!blockDName.equals("")  && !blockDName.equals(blockName) && !blockDName.equals(blockBName) && !blockDName.equals(blockCName)){
                            viewRouting.add(new AbstractAction(blockDName) {
                                public void actionPerformed(ActionEvent e) {
                                    AbstractAction  routeTableAction = new  LayoutBlockRouteTableAction(blockDName, getLayoutBlockD());
                                    routeTableAction.actionPerformed(e);
                                }
                            });
                        }
                        
                        popup.add(viewRouting);
                    }
                }
                
                if (blockBoundaries){
                    popup.add(new AbstractAction(rb.getString("SetSignalMasts")) {
                        public void actionPerformed(ActionEvent e) {
                            if (tools == null) {
                                tools = new LayoutEditorTools(layoutEditor);
                            }
                                
                            tools.setSignalMastsAtTurnoutFromMenu(instance,
                            boundaryBetween);
                        }
                    });
                    popup.add(new AbstractAction(rb.getString("SetSensors")) {
                        public void actionPerformed(ActionEvent e) {
                            if (tools == null) {
                                tools = new LayoutEditorTools(layoutEditor);
                            }
                                
                            tools.setSensorsAtTurnoutFromMenu(instance,
                            boundaryBetween, layoutEditor.sensorIconEditor, layoutEditor.sensorFrame);
                        }
                    });
                }
            }
            setAdditionalEditPopUpMenu(popup);
            layoutEditor.setShowAlignmentMenu(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
        } else if(!viewAdditionalMenu.isEmpty()){
            setAdditionalViewPopUpMenu(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    
    public String[] getBlockBoundaries(){
        final String[] boundaryBetween = new String[4];
        //ArrayList<String> boundaryBetween = new ArrayList<String>(4);
        if ((type==WYE_TURNOUT) || (type ==RH_TURNOUT) || (type == LH_TURNOUT)){
            //This should only be needed where we are looking at a single turnout.
            if(block!=null){
                LayoutBlock aLBlock = null;
                LayoutBlock bLBlock = null;
                LayoutBlock cLBlock = null;
                if (connectA instanceof TrackSegment){
                    aLBlock =((TrackSegment)connectA).getLayoutBlock();
                    if(aLBlock!=block){
                        try {
                            boundaryBetween[0]=(aLBlock.getDisplayName()+ " - " + block.getDisplayName());
                        } catch (java.lang.NullPointerException e){
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }
                
                if (connectB instanceof TrackSegment){
                    bLBlock =((TrackSegment)connectB).getLayoutBlock();
                    if(bLBlock!=block){
                        try {
                            boundaryBetween[1]=(bLBlock.getDisplayName()+ " - " + block.getDisplayName());
                        } catch (java.lang.NullPointerException e){
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection B doesn't contain a layout block");
                        }
                    }
                }
                if ((connectC instanceof TrackSegment) && (((TrackSegment)connectC).getLayoutBlock()!=block)){
                    cLBlock = ((TrackSegment)connectC).getLayoutBlock();
                    if(cLBlock!=block){
                        try{
                            boundaryBetween[2]=(cLBlock.getDisplayName()+ " - " + block.getDisplayName());
                        } catch (java.lang.NullPointerException e){
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection C doesn't contain a layout block");
                        }
                    }
                }
            }
        }
        
        else {
            /*ArrayList<LayoutBlock> localblks = new ArrayList<LayoutBlock>(4);
            if(block!=null)
                localblks.add(block);
            if(blockB!=null)
                localblks.add(blockB);
            if(blockC!=null)
                localblks.add(blockC);
            if(blockD!=null)
                localblks.add(blockD);*/
            
            LayoutBlock aLBlock = null;
            LayoutBlock bLBlock = null;
            LayoutBlock cLBlock = null;
            LayoutBlock dLBlock = null;
            if(block!=null){
                if (connectA instanceof TrackSegment){
                    aLBlock =((TrackSegment)connectA).getLayoutBlock();
                    if(aLBlock!=block){
                        try {
                            boundaryBetween[0]=(aLBlock.getDisplayName()+ " - " + block.getDisplayName());
                        } catch (java.lang.NullPointerException e){
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    } else if (block!=blockB){
                        try {
                            boundaryBetween[0]=(block.getDisplayName()+ " - " + blockB.getDisplayName());
                        } catch (java.lang.NullPointerException e){
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }
                
                if (connectB instanceof TrackSegment){
                    bLBlock =((TrackSegment)connectB).getLayoutBlock();
                    
                    if (bLBlock!=block && bLBlock!=blockB){
                        try {
                            boundaryBetween[1]=(bLBlock.getDisplayName()+ " - " + blockB.getDisplayName());
                        } catch (java.lang.NullPointerException e){
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection B doesn't contain a layout block");
                        }
                    } else if (block!=blockB){
                        //This is an interal block on the turnout
                        try {
                            boundaryBetween[1]=(blockB.getDisplayName()+ " - " + block.getDisplayName());
                        } catch (java.lang.NullPointerException e){
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }
                if (connectC instanceof TrackSegment){
                    cLBlock =((TrackSegment)connectC).getLayoutBlock();
                    if (cLBlock!=block && cLBlock!=blockB && cLBlock!=blockC) {
                        try{
                            boundaryBetween[2]=(cLBlock.getDisplayName()+ " - " + blockC.getDisplayName());
                        } catch (java.lang.NullPointerException e){
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection C doesn't contain a layout block");
                        }
                    } else if (blockC!=blockD){
                        //This is an interal block on the turnout
                        try {
                            boundaryBetween[2]=(blockC.getDisplayName()+ " - " + blockD.getDisplayName());
                        } catch (java.lang.NullPointerException e){
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }
                if (connectD instanceof TrackSegment){
                    dLBlock =((TrackSegment)connectD).getLayoutBlock();
                    if (dLBlock!=block && dLBlock!=blockB && dLBlock!=blockC && dLBlock!=blockD) {
                        try{
                            boundaryBetween[3]=(dLBlock.getDisplayName()+ " - " + blockD.getDisplayName());
                        } catch (java.lang.NullPointerException e){
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection C doesn't contain a layout block");
                        }
                    } else if (blockC!=blockD){
                        //This is an interal block on the turnout
                        try {
                            boundaryBetween[3]=(blockD.getDisplayName()+ " - " + blockC.getDisplayName());
                        } catch (java.lang.NullPointerException e){
                            //Can be considered normal if tracksegement hasn't yet been allocated a block
                            log.debug("TrackSegement at connection A doesn't contain a layout block");
                        }
                    }
                }
            }
            
        }
        return boundaryBetween;
    }
    
	// variables for Edit Layout Turnout pane
	protected JmriJFrame editLayoutTurnoutFrame = null;
	private JTextField turnoutNameField = new JTextField(16);
    private JmriBeanComboBox secondTurnoutComboBox;
    private JLabel secondTurnoutLabel;
	protected JTextField blockNameField = new JTextField(16);
	private JTextField blockBNameField = new JTextField(16);
	private JTextField blockCNameField = new JTextField(16);
	private JTextField blockDNameField = new JTextField(16);
    private JComboBox stateBox = new JComboBox();
    private JCheckBox hiddenBox = new JCheckBox(rb.getString("HideTurnout"));
    private int turnoutClosedIndex;
    private int turnoutThrownIndex;
	private JButton turnoutEditBlock;
	private JButton turnoutEditDone;
	private JButton turnoutEditCancel;
	private JButton turnoutEditBlockB;
	private JButton turnoutEditBlockC;
	private JButton turnoutEditBlockD;
	private boolean editOpen = false;
	protected boolean needRedraw = false;
	protected boolean needsBlockUpdate = false;
    private JCheckBox additionalTurnout = new JCheckBox(rb.getString("SupportingTurnout"));

    /**
     * Edit a Layout Turnout 
     */
	protected void editLayoutTurnout() {
		if (editOpen) {
			editLayoutTurnoutFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (editLayoutTurnoutFrame == null) {
            editLayoutTurnoutFrame = new JmriJFrame( rb.getString("EditTurnout"), false, true );
            editLayoutTurnoutFrame.addHelpMenu("package.jmri.jmrit.display.EditLayoutTurnout", true);
            editLayoutTurnoutFrame.setLocation(50,30);
            Container contentPane = editLayoutTurnoutFrame.getContentPane();        
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			// setup turnout name
            JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
			JLabel turnoutNameLabel = new JLabel( rb.getString("Turnout")+" "+rb.getString("Name") );
            panel1.add(turnoutNameLabel);
            panel1.add(turnoutNameField);
            turnoutNameField.setToolTipText( rb.getString("EditTurnoutNameHint") );
            contentPane.add(panel1);

            JPanel panel1a = new JPanel();
            panel1a.setLayout(new BoxLayout(panel1a, BoxLayout.Y_AXIS));
            secondTurnoutComboBox = new JmriBeanComboBox(InstanceManager.turnoutManagerInstance(), getSecondTurnout(), JmriBeanComboBox.DISPLAYNAME);
            additionalTurnout.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(additionalTurnout.isSelected()){
                        secondTurnoutLabel.setEnabled(true);
                        secondTurnoutComboBox.setEnabled(true);
                    } else  {
                        secondTurnoutLabel.setEnabled(false);
                        secondTurnoutComboBox.setEnabled(false);
                    }
                }
            });
            if ( (type != DOUBLE_XOVER) && (type != RH_XOVER) && (type != LH_XOVER) ) {
                additionalTurnout.setText(rb.getString("ThrowTwoTurnouts"));
            }
            panel1a.add(additionalTurnout);
            contentPane.add(panel1a);
            secondTurnoutLabel = new JLabel( rb.getString("Supporting") + rb.getString("Turnout")+" "+rb.getString("Name") );
            secondTurnoutLabel.setEnabled(false);
            secondTurnoutComboBox.setEnabled(false);
            JPanel panel1b = new JPanel();
            panel1b.add(secondTurnoutLabel);
            panel1b.add(secondTurnoutComboBox);
            contentPane.add(panel1b);

            
			// add continuing state choice, if not crossover
			if ( (type != DOUBLE_XOVER) && (type != RH_XOVER) && (type != LH_XOVER) ) {
				JPanel panel3 = new JPanel(); 
				panel3.setLayout(new FlowLayout());
				stateBox.removeAllItems();
				stateBox.addItem( InstanceManager.turnoutManagerInstance().getClosedText() );
				turnoutClosedIndex = 0;
				stateBox.addItem( InstanceManager.turnoutManagerInstance().getThrownText() );
				turnoutThrownIndex = 1;
				stateBox.setToolTipText(rb.getString("StateToolTip"));
				panel3.add (new JLabel(rb.getString("ContinuingState")));
				panel3.add (stateBox);
				contentPane.add(panel3);
			} 
            
            JPanel panel33 = new JPanel(); 
            panel33.setLayout(new FlowLayout());
			hiddenBox.setToolTipText(rb.getString("HiddenToolTip"));
			panel33.add (hiddenBox);
            contentPane.add(panel33);			

            TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
            border.setTitle(rb.getString("Block"));
			// setup block name
            JPanel panel2 = new JPanel(); 
            panel2.setBorder(border);
            panel2.setLayout(new FlowLayout());
            panel2.add(blockNameField);
            blockNameField.setToolTipText( rb.getString("EditBlockNameHint") );
            panel2.add(turnoutEditBlock = new JButton(rb.getString("CreateEdit")));
            turnoutEditBlock.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    turnoutEditBlockPressed(e);
                }
            });
            contentPane.add(panel2);
			if ( (type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER) ) { 
				JPanel panel21 = new JPanel(); 
				panel21.setLayout(new FlowLayout());
                TitledBorder borderblk2 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                borderblk2.setTitle(rb.getString("Block") + " 2");
                panel21.setBorder(borderblk2);
				panel21.add(blockBNameField);                
				blockBNameField.setToolTipText( rb.getString("EditBlockBNameHint") );
				
				panel21.add(turnoutEditBlockB = new JButton(rb.getString("CreateEdit")));
				turnoutEditBlockB.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						turnoutEditBlockBPressed(e);
					}
				});
				turnoutEditBlockB.setToolTipText( rb.getString("EditBlockBHint") );
                contentPane.add(panel21);
                
				JPanel panel22 = new JPanel(); 
				panel22.setLayout(new FlowLayout());
                TitledBorder borderblk3 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                borderblk3.setTitle(rb.getString("Block") + " 3");
                panel22.setBorder(borderblk3);
				panel22.add(blockCNameField);
                blockCNameField.setToolTipText( rb.getString("EditBlockCNameHint") );
                panel22.add(turnoutEditBlockC = new JButton(rb.getString("CreateEdit")));
				turnoutEditBlockC.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						turnoutEditBlockCPressed(e);
					}
				});
				turnoutEditBlockC.setToolTipText( rb.getString("EditBlockCHint") );
				contentPane.add(panel22);

				JPanel panel23 = new JPanel(); 
				panel23.setLayout(new FlowLayout());
                TitledBorder borderblk4 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                borderblk4.setTitle(rb.getString("Block") + " 4");
                panel23.setBorder(borderblk4);
				panel23.add(blockDNameField);
				blockDNameField.setToolTipText( rb.getString("EditBlockDNameHint") );
                panel23.add(turnoutEditBlockD = new JButton(rb.getString("CreateEdit")));
				turnoutEditBlockD.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						turnoutEditBlockDPressed(e);
					}
				});
				turnoutEditBlockD.setToolTipText( rb.getString("EditBlockDHint") );
				contentPane.add(panel23);
			}
			// set up Edit Block, Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
			// Edit Block

            turnoutEditBlock.setToolTipText( rb.getString("EditBlockHint") );
			// Done
            panel5.add(turnoutEditDone = new JButton(rb.getString("Done")));
            turnoutEditDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    turnoutEditDonePressed(e);
                }
            });
            turnoutEditDone.setToolTipText( rb.getString("DoneHint") );
			// Cancel
            panel5.add(turnoutEditCancel = new JButton(rb.getString("Cancel")));
            turnoutEditCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    turnoutEditCancelPressed(e);
                }
            });
            turnoutEditCancel.setToolTipText( rb.getString("CancelHint") );
            contentPane.add(panel5);
		}
        
        hiddenBox.setSelected(hidden);
        
		// Set up for Edit
		blockNameField.setText(blockName);
		if ( (type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER) ) {
			blockBNameField.setText(blockBName);
			blockCNameField.setText(blockCName);
			blockDNameField.setText(blockDName);
		}
		turnoutNameField.setText(turnoutName);
        
        
        if(secondNamedTurnout!=null){
            additionalTurnout.setSelected(true);
            secondTurnoutLabel.setEnabled(true);
            secondTurnoutComboBox.setEnabled(true);
        }
        
		if ( (type != DOUBLE_XOVER) && (type != RH_XOVER) && (type != LH_XOVER) ) {
			if (continuingSense==Turnout.CLOSED) {
				stateBox.setSelectedIndex(turnoutClosedIndex);
			}
			else {
				stateBox.setSelectedIndex(turnoutThrownIndex);
			}
		}
        
		editLayoutTurnoutFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					turnoutEditCancelPressed(null);
				}
			});
        editLayoutTurnoutFrame.pack();
        editLayoutTurnoutFrame.setVisible(true);		
		editOpen = true;
		needsBlockUpdate = false;
	}
    
	void turnoutEditBlockPressed(ActionEvent a) {
		// check if a block name has been entered
		if (!blockName.equals(blockNameField.getText().trim()) ) {
			// block has changed, if old block exists, decrement use
			if ( (block!=null) && (block!=blockB) && (block!=blockC)
							&& (block!=blockD) ) {
				block.decrementUse();
			}
			// get new block, or null if block has been removed
			blockName = blockNameField.getText().trim();
			block = layoutEditor.provideLayoutBlock(blockName);
			if (block==null) {
				blockName = "";
			}
			// decrement use if block was already counted
			if ( (block!=null) && ( (block==blockB) || (block==blockC) ||
					(block==blockD) ) ) block.decrementUse();
			needRedraw = true;
			needsBlockUpdate = true;
		}
		// check if a block exists to edit
		if (block==null) {
			JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
					rb.getString("Error1"),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		block.editLayoutBlock(editLayoutTurnoutFrame);
		needRedraw = true;
		layoutEditor.setDirty();
	}
	void turnoutEditBlockBPressed(ActionEvent a) {
		// check if a block name has been entered
		if (!blockBName.equals(blockBNameField.getText().trim()) ) {
			// block has changed, if old block exists, decrement use
			if ( (blockB!=null) && (block!=blockB) && (blockB!=blockC)
							&& (blockB!=blockD) ) {
				blockB.decrementUse();
			}
			// get new block, or null if block has been removed
			blockBName = blockBNameField.getText().trim();
			blockB = layoutEditor.provideLayoutBlock(blockBName);
			if (blockB==null) {
				blockBName = "";
			}
			// decrement use if block was already counted
			if ( (blockB!=null) && ( (block==blockB) || (blockB==blockC) ||
					(blockB==blockD) ) ) blockB.decrementUse();
			needRedraw = true;
			needsBlockUpdate = true;
		}
		// check if a block exists to edit
		if (blockB==null) {
			JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
					rb.getString("Error1"),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		blockB.editLayoutBlock(editLayoutTurnoutFrame);
		needRedraw = true;
		layoutEditor.setDirty();
	}
	void turnoutEditBlockCPressed(ActionEvent a) {
		// check if a block name has been entered
		if (!blockCName.equals(blockCNameField.getText().trim()) ) {
			// block has changed, if old block exists, decrement use
			if ( (blockC!=null) && (block!=blockC) && (blockB!=blockC)
							&& (blockC!=blockD) ) {
				blockC.decrementUse();
			}
			// get new block, or null if block has been removed
			blockCName = blockCNameField.getText().trim();
			blockC = layoutEditor.provideLayoutBlock(blockCName);
			if (blockC==null) {
				blockCName = "";
			}
			// decrement use if block was already counted
			if ( (blockC!=null) && ( (block==blockC) || (blockB==blockC) ||
					(blockC==blockD) ) ) blockD.decrementUse();
			needRedraw = true;
			needsBlockUpdate = true;
		}
		// check if a block exists to edit
		if (blockC==null) {
			JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
					rb.getString("Error1"),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		blockC.editLayoutBlock(editLayoutTurnoutFrame);
		needRedraw = true;
		layoutEditor.setDirty();
	}
	void turnoutEditBlockDPressed(ActionEvent a) {
		// check if a block name has been entered
		if (!blockDName.equals(blockDNameField.getText().trim()) ) {
			// block has changed, if old block exists, decrement use
			if ( (blockD!=null) && (block!=blockD) && (blockB!=blockD)
							&& (blockC!=blockD) ) {
				blockD.decrementUse();
			}
			// get new block, or null if block has been removed
			blockDName = blockDNameField.getText().trim();
			blockD = layoutEditor.provideLayoutBlock(blockDName);
			if (blockD==null) {
				blockDName = "";
			}
			// decrement use if block was already counted
			if ( (blockD!=null) && ( (block==blockD) || (blockB==blockD) ||
					(blockC==blockD) ) ) blockD.decrementUse();
			needRedraw = true;
			needsBlockUpdate = true;
		}
		// check if a block exists to edit
		if (blockD==null) {
			JOptionPane.showMessageDialog(editLayoutTurnoutFrame,
					rb.getString("Error1"),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		blockD.editLayoutBlock(editLayoutTurnoutFrame);
		needRedraw = true;
		layoutEditor.setDirty();
	}
	void turnoutEditDonePressed(ActionEvent a) {
		// check if Turnout changed
		if ( !turnoutName.equals(turnoutNameField.getText().trim()) ) {
			// turnout has changed
			String newName = turnoutNameField.getText().trim();
			if ( layoutEditor.validatePhysicalTurnout(newName,
							editLayoutTurnoutFrame) ) {
				setTurnout(newName);
			}
			else {
				namedTurnout = null;
				turnoutName = "";
				turnoutNameField.setText("");
			}
			needRedraw = true;
		}
        
        if(additionalTurnout.isSelected()){
            if ( !secondTurnoutName.equals(secondTurnoutComboBox.getSelectedDisplayName()) ) {
                if ( (type == DOUBLE_XOVER) || (type == RH_XOVER) || (type == LH_XOVER) ) {
                // turnout has changed
                    String newName = secondTurnoutComboBox.getSelectedDisplayName();
                    if ( layoutEditor.validatePhysicalTurnout(newName,
                                    editLayoutTurnoutFrame) ) {
                        setSecondTurnout(newName);
                    }
                    else {
                        additionalTurnout.setSelected(false);
                        secondNamedTurnout = null;
                        secondTurnoutName = "";
                        //secondTurnoutNameField.setText("");
                    }
                    needRedraw = true;
                } else {
                    setSecondTurnout(secondTurnoutComboBox.getSelectedDisplayName());
                }
            }
        } else {
            setSecondTurnout(null);
        }
		// set the continuing route Turnout State
		if ( (type==RH_TURNOUT) || (type==LH_TURNOUT) || (type==WYE_TURNOUT) ) {
			continuingSense = Turnout.CLOSED;
			if ( stateBox.getSelectedIndex() == turnoutThrownIndex ) {
				continuingSense = Turnout.THROWN;
			}
		}
		// check if Block changed
		if ( !blockName.equals(blockNameField.getText().trim()) ) {
			// block has changed, if old block exists, decrement use
			if ( (block!=null) && (block!=blockB) && (block!=blockC) &&
					(block!=blockD) ) {
				block.decrementUse();
			}
			// get new block, or null if block has been removed
			blockName = blockNameField.getText().trim();
			block = layoutEditor.provideLayoutBlock(blockName);
			if (block==null) {
				blockName = "";
			}
			// decrement use if block was already counted
			if ( (block!=null) && ( (block==blockB) || (block==blockC) ||
					(block==blockD) ) ) block.decrementUse();
			needRedraw = true;
			needsBlockUpdate = true;
		}
		if ( (type==DOUBLE_XOVER) || (type==LH_XOVER) || (type==RH_XOVER) ) {
			// check if Block 2 changed
			if ( !blockBName.equals(blockBNameField.getText().trim()) ) {
				// block has changed, if old block exists, decrement use
				if ( (blockB!=null) && (block!=blockB) && (blockB!=blockC)
							&& (blockB!=blockD) ) {
					blockB.decrementUse();
				}
				// get new block, or null if block has been removed
				blockBName = blockBNameField.getText().trim();
				blockB = layoutEditor.provideLayoutBlock(blockBName);
				if (blockB==null) {
					blockBName = "";
				}
				// decrement use if block was already counted
				if ( (blockB!=null) && ( (block==blockB) || (blockB==blockC) ||
						(blockB==blockD) ) ) blockB.decrementUse();
				needRedraw = true;
				needsBlockUpdate = true;
			}
			// check if Block 3 changed
			if (!blockCName.equals(blockCNameField.getText().trim()) ) {
				// block has changed, if old block exists, decrement use
				if ( (blockC!=null) && (block!=blockC) && (blockB!=blockC)
							&& (blockC!=blockD) ) {
					blockC.decrementUse();
				}
				// get new block, or null if block has been removed
				blockCName = blockCNameField.getText().trim();
				blockC = layoutEditor.provideLayoutBlock(blockCName);
				if (blockC==null) {
					blockCName = "";
				}


				// decrement use if block was already counted
				if ( (blockC!=null) && ( (block==blockC) || (blockB==blockC) ||
						(blockC==blockD) ) ) blockC.decrementUse();
				needRedraw = true;
				needsBlockUpdate = true;
			}
			// check if Block 4 changed
			if (!blockDName.equals(blockDNameField.getText().trim()) ) {
				// block has changed, if old block exists, decrement use
				if ( (blockD!=null) && (block!=blockD) && (blockB!=blockD)
							&& (blockC!=blockD) ) {
					blockD.decrementUse();
				}
				// get new block, or null if block has been removed
				blockDName = blockDNameField.getText().trim();
				blockD = layoutEditor.provideLayoutBlock(blockDName);
				if (blockD==null) {
					blockDName = "";
				}
				// decrement use if block was already counted
				if ( (blockD!=null) && ( (block==blockD) || (blockB==blockD) ||
						(blockC==blockD) ) ) blockD.decrementUse();
				needRedraw = true;
				needsBlockUpdate = true;
			}
		}
        // set hidden
		boolean oldHidden = hidden;
		hidden = hiddenBox.isSelected();
        if(oldHidden!=hidden)
            needRedraw=true;
		editOpen = false;
		editLayoutTurnoutFrame.setVisible(false);
		editLayoutTurnoutFrame.dispose();
		editLayoutTurnoutFrame = null;
		if (needsBlockUpdate){
            updateBlockInfo();
            reCheckBlockBoundary();
        }
		if (needRedraw) {
			layoutEditor.redrawPanel();
			layoutEditor.setDirty();
		}
	}
	void turnoutEditCancelPressed(ActionEvent a) {
		editOpen = false;
		editLayoutTurnoutFrame.setVisible(false);
		editLayoutTurnoutFrame.dispose();
		editLayoutTurnoutFrame = null;
		if (needsBlockUpdate) updateBlockInfo();
		if (needRedraw) {
			layoutEditor.redrawPanel();
			layoutEditor.setDirty();
		}
	}

    //@todo on the cross-overs check the internal boundary details.
    public void reCheckBlockBoundary(){
        if(connectA==null && connectB==null && connectC==null){
            if ((type==RH_TURNOUT) || (type==LH_TURNOUT) || (type==WYE_TURNOUT)){
                if(signalAMastNamed!=null)
                    removeSML(getSignalAMast());
                if(signalBMastNamed!=null)
                    removeSML(getSignalBMast());
                if(signalCMastNamed!=null)
                    removeSML(getSignalCMast());
                signalAMastNamed = null;
                signalBMastNamed = null;
                signalCMastNamed = null;
                sensorANamed=null;
                sensorBNamed=null;
                sensorCNamed=null;
                return;
            
            } else if (((type==DOUBLE_XOVER)||(type==RH_XOVER)||(type==LH_XOVER)) && connectD==null){
                if(signalAMastNamed!=null)
                    removeSML(getSignalAMast());
                if(signalBMastNamed!=null)
                    removeSML(getSignalBMast());
                if(signalCMastNamed!=null)
                    removeSML(getSignalCMast());
                if(signalDMastNamed!=null)
                    removeSML(getSignalDMast());
                signalAMastNamed = null;
                signalBMastNamed = null;
                signalCMastNamed = null;
                signalDMastNamed = null;
                sensorANamed=null;
                sensorBNamed=null;
                sensorCNamed=null;
                sensorDNamed=null;
                return;
            }
        }
        
        if(connectA==null || connectB==null || connectC==null){
            //could still be in the process of rebuilding.
            return;
        } else if ((connectD == null) && ((type==DOUBLE_XOVER)||(type==RH_XOVER)||(type==LH_XOVER))){
            //could still be in the process of rebuilding.
            return;
        }
        
        TrackSegment trkA;
        TrackSegment trkB;
        TrackSegment trkC;
        TrackSegment trkD;
        
        if(connectA instanceof TrackSegment){
            trkA = (TrackSegment)connectA;
            if(trkA.getLayoutBlock()==block){
                if(signalAMastNamed!=null)
                    removeSML(getSignalAMast());
                signalAMastNamed = null;
                sensorANamed=null;
            }
        }
        if(connectB instanceof TrackSegment){
            trkB = (TrackSegment)connectB;
            if(trkB.getLayoutBlock()==block || trkB.getLayoutBlock()==blockB){
               if(signalBMastNamed!=null)
                    removeSML(getSignalBMast());
                signalBMastNamed = null;
                sensorBNamed=null;

            }
        } if(connectC instanceof TrackSegment) {
            trkC = (TrackSegment)connectC;
            if(trkC.getLayoutBlock()==block || trkC.getLayoutBlock()==blockB || trkC.getLayoutBlock()==blockC){
                if(signalCMastNamed!=null)
                    removeSML(getSignalCMast());
                signalCMastNamed = null;
                sensorCNamed=null;

            }
        } if(connectD!=null && connectD instanceof TrackSegment && ((type==DOUBLE_XOVER)||(type==RH_XOVER)||(type==LH_XOVER))){
            trkD = (TrackSegment)connectD;
            if(trkD.getLayoutBlock()==block || trkD.getLayoutBlock()==blockB || trkD.getLayoutBlock()==blockC || trkD.getLayoutBlock()==blockD){
                if(signalDMastNamed!=null)
                    removeSML(getSignalDMast());
                signalDMastNamed = null;
                sensorDNamed=null;
            }
        }
    }
    
    public ArrayList<LayoutBlock> getProtectedBlocks(jmri.NamedBean bean){
        ArrayList<LayoutBlock> ret = new ArrayList<LayoutBlock>(2);
        if(block==null){
            return ret;
        }
        if(getTurnoutType()>=DOUBLE_XOVER  && getTurnoutType()<=LH_XOVER){
            if((getTurnoutType()==DOUBLE_XOVER || getTurnoutType()==RH_XOVER) && (getSignalAMast() == bean || getSignalCMast() == bean || getSensorA()==bean || getSensorC()==bean)){
                if(getSignalAMast() == bean || getSensorA()==bean){
                    if(connectA!=null){
                        if(((TrackSegment)connectA).getLayoutBlock()==block){
                            if(blockB!=null && block!=blockB && blockC!=null && block!=blockC){
                                ret.add(blockB);
                                ret.add(blockC);
                            }
                        } else {
                            ret.add(block);
                        }
                    }
                } else {
                    if(connectC!=null && blockC!=null) {
                        if(((TrackSegment)connectC).getLayoutBlock()==blockC){
                            if(blockC!=block && blockD!=null && blockC!=blockD){
                                ret.add(block);
                                ret.add(blockD);
                            }
                        } else {
                            ret.add(blockC);
                        }
                    }
                }
            }
            if((getTurnoutType()==DOUBLE_XOVER || getTurnoutType()==LH_XOVER) && (getSignalBMast() == bean || getSignalDMast() == bean || getSensorB()==bean || getSensorD()==bean)){
                if(getSignalBMast() == bean || getSensorB()==bean){
                    if(connectB!=null && blockB !=null){
                        if(((TrackSegment)connectB).getLayoutBlock()==blockB){
                            if(block!=blockB && blockD!=null && blockB!=blockD){
                                ret.add(block);
                                ret.add(blockD);
                            }
                        } else {
                            ret.add(blockB);
                        }
                    }
                } else {
                    if(connectD!=null && blockD!=null){
                        if(((TrackSegment)connectD).getLayoutBlock()==blockD){
                            if(blockB!=null && blockB!=blockD && blockC!=null && blockC!=blockD){
                                ret.add(blockB);
                                ret.add(blockC);
                            }
                        } else {
                            ret.add(blockD);
                        }
                    }
                }
            }
            if(getTurnoutType()==RH_XOVER && (getSignalBMast() == bean || getSignalDMast() == bean || getSensorB()==bean || getSensorD()==bean)){
                if(getSignalBMast() == bean || getSensorB()==bean){
                    if(connectB!=null && ((TrackSegment)connectB).getLayoutBlock()==blockB){
                        if(blockB!=block){
                            ret.add(block);
                        }
                    } else {
                        ret.add(blockB);
                    }
                } else {
                    if(connectD!=null && ((TrackSegment)connectD).getLayoutBlock()==blockD){
                        if(blockC!=blockD){
                            ret.add(blockC);
                        }
                    } else {
                        ret.add(blockD);
                    }
                }
            }
            if(getTurnoutType()==LH_XOVER && (getSensorA()==bean || getSensorC()==bean || getSignalAMast() == bean || getSignalCMast() == bean)){
                if(getSignalAMast() == bean || getSensorA()==bean){
                    if(connectA!=null && ((TrackSegment)connectA).getLayoutBlock()==block){
                        if(blockB!=block){
                            ret.add(blockB);
                        }
                    } else {
                        ret.add(block);
                    }
                } else {
                    if(connectC!=null && ((TrackSegment)connectC).getLayoutBlock()==blockC){
                        if(blockC!=blockD){
                            ret.add(blockD);
                        }
                    } else {
                        ret.add(blockC);
                    }
                }
            }
        } else {
            if(connectA!=null){
                if(getSignalAMast() == bean || getSensorA()==bean){
                    //Mast at throat
                        //if the turnout is in the same block as the segment connected at the throat, then we can be protecting two blocks
                    if(((TrackSegment)connectA).getLayoutBlock()==block){
                        if(connectB!=null && connectC!=null){
                            if(((TrackSegment)connectB).getLayoutBlock()!=block && ((TrackSegment)connectC).getLayoutBlock()!=block){
                                ret.add(((TrackSegment)connectB).getLayoutBlock());
                                ret.add(((TrackSegment)connectC).getLayoutBlock());
                            }
                        }
                    } else {
                        ret.add(block);
                    }
                } else if(getSignalBMast() == bean || getSensorB()==bean){
                    //Mast at Continuing
                    if(connectB!=null && ((TrackSegment)connectB).getLayoutBlock()==block){
                        if(((TrackSegment)connectA).getLayoutBlock()!=block) ret.add(((TrackSegment)connectA).getLayoutBlock());
                    } else {
                        ret.add(block);
                    }
                } else if(getSignalCMast() == bean || getSensorC()==bean){
                    //Mast at Diverging
                    if(connectC!=null && ((TrackSegment)connectC).getLayoutBlock()==block){
                        if(((TrackSegment)connectA).getLayoutBlock()!=block) ret.add(((TrackSegment)connectA).getLayoutBlock());
                    } else {
                        ret.add(block);
                    }
                }
            }
        }
        return ret;
    }
    
    protected void removeSML(SignalMast signalMast){
        if(signalMast==null)
            return;
        if(jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled() && InstanceManager.signalMastLogicManagerInstance().isSignalMastUsed(signalMast)){
            SignallingGuiTools.removeSignalMastLogic(null, signalMast);
        }
    }
    
    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    void dispose() {
    }

    /**
     * Removes this object from display and persistance
     */
    void remove() {
		// if a turnout has been activated, deactivate it
		deactivateTurnout();
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
    
    ArrayList<JMenuItem> editAdditionalMenu = new ArrayList<JMenuItem>(0);
    ArrayList<JMenuItem> viewAdditionalMenu = new ArrayList<JMenuItem>(0);
    
    public void addEditPopUpMenu(JMenuItem menu){
        if(!editAdditionalMenu.contains(menu)){
            editAdditionalMenu.add(menu);
        }
    }
    
    public void addViewPopUpMenu(JMenuItem menu){
        if(!viewAdditionalMenu.contains(menu)){
            viewAdditionalMenu.add(menu);
        }
    }
    
    public void setAdditionalEditPopUpMenu(JPopupMenu popup){
        if(editAdditionalMenu.isEmpty())
            return;
        popup.addSeparator();
        for(JMenuItem mi:editAdditionalMenu){
            popup.add(mi);
        }
    }
    
    public void setAdditionalViewPopUpMenu(JPopupMenu popup){
        if(viewAdditionalMenu.isEmpty())
            return;
        popup.addSeparator();
        for(JMenuItem mi:viewAdditionalMenu){
            popup.add(mi);
        }
    }

    static Logger log = LoggerFactory.getLogger(LayoutTurnout.class.getName());

}
