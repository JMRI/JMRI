// jmri.jmrit.display.LayoutBlockManager.java
package jmri.jmrit.display.layoutEditor;

import jmri.managers.AbstractManager;
import jmri.Sensor;
import jmri.Block;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.util.NamedBeanHandle;

import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

/**
 * Implementation of a Manager to handle LayoutBlocks
 * Note: that the same LayoutBlocks may appear in multiple LayoutEditor panels.
 * <P>
 * This manager does not enforce any particular system naming convention.
 * <P>
 * LayoutBlocks are usually addressed by userName.  The systemName is hidden
 *    from the user for the most part.
 *
 * @author      Dave Duchamp Copyright (C) 2007
 * @version	$Revision: 1.10 $
 */
public class LayoutBlockManager extends AbstractManager {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    public LayoutBlockManager() {
        super();
    }

    public String getSystemPrefix() { return "I"; }
    public char typeLetter() { return 'B'; }
	private int blkNum = 1;
    
    /**
     * Method to create a new LayoutBlock if the LayoutBlock does not exist
     *   Returns null if a LayoutBlock with the same systemName or userName
     *       already exists, or if there is trouble creating a new LayoutBlock.
	 *   Note that since the userName is used to address LayoutBlocks, the 
	 *       user name must be present.  If the user name is not present,
	 *       the new LayoutBlock is not created, and null is returned.
     */
    public LayoutBlock createNewLayoutBlock(String systemName, String userName) {
        // Check that LayoutBlock does not already exist
        LayoutBlock block = null;
        if (userName == null || userName.equals("")) {
			log.error("Attempt to create a LayoutBlock with no user name");
			return null;
		}
		block = getByUserName(userName);
		if (block!=null) return null;
		// here if not found under user name
		String sName = "";
		if (systemName == null) {
			// create a new unique system name
			boolean found = true;
			while (found) {
				sName = "ILB"+blkNum;
				blkNum++;
				block = getBySystemName(sName);
				if (block==null) found = false;
			}
		}
		else {
			// try the supplied system name
			block = getBySystemName((systemName.toUpperCase()));
			if (block!=null) return null;
			sName = systemName.toUpperCase();
		}
		// LayoutBlock does not exist, create a new LayoutBlock
		block = new LayoutBlock(sName,userName);
		// save in the maps
		register(block);
		return block;
    }

    /**
     * Remove an existing LayoutBlock. 
	 */
    public void deleteLayoutBlock(LayoutBlock block) {
        deregister(block);
    }
    
    /** 
     * Method to get an existing LayoutBlock.  First looks up assuming that
     *      name is a User Name.  If this fails looks up assuming
     *      that name is a System Name.  If both fail, returns null.
     */
    public LayoutBlock getLayoutBlock(String name) {
        LayoutBlock block = getByUserName(name);
        if (block!=null) return block;
        return getBySystemName(name);
    }

    public LayoutBlock getLayoutBlock(Block block){
        LayoutBlock lblock;
        java.util.Iterator<String> iter = getSystemNameList().iterator();
        while (iter.hasNext()) {
            String sName = iter.next();
            if (sName==null) {
                log.error("System name null during scan of LayoutBlocks");
            }
            else {
                    lblock = getBySystemName(sName);
                    if (lblock.getBlock() == block) return lblock;
            }
        }
        return null;
    }

    public LayoutBlock getBySystemName(String name) {
		String key = name.toUpperCase();
        return (LayoutBlock)_tsys.get(key);
    }

    public LayoutBlock getByUserName(String key) {
        return (LayoutBlock)_tuser.get(key);
    }
    
    static LayoutBlockManager _instance = null;
    static public LayoutBlockManager instance() {
        if (_instance == null) {
            _instance = new LayoutBlockManager();
        }
        return (_instance);
    }
	
	/**
	 * Method to find a LayoutBlock with a specified Sensor assigned as its 
	 *    occupancy sensor.  Returns the block or null if no existing LayoutBlock
	 *    has the Sensor assigned.
	 */
	public LayoutBlock getBlockWithSensorAssigned(Sensor s) {
		java.util.Iterator<String> iter = getSystemNameList().iterator();
        while (iter.hasNext()) {
            String sName = iter.next();
            if (sName==null) { 
                log.error("System name null during scan of LayoutBlocks");
            }
            else {
				LayoutBlock block = getBySystemName(sName);
				if (block.getOccupancySensor() == s) return block;
            }
        }
		return null;
	}
	
	/**
	 * Initializes/checks the Paths of all Blocks associated with LayoutBlocks.
	 * <P>
	 * This routine should be called when loading panels, after all Layout Editor panels have been loaded.
	 */
	public void initializeLayoutBlockPaths() {
		// cycle through all LayoutBlocks, completing initialization of associated jmri.Blocks
		java.util.Iterator<String> iter = getSystemNameList().iterator();
		while (iter.hasNext()) {
			String sName = iter.next();
			if (sName==null) log.error("System name null during 1st initialization of LayoutBlocks");
			LayoutBlock b = getBySystemName(sName); 
			b.initializeLayoutBlock();
		}	
		// cycle through all LayoutBlocks, updating Paths of associated jmri.Blocks
		badBeanErrors = 0;
		iter = getSystemNameList().iterator();
		while (iter.hasNext()) {
			String sName = iter.next();
			if (sName==null) log.error("System name null during 2nd initialization of LayoutBlocks");
			log.debug("LayoutBlock initialization - system name = "+sName);
			LayoutBlock b = getBySystemName(sName); 
			b.updatePaths();
			if (b.getBlock().getValue()!=null) b.getBlock().setValue(null);
		}
		if (badBeanErrors>0) {
			JOptionPane.showMessageDialog(null,""+badBeanErrors+" "+rb.getString("Warn2"),
					rb.getString("WarningTitle"),JOptionPane.ERROR_MESSAGE);
		}			
		try {
			new BlockValueFile().readBlockValues();
		} 
		catch (org.jdom.JDOMException jde) {
			log.error("JDOM Exception when retreiving block values "+jde);
		}				
		catch (java.io.IOException ioe) {
			log.error("I/O Exception when retreiving block values "+ioe);
		}
		// special tests for getFacingSignalHead method - comment out next three lines unless using LayoutEditorTests
//		LayoutEditorTests layoutEditorTests = new LayoutEditorTests();
//		layoutEditorTests.runClinicTests();
//		layoutEditorTests.runTestPanel3Tests();
	}
	private int badBeanErrors = 0;
	public void addBadBeanError() {badBeanErrors ++;}
	
