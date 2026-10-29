package jmri.jmrix.marklin;

/**
 * Codec for Marklin CAN (MCAN) protocol messages.
 * <p>
 * This class provides encoding and decoding functionality for MCAN messages,
 * separating protocol concerns from display and application logic.
 * <p>
 * The MCAN protocol structure:
 * <ul>
 * <li>Byte 0: Priority (4 bits high) + Command high bits (4 bits low)</li>
 * <li>Byte 1: Command low bits (7 bits) + Response flag (1 bit low)</li>
 * <li>Bytes 2-3: Hash (16 bits)</li>
 * <li>Byte 4: Data Length Code (DLC, 0-8)</li>
 * <li>Bytes 5-8: CAN Address (32 bits, big-endian)</li>
 * <li>Bytes 9-12: Data bytes (0-8 bytes as specified by DLC)</li>
 * </ul>
 *
 * @author Ralf Lang
 * @see <a href="https://www.maerklin.de/fileadmin/media/produkte/CS2_can-protokoll_1-0.pdf">CS2 CAN Protocol 1.0</a>
 * @see <a href="https://streaming.maerklin.de/public-media/cs2/cs2CAN-Protokoll-2_0.pdf">CS2 CAN Protocol 2.0</a>
 */
public class MarklinCanCodec {

    /**
     * Decoded MCAN message structure containing all protocol fields.
     */
    public static class DecodedMessage {
        private final int priority;
        private final int command;
        private final boolean isResponse;
        private final int[] hash;
        private final int dataLength;
        private final long address;
        private final int[] data;

        public DecodedMessage(int priority, int command, boolean isResponse,
                            int[] hash, int dataLength, long address, int[] data) {
            this.priority = priority;
            this.command = command;
            this.isResponse = isResponse;
            this.hash = hash;
            this.dataLength = dataLength;
            this.address = address;
            this.data = data;
        }

        public int getPriority() {
            return priority;
        }

        public int getCommand() {
            return command;
        }

        public boolean isResponse() {
            return isResponse;
        }

        public int[] getHash() {
            return hash;
        }

        public int getDataLength() {
            return dataLength;
        }

        public long getAddress() {
            return address;
        }

        public int[] getData() {
            return data;
        }

        /**
         * Get a specific data byte.
         * @param index index of the data byte (0-7)
         * @return the data byte value or 0 if index is out of bounds
         */
        public int getDataByte(int index) {
            if (index >= 0 && index < data.length) {
                return data[index];
            }
            return 0;
        }

        /**
         * Get command type category.
         * @return String describing the command category
         */
        public String getCommandCategory() {
            if (command == MarklinConstants.SYSCOMMANDSTART) {
                return "SYSTEM";
            } else if (command >= MarklinConstants.MANCOMMANDSTART && command <= MarklinConstants.MANCOMMANDEND) {
                return "MANAGEMENT";
            } else if (command >= MarklinConstants.ACCCOMMANDSTART && command <= MarklinConstants.ACCCOMMANDEND) {
                return "ACCESSORY";
            } else if (command >= MarklinConstants.SOFCOMMANDSTART && command <= MarklinConstants.SOFCOMMANDEND) {
                return "SOFTWARE";
            } else if (command >= MarklinConstants.GUICOMMANDSTART && command <= MarklinConstants.GUICOMMANDEND) {
                return "GUI";
            } else if (command >= MarklinConstants.FEECOMMANDSTART && command <= MarklinConstants.FEECOMMANDEND) {
                return "FEEDBACK";
            } else if (command >= MarklinConstants.AUTCOMMANDSTART && command <= MarklinConstants.AUTCOMMANDEND) {
                return "AUTOMATION";
            }
            return "UNKNOWN";
        }
    }

    /**
     * Builder for creating encoded MCAN messages.
     */
    public static class MessageBuilder {
        private int priority = MarklinConstants.PRIO_1;
        private int command = 0;
        private boolean isResponse = false;
        private int hashByte1 = MarklinConstants.HASHBYTE1;
        private int hashByte2 = MarklinConstants.HASHBYTE2;
        private long address = 0;
        private int[] data = new int[8];
        private int dataLength = 0;

        public MessageBuilder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public MessageBuilder setCommand(int command) {
            this.command = command;
            return this;
        }

        public MessageBuilder setResponse(boolean isResponse) {
            this.isResponse = isResponse;
            return this;
        }

        public MessageBuilder setHash(int byte1, int byte2) {
            this.hashByte1 = byte1;
            this.hashByte2 = byte2;
            return this;
        }

        public MessageBuilder setAddress(long address) {
            this.address = address;
            return this;
        }

        public MessageBuilder setData(int... dataBytes) {
            this.dataLength = Math.min(dataBytes.length, 8);
            System.arraycopy(dataBytes, 0, this.data, 0, this.dataLength);
            return this;
        }

        public MessageBuilder setDataLength(int length) {
            this.dataLength = Math.min(length, 8);
            return this;
        }

