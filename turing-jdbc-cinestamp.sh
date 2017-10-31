#! /bin/bash

./turing-jdbc.sh -d com.mysql.jdbc.Driver -c jdbc:mysql://localhost/cinestamp  -q "select  csmoviedb_id as id, original_title as title, overview as text, release_date as original_date, concat_ws('/', 'https://cinestamp.com/movie', csmoviedb_id) as url from CSMovieDBLanguage where language = 'en' limit 100" -u cinestamp -p cinestamp
