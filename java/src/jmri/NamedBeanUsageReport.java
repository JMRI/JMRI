package jmri;

public class NamedBeanUsageReport {

    NamedBean _usingBean;
    NamedBean _usedBean;
    String _bundleKey;

    public NamedBeanUsageReport(NamedBean usingBean, NamedBean usedBean, String bundleKey) {
        _usingBean = usingBean;
        _usedBean = usedBean;
        _bundleKey = bundleKey;
    }

    public String getBundleKey() {
        return _bundleKey;
    }
}
