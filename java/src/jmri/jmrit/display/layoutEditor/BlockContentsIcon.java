package jmri.jmrit.display.layoutEditor;
/**
 * An icon to display a status of a Block Object.<P>
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.roster.RosterEntry;
import jmri.Block;
import javax.swing.JOptionPane;

// This is the same name as display.MemoryIcon, but a very
// separate class. That's not good. Unfortunately, it's too 
// hard to disentangle that now because it's resident in the
// panel file that have been written out, so we just annote 
// the fact.
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class BlockContentsIcon extends jmri.jmrit.display.BlockContentsIcon {

    String defaultText = " ";

    public BlockContentsIcon(String s, LayoutEditor panel) {
        super(s, panel);
        log.debug("BlockContentsIcon ctor= "+BlockContentsIcon.class.getName());
    }
    
    LayoutBlock lBlock = null;
    
    public void setBlock(jmri.NamedBeanHandle<Block> m) {
        super.setBlock(m);
        if(getBlock()!=null){
            lBlock = jmri.InstanceManager.layoutBlockManagerInstance().getLayoutBlock(getBlock());
        }
    }
    
    protected void addRosterToIcon(RosterEntry roster){
        if(!jmri.InstanceManager.layoutBlockManagerInstance().isAdvancedRoutingEnabled() || lBlock==null){
            super.addRosterToIcon(roster);
            return;
        }
        
        int paths = lBlock.getNumberOfThroughPaths();
        jmri.Block srcBlock=null;
        jmri.Block desBlock=null;
        for(int i = 0; i<paths; i++){
            if(lBlock.isThroughPathActive(i)){
                srcBlock = lBlock.getThroughPathSource(i);
                desBlock = lBlock.getThroughPathDestination(i);
                break;
            }
        }
        int dirA;
        int dirB;
        if(srcBlock!=null && desBlock!=null){
            dirA = lBlock.getNeighbourDirection(srcBlock);
            dirB = lBlock.getNeighbourDirection(desBlock);
        }
        else {
            dirA = jmri.Path.EAST;
            dirB = jmri.Path.WEST;
        }
        
        Object[] options = {"Facing "+jmri.Path.decodeDirection(dirB),
                    "Facing "+jmri.Path.decodeDirection(dirA),
                    "Do Not Add"};
        int n = JOptionPane.showOptionDialog(this,
            "Would you like to assign loco "
            +  roster.titleString() + " to this location",
            "Assign Loco",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[2]);
        if(n==2)
            return;
        if(n==0) {
            flipRosterIcon = true;
            getBlock().setDirection(dirB);
        } else {
            flipRosterIcon = false;
            getBlock().setDirection(dirA);
        }
        if(getBlock().getValue()==roster){
            //No change in the loco but a change in direction facing might have occured
             updateIconFromRosterVal(roster);
        } else {
            setValue(roster);
        }
    }
    
    static Logger log = LoggerFactory.getLogger(BlockContentsIcon.class.getName());
}

