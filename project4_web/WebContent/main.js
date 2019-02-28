function handleStarResult(resultData) {

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let genresElement = jQuery("#genres");
    let titlesElement = jQuery("#titles");
    let rowHTML = "";
    
//    index.html?p=0?numRecord=20
    		
    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {
    	//?genre=' + resultData[i]["genre_name"] + '
    	/*
    	rowHTML+='<a href="index.html?p=0&numRecord=20">'
        + resultData[i]["genre_name"] +     // display movie_name for the link text
        '</a>'+'&nbsp&nbsp';*/
    	
    	
    	rowHTML+='<a href="index.html?p=0&numRecord=20&genre='+ resultData[i]["genre_name"] +'">'
        + resultData[i]["genre_name"] +     // display movie_name for the link text
        '</a>'+'&nbsp&nbsp';
    	
    	if(i > 0 && i % 8 == 0){
    		rowHTML+="<br/>";
    	}
    	//rowHTML+=resultData[i]["genre_name"];
    }
    genresElement.append(rowHTML);
    
    rowHTML = "";
    for (let i=0; i<9; i++){
    	rowHTML+='<a href="index.html?p=0&numRecord=20&genre=' + i + '">'+i+'</a>'+'&nbsp | &nbsp';
    }
    rowHTML+='<a href="index.html?p=0&numRecord=20&genre=' + 9 + '">'+9+'</a>'+'<br/>';
    
    for(let i = "A".charCodeAt(0); i <= "L".charCodeAt(0); i++) {
    	rowHTML+='<a href="index.html?p=0&numRecord=20&genre=' + String.fromCharCode(i) + '">'+String.fromCharCode(i)+'</a>'+'&nbsp | &nbsp';
    }
    rowHTML+='<a href="index.html?p=0&numRecord=20&genre=' + 'M'+ '">'+'M'+'</a>'+'<br/>';
    for(let i = "N".charCodeAt(0); i <= "Y".charCodeAt(0); i++) {
    	rowHTML+='<a href="index.html?p=0&numRecord=20&genre=' + String.fromCharCode(i) + '">'+String.fromCharCode(i)+'</a>'+'&nbsp | &nbsp';
    }
    rowHTML+='<a href="index.html?p=0&numRecord=20&genre=' + 'Z'+ '">'+'Z'+'</a>'+'<br/>';
    titlesElement.append(rowHTML);
}

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "main_page", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});