package jmri.jmrix.marklin;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Timer;
import jmri.jmrix.marklin.mcan.McanFrame;
import jmri.jmrix.marklin.mcan.cs2.Cs2Frame;
import jmri.jmrix.marklin.mcan.cs2.Cs2GuiCommands;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection owned roster reader running on the event dispatch thread. At most one named transfer runs
 * per connection.
 */
public final class Ms2LocoInfoService implements MarklinListener {
    public enum State { IDLE, READING, COMPLETE, FAILED, CANCELLED }
    private final MarklinSystemConnectionMemo memo;
    private final MarklinTrafficController traffic;
    private final SeniorThrottleService senior;
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private final Map<McanDevice, Map<String, Ms2LocoRecord>> rosters = new HashMap<>();
    private final Map<McanDevice, String> lists = new HashMap<>();
    private final Set<Integer> observedHashes = new HashSet<>();
    private final Timer timer;
    private volatile Ms2LocoInfoSession session;
    private McanDevice target;
    private int providerHash;
    private int requesterHash;
    private long started;
    private State state = State.IDLE;
    private String detail = "";
    private boolean disposed;

    public Ms2LocoInfoService(MarklinSystemConnectionMemo memo) {
        this.memo = memo;
        traffic = java.util.Objects.requireNonNull(memo.getTrafficController(), "No MCAN transport");
        senior = memo.getSeniorThrottleService();
        traffic.addMarklinListener(this);
        timer = new Timer(25, e -> tick());
    }

    /** Caller must be on the EDT. Requires a positively identified master. */
    public void requestLocos(McanDevice device) {
        if (disposed || isBusy()) {
            throw new IllegalStateException("MS2 reader disposed or already busy");
        }
        if (senior.getRole(device) != SeniorThrottleService.Role.MASTER || observedHash(device) < 0) {
            throw new IllegalArgumentException("Only a recently identified senior MS2 can supply its roster");
        }
        target = device;
        providerHash = observedHash(device);
        Set<Integer> hashes = new HashSet<>(observedHashes);
        for (McanDevice known : memo.getMcanDeviceManager().getNamedBeanSet()) {
            NativeDeviceService.get(known).ifPresent(nativeState ->
                    nativeState.getObservedHash().ifPresent(hashes::add));
        }
        requesterHash = -1;
        for (int candidate = 0x4712; candidate <= 0xFFFF; candidate++) {
            int hash = (candidate & 0xFC7F) | 0x0300;
            if (!hashes.contains(hash)) {
                requesterHash = hash;
                break;
            }
        }
        if (requesterHash < 0) {
            throw new IllegalStateException("No unused requester hash");
        }
        started = System.nanoTime();
        detail = "";
        session = new Ms2LocoInfoSession(providerHash, requesterHash, 50, new Ms2LocoInfoSession.Listener() {
            @Override
            public void listReceived(String text) {
                Map<String, Ms2LocoRecord> records = parseList(text);
                // Preserve validated details only while their list metadata is unchanged.
                Map<String, Ms2LocoRecord> old = rosters.getOrDefault(target, Collections.emptyMap());
                records.replaceAll((name, record) -> old.containsKey(name)
                        && old.get(name).getListText().equals(record.getListText()) ? old.get(name) : record);
                rosters.put(target, records);
                lists.put(target, text);
                changed();
            }
            @Override
            public void locoReceived(String name, String text) {
                Map<String, Ms2LocoRecord> records = rosters.get(target);
                Ms2LocoRecord entry = records.get(name);
                records.put(name, new Ms2LocoRecord(name, entry.getListText(), text));
                changed();
            }
            @Override
            public void error(String reason) {
                detail = reason;
                log.warn("MS2 {}: {}", target.getSystemName(), reason);
                changed();
            }
            @Override
            public void finished(int records, int failures) {
                state = failures == 0 ? State.COMPLETE : State.FAILED;
                timer.stop();
                changed();
            }
        });
        state = State.READING;
        changed();
        timer.start();
    }

