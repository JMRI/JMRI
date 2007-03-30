// SpjFile.java

package jmri.jmrix.loconet.spjfile;

import java.io.*;

/**
 * Provide tools for reading, writing and accessing
 * Digitrax SPJ files
 *
 * @author		Bob Jacobsen  Copyright (C) 2006
 * @version             $Revision: 1.6 $
 */

public class SpjFile {

    public SpjFile(String name) {
        file = new File(name);
    }
    
    /**
     * Number of headers present in the file. 
     *
     * @return -1 if error
     */
    public int numHeaders() {
        if (headers != null && h0 != null) return h0.numHeaders();
        else return -1;
    }
    
    public String getComment() {
        return h0.getComment();
    }
    
    public Header getHeader(int index) {
        return headers[index];
    }
    
    public Header findSdfHeader() {
        int n = numHeaders();
        for (int i = 1; i< n; i++) 
            if (headers[i].isSDF()) return headers[i];
        return null;
    }
    
    /**
     * Find the map entry (character string) that 
     * corresponds to a particular handle number
     */
    public String getMapEntry(int i) {
        if (log.isDebugEnabled()) log.debug("getMapEntry("+i+")");
        loadMapCache();
        String wanted = ""+i+" ";
        for (int j = 0; j<mapCache.length; j++) {
            if (mapCache[j].startsWith(wanted)) {
                return mapCache[j].substring(wanted.length());
            }
        }
        return null;
    }
    
    String[] mapCache = null;
    
    void loadMapCache() {
        if (mapCache != null) return;

        // find the map entries
        log.debug("loading map cache");
        int map;
        for (map = 1; map< numHeaders(); map++) 
            if (headers[map].isMap()) break;
        // map holds the map index, hopefully
        if (map > numHeaders()) {
            log.error("Did not find map data");
            return;
        }
        
        // here found it, count lines
        byte[] buffer = headers[map].getByteArray();
        log.debug("map buffer length "+buffer.length);
        int count = 0;
        for (int i=0; i<buffer.length;i++) if (buffer[i]==0x0D) count++;
        
        mapCache = new String[count];
        
        log.debug("found "+count+" map entries");
        
        int start = 0;
        int end = 0;
        int index = 0;
        
        // loop through the string, look for each line
        if (log.isDebugEnabled()) log.debug("start loop over map with buffer length = "+buffer.length);
        while ( (++end) < buffer.length) {
            if (buffer[end] == 0x0D || buffer[end] == 0x0A) {
                // sound end; make string
                String next = new String(buffer, start, end-start);
                // increment pointers
                start = ++end;
                if (log.isDebugEnabled()) log.debug("new start value is "+start);
                if (log.isDebugEnabled()) log.debug("new end value is   "+end);
               
                // if another linefeed or newline is present, skip it too
                if ( (buffer[end-1] == 0x0D || ((end < buffer.length) && buffer[end] == 0x0A)) ||
                     (buffer[end-1] == 0x0A || ((end < buffer.length) && buffer[end] == 0x0D)) ) {
                        start++;
                        end++;
                }
                // store entry
                if (log.isDebugEnabled()) log.debug(" store entry "+index);
                mapCache[index++] = next;
            }
        }
    }
    
    /**
     * Save this file. This is not protected against 
     * overwrite, etc.
     * @throws java.io.IOException if anything goes wrong
     */
    public void save(String name) throws java.io.IOException {
    }
    
    /**
     * Read the file whose name was provided earlier
     */
    public void read() throws java.io.IOException {
        if (file == null) {
            throw new java.io.IOException("Null file during read");
        }
        InputStream s = new java.io.BufferedInputStream(new java.io.FileInputStream(file));
    
        // get first header record
        h0 = new FirstHeader();
        h0.load(s);
        if (log.isDebugEnabled()) log.debug(h0.toString());
        int n = h0.numHeaders();
        headers = new Header[n];
        headers[0] = h0;
        
        for (int i = 1; i< n; i++) {  // header 0 already read
            headers[i] = new Header();
            headers[i].load(s);
            if (log.isDebugEnabled()) log.debug("Header "+i+" "+headers[i].toString());
        }
   
        // now read the rest of the file, loading bytes
        
        // first, scan for things we can't handle
        for (int i = 1; i< n; i++) {
            if (headers[i].getDataLength() > headers[i].getRecordLength())
                log.error("header "+i+" has data length "+headers[i].getDataLength()
                        +" greater than record length "+headers[i].getRecordLength());

            for (int j = 1; j<i; j++) {
                if (headers[i].getHandle() == headers[j].getHandle()
                    && headers[i].getType() == 1
                    && headers[j].getType() == 1)
                    log.error("Duplicate handle number in records "+i+"("+headers[i].getHandle()+") and "
                            +j+"("+headers[j].getHandle()+")");
            }
            if (headers[i].getType()  > 6 ) log.error("Type field unexpected value: "+headers[i].getType());
            if (headers[i].getType() == 0 ) log.error("Type field unexpected value: "+headers[i].getType());
            if (headers[i].getType() < -1 ) log.error("Type field unexpected value: "+headers[i].getType());
        }
        
        // find end of last part
        int length = 0;
        for (int i = 1; i< n; i++) {
            if (length < headers[i].getRecordStart()+headers[i].getRecordLength())
                length = headers[i].getRecordStart()+headers[i].getRecordLength();
        }
        
        if (log.isDebugEnabled()) log.debug("Last byte at "+length);
        
        // inefficient way to read, hecause of all the skips (instead
        // of seeks)  But it handles non-consecutive and overlapping definitions.
        for (int i = 1; i< n; i++) {
            s.close();
            s = new java.io.BufferedInputStream(new java.io.FileInputStream(file));
            s.skip(headers[i].getRecordStart());
            
            byte [] array = new byte[headers[i].getRecordLength()];
            int read = s.read(array);
            if (read != headers[i].getRecordLength())
                log.error("header "+i+" read "+read+", expected "+headers[i].getRecordLength());
                
            headers[i].setByteArray(array);
        }
        
        s.close();
        
    }
    
