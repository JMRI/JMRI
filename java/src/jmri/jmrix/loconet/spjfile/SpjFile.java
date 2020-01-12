package jmri.jmrix.loconet.spjfile;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import jmri.jmrix.loconet.sdf.SdfBuffer;

/**
 * Provide tools for reading, writing and accessing Digitrax SPJ files.
 * <p>
 * Four-byte quantities in SPJ files are little-endian.
 *
 * @author Bob Jacobsen Copyright (C) 2006, 2009
 */
public class SpjFile {

    public SpjFile(File file) {
        this.file = file;
    }

    /**
     * Number of headers present in the file.
     *
     * @return -1 if error
     */
    public int numHeaders() {
        if (headers != null && h0 != null) {
            return h0.numHeaders();
        } else {
            return -1;
        }
    }

    public String getComment() {
        return h0.getComment();
    }

    public Header getHeader(int index) {
        return headers[index];
    }

    public Header findSdfHeader() {
        int n = numHeaders();
        for (int i = 1; i < n; i++) {
            if (headers[i].isSDF()) {
                return headers[i];
            }
        }
        return null;
    }

    /**
     * Find the map entry (character string) that corresponds to a particular
     * handle number.
     */
    public String getMapEntry(int i) {
        log.debug("getMapEntry({})", i);
        loadMapCache();
        String wanted = "" + i + " ";
        for (int j = 0; j < mapCache.length; j++) {
            if (mapCache[j].startsWith(wanted)) {
                return mapCache[j].substring(wanted.length());
            }
        }
        return null;
    }

    String[] mapCache = null;

