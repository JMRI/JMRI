// jmri.jmrit.display.LayoutBlockManager.java

package jmri.jmrit.display;

import jmri.AbstractManager;
import jmri.Sensor;
import jmri.Block;
import jmri.SignalHead;
import jmri.InstanceManager;
import jmri.Turnout;

import java.util.ArrayList;

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
 * @version	$Revision: 1.5 $
 */
public class LayoutBlockManager extends AbstractManager {

    public LayoutBlockManager() {
        super();
    }

    public char systemLetter() { return 'I'; }
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
        if (block!=null) {
            // save in the maps
            register(block);
        }
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
		java.util.Iterator iter = getSystemNameList().iterator();
        while (iter.hasNext()) {
            String sName = (String)iter.next();
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
		// cycle through all LayoutBlocks, updating Paths of associated jmri.Blocks
		java.util.Iterator iter = getSystemNameList().iterator();
		while (iter.hasNext()) {
			String sName = (String)iter.next();
			if (sName==null) log.error("System name null during initialization of LayoutBlocks");
			log.debug("LayoutBlock initialization - system name = "+sName);
			LayoutBlock b = getBySystemName(sName); 
			b.updatePaths();
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
//		runTests();
	}
	
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
	 */
	public SignalHead getFacingSignalHead (Block facingBlock, Block protectedBlock) {
		// check input
		if ( (facingBlock == null) || (protectedBlock == null) ) {
			log.error ("null block in call to getFacingSignalHead");
			return null;
		}
		// non-null - check if input corresponds to Blacks in a Layout Editor panel.
		LayoutBlock fLayoutBlock = getByUserName(facingBlock.getUserName());
		LayoutBlock pLayoutBlock = getByUserName(protectedBlock.getUserName());
		if ( (fLayoutBlock==null) || (pLayoutBlock==null) ) {
			if (fLayoutBlock==null) log.error("Block "+facingBlock.getSystemName()+"is not on a Layout Editor panel.");
			if (pLayoutBlock==null) log.error("Block "+protectedBlock.getSystemName()+"is not on a Layout Editor panel.");
			return null;
		}
		// input has corresponding LayoutBlocks - does it correspond to a block boundary?
		LayoutEditor panel = fLayoutBlock.getMaxConnectedPanel();
		ArrayList c = panel.auxTools.getConnectivityList(fLayoutBlock);
		LayoutConnectivity lc = null;
		int i = 0;
		boolean facingIsBlock1 = true;
		while ((i<c.size()) && (lc==null)) {
			LayoutConnectivity tlc = (LayoutConnectivity)c.get(i);
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
			// this is not an internal crossover block boundary
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
						if ( (lt.getSignalA2Name()==null) || (lt.getSignalA2Name()=="") )
							//there is no signal head for diverging (crossed over)
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
						else
							// there is a diverging (crossed over) signal head, return it
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA2Name()));						
					}
					else {
						if ( (lt.getSignalC2Name()==null) || (lt.getSignalC2Name()=="") )
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
						else
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC2Name()));						
					}
				case LayoutConnectivity.XOVER_BOUNDARY_BD:
					if (facingIsBlock1) {
						if ( (lt.getSignalB2Name()==null) || (lt.getSignalB2Name()=="") )
							//there is no signal head for diverging (crossed over)
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
						else
							// there is a diverging (crossed over) signal head, return it
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB2Name()));						
					}
					else {
						if ( (lt.getSignalD2Name()==null) || (lt.getSignalD2Name()=="") )
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD1Name()));
						else
							return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalD2Name()));						
					}
			}
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
			// block boundary is at the facing point of a turnout
			lt = (LayoutTurnout)connected;
			if (facingIsBlock1) {
				if ( (lt.getSignalA2Name()==null) || (lt.getSignalA2Name()=="") )
					//there is no signal head for diverging 
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
				else {
					// check turnout position to decide which signal head to return
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
			else {
				// check turnout position to decide which signal head to return
				int state = lt.getTurnout().getKnownState();
				if ( ( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
					( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.THROWN) ) ) 
					// continuing
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
				else if ( ( (state==Turnout.THROWN) && (lt.getContinuingSense()==Turnout.CLOSED) ) ||
					( (state==Turnout.CLOSED) && (lt.getContinuingSense()==Turnout.THROWN) ) ) 
					// diverging
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
				else {
					// turnout state is UNKNOWN or INCONSISTENT
					log.error("Cannot choose signal head because turnout "+lt.getTurnout().getSystemName()+
										" is in an UNKNOWN or INCONSISTENT state.");
					return null;
				}
			}
		}
		if (cType==LayoutEditor.TURNOUT_B) {
			// block boundary is at the continuing track of a turnout
			lt = (LayoutTurnout)connected;
			if (facingIsBlock1)
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalB1Name()));
			else {
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
			}
		}
		if (cType==LayoutEditor.TURNOUT_C) {
			// block boundary is at the diverging track of a turnout
			lt = (LayoutTurnout)connected;
			if (facingIsBlock1)
				return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalC1Name()));
			else {
				if ( (lt.getSignalA2Name()==null) || (lt.getSignalA2Name()=="") )
					// there is no signal head at the throat for diverging 
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA1Name()));
				else 
					// there is a diverging head at the throat, return it
					return (InstanceManager.signalHeadManagerInstance().getSignalHead(lt.getSignalA2Name()));
			}
		}
		// block boundary must be at a level crossing
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
/*	
	private void runTests() {
		// run tests of getFacingSignalHead
		// needs for LayoutEditorClinic.xml configuration/panel file to be loaded
		// does not test cases involving crossovers
		// does not test cases where a block boundary is at the throat of a RH, LH, or WYE turnout
		if (getLayoutBlock("siding")==null) {
			log.error("runTests skipped, block 'siding' not present");
			return;
		}
		Block siding = InstanceManager.blockManagerInstance().getByUserName("siding");
		Block passing = InstanceManager.blockManagerInstance().getByUserName("passing");
		Block northeast = InstanceManager.blockManagerInstance().getByUserName("north east");
		Block northwest = InstanceManager.blockManagerInstance().getByUserName("north west");
		Block southeast = InstanceManager.blockManagerInstance().getByUserName("south east");
		Block southwest = InstanceManager.blockManagerInstance().getByUserName("south west");
		Block south = InstanceManager.blockManagerInstance().getByUserName("south");
		Block industry = InstanceManager.blockManagerInstance().getByUserName("industry");
		// full turnout signals (4 heads)
		SignalHead head = getFacingSignalHead(siding,northeast);
		log.error("SignalHead '"+head.getUserName()+"' returned for siding,north east");
		head = getFacingSignalHead(passing,northeast);
		log.error("SignalHead '"+head.getUserName()+"' returned for passing,north east");
		head = getFacingSignalHead(northeast,siding);
		log.error("SignalHead '"+head.getUserName()+"' returned for north east,siding");
		head = getFacingSignalHead(northeast,passing);
		log.error("SignalHead '"+head.getUserName()+"' returned for north east,passing");
		head = getFacingSignalHead(siding,northwest);
		log.error("SignalHead '"+head.getUserName()+"' returned for siding,north west");
		head = getFacingSignalHead(passing,northwest);
		log.error("SignalHead '"+head.getUserName()+"' returned for passing,north west");
		head = getFacingSignalHead(northwest,siding);
		log.error("SignalHead '"+head.getUserName()+"' returned for north west,siding");
		head = getFacingSignalHead(northwest,passing);
		log.error("SignalHead '"+head.getUserName()+"' returned for north west,passing");
		// 3-head turnout signals
		head = getFacingSignalHead(industry,south);
		log.error("SignalHead '"+head.getUserName()+"' returned for industry,south");
		head = getFacingSignalHead(south,industry);
		log.error("SignalHead '"+head.getUserName()+"' returned for south,industry");
		head = getFacingSignalHead(south,southeast);
		log.error("SignalHead '"+head.getUserName()+"' returned for south,south east");
		head = getFacingSignalHead(southeast,south);
		log.error("SignalHead '"+head.getUserName()+"' returned for south east,south");		
		// block boundary signals
		head = getFacingSignalHead(northwest,southwest);
		log.error("SignalHead '"+head.getUserName()+"' returned for north west,south west");
		head = getFacingSignalHead(southwest,northwest);
		log.error("SignalHead '"+head.getUserName()+"' returned for south west,north west");
		head = getFacingSignalHead(northeast,southeast);
		log.error("SignalHead '"+head.getUserName()+"' returned for north east,south east");
		head = getFacingSignalHead(southeast,northeast);
		log.error("SignalHead '"+head.getUserName()+"' returned for south east,north east");
		head = getFacingSignalHead(south,southwest);
		log.error("SignalHead '"+head.getUserName()+"' returned for south,south west");
		head = getFacingSignalHead(southwest,south);
		log.error("SignalHead '"+head.getUserName()+"' returned for south west,south");
		
		return;
	} */
	
	private boolean warnConnectivity = true;
	/**
	 * Controls switching off incompatible block connectivity messages
	 * <P>
	 * Warnings are always on when program starts up. Once stopped by the user, these messages may not
	 *	be switched on again until program restarts.
	 */
	public boolean warn() {return warnConnectivity;}
	public void turnOffWarning() {warnConnectivity = false;}
	

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutBlockManager.class.getName());
}

/* @(#)LayoutBlockManager.java */
