// Please read 'inControl.txt'

// *************** Variables and initial values ***************

// global constants
var cF0Label="F0 - Lights On/Off"; // default for F0
var cF0Image="inControl/Light.png"; // default for F0
var cF0ImagePressed="inControl/LightPressed.png"; // default for F0

// global vars
var vPowerStatus=-1; // Power Status
var vIsIE4_6=false; // flag to identify browser
var vRequest; // request and XML document objects
var vDebug; //to retrieve Debug behaviour from query string
var vRefreshRate; //to retrieve Refresh Rate (seconds) from query string (0= no refresh / default=5)
var vWidth; //to retrieve board Width from query string
var vPower; //to retrieve Power Buttons display behaviour from query string
var vLocoAddress; //to retrieve Loco Address from query string
var vLocoName; //to retrieve Loco Name from query string
var vLocoImage; //to retrieve Loco Image from query string
var vFnLabel=new Array(); //to retrieve Function Keys from query string
var vFnImage=new Array(); //to retrieve Function Keys from query string
var vFnImagePressed=new Array(); //to retrieve Function Keys from query string
var vFnToggle=new Array(); //to store the Function Keys status

// *************** Run as soon as possible ***************

window.onerror=function(errMsg, errUrl, errLineNumber) {
 alert("Error running javascript: "+errMsg+"\nURL: "+errUrl+"\nLine Number: "+errLineNumber);
 return true;
}

function GetJmriStatusLoop() { // get JMRI status
 PowerStatus();
 if (vRefreshRate>0) setTimeout("GetJmriStatusLoop()",vRefreshRate*1000);
}
 
 // *************** Functions ***************

// run after page loading (inControl.html)
function inControl() {
 var arr;
 vDebug=gup("debug");
 vRefreshRate=gup("refreshrate");
 if (vRefreshRate=="") vRefreshRate="5";
 vRefreshRate=(+vRefreshRate);
 vWidth=gup("width");
 vPower=gup("power");
 vLocoAddress=gup("locoaddress");
 if (vLocoAddress=="") vLocoAddress="3";
 vLocoName=unescape(gup("loconame"));
 vLocoImage=gup("locoimage");
 for (var i=0;i<30;i++) {
  vFnLabel[i]=unescape(gup("f"+i+"label"));
  vFnImage[i]=gup("f"+i+"image");
  vFnImagePressed[i]=gup("f"+i+"imagepressed");
  vFnToggle[i]=false;
 }
 if (vFnLabel[0]=="") vFnLabel[0]=cF0Label;
 if (vFnImage[0]=="") vFnImage[0]=cF0Image;
 if (vFnImagePressed[0]=="") vFnImagePressed[0]=cF0ImagePressed;
 if (vWidth!="") {
  document.getElementById("tblBoard").style.width=vWidth+"px";
  document.getElementById("tdBoard").style.width=vWidth+"px";
  document.getElementById("tdLeft").style.width=Math.round(vWidth*0.55)+"px";
  document.getElementById("tdRight").style.width=Math.round(vWidth*0.45)+"px";
  document.getElementById("imgLocoImage").style.width=vWidth+"px";
 }
 document.getElementById("lblLocoName").innerHTML=vLocoName;
 document.title+=" - "+vLocoName+" ("+vLocoAddress+")";
 document.getElementById("lblLocoAddress").innerHTML=vLocoAddress;
 if (vDebug!="") {
  document.getElementById("divLocoNameAddress").onclick=ShowDebugInfo;
  document.getElementById("divLocoNameAddress").title="Click for Debug info";
 }
if (vLocoImage!="") {
  document.getElementById("imgLocoImage").style.display="block";
  document.getElementById("imgLocoImage").src=vLocoImage;
  if (vDebug!="") {
   document.getElementById("imgLocoImage").onclick=ShowDebugInfo;
   document.getElementById("imgLocoImage").title="Click for Debug info";
  }
 }
 document.getElementById("lblSpeed").innerHTML=0;
 for (var i=0;i<30;i++) {
  if (vFnImage[i]=="") { // Label
   if (vFnLabel[i]=="" && vFnImagePressed[i]!="") vFnLabel[i]="F"+i;
   if (vFnLabel[i]!="") document.getElementById("divFunctions").innerHTML+='<label id="lblF'+i+'" class="lblRect1" onClick="Fn('+i+')">'+vFnLabel[i]+'</label>';
  } else { // Image
   document.getElementById("divFunctions").innerHTML+='<img id="imgF'+i+'" src="'+vFnImage[i]+'" alt="'+((vFnLabel[i]!='')?vFnLabel[i]:'F'+i)+'" title="'+((vFnLabel[i]!='')?vFnLabel[i]:'F'+i)+'" class="imgRect2" onClick="Fn('+i+')" /><br />';
  }
 }
 if (vPower=="r" || vPower=="R") document.getElementById("divPowerRight").style.display="block";
 else if (vPower!="") document.getElementById("divPowerLeft").style.display="block";
 ShowDebugInfo();
 PowerStatus();
 UpdateFKs();
 if (vRefreshRate>0) setTimeout("GetJmriStatusLoop()",vRefreshRate*1000);
}

