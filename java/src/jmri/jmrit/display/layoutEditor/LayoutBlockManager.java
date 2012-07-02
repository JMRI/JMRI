// jmri.jmrit.display.LayoutBlockManager.java
package jmri.jmrit.display.layoutEditor;

import jmri.managers.AbstractManager;
import jmri.Sensor;
import jmri.Block;
import jmri.Memory;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.NamedBeanHandle;
import jmri.NamedBean;
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
 * @version	$Revision$
 */
public class LayoutBlockManager extends AbstractManager {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    public LayoutBlockManager() {
        super();
    }
    
    public int getXMLOrder(){
        return jmri.Manager.LAYOUTBLOCKS;
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
    
    public LayoutBlock createNewLayoutBlock(){
        boolean found = true;
        while (found) {
            String sName = "ILB"+blkNum;
            LayoutBlock block = getBySystemName(sName);
            if (block==null){
                found = false;
                String uName = "AUTOBLK:"+blkNum;
                block = new LayoutBlock(sName,uName);
                register(block);
                return block;
            }
            blkNum++;
        }
        return null;
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
	 * Method to find a LayoutBlock with a specified Memory assigned as its 
	 *    value display.  Returns the block or null if no existing LayoutBlock
	 *    has the memory assigned.
	 */
	public LayoutBlock getBlockWithMemoryAssigned(Memory m) {
		java.util.Iterator<String> iter = getSystemNameList().iterator();
        while (iter.hasNext()) {
            String sName = iter.next();
            if (sName==null) { 
                log.error("System name null during scan of LayoutBlocks");
            }
            else {
				LayoutBlock block = getBySystemName(sName);
				if (block.getMemory() == m) return block;
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
        initialized = true;
        initializeLayoutBlockRouting();
	}
    
    private boolean initialized = false;
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
        if(connected==null){
            log.error("No connectivity object found between Blocks "+facingBlock.getSystemName()+
										", "+protectedBlock.getSystemName() + " " + cType);
            return null;
        }
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
        if((cType>=LayoutEditor.SLIP_A) && (cType<=LayoutEditor.SLIP_D)){
            if(!facingIsBlock1)
                return null;
        
            LayoutSlip ls = (LayoutSlip)connected;
            if(cType==LayoutEditor.SLIP_A){
                if(ls.getSlipState()==LayoutSlip.STATE_AD)
                    return (InstanceManager.signalHeadManagerInstance().getSignalHead(ls.getSignalA2Name()));
                else
                    return (InstanceManager.signalHeadManagerInstance().getSignalHead(ls.getSignalA1Name()));
            }
            if(cType==LayoutEditor.SLIP_B){
                if(ls.getTurnoutType()==LayoutSlip.DOUBLE_SLIP){
                    if(ls.getSlipState()==LayoutSlip.STATE_BC)
                        return (InstanceManager.signalHeadManagerInstance().getSignalHead(ls.getSignalB2Name()));
                    else
                        return (InstanceManager.signalHeadManagerInstance().getSignalHead(ls.getSignalB1Name()));
                }
                else
                    return (InstanceManager.signalHeadManagerInstance().getSignalHead(ls.getSignalB1Name()));
            }
            if(cType==LayoutEditor.SLIP_C){
                if(ls.getTurnoutType()==LayoutSlip.DOUBLE_SLIP){
                    if(ls.getSlipState()==LayoutSlip.STATE_BC)
                        return (InstanceManager.signalHeadManagerInstance().getSignalHead(ls.getSignalC2Name()));
                    else
                        return (InstanceManager.signalHeadManagerInstance().getSignalHead(ls.getSignalC1Name()));
                }
                else
                    return (InstanceManager.signalHeadManagerInstance().getSignalHead(ls.getSignalC1Name()));
            }
            if(cType==LayoutEditor.SLIP_D){
                if(ls.getSlipState()==LayoutSlip.STATE_AD)
                    return (InstanceManager.signalHeadManagerInstance().getSignalHead(ls.getSignalD2Name()));
                else
                    return (InstanceManager.signalHeadManagerInstance().getSignalHead(ls.getSignalD1Name()));
            }
        }
		// block boundary must be at a level crossing
		if ( (cType<LayoutEditor.LEVEL_XING_A) || (cType>LayoutEditor.LEVEL_XING_D) ) {
			log.error(cType +  " " + connected +" Block Boundary not identified correctly - Blocks "+facingBlock.getSystemName()+
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
	 * Method to return the named bean of either a Sensor or signalmast facing into a specified Block from a specified protected Block.
	 * <P>
     * @param facingBlock
     * @param panel
     * @return The assigned sensor or signal mast as a named bean
     */
    public jmri.NamedBean getNamedBeanAtEndBumper(Block facingBlock, LayoutEditor panel){
        jmri.NamedBean bean = getSignalMastAtEndBumper(facingBlock, panel);
        if(bean!=null)
            return bean;
        else return getSensorAtEndBumper(facingBlock, panel);
    }
    
    /**
     * Method to return a Signal Mast that is assigned to a block which has an
     * end bumper at one end.
    */
    public SignalMast getSignalMastAtEndBumper(Block facingBlock, LayoutEditor panel){
        if (facingBlock==null){
            log.error ("null block in call to getFacingSignalMast");
			return null;
		}
        LayoutBlock fLayoutBlock = getByUserName(facingBlock.getUserName());
        if (fLayoutBlock==null){
            log.error("Block "+facingBlock.getSystemName()+"is not on a Layout Editor panel.");
			return null;
		}
        if(panel==null)
            panel = fLayoutBlock.getMaxConnectedPanel();
        for(int i = 0; i<panel.trackList.size(); i++){
            TrackSegment t = panel.trackList.get(i);
            if(t.getLayoutBlock()==fLayoutBlock){
                PositionablePoint p = null;
                if(t.getType1()==LayoutEditor.POS_POINT){
                    p = (PositionablePoint) t.getConnect1();
                        if(p.getType()==PositionablePoint.END_BUMPER){
                            if(!p.getEastBoundSignalMast().equals(""))
                                return jmri.InstanceManager.signalMastManagerInstance().getSignalMast(p.getEastBoundSignalMast());
                            if(!p.getWestBoundSignalMast().equals(""))
                                return jmri.InstanceManager.signalMastManagerInstance().getSignalMast(p.getWestBoundSignalMast());
                        }

                }
                if (t.getType2()==LayoutEditor.POS_POINT){
                    p = (PositionablePoint) t.getConnect2();
                        if(p.getType()==PositionablePoint.END_BUMPER){
                            if(!p.getEastBoundSignalMast().equals(""))
                                return jmri.InstanceManager.signalMastManagerInstance().getSignalMast(p.getEastBoundSignalMast());
                            if(!p.getWestBoundSignalMast().equals(""))
                                return jmri.InstanceManager.signalMastManagerInstance().getSignalMast(p.getWestBoundSignalMast());
                        }
                }
            }
        }
        return null;
    }
    
    /**
     * Method to return a Sensor facing into a specific Block.
     * This is used for blocks that have an end bumper at one end
    */
    public Sensor getSensorAtEndBumper(Block facingBlock, LayoutEditor panel){
        if (facingBlock==null){
            log.error ("null block in call to getFacingSensor");
			return null;
		}
        LayoutBlock fLayoutBlock = getByUserName(facingBlock.getUserName());
        if (fLayoutBlock==null){
            log.error("Block "+facingBlock.getSystemName()+"is not on a Layout Editor panel.");
			return null;
		}
        if(panel==null)
            panel = fLayoutBlock.getMaxConnectedPanel();
        for(int i = 0; i<panel.trackList.size(); i++){
            TrackSegment t = panel.trackList.get(i);
            if(t.getLayoutBlock()==fLayoutBlock){
                PositionablePoint p = null;
                if(t.getType1()==LayoutEditor.POS_POINT){
                    p = (PositionablePoint) t.getConnect1();
                        if(p.getType()==PositionablePoint.END_BUMPER){
                            if(!p.getEastBoundSensor().equals(""))
                                return jmri.InstanceManager.sensorManagerInstance().getSensor(p.getEastBoundSensor());
                            if(!p.getWestBoundSensor().equals(""))
                                return jmri.InstanceManager.sensorManagerInstance().getSensor(p.getWestBoundSensor());
                        }

                }
                if (t.getType2()==LayoutEditor.POS_POINT){
                    p = (PositionablePoint) t.getConnect2();
                        if(p.getType()==PositionablePoint.END_BUMPER){
                            if(!p.getEastBoundSensor().equals(""))
                                return jmri.InstanceManager.sensorManagerInstance().getSensor(p.getEastBoundSensor());
                            if(!p.getWestBoundSensor().equals(""))
                                return jmri.InstanceManager.sensorManagerInstance().getSensor(p.getWestBoundSensor());
                        }
                }
            }
        }
        return null;
    }
    
    /**
     * Method to return the named bean of either a Sensor or signalmast facing into a specified Block from a specified protected Block.
     * <P>
     * @param facingBlock
     * @param protectedBlock
     * @return The assigned sensor or signal mast as a named bean
     */
    public jmri.NamedBean getFacingNamedBean(Block facingBlock, Block protectedBlock, LayoutEditor panel){
        jmri.NamedBean bean = getFacingSignalMast(facingBlock, protectedBlock, panel);
        if(bean!=null)
            return bean;
        else return getFacingSensor(facingBlock, protectedBlock, panel);
    }
    
    public SignalMast getFacingSignalMast(Block facingBlock, Block protectedBlock){
        return getFacingSignalMast(facingBlock, protectedBlock, null);
    }
    
	/**
	 * Method to return the Signal Mast facing into a specified Block from a specified protected Block.
	 * <P>
     * @param facingBlock
     * @param protectedBlock
     * @return The assigned signalMast.
     */
    public SignalMast getFacingSignalMast(Block facingBlock, Block protectedBlock, LayoutEditor panel){
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
        if(panel==null){
            panel = fLayoutBlock.getMaxConnectedPanel();
        }
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
        if(connected==null){
            log.error("No connectivity object found between Blocks "+facingBlock.getSystemName()+
										", "+protectedBlock.getSystemName()  + " " + cType);
            return null;
        }
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
        if(!facingIsBlock1)
            return null;
        if (cType==LayoutEditor.TURNOUT_A) {
            lt = (LayoutTurnout)connected;
            if ((lt.getLinkType()==LayoutTurnout.NO_LINK) || (lt.getLinkType()==LayoutTurnout.FIRST_3_WAY)){
                if ( (lt.getSignalAMast()!=null) || (!lt.getSignalAMast().equals("")) ){
                    return (InstanceManager.signalMastManagerInstance().getSignalMast(lt.getSignalAMast()));
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
            if ( (lt.getSignalBMast()!=null) || (!lt.getSignalBMast().equals("")) ){
                return (InstanceManager.signalMastManagerInstance().getSignalMast(lt.getSignalBMast()));
            }
            return null;
        }
        if (cType==LayoutEditor.TURNOUT_C) {
            lt = (LayoutTurnout)connected;
            if ( (lt.getSignalCMast()!=null) || (!lt.getSignalCMast().equals("")) ){
                return (InstanceManager.signalMastManagerInstance().getSignalMast(lt.getSignalCMast()));
            }
            return null;
        }
        
        if (cType==LayoutEditor.TURNOUT_D) {
            lt = (LayoutTurnout)connected;
                if ( (lt.getSignalDMast()!=null) || (!lt.getSignalDMast().equals("")) ){
                    return (InstanceManager.signalMastManagerInstance().getSignalMast(lt.getSignalDMast()));
                }
            return null;
        }
        
        if((cType>=LayoutEditor.SLIP_A) && (cType<=LayoutEditor.SLIP_D)){
            LayoutSlip ls = (LayoutSlip)connected;
            if(cType==LayoutEditor.SLIP_A){
                if((ls.getSignalAMast()!=null) || (!ls.getSignalAMast().equals(""))){
                    return (InstanceManager.signalMastManagerInstance().getSignalMast(ls.getSignalAMast()));
                }
                return null;
            }
            if(cType==LayoutEditor.SLIP_B){
                if((ls.getSignalBMast()!=null) || (!ls.getSignalBMast().equals(""))){
                    return (InstanceManager.signalMastManagerInstance().getSignalMast(ls.getSignalBMast()));
                }
                return null;
            }
            if(cType==LayoutEditor.SLIP_C){
                if((ls.getSignalCMast()!=null) || (!ls.getSignalCMast().equals(""))){
                    return (InstanceManager.signalMastManagerInstance().getSignalMast(ls.getSignalCMast()));
                }
                return null;
            }
            if(cType==LayoutEditor.SLIP_D){
                if((ls.getSignalDMast()!=null) || (!ls.getSignalDMast().equals(""))){
                    return (InstanceManager.signalMastManagerInstance().getSignalMast(ls.getSignalDMast()));
                }
                return null;
            }
        }
        
        if ( (cType<LayoutEditor.LEVEL_XING_A) || (cType>LayoutEditor.LEVEL_XING_D) ) {
			log.error("Block Boundary not identified correctly - Blocks "+facingBlock.getSystemName()+
										", "+protectedBlock.getSystemName());
			return null;
		}
        /*We don't allow signal masts on the block outward facing from the level
        xing, nor do we consider the signal mast, that is protecting the in block on the xing*/
		LevelXing xing = (LevelXing)connected;
        if (cType==LayoutEditor.LEVEL_XING_A) {
            // block boundary is at the A connection of a level crossing
            return (InstanceManager.signalMastManagerInstance().getSignalMast(xing.getSignalAMastName()));
        }
        if (cType==LayoutEditor.LEVEL_XING_B) {
             // block boundary is at the B connection of a level crossing
            return (InstanceManager.signalMastManagerInstance().getSignalMast(xing.getSignalBMastName()));
        }
        if (cType==LayoutEditor.LEVEL_XING_C) {
            // block boundary is at the C connection of a level crossing
            return (InstanceManager.signalMastManagerInstance().getSignalMast(xing.getSignalCMastName()));
        }
        if (cType==LayoutEditor.LEVEL_XING_D) {
            // block boundary is at the D connection of a level crossing
            return (InstanceManager.signalMastManagerInstance().getSignalMast(xing.getSignalDMastName()));
        }
        return null;
    }
    
    /**
	 * Method to return the Sensor facing into a specified Block from a specified protected Block.
	 * <P>
     * @param facingBlock
     * @param protectedBlock
     * @return The assigned sensor.
     */
    public Sensor getFacingSensor (Block facingBlock, Block protectedBlock, LayoutEditor panel){
        // check input
		if ( (facingBlock == null) || (protectedBlock == null) ) {
			log.error ("null block in call to getFacingSignal");
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
        if(panel==null){
            panel = fLayoutBlock.getMaxConnectedPanel();
        }
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
        if(connected==null){
            log.error("No connectivity object found between Blocks "+facingBlock.getSystemName()+
										", "+protectedBlock.getSystemName()  + " " + cType);
            return null;
        }
        
		if (cType==LayoutEditor.TRACK) {
			// block boundary is at an Anchor Point
			LayoutEditorTools tools = new LayoutEditorTools(panel);
			PositionablePoint p = panel.findPositionablePointAtTrackSegments(tr,(TrackSegment)connected);
			boolean block1IsWestEnd = tools.isAtWestEndOfAnchor(tr,p);
			if ( (block1IsWestEnd && facingIsBlock1) || (!block1IsWestEnd && !facingIsBlock1) ) {
				// block1 is on the west (north) end of the block boundary
				return (InstanceManager.sensorManagerInstance().getSensor(p.getEastBoundSensor()));
			}
			else {
				return (InstanceManager.sensorManagerInstance().getSensor(p.getWestBoundSensor()));
			}
		}
        if(!facingIsBlock1)
            return null;
        if (cType==LayoutEditor.TURNOUT_A) {
            lt = (LayoutTurnout)connected;
            if ((lt.getLinkType()==LayoutTurnout.NO_LINK) || (lt.getLinkType()==LayoutTurnout.FIRST_3_WAY)){
                if ( (lt.getSensorA()!=null) || (!lt.getSensorA().equals("")) ){
                    return (InstanceManager.sensorManagerInstance().getSensor(lt.getSensorA()));
                }
                // we only allow signal s inbound to the turnout.
                return null;
            }
            else {
                return null;
            }
        }
        
        if (cType==LayoutEditor.TURNOUT_B) {
            lt = (LayoutTurnout)connected;
            if ( (lt.getSensorB()!=null) || (!lt.getSensorB().equals("")) ){
                return (InstanceManager.sensorManagerInstance().getSensor(lt.getSensorB()));
            }
            return null;
        }
        if (cType==LayoutEditor.TURNOUT_C) {
            lt = (LayoutTurnout)connected;
            if ( (lt.getSensorC()!=null) || (!lt.getSensorC().equals("")) ){
                return (InstanceManager.sensorManagerInstance().getSensor(lt.getSensorC()));
            }
            return null;
        }
        
        if (cType==LayoutEditor.TURNOUT_D) {
            lt = (LayoutTurnout)connected;
                if ( (lt.getSensorD()!=null) || (!lt.getSensorD().equals("")) ){
                    return (InstanceManager.sensorManagerInstance().getSensor(lt.getSensorD()));
                }
            return null;
        }
        if((cType>=LayoutEditor.SLIP_A) && (cType<=LayoutEditor.SLIP_D)){
            LayoutSlip ls = (LayoutSlip)connected;
            if(cType==LayoutEditor.SLIP_A){
                if((ls.getSensorA()!=null) || (!ls.getSensorA().equals(""))){
                    return (InstanceManager.sensorManagerInstance().getSensor(ls.getSensorA()));
                }
                return null;
            }
            if(cType==LayoutEditor.SLIP_B){
                if((ls.getSensorB()!=null) || (!ls.getSensorB().equals(""))){
                    return (InstanceManager.sensorManagerInstance().getSensor(ls.getSensorB()));
                }
                return null;
            }
            if(cType==LayoutEditor.SLIP_C){
                if((ls.getSensorC()!=null) || (!ls.getSensorC().equals(""))){
                    return (InstanceManager.sensorManagerInstance().getSensor(ls.getSensorC()));
                }
                return null;
            }
            if(cType==LayoutEditor.SLIP_D){
                if((ls.getSensorD()!=null) || (!ls.getSensorD().equals(""))){
                    return (InstanceManager.sensorManagerInstance().getSensor(ls.getSensorD()));
                }
                return null;
            }
        }
        if ( (cType<LayoutEditor.LEVEL_XING_A) || (cType>LayoutEditor.LEVEL_XING_D) ) {
			log.error(cType +  " " + connected +" Block Boundary not identified correctly - Blocks "+facingBlock.getSystemName()+
										", "+protectedBlock.getSystemName());
			return null;
		}
        /*We don't allow signal s on the block outward facing from the level
        xing, nor do we consider the signal , that is protecting the in block on the xing*/
		LevelXing xing = (LevelXing)connected;
        if (cType==LayoutEditor.LEVEL_XING_A) {
            // block boundary is at the A connection of a level crossing
            return (InstanceManager.sensorManagerInstance().getSensor(xing.getSensorAName()));
        }
        if (cType==LayoutEditor.LEVEL_XING_B) {
             // block boundary is at the B connection of a level crossing
            return (InstanceManager.sensorManagerInstance().getSensor(xing.getSensorBName()));
        }
        if (cType==LayoutEditor.LEVEL_XING_C) {
            // block boundary is at the C connection of a level crossing
            return (InstanceManager.sensorManagerInstance().getSensor(xing.getSensorCName()));
        }
        if (cType==LayoutEditor.LEVEL_XING_D) {
            // block boundary is at the D connection of a level crossing
            return (InstanceManager.sensorManagerInstance().getSensor(xing.getSensorDName()));
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
        Object sig = getFacingSignalMast(facingBlock, protectedBlock, null);
        if (sig!=null)
            return sig;
        sig = getFacingSignalHead(facingBlock, protectedBlock);
        return sig;
    }
    
    /**
     * Method to return the block facing a given bean object (Sensor, SignalMast or SignalHead).
     * <P>
     * @param nb NamedBean
     * @param panel - panel that this bean is on
     * @return The block that the bean object is facing
     */
    public LayoutBlock getFacingBlockByNamedBean(NamedBean nb, LayoutEditor panel){
        if(nb instanceof SignalMast){
            return getFacingBlockByMast((SignalMast)nb, panel);
        }
        if(nb instanceof SignalHead){
            return getFacingBlock((SignalHead)nb, panel);
        }
        if(nb instanceof Sensor){
            return getFacingBlockBySensor((Sensor)nb, panel);
        }
        return null;
    }
    
     /**
     * Method to return the block that a given bean object (Sensor, SignalMast or SignalHead) is protecting
     * <P>
     * @param nb NamedBean
     * @param panel - panel that this bean is on
     * @return The block that the bean object is facing
     */
    public LayoutBlock getProtectedBlockByNamedBean(NamedBean nb, LayoutEditor panel){
        if(nb instanceof SignalMast){
            return getProtectedBlockByMast((SignalMast)nb, panel);
        }
        if(nb instanceof SignalHead){
            return getProtectedBlock((SignalHead)nb, panel);
        }
        if(nb instanceof Sensor){
            return getProtectedBlockBySensor((Sensor)nb, panel);
        }
        return null;
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
        LayoutSlip ls = panel.findLayoutSlipBySignalMast(signalMastName);
        if(ls!=null){
            return ls.getLayoutBlock();
        }
        return null;
    }
     
    /**
     * Method to return the LayoutBlock that a given signal mast is facing.
     */
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
        
        LayoutSlip ls = panel.findLayoutSlipBySignalMast(signalMastName);
        if(ls!=null){
            Object connect;
            if(ls.getSignalAMast().equals(signalMastName)){
                connect = ls.getConnectA();
            } else if (ls.getSignalBMast().equals(signalMastName)) {
                connect = ls.getConnectB();
            } else if (ls.getSignalCMast().equals(signalMastName)) {
                connect = ls.getConnectC();
            } else {
                connect = ls.getConnectD();
            }
            
            if (connect instanceof TrackSegment){
                tr = (TrackSegment) connect;
                log.debug("return block " + tr.getLayoutBlock().getDisplayName());
                return tr.getLayoutBlock();
            }
        }
        return null;
    }
     
    /**
     * Method to return the LayoutBlock that a given sensor is protecting.
     */
    public LayoutBlock getProtectedBlockBySensor(Sensor sensor, LayoutEditor panel){
        LayoutBlock pro = getProtectedBlockBySensor(sensor.getUserName(), panel);
        if(pro == null)
            pro = getProtectedBlockBySensor(sensor.getSystemName(), panel);
        return pro;
    }
     
    /**
     * Method to return the LayoutBlock that a given sensor is protecting.
     */
    public LayoutBlock getProtectedBlockBySensor(String sensorName, LayoutEditor panel){
        PositionablePoint pp = panel.findPositionablePointByEastBoundSensor(sensorName);
        TrackSegment tr;
        boolean east = true;
        if (pp==null) {
            pp = panel.findPositionablePointByWestBoundSensor(sensorName);
            east=false;
        }
        if(pp!=null){
            LayoutEditorTools tools = new LayoutEditorTools(panel);
            if(east) {
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
        
        LayoutTurnout t = panel.findLayoutTurnoutBySensor(sensorName);
        if(t!=null){
            if(t.getSensorA().equals(sensorName)){
                return t.getLayoutBlock();
            } else if (t.getSensorB().equals(sensorName)) {
                return t.getLayoutBlockB();
            } else if (t.getSensorC().equals(sensorName)) {
                return t.getLayoutBlockC();
            } else {
                return t.getLayoutBlockD();
            }
        }
        
        LevelXing l = panel.findLevelXingBySensor(sensorName);
        if(l!=null){
            if(l.getSensorAName().equals(sensorName)){
                return l.getLayoutBlockAC();
            } else if (l.getSensorBName().equals(sensorName)) {
                return l.getLayoutBlockBD();
            } else if (l.getSensorCName().equals(sensorName)) {
                return l.getLayoutBlockAC();
            } else {
                return l.getLayoutBlockBD();
            }
            
        }
        LayoutSlip ls = panel.findLayoutSlipBySensor(sensorName);
        if(ls!=null){
            return ls.getLayoutBlock();
        }
        return null;
    }

    /**
     * Method to return the LayoutBlock that a given sensor is facing.
     */
    public LayoutBlock getFacingBlockBySensor(Sensor sensor, LayoutEditor panel){
        LayoutBlock facing = getFacingBlockBySensor(sensor.getUserName(), panel);
        if(facing == null)
            facing = getFacingBlockBySensor(sensor.getSystemName(), panel);
        return facing;
    }
    
    /**
     * Method to return the LayoutBlock that a given sensor is facing.
     */
    public LayoutBlock getFacingBlockBySensor(String sensorName, LayoutEditor panel){
        PositionablePoint pp = panel.findPositionablePointByEastBoundSensor(sensorName); //was west
        TrackSegment tr = null;
        boolean east = true;
        //Don't think that the logic for this is the right way round
        if (pp==null) {
            pp = panel.findPositionablePointByWestBoundSensor(sensorName);  // was east
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
        LayoutTurnout t = panel.findLayoutTurnoutBySensor(sensorName);
        if(t!=null){
            log.debug("found signalmast at turnout " + t.getTurnout().getDisplayName());
            Object connect;
            if(t.getSensorA().equals(sensorName)){
                connect = t.getConnectA();
            } else if (t.getSensorB().equals(sensorName)) {
                connect = t.getConnectB();
            } else if (t.getSensorC().equals(sensorName)) {
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
        
        LevelXing l = panel.findLevelXingBySensor(sensorName);
        if(l!=null){
            Object connect;
            if(l.getSensorAName().equals(sensorName)){
                connect = l.getConnectA();
            } else if (l.getSensorBName().equals(sensorName)) {
                connect = l.getConnectB();
            } else if (l.getSensorCName().equals(sensorName)) {
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
        LayoutSlip ls = panel.findLayoutSlipBySensor(sensorName);
        if(ls!=null){
            Object connect;
            if(ls.getSensorA().equals(sensorName)){
                connect = ls.getConnectA();
            } else if (ls.getSensorB().equals(sensorName)) {
                connect = ls.getConnectB();
            } else if (ls.getSensorC().equals(sensorName)) {
                connect = ls.getConnectC();
            } else {
                connect = ls.getConnectD();
            }
            
            if (connect instanceof TrackSegment){
                tr = (TrackSegment) connect;
                log.debug("return block " + tr.getLayoutBlock().getDisplayName());
                return tr.getLayoutBlock();
            }
        }
        return null;
    }

    public LayoutBlock getProtectedBlock(SignalHead signalHead, LayoutEditor panel){
        LayoutBlock protect = getProtectedBlock(signalHead.getUserName(), panel);
        if(protect == null)
            protect = getProtectedBlock(signalHead.getSystemName(), panel);
        return protect;
    }
    
    /**
     * Method to return the LayoutBlock that a given signal is protecting.
     */
     /* @TODO This needs to be expanded to cover turnouts and level crossings. */
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
    
    public LayoutBlock getFacingBlock(SignalHead signalHead, LayoutEditor panel){
        LayoutBlock facing = getFacingBlock(signalHead.getUserName(), panel);
        if(facing == null)
            facing = getFacingBlock(signalHead.getSystemName(), panel);
        return facing;
    }
    
    /**
     * Method to return the LayoutBlock that a given signal is facing.
     */
     /* @TODO This needs to be expanded to cover turnouts and level crossings. */
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
    
    /**
    * returns true if advanced layout block routing has been enabled.
    */
    public boolean isAdvancedRoutingEnabled() { return enableAdvancedRouting; }
    
    /**
    * Enables the advanced layout block routing protocol
    * <p>
    * The block routing protocol enables each layout block to build up a list of 
    * all reachable blocks, along with how far away they are, which direction they
    * are in and which of the connected blocks they are reachable from.
    */
    public void enableAdvancedRouting(boolean boo) { 
        if (boo==enableAdvancedRouting)
            return;
        enableAdvancedRouting = boo;
        if (boo && initialized){
            initializeLayoutBlockRouting();
        }
        firePropertyChange("advancedRoutingEnabled", !enableAdvancedRouting, enableAdvancedRouting);
    }
    
    private void initializeLayoutBlockRouting() {
        if(!enableAdvancedRouting || !initialized)
            return;
		// cycle through all LayoutBlocks, completing initialization of the layout block routing
        java.util.Enumeration<jmri.NamedBean> en = _tsys.elements();
        while (en.hasMoreElements()) {
			((LayoutBlock)en.nextElement()).initializeLayoutBlockRouting();
        }
    }
    
    public LayoutBlockConnectivityTools getLayoutBlockConnectivityTools(){
        return lbct;
    }

    LayoutBlockConnectivityTools lbct = new LayoutBlockConnectivityTools();
    
    private long lastRoutingChange;

    void setLastRoutingChange(){
        lastRoutingChange = System.nanoTime();
        stabilised = false;
        setRoutingStabilised();
    }

    boolean checking = false;
    boolean stabilised = false;

    private void setRoutingStabilised(){
        if (checking){
            return;
        }
        log.debug("routing table change has been initiated");
        checking=true;
        if(namedStabilisedIndicator!=null){
            try {
                namedStabilisedIndicator.getBean().setState(Sensor.INACTIVE);
            } catch (jmri.JmriException ex){
                log.debug("Error setting stability indicator sensor");
            }
        }
        Runnable r = new Runnable() {
          public void run() {
            try {
              firePropertyChange("topology", true, false);
              long oldvalue = lastRoutingChange;
              while (!stabilised) {
                Thread.sleep(2000L);
                if(oldvalue==lastRoutingChange){
                    log.debug("routing table has now been stable for 2 seconds");
                    checking=false;
                    stabilised=true;
                    firePropertyChange("topology", false, true);
                    if(namedStabilisedIndicator!=null){
                        namedStabilisedIndicator.getBean().setState(Sensor.ACTIVE);
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
        thr.start();
    }

    Thread thr = null;
    
    private NamedBeanHandle<Sensor> namedStabilisedIndicator;
    
    /**
    * Assign a sensor to the routing protocol, that changes state dependant upon
    * if the routing protocol has stabilised or is under going a change.
    */
    public void setStabilisedSensor(String pName) throws jmri.JmriException {
        if (InstanceManager.sensorManagerInstance()!=null) {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
            if (sensor != null) {
                namedStabilisedIndicator = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, sensor);
            } else {
                log.error("Sensor '"+pName+"' not available");
                throw new jmri.JmriException("Sensor '"+pName+"' not available");
            }
            try{
                if(stabilised)
                    sensor.setState(Sensor.ACTIVE);
                else
                    sensor.setState(Sensor.INACTIVE);
            } catch (jmri.JmriException ex){
                log.error("Error setting stablilty indicator sensor");
            }
        } else {
            log.error("No SensorManager for this protocol");
            throw new jmri.JmriException("No Sensor Manager Found");
        }
    }
    
    /**
    * Return the sensor used to indicate if the routing protocol has stabilised or not
    */
    public Sensor getStabilisedSensor(){
        if(namedStabilisedIndicator==null)
            return null;
        return namedStabilisedIndicator.getBean();
    }

    /**
    *   Get the sensor used for the stability indication
    */
    public NamedBeanHandle <Sensor> getNamedStabilisedSensor(){
        return namedStabilisedIndicator;
    }
    
    /**
    * Returns true if the layout block routing protocol has stabilised
    */
    public boolean routingStablised(){
        return stabilised;
    }
    
    /**
    * returns the time when the last routing change was made, recorded as System.nanoTime()
    */
    public long getLastRoutingChange(){
        return lastRoutingChange;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutBlockManager.class.getName());
}

/* @(#)LayoutBlockManager.java */
