package jmri.jmrit.throttle;


public interface FunctionListener extends java.util.EventListener
{
    public void notifyFunctionStateChanged(int functionNumber, boolean isOn);
}