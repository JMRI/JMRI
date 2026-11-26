package jmri.jmrix.tmcc.simulator.simulatedtrains;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.DccLocoAddress;
import jmri.InvokeOnAnyThread;
import jmri.SpeedStepMode;
import jmri.jmrit.simulation.SimulatedSpeedSource;
import jmri.jmrix.tmcc.simulator.SimulatorAdapter;

/**
 * Speed commands for simulated trains from a TMCC Simulator connection: what
 * each engine is told by the commands the simulated command base is sent,
 * read here as the Lionel command formats give them, not with JMRI's TMCC
 * code, so a fault there shows as a train moving differently from what was
 * asked.
 * <p>
 * Each command is three bytes, a first byte then a 16 bit word. Read here:
 * <ul>
 * <li>0xFE, TMCC 1, engine commands 00AAAAAAACCDDDDD: CC 11 sets absolute
 * speed D (0 to 31); CC 10 changes the speed by D - 5 (D 0 to 10), within 0
 * to 31; CC 00 with D 0 sets forward, 1 toggles the direction, 3 sets
 * reverse. 0xFFFF halts everything.
 * <li>0xF8, Legacy engine commands AAAAAAA0DDDDDDDD: absolute speed D (0 to
 * 199); AAAAAAA1 then 011DDDDD: absolute speed D (0 to 31); AAAAAAA1 then 0,
 * 1 or 3: forward, toggle, reverse. 0xFF8B halts everything.
 * </ul>
 * Train (TR), switch, accessory, route and group commands, Legacy
 * multi-word commands (0xFB) and Legacy train commands (0xF9) move no
 * engine and are passed over.
 * <p>
 * An engine is taken to start forward, and to keep its speed when only its
 * direction is changed, as JMRI's throttle sends a speed of 0 with each
 * direction change anyway. A halt is an emergency stop for every engine
 * sent a command so far. TMCC has no long and short addresses: an engine is
 * found by its number alone.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@javax.annotation.concurrent.ThreadSafe
public class TmccSimulatedSpeedSource implements SimulatedSpeedSource, SimulatorAdapter.MessageListener {

    private final Map<Integer, Command> commands = new ConcurrentHashMap<>();
    private final List<CommandListener> listeners = new CopyOnWriteArrayList<>();
    /** Bytes of a command split between two reads. */
    private final int[] pending = new int[2];
    private int pendingCount = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckForNull
    @InvokeOnAnyThread
    public Command getCommand(@Nonnull DccLocoAddress address) {
        return commands.get(address.getNumber());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @InvokeOnAnyThread
    public void addCommandListener(@Nonnull CommandListener listener) {
        listeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @InvokeOnAnyThread
    public void removeCommandListener(@Nonnull CommandListener listener) {
        listeners.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @InvokeOnAnyThread
    public synchronized void messageReceived(String message, long timeNanos) {
        String text = message.trim();
        if (text.isEmpty()) {
            return;
        }
        String[] w = text.split("\\s+");
        int[] b = new int[pendingCount + w.length];
        System.arraycopy(pending, 0, b, 0, pendingCount);
        try {
            for (int i = 0; i < w.length; i++) {
                b[pendingCount + i] = Integer.parseInt(w[i], 16) & 0xFF;
            }
        } catch (NumberFormatException e) {
            pendingCount = 0;
            return; // not bytes
        }
        pendingCount = 0;
        int i = 0;
        while (i < b.length) {
            int first = b[i];
            if (first != 0xFE && first != 0xF8 && first != 0xF9 && first != 0xFB) {
                i++; // not the start of a command: look for the next
                continue;
            }
            if (b.length - i < 3) {
                for (int j = i; j < b.length; j++) {
                    pending[pendingCount++] = b[j];
                }
                return;
            }
            command(first, (b[i + 1] << 8) | b[i + 2], timeNanos);
            i += 3;
        }
    }

    private void command(int first, int word, long timeNanos) {
        if (first == 0xFE) {
            if (word == 0xFFFF) {
                haltAll(timeNanos);
            } else if ((word & 0xC000) == 0) {
                int address = (word >> 7) & 0x7F;
                int data = word & 0x1F;
                switch ((word >> 5) & 0x3) {
                    case 3:
                        speed(address, data, SpeedStepMode.TMCC1_32, timeNanos);
                        break;
                    case 2:
                        if (data <= 10) {
                            Command old = commands.get(address);
                            int step = old == null || old.isEmergencyStop() ? 0 : old.getSpeedStep();
                            if (old != null && old.getMode() != SpeedStepMode.TMCC1_32) {
                                step = (int) Math.round(old.getSetting() * SpeedStepMode.TMCC1_32.numSteps);
                            }
                            speed(address, Math.max(0, Math.min(31, step + data - 5)), SpeedStepMode.TMCC1_32,
                                    timeNanos);
                        }
                        break;
                    case 0:
                        direction(address, data, timeNanos);
                        break;
                    default:
                        break; // other engine commands: sounds, lights, couplers
                }
            }
        } else if (first == 0xF8) {
            if (word == 0xFF8B) {
                haltAll(timeNanos);
                return;
            }
            int address = (word >> 9) & 0x7F;
            int data = word & 0xFF;
            if ((word & 0x100) == 0) {
                speed(address, Math.min(data, 199), SpeedStepMode.TMCC2_200, timeNanos);
            } else if ((data & 0xE0) == 0x60) {
                speed(address, data & 0x1F, SpeedStepMode.TMCC2_32, timeNanos);
            } else {
                direction(address, data, timeNanos);
            }
        }
        // 0xF9 Legacy train and 0xFB multi-word commands move no engine
    }

    private void speed(int address, int step, SpeedStepMode mode, long timeNanos) {
        Command old = commands.get(address);
        boolean forward = old == null || old.isForward();
        received(address, new Command(step, mode, forward, false, timeNanos));
    }

    private void direction(int address, int data, long timeNanos) {
        if (data != 0 && data != 1 && data != 3) {
            return; // not a direction command
        }
        Command old = commands.get(address);
        boolean was = old == null || old.isForward();
        boolean forward = data == 0 || (data == 1 && !was);
        received(address, old == null
                ? new Command(0, SpeedStepMode.TMCC1_32, forward, false, timeNanos)
                : new Command(old.getSpeedStep(), old.getMode(), forward, old.isEmergencyStop(), timeNanos));
    }

    private void haltAll(long timeNanos) {
        for (Map.Entry<Integer, Command> e : commands.entrySet()) {
            Command old = e.getValue();
            received(e.getKey(), new Command(0, old.getMode(), old.isForward(), true, timeNanos));
        }
    }

    private void received(int address, Command now) {
        Command old = commands.get(address);
        if (old != null && old.getSpeedStep() == now.getSpeedStep() && old.getMode() == now.getMode()
                && old.isForward() == now.isForward() && old.isEmergencyStop() == now.isEmergencyStop()) {
            return; // the same command again: nothing changes for the engine
        }
        commands.put(address, now);
        DccLocoAddress a = new DccLocoAddress(address, false);
        for (CommandListener l : listeners) {
            l.commandChanged(a);
        }
    }
}
