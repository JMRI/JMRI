package apps;

import java.util.ArrayList;
import java.util.List;

/**
 * marklin-poke: standalone CLI tool that connects to a running McanTcpBridge as a plain TCP
 * client, sends user-supplied 13-byte CAN frames and prints everything seen on the bus.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class MarklinPoke {

    /** Parsed CLI arguments. */
    public static final class Args {
        private final String host;
        private final int port;
        private final List<byte[]> frames;
        private final long paddingMs;
        private final int watchSecs;

        public Args(String host, int port, List<byte[]> frames, long paddingMs, int watchSecs) {
            this.host = host;
            this.port = port;
            this.frames = frames;
            this.paddingMs = paddingMs;
            this.watchSecs = watchSecs;
        }

        public String host() { return host; }
        public int port() { return port; }
        public List<byte[]> frames() { return frames; }
        public long paddingMs() { return paddingMs; }
        public int watchSecs() { return watchSecs; }
    }

    /**
     * Parse a comma-separated list of 13-byte CAN frames from hex strings.
     * Spaces within each token are ignored. Blank tokens are silently skipped.
     *
     * @param commandArg raw --command value
     * @return list of parsed 13-byte frames
     * @throws IllegalArgumentException if any non-blank token does not decode to exactly 13 bytes
     */
    public static List<byte[]> parseFrames(String commandArg) {
        String[] tokens = commandArg.split(",");
        List<byte[]> result = new ArrayList<>();
        int frameNumber = 0;
        for (String token : tokens) {
            String stripped = token.replaceAll("\\s+", "");
            if (stripped.isEmpty()) {
                continue;
            }
            frameNumber++;
            if (stripped.length() % 2 != 0) {
                throw new IllegalArgumentException(
                    "Frame " + frameNumber + " has odd number of hex digits");
            }
            byte[] bytes = new byte[stripped.length() / 2];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) Integer.parseInt(stripped.substring(i * 2, i * 2 + 2), 16);
            }
            if (bytes.length != 13) {
                throw new IllegalArgumentException(
                    "Frame " + frameNumber + " has " + bytes.length + " bytes, expected 13");
            }
            result.add(bytes);
        }
        return result;
    }

    /**
     * Parse CLI arguments into an Args record.
     * Prints usage to System.err and calls System.exit(1) on bad input.
     *
     * @param args CLI arguments
     * @return parsed Args
     */
    public static Args parseArgs(String[] args) {
        String host = "localhost";
        int port = 15731;
        String command = null;
        long paddingMs = 50L;
        int watchSecs = 5;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--host":
                    host = args[++i];
                    break;
                case "--port":
                    port = Integer.parseInt(args[++i]);
                    break;
                case "--command":
                    command = args[++i];
                    break;
                case "--padding":
                    paddingMs = Long.parseLong(args[++i]);
                    break;
                case "--watch":
                    watchSecs = Integer.parseInt(args[++i]);
                    break;
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    printUsage();
                    System.exit(1);
            }
        }

        if (command == null) {
            System.err.println("--command is required");
            printUsage();
            System.exit(1);
        }

        List<byte[]> frames;
        try {
            frames = parseFrames(command);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid --command: " + e.getMessage());
            System.exit(1);
            return null; // unreachable
        }

        return new Args(host, port, frames, paddingMs, watchSecs);
    }

    private static void printUsage() {
        System.err.println("Usage: marklin-poke --command \"<hex>\" [--host <h>] [--port <p>] [--padding <ms>] [--watch <s>]");
        System.err.println("  --command   Comma-separated 13-byte CAN frames as hex (spaces ignored within frame)");
        System.err.println("  --host      Bridge host (default: localhost)");
        System.err.println("  --port      Bridge TCP port (default: 15731)");
        System.err.println("  --padding   Milliseconds between frames (default: 50)");
        System.err.println("  --watch     Seconds to watch after last frame (default: 5)");
    }

    public static void main(String[] args) {
        Args parsed = parseArgs(args);
        MarklinPokeRunner runner = new MarklinPokeRunner(parsed);
        runner.run();
    }
}
