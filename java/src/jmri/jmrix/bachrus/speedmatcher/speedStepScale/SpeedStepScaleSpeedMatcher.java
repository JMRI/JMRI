package jmri.jmrix.bachrus.speedmatcher.speedStepScale;

import jmri.jmrix.bachrus.speedmatcher.SpeedMatcher;
import jmri.jmrix.bachrus.speedmatcher.SpeedMatcherConfig;

/**
 *
 * @author Todd Wegter
 */
public abstract class SpeedStepScaleSpeedMatcher extends SpeedMatcher {

    //<editor-fold defaultstate="collapsed" desc="Instance Variables">

    //</editor-fold>

    public SpeedStepScaleSpeedMatcher(SpeedMatcherConfig config) {
        super(config);
    }

     //<editor-fold defaultstate="collapsed" desc="Protected APIs">
    @Override
    protected boolean validate() {
         throw new UnsupportedOperationException("Not supported yet."); //TODO: speedStepScale validate 
    }
    //</editor-fold>

}