package jmri.jmrix.anyma_dmx;

public interface AnymaDMX_Provider {

    public String getName();

//    public boolean hasPin(Pin pin);
//
//    public void export(Pin pin, PinMode pm, PinState ps);
//
//    public void export(Pin pin, PinMode pm);
//
//    public boolean isExported(Pin pin);
//
//    public void unexport(Pin pin);
//
//    public void setMode(Pin pin, PinMode pm);
//
//    public PinMode getMode(Pin pin);
//
//    public void setPullResistance(Pin pin, PinPullResistance ppr);
//
//    public PinPullResistance getPullResistance(Pin pin);
//
//    public void setState(Pin pin, PinState ps);
//
//    public PinState getState(Pin pin);
//
//    public void setValue(Pin pin, double d);
//
//    public double getValue(Pin pin);
//
//    public void setPwm(Pin pin, int i);
//
//    public void setPwmRange(Pin pin, int i);
//
//    public int getPwm(Pin pin);
//
//    public void addListener(Pin pin, PinListener pl);
//
//    public void removeListener(Pin pin, PinListener pl);
//
//    public void removeAllListeners();

    public void shutdown();

    public boolean isShutdown();
}
