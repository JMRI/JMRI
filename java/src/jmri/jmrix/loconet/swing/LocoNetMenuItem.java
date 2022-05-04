package jmri.jmrix.loconet.swing;

/**
 * A LocoNet Menu item.
 * 
 * @author Bob Milhaupt  Copyright (C) 2021
 */
public class LocoNetMenuItem {
    /**
     * Construct a Menu Item for all LocoNet-specific connection menu(s).
     * 
     * @param <T> Type of class to be executed upon activation of the menu item
     * @param name Text to be shown in the menu item
     * @param load class to be constructed upon selection of the menu item
     * @param interfaceOnly true if menu item is to be shown only for JMRI 
     *          connections having a physical interface, else false
     * @param hasGui true if the menu item has a GUI object which must be 
     *          associated with the main JMRI Window Interface, else false
     */
    public <T> LocoNetMenuItem(String name, Class<T> load, boolean interfaceOnly, boolean hasGui) {
        
        this.name = name;
        this.classToLoad = load;
        this.hasGui = hasGui;
        this.interfaceOnly = interfaceOnly;
        }
    
    private final boolean interfaceOnly;
    private final String name;
    private final Class<?> classToLoad;
    private final boolean hasGui;


    public boolean isInterfaceOnly() {
        return interfaceOnly;
    }
    public String getName() {
        return name;
    }

    public Class<?> getClassToLoad() {
        return classToLoad;
    }

    public boolean hasGui() {
        return hasGui;
    }

}
