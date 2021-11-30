package jmri.jmrit.logixng.log;

/**
 * The encodings that are supported in the LogixNG log.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public enum Encodings {
    
    /**
     * This encoding saves each state as a '0' or '1' character.
     */
    ASCII_ONE_BIT_PER_CHAR("ascii_1_bit_per_char", AsciiOneBitPerCharDecoder.class, AsciiOneBitPerCharEncoder.class);
    
    private final String _name;
    private final Class<? extends LogReaderDecoder> _decoderClass;
    private final Class<? extends LogWriterEncoder> _encoderClass;
    
    Encodings(String name,
            Class<? extends LogReaderDecoder> decoderClass,
            Class<? extends LogWriterEncoder> encoderClass) {
        _name = name;
        _decoderClass = decoderClass;
        _encoderClass = encoderClass;
    }
    
    /**
     * Get the name of the encoding.
     * This name is stored in header of the log file.
     * 
     * @return the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     * The class that is responsible to decode this encoding.
     * 
     * @return a class object for the decoder class
     */
    public Class<? extends LogReaderDecoder> getDecoderClass() {
        return _decoderClass;
    }
    
    /**
     * The class that is responsible to encode this encoding.
     * 
     * @return a class object for the encoder class.
     */
    public Class<? extends LogWriterEncoder> getEncoderClass() {
        return _encoderClass;
    }
    
    /**
     * Get the encoding from the name of the encoding.
     * 
     * @param name the name of the encoding
     * @return the encoder
     * @throws IllegalArgumentException if there is no encoding with that name
     */
    public static Encodings getEncodingFromName(String name) {
        for (Encodings encoding : Encodings.values()) {
            if (encoding.getName().equals(name)) {
                return encoding;
            }
        }

        throw new IllegalArgumentException("Encoding is unknown");
    }
    
}
