//script inserted by JmriJFrameServlet.java, which also inserts some variables used here

$(function () {
	//setup infinite image refresh loop  
	setInterval(function () {
		reloadImage();
	},  noclickRetryTime * 1000);  //set refresh time in milliseconds

	//handle click on image div
	$('div#frame_image_wrapper').click(function(event) {
		//send response as if clicked on
		$.get("/frame/" + frameName + ".html?" + event.pageX + "," + event.pageY, function(data) {
			reloadImage();  //update the image after successful call
		});
//		return false; 
	});

});

function reloadImage() {
	var img = new Image();
	$(img).load(function () {
		$("div#frame_image_wrapper").html(this);  //load updated image into wrapper
	}).error(function () { //nothing
	}).attr("src", "/frame/" + frameName + ".png?r="+ Math.floor(Math.random()*9999)) //include rand to force browser rtv
	.attr("ismap", "true");
}