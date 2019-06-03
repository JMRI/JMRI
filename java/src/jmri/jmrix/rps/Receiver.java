package jmri.jmrix.rps;

import javax.vecmath.Point3d;

/**
 * Holds all the state information for a single receiver.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class Receiver {

    public Receiver(Point3d position) {
        this.position = position;
    }

    public void setPosition(Point3d position) {
        this.position = position;
    }

    public Point3d getPosition() {
        return position;
    }
    private Point3d position;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    boolean active = false;

    int last = -1;

    public int getLastTime() {
        return last;
    }

    public void setLastTime(int m) {
        last = m;
    }

    int min = 0;

    public int getMinTime() {
        return min;
    }

    public void setMinTime(int m) {
        min = m;
    }

    int max = 99999;

    public int getMaxTime() {
        return max;
    }

    public void setMaxTime(int m) {
        max = m;
    }

}