// debug info
function ShowDebugInfo() {
 var s;
 if (vDebug!="") {
  s="     Input parameters:\n";
  s+="debug: "+vDebug+"\n";
  s+="refreshrate: "+vRefreshRate+"\n";
  s+="width: "+vWidth+"\n";
  s+="power: "+vPower+"\n";
  s+="locoaddress: "+vLocoAddress+"\n";
  s+="loconame: "+vLocoName+"\n";
  s+="locoimage: "+vLocoImage+"\n";
  for (var i=0;i<30;i++) {
   if (vFnLabel[i]!="") s+="f"+i+"label: "+vFnLabel[i]+"\n";
   if (vFnImage[i]!="") s+="f"+i+"image: "+vFnImage[i]+"\n";
   if (vFnImagePressed[i]!="") s+="f"+i+"imagepressed: "+vFnImagePressed[i]+"\n";
  }
  s+="\n";
  s+="     Misc size:\n";
  s+="tblBoard Width: "+document.getElementById("tblBoard").style.width+"\n";
  s+="imgLocoImage Width: "+document.getElementById("imgLocoImage").style.width+"\n";
  s+="tdLeft Width: "+document.getElementById("tdLeft").style.width+"\n";
  s+="tdRight Width: "+document.getElementById("tdRight").style.width+"\n";
  s+="window Left: "+posLeft()+"\n";
  s+="window Top: "+posTop()+"\n";
  s+="window Right: "+posRight()+"\n";
  s+="window Bottom: "+posBottom()+"\n";
  s+="window Width: "+pageWidth()+"\n";
  s+="window Height: "+pageHeight();
  alert(s);
 }
}

// Browser Window Size and Position
// copyright Stephen Chapman, 3rd Jan 2005, 8th Dec 2005
// you may copy these functions but please keep the copyright notice as well
function pageWidth() {return window.innerWidth != null? window.innerWidth : document.documentElement && document.documentElement.clientWidth ? document.documentElement.clientWidth : document.body != null ? document.body.clientWidth : null;}
function pageHeight() {return  window.innerHeight != null? window.innerHeight : document.documentElement && document.documentElement.clientHeight ? document.documentElement.clientHeight : document.body != null? document.body.clientHeight : null;}
function posLeft() {return typeof window.pageXOffset != 'undefined' ? window.pageXOffset :document.documentElement && document.documentElement.scrollLeft ? document.documentElement.scrollLeft : document.body.scrollLeft ? document.body.scrollLeft : 0;}
function posTop() {return typeof window.pageYOffset != 'undefined' ?  window.pageYOffset : document.documentElement && document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop ? document.body.scrollTop : 0;}
function posRight() {return posLeft()+pageWidth();}
function posBottom() {return posTop()+pageHeight();}

// retrieve query string value
function gup(name) {
 name=name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
 var regexS="[\\?&]"+name+"=([^&#]*)";
 var regex=new RegExp(regexS);
 var results=regex.exec(window.location.href);
 if(results==null) return ""; else return results[1];
}

// all Function Key On/Off
function UpdateFKs() {
 for (var i=0;i<30;i++) UpdateFK(i);
}

// Function Key On/Off
function UpdateFK(f) {
 if (vFnImagePressed[f]!="") {
  if (vFnImage[f]=="") { // Label
   document.getElementById("lblF"+f).className=(vFnToggle[f]?"lblRect1Pressed":"lblRect1UnPressed");
  } else { // Image
   document.getElementById("imgF"+f).src=(vFnToggle[f]?vFnImagePressed[f]:vFnImage[f]);
  }
 }
}

// speed up by 10
function PlusPlus() {
 if (vPowerStatus!=2) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=(+document.getElementById("lblSpeed").innerHTML);
 if (lblSpeedValue<91) lblSpeedValue+=10; else lblSpeedValue=100;
 if (loadDoc("SetThrottle",false,"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n  </throttle>")) document.getElementById("lblSpeed").innerHTML=lblSpeedValue;
}

// speed up
function Plus() {
 if (vPowerStatus!=2) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=document.getElementById("lblSpeed").innerHTML;
 if (lblSpeedValue<100) {
  lblSpeedValue++;
  if (loadDoc("SetThrottle",false,"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n  </throttle>")) document.getElementById("lblSpeed").innerHTML=lblSpeedValue;
 }
}

