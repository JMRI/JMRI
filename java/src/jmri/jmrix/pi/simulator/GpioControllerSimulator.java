package jmri.jmrix.pi.simulator;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.trigger.GpioTrigger;

import java.util.Collection;

/**
 * Simulates a Raspberry Pi.
 *
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class GpioControllerSimulator implements GpioController {

    @Override
    public void export(PinMode pm, PinState ps, GpioPin... gps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void export(PinMode pm, GpioPin... gps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isExported(GpioPin... gps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void unexport(Pin... pins) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void unexport(GpioPin... gps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void unexportAll() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setMode(PinMode pm, GpioPin... gps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public PinMode getMode(GpioPin gp) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isMode(PinMode pm, GpioPin... gps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setPullResistance(PinPullResistance ppr, GpioPin... gps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public PinPullResistance getPullResistance(GpioPin gp) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isPullResistance(PinPullResistance ppr, GpioPin... gps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void high(GpioPinDigitalOutput... gpdos) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isHigh(GpioPinDigital... gpds) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void low(GpioPinDigitalOutput... gpdos) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isLow(GpioPinDigital... gpds) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setState(PinState ps, GpioPinDigitalOutput... gpdos) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setState(boolean bln, GpioPinDigitalOutput... gpdos) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isState(PinState ps, GpioPinDigital... gpds) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public PinState getState(GpioPinDigital gpd) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void toggle(GpioPinDigitalOutput... gpdos) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void pulse(long l, GpioPinDigitalOutput... gpdos) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setValue(double d, GpioPinAnalogOutput... gpaos) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public double getValue(GpioPinAnalog gpa) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void addListener(GpioPinListener gl, GpioPinInput... gpis) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void addListener(GpioPinListener[] gls, GpioPinInput... gpis) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removeListener(GpioPinListener gl, GpioPinInput... gpis) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removeListener(GpioPinListener[] gls, GpioPinInput... gpis) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removeAllListeners() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void addTrigger(GpioTrigger gt, GpioPinInput... gpis) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void addTrigger(GpioTrigger[] gts, GpioPinInput... gpis) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removeTrigger(GpioTrigger gt, GpioPinInput... gpis) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removeTrigger(GpioTrigger[] gts, GpioPinInput... gpis) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removeAllTriggers() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider gp, Pin pin, String string, PinMode pm, PinPullResistance ppr) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider gp, Pin pin, PinMode pm, PinPullResistance ppr) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider gp, Pin pin, String string, PinMode pm) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(GpioProvider gp, Pin pin, PinMode pm) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, String string, PinMode pm, PinPullResistance ppr) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, PinMode pm, PinPullResistance ppr) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, String string, PinMode pm) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalMultipurpose provisionDigitalMultipurposePin(Pin pin, PinMode pm) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider gp, Pin pin, String string, PinPullResistance ppr) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider gp, Pin pin, PinPullResistance ppr) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider gp, Pin pin, String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(GpioProvider gp, Pin pin) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(Pin pin, String string, PinPullResistance ppr) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(Pin pin, PinPullResistance ppr) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(Pin pin, String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalInput provisionDigitalInputPin(Pin pin) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider gp, Pin pin, String string, PinState ps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider gp, Pin pin, PinState ps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider gp, Pin pin, String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(GpioProvider gp, Pin pin) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, String string, PinState ps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, PinState ps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin, String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinDigitalOutput provisionDigitalOutputPin(Pin pin) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinAnalogInput provisionAnalogInputPin(GpioProvider gp, Pin pin, String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinAnalogInput provisionAnalogInputPin(GpioProvider gp, Pin pin) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinAnalogInput provisionAnalogInputPin(Pin pin, String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinAnalogInput provisionAnalogInputPin(Pin pin) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider gp, Pin pin, String string, double d) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider gp, Pin pin, double d) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider gp, Pin pin, String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(GpioProvider gp, Pin pin) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin, String string, double d) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin, double d) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin, String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinAnalogOutput provisionAnalogOutputPin(Pin pin) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider gp, Pin pin, String string, int i) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider gp, Pin pin, int i) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider gp, Pin pin, String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(GpioProvider gp, Pin pin) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(Pin pin, String string, int i) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(Pin pin, int i) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(Pin pin, String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionPwmOutputPin(Pin pin) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider gp, Pin pin, String string, int i) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider gp, Pin pin, int i) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider gp, Pin pin, String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(GpioProvider gp, Pin pin) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin, String string, int i) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin, int i) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin, String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPinPwmOutput provisionSoftPwmOutputPin(Pin pin) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPin provisionPin(GpioProvider gp, Pin pin, String string, PinMode pm, PinState ps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPin provisionPin(GpioProvider gp, Pin pin, String string, PinMode pm) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPin provisionPin(GpioProvider gp, Pin pin, PinMode pm) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPin provisionPin(Pin pin, String string, PinMode pm) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPin provisionPin(Pin pin, PinMode pm) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setShutdownOptions(GpioPinShutdown gps, GpioPin... gps1) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setShutdownOptions(Boolean bln, GpioPin... gps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setShutdownOptions(Boolean bln, PinState ps, GpioPin... gps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setShutdownOptions(Boolean bln, PinState ps, PinPullResistance ppr, GpioPin... gps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setShutdownOptions(Boolean bln, PinState ps, PinPullResistance ppr, PinMode pm, GpioPin... gps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Collection<GpioPin> getProvisionedPins() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPin getProvisionedPin(Pin pin) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public GpioPin getProvisionedPin(String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void unprovisionPin(GpioPin... gps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isShutdown() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported");
    }

}
