package bookedout;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

public class Review {
    private int bookID;
    private int reviewID;
    private int reviewerID;
    private String reviewerName;
    private int rating;
    private boolean textReview;
    private String title;
    private String body;
    private Date date;
    private boolean edited;

    private Review() { }

    private static Review buildReviewFromResult(ResultSet rs) throws SQLException {
        // Result must be the result of * reviews JOIN users
        Review review = new Review();
        review.bookID = rs.getInt(1);
        review.reviewID = rs.getInt(2);
        review.reviewerID = rs.getInt(3);
        review.rating = rs.getInt(4);
        review.textReview = rs.getBoolean(5);
        review.title = rs.getString(6);
        review.body = rs.getString(7);
        review.date = rs.getDate(8);
        review.edited = rs.getBoolean(9);
        review.reviewerName = rs.getString(12);
        if (review.reviewerName == null) review.reviewerName = "DELETED USER";
        return review;
    }

    public static Review generateReviewByID(int reviewID, Connection db) throws SQLException {
        Statement st = db.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM reviews LEFT JOIN users ON reviews.reviewerID=users.userID WHERE reviewID=" + Integer.toString(reviewID) + ";");
        if (rs.next()) {
            return buildReviewFromResult(rs);
        } else {
            return null;
        }
    }

    public static ArrayList<Review> getBookReviews(int bookID, Connection db) throws SQLException {
        Statement st = db.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM reviews LEFT JOIN users ON reviews.reviewerID=users.userID WHERE bookID=" + Integer.toString(bookID) +" AND textReview = TRUE ORDER BY date DESC;");
        ArrayList<Review> reviews = new ArrayList<Review>();
        while (rs.next()) {
            reviews.add(buildReviewFromResult(rs));
        }
        return reviews;
    }

    public static Boolean reviewExists(int userID, int bookID, Connection db) {
        try {
            ResultSet rs = db.createStatement().executeQuery("SELECT COUNT(reviewID) FROM reviews WHERE bookID=" + 
            Integer.toString(bookID) + " AND reviewerID=" + Integer.toString(userID) + ";");
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException ex) {
            return false;
        }
    }

    public static void createReview(int bookID, int reviewerID, int rating, boolean textReview, String title, String body, Connection db) throws SQLException {
        String sql = "INSERT INTO reviews (bookID, reviewerID, rating, textReview";
        Statement st = db.createStatement();
        if (textReview) sql = sql + ", title, body";
        sql = sql + ") VALUES (" + Integer.toString(bookID) + ", " + Integer.toString(reviewerID) + ", " +
                Integer.toString(rating) + ", " + Boolean.toString(textReview);
        if (textReview) sql = sql + ", \"" + title + "\", \"" + body + "\"";
        sql = sql + ");";
        st.execute(sql);
        
        sql = "UPDATE books SET ratings = ratings + 1 WHERE bookID=" + Integer.toString(bookID) + ";";
        st.execute(sql); 

        sql = "UPDATE books SET avgRating = avgRating + (" + Integer.toString(rating) + " - avgRating) / ratings WHERE bookID = " + Integer.toString(bookID) + ";";
        st.execute(sql); 
    }

    public static void deleteReview(int reviewID, Connection db) throws SQLException {
        Review r = generateReviewByID(reviewID, db);
        int rating = r.rating;
        int bookID = r.bookID;
        Statement st = db.createStatement();
        st.execute("DELETE FROM reviews WHERE reviewID = " + Integer.toString(reviewID) + ";");
        st.execute("UPDATE books SET ratings = ratings - 1 WHERE bookID = " + Integer.toString(bookID) + ";");
        st.execute("UPDATE books SET avgRating = (CASE WHEN ratings = 0 THEN 0 ELSE avgRating + (avgRating - " + Integer.toString(rating) + ") / ratings END) WHERE bookID = " + Integer.toString(bookID) + ";");
    }

    public static void updateReview(int bookID, int reviewID, int rating, int oldRating, boolean textReview, String title, String body, Connection db) throws SQLException {
        String sql = "UPDATE reviews SET rating = " + Integer.toString(rating) + ", textReview = " + Boolean.toString(textReview);
        if (textReview) sql = sql + ", title=\"" + title + "\", body=\"" + body + "\"";
        sql = sql + ", edited = TRUE WHERE reviewID = " + Integer.toString(reviewID) + ";";
        Statement st = db.createStatement();
        st.execute(sql);
        st.execute("UPDATE books SET avgRating = avgRating + (" + Integer.toString(rating) + " - " + Integer.toString(oldRating) + ") / ratings WHERE bookID = " + Integer.toString(bookID) + ";");
    }

    public String toJSONObject() {
        return "{\"bookID\":" + Integer.toString(bookID) + ", " +
                "\"reviewID\":" + Integer.toString(reviewID) + ", " +
                "\"reviewerID\":" + Integer.toString(reviewerID) + ", " +
                "\"reviewerName\":\"" + reviewerName + "\", " +
                "\"rating\":" + Integer.toString(rating) + ", " +
                "\"textReview\":" + Boolean.toString(textReview) + ", " +
                "\"title\":\"" + title + "\", " +
                "\"body\":\"" + body + "\", " +
                "\"date\":\"" + date.toString() + "\", " +
                "\"edited\":" + Boolean.toString(edited) + "}";
    }

    public int getBookID() {
        return bookID;
    }

    public int getReviewID() {
        return reviewID;
    }

    public int getReviewerID() {
        return reviewerID;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public int getRating() {
        return rating;
    }

    public boolean isTextReview() {
        return textReview;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public Date getDate() {
        return date;
    }

    public boolean isEdited() {
        return edited;
    }
  
}
