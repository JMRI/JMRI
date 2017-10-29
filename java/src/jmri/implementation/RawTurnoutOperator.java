package jmri.implementation;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete subclass of TurnoutOperator for a turnout that has no feedback. This
 * operator sends raw NMRA accessory decoder packets to the layout instead of
 * using the built in turnout code. It should be used only with turnouts with
 * DIRECT, ONESENSOR or TWOSENSOR feedback. This class is based on the
 * NoFeedbackTurnoutOperator class.
 *
 * @author Paul Bender Copyright 2008
 */
public class RawTurnoutOperator extends TurnoutOperator {

    long interval;
    int maxTries;
    int tries = 0;
    int address = 0;
    CommandStation c;

    public RawTurnoutOperator(AbstractTurnout t, long i, int mt) {
        super(t);
        String sysName = t.getSystemName();
        int startAddress = sysName.lastIndexOf("T");
        address = Integer.parseInt(sysName.substring(startAddress + 1, sysName.length()));
        String prefix = t.getSystemName().substring(0, startAddress);
        java.util.List<jmri.CommandStation> connList = jmri.InstanceManager.getList(jmri.CommandStation.class);
        for (int x = 0; x < connList.size(); x++) {
            jmri.CommandStation station = connList.get(x);
            if (station.getSystemPrefix().equals(prefix)) {
                c = station;
                break;
            }
        }
        if (c == null) {
            c = InstanceManager.getNullableDefault(CommandStation.class);
            log.error("No match against the command station for " + sysName + ", so will use the default");
        }
        interval = i;
        maxTries = mt;
    }

    private void sendCommand() {
        byte pkt[] = jmri.NmraPacket.accDecoderPkt(address, myTurnout.getCommandedState() == Turnout.CLOSED);
        c.sendPacket(pkt, 1);
    }

    /**
     * Do the autmation for a turnout with no feedback. This means try maxTries
     * times at an interval of interval. Note the call to operatorCheck each
     * time we're about to actually do something - if we're no longer the
     * current operator this throws TurnoutOperatorException which just
     * terminates the thread.
     */
    @Override
    public void run() {
        try {
            operatorCheck();
            sendCommand();
            while (++tries < maxTries) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // retain if needed later
                }
                operatorCheck();
                sendCommand();
            }
            myTurnout.setKnownStateToCommanded();
        } catch (TurnoutOperatorException e) {
        }
    }

    private final static Logger log = LoggerFactory.getLogger(RawTurnoutOperator.class);
}
