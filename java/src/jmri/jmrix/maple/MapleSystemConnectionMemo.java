package jmri.jmrix.maple;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.ConfiguringSystemConnectionMemo;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimum required SystemConnectionMemo for Maple.
 * Expanded for multichar/multiconnection support.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class MapleSystemConnectionMemo extends DefaultSystemConnectionMemo implements ConfiguringSystemConnectionMemo {

    public MapleSystemConnectionMemo() {
        this("K", SerialConnectionTypeList.MAPLE);
    }

    public MapleSystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        super(prefix, userName);

        InstanceManager.store(this, MapleSystemConnectionMemo.class);

        // create and register the ComponentFactory
        InstanceManager.store(new jmri.jmrix.maple.swing.MapleComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created MapleSystemConnectionMemo");
    }

    private SerialTrafficController tc = null;

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param s jmri.jmrix.maple.SerialTrafficController object to use.
     */
    public void setTrafficController(SerialTrafficController s){
        tc = s;
    }

    /**
     * Get the traffic controller instance associated with this connection memo.
     * @return traffic controller, new instance if null.
     */
    public SerialTrafficController getTrafficController(){
        if (tc == null) {
            setTrafficController(new SerialTrafficController());
            log.debug("Auto create of SerialTrafficController for initial configuration");
        }
        return tc;
    }

    /**
     * Provide menu strings.
     *
     * @return null as there is no menu for Maple connections
     */
    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    public void configureManagers(){
        setTurnoutManager(new SerialTurnoutManager(this));
        InstanceManager.setTurnoutManager(getTurnoutManager());
        setLightManager(new SerialLightManager(this));
        InstanceManager.setLightManager(getLightManager());
        setSensorManager(new SerialSensorManager(this));
        InstanceManager.setSensorManager(getSensorManager());

        register();
    }

    /**
     * Provide access to the SensorManager for this particular connection.
     * <p>
     * NOTE: SensorManager defaults to NULL
     * @return sensor manager.
     */
    public SensorManager getSensorManager() {
        return get(SensorManager.class);
    }

    public void setSensorManager(SerialSensorManager s) {
        store(s,SensorManager.class);
        getTrafficController().setSensorManager(s);
    }

    /**
     * Provide access to the TurnoutManager for this particular connection.
     * <p>
     * NOTE: TurnoutManager defaults to NULL
     * @return turnout manager.
     */
    public TurnoutManager getTurnoutManager() {
        return get(TurnoutManager.class);

    }

    public void setTurnoutManager(SerialTurnoutManager t) {
        store(t,TurnoutManager.class);
    }

    /**
     * Provide access to the LightManager for this particular connection.
     * <p>
     * NOTE: LightManager defaults to NULL
     * @return light manager.
     */
    public LightManager getLightManager() {
        return get(LightManager.class);
    }

    public void setLightManager(SerialLightManager l) {
        store(l,LightManager.class);
    }

    // no dispose() for Maple

    private final static Logger log = LoggerFactory.getLogger(MapleSystemConnectionMemo.class);

}
