#! /bin/bash
echo EN_US
echo Cast
CAST_SQL=`cat character_en.sql`
./turing-jdbc.sh --site 1 -t Character --include-type-in-id true -z 100 -d com.mysql.cj.jdbc.Driver -c jdbc:mysql://localhost/cinestamp  -q "${CAST_SQL}" -u cinestamp -p cinestamp

echo Movie
MOVIE_SQL=`cat movie_en.sql` 
./turing-jdbc.sh --site 1 -t Movie --multi-valued-separator "," --multi-valued-field cast,persona,streaming,tv,genres --include-type-in-id true -z 100 -d com.mysql.cj.jdbc.Driver -c jdbc:mysql://localhost/cinestamp  -q "${MOVIE_SQL}" -u cinestamp -p cinestamp

#echo PT_BR
#echo Cast
#CAST_SQL="SELECT id, name as title, name as text, name as cast FROM CSMovieDBPerson LIMIT 100"
#./turing-jdbc.sh --site 2 -t CAST_TYPE --include-type-in-id true -z 100 -d com.mysql.cj.jdbc.Driver -c jdbc:mysql://localhost/cinestamp  -q "${CAST_SQL}" -u cinestamp -p cinestamp

#echo Movie
#MOVIE_SQL="SELECT m.csmoviedb_id as id, m.original_title as title, m.overview as text, m.release_date as original_date, concat_ws('/', 'https://cinestamp.com/movie', m.csmoviedb_id) as url, GROUP_CONCAT(p.name) as cast, GROUP_CONCAT(c.persona) as persona FROM CSMovieDBLanguage m, CSMovieDBCast c, CSMovieDBPerson p where c.moviedb_id = m.csmoviedb_id and c.moviedb_person_id = p.id and m.language = 'pt' group by c.moviedb_id LIMIT 100"
#./turing-jdbc.sh --site 2 -t MOVIE_TYPE --multi-valued-separator "," --multi-valued-field cast,persona --include-type-in-id true -z 100 -d com.mysql.cj.jdbc.Driver -c jdbc:mysql://localhost/cinestamp  -q "${MOVIE_SQL}" -u cinestamp -p cinestamp
