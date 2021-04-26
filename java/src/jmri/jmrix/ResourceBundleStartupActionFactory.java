package jmri.jmrix;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import jmri.util.startup.AbstractStartupActionFactory;

/**
 * Create a factory of startup actions from a ResourceBundle.
 * 
 * @author Randall Wood Copyright 2020
 */
public class ResourceBundleStartupActionFactory extends AbstractStartupActionFactory {

    private final ResourceBundle bundle;
    private Class<?>[] classes;
    
    protected ResourceBundleStartupActionFactory(ResourceBundle resource) {
        bundle = resource;
    }

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (Arrays.asList(getActionClasses()).contains(clazz)) {
            return ResourceBundle.getBundle(bundle.getBaseBundleName(), locale).getString(clazz.getName());
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        if (classes == null) {
            if (bundle != null) {
                Set<Class<?>> set = new HashSet<>();
                bundle.keySet().forEach(k -> {
                    try {
                        set.add(Class.forName(k));
                    } catch (ClassNotFoundException ex) {
                        log.error("Specified class {} not found.", k, ex);
                    }
                });
                classes = set.toArray(new Class<?>[set.size()]);
            } else {
                classes = new Class<?>[0];
            }
        }
        return classes;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ResourceBundleStartupActionFactory.class);
}
