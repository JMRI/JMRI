package jmri.jmrix.pi.simulator;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.trigger.GpioTrigger;

import java.util.*;
/**
 * Simulates GpioPinDigitalInput.
 *
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class GpioPinDigitalInputSimulator implements GpioPinDigitalInput {

//    private final Pin pin;
    private String name;
//    private final PinPullResistance ppr;
    private PinState pinState = PinState.LOW;
    private PinMode pinMode = PinMode.DIGITAL_INPUT;
    private PinPullResistance pinPullResistance = PinPullResistance.OFF;

    private final List<GpioPinListener> listeners = new ArrayList<>();

    public GpioPinDigitalInputSimulator(Pin pin, String string, PinPullResistance ppr) {
//        this.pin = pin;
        this.name = string;
//        this.ppr = ppr;
    }

    @Override
    public boolean hasDebounce(PinState ps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getDebounce(PinState ps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setDebounce(int i) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setDebounce(int i, PinState... pss) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isHigh() {
        return pinState.isHigh();
    }

    @Override
    public boolean isLow() {
        return pinState.isLow();
    }

    @Override
    public PinState getState() {
        return pinState;
    }

    @Override
    public boolean isState(PinState ps) {
        return pinState == ps;
    }

    @Override
    public GpioProvider getProvider() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Pin getPin() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setName(String string) {
        name = string;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setTag(Object o) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object getTag() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setProperty(String string, String string1) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean hasProperty(String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getProperty(String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getProperty(String string, String string1) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Map<String, String> getProperties() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removeProperty(String string) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void clearProperties() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void export(PinMode pm) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void export(PinMode pm, PinState ps) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void unexport() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isExported() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setMode(PinMode pm) {
        this.pinMode = pm;
        // We should probably call all the listeners here????
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public PinMode getMode() {
        return pinMode;
    }

    @Override
    public boolean isMode(PinMode pm) {
        return pinMode == pm;
    }

    @Override
    public void setPullResistance(PinPullResistance ppr) {
        pinPullResistance = ppr;
    }

    @Override
    public PinPullResistance getPullResistance() {
        return pinPullResistance;
    }

    @Override
    public boolean isPullResistance(PinPullResistance ppr) {
        return pinPullResistance == ppr;
    }

    @Override
    public Collection<GpioPinListener> getListeners() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void addListener(GpioPinListener... gls) {
        for (GpioPinListener gl : gls) {
            listeners.add(gl);
        }
    }

    @Override
    public void addListener(List<? extends GpioPinListener> list) {
        listeners.addAll(list);
    }

    @Override
    public boolean hasListener(GpioPinListener... gls) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removeListener(GpioPinListener... gls) {
        for (GpioPinListener gl : gls) {
            listeners.remove(gl);
        }
    }

    @Override
    public void removeListener(List<? extends GpioPinListener> list) {
        listeners.removeAll(list);
    }

    @Override
    public void removeAllListeners() {
        listeners.clear();
    }

    @Override
    public GpioPinShutdown getShutdownOptions() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setShutdownOptions(GpioPinShutdown gps) {
//        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setShutdownOptions(Boolean bln) {
//        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setShutdownOptions(Boolean bln, PinState ps) {
//        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setShutdownOptions(Boolean bln, PinState ps, PinPullResistance ppr) {
//        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setShutdownOptions(Boolean bln, PinState ps, PinPullResistance ppr, PinMode pm) {
//        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Collection<GpioTrigger> getTriggers() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void addTrigger(GpioTrigger... gts) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void addTrigger(List<? extends GpioTrigger> list) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removeTrigger(GpioTrigger... gts) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removeTrigger(List<? extends GpioTrigger> list) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void removeAllTriggers() {
        throw new UnsupportedOperationException("Not supported");
    }

}