   /**
    * Write data from headers into separate files.
    *
    * Normally, we just work with the data within this file.
    * This method allows us to extract the contents of the file
    * for external use.
    */
   public void writeSubFiles() throws IOException {  
        // write data from WAV headers into separate files
        int n = numHeaders();
        for (int i = 1; i< n; i++) {
            if (headers[i].isWAV()) {
                writeSubFile(i, ""+i+".wav");
            } else if (headers[i].isSDF()) {
                writeSubFile(i, ""+i+".sdf");
            } else if (headers[i].getType() == 3) {
                writeSubFile(i, ""+i+".cv");
            } else if (headers[i].getType() == 4) {
                writeSubFile(i, ""+i+".txt");
            } else if (headers[i].isMap()) {
                writeSubFile(i, ""+i+".map");
            } else if (headers[i].getType() == 6) {
                writeSubFile(i, ""+i+".uwav");
            }
        }
    }
    
   
    /**
     * Write the content from a specific header as a 
     * new "subfile"
     * @param i index of the specific header
     * @param name filename
     */
    void writeSubFile(int i, String name) throws IOException {
        File outfile = new File(name);
        OutputStream ostream = new FileOutputStream(outfile);
        ostream.write(headers[i].getByteArray());
        ostream.close();
    }
       
    public void dispose() {
    }
    
    File file;
    FirstHeader h0;
    Header[] headers;

    /**
     * Class representing a header record
     */
    public class Header {
        int type;
        int handle;
        int recordStart;
        int dataStart;
        int dataLength;
        int recordLength;
        int time;
        
        int spare1;
        int spare2;
        int spare3;
        int spare4;
        int spare5;
        int spare6;
        int spare7;
        
        String filename;
        
        public int getType() { return type; }
        public int getHandle() {return handle; }
        
        public int getDataStart() { return dataStart; }
        public int getDataLength() { return dataLength; }
        
        public int getRecordStart() { return recordStart; }
        public int getRecordLength() { return recordLength; }

        public String getName() {return filename;}
        
        byte[] bytes;
        void setByteArray(byte[] a) {
            bytes = a;
        }
        public byte[] getByteArray() { return bytes; }
        
        void load(InputStream s) throws java.io.IOException {
            type = readInt4(s);
            handle =  readInt4(s);
            recordStart =  readInt4(s);
            dataStart =  readInt4(s);
            dataLength =  readInt4(s);
            recordLength =  readInt4(s);
            time = readInt4(s);
            
            spare1 = readInt4(s);
            spare2 = readInt4(s);
            spare3 = readInt4(s);
            spare4 = readInt4(s);
            spare5 = readInt4(s);
            spare6 = readInt4(s);
            spare7 = readInt4(s);
                        
            byte[] name = new byte[72];
            s.read(name);
            // name is zero-terminated, so we have to truncate that array
            int len = 0;
            for (len=0; len<72; len++) if (name[len]==0) break;
            byte[] shortname = new byte[len];
            for (int i=0; i<len; i++) shortname[i] = name[i];
            filename = new String(shortname);
        }
        
        public String toString() {
            return "type= "+typeAsString()+", handle= "+handle+", rs= "+recordStart+", ds= "
                    +dataStart+", dl = "+dataLength+", rl= "+recordLength
                    +", filename= "+filename;
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
         * Read a 4-byte integer, handling endian-ness of SPJ files
         */
        private int readInt4(InputStream s) throws java.io.IOException {
            int i1 = s.read()&0xFF;
            int i2 = s.read()&0xFF;
            int i3 = s.read()&0xFF;
            int i4 = s.read()&0xFF;
            return i1+(i2<<8)+(i3<<16)+(i4<<24);
        }

        /**
         * Read a 2-byte integer, handling endian-ness of SPJ files
         */
        private int readInt2(InputStream s) throws java.io.IOException {
            int i1 = s.read()&0xFF;
            int i2 = s.read()&0xFF;
            return i1+(i2<<8);
        }

        public String typeAsString() {
            if (type == -1) return  " initial ";
            if ((type >= 0 ) && (type < 7)) {
                String[] names = {      "(unused) ",  // 0
                                        "WAV      ",  // 1
                                        "SDF      ",  // 2
                                        " CV data ",  // 3
                                        " comment ",  // 4
                                        ".map file",  // 5
                                        "WAV (mty)"}; // 6
                return names[type];
            } 
            // unexpected answer
            log.warn("Unexpected type = "+type);
            return "Uknown "+type;
        }             
    }
    
    /**
     * Class representing first header
     */
     
    class FirstHeader extends Header {
        /**
         * Number of headers, including the initial
         * system header.
         */
        int numHeaders() { return (dataStart/128); }
        float version() { return recordStart/100.f; }
        String getComment() { return filename; }
        
        
        public String toString() {
            return "initial record, version="+version()+" num headers = "+numHeaders()
                    +", comment= "+filename;
        }
    }
        
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SpjFile.class.getName());

}

/* @(#)SpjFile.java */
