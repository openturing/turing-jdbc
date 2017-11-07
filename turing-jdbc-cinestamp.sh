#! /bin/bash
echo Cast
CAST_SQL="SELECT id, name as title, name as text, name as turing_entity_cast FROM CSMovieDBPerson LIMIT 100"
./turing-jdbc.sh -t CAST_TYPE --include-type-in-id true -z 100 -d com.mysql.cj.jdbc.Driver -c jdbc:mysql://localhost/cinestamp  -q "${CAST_SQL}" -u cinestamp -p cinestamp

echo Movie
MOVIE_SQL="SELECT m.csmoviedb_id as id, m.original_title as title, m.overview as text, m.release_date as original_date, concat_ws('/', 'https://cinestamp.com/movie', m.csmoviedb_id) as url, GROUP_CONCAT(p.name) as turing_entity_cast, GROUP_CONCAT(c.persona) as turing_entity_character FROM CSMovieDBLanguage m, CSMovieDBCast c, CSMovieDBPerson p where c.moviedb_id = m.csmoviedb_id and c.moviedb_person_id = p.id and m.language = 'en' group by c.moviedb_id LIMIT 100"
./turing-jdbc.sh -t MOVIE_TYPE --multi-valued-separator "," --multi-valued-field turing_entity_cast,turing_entity_character --include-type-in-id true -z 100 -d com.mysql.cj.jdbc.Driver -c jdbc:mysql://localhost/cinestamp  -q "${MOVIE_SQL}" -u cinestamp -p cinestamp