    void loadMapCache() {
        if (mapCache != null) {
            return;
        }

        // find the map entries
        log.debug("loading map cache");
        int map;
        for (map = 1; map < numHeaders(); map++) {
            if (headers[map].isMap()) {
                break;
            }
        }
        // map holds the map index, hopefully
        if (map > numHeaders()) {
            log.error("Did not find map data");
            return;
        }

        // here found it, count lines
        byte[] buffer = headers[map].getByteArray();
        log.debug("map buffer length {}", buffer.length);
        int count = 0;
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == 0x0D) {
                count++;
            }
        }

        mapCache = new String[count];

        log.debug("found {} map entries", count);

        int start = 0;
        int end = 0;
        int index = 0;

        // loop through the string, look for each line
        log.debug("start loop over map with buffer length = {}", buffer.length);
        while ((++end) < buffer.length) {
            if (buffer[end] == 0x0D || buffer[end] == 0x0A) {
                // sound end; make string
                String next = new String(buffer, start, end - start);
                // increment pointers
                start = ++end;
                log.debug("new start value is {}", start);
                log.debug("new end value is   {}", end);

                // if another linefeed or newline is present, skip it too
                if ((buffer[end - 1] == 0x0D || ((end < buffer.length) && buffer[end] == 0x0A))
                        || (buffer[end - 1] == 0x0A || ((end < buffer.length) && buffer[end] == 0x0D))) {
                    start++;
                    end++;
                }
                // store entry
                log.debug(" store entry {}", index);
                mapCache[index++] = next;
            }
        }
    }

    /**
     * Save this file. It lays the file out again, changing the record start
     * addresses into a sequential series.
     *
     * @throws java.io.IOException if anything goes wrong
     */
    public void save(String name) throws java.io.IOException {
        if (name == null) {
            throw new java.io.IOException("Null name during write"); // NOI18N
        }
        try (OutputStream s = new java.io.BufferedOutputStream(
                new java.io.FileOutputStream(new java.io.File(name)))) {

            // find size of output file
            int length = Header.HEADERSIZE * h0.numHeaders();  // allow header space at start
            for (int i = 1; i < h0.numHeaders(); i++) {
                length += headers[i].getRecordLength();
            }
            byte[] buffer = new byte[length];
            for (int i = 0; i < length; i++) {
                buffer[i] = 0;
            }

            // start with first header
            int index = 0;
            index = h0.store(buffer, index);

            if (index != Header.HEADERSIZE) {
                log.error("Unexpected 1st header length: {}", index);
            }

            int datastart = index * h0.numHeaders(); //index is the length of the 1st header

            // rest of the headers
            for (int i = 1; i < h0.numHeaders(); i++) {  // header 0 already done
                // Update header pointers.
                headers[i].updateStart(datastart);
                datastart += headers[i].getRecordLength();

                // copy contents into output buffer
                index = headers[i].store(buffer, index);
            }

            // copy the chunks; skip the first header, with no data
            for (int i = 1; i < h0.numHeaders(); i++) {
                int start = headers[i].getRecordStart();
                int count = headers[i].getRecordLength();  // stored one long

                byte[] content = headers[i].getByteArray();
                if (count != content.length) {
                    log.error("header count {} != content length {}", count, content.length);
                }
                for (int j = 0; j < count; j++) {
                    buffer[start + j] = content[j];
                }
            }

            // write out the buffer
            s.write(buffer);

            // purge buffers
            s.close();
        }
    }

    /**
     * Read the file whose name was provided earlier
     */
    public void read() throws java.io.IOException {
        if (file == null) {
            throw new java.io.IOException("Null file during read"); // NOI18N
        }
        int n;
        try (InputStream s = new java.io.BufferedInputStream(new java.io.FileInputStream(file))) {

            // get first header record
            h0 = new FirstHeader();
            h0.load(s);
            if (log.isDebugEnabled()) {
                log.debug(h0.toString());
            }
            n = h0.numHeaders();
            headers = new Header[n];
            headers[0] = h0;

            for (int i = 1; i < n; i++) {  // header 0 already read
                headers[i] = new Header();
                headers[i].load(s);
                log.debug("Header {} {}", i, headers[i].toString());
            }

            // now read the rest of the file, loading bytes
            // first, scan for things we can't handle
            for (int i = 1; i < n; i++) {
                if (log.isDebugEnabled()) {
                    log.debug("Header {}  length {} type {}", i, headers[i].getDataLength(), headers[i].getType()); // NOI18N
                }
                if (headers[i].getDataLength() > headers[i].getRecordLength()) {
                    log.error("header {} has data length {} greater than record length {}",
                            i, headers[i].getDataLength(), headers[i].getRecordLength()); // NOI18N
                }

                for (int j = 1; j < i; j++) {
                    if (headers[i].getHandle() == headers[j].getHandle()
                            && headers[i].getType() == 1
                            && headers[j].getType() == 1) {
                        log.error("Duplicate handle number in records " + i + "(" + headers[i].getHandle() + ") and "
                                + j + "(" + headers[j].getHandle() + ")");
                    }
                }
                if (headers[i].getType() > 6) {
                    log.error("Type field unexpected value: {}", headers[i].getType());
                }
                if (headers[i].getType() == 0) {
                    log.error("Type field unexpected value: {}", headers[i].getType());
                }
                if (headers[i].getType() < -1) {
                    log.error("Type field unexpected value: {}", headers[i].getType());
                }
            }

            // find end of last part
            int length = 0;
            for (int i = 1; i < n; i++) {
                if (length < headers[i].getRecordStart() + headers[i].getRecordLength()) {
                    length = headers[i].getRecordStart() + headers[i].getRecordLength();
                }
            }

            log.debug("Last byte at {}", length);
            s.close();
        }
        
        // inefficient way to read, hecause of all the skips (instead
        // of seeks)  But it handles non-consecutive and overlapping definitions.
        for (int i = 1; i < n; i++) {
            try (InputStream s = new java.io.BufferedInputStream(new java.io.FileInputStream(file))) {
                long count = s.skip(headers[i].getRecordStart());
                if (count != headers[i].getRecordStart()) {
                    log.warn("Only skipped {} characters, should have skipped {}", count, headers[i].getRecordStart());
                }
                byte[] array = new byte[headers[i].getRecordLength()];
                int read = s.read(array);
                if (read != headers[i].getRecordLength()) {
                    log.error("header {} read {}, expected {}", i, read, headers[i].getRecordLength());
                }

                headers[i].setByteArray(array);
                s.close();
            }
        }
    }

    /**
     * Write data from headers into separate files.
     *
     * Normally, we just work with the data within this file. This method allows
     * us to extract the contents of the file for external use.
     */
    public void writeSubFiles() throws IOException {
        // write data from WAV headers into separate files
        int n = numHeaders();
        for (int i = 1; i < n; i++) {
            if (headers[i].isWAV()) {
                writeSubFile(i, "" + i + ".wav"); // NOI18N
            } else if (headers[i].isSDF()) {
                writeSubFile(i, "" + i + ".sdf"); // NOI18N
            } else if (headers[i].getType() == 3) {
                writeSubFile(i, "" + i + ".cv"); // NOI18N
            } else if (headers[i].getType() == 4) {
                writeSubFile(i, "" + i + ".txt"); // NOI18N
            } else if (headers[i].isMap()) {
                writeSubFile(i, "" + i + ".map"); // NOI18N
            } else if (headers[i].getType() == 6) {
                writeSubFile(i, "" + i + ".uwav"); // NOI18N
            }
        }
    }

    /**
     * Write the content from a specific header as a new "subfile".
     *
     * @param i    index of the specific header
     * @param name filename
     */
    void writeSubFile(int i, String name) throws IOException {
        File outfile = new File(name);
        OutputStream ostream = new FileOutputStream(outfile);
        try {
            ostream.write(headers[i].getByteArray());
        } finally {
            ostream.close();
        }
    }

    public void dispose() {
    }

    File file;
    FirstHeader h0;
    Header[] headers;

    /**
     * Class representing a header record.
     */
    public class Header {

        final static int HEADERSIZE = 128; // bytes

        int type;
        int handle;

        // Offset in overall buffer where the complete record
        // associated with this header is found
        int recordStart;

        // Offset in overall buffer where the data part of the
        // record associated with this header is found
        int dataStart;

        // Length of the data in the associated record
        int dataLength;
        // Length of the associated record
        int recordLength;

        int time;

        @SuppressFBWarnings(value = "URF_UNREAD_FIELD") // we maintain this, but don't use it for anything yet
        int spare1;

        @SuppressFBWarnings(value = "URF_UNREAD_FIELD") // we maintain this, but don't use it for anything yet
        int spare2;

        @SuppressFBWarnings(value = "URF_UNREAD_FIELD") // we maintain this, but don't use it for anything yet
        int spare3;

        @SuppressFBWarnings(value = "URF_UNREAD_FIELD") // we maintain this, but don't use it for anything yet
        int spare4;

        @SuppressFBWarnings(value = "URF_UNREAD_FIELD") // we maintain this, but don't use it for anything yet
        int spare5;

        @SuppressFBWarnings(value = "URF_UNREAD_FIELD") // we maintain this, but don't use it for anything yet
        int spare6;

        @SuppressFBWarnings(value = "URF_UNREAD_FIELD") // we maintain this, but don't use it for anything yet
        int spare7;

        String filename;

        public int getType() {
            return type;
        }

        public int getHandle() {
            return handle;
        }

        public int getDataStart() {
            return dataStart;
        }

        public void setDataStart(int i) {
            dataStart = i;
        }

        public int getDataLength() {
            return dataLength;
        }

        private void setDataLength(int i) {
            dataLength = i;
        }

        public int getRecordStart() {
            return recordStart;
        }

        public void setRecordStart(int i) {
            recordStart = i;
        }

        /**
         * This method, in addition to returning the needed record size, will
         * also pull a SdfBuffer back into the record if one exists.
         */
        public int getRecordLength() {
            if (sdfBuffer != null) {
                sdfBuffer.loadByteArray();
                byte[] a = sdfBuffer.getByteArray();
                setByteArray(a);
                dataLength = bytes.length;
                recordLength = bytes.length;
            }
            return recordLength;
        }

        public void setRecordLength(int i) {
            recordLength = i;
        }

        public String getName() {
            return filename;
        }

        public void setName(String name) {
            if (name.length() > 72) {
                log.error("new filename too long: {}", filename.length());
            }
            filename = name;
        }

        byte[] bytes;

        /**
         * Copy new data into the local byte array.
         */
        private void setByteArray(byte[] a) {
            bytes = new byte[a.length];
            for (int i = 0; i < a.length; i++) {
                bytes[i] = a[i];
            }
        }

        public byte[] getByteArray() {
            return Arrays.copyOf(bytes, bytes.length);
        }

        /**
         * Get as a SDF buffer. This buffer then becomes associated, and a later
         * write will use the buffer's contents.
         */
        public SdfBuffer getSdfBuffer() {
            sdfBuffer = new SdfBuffer(getByteArray());
            return sdfBuffer;
        }

        SdfBuffer sdfBuffer = null;

        /**
         * Data record associated with this header is being being repositioned.
         */
        void updateStart(int newRecordStart) {
            //int oldRecordStart = getRecordStart();
            int dataStartOffset = getDataStart() - getRecordStart();
            setRecordStart(newRecordStart);
            setDataStart(newRecordStart + dataStartOffset);
        }

        /**
         * Provide new content. The data start and data length values are
         * computed from the arguments, and stored relative to the length.
         *
         * @param array  New byte array; copied into header
         * @param start  data start location within array
         * @param length data length in bytes (not record length)
         */
        public void setContent(byte[] array, int start, int length) {
            log.debug("setContent length = 0x{}", Integer.toHexString(length));
            setByteArray(array);
            setDataStart(getRecordStart() + start);
            setDataLength(length);
            setRecordLength(array.length);
        }

        int store(byte[] buffer, int index) {
            index = copyInt4(buffer, index, type);
            index = copyInt4(buffer, index, handle);
            index = copyInt4(buffer, index, recordStart);
            index = copyInt4(buffer, index, dataStart);
            index = copyInt4(buffer, index, dataLength);
            index = copyInt4(buffer, index, recordLength);
            index = copyInt4(buffer, index, time);

            index = copyInt4(buffer, index, 0); // spare 1
            index = copyInt4(buffer, index, 0); // spare 2
            index = copyInt4(buffer, index, 0); // spare 3
            index = copyInt4(buffer, index, 0); // spare 4
            index = copyInt4(buffer, index, 0); // spare 5
            index = copyInt4(buffer, index, 0); // spare 6
            index = copyInt4(buffer, index, 0); // spare 7

            // name is written in zero-filled array
            byte[] name = filename.getBytes();
            if (name.length > 72) {
                log.error("Name too long: {}", name.length);
            }
            for (int i = 0; i < name.length; i++) {
                buffer[index + i] = name[i];
            }

            return index + 72;
        }

        void store(OutputStream s) throws java.io.IOException {
            writeInt4(s, type);
            writeInt4(s, handle);
            writeInt4(s, recordStart);
            writeInt4(s, dataStart);
            writeInt4(s, dataLength);
            writeInt4(s, recordLength);
            writeInt4(s, time);

            writeInt4(s, 0);  // spare 1
            writeInt4(s, 0);  // spare 2
            writeInt4(s, 0);  // spare 3
            writeInt4(s, 0);  // spare 4
            writeInt4(s, 0);  // spare 5
            writeInt4(s, 0);  // spare 6
            writeInt4(s, 0);  // spare 7

            // name is written in zero-filled array
            byte[] name = filename.getBytes();
            if (name.length > 72) {
                log.error("Name too long: {}", name.length);
            }
            byte[] buffer = new byte[72];
            for (int i = 0; i < 72; i++) {
                buffer[i] = 0;
            }
            for (int i = 0; i < name.length; i++) {
                buffer[i] = name[i];
            }
            s.write(buffer);
        }

        void load(InputStream s) throws java.io.IOException {
            type = readInt4(s);
            handle = readInt4(s);
            recordStart = readInt4(s);
            dataStart = readInt4(s);
            dataLength = readInt4(s);
            recordLength = readInt4(s);
            time = readInt4(s);

            spare1 = readInt4(s);
            spare2 = readInt4(s);
            spare3 = readInt4(s);
            spare4 = readInt4(s);
            spare5 = readInt4(s);
            spare6 = readInt4(s);
            spare7 = readInt4(s);

            byte[] name = new byte[72];
            int readLength = s.read(name);
            // name is zero-terminated, so we have to truncate that array
            int len = 0;
            for (len = 0; len < readLength; len++) {
                if (name[len] == 0) {
                    break;
                }
            }
            byte[] shortname = new byte[len];
            for (int i = 0; i < len; i++) {
                shortname[i] = name[i];
            }
            filename = new String(shortname);
        }

        @Override
        public String toString() {
            return "type= " + typeAsString() + ", handle= " + handle + ", rs= " + recordStart + ", ds= " + dataStart // NOI18N
                    + ", ds-rs = " + (dataStart - recordStart) // NOI18N
                    + ", dl = " + dataLength + ", rl= " + recordLength // NOI18N
                    + ", rl-dl = " + (recordLength - dataLength) // NOI18N
                    + ", filename= " + filename; // NOI18N
        }

        public boolean isWAV() {
            return (getType() == 1);
        }

        public boolean isSDF() {
            return (getType() == 2);
        }

        public boolean isMap() {
            return (getType() == 5);
        }

        public boolean isTxt() {
            return (getType() == 4);
        }

        /**
         * Read a 4-byte integer, handling endian-ness of SPJ files.
         */
        private int readInt4(InputStream s) throws java.io.IOException {
            int i1 = s.read() & 0xFF;
            int i2 = s.read() & 0xFF;
            int i3 = s.read() & 0xFF;
            int i4 = s.read() & 0xFF;
            return i1 + (i2 << 8) + (i3 << 16) + (i4 << 24);
        }

        /**
         * Write a 4-byte integer, handling endian-ness of SPJ files.
         */
        private void writeInt4(OutputStream s, int i) throws java.io.IOException {
            byte i1 = (byte) (i & 0xFF);
            byte i2 = (byte) ((i >> 8) & 0xFF);
            byte i3 = (byte) ((i >> 16) & 0xFF);
            byte i4 = (byte) ((i >> 24) & 0xFF);

            s.write(i1);
            s.write(i2);
            s.write(i3);
            s.write(i4);
        }

        /**
         * Copy a 4-byte integer to byte buffer, handling little-endian-ness of
         * SPJ files.
         */
        private int copyInt4(byte[] buffer, int index, int i) {
            buffer[index++] = (byte) (i & 0xFF);
            buffer[index++] = (byte) ((i >> 8) & 0xFF);
            buffer[index++] = (byte) ((i >> 16) & 0xFF);
            buffer[index++] = (byte) ((i >> 24) & 0xFF);
            return index;
        }

        public String typeAsString() {
            if (type == -1) {
                return " initial "; // NOI18N
            }
            if ((type >= 0) && (type < 7)) {
                String[] names = {"(unused) ", // 0 // NOI18N
                    "WAV      ", // 1 // NOI18N
                    "SDF      ", // 2 // NOI18N
                    " CV data ", // 3 // NOI18N
                    " comment ", // 4 // NOI18N
                    ".map file", // 5 // NOI18N
                    "WAV (mty)"}; // 6 // NOI18N
                return names[type];
            }
            // unexpected answer
            log.warn("Unexpected type = {}", type); // NOI18N
            return "Unknown " + type; // NOI18N
        }
    }

    /**
     * Class representing first header
     */
    class FirstHeader extends Header {

        /**
         * Number of headers, including the initial system header.
         */
        int numHeaders() {
            return (dataStart / 128);
        }

        float version() {
            return recordStart / 100.f;
        }

        String getComment() {
            return filename;
        }

        @Override
        public String toString() {
            return "initial record, version=" + version() + " num headers = " + numHeaders() // NOI18N
                    + ", comment= " + filename; // NOI18N
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SpjFile.class);

}
