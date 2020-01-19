


$(document).ready(function () {

  $('.mobile-nav-btn').on('click', function () {
    $('.hamburger-menu').toggleClass('open');
  });

});


$(document).ready(function () {
/* var navheight = $('#navbar').height(); */

	$("#scrollTop, .btn-slide").click(function() {
	    // get the destination
	    var destination = $(this).attr('href');
	    
	    $('html, body').stop().animate({
	        scrollTop: $(destination).offset().top
	    }, 700);
	    
	    // override the default link click behavior
	    return false;
	});

});


