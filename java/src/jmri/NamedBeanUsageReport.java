package jmri;

import javax.annotation.Nonnull;

/**
 * This is a data structure to pass usage information from getUsageReport() requests
 * back to the calling object.
 *
 * @author Dave Sand Copyright (C) 2020
 */
@javax.annotation.concurrent.Immutable
public class NamedBeanUsageReport {

    final public NamedBean usingBean;
    final public NamedBean usedBean;
    final public String usageKey;

    /**
     * Create a usage report.
     *
     * @param usingBean Identifies the report creator.  Can be null.
     * @param usedBean The search argument.
     * @param usageKey Identifies the report type.  Used to control result
     * processing.  Might also be used as a bundle key.
     */
    public NamedBeanUsageReport(NamedBean usingBean, @Nonnull NamedBean usedBean, @Nonnull String usageKey) {
        this.usingBean = usingBean;
        this.usedBean = usedBean;
        this.usageKey = usageKey;
    }
}
