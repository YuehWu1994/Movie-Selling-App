/**
 * Handle the data returned by IndexServlet
 * @param resultDataString jsonObject, consists of session info
 */
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


function handleSessionData(resultDataString) {
    resultDataJson = JSON.parse(resultDataString);

    console.log("handle session response");
    console.log(resultDataJson);
    console.log(resultDataJson["sessionID"]);
    // show the session information 
    //$("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
//    $("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);
}

/**
 * Handle the items in item list 
 * @param resultDataString jsonObject, needs to be parsed to html 
 */
function handleCartArray(resultDataString) {
    const resultArray = resultDataString.split(",");
    //console.log(resultArray.length);
    // append default movie at shopping cart
    let buyMovie = getParameterByName('movie');
    $('#title').val(buyMovie);
    
    // append shopping list table   
    let res = "<h2 class='text-center'>Shopping List</h2>"
    res += ("<table id='star_table' class='table table-striped'>");
    res += ("<thead><tr><th>No.</th><th>Title</th><th>Qty</th><th></th></tr></thead><tbody>");
    for(let i = 1; i < resultArray.length; i += 2){
    	let num = Math.floor(i/2)+1;
    	res += "<tr>";
    	res += "<td>" + num + "</td>";
    	res += "<td>" + resultArray[i-1] + "</td>"
    	res += "<td>" + resultArray[i] + "</td>" 
    	var strM = resultArray[i-1];
    	strM = strM.replace(/ /g, '_'); // g: global search
    	console.log(strM);
    	res += "<td><button class='btn btn-outline-primary' onclick="+ "handleRemoval('" + strM + "')" + " type='button'>Remove</button></td>"
    	res += "</tr>";
    }
    res += "</tbody></table>";
    
    // clear the old array and show the new array in the frontend
    $("#item_list").html("");
    $("#item_list").append(res);
}


/**
 * Submit form content with POST method
 * @param cartEvent
 */
function handleCartInfo(cartEvent) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    cartEvent.preventDefault();

    $.get(
        "api/shoppingCart",
        // Serialize the cart form to the data sent by POST request
        $("#cart").serialize(),
        (resultDataString) => handleCartArray(resultDataString)
    );
}

//function handleRemovalOld(purchaseEvent) {
//    console.log("Click Remove");
//    /**
//     * When users click the submit button, the browser will not direct
//     * users to the url defined in HTML form. Instead, it will call this
//     * event handler when the event is triggered.
//     */
//    //alert("Text: " + $("#cart tbody tr td:nth-child(1) input").attr("value"));
//    //alert("Text: " + $("#cart tbody tr td:nth-child(1) input").val());
//    purchaseEvent.preventDefault();
//
//    $.get(
//        "api/shoppingCart",
//        // Serialize the cart form to the data sent by POST request
//        {removeMovie: $("#cart tbody tr td:nth-child(1) input").val()},
//        (resultDataString) => handleCartArray(resultDataString)
//    );
//}

function handleRemoval(movie) {
	var strM = movie;
	strM = strM.replace(/_/g, ' ');
	console.log(strM);
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    //alert("Text: " + $("#cart tbody tr td:nth-child(1) input").attr("value"));
    //alert("Text: " + $("#cart tbody tr td:nth-child(1) input").val());

    $.get(
          "api/shoppingCart",
          // Serialize the cart form to the data sent by POST request
          {removeMovie: strM},
          (resultDataString) => handleCartArray(resultDataString)
    );
}


//$.ajax({
//    type: "POST",
//    url: "api/shoppingCart",
//    success: (resultDataString) => handleSessionData(resultDataString)
//});

$.ajax({
    method: "GET",
    url: "api/shoppingCart",
    success: (resultDataString) => handleCartArray(resultDataString)
});

// Bind the submit action of the form to a event handler function
$("#cart").submit((event) => handleCartInfo(event));
//$("#btnText").click((event) => handleRemovalOld(event));

let store_qry={};

function handleLookup(query, doneCallback) {
	console.log("autocomplete initiated")
	//console.log("sending AJAX request to backend Java Servlet")
	
	// TODO: if you want to check past query results first, you can do it here
	if(query in store_qry){
		console.log("Return query result in cache");
		handleLookupAjaxSuccess(store_qry[query], query, doneCallback);
		return;
	}
	
	// sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
	// with the query data
	console.log("Return query result in Ajax");
	jQuery.ajax({
		"method": "GET",
		// generate the request url from the query.
		// escape the query string to avoid errors caused by special characters 
		"url": "main_page?query=" + escape(query),
		"success": function(data) {
			// pass the data, query, and doneCallback function into the success handler
			handleLookupAjaxSuccess(data, query, doneCallback) 
		},
		"error": function(errorData) {
			console.log("lookup ajax error")
			console.log(errorData)
		}
	})
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
	//console.log("lookup ajax successful");
	
	let data_sliced;
	for (let i=0; i<data.length; i++){
		if("value" in data[i]){
			data_sliced=data.slice(i);
			break;
		}
	}
	
	if(data_sliced == null) return;
	console.log("suggestion list");
	console.log(data_sliced);
	var jsonData = JSON.parse(JSON.stringify(data_sliced.slice(0,10)));

	// TODO: if you want to cache the result into a global variable you can do it here
	store_qry[query]=data;
	
	// call the callback function provided by the autocomplete library
	// add "{suggestions: jsonData}" to satisfy the library response format according to
	//   the "Response Format" section in documentation
	doneCallback( { suggestions: jsonData } );
}

/*
 * This function is the select suggestion handler function. 
 * When a suggestion is selected, this function is called by the library.
 * 
 * You can redirect to the page you want using the suggestion data.
 */
//let genre = getParameterByName('genre');

function handleSelectSuggestion(suggestion) {
	// TODO: jump to the specific result page based on the selected suggestion
	console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movie_id"])
	//window.location='index.html?p=0&numRecord=20&genre=&Title='+suggestion["value"]+'&autocom=true';
	window.location='single-movie.html?Title='+suggestion["value"]+'&id='+suggestion["data"]["movie_id"];
}

//$('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
	// documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
    		handleLookup(query, doneCallback)
    },
    
    onSelect: function(suggestion) {
    		handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
    minChars: 3,
});

/*
 * do normal full text search if no suggestion is selected 
 */
function handleNormalSearch(query) {
	console.log("doing normal search with query: " + query);
	// TODO: you should do normal search here
	window.location='index.html?p=0&numRecord=20&genre=&Title='+query;
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
	// keyCode 13 is the enter key
	if (event.keyCode == 13) {
		// pass the value of the input box to the handler function
		handleNormalSearch($('#autocomplete').val())
	}
})

