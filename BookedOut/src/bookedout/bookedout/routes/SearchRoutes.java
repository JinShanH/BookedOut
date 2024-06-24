package bookedout.routes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import bookedout.GenreID;
import spark.Request;
import spark.Response;

public class SearchRoutes {
// ----- HELPERS -----
    /**
     * Returns a ResultSet of bookIDs
     * @param keywords
     * @return
     */
    private static ResultSet simpleSearchBooks(String[] keywords, Connection db) throws SQLException {
        String[] fields = new String[]{"isbn", "title", "description", "publisher"};
        int[] genres = Arrays.stream(keywords).filter(s -> GenreID.getID(s) >= 0).flatMapToInt(s -> IntStream.of(GenreID.getID(s))).toArray();
        ArrayList<String> fieldConditions = new ArrayList<String>();
        // Build list of conditions for fields
        for (int i = 0; i < fields.length; i++) {
            for (int j = 0; j < keywords.length; j++) {
                fieldConditions.add(fields[i] + " LIKE '%" + keywords[j] +"%'");
            }
        }

        // Convert conditions for fields to cases
        ArrayList<String> fieldCases = new ArrayList<String>();
        fieldConditions.forEach(con -> fieldCases.add("(CASE WHEN " + con + " THEN 1 ELSE 0 END)"));
        ArrayList<String> fieldSelect = new ArrayList<String>();
        fieldConditions.forEach(con -> fieldSelect.add("(" + con + ")"));
        // Generate first selection from books table
        String selectFromBooks = "SELECT bookID, " + String.join(" + ", fieldCases) + " AS priority";
        selectFromBooks = selectFromBooks + " FROM books WHERE " + String.join(" OR ", fieldSelect);
        
        // Convert conditions for genres to cases
        ArrayList<String> genreCases = new ArrayList<String>();
        ArrayList<String> genreSelect = new ArrayList<String>();
        for (int i = 0; i < genres.length; i++) {
            genreCases.add("(CASE WHEN genre=" + Integer.toString(genres[i]) + " THEN 1 ELSE 0 END)");
            genreSelect.add("(genre=" + Integer.toString(genres[i]) + ")");
        }
        // Generate second selection from genre table
        String selectFromGenres = "";
        if (genres.length > 0) {
            selectFromGenres = "SELECT bookID, " + String.join(" + ", genreCases) + " AS priority";
            selectFromGenres = selectFromGenres + " FROM bookGenres WHERE " + String.join(" OR ", genreSelect);
        }

        // Convert conditions for authors to cases
        ArrayList<String> authorCases = new ArrayList<String>();
        ArrayList<String> authorSelect = new ArrayList<String>();
        for (int i = 0; i < keywords.length; i++) {
            authorCases.add("(CASE WHEN author LIKE '%" + keywords[i] + "%' THEN 1 ELSE 0 END)");
            authorSelect.add("(author LIKE '%" + keywords[i] + "%')");
        }
        // Generate third selection from authors table
        String selectFromAuthors = "SELECT bookID, " + String.join(" + ", authorCases) + " AS priority";
        selectFromAuthors = selectFromAuthors + " FROM bookAuthors WHERE " + String.join(" OR ", authorSelect); 

        // Generate final SQL statement and execute
        String sql = "SELECT books.bookID, books.isbn, books.title, books.coverPath, books.ratings, books.avgRating, " +
        " books.readers, GROUP_CONCAT(DISTINCT genre) AS genres, GROUP_CONCAT(DISTINCT author) AS authors, SUM(priority) AS priority FROM (";
        sql = sql + selectFromBooks;
        sql = sql + " UNION ALL " + selectFromAuthors;
        if (genres.length > 0) sql = sql + " UNION ALL " + selectFromGenres;
        sql = sql + ") union1 INNER JOIN books ON union1.bookID = books.bookID LEFT JOIN bookAuthors ON union1.bookID = bookAuthors.bookID "+
        "LEFT JOIN bookGenres ON union1.bookID = bookGenres.bookID GROUP BY bookID ORDER BY priority DESC;";
        return db.createStatement().executeQuery(sql);
    }

    /**
     * Returns a ResultSet of userIDs
     * @param keywords
     * @return
     */
    private static ResultSet simpleSearchUsers(String[] keywords, Connection db) throws SQLException {
        String[] fields = new String[]{"username", "displayname"};
        ArrayList<String> fieldConditions = new ArrayList<String>();

        // Build list of conditions for fields
        for (int i = 0; i < fields.length; i++) {
            for (int j = 0; j < keywords.length; j++) {
                fieldConditions.add(fields[i] + " LIKE '%" + keywords[j] +"%'");
            }
        }

        // Convert conditions for fields to cases
        ArrayList<String> fieldCases = new ArrayList<String>();
        fieldConditions.forEach(con -> fieldCases.add("(CASE WHEN " + con + " THEN 1 ELSE 0 END)"));
        ArrayList<String> fieldSelect = new ArrayList<String>();
        fieldConditions.forEach(con -> fieldSelect.add("(" + con + ")"));

        // Generate first selection from users table
        String selectFromUsers = "SELECT userID, username, displayname, " + String.join(" + ", fieldCases) + " AS priority";
        selectFromUsers = selectFromUsers + " FROM users WHERE " + String.join(" OR ", fieldSelect);
        
        // Generate final SQL statement and execute
        String sql = selectFromUsers + " ORDER BY priority DESC;";
        return db.createStatement().executeQuery(sql);
    }

