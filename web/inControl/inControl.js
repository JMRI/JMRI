// Please read 'inControl.txt'

// *************** Variables and initial values ***************

// global constants
var cF0Label="F0 - Lights On/Off"; // default for F0
var cF0Image="inControl/Light.png"; // default for F0
var cF0ImagePressed="inControl/LightPressed.png"; // default for F0

// global vars
var vPowerStatus=-1; // Power Status
var vIsIE4_6=false; // flag to identify browser
function objRequestElement() { // object to call XMLIO asynchronously
     this.reqObj=null;
	 this.reqCallBack=function() { // handle onreadystatechange event of objRequestElement.reqObj object
                   // only if objRequestElement.reqObj shows "loaded"
                   if (this.readyState==4) { // only if "OK"
                    if (this.status==200 && this.responseText!="") { // empty to control error on Firefox
                     asyncProcessReturn(this);
                    } else {
                     alert("Communication error.\nCheck connection to train network.");
                    }
                   }
	              }
    };
var vRequestArray=[]; //array to store objects (max=10) to call XMLIO asynchronously
var vDebug; //to retrieve Debug behaviour from query string
var vWaitforResp; //to call XMLIO synchronously
var vRefreshRate; //to retrieve Refresh Rate (seconds) from query string (0= no refresh / default=5)
var vWidth; //to retrieve board Width from query string
var vPower; //to retrieve Power Buttons display behaviour from query string
var vLocoAddress; //to retrieve Loco Address from query string
var vLocoName; //to retrieve Loco Name from query string
var vLocoImage; //to retrieve Loco Image from query string
var vForward; // to store the Forward status
var vFnLabel=new Array(); //to retrieve Function Keys from query string
var vFnImage=new Array(); //to retrieve Function Keys from query string
var vFnImagePressed=new Array(); //to retrieve Function Keys from query string
var vFnToggle=new Array(); //to store the Function Keys status

// *************** Run as soon as possible ***************

