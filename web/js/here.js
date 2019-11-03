/*
Highlight the li element with href link to the current page in the left sidebar
Since 11 2019
*/

document.documentElement.className="hasJS";

$(function(){
  $('a').each(function() {
    if ($(this).prop('href') == window.location.href) {
      $(this.parentNode).addClass('here');
    }
  });
})
