package jmri.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Unzip a local file or URL to into a specified directory
 * 
 * Largely from https://examples.javacodegeeks.com/core-java/util/zip/zipinputstream/java-unzip-file-example/
 */
public class UnzipFileClass {
		
	/**
	 * Unzip contents into a directory
	 * @param destinationFolder Destination for contents, created if need be; relative or absolute, but must be pre-expanded
	 * @param zipFile .zip file name; relative or absolute, but must be pre-expanded
	 */
	public static void unzipFunction(String destinationFolder, String zipFile) throws java.io.FileNotFoundException {
        File directory = new File(destinationFolder);
        FileInputStream fInput = new FileInputStream(zipFile);
        
	    unzipFunction(directory, fInput);
	}
	
	/**
	 * Unzip contents into a directory
	 * @param directory Destination for contents, created if need be
	 * @param input in .zip format
	 */
	public static void unzipFunction(File directory, InputStream input) {	        
		// if the output directory doesn't exist, create it
		if(!directory.exists()) {
			if ( !directory.mkdirs() ) log.error("Unable to create output directory {}", directory);
        }
        String destinationFolder = directory.getPath();
        
		// buffer for read and write data to file
		byte[] buffer = new byte[2048];
        
		try (ZipInputStream zipInput = new ZipInputStream(input)) {
            
			ZipEntry entry = zipInput.getNextEntry();
            
			while(entry != null){
				String entryName = entry.getName();
				File file = new File(destinationFolder + File.separator + entryName);
                
				log.info("Unzip file {} to {}", entryName, file.getAbsolutePath());
                
				// create the directories of the zip directory
				if(entry.isDirectory()) {
					File newDir = new File(file.getAbsolutePath());
					if(!newDir.exists()) {
						boolean success = newDir.mkdirs();
						if(success == false) {
							log.error("Problem creating Folder {}", newDir);
						}
					}
                }
				else {
					try  (FileOutputStream fOutput = new FileOutputStream(file)) {
                        int count = 0;
                        while ((count = zipInput.read(buffer)) > 0) {
                            // write 'count' bytes to the file output stream
                            fOutput.write(buffer, 0, count);
                        }
                    } catch (IOException e) {
                        log.error("Error writing unpacked zip file contents", e);
                        return;
                    }
				}
				// close ZipEntry and take the next one
				zipInput.closeEntry();
				entry = zipInput.getNextEntry();
			}
            
			// close the last ZipEntry
			zipInput.closeEntry();
            
			zipInput.close();
			input.close();
		} catch (IOException e) {
			log.error("Error unpacking zip file", e);
		}
	}

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PhysicalLocation.class);	
}
