//setup image refresh loop  (script inserted by JmriJFrameServlet.java)
$(function () {
  setInterval(function () {
    var img = new Image();
    $(img).load(function () {
        $("#frame_image").html(this);
    }).error(function () {
    }).attr("src", "/frame/footscray.png?r="+ Math.floor(Math.random()*9999))
      .attr("ismap", "true");
  },  noclickRetryTime * 1000);  //set refresh time in milliseconds
});
