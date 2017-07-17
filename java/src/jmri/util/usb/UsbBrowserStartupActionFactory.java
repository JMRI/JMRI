package jmri.util.usb;

import apps.startup.AbstractStartupActionFactory;
import java.util.Locale;

/**
 * {@link apps.startup.StartupActionFactory} for the
 * {@link jmri.util.usb.UsbBrowserAction}.
 *
 * @author Randall Wood Copyright (C) 2017
 */
public class UsbBrowserStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (!clazz.equals(UsbBrowserAction.class)) {
            throw new IllegalArgumentException();
        }
        return Bundle.getMessage(locale, "StartUsbBrowserAction"); // NOI18N
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{UsbBrowserAction.class};
    }

    @Override
    public String[] getOverriddenClasses(Class<?> clazz) throws IllegalArgumentException {
        return new String[]{"jmri.jmrix.libusb.UsbViewAction"};
    }
}