// speed down
function Minus() {
 if (vPowerStatus!=2) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=document.getElementById("lblSpeed").innerHTML;
 if (lblSpeedValue>0) {
  lblSpeedValue--;
  if (loadDoc("SetThrottle",false,"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n  </throttle>")) document.getElementById("lblSpeed").innerHTML=lblSpeedValue;
 }
}

// speed down by 10
function MinusMinus() {
 if (vPowerStatus!=2) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=(+document.getElementById("lblSpeed").innerHTML);
 if (lblSpeedValue>9) lblSpeedValue-=10; else lblSpeedValue=0;
 if (loadDoc("SetThrottle",false,"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n  </throttle>")) document.getElementById("lblSpeed").innerHTML=lblSpeedValue;
}

// reverse
function Rev() {
 if (vPowerStatus!=2) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=document.getElementById("lblSpeed").innerHTML;
 loadDoc("SetThrottle",false,"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n    <forward>false</forward>\n  </throttle>");
}

// forward
function Frw() {
 if (vPowerStatus!=2) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=document.getElementById("lblSpeed").innerHTML;
 loadDoc("SetThrottle",false,"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n    <forward>true</forward>\n  </throttle>");
}

// STOP
function STOP() {
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 if (loadDoc("SetThrottle",false,"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>-1</speed>\n  </throttle>")) document.getElementById("lblSpeed").innerHTML=0;
}

// power status
function PowerStatus() {
 loadDoc("GetPower",false,"  <list>\n    <type>power</type>\n  </list>");
}

// power ON
function PowerOn() {
 if (vPowerStatus==2) return;
 if (loadDoc("SetPower",false,"  <item>\n    <type>power</type>\n    <name>power</name>\n    <set>2</set>\n  </item>")) PowerStatus();
}

// power OFF
function PowerOff() {
 if (vPowerStatus==4) return;
 if (loadDoc("SetPower",false,"  <item>\n    <type>power</type>\n    <name>power</name>\n    <set>4</set>\n  </item>")) PowerStatus();
}

// Fn (functions)
function Fn(fn) {
 if (vPowerStatus!=2) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=document.getElementById("lblSpeed").innerHTML;
 if (!vFnToggle[fn]) {
  if(loadDoc("SetThrottle",false,"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n    <F"+fn+">true</F"+fn+">\n  </throttle>")) vFnToggle[fn]=true;
 } else {
  if (loadDoc("SetThrottle",false,"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n    <F"+fn+">false</F"+fn+">\n  </throttle>")) vFnToggle[fn]=false;
 }
 UpdateFK(fn);
}

