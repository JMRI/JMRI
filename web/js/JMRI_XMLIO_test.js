$(document).ready(
        function() {

            //attach code to Send button
            $('button#btnSend').click(
                    function() {
                        var $commandstr = '<XMLIO>' + $('textarea#txtData').val() + '</XMLIO>';
                        $('textarea#result').empty();
                        $('div#formatted').text($commandstr);
                        $('div#formattedLabel').text("Waiting....  Sent:");

                        var $outputstr = "";
                        var $headers = [];


                        $.post(
                                '/xmlio', //<--url
                                $commandstr, //<--data
                                function($r, $s, $x) { //<--success
                                    $processResponse($r, $s, $x)
                                },
                                'xml' //<--dataType
                        );
//                      }
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
                $('textarea#result').text((new XMLSerializer()).serializeToString($returnedData));  //output raw result                  
                var $xml = $($returnedData);  //jQuery-ize returned data for easier access
                $xml.find('item').each( //find and process all "item" entries (list)
                        function() {
                            $outputstr += "<tr>";  //start a new row
                            for (var $i=0; $i < $(this)[0].childNodes.length; $i++) {
                                if ($(this)[0].childNodes[$i].nodeName != "#text") { //skip empty elements (whitespace, etc.)
                                    $outputstr += "<td>" + $(this)[0].childNodes[$i].textContent + "</td>";
                                    $headers[$i] = $(this)[0].childNodes[$i].nodeName;  //save node name for header row
                                }
                            }
                            $outputstr += "</tr>";
                        }
                );
                if ($outputstr == "") { //no "items" found
                    $outputstr = "&nbsp;";

                } else {  //generate a table for "items" results
                    var $headerstr = "";  //populate table header
                    for (var $i=0; $i < $headers.length; $i++) {
                        if ($headers[$i] != null) {  //skip the undefined ones
                            $headerstr += "<th>" + $headers[$i] + "</th>";
                        }
                    }
                    $outputstr = "<table>" +$headerstr + $outputstr + '</table>'; //put the parts together
                }
                $('div#formatted').html($outputstr);  //output results as table
                $('div#formattedLabel').text("Formatted results:");
            };
        }
);
