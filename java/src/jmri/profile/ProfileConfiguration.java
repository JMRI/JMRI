package jmri.profile;

import jmri.util.prefs.JmriConfigurationProvider;
import org.w3c.dom.Element;

/**
 *
 * @author Randall Wood
 */
class ProfileConfiguration implements AuxiliaryConfiguration {

    private final Profile project;

    public ProfileConfiguration(Profile project) {
        this.project = project;
    }

    @Override
    public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
        return JmriConfigurationProvider.getConfiguration(this.project).getConfigurationFragment(elementName, namespace, shared);
    }

    @Override
    public void putConfigurationFragment(Element fragment, boolean shared) {
        JmriConfigurationProvider.getConfiguration(this.project).putConfigurationFragment(fragment, shared);
    }

    @Override
    public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) {
        return JmriConfigurationProvider.getConfiguration(this.project).removeConfigurationFragment(elementName, namespace, shared);
    }

}
