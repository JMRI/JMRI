$(document).ready(
        function() {

            //attach code to Send button
            $('button#btnSend').click(
                    function() {
                        var $commandstr = '<XMLIO>' + $('textarea#txtData').val() + '</XMLIO>';
                        $('textarea#result').empty();
                        $('div#formatted').text($commandstr);
                        $('div#formattedLabel').text("Waiting....  Sent:");

                        $.post(
                                '/xmlio/', //<--url
                                $commandstr, //<--data
                                function($r, $s, $x) { //<--success
                                    $processResponse($r, $s, $x)
                                },
                                'xml' //<--dataType
                        );
                    }
            );

            //register a generic handler to show ajax errors
            $(document).ajaxError(function(e, xhr, settings, exception) {
                alert('error in: ' + settings.url + ' \\n'+'error:\\n' + exception);
            });

            //copy clicked value to Send box for editing
            $('table#samples td').click(
                    function() {
                        $('textarea#txtData').val($(this).text());  
                    }
            );

            //copy double-clicked value from list and go ahead and send it
            $('table#samples td').dblclick(
                    function() {
                        $('textarea#txtData').val($(this).text());  //set value of send box to sample clicked
                        $('button#btnSend').click(); //process the button press
                    }
            );

            //copy response value to send box and go ahead and send it
            $('button#btnReSend').click(
                    function() {
                        $('textarea#txtData').val($('textarea#result').text().replace('<?xml version="1.0" encoding="UTF-8"?>',"").replace("</XMLIO>","").replace("<XMLIO>",""));  //set value of send box to returned value
                    }
            );

            //copy response value to send box and go ahead and send it
            $('button#btnReSend').dblclick(
                    function() {
                        $('textarea#txtData').val($('textarea#result').text().replace('<?xml version="1.0" encoding="UTF-8"?>',"").replace("</XMLIO>","").replace("<XMLIO>",""));  //set value of send box to returned value
                        $('button#btnSend').click(); //process the button press
                    }
            );
            //process the response returned for the "list" command
            var $processResponse = function($returnedData, $success, $xhr) {
                var $outputstr = "";
                var $headers = [];
                $('textarea#result').text(xml2Str($returnedData));  //output raw result                  
                var $xml = $($returnedData);  //jQuery-ize returned data for easier access
                $xml.find('item').each( //find and process all "item" entries (list)
                		function() {
        					$col = 0;
                			$outputstr += "<tr>";  //start a new row
                			$($(this)[0].childNodes).each(
                					function() {
                						if (this.nodeName != "#text") { //skip empty elements (whitespace, etc.)
                							$outputstr += "<td>" + $(this).text() + "</td>";
            								$col++;
                							$headers[$col] = this.nodeName;  //save node name for header row
                						}
                					}
                			);
                			$outputstr += "</tr>";
                		}
                );
                if ($outputstr == "") { //no "items" found, try for attribute-based xml
                	$($xml.find('XMLIO')[0].childNodes).each(
                			function() {
                				if (this.nodeName != "#text") { //skip empty elements (whitespace, etc.)
                					$outputstr += "<tr>"; //start a new row,
                					$outputstr += "<td>" + this.nodeName + "</td>";  //set name as first col
                					$col = 0;
                					$headers[$col] = "type";  //type is inferred
                					$(this.attributes).each(
                							function() {
                								$outputstr += "<td>" + this.value + "</td>";
                								$col++;
                								$headers[$col] = this.name;  //save attribute name for header row
                							}
                					);
                					$outputstr += "</tr>";
                				}
                			}
                	);
                }
                if ($outputstr == "") { //if still no results found, leave empty
                	$outputstr = "&nbsp;";
                } else {  //generate a table for results
                    var $headerstr = "";  //populate table header
                    for (var $i=0; $i < $headers.length; $i++) {
                        if ($headers[$i] != null) {  //skip the undefined ones
                            $headerstr += "<th>" + $headers[$i] + "</th>";
                        }
                    }
                    $outputstr = "<table>" +$headerstr + $outputstr + '</table>'; //put the parts together
                }
                $('div#formatted').html($outputstr);  //put results table into proper div
                $('div#formattedLabel').text("Formatted results:");
            };
        }

);

//workaround for IE (from http://www.webdeveloper.com/forum/showthread.php?t=187378)
function xml2Str(xmlNode) {
	try {  // Gecko-based browsers, Safari, Opera.
		return (new XMLSerializer()).serializeToString(xmlNode);
	}
	catch (e) {
		try {	// Internet Explorer.
			return xmlNode.xml;
		}
		catch (e)	{  //Strange Browser ??
			alert('Xmlserializer not supported');
		}
	}
	return false;
}
