#! /bin/bash

./turing-jdbc.sh -t MOVIE_TYPE -z 100 -d com.mysql.jdbc.Driver -c jdbc:mysql://localhost/cinestamp  -q "select  csmoviedb_id as id, original_title as title, overview as text, release_date as original_date, concat_ws('/', 'https://cinestamp.com/movie', csmoviedb_id) as url from CSMovieDBLanguage where language = 'en'" -u cinestamp -p cinestamp
