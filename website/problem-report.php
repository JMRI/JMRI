<?php

/**
 * Used by jmri.jmrit.mailreporter to file 
 * problem information onto a mailing list.
 */


/* Redirect access if not running on defined host 
 * This is used because jmri.org can't send email
 * any more.
 */
$intended_host = "signer.jmri.org:50080";

if ($_SERVER['HTTP_HOST'] != $intended_host) {
    header("Location: http://".$intended_host."/problem-report.php", true, 307);
    exit;
}

/**
 * Process an HTTP POST request based on the following fields:
 * reporter: email address of reporter
 * sendcopy: send a copy of message to the reporter (yes|no)
 * summary: short summary of problem (used as title)
 * problem: detailed error report (used as body)
 * logfileupload: log files to attach
 */

// First check that we've been accessed via an HTTP POST request
if((strtoupper($_SERVER['REQUEST_METHOD']) == 'POST') 
        && (!empty($_POST['reporter']))) {

    $destination = "jmri-reports@lists.sourceforge.net";
    
    // Set-up basic headers
    $headers = "";
    $headers = 'From: '.$_POST['reporter']."\r\n";

    // If requested, add sender to the recipents
    if (!empty($_POST['sendcopy'])) {
        if (test_input($_POST['sendcopy']) == 'yes') {
            $headers = $headers.'Cc: '.test_input($_POST['reporter'])."\r\n";
        }
    }

    // Get detailed problem
    $body = htmlspecialchars($_POST['problem']);

    // If we've got attachments, need to create a multipart message
    if (!empty($_FILES)) {
        // Define multipart boundary string
        $randomVal = md5(time());
        $mimeBoundary = "==Multipart_Boundary_x{$randomVal}x";

        // Define MIME header
        $headers = $headers.'MIME-Version: 1.0'."\r\n";
        $headers = $headers.'Content-Type: '."multipart/mixed;\n boundary=\"{$mimeBoundary}\""."\r\n";

        // Start Multipart Boundary above message
        $body = "This is a multi-part message in MIME format.\n\n" .
                "--{$mimeBoundary}\n" .
                "Content-Type: text/plain; charset=\"ISO-8859-1\"\n" .
                "Content-Transfer-Encoding: 7bit\n\n" .
                $body . "\n\n";

        // Loop through attachments
        foreach( $_FILES['logfileupload']['tmp_name'] as $index => $tmpName) {
            if(!empty($_FILES['logfileupload']['error'][$index])) {
                // Oh dear, there's a problem with this file.
                return false;
            }

            // Now check that the file isn't empty and that it is an uploaded file
            if(!empty($tmpName) && is_uploaded_file($tmpName)) {
                // We're good. Now get some info
                $fileType = $_FILES['logfileupload']['type'][$index];
                $fileName = $_FILES['logfileupload']['name'][$index];

                // 'Orrible hack to mangle .zip file extension as these
                // are no longer allowed by SourceForge...
                if(substr($fileName, -4) == ".zip") {
                    // Trim off the last character
                    $fileName = substr($fileName, 0, -1);
                }

                // Read file in binary mode (rb)
                $file = fopen($tmpName,'rb');
                $data = fread($file,filesize($tmpName));
                fclose($file);

                // Encode file data
                $data = chunk_split(base64_encode($data));

                // Add attachment to message
                $body .= "--{$mimeBoundary}\n" .
                        "Content-Type: {$fileType}; name=\"{$fileName}\"\n" .
                        "Content-Transfer-Encoding: base64\n" .
                        "Content-Disposition: attachment; filename=\"{$fileName}\"\n\n" .
                        $data . "\n\n";
            }
        }
        // Add final MIME boundary
        $body .= "--{$mimeBoundary}\n";
    }

    // Now send it
    $status = mail($destination, test_input($_POST['summary']), $body, $headers);

    print "status ".$status;
    
    // Check for errors
    if (! $status) {
        echo '<p>Message NOT sent</p>\n';
    } else {
        echo '<p>Message successfully sent!</p>';
    }
} else {
    // We've been accessed directly - report appropriately
    echo '<p>No form data received.</p>';
}

/**
 * Function to validate form input.
 * Strips unnecessary characters, removes backslashes and escapes any embedded HTML.
 * @param binary $data form input data
 * @return binary cleansed input data
 */
function test_input($data) {
    $data = trim($data);
    $data = stripslashes($data);
    $data = htmlspecialchars($data);
    return $data;
}