	/**
	 * Method to return the Signal Head facing into a specified Block from a specified protected Block.
	 * <P>
	 * This method is primarily designed for use with scripts to get information initially residing in 
	 *    a Layout Editor panel.
	 * If either of the input Blocks is null, or if the two blocks do not join at a block boundary, or
	 *    if either of the input Blocks are not Layout Editor panel blocks,
	 *	  an error message is logged, and "null" is returned.
	 * If the signal at the block boundary has two heads--is located at the facing point of a turnout--
	 *	  the Signal Head that applies for the current setting of turnout (THROWN or CLOSED) is returned. 
	 *    If the turnout state is UNKNOWN or INCONSISTENT, an error message is logged, and "null" is returned.
	 * If the signal at the block boundary has three heads--the facing point of a 3-way turnout--the 
	 *	  Signal Head that applies for the current settings of the two turnouts of the 3-way turnout is returned.
	 *	  If the turnout state of either turnout is UNKNOWN or INCONSISTENT, an error is logged and "null" is returned.
	 * "null" is returned if the block boundary is between the two turnouts of a THROAT_TO_THROAT turnout or a 3-way
	 *    turnout. "null" is returned for block boundaries exiting a THROAT_TO_THROAT turnout block, since there are 
	 *    no signals that apply there.
	 */
	public SignalHead getFacingSignalHead (Block facingBlock, Block protectedBlock) {
		// check input
		if ( (facingBlock == null) || (protectedBlock == null) ) {
			log.error ("null block in call to getFacingSignalHead");
			return null;
		}
		// non-null - check if input corresponds to Blocks in a Layout Editor panel.
		LayoutBlock fLayoutBlock = getByUserName(facingBlock.getUserName());
		LayoutBlock pLayoutBlock = getByUserName(protectedBlock.getUserName());
		if ( (fLayoutBlock==null) || (pLayoutBlock==null) ) {
			if (fLayoutBlock==null) log.error("Block "+facingBlock.getSystemName()+"is not on a Layout Editor panel.");
			if (pLayoutBlock==null) log.error("Block "+protectedBlock.getSystemName()+"is not on a Layout Editor panel.");
			return null;
		}
		// input has corresponding LayoutBlocks - does it correspond to a block boundary?
		LayoutEditor panel = fLayoutBlock.getMaxConnectedPanel();
		ArrayList<LayoutConnectivity> c = panel.auxTools.getConnectivityList(fLayoutBlock);
		LayoutConnectivity lc = null;
		int i = 0;
		boolean facingIsBlock1 = true;
		while ((i<c.size()) && (lc==null)) {
			LayoutConnectivity tlc = c.get(i);
			if ( (tlc.getBlock1()==fLayoutBlock) && (tlc.getBlock2()==pLayoutBlock) ) {
				lc = tlc;
			}
			else if ( (tlc.getBlock1()==pLayoutBlock) && (tlc.getBlock2()==fLayoutBlock) ) {
				lc = tlc;
				facingIsBlock1 = false;
			}
			i ++;
		}
		if (lc==null) {
			log.error("Block "+facingBlock.getSystemName()+"is not connected to Block "+protectedBlock.getSystemName());
			return null;
		}
		// blocks are connected, get connection item types
		LayoutTurnout lt = null;
		TrackSegment tr = lc.getTrackSegment();
		int cType = 0;
		if (tr==null) {
			// this is an internal crossover block boundary
			lt = lc.getXover();
			cType = lc.getXoverBoundaryType();
			switch (cType) {
				case LayoutConnectivity.XOVER_BOUNDARY_AB:
					if (facingIsBlock1) 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
					else 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
				case LayoutConnectivity.XOVER_BOUNDARY_CD:
					if (facingIsBlock1) 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
					else 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD1Name()));
				case LayoutConnectivity.XOVER_BOUNDARY_AC:
					if (facingIsBlock1) {
						if ( (lt.getSignalA2Name()==null) || (lt.getSignalA2Name().equals("")) )
							//there is no signal head for diverging (crossed over)
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
						else
							// there is a diverging (crossed over) signal head, return it
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA2Name()));						
					}
					else {
						if ( (lt.getSignalC2Name()==null) || (lt.getSignalC2Name().equals("")) )
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
						else
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC2Name()));						
					}
				case LayoutConnectivity.XOVER_BOUNDARY_BD:
					if (facingIsBlock1) {
						if ( (lt.getSignalB2Name()==null) || (lt.getSignalB2Name().equals("")) )
							//there is no signal head for diverging (crossed over)
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
						else
							// there is a diverging (crossed over) signal head, return it
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB2Name()));						
					}
					else {
						if ( (lt.getSignalD2Name()==null) || (lt.getSignalD2Name().equals("")) )
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD1Name()));
						else
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD2Name()));						
					}
			}
			// should never reach here, but ...
			log.error("crossover turnout block boundary not found in getFacingSignal");
			return null;
		}
		// not internal crossover block boundary
		Object connected = lc.getConnectedObject();
		cType = lc.getConnectedType();
		if (cType==LayoutEditor.TRACK) {
			// block boundary is at an Anchor Point
			LayoutEditorTools tools = new LayoutEditorTools(panel);
			PositionablePoint p = panel.findPositionablePointAtTrackSegments(tr,(TrackSegment)connected);
			boolean block1IsWestEnd = tools.isAtWestEndOfAnchor(tr,p);
			if ( (block1IsWestEnd && facingIsBlock1) || (!block1IsWestEnd && !facingIsBlock1) ) {
				// block1 is on the west (north) end of the block boundary
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(p.getEastBoundSignal()));
			}
			else {
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(p.getWestBoundSignal()));
			}
		}
		if (cType==LayoutEditor.TURNOUT_A) {
			// block boundary is at the facing point of a turnout or A connection of a crossover turnout
			lt = (LayoutTurnout)connected;
			if (lt.getLinkType()==LayoutTurnout.NO_LINK) {
				// standard turnout or A connection of a crossover turnout
				if (facingIsBlock1) {
					if ( (lt.getSignalA2Name()==null) || (lt.getSignalA2Name().equals("")) )
						//there is no signal head for diverging 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
					else {
						// check if track segments at B or C are in protected block (block 2)
						if ( ((TrackSegment)(lt.getConnectB())).getBlockName().equals(protectedBlock.getUserName()) ) {
							// track segment connected at B matches block 2, check C
							if ( !(((TrackSegment)lt.getConnectC()).getBlockName().equals(protectedBlock.getUserName())) ) {
								// track segment connected at C is not in block2, return continuing signal head at A
								if (lt.getContinuingSense()==Turnout.CLOSED) 
									return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
								else 
									return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA2Name()));
							}
							else {
								// B and C both in block2, check turnout position to decide which signal head to return
								int state = lt.getTurnout().getKnownState();
								if ( ( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
										( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.THROWN) ) ) 
									// continuing
										return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
								else if ( ( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
										( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.THROWN) ) ) 
									// diverging
									return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA2Name()));
								else {
									// turnout state is UNKNOWN or INCONSISTENT
									log.error("Cannot choose signal head because turnout "+lt.getTurnout().getSystemName()+
										" is in an UNKNOWN or INCONSISTENT state.");
									return null;
								}
							}
						}
						// track segment connected at B is not in block 2
						if ( (((TrackSegment)lt.getConnectC()).getBlockName().equals(protectedBlock.getUserName())) ) {
							// track segment connected at C is in block 2, return diverging signal head
							if (lt.getContinuingSense()==Turnout.CLOSED)
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA2Name()));
							else 
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
						}
						else {
							// neither track segment is in block 2 - should never get here unless layout turnout is 
							//      the only item in block 2
							if (!(lt.getBlockName().equals(protectedBlock.getUserName())))
								log.error("neither signal at A protects block "+protectedBlock.getUserName()+
												", and turnout is not in block either");
							return null;
						}
					}
				}
				else {
					// check if track segments at B or C are in facing block (block 1)
					if ( ((TrackSegment)(lt.getConnectB())).getBlockName().equals(facingBlock.getUserName()) ) {
						// track segment connected at B matches block 1, check C
						if ( !(((TrackSegment)lt.getConnectC()).getBlockName().equals(facingBlock.getUserName())) ) 
							// track segment connected at C is not in block 2, return signal head at continuing end
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
						else {
							// B and C both in block 1, check turnout position to decide which signal head to return
							int state = lt.getTurnout().getKnownState();
							if ( ( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
										( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.THROWN) ) )
								// continuing  
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
							else if ( ( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
										( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.THROWN) ) ) {
								// diverging, check for second head
								if ( (lt.getSignalC2Name()==null) || (lt.getSignalC2Name().equals("")) )
									return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
								else 
									return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC2Name()));							
							}
							else {
								// turnout state is UNKNOWN or INCONSISTENT
								log.error("Cannot choose signal head because turnout "+lt.getTurnout().getSystemName()+
											" is in an UNKNOWN or INCONSISTENT state.");
								return null;
							}
						}
					}
					// track segment connected at B is not in block 1
					if ( ((TrackSegment)lt.getConnectC()).getBlockName().equals(facingBlock.getUserName()) ) {
						// track segment connected at C is in block 1, return diverging signal head, check for second head
						if ( (lt.getSignalC2Name()==null) || (lt.getSignalC2Name().equals("")) )
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
						else 
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC2Name()));
					}
					else {
						// neither track segment is in block 1 - should never get here unless layout turnout is 
						//    the only item in block 1
						if (!(lt.getBlockName().equals(facingBlock.getUserName())))
							log.error("no signal faces block "+facingBlock.getUserName()+
												", and turnout is not in block either");
						return null;
					}
				}
			}
			else if (lt.getLinkType()==LayoutTurnout.THROAT_TO_THROAT) {
				//  There are no signals at the throat of a THROAT_TO_THROAT
				//  There should not be a block boundary here
				return null;
			}
			else if (lt.getLinkType()==LayoutTurnout.FIRST_3_WAY) {
				// 3-way turnout is in its own block - block boundary is at the throat of the 3-way turnout
				if (!facingIsBlock1) {				
					// facing block is within the three-way turnout's block - no signals for exit of the block
					return null;
				}
				else {
					// select throat signal according to state of the 3-way turnout
					int state = lt.getTurnout().getKnownState();
					if (state==Turnout.THROWN) {
						if ( (lt.getSignalA2Name()==null) || (lt.getSignalA2Name().equals("")) ) 
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
						else 
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA2Name()));
					}
					else if (state==Turnout.CLOSED) {
						LayoutTurnout tLinked = getLayoutTurnoutFromTurnoutName(lt.getLinkedTurnoutName(),panel);
						state = tLinked.getTurnout().getKnownState();
						if (state==Turnout.CLOSED) {						
							if (tLinked.getContinuingSense()==Turnout.CLOSED) 
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));								
							else if ( (lt.getSignalA3Name()==null) || (lt.getSignalA3Name().equals("")) ) 
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
							else 
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA3Name()));
						}
						else if (state==Turnout.THROWN) {						
							if (tLinked.getContinuingSense()==Turnout.THROWN) 
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));								
							else if ( (lt.getSignalA3Name()==null) || (lt.getSignalA3Name().equals("")) ) 
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
							else 
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA3Name()));
						}
						else {
							// should never get here - linked turnout state is UNKNOWN or INCONSISTENT
							log.error("Cannot choose 3-way signal head to return because turnout "+tLinked.getTurnout().
									getSystemName()+" is in an UNKNOWN or INCONSISTENT state.");
							return null;
						}
					}
					else {
						// should never get here - linked turnout state is UNKNOWN or INCONSISTENT
						log.error("Cannot choose 3-way signal head to return because turnout "+lt.getTurnout().
									getSystemName()+" is in an UNKNOWN or INCONSISTENT state.");
						return null;
					}
				}
			}
			else if (lt.getLinkType()==LayoutTurnout.SECOND_3_WAY) {
				// There are no signals at the throat of the SECOND_3_WAY turnout of a 3-way turnout
				// There should not be a block boundary here
				return null;
			}
		}
		if (cType==LayoutEditor.TURNOUT_B) {
			// block boundary is at the continuing track of a turnout or B connection of a crossover turnout
			lt = (LayoutTurnout)connected;
			// check for double crossover or LH crossover 
			if ( ((lt.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) || 
						(lt.getTurnoutType()==LayoutTurnout.LH_XOVER)) ) {
				if (facingIsBlock1) {
					if ( (lt.getSignalB2Name()==null) || (lt.getSignalB2Name().equals("")) ) 
					// there is only one signal at B, return it
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
					// check if track segments at A or D are in protected block (block 2)
					if ( ((TrackSegment)(lt.getConnectA())).getBlockName().equals(protectedBlock.getUserName()) ) {
						// track segment connected at A matches block 2, check D
						if ( !(((TrackSegment)lt.getConnectD()).getBlockName().equals(protectedBlock.getUserName())) ) {
							// track segment connected at D is not in block2, return continuing signal head at B
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
						}
						else {
							// A and D both in block 2, check turnout position to decide which signal head to return
							int state = lt.getTurnout().getKnownState();
							if ( ( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
									( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.THROWN) ) )
								// continuing  
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
							else if ( ( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
									( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.THROWN) ) ) 
								// diverging (crossed over)
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB2Name()));
							else {
								// turnout state is UNKNOWN or INCONSISTENT
								log.error("Cannot choose signal head because turnout "+lt.getTurnout().getSystemName()+
										" is in an UNKNOWN or INCONSISTENT state.");
								return null;
							}
						}
					}
					// track segment connected at A is not in block 2
					if ( (((TrackSegment)lt.getConnectD()).getBlockName().equals(protectedBlock.getUserName())) ) 
						// track segment connected at D is in block 2, return diverging signal head
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB2Name()));
					else {
						// neither track segment is in block 2 - should never get here unless layout turnout is 
						//       only item in block 2
						if (!(lt.getBlockName().equals(protectedBlock.getUserName())))
							log.error("neither signal at B protects block "+protectedBlock.getUserName()+
											", and turnout is not in block either");
						return null;
					}
				}
				else {
					// check if track segments at A or D are in facing block (block 1)
					if ( ((TrackSegment)(lt.getConnectA())).getBlockName().equals(facingBlock.getUserName()) ) {
						// track segment connected at A matches block 1, check D
						if ( !(((TrackSegment)lt.getConnectD()).getBlockName().equals(facingBlock.getUserName())) ) 
							// track segment connected at D is not in block 2, return signal head at continuing end
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
						else {
							// A and D both in block 1, check turnout position to decide which signal head to return
							int state = lt.getTurnout().getKnownState();
							if ( ( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
									( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.THROWN) ) )
								// continuing  
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
							else if ( ( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
									( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.THROWN) ) ) {
								// diverging, check for second head
								if ( (lt.getSignalD2Name()==null) || (lt.getSignalD2Name().equals("")) )
									return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD1Name()));
								else 
									return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD2Name()));							
							}
							else {
								// turnout state is UNKNOWN or INCONSISTENT
								log.error("Cannot choose signal head because turnout "+lt.getTurnout().getSystemName()+
										" is in an UNKNOWN or INCONSISTENT state.");
								return null;
							}
						}
					}
					// track segment connected at A is not in block 1
					if ( ((TrackSegment)lt.getConnectD()).getBlockName().equals(facingBlock.getUserName()) ) {
						// track segment connected at D is in block 1, return diverging signal head, check for second head
						if ( (lt.getSignalD2Name()==null) || (lt.getSignalD2Name().equals("")) )
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD1Name()));
						else 
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD2Name()));
					}
					else {
						// neither track segment is in block 1 - should never get here unless layout turnout is 
						//    the only item in block 1
						if (!(lt.getBlockName().equals(facingBlock.getUserName())))
							log.error("no signal faces block "+facingBlock.getUserName()+
											", and turnout is not in block either");
						return null;
					}
				}
			}
			// not double crossover or LH crossover
			if (  (lt.getLinkType()==LayoutTurnout.NO_LINK) && (lt.getContinuingSense()==Turnout.CLOSED) ) {
				if (facingIsBlock1)
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
				else 
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
			}
			else if (lt.getLinkType()==LayoutTurnout.NO_LINK) {
				if (facingIsBlock1)
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
				else {
					if ( (lt.getSignalA2Name()==null) || (lt.getSignalA2Name().equals("")) )
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
					else 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA2Name()));
				}
			}
			else if (lt.getLinkType()==LayoutTurnout.THROAT_TO_THROAT) {
				if (!facingIsBlock1) {
					//  There are no signals at the throat of a THROAT_TO_THROAT 
					return null;
				}
				// facing block is outside of the THROAT_TO_THROAT
				if ( (lt.getContinuingSense()==Turnout.CLOSED) && ((lt.getSignalB2Name()==null) || 
														(lt.getSignalB2Name().equals(""))) ) 
					// there is only one signal head here - return it
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
				else if ( (lt.getContinuingSense()==Turnout.THROWN) && ((lt.getSignalC2Name()==null) || 
														(lt.getSignalC2Name().equals(""))) ) 
					// there is only one signal head here - return it
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
				// There are two signals here get linked turnout and decide which to return from linked turnout state
				LayoutTurnout tLinked = getLayoutTurnoutFromTurnoutName(lt.getLinkedTurnoutName(),panel);
				int state = tLinked.getTurnout().getKnownState();
				if (state==Turnout.CLOSED) {
					if (lt.getContinuingSense()==Turnout.CLOSED) 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
					else 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));					
				}
				else if (state==Turnout.THROWN) {
					if (lt.getContinuingSense()==Turnout.CLOSED) 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB2Name()));
					else 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC2Name()));					
				}
				else 
					// should never get here - linked turnout state is UNKNOWN or INCONSISTENT
					log.error("Cannot choose signal head to return because turnout "+tLinked.getTurnout().getSystemName()+
										" is in an UNKNOWN or INCONSISTENT state.");
					return null;
			}
			else if (lt.getLinkType()==LayoutTurnout.FIRST_3_WAY) {
				// there is no signal at the FIRST_3_WAY turnout continuing track of a 3-way turnout
				// there should not be a block boundary here				
				return null;
			}
			else if (lt.getLinkType()==LayoutTurnout.SECOND_3_WAY) {
				if (facingIsBlock1) {
					if (lt.getContinuingSense()==Turnout.CLOSED) {				
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
					}
					else {
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
					}
				}
				else {
					// signal is at the linked turnout - the throat of the 3-way turnout
					LayoutTurnout tLinked = getLayoutTurnoutFromTurnoutName(lt.getLinkedTurnoutName(),panel);
					if (lt.getContinuingSense()==Turnout.CLOSED) {				
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(tLinked.getSignalA1Name()));
					}
					else {
						if ( (tLinked.getSignalA3Name()==null) || (lt.getSignalA3Name().equals("")) ) 
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(tLinked.getSignalA1Name()));
						else 
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(tLinked.getSignalA3Name()));
					}					
				}
			}
		}
		if (cType==LayoutEditor.TURNOUT_C) {
			// block boundary is at the diverging track of a turnout or C connection of a crossover turnout
			lt = (LayoutTurnout)connected;
			// check for double crossover or RH crossover
			if ( (lt.getTurnoutType()==LayoutTurnout.DOUBLE_XOVER) || 
						(lt.getTurnoutType()==LayoutTurnout.RH_XOVER) ) {
				if (facingIsBlock1) {
					if ( (lt.getSignalC2Name()==null) || (lt.getSignalC2Name().equals("")) )
						// there is only one head at C, return it
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));						
					// check if track segments at A or D are in protected block (block 2)
					if ( ((TrackSegment)(lt.getConnectA())).getBlockName().equals(protectedBlock.getUserName()) ) {
						// track segment connected at A matches block 2, check D
						if ( !(((TrackSegment)lt.getConnectD()).getBlockName().equals(protectedBlock.getUserName())) ) {
							// track segment connected at D is not in block2, return diverging signal head at C
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC2Name()));
						}
						else {
							// A and D both in block 2, check turnout position to decide which signal head to return
							int state = lt.getTurnout().getKnownState();
							if ( ( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
									( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.THROWN) ) )
								// continuing  
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
							else if ( ( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
									( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.THROWN) ) ) 
								// diverging (crossed over)
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC2Name()));
							else {
								// turnout state is UNKNOWN or INCONSISTENT
								log.error("Cannot choose signal head because turnout "+lt.getTurnout().getSystemName()+
										" is in an UNKNOWN or INCONSISTENT state.");
								return null;
							}
						}
					}
					// track segment connected at A is not in block 2
					if ( (((TrackSegment)lt.getConnectD()).getBlockName().equals(protectedBlock.getUserName())) ) 
						// track segment connected at D is in block 2, return continuing signal head
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
					else {
						// neither track segment is in block 2 - should never get here unless layout turnout is 
						//       only item in block 2
						if (!(lt.getBlockName().equals(protectedBlock.getUserName())))
							log.error("neither signal at C protects block "+protectedBlock.getUserName()+
											", and turnout is not in block either");
						return null;
					}
				}
				else {
					// check if track segments at D or A are in facing block (block 1)
					if ( ((TrackSegment)(lt.getConnectD())).getBlockName().equals(facingBlock.getUserName()) ) {
						// track segment connected at D matches block 1, check A
						if ( !(((TrackSegment)lt.getConnectA()).getBlockName().equals(facingBlock.getUserName())) ) 
							// track segment connected at A is not in block 2, return signal head at continuing end
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD1Name()));
						else {
							// A and D both in block 1, check turnout position to decide which signal head to return
							int state = lt.getTurnout().getKnownState();
							if ( ( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
									( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.THROWN) ) )
								// continuing  
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD1Name()));
							else if ( ( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
									( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.THROWN) ) ) {
								// diverging, check for second head
								if ( (lt.getSignalA2Name()==null) || (lt.getSignalA2Name().equals("")) )
									return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
								else 
									return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA2Name()));							
							}
							else {
								// turnout state is UNKNOWN or INCONSISTENT
								log.error("Cannot choose signal head because turnout "+lt.getTurnout().getSystemName()+
										" is in an UNKNOWN or INCONSISTENT state.");
								return null;
							}
						}
					}
					// track segment connected at D is not in block 1
					if ( ((TrackSegment)lt.getConnectA()).getBlockName().equals(facingBlock.getUserName()) ) {
						// track segment connected at A is in block 1, return diverging signal head, check for second head
						if ( (lt.getSignalA2Name()==null) || (lt.getSignalA2Name().equals("")) )
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
						else 
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA2Name()));
					}
					else {
						// neither track segment is in block 1 - should never get here unless layout turnout is 
						//    the only item in block 1
						if (!(lt.getBlockName().equals(facingBlock.getUserName())))
							log.error("no signal faces block "+facingBlock.getUserName()+
											", and turnout is not in block either");
						return null;
					}
				}
			}
			// not double crossover or RH crossover
			if ( (lt.getLinkType()==LayoutTurnout.NO_LINK) && (lt.getContinuingSense()==Turnout.CLOSED) ) {
				if (facingIsBlock1)
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
				else if (lt.getTurnoutType()==LayoutTurnout.LH_XOVER) 
					// LH turnout - this is continuing track for D connection
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD1Name()));
				else {
					// RH, LH or WYE turnout, this is diverging track for A connection
					if ( (lt.getSignalA2Name()==null) || (lt.getSignalA2Name().equals("")) )
						// there is no signal head at the throat for diverging 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
					else 
						// there is a diverging head at the throat, return it
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA2Name()));
				}
			}
			else if (lt.getLinkType()==LayoutTurnout.NO_LINK) {
				if (facingIsBlock1)
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
				else 
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
			}
			else if (lt.getLinkType()==LayoutTurnout.THROAT_TO_THROAT) {
				if (!facingIsBlock1) {
					//  There are no signals at the throat of a THROAT_TO_THROAT 
					return null;
				}
				// facing block is outside of the THROAT_TO_THROAT
				if ( (lt.getContinuingSense()==Turnout.CLOSED) && ((lt.getSignalC2Name()==null) || 
														(lt.getSignalC2Name().equals(""))) ) 
					// there is only one signal head here - return it
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
				else if ( (lt.getContinuingSense()==Turnout.THROWN) && ((lt.getSignalB2Name()==null) || 
														(lt.getSignalB2Name().equals(""))) ) 
					// there is only one signal head here - return it
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
				// There are two signals here get linked turnout and decide which to return from linked turnout state
				LayoutTurnout tLinked = getLayoutTurnoutFromTurnoutName(lt.getLinkedTurnoutName(),panel);
				int state = tLinked.getTurnout().getKnownState();
				if (state==Turnout.CLOSED) {
					if (lt.getContinuingSense()==Turnout.CLOSED) 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
					else 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));					
				}
				else if (state==Turnout.THROWN) {
					if (lt.getContinuingSense()==Turnout.CLOSED) 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC2Name()));
					else 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB2Name()));					
				}
				else {
					// should never get here - linked turnout state is UNKNOWN or INCONSISTENT
					log.error("Cannot choose signal head to return because turnout "+tLinked.getTurnout().getSystemName()+
										" is in an UNKNOWN or INCONSISTENT state.");
					return null;
				}
			}
			else if (lt.getLinkType()==LayoutTurnout.FIRST_3_WAY) {
				if (facingIsBlock1) {
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
				}
				else {
					if ( (lt.getSignalA2Name()==null) || (lt.getSignalA2Name().equals("")) ) 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
					else 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA2Name()));
				}
			}
			else if (lt.getLinkType()==LayoutTurnout.SECOND_3_WAY) {
				if  (facingIsBlock1) {
					if (lt.getContinuingSense()==Turnout.CLOSED) {				
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
					}
					else {
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
					}
				}
				else {
					// signal is at the linked turnout - the throat of the 3-way turnout
					LayoutTurnout tLinked = getLayoutTurnoutFromTurnoutName(lt.getLinkedTurnoutName(),panel);
					if (lt.getContinuingSense()==Turnout.CLOSED) {				
						if ( (tLinked.getSignalA3Name()==null) || (tLinked.getSignalA3Name().equals("")) ) 
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(tLinked.getSignalA1Name()));
						else 
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(tLinked.getSignalA3Name()));
					}
					else {
						if ( (tLinked.getSignalA2Name()==null) || (tLinked.getSignalA2Name().equals("")) ) 
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(tLinked.getSignalA1Name()));
						else 
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(tLinked.getSignalA2Name()));
					}					
				}
			}
		}
		if (cType==LayoutEditor.TURNOUT_D) {
			// block boundary is at D connectin of a crossover turnout
			lt = (LayoutTurnout)connected;
			if (lt.getTurnoutType()==LayoutTurnout.RH_XOVER) {
				// no diverging route possible, this is continuing track for C connection
				if (facingIsBlock1)
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD1Name()));
				else
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
			}
			if (facingIsBlock1) {
				if ( (lt.getSignalD2Name()==null) || (lt.getSignalD2Name().equals("")) )
					//there is no signal head for diverging 
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD1Name()));
				else {
					// check if track segments at C or B are in protected block (block 2)
					if ( ((TrackSegment)(lt.getConnectC())).getBlockName().equals(protectedBlock.getUserName()) ) {
						// track segment connected at C matches block 2, check B
						if ( !(((TrackSegment)lt.getConnectB()).getBlockName().equals(protectedBlock.getUserName())) ) {
							// track segment connected at B is not in block2, return continuing signal head at D
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD1Name()));
						}
						else {
							// C and B both in block2, check turnout position to decide which signal head to return
							int state = lt.getTurnout().getKnownState();
							if ( ( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
									( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.THROWN) ) )
								// continuing  
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD1Name()));
							else if ( ( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
									( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.THROWN) ) ) 
								// diverging
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD2Name()));
							else {
								// turnout state is UNKNOWN or INCONSISTENT
								log.error("Cannot choose signal head because turnout "+lt.getTurnout().getSystemName()+
										" is in an UNKNOWN or INCONSISTENT state.");
								return null;
							}
						}
					}
					// track segment connected at C is not in block 2
					if ( (((TrackSegment)lt.getConnectB()).getBlockName().equals(protectedBlock.getUserName())) ) 
						// track segment connected at B is in block 2, return diverging signal head
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD2Name()));
					else {
						// neither track segment is in block 2 - should never get here unless layout turnout is 
						//      the only item in block 2
						if (!(lt.getBlockName().equals(protectedBlock.getUserName())))
							log.error("neither signal at D protects block "+protectedBlock.getUserName()+
											", and turnout is not in block either");
						return null;
					}
				}
			}
			else {
				// check if track segments at C or B are in facing block (block 1)
				if ( ((TrackSegment)(lt.getConnectC())).getBlockName().equals(facingBlock.getUserName()) ) {
					// track segment connected at C matches block 1, check B
					if ( !(((TrackSegment)lt.getConnectB()).getBlockName().equals(facingBlock.getUserName())) ) 
						// track segment connected at B is not in block 2, return signal head at continuing end
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
					else {
						// C and B both in block 1, check turnout position to decide which signal head to return
						int state = lt.getTurnout().getKnownState();
						if ( ( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
									( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.THROWN) ) )
							// continuing  
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
						else if ( ( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
									( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.THROWN) ) ) {
							// diverging, check for second head
							if ( (lt.getSignalB2Name()==null) || (lt.getSignalB2Name().equals("")) )
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
							else 
								return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB2Name()));							
						}
						else {
							// turnout state is UNKNOWN or INCONSISTENT
							log.error("Cannot choose signal head because turnout "+lt.getTurnout().getSystemName()+
										" is in an UNKNOWN or INCONSISTENT state.");
							return null;
						}
					}
				}
				// track segment connected at C is not in block 1
				if ( ((TrackSegment)lt.getConnectB()).getBlockName().equals(facingBlock.getUserName()) ) {
					// track segment connected at B is in block 1, return diverging signal head, check for second head
					if ( (lt.getSignalB2Name()==null) || (lt.getSignalB2Name().equals("")) )
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
					else 
						return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB2Name()));
				}
				else {
					// neither track segment is in block 1 - should never get here unless layout turnout is 
					//    the only item in block 1
					if (!(lt.getBlockName().equals(facingBlock.getUserName())))
						log.error("no signal faces block "+facingBlock.getUserName()+
											", and turnout is not in block either");
					return null;
				}
			}
		}
		// block boundary must be at a level crossing
		if ( (cType<LayoutEditor.LEVEL_XING_A) || (cType>LayoutEditor.LEVEL_XING_D) ) {
			log.error("Block Boundary not identified correctly - Blocks "+facingBlock.getSystemName()+
										", "+protectedBlock.getSystemName());
			return null;
		}
		LevelXing xing = (LevelXing)connected;
		if (cType==LayoutEditor.LEVEL_XING_A) {
			// block boundary is at the A connection of a level crossing
			if (facingIsBlock1) 
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(xing.getSignalAName()));
			else 
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(xing.getSignalCName()));
		}
		if (cType==LayoutEditor.LEVEL_XING_B) {
			// block boundary is at the B connection of a level crossing
			if (facingIsBlock1) 
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(xing.getSignalBName()));
			else 
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(xing.getSignalDName()));
		}
		if (cType==LayoutEditor.LEVEL_XING_C) {
			// block boundary is at the C connection of a level crossing
			if (facingIsBlock1) 
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(xing.getSignalCName()));
			else 
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(xing.getSignalAName()));
		}
		if (cType==LayoutEditor.LEVEL_XING_D) {
			// block boundary is at the D connection of a level crossing
			if (facingIsBlock1) 
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(xing.getSignalDName()));
			else 
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(xing.getSignalBName()));
		}
		return null;		
	}
    /**
     * Method to return the LayoutBlock that a given signal is protecting.
     */
