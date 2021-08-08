package jmri.jmrit.display.layoutEditor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.Color;

import javax.swing.JOptionPane;

import jmri.*;
import jmri.jmrit.display.Editor;
import jmri.jmrit.roster.RosterEntry;

/**
 * An icon to display a status of a Block Object.
 * <p>
 * This is the same name as display.BlockContentsIcon, it follows on from the
 * MemoryIcon
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class BlockContentsIcon extends jmri.jmrit.display.BlockContentsIcon {

    //TODO: unused - dead-code strip
    //private final String defaultText = " ";
    public BlockContentsIcon(String s, LayoutEditor panel) {
        super(s, panel);
        log.debug("BlockContentsIcon ctor= {}", BlockContentsIcon.class.getName());
    }

    private LayoutBlock lBlock = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBlock(NamedBeanHandle<Block> m) {
        super.setBlock(m);
        if (getBlock() != null) {
            lBlock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(getBlock());
        }
    }

    /**
     * add a roster to this icon
     *
     * @param roster to add
     */
    @Override
    protected void addRosterToIcon(RosterEntry roster) {
        if (!InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled() || lBlock == null) {
            super.addRosterToIcon(roster);
            return;
        }

        int paths = lBlock.getNumberOfThroughPaths();
        Block srcBlock = null;
        Block desBlock = null;
        for (int i = 0; i < paths; i++) {
            if (lBlock.isThroughPathActive(i)) {
                srcBlock = lBlock.getThroughPathSource(i);
                desBlock = lBlock.getThroughPathDestination(i);
                break;
            }
        }
        int dirA;
        int dirB;
        if (srcBlock != null && desBlock != null) {
            dirA = lBlock.getNeighbourDirection(srcBlock);
            dirB = lBlock.getNeighbourDirection(desBlock);
        } else {
            dirA = Path.EAST;
            dirB = Path.WEST;
        }

        Object[] options = {"Facing " + Path.decodeDirection(dirB),
            "Facing " + Path.decodeDirection(dirA),
            "Do Not Add"};
        int n = JOptionPane.showOptionDialog(this,
                "Would you like to assign loco "
                + roster.titleString() + " to this location",
                "Assign Loco",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);
        if (n == 2) {
            return;
        }
        if (n == 0) {
            flipRosterIcon = true;
            getBlock().setDirection(dirB);
        } else {
            flipRosterIcon = false;
            getBlock().setDirection(dirA);
        }
        if (getBlock().getValue() == roster) {
            //No change in the loco but a change in direction facing might have occurred
            updateIconFromRosterVal(roster);
        } else {
            setValue(roster);
        }
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        Editor e = getEditor();
        if (e != null) {
            e.repaint();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BlockContentsIcon.class);
}
