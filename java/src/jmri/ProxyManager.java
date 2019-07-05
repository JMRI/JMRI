package jmri;

/**
 * Interface for Managers of NamedBeans that are proxies for a collection of
 * like Managers.
 * <p>
 * At present, this is merely an extension point that can be used to test if a
 * Manager is a proxy.
 *
 * @author Randall Wood Copyright 2019
 * @param <B> type of supported NamedBean
 */
public interface ProxyManager<B extends NamedBean> extends Manager<B> {

}
