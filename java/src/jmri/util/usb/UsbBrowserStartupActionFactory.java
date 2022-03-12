package jmri.util.usb;

import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import java.util.Locale;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link jmri.util.startup.StartupActionFactory} for the
 * {@link jmri.util.usb.UsbBrowserAction}.
 *
 * @author Randall Wood Copyright (C) 2017
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class UsbBrowserStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(UsbBrowserAction.class)) {
            return Bundle.getMessage(locale, "StartUsbBrowserAction"); // NOI18N
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
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
