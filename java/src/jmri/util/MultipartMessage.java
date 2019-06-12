package jmri.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends multi-part HTTP POST requests to a web server
 * <p>
 * Based on
 * http://www.codejava.net/java-se/networking/upload-files-by-sending-multipart-request-programmatically
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Matthew Harris Copyright (C) 2014
 */
public class MultipartMessage {

    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private final HttpURLConnection httpConn;
    private final String charSet;
    private final OutputStream outStream;
    private final PrintWriter writer;

    /**
     * Constructor initialises a new HTTP POST request with content type set to
     * 'multipart/form-data'.
     * <p>
     * This allows for additional binary data to be uploaded.
     *
     * @param requestURL URL to which this request should be sent
     * @param charSet    character set encoding of this message
     * @throws IOException if {@link OutputStream} cannot be created
     */
    public MultipartMessage(String requestURL, String charSet) throws IOException {
        this.charSet = charSet;

        // create unique multi-part message boundary
        boundary = "===" + System.currentTimeMillis() + "===";
        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        httpConn.setRequestProperty("User-Agent", "JMRI " + jmri.Version.getCanonicalVersion());
        outStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outStream, this.charSet), true);
    }

    /**
     * Adds form field data to the request
     *
     * @param name  field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        log.debug("add form field: {}; value: {}", name, value);
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + name
                + "\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charSet)
                .append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds an upload file section to the request. MIME type of the file is
     * determined based on the file extension.
     *
     * @param fieldName  name attribute in form &lt;input name="{fieldName}"
     *                   type="file" /&gt;
     * @param uploadFile file to be uploaded
     * @throws IOException if problem adding file to request
     */
    public void addFilePart(String fieldName, File uploadFile) throws IOException {
        addFilePart(fieldName, uploadFile, URLConnection.guessContentTypeFromName(uploadFile.getName()));
    }

    /**
     * Adds an upload file section to the request. MIME type of the file is
     * explicitly set.
     *
     * @param fieldName  name attribute in form &lt;input name="{fieldName}"
     *                   type="file" /&gt;
     * @param uploadFile file to be uploaded
     * @param fileType   MIME type of file
     * @throws IOException if problem adding file to request
     */
    public void addFilePart(String fieldName, File uploadFile, String fileType) throws IOException {
        log.debug("add file field: {}; file: {}; type: {}", fieldName, uploadFile, fileType);
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: " + fileType).append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        try (FileInputStream inStream = new FileInputStream(uploadFile)) {
            byte[] buffer = new byte[4096];
            @SuppressWarnings("UnusedAssignment")
            int bytesRead = -1;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            outStream.flush();
        }

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a header field to the request
     *
     * @param name  name of header field
     * @param value value of header field
     */
    public void addHeaderField(String name, String value) {
        log.debug("add header field: {}; value: {}", name, value);
        writer.append(name + ": " + value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Finalise and send MultipartMessage to end-point.
     *
     * @return Responses from end-point as a List of Strings
     * @throws IOException if problem sending MultipartMessage to end-point
     */
    public List<String> finish() throws IOException {
        List<String> response = new ArrayList<>();

        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        // check server status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.add(line);
                }
            }
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }

        return response;
    }

    private static final Logger log = LoggerFactory.getLogger(MultipartMessage.class);

}
