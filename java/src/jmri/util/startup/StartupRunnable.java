package jmri.util.startup;

import org.apiguardian.api.API;

/**
 * A {@link Runnable} that is called as a startup action if registered to
 * provide a StartupRunnable service that can be loaded by
 * {@link java.util.ServiceLoader#load(java.lang.Class)}.
 *
 * @author Randall Wood Copyright 2020
 */
@API(status = API.Status.MAINTAINED)
public interface StartupRunnable extends Runnable {

}
