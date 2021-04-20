package jmri.jmrit.logixng.tools;

import jmri.*;
import jmri.jmrit.logix.OBlock;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 This test tests expression oblock
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class ImportExpressionOBlockTest extends ImportExpressionComplexTestBase {

    OBlock oblock;
    ConditionalVariable cv;
    
    private enum OBlockEnum {
        EqualsUnoccupied(OBlock.OBlockStatus.TrackError, OBlock.OBlockStatus.Occupied, OBlock.OBlockStatus.Unoccupied),
        EqualsOccupied(OBlock.OBlockStatus.TrackError, OBlock.OBlockStatus.Unoccupied, OBlock.OBlockStatus.Occupied),
        EqualsAllocated(OBlock.OBlockStatus.TrackError, OBlock.OBlockStatus.Occupied, OBlock.OBlockStatus.Allocated),
        EqualsRunning(OBlock.OBlockStatus.TrackError, OBlock.OBlockStatus.Occupied, OBlock.OBlockStatus.Running),
        EqualsOutOfService(OBlock.OBlockStatus.TrackError, OBlock.OBlockStatus.Occupied, OBlock.OBlockStatus.OutOfService),
        EqualsDark(OBlock.OBlockStatus.TrackError, OBlock.OBlockStatus.Occupied, OBlock.OBlockStatus.Dark),
        EqualsTrackError(OBlock.OBlockStatus.Dark, OBlock.OBlockStatus.Occupied, OBlock.OBlockStatus.TrackError);
        
        private final OBlock.OBlockStatus initOBlockStatus;
        private final OBlock.OBlockStatus failOBlockStatus;
        private final OBlock.OBlockStatus successOBlockStatus;
        
        private OBlockEnum(OBlock.OBlockStatus initOBlockStatus, OBlock.OBlockStatus failOBlockStatus, OBlock.OBlockStatus successOBlockStatus) {
            this.initOBlockStatus = initOBlockStatus;
            this.failOBlockStatus = failOBlockStatus;
            this.successOBlockStatus = successOBlockStatus;
        }
    }
    
    @Override
    public Enum[] getEnums() {
        return OBlockEnum.values();
    }
    
    @Override
    public void setNamedBeanState(Enum e, Setup setup) throws JmriException {
        OBlockEnum me = OBlockEnum.valueOf(e.name());
        
        cv.setType(Conditional.Type.BLOCK_STATUS_EQUALS);
        cv.setDataString(me.successOBlockStatus.getName());
        
        switch (me) {
            case EqualsUnoccupied:
            case EqualsOccupied:
            case EqualsAllocated:
            case EqualsRunning:
            case EqualsOutOfService:
            case EqualsDark:
            case EqualsTrackError:
                switch (setup) {
                    case Init: oblock.setState(me.initOBlockStatus.getStatus()); break;
                    case Fail1:
                    case Fail2:
                    case Fail3: oblock.setState(me.failOBlockStatus.getStatus()); break;
                    case Succeed1:
                    case Succeed2:
                    case Succeed3:
                    case Succeed4: oblock.setState(me.successOBlockStatus.getStatus()); break;
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            default:
                throw new RuntimeException("Unknown enum: "+e.name());
        }
    }
    
    @Override
    public ConditionalVariable newConditionalVariable() {
        oblock = new OBlock("OB99");
        InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).register(oblock);
        cv = new ConditionalVariable();
        cv.setName("OB99");
        return cv;
    }
    
}
