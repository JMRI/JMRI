package jmri.jmrix.anyma_dmx;

public class DMX_Factory {

    private static DMX_Controller controller;
    private static DMX_Provider provider;
    
    private DMX_Factory() {

    }

    public static DMX_Controller getInstance() {
        return controller;
    }

    public static DMX_Provider getDefaultProvider() {
        return provider;
    }

    public static void setDefaultProvider(DMX_Provider provider) {
        DMX_Factory.provider = provider;
    }
}
