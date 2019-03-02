
function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");
    
    // Populate the star table
    let confirm = jQuery("#confirmation_table_body");
    for (let i = 0; i < resultData.length; i++) {
    	let rowHTML = "";
    	rowHTML += "<tr>";
    	let num=i+1;
    	rowHTML += "<th>" + num + "</th>";
    	rowHTML += "<th>" + resultData[i]["sale_id"] + "</th>";
    	rowHTML += "<th>" + resultData[i]["movie_title"] + "</th>";
    	rowHTML += "<th>" + resultData[i]["quantity"] + "</th>";
        // Append the row created to the table body, which will refresh the page
    	confirm.append(rowHTML);
    }
}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/confirm",
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});