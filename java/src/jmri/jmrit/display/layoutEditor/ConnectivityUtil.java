// jmri.jmrit.display.ConnectivityUtil.java
package jmri.jmrit.display.layoutEditor;

import java.util.ResourceBundle;
import java.util.ArrayList;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.EntryPoint;
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
 * @version $Revision$
 */

public class ConnectivityUtil 
{

	// Defined text resource
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");
	
	// constants
	
	// operational instance variables 
	private LayoutEditor layoutEditor = null;
	private LayoutEditorAuxTools auxTools = null;
	private LayoutBlockManager layoutBlockManager = null;
	private LayoutEditorTools leTools = null;
	
	// constructor method
	public ConnectivityUtil(LayoutEditor thePanel) {
		layoutEditor = thePanel;
		auxTools = new LayoutEditorAuxTools(layoutEditor);
		leTools = layoutEditor.getLETools();
		layoutBlockManager = InstanceManager.layoutBlockManagerInstance();
	}

    private ArrayList<Integer> companion = null;
	private TrackSegment tr = null;
	private int prevConnectType = 0;
	private	Object prevConnectObject = null;
	LayoutBlock lb = null;
	LayoutBlock nlb = null;
	LayoutBlock plb = null;

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
    public ArrayList<LayoutTurnout> getTurnoutList(Block block, Block prevBlock, Block nextBlock) {
        turnoutConnectivity = true;
		ArrayList<LayoutTurnout> list = new ArrayList<LayoutTurnout>();
		companion = new ArrayList<Integer>();
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
			ArrayList<LayoutTurnout> allTurnouts = getAllTurnoutsThisBlock(lb);
			for (int i = 0; i<allTurnouts.size(); i++) {
				LayoutTurnout ltx = allTurnouts.get(i);
                int tTyp = ltx.getTurnoutType();
                list.add(ltx);
                if(ltx instanceof LayoutSlip){
                    LayoutSlip ls = (LayoutSlip) ltx;
                    if(((TrackSegment)ls.getConnectA()).getLayoutBlock()==lb){
                        if(((TrackSegment)ls.getConnectC()).getLayoutBlock()==nlb ||
                            ((TrackSegment)ls.getConnectC()).getLayoutBlock()==plb){
                                companion.add(Integer.valueOf(LayoutSlip.STATE_AC));
                        }
                        else if(((TrackSegment)ls.getConnectD()).getLayoutBlock()==nlb ||
                            ((TrackSegment)ls.getConnectD()).getLayoutBlock()==plb){
                                companion.add(Integer.valueOf(LayoutSlip.STATE_AD));
                        } else if (((TrackSegment)ls.getConnectC()).getLayoutBlock()==lb){
                            companion.add(Integer.valueOf(LayoutSlip.STATE_AC));
                        } else if (((TrackSegment)ls.getConnectD()).getLayoutBlock()==lb){
                            companion.add(Integer.valueOf(LayoutSlip.STATE_AD));
                        } else {
                            log.error("Cannot determine slip setting " + ls.getName());
                            companion.add(Integer.valueOf(LayoutSlip.UNKNOWN));
                        }
                    } 
                    else if (((TrackSegment)ls.getConnectB()).getLayoutBlock()==lb){
                        if(tTyp==LayoutSlip.DOUBLE_SLIP){
                            if(((TrackSegment)ls.getConnectD()).getLayoutBlock()==nlb ||
                                ((TrackSegment)ls.getConnectD()).getLayoutBlock()==plb){
                                    companion.add(Integer.valueOf(LayoutSlip.STATE_BD));
                            } 
                            else if(((TrackSegment)ls.getConnectC()).getLayoutBlock()==nlb ||
                                ((TrackSegment)ls.getConnectC()).getLayoutBlock()==plb){
                                    companion.add(Integer.valueOf(LayoutSlip.STATE_BC));
                            } 
                            else if (((TrackSegment)ls.getConnectD()).getLayoutBlock()==lb){
                                companion.add(Integer.valueOf(LayoutSlip.STATE_BD));
                            } else if (((TrackSegment)ls.getConnectC()).getLayoutBlock()==lb){
                                companion.add(Integer.valueOf(LayoutSlip.STATE_BC));
                            }
                            else {
                                log.error("Cannot determine slip setting " + ls.getName());
                                companion.add(Integer.valueOf(LayoutSlip.UNKNOWN));
                            }
                        
                        } else {
                            if(((TrackSegment)ls.getConnectD()).getLayoutBlock()==nlb ||
                                ((TrackSegment)ls.getConnectD()).getLayoutBlock()==plb){
                                    companion.add(Integer.valueOf(LayoutSlip.STATE_BD));
                            } else if (((TrackSegment)ls.getConnectD()).getLayoutBlock()==lb){
                                companion.add(Integer.valueOf(LayoutSlip.STATE_BD));
                            } else {
                                log.error("Cannot determine slip setting " + ls.getName());
                                companion.add(Integer.valueOf(LayoutSlip.UNKNOWN));
                            }
                        }
                    }
                    else if (((TrackSegment)ls.getConnectC()).getLayoutBlock()==lb){
                        if(tTyp==LayoutSlip.DOUBLE_SLIP){
                            if(((TrackSegment)ls.getConnectA()).getLayoutBlock()==nlb ||
                                ((TrackSegment)ls.getConnectA()).getLayoutBlock()==plb){
                                    companion.add(Integer.valueOf(LayoutSlip.STATE_AC));
                            } 
                            else if(((TrackSegment)ls.getConnectB()).getLayoutBlock()==nlb ||
                                ((TrackSegment)ls.getConnectB()).getLayoutBlock()==plb){
                                    companion.add(Integer.valueOf(LayoutSlip.STATE_BC));
                            } 
                            else if (((TrackSegment)ls.getConnectA()).getLayoutBlock()==lb){
                                companion.add(Integer.valueOf(LayoutSlip.STATE_AC));
                            } else if (((TrackSegment)ls.getConnectB()).getLayoutBlock()==lb){
                                companion.add(Integer.valueOf(LayoutSlip.STATE_BC));
                            }
                            else {
                                log.error("Cannot determine slip setting " + ls.getName());
                                companion.add(Integer.valueOf(LayoutSlip.UNKNOWN));
                            }
                        
                        } else {
                            if(((TrackSegment)ls.getConnectA()).getLayoutBlock()==nlb ||
                                ((TrackSegment)ls.getConnectA()).getLayoutBlock()==plb){
                                    companion.add(Integer.valueOf(LayoutSlip.STATE_AC));
                            } else if (((TrackSegment)ls.getConnectA()).getLayoutBlock()==lb){
                                companion.add(Integer.valueOf(LayoutSlip.STATE_AC));
                            } else {
                                log.error("Cannot determine slip setting " + ls.getName());
                                companion.add(Integer.valueOf(LayoutSlip.UNKNOWN));
                            }
                        }
                    }
                    else if (((TrackSegment)ls.getConnectD()).getLayoutBlock()==lb){
                        if(((TrackSegment)ls.getConnectA()).getLayoutBlock()==nlb ||
                            ((TrackSegment)ls.getConnectA()).getLayoutBlock()==plb){
                                companion.add(Integer.valueOf(LayoutSlip.STATE_AD));
                        }
                        else if(((TrackSegment)ls.getConnectB()).getLayoutBlock()==nlb ||
                            ((TrackSegment)ls.getConnectB()).getLayoutBlock()==plb){
                                companion.add(Integer.valueOf(LayoutSlip.STATE_BD));
                        } else if (((TrackSegment)ls.getConnectA()).getLayoutBlock()==lb){
                            companion.add(Integer.valueOf(LayoutSlip.STATE_AD));
                        } else if (((TrackSegment)ls.getConnectB()).getLayoutBlock()==lb){
                            companion.add(Integer.valueOf(LayoutSlip.STATE_AD));
                        } else {
                            log.error("Cannot determine slip setting " + ls.getName());
                            companion.add(Integer.valueOf(LayoutSlip.UNKNOWN));
                        }
                    }
                    else {
                        log.error("Cannot determine turnout setting for "+ls.getName());
                        companion.add(Integer.valueOf(LayoutSlip.UNKNOWN));
                    }
                    break;
                    
                    
                     
                } else {
                    //list.add(ltx);
                    //int tTyp = ltx.getTurnoutType();
                    switch (tTyp) {
                        case LayoutTurnout.RH_TURNOUT:
                        case LayoutTurnout.LH_TURNOUT:
                        case LayoutTurnout.WYE_TURNOUT:
                            if (((TrackSegment)ltx.getConnectA()).getLayoutBlock()==lb) {
                                if ( (((TrackSegment)ltx.getConnectC()).getLayoutBlock()==nlb) ||
                                        (((TrackSegment)ltx.getConnectC()).getLayoutBlock()==plb) ) {
                                    companion.add(Integer.valueOf(Turnout.THROWN));
                                }
                                else if ( (((TrackSegment)ltx.getConnectB()).getLayoutBlock()==nlb) ||
                                        (((TrackSegment)ltx.getConnectB()).getLayoutBlock()==plb) ) {
                                    companion.add(Integer.valueOf(Turnout.CLOSED));
                                }
                                else if (((TrackSegment)ltx.getConnectB()).getLayoutBlock()==lb) {
                                    companion.add(Integer.valueOf(Turnout.CLOSED));
                                }
                                else if (((TrackSegment)ltx.getConnectC()).getLayoutBlock()==lb) {
                                    companion.add(Integer.valueOf(Turnout.THROWN));
                                }
                                else {
                                    log.error("Cannot determine turnout setting - "+ltx.getTurnoutName());
                                    companion.add(Integer.valueOf(Turnout.CLOSED));
                                }
                            }
                            else if (((TrackSegment)ltx.getConnectB()).getLayoutBlock()==lb) {
                                companion.add(Integer.valueOf(Turnout.CLOSED));
                            }
                            else if (((TrackSegment)ltx.getConnectC()).getLayoutBlock()==lb) {
                                companion.add(Integer.valueOf(Turnout.THROWN));
                            }
                            else {
                                log.error("Cannot determine turnout setting for "+ltx.getTurnoutName());
                                companion.add(Integer.valueOf(Turnout.CLOSED));
                            }
                            break;
                        case LayoutTurnout.RH_XOVER:
                        case LayoutTurnout.LH_XOVER:
                        case LayoutTurnout.DOUBLE_XOVER:
                            if (ltx.getLayoutBlock()==lb) {
                                if ( (tTyp!=LayoutTurnout.LH_XOVER) && ((ltx.getLayoutBlockC()==nlb) ||
                                            (ltx.getLayoutBlockC()==plb)) ) {
                                    companion.add(Integer.valueOf(Turnout.THROWN));
                                }
                                else if ( (ltx.getLayoutBlockB()==nlb) ||
                                            (ltx.getLayoutBlockB()==plb) ) {
                                    companion.add(Integer.valueOf(Turnout.CLOSED));
                                }
                                else if (ltx.getLayoutBlockB()==lb) {
                                    companion.add(Integer.valueOf(Turnout.CLOSED));
                                }
                                else if ( (tTyp!=LayoutTurnout.LH_XOVER) &&
                                                    (ltx.getLayoutBlockC()==lb) ) {
                                    companion.add(Integer.valueOf(Turnout.THROWN));
                                }
                                else {
                                    log.error("Cannot determine turnout setting(A) - "+ltx.getTurnoutName());
                                    companion.add(Integer.valueOf(Turnout.CLOSED));
                                }
                            }
                            else if (ltx.getLayoutBlockB()==lb) {
                                if ( (ltx.getLayoutBlock()==nlb) || (ltx.getLayoutBlock()==plb) ) {
                                    companion.add(Integer.valueOf(Turnout.CLOSED));
                                }
                                else if ( (tTyp!=LayoutTurnout.RH_XOVER) && ((ltx.getLayoutBlockD()==nlb) ||
                                            (ltx.getLayoutBlockD()==plb) || (ltx.getLayoutBlockD()==lb)) ) {
                                    companion.add(Integer.valueOf(Turnout.THROWN));
                                }
                                else {
                                    log.error("Cannot determine turnout setting(B) - "+ltx.getTurnoutName());
                                    companion.add(Integer.valueOf(Turnout.CLOSED));
                                }
                            }
                            else if (ltx.getLayoutBlockC()==lb) {
                                if ( (tTyp!=LayoutTurnout.LH_XOVER) && ((ltx.getLayoutBlock()==nlb) ||
                                            (ltx.getLayoutBlock()==plb)) ) {
                                    companion.add(Integer.valueOf(Turnout.THROWN));
                                }
                                else if ( (ltx.getLayoutBlockD()==nlb) ||
                                            (ltx.getLayoutBlockD()==plb) || (ltx.getLayoutBlockD()==lb) ) {
                                    companion.add(Integer.valueOf(Turnout.CLOSED));
                                }
                                else if ( (tTyp!=LayoutTurnout.LH_XOVER) && (ltx.getLayoutBlockD()==lb) ) {
                                    companion.add(Integer.valueOf(Turnout.THROWN));
                                }
                                else {
                                    log.error("Cannot determine turnout setting(C) - "+ltx.getTurnoutName());
                                    companion.add(Integer.valueOf(Turnout.CLOSED));
                                }
                            }
                            else if (ltx.getLayoutBlockD()==lb) {
                                if ( (ltx.getLayoutBlockC()==nlb) || (ltx.getLayoutBlockC()==plb) ) {
                                    companion.add(Integer.valueOf(Turnout.CLOSED));
                                }
                                else if ( (tTyp!=LayoutTurnout.RH_XOVER) && ((ltx.getLayoutBlockB()==nlb) ||
                                            (ltx.getLayoutBlockB()==plb)) ) {
                                    companion.add(Integer.valueOf(Turnout.THROWN));
                                }
                                else {
                                    log.error("Cannot determine turnout setting(D) - "+ltx.getTurnoutName());
                                    companion.add(Integer.valueOf(Turnout.CLOSED));
                                }
                            }
                            break;
                    }
                }
			}
			return list;
		}
			
