package jmri.jmrix.direct;

import jmri.NmraPacket;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for the MakePacketTest class
 *
 * @author	Bob Jacobsen Copyright 2004
 */
public class MakePacketTest {

    @Test
    public void testCreate() {
    }

    @Test
    public void testSimplePacket() {
        int[] result = new int[100];

        byte buffer[] = new byte[3];
        boolean test_retval;
        test_retval = MakePacket.setPreambleLength(15);

        buffer[0] = 0;
        buffer[1] = 1;
        buffer[2] = 0 ^ 1;
        result = MakePacket.createStream(buffer);
        /**
         * resulting stream should be in hex 55 55 55 c6 c6 c6 c6 c6 c6 c6 c6 96
         * c6 c6 c6 5c
         */
        Assert.assertTrue(test_retval);
        Assert.assertEquals("Simple lenght", 16, result[0]);
        Assert.assertEquals("Simple 0", 85, result[1]);
        Assert.assertEquals("Simple 1 ", 85, result[2]);
        Assert.assertEquals("Simple 2 ", 85, result[3]);
        Assert.assertEquals("Simple 3", 198, result[4]);
        Assert.assertEquals("Simple 4 ", 198, result[5]);
        Assert.assertEquals("Simple 5", 198, result[6]);
        Assert.assertEquals("Simple 6 ", 198, result[7]);
        Assert.assertEquals("Simple 7 ", 198, result[8]);
        Assert.assertEquals("Simple 8 ", 198, result[9]);
        Assert.assertEquals("Simple 9 ", 198, result[10]);
        Assert.assertEquals("Simple 10", 198, result[11]);
        Assert.assertEquals("Simple 11", 150, result[12]);
        Assert.assertEquals("Simple 12", 198, result[13]);
        Assert.assertEquals("Simple 13", 198, result[14]);
        Assert.assertEquals("Simple 14", 198, result[15]);
        Assert.assertEquals("Simple 15 ", 92, result[16]);
    }

    @Test
    public void testPreamble() {
        int[] result = new int[100];
        byte buffer[] = new byte[3];
        boolean test_retval;
        test_retval = MakePacket.setPreambleLength(20);
        Assert.assertEquals("Preamble set to 20", true, test_retval);
        /**
         * Now actually test that it does generate a serial stream with 20 '1's
         * as preamble
         */

        buffer[0] = 0;
        buffer[1] = 1;
        buffer[2] = 0 ^ 1;
        result = MakePacket.createStream(buffer);
        Assert.assertEquals("preamble", 85, result[3]);

        test_retval = MakePacket.setPreambleLength(16);
        /**
         * should return false since preamble has to be a multiply of 15
         */
        Assert.assertEquals("preamble not mutply of 5", false, test_retval);
        test_retval = MakePacket.setPreambleLength(15);

    }

    /**
     * Test all possible three-byte packets. This ensures that MakePacket thinks
     * it can find a way of representing each of those packets. Note that the
     * output packet is not checked for correctness.
     * <p>
     * Unfortunately, due to the number of trials, this takes too long to be
     * included in normal runs. Hence it's name has been modified so that JUnit
     * will not routinely select it to be run.
     */
    @Test
    @Ignore("Disabled in JUnit 3")
    public void testAll3BytePacket() {
        int[] result = new int[100];
        byte i, j;
        byte buffer[] = new byte[3];
        boolean test_retval;
        test_retval = MakePacket.setPreambleLength(15);
        Assert.assertTrue(test_retval);

        for (i = -128; i < 127; i++) {
            for (j = -128; j < 127; j++) {
                buffer[0] = i;
                buffer[1] = j;
                buffer[2] = (byte) (buffer[0] ^ buffer[1]);
                result = MakePacket.createStream(buffer);
                if (result[0] == 0) {
                    Assert.assertEquals("test all -  invalid lenght", 10, result[0]);
                }
            }
        }
    }

    /**
     * Test all possible combinations of locomotive address (short and long
     * forms) and speed value. This ensures that MakePacket thinks it can find a
     * way of representing each of those packets. Note that the output packet is
     * not checked for correctness.
     * <p>
     * Unfortunately, due to the number of trials, this takes too long to be
     * included in normal runs. Hence it's name has been modified so that JUnit
     * will not routinely select it to be run.
     */
    @Test
    @Ignore("Disabled in JUnit 3")
    public void testAllSpeed128Packets() {
        int[] result = new int[100];
        int addressRange, speedRange;
        byte buffer[] = new byte[6];
        boolean test_retval, Direction;
        Direction = true; /*Set direction to forwards */

        test_retval = MakePacket.setPreambleLength(15);
        Assert.assertTrue(test_retval);

        for (addressRange = 0; addressRange < 10239; addressRange++) {
            for (speedRange = 0; speedRange < 127; speedRange++) {
                Direction = true;
                buffer = NmraPacket.speedStep128Packet(addressRange, true, speedRange, Direction);

                result = MakePacket.createStream(buffer);
                if (result[0] == 0) {
                    Assert.assertEquals("test 128 speed forward direction (long addresses) -  invalid lenght", 10, result[0]);
                }
                Direction = false;
                buffer = NmraPacket.speedStep128Packet(addressRange, true, speedRange, Direction);

                result = MakePacket.createStream(buffer);
                if (result[0] == 0) {
                    Assert.assertEquals("test 128 speed backward direction (long addresses) -  invalid lenght", 10, result[0]);
                }
            }
        }
        for (addressRange = 0; addressRange < 127; addressRange++) {
            for (speedRange = 0; speedRange < 127; speedRange++) {
                buffer = NmraPacket.speedStep128Packet(addressRange, false, speedRange, Direction);

                result = MakePacket.createStream(buffer);
                if (result[0] == 0) {
                    Assert.assertEquals("test 128 speed (short addresses) -  invalid lenght", 10, result[0]);
                }
            }
        }
    }

    /**
     * Test all possible combinations of locomotive address (short and long
     * forms) and CV address/value to write. This ensures that MakePacket thinks
     * it can find a way of representing each of those packets. Note that the
     * output packet is not checked for correctness.
     * <p>
     * Unfortunately, due to the number of trials, this takes too long to be
     * included in normal runs. Hence it's name has been modified so that JUnit
     * will not routinely select it to be run.
     */
    @Test
    @Ignore("Disabled in JUnit 3")
    public void testAllOpsCvWrite() {
        int[] result = new int[100];
        int addressRange, cvNum, data;
        byte buffer[] = new byte[6];
        boolean test_retval;

        test_retval = MakePacket.setPreambleLength(15);
        Assert.assertTrue(test_retval);

        for (addressRange = 0; addressRange < 10239; addressRange++) {
            for (cvNum = 2; cvNum < 512; cvNum++) {
                for (data = 0; data < 127; data++) {
                    buffer = NmraPacket.opsCvWriteByte(addressRange, true, cvNum, data);

                    result = MakePacket.createStream(buffer);
                    if (result[0] == 0) {
                        Assert.assertEquals("test ops CV write (long addresses) -  invalid lenght", 10, result[0]);
                    }
                    if (addressRange < 127) {
                        buffer = NmraPacket.opsCvWriteByte(addressRange, false, cvNum, data);

                        result = MakePacket.createStream(buffer);
                        if (result[0] == 0) {
                            Assert.assertEquals("test ops CV write (short addresses) -  invalid lenght", 10, result[0]);
                        }

                    }

                }
            }
        }
    }

}
