package jmri.jmrit.logixng.tools;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import jmri.*;
import jmri.implementation.VirtualSignalHead;
import jmri.jmrit.logix.*;
import jmri.util.JUnitUtil;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 This test tests expression warrant
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class ImportExpressionWarrantTest extends ImportExpressionComplexTestBase {

    OBlockManager blkMgr;
    Warrant warrant;
    OBlock block1;
    OPath path1;
    DccLocoAddress address;
    ConditionalVariable cv;
    
    private enum WarrantEnum {
        ROUTE_FREE(Conditional.Type.ROUTE_FREE),
        ROUTE_OCCUPIED(Conditional.Type.ROUTE_OCCUPIED),
        ROUTE_ALLOCATED(Conditional.Type.ROUTE_ALLOCATED),
        ROUTE_SET(Conditional.Type.ROUTE_SET),
        TRAIN_RUNNING(Conditional.Type.TRAIN_RUNNING);
        
        private final Conditional.Type type;
        
        private WarrantEnum(Conditional.Type type) {
            this.type = type;
        }
        
    }
    
    @Override
    public Enum[] getEnums() {
        return WarrantEnum.values();
    }
    
    @Override
    public void setNamedBeanState(Enum e, Setup setup) throws JmriException {
        WarrantEnum me = WarrantEnum.valueOf(e.name());
        
        cv.setType(me.type);
        
        switch (me) {
            case ROUTE_FREE:
//                warrant._routeIsFree = expectSuccess;
                break;
            case ROUTE_OCCUPIED:
//                warrant._routeIsOccupied = expectSuccess;
                break;
            case ROUTE_ALLOCATED:
//                warrant._isAllocated = expectSuccess;
                break;
            case ROUTE_SET:
//                warrant._hasRouteSet = expectSuccess;
                break;
            case TRAIN_RUNNING:
                switch (setup) {
                    case Init:
                    case Fail1:
                    case Fail2:
                    case Fail3:
                        warrant.stopWarrant(true);
                        warrant.setRunMode(Warrant.MODE_NONE, address, null, null, false);
                        break;
                    case Succeed1:
                    case Succeed2:
                    case Succeed3:
                    case Succeed4:
                        warrant.stopWarrant(true);
                        block1.setValue(OBlock.OCCUPIED);
                        
                        ArrayList<BlockOrder> orders = new ArrayList<>();
                        orders.add(new BlockOrder(blkMgr.getOBlock("North"), "NorthToWest", "", "NorthWest"));
                        BlockOrder viaOrder = new BlockOrder(blkMgr.getOBlock("West"), "SouthToNorth", "NorthWest", "SouthWest");
                        orders.add(viaOrder);
                        BlockOrder lastOrder = new BlockOrder(blkMgr.getOBlock("South"), "SouthToWest", "SouthWest", null);
                        orders.add(lastOrder);
                        
                        warrant.setThrottleCommands(new ArrayList<>());
                        warrant.addThrottleCommand(new ThrottleSetting(0, "Speed", "0.0", "North"));
                        warrant.addThrottleCommand(new ThrottleSetting(10, "Speed", "0.4", "North"));
                        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "West"));
                        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.5", "West"));
                        warrant.addThrottleCommand(new ThrottleSetting(100, "NoOp", "Enter Block", "South"));
                        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.3", "South"));
                        warrant.addThrottleCommand(new ThrottleSetting(100, "Speed", "0.0", "South"));
                        
                        warrant.getSpeedUtil().setDccAddress("999(L)");
                        warrant.setBlockOrders(orders);
                        
                        warrant.setRoute(false, orders);
                        warrant.checkStartBlock();
                        warrant.checkRoute();
                        
                        warrant.setTrainName("TestTrain");
                        
                        
                        warrant.setRunMode(Warrant.MODE_RUN, address, null, null, false);
                        break;
//                    case Succeed3: warrant._runMode = Warrant.MODE_LEARN; break;
//                    case Succeed4: warrant._runMode = Warrant.MODE_RUN; break;
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            default:
                throw new RuntimeException("Unknown enum: "+e.name());
        }
    }
    
    @Override
    public ConditionalVariable newConditionalVariable() {
        JUnitUtil.initDebugThrottleManager();
        
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        
        warrant = new Warrant("IW1", null);
        warrant.setTrainName("T1");
        InstanceManager.getDefault(WarrantManager.class).register(warrant);
        blkMgr = new OBlockManager();
        blkMgr.createNewOBlock("OB1", "West");
        blkMgr.createNewOBlock("OB2", "East");
        blkMgr.createNewOBlock("OB3", "North");
        blkMgr.createNewOBlock("OB4", "South");
        block1 = blkMgr.createNewOBlock("OB102", "c");
        path1 = new OPath(block1, "path1");
        block1.addPath(path1);
        List<BlockOrder> orders = new ArrayList<>();
        orders.add(new BlockOrder(block1, "path1", null, null));
        warrant.setBlockOrders(orders);
        block1.allocate(warrant);
        address = new DccLocoAddress(300, true);
        cv = new ConditionalVariable();
        cv.setName("IW1");
        return cv;
    }
    
}
