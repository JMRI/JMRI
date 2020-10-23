package jmri.jmrit.logixng.tools;

import java.util.Arrays;

import jmri.*;
import jmri.implementation.VirtualSignalHead;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 This test tests expression signalMast
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class ImportExpressionSignalMastTest extends ImportExpressionComplexTestBase {

    SignalHead signalHead;
    SignalMast signalMast;
    ConditionalVariable cv;
    
    private enum SignalMastEnum {
        EqualsClear(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Approach Medium", "Medium Clear", "Clear"),
        EqualsApproachMedium(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Medium Clear", "Approach Medium"),
        EqualsMediumClear(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Approach Medium", "Medium Clear"),
        EqualsMediumApproachSlow(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Approach Medium", "Medium Approach Slow"),
        EqualsApproachSlow(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Approach Medium", "Approach Slow"),
        EqualsApproach(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Approach Medium", "Approach"),
        EqualsMediumApproach(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Approach Medium", "Medium Approach"),
        EqualsSlowClear(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Approach Medium", "Slow Clear"),
        EqualsSlowApproach(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Approach Medium", "Slow Approach"),
        EqualsPermissive(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Approach Medium", "Permissive"),
        EqualsPermissiveMedium(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Approach Medium", "Permissive Medium"),
        EqualsRestricting(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Approach Medium", "Restricting"),
        EqualsStopAndProceed(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Approach Medium", "Stop and Proceed"),
        EqualsStopAndProceedMedium(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Approach Medium", "Stop and Proceed Medium"),
        EqualsStop(Conditional.Type.SIGNAL_MAST_ASPECT_EQUALS, "Clear", "Approach Medium", "Stop"),
        SIGNAL_MAST_LIT(Conditional.Type.SIGNAL_MAST_LIT, null, null, null),
        SIGNAL_MAST_HELD(Conditional.Type.SIGNAL_MAST_HELD, null, null, null);
        
        private final Conditional.Type type;
        private final String initAspect;
        private final String failAspect;
        private final String successAspect;
        
        private SignalMastEnum(Conditional.Type type, String initAspect, String failAspect, String successAspect) {
            this.type = type;
            this.initAspect = initAspect;
            this.failAspect = failAspect;
            this.successAspect = successAspect;
        }
    }
    
    @Override
    public Enum[] getEnums() {
        return SignalMastEnum.values();
    }
    
    @Override
    public void setNamedBeanState(Enum e, Setup setup) throws JmriException {
        SignalMastEnum me = SignalMastEnum.valueOf(e.name());
        
        cv.setType(me.type);
        cv.setDataString(me.successAspect);
        
        switch (me) {
            case EqualsClear:
            case EqualsApproachMedium:
            case EqualsMediumClear:
            case EqualsMediumApproachSlow:
            case EqualsApproachSlow:
            case EqualsApproach:
            case EqualsMediumApproach:
            case EqualsSlowClear:
            case EqualsSlowApproach:
            case EqualsPermissive:
            case EqualsPermissiveMedium:
            case EqualsRestricting:
            case EqualsStopAndProceed:
            case EqualsStopAndProceedMedium:
            case EqualsStop:
                switch (setup) {
                    case Init: signalMast.setAspect(me.initAspect); break;
                    case Fail1:
                    case Fail2:
                    case Fail3: signalMast.setAspect(me.failAspect); break;
                    case Succeed1:
                    case Succeed2:
                    case Succeed3:
                    case Succeed4: signalMast.setAspect(me.successAspect); break;
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            case SIGNAL_MAST_LIT:
                switch (setup) {
                    case Init: signalMast.setLit(false); break;
                    case Fail1:
                    case Fail2:
                    case Fail3: signalMast.setLit(false); break;
                    case Succeed1:
                    case Succeed2:
                    case Succeed3:
                    case Succeed4: signalMast.setLit(true); break;
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            case SIGNAL_MAST_HELD:
                switch (setup) {
                    case Init: signalMast.setHeld(false); break;
                    case Fail1:
                    case Fail2:
                    case Fail3: signalMast.setHeld(false); break;
                    case Succeed1:
                    case Succeed2:
                    case Succeed3:
                    case Succeed4: signalMast.setHeld(true); break;
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
            
            default:
                throw new RuntimeException("Unknown enum: "+e.name());
        }
    }
    
    @Override
    public ConditionalVariable newConditionalVariable() {
        // Note that the signal head IH1 created here are also used to test the signal mast.
        signalHead = new MySignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHead);
        
        // The signal head IH1 created above is also used here in signal mast IF$shsm:AAR-1946:CPL(IH1)
        signalMast = InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        
        cv = new ConditionalVariable();
        cv.setName("IF$shsm:AAR-1946:CPL(IH1)");
        return cv;
    }
    
    
    
    private static class MySignalHead extends VirtualSignalHead {
        
        public MySignalHead(String sys, String user) {
            super(sys, user);
        }

        public MySignalHead(String sys) {
            super(sys);
        }

        final static private int[] VALID_STATES = new int[]{
            DARK,
            RED,
            YELLOW,
            GREEN,
            LUNAR,
            FLASHRED,
            FLASHYELLOW,
            FLASHGREEN,
            FLASHLUNAR,
        }; // No int for Lunar

        final static private String[] VALID_STATE_KEYS = new String[]{
            "SignalHeadStateDark",
            "SignalHeadStateRed",
            "SignalHeadStateYellow",
            "SignalHeadStateGreen",
            "SignalHeadStateLunar",
            "SignalHeadStateFlashingRed",
            "SignalHeadStateFlashingYellow",
            "SignalHeadStateFlashingGreen",
            "SignalHeadStateFlashingLunar",
        };

        /**
         * {@inheritDoc}
         */
        @Override
        public int[] getValidStates() {
            return Arrays.copyOf(VALID_STATES, VALID_STATES.length);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String[] getValidStateKeys() {
            return Arrays.copyOf(VALID_STATE_KEYS, VALID_STATE_KEYS.length);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String[] getValidStateNames() {
            String[] stateNames = new String[VALID_STATE_KEYS.length];
            int i = 0;
            for (String stateKey : VALID_STATE_KEYS) {
                stateNames[i++] = stateKey;     // No need to use Bundle for this test
            }
            return stateNames;
        }
    }
    
}