// sends an XML request
function loadDoc(id,async,xmlData) {
 var r=false;
 try {
  if (async) r=asyncLoadXMLDoc(id,"/xmlio", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xmlio>\n"+ xmlData+"\n</xmlio>");
  else r=syncLoadXMLDoc(id,"/xmlio", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xmlio>\n"+ xmlData+"\n</xmlio>");
 }
 catch(e) {
  alert("Communication error.\nCheck connection to train network.");
 }
 return r;
}

// retrieve XML document (reusable generic function);
// parameter is URL string that includes the XML request
function syncLoadXMLDoc(id,url,data) {
 var request;
 var r=false;
 if (vDebug!="") alert("XML to send synchronously to '"+url+"':\n"+data);
 // branch for native XMLHttpRequest object
 if (window.XMLHttpRequest) {
  request=new XMLHttpRequest();
  request.open("POST",url,false);
  request.send(data);
  // GET method also works for short requests:
  // request.open("GET",url+"/"+data,false);
  // request.send(null);
  r=syncProcessReqResponse(id,request);
  // branch for IE/Windows ActiveX version
 } else if (window.ActiveXObject) {
  vIsIE4_6=true;
  request=new ActiveXObject("Microsoft.XMLHTTP");
  if (request) {
   request.open("POST",url,false);
   request.send(data);
   r=syncProcessReqResponse(id,request);
  } else alert("Error creating XMLHTTP object.");
 } else alert("No XMLHTTP function.");
 return r;
}

// handle sync response of request object
function syncProcessReqResponse(id,request) {
 var r=false;
 if (request.status==200 && request.responseText!="") { // empty to control error on Firefox
  r=syncProcessReturn(id,request);
 } else {
  alert("Communication error.\nCheck connection to train network.");
 }
 return r;
}

// process returned XML document
function syncProcessReturn(id,request) {
 var r=false;
 var items;
 var itemName;
 if (vDebug!="") alert("Response from JMRI (sync):\n"+request.responseText);
 items=request.responseXML.getElementsByTagName("throttle"); // only 'throttle'
 if (items.length>0 && id=="SetThrottle") return true;
 items=request.responseXML.getElementsByTagName("item"); // only 'item'
 for (var i=0;i<items.length;i++) {
  itemName=getElementTextNS("","name", items[i],0);
  switch(itemName) {
   case "power": {
    if (id=="GetPower") r=ReturnedPowerStatus(getElementTextNS("","value", items[i],0)); else if (id="SetPower") r=true;
    break;
   }
  }
 }
 return r;
}

// retrieve XML document (reusable generic function);
// parameter is URL string that includes the XML request
function asyncLoadXMLDoc(id,url,data) {
 var r=false;
 if (vDebug!="") alert("XML to send asynchronously to '"+url+"':\n"+data);
 // branch for native XMLHttpRequest object
 if (window.XMLHttpRequest) {
  vRequest=new XMLHttpRequest();
  vRequest.onreadystatechange=asyncProcessReqChange;
  vRequest.open("POST",url,true);
  vRequest.send(data);
  // GET method also works for short requests:
  // vRequest.open("GET",url+"/"+data,true);
  // vRequest.send(null);
  r=true;
  // branch for IE/Windows ActiveX version
 } else if (window.ActiveXObject) {
  vIsIE4_6=true;
  vRequest=new ActiveXObject("Microsoft.XMLHTTP");
  if (vRequest) {
   vRequest.onreadystatechange=asyncProcessReqChange;
   vRequest.open("POST",url,true);
   vRequest.send(data);
   r=true;
  } else alert("Error creating XMLHTTP object.");
 } else alert("No XMLHTTP function.");
 return r;
}

// handle onreadystatechange event of vRequest object
function asyncProcessReqChange() {
 // only if vRequest shows "loaded"
 if (vRequest.readyState==4) {
  // only if "OK"
  if (vRequest.status==200 && vRequest.responseText!="") { // empty to control error on Firefox
   asyncProcessReturn();
  } else {
   alert("Communication error.\nCheck connection to train network.");
  }
 }
}

// process returned XML document
function asyncProcessReturn() {
 var items;
 var itemName;
 if (vDebug!="") alert("Response from JMRI (async):\n"+vRequest.responseText);
 items=vRequest.responseXML.getElementsByTagName("item"); // only 'item', not 'throttle'
 for (var i=0;i<items.length;i++) {
  itemName=getElementTextNS("","name", items[i],0);
  switch(itemName) {
   case "xpto": {
// for future use
    break;
   }
  }
 }
}

// retrieve text of an XML document element, including
// elements using namespaces
function getElementTextNS(prefix,local,parentElem,index) {
 var result="";
 if (prefix && vIsIE4_6) {
  // IE/Windows way of handling namespaces
  result=parentElem.getElementsByTagName(prefix+":"+local)[index];
 } else {
  // the namespace versions of this method 
  // (getElementsByTagNameNS()) operate
  // differently in Safari and Mozilla, but both
  // return value with just local name, provided 
  // there aren't conflicts with non-namespace element
  // names
  result=parentElem.getElementsByTagName(local)[index];
 }
 if (result) {
  // get text, accounting for possible
  // whitespace (carriage return) text nodes 
  if (result.childNodes.length>1) {
   return result.childNodes[1].nodeValue;
  } else {
   return result.firstChild.nodeValue;    		
  }
 } else {
  return "n/a";
 }
}

// returned power status
function ReturnedPowerStatus(value) {
 var r=true;
 switch(value) {
  case "0": { //undefined
   vPowerStatus=0;
   document.bgColor="Gainsboro";
   document.body.style.backgroundColor="Gainsboro";
   document.getElementById("imgPowerLeftOn").src="inControl/PowerGreen24.png";
   document.getElementById("imgPowerLeftOff").src="inControl/PowerRed24.png";
   document.getElementById("imgPowerRightOn").src="inControl/PowerGreen24.png";
   document.getElementById("imgPowerRightOff").src="inControl/PowerRed24.png";
   break;
  }
  case "2": { //power on
   vPowerStatus=2;
   document.bgColor="LightGreen";
   document.body.style.backgroundColor="LightGreen";
   document.getElementById("imgPowerLeftOn").src="inControl/PowerGrey24.png";
   document.getElementById("imgPowerLeftOff").src="inControl/PowerRed24.png";
   document.getElementById("imgPowerRightOn").src="inControl/PowerGrey24.png";
   document.getElementById("imgPowerRightOff").src="inControl/PowerRed24.png";
   break;
  }
  case "4": { //power off
   vPowerStatus=4;
   document.bgColor="Tomato";
   document.body.style.backgroundColor="Tomato";
   document.getElementById("imgPowerLeftOn").src="inControl/PowerGreen24.png";
   document.getElementById("imgPowerLeftOff").src="inControl/PowerGrey24.png";
   document.getElementById("imgPowerRightOn").src="inControl/PowerGreen24.png";
   document.getElementById("imgPowerRightOff").src="inControl/PowerGrey24.png";
   break;
  }
  default: {
   r=false;
  }
 }
 return r;
}