window.onerror=function(errMsg,errUrl,errLineNumber) {
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
 vWaitforResp=gup("waitforresp");
 vRefreshRate=gup("refreshrate");
 if (vRefreshRate=="") vRefreshRate="5";
 vRefreshRate=(+vRefreshRate);
 vWidth=gup("width");
 vPower=gup("power");
 vLocoAddress=gup("locoaddress");
 if (vLocoAddress=="") vLocoAddress="3";
 vLocoName=unescape(gup("loconame"));
 vLocoImage=gup("locoimage");
 vForward=true;
 for (var i=0;i<13;i++) {
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
 document.getElementById("divFunctions").innerHTML+='<table frame="void" align="center" border="0" cellspacing="0" cellpadding="0" width="100%"><tr>';
 document.getElementById("divFunctions").innerHTML+='<td>';
 for (var i=0;i<13;i++) {
  if (vFnImage[i]=="") { // Label
   if (vFnLabel[i]=="" && vFnImagePressed[i]!="") vFnLabel[i]="F"+i;
   if (vFnLabel[i]!="") document.getElementById("divFunctions").innerHTML+='<label id="lblF'+i+'" class="lblRect1" onClick="Fn('+i+')">'+vFnLabel[i]+'</label>';
  } else { // Image
   document.getElementById("divFunctions").innerHTML+='<img id="imgF'+i+'" src="'+vFnImage[i]+'" alt="'+((vFnLabel[i]!='')?vFnLabel[i]:'F'+i)+'" title="'+((vFnLabel[i]!='')?vFnLabel[i]:'F'+i)+'" class="imgRect2" onClick="Fn('+i+')" /><br />';
  }
// vFunctionColumns: (calcular se F<i> existente) document.getElementById("divFunctions").innerHTML+='</td><td>';
 }
 if (vPower=="r" || vPower=="R") { // at bottom of the last column
  document.getElementById("divFunctions").innerHTML+='<div id="divPowerRight">';
  document.getElementById("divFunctions").innerHTML+='<table frame="void" align="center" border="0" cellspacing="0" cellpadding="0" width="100%"><tr><td nowrap="nowrap">';
  document.getElementById("divFunctions").innerHTML+='<hr />';
  document.getElementById("divFunctions").innerHTML+='<img id="imgPowerRightOn" src="inControl/PowerGreen24.png" alt="On" title="Power On" class="imgRect1" onClick="PowerOn()" />';
  document.getElementById("divFunctions").innerHTML+='<img id="imgPowerRightOff" src="inControl/PowerRed24.png" alt="Off" title="Power Off" class="imgRect1" onClick="PowerOff()" />';
  document.getElementById("divFunctions").innerHTML+='</td></tr></table>';
  document.getElementById("divFunctions").innerHTML+='</div>';
 }
 document.getElementById("divFunctions").innerHTML+='</td>';
 document.getElementById("divFunctions").innerHTML+='</tr></table>';
 if (vPower!="" && vPower!="r" && vPower!="R") document.getElementById("divPowerLeft").style.display="block";
 ShowDebugInfo();
 PowerStatus();
 ThrottleStatus();
 if (vRefreshRate>0) setTimeout("GetJmriStatusLoop()",vRefreshRate*1000);
}

// debug info
function ShowDebugInfo() {
 var s;
 if (vDebug!="") {
  s="     Input parameters:\n";
  s+="debug: "+vDebug+"\n";
  s+="waitforresp: "+vWaitforResp+"\n";
  s+="refreshrate: "+vRefreshRate+"\n";
  s+="width: "+vWidth+"\n";
  s+="power: "+vPower+"\n";
  s+="locoaddress: "+vLocoAddress+"\n";
  s+="loconame: "+vLocoName+"\n";
  s+="locoimage: "+vLocoImage+"\n";
  for (var i=0;i<13;i++) {
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

// speed up by 10
function PlusPlus() {
 if (vPowerStatus!=2) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=(+document.getElementById("lblSpeed").innerHTML);
 if (lblSpeedValue<91) lblSpeedValue+=10; else lblSpeedValue=100;
 if (loadDoc("SetThrottle",(vWaitforResp!=""),"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n  </throttle>")) if (vWaitforResp=="") document.getElementById("lblSpeed").innerHTML=lblSpeedValue;
}

// speed up
function Plus() {
 if (vPowerStatus!=2) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=document.getElementById("lblSpeed").innerHTML;
 if (lblSpeedValue<100) {
  lblSpeedValue++;
  if (loadDoc("SetThrottle",(vWaitforResp!=""),"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n  </throttle>")) if (vWaitforResp=="") document.getElementById("lblSpeed").innerHTML=lblSpeedValue;
 }
}

// speed down
function Minus() {
 if (vPowerStatus!=2) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=document.getElementById("lblSpeed").innerHTML;
 if (lblSpeedValue>0) {
  lblSpeedValue--;
  if (loadDoc("SetThrottle",(vWaitforResp!=""),"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n  </throttle>")) if (vWaitforResp=="") document.getElementById("lblSpeed").innerHTML=lblSpeedValue;
 }
}

// speed down by 10
function MinusMinus() {
 if (vPowerStatus!=2) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=(+document.getElementById("lblSpeed").innerHTML);
 if (lblSpeedValue>9) lblSpeedValue-=10; else lblSpeedValue=0;
 if (loadDoc("SetThrottle",(vWaitforResp!=""),"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n  </throttle>")) if (vWaitforResp=="") document.getElementById("lblSpeed").innerHTML=lblSpeedValue;
}

// reverse
function Rev() {
 if (vPowerStatus!=2 || !vForward) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=document.getElementById("lblSpeed").innerHTML;
 if (loadDoc("SetThrottle",(vWaitforResp!=""),"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n    <forward>false</forward>\n  </throttle>")) if (vWaitforResp=="") {vForward=false; UpdateForward();}
}

// forward
function Frw() {
 if (vPowerStatus!=2 || vForward) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=document.getElementById("lblSpeed").innerHTML;
 if (loadDoc("SetThrottle",(vWaitforResp!=""),"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n    <forward>true</forward>\n  </throttle>")) if (vWaitforResp=="") {vForward=true; UpdateForward();}
}

// STOP
function STOP() {
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 if (loadDoc("SetThrottle",(vWaitforResp!=""),"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>-1</speed>\n  </throttle>")) if (vWaitforResp=="") document.getElementById("lblSpeed").innerHTML=0;
}

// throttle status
function ThrottleStatus() {
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 loadDoc("GetThrottle",(vWaitforResp!=""),"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n  </throttle>");
}

// power status
function PowerStatus() {
 loadDoc("GetPower",(vWaitforResp!=""),"  <list>\n    <type>power</type>\n  </list>");
}

// power ON
function PowerOn() {
 if (vPowerStatus==2) return;
 if (loadDoc("SetPower",true,"  <item>\n    <type>power</type>\n    <name>power</name>\n    <set>2</set>\n  </item>")) PowerStatus();
}

// power OFF
function PowerOff() {
 if (vPowerStatus==4) return;
 if (loadDoc("SetPower",true,"  <item>\n    <type>power</type>\n    <name>power</name>\n    <set>4</set>\n  </item>")) PowerStatus();
}

// Fn (functions)
function Fn(fn) {
 if (vPowerStatus!=2) return;
 var lblLocoAddressValue=document.getElementById("lblLocoAddress").innerHTML;
 var lblSpeedValue=document.getElementById("lblSpeed").innerHTML;
 if (!vFnToggle[fn]) {
  if (loadDoc("SetThrottle",(vWaitforResp!=""),"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n    <F"+fn+">true</F"+fn+">\n  </throttle>")) {
   if (vWaitforResp=="") {vFnToggle[fn]=true; UpdateFK(fn);}
  }
 } else {
  if (loadDoc("SetThrottle",(vWaitforResp!=""),"  <throttle>\n    <address>"+lblLocoAddressValue+"</address>\n    <speed>"+lblSpeedValue/100+"</speed>\n    <F"+fn+">false</F"+fn+">\n  </throttle>")) {
   if (vWaitforResp=="") {vFnToggle[fn]=false; UpdateFK(fn);}
  }
 }
}

// sends an XML request
function loadDoc(id,WaitforResp,xmlData) {
 if (!WaitforResp) return asyncLoadXMLDoc(id,"/xmlio", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xmlio>\n"+ xmlData+"\n</xmlio>");
 else return syncLoadXMLDoc(id,"/xmlio", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xmlio>\n"+ xmlData+"\n</xmlio>");
}

// retrieve XML document (reusable generic function);
// parameter is URL string that includes the XML request
function syncLoadXMLDoc(id,url,data) {
 var request;
 var r=false;
 try {
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
 }
 catch(e) {
  alert("Communication error.\nCheck connection to train network.");
 }
 return r;
}

// handle sync response of request object
function syncProcessReqResponse(id,request) {
 var r=false;
 if (request.status==200 && request.responseText!="") { // empty to control error on Firefox
  r=syncProcessReturn(id,request);
 } else {
  alert("Error response from JMRI server.");
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
 for (var i=0;i<items.length;i++) {
  itemName=getElementTextNS("","speed", items[i],0);
  if (+itemName<0) itemName="0";
  if (itemName!="") document.getElementById("lblSpeed").innerHTML=itemName*100;
  itemName=getElementTextNS("","forward", items[i],0);
  if (itemName!="") vForward=(itemName=="true");
  UpdateForward();
  for (var j=0;j<13;j++) {
   itemName=getElementTextNS("","F"+j, items[i],0);
   if (itemName!="") vFnToggle[j]=(itemName=="true");
   UpdateFK(j);
  }
 }
 if (items.length>0) return r;
 items=request.responseXML.getElementsByTagName("item"); // only 'item'
 for (var i=0;i<items.length;i++) {
  itemName=getElementTextNS("","name", items[i],0);
  switch(itemName) {
   case "power": {
    if (id=="GetPower") r=ReturnedPowerStatus(getElementTextNS("","value", items[i],0)); else if (id=="SetPower") r=true;
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
 try {
  if (vRequestArray.length>9) vRequestArray.pop();
  vRequestArray.unshift(new objRequestElement());
  if (vDebug!="") alert("XML to send asynchronously to '"+url+"':\n"+data);
  // branch for native XMLHttpRequest object
  if (window.XMLHttpRequest) {
   vRequestArray[0].reqObj=new XMLHttpRequest();
   vRequestArray[0].reqObj.onreadystatechange=vRequestArray[0].reqCallBack;
   vRequestArray[0].reqObj.open("POST",url,true);
   vRequestArray[0].reqObj.send(data);
   // GET method also works for short requests:
   // vRequestArray[0].reqObj.open("GET",url+"/"+data,true);
   // vRequestArray[0].reqObj.send(null);
   r=true;
   // branch for IE/Windows ActiveX version
  } else if (window.ActiveXObject) {
   vIsIE4_6=true;
   vRequestArray[0].reqObj=new ActiveXObject("Microsoft.XMLHTTP");
   if (vRequestArray[0].reqObj) {
    vRequestArray[0].reqObj.onreadystatechange=vRequestArray[0].reqCallBack;
    vRequestArray[0].reqObj.open("POST",url,true);
    vRequestArray[0].reqObj.send(data);
    r=true;
   } else alert("Error creating XMLHTTP object.");
  } else alert("No XMLHTTP function.");
 }
 catch(e) {
  alert("Error sending XMLHTTP async message.");
 }
 return r;
}

// process returned XML document
function asyncProcessReturn(request) {
 var items;
 var itemName;
 if (vDebug!="") alert("Response from JMRI (async):\n"+request.responseText);
 items=request.responseXML.getElementsByTagName("throttle"); // only 'throttle'
 for (var i=0;i<items.length;i++) {
  itemName=getElementTextNS("","speed", items[i],0);
  if (+itemName<0) itemName="0";
  if (itemName!="") document.getElementById("lblSpeed").innerHTML=itemName*100;
  itemName=getElementTextNS("","forward", items[i],0);
  if (itemName!="") vForward=(itemName=="true");
  UpdateForward();
  for (var j=0;j<13;j++) {
   itemName=getElementTextNS("","F"+j, items[i],0);
   if (itemName!="") vFnToggle[j]=(itemName=="true");
   UpdateFK(j);
  }
 }
 if (items.length>0) return;
 items=request.responseXML.getElementsByTagName("item"); // only 'item'
 for (var i=0;i<items.length;i++) {
  itemName=getElementTextNS("","name", items[i],0);
  switch(itemName) {
   case "power": {
    ReturnedPowerStatus(getElementTextNS("","value", items[i],0));
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
  return "";
 }
}

// Forward On/Off
function UpdateForward() {
 document.getElementById("imgRev").src=(vForward?"inControl/LeftRed.png":"inControl/LeftGreen.png");
 document.getElementById("imgFrw").src=(vForward?"inControl/RightGreen.png":"inControl/RightRed.png");
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

// returned power status
function ReturnedPowerStatus(value) {
 var r=true;
 switch(value) {
  case "0": { //undefined
   vPowerStatus=0;
   document.bgColor="Gainsboro";
   document.body.style.backgroundColor="Gainsboro";
   if (document.getElementById("imgPowerLeftOn")) document.getElementById("imgPowerLeftOn").src="inControl/PowerGreen24.png";
   if (document.getElementById("imgPowerLeftOff")) document.getElementById("imgPowerLeftOff").src="inControl/PowerRed24.png";
   if (document.getElementById("imgPowerRightOn")) document.getElementById("imgPowerRightOn").src="inControl/PowerGreen24.png";
   if (document.getElementById("imgPowerRightOff")) document.getElementById("imgPowerRightOff").src="inControl/PowerRed24.png";
   break;
  }
  case "2": { //power on
   vPowerStatus=2;
   document.bgColor="LightGreen";
   document.body.style.backgroundColor="LightGreen";
   if (document.getElementById("imgPowerLeftOn")) document.getElementById("imgPowerLeftOn").src="inControl/PowerGrey24.png";
   if (document.getElementById("imgPowerLeftOff")) document.getElementById("imgPowerLeftOff").src="inControl/PowerRed24.png";
   if (document.getElementById("imgPowerRightOn")) document.getElementById("imgPowerRightOn").src="inControl/PowerGrey24.png";
   if (document.getElementById("imgPowerRightOff")) document.getElementById("imgPowerRightOff").src="inControl/PowerRed24.png";
   break;
  }
  case "4": { //power off
   vPowerStatus=4;
   document.bgColor="Tomato";
   document.body.style.backgroundColor="Tomato";
   if (document.getElementById("imgPowerLeftOn")) document.getElementById("imgPowerLeftOn").src="inControl/PowerGreen24.png";
   if (document.getElementById("imgPowerLeftOff")) document.getElementById("imgPowerLeftOff").src="inControl/PowerGrey24.png";
   if (document.getElementById("imgPowerRightOn")) document.getElementById("imgPowerRightOn").src="inControl/PowerGreen24.png";
   if (document.getElementById("imgPowerRightOff")) document.getElementById("imgPowerRightOff").src="inControl/PowerGrey24.png";
   break;
  }
  default: {
   r=false;
  }
 }
 return r;
}
