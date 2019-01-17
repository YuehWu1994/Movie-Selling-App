create database moviedb;
use moviedb;

CREATE TABLE movies (
    id VARCHAR(10) PRIMARY KEY NOT NULL,
    title VARCHAR(100) DEFAULT '',
    year INT NOT NULL,
    director VARCHAR(100) DEFAULT ''
);
CREATE TABLE stars (
    id VARCHAR(10) PRIMARY KEY NOT NULL,
    name VARCHAR(100) DEFAULT '',
    birthYear INT
);
CREATE TABLE stars_in_movies (
	starId VARCHAR(10) DEFAULT '',
    movieId VARCHAR(10) DEFAULT '',
    FOREIGN KEY (starId) REFERENCES stars(id) ON DELETE CASCADE,
    FOREIGN KEY (movieId) REFERENCES movies(id) ON DELETE CASCADE
);
CREATE TABLE genres (
	id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    name VARCHAR(32) DEFAULT ''
);
CREATE TABLE genres_in_movies (
	genreId INT NOT NULL,
	movieId VARCHAR(10) DEFAULT '',
    FOREIGN KEY (genreId) REFERENCES genres(id) ON DELETE CASCADE,
    FOREIGN KEY (movieId) REFERENCES movies(id) ON DELETE CASCADE
);
CREATE TABLE creditcards (
    id VARCHAR(20) PRIMARY KEY DEFAULT '',
    firstName VARCHAR(50) DEFAULT '',
    lastName VARCHAR(50) DEFAULT '',
    expiration DATE NOT NULL
);
CREATE TABLE customers (
	id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    firstName VARCHAR(50) DEFAULT '',
    lastName VARCHAR(50) DEFAULT '',
    ccId VARCHAR(20) DEFAULT '',
    address VARCHAR(200) DEFAULT '',
    email VARCHAR(50) DEFAULT '',
    password VARCHAR(20) DEFAULT '',
    FOREIGN KEY (ccId) REFERENCES creditcards(id) on DELETE CASCADE
);
CREATE TABLE sales (
    id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    customerId INT NOT NULL,
    FOREIGN KEY (customerId) REFERENCES customers(id) on DELETE CASCADE,
    movieId VARCHAR(10) DEFAULT '',
    FOREIGN KEY (movieId) REFERENCES movies(id) on DELETE CASCADE,
    saleDate DATE NOT NULL
);

CREATE TABLE ratings (
    movieId VARCHAR(10) DEFAULT '',
    FOREIGN KEY (movieId) REFERENCES movies(id) on DELETE CASCADE,
    rating FLOAT(8) NOT NULL,
    numVotes INT NOT NULL
);