		ArrayList<LayoutConnectivity> cList = auxTools.getConnectivityList(lb);
		int cType = 0;
		// initialize the connectivity search, processing a turnout in this block if it is present
		boolean notFound = true;
		for (int i=0; (i<cList.size()) && notFound; i++) {
			LayoutConnectivity lc = cList.get(i);
			if ( (lc.getXover() != null) && ( ((lc.getBlock1()==lb) && (lc.getBlock2()==plb)) || 
						((lc.getBlock1()==plb) && (lc.getBlock2()==lb)) ) ) {
				// have a block boundary in a crossover turnout, add turnout to the List
				LayoutTurnout xt = lc.getXover();
				int setting = Turnout.THROWN;
				list.add(xt);
				// determine setting and setup track segment if there is one
				tr = null;
				prevConnectObject = xt;
				switch (lc.getXoverBoundaryType()) {
					case LayoutConnectivity.XOVER_BOUNDARY_AB:
						setting = Turnout.CLOSED;
						if (((TrackSegment)xt.getConnectA()!=null) && (lb==((TrackSegment)xt.getConnectA()).getLayoutBlock())) {
							// block exits Xover at A
							tr = (TrackSegment)xt.getConnectA();
							prevConnectType = LayoutEditor.TURNOUT_A;
						}
						else if (((TrackSegment)xt.getConnectB()!=null) && (lb==((TrackSegment)xt.getConnectB()).getLayoutBlock())) {
							// block exits Xover at B
							tr = (TrackSegment)xt.getConnectB();
                            prevConnectType = LayoutEditor.TURNOUT_B;
							}
						break;
					case LayoutConnectivity.XOVER_BOUNDARY_CD:
						setting = Turnout.CLOSED;
						if (((TrackSegment)xt.getConnectC()!=null) && (lb==((TrackSegment)xt.getConnectC()).getLayoutBlock())) {
							// block exits Xover at C
							tr = (TrackSegment)xt.getConnectC();
							prevConnectType = LayoutEditor.TURNOUT_C;
						}
						else if (((TrackSegment)xt.getConnectD()!=null) && (lb==((TrackSegment)xt.getConnectD()).getLayoutBlock())) {
							// block exits Xover at D
							tr = (TrackSegment)xt.getConnectD();
							prevConnectType = LayoutEditor.TURNOUT_D;
						}
						break;
					case LayoutConnectivity.XOVER_BOUNDARY_AC:
						if (((TrackSegment)xt.getConnectA()!=null) && (lb==((TrackSegment)xt.getConnectA()).getLayoutBlock())) {
							// block exits Xover at A
							tr = (TrackSegment)xt.getConnectA();
							prevConnectType = LayoutEditor.TURNOUT_A;
						}
						else if (((TrackSegment)xt.getConnectC()!=null) && (lb==((TrackSegment)xt.getConnectC()).getLayoutBlock())) {
							// block exits Xover at C
							tr = (TrackSegment)xt.getConnectC();
							prevConnectType = LayoutEditor.TURNOUT_C;
						}
						break;
					case LayoutConnectivity.XOVER_BOUNDARY_BD:
						if (((TrackSegment)xt.getConnectB()!=null) && (lb==((TrackSegment)xt.getConnectB()).getLayoutBlock())) {
							// block exits Xover at B
							tr = (TrackSegment)xt.getConnectB();
							prevConnectType = LayoutEditor.TURNOUT_B;
						}
						else if (((TrackSegment)xt.getConnectD()!=null) && (lb==((TrackSegment)xt.getConnectD()).getLayoutBlock())) {
							// block exits Xover at D
							tr = (TrackSegment)xt.getConnectD();
							prevConnectType = LayoutEditor.TURNOUT_D;
						}
						break;
                    default:  break;
				}
				companion.add(Integer.valueOf(setting));
				notFound = false;
			} 
			else if ( (lc.getBlock1()==lb) && (lc.getBlock2()==plb) ) {
				// no turnout  or level crossing at the beginning of this block					
				tr = lc.getTrackSegment();
				if (lc.getConnectedType() == LayoutEditor.TRACK) {
					prevConnectType = LayoutEditor.POS_POINT;
					prevConnectObject = lc.getAnchor();					
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
					prevConnectObject = lc.getAnchor();
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
					list.add((LayoutTurnout)lc.getConnectedObject());
					companion.add(getTurnoutSetting((LayoutTurnout)lc.getConnectedObject(), cType));
				} 
                else if ((cType>=LayoutEditor.SLIP_A) && (cType<=LayoutEditor.SLIP_D)){
                    list.add((LayoutSlip)lc.getConnectedObject());
                    companion.add(getTurnoutSetting((LayoutSlip)lc.getConnectedObject(), cType));
                }
                notFound = false;
			}
		}
		if (notFound) {
			if (prevBlock!=null)
				// could not initialize the connectivity search
				log.error ("Could not find connection between Blocks "+block.getUserName()+" and "+
						prevBlock.getUserName());
			else
				log.error("Could not find connection between Blocks "+block.getUserName()+", prevBock is null!");
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
					if ((tr==null) || (tr.getLayoutBlock() != lb)) {
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
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.CLOSED));
								tr = null;
							}
							else if ( (lt.getLayoutBlockC()==nlb) && (tType!=LayoutTurnout.LH_XOVER) ) {
								// exits Block at C, either Double or RH
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.THROWN));
								tr = null;
							}								
							else if (lt.getLayoutBlockB()==lb) {
								// block continues at B
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.CLOSED));
								tr = (TrackSegment)lt.getConnectB();
								prevConnectType = LayoutEditor.TURNOUT_B;
								prevConnectObject = cObject;
							}
							else if ( (lt.getLayoutBlockC()==lb) && (tType!=LayoutTurnout.LH_XOVER) ) {
								// block continues at C, either Double or RH
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.THROWN));
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
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.CLOSED));
								tr = null;
							}
							else if ( (lt.getLayoutBlockD()==nlb) && (tType!=LayoutTurnout.RH_XOVER) ) {
								// exits Block at D, either Double or LH
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.THROWN));
								tr = null;
							}								
							else if (lt.getLayoutBlock()==lb) {
								// block continues at A
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.CLOSED));
								tr = (TrackSegment)lt.getConnectA();
								prevConnectType = LayoutEditor.TURNOUT_A;
								prevConnectObject = cObject;
							}
							else if ( (lt.getLayoutBlockD()==lb) && (tType!=LayoutTurnout.RH_XOVER) ) {
								// block continues at D, either Double or LH
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.THROWN));
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
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.CLOSED));
								tr = null;
							}
							else if ( (lt.getLayoutBlock()==nlb) && (tType!=LayoutTurnout.LH_XOVER) ) {
								// exits Block at A, either Double or RH
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.THROWN));
								tr = null;
							}								
							else if (lt.getLayoutBlockD()==lb) {
								// block continues at D
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.CLOSED));
								tr = (TrackSegment)lt.getConnectD();
								prevConnectType = LayoutEditor.TURNOUT_D;
								prevConnectObject = cObject;
							}
							else if ( (lt.getLayoutBlock()==lb) && (tType!=LayoutTurnout.LH_XOVER) ) {
								// block continues at A, either Double or RH
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.THROWN));
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
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.CLOSED));
								tr = null;
							}
							else if ( (lt.getLayoutBlockB()==nlb) && (tType!=LayoutTurnout.RH_XOVER) ) {
								// exits Block at B, either Double or LH
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.THROWN));
								tr = null;
							}								
							else if (lt.getLayoutBlockC()==lb) {
								// block continues at C
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.CLOSED));
								tr = (TrackSegment)lt.getConnectC();
								prevConnectType = LayoutEditor.TURNOUT_C;
								prevConnectObject = cObject;
							}
							else if ( (lt.getLayoutBlockB()==lb) && (tType!=LayoutTurnout.RH_XOVER) ) {
								// block continues at B, either Double or LH
								list.add((LayoutTurnout)cObject);
								companion.add( Integer.valueOf(Turnout.THROWN));
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
                        list.add((LayoutTurnout)cObject);
                        companion.add(getTurnoutSetting(lt, cType));
					}
				}
			} 
            else if ((cType>=LayoutEditor.SLIP_A) && (cType<=LayoutEditor.SLIP_D)){
                // reached a LayoutSlip
				LayoutSlip ls = (LayoutSlip)cObject;
                if(ls.getLayoutBlock()!=lb){
                    //Slip is outside of the current block
                    tr=null;
                } else {
                    // turnout is inside current block, add it to the list 
                    list.add(ls);
                    companion.add(getTurnoutSetting(ls, cType));
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
	public ArrayList<Integer> getTurnoutSettingList() { return companion; }
	
	/** 
	 * Returns a list of all Blocks connected to a specified Block
	 */
	public ArrayList<Block> getConnectedBlocks(Block block) {	
		ArrayList<Block> list = new ArrayList<Block>();
		lb = layoutBlockManager.getByUserName(block.getUserName());
		ArrayList<LayoutConnectivity> cList = auxTools.getConnectivityList(lb);
		for (int i = 0; i<cList.size(); i++) {
			LayoutConnectivity lc = cList.get(i);
			if (lc.getBlock1().getBlock()==block) {
				list.add((lc.getBlock2()).getBlock());
			}
			else if (lc.getBlock2().getBlock()==block) {
				list.add((lc.getBlock1()).getBlock());
			}
		}						
		return list;
	}
	
	/** 
	 * Returns a list of all anchor point boundaries involving the specified Block
	 */
	public ArrayList<PositionablePoint> getAnchorBoundariesThisBlock(Block block) {
		ArrayList<PositionablePoint> list = new ArrayList<PositionablePoint>();
		LayoutBlock lBlock = layoutBlockManager.getByUserName(block.getUserName());
		for (int i = 0; i<layoutEditor.pointList.size(); i++) {
			PositionablePoint p = layoutEditor.pointList.get(i);                     
			if ((p.getConnect2()!=null) && (p.getConnect1()!=null)) {
                                if ((p.getConnect2().getLayoutBlock()!=null) && (p.getConnect1().getLayoutBlock()!=null)) {
                                    if ( (((p.getConnect1()).getLayoutBlock()==lBlock) && ((p.getConnect2()).getLayoutBlock()!=lBlock)) ||
                                            (((p.getConnect1()).getLayoutBlock()!=lBlock) && ((p.getConnect2()).getLayoutBlock()==lBlock)) ) {
                                            list.add(p);
                                    }
                                }
			}
		}
		return list;
	}
	
	/** 
	 * Returns a list of all levelXings involving the specified Block. To be returned, a levelXing must have all its
	 *		four connections and all blocks must be assigned.  If any connection is missing, or if a block assignmnet 
	 *		is missing, an error message is printed and the level crossing is not added to the list.
	 */
	public ArrayList<LevelXing> getLevelCrossingsThisBlock(Block block) {	
		ArrayList<LevelXing> list = new ArrayList<LevelXing>();
		LayoutBlock lBlock = layoutBlockManager.getByUserName(block.getUserName());
		for (int i = 0; i<layoutEditor.xingList.size(); i++) {
			LevelXing x = layoutEditor.xingList.get(i);
			boolean found = false;
			if ( (x.getLayoutBlockAC()==lBlock) || (x.getLayoutBlockBD()==lBlock) ) found = true;
			else if ( (x.getConnectA()!=null) && (((TrackSegment)x.getConnectA()).getLayoutBlock()==lBlock) )
				found = true;
			else if ( (x.getConnectB()!=null) && (((TrackSegment)x.getConnectB()).getLayoutBlock()==lBlock) )
				found = true;
			else if ( (x.getConnectC()!=null) && (((TrackSegment)x.getConnectC()).getLayoutBlock()==lBlock) ) 
				found = true;
			else if ( (x.getConnectD()!=null) && (((TrackSegment)x.getConnectD()).getLayoutBlock()==lBlock) ) 
				found = true;
			if (found) {
				if ( (x.getConnectA()!=null) && (((TrackSegment)x.getConnectA()).getLayoutBlock()!=null) &&
						(x.getConnectB()!=null) && (((TrackSegment)x.getConnectB()).getLayoutBlock()!=null) &&				
						(x.getConnectC()!=null) && (((TrackSegment)x.getConnectC()).getLayoutBlock()!=null) &&				
						(x.getConnectD()!=null) && (((TrackSegment)x.getConnectD()).getLayoutBlock()!=null) &&
						(x.getLayoutBlockAC()!=null) && (x.getLayoutBlockBD()!=null) ) {				
					list.add(x);
				}
				else {
					log.error("Missing connection or block assignment at Level Crossing in Block "+block.getUserName());
				}
			}
		}
		return list;
	}
	
    //Need to search through code to find out where this is being used
	/** 
	 * Returns a list of all layout turnouts involving the specified Block
	 */
	public ArrayList<LayoutTurnout> getLayoutTurnoutsThisBlock(Block block) {	
		ArrayList<LayoutTurnout> list = new ArrayList<LayoutTurnout>();
		LayoutBlock lBlock = layoutBlockManager.getByUserName(block.getUserName());
		String lBlockName = block.getUserName();
		for (int i = 0; i<layoutEditor.turnoutList.size(); i++) {
			LayoutTurnout t = layoutEditor.turnoutList.get(i);
			if ( (t.getBlockName().equals(lBlockName)) || (t.getBlockBName().equals(lBlockName)) || 
					(t.getBlockCName().equals(lBlockName)) || (t.getBlockDName().equals(lBlockName)) ) list.add(t);
			else if ( (t.getConnectA()!=null) && (((TrackSegment)t.getConnectA()).getLayoutBlock()==lBlock) ) 
				list.add(t);
			else if ( (t.getConnectB()!=null) && (((TrackSegment)t.getConnectB()).getLayoutBlock()==lBlock) ) 
				list.add(t);
			else if ( (t.getConnectC()!=null) && (((TrackSegment)t.getConnectC()).getLayoutBlock()==lBlock) ) 
				list.add(t);
			else if ( (t.getConnectD()!=null) && (((TrackSegment)t.getConnectD()).getLayoutBlock()==lBlock) ) 
				list.add(t);
		}
        for(LayoutSlip ls:layoutEditor.slipList){
            if(ls.getBlockName().equals(lBlockName)){
                list.add(ls);
            } else if ( (ls.getConnectA()!=null) && (((TrackSegment)ls.getConnectA()).getLayoutBlock()==lBlock) ) 
				list.add(ls);
			else if ( (ls.getConnectB()!=null) && (((TrackSegment)ls.getConnectB()).getLayoutBlock()==lBlock) ) 
				list.add(ls);
			else if ( (ls.getConnectC()!=null) && (((TrackSegment)ls.getConnectC()).getLayoutBlock()==lBlock) ) 
				list.add(ls);
			else if ( (ls.getConnectD()!=null) && (((TrackSegment)ls.getConnectD()).getLayoutBlock()==lBlock) ) 
				list.add(ls);
        }
// djd debugging - lists turnouts for a block
// debugging code - comment out when not debugging something involving this method
//		String txt = "Turnouts for Block "+block.getUserName()+" - ";
//		for (int k = 0; k<list.size(); k++) {
//			if (k>0) txt = txt+", ";
//			if ( (list.get(k)).getTurnout()!=null)
//				txt = txt+(list.get(k)).getTurnout().getSystemName();
//			else txt = txt+"???";
//		}
//		log.error(txt);
// end debugging code
		return list;
	}
	
	/**
	 * Returns 'true' if specified Layout Turnout has required signal heads.
	 * Returns 'false' if one or more of the required signals are missing.
	 */
	public boolean layoutTurnoutHasRequiredSignals(LayoutTurnout t) {
		if (t.getLinkType()==LayoutTurnout.NO_LINK) {
			if ( (t.getTurnoutType()==LayoutTurnout.RH_TURNOUT) || (t.getTurnoutType()==LayoutTurnout.LH_TURNOUT) ||
									(t.getTurnoutType()==LayoutTurnout.WYE_TURNOUT) ) {
				if ( (t.getSignalA1Name()!=null) && (!t.getSignalA1Name().equals("")) &&
						(t.getSignalB1Name()!=null) && (!t.getSignalB1Name().equals("")) &&
								(t.getSignalC1Name()!=null) && (!t.getSignalC1Name().equals("")) ) 
					return true;
				else return false;
			}
            else if (t.getTurnoutType()==LayoutTurnout.SINGLE_SLIP || t.getTurnoutType()==LayoutTurnout.DOUBLE_SLIP){
                if ((t.getSignalA1Name()!=null) && (!t.getSignalA1Name().equals("")) &&
                     (t.getSignalA2Name()!=null) && (!t.getSignalA2Name().equals("")) &&
                       (t.getSignalB1Name()!=null) && (!t.getSignalB1Name().equals("")) &&
                        (t.getSignalC1Name()!=null) && (!t.getSignalC1Name().equals("")) &&
                         (t.getSignalD1Name()!=null) && (!t.getSignalD1Name().equals("")) &&
                          (t.getSignalD2Name()!=null) && (!t.getSignalD2Name().equals(""))) {
                        
                            if(t.getTurnoutType()==LayoutTurnout.SINGLE_SLIP){
                                return true;
                            }
                            if(t.getTurnoutType()==LayoutTurnout.DOUBLE_SLIP){
                                if((t.getSignalB2Name()!=null) && (!t.getSignalB2Name().equals("")) &&
                                    (t.getSignalC2Name()!=null) && (!t.getSignalC2Name().equals("")) ){
                                        return true;
                                }
                            }
                }
                return false;
            }
			else {
				if ( (t.getSignalA1Name()!=null) && (!t.getSignalA1Name().equals("")) &&
						(t.getSignalB1Name()!=null) && (!t.getSignalB1Name().equals("")) &&
							(t.getSignalC1Name()!=null) && (!t.getSignalC1Name().equals("")) && 
									(t.getSignalD1Name()!=null) && (!t.getSignalD1Name().equals("")) ) 
					return true;
				else return false;
			}
		}
		else if (t.getLinkType()==LayoutTurnout.FIRST_3_WAY) {
			if ( (t.getSignalA1Name()!=null) && (!t.getSignalA1Name().equals("")) &&
						(t.getSignalC1Name()!=null) && (!t.getSignalC1Name().equals("")) )
				return true;
			else return false;
		}
		else if (t.getLinkType()==LayoutTurnout.SECOND_3_WAY) {
			if ( (t.getSignalB1Name()!=null) && (!t.getSignalB1Name().equals("")) &&
						(t.getSignalC1Name()!=null) && (!t.getSignalC1Name().equals("")) )
				return true;
			else return false;
		}
		else if (t.getLinkType()==LayoutTurnout.THROAT_TO_THROAT) {
			if ( (t.getSignalB1Name()!=null) && (!t.getSignalB1Name().equals("")) &&
						(t.getSignalC1Name()!=null) && (!t.getSignalC1Name().equals("")) )
				return true;
			else return false;
		}
		return false;
	}
	
	/**
	 * Returns the Signal Head at the Anchor block boundary 
	 * If 'facing' is 'true', returns the head that faces toward the specified Block
	 * If 'facing' is 'false', returns the head that faces away from the specified Block
	 */		
	public SignalHead getSignalHeadAtAnchor(PositionablePoint p, Block block, boolean facing) {
		if ( (p==null) || (block==null) ) {
			log.error("null arguments in call to getSignalHeadAtAnchor");
			return null;
		}
		LayoutBlock lBlock = layoutBlockManager.getByUserName(block.getUserName());
		if (((p.getConnect1()).getLayoutBlock()==lBlock) && ((p.getConnect2()).getLayoutBlock()!=lBlock)) {
			if ( (leTools.isAtWestEndOfAnchor(p.getConnect2(), p) && facing) ||
						((!leTools.isAtWestEndOfAnchor(p.getConnect2(), p)) && (!facing)) )				
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(p.getWestBoundSignal()));
			else
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(p.getEastBoundSignal()));
		}
		else if (((p.getConnect1()).getLayoutBlock()!=lBlock) && ((p.getConnect2()).getLayoutBlock()==lBlock)) {
			if ( (leTools.isAtWestEndOfAnchor(p.getConnect1(), p) && facing) ||
						((!leTools.isAtWestEndOfAnchor(p.getConnect1(), p)) && (!facing)) )
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(p.getWestBoundSignal()));
			else
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(p.getEastBoundSignal()));
		}
		else {
			// should never happen
			return null;
		}
	}
    
    	/**
	 * Returns the Signal Mast at the Anchor block boundary 
	 * If 'facing' is 'true', returns the head that faces toward the specified Block
	 * If 'facing' is 'false', returns the head that faces away from the specified Block
	 */		
	public SignalMast getSignalMastAtAnchor(PositionablePoint p, Block block, boolean facing) {
		if ( (p==null) || (block==null) ) {
			log.error("null arguments in call to getSignalHeadAtAnchor");
			return null;
		}
		LayoutBlock lBlock = layoutBlockManager.getByUserName(block.getUserName());
		if (((p.getConnect1()).getLayoutBlock()==lBlock) && ((p.getConnect2()).getLayoutBlock()!=lBlock)) {
			if ( (leTools.isAtWestEndOfAnchor(p.getConnect2(), p) && facing) ||
						((!leTools.isAtWestEndOfAnchor(p.getConnect2(), p)) && (!facing)) )				
				return (InstanceManager.signalMastManagerInstance().getSignalMast(p.getWestBoundSignalMast()));
			else
				return (InstanceManager.signalMastManagerInstance().getSignalMast(p.getEastBoundSignalMast()));
		}
		else if (((p.getConnect1()).getLayoutBlock()!=lBlock) && ((p.getConnect2()).getLayoutBlock()==lBlock)) {
			if ( (leTools.isAtWestEndOfAnchor(p.getConnect1(), p) && facing) ||
						((!leTools.isAtWestEndOfAnchor(p.getConnect1(), p)) && (!facing)) )
				return (InstanceManager.signalMastManagerInstance().getSignalMast(p.getWestBoundSignalMast()));
			else
				return (InstanceManager.signalMastManagerInstance().getSignalMast(p.getEastBoundSignalMast()));
		}
		else {
			// should never happen
			return null;
		}
	}
    
    
    //Signalmasts are only valid or requited on the boundary to a block.
    public boolean layoutTurnoutHasSignalMasts(LayoutTurnout t) {
        String[] turnoutBlocks = t.getBlockBoundaries();
        boolean valid = true;
        if(turnoutBlocks[0]!=null && (t.getSignalAMast()==null  || t.getSignalAMast().equals("")))
            valid = false;
        if(turnoutBlocks[1]!=null && (t.getSignalBMast()==null  || t.getSignalBMast().equals("")))
            valid = false;
        if(turnoutBlocks[2]!=null && (t.getSignalCMast()==null  || t.getSignalCMast().equals("")))
            valid = false;
        if(turnoutBlocks[3]!=null && (t.getSignalDMast()==null  || t.getSignalDMast().equals("")))
            valid = false;
		return valid;
	}

	/**
	 * Returns the Signal Head at the level crossing
	 * If 'facing' is 'true', returns the head that faces toward the specified Block
	 * If 'facing' is 'false', returns the head that faces away from the specified Block
	 */		
	public SignalHead getSignalHeadAtLevelXing(LevelXing x, Block block, boolean facing) {
		if ( (x==null) || (block==null) ) {
			log.error("null arguments in call to getSignalHeadAtLevelXing");
			return null;
		}
		LayoutBlock lBlock = layoutBlockManager.getByUserName(block.getUserName());
		if ( (x.getConnectA()==null) || (x.getConnectB()==null) || 
											(x.getConnectC()==null) || (x.getConnectD()==null) ) {
			log.error("Missing track around level crossing near Block "+block.getUserName());
			return null;
		}
		if (((TrackSegment)x.getConnectA()).getLayoutBlock()==lBlock) {
			if (facing) 
				return InstanceManager.signalHeadManagerInstance().getSignalHead(x.getSignalCName());
			else 
				return InstanceManager.signalHeadManagerInstance().getSignalHead(x.getSignalAName());
		}
		if (((TrackSegment)x.getConnectB()).getLayoutBlock()==lBlock) {
			if (facing) 
				return InstanceManager.signalHeadManagerInstance().getSignalHead(x.getSignalDName());
			else 
				return InstanceManager.signalHeadManagerInstance().getSignalHead(x.getSignalBName());
		}
		if (((TrackSegment)x.getConnectC()).getLayoutBlock()==lBlock) {
			if (facing) 
				return InstanceManager.signalHeadManagerInstance().getSignalHead(x.getSignalAName());
			else 
				return InstanceManager.signalHeadManagerInstance().getSignalHead(x.getSignalCName());
		}
		if (((TrackSegment)x.getConnectD()).getLayoutBlock()==lBlock) {
			if (facing) 
				return InstanceManager.signalHeadManagerInstance().getSignalHead(x.getSignalBName());
			else 
				return InstanceManager.signalHeadManagerInstance().getSignalHead(x.getSignalDName());
		}
		return null;
	}
	
	/** 
	 * Returns 'true' if the specified block is internal to the Level Xing, and if all else is OK.
	 * Returns 'false' if one of the connecting Track Segments is in the Block, or if there is 
	 *		a problem with looking for a signal head.
	 */
	public boolean blockInternalToLevelXing(LevelXing x, Block block) {
		if ( (x==null) || (block==null) ) return false;
		LayoutBlock lBlock = layoutBlockManager.getByUserName(block.getUserName());
		if (lBlock==null) return false;
		if ( (x.getConnectA()==null) || (x.getConnectB()==null) || 
						(x.getConnectC()==null) || (x.getConnectD()==null) ) return false;
		if ( (x.getLayoutBlockAC()!=lBlock) && (x.getLayoutBlockBD()!=lBlock) ) return false;
		if (((TrackSegment)x.getConnectA()).getLayoutBlock()==lBlock) return false;
		if (((TrackSegment)x.getConnectB()).getLayoutBlock()==lBlock) return false;
		if (((TrackSegment)x.getConnectC()).getLayoutBlock()==lBlock) return false;
		if (((TrackSegment)x.getConnectD()).getLayoutBlock()==lBlock) return false;
		return true;
	}
	
	/**
	 * Matches the anchor point to an Entry Point, and returns the direction specified in the Entry Point
	 *   If no match is found, UNKNOWN is returned, indicating that the block boundary is internal to the 
	 *		Section.
	 */
	public int getDirectionFromAnchor(ArrayList<EntryPoint> mForwardEntryPoints, ArrayList<EntryPoint> mReverseEntryPoints, 
												PositionablePoint p) {
		Block block1 = p.getConnect1().getLayoutBlock().getBlock();
		Block block2 = p.getConnect2().getLayoutBlock().getBlock();
		for (int i = 0; i<mForwardEntryPoints.size(); i++) {
			EntryPoint ep = mForwardEntryPoints.get(i);
			if ( ((ep.getBlock()==block1) && (ep.getFromBlock()==block2)) ||
					((ep.getBlock()==block2) && (ep.getFromBlock()==block1)) )
				return EntryPoint.FORWARD;
		}
		for (int j = 0; j<mReverseEntryPoints.size(); j++) {
			EntryPoint ep = mReverseEntryPoints.get(j);
			if ( ((ep.getBlock()==block1) && (ep.getFromBlock()==block2)) ||
					((ep.getBlock()==block2) && (ep.getFromBlock()==block1)) )
				return EntryPoint.REVERSE;
		}
		return EntryPoint.UNKNOWN;
	}
	
	/**
	 * Checks if a Level Crossing's AC track and its two connecting Track Segments  are internal to the 
	 *		specified block.
	 *	If the A and C connecting Track Segments are in the Block, and the LevelXing's AC track is in the
	 *		block, returns 'true". Otherwise returns 'false', even if one of the tracks of the LevelXing 
	 *		is in the block.
	 * Note; if two connecting track segments are in the block, but the internal connecting 
	 *		track is not, that is an error in the Layout Editor panel. If found, an error message is 
	 *		generated and 'false' is returned.
	 */
	public boolean isInternalLevelXingAC(LevelXing x, Block block) {
		LayoutBlock lBlock = layoutBlockManager.getByUserName(block.getUserName());
		if ( (((TrackSegment)x.getConnectA()).getLayoutBlock()==lBlock) &&
				(((TrackSegment)x.getConnectC()).getLayoutBlock()==lBlock) ) {
			if (x.getLayoutBlockAC()==lBlock) {
				return true;
			}
			else {
				log.error("Panel blocking error at AC of Level Crossing in Block "+block.getUserName());
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Checks if a Level Crossing's BD track and its two connecting Track Segments  are internal to the 
	 *		specified block. 
	 *	If the B and D connecting Track Segments are in the Block, and the LevelXing's BD track is in the
	 *		block, returns 'true". Otherwise returns 'false', even if one of the tracks of the LevelXing 
	 *		is in the block.
	 * Note; if two connecting track segments are in the block, but the internal connecting 
	 *		track is not, that is an error in the Layout Editor panel. If found, an error message is 
	 *		generated and 'false' is returned.
	 */
	public boolean isInternalLevelXingBD(LevelXing x, Block block) {
		LayoutBlock lBlock = layoutBlockManager.getByUserName(block.getUserName());
		if ( (((TrackSegment)x.getConnectB()).getLayoutBlock()==lBlock) &&
				(((TrackSegment)x.getConnectD()).getLayoutBlock()==lBlock) ) {
			if (x.getLayoutBlockBD()==lBlock) {
				return true;
			}
			else {
				log.error("Panel blocking error at BD of Level Crossing in Block "+block.getUserName());
				return false;
			}
		}
		return false;										
	}

	/*
	 * Defines where to place sensor in a FACING mode SSL
	 */
	public static final int OVERALL = 0x00;
	public static final int CONTINUING = 0x01;
	public static final int DIVERGING = 0x02;
	
	/*
	 * Adds the specified sensor ('name') to the SSL for the specified signal head
	 * 'name' should be the system name for the sensor.
	 * Returns 'true' if the sensor was already in the signal head SSL or if it has been 
	 *		added successfully.  Returns 'false' and logs an error if not.
	 * If the SSL has not been set up yet, the sensor is not added, an error message is output, 
	 *		and 'false' is returned.
	 * Parameters: 'name' - sensor name, 'sh' - signal head, 'where' should be DIVERGING if the sensor 
	 *		is being added to the diverging (second) part of a facing mode SSL, 'CONTINUING' if 
	 *		the sensor is being added to the continuing (first) part of a facing mode SSL, OVERALL if 
	 *		the sensor is being added to the overall sensor list of a facing mode SSL. 'where' is
	 *		ignored if not a facing mode SSL.
	 */
	public boolean addSensorToSignalHeadLogic(String name, SignalHead sh, int where) {
		if (sh==null) {
			log.error("Null signal head on entry to addSensorToSignalHeadLogic");
			return false;
		}
		if ( (name==null) || name.equals("") ) {
			log.error("Null string for sensor name on entry to addSensorToSignalHeadLogic");
			return false;
		}
		BlockBossLogic bbLogic = BlockBossLogic.getStoppedObject(sh.getSystemName());
		if (bbLogic==null) {
			log.error("Trouble opening BlockBossLogic for "+sh.getSystemName());
			return false;
		}
		int mode = bbLogic.getMode();
		if ( ((mode==BlockBossLogic.SINGLEBLOCK) || (mode==BlockBossLogic.TRAILINGMAIN) ||
					(mode==BlockBossLogic.TRAILINGDIVERGING)) || ((mode==BlockBossLogic.FACING) && 
						(where==OVERALL)) ) {
			if ( ( (bbLogic.getSensor1()!=null) && (bbLogic.getSensor1()).equals(name) ) ||
					( (bbLogic.getSensor2()!=null) && (bbLogic.getSensor2()).equals(name) ) ||
					( (bbLogic.getSensor3()!=null) && (bbLogic.getSensor3()).equals(name) ) ||
					( (bbLogic.getSensor4()!=null) && (bbLogic.getSensor4()).equals(name) ) ||
					( (bbLogic.getSensor5()!=null) && (bbLogic.getSensor5()).equals(name) ) ) {
				bbLogic.retain();
				bbLogic.start();
				return true;
			}
			if (bbLogic.getSensor1()==null) bbLogic.setSensor1(name);
			else if (bbLogic.getSensor2()==null) bbLogic.setSensor2(name);
			else if (bbLogic.getSensor3()==null) bbLogic.setSensor3(name);
			else if (bbLogic.getSensor4()==null) bbLogic.setSensor4(name);
			else if (bbLogic.getSensor5()==null) bbLogic.setSensor5(name);
			else {
				log.error("Error - could not add sensor to SSL for signal head "+sh.getSystemName()+
										" because there is no room in the SSL.");								
				bbLogic.retain();
				bbLogic.start();
				return false;
			}
		}
		else if (mode==BlockBossLogic.FACING) {
			if (where==DIVERGING) {
				if ( ( (bbLogic.getWatchedSensor2()!=null) && (bbLogic.getWatchedSensor2()).equals(name) ) ||
						( (bbLogic.getWatchedSensor2Alt()!=null) && (bbLogic.getWatchedSensor2Alt()).equals(name) ) ) {
					bbLogic.retain();
					bbLogic.start();
					return true;
				}
				if (bbLogic.getWatchedSensor2()==null) bbLogic.setWatchedSensor2(name);
				else if (bbLogic.getWatchedSensor2Alt()==null) bbLogic.setWatchedSensor2Alt(name);
				else {
					log.error("Error - could not add watched sensor to SSL for signal head "+sh.getSystemName()+
										" because there is no room in the facing SSL diverging part.");
					bbLogic.retain();
					bbLogic.start();
					return false;
				}
			}
			else if (where==CONTINUING) {
				if ( ( (bbLogic.getWatchedSensor1()!=null) && (bbLogic.getWatchedSensor1()).equals(name) ) ||
						( (bbLogic.getWatchedSensor1Alt()!=null) && (bbLogic.getWatchedSensor1Alt()).equals(name) ) ) {
					bbLogic.retain();
					bbLogic.start();
					return true;
				}
				if (bbLogic.getWatchedSensor1()==null) bbLogic.setWatchedSensor1(name);
				else if (bbLogic.getWatchedSensor1Alt()==null) bbLogic.setWatchedSensor1Alt(name);
				else {
					log.error("Error - could not add watched sensor to SSL for signal head "+sh.getSystemName()+
										" because there is no room in the facing SSL continuing part.");
					bbLogic.retain();
					bbLogic.start();
					return false;
				}
			}
			else {
				log.error("Error - could not add watched sensor to SSL for signal head "+sh.getSystemName()+
										"because 'where' to place the sensor was not correctly designated.");
				bbLogic.retain();
				bbLogic.start();
				return false;
			}							
		}
		else {
			log.error("SSL has not been set up for signal head "+sh.getSystemName()+
								". Could not add sensor - "+name+".");
			return false;
		}
		bbLogic.retain();
		bbLogic.start();
		return true;
	}
	
	/*
	 * Revoves the specified sensors ('names') from the SSL for the specified signal head if 
	 *      any of the sensors is currently in the SSL.
	 * Returns 'false' if an error was found, and issues a message to the error log.
	 * Returns 'true' if no error, whether any sensors were found or not.
	 */
	public boolean removeSensorsFromSignalHeadLogic(ArrayList<String> names, SignalHead sh) {
		if (sh==null) {
			log.error("Null signal head on entry to removeSensorsFromSignalHeadLogic");
			return false;
		}
		if (names==null) {
			log.error("Null ArrayList of sensor names on entry to removeSensorsFromSignalHeadLogic");
			return false;
		}
		BlockBossLogic bbLogic = BlockBossLogic.getStoppedObject(sh.getSystemName());
		if (bbLogic==null) {
			log.error("Trouble opening BlockBossLogic for "+sh.getSystemName());
			return false;
		}
		for (int i = 0; i<names.size(); i++) {
			String name = names.get(i);
			if ( (bbLogic.getSensor1()!=null) && (bbLogic.getSensor1()).equals(name) )
				bbLogic.setSensor1(null);
			if ( (bbLogic.getSensor2()!=null) && (bbLogic.getSensor2()).equals(name) ) 
				bbLogic.setSensor2(null);
			if ( (bbLogic.getSensor3()!=null) && (bbLogic.getSensor3()).equals(name) ) 
				bbLogic.setSensor3(null);
			if ( (bbLogic.getSensor4()!=null) && (bbLogic.getSensor4()).equals(name) ) 
				bbLogic.setSensor4(null);
			if ( (bbLogic.getSensor5()!=null) && (bbLogic.getSensor5()).equals(name) ) 
				bbLogic.setSensor5(null);
			if (bbLogic.getMode()==BlockBossLogic.FACING) {
				if ( (bbLogic.getWatchedSensor1()!=null) && (bbLogic.getWatchedSensor1()).equals(name) )
					bbLogic.setWatchedSensor1(null);
				if ( (bbLogic.getWatchedSensor1Alt()!=null) && (bbLogic.getWatchedSensor1Alt()).equals(name) )
					bbLogic.setWatchedSensor1Alt(null);	
				if ( (bbLogic.getWatchedSensor2()!=null) && (bbLogic.getWatchedSensor2()).equals(name) )
					bbLogic.setWatchedSensor2(null);
				if ( (bbLogic.getWatchedSensor2Alt()!=null) && (bbLogic.getWatchedSensor2Alt()).equals(name) )
					bbLogic.setWatchedSensor2Alt(null);	
			}							
		}
		if (bbLogic.getMode()==0) {
			// this to avoid Unexpected mode ERROR message at startup
			bbLogic.setMode(BlockBossLogic.SINGLEBLOCK);
		}
		bbLogic.retain();
		bbLogic.start();
		return true;
	}
	
	/** 
	 * Returns the next Node following the specified TrackNode
	 *<P>
	 * If the specified track node can lead to different paths to the next node, for example, 
	 *       if the specified track node is a turnout entered at its throat, then "cNodeState" 
	 *       must be specified to choose between the possible paths.
	 * Returns a TrackNode if one is reached. Returns null if trouble following the track.
	 * .
	 */
	public TrackNode getNextNode(TrackNode cNode, int cNodeState) {
		if (cNode==null) {
			log.error("getNextNode called with a null Track Node");
			return null;
		}
		if (cNode.reachedEndOfTrack()) {
			log.error("getNextNode - attempt to search past endBumper");
			return null;
		}
		return (getTrackNode(cNode.getNode(), cNode.getNodeType(), cNode.getTrackSegment(), cNodeState));
	}	
	
	/** 
	 * Returns the next Node following the node specified by "cNode" and "cNodeType", assuming that cNode was 
	 *       reached via the specified TrackSegment.
	 *<P>
	 * If the specified track node can lead to different paths to the next node, for example, 
	 *       if the specified track node is a turnout entered at its throat, then "cNodeState" 
	 *       must be specified to choose between the possible paths. If cNodeState = 0, the search 
	 *		 will follow the 'continuing' track; if cNodeState = 1, the search will follow the 
	 *		 'diverging' track; if cNodeState = 2 (3-way turnouts only), the search will follow 
	 *		 the second 'diverging' track. 
	 *<P>
	 * In determining which track is the 'continuing' track for RH, LH, and WYE turnouts, this search 
	 *		 routine uses the layout turnout's 'continuingState'.
	 *<P>
	 * When following track, this method skips over anchor points that are not block boundaries.
	 *<P>
	 * When following track, this method treats a modelled 3-way turnout as a single turnout. It also 
	 *		treats two THROAT_TO_THROAT turnouts as a single turnout, but with each turnout having a 
	 *		continuing sense.
	 *<P>
	 * Returns a TrackNode if a node or end_of-track is reached. Returns null if trouble following the track. 
	 */
	public TrackNode getTrackNode(Object cNode, int cNodeType, TrackSegment cTrack, int cNodeState) {
		// initialize
		Object node = null;
		int nodeType = LayoutEditor.NONE;
		TrackSegment track = null;
		boolean hitEnd = false;
		@SuppressWarnings("unused")
		int pType = cNodeType;
		Object pObject = cNode;
		TrackSegment tTrack = null;
		switch (cNodeType) {
			case LayoutEditor.POS_POINT:
				PositionablePoint p = (PositionablePoint)cNode;
				if (p.getType()==PositionablePoint.END_BUMPER) {
					log.error("Attempt to search beyond end of track");
					return null;
				}
				if (p.getConnect1()==tTrack) 
					tTrack = p.getConnect2();
				else
					tTrack = p.getConnect1();
				break;
			case LayoutEditor.TURNOUT_A:
				if	( (((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.RH_TURNOUT) || 
						(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.LH_TURNOUT) ||
						(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.WYE_TURNOUT) ) {
					if ( (((LayoutTurnout)cNode).getLinkedTurnoutName()==null) || 
										(((LayoutTurnout)cNode).getLinkedTurnoutName().equals("")) ) {
						// Standard turnout - node type A
						if (((LayoutTurnout)cNode).getContinuingSense()==Turnout.CLOSED) {
							if (cNodeState==0) {
								tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectB();
								pType = LayoutEditor.TURNOUT_B;
							}
							else if (cNodeState==1) {
								tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectC();
								pType = LayoutEditor.TURNOUT_C;
							}
							else {
								log.error("Bad cNodeState argument when searching track-std. normal");
								return null;
							}
						}
						else {
							if (cNodeState==0) {
								tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectC();
								pType = LayoutEditor.TURNOUT_C;
							}
							else if (cNodeState==1) {
								tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectB();
								pType = LayoutEditor.TURNOUT_B;
							}
							else {
								log.error("Bad cNodeState argument when searching track-std reversed");
								return null;
							}
						}
					}
					else {
						// linked turnout - node type A
						LayoutTurnout lto = layoutEditor.findLayoutTurnoutByName(((LayoutTurnout)cNode).getLinkedTurnoutName());
						if (((LayoutTurnout)cNode).getLinkType()==LayoutTurnout.THROAT_TO_THROAT) {
							if (cNodeState==0) {
								if (lto.getContinuingSense()==Turnout.CLOSED) {
									tTrack = (TrackSegment)lto.getConnectB();
									pType = LayoutEditor.TURNOUT_B;
								}
								else {
									tTrack = (TrackSegment)lto.getConnectC();
									pType = LayoutEditor.TURNOUT_C;
								}
							}
								else if (cNodeState==1) {
								if (lto.getContinuingSense()==Turnout.CLOSED) {
									tTrack = (TrackSegment)lto.getConnectC();
									pType = LayoutEditor.TURNOUT_C;
								}
								else {
									tTrack = (TrackSegment)lto.getConnectB();
									pType = LayoutEditor.TURNOUT_B;
								}
							}
							else {
								log.error("Bad cNodeState argument when searching track - THROAT_TO_THROAT");
								return null;
							}
							pObject = lto;
						}
						else if (((LayoutTurnout)cNode).getLinkType()==LayoutTurnout.FIRST_3_WAY) {
							if (cNodeState==0) {
								if (lto.getContinuingSense()==Turnout.CLOSED) {
									tTrack = (TrackSegment)lto.getConnectB();
									pType = LayoutEditor.TURNOUT_B;
								}
								else {
									tTrack = (TrackSegment)lto.getConnectC();
									pType = LayoutEditor.TURNOUT_C;
								}
								pObject = lto;
							}
							else if (cNodeState==1) {
								if (((LayoutTurnout)cNode).getContinuingSense()==Turnout.CLOSED) {
									tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectC();
									pType = LayoutEditor.TURNOUT_C;
								}
								else {
									tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectB();
									pType = LayoutEditor.TURNOUT_B;
								}
							}
							else if (cNodeState==2) {
								if (lto.getContinuingSense()==Turnout.CLOSED) {
									tTrack = (TrackSegment)lto.getConnectC();
									pType = LayoutEditor.TURNOUT_C;
								}
								else {
									tTrack = (TrackSegment)lto.getConnectB();
									pType = LayoutEditor.TURNOUT_B;
								}
								pObject = lto;
							}
							else {
								log.error("Bad cNodeState argument when searching track - FIRST_3_WAY");
								return null;
							}
						}			
					}
				}
				else if ( (((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.RH_XOVER) || 
						(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.LH_XOVER) ||
						(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) ) {
					// crossover turnout - node type A
					if (cNodeState==0) {
						tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectB();
						pType = LayoutEditor.TURNOUT_B;
					}
					else if (cNodeState==1) {
						if ( (cNodeType==LayoutEditor.TURNOUT_A) && 
									(!(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.LH_XOVER)) ){
							tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectC();
							pType = LayoutEditor.TURNOUT_C;
						}
						else {
							log.error("Request to follow not allowed switch setting at LH_XOVER or RH_OVER");
							return null;
						}
					}
					else {
						log.error("Bad cNodeState argument when searching track- XOVER A");
						return null;
					}
				}
				break;
			case LayoutEditor.TURNOUT_B:
			case LayoutEditor.TURNOUT_C:
				if ( (((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.RH_TURNOUT) || 
						(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.LH_TURNOUT) ||
						(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.WYE_TURNOUT) ) {
					if ( (((LayoutTurnout)cNode).getLinkedTurnoutName()==null) || 
								(((LayoutTurnout)cNode).getLinkedTurnoutName().equals("")) ||
									(((LayoutTurnout)cNode).getLinkType()==LayoutTurnout.FIRST_3_WAY) ) {
						tTrack = (TrackSegment)(((LayoutTurnout)cNode).getConnectA());
						pType = LayoutEditor.TURNOUT_A;
					}
					else {
						LayoutTurnout lto = layoutEditor.findLayoutTurnoutByName(((LayoutTurnout)cNode).getLinkedTurnoutName());
						if (((LayoutTurnout)cNode).getLinkType()==LayoutTurnout.SECOND_3_WAY) {
							tTrack = (TrackSegment)(lto.getConnectA());
							pType = LayoutEditor.TURNOUT_A;
						}
						else if (((LayoutTurnout)cNode).getLinkType()==LayoutTurnout.THROAT_TO_THROAT) {
							if (cNodeState==0) {
									if (lto.getContinuingSense()==Turnout.CLOSED) {
									tTrack = (TrackSegment)lto.getConnectB();
									pType = LayoutEditor.TURNOUT_B;
								}
								else {
									tTrack = (TrackSegment)lto.getConnectC();
									pType = LayoutEditor.TURNOUT_C;
								}
							}
							else if (cNodeState==1) {
								if (lto.getContinuingSense()==Turnout.CLOSED) {
									tTrack = (TrackSegment)lto.getConnectC();
									pType = LayoutEditor.TURNOUT_C;
								}
								else {
									tTrack = (TrackSegment)lto.getConnectB();
									pType = LayoutEditor.TURNOUT_B;
								}
							}
							else {
								log.error("Bad cNodeState argument when searching track - THROAT_TO_THROAT - 2");
								return null;
							}
						}
						pObject = lto;
					}
				}
				else if ( (((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.RH_XOVER) || 
						(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.LH_XOVER) ||
						(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) ) {
					if (cNodeState==0) {
						if (cNodeType==LayoutEditor.TURNOUT_B) {
							tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectA();
							pType = LayoutEditor.TURNOUT_A;
						}
						else if (cNodeType==LayoutEditor.TURNOUT_C) {
							tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectD();
							pType = LayoutEditor.TURNOUT_D;
						}
					}
					else if (cNodeState==1) {
						if ( (cNodeType==LayoutEditor.TURNOUT_C) && 
									(!(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.LH_XOVER)) ) {
							tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectA();
							pType = LayoutEditor.TURNOUT_A;
						}
						else if ( (cNodeType==LayoutEditor.TURNOUT_B) && 
									(!(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.RH_XOVER)) ){
							tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectD();
							pType = LayoutEditor.TURNOUT_D;
						}
						else {
							log.error("Request to follow not allowed switch setting at LH_XOVER or RH_OVER");
							return null;
						}
					}
					else {
						log.error("Bad cNodeState argument when searching track - XOVER B or C");
						return null;
					}
				}
				break;
			case LayoutEditor.TURNOUT_D:
				if ( (((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.RH_XOVER) || 
						(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.LH_XOVER) ||
						(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) ) {
					if (cNodeState==0) {
						tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectC();
						pType = LayoutEditor.TURNOUT_C;
					}
					else if (cNodeState==1) {
						if (!(((LayoutTurnout)cNode).getTurnoutType()==LayoutTurnout.RH_XOVER)) {
							tTrack = (TrackSegment)((LayoutTurnout)cNode).getConnectB();
							pType = LayoutEditor.TURNOUT_B;
						}
						else {
							log.error("Request to follow not allowed switch setting at LH_XOVER or RH_OVER");
							return null;
						}
					}
					else {
						log.error("Bad cNodeState argument when searching track - XOVER D");
						return null;
					}
				}
				else {
					log.error ("Bad traak node type - TURNOUT_D, but not a crossover turnout");
					return null;
				}
				break;
			case LayoutEditor.LEVEL_XING_A :
				tTrack = (TrackSegment)((LevelXing)cNode).getConnectC();
				pType = LayoutEditor.LEVEL_XING_C;
				break;
			case LayoutEditor.LEVEL_XING_B :
				tTrack = (TrackSegment)((LevelXing)cNode).getConnectD();
				pType = LayoutEditor.LEVEL_XING_D;
				break;
			case LayoutEditor.LEVEL_XING_C:
				tTrack = (TrackSegment)((LevelXing)cNode).getConnectA();
				pType = LayoutEditor.LEVEL_XING_A;
				break;
			case LayoutEditor.LEVEL_XING_D:
				tTrack = (TrackSegment)((LevelXing)cNode).getConnectB();
				pType = LayoutEditor.LEVEL_XING_B;
				break;
			case LayoutEditor.SLIP_A :
				tTrack = (TrackSegment)((LevelXing)cNode).getConnectC();
				pType = LayoutEditor.SLIP_C;
				break;
			case LayoutEditor.SLIP_B :
				tTrack = (TrackSegment)((LevelXing)cNode).getConnectD();
				pType = LayoutEditor.SLIP_D;
				break;
			case LayoutEditor.SLIP_C:
				tTrack = (TrackSegment)((LevelXing)cNode).getConnectA();
				pType = LayoutEditor.SLIP_A;
				break;
			case LayoutEditor.SLIP_D:
				tTrack = (TrackSegment)((LevelXing)cNode).getConnectB();
				pType = LayoutEditor.SLIP_B;
				break;
			default:
				log.error("Unable to initiate 'getTrackNode'.  Probably bad input Track Node.");
				return null; 
		}
		
		// follow track to anchor block boundary, turnout, or level crossing
		boolean hasNode = false;
		Object tObject = null;
		int tType = 0;
		if (tTrack==null){
			log.error("Error tTrack is null!");
			return null;
		}
		while (!hasNode) {
			if (tTrack.getConnect1()==pObject) {
				tObject = tTrack.getConnect2();
				tType = tTrack.getType2();
			}
			else {
				tObject = tTrack.getConnect1();
				tType = tTrack.getType1();
			}
			if (tObject==null) {
				log.error("Error while following track looking for next node");
				return null;
			}
			if (tType!=LayoutEditor.POS_POINT) {
				node = tObject;
				nodeType = tType;
				track = tTrack;
				hasNode = true;
			}	
			else {
				PositionablePoint p = (PositionablePoint)tObject;
				if (p.getType()==PositionablePoint.END_BUMPER) {
					hitEnd = true;
					hasNode = true;
				}
				else {
					TrackSegment con1 = p.getConnect1();
					TrackSegment con2 = p.getConnect2();
					if ( (con1==null) || (con2==null) ) {
						log.error("Error - Breakin connectivity at Anchor Point when searching for track node");
						return null;
					}
					if (con1.getLayoutBlock()!=con2.getLayoutBlock()) {
						node = tObject;
						nodeType = LayoutEditor.POS_POINT;
						track = tTrack;
						hasNode = true;
					}
					else {
						if (con1==tTrack) 
							tTrack = con2;
						else 
							tTrack = con1;
						pObject = tObject;
					}						
				}
			}
		}		
		return (new TrackNode(node, nodeType, track, hitEnd, cNodeState));
	}

	/**
	 * Returns an "exit block" for the specified track node if there is one, else returns null.
	 * An "exit block" must be different from the block of the track segment in the node.
	 * If the node is a PositionablePoint, it is assumed to be a block boundary anchor point.
	 * If an "excludedBlock" is entered, that block will not be returned as the exit block of 
	 *		a Node of type TURNOUT_x.
	 */
	public Block getExitBlockForTrackNode(TrackNode node, Block excludedBlock) {
		if ( (node==null) || node.reachedEndOfTrack() ) return null;
		Block block = null;
		switch (node.getNodeType()) {
			case LayoutEditor.POS_POINT:
				PositionablePoint p = (PositionablePoint)node.getNode();
				block = p.getConnect1().getLayoutBlock().getBlock();
				if (block==node.getTrackSegment().getLayoutBlock().getBlock()) 
					block = p.getConnect2().getLayoutBlock().getBlock();
				break;
			case LayoutEditor.TURNOUT_A:
				LayoutTurnout lt = (LayoutTurnout)node.getNode();
				Block tBlock = ((TrackSegment)lt.getConnectB()).getLayoutBlock().getBlock();
				if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
						(tBlock!=excludedBlock) ) block = tBlock;
				else if (lt.getTurnoutType()!=LayoutTurnout.LH_XOVER) {
					tBlock = ((TrackSegment)lt.getConnectC()).getLayoutBlock().getBlock();
					if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
							(tBlock!=excludedBlock) ) block = tBlock;
				}	
				break;
			case LayoutEditor.TURNOUT_B:
				lt = (LayoutTurnout)node.getNode();
				tBlock = ((TrackSegment)lt.getConnectA()).getLayoutBlock().getBlock();
				if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
						(tBlock!=excludedBlock) ) block = tBlock;
				else if ( (lt.getTurnoutType()==LayoutTurnout.LH_XOVER) || 
							(lt.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) ) {
					tBlock = ((TrackSegment)lt.getConnectD()).getLayoutBlock().getBlock();
					if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
							(tBlock!=excludedBlock) ) block = tBlock;
				}	
				break;
			case LayoutEditor.TURNOUT_C:
				lt = (LayoutTurnout)node.getNode();
				if (lt.getTurnoutType()!=LayoutTurnout.LH_XOVER) {
					tBlock = ((TrackSegment)lt.getConnectA()).getLayoutBlock().getBlock();
					if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
							(tBlock!=excludedBlock) ) block = tBlock;
				}
				if ( (block==null) && ((lt.getTurnoutType()==LayoutTurnout.LH_XOVER) || 
							(lt.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER)) ) {
					tBlock = ((TrackSegment)lt.getConnectD()).getLayoutBlock().getBlock();
					if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
							(tBlock!=excludedBlock) ) block = tBlock;
				}	
				break;
			case LayoutEditor.TURNOUT_D:
				lt = (LayoutTurnout)node.getNode();
				if ( (lt.getTurnoutType()==LayoutTurnout.LH_XOVER) || 
							(lt.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) ) {
					tBlock = ((TrackSegment)lt.getConnectB()).getLayoutBlock().getBlock();
					if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
							(tBlock!=excludedBlock) ) block = tBlock;
				}				
				break;
			case LayoutEditor.LEVEL_XING_A:
				LevelXing x = (LevelXing)node.getNode();
				tBlock = ((TrackSegment)x.getConnectC()).getLayoutBlock().getBlock();
				if (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) block = tBlock;			
				break;
			case LayoutEditor.LEVEL_XING_B:
				x = (LevelXing)node.getNode();
				tBlock = ((TrackSegment)x.getConnectD()).getLayoutBlock().getBlock();
				if (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) block = tBlock;						
				break;
			case LayoutEditor.LEVEL_XING_C:
				x = (LevelXing)node.getNode();
				tBlock = ((TrackSegment)x.getConnectA()).getLayoutBlock().getBlock();
				if (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) block = tBlock;						
				break;
			case LayoutEditor.LEVEL_XING_D:
				x = (LevelXing)node.getNode();
				tBlock = ((TrackSegment)x.getConnectB()).getLayoutBlock().getBlock();
				if (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) block = tBlock;			
				break;
            case LayoutEditor.SLIP_A:
                LayoutSlip ls = (LayoutSlip)node.getNode();
                tBlock = ((TrackSegment)ls.getConnectC()).getLayoutBlock().getBlock();
				if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
						(tBlock!=excludedBlock) ) block = tBlock;
				else {
					tBlock = ((TrackSegment)ls.getConnectD()).getLayoutBlock().getBlock();
					if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
							(tBlock!=excludedBlock) ) block = tBlock;
				}
                break;
            case LayoutEditor.SLIP_B:
                ls = (LayoutSlip)node.getNode();
                 tBlock = ((TrackSegment)ls.getConnectD()).getLayoutBlock().getBlock();
                if(ls.getTurnoutType()==LayoutSlip.DOUBLE_SLIP){
                    //Double slip
                    if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
                            (tBlock!=excludedBlock) ) block = tBlock;
                    else {
                        tBlock = ((TrackSegment)ls.getConnectC()).getLayoutBlock().getBlock();
                        if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
                                (tBlock!=excludedBlock) ) block = tBlock;
                    }
                } else{
                    if (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) block = tBlock;			
                }
                break;
            case LayoutEditor.SLIP_C:
                ls = (LayoutSlip)node.getNode();
                tBlock = ((TrackSegment)ls.getConnectA()).getLayoutBlock().getBlock();
                if(ls.getTurnoutType()==LayoutSlip.DOUBLE_SLIP){
                    if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
                            (tBlock!=excludedBlock) ) block = tBlock;
                    else {
                        tBlock = ((TrackSegment)ls.getConnectB()).getLayoutBlock().getBlock();
                        if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
                                (tBlock!=excludedBlock) ) block = tBlock;
                    }
                } else{
                    if (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) block = tBlock;			
                }
                break;
            case LayoutEditor.SLIP_D:
                ls = (LayoutSlip)node.getNode();
                tBlock = ((TrackSegment)ls.getConnectB()).getLayoutBlock().getBlock();
				if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
						(tBlock!=excludedBlock) ) block = tBlock;
				else {
					tBlock = ((TrackSegment)ls.getConnectA()).getLayoutBlock().getBlock();
					if ( (tBlock!=node.getTrackSegment().getLayoutBlock().getBlock()) &&
							(tBlock!=excludedBlock) ) block = tBlock;
				}
                break;
            default : break;
		}
		return block;
	}
	
	// support methods

	/**
	 * Initializes the setting (as an object), sets the new track segment (if in Block), and sets the
	 *    prevConnectType. 
	 */
	private Integer getTurnoutSetting(LayoutTurnout lt, int cType) {
        prevConnectObject = lt;
		int setting = Turnout.THROWN;
		int tType = lt.getTurnoutType();
        if(lt instanceof LayoutSlip){
            setting = LayoutSlip.UNKNOWN;
            LayoutSlip ls = (LayoutSlip)lt;
            tType = ls.getTurnoutType();
            switch(cType){
                case LayoutEditor.SLIP_A :  if(nlb==((TrackSegment)ls.getConnectC()).getLayoutBlock()){
                                                // exiting block at C
                                                prevConnectType= LayoutEditor.SLIP_A;
                                                setting = LayoutSlip.STATE_AC;
                                                tr = (TrackSegment)ls.getConnectC();
                                            } else if (nlb==((TrackSegment)ls.getConnectD()).getLayoutBlock()){
                                                    
                                                // exiting block at D
                                                prevConnectType= LayoutEditor.SLIP_A;
                                                setting = LayoutSlip.STATE_AD;
                                                tr = (TrackSegment)ls.getConnectD();
                                            } else if (lb==((TrackSegment)ls.getConnectC()).getLayoutBlock() &&
                                                    lb!=((TrackSegment)ls.getConnectD()).getLayoutBlock()) {
                                                // block continues at C only
                                                tr = (TrackSegment)lt.getConnectC();
                                                setting = LayoutSlip.STATE_AC;
                                                prevConnectType = LayoutEditor.SLIP_A;
                                            
                                            } else if (lb==((TrackSegment)ls.getConnectD()).getLayoutBlock() &&
                                                    lb!=((TrackSegment)ls.getConnectC()).getLayoutBlock()) {
                                                // block continues at D only
                                                setting = LayoutSlip.STATE_AD;
                                                tr = (TrackSegment)lt.getConnectD();
                                                prevConnectType = LayoutEditor.SLIP_A;
                                            } else {
                                                if((ls.getConnectC()!=null) && trackSegmentLeadsTo((TrackSegment)ls.getConnectC(), (Object)ls)){
                                                    prevConnectType = LayoutEditor.SLIP_C;
                                                    setting = LayoutSlip.STATE_AC;
                                                    tr = (TrackSegment)lt.getConnectC();
                                                } else if((ls.getConnectD()!=null) && trackSegmentLeadsTo((TrackSegment)ls.getConnectD(), (Object)ls)){
                                                    prevConnectType = LayoutEditor.SLIP_D;
                                                    setting = LayoutSlip.STATE_AD;
                                                    tr = (TrackSegment)lt.getConnectD();
                                                }
                                                else {
                                                    log.error("Error - Neither branch at track node leads to requested Block.(LS1)");
                                                    tr=null;
                                                }
                                            }
                                            break;
                case LayoutEditor.SLIP_B :  if(nlb==((TrackSegment)ls.getConnectD()).getLayoutBlock()){
                                                // exiting block at D
                                                prevConnectType= LayoutEditor.SLIP_D;
                                                setting = LayoutSlip.STATE_BD;
                                                tr = (TrackSegment)ls.getConnectD();
                                            } else if (nlb==((TrackSegment)ls.getConnectC()).getLayoutBlock() &&
                                                tType == LayoutSlip.DOUBLE_SLIP){
                                                // exiting block at C
                                                prevConnectType= LayoutEditor.SLIP_C;
                                                setting = LayoutSlip.STATE_BC;
                                                tr = (TrackSegment)ls.getConnectC();
                                            }  else {
                                                if(tType == LayoutSlip.DOUBLE_SLIP){
                                                    if(lb==((TrackSegment)ls.getConnectD()).getLayoutBlock() &&
                                                        lb!=((TrackSegment)ls.getConnectC()).getLayoutBlock()){
                                                        //Found continuing at D only
                                                        tr = (TrackSegment)lt.getConnectD();
                                                        setting = LayoutSlip.STATE_BD;
                                                        prevConnectType = LayoutEditor.SLIP_D;
                                                    
                                                    
                                                    } else if(lb==((TrackSegment)ls.getConnectC()).getLayoutBlock() &&
                                                        lb!=((TrackSegment)ls.getConnectD()).getLayoutBlock()){
                                                        //Found continuing at C only
                                                        tr = (TrackSegment)lt.getConnectC();
                                                        setting = LayoutSlip.STATE_BC;
                                                        prevConnectType = LayoutEditor.SLIP_B;
                                                    } else {
                                                        if((ls.getConnectD()!=null) && trackSegmentLeadsTo((TrackSegment)ls.getConnectD(), (Object)ls)){
                                                            prevConnectType = LayoutEditor.SLIP_D;
                                                            setting = LayoutSlip.STATE_BD;
                                                            tr = (TrackSegment)lt.getConnectD();
                                                        } else if((ls.getConnectC()!=null) && trackSegmentLeadsTo((TrackSegment)ls.getConnectC(), (Object)ls)){
                                                            prevConnectType = LayoutEditor.SLIP_C;
                                                            setting = LayoutSlip.STATE_BC;
                                                            tr = (TrackSegment)lt.getConnectC();
                                                        }
                                                        else {
                                                            log.error("Error - Neither branch at track node leads to requested Block.(LS2)");
                                                            tr=null;
                                                        }
                                                    
                                                    
                                                    }
                                                } else {
                                                    if(lb==((TrackSegment)ls.getConnectD()).getLayoutBlock()){
                                                        //Found continuing at D only
                                                        tr = (TrackSegment)lt.getConnectD();
                                                        setting = LayoutSlip.STATE_BD;
                                                        prevConnectType = LayoutEditor.SLIP_D;
                                                    } else {
                                                        tr=null;
                                                    }
                                                }
                                            }
                                            break;
                case LayoutEditor.SLIP_C :  if(nlb==((TrackSegment)ls.getConnectA()).getLayoutBlock()){
                                                // exiting block at A
                                                prevConnectType= LayoutEditor.SLIP_A;
                                                setting = LayoutSlip.STATE_AC;
                                                tr = (TrackSegment)ls.getConnectA();
                                            } else if (nlb==((TrackSegment)ls.getConnectB()).getLayoutBlock() &&
                                                tType == LayoutSlip.DOUBLE_SLIP){
                                                // exiting block at B
                                                prevConnectType= LayoutEditor.SLIP_B;
                                                setting = LayoutSlip.STATE_BC;
                                                tr = (TrackSegment)ls.getConnectB();
                                            } else {
                                                if(tType == LayoutSlip.DOUBLE_SLIP){
                                                    if(lb==((TrackSegment)ls.getConnectA()).getLayoutBlock() &&
                                                        lb!=((TrackSegment)ls.getConnectB()).getLayoutBlock()){
                                                        //Found continuing at A only
                                                        tr = (TrackSegment)lt.getConnectA();
                                                        setting = LayoutSlip.STATE_AC;
                                                        prevConnectType = LayoutEditor.SLIP_A;
                                                    
                                                    
                                                    } else if(lb==((TrackSegment)ls.getConnectB()).getLayoutBlock() &&
                                                        lb!=((TrackSegment)ls.getConnectA()).getLayoutBlock()){
                                                        //Found continuing at B only
                                                        tr = (TrackSegment)lt.getConnectB();
                                                        setting = LayoutSlip.STATE_BC;
                                                        prevConnectType = LayoutEditor.SLIP_B;
                                                        
                                                        
                                                    } else {
                                                        if((ls.getConnectA()!=null) && trackSegmentLeadsTo((TrackSegment)ls.getConnectA(), (Object)ls)){
                                                            prevConnectType = LayoutEditor.SLIP_A;
                                                            setting = LayoutSlip.STATE_AC;
                                                            tr = (TrackSegment)lt.getConnectA();
                                                        } else if((ls.getConnectB()!=null) && trackSegmentLeadsTo((TrackSegment)ls.getConnectB(), (Object)ls)){
                                                            prevConnectType = LayoutEditor.SLIP_B;
                                                            setting = LayoutSlip.STATE_BC;
                                                            tr = (TrackSegment)lt.getConnectB();
                                                        }
                                                        else {
                                                            log.error("Error - Neither branch at track node leads to requested Block.(LS3)");
                                                            tr=null;
                                                        }
                                                    }
                                                } else {
                                                    if(lb==((TrackSegment)ls.getConnectA()).getLayoutBlock()){
                                                        //Found continuing at A only
                                                        tr = (TrackSegment)lt.getConnectA();
                                                        setting = LayoutSlip.STATE_AC;
                                                        prevConnectType = LayoutEditor.SLIP_A;
                                                    } else {
                                                        tr=null;
                                                    }
                                                }
                                            }
                                            break;
                case LayoutEditor.SLIP_D : if(nlb==((TrackSegment)ls.getConnectB()).getLayoutBlock()){
                                                // exiting block at B
                                                prevConnectType= LayoutEditor.SLIP_B;
                                                setting = LayoutSlip.STATE_BD;
                                                tr = (TrackSegment)ls.getConnectB();
                                            } else if (nlb==((TrackSegment)ls.getConnectA()).getLayoutBlock()){
                                                // exiting block at B
                                                prevConnectType= LayoutEditor.SLIP_A;
                                                setting = LayoutSlip.STATE_AD;
                                                tr = (TrackSegment)ls.getConnectA();
                                            } else if (lb==((TrackSegment)ls.getConnectB()).getLayoutBlock() &&
                                                lb!=((TrackSegment)ls.getConnectA()).getLayoutBlock()){
                                                //Found continuing at B only
                                                tr = (TrackSegment)lt.getConnectB();
                                                setting = LayoutSlip.STATE_BD;
                                                prevConnectType = LayoutEditor.SLIP_B;
                                            
                                            } else if (lb==((TrackSegment)ls.getConnectA()).getLayoutBlock() &&
                                                lb!=((TrackSegment)ls.getConnectB()).getLayoutBlock()) {
                                                //Found continuing at A only
                                                setting = LayoutSlip.STATE_AD;
                                                tr = (TrackSegment)lt.getConnectA();
                                                prevConnectType = LayoutEditor.SLIP_A;
                                            } else {
                                                if((ls.getConnectA()!=null) && trackSegmentLeadsTo((TrackSegment)ls.getConnectA(), (Object)ls)){
                                                    prevConnectType = LayoutEditor.SLIP_A;
                                                    setting = LayoutSlip.STATE_AD;
                                                    tr = (TrackSegment)lt.getConnectA();
                                                } else if((ls.getConnectB()!=null) && trackSegmentLeadsTo((TrackSegment)ls.getConnectB(), (Object)ls)){
                                                    prevConnectType = LayoutEditor.SLIP_B;
                                                    setting = LayoutSlip.STATE_BD;
                                                    tr = (TrackSegment)lt.getConnectB();
                                                }
                                                else {
                                                    log.error("Error - Neither branch at track node leads to requested Block.(LS4)");
                                                    tr=null;
                                                }
                                            }
                                            break;
                default : break;
            }
            if ( (tr!=null) && (tr.getLayoutBlock() != lb) ) {
                // continuing track segment is not in this block
                tr = null;
            }
            else if (tr==null) {
                log.error("Connectivity not complete at Slip "+ls.getDisplayName());
                            turnoutConnectivity = false;
            }
        } else {
            switch (cType) {
                case LayoutEditor.TURNOUT_A:
                    // check for left-handed crossover
                    if (tType == LayoutTurnout.LH_XOVER) {
                        // entering at a continuing track of a left-handed crossover
                        prevConnectType = LayoutEditor.TURNOUT_B;
                        setting = Turnout.CLOSED;
                        tr = (TrackSegment)lt.getConnectB();					
                    }
                    // entering at a throat, determine exit by checking block of connected track segment
                    else if ( (nlb==lt.getLayoutBlockB()) || ((lt.getConnectB()!=null) && 
                                (nlb==((TrackSegment)lt.getConnectB()).getLayoutBlock())) ) {
                        // exiting block at continuing track
                        prevConnectType = LayoutEditor.TURNOUT_B;
                        setting = Turnout.CLOSED;
                        tr = (TrackSegment)lt.getConnectB();
                    }
                    else if ( (nlb==lt.getLayoutBlockC()) || ((lt.getConnectC()!=null) && 
                                (nlb==((TrackSegment)lt.getConnectC()).getLayoutBlock())) ) {
                        // exiting block at diverging track
                        prevConnectType = LayoutEditor.TURNOUT_C;
                        tr = (TrackSegment)lt.getConnectC();
                    }
                    // must stay in block after turnout - check if only one track continues in block
                    else if ((lt.getConnectB()!=null) &&  (lb==((TrackSegment)lt.getConnectB()).getLayoutBlock()) && 
                            (lt.getConnectC()!=null) && (lb!=((TrackSegment)lt.getConnectC()).getLayoutBlock())) {
                        // continuing in block on continuing track only
                        prevConnectType = LayoutEditor.TURNOUT_B;
                        setting = Turnout.CLOSED;
                        tr = (TrackSegment)lt.getConnectB();
                    }
                    else if ((lt.getConnectC()!=null) && (lb==((TrackSegment)lt.getConnectC()).getLayoutBlock()) &&
                            (lt.getConnectB()!=null) && (lb!=((TrackSegment)lt.getConnectB()).getLayoutBlock())) {
                        // continuing in block on diverging track only
                        prevConnectType = LayoutEditor.TURNOUT_C;
                        tr = (TrackSegment)lt.getConnectC();
                    }
                    // both connecting track segments continue in current block, must search further
                    else {
                        // check if continuing track leads to the next block
                        if ((lt.getConnectB()!=null) && trackSegmentLeadsTo((TrackSegment)lt.getConnectB(),(Object)lt)) {
                            prevConnectType = LayoutEditor.TURNOUT_B;
                            setting = Turnout.CLOSED;
                            tr = (TrackSegment)lt.getConnectB();
                        }
                        // check if diverging track leads to the next block
                        else if ((lt.getConnectC()!=null) && trackSegmentLeadsTo((TrackSegment)lt.getConnectC(),(Object)lt)) {
                            prevConnectType = LayoutEditor.TURNOUT_C;
                            tr = (TrackSegment)lt.getConnectC();
                        }
                        else {
                            log.error("Error - Neither branch at track node leads to requested Block.(1)");
                            tr = null;
                        }					
                    }
                    break;
                case LayoutEditor.TURNOUT_B:
                    if ( (tType==LayoutTurnout.LH_XOVER) || (tType==LayoutTurnout.DOUBLE_XOVER) ) {
                        // entering at a throat of a double crossover or a left-handed crossover
                        if ( (nlb==lt.getLayoutBlock()) || ((lt.getConnectA()!=null) && 
                                    (nlb==((TrackSegment)lt.getConnectA()).getLayoutBlock())) ) {
                            // exiting block at continuing track
                            prevConnectType = LayoutEditor.TURNOUT_A;
                            setting = Turnout.CLOSED;
                            tr = (TrackSegment)lt.getConnectB();
                        }
                        else if ( (nlb==lt.getLayoutBlockD()) || ((lt.getConnectD()!=null) && 
                                    (nlb==((TrackSegment)lt.getConnectD()).getLayoutBlock())) ) {
                            // exiting block at diverging track
                            prevConnectType = LayoutEditor.TURNOUT_D;
                            tr = (TrackSegment)lt.getConnectD();
                        }
                        // must stay in block after turnout
                        else if ( ((lt.getConnectA()!=null) && (lb==((TrackSegment)lt.getConnectA()).getLayoutBlock())) && 
                                    ((lt.getConnectD()!=null) && (lb!=((TrackSegment)lt.getConnectD()).getLayoutBlock())) ) {
                            // continuing in block on continuing track only
                            prevConnectType = LayoutEditor.TURNOUT_A;
                            setting = Turnout.CLOSED;
                            tr = (TrackSegment)lt.getConnectA();
                        }
                        else if ( ((lt.getConnectD()!=null) && (lb==((TrackSegment)lt.getConnectD()).getLayoutBlock())) && 
                                    ((lt.getConnectA()!=null) && (lb!=((TrackSegment)lt.getConnectA()).getLayoutBlock())) ) {
                            // continuing in block on diverging track only
                            prevConnectType = LayoutEditor.TURNOUT_D;
                            tr = (TrackSegment)lt.getConnectD();
                        }
                        // both connecting track segments continue in current block, must search further
                        else {
                            // check if continuing track leads to the next block
                            if ((lt.getConnectA()!=null) && trackSegmentLeadsTo((TrackSegment)lt.getConnectA(),(Object)lt)) {
                                prevConnectType = LayoutEditor.TURNOUT_A;
                                setting = Turnout.CLOSED;
                                tr = (TrackSegment)lt.getConnectA();
                            }
                            // check if diverging track leads to the next block
                            else if ((lt.getConnectD()!=null) && trackSegmentLeadsTo((TrackSegment)lt.getConnectD(),(Object)lt)) {
                                prevConnectType = LayoutEditor.TURNOUT_D;
                                tr = (TrackSegment)lt.getConnectD();
                            }
                            else {
                                log.error("Error - Neither branch at track node leads to requested Block.(2)");
                                tr = null;
                            }					
                        }
                    }
                    else {
                        // entering at continuing track, must exit at throat
                        prevConnectType = LayoutEditor.TURNOUT_A;
                        setting = Turnout.CLOSED;
                        tr = (TrackSegment)lt.getConnectA();
                    }
                    break;
                case LayoutEditor.TURNOUT_C:
                    if ( (tType==LayoutTurnout.RH_XOVER) || (tType==LayoutTurnout.DOUBLE_XOVER) ) {
                        // entering at a throat of a double crossover or a right-handed crossover
                        if ( (nlb==lt.getLayoutBlockD()) || ((lt.getConnectD()!=null) && 
                                    (nlb==((TrackSegment)lt.getConnectD()).getLayoutBlock())) ) {
                            // exiting block at continuing track
                            prevConnectType = LayoutEditor.TURNOUT_D;
                            setting = Turnout.CLOSED;
                            tr = (TrackSegment)lt.getConnectD();
                        }
                        else if ( (nlb==lt.getLayoutBlock()) || ((lt.getConnectA()!=null) && 
                                    (nlb==((TrackSegment)lt.getConnectA()).getLayoutBlock())) ) {
                            // exiting block at diverging track
                            prevConnectType = LayoutEditor.TURNOUT_A;
                            tr = (TrackSegment)lt.getConnectA();
                        }
                        // must stay in block after turnout
                        else if ( ((lt.getConnectD()!=null) && (lb==((TrackSegment)lt.getConnectD()).getLayoutBlock())) && 
                                    ((lt.getConnectA()!=null) && (lb!=((TrackSegment)lt.getConnectA()).getLayoutBlock())) ) {
                            // continuing in block on continuing track
                            prevConnectType = LayoutEditor.TURNOUT_D;
                            setting = Turnout.CLOSED;
                            tr = (TrackSegment)lt.getConnectD();
                        }
                        else if ( ((lt.getConnectA()!=null) && (lb==((TrackSegment)lt.getConnectA()).getLayoutBlock())) && 
                                    ((lt.getConnectD()!=null) && (lb!=((TrackSegment)lt.getConnectD()).getLayoutBlock())) ) {
                            // continuing in block on diverging track
                            prevConnectType = LayoutEditor.TURNOUT_A;
                            tr = (TrackSegment)lt.getConnectA();
                        }
                        // both connecting track segments continue in current block, must search further
                        else {
                            // check if continuing track leads to the next block
                            if ((lt.getConnectD()!=null) && trackSegmentLeadsTo((TrackSegment)lt.getConnectD(),(Object)lt)) {
                                prevConnectType = LayoutEditor.TURNOUT_D;
                                setting = Turnout.CLOSED;
                                tr = (TrackSegment)lt.getConnectD();
                            }
                            // check if diverging track leads to the next block
                            else if ((lt.getConnectA()!=null) && trackSegmentLeadsTo((TrackSegment)lt.getConnectA(),(Object)lt)) {
                                prevConnectType = LayoutEditor.TURNOUT_A;
                                tr = (TrackSegment)lt.getConnectA();
                            }
                            else {
                                log.error("Error - Neither branch at track node leads to requested Block.(3)");
                                tr = null;
                            }					
                        }
                    }
                    else {
                        // entering at diverging track, must exit at throat
                        prevConnectType = LayoutEditor.TURNOUT_A;				
                        tr = (TrackSegment)lt.getConnectA();
                    }
                    break;
                case LayoutEditor.TURNOUT_D:
                    if ( (tType==LayoutTurnout.LH_XOVER) || (tType==LayoutTurnout.DOUBLE_XOVER) ) {
                        // entering at a throat of a double crossover or a left-handed crossover
                        if ( (nlb==lt.getLayoutBlockC()) || ((lt.getConnectC()!=null) && 
                                    (nlb==((TrackSegment)lt.getConnectC()).getLayoutBlock())) ) {
                            // exiting block at continuing track
                            prevConnectType = LayoutEditor.TURNOUT_C;
                            setting = Turnout.CLOSED;
                            tr = (TrackSegment)lt.getConnectC();
                        }
                        else if ( (nlb==lt.getLayoutBlockB()) || ((lt.getConnectB()!=null) && 
                                    (nlb==((TrackSegment)lt.getConnectB()).getLayoutBlock())) ) {
                            // exiting block at diverging track
                            prevConnectType = LayoutEditor.TURNOUT_B;
                            tr = (TrackSegment)lt.getConnectB();
                        }
                        // must stay in block after turnout
                        else if ( ((lt.getConnectC()!=null) && (lb==((TrackSegment)lt.getConnectC()).getLayoutBlock())) && 
                                    ((lt.getConnectB()!=null) && (lb!=((TrackSegment)lt.getConnectB()).getLayoutBlock())) ) {
                            // continuing in block on continuing track
                            prevConnectType = LayoutEditor.TURNOUT_C;
                            setting = Turnout.CLOSED;
                            tr = (TrackSegment)lt.getConnectC();
                        }
                        else if ( ((lt.getConnectB()!=null) && (lb==((TrackSegment)lt.getConnectB()).getLayoutBlock())) && 
                                    ((lt.getConnectC()!=null) && (lb!=((TrackSegment)lt.getConnectC()).getLayoutBlock())) ) {
                            // continuing in block on diverging track
                            prevConnectType = LayoutEditor.TURNOUT_B;
                            tr = (TrackSegment)lt.getConnectB();
                        }
                        // both connecting track segments continue in current block, must search further
                        else {
                            // check if continuing track leads to the next block
                            if ((lt.getConnectC()!=null) && trackSegmentLeadsTo((TrackSegment)lt.getConnectC(),(Object)lt)) {
                                prevConnectType = LayoutEditor.TURNOUT_C;
                                setting = Turnout.CLOSED;
                                tr = (TrackSegment)lt.getConnectC();
                            }
                            // check if diverging track leads to the next block
                            else if ((lt.getConnectB()!=null) && trackSegmentLeadsTo((TrackSegment)lt.getConnectB(),(Object)lt)) {
                                prevConnectType = LayoutEditor.TURNOUT_B;
                                tr = (TrackSegment)lt.getConnectB();
                            }
                            else {
                                log.error("Error - Neither branch at track node leads to requested Block.(2)");
                                tr = null;
                            }					
                        }
                    }
                    else {
                    // entering at diverging track of a right-handed crossover, must exit at throat
                        prevConnectType = LayoutEditor.TURNOUT_A;				
                        tr = (TrackSegment)lt.getConnectA();
                    }
                    break;
            }
            if ( (tr!=null) && (tr.getLayoutBlock() != lb) ) {
                // continuing track segment is not in this block
                tr = null;
            }
            else if (tr==null) {
                log.error("Connectivity not complete at turnout "+lt.getTurnoutName());
                            turnoutConnectivity = false;
            }
            if (lt.getContinuingSense() != Turnout.CLOSED) {
                if (setting == Turnout.THROWN) setting = Turnout.CLOSED;
                else if (setting == Turnout.CLOSED) setting = Turnout.THROWN;
            }
        }
		return (Integer.valueOf(setting));
	}
		
	/**
	 * This method follows the track from a beginning track segment to its exits 
	 *	from the current LayoutBlock 'lb' until the track connects to the designated 
	 *  Block 'nlb' or all exit points have been tested. 
	 * Returns 'true' if designated Block is connected; returns 'false' if not.
	 */	
	private boolean trackSegmentLeadsTo(TrackSegment tsg, Object ob) {
		if ( (tsg==null) || (ob==null) ) {
			log.error("Null argument on entry to trackSegmentLeadsTo");
			return false;
		}
		TrackSegment curTS = tsg;
		Object curObj = ob;
		ArrayList<TrackSegment> posTS = new ArrayList<TrackSegment>(); 
		ArrayList<Object> posOB = new ArrayList<Object>();
		int conType = 0;
		Object conObj = null;
		// follow track to all exit points outside this block
		while (curTS!=null) {
			if (curTS.getLayoutBlock()==nlb) return true;
			if (curTS.getLayoutBlock()==lb) {
				// identify next destination along track
				if (curTS.getConnect1() == curObj) {
					// entered through 1, leaving through 2
					conType = curTS.getType2();
					conObj = curTS.getConnect2();
				}
				else if (curTS.getConnect2() == curObj) {
					// entered through 2, leaving through 1
					conType = curTS.getType1();
					conObj = curTS.getConnect1();
				}
				else {
					log.error("Connectivity error when following track in Block "+lb.getUserName());
					return false;
				}
				// follow track according to next destination type
				if (conType==LayoutEditor.POS_POINT) {
					// reached anchor point or end bumper
					if (((PositionablePoint)conObj).getType() == PositionablePoint.END_BUMPER) {
						// end of line without reaching 'nlb'
						curTS = null;					
					}
					else if (((PositionablePoint)conObj).getType() == PositionablePoint.ANCHOR) {
						// proceed to next track segment if within the same Block
						if (((PositionablePoint)conObj).getConnect1() == curTS) {
							curTS = (((PositionablePoint)conObj).getConnect2());
						}
						else {
							curTS = (((PositionablePoint)conObj).getConnect1());
						}
						curObj = conObj;
					}
				}
				else if ( (conType>=LayoutEditor.LEVEL_XING_A) && (conType<=LayoutEditor.LEVEL_XING_D) ) {
					// reached a level crossing
					if ( (conType==LayoutEditor.LEVEL_XING_A) || (conType==LayoutEditor.LEVEL_XING_C) ) {
						if (((LevelXing)conObj).getLayoutBlockAC()!=lb) {
							if (((LevelXing)conObj).getLayoutBlockAC()==nlb) return true;
							else curTS = null;
						}
						else if (conType==LayoutEditor.LEVEL_XING_A) {
							curTS = (TrackSegment)((LevelXing)conObj).getConnectC();
						}
						else {
							curTS = (TrackSegment)((LevelXing)conObj).getConnectA();
						}
					}
					else {
						if (((LevelXing)conObj).getLayoutBlockBD()!=lb) {
							if (((LevelXing)conObj).getLayoutBlockBD()==nlb) return true;
							else curTS = null;
						}
						else if (conType==LayoutEditor.LEVEL_XING_B) {
							curTS = (TrackSegment)((LevelXing)conObj).getConnectD();
						}
						else {
							curTS = (TrackSegment)((LevelXing)conObj).getConnectB();
						}
					}
					curObj = conObj;
				}
				else if ( (conType>=LayoutEditor.TURNOUT_A) && (conType<=LayoutEditor.TURNOUT_D) ) {
					// reached a turnout
					LayoutTurnout lt = (LayoutTurnout)conObj;
					int tType = lt.getTurnoutType();
					if ( (tType==LayoutTurnout.DOUBLE_XOVER) || (tType==LayoutTurnout.RH_XOVER) ||
							(tType==LayoutTurnout.LH_XOVER) ) {
						// reached a crossover turnout
						switch (conType) {
							case LayoutEditor.TURNOUT_A:
								if ((lt.getLayoutBlock())!=lb) {
									if (lt.getLayoutBlock()==nlb) return true;
									else curTS = null;
								}
								else if ( (lt.getLayoutBlockB()==nlb) || ( (tType!=LayoutTurnout.LH_XOVER) && 
															(lt.getLayoutBlockC()==nlb) ) )return true;
								else if (lt.getLayoutBlockB()==lb) {
									curTS = (TrackSegment)lt.getConnectB();
									if ( (tType!=LayoutTurnout.LH_XOVER) && (lt.getLayoutBlockC()==lb) ) {
										//if (posTS != null) {
											posTS.add((TrackSegment)lt.getConnectC());
										//}
										posOB.add(conObj);
									}
								}
								else if ( (tType!=LayoutTurnout.LH_XOVER) && (lt.getLayoutBlockC()==lb) ) {
									curTS = (TrackSegment)lt.getConnectC();
								}
								else curTS = null;
								curObj = conObj;
								break;
							case LayoutEditor.TURNOUT_B:
								if ((lt.getLayoutBlockB())!=lb) {
									if (lt.getLayoutBlockB()==nlb) return true;
									else curTS = null;
								}
								else if ( (lt.getLayoutBlock()==nlb) || ( (tType!=LayoutTurnout.RH_XOVER) && 
																		  (lt.getLayoutBlockD()==nlb) ) ) return true;
								else if (lt.getLayoutBlock()==lb) {
									curTS = (TrackSegment)lt.getConnectA();
									if ( (tType!=LayoutTurnout.RH_XOVER) && (lt.getLayoutBlockD()==lb) ) {
										//if (posTS != null) {
											posTS.add((TrackSegment)lt.getConnectD());
										//}
										posOB.add(conObj);
									}
								}
								else if ( (tType!=LayoutTurnout.RH_XOVER) && (lt.getLayoutBlockD()==lb) ) {
									curTS = (TrackSegment)lt.getConnectD();
								}
								else curTS = null;
								curObj = conObj;
								break;
							case LayoutEditor.TURNOUT_C:
								if ((lt.getLayoutBlockC())!=lb) {
									if (lt.getLayoutBlockC()==nlb) return true;
									else curTS = null;
								}
								else if ( (lt.getLayoutBlockD()==nlb) || ( (tType!=LayoutTurnout.LH_XOVER) && 
																		  (lt.getLayoutBlock()==nlb) ) )return true;
								else if (lt.getLayoutBlockD()==lb) {
									curTS = (TrackSegment)lt.getConnectD();
									if ( (tType!=LayoutTurnout.LH_XOVER) && (lt.getLayoutBlock()==lb) ) {
										//if (posTS != null) {
											posTS.add((TrackSegment)lt.getConnectA());
										//}
										posOB.add(conObj);
									}
								}
								else if ( (tType!=LayoutTurnout.LH_XOVER) && (lt.getLayoutBlock()==lb) ) {
									curTS = (TrackSegment)lt.getConnectA();
								}
								else curTS = null;
								curObj = conObj;
								break;
							case LayoutEditor.TURNOUT_D:
								if ((lt.getLayoutBlockD())!=lb) {
									if (lt.getLayoutBlockD()==nlb) return true;
									else curTS = null;
								}
								else if ( (lt.getLayoutBlockC()==nlb) || ( (tType!=LayoutTurnout.RH_XOVER) && 
																		  (lt.getLayoutBlockB()==nlb) ) )return true;
								else if (lt.getLayoutBlockC()==lb) {
									curTS = (TrackSegment)lt.getConnectC();
									if ( (tType!=LayoutTurnout.RH_XOVER) && (lt.getLayoutBlockB()==lb) ) {
										//if (posTS != null) {
											posTS.add((TrackSegment)lt.getConnectB());
										//}
										posOB.add(conObj);
									}
								}
								else if ( (tType!=LayoutTurnout.RH_XOVER) && (lt.getLayoutBlockB()==lb) ) {
									curTS = (TrackSegment)lt.getConnectB();
								}
								else curTS = null;
								curObj = conObj;
								break;
                            default : break;
						}
					}
					else if ( (tType==LayoutTurnout.RH_TURNOUT) || (tType==LayoutTurnout.LH_TURNOUT) ||
							 (tType==LayoutTurnout.WYE_TURNOUT) ) {
						// reached RH. LH, or WYE turnout
						if (lt.getLayoutBlock()!=lb) {
							if (lt.getLayoutBlock()==nlb) return true;
							else curTS = null;
						}
						else {
							if (conType==LayoutEditor.TURNOUT_A) {
								if ( (((TrackSegment)lt.getConnectB()).getLayoutBlock()==nlb) ||
									(((TrackSegment)lt.getConnectC()).getLayoutBlock()==nlb) ) return true;
								else if (((TrackSegment)lt.getConnectB()).getLayoutBlock()==lb) {
									curTS = (TrackSegment)lt.getConnectB();
									if (((TrackSegment)lt.getConnectC()).getLayoutBlock()==lb) {
										//if (posTS != null) {
											posTS.add((TrackSegment)lt.getConnectC());
										//}
										posOB.add(conObj);
									}
								}
								else curTS = (TrackSegment)lt.getConnectC();
							}							
							else curTS = (TrackSegment)lt.getConnectA();
							curObj = conObj;
						}
					}
				} 
                else if(conType>=LayoutEditor.SLIP_A && conType<=LayoutEditor.SLIP_D){
                    LayoutSlip ls = (LayoutSlip)conObj;
					int tType = ls.getTurnoutType();
                    if (ls.getLayoutBlock()!=lb){
                        if(ls.getLayoutBlock()==nlb) return true;
                        else curTS = null;
                    } else {
                        switch (conType){
                                case LayoutEditor.SLIP_A:
                                                     if(((TrackSegment)ls.getConnectC()).getLayoutBlock()==nlb) {
                                                        //Leg A-D has next lb
                                                        return true;
                                                     }
                                                     if(((TrackSegment)ls.getConnectD()).getLayoutBlock()==nlb){
                                                        //Leg A-C has next lb
                                                        return true;
                                                     }
                                                     if (((TrackSegment)ls.getConnectC()).getLayoutBlock()==lb){
                                                        curTS = (TrackSegment)ls.getConnectC();
                                                        if (((TrackSegment)ls.getConnectD()).getLayoutBlock()==lb) {
                                                            //if (posTS != null) {
                                                                posTS.add((TrackSegment)ls.getConnectD());
                                                            //}
                                                            posOB.add(conObj);
                                                        }
                                                     } else {
                                                        curTS = (TrackSegment)ls.getConnectD();
                                                     }
                                                     break;
                                case LayoutEditor.SLIP_B: 
                                                        if(tType==LayoutSlip.SINGLE_SLIP){
                                                            curTS = (TrackSegment)ls.getConnectD();
                                                            break;
                                                        }
                                                        if(((TrackSegment)ls.getConnectC()).getLayoutBlock()==nlb) {
                                                            //Leg B-C has next lb
                                                            return true;
                                                        }
                                                        if(((TrackSegment)ls.getConnectB()).getLayoutBlock()==nlb){
                                                            //Leg D-B has next lb
                                                            return true;
                                                        }
                                                        if (((TrackSegment)ls.getConnectC()).getLayoutBlock()==lb){
                                                            curTS = (TrackSegment)ls.getConnectC();
                                                            if (((TrackSegment)ls.getConnectD()).getLayoutBlock()==lb) {
                                                                //if (posTS != null) {
                                                                    posTS.add((TrackSegment)ls.getConnectD());
                                                                //}
                                                                posOB.add(conObj);
                                                            }
                                                        } else {
                                                            curTS = (TrackSegment)ls.getConnectD();
                                                        }
                                                        break;
                                case LayoutEditor.SLIP_C:
                                                        if(tType==LayoutSlip.SINGLE_SLIP){
                                                            curTS = (TrackSegment)ls.getConnectA();
                                                            break;
                                                        }
                                                        if(((TrackSegment)ls.getConnectA()).getLayoutBlock()==nlb) {
                                                            //Leg A-C has next lb
                                                            return true;
                                                        }
                                                        if(((TrackSegment)ls.getConnectB()).getLayoutBlock()==nlb){
                                                            //Leg B-C has next lb
                                                            return true;
                                                        }
                                                        if (((TrackSegment)ls.getConnectB()).getLayoutBlock()==lb){
                                                            curTS = (TrackSegment)ls.getConnectB();
                                                            if (((TrackSegment)ls.getConnectA()).getLayoutBlock()==lb) {
                                                                //if (posTS != null) {
                                                                    posTS.add((TrackSegment)ls.getConnectA());
                                                                //}
                                                                posOB.add(conObj);
                                                            }
                                                        } else {
                                                            curTS = (TrackSegment)ls.getConnectA();
                                                        }
                                                        break;
                                case LayoutEditor.SLIP_D:
                                                        if(((TrackSegment)ls.getConnectA()).getLayoutBlock()==nlb) {
                                                            //Leg D-A has next lb
                                                            return true;
                                                        }
                                                        if(((TrackSegment)ls.getConnectB()).getLayoutBlock()==nlb){
                                                            //Leg D-B has next lb
                                                            return true;
                                                        }
                                                        if (((TrackSegment)ls.getConnectB()).getLayoutBlock()==lb){
                                                            curTS = (TrackSegment)ls.getConnectB();
                                                            if (((TrackSegment)ls.getConnectA()).getLayoutBlock()==lb) {
                                                                //if (posTS != null) {
                                                                    posTS.add((TrackSegment)ls.getConnectA());
                                                                //}
                                                                posOB.add(conObj);
                                                            }
                                                        } else {
                                                            curTS = (TrackSegment)ls.getConnectA();
                                                        }
                                                        break;
                        }
                    }
                }
			}
			else curTS = null;			
				
			if (curTS==null) {
				// reached an end point outside this block that was not 'nlb' - any other paths to follow?
				//if ( (posTS!=null) && (posTS.size()>0) ) {
				if (posTS.size()>0) {	
					// paths remain, initialize the next one
					curTS = posTS.get(0);
					curObj = posOB.get(0);
					// remove it from the list of unexplored paths
					posTS.remove(0);
					posOB.remove(0);
				}
			}			
		}
		// searched all possible paths in this block, 'lb', without finding the desired exit block, 'nlb'
		return false;
	}
	
	private boolean turnoutConnectivity = true;
	/**
	 * This flag can be checked after performing a getTurnoutList() to check
	 *	if the connectivity of the turnouts has been completed in the block
	 *	when the getTurnoutList() was called.
	 *  Returns 'false' if a turnout conectivity is not complete.
	 *  Returns 'true' if the turnout conectivity is complete.
	 */
	public boolean isTurnoutConnectivityComplete() { return turnoutConnectivity; }

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
            default : break;
		}
		if (tr.getLayoutBlock() != lb) {
			// track segment is not in this block
			tr = null;
		}
		else {
			// track segment is in this block
			prevConnectObject = x;
		}
	}
	public ArrayList<LayoutTurnout> getAllTurnoutsThisBlock(LayoutBlock lb) {
		ArrayList<LayoutTurnout> list = new ArrayList<LayoutTurnout>();
		for (int i = 0; i < layoutEditor.turnoutList.size(); i++) {
			LayoutTurnout lt = layoutEditor.turnoutList.get(i);
			if ( (lt.getLayoutBlock()==lb) || (lt.getLayoutBlockB()==lb) ||
					(lt.getLayoutBlockC()==lb) || (lt.getLayoutBlockD()==lb) ) {
				list.add(lt);
			}		
		}
        for(LayoutTurnout lt :layoutEditor.slipList){
            if(lt.getLayoutBlock()==lb)
                list.add(lt);
        }
		return list;
	}


	// initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectivityUtil.class.getName());
}
