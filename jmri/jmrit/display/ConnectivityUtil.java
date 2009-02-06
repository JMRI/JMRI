// jmri.jmrit.display.ConnectivityUtil.java

package jmri.jmrit.display;

import java.util.ResourceBundle;
import java.util.ArrayList;

import javax.swing.*;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.SignalHead;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.blockboss.BlockBossLogic;

/**
 * ConnectivityUtil provides methods supporting use of layout connectivity available 
 *	in Layout Editor panels.  These tools allow outside classes to inquire into 
 *  connectivity information contained in a specified Layout Editor panel.
 * <P>
 * Connectivity information is stored in the track diagram of a Layout Editor panel. The 
 *  "connectivity graph" of the layout consists of nodes (LayoutTurnouts, LevelXings, and 
 *  PositionablePoints) connected by lines (TrackSegments). These methods extract information 
 *  from the connection graph and make it available. Each instance of ConnectivityUtil is 
 *	associated with a specific Layout Editor panel, and is accessed via that LayoutEditor panel's
 *  'getConnectivityUtil' method.
 * <P>
 * The methods in this module do not modify the Layout in any way, or change the state of 
 *  items on the layout. They only provide information to allow other modules to do so as
 *  appropriate. For example, the "getTurnoutList" method provides information about the 
 *  turnouts in a block, but does not test the state, or change the state, of any turnout.
 * <P>
 * The methods in this module are accessed via direct calls from the inquiring 
 *   method. 
 * <P>
 * @author Dave Duchamp Copyright (c) 2009
 * @version $Revision: 1.1 $
 */

public class ConnectivityUtil 
{

	// Defined text resource
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.LayoutEditorBundle");
	
	// constants
	
	// operational instance variables 
	private LayoutEditor layoutEditor = null;
	private LayoutEditorAuxTools auxTools = null;
	private LayoutBlockManager layoutBlockManager = null;
	
	// constructor method
	public ConnectivityUtil(LayoutEditor thePanel) {
		layoutEditor = thePanel;
		auxTools = new LayoutEditorAuxTools(layoutEditor);
		layoutBlockManager = InstanceManager.layoutBlockManagerInstance();
	}
	
