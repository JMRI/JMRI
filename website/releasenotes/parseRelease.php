<?php
// routines for parsing n.nn.nn strings to and from numbers

function intToString($val) {
    $first = $val / (100*100) % 100;
    $second = ($val/100) % 100;
    $third =  $val % 100;
    
    return $first.".".$second.".".$third;
}

// if (intToString(12345) != "1.23.45") print "error 1.23.45 doesn't equal ".intToString(12345);
// if (intToString(10203) != "1.2.3") print "error 1.2.3 doesn't equal ".intToString(10203);

function stringToInt($val) {
    $input = $val;
    $retval = 0;
    
    $i = strpos($input, '.');
    if (!$i) return $retval;
    $retval = intval(substr($input, 0, $i))*100*100;
    $input = substr($input, $i+1);
    
    $i = strpos($input, '.');
    if (!$i) return $retval + intval($input)*100;
    $retval = $retval + intval(substr($input, 0, $i))*100;
    $input = substr($input, $i+1);

    $retval = $retval + intval($input);

    return $retval;
}

// if (stringToInt("1.2.3") != 10203) print "error 10203 doesn't equal ".stringToInt("1.2.3");
// if (stringToInt("1.23.45") != 12345) print "error 12345 doesn't equal ".stringToInt("1.23.45");
// if (stringToInt("1.23") != 12300) print "error 12300 doesn't equal ".stringToInt("1.23");

// find and return most recent release file name, e.g. jmri.1.2.3.shtml
function latestFilename() {
    $d = dir(".");
    while (false !== ($entry = $d->read())) {
       if (substr($entry,0,4) == 'jmri' && substr($entry,-6) == '.shtml' ) {
         $list[] = stringToInt(substr($entry,4, strlen($entry)-10));
       }
    }
    $d->close();
    sort($list);
    return "jmri".intToString($list[count($list)-1]).".shtml";
}

?>
