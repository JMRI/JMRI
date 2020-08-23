package jmri.jmrix.pricom.pockettester;

public class TestConstants {

    private TestConstants(){
    }

    // convenient strings
    static String version = "PRICOM Design DCC Pocket Tester - Version 1.4\n";
    static String idlePacket = "Decoder Idle Packet\n";
    static String speed0003A = "ADR=0003 CMD=Speed    STP=128   DIR=Fwd SPD=S124\n";
    static String speed0003B = "ADR=0003 CMD=Speed    STP=128   DIR=Fwd SPD=S000\n";
    static String speed0123A = "ADR=0123 CMD=Speed    STP=128   DIR=Fwd SPD=S124\n";
    static String speed0123B = "ADR=0123 CMD=Speed    STP=128   DIR=Fwd SPD=S000\n";
    static String speed012A = "ADR= 012 CMD=Speed    STP=128   DIR=Fwd SPD=S124\n";
    static String speed012B = "ADR= 012 CMD=Speed    STP=128   DIR=Fwd SPD=S000\n";
    static String acc0222A = "ADR=0222 CMD=Accessry VAL=Thrown/R(OFF) ACT=OFF\n";

    // note that the "=" glyphs in the following are actually two characters
    static String status1 = " 5A=0003992832 5B=0000000000 5C=0000000000 5D=0000000000 5E=0000000000";
    static String status2 = " TV=0000013417";
    static String status3 = " 6A=0003779802 6B=0003359824 6C=0000419978 6D=0000000000 6E=0000000000 6F=0000000000 6G=0000000121 6H=0000041645";
    static String status4 = " 3A=0003992832 3B=0000000000 3C=0000000000 3D=0000000000 3E=0000000000 3F=0000000000";
    static String status5 = " 1A=0000563761 1B=0000412530 1C=0000151263 1D=0000013752 1E=0000000000 1F=0000000000";
    static String status6 = " 6A=0003779802 6B=0003359824 6C=0000419978 6D=0000000000 6E=0000000000 6F=0000000000 6G=0000000000 6H=0000000000";

}