        /**
         * Build the message and encode it into a 13-byte array.
         * @return 13-byte array containing the encoded MCAN message
         */
        public int[] build() {
            int[] message = new int[13];

            // Encode byte 0: Priority (bits 7-4) + Command high bits (bits 3-0 are command bits 10-7)
            message[0] = ((priority & 0x0F) << 4) | ((command >> 7) & 0x0F);

            // Encode byte 1: Command low bits (7 bits) + Response flag
            message[1] = ((command & 0x7F) << 1) | (isResponse ? 0x01 : 0x00);

            // Hash bytes
            message[2] = hashByte1 & 0xFF;
            message[3] = hashByte2 & 0xFF;

            // Data length code
            message[4] = dataLength & 0xFF; // DLC is number of data bytes (0-8), NOT including 4 address bytes

            // Address bytes (big-endian)
            message[5] = (int) ((address >> 24) & 0xFF);
            message[6] = (int) ((address >> 16) & 0xFF);
            message[7] = (int) ((address >> 8) & 0xFF);
            message[8] = (int) (address & 0xFF);

            // Data bytes
            for (int i = 0; i < dataLength && i < 8; i++) {
                message[9 + i] = data[i] & 0xFF;
            }

            return message;
        }
    }

    /**
     * Decode a raw MCAN message into its constituent parts.
     *
     * @param rawMessage 13-byte array containing the MCAN message
     * @return DecodedMessage containing all protocol fields
     * @throws IllegalArgumentException if message length is not 13 bytes
     */
    public static DecodedMessage decode(int[] rawMessage) {
        if (rawMessage.length < 13) {
            throw new IllegalArgumentException("MCAN message must be at least 13 bytes");
        }

        // Decode priority from byte 0 (bits 7-4)
        int priority = (rawMessage[0] >> 4) & 0x0F;

        // Decode command from bytes 0-1
        // Byte 0 bits 3-0 contain command bits 10-7, byte 1 bits 7-1 contain command bits 6-0
        int command = ((rawMessage[0] & 0x0F) << 7) | ((rawMessage[1] >> 1) & 0x7F);

        // Decode response flag from byte 1 (bit 0)
        boolean isResponse = (rawMessage[1] & 0x01) == 0x01;

        // Hash bytes
        int[] hash = new int[]{rawMessage[2] & 0xFF, rawMessage[3] & 0xFF};

        // Data length code (number of data bytes, 0-8)
        // The 13-byte frame has bytes 5-12 as payload; bytes 5-8 overlap with the
        // decoded "address" field. Data extraction starts at byte 9, so a DLC > 4
        // would run past the end of the 13-byte frame. Clamp to what actually fits.
        int dataLength = rawMessage[4] & 0xFF;
        int maxData = rawMessage.length - 9; // bytes remaining after the address field
        if (dataLength > maxData) {
            dataLength = Math.max(0, maxData);
        }

        // Decode address (big-endian, 32-bit)
        long address = ((long) (rawMessage[5] & 0xFF) << 24)
                     | ((long) (rawMessage[6] & 0xFF) << 16)
                     | ((long) (rawMessage[7] & 0xFF) << 8)
                     | (rawMessage[8] & 0xFF);

        // Extract data bytes
        int[] data = new int[dataLength];
        for (int i = 0; i < dataLength; i++) {
            data[i] = rawMessage[9 + i] & 0xFF;
        }

        return new DecodedMessage(priority, command, isResponse, hash, dataLength, address, data);
    }

    /**
     * Decode a MarklinReply message into its constituent parts.
     * Convenience method that extracts the raw bytes from a MarklinReply.
     *
     * @param reply MarklinReply to decode
     * @return DecodedMessage containing all protocol fields
     * @throws IllegalArgumentException if message length is not 13 bytes
     */
    public static DecodedMessage decode(MarklinReply reply) {
        int[] rawData = new int[13];
        for (int i = 0; i < 13; i++) {
            rawData[i] = reply.getElement(i);
        }
        return decode(rawData);
    }

    /**
     * Decode a MarklinMessage into its constituent parts.
     * Convenience method that extracts the raw bytes from a MarklinMessage.
     *
     * @param message MarklinMessage to decode
     * @return DecodedMessage containing all protocol fields
     * @throws IllegalArgumentException if message length is not 13 bytes
     */
    public static DecodedMessage decode(MarklinMessage message) {
        int[] rawData = new int[13];
        for (int i = 0; i < 13; i++) {
            rawData[i] = message.getElement(i);
        }
        return decode(rawData);
    }

    /**
     * Create a new message builder.
     * @return MessageBuilder instance for fluent API construction
     */
    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    /**
     * Get priority level description.
     * @param priority priority value (0-3)
     * @return human-readable priority description
     */
    public static String getPriorityDescription(int priority) {
        switch (priority) {
            case MarklinConstants.PRIO_1:
                return "Priority 1: Stop/Go/Short";
            case MarklinConstants.PRIO_2:
                return "Priority 2: Feedback";
            case MarklinConstants.PRIO_3:
                return "Priority 3: Engine Stop";
            case MarklinConstants.PRIO_4:
                return "Priority 4: Engine/Accessory Command";
            default:
                return "Unknown Priority";
        }
    }

