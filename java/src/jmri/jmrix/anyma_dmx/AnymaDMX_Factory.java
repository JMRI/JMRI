package jmri.jmrix.anyma_dmx;

public class AnymaDMX_Factory {

    private static AnymaDMX_Controller controller;
    private static AnymaDMX_Provider provider;

    public AnymaDMX_Factory(AnymaDMX_Controller controller) {
        AnymaDMX_Factory.controller = controller;
    }

    public static AnymaDMX_Controller getInstance() {
        return controller;
    }

    public static AnymaDMX_Provider getDefaultProvider() {
        return provider;
    }

    public static void setDefaultProvider(AnymaDMX_Provider provider) {
        AnymaDMX_Factory.provider = provider;
    }
}
