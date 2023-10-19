package jmri.jmrix.bidib.tcpserver;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for BiDiB TCP server startup actions.
 * 
 * @author Randall Wood Copyright 2020
 * @author Eckart Meyer Copyright (C) 2023
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class TcpServerStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(TcpServerAction.class)) {
            //return Bundle.getMessage(locale, "StartupServerAction");
            return "Start BiDiB over TCP Server";
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{TcpServerAction.class};
    }

}
