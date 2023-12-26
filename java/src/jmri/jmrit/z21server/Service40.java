package jmri.jmrit.z21server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Arrays;

public class Service40 {
    private static final String moduleIdent = "[Service 40] ";

    private final static Logger log = LoggerFactory.getLogger(Service40.class);


    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    public static byte[] handleService(byte[] data, InetAddress clientAddress) {
        int command = data[0];
        switch (command){
            case 0x21:
                return handleHeader21(data[1]);
            case (byte)0xE3:
                return handleHeaderE3(Arrays.copyOfRange(data, 1, 4), clientAddress);
            case (byte)0xE4:
                return handleHeaderE4(Arrays.copyOfRange(data, 1, 5), clientAddress);
            default:
                log.debug("{} Header {} not yet supported", moduleIdent, Integer.toHexString(command));
                break;
        }
        return null;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    private static byte[] handleHeader21(int db0){
        switch (db0){
            case 0x21:
                // Get z21 version
                break;
            case 0x24:
                // Get z21 status
                byte[] answer = new byte[8];
                answer[0] = (byte) 0x08;
                answer[1] = (byte) 0x00;
                answer[2] = (byte) 0x40;
                answer[3] = (byte) 0x00;
                answer[4] = (byte) 0x62;
                answer[5] = (byte) 0x22;
                answer[6] = (byte) 0x00;
                answer[7] = ClientManager.xor(answer);
                return answer;
            case 0x80:
                log.debug("{} Set track power to off", moduleIdent);
                break;
            case 0x81:
                log.debug("{} Set track power to on", moduleIdent);
                break;
            default:
                break;
        }
        return null;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    private static byte[] handleHeaderE3(byte[] data, InetAddress clientAddress) {
        int db0 = data[0];
        if (db0 == (byte)0xF0) {
            // Get loco status command
            int locomotiveAddress = (((data[1] & 0xFF) & 0x3F) << 8) + (data[2] & 0xFF);
            log.debug("{} Get loco no {} status", moduleIdent, locomotiveAddress);

            ClientManager.getInstance().registerLocoIfNeeded(clientAddress, locomotiveAddress);

            return ClientManager.getInstance().getLocoStatusMessage(clientAddress, locomotiveAddress);

        } else {
            log.debug("{} Header E3 with function {} is not supported", moduleIdent,  Integer.toHexString(db0));
        }
        return null;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
    justification = "Messages can be of any length, null is used to indicate absence of message for caller")
    private static byte[] handleHeaderE4(byte[] data, InetAddress clientAddress) {
        if (data[0] == 0x13) {
            int locomotiveAddress = (((data[1] & 0xFF) & 0x3F) << 8) + (data[2] & 0xFF);
            int rawSpeedData = data[3] & 0xFF;
            boolean bForward = ((rawSpeedData & 0x80) >> 7) == 1;
            int actualSpeed = rawSpeedData & 0x7F;
            log.debug("Set loco no {} direction {} with speed {}",locomotiveAddress, (bForward ? "FWD" : "RWD"), actualSpeed);

            ClientManager.getInstance().setLocoSpeedAndDirection(clientAddress, locomotiveAddress, actualSpeed, bForward);

            return ClientManager.getInstance().getLocoStatusMessage(clientAddress, locomotiveAddress);
        }
        if (data[0] == (byte)0xF8) {
            int locomotiveAddress = (((data[1] & 0xFF) & 0x3F) << 8) + (data[2] & 0xFF);
            boolean bOn = (((data[3] & 0xFF) & 0x40) >> 6) == 1;
            int functionNumber = (data[3] & 0xFF) & 0x3F;
            log.debug("Set loco no {} function no {} to {}", locomotiveAddress, functionNumber, (bOn ? "ON" : "OFF"));

            ClientManager.getInstance().setLocoFunction(clientAddress, locomotiveAddress, functionNumber, bOn);

            return ClientManager.getInstance().getLocoStatusMessage(clientAddress, locomotiveAddress);
        }
        return null;
    }
}
