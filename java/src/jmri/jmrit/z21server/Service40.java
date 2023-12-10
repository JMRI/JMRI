package jmri.jmrit.z21server;

import java.net.InetAddress;
import java.util.Arrays;

public class Service40 {
    private static String moduleIdent = "[Service 40] ";

    private static float speedMultiplier = 1.0f / 128.0f;

    public static void handleService(byte[] data, InetAddress clientAddress) {
        int command = data[0];
        switch (command){
            case 0x21:
                handleHeader21(data[1]);
                break;
            case (byte)0xE3:
                handleHeaderE3(Arrays.copyOfRange(data, 1, 4), clientAddress);
                break;
            case (byte)0xE4:
                handleHeaderE4(Arrays.copyOfRange(data, 1, 5), clientAddress);
                break;
            default:
                System.out.println(moduleIdent + "Header " + Integer.toHexString(command) + " not yet supported");
                break;
        }
    }

    private static void handleHeader21(int db0){
        switch (db0){
            case 0x21:
                System.out.println(moduleIdent + "Get z21 version");
                break;
            case 0x24:
                System.out.println(moduleIdent + "Get z21 status");
                break;
            case 0x80:
                System.out.println(moduleIdent + "Set track power to off");
                break;
            case 0x81:
                System.out.println(moduleIdent + "Set track power to on");
                break;
            default:
                System.out.println(moduleIdent + "0x21 c pété");
                break;
        }
    }

    private static void handleHeaderE3(byte[] data, InetAddress clientAddress) {
        int db0 = data[0];
        if (db0 == (byte)0xF0) {
            // Get loco status command
            int locomotiveAddress = (((data[1] & 0xFF) & 0x3F) << 8) + (data[2] & 0xFF);
            System.out.println(moduleIdent + "Get loco no " + locomotiveAddress + " status");

            ClientManager.getInstance().registerLocoIfNeeded(clientAddress, locomotiveAddress);

        } else {
            System.out.println(moduleIdent + "Header E3 with function " + Integer.toHexString(db0) + " is not supported");
        }
    }

    private static void handleHeaderE4(byte[] data, InetAddress clientAddress) {
        if (data[0] == 0x13) {
            int locomotiveAddress = (((data[1] & 0xFF) & 0x3F) << 8) + (data[2] & 0xFF);
            int rawSpeedData = data[3] & 0xFF;
            boolean bForward = ((rawSpeedData & 0x80) >> 7) == 1;
            int actualSpeed = rawSpeedData & 0x7F;
            System.out.println("Set loco no " + locomotiveAddress + " direction " + (bForward ? "FWD" : "RWD") + " with speed " + actualSpeed);

            ClientManager.getInstance().setLocoSpeedAndDirection(clientAddress, locomotiveAddress, actualSpeed, bForward);

        }

    }
}