    private void tick() {
        if (disposed || !isBusy()) {
            return;
        }
        if (senior.getRole(target) != SeniorThrottleService.Role.MASTER
                || observedHash(target) != providerHash) {
            cancel("MS2 master is no longer current or its sender hash changed");
            return;
        }
        int[] frame = session.nextFrame(elapsed());
        if (frame != null) {
            MarklinMessage message = new MarklinMessage(frame);
            message.setReplyExpected(false);
            traffic.sendMarklinMessage(message, null);
        }
    }

    private long elapsed() {
        return (System.nanoTime() - started) / 1_000_000;
    }

    @Override
    public void message(MarklinMessage message) {
        receive(message.getFrame());
    }

    @Override
    public void reply(MarklinReply reply) {
        if (reply.getNumDataElements() != 13) {
            return;
        }
        receive(reply.getFrame());
    }

    private void receive(McanFrame frame) {
        // Snapshot the acquisition before dispatch: Queued frames from an earlier
        // acquisition must not be delivered to a newly started local session.
        Ms2LocoInfoSession receivedFor = session;
        ThreadingUtil.runOnGUI(() -> {
            if (disposed) {
                return;
            }
            if (frame instanceof Cs2Frame
                    && !(frame instanceof Cs2GuiCommands.RequestConfigDataRequestFrame
                    && ((Cs2Frame) frame).header().hash() == requesterHash)) {
                observedHashes.add(((Cs2Frame) frame).header().hash());
            }
            if (isBusy() && session == receivedFor) {
                if (senior.getRole(target) != SeniorThrottleService.Role.MASTER
                        || observedHash(target) != providerHash) {
                    cancel("MS2 master is no longer current or its sender hash changed");
                } else {
                    session.accept(frame, elapsed());
                }
            }
        });
    }

    private static int observedHash(McanDevice device) {
        return NativeDeviceService.get(device).map(nativeState ->
                nativeState.getObservedHash().orElse(-1)).orElse(-1);
    }

    static Map<String, Ms2LocoRecord> parseList(String text) {
        Map<String, Ms2LocoRecord> result = new LinkedHashMap<>();
        List<String> entries = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean hasName = false;
        for (String line : text.split("\\r?\\n")) {
            String token = line.strip();
            if ((token.startsWith(".idx=") || token.equals("lok") || token.equals("lokomotive")
                    || token.startsWith(".name=")) && hasName) {
                entries.add(current.toString());
                current.setLength(0);
                hasName = false;
            }
            current.append(line).append('\n');
            hasName |= token.startsWith(".name=");
        }
        if (hasName) {
            entries.add(current.toString());
        }
        for (String entry : entries) {
            for (String line : entry.split("\\r?\\n")) {
                String token = line.strip();
                if (token.startsWith(".name=")) {
                    String name = token.substring(6);
                    result.put(name, new Ms2LocoRecord(name, entry, ""));
                    break;
                }
            }
        }
        return result;
    }

    public List<Ms2LocoRecord> getLocos(McanDevice device) {
        return List.copyOf(rosters.getOrDefault(device, Collections.emptyMap()).values());
    }

    public String getListText(McanDevice device) { return lists.getOrDefault(device, ""); }
    public boolean isBusy() { return state == State.READING; }
    public State getState() { return state; }
    public McanDevice getTarget() { return target; }
    public String getDetail() { return detail; }

    public void cancel(String reason) {
        if (isBusy()) {
            session.cancel(reason);
            state = State.CANCELLED;
            timer.stop();
            changed();
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }

    private void changed() { changes.firePropertyChange("roster", null, target); }

    public void dispose() {
        ThreadingUtil.runOnGUI(this::disposeOnGui);
    }

    private void disposeOnGui() {
        if (disposed) {
            return;
        }
        cancel("Connection disposed");
        disposed = true;
        timer.stop();
        traffic.removeMarklinListener(this);
        for (PropertyChangeListener listener : changes.getPropertyChangeListeners()) {
            changes.removePropertyChangeListener(listener);
        }
        rosters.clear();
        lists.clear();
    }

    private static final Logger log = LoggerFactory.getLogger(Ms2LocoInfoService.class);
}
