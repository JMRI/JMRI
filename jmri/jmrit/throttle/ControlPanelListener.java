package jmri.jmrit.throttle;

public interface ControlPanelListener extends java.util.EventListener
{
        public void notifySpeedChanged(int speed);
        public void notifyDirectionChanged(boolean isForward);

}