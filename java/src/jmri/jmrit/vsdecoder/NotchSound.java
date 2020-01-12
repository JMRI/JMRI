package jmri.jmrit.vsdecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author Mark Underwood Copyright (C) 2011
 */
class NotchSound extends SoundBite {

    // Engine-notch specific values
    protected int next_notch, prev_notch;
    protected float accel_limit, decel_limit;
    protected SoundBite accel_sound, decel_sound;

    public NotchSound(VSDFile vf, String filename, String sname, String uname) {
        super(vf, filename, sname, uname);
    }

    public void setNextNotch(int n) {
        next_notch = n;
    }

    public void setNextNotch(String s) {
        if (s == null) {
            next_notch = 0;
            return;
        }
        try {
            next_notch = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            log.debug("Invalid integer: " + s);
            next_notch = 0;
        }
    }

    public void setPrevNotch(int n) {
        prev_notch = n;
    }

    public void setPrevNotch(String s) {
        if (s == null) {
            prev_notch = 0;
            return;
        }
        try {
            prev_notch = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            log.debug("Invalid integer: " + s);
            prev_notch = 0;
        }
    }

    public void setAccelLimit(float l) {
        accel_limit = l;
    }

    public void setAccelLimit(String s) {
        if (s == null) {
            accel_limit = 0.0f;
            return;
        }
        try {
            accel_limit = Float.parseFloat(s) / 100.0f;
        } catch (NumberFormatException e) {
            log.debug("Invalid float: " + s);
            accel_limit = 0;
        }
    }

    public void setDecelLimit(float l) {
        decel_limit = l;
    }

    public void setDecelLimit(String s) {
        if (s == null) {
            decel_limit = 0.0f;
            return;
        }
        try {
            decel_limit = Float.parseFloat(s) / 100.0f;
        } catch (NumberFormatException e) {
            log.debug("Invalid float: " + s);
            decel_limit = 0;
        }
    }

    public void setAccelSound(SoundBite s) {
        accel_sound = s;
    }

    public void setDecelSound(SoundBite s) {
        decel_sound = s;
    }

    public int getNextNotch() {
        return (next_notch);
    }

    public int getPrevNotch() {
        return (prev_notch);
    }

    public float getAccelLimit() {
        return (accel_limit);
    }

    public float getDecelLimit() {
        return (decel_limit);
    }

    public SoundBite getAccelSound() {
        return (accel_sound);
    }

    public SoundBite getDecelSound() {
        return (decel_sound);
    }

    private static final Logger log = LoggerFactory.getLogger(NotchSound.class);

}
