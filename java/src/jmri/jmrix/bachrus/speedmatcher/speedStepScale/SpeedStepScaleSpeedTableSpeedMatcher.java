package jmri.jmrix.bachrus.speedmatcher.speedStepScale;

import jmri.jmrix.bachrus.Speed;

/**
 *
 * @author Todd Wegter Copyright (C) 2024
 */
public class SpeedStepScaleSpeedTableSpeedMatcher extends SpeedStepScaleSpeedMatcher {

    //<editor-fold defaultstate="collapsed" desc="Constants">
    private final int INITIAL_STEP1 = 1;
    private final int INITIAL_STEP28 = 255;
    private final int INITIAL_TRIM = 128;

    private final int TOP_SPEED_STEP_MAX = 255;
    private final int STEP1_MIN = 1;
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Enums">
    protected enum SpeedMatcherState {
        IDLE,
        WAIT_FOR_THROTTLE,
        INIT_THROTTLE,
        INIT_ACCEL,
        INIT_DECEL,
        INIT_SPEED_TABLE,
        INIT_FORWARD_TRIM,
        INIT_REVERSE_TRIM,
        POST_INIT,
        FORWARD_WARM_UP,
        READ_MAX_SPEED,
        SET_UPPER_SPEED_STEPS,
        FORWARD_SPEED_MATCH,
        POST_SPEED_MATCH,
        REVERSE_WARM_UP,
        REVERSE_SPEED_MATCH_TRIM,
        COMPLETE,
        USER_STOPPED,
        CLEAN_UP,
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Instance Variables">
    private SpeedTableStep initSpeedTableStep;
    private int initSpeedTableStepValue;
    
    private SpeedTableStep speedMatchSpeedTableStep;

    private int speedMatchCVValue = INITIAL_STEP28;
    private int lastSpeedMatchCVValue = INITIAL_STEP28;

    private int reverseTrimValue = INITIAL_TRIM;
    private int lastReverseTrimValue = INITIAL_TRIM;    
    //</editor-fold>
    
    
    public SpeedStepScaleSpeedTableSpeedMatcher(SpeedStepScaleSpeedMatcherConfig config) {
        super(config);
    }

    @Override
    public boolean startSpeedMatcher() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stopSpeedMatcher() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSpeedMatcherIdle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void programmingOpReply(int value, int status) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
