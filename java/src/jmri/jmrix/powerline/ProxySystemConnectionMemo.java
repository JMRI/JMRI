package jmri.jmrix.powerline;

import java.util.ResourceBundle;
import jmri.ProgrammerManager;

/**
 * Proxy SerialSystemConnectionMemo that allows SerialSystemConnectionMemos to
 * be swapped in as needed.
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public class ProxySystemConnectionMemo extends SerialSystemConnectionMemo {

    SerialSystemConnectionMemo memo = null;

    public void setSystemConnectionMemo(SerialSystemConnectionMemo memo) {
        this.memo = memo;
    }

    @Override
    public SerialTrafficController getTrafficController() {
        return this.memo.getTrafficController();
    }

    @Override
    public void setTrafficController(SerialTrafficController tc) {
        this.memo.setTrafficController(tc);
    }

    @Override
    public SerialAddress getSerialAddress() {
        return this.memo.getSerialAddress();
    }

    @Override
    public void setSerialAddress(SerialAddress sa) {
        this.memo.setSerialAddress(sa);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ProgrammerManager getProgrammerManager() {
        return this.memo.getProgrammerManager();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setProgrammerManager(ProgrammerManager p) {
        this.memo.setProgrammerManager(p);
    }

    @Override
    public boolean provides(Class<?> type) {
        return this.memo.provides(type);
    }

    @Override
    public <T> T get(Class<?> T) {
        return this.memo.get(T);
    }

    @Override
    public void configureManagers() {
        this.memo.configureManagers();
    }

    @Override
    public SerialTurnoutManager getTurnoutManager() {
        return this.memo.getTurnoutManager();
    }

    @Override
    public SerialLightManager getLightManager() {
        return this.memo.getLightManager();
    }

    @Override
    public SerialSensorManager getSensorManager() {
        return this.memo.getSensorManager();
    }

    @Override
    public void setTurnoutManager(SerialTurnoutManager m) {
        this.memo.setTurnoutManager(m);
    }

    @Override
    public void setLightManager(SerialLightManager m) {
        this.memo.setLightManager(m);
    }

    @Override
    public void setSensorManager(SerialSensorManager m) {
        this.memo.setSensorManager(m);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return this.memo.getActionModelResourceBundle();
    }

    @Override
    public void dispose() {
        this.memo.dispose();
    }

    @Override
    public boolean getDisabled() {
        return this.memo.getDisabled();
    }

    @Override
    public void register() {
        this.memo.register();
    }

    @Override
    public String getSystemPrefix() {
        return this.memo.getSystemPrefix();
    }

    @Override
    public boolean setSystemPrefix(String systemPrefix) {
        return this.memo.setSystemPrefix(systemPrefix);
    }

    @Override
    public String getUserName() {
        return this.memo.getUserName();
    }

    @Override
    public boolean setUserName(String name) {
        return this.memo.setUserName(name);
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.memo.setDisabled(disabled);
    }
}
