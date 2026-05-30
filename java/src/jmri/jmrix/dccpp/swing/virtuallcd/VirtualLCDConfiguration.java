package jmri.jmrix.dccpp.swing.virtuallcd;

import java.util.Set;

import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;

/**
 * Configuration for a VirtualLCD display.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public interface VirtualLCDConfiguration {

    enum DisplayConfig {
        ConfigureVirtualLCD_AllDisplays("ConfigureVirtualLCD_AllDisplays"),
        ConfigureVirtualLCD_OneDisplay("ConfigureVirtualLCD_OneDisplay"),
        ConfigureVirtualLCD_IntervalDisplay("ConfigureVirtualLCD_IntervalDisplay"),
        ConfigureVirtualLCD_SelectedDisplays("ConfigureVirtualLCD_SelectedDisplays");

        private final String descr;

        private DisplayConfig(String property) {
            this.descr = Bundle.getMessage(property);
        }

        public String toString() {
            return descr;
        }
    }

    void setMemo(DCCppSystemConnectionMemo memo);

    DCCppSystemConnectionMemo getMemo();

    void setDisplayConfig(DisplayConfig displayConfig);

    DisplayConfig getDisplayConfig();

    void setDisplayNo(int displayNo);

    int getDisplayNo();

    void setMinDisplayNo(int displayNo);

    int getMinDisplayNo();

    void setMaxDisplayNo(int displayNo);

    int getMaxDisplayNo();

    void setSelectedDisplays(Set<Integer> selectedDisplays);

    Set<Integer> getSelectedDisplays();

}