    /**
     * Returns a ResultSet of collectionIDs
     * @param keywords
     * @return
     */
    private static ResultSet simpleSearchCollections(String[] keywords, Connection db) throws SQLException {
        String[] fields = new String[]{"name", "description"};
        ArrayList<String> fieldConditions = new ArrayList<String>();

        // Build list of conditions for fields
        for (int i = 0; i < fields.length; i++) {
            for (int j = 0; j < keywords.length; j++) {
                fieldConditions.add(fields[i] + " LIKE '%" + keywords[j] +"%'");
            }
        }

        // Convert conditions for fields to cases
        ArrayList<String> fieldCases = new ArrayList<String>();
        fieldConditions.forEach(con -> fieldCases.add("(CASE WHEN " + con + " THEN 1 ELSE 0 END)"));
        ArrayList<String> fieldSelect = new ArrayList<String>();
        fieldConditions.forEach(con -> fieldSelect.add("(" + con + ")"));

        // Generate first selection from collections table
        String selectFromCollections = "SELECT collectionID, ownerID, " + String.join(" + ", fieldCases) + " AS priority";
        selectFromCollections = selectFromCollections + " FROM collections WHERE isPublic=true AND (" + String.join(" OR ", fieldSelect) + ")";

        // Try and match author
        fields = new String[]{"username", "displayname"};
        fieldConditions = new ArrayList<String>();

        // Build list of conditions for fields
        for (int i = 0; i < fields.length; i++) {
            for (int j = 0; j < keywords.length; j++) {
                fieldConditions.add(fields[i] + " LIKE '%" + keywords[j] +"%'");
            }
        }

        // Convert conditions for fields to cases
        ArrayList<String> fieldCases2 = new ArrayList<String>();
        fieldConditions.forEach(con -> fieldCases2.add("(CASE WHEN " + con + " THEN 1 ELSE 0 END)"));
        ArrayList<String> fieldSelect2 = new ArrayList<String>();
        fieldConditions.forEach(con -> fieldSelect2.add("(" + con + ")"));

        // Generate second selection from users table
        String selectFromUsers = "SELECT collections.collectionID, collections.ownerID, " + String.join(" + ", fieldCases2) + " AS priority";
        selectFromUsers = selectFromUsers + " FROM (collections INNER JOIN users ON collections.ownerID=users.userID) WHERE isPublic=true AND (" + String.join(" OR ", fieldSelect2) + ")";
        
        // Generate final SQL statement and execute
        String sql = "SELECT collections.collectionID, collections.name, collections.created, collections.lastUpdated, collections.ratings, collections.avgRating," +
        " GROUP_CONCAT(DISTINCT users.displayname) as owner, SUM(priority) AS priority FROM (";
        sql = sql + selectFromCollections + " UNION ALL " + selectFromUsers + ") union1 LEFT JOIN collections ON union1.collectionID = collections.collectionID " +
        "LEFT JOIN users ON union1.ownerID = users.userID GROUP BY collectionID ORDER BY priority DESC";
        return db.createStatement().executeQuery(sql);
    }

    /**
     * Returns a list of book IDs matching search criteria. 
     * Simple method: Search a table (book, user, collection) and test if each keyword matches a column. Each column match is
     * worth 1 point. Sum points and sort descending on points, then return the list
     * sorted that way
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return String that greets a user by display name
     */
    public static ResultSet simpleSearch(String searchSubject, String[] keywords, Connection db) throws SQLException {
        // Select search type
        switch (searchSubject) {
            case "books":
                return simpleSearchBooks(keywords, db);
            case "collections":
                return simpleSearchCollections(keywords, db);
            case "users":
                return simpleSearchUsers(keywords, db);
            default:
                return null;
        }
    }

// -----  ROUTES -----

    /**
     * Returns a list of book IDs matching search criteria. 
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return a list of book IDs matching search criteria. 
     */
    public static String search(Request req, Response res, Connection db) {
        // Set subject
        String searchSubject = req.params(":subject");

        // Get keywords and perform search
        ResultSet rs;
        String[] keywords = req.queryParams("q").split(" ");
        try {
            rs = simpleSearch(searchSubject, keywords, db);
        } catch (SQLException ex) {
            res.status(500);
            return "{\"msg\":\"Error getting data from database.\"}";
        }
        
        // Check that the search subject was found correctly
        if (rs == null) {
            res.status(404);
            return "{\"msg\":\"Could not find search subject\"}";
        }
        
        // Build JSON Array string from the ResultSet
        String array = "[";
        try {
            int columns = rs.getMetaData().getColumnCount() - 1;
            String[] headers = new String[columns];
            for (int i = 0; i < columns; i++) {
                headers[i] = rs.getMetaData().getColumnLabel(i + 1);
            }
            while (rs.next()) {
                array = array + "{";
                for (int i = 0; i < columns - 1; i++) {
                    array = array + "\"" + headers[i] + "\":\"" + rs.getString(i + 1) + "\", ";
                }
                array = array + "\"" + headers[columns - 1] + "\":\"" + rs.getString(columns) + "\"";
                array = array + "},"; 
            }
            array = array.substring(0, array.length() - 1) + "]";
            if (array.equals("]")) array = "[]";
        } catch (SQLException ex) {
            res.status(500);
            return "{\"msg\":\"Unknown SQL Exception in parsing.\"}";
        }

        // Return payload
        return "{\"msg\":\"Success\", \"" + searchSubject + "\":" + array + "}";
    }
}