    /**
     * Determine the protocol type from a CAN address.
     * @param address CAN address to analyze
     * @return protocol description string
     */
    public static String getProtocolFromAddress(long address) {
        if (address >= MarklinConstants.MM1START && address <= MarklinConstants.MM1END) {
            return address == 0 ? "Broadcast" : "MM1/MM2 Loco";
        } else if (address >= MarklinConstants.MM1FUNCTSTART && address <= MarklinConstants.MM1FUNCTEND) {
            return "MM Function Decoder";
        } else if (address >= MarklinConstants.MM1LOCOSTART && address <= MarklinConstants.MM1LOCOEND) {
            return "MM1/MM2 Loco (20kHz)";
        } else if (address >= MarklinConstants.SX1START && address <= MarklinConstants.SX1END) {
            return "Selectrix SX1";
        } else if (address >= MarklinConstants.SX1ACCSTART && address <= MarklinConstants.SX1ACCEND) {
            return "Selectrix SX1 Accessory";
        } else if (address >= MarklinConstants.MM1ACCSTART && address <= MarklinConstants.MM1ACCEND) {
            return "MM Accessory";
        } else if (address >= MarklinConstants.DCCACCSTART && address <= MarklinConstants.DCCACCEND) {
            return "DCC Accessory";
        } else if (address >= MarklinConstants.MFXSTART && address <= MarklinConstants.MFXEND) {
            return "MFX";
        } else if (address >= MarklinConstants.SX2START && address <= MarklinConstants.SX2END) {
            return "Selectrix SX2";
        } else if (address >= MarklinConstants.DCCSTART && address <= MarklinConstants.DCCEND) {
            return "DCC";
        } else if (address >= MarklinConstants.CLUBRANGESTART && address <= MarklinConstants.CLUBRANGEEND) {
            return "Club Range";
        } else if (address >= MarklinConstants.VENDORRANGESTART && address <= MarklinConstants.VENDORRANGEEND) {
            return "Vendor Range";
        }
        return "Unknown Protocol";
    }

    /**
     * Get the base address offset for a given protocol/address.
     * @param address CAN address
     * @return the base address value after removing protocol offset
     */
    public static long getBaseAddress(long address) {
        if (address >= MarklinConstants.MM1START && address <= MarklinConstants.MM1END) {
            return address;
        } else if (address >= MarklinConstants.MM1FUNCTSTART && address <= MarklinConstants.MM1FUNCTEND) {
            return address - MarklinConstants.MM1FUNCTSTART;
        } else if (address >= MarklinConstants.MM1LOCOSTART && address <= MarklinConstants.MM1LOCOEND) {
            return address - MarklinConstants.MM1LOCOSTART;
        } else if (address >= MarklinConstants.SX1START && address <= MarklinConstants.SX1END) {
            return address - MarklinConstants.SX1START;
        } else if (address >= MarklinConstants.SX1ACCSTART && address <= MarklinConstants.SX1ACCEND) {
            return address - MarklinConstants.SX1ACCSTART;
        } else if (address >= MarklinConstants.MM1ACCSTART && address <= MarklinConstants.MM1ACCEND) {
            return address - MarklinConstants.MM1ACCSTART;
        } else if (address >= MarklinConstants.DCCACCSTART && address <= MarklinConstants.DCCACCEND) {
            return address - MarklinConstants.DCCACCSTART;
        } else if (address >= MarklinConstants.MFXSTART && address <= MarklinConstants.MFXEND) {
            return address - MarklinConstants.MFXSTART;
        } else if (address >= MarklinConstants.SX2START && address <= MarklinConstants.SX2END) {
            return address - MarklinConstants.SX2START;
        } else if (address >= MarklinConstants.DCCSTART && address <= MarklinConstants.DCCEND) {
            return address - MarklinConstants.DCCSTART;
        }
        return address;
    }

    /**
     * Extract priority from a MarklinReply.
     * @param reply the reply message
     * @return priority value
     */
    public static int getPriority(MarklinReply reply) {
        return (reply.getElement(0) >> 4) & 0x0F;
    }

    /**
     * Extract command from a MarklinReply.
     * @param reply the reply message
     * @return command value
     */
    public static int getCommand(MarklinReply reply) {
        return ((reply.getElement(0) & 0x7F) << 7) | ((reply.getElement(1) >> 1) & 0x7F);
    }

    /**
     * Extract response flag from a MarklinReply.
     * @param reply the reply message
     * @return true if this is a response message
     */
    public static boolean isResponse(MarklinReply reply) {
        return (reply.getElement(1) & 0x01) == 0x01;
    }

    /**
     * Extract address from a MarklinReply.
     * @param reply the reply message
     * @return 32-bit CAN address
     */
    public static long getAddress(MarklinReply reply) {
        long addr = (reply.getElement(5) & 0xFF);
        addr = (addr << 8) + (reply.getElement(6) & 0xFF);
        addr = (addr << 8) + (reply.getElement(7) & 0xFF);
        addr = (addr << 8) + (reply.getElement(8) & 0xFF);
        return addr;
    }
}
