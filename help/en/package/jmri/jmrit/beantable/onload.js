    function onLoad() {
	  if (window.location.href.startsWith("file:///") && !window.location.href.endsWith("?inside"))
	  {
        var urlParts = window.location.href.split("/help/en/");
        window.location.href = urlParts[0] + "/help/en/local/index.html?url=" + window.location.href + "?inside";
	  }
    }
