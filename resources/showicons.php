<?php

function endswith($string, $test) {
    $strlen = strlen($string);
    $testlen = strlen($test);
    if ($testlen > $strlen) return false;
    return substr_compare(strtolower($string), strtolower($test), -$testlen) === 0;
}

function showSubdirs() {

    $d = dir(".");

    $name = getcwd();
    $n = strrpos($name, "/resources");
    
    echo "This is the ".substr($name, $n+1)." directory<p>";
    
    echo "<h2>Subdirectories</h2>\n";
    
    echo '<a href="..">Up one level</a><p>';
    
    while (false !== ($entry = $d->read())) {
       if (is_dir($entry) && substr($entry,0,1) != '.') {
         echo '<img src="http://jmri.org/icons/folder.gif"> <a href="'.$entry.'">'.$entry.'</a></td><p>'."\n";
       }
    }
    $d->close();
}

function showFiles() {
    
    echo "<h2>Icons</h2>\n";
    echo '<table border="1">';
    
    $d = dir(".");
    while (false !== ($entry = $d->read())) {
       if (endswith($entry, ".gif") || endswith($entry, ".jpg") || endswith($entry, ".png")) {
        echo '<tr><td>'.$entry.' </td><td bgcolor="#C0C0C0"><a href="'.$entry.'"><img src="'.$entry.'"></a></td>'."\n";
       }
    }
    $d->close();
    
    echo "</table>\n";
}

?>
