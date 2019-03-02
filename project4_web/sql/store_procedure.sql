CREATE PROCEDURE how_is_it (IN x INT) 
BEGIN
	IF (x > 5) THEN
		SELECT CONCAT(x, " is higher") as answer;
	ELSE
		SELECT CONCAT(x, " is lower") as answer;
	END IF; 
END
$$


DELIMITER $$
CREATE PROCEDURE addMovie (IN title VARCHAR(100), IN year INT, IN director VARCHAR(100), IN starName VARCHAR(100), IN genreName VARCHAR(32), OUT movieExist INT, OUT starExist INT, OUT genreExist INT) 
BEGIN
	SET @mxMovie = (SELECT MAX(id) from movies);
	SET @tmp =  CONVERT(SUBSTRING(@mxMovie, 3),UNSIGNED INTEGER) + 1;
	SET @mxMovie = CONCAT ("tt",CONVERT(@tmp, CHAR(10)));

	SET @mxStar = (select MAX(id) from stars);
	SET @tmp =  CONVERT(SUBSTRING(@mxStar, 3),UNSIGNED INTEGER) + 1;
	SET @mxStar = CONCAT ("nm",CONVERT(@tmp, CHAR(10)));

	SET @mxGenre = (select MAX(id) from genres);
	SET @mxGenre = @mxGenre + 1;

	SET @movie = (SELECT m.title from movies m where m.title = title and m.year = year and m.director = director);
	IF (@movie IS NULL) THEN
		SET movieExist = 0;
		INSERT INTO movies VALUES(@mxMovie,title,year,director);
		select 'Insert movie' AS '';
		# If star exist
		IF (starName Not IN (SELECT name from stars)) THEN
			SET starExist = 0;
			INSERT INTO stars (id, name) VALUES(@mxStar,starName);
			select 'Insert stars' AS '';
			INSERT INTO stars_in_movies VALUES(@mxStar,@mxMovie);
			select 'Insert stars_in_movies new' AS '';
		ELSE
			SET starExist = 1;
			SET @existStar = (SELECT id from stars where stars.name = starName limit 1);
			INSERT INTO stars_in_movies VALUES(@existStar,@mxMovie);
			select 'Insert stars_in_movies exist' AS '';

		END IF;
		
		# If genre exist
		IF (genreName Not IN (SELECT name from genres)) THEN
			SET genreExist = 0;
			INSERT INTO genres (id, name) VALUES(@mxGenre,genreName);
			select 'Insert genres' AS '';
			INSERT INTO genres_in_movies VALUES(@mxGenre,@mxMovie);
			select 'Insert genres_in_movies new' AS '';
		ELSE
			SET genreExist = 1;
			SET @existGenre = (SELECT id from genres where genres.name = genreName limit 1);
			INSERT INTO genres_in_movies VALUES(@existGenre,@mxMovie);
			select 'Insert genres_in_movies exist' AS '';			
		END IF;


		# output
	ELSE
		SET movieExist = 1;
	END IF;
END
$$
DELIMITER ;

