package jmri.jmrix.dcc4pc;

import java.util.function.Supplier;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.AddressedProgrammer;
import jmri.AddressedProgrammerManager;
import jmri.CommandStation;
import jmri.GlobalProgrammerManager;
import jmri.managers.DefaultProgrammerManager;

/**
 * DCC4PC Programmer acts as a proxy for ops mode programming. Another
 * connection (the one chosen with the connection's "Programmer" option) sends
 * the Programming on Main packets, and the decoder's RailCom reply comes back
 * through the RailCom reader; see {@link Dcc4PcOpsModeProgrammer}.
 * <p>
 * Only ops mode is provided. Service mode stays with the other connection.
 *
 * @see jmri.managers.DefaultProgrammerManager
 * @author Kevin Dickerson Copyright (C) 2012
 *
 */
public class Dcc4PcProgrammerManager extends DefaultProgrammerManager {

    /**
     * Finds the other connection's programmer manager, or null if that
     * connection isn't running (yet). Looked up when needed, so this manager
     * can exist before the other connection has started.
     */
    private final Supplier<AddressedProgrammerManager> manager;

    /** Finds the other connection's command station, which sends POM read packets; null if none. */
    private final Supplier<CommandStation> commandStation;

    /**
     * @param manager        finds the other connection's programmer manager
     * @param commandStation finds the other connection's command station
     * @param memo           the DCC4PC connection, which gives this manager its name
     */
    public Dcc4PcProgrammerManager(@Nonnull Supplier<AddressedProgrammerManager> manager,
            @Nonnull Supplier<CommandStation> commandStation, @Nonnull Dcc4PcSystemConnectionMemo memo) {
        super(null, memo);
        this.manager = manager;
        this.commandStation = commandStation;
    }

    /**
     * @param manager the other connection's programmer manager
     * @param memo    the DCC4PC connection, which gives this manager its name
     */
    public Dcc4PcProgrammerManager(@Nonnull AddressedProgrammerManager manager, @Nonnull Dcc4PcSystemConnectionMemo memo) {
        this(() -> manager, () -> null, memo);
    }

    /**
     * @param <T>     a manager for both modes
     * @param manager the other connection's programmer manager
     */
    public <T extends AddressedProgrammerManager & GlobalProgrammerManager> Dcc4PcProgrammerManager(@Nonnull T manager) {
        super(null);
        this.manager = () -> manager;
        this.commandStation = () -> null;
    }

    @CheckForNull
    private AddressedProgrammerManager realManager() {
        AddressedProgrammerManager m = manager.get();
        if (m == null) {
            log.warn("{}: no connection to send Programming on Main packets; check the connection's"
                    + " Programmer option, or the default Command Station in Preferences, Defaults", getUserName());
        }
        return m;
    }

    /**
     * {@inheritDoc}
     * <p>
     * True until the other connection says otherwise: it may simply not have
     * started yet.
     */
    @Override
    public boolean isAddressedModePossible() {
        AddressedProgrammerManager m = manager.get();
        return m == null || m.isAddressedModePossible();
    }

    @Override
    public boolean isGlobalProgrammerAvailable() {
        return false;
    }

    @Override
    protected AddressedProgrammer getConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        AddressedProgrammerManager m = realManager();
        return m == null ? null : new Dcc4PcOpsModeProgrammer(pLongAddress, pAddress, m, commandStation.get());
    }

    @Override
    protected AddressedProgrammer reserveConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
        // a reserved programmer must read through DCC4PC too
        return getConcreteAddressedProgrammer(pLongAddress, pAddress);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Dcc4PcProgrammerManager.class);
}
