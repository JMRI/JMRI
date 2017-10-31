package jmri.jmrix.anyma_dmx;

public interface DMX_Controller {

//    public void export(PinMode pm, PinState ps, GpioPin[] gps);
//
//    public void export(PinMode pm, GpioPin[] gps);
//
//    public boolean isExported(GpioPin[] gps);
//
//    public void unexport(Pin[] pins);
//
//    public void unexport(GpioPin[] gps);
//
//    public void unexportAll();
//
//    public void setMode(PinMode pm, GpioPin[] gps);
//
//    public PinMode getMode(GpioPin gp);
//
//    public boolean isMode(PinMode pm, GpioPin[] gps);
//
//    public void setPullResistance(PinPullResistance ppr, GpioPin[] gps);
//
//    public PinPullResistance getPullResistance(GpioPin gp);
//
//    public boolean isPullResistance(PinPullResistance ppr, GpioPin[] gps);
//
//    public void high(GpioPinDigitalOutput[] gpdos);
//
//    public boolean isHigh(GpioPinDigital[] gpds);
//
//    public void low(GpioPinDigitalOutput[] gpdos);
//
//    public boolean isLow(GpioPinDigital[] gpds);
//
//    public void setState(PinState ps, GpioPinDigitalOutput[] gpdos);
//
//    public void setState(boolean bln, GpioPinDigitalOutput[] gpdos);
//
//    public boolean isState(PinState ps, GpioPinDigital[] gpds);
//
//    public PinState getState(GpioPinDigital gpd);
//
//    public void toggle(GpioPinDigitalOutput[] gpdos);
//
//    public void pulse(long l, GpioPinDigitalOutput[] gpdos);
//
//    public void setValue(double d, GpioPinAnalogOutput[] gpaos);
//
//    public double getValue(GpioPinAnalog gpa);
//
//    public void addListener(GpioPinListener gl, GpioPinInput[] gpis);
//
//    public void addListener(GpioPinListener[] gls, GpioPinInput[] gpis);
//
//    public void removeListener(GpioPinListener gl, GpioPinInput[] gpis);
//
//    public void removeListener(GpioPinListener[] gls, GpioPinInput[] gpis);
//
//    public void removeAllListeners();
//
//    public void addTrigger(GpioTrigger gt, GpioPinInput[] gpis);
//
//    public void addTrigger(GpioTrigger[] gts, GpioPinInput[] gpis);
//
//    public void removeTrigger(GpioTrigger gt, GpioPinInput[] gpis);
//
//    public void removeTrigger(GpioTrigger[] gts, GpioPinInput[] gpis);
//
//    public void removeAllTriggers();
//
//    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider gp, Pin pin, String string, PinMode pm, PinPullResistance ppr);
//
//    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider gp, Pin pin, PinMode pm, PinPullResistance ppr);
//
//    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider gp, Pin pin, String string, PinMode pm);
//
//    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider gp, Pin pin, PinMode pm);
//
//    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, String string, PinMode pm, PinPullResistance ppr);
//
//    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, PinMode pm, PinPullResistance ppr);
//
//    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, String string, PinMode pm);
//
//    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, PinMode pm);
//
//    public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider gp, Pin pin, String string, PinPullResistance ppr);
//
//    public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider gp, Pin pin, PinPullResistance ppr);
//
//    public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider gp, Pin pin, String string);
//
//    public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider gp, Pin pin);
//
//    public GpioPinDigitalInput provisionDigitalInputPin(Pin pin, String string, PinPullResistance ppr);
//
//    public GpioPinDigitalInput provisionDigitalInputPin(Pin pin, PinPullResistance ppr);
//
//    public GpioPinDigitalInput provisionDigitalInputPin(Pin pin, String string);
//
//    public GpioPinDigitalInput provisionDigitalInputPin(Pin pin);
//
//    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider gp, Pin pin, String string, PinState ps);
//
//    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider gp, Pin pin, PinState ps);
//
//    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider gp, Pin pin, String string);
//
//    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider gp, Pin pin);
//
//    public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, String string, PinState ps);
//
//    public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, PinState ps);
//
//    public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, String string);
//
//    public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin);
//
//    public GpioPinAnalogInput provisionAnalogInputPin(GpioProvider gp, Pin pin, String string);
//
//    public GpioPinAnalogInput provisionAnalogInputPin(GpioProvider gp, Pin pin);
//
//    public GpioPinAnalogInput provisionAnalogInputPin(Pin pin, String string);
//
//    public GpioPinAnalogInput provisionAnalogInputPin(Pin pin);
//
//    public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider gp, Pin pin, String string, double d);
//
//    public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider gp, Pin pin, double d);
//
//    public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider gp, Pin pin, String string);
//
//    public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider gp, Pin pin);
//
//    public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin, String string, double d);
//
//    public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin, double d);
//
//    public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin, String string);
//
//    public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin);
//
//    public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider gp, Pin pin, String string, int i);
//
//    public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider gp, Pin pin, int i);
//
//    public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider gp, Pin pin, String string);
//
//    public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider gp, Pin pin);
//
//    public GpioPinPwmOutput provisionPwmOutputPin(Pin pin, String string, int i);
//
//    public GpioPinPwmOutput provisionPwmOutputPin(Pin pin, int i);
//
//    public GpioPinPwmOutput provisionPwmOutputPin(Pin pin, String string);
//
//    public GpioPinPwmOutput provisionPwmOutputPin(Pin pin);
//
//    public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider gp, Pin pin, String string, int i);
//
//    public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider gp, Pin pin, int i);
//
//    public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider gp, Pin pin, String string);
//
//    public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider gp, Pin pin);
//
//    public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin, String string, int i);
//
//    public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin, int i);
//
//    public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin, String string);
//
//    public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin);
//
//    public GpioPin provisionPin(GpioProvider gp, Pin pin, String string, PinMode pm, PinState ps);
//
//    public GpioPin provisionPin(GpioProvider gp, Pin pin, String string, PinMode pm);
//
//    public GpioPin provisionPin(GpioProvider gp, Pin pin, PinMode pm);
//
//    public GpioPin provisionPin(Pin pin, String string, PinMode pm);
//
//    public GpioPin provisionPin(Pin pin, PinMode pm);
//
//    public void setShutdownOptions(GpioPinShutdown gps, GpioPin[] gps1);
//
//    public void setShutdownOptions(Boolean bln, GpioPin[] gps);
//
//    public void setShutdownOptions(Boolean bln, PinState ps, GpioPin[] gps);
//
//    public void setShutdownOptions(Boolean bln, PinState ps, PinPullResistance ppr, GpioPin[] gps);
//
//    public void setShutdownOptions(Boolean bln, PinState ps, PinPullResistance ppr, PinMode pm, GpioPin[] gps);
//
//    public Collection<GpioPin> getProvisionedPins();
//
//    public GpioPin getProvisionedPin(Pin pin);
//
//    public GpioPin getProvisionedPin(String string);
//
//    public void unprovisionPin(GpioPin[] gps);

    public boolean isShutdown();

    public void shutdown();
}
