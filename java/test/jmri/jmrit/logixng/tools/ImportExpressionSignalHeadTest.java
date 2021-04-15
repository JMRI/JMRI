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
 This test tests expression signalHead
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class ImportExpressionSignalHeadTest extends ImportExpressionComplexTestBase {

    SignalHead signalHead;
    ConditionalVariable cv;
    
    private enum SignalHeadEnum {
        EqualsDARK(Conditional.Type.SIGNAL_HEAD_DARK, SignalHead.RED, SignalHead.FLASHRED, SignalHead.DARK),
        EqualsRED(Conditional.Type.SIGNAL_HEAD_RED, SignalHead.DARK, SignalHead.DARK, SignalHead.RED),
        EqualsFLASHRED(Conditional.Type.SIGNAL_HEAD_FLASHRED, SignalHead.DARK, SignalHead.RED, SignalHead.FLASHRED),
        EqualsYELLOW(Conditional.Type.SIGNAL_HEAD_YELLOW, SignalHead.DARK, SignalHead.RED, SignalHead.YELLOW),
        EqualsFLASHYELLOW(Conditional.Type.SIGNAL_HEAD_FLASHYELLOW, SignalHead.RED, SignalHead.DARK, SignalHead.FLASHYELLOW),
        EqualsGREEN(Conditional.Type.SIGNAL_HEAD_GREEN, SignalHead.DARK, SignalHead.RED, SignalHead.GREEN),
        EqualsFLASHGREEN(Conditional.Type.SIGNAL_HEAD_FLASHGREEN, SignalHead.DARK, SignalHead.RED, SignalHead.FLASHGREEN),
        EqualsLUNAR(Conditional.Type.SIGNAL_HEAD_LUNAR, SignalHead.DARK, SignalHead.RED, SignalHead.LUNAR),
        EqualsFLASHLUNAR(Conditional.Type.SIGNAL_HEAD_FLASHLUNAR, SignalHead.DARK, SignalHead.RED, SignalHead.FLASHLUNAR),
        IsLit(Conditional.Type.SIGNAL_HEAD_LIT, -1, -1, -1),
        IsHeld(Conditional.Type.SIGNAL_HEAD_HELD, -1, -1, -1);
        
        private Conditional.Type type;
        private int initAppearance;
        private int failAppearance;
        private int successAppearance;
        
        private SignalHeadEnum(Conditional.Type type, int initAppearance, int failAppearance, int successAppearance) {
            this.type = type;
            this.initAppearance = initAppearance;
            this.failAppearance = failAppearance;
            this.successAppearance = successAppearance;
        }
    }
    
    @Override
    public Enum[] getEnums() {
        return SignalHeadEnum.values();
    }
    
    @Override
    public void setNamedBeanState(Enum e, Setup setup) throws JmriException {
        SignalHeadEnum me = SignalHeadEnum.valueOf(e.name());
        
        cv.setType(me.type);
        
        switch (me) {
            case EqualsDARK:
            case EqualsRED:
            case EqualsFLASHRED:
            case EqualsYELLOW:
            case EqualsFLASHYELLOW:
            case EqualsGREEN:
            case EqualsFLASHGREEN:
            case EqualsLUNAR:
            case EqualsFLASHLUNAR:
                switch (setup) {
                    case Init: signalHead.setAppearance(me.initAppearance); break;
                    case Fail1:
                    case Fail2:
                    case Fail3: signalHead.setAppearance(me.failAppearance); break;
                    case Succeed1:
                    case Succeed2:
                    case Succeed3:
                    case Succeed4: signalHead.setAppearance(me.successAppearance); break;
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
//                System.out.format("setNamedBeanState: %s, %s, %s%n", me.name(), setup.name(), signalHead.getAppearanceKey());
                break;
                
            case IsLit:
                switch (setup) {
                    case Init: signalHead.setLit(false); break;
                    case Fail1:
                    case Fail2:
                    case Fail3: signalHead.setLit(false); break;
                    case Succeed1:
                    case Succeed2:
                    case Succeed3:
                    case Succeed4: signalHead.setLit(true); break;
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            case IsHeld:
                switch (setup) {
                    case Init: signalHead.setHeld(false); break;
                    case Fail1:
                    case Fail2:
                    case Fail3: signalHead.setHeld(false); break;
                    case Succeed1:
                    case Succeed2:
                    case Succeed3:
                    case Succeed4: signalHead.setHeld(true); break;
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
            
            default:
                throw new RuntimeException("Unknown enum: "+e.name());
        }
    }
    
    @Override
    public ConditionalVariable newConditionalVariable() {
        signalHead = new MySignalHead("IH1");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHead);
        cv = new ConditionalVariable();
        cv.setName("IH1");
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
