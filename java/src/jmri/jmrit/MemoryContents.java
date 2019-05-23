package jmri.jmrit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Models (and provides utility functions for) board memory as expressed in .hex
 * files and .DMF files.
 * <p>
 * Provides mechanisms to read and interpret firmware update files into an
 * internal data structure. Provides mechanisms to in create firmware update
 * files from an internal data structure. Provides mechanisms to allow other
 * agents to access the data in the internal data structures for the purpose of
 * sending the data to the device to be updated. Supports the Intel "I8HEX" file
 * format and a derivative ".dmf" file format created by Digitrax.
 * <p>
 * Support for the Intel "I8HEX" format includes support for record types "00"
 * and "01". The "I8HEX" format implements records with a LOAD OFFSET field of
 * 16 bits. To support the full 24-bit addressing range provided by the LocoNet
 * messaging protocol for firmware updates, this class is able to interpret
 * record type "04" (Extended Linear Address) records for input files with
 * 16-bit LOAD OFFSET fields. Record type "04" are typically found in the Intel
 * "I32HEX" 32-bit addressing format. Because the class supports only 24 bits of
 * address, interpretation of the "04" record type requires that the upper 8
 * bits of the 16-bit data field be 0.
 * <p>
 * Support for some .hex files emitted by some tool-sets requires support for
 * the Extended Segment Address record type (record type "02"), which may be
 * used in I16HEX format files. This version of the {@link #readHex} method
 * supports the Extended Segment Address record type ONLY when the segment
 * specified in the data field is 0x0000.
 * <p>
 * Support for the Digitrax ".DMF" format is an extension to the "I8HEX"
 * support. This extension supports interpretation of the 24-bit LOAD OFFSET
 * fields used in .DFM files. The class does not allow files with 24-bit LOAD
 * OFFSET fields to use the "04" (Extended Linear Address) record type unless
 * its data field is 0x0000.
 * <p>
 * Support for the ".DMF" format allows capture of Key/Value pairs which may be
 * embedded in special comments within a .DMF file. This support is enabled for
 * I8HEX files.
 * <p>
 * The class treats the information within a file's records as having
 * "big-endian" address values in the record LOAD OFFSET field. The INFO or DATA
 * field information is interpreted as 8-bit values, with the left-most value in
 * the INFO or DATA field corresponding to the address specified by the record's
 * LOAD OFFSET field plus the influence of the most recent previous Extended
 * Linear Address record, if any.
 * <p>
 * The INFO or DATA field for Extended Linear Address records is interpreted as
 * a big-endian value, where bits 7 thru 0 of the data field value are used as
 * bits 23 thru 16 of the effective address, while bits 15 thru 0 of the
 * effective address are from the 16-bit LOAD OFFSET of each data record. Bits
 * 15 thru 8 of the Extended Linear Address record INFO or DATA field must be 0
 * because of the 24-bit address limitation of this implementation.
 * <p>
 * The class does not have to know anything about filenames or filename
 * extensions. Instead, to read a file, an instantiating method will create a
 * {@link File} object and pass that object to {@link #readHex}.
 * Similarly, when writing the contents of data storage to a file, the
 * instantiating method will create a {@link File} and an associated
 * {@link Writer} and pass the {@link Writer} object to
 * {@link #writeHex}. The mechanisms implemented within this class do not
 * know about or care about the filename or its extension and do not use that
 * information as part of its file interpretation or file creation.
 * <p>
 * The class is implemented with a maximum of 24 bits of address space, with up
 * to 256 pages of up to 65536 bytes per page. A "sparse" implementation of
 * memory is modeled, where only occupied pages are allocated within the Java
 * system's memory.
 * <hr>
 * The Intel "Hexadecimal Object File Format File Format Specification"
 * uses the following terms for the fields of the record:
 * <dl>
 * <dt>RECORD MARK</dt><dd>first character of a record.  ':'</dd>
 * 
 * <dt>RECLEN</dt><dd>a two-character specifier of the number of bytes of information 
 *          in the "INFO or DATA" field.  Immediately follows the RECORD 
 *          MARK charcter. Since each byte within the "INFO or DATA" field is 
 *          represented by two ASCII characters, the data field contains twice
 *          the RECLEN value number of ASCII characters.</dd>
 * 
 * <dt>LOAD OFFSET</dt><dd>specifies the 16-bit starting load offset of the data bytes.
 *          This applies only to "Data" records, so this class requires that
 *          this field must encode 0x0000 for all other record types.  The LOAD
 *          OFFSET field immediately follows the RECLEN field.
 * <p>
 *          Note that for the 24-bit addressing format used with ".DMF" 
 *          files, this field is a 24-bit starting load offset, represented by
 *          six ASCII characters, rather than the four ASCII characters 
 *          specified in the Intel specification.</dd>
 * 
 * <dt>RECTYP</dt><dd>RECord TYPe - indicates the record type for this record.  The 
 *          RECTYPE field immediately follows the LOAD OFFSET field.</dd>
 * 
 * <dt>INFO or DATA</dt><dd>(Optional) field containing information or data which is 
 *          appropriate to the RECTYP.  Immediately follows the RECTYP field.
 *          contains RECLEN times 2 characters, where consecutive pairs of
 *          characters represent one byte of info or data.</dd>
 * 
 * <dt>CHKSUM</dt><dd>8-bit Checksum, computed using the hexadecimal byte values represented 
 *          by the character pairs in RECLEN, LOAD OFFSET, RECTYP, and INFO 
 *          or DATA fields, such that the computed sum, when added to the 
 *          CKSUM value, sums to an 8-bit value of 0x00.</dd>
 * </dl>
 * This information based on the Intel document "Hexadecimal Object File Format
 * Specification", Revision A, January 6, 1988.
 * <p>
 * Mnemonically, a properly formatted record would appear as:
 * <pre>
 *     :lloooott{dd}cc
 * where:
 *      ':'     is the RECORD MARK
 *      "ll"    is the RECLEN
 *      "oooo"  is the 16-bit LOAD OFFSET
 *      "tt"    is the RECTYP
 *      "{dd}"  is the INFO or DATA field, containing zero or more pairs of 
 *                  characters of Info or Data associated with the record
 *      "cc"    is the CHKSUM
 * </pre>
 * <p>
 * and a few examples of complaint records would be:
 * <ul>
 *     <li>:02041000FADE07
 *     <li>:020000024010AC
 *     <li>:00000001FF
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2005, 2008
 * @author B. Milhaupt Copyright (C) 2014, 2017
 */
public class MemoryContents {

    // Class (static) variables

    /* For convenience, a page of local storage of data is sized to equal one 
     * "segment" within an input file.  As such, the terms "page" and "segment" 
     * are used interchangeably throughout here.
     * 
     * The number of pages is chosen to match the 24-bit address space.
     */
    private static final int DEFAULT_MEM_VALUE = -1;
    private static final int PAGESIZE = 0x10000;
    private static final int PAGES = 256;

    private static final int RECTYP_DATA_RECORD = 0;
    private static final String STRING_DATA_RECTYP = StringUtil.twoHexFromInt(RECTYP_DATA_RECORD);
    private static final int RECTYP_EXTENDED_SEGMENT_ADDRESS_RECORD = 2;
    private static final int RECTYP_EXTENDED_LINEAR_ADDRESS_RECORD = 4;
    private static final int RECTYP_EOF_RECORD = 1;
    private static final int CHARS_IN_RECORD_MARK = 1;
    private static final int CHARS_IN_RECORD_LENGTH = 2;
    private static final int CHARS_IN_RECORD_TYPE = 2;
    private static final int CHARS_IN_EACH_DATA_BYTE = 2;
    private static final int CHARS_IN_CHECKSUM = 2;
    private static final int CHARS_IN_24_BIT_ADDRESS = 6;
    private static final int CHARS_IN_16_BIT_ADDRESS = 4;

    private static final char LEADING_CHAR_COMMENT = '#'; // NOI18N
    private static final char LEADING_CHAR_KEY_VALUE = '!'; // NOI18N
    private static final char LEADING_CHAR_RECORD_MARK = ':'; // NOI18N

    // Instance variables
    /**
     * Firmware data storage
     *
     * Implemented as a two-dimensional array where the first dimension
     * represents the "page" number, and the second dimension represents the
     * byte within the page of {@link #PAGESIZE} bytes.
     */
    private final int[][] pageArray;
    private int currentPage;
    private int lineNum;
    private boolean hasData;
    private int curExtLinAddr;
    private int curExtSegAddr;

    /**
     * Storage for Key/Value comment information extracted from key/value
     * comments within a .DMF or .hex file
     */
    private ArrayList<String> keyValComments = new ArrayList<String>(1);

    /**
     * Defines the LOAD OFFSET field type used/expected for records in "I8HEX"
     * and ".DMF" file formats.
     * <p>
     * When reading a file using the {@link #readHex} method, the value is
     * inferred from the first record and then used to validate the remaining
     * records in the file.
     * <p>
     * This value must be properly set before invoking the {@link #writeHex}
     * method.
     */
    private LoadOffsetFieldType loadOffsetFieldType = LoadOffsetFieldType.UNDEFINED;

    /**
     */
    public MemoryContents() {
        pageArray = new int[PAGES][];
        currentPage = -1;
        hasData = false;
        curExtLinAddr = 0;
        curExtSegAddr = 0;
        keyValComments = new ArrayList<String>(1);
    }

    private boolean isPageInitialized(int page) {
        return (pageArray[page] != null);
    }

    /**
     * Initialize a single page of data storage, if and only if the page has not
     * been initialized already.
     *
     */
    private void initPage(int page) {
        if (pageArray[page] != null) {
            if (log.isDebugEnabled()) {
                log.debug("Method initPage was previously invoked for page " // NOI18N
                        + page);
            }
            return;
        }

        int[] largeArray = new int[PAGESIZE];
        for (int i = 0; i < PAGESIZE; i++) {
            largeArray[i] = DEFAULT_MEM_VALUE;  // default contents
        }
        pageArray[page] = largeArray;
    }

    /**
     * Perform a read of a .hex file information into JAVA memory. Assumes that
     * the file is of the Intel "I8HEX" format or the similar Digitrax ".DMF"
     * format. Automatically infers the file type. Performs various checks upon
     * the incoming data to help ensure proper interpretation of the file and to
     * help detect corrupted files. Extracts "key/value" pair information from
     * comments for use by the invoking method.
     * <p>
     * Integrity checks include:
     * <ul>
     * <li>Identification of LOAD OFFSET field type from first record
     * <li>Verification that all subsequent records use the same LOAD OFFSET
     * field type
     * <li>Verification of checksum found at the end of each record
     * <li>Verification of supported record types
     * <li>Flagging of lines which are neither comment lines or records
     * <li>Identification of a missing EOF record
     * <li>Identification of any record after an EOF record
     * <li>Identification of a file without any data record
     * <li>Identification of any records which have extra characters after the
     * checksum
     * </ul>
     * <p>
     * When reading the file, {@link #readHex} infers the addressing format
     * from the first record found in the file, and future records are
     * interpreted using that addressing format. It is not necessary to
     * pre-configure the addressing format before reading the file. This is a
     * departure from previous versions of this method.
     * <p>
     * Blank lines are allowed and are ignored.
     * <p>
     * This code supports reading of files containing comments. Comment lines
     * which begin with '#' are ignored.
     * <p>
     * Comment lines which * begin with '!' may encode Key/Value pair
     * information. Such Key/Value pair information is used within the .DMF
     * format to provide configuration information for firmware update
     * mechanism. This class also extracts key/value pair comments "I8HEX"
     * format files. After successful completion of the {@link #readHex} call,
     * then the {@link #extractValueOfKey(String keyName)} method may be used to inspect individual key values.
     * <p>
     * Key/Value pair definition comment lines are of the format:
     * <p>
     * {@code ! KeyName: Value}
     *
     * @param filename string containing complete filename with path
     * @throws FileNotFoundException               if the file does not exist
     * @throws MemoryFileRecordLengthException     if a record line is too long
     *                                             or short
     * @throws MemoryFileChecksumException         if a record checksum does not
     *                                             match the computed record
     *                                             checksum
     * @throws MemoryFileUnknownRecordType         if a record contains an
     *                                             unsupported record type
     * @throws MemoryFileRecordContentException    if a record contains
     *                                             inappropriate characters
     * @throws MemoryFileNoEOFRecordException      if a file does not contain an
     *                                             EOF record
     * @throws MemoryFileNoDataRecordsException    if a file does not contain
     *                                             any data records
     * @throws MemoryFileRecordFoundAfterEOFRecord if a file contains records
     *                                             after the EOF record
     * @throws MemoryFileAddressingRangeException  if a file contains an
     *                                             Extended Linear Address
     *                                             record outside of the
     *                                             supported address range
     * @throws IOException                         if a file cannot be opened
     *                                             via newBufferedReader
     */
    public void readHex(String filename) throws FileNotFoundException,
            MemoryFileRecordLengthException, MemoryFileChecksumException,
            MemoryFileUnknownRecordType, MemoryFileRecordContentException,
            MemoryFileNoDataRecordsException, MemoryFileNoEOFRecordException,
            MemoryFileRecordFoundAfterEOFRecord, MemoryFileAddressingRangeException,
            IOException {
        readHex(new File(filename));
    }

    /**
     * Perform a read of a .hex file information into JAVA memory. Assumes that
     * the file is of the Intel "I8HEX" format or the similar Digitrax ".DMF"
     * format. Automatically infers the file type. Performs various checks upon
     * the incoming data to help ensure proper interpretation of the file and to
     * help detect corrupted files. Extracts "key/value" pair information from
     * comments for use by the invoking method.
     * <p>
     * Integrity checks include:
     * <ul>
     * <li>Identification of LOAD OFFSET field type from first record
     * <li>Verification that all subsequent records use the same LOAD OFFSET
     * field type
     * <li>Verification of checksum found at the end of each record
     * <li>Verification of supported record types
     * <li>Flagging of lines which are neither comment lines or records
     * <li>Identification of a missing EOF record
     * <li>Identification of any record after an EOF record
     * <li>Identification of a file without any data record
     * <li>Identification of any records which have extra characters after the
     * checksum
     * </ul><p>
     * When reading the file, {@link #readHex} infers the addressing format
     * from the first record found in the file, and future records are
     * interpreted using that addressing format. It is not necessary to
     * pre-configure the addressing format before reading the file. This is a
     * departure from previous versions of this method.
     * <p>
     * Blank lines are allowed and are ignored.
     * <p>
     * This code supports reading of files containing comments. Comment lines
     * which begin with '#' are ignored.
     * <p>
     * Comment lines which * begin with '!' may encode Key/Value pair
     * information. Such Key/Value pair information is used within the .DMF
     * format to provide configuration information for firmware update
     * mechanism. This class also extracts key/value pair comments "I8HEX"
     * format files. After successful completion of this method,
     * then the {@code #extractValueOfKey(String keyName)} method may be used to inspect individual key values.
     * <p>
     * Key/Value pair definition comment lines are of the format:
     * <p>
     * {@code ! KeyName: Value}
     *
     * @param file file to read
     * @throws FileNotFoundException               if the file does not exist
     * @throws MemoryFileRecordLengthException     if a record line is too long
     *                                             or short
     * @throws MemoryFileChecksumException         if a record checksum does not
     *                                             match the computed record
     *                                             checksum
     * @throws MemoryFileUnknownRecordType         if a record contains an
     *                                             unsupported record type
     * @throws MemoryFileRecordContentException    if a record contains
     *                                             inappropriate characters
     * @throws MemoryFileNoEOFRecordException      if a file does not contain an
     *                                             EOF record
     * @throws MemoryFileNoDataRecordsException    if a file does not contain
     *                                             any data records
     * @throws MemoryFileRecordFoundAfterEOFRecord if a file contains records
     *                                             after the EOF record
     * @throws MemoryFileAddressingRangeException  if a file contains an
     *                                             Extended Linear Address
     *                                             record outside of the
     *                                             supported address range
     * @throws IOException                         if a file cannot be opened
     *                                             via newBufferedReader
     */
    public void readHex(File file) throws FileNotFoundException,
            MemoryFileRecordLengthException, MemoryFileChecksumException,
            MemoryFileUnknownRecordType, MemoryFileRecordContentException,
            MemoryFileNoDataRecordsException, MemoryFileNoEOFRecordException,
            MemoryFileRecordFoundAfterEOFRecord, MemoryFileAddressingRangeException,
            IOException {
        BufferedReader fileStream;
        try {
            fileStream = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (IOException ex) {
            throw new FileNotFoundException(ex.toString());
        }
        
        this.clear();   // Ensure that the information storage is clear of any 
                        // previous contents
        currentPage = 0;
        loadOffsetFieldType = LoadOffsetFieldType.UNDEFINED;
        boolean foundDataRecords = false;
        boolean foundEOFRecord = false;

        keyValComments.clear();  // ensure that no key/value pair values are retained 
        //from a previous invocation.

        lineNum = 0;
        // begin reading the file
        try {
            //byte bval;
            int ival;
            String line;
            while ((line = fileStream.readLine()) != null) {
                // this loop reads one line per turn
                lineNum++;

                // decode line type
                int len = line.length();
                if (len < 1) {
                    continue; // skip empty lines
                }
                if (line.charAt(0) == LEADING_CHAR_COMMENT) {
                    // human comment.  Ignore it.
                } else if (line.charAt(0) == LEADING_CHAR_KEY_VALUE) {
                    // machine comment; store it to allow for key/value extraction
                    keyValComments.add(line);
                } else if (line.charAt(0) == LEADING_CHAR_RECORD_MARK) {
                    // hex file record - determine LOAD OFFSET field type (if not yet 
                    // then interpret the record based on its RECTYP

                    int indexOfLastAddressCharacter;
                    if (loadOffsetFieldType == LoadOffsetFieldType.UNDEFINED) {
                        // Infer the file's LOAD OFFSET field type from the first record.
                        // It is sufficient to infer the LOAD OFFSET field type once, then 
                        // interpret all future records as the same type without 
                        // checking the type again, because the checksum verfication
                        // uses the LOAD OFFSET field type as part of the 
                        // checksum verification.

                        loadOffsetFieldType = inferRecordAddressType(line);

                        if ((isLoadOffsetType16Bits())
                                && (isLoadOffsetType24Bits())) {
                            // could not infer a valid addressing type.
                            String message = "Could not infer addressing type from" // NOI18N
                                    + " line " + lineNum + "."; // NOI18N
                            log.error(message);
                            throw new MemoryFileRecordContentException(message);
                        }
                    }

                    // Determine the index of the last character of the line which 
                    // contains LOAD OFFSET field info
                    indexOfLastAddressCharacter = charsInAddress() + 2;
                    if (indexOfLastAddressCharacter < 0) {
                        // unknown LOAD OFFSET field type - cannot continue.
                        String message = "Fell thru with unknown loadOffsetFieldType value " // NOI18N
                                + loadOffsetFieldType + " for line" + lineNum + "."; // NOI18N
                        log.error(message);
                        throw new MemoryFileAddressingRangeException(message);
                    }

                    // extract the RECTYP.
                    int recordType = Integer.valueOf(line.substring(indexOfLastAddressCharacter + 1,
                            indexOfLastAddressCharacter + 3), 16).intValue();
                    if (log.isDebugEnabled()) {
                        log.debug("RECTYP = 0x" // NOI18N
                                + Integer.toHexString(recordType));
                    }

                    // verify record character count
                    int count = extractRecLen(line);
                    if (len != CHARS_IN_RECORD_MARK + CHARS_IN_RECORD_LENGTH
                            + charsInAddress()
                            + CHARS_IN_RECORD_TYPE
                            + (count * CHARS_IN_EACH_DATA_BYTE) + CHARS_IN_CHECKSUM) {
                        // line length error - invalid record or invalid data 
                        // length byte or incorrect LOAD OFFSET field type
                        String message
                                = "Data record line length is incorrect for " // NOI18N
                                + "inferred addressing type and for data " // NOI18N
                                + "count field in line " + lineNum;// NOI18N
                        log.error(message);
                        throw new MemoryFileRecordLengthException(message);
                    }

                    // verify the checksum now that we know the RECTYP.
                    // Do this by calculating the checksum of all characters on 
                    //line (except the ':' record mark), which should result in 
                    // a computed checksum value of 0
                    int computedChecksum = calculate8BitChecksum(line.substring(CHARS_IN_RECORD_MARK));
                    if (computedChecksum != 0x00) {
                        // line's checksum is incorrect.  Find checksum of 
                        // all but the checksum bytes
                        computedChecksum = calculate8BitChecksum(
                                line.substring(
                                        CHARS_IN_RECORD_MARK,
                                        line.length()
                                        - CHARS_IN_RECORD_MARK
                                        - CHARS_IN_CHECKSUM + 1)
                        );
                        int expectedChecksum = Integer.parseInt(line.substring(line.length() - 2), 16);
                        String message = "Record checksum error in line " // NOI18N
                                + lineNum
                                + " - computed checksum = 0x" // NOI18N
                                + Integer.toHexString(computedChecksum)
                                + ", expected checksum = 0x" // NOI18N
                                + Integer.toHexString(expectedChecksum)
                                + "."; // NOI18N
                        log.error(message);
                        throw new MemoryFileChecksumException(message);
                    }

                    if (recordType == RECTYP_DATA_RECORD) {
                        // Record Type 0x00
                        if (foundEOFRecord) {
                            // problem - data record happened after an EOF record was parsed
                            String message = "Found a Data record in line " // NOI18N
                                    + lineNum + " after the EOF record"; // NOI18N
                            log.error(message);
                            throw new MemoryFileRecordFoundAfterEOFRecord(message);
                        }

                        int recordAddress = extractLoadOffset(line);

                        recordAddress &= (isLoadOffsetType24Bits())
                                ? 0x00FFFFFF : 0x0000FFFF;

                        // compute effective address (assumes cannot have 
                        // non-zero values in both curExtLinAddr and 
                        // curExtSegAddr)
                        int effectiveAddress = recordAddress + curExtLinAddr + curExtSegAddr;

                        if (addressAndCountIsOk(effectiveAddress, count) == false) {
                            // data crosses memory boundary that can be mis-interpreted.
                            // So refuse the file.
                            String message = "Data crosses boundary which could lead to " // NOI18N
                                    + " mis-interpretation.  Aborting read at line " // NOI18N
                                    + line;
                            log.error(message);
                            throw new MemoryFileAddressingRangeException(message);
                        }

                        int effectivePage = effectiveAddress / PAGESIZE;
                        if (!isPageInitialized(effectivePage)) {
                            initPage(effectivePage);
                            log.debug("effective address 0x{} is causing change to segment 0x{}", // NOI18N
                                    Integer.toHexString(effectiveAddress),
                                    Integer.toHexString(effectivePage));
                        }
                        int effectiveOffset = effectiveAddress % PAGESIZE;

                        log.debug("Effective address 0x{}, effective page 0x{}, effective offset 0x{}",
                                Integer.toHexString(effectiveAddress),
                                Integer.toHexString(effectivePage),
                                Integer.toHexString(effectiveOffset));
                        for (int i = 0; i < count; ++i) {
                            int startIndex = indexOfLastAddressCharacter + 3 + (i * 2);
                            // parse as hex into integer, then convert to byte
                            ival = Integer.valueOf(line.substring(startIndex, startIndex + 2), 16).intValue();
                            pageArray[effectivePage][effectiveOffset++] = ival;
                            hasData = true;
                        }
                        foundDataRecords = true;

                    } else if (recordType == RECTYP_EXTENDED_SEGMENT_ADDRESS_RECORD) {
                        // parse Extended Segment Address record to check for
                        // validity
                        if (foundEOFRecord) {
                            String message
                                    = "Found a Extended Segment Address record in line " // NOI18N
                                    + lineNum
                                    + " after the EOF record"; // NOI18N
                            log.error(message);
                            throw new MemoryFileRecordFoundAfterEOFRecord(message);
                        }

                        int datacount = extractRecLen(line);
                        if (datacount != 2) {
                            String message = "Extended Segment Address record " // NOI18N
                                    + "did not have 16 bits of data content." // NOI18N
                                    + lineNum;
                            log.error(message);
                            throw new MemoryFileRecordContentException(message);
                        }
                        int startpoint = indexOfLastAddressCharacter + 3;
                        // compute page number from '20-bit segment address' in record
                        int newPage = 16 * Integer.valueOf(line.substring(startpoint,
                                (startpoint + 2 * datacount)), 16).intValue();

                        // check for an allowed segment value
                        if (newPage != 0) {
                            String message = "Unsupported Extended Segment Address " // NOI18N
                                    + "Record data value 0x" // NOI18N
                                    + Integer.toHexString(newPage)
                                    + " in line " + lineNum; // NOI18N
                            log.error(message);
                            throw new MemoryFileAddressingRangeException(message);
                        }
                        curExtLinAddr = 0;
                        curExtSegAddr = newPage;
                        if (newPage != currentPage) {
                            currentPage = newPage;
                            initPage(currentPage);
                        }

                    } else if (recordType == RECTYP_EXTENDED_LINEAR_ADDRESS_RECORD) {
                        // Record Type 0x04
                        if (foundEOFRecord) {
                            String message
                                    = "Found a Extended Linear Address record in line " // NOI18N
                                    + lineNum
                                    + " after the EOF record"; // NOI18N
                            log.error(message);
                            throw new MemoryFileRecordFoundAfterEOFRecord(message);
                        }

                        // validate that LOAD OFFSET field of record is all zeros.
                        if (extractLoadOffset(line) != 0) {
                            String message = "Extended Linear Address record has " // NOI18N
                                    + "non-zero LOAD OFFSET field." // NOI18N
                                    + lineNum;
                            log.error(message);
                            throw new MemoryFileRecordContentException(message);
                        }

                        // Allow non-zero Extended Linear Address value ONLY if 16-bit addressing!
                        int datacount = extractRecLen(line);
                        if (datacount != 2) {
                            String message = "Expect data payload length of 2, " // NOI18N
                                    + "found RECLEN value of " + // NOI18N
                                    +extractRecLen(line)
                                    + " in line " + lineNum; // NOI18N
                            log.error(message);
                            throw new MemoryFileRecordContentException(message);
                        }
                        int startpoint = indexOfLastAddressCharacter + 3;
                        int tempPage = Integer.valueOf(line.substring(startpoint,
                                (startpoint + 2 * datacount)), 16).intValue();

                        if ((tempPage != 0) && (isLoadOffsetType24Bits())) {
                            // disallow non-zero extended linear address if 24-bit addressing
                            String message = "Extended Linear Address record with non-zero" // NOI18N
                                    + "data field in line " // NOI18N
                                    + lineNum
                                    + " is not allowed in files using " // NOI18N
                                    + "24-bit LOAD OFFSET field.";  // NOI18N
                            log.error(message); // NOI18N
                            throw new MemoryFileRecordContentException(message);
                        } else if (tempPage < PAGES) {
                            curExtLinAddr = tempPage * 65536;
                            curExtSegAddr = 0;
                            currentPage = tempPage;
                            initPage(currentPage);
                            if (log.isDebugEnabled()) {
                                log.debug("New page 0x" + Integer.toHexString(currentPage)); // NOI18N
                            } // NOI18N
                        } else {
                            String message = "Page number 0x" // NOI18N
                                    + Integer.toHexString(tempPage)
                                    + " specified in line number " // NOI18N
                                    + lineNum
                                    + " is beyond the supported 24-bit address range."; // NOI18N;
                            log.error(message);
                            throw new MemoryFileAddressingRangeException(message);
                        }

                    } else if (recordType == RECTYP_EOF_RECORD) {
                        if ((extractRecLen(line) != 0)
                                || (extractLoadOffset(line) != 0)) {
                            String message = "Illegal EOF record form in line " // NOI18N
                                    + lineNum;
                            log.error(message);
                            throw new MemoryFileRecordContentException(message);
                        }

                        foundEOFRecord = true;
                        continue; // not record we need to handle
                    } else {
                        String message = "Unknown RECTYP 0x" // NOI18N
                                + Integer.toHexString(recordType)
                                + " was found in line " // NOI18N
                                + lineNum + ".  Aborting file read."; // NOI18N
                        log.error(message);
                        throw new MemoryFileUnknownRecordType(message);
                    }
                    // end parsing hex file record
                } else {
                    String message = "Unknown line type in line " + lineNum + "."; // NOI18N
                    log.error(message);
                    throw new MemoryFileUnknownRecordType(message);
                }
            }
        } catch (IOException e) {
            log.error("Exception reading file", e);
        } // NOI18N
        finally {
            try {
                fileStream.close();
            } catch (IOException e2) {
                log.error("Exception closing file", e2);
            } // NOI18N
        }
        if (!foundDataRecords) {
            String message = "No Data Records found in file - aborting."; // NOI18N
            log.error(message);
            throw new MemoryFileNoDataRecordsException(message);
        } else if (!foundEOFRecord) {  // found Data Records, but no EOF
            String message = "No EOF Record found in file - aborting."; // NOI18N
            log.error(message);
            throw new MemoryFileNoEOFRecordException(message);
        }
    }

    /**
     * Sends a character stream of an image of a programmatic representation of
     * memory in the Intel "I8HEX" file format to a Writer.
     * <p>
     * Number of bytes of data per data record is fixed at 16. Does not write
     * any comment information to the file.
     * <p>
     * This method generates only RECTYPs "00" and "01", and does not generate
     * any comment lines in its output.
     *
     * @param w Writer to which the character stream is sent
     * @throws IOException                         upon file access problem
     * @throws MemoryFileAddressingFormatException if unsupported addressing
     *                                             format
     */
    public void writeHex(Writer w) throws IOException, MemoryFileAddressingFormatException {
        writeHex(w, 16);
    }

    /**
     * Sends a character stream of key/value pairs (if requested) and an image
     * of a programmatic representation of memory in either the Intel "I8HEX" or
     * Digitrax ".DMF" file format to a Writer.
     * <p>
     * When selected for writing, the key/value pairs are provided at the
     * beginning of the character stream. Note that comments of the key/value
     * format implemented here is not in compliance with the "I8HEX" format.
     * <p>
     * The "I8HEX" format is used when the {@link #loadOffsetFieldType} is
     * configured for 16-bit addresses in the record LOAD OFFSET field. The
     * ".DMF" format is used when the {@link #loadOffsetFieldType} is
     * configured for 24-bit addresses in the record LOAD OFFSET field.
     * <p>
     * The method generates only RECTYPs "00" and "01", and does not generate
     * any comment lines in its output.
     *
     * @param writer       Writer to which the character stream is sent
     * @param writeKeyVals determines whether key/value pairs (if any) are
     *                     written at the beginning of the stream
     * @param blockSize    is the maximum number of bytes defined in a data
     *                     record
     * @throws IOException                         upon file access problem
     * @throws MemoryFileAddressingFormatException if unsupported addressing
     *                                             format
     */
    public void writeHex(Writer writer, boolean writeKeyVals, int blockSize)
            throws IOException, MemoryFileAddressingFormatException {
        if (writeKeyVals) {
            writeComments(writer);
        }
        writeHex(writer, blockSize);
    }

    /**
     * Sends a character stream of an image of a programmatic representation of
     * memory in either the Intel "I8HEX" or Digitrax ".DMF" file format to a
     * Writer.
     * <p>
     * The "I8HEX" format is used when the{@link #loadOffsetFieldType} is
     * configured for 16-bit addresses in the record LOAD OFFSET field. The
     * ".DMF" format is used when the {@link #loadOffsetFieldType} is
     * configured for 24-bit addresses in the record LOAD OFFSET field.
     * <p>
     * The method generates only RECTYPs "00" and "01", and does not generate
     * any comment lines in its output.
     *
     * @param writer    Writer to which the character stream is sent
     * @param blockSize is the maximum number of bytes defined in a data record
     * @throws IOException                         upon file access problem
     * @throws MemoryFileAddressingFormatException if unsupported addressing
     *                                             format
     */
    private void writeHex(Writer writer, int blockSize)
            throws IOException, MemoryFileAddressingFormatException {
        int blocksize = blockSize; // number of bytes per record in .hex file
        // validate Address format selection
        if ((!isLoadOffsetType16Bits())
                && (!isLoadOffsetType24Bits())) {
            String message = "Invalid loadOffsetFieldType at writeHex invocation"; // NOI18N
            log.error(message);
            throw new MemoryFileAddressingFormatException(message);
        }

        for (int segment = 0; segment < PAGES; ++segment) {
            if (pageArray[segment] != null) {
                if ((segment != 0) && (isLoadOffsetType16Bits())) {
                    // write an extended linear address record for 16-bit LOAD OFFSET field size files only
                    StringBuffer output = new StringBuffer(":0200000400"); // NOI18N
                    output.append(StringUtil.twoHexFromInt(segment));

                    int checksum = calculate8BitChecksum(output.substring(CHARS_IN_RECORD_MARK));
                    output.append(StringUtil.twoHexFromInt(checksum));
                    output.append("\n"); // NOI18N

                    writer.write(output.toString());
                }
                for (int i = 0; i < pageArray[segment].length - blocksize + 1; i += blocksize) {
                    if (log.isDebugEnabled()) {
                        log.debug("write at 0x" + Integer.toHexString(i)); // NOI18N
                    }
                    // see if need to write the current block
                    boolean write = false;
                    int startOffset = -1;

                    // Avoid producing a record which spans the natural alignment of
                    // addresses with respect to blocksize.  In other words, do not produce
                    // a data record that spans both sides of an Address which is a natural
                    // mulitple of blocksize.
                    for (int j = i; j < (i + blocksize) - ((i + blocksize) % blocksize); j++) {
                        if (pageArray[segment][j] >= 0) {
                            write = true;
                            if (startOffset < 0) {
                                startOffset = j;
                                if (log.isDebugEnabled()) {
                                    log.debug("startOffset = 0x" + Integer.toHexString(startOffset)); // NOI18N
                                }
                            }
                        }
                        if (((write == true) && (j == i + (blocksize - 1)))
                                || ((write == true) && (pageArray[segment][j] < 0))) {
                            // got to end of block size, or got a gap in the data
                            // need to write out at least a partial block of data
                            int addressForAddressField = startOffset;
                            if (isLoadOffsetType24Bits()) {
                                addressForAddressField += segment * PAGESIZE;
                            }
                            int addrMostSByte = (addressForAddressField) / 65536;
                            int addrMidSByte = ((addressForAddressField) - (65536 * addrMostSByte)) / 256;
                            int addrLeastSByte = (addressForAddressField) - (256 * addrMidSByte) - (65536 * addrMostSByte);
                            int count = j - startOffset;
                            if ( j == i + (blocksize - 1) ) {
                                count++;
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Writing Address " + startOffset + " (" // NOI18N
                                        + (isLoadOffsetType24Bits() ? "24" : "16") // NOI18N
                                        + "bit Address) count " // NOI18N
                                        + count
                                );
                            }

                            StringBuffer output = new StringBuffer(":"); // NOI18N
                            output.append(StringUtil.twoHexFromInt(count));
                            if (isLoadOffsetType24Bits()) {
                                output.append(StringUtil.twoHexFromInt(addrMostSByte));
                            }
                            output.append(StringUtil.twoHexFromInt(addrMidSByte));
                            output.append(StringUtil.twoHexFromInt(addrLeastSByte));
                            output.append(STRING_DATA_RECTYP);

                            for (int k = 0; k < count; ++k) {
                                int val = pageArray[segment][startOffset + k];
                                output.append(StringUtil.twoHexFromInt(val));
                            }
                            int checksum = calculate8BitChecksum(output.substring(CHARS_IN_RECORD_MARK));
                            output.append(StringUtil.twoHexFromInt(checksum));
                            output.append("\n"); // NOI18N
                            writer.write(output.toString());
                            write = false;
                            startOffset = -1;
                        }
                    }
                    if (!write) {
                        continue; // no, we don't
                    }
                }
            }
        }
        // write last line & close
        writer.write((isLoadOffsetType24Bits()) ? ":0000000001FF\n" : ":00000001FF\n"); // NOI18N
        writer.flush();
    }

    /**
     * Return the address of the next location containing data, including the
     * location in the argument
     *
     * @param location indicates the address from which the next location is
     *                 determined
     * @return the next location
     */
    public int nextContent(int location) {
        currentPage = location / PAGESIZE;
        int offset = location % PAGESIZE;
        for (; currentPage < PAGES; currentPage++) {
            if (pageArray[currentPage] != null) {
                for (; offset < pageArray[currentPage].length; offset++) {
                    if (pageArray[currentPage][offset] != DEFAULT_MEM_VALUE) {
                        return offset + currentPage * PAGESIZE;
                    }
                }
            }
            offset = 0;
        }
        return -1;
    }

    /**
     * Modifies the programmatic representation of memory to reflect a specified
     * value.
     *
     * @param location location within programmatic representation of memory to
     *                 modify
     * @param value    value to be placed at location within programmatic
     *                 representation of memory
     */
    public void setLocation(int location, int value) {
        currentPage = location / PAGESIZE;

        pageArray[currentPage][location % PAGESIZE] = value;
        hasData = true;
    }

    /**
     * Queries the programmatic representation of memory to determine if
     * location is represented.
     *
     * @param location location within programmatic representation of memory to
     *                 inspect
     * @return true if location exists within programmatic representation of
     *         memory
     */
    public boolean locationInUse(int location) {
        currentPage = location / PAGESIZE;
        if (pageArray[currentPage] == null) {
            return false;
        }
        try {
            return pageArray[currentPage][location % PAGESIZE] != DEFAULT_MEM_VALUE;
        } catch (Exception e) {
            log.error("error in locationInUse " + currentPage + " " + location, e); // NOI18N
            return false;
        }
    }

    /**
     * Returns the value from the programmatic representation of memory for the
     * specified location. Returns -1 if the specified location is not currently
     * represented in the programmatic representation of memory.
     *
     * @param location location within programmatic representation of memory to
     *                 report
     * @return value found at the specified location.
     */
    public int getLocation(int location) {
        currentPage = location / PAGESIZE;
        if (pageArray[currentPage] == null) {
            log.error("Error in getLocation(0x" // NOI18N
                    + Integer.toHexString(location)
                    + "): accessed uninitialized page " // NOI18N
                    + currentPage);
            return DEFAULT_MEM_VALUE;
        }
        try {
            return pageArray[currentPage][location % PAGESIZE];
        } catch (Exception e) {
            log.error("Error in getLocation(0x" // NOI18N
                    + Integer.toHexString(location)
                    + "); computed (current page 0x" // NOI18N
                    + Integer.toHexString(currentPage)
                    + "): exception ", e); // NOI18N
            return 0;
        }
    }

    /**
     * Reports whether the object has not been initialized with any data.
     *
     * @return false if object contains data, true if no data stored in object.
     */
    public boolean isEmpty() {
        return !hasData;
    }

    /**
     * Infers addressing type from contents of string containing a record.
     * <p>
     * Returns ADDRESSFIELDSIZEUNKNOWN if
     * <ul>
     * <li>the recordString does not begin with ':'
     * <li>the length of recordString is not appropriate to define an integral
     * number of bytes
     * <li>the recordString checksum does not match a checksum computed for the
     * recordString
     * <li>if the record type extracted after inferring the addressing type is
     * an unsupported record type
     * <li>if the length of recordString did not match the length expected for
     * the inferred addressing type.
     * <ul>
     *
     * @param recordString the ASCII record, including the leading ':'
     * @return the inferred addressing type, or ADDRESSFIELDSIZEUNKNOWN if the
     *         addressing type cannot be inferred
     */
    private LoadOffsetFieldType inferRecordAddressType(String recordString) {
        if (recordString.charAt(0) != LEADING_CHAR_RECORD_MARK) {
            log.error("Cannot infer record addressing type because line " // NOI18N
                    + lineNum
                    + " is not a record."); // NOI18N
            return LoadOffsetFieldType.ADDRESSFIELDSIZEUNKNOWN;
        }
        String r = recordString.substring(CHARS_IN_RECORD_MARK);  // create a string without the leading ':'
        int len = r.length();
        if (((len + 1) / 2) != (len / 2)) {
            // Not an even number of characters in the line (after removing the ':'
            // character), so must be a bad record.
            log.error("Cannot infer record addressing type because line " // NOI18N
                    + lineNum
                    + " does not " // NOI18N
                    + "have the correct number of characters."); // NOI18N
            return LoadOffsetFieldType.ADDRESSFIELDSIZEUNKNOWN;
        }

        int datalen = Integer.parseInt(r.substring(0, 2), 16);
        int checksumInRecord = Integer.parseInt(r.substring(len - 2, len), 16);

        // Compute the checksum of the record
        int calculatedChecksum = calculate8BitChecksum(recordString.substring(CHARS_IN_RECORD_MARK,
                recordString.length() - CHARS_IN_CHECKSUM));

        // Return if record checksum value does not match calculated checksum
        if (calculatedChecksum != checksumInRecord) {
            log.error("Cannot infer record addressing type because line " // NOI18N
                    + lineNum
                    + " does not have the correct checksum (expect 0x" // NOI18N
                    + Integer.toHexString(calculatedChecksum)
                    + ", found CHKSUM = 0x" // NOI18N
                    + Integer.toHexString(checksumInRecord)
                    + ")"); // NOI18N
            return LoadOffsetFieldType.ADDRESSFIELDSIZEUNKNOWN;
        }

        // Checksum is ok, so can check length of line versus address size.
        // Compute expected line lengths based on possible address sizes
        int computedLenIf16Bit = 2 + 4 + 2 + (datalen * 2) + 2;
        int computedLenIf24Bit = computedLenIf16Bit + 2;

        // Determine if record line length matches any of the expected line lengths
        if (computedLenIf16Bit == len) {
            //inferred 16-bit addressing based on length.  Check the record type.
            if (isSupportedRecordType(Integer.parseInt(r.substring(6, 8), 16))) {
                return LoadOffsetFieldType.ADDRESSFIELDSIZE16BITS;
            } else {
                log.error("Cannot infer record addressing type in line " // NOI18N
                        + lineNum
                        + " because record " // NOI18N
                        + "type is an unsupported record type."); // NOI18N
                return LoadOffsetFieldType.ADDRESSFIELDSIZEUNKNOWN;
            }
        }

        if (computedLenIf24Bit == len) {
            //inferred 24-bit addressing based on length.  Check the record type.
            if (isSupportedRecordType(Integer.parseInt(r.substring(8, 10), 16))) {
                return LoadOffsetFieldType.ADDRESSFIELDSIZE24BITS;
            } else {
                log.error("Cannot infer record addressing type in line " // NOI18N
                        + lineNum
                        + " because record type is an unsupported record type."); // NOI18N
                return LoadOffsetFieldType.ADDRESSFIELDSIZEUNKNOWN;
            }
        }

        // Record length did not match a calculated line length for any supported
        // addressing type.  Report unknown record addressing type.
        return LoadOffsetFieldType.ADDRESSFIELDSIZEUNKNOWN;
    }

    /**
     * Calculates an 8-bit checksum value from a string which uses sequential
     * pairs of ASCII characters to encode the hexadecimal values of a sequence
     * of bytes.
     * <p>
     * When used to calculate the checksum of a record in I8HEX or similar
     * format, the infoString parameter is expected to include only those
     * characters which are used for calculation of the checksum. The "record
     * mark" at the beginning of a record should not be included in the
     * infoString. Similarly, the checksum at the end of a record should
     * generally not be included in the infoString.
     * <p>
     * An example infoString value might be: 020000040010
     * <p>
     * In case of an invalid infoString, the returned checksum is -1.
     * <p>
     * If using this method to verify the checksum of a record, the infoString
     * should include the record Checksum characters. Then the invoking method
     * may check for a non-zero return value to indicate a checksum error.
     *
     * @param infoString a string of characters for which the checksum is
     *                   calculated
     * @return the calculated 8-bit checksum, or -1 if not a valid infoString
     */
    private int calculate8BitChecksum(String infoString) {
        // check length of record content for an even number of characters
        int len = infoString.length();
        if (((len + 1) / 2) != (len / 2)) {
            return -1;
        }

        // Compute the checksum of the record, omitting the last two characters.
        int calculatedChecksum = 0;
        for (int i = 0; i < len; i += 2) {
            calculatedChecksum += Integer.parseInt(infoString.substring(i, i + 2), 16);
        }
        // Safely remove extraneous bits from the calculated checksum to create an 
        // 8-bit result.
        return (0xFF & (0x100 - (calculatedChecksum & 0xFF)));
    }

    /**
     * Determines if a given amount of data will pass a segment boundary when
     * added to the memory image beginning at a given address.
     *
     * @param addr  address for begin of a sequence of bytes
     * @param count number of bytes
     * @return true if string of bytes will not cross into another page, else
     *         false.
     */
    private boolean addressAndCountIsOk(int addr, int count) {
        int beginPage = addr / PAGESIZE;
        int endPage = ((addr + count - 1) / PAGESIZE);
        log.debug("Effective Record Addr = 0x" + Integer.toHexString(addr) // NOI18N
                + " count = " + count // NOI18N
                + " BeginPage = " + beginPage // NOI18N
                + " endpage = " + endPage); // NOI18N
        return (beginPage == endPage);
    }

    /**
     * Finds the Value for a specified Key if that Key is found in the list of
     * Key/Value pair comment lines. The list of Key/Value pair comment lines is
     * created while the input file is processed.
     * <p>
     * Key/value pair information is extractable only from comments of the form:
     * <p>
     * {@code ! Key/Value}
     *
     * @param keyName Key/value comment line, including the leading "! "
     * @return String containing Key name
     */
    public String extractValueOfKey(String keyName) {
        for (int i = 0; i < keyValComments.size(); i++) {
            String t = keyValComments.get(i);
            String targetedKey = "! " + keyName + ": "; // NOI18N
            if (t.startsWith(targetedKey)) {
                int f = t.indexOf(": "); // NOI18N
                String value = t.substring(f + 2, t.length());
                if (log.isDebugEnabled()) {
                    log.debug("Key " + keyName + " was found in firmware image with value '" + value + "'"); // NOI18N
                }
                return value;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Key " + keyName + " is not defined in firmware image"); // NOI18N
        }
        return null;

    }

    /**
     * Finds the index of the specified key within the array containing
     * key/value comments
     *
     * @param keyName Key to search for in the internal storage
     * @return index in the arraylist for the specified key, or -1 if the key is
     *         not found in the list
     */
    private int findKeyCommentIndex(String keyName) {
        for (int i = 0; i < keyValComments.size(); i++) {
            String t = keyValComments.get(i);
            String targetedKey = "! " + keyName + ": "; // NOI18N
            if (t.startsWith(targetedKey)) {
                return i;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Did not find key " + keyName); // NOI18N
        }
        return -1;
    }

    /**
     * Updates the internal key/value storage to reflect the parameters. If the
     * key already exists, its value is updated based on the parameter. If the
     * key does not exist, a new key/value pair comment is added to the
     * key/value storage list.
     *
     * @param keyName key to use
     * @param value   value to store
     */
    public void addKeyValueComment(String keyName, String value) {
        int keyIndex;
        if ((keyIndex = findKeyCommentIndex(keyName)) < 0) {
            // key does not already exist.  Can simply add the key/value comment
            keyValComments.add("! " + keyName + ": " + value + "\n"); // NOI18N
            return;
        }
        log.warn("Key " + keyName + " already exists in key/value set.  Overriding previous value!"); // NOI18N
        keyValComments.set(keyIndex, "! " + keyName + ": " + value + "\n"); // NOI18N
    }

    public enum LoadOffsetFieldType {

        UNDEFINED,
        ADDRESSFIELDSIZE16BITS,
        ADDRESSFIELDSIZE24BITS,
        ADDRESSFIELDSIZEUNKNOWN
    }

    /**
     * Configures the Addressing format used in the LOAD OFFSET field when
     * writing to a .hex file using the {@link #writeHex} method.
     * <p>
     * Note that the {@link #readHex} method infers the addressing format
     * from the first record in the file and updates the stored address format
     * based on the format found in the file.
     *
     * @param addressingType addressing type to use
     */
    public void setAddressFormat(LoadOffsetFieldType addressingType) {
        loadOffsetFieldType = addressingType;
    }

    /**
     * Returns the current addressing format setting. The current setting is
     * established by the last occurrence of the {@link #setAddressFormat}
     * method or {@link #readHex} method invocation.
     *
     * @return the current Addressing format setting
     */
    public LoadOffsetFieldType getCurrentAddressFormat() {
        return loadOffsetFieldType;
    }

    /**
     * Writes key/data pair information to an output file
     * <p>
     * Since the key/value metadata is typically presented at the beginning of a
     * firmware file, the method would typically be invoked before invocation of
     * the writeHex method.
     * <p>
     * @param writer Writer to which the character stream is sent
     * @throws IOException if problems writing data to file
     */
    public void writeComments(Writer writer) throws IOException {
        for (String s : keyValComments) {
            writer.write(s);
        }
    }

    private boolean isLoadOffsetType24Bits() {
        return loadOffsetFieldType == LoadOffsetFieldType.ADDRESSFIELDSIZE24BITS;
    }

    private boolean isLoadOffsetType16Bits() {
        return loadOffsetFieldType == LoadOffsetFieldType.ADDRESSFIELDSIZE16BITS;
    }

    private boolean isSupportedRecordType(int recordType) {
        switch (recordType) {
            case RECTYP_DATA_RECORD:
            case RECTYP_EXTENDED_LINEAR_ADDRESS_RECORD:
            case RECTYP_EOF_RECORD:
            case RECTYP_EXTENDED_SEGMENT_ADDRESS_RECORD:
                return true;
            default:
                return false;
        }
    }

    private int extractRecLen(String line) {
        return Integer.valueOf(line.substring(CHARS_IN_RECORD_MARK,
                CHARS_IN_RECORD_MARK + CHARS_IN_RECORD_LENGTH), 16).intValue();
    }

    private int charsInAddress() {
        if (isLoadOffsetType24Bits()) {
            return CHARS_IN_24_BIT_ADDRESS;
        } else if (isLoadOffsetType16Bits()) {
            return CHARS_IN_16_BIT_ADDRESS;
        } else {
            return -999;
        }
    }

    private int extractLoadOffset(String line) {
        return Integer.parseInt(
                line.substring(CHARS_IN_RECORD_MARK + CHARS_IN_RECORD_LENGTH,
                        CHARS_IN_RECORD_MARK + CHARS_IN_RECORD_LENGTH + charsInAddress()), 16);
    }
    
    /**
     * Generalized class from which detailed exceptions are derived.
     */
    public class MemoryFileException extends jmri.JmriException {

        public MemoryFileException() {
            super();
        }

        public MemoryFileException(String s) {
            super(s);
        }
    }

    /**
     * An exception for a record which has incorrect checksum.
     */
    public class MemoryFileChecksumException extends MemoryFileException {

        public MemoryFileChecksumException() {
            super();
        }

        public MemoryFileChecksumException(String s) {
            super(s);
        }
    }

    /**
     * An exception for a record containing a record type which is not
     * supported.
     */
    public class MemoryFileUnknownRecordType extends MemoryFileException {

        public MemoryFileUnknownRecordType() {
            super();
        }

        public MemoryFileUnknownRecordType(String s) {
            super(s);
        }
    }

    /**
     * An exception for a record which has content which cannot be parsed.
     * <p>
     * Possible examples may include records which include characters other than
     * ASCII characters associated with hexadecimal digits and the initial ':'
     * character, trailing spaces, etc.
     */
    public class MemoryFileRecordContentException extends MemoryFileException {

        public MemoryFileRecordContentException() {
            super();
        }

        public MemoryFileRecordContentException(String s) {
            super(s);
        }
    }

    /**
     * An exception for a data record where there are too many or too few
     * characters versus the number of characters expected based on the record
     * type field, LOAD OFFSET field size, and data count field.
     */
    public class MemoryFileRecordLengthException extends MemoryFileException {

        public MemoryFileRecordLengthException() {
            super();
        }

        public MemoryFileRecordLengthException(String s) {
            super(s);
        }
    }

    /**
     * An exception for an unsupported addressing format
     */
    public class MemoryFileAddressingFormatException extends MemoryFileException {

        public MemoryFileAddressingFormatException() {
            super();
        }

        public MemoryFileAddressingFormatException(String s) {
            super(s);
        }
    }

    /**
     * An exception for an address outside of the supported range
     */
    public class MemoryFileAddressingRangeException extends MemoryFileException {

        public MemoryFileAddressingRangeException() {
            super();
        }

        public MemoryFileAddressingRangeException(String s) {
            super(s);
        }
    }

    /**
     * An exception for a file with no data records
     */
    public class MemoryFileNoDataRecordsException extends MemoryFileException {

        public MemoryFileNoDataRecordsException() {
            super();
        }

        public MemoryFileNoDataRecordsException(String s) {
            super(s);
        }
    }

    /**
     * An exception for a file without an end-of-file record
     */
    public class MemoryFileNoEOFRecordException extends MemoryFileException {

        public MemoryFileNoEOFRecordException() {
            super();
        }

        public MemoryFileNoEOFRecordException(String s) {
            super(s);
        }
    }

    /**
     * An exception for a file containing at least one record after the EOF
     * record
     */
    public class MemoryFileRecordFoundAfterEOFRecord extends MemoryFileException {

        public MemoryFileRecordFoundAfterEOFRecord() {
            super();
        }

        public MemoryFileRecordFoundAfterEOFRecord(String s) {
            super(s);
        }
    }

    /**
     * Summarize contents
     */
    @Override
    public String toString() {
        StringBuffer retval = new StringBuffer("Pages occupied: "); // NOI18N
        for (int page=0; page<PAGES; page++) {
            if (isPageInitialized(page)) {
                retval.append(page);
                retval.append(" ");
            }
        }
        return new String(retval);
    }

    /**
     * Clear out an imported Firmware File.
     * 
     * This may be used, when the instantiating object has evaluated the contents of 
     * a firmware file and found it to be inappropriate for updating to a device, 
     * to clear out the firmware image so that there is no chance that it can be
     * updated to the device.
     * 
     */
    public void clear() {
        log.info("Clearing a MemoryContents object by program request.");
        currentPage = -1;
        hasData = false;
        curExtLinAddr = 0;
        curExtSegAddr = 0;
        keyValComments = new ArrayList<String>(1);
        for (int i = 0 ; i < pageArray.length; ++i) {
            pageArray[i] = null;
        }
        
    }

    private final static Logger log = LoggerFactory.getLogger(MemoryContents.class);
}
