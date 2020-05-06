<?php

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

    $d = dir(".");

    $name = getcwd();
    $n = strrpos($name, "/resources");
    
    echo "This is the ".substr($name, $n+1)." directory<p>";
    
    echo '<a href="..">Up one level</a><p>';
    
    // show README if present
    if (file_exists("README")) {
        echo "<pre>\n";
        echo file_get_contents( "README" );
        echo "</pre>\n";
    }
    
    // show subdirectories
    $list = array();
    
    while (false !== ($entry = $d->read())) {
       if (is_dir($entry) && substr($entry,0,1) != '.') {
         $list[] = $entry;
       }
    }
    $d->close();

    if (sizeof($list)> 0) {
        echo "<h2>Subdirectories</h2>\n";
    
        sort($list);
        foreach ($list as $entry) {
            echo '<img src="https://www.jmri.org/icons/folder.gif"> <a href="'.$entry.'">'.$entry.'</a></td><p>'."\n";
        }
    }
}

function showFiles() {
        
    $listIcon = array();
    $listOther = array();

    $d = dir(".");
    while (false !== ($entry = $d->read())) {
       if (endswith($entry, ".gif") 
            || endswith($entry, ".jpg") 
            || endswith($entry, ".png")
            || endswith($entry, ".EPS")
            || endswith($entry, ".PSD")
            || endswith($entry, ".md")
            ) {
         $listIcon[] = $entry;
       } else if (! (startswith($entry, ".") || is_dir($entry) || endswith($entry, ".php") ) ) {
         $listOther[] = $entry;
       }
    }
    $d->close();
    
    if (sizeof($listIcon)> 0) {
    
        echo "<h2>Icons</h2>\n";
        echo '<table border="1">';

        sort($listIcon);

        foreach ($listIcon as $entry) {
            echo '<tr>'."\n";
            echo     '<td><a href="'.$entry.'">'.$entry.' </a></td>'."\n";
           if (endswith($entry, ".gif") 
                || endswith($entry, ".jpg") 
                || endswith($entry, ".png")
                ) {
                    // display as image
                    //   would be good to add a limiting size here
                    echo     '<td bgcolor="#C0C0C0"><a href="'.$entry.'"><img src="'.$entry.'" style="max-width:500px;max-height:500px;"></a></tr>'."\n";
            } else {
                    // link without display
                    echo     '<td bgcolor="#C0C0C0"><a href="'.$entry.'" download="'.$entry.'">(download)</a></tr>'."\n";
            }
            echo '</tr>'."\n";
        }

        echo "</table>\n";
    }
    
    // now show the rest of the files

    if (sizeof($listOther)> 0) {
    
        echo "<h2>Other Files</h2>\n";
        echo '<table border="1">';

        sort($listOther);

        foreach ($listOther as $entry) {
            echo '<tr>'."\n";
            echo     '<td><a href="'.$entry.'">'.$entry.' </a></td>'."\n";
           if (endswith($entry, ".gif") 
                || endswith($entry, ".jpg") 
                || endswith($entry, ".png")
                ) {
                    // display as name
                    echo     '<td bgcolor="#C0C0C0"><a href="'.$entry.'"><img src="'.$entry.'" style="max-width:500px;max-height:500px;"></a></tr>'."\n";
            } else {
                    // link without display
                    echo     '<td bgcolor="#C0C0C0"><a href="'.$entry.'" download="'.$entry.'">(download)</a></tr>'."\n";
            }
            echo '</tr>'."\n";
        }

        echo "</table>\n";
    }
    
}

?>
