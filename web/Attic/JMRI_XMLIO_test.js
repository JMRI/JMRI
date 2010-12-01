$(document).ready(
		function() {
			
			//attach code to Send button
			$('button#btnSend').click(
					function() {
						var $commandstr = '<XMLIO>' + $('textarea#txtData').val() + '</XMLIO>';
//						if ($commandstr != "") {
						$('div#result').empty();
						$('div#formatted').text("sent '"+$commandstr+"'...");
						var $outputstr = "";
						var $headers = [];


						$.get(
								'/xmlio', //<--url
								$commandstr, //<--data
								function($xml) { //<--success
									$('div#result').text((new XMLSerializer()).serializeToString($xml));  //output raw result    				
									$xml = $($xml);  //jQuery-ize returned data for easier access
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

									} else {  //generate a table for results
										var $headerstr = "";  //populate table header
										for (var $i=0; $i < $headers.length; $i++) {
											if ($headers[$i] != null) {  //skip the undefined ones
												$headerstr += "<th>" + $headers[$i] + "</th>";
											}
										}
										$outputstr = "<table>" +$headerstr + $outputstr + '</table>'; //put the parts together
									}
									$('div#formatted').html($outputstr);  //output results as table

								},
								'xml' //<--dataType
						);
//						}
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

			//copy value and go ahead and send it
			$('table#samples td').dblclick(
					function() {
						$('textarea#txtData').val($(this).text());  //set value of send box to sample clicked
						$('button#btnSend').click(); //process the button press
					}
			);

		}
);
