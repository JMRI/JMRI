package jmri.jmrit.entryexit;

import jmri.Block;
import jmri.NamedBean;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;

public class ManuallySetRoute {

    LayoutBlockManager lbm = jmri.InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
    PointDetails sourcePoint = null;

    public ManuallySetRoute(PointDetails pd) {
        sourcePoint = pd;
        LayoutBlock facing = lbm.getFacingBlockByNamedBean(pd.getSensor(), pd.getPanel());
        EntryExitPairs manager = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class);
        for (LayoutBlock pro : lbm.getProtectingBlocksByNamedBean(pd.getSensor(), pd.getPanel())) {
            if (findDestPoint(pro, facing)) {
                PointDetails dest = manager.providePoint(destLoc, pd.getPanel());
                Source src = manager.getSourceForPoint(pd);
                if (dest != null && src != null) {
                    DestinationPoints dp = src.getDestForPoint(dest);
                    if (dp != null) {
                        dp.setInterlockRoute(false);
                        break;
                    }
                }
            }
        }
    }

    NamedBean destLoc = null;

    int depth = 0;

    boolean findDestPoint(LayoutBlock pro, LayoutBlock facing) {
        depth++;
        if (depth > 50) //This is to prevent a loop, only look as far as 50 blocks
        {
            return false;
        }
        boolean looking = true;
        if (pro.getNumberOfThroughPaths() == 0) {
            destLoc = lbm.getSensorAtEndBumper(pro.getBlock(), sourcePoint.getPanel());
        } else {
            while (looking) {
                Block found = cycle(pro, facing);
                if (found != null) {
                    destLoc = lbm.getFacingBean(pro.getBlock(), found, sourcePoint.getPanel(), jmri.Sensor.class);
                    if (destLoc != null) {
                        looking = false;
                    } else {
                        findDestPoint(lbm.getLayoutBlock(found), pro);
                        looking = false;
                    }
                } else {
                    looking = false;
                }
            }
        }
        if (destLoc != null) {
            return true;
        }
        return false;

    }

    Block cycle(LayoutBlock protect, LayoutBlock face) {
        for (int i = 0; i < protect.getNumberOfThroughPaths(); i++) {
            if (protect.isThroughPathActive(i)) {
                if (protect.getThroughPathSource(i) == face.getBlock()) {
                    jmri.Block found = protect.getThroughPathDestination(i);
                    if (found.getState() == jmri.Block.UNOCCUPIED && !lbm.getLayoutBlock(found).getUseExtraColor()) {
                        return found;
                    }
                }
            }
        }
        return null;
    }
}
