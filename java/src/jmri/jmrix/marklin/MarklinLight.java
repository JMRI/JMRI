package jmri.jmrix.marklin;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import jmri.implementation.AbstractLight;
import jmri.jmrix.marklin.mcan.McanFrame;
import jmri.jmrix.marklin.mcan.cs2.Cs2AccessoryCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2Frame;

/**
 * Binary light controlled by a Marklin accessory decoder.
 * Protocol-prefixed addresses use the same wire format as Marklin turnouts.
 */
public class MarklinLight extends AbstractLight implements MarklinListener {

    private final MarklinTrafficController tc;
    private final int canAddress;
    private final List<ReleaseTask> pendingReleases = new ArrayList<>();

    /**
     * Create a light without sending a command to the layout.
     *
     * @param systemName full system name, e.g. {@code MLDCC45} or {@code ML45}
     * @param userName optional user name
     * @param prefix connection system prefix
     * @param tc traffic controller
     */
    public MarklinLight(String systemName, String userName, String prefix, MarklinTrafficController tc) {
        super(systemName, userName);
        this.tc = tc;
        canAddress = MarklinAccessoryAddress.toCANAddress(systemName.substring(prefix.length() + 1));
        tc.addMarklinListener(this);
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized void doNewState(int oldState, int newState) {
        final int position = newState == ON ? 1 : 0;
        tc.sendMarklinMessage(MarklinMessage.getSetTurnout(canAddress, position, 1), this);
        ReleaseTask release = new ReleaseTask(position);
        pendingReleases.add(release);
        jmri.util.TimerUtil.schedule(release, MarklinTurnout.METERINTERVAL);
    }

    /** {@inheritDoc} */
    @Override
    public void reply(MarklinReply reply) {
        McanFrame frame = reply.getFrame();
        long address;
        int aspect;
        if (frame instanceof Cs2AccessoryCommands.AccessoryFrame) {
            address = ((Cs2AccessoryCommands.AccessoryFrame) frame).address();
            aspect = ((Cs2AccessoryCommands.AccessoryFrame) frame).aspect();
        } else if (frame instanceof Cs2AccessoryCommands.AccessoryReplyFrame) {
            address = ((Cs2AccessoryCommands.AccessoryReplyFrame) frame).address();
            aspect = ((Cs2AccessoryCommands.AccessoryReplyFrame) frame).aspect();
        } else {
            return;
        }
        if (((Cs2Frame) frame).header().priority() != MarklinConstants.PRIO_1 || address != canAddress) {
            return;
        }
        switch (aspect) {
            case 0:
                notifyStateChange(mState, OFF);
                break;
            case 1:
                notifyStateChange(mState, ON);
                break;
            default:
                log.warn("Unknown state command {} for light {}", aspect, getSystemName());
                break;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void message(MarklinMessage message) {
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void dispose() {
        // Release energized outputs before removing the listener or closing the connection.
        while (!pendingReleases.isEmpty()) {
            ReleaseTask release = pendingReleases.get(0);
            release.cancel();
            release.run();
        }
        tc.removeMarklinListener(this);
        super.dispose();
    }

    private class ReleaseTask extends TimerTask {
        private final int position;

        ReleaseTask(int position) {
            this.position = position;
        }

        @Override
        public void run() {
            synchronized (MarklinLight.this) {
                if (pendingReleases.remove(this)) {
                    tc.sendMarklinMessage(MarklinMessage.getSetTurnout(canAddress, position, 0), MarklinLight.this);
                }
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MarklinLight.class);
}
