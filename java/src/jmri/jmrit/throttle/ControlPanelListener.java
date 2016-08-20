package jmri.jmrit.throttle;

/**
 *
 * @author glen Copyright (C) 2002
 */
public interface ControlPanelListener extends java.util.EventListener {

    public void notifySpeedChanged(int speed);

    public void notifyDirectionChanged(boolean isForward);

}
