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
    final public String usageData;

    /**
     * Create report with the required key.
     * @param usageKey Identifies the report type.  Used to control result.
     */
    public NamedBeanUsageReport(@Nonnull String usageKey) {
        this(usageKey, null, "");
    }

    /**
     * Create report with the required key and a bean.
     * @param usageKey Identifies the report type.  Used to control result.
     * @param usageBean Identifies a related bean such as SML destination mast.  Can be null.
     */
    public NamedBeanUsageReport(@Nonnull String usageKey, NamedBean usageBean) {
        this(usageKey, usageBean, "");
    }

    /**
     * Create report with the required key and additional data.
     * @param usageKey Identifies the report type.  Used to control result.
     * @param usageData Optional additional data.
     */
    public NamedBeanUsageReport(@Nonnull String usageKey, String usageData) {
        this(usageKey, null, usageData);
    }

    /**
     * Create a usage report.
     *
     * @param usageKey Identifies the report type.  Used to control result
     * processing.  Might also be used as a bundle key.
     * @param usageBean Identifies a related bean such as SML destination mast.  Can be null.
     * @param usageData Optional additional data.
     */
    public NamedBeanUsageReport(@Nonnull String usageKey, NamedBean usageBean, String usageData) {
        this.usageKey = usageKey;
        this.usageBean = usageBean;
        this.usageData = usageData;
    }
}
