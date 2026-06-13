package apps;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MarklinPoke} argument parsing and frame decoding.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
class MarklinPokeTest {

    @Test
    void parseFrames_singleFrame_13bytes() {
        String hex = "00 01 02 03 04 05 06 07 08 09 0A 0B 0C";
        List<byte[]> frames = MarklinPoke.parseFrames(hex);
        assertEquals(1, frames.size());
        assertEquals(13, frames.get(0).length);
        assertEquals(0x0C, frames.get(0)[12] & 0xFF);
    }

    @Test
    void parseFrames_twoFramesCommaSeparated() {
        String hex = "00 00 00 00 00 00 00 00 00 00 00 00 00," +
                     "FF FF FF FF FF FF FF FF FF FF FF FF FF";
        List<byte[]> frames = MarklinPoke.parseFrames(hex);
        assertEquals(2, frames.size());
    }

    @Test
    void parseFrames_blankTokenSkipped() {
        String hex = "00 00 00 00 00 00 00 00 00 00 00 00 00, ,";
        List<byte[]> frames = MarklinPoke.parseFrames(hex);
        assertEquals(1, frames.size());
    }

    @Test
    void parseFrames_wrongLength_throws() {
        String hex = "00 01 02";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> MarklinPoke.parseFrames(hex));
        assertTrue(ex.getMessage().contains("Frame 1"));
        assertTrue(ex.getMessage().contains("3 bytes"));
    }

    @Test
    void parseFrames_noSpaces_accepted() {
        String hex = "000102030405060708090A0B0C";
        List<byte[]> frames = MarklinPoke.parseFrames(hex);
        assertEquals(1, frames.size());
        assertEquals(13, frames.get(0).length);
    }

    @Test
    void parseArgs_defaults() {
        MarklinPoke.Args args = MarklinPoke.parseArgs(
            new String[]{"--command", "00 00 00 00 00 00 00 00 00 00 00 00 00"});
        assertEquals("localhost", args.host());
        assertEquals(15731, args.port());
        assertEquals(50L, args.paddingMs());
        assertEquals(5, args.watchSecs());
        assertEquals(1, args.frames().size());
    }

    @Test
    void parseArgs_allFlags() {
        MarklinPoke.Args args = MarklinPoke.parseArgs(new String[]{
            "--host", "192.168.1.5",
            "--port", "9999",
            "--command", "AA BB CC DD EE FF 00 11 22 33 44 55 66",
            "--padding", "300",
            "--watch", "10"
        });
        assertEquals("192.168.1.5", args.host());
        assertEquals(9999, args.port());
        assertEquals(300L, args.paddingMs());
        assertEquals(10, args.watchSecs());
    }
}
