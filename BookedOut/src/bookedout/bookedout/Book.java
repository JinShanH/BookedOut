package bookedout;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mysql.cj.protocol.Resultset;

/**
 * Book object
 * @author T10A-BookedOut
 */

public class Book {

    public int bookID = 0;
    public String isbn;
    public String title;
    public String description;
    public ArrayList<String> authors;
    public String publisher;
    public Date published;
    public String cover;
    public int ratings = 0;
    public double avgRating = 0;
    public int readers = 0;
    public ArrayList<Review> reviews;
    public ArrayList<Integer> collections;
    
    public Book() {
        this.authors = new ArrayList<String>();
        this.reviews = new ArrayList<Review>();
        this.collections = new ArrayList<Integer>();
    }

    // Simple constructor
    public Book(int bookID, String isbn, String title, String description) {
        this();

        this.bookID = bookID;
        this.isbn = isbn;
        this.title = title;
        this.description = description;
    }
    
    // Result set must be result of SELECT book.*, GROUP_CONCAT(author), GROUP_CONCAT(genre) FROM books LEFT JOIN
    public static Book buildBookFromResult(ResultSet rs, Connection db) throws SQLException {
        Book book = new Book();
        book.bookID = rs.getInt(1);
        book.isbn = rs.getString(2);
        book.title = rs.getString(3);
        book.description = rs.getString(4);
        book.publisher = rs.getString(5);
        book.published = rs.getDate(6);
        book.cover = rs.getString(7);
        book.ratings = rs.getInt(8);
        book.avgRating = rs.getDouble(9);
        book.readers = rs.getInt(10);
        String[] authors = rs.getString(11).split(",");
        for (int i = 0; i < authors.length; i++) book.authors.add(authors[i]);
        book.reviews = Review.getBookReviews(book.bookID, db);
        return book;
    }

    public String toJSONObject() {
        String json = "{";
        json = json + "\"bookID\":" + Integer.toString(this.bookID) + ",";
        json = json + "\"isbn\":\"" + this.isbn + "\",";
        json = json + "\"title\":\"" + this.title + "\",";
        json = json + "\"description\":\"" + this.description + "\",";
        json = json + "\"publisher\":\"" + this.publisher + "\",";
        json = json + "\"published\":\"" + this.published.toString() + "\",";
        json = json + "\"cover\":\"" + this.cover + "\",";
        json = json + "\"ratings\":" + Integer.toString(this.ratings) + ",";
        json = json + "\"avgRating\":" + Double.toString(this.avgRating) + ",";
        json = json + "\"readers\":" + Integer.toString(this.readers) + ",";
        json = json + "\"authors\":\"" + String.join(", ", this.authors) + "\",";
        json = json + "\"reviews\":" + "[";
        for (int i = 0; i < this.reviews.size(); i++) {
            json = json + this.reviews.get(i).toJSONObject();
            if (i+1 < this.reviews.size()) json = json + ",";
        }
        json = json + "]";
        json = json + "}";
        return json;
    }

    public static Book generateBookByID(int bookID, Connection db) throws SQLException {
        Statement st = db.createStatement();
        ResultSet rs = st.executeQuery("SELECT books.*, GROUP_CONCAT(DISTINCT author) AS authors, GROUP_CONCAT(DISTINCT genre) AS genres FROM " +
                                        "books LEFT JOIN bookAuthors ON books.bookID=bookAuthors.bookID LEFT JOIN bookGenres ON books.bookID = bookGenres.bookID " +
                                        "WHERE books.bookID=" + Integer.toString(bookID) + " GROUP BY books.bookID;");
        try {
            rs.next();
            return buildBookFromResult(rs, db);
        } catch (SQLException ex) {
            return null;
        }
    }

    /**
     * Create a book object using the isbn
     * Assumes that the isbn is valid
     * @param isbn
     * @param db
     * @return book object
     * @throws SQLException
     */
    public static Book generateBookbyISBN(String isbn, Connection db) throws SQLException {
        Statement st = db.createStatement();
        String query = "SELECT books.*, GROUP_CONCAT(DISTINCT author) AS authors, GROUP_CONCAT(DISTINCT genre) AS genres FROM books " +
                        "LEFT JOIN bookAuthors ON books.bookID = bookAuthors.bookID " +
                        "LEFT JOIN bookGenres ON books.bookID = bookGenres.bookID " +
                        "WHERE books.isbn = '" + isbn + "' GROUP BY books.bookID;";
        ResultSet rs = st.executeQuery(query);
        try {
            rs.next();
            return buildBookFromResult(rs, db);
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            System.out.println("generateBookbyISBN: Error connecting to database");
            return null;
        }
    }

    public Book createBookObject(Integer bookID, Connection db) {
        return null;
    }

    public int getID() {
        return this.bookID;
    }

    public String getISBN() {
        return this.isbn;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public ArrayList<String> getAuthors() {
        return this.authors;
    }

    public String getPublisher() {
        return this.publisher;
    }

    public Date getPublished() {
        return this.published;
    }

    public String getCover() {
        return this.cover;
    }

    public int getRatings() {
        return this.ratings;
    }

    public double getAvgRating() {
        return this.avgRating;
    }

    public ArrayList<Review> getReviews() {
        return this.reviews;
    }

    public ArrayList<Integer> getCollections() {
        return this.collections;
    }

    public int getReaders() {
        return this.readers;
    }
}