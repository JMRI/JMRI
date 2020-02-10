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

    final public String usageKey;
    final public NamedBean usageBean;

    public NamedBeanUsageReport(@Nonnull String usageKey) {
        this(usageKey, null);
    }

    /**
     * Create a usage report.
     *
     * @param usageKey Identifies the report type.  Used to control result
     * processing.  Might also be used as a bundle key.
     * @param usageBean Identifies a related bean suach as SML destination mast.  Can be null.
     */
    public NamedBeanUsageReport(@Nonnull String usageKey, NamedBean usageBean) {
        this.usageKey = usageKey;
        this.usageBean = usageBean;
    }
}
