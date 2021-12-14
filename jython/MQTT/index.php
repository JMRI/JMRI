<!DOCTYPE html>
<html>
<head>
	<title>JMRI Jython Example Files (Topic Subdirectory)</title>
</head>
<body>
<h2>JMRI Jython Example Files (Topic Subdirectory)</h2>


<?php

//Based on two php files in the resources directory: index, showIcons

function startswith($string, $test) {
    $strlen = strlen($string);
    $testlen = strlen($test);
    if ($testlen > $strlen) return false;
    return substr($string, 0, strlen($test)) == $test;
}

function endswith($string, $test) {
    $strlen = strlen($string);
    $testlen = strlen($test);
    if ($testlen > $strlen) return false;
    return substr_compare(strtolower($string), strtolower($test), -$testlen) === 0;
}

function showSubdirs() {

    // show subdirectories
    $list = array();

    $d = dir(".");
    while (false !== ($entry = $d->read())) {
       if (is_dir($entry) && substr($entry,0,1) != '.') {
         $list[] = $entry;
       }
    }
    $d->close();

    if (sizeof($list)> 0) {
        echo "<h3>Subdirectories</h3>\n";

        sort($list);
        foreach ($list as $entry) {
            echo '<img src="https://www.jmri.org/icons/folder.gif"> <a href="'.$entry.'">'.$entry.'</a><p>'."\n";
        }
    }
}

function showFilesAndIcons() {

    $listIcon = array();
    $listOther = array();
    $listPy = array();

    $d = dir(".");
    while (false !== ($entry = $d->read())) {
       if (endswith($entry, ".py")) {
         $listPy[] = $entry;
       }
       elseif (endswith($entry, ".gif")
            || endswith($entry, ".jpg")
            || endswith($entry, ".png")
            || endswith($entry, ".EPS")
            || endswith($entry, ".PSD")
            )   {
         $listIcon[] = $entry;
       }
       elseif (! (startswith($entry, ".") || is_dir($entry) || endswith($entry, ".php")) ) {
         $listOther[] = $entry;
       }
    }
    $d->close();

    // now show the files
    if (sizeof($listPy)> 0) {

        echo "<h3>Sample Scripts</h3>\n";
        echo '<table border="1">';

        sort($listPy);

        foreach ($listPy as $entry) {
            echo '<tr>'."\n";
            echo     '<td><a href="'.$entry.'">'.$entry.' </a></td>'."\n";
            echo     '<td bgcolor="#C0C0C0"><a href="'.$entry.'" download="'.$entry.'">(download)</a></tr>'."\n";
            echo '</tr>'."\n";
        }

        echo "</table>\n";
    }

    // Show any icons
    if (sizeof($listIcon)> 0) {

        echo "<h3>Icons</h3>\n";
        echo '<table border="1">';

        sort($listIcon);

        foreach ($listIcon as $entry) {
            echo '<tr>'."\n";
            echo     '<td><a href="'.$entry.'">'.$entry.' </a></td>'."\n";
            if (endswith($entry, ".gif") || endswith($entry, ".jpg") || endswith($entry, ".png")) {
                // display as image
                //   would be good to add a limiting size here
                echo     '<td bgcolor="#C0C0C0"><a href="'.$entry.'"><img src="'.$entry.'" style="max-width:500px;max-height:500px;"></a></tr>'."\n";
            }
            else {
                // link without display
                echo     '<td bgcolor="#C0C0C0"><a href="'.$entry.'" download="'.$entry.'">(download)</a></tr>'."\n";
            }
            echo '</tr>'."\n";
        }

        echo "</table>\n";
    }

    // now show the rest of the files
    if (sizeof($listOther)> 0) {

        echo "<h3>Other Files</h3>\n";
        echo '<table border="1">';

        sort($listOther);

        foreach ($listOther as $entry) {
            echo '<tr>'."\n";
            echo     '<td><a href="'.$entry.'">'.$entry.' </a></td>'."\n";
            echo     '<td bgcolor="#C0C0C0"><a href="'.$entry.'" download="'.$entry.'">(download)</a></tr>'."\n";
            echo '</tr>'."\n";
        }

        echo "</table>\n";
    }

}

//MAINLINE

// show README if present
if (file_exists("README.md")) {
    echo "<pre>\n";
    echo file_get_contents( "README.md" );
    echo "</pre>\n";
}
elseif (file_exists("README")) {
    echo "<pre>\n";
    echo file_get_contents( "README" );
    echo "</pre>\n";
}
elseif (file_exists("README.txt")) {
    echo "<pre>\n";
    echo file_get_contents( "README.txt" );
    echo "</pre>\n";
}


showFilesAndIcons();
showSubdirs();

?>


</body>
</html>
