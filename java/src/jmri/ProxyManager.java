package jmri;

import java.util.List;
import javax.annotation.Nonnull;

/**
 * Interface for Managers of NamedBeans that are proxies for a collection of
 * Managers for the same type of NamedBean.
 *
 * @author Randall Wood Copyright 2019
 * @param <B> type of supported NamedBean
 */
public interface ProxyManager<B extends NamedBean> extends Manager<B> {

    /**
     * Add a Manager to the collection of Managers.
     *
     * @param manager the Manager to add; if manager has already been added, it
     *                will not be added again
     */
    public void addManager(@Nonnull Manager<B> manager);

    /**
     * Get the default manager or the internal manager if no default manager has
     * been set.
     *
     * @return the default manager or the internal manager
     */
    @Nonnull
    public Manager<B> getDefaultManager();

    /**
     * Returns a list of all managers, including the internal manager. This is
     * not a live list, but it is in alpha order (don't assume default is at
     * front)
     *
     * @return the list of managers
     */
    public List<Manager<B>> getManagerList();

    /**
     * Get a list of all managers, with the default as the first item and internal
     * default as the last item.
     *
     * @return the list of managers
     */
    public List<Manager<B>> getDisplayOrderManagerList();
}