/*    public LayoutBlock getProtectedBlock(String signalName, LayoutEditor panel){
        PositionablePoint pp = panel.findPositionablePointByEastBoundSignal(signalName);
        TrackSegment tr;
        if (pp==null) {
            pp = panel.findPositionablePointByWestBoundSignal(signalName);
            if (pp==null)
                return null;
            tr = pp.getConnect1();
        } else {
            tr = pp.getConnect2();
        }
        //tr = pp.getConnect2();
        if (tr==null)
            return null;
        return tr.getLayoutBlock();
    }*/
    
    /**
     * Method to return the LayoutBlock that a given signal is facing.
     */
/*    public LayoutBlock getFacingBlock(String signalName, LayoutEditor panel){
        PositionablePoint pp = panel.findPositionablePointByWestBoundSignal(signalName);
        TrackSegment tr;
        if (pp==null) {
            pp = panel.findPositionablePointByWestBoundSignal(signalName);
            if (pp==null)
                return null;
            tr = pp.getConnect1();
        } else {
            tr = pp.getConnect2();
        }
        if (tr==null)
            return null;
        return tr.getLayoutBlock();
    }*/
    
	private LayoutTurnout getLayoutTurnoutFromTurnoutName(String turnoutName, LayoutEditor panel) {
		Turnout t = InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
		if (t==null) return null;
		LayoutTurnout lt = null;
		for (int i=0; i<panel.turnoutList.size(); i++) {
			lt = panel.turnoutList.get(i);
			if (lt.getTurnout()==t) return lt;
		}
		return null;
	}

	/**
	 * Method to return the Signal Mast facing into a specified Block from a specified protected Block.
	 * <P>
     * @param facingBlock
     * @param protectedBlock
     * @return The assigned signalMast.
     */

    public SignalMast getFacingSignalMast (Block facingBlock, Block protectedBlock){
        // check input
		if ( (facingBlock == null) || (protectedBlock == null) ) {
			log.error ("null block in call to getFacingSignalMast");
			return null;
		}
        // non-null - check if input corresponds to Blocks in a Layout Editor panel.
		LayoutBlock fLayoutBlock = getByUserName(facingBlock.getUserName());
		LayoutBlock pLayoutBlock = getByUserName(protectedBlock.getUserName());
		if ( (fLayoutBlock==null) || (pLayoutBlock==null) ) {
			if (fLayoutBlock==null) log.error("Block "+facingBlock.getSystemName()+"is not on a Layout Editor panel.");
			if (pLayoutBlock==null) log.error("Block "+protectedBlock.getSystemName()+"is not on a Layout Editor panel.");
			return null;
		}
		// input has corresponding LayoutBlocks - does it correspond to a block boundary?
		LayoutEditor panel = fLayoutBlock.getMaxConnectedPanel();
		ArrayList<LayoutConnectivity> c = panel.auxTools.getConnectivityList(fLayoutBlock);
		LayoutConnectivity lc = null;
		int i = 0;
		boolean facingIsBlock1 = true;
		while ((i<c.size()) && (lc==null)) {
			LayoutConnectivity tlc = c.get(i);
			if ( (tlc.getBlock1()==fLayoutBlock) && (tlc.getBlock2()==pLayoutBlock) ) {
				lc = tlc;
			}
			else if ( (tlc.getBlock1()==pLayoutBlock) && (tlc.getBlock2()==fLayoutBlock) ) {
				lc = tlc;
				facingIsBlock1 = false;
			}
			i ++;
		}
		if (lc==null) {
			log.error("Block "+facingBlock.getSystemName()+" is not connected to Block "+protectedBlock.getSystemName());
			return null;
		}
		LayoutTurnout lt = null;
        Object connected = lc.getConnectedObject();
		TrackSegment tr = lc.getTrackSegment();
        int cType = lc.getConnectedType();
		if (cType==LayoutEditor.TRACK) {
			// block boundary is at an Anchor Point
			LayoutEditorTools tools = new LayoutEditorTools(panel);
			PositionablePoint p = panel.findPositionablePointAtTrackSegments(tr,(TrackSegment)connected);
			boolean block1IsWestEnd = tools.isAtWestEndOfAnchor(tr,p);
			if ( (block1IsWestEnd && facingIsBlock1) || (!block1IsWestEnd && !facingIsBlock1) ) {
				// block1 is on the west (north) end of the block boundary
				return (InstanceManager.signalMastManagerInstance().getSignalMast(p.getEastBoundSignalMast()));
			}
			else {
				return (InstanceManager.signalMastManagerInstance().getSignalMast(p.getWestBoundSignalMast()));
			}
		}
        if (cType==LayoutEditor.TURNOUT_A) {
            lt = (LayoutTurnout)connected;
            if ((lt.getLinkType()==LayoutTurnout.NO_LINK) || (lt.getLinkType()==LayoutTurnout.FIRST_3_WAY)){
                if (facingIsBlock1) {
                    if ( (lt.getSignalAMast()!=null) || (!lt.getSignalAMast().equals("")) ){
						return (InstanceManager.signalMastManagerInstance().getSignalMast(lt.getSignalAMast()));
                    }
                }
                // we only allow signal masts inbound to the turnout.
                return null;
            }
            else {
                return null;
            }
        }
        
        if (cType==LayoutEditor.TURNOUT_B) {
            lt = (LayoutTurnout)connected;
            if (facingIsBlock1) {
                if ( (lt.getSignalBMast()!=null) || (!lt.getSignalBMast().equals("")) ){
                    return (InstanceManager.signalMastManagerInstance().getSignalMast(lt.getSignalBMast()));
                }
            }
            return null;
        }
        if (cType==LayoutEditor.TURNOUT_C) {
            lt = (LayoutTurnout)connected;
            if (facingIsBlock1) {
                if ( (lt.getSignalCMast()!=null) || (!lt.getSignalCMast().equals("")) ){
                    return (InstanceManager.signalMastManagerInstance().getSignalMast(lt.getSignalCMast()));
                }
            }
            return null;
        }
        
        if (cType==LayoutEditor.TURNOUT_D) {
            lt = (LayoutTurnout)connected;
            if (facingIsBlock1) {
                if ( (lt.getSignalDMast()!=null) || (!lt.getSignalDMast().equals("")) ){
                    return (InstanceManager.signalMastManagerInstance().getSignalMast(lt.getSignalDMast()));
                }
            }
            return null;
        }
        if ( (cType<LayoutEditor.LEVEL_XING_A) || (cType>LayoutEditor.LEVEL_XING_D) ) {
			log.error("Block Boundary not identified correctly - Blocks "+facingBlock.getSystemName()+
										", "+protectedBlock.getSystemName());
			return null;
		}
		LevelXing xing = (LevelXing)connected;
		if (cType==LayoutEditor.LEVEL_XING_A) {
			// block boundary is at the A connection of a level crossing
			if (facingIsBlock1) 
				return (InstanceManager.signalMastManagerInstance().getSignalMast(xing.getSignalAMastName()));
			else 
				return (InstanceManager.signalMastManagerInstance().getSignalMast(xing.getSignalCMastName()));
		}
		if (cType==LayoutEditor.LEVEL_XING_B) {
			// block boundary is at the B connection of a level crossing
			if (facingIsBlock1) 
				return (InstanceManager.signalMastManagerInstance().getSignalMast(xing.getSignalBMastName()));
			else 
				return (InstanceManager.signalMastManagerInstance().getSignalMast(xing.getSignalDMastName()));
		}
		if (cType==LayoutEditor.LEVEL_XING_C) {
			// block boundary is at the C connection of a level crossing
			if (facingIsBlock1) 
				return (InstanceManager.signalMastManagerInstance().getSignalMast(xing.getSignalCMastName()));
			else 
				return (InstanceManager.signalMastManagerInstance().getSignalMast(xing.getSignalAMastName()));
		}
		if (cType==LayoutEditor.LEVEL_XING_D) {
			// block boundary is at the D connection of a level crossing
			if (facingIsBlock1) 
				return (InstanceManager.signalMastManagerInstance().getSignalMast(xing.getSignalDMastName()));
			else 
				return (InstanceManager.signalMastManagerInstance().getSignalMast(xing.getSignalBMastName()));
		}
        return null;
    }

    /**
     * Returns in the first instance a Signal Mast or if none exists a Signal Head
     * for a given facing block and protected block combination.
     * see getFacingSignalMast and getFacingSignalHead as to how they deal with
     * what they each return.
     * <p>
     * @param facingBlock
     * @param protectedBlock
     * @return either a signalMast or signalHead
     */
    public Object getFacingSignalObject(Block facingBlock, Block protectedBlock){
        Object sig = getFacingSignalMast(facingBlock, protectedBlock);
        if (sig!=null)
            return sig;
        sig = getFacingSignalHead(facingBlock, protectedBlock);
        return sig;
    }
    
    public LayoutBlock getProtectedBlockByMast(SignalMast signalMast, LayoutEditor panel){
        LayoutBlock protect = getProtectedBlockByMast(signalMast.getUserName(), panel);
        if(protect == null)
            protect = getProtectedBlockByMast(signalMast.getSystemName(), panel);
        return protect;
    }
    /**
     * Method to return the LayoutBlock that a given signal is protecting.
     */
    public LayoutBlock getProtectedBlockByMast(String signalMastName, LayoutEditor panel){
        PositionablePoint pp = panel.findPositionablePointByEastBoundSignalMast(signalMastName);
        TrackSegment tr = null;
        boolean east = true;
        //Don't think that the logic for this is the right way round
        if (pp==null) {
            pp = panel.findPositionablePointByWestBoundSignalMast(signalMastName);  // was east
            east = false;
        }
        if(pp!=null){
            LayoutEditorTools tools = new LayoutEditorTools(panel);
            if(east){
                if(tools.isAtWestEndOfAnchor(pp.getConnect1(), pp)){
                    tr=pp.getConnect2();
                }
                else {
                    tr=pp.getConnect1();
                }
            } else {
                if(tools.isAtWestEndOfAnchor(pp.getConnect1(), pp)){
                    tr=pp.getConnect1();
                }
                else {
                    tr=pp.getConnect2();
                }
            }
            
            if (tr!=null){
                return tr.getLayoutBlock();
            }
        }
        
        LayoutTurnout t = panel.findLayoutTurnoutBySignalMast(signalMastName);
        if(t!=null){
            if(t.getSignalAMast().equals(signalMastName)){
                return t.getLayoutBlock();
            } else if (t.getSignalBMast().equals(signalMastName)) {
                return t.getLayoutBlockB();
            } else if (t.getSignalCMast().equals(signalMastName)) {
                return t.getLayoutBlockC();
            } else {
                return t.getLayoutBlockD();
            }
        }
        
        LevelXing l = panel.findLevelXingBySignalMast(signalMastName);
        if(l!=null){
            if(l.getSignalAMastName().equals(signalMastName)){
                return l.getLayoutBlockAC();
            } else if (l.getSignalBMastName().equals(signalMastName)) {
                return l.getLayoutBlockBD();
            } else if (l.getSignalCMastName().equals(signalMastName)) {
                return l.getLayoutBlockAC();
            } else {
                return l.getLayoutBlockBD();
            }
            
        }
        return null;
    }

    public LayoutBlock getFacingBlockByMast(SignalMast signalMast, LayoutEditor panel){
        LayoutBlock facing = getFacingBlockByMast(signalMast.getUserName(), panel);
        if(facing == null)
            facing = getFacingBlockByMast(signalMast.getSystemName(), panel);
        return facing;
    }
    
    /**
     * Method to return the LayoutBlock that a given signal is facing.
     */
    public LayoutBlock getFacingBlockByMast(String signalMastName, LayoutEditor panel){
        PositionablePoint pp = panel.findPositionablePointByEastBoundSignalMast(signalMastName); //was west
        TrackSegment tr = null;
        boolean east = true;
        //Don't think that the logic for this is the right way round
        if (pp==null) {
            pp = panel.findPositionablePointByWestBoundSignalMast(signalMastName);  // was east
            east = false;
        }
        if(pp!=null){
            LayoutEditorTools tools = new LayoutEditorTools(panel);
            if(east){
                if(tools.isAtWestEndOfAnchor(pp.getConnect1(), pp)){
                    tr=pp.getConnect1();
                }
                else {
                    tr=pp.getConnect2();
                }
            } else {
                if(tools.isAtWestEndOfAnchor(pp.getConnect1(), pp)){
                    tr=pp.getConnect2();
                }
                else {
                    tr=pp.getConnect1();
                }
            }
            
            if (tr!=null){
                log.debug("found facing block by positionable point");
                return tr.getLayoutBlock();
            }
        }
        LayoutTurnout t = panel.findLayoutTurnoutBySignalMast(signalMastName);
        if(t!=null){
            log.debug("found signalmast at turnout " + t.getTurnout().getDisplayName());
            Object connect;
            if(t.getSignalAMast().equals(signalMastName)){
                connect = t.getConnectA();
            } else if (t.getSignalBMast().equals(signalMastName)) {
                connect = t.getConnectB();
            } else if (t.getSignalCMast().equals(signalMastName)) {
                connect = t.getConnectC();
            } else {
                connect = t.getConnectD();
            }
            if (connect instanceof TrackSegment){
                tr = (TrackSegment) connect;
                log.debug("return block " + tr.getLayoutBlock().getDisplayName());
                return tr.getLayoutBlock();
            
            }
        }
        
        LevelXing l = panel.findLevelXingBySignalMast(signalMastName);
        if(l!=null){
            Object connect;
            if(l.getSignalAMastName().equals(signalMastName)){
                connect = l.getConnectA();
            } else if (l.getSignalBMastName().equals(signalMastName)) {
                connect = l.getConnectB();
            } else if (l.getSignalCMastName().equals(signalMastName)) {
                connect = l.getConnectC();
            } else {
                connect = l.getConnectD();
            }
            
            if (connect instanceof TrackSegment){
                tr = (TrackSegment) connect;
                log.debug("return block " + tr.getLayoutBlock().getDisplayName());
                return tr.getLayoutBlock();
            
            }
            
        }
        return null;
    }

    /* This needs to be expanded to cover turnouts and level crossings. */
    public LayoutBlock getProtectedBlockBySensor(String sensorName, LayoutEditor panel){
        PositionablePoint pp = panel.findPositionablePointByEastBoundSensor(sensorName);
        TrackSegment tr;
        if (pp==null) {
            pp = panel.findPositionablePointByWestBoundSensor(sensorName);
            if (pp==null)
                return null;
            tr = pp.getConnect1();
        } else {
            tr = pp.getConnect2();
        }
        //tr = pp.getConnect2();
        if (tr==null)
            return null;
        return tr.getLayoutBlock();
    }

    /**
     * Method to return the LayoutBlock that a given signal is facing.
     */
    /* This needs to be expanded to cover turnouts and level crossings. */
    public LayoutBlock getFacingBlockBySensor(String sensorName, LayoutEditor panel){
        PositionablePoint pp = panel.findPositionablePointByWestBoundSensor(sensorName);
        TrackSegment tr;
        if (pp==null) {
            pp = panel.findPositionablePointByWestBoundSensor(sensorName);
            if (pp==null)
                return null;
            tr = pp.getConnect1();
        } else {
            tr = pp.getConnect2();
        }
        if (tr==null)
            return null;
        return tr.getLayoutBlock();
    }

    /**
     * Method to return the LayoutBlock that a given signal is protecting.
     */
     /* This needs to be expanded to cover turnouts and level crossings. */
    public LayoutBlock getProtectedBlock(String signalName, LayoutEditor panel){
        PositionablePoint pp = panel.findPositionablePointByEastBoundSignal(signalName);
        TrackSegment tr;
        if (pp==null) {
            pp = panel.findPositionablePointByWestBoundSignal(signalName);
            if (pp==null)
                return null;
            tr = pp.getConnect1();
        } else {
            tr = pp.getConnect2();
        }
        //tr = pp.getConnect2();
        if (tr==null)
            return null;
        return tr.getLayoutBlock();
    }
    
    /**
     * Method to return the LayoutBlock that a given signal is facing.
     */
     /* This needs to be expanded to cover turnouts and level crossings. */
    public LayoutBlock getFacingBlock(String signalName, LayoutEditor panel){
        PositionablePoint pp = panel.findPositionablePointByWestBoundSignal(signalName);
        TrackSegment tr;
        if (pp==null) {
            pp = panel.findPositionablePointByWestBoundSignal(signalName);
            if (pp==null)
                return null;
            tr = pp.getConnect1();
        } else {
            tr = pp.getConnect2();
        }
        if (tr==null)
            return null;
        return tr.getLayoutBlock();
    }

    public final static int MASTTOMAST = 0x01;
    public final static int HEADTOHEAD = 0x02;
    public final static int ANY = 0x04;
    public final static int NONE = 0x00;
    
    private static int ttlSize = 50;

    /*
    Test method to try and workout a better way of handling going back through 
    the paths if we reach a dead end as such.
    */
    /*public ArrayList<LayoutBlock> getAltLayoutBlocks(LayoutBlock sourceLayoutBlock, LayoutBlock destinationLayoutBlock, LayoutBlock protectingLayoutBlock, boolean validateOnly, int pathMethod) throws jmri.JmriException{
        lastErrorMessage= "";
        if (!isAdvancedRoutingEnabled()){
            log.info("Advanced routing has not been enabled therefore we cannot use this function");
            throw new jmri.JmriException("Advanced routing has not been enabled therefore we cannot use this function");
            //return null;
        }
        
        int directionOfTravel = sourceLayoutBlock.getNeighbourDirection(protectingLayoutBlock);
        Block currentBlock = sourceLayoutBlock.getBlock();

        Block destBlock = destinationLayoutBlock.getBlock();
        log.debug("Destination Block " + destinationLayoutBlock.getDisplayName() + " " + destBlock);
        Block nextBlock = protectingLayoutBlock.getBlock();
        log.debug("s:" + sourceLayoutBlock.getDisplayName() + " p:" + protectingLayoutBlock.getDisplayName() + " d:" + destinationLayoutBlock.getDisplayName());

        ArrayList<Integer> blockIndex = new ArrayList<Integer>();
        ArrayList<BlocksTested> blocksInRoute = new ArrayList<BlocksTested>();

        blocksInRoute.add(new BlocksTested(sourceLayoutBlock));
        
        if(!validateOnly){
            if (canLBlockBeUsed(protectingLayoutBlock)){
                blocksInRoute.add(new BlocksTested(protectingLayoutBlock));

            } else {
                lastErrorMessage = "Block we are protecting is already occupied or reserved";
                log.debug(lastErrorMessage);
                JOptionPane.showMessageDialog(null, lastErrorMessage);
                throw new jmri.JmriException(lastErrorMessage);
            }
        } else {
            blocksInRoute.add(new BlocksTested(protectingLayoutBlock));
        }
        if (destinationLayoutBlock==protectingLayoutBlock){
            ArrayList<LayoutBlock> returnBlocks = new ArrayList<LayoutBlock>();
            for (int i =0; i<blocksInRoute.size(); i++){
                returnBlocks.add(blocksInRoute.get(i).getBlock());
            }
            
            return returnBlocks;
        }
        LayoutBlock currentLBlock = protectingLayoutBlock;

        BlocksTested bt = blocksInRoute.get(blocksInRoute.size()-1);

        int ttl=1;
        int offSet=-1;
        while (ttl <ttlSize){ //value should be higher but low for test!
            log.debug("===== Ttl value = " + ttl + " ======");
            log.debug("Looking for next block");
            int nextBlockIndex = 0;
            if(currentBlock==sourceLayoutBlock.getBlock() && nextBlock==protectingLayoutBlock.getBlock()){
                //for the first block, the current block and protecting block are likely to contain a signal head or mast, therefore we will
                //ignore checking for this.
                nextBlockIndex = findBestHop(currentBlock, nextBlock, destBlock, directionOfTravel, offSet, validateOnly, NONE);
            }
            else {
                nextBlockIndex = findBestHop(currentBlock, nextBlock, destBlock, directionOfTravel, offSet, validateOnly, pathMethod);
            }
            if (nextBlockIndex!=-1){
                bt.addIndex(nextBlockIndex);
                log.debug("block index returned " + nextBlockIndex + " Blocks in route size " + blocksInRoute.size());
                //Sets the old next block to be our current block.
                currentLBlock = InstanceManager.layoutBlockManagerInstance().getLayoutBlock(nextBlock);

                offSet = -1;

                directionOfTravel = currentLBlock.getDirectionAtIndex(nextBlockIndex);

                currentBlock = nextBlock;
                nextBlock = currentLBlock.getNextBlockAtIndex(nextBlockIndex);
                LayoutBlock nextLBlock = InstanceManager.layoutBlockManagerInstance().getLayoutBlock(nextBlock);

                log.debug("Blocks in route size " + blocksInRoute.size());
                log.debug(nextBlock + " " + destBlock);
                if (nextBlock==currentBlock){
                    nextBlock = currentLBlock.getDestBlockAtIndex(nextBlockIndex);
                    log.debug("the next block to our destination we are looking for is directly connected to this one");
                } else if(protectingLayoutBlock!=nextLBlock){
                    log.debug("Add block " + nextLBlock.getDisplayName());

                    bt = new BlocksTested(nextLBlock);
                }
                if (nextBlock==destBlock){
                    log.debug("Adding destination Block " + destinationLayoutBlock.getDisplayName());
                    log.debug("arrived at destination block");
                    
                    ArrayList<LayoutBlock> returnBlocks = new ArrayList<LayoutBlock>();
                    for (int i =0; i<blocksInRoute.size(); i++){
                        returnBlocks.add(blocksInRoute.get(i).getBlock());
                    }
                    returnBlocks.add(destinationLayoutBlock);
                    return returnBlocks;
                }
            } 
            else {
            //-1 is returned when there are no more valid besthop valids found
                //Block index is -1, so we need to go back a block and find another way.

                //So we have gone back as far as our starting block so we better return.
                int birSize = blocksInRoute.size();
                log.debug("block in route size " + birSize);
                if (birSize<=2) {
                    log.debug("drop out here with ttl");
                    ttl=ttlSize+1;
                }
                else {
                    for (int t = 0; t<blocksInRoute.size(); t++){
                        log.debug("index " + t + " block " + blocksInRoute.get(t).getBlock().getDisplayName());
                    }
                    log.debug("To remove last block " + blocksInRoute.get(birSize-1).getBlock().getDisplayName() + " at index " + blockIndex.get(birSize-1));
                    log.debug("Will set offSet at " + (blockIndex.get(birSize-1)));

                    currentBlock = blocksInRoute.get(birSize-3).getBlock().getBlock();
                    nextBlock = blocksInRoute.get(birSize-2).getBlock().getBlock();
                    offSet = blocksInRoute.get(birSize-2).getLastIndex();
                    blocksInRoute.remove(birSize-1);
                    ttl--;
                }
            }
            ttl++;
        }
        if(ttl==ttlSize){
            lastErrorMessage = "ttlExpired";
        }
        //we exited the loop without either finding the destination or we had error.
        throw new jmri.JmriException(lastErrorMessage);
    }
    
    class BlocksTested {
        
        LayoutBlock block;
        ArrayList<Integer> indexNumber = new ArrayList<Integer>();
        
        BlocksTested(LayoutBlock block){
            this.block=block;
        }
        
        void addIndex (int x){
            indexNumber.add(x);
        }
        
        int getLastIndex(){
            return indexNumber.get(indexNumber.size()-1);
        }
        
        LayoutBlock getBlock(){
            return block;
        }
    }*/

    
    public ArrayList<LayoutBlock> getLayoutBlocks(LayoutBlock sourceLayoutBlock, LayoutBlock destinationLayoutBlock, LayoutBlock protectingLayoutBlock, boolean validateOnly, int pathMethod) throws jmri.JmriException{
        lastErrorMessage= "";
        if (!isAdvancedRoutingEnabled()){
            log.info("Advanced routing has not been enabled therefore we cannot use this function");
            throw new jmri.JmriException("Advanced routing has not been enabled therefore we cannot use this function");
            //return null;
        }
        
        int directionOfTravel = sourceLayoutBlock.getNeighbourDirection(protectingLayoutBlock);
        Block currentBlock = sourceLayoutBlock.getBlock();
        //Block previousBlock = currentBlock;
        Block destBlock = destinationLayoutBlock.getBlock();
        log.debug("Destination Block " + destinationLayoutBlock.getDisplayName() /*+ destBlock.getDisplayName()*/ + " " + destBlock);
        Block nextBlock = protectingLayoutBlock.getBlock();
        if(log.isDebugEnabled()){
            log.debug("s:" + sourceLayoutBlock.getDisplayName() + " p:" + protectingLayoutBlock.getDisplayName() + " d:" + destinationLayoutBlock.getDisplayName());
        }

        ArrayList<Integer> blockIndex = new ArrayList<Integer>();
        ArrayList<LayoutBlock> blocksInRoute = new ArrayList<LayoutBlock>();
        blocksInRoute.add(sourceLayoutBlock);
        blockIndex.add(0);
        
        if(!validateOnly){
            if (canLBlockBeUsed(protectingLayoutBlock)){
                blocksInRoute.add(protectingLayoutBlock);
                //Need a way to get the block index here.
                blockIndex.add(0);
            } else {
                lastErrorMessage = "Block we are protecting is already occupied or reserved";
                log.debug(lastErrorMessage);
                JOptionPane.showMessageDialog(null, lastErrorMessage);
                throw new jmri.JmriException(lastErrorMessage);
            }
        } else {
            blocksInRoute.add(protectingLayoutBlock);
            blockIndex.add(0);
        }
        if (destinationLayoutBlock==protectingLayoutBlock){
            return blocksInRoute;
        }
        LayoutBlock currentLBlock = protectingLayoutBlock;

        int ttl=1;
        int offSet=-1;
        while (ttl <ttlSize){ //value should be higher but low for test!
            log.debug("===== Ttl value = " + ttl + " ======");
            log.debug("Looking for next block");
            int nextBlockIndex = 0;
            if(currentBlock==sourceLayoutBlock.getBlock() && nextBlock==protectingLayoutBlock.getBlock()){
                //for the first block, the current block and protecting block are likely to contain a signal head or mast, therefore we will
                //ignore checking for this.
                nextBlockIndex = findBestHop(currentBlock, nextBlock, destBlock, directionOfTravel, offSet, validateOnly, NONE);
            }
            else {
                nextBlockIndex = findBestHop(currentBlock, nextBlock, destBlock, directionOfTravel, offSet, validateOnly, pathMethod);
            }
            if (nextBlockIndex!=-1){
                if(log.isDebugEnabled()){
                    log.debug("block index returned " + nextBlockIndex + " Blocks in route size " + blocksInRoute.size());
                }
                //Sets the old next block to be our current block.
                currentLBlock = InstanceManager.layoutBlockManagerInstance().getLayoutBlock(nextBlock);

                offSet = -1;
                //int hopcountToNextBlock = currentLBlock.getBlockCountAtIndex(nextBlockIndex);
                directionOfTravel =currentLBlock.getDirectionAtIndex(nextBlockIndex);
//                previousBlock=currentBlock;
                currentBlock = nextBlock;
                nextBlock = currentLBlock.getNextBlockAtIndex(nextBlockIndex);
                LayoutBlock nextLBlock = InstanceManager.layoutBlockManagerInstance().getLayoutBlock(nextBlock);

                if(log.isDebugEnabled()){
                    log.debug("Blocks in route size " + blocksInRoute.size());
                    log.debug(nextBlock + " " + destBlock);
                }
                if (nextBlock==currentBlock){
                    nextBlock = currentLBlock.getDestBlockAtIndex(nextBlockIndex);
                    log.debug("the next block to our destination we are looking for is directly connected to this one");
                } else if(protectingLayoutBlock!=nextLBlock){
                    if(log.isDebugEnabled()){
                        log.debug("Add block " + nextLBlock.getDisplayName());
                    }
                    blockIndex.add(nextBlockIndex);
                    blocksInRoute.add(nextLBlock);
                }
                if (nextBlock==destBlock){
                    if(log.isDebugEnabled()){
                        log.debug("Adding destination Block " + destinationLayoutBlock.getDisplayName());
                    }
                    blocksInRoute.add(destinationLayoutBlock);
                    log.debug("arrived at destination block");
                    return blocksInRoute;
                }
            } 
            else {
                //Block index is -1, so we need to go back a block and find another way.

                //So we have gone back as far as our starting block so we better return.
                int birSize = blocksInRoute.size();
                log.debug("block in route size " + birSize);
                if (birSize<=2) {
                    log.debug("drop out here with ttl");
                    ttl=ttlSize+1;
                }
                else {
                    if(log.isDebugEnabled()){
                        for (int t = 0; t<blocksInRoute.size(); t++){
                            log.debug("index " + t + " block " + blocksInRoute.get(t).getDisplayName() + " " +blockIndex.get(t));
                        }
                        log.debug("To remove last block " + blocksInRoute.get(birSize-1).getDisplayName() + " at index " + blockIndex.get(birSize-1));
                        log.debug("Will set offSet at " + (blockIndex.get(birSize-1)));
                    }
                    offSet = blockIndex.get(birSize-1);
                    currentBlock = blocksInRoute.get(birSize-3).getBlock();
                    nextBlock = blocksInRoute.get(birSize-2).getBlock();
                    blocksInRoute.remove(birSize-1);
                    blockIndex.remove(birSize-1);
                    ttl--;
                }
            }
            ttl++;
        }
        if(ttl==ttlSize){
            lastErrorMessage = "ttlExpired";
        }
        //we exited the loop without either finding the destination or we had error.
        throw new jmri.JmriException(lastErrorMessage);
    }

    private boolean canLBlockBeUsed(LayoutBlock lBlock){
        if (lBlock.getBlock().getPermissiveWorking())
            return true;
        if (lBlock.getState()==Block.OCCUPIED)
            return false;
        if (lBlock.getUseExtraColor())
            return false;
        return true;
    }
    
    String lastErrorMessage = "";

    //We need to take into account if the returned block has a signalmast attached.
    int findBestHop(Block preBlock, Block currentBlock, Block destBlock, int direction, int offSet, boolean validateOnly, int pathMethod){
        int blockindex = 0;
        int lastReturnedBlockIndex = -1;
        Block block;
        LayoutBlock currentLBlock = InstanceManager.layoutBlockManagerInstance().getLayoutBlock(currentBlock);
        if(log.isDebugEnabled())
            log.debug("In find best hop current " + currentLBlock.getDisplayName() + " previous " + preBlock.getDisplayName());
        while(blockindex!=-1){
            if (currentBlock==preBlock){
                //Basically looking for the connected block, which there should only be one of!
                blockindex = currentLBlock.getConnectedBlockRouteIndex(destBlock, direction);
            } else {
                blockindex = currentLBlock.getNextBestBlock(preBlock, destBlock, offSet, METRIC);
            }
            if (blockindex!=-1){
                if(lastReturnedBlockIndex==blockindex){
                    lastErrorMessage = "Block already returned";
                    return -1;
                }
                block = currentLBlock.getNextBlockAtIndex(blockindex);
                LayoutBlock lBlock = InstanceManager.layoutBlockManagerInstance().getLayoutBlock(block);
                if ((block == currentBlock) && (currentLBlock.getThroughPathIndex(preBlock, destBlock)==-1)){
                    lastErrorMessage="block " + block.getDisplayName() + " is directly attached, however the route to the destination block " + destBlock.getDisplayName() + " can not be directly used";
                    log.debug(lastErrorMessage);
                }
                else if ((validateOnly) || (canLBlockBeUsed(lBlock))){
                    if(log.isDebugEnabled()){
                        log.debug(block.getDisplayName() + " not occupied & not reserved but we need to check if the anchor point between the two contains a signal or not");
                        log.debug(currentBlock.getDisplayName() + " " + block.getDisplayName());
                    }
                    Block blocktoCheck = block;
                    if (block == currentBlock){
                        log.debug("current block matches returned block therefore the next block is directly connected");
                        blocktoCheck=destBlock;
                     }
                    jmri.NamedBean signal = null;
                    switch(pathMethod){
                        case MASTTOMAST : signal = getFacingSignalMast(currentBlock, blocktoCheck); break;
                        case HEADTOHEAD : signal = getFacingSignalHead(currentBlock, blocktoCheck); break;
                        case ANY : signal = (jmri.NamedBean) getFacingSignalObject(currentBlock, blocktoCheck); break;
                    }
                    if (signal==null){
                        log.debug("No object found so okay to return");
                        return blockindex;
                    } else {
                        lastErrorMessage ="Signal " + signal.getDisplayName() + " already exists between blocks " + currentBlock.getDisplayName() + " and " + blocktoCheck.getDisplayName() + " in the same direction on this path";
                        log.debug(lastErrorMessage);
                    }
                } else {
                    lastErrorMessage="block " + block.getDisplayName() + " found not to be not usable";
                    log.debug(lastErrorMessage);
                }
                lastReturnedBlockIndex = blockindex;
                offSet = blockindex;
            } else {
                log.debug("At this point the getNextBextBlock() has returned a -1");
            }
        }
        return -1;
    }
    
    /**
     * Determines if one set of blocks is reachable from another set of blocks
     * based upon the directions of the set of blocks.
     * <p>
     * This is used to help with identifying items such as signalmasts located 
     * at positionable points or turnouts are facing in the same direction as 
     * other given signalmasts.
     * <p>
     * Given the current block and the next block we can work out the direction 
     * of travel.
     * Given the destBlock and the next block on, we can determine the whether 
     * the destBlock comes before the destBlock+1.
     * returns true if destBlock comes before destBlock+1
     * returns false if destBlock comes after destBlock+1
     * throws Jmri.Exception if any Block is null;
     */
    public boolean checkValidDest(LayoutBlock currentBlock, LayoutBlock nextBlock, LayoutBlock destBlock, LayoutBlock destBlockn1) throws jmri.JmriException {
        if (!isAdvancedRoutingEnabled()){
            log.info("Advanced routing has not been enabled therefore we cannot use this function");
            throw new jmri.JmriException("Advanced routing has not been enabled therefore we cannot use this function");
        }
        if(log.isDebugEnabled())
            log.debug(currentBlock.getDisplayName() + ", " + nextBlock.getDisplayName() + ", " + destBlock.getDisplayName() + ", " + destBlockn1.getDisplayName());
        if((destBlockn1!=null) && (destBlock!=null) && (currentBlock!=null) && (nextBlock!=null)){
            if(!currentBlock.isRouteToDestValid(nextBlock.getBlock(), destBlock.getBlock())){
                log.debug("Route to dest not valid");
                return false;
            }
            if(log.isDebugEnabled()){
                log.debug("dest " + destBlock.getDisplayName());
                log.debug("remote prot " + destBlockn1.getDisplayName());
            }
            //Do a simple test to see if one is reachable from the other.
            int desCount = currentBlock.getBlockHopCount(destBlock.getBlock(), nextBlock.getBlock());
            int proCount = currentBlock.getBlockHopCount(destBlockn1.getBlock(), nextBlock.getBlock());
            log.debug("dest " + desCount + " protecting " + proCount);
            if(proCount<desCount){
                /*Need to do a more advanced check in this case as the destBlockn1
                could be reached via a different route and therefore have a smaller 
                hop count we need to therefore step through each block until we reach
                the end.*/
                ArrayList<LayoutBlock> blockList = getLayoutBlocks(currentBlock, destBlock, nextBlock, true, MASTTOMAST);
                if(blockList.contains(destBlockn1)){
                    log.debug("Signal mast in the wrong direction");
                    return false;
                }
                /*Work on the basis that if you get the blocks from source to dest
                then the dest+1 block should not be included*/
                log.debug("Signal mast in the correct direction");
                return true;
            } else {
                return true;
            }
        } else if (destBlockn1==null){
            throw new jmri.JmriException("Block in Destination Protecting Field returns as invalid");
        } else if (destBlock==null){
            throw new jmri.JmriException("Block in Destination Field returns as invalid");
        } else if (currentBlock==null){
            throw new jmri.JmriException("Block in Facing Field returns as invalid");
        }
        else if (nextBlock==null){
            throw new jmri.JmriException("Block in Protecting Field returns as invalid");
        }
        throw new jmri.JmriException("BlockIsNull");
    }

	private boolean warnConnectivity = true;
	/**
	 * Controls switching off incompatible block connectivity messages
	 * <P>
	 * Warnings are always on when program starts up. Once stopped by the user, these messages may not
	 *	be switched on again until program restarts.
	 */
	public boolean warn() {return warnConnectivity;}
	public void turnOffWarning() {warnConnectivity = false;}
	
    protected boolean enableAdvancedRouting = false;
    public boolean isAdvancedRoutingEnabled() { return enableAdvancedRouting; }
    public void enableAdvancedRouting(boolean boo) { 
        if (boo==enableAdvancedRouting)
            return;
        enableAdvancedRouting = boo;
        if (boo)
            initializeLayoutBlockPaths();
    }
    
    public final static int HOPCOUNT = 0x00;
    public final static int METRIC = 0x01;


    private long lastRoutingChange;
    public void setLastRoutingChange(){
        lastRoutingChange = System.nanoTime();
        stablised = false;
        setRoutingStablised();
    }

    boolean checking = false;
    boolean stablised = false;

    private void setRoutingStablised(){
        if (checking){
            return;
        }
        log.debug("routing table change has been initiated");
        checking=true;
        if(namedStablisedIndicator!=null){
            try {
                namedStablisedIndicator.getBean().setState(Sensor.INACTIVE);
            } catch (jmri.JmriException ex){
                log.debug("Error setting stability indicator sensor");
            }
        }
        Runnable r = new Runnable() {
          public void run() {
            try {
              firePropertyChange("topology", true, false);
              long oldvalue = lastRoutingChange;
              while (!stablised) {
                Thread.sleep(2000L);
                if(oldvalue==lastRoutingChange){
                    log.debug("routing table has now been stable for 2 seconds");
                    checking=false;
                    stablised=true;
                    firePropertyChange("topology", false, true);
                    if(namedStablisedIndicator!=null){
                        namedStablisedIndicator.getBean().setState(Sensor.ACTIVE);
                    }
                }
                oldvalue = lastRoutingChange;
              }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                checking=false;
            } catch (jmri.JmriException ex){
                log.debug("Error setting stability indicator sensor");
            }
          }
        };
        thr = new Thread(r);
        /*try{
        thr.join();
        } catch (InterruptedException ex) {
//            System.out.println("interrupted at join " + ex);
            checking=false;
        }*/
        thr.start();
    }

    Thread thr = null;
    
    private NamedBeanHandle<Sensor> namedStablisedIndicator;
    
    public void setStablisedSensor(String pName) throws jmri.JmriException {
        if (InstanceManager.sensorManagerInstance()!=null) {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
            if (sensor != null) {
                namedStablisedIndicator = new NamedBeanHandle<Sensor>(pName, sensor);
            } else {
                log.error("Sensor '"+pName+"' not available");
                throw new jmri.JmriException("Sensor '"+pName+"' not available");
            }
        } else {
            log.error("No SensorManager for this protocol");
            throw new jmri.JmriException("No Sensor Manager Found");
        }
    }
    
    public Sensor getStablisedSensor(){
        if(namedStablisedIndicator==null)
            return null;
        return namedStablisedIndicator.getBean();
    }

    public NamedBeanHandle <Sensor> getNamedStablisedSensor(){
        return namedStablisedIndicator;
    }
    
    public boolean routingStablised(){
        return stablised;
    }
    public long getLastRoutingChange(){
        return lastRoutingChange;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutBlockManager.class.getName());
}

/* @(#)LayoutBlockManager.java */
