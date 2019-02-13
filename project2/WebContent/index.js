

/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
$(document).ready(function() {
    $('body').hide().fadeIn(1000);
	$("a").click(function(e) {
	    e.preventDefault();
	    $link = $(this).attr("href");
	    $("body").fadeOut(1000,function(){
		    window.location =  $link; 
		});
    });
});


function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function visitPage(type){
	//window.location='index.html?p='+currentPage+'&numRecord='+recordNum+'&genre='+genre+'&sort='+type;
	window.location='index.html?p='+currentPage+'&numRecord='+recordNum+'&genre='+genre+'&Title='+Title+'&Year='+Year+'&Director='+Director+'&Star_name='+Star_name+'&sort='+type;
}

function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    let currentPage = parseInt(getParameterByName('p'));
    let recordNum = parseInt(getParameterByName('numRecord'));
    let moviePage = Math.ceil(parseInt(resultData[0]['movieSize'])/recordNum);
    let genre=getParameterByName('genre');
    //console.log(moviePage);
    
    // Populate the star table
    let starTableBodyElement = jQuery("#movie_table_body");
    for (let i = 1; i <= Math.min(recordNum, resultData.length-1); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        let num=i;
        rowHTML += "<th>" + num + "</th>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-movie.html?p='+currentPage+'&numRecord='+recordNum+'&genre='+genre+'&id=' + resultData[i]['movie_id'] +'&Title='+Title+'&Year='+Year+'&Director='+Director+'&Star_name='+Star_name+ '">'
            + resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        //rowHTML += "<th>" + resultData[i]["genreList"] + "</th>";
        
        //For genres list
        let genreList=resultData[i]["genreList"].split(",");
        rowHTML += "<th>";
        for(let j=0; j<genreList.length; j++){
        	rowHTML+=('<a href="index.html?p='+currentPage+'&numRecord='+recordNum+'&genre='+ genreList[j] + '">'
                    + genreList[j] +     // display movie_name for the link text
                    '</a>'
        	);
        	rowHTML+= ",";
        }
        rowHTML = rowHTML.substring(0, rowHTML.length-1);
        rowHTML += "</th>";
        
        //For stars list
        let stars_name = resultData[i]["stars_name"].split(",");
        let stars_id = resultData[i]["stars_id"].split(",");
        rowHTML += "<th>";
        for (let j=0; j<stars_name.length-1; j++){
        	rowHTML+=('<a href="single-star.html?p='+currentPage+'&numRecord='+recordNum+'&genre='+genre+'&id=' + stars_id[j] + '">'
                    + stars_name[j] +     // display movie_name for the link text
                    '</a>'
        	);
        	rowHTML+= ",";
        }
        rowHTML = rowHTML.substring(0, rowHTML.length-1);
        rowHTML += "</th>";
        
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "<th><a class='btn btn-outline-primary' href='shoppingCart.html?movie=" + resultData[i]["movie_title"] + "'" + "role='button'>Add Cart</a></th>"
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
    
    //pagination
    /*
    let paginationElement = jQuery("#pagination_list");
    let rowHTML = "<ul class='pagination justify-content-center'>";
    if(currentPage-1 >= 0) rowHTML += "<li class='page-item'>";
    else rowHTML += "<li class='page-item disabled'>";
    rowHTML += "<a class='page-link' href='?p="+ (currentPage-1) + '&numRecord=' + recordNum +"'>" + 'Previous' + "</a></li>";
    for (let i = currentPage-3; i <= currentPage+3; i++) {
    	if(i < 0 || i >= moviePage) continue;
    	if(i == currentPage) rowHTML += "<li class='page-item active'>";
    	else rowHTML += "<li class='page-item'>";
    	rowHTML += "<a class='page-link' href='?p="+ i + '&numRecord=' + recordNum +"'>" + (i+1) + "</a></li>";
    }
    if(currentPage+1 < moviePage) rowHTML += "<li class='page-item'>";
    else rowHTML += "<li class='page-item disabled'>";
    rowHTML += "<a class='page-link' href='?p="+ (currentPage+1) + '&numRecord=' + recordNum +"'>" + 'Next' + "</a></li>"+ "</ul>";
    rowHTML += "</ul>";*/
    
    //'<a href="index.html?p=0&numRecord=20&genre='+ resultData[i]["genre_name"] +'">'
    
    let paginationElement = jQuery("#pagination_list");
    let rowHTML = "<ul class='pagination justify-content-center'>";
    if(currentPage-1 >= 0) rowHTML += "<li class='page-item'>";
    else rowHTML += "<li class='page-item disabled'>";
    rowHTML += "<a class='page-link' href='?p="+ (currentPage-1) + '&numRecord=' + recordNum +'&genre='+genre+ "&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+"'>" + 'Previous' + "</a></li>";
    for (let i = currentPage-3; i <= currentPage+3; i++) {
    	if(i < 0 || i >= moviePage) continue;
    	if(i == currentPage) rowHTML += "<li class='page-item active'>";
    	else rowHTML += "<li class='page-item'>";
    	rowHTML += "<a class='page-link' href='?p="+ i + '&numRecord=' + recordNum +'&genre='+genre+"&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+"'>" + (i+1) + "</a></li>";
    }
    if(currentPage+1 < moviePage) rowHTML += "<li class='page-item'>";
    else rowHTML += "<li class='page-item disabled'>";
    rowHTML += "<a class='page-link' href='?p="+ (currentPage+1) + '&numRecord=' + recordNum +'&genre='+genre+"&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+"'>" + 'Next' + "</a></li>"+ "</ul>";
    rowHTML += "</ul>";
    paginationElement.append(rowHTML);
    

    
    
    let movieperPageBtn = jQuery("#movieperPageBtn");
    movieperPageBtn.append("<button class='btn btn-secondary dropdown-toggle btn-sm' type='button' id='dropdownMenuButton' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>"
    		+ "Movie per Page" + "</button>"
    		+ "<div class='dropdown-menu' aria-labelledby='dropdownMenuButton'>"
    		+ "<a class='dropdown-item' href='?p="+ currentPage * Math.floor(recordNum/10) + '&numRecord=' + 10 + '&genre='+genre + "&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+ "'>" + "10" + "</a>"
    		+ "<a class='dropdown-item' href='?p="+ currentPage * Math.floor(recordNum/20) + '&numRecord=' + 20 + '&genre='+genre + "&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+ "'>" + "20" + "</a>"
    		+ "<a class='dropdown-item' href='?p="+ currentPage * Math.floor(recordNum/40) + '&numRecord=' + 40 + '&genre='+genre + "&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type+ "'>" + "40" + "</a>"+ "</div>");
    
    
    let sort_button = jQuery("#sort_button");
    //sort_button.append("<button onclick='location.href='http://www.example.com'' type='button'> test &#9650 </button>")
    
    movieperPageBtn.append(
    		 "<button onclick="+"visitPage('title_up')"+" type='button'> Title &#9650 </button>"
    		+"<button onclick="+"visitPage('title_down')"+" type='button'> Title &#9660 </button>"
            +"<button onclick="+"visitPage('rating_up')"+" type='button'> Rating &#9650 </button>"
            +"<button onclick="+"visitPage('rating_down')"+" type='button'> Rating &#9660 </button>"		
    );
    
}


function handleAddcart(addCardEvent){
	addCardEvent.preventDefault();
	console.log("hello");
//    $.get(
//        "api/shoppingCart",
//        // Serialize the cart form to the data sent by POST request
//        {removeMovie: $("#cart tbody tr td:nth-child(1) input").val()},
//        (resultDataString) => handleCartArray(resultDataString)
//    );
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
let currentPage = parseInt(getParameterByName('p'));
let recordNum = getParameterByName('numRecord');
let genre = getParameterByName('genre');
let Title = getParameterByName('Title');
let Year = getParameterByName('Year');
let Director = getParameterByName('Director');
let Star_name = getParameterByName('Star_name');
let Sort_type = getParameterByName('sort');

// Makes the HTTP GET request and registers on success callback function handleStarResult

/*
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies?p=" + currentPage + "&numRecord=" +recordNum, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});*/

/*
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies?p=" + currentPage + "&numRecord=" +recordNum+"&genre="+genre, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});*/

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies?p=" + currentPage + "&numRecord=" +recordNum+"&genre="+genre+"&Title="+Title+"&Year="+Year+"&Director="+Director+"&Star_name="+Star_name+"&sort="+Sort_type, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});