	/** 
	 * Provides a list of LayoutTurnouts in a specified Block (block), in order, beginning at the connection 
	 *   to the specified previous Block (prevBlock) and continuing to the specfied next Block 
	 *   (nextBlock).
	 * Also compiles a companion list of how the turnout should be set for the specified connectivity.
	 *	 The companion list can be accessed by "getTurnoutSettingList" immediately after this method
	 *	 returns.
	 * If both the previous Block or the next Block are specified, follows the connectivity and returns
	 *   only those turnouts needed for the transit of this block.  If either are not present (null), 
	 *   returns all turnouts in this block, with settings to enter/exit to whatever block is specified, 
	 *   and other settings set to CLOSED.
	 * Returns an empty list if a connectivity anamoly is discovered--specified blocks are not connected.
	 */
	private ArrayList companion = null;
	private TrackSegment tr = null;
	private int prevConnectType = 0;
	private	Object prevConnectObject = null;
	LayoutBlock lb = null;
	LayoutBlock nlb = null;
	LayoutBlock plb = null;
	public ArrayList getTurnoutList(Block block, Block prevBlock, Block nextBlock) {
		ArrayList list = new ArrayList();
		companion = new ArrayList();
		// initialize
		lb = layoutBlockManager.getByUserName(block.getUserName());
		if (prevBlock!=null) {
			plb = layoutBlockManager.getByUserName(prevBlock.getUserName());
		}
		if (nextBlock!=null) {
			nlb = layoutBlockManager.getByUserName(nextBlock.getUserName());
		}
		if ( (plb==null) || (nlb==null) ) {
			// special search with partial information - not as good, order not assured
			ArrayList allTurnouts = getAllTurnoutsThisBlock(lb);
			for (int i = 0; i<allTurnouts.size(); i++) {
				LayoutTurnout ltx = (LayoutTurnout)allTurnouts.get(i);
				list.add(ltx);
				int tTyp = ltx.getTurnoutType();
				switch (tTyp) {
					case LayoutTurnout.RH_TURNOUT:
					case LayoutTurnout.LH_TURNOUT:
					case LayoutTurnout.WYE_TURNOUT:
						if (((TrackSegment)ltx.getConnectA()).getLayoutBlock()==lb) {
							if ( (((TrackSegment)ltx.getConnectC()).getLayoutBlock()==nlb) ||
									(((TrackSegment)ltx.getConnectC()).getLayoutBlock()==plb) ) {
								companion.add(new Integer(Turnout.THROWN));
							}
							else if ( (((TrackSegment)ltx.getConnectB()).getLayoutBlock()==nlb) ||
									(((TrackSegment)ltx.getConnectB()).getLayoutBlock()==plb) ) {
								companion.add(new Integer(Turnout.CLOSED));
							}
							else if (((TrackSegment)ltx.getConnectB()).getLayoutBlock()==lb) {
								companion.add(new Integer(Turnout.CLOSED));
							}
							else if (((TrackSegment)ltx.getConnectC()).getLayoutBlock()==lb) {
								companion.add(new Integer(Turnout.THROWN));
							}
							else {
								log.error("Cannot determine turnout setting - "+ltx.getTurnoutName());
								companion.add(new Integer(Turnout.CLOSED));
							}
						}
						else if (((TrackSegment)ltx.getConnectB()).getLayoutBlock()==lb) {
							companion.add(new Integer(Turnout.CLOSED));
						}
						else if (((TrackSegment)ltx.getConnectC()).getLayoutBlock()==lb) {
							companion.add(new Integer(Turnout.THROWN));
						}
						else {
							log.error("Cannot determine turnout setting for "+ltx.getTurnoutName());
							companion.add(new Integer(Turnout.CLOSED));
						}
						break;
					case LayoutTurnout.RH_XOVER:
					case LayoutTurnout.LH_XOVER:
					case LayoutTurnout.DOUBLE_XOVER:
						if (ltx.getLayoutBlock()==lb) {
							if ( (tTyp!=LayoutTurnout.LH_XOVER) && ((ltx.getLayoutBlockC()==nlb) ||
										(ltx.getLayoutBlockC()==plb)) ) {
								companion.add(new Integer(Turnout.THROWN));
							}
							else if ( (ltx.getLayoutBlockB()==nlb) ||
										(ltx.getLayoutBlockB()==plb) ) {
								companion.add(new Integer(Turnout.CLOSED));
							}
							else if (ltx.getLayoutBlockB()==lb) {
								companion.add(new Integer(Turnout.CLOSED));
							}
							else if ( (tTyp!=LayoutTurnout.LH_XOVER) &&
												(ltx.getLayoutBlockC()==lb) ) {
								companion.add(new Integer(Turnout.THROWN));
							}
							else {
								log.error("Cannot determine turnout setting(A) - "+ltx.getTurnoutName());
								companion.add(new Integer(Turnout.CLOSED));
							}
						}
						else if (ltx.getLayoutBlockB()==lb) {
							if ( (ltx.getLayoutBlock()==nlb) || (ltx.getLayoutBlock()==plb) ) {
								companion.add(new Integer(Turnout.CLOSED));
							}
							else if ( (tTyp!=LayoutTurnout.RH_XOVER) && ((ltx.getLayoutBlockD()==nlb) ||
										(ltx.getLayoutBlockD()==plb) || (ltx.getLayoutBlockD()==lb)) ) {
								companion.add(new Integer(Turnout.THROWN));
							}
							else {
								log.error("Cannot determine turnout setting(B) - "+ltx.getTurnoutName());
								companion.add(new Integer(Turnout.CLOSED));
							}
						}
						else if (ltx.getLayoutBlockC()==lb) {
							if ( (tTyp!=LayoutTurnout.LH_XOVER) && ((ltx.getLayoutBlock()==nlb) ||
										(ltx.getLayoutBlock()==plb)) ) {
								companion.add(new Integer(Turnout.THROWN));
							}
							else if ( (ltx.getLayoutBlockD()==nlb) ||
										(ltx.getLayoutBlockD()==plb) || (ltx.getLayoutBlockD()==lb) ) {
								companion.add(new Integer(Turnout.CLOSED));
							}
							else if ( (tTyp!=LayoutTurnout.LH_XOVER) && (ltx.getLayoutBlockD()==lb) ) {
								companion.add(new Integer(Turnout.THROWN));
							}
							else {
								log.error("Cannot determine turnout setting(C) - "+ltx.getTurnoutName());
								companion.add(new Integer(Turnout.CLOSED));
							}
						}
						else if (ltx.getLayoutBlockD()==lb) {
							if ( (ltx.getLayoutBlockC()==nlb) || (ltx.getLayoutBlockC()==plb) ) {
								companion.add(new Integer(Turnout.CLOSED));
							}
							else if ( (tTyp!=LayoutTurnout.RH_XOVER) && ((ltx.getLayoutBlockB()==nlb) ||
										(ltx.getLayoutBlockB()==plb)) ) {
								companion.add(new Integer(Turnout.THROWN));
							}
							else {
								log.error("Cannot determine turnout setting(D) - "+ltx.getTurnoutName());
								companion.add(new Integer(Turnout.CLOSED));
							}
						}
						break;
				}
			}
			return list;
		}
			
		ArrayList cList = auxTools.getConnectivityList(lb);
		int cType = 0;
		// initialize the connectivity search, processing a turnout in this block if it is present
		boolean notFound = true;
		for (int i=0; (i<cList.size()) && notFound; i++) {
			LayoutConnectivity lc = (LayoutConnectivity)cList.get(i);
			if ( (lc.getXover() != null) && ( ((lc.getBlock1()==lb) && (lc.getBlock2()==plb)) || 
						((lc.getBlock1()==plb) && (lc.getBlock2()==lb)) ) ) {
				// have a block boundary in a crossover turnout, add turnout to the List
				LayoutTurnout xt = lc.getXover();
				int setting = Turnout.THROWN;
				list.add(xt);
				// determine setting and setup track segment if there is one
				tr = null;
				prevConnectObject = (Object)xt;
				switch (lc.getXoverBoundaryType()) {
					case LayoutConnectivity.XOVER_BOUNDARY_AB:
						setting = Turnout.CLOSED;
						if (lb==((TrackSegment)xt.getConnectA()).getLayoutBlock()) {
							// block exits Xover at A
							tr = (TrackSegment)xt.getConnectA();
							prevConnectType = LayoutEditor.TURNOUT_A;
						}
						else if (lb==((TrackSegment)xt.getConnectB()).getLayoutBlock()) {
							// block exits Xover at B
							tr = (TrackSegment)xt.getConnectB();
						prevConnectType = LayoutEditor.TURNOUT_B;
							}
						break;
					case LayoutConnectivity.XOVER_BOUNDARY_CD:
						setting = Turnout.CLOSED;
						if (lb==((TrackSegment)xt.getConnectC()).getLayoutBlock()) {
							// block exits Xover at C
							tr = (TrackSegment)xt.getConnectC();
							prevConnectType = LayoutEditor.TURNOUT_C;
						}
						else if (lb==((TrackSegment)xt.getConnectD()).getLayoutBlock()) {
							// block exits Xover at D
							tr = (TrackSegment)xt.getConnectD();
							prevConnectType = LayoutEditor.TURNOUT_D;
						}
						break;
					case LayoutConnectivity.XOVER_BOUNDARY_AC:
						if (lb==((TrackSegment)xt.getConnectA()).getLayoutBlock()) {
							// block exits Xover at A
							tr = (TrackSegment)xt.getConnectA();
							prevConnectType = LayoutEditor.TURNOUT_A;
						}
						else if (lb==((TrackSegment)xt.getConnectC()).getLayoutBlock()) {
							// block exits Xover at C
							tr = (TrackSegment)xt.getConnectC();
							prevConnectType = LayoutEditor.TURNOUT_C;
						}
						break;
					case LayoutConnectivity.XOVER_BOUNDARY_BD:
						if (lb==((TrackSegment)xt.getConnectB()).getLayoutBlock()) {
							// block exits Xover at B
							tr = (TrackSegment)xt.getConnectB();
							prevConnectType = LayoutEditor.TURNOUT_B;
						}
						else if (lb==((TrackSegment)xt.getConnectD()).getLayoutBlock()) {
							// block exits Xover at D
							tr = (TrackSegment)xt.getConnectD();
							prevConnectType = LayoutEditor.TURNOUT_D;
						}
						break;
				}
				companion.add((Object) new Integer(setting));
				notFound = false;
			} 
			else if ( (lc.getBlock1()==lb) && (lc.getBlock2()==plb) ) {
				// no turnout  or level crossing at the beginning of this block					
				tr = lc.getTrackSegment();
				if (lc.getConnectedType() == LayoutEditor.TRACK) {
					prevConnectType = LayoutEditor.POS_POINT;
					prevConnectObject = (Object)lc.getAnchor();					
				}
				else {
					prevConnectType = lc.getConnectedType();
					prevConnectObject = lc.getConnectedObject();
				}
				notFound = false;
			}
			else if ( (lc.getBlock2()==lb) && (lc.getBlock1()==plb) ) {
				cType = lc.getConnectedType();
				// check for connection to a track segment
				if (cType == LayoutEditor.TRACK) {
					tr = (TrackSegment)lc.getConnectedObject();
					prevConnectType = LayoutEditor.POS_POINT;
					prevConnectObject = (Object)lc.getAnchor();
				}
				// check for a level crossing
				else if ( (cType>=LayoutEditor.LEVEL_XING_A) && (cType<=LayoutEditor.LEVEL_XING_D) ) {
					// entering this Block at a level crossing, skip over it an initialize the next
					//		TrackSegment if there is one in this Block
					setupOpposingTrackSegment((LevelXing)lc.getConnectedObject(), cType);
				}
				// check for turnout
				else if ( (cType>=LayoutEditor.TURNOUT_A) && (cType<=LayoutEditor.TURNOUT_D) ) {
					// add turnout to list
					list.add(lc.getConnectedObject());
					companion.add(getTurnoutSetting((LayoutTurnout)lc.getConnectedObject(), cType));
				}
				notFound = false;
			}
		}
		if (notFound) {
			// could not initialize the connectivity search
			log.error ("Could not find connection between Blocks "+block.getUserName()+" and "+
																prevBlock.getUserName());
			return list;
		}
		// search connectivity for turnouts by following TrackSegments to end of Block		
		while (tr!=null) {
			Object cObject = null;
			// identify next connection
			if ( (tr.getConnect1() == prevConnectObject) && (tr.getType1() == prevConnectType) ) {
				cType = tr.getType2();
				cObject = tr.getConnect2();
			}
			else if ( (tr.getConnect2() == prevConnectObject) && (tr.getType2() == prevConnectType) ) {
				cType = tr.getType1();
				cObject = tr.getConnect1();
			}
			else {
				log.error("Connectivity error when searching turnouts in Block "+lb.getUserName());
				tr = null;
				break;
			}
			if (cType==LayoutEditor.POS_POINT) {
				// reached anchor point or end bumper
				if (((PositionablePoint)cObject).getType() == PositionablePoint.END_BUMPER) {
					// end of line
					tr = null;
				}
				else if (((PositionablePoint)cObject).getType() == PositionablePoint.ANCHOR) {
					// proceed to next track segment if within the same Block
					if (((PositionablePoint)cObject).getConnect1() == tr) {
						tr = ((PositionablePoint)cObject).getConnect2();
					}
					else {
						tr = ((PositionablePoint)cObject).getConnect1();
					}
					if (tr.getLayoutBlock() != lb) {
						// track segment is not in this block
						tr = null;
					}
					else {
						prevConnectType = cType;
						prevConnectObject = cObject;
					}
				}
			}
			else if ( (cType>=LayoutEditor.LEVEL_XING_A) && (cType<=LayoutEditor.LEVEL_XING_D) ) {
				// reached a level crossing, is it within this block?
				if ( (cType==LayoutEditor.LEVEL_XING_A) || (cType==LayoutEditor.LEVEL_XING_C) ) {
					if (((LevelXing)cObject).getLayoutBlockAC()!=lb) {
						// outside of block
						tr = null;
					}
					else {
						// same block
						setupOpposingTrackSegment((LevelXing)cObject, cType);
					}
				}
				else {
					if (((LevelXing)cObject).getLayoutBlockBD()!=lb) {
						// outside of block
						tr = null;
					}
					else {
						// same block
						setupOpposingTrackSegment((LevelXing)cObject, cType);
					}
				}
			}
			else if ( (cType>=LayoutEditor.TURNOUT_A) && (cType<=LayoutEditor.TURNOUT_D) ) {
				// reached a turnout
				LayoutTurnout lt = (LayoutTurnout)cObject;
				int tType = lt.getTurnoutType();
				// is this turnout a crossover turnout at least partly within this block?
				if ( (tType==LayoutTurnout.DOUBLE_XOVER) || (tType==LayoutTurnout.RH_XOVER) ||
							(tType==LayoutTurnout.LH_XOVER) ) {
					// reached a crossover turnout
					switch (cType) {
						case LayoutEditor.TURNOUT_A:
							if ((lt.getLayoutBlock())!=lb) {
								// connection is outside of the current block
								tr = null;
							}
							else if (lt.getLayoutBlockB()==nlb) {
								// exits Block at B
								list.add(cObject);
								companion.add( new Integer(Turnout.CLOSED));
								tr = null;
							}
							else if ( (lt.getLayoutBlockC()==nlb) && (tType!=LayoutTurnout.LH_XOVER) ) {
								// exits Block at C, either Double or RH
								list.add(cObject);
								companion.add( new Integer(Turnout.THROWN));
								tr = null;
							}								
							else if (lt.getLayoutBlockB()==lb) {
								// block continues at B
								list.add(cObject);
								companion.add( new Integer(Turnout.CLOSED));
								tr = (TrackSegment)lt.getConnectB();
								prevConnectType = LayoutEditor.TURNOUT_B;
								prevConnectObject = cObject;
							}
							else if ( (lt.getLayoutBlockC()==lb) && (tType!=LayoutTurnout.LH_XOVER) ) {
								// block continues at C, either Double or RH
								list.add(cObject);
								companion.add( new Integer(Turnout.THROWN));
								tr = (TrackSegment)lt.getConnectC();
								prevConnectType = LayoutEditor.TURNOUT_C;
								prevConnectObject = cObject;
							}
							else {
								// no legal outcome found, print error
								log.error("Connectivity mismatch at A in turnout "+lt.getTurnoutName());
								tr = null;
							}
							break;
						case LayoutEditor.TURNOUT_B:
							if ((lt.getLayoutBlockB())!=lb) {
								// connection is outside of the current block
								tr = null;
							}
							else if (lt.getLayoutBlock()==nlb) {
								// exits Block at A
								list.add(cObject);
								companion.add( new Integer(Turnout.CLOSED));
								tr = null;
							}
							else if ( (lt.getLayoutBlockD()==nlb) && (tType!=LayoutTurnout.RH_XOVER) ) {
								// exits Block at D, either Double or LH
								list.add(cObject);
								companion.add( new Integer(Turnout.THROWN));
								tr = null;
							}								
							else if (lt.getLayoutBlock()==lb) {
								// block continues at A
								list.add(cObject);
								companion.add( new Integer(Turnout.CLOSED));
								tr = (TrackSegment)lt.getConnectA();
								prevConnectType = LayoutEditor.TURNOUT_A;
								prevConnectObject = cObject;
							}
							else if ( (lt.getLayoutBlockD()==lb) && (tType!=LayoutTurnout.RH_XOVER) ) {
								// block continues at D, either Double or LH
								list.add(cObject);
								companion.add( new Integer(Turnout.THROWN));
								tr = (TrackSegment)lt.getConnectD();
								prevConnectType = LayoutEditor.TURNOUT_D;
								prevConnectObject = cObject;
							}
							else {
								// no legal outcome found, print error
								log.error("Connectivity mismatch at B in turnout "+lt.getTurnoutName());
								tr = null;
							}
							break;
						case LayoutEditor.TURNOUT_C:
							if ((lt.getLayoutBlockC())!=lb) {
								// connection is outside of the current block
								tr = null;
							}
							else if (lt.getLayoutBlockD()==nlb) {
								// exits Block at D
								list.add(cObject);
								companion.add( new Integer(Turnout.CLOSED));
								tr = null;
							}
							else if ( (lt.getLayoutBlock()==nlb) && (tType!=LayoutTurnout.LH_XOVER) ) {
								// exits Block at A, either Double or RH
								list.add(cObject);
								companion.add( new Integer(Turnout.THROWN));
								tr = null;
							}								
							else if (lt.getLayoutBlockD()==lb) {
								// block continues at D
								list.add(cObject);
								companion.add( new Integer(Turnout.CLOSED));
								tr = (TrackSegment)lt.getConnectD();
								prevConnectType = LayoutEditor.TURNOUT_D;
								prevConnectObject = cObject;
							}
							else if ( (lt.getLayoutBlock()==lb) && (tType!=LayoutTurnout.LH_XOVER) ) {
								// block continues at A, either Double or RH
								list.add(cObject);
								companion.add( new Integer(Turnout.THROWN));
								tr = (TrackSegment)lt.getConnectA();
								prevConnectType = LayoutEditor.TURNOUT_A;
								prevConnectObject = cObject;
							}
							else {
								// no legal outcome found, print error
								log.error("Connectivity mismatch at C in turnout "+lt.getTurnoutName());
								tr = null;
							}
							break;
						case LayoutEditor.TURNOUT_D:
							if ((lt.getLayoutBlockD())!=lb) {
								// connection is outside of the current block
								tr = null;
							}
							else if (lt.getLayoutBlockC()==nlb) {
								// exits Block at C
								list.add(cObject);
								companion.add( new Integer(Turnout.CLOSED));
								tr = null;
							}
							else if ( (lt.getLayoutBlockB()==nlb) && (tType!=LayoutTurnout.RH_XOVER) ) {
								// exits Block at B, either Double or LH
								list.add(cObject);
								companion.add( new Integer(Turnout.THROWN));
								tr = null;
							}								
							else if (lt.getLayoutBlockC()==lb) {
								// block continues at C
								list.add(cObject);
								companion.add( new Integer(Turnout.CLOSED));
								tr = (TrackSegment)lt.getConnectC();
								prevConnectType = LayoutEditor.TURNOUT_C;
								prevConnectObject = cObject;
							}
							else if ( (lt.getLayoutBlockB()==lb) && (tType!=LayoutTurnout.RH_XOVER) ) {
								// block continues at B, either Double or LH
								list.add(cObject);
								companion.add( new Integer(Turnout.THROWN));
								tr = (TrackSegment)lt.getConnectB();
								prevConnectType = LayoutEditor.TURNOUT_B;
								prevConnectObject = cObject;
							}
							else {
								// no legal outcome found, print error
								log.error("Connectivity mismatch at D in turnout "+lt.getTurnoutName());
								tr = null;
							}
							break;
					}
				}
				else if ( (tType==LayoutTurnout.RH_TURNOUT) || (tType==LayoutTurnout.LH_TURNOUT) ||
							(tType==LayoutTurnout.WYE_TURNOUT) ) {
					// reached RH. LH, or WYE turnout, is it in the current Block?
					if (lt.getLayoutBlock()!=lb) {
						// turnout is outside of current block
						tr = null;
					}
					else {
						// turnout is inside current block, add it to the list 
						list.add(cObject);
						companion.add(getTurnoutSetting(lt, cType));
					}
				}
			}
		}
		return list;
	}
	/**
	 * Returns a list of turnout settings (as Integer Objects) to accomplish the transition through
	 *  the Block specified in 'getTurnoutList'.  Settings and Turnouts are in sync by position in 
	 *  the returned list.
	 */
	public ArrayList getTurnoutSettingList() { return companion; }
	/**
	 * Initializes the setting (as an object), sets the new track segment (if in Block), and sets the
	 *    prevConnectType. 
	 */
	private Object getTurnoutSetting(LayoutTurnout lt, int cType) {
		prevConnectObject = (Object)lt;
		int setting = Turnout.THROWN;
		switch (cType) {
			case LayoutEditor.TURNOUT_A:
				// entering at throat, determine exit by checking block of connected track segment				
				if (nlb==((TrackSegment)lt.getConnectB()).getLayoutBlock()) {
					// exiting block at continuing track
					prevConnectType = LayoutEditor.TURNOUT_B;
					setting = Turnout.CLOSED;
					tr = (TrackSegment)lt.getConnectB();
				}
				else if (nlb==((TrackSegment)lt.getConnectC()).getLayoutBlock()) {
					// exiting block at diverging track
					prevConnectType = LayoutEditor.TURNOUT_C;
					tr = (TrackSegment)lt.getConnectC();
				}
				// must stay in block after turnout
				else if (lb==((TrackSegment)lt.getConnectB()).getLayoutBlock()) {
					// continuing in block on continuing track
					prevConnectType = LayoutEditor.TURNOUT_B;
					setting = Turnout.CLOSED;
					tr = (TrackSegment)lt.getConnectB();
				}
				else if (lb==((TrackSegment)lt.getConnectC()).getLayoutBlock()) {
					// continuing in block on diverging track
					prevConnectType = LayoutEditor.TURNOUT_C;
					tr = (TrackSegment)lt.getConnectC();
				}
				break;
			case LayoutEditor.TURNOUT_B:
				// entering at continuing track, must exit at throat
				prevConnectType = LayoutEditor.TURNOUT_A;				
				setting = Turnout.CLOSED;
				tr = (TrackSegment)lt.getConnectA();
				break;
			case LayoutEditor.TURNOUT_C:
				// entering at diverging track, must exit at throat
				prevConnectType = LayoutEditor.TURNOUT_A;				
				tr = (TrackSegment)lt.getConnectA();
				break;
		}
		if ( (tr!=null) && (tr.getLayoutBlock() != lb) ) {
			// continuing track segment is not in this block
			tr = null;
		}
		else if (tr==null) {
			log.error("Connectivity not complete at turnout "+lt.getTurnoutName());
		}
		if (lt.getContinuingSense() != Turnout.CLOSED) {
			if (setting == Turnout.THROWN) setting = Turnout.CLOSED;
			else if (setting == Turnout.CLOSED) setting = Turnout.THROWN;
		}
		return ((Object) new Integer(setting));
	}
	private void setupOpposingTrackSegment(LevelXing x, int cType) {
		switch (cType) {
			case LayoutEditor.LEVEL_XING_A:
				tr = (TrackSegment)x.getConnectC();
				prevConnectType = LayoutEditor.LEVEL_XING_C;
				break;
			case LayoutEditor.LEVEL_XING_B:
				tr = (TrackSegment)x.getConnectD();
				prevConnectType = LayoutEditor.LEVEL_XING_D;
				break;
			case LayoutEditor.LEVEL_XING_C:
				tr = (TrackSegment)x.getConnectA();
				prevConnectType = LayoutEditor.LEVEL_XING_A;
				break;
			case LayoutEditor.LEVEL_XING_D:
				tr = (TrackSegment)x.getConnectB();
				prevConnectType = LayoutEditor.LEVEL_XING_B;
				break;
		}
		if (tr.getLayoutBlock() != lb) {
			// track segment is not in this block
			tr = null;
		}
		else {
			// track segment is in this block
			prevConnectObject = (Object)x;
		}
	}
	public ArrayList getAllTurnoutsThisBlock(LayoutBlock lb) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < layoutEditor.turnoutList.size(); i++) {
			LayoutTurnout lt = (LayoutTurnout)layoutEditor.turnoutList.get(i);
			if ( (lt.getLayoutBlock()==lb) || (lt.getLayoutBlockB()==lb) ||
					(lt.getLayoutBlockC()==lb) || (lt.getLayoutBlockD()==lb) ) {
				list.add((Object)lt);
			}		
		}		
		return list;
	}


	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConnectivityUtil.class.getName());
}
