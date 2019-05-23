/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.bachrus;

/**
 * Useful stuff for speed conversion
 *
 * @author Andrew Crosland Copyright (C) 2010
 */
public class Speed {

    static final int MPH = 0;
    static final int KPH = 1;

    static final float MPH_KPH_FACTOR = 1.609344F;
    static final float MPH_TO_KPH = MPH_KPH_FACTOR;
    static final float KPH_TO_MPH = 1 / MPH_KPH_FACTOR;

    public static float mphToKph(float m) {
        return m * MPH_TO_KPH;
    }

    public static float kphToMph(float k) {
        return k * KPH_TO_MPH;
    }
}
