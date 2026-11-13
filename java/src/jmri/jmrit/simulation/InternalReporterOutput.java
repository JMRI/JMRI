package jmri.jmrit.simulation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.InvokeOnAnyThread;
import jmri.InvokeOnLayoutThread;
import jmri.Manager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrix.internal.InternalReporterManager;
import jmri.managers.ProxyReporterManager;
import jmri.util.ThreadingUtil;

/**
 * Reports simulated trains' ID tags on internal reporters, as a reader that
 * sees which train is in a Block (RailCom, say) would: the tag of the train
 * that has come in, and nothing once no train with a tag is there.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@javax.annotation.concurrent.ThreadSafe
public class InternalReporterOutput implements SimulatedReporterOutput {

    /**
     * {@inheritDoc}
     * <p>
     * True for a reporter of the Internal connection.
     */
    @Override
    @InvokeOnLayoutThread
    public boolean reports(@Nonnull Reporter reporter) {
        ReporterManager rm = InstanceManager.getDefault(ReporterManager.class);
        if (rm instanceof ProxyReporterManager) {
            for (Manager<Reporter> m : ((ProxyReporterManager) rm).getManagerList()) {
                if (m instanceof InternalReporterManager && m.getBySystemName(reporter.getSystemName()) == reporter) {
                    return true;
                }
            }
            return false;
        }
        return rm instanceof InternalReporterManager && rm.getBySystemName(reporter.getSystemName()) == reporter;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Sets the reporter's report on the layout thread (Swing thread), later:
     * the ID tag with that tag ID, made if there is none yet, or null.
     */
    @Override
    @InvokeOnAnyThread
    public void report(@Nonnull Reporter reporter, @CheckForNull String tagId, long timeNanos) {
        ThreadingUtil.runOnLayoutEventually(() -> {
            IdTag tag = (tagId == null) ? null : InstanceManager.getDefault(IdTagManager.class).provideIdTag(tagId);
            reporter.setReport(tag);
        });
    }
}
