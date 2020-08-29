package jmri.implementation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;

/**
 * Default class for multi meter objects.
 *
 * @author Mark Underwood    (C) 2015
 * @author Daniel Bergqvist  (C) 2020
 */
public class DefaultMeterGroup extends AbstractNamedBean implements MeterGroup {

//    protected float current_float = 0.0f;
//    protected float voltage_float = 0.0f;
    
    private final Map<String, MeterInfo> _meters = new HashMap<>();

    public DefaultMeterGroup(@Nonnull String sys) throws NamedBean.BadSystemNameException {
        super(sys);
    }
    
    /**
     * Get a list of all the meters registered in this multimeter.
     * @return the list of meters
     */
    @Override
    public Collection<MeterInfo> getMeters() {
        return Collections.unmodifiableCollection(_meters.values());
    }
    
    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public MeterInfo getMeterByName(String name) {
        return _meters.get(name);
    }
    
    /** {@inheritDoc} */
    @Override
    public void addMeter(final String name, final String descr, final Meter meter) {
        MeterInfo mi = new MeterInfo() {
            @Override
            public String getName() { return name; }

            @Override
            public String getDescription() { return descr; }

            @Override
            public Meter getMeter() { return meter; }
        };
        _meters.put(name, mi);
    }
    
    /** {@inheritDoc} */
    @Override
    public void removeMeter(String name) {
        _meters.remove(name);
    }
    
    /** {@inheritDoc} */
    @Override
    public void removeMeter(Meter meter) {
        MeterInfo meterInfo = null;
        for (MeterInfo mi : _meters.values()) {
            if (mi.getMeter() == meter) {
                meterInfo = mi;
                break;
            }
        }
        if (meterInfo != null) {
            _meters.remove(meterInfo.getName());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void enable() {
        for (MeterInfo mi : _meters.values()) {
            mi.getMeter().enable();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void disable() {
        for (MeterInfo mi : _meters.values()) {
            mi.getMeter().disable();
        }
    }

    @Override
    public void setState(int s) throws JmriException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getState() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameMultiMeter");
    }
    
    /**
     * Request an update from the layout.
     */
    public void requestUpdateFromLayout() {
        for (MeterInfo mi : _meters.values()) {
            mi.getMeter().disable();
        }
    }
    
    
/*    
    @Override
    public void setCurrent(float c) {
        float old = current_float;
        current_float = c;
        this.firePropertyChange(CURRENT, old, c);
    }

    @Override
    public float getCurrent() {
        return current_float;
    }

    @Override
    public CurrentUnits getCurrentUnits() {
        return currentUnits;
    }

    @Override
    public void setVoltage(float v) {
        float old = voltage_float;
        voltage_float = v;
        this.firePropertyChange(VOLTAGE, old, v);
    }

    @Override
    public float getVoltage() {
        return voltage_float;
    }
*/
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(){
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMeterGroup.class);

}
