package bookedout;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

public class User {
    private int userID = 0;
    private String username;
    private String displayName;
    private String email;
    private List<Integer> userCollections;
    private List<Integer> followedCollections;
    private List<Integer> reviews;
    private List<Integer> followers;
    private List<Integer> following;
    private List<Integer> notifications;
    private List<Integer> unreadNotifications;
    private List<Integer> similarUsers;
    private List<Integer> bookRecs;
    private List<Integer> readWishlist;
    private List<Integer> readBooks;
    private List<Integer> favouriteBooks;
    private Integer readingGoal = 0;
    private Integer booksReadMonth = 0;
    private String favouriteAuthor;
    private Integer favouriteBook = -1;
    private Integer favouriteGenre = -1;
    private Integer pagesRead = 0;
    private Integer booksRead = 0;
    private String password;

    private User() {
        this.userCollections = new ArrayList<Integer>();
        this.followedCollections = new ArrayList<Integer>();
        this.reviews = new ArrayList<Integer>();
        this.followers = new ArrayList<Integer>();
        this.following = new ArrayList<Integer>();
        this.notifications = new ArrayList<Integer>();
        this.unreadNotifications = new ArrayList<Integer>();
        this.similarUsers = new ArrayList<Integer>();
        this.bookRecs = new ArrayList<Integer>();
        this.readWishlist = new ArrayList<Integer>();
        this.readBooks = new ArrayList<Integer>();
        this.favouriteBooks = new ArrayList<Integer>();
    }

    /**
     * Creates and returns a user object by their ID from the database 
     * @param userID ID of user to fetch
     * @param db Connection to database to fetch from
     * @return User object representing the requested user in the database
     */
    public static User generateUserByID(Integer userID, Connection db) {
        try {
            User user = new User();
            Statement st = db.createStatement();
            // Get basic profile info
            ResultSet rs = st.executeQuery("SELECT * FROM users WHERE userID='" + Integer.toString(userID) + "';");
            rs.next();
            user.userID = rs.getInt(1);
            user.username = rs.getString(2);
            user.displayName = rs.getString(3);
            user.email = rs.getString(4);
            user.readingGoal = rs.getInt(5);
            user.booksReadMonth = rs.getInt(6);
            user.favouriteAuthor = rs.getString(7);
            if (user.favouriteAuthor == null) user.favouriteAuthor = "No Favourite Author Set";
            user.favouriteBook = rs.getInt(8);
            user.favouriteGenre = rs.getInt(9);
            user.booksRead = rs.getInt(10);
            user.pagesRead = rs.getInt(11);
            user.password = rs.getString(12);
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Creates and returns a user object by their username from the database
     * @param username Username of the user to fetch
     * @param db Connection to database to fetch from
     * @return User object representing the requested user in the database
    */ 
    public static User generateUserByName(String username, Connection db) {
        try {
            User user = new User();
            Statement st = db.createStatement();
            // Get basic profile info
            ResultSet rs = st.executeQuery("SELECT * FROM users WHERE username='" + username + "';");
            rs.next();
            user.userID = rs.getInt(1);
            user.username = rs.getString(2);
            user.displayName = rs.getString(3);
            user.email = rs.getString(4);
            user.readingGoal = rs.getInt(5);
            user.booksReadMonth = rs.getInt(6);
            user.favouriteAuthor = rs.getString(7);
            if (user.favouriteAuthor == null) user.favouriteAuthor = "No Favourite Author Set";
            user.favouriteBook = rs.getInt(8);
            user.favouriteGenre = rs.getInt(9);
            user.booksRead = rs.getInt(10);
            user.pagesRead = rs.getInt(11);
            user.password = rs.getString(12);
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Generates a user in the database
     * @param username Username to set for the user
     * @param displayName Display name to set for the user
     * @param email Email for the user
     * @param password Password for the user
     * @param db Connection to the database to insert into
     * @throws SQLException
     */
    public static void createUser(String username, String displayName, String email, String password, Connection db) throws SQLException {
        Statement st = db.createStatement();
        st.executeUpdate("INSERT INTO users (username, displayName, email, password) VALUES (" +
            "'" + username + "', " +
            "'" + displayName + "', " + 
            "'" + email + "', " +
            "'" + password +"');");
    }

    /**
     * Updates fields of user object in database
     * @param db Connection to the database
     * @throws SQLException
     */
    public void updateInDatabase(Connection db) throws SQLException {
        Statement st = db.createStatement();
        st.executeUpdate("UPDATE users SET " + 
                            "username=\"" + this.username + "\"" + 
                            ", password=\"" + this.password + "\"" + 
                            ", displayname=\"" + this.displayName + "\"" + 
                            ", email=\"" + this.email + "\"" + 
                            ", favouriteAuthor=\"" + this.favouriteAuthor + "\"" + 
                            ", favouriteBook=" + this.favouriteBook +
                            ", favouriteGenre=" + this.favouriteGenre + 
                            ", readingGoal=" + this.readingGoal +
                        " WHERE userID=" + this.userID); 
    }

    /**
     * @return Unique ID for the user in the database
     */
    public int getUserID() {
        return userID;
    }

    /**
     * @return Unique username for the user object (originally equal to database)
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username for the user object which is not reflected in database
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return Display name of the user object (originally equal to database)
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name for the user object which is not reflected in database
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return Email of the user object (originally equal to database)
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email for the user object which is not reflected in database
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return A list of collection IDs that the user owns
     */
    public List<Integer> getUserCollections() {
        return userCollections;
    }

    /**
     * @return A list of collection IDs that the user follows
     */
    public List<Integer> getFollowedCollections() {
        return followedCollections;
    }

    /**
     * @return A list of review IDs that the user published
     */
    public List<Integer> getReviews() {
        return reviews;
    }

    /**
     * @return A list of user IDs that the user follows
     */
    public List<Integer> getFollowers() {
        return followers;
    }

    /**
     * @return A list of user IDs that the user is followed by
     */
    public List<Integer> getFollowing() {
        return following;
    }

    /**
     * @return A list of notification IDs that belong to the user
     */
    public List<Integer> getNotifications() {
        return notifications;
    }

    /**
     * @return A list of notification IDs that are unread and belong to the user
     */
    public List<Integer> getUnreadNotifications() {
        return unreadNotifications;
    }

    /**
     * @return A list of user IDs that are deemed to be similar to the user
     */
    public List<Integer> getSimilarUsers() {
        return similarUsers;
    }

    /**
     * @return A list of book IDs that the system recommend to the user
     */
    public List<Integer> getBookRecs() {
        return bookRecs;
    }

    /**
     * @return A list of book IDs that the user has indicated they want to read
     */
    public List<Integer> getReadWishlist() {
        return readWishlist;
    }

    /**
     * @return A list of book IDs that the user has read
     */
    public List<Integer> getReadBooks() {
        return readBooks;
    }

    /**
     * @return A list of book IDs that the user has indicated are their "favourite" books
     */
    public List<Integer> getFavouriteBooks() {
        return favouriteBooks;
    }

    /**
     * @return Monthly reading goal for the user object  (originally equal to database)
     */
    public Integer getReadingGoal() {
        return readingGoal;
    }

    /**
     * Sets the monthly reading goal for the user object which is not reflected in database
     * @param readingGoal
     */
    public void setReadingGoal(Integer readingGoal) {
        this.readingGoal = readingGoal;
    }

    /**
     * @return The number of books the user has read during the current month
     */
    public Integer getBooksReadMonth() {
        return booksReadMonth;
    }

    /**
     * @return Favourite author for the user object  (originally equal to database)
     */
    public String getFavouriteAuthor() {
        return favouriteAuthor;
    }

    /**
     * Sets the favourite author for the user object which is not reflected in the database
     * @param favouriteAuthor
     */
    public void setFavouriteAuthor(String favouriteAuthor) {
        this.favouriteAuthor = favouriteAuthor;
    }

    /**
     * @return Favourite book for the user object  (originally equal to database)
     */
    public Integer getFavouriteBook() {
        return favouriteBook;
    }

    /**
     * Sets the favourite book for the user object which is not reflected in the database
     * @param favouriteBook
     */
    public void setFavouriteBook(Integer favouriteBook) {
        this.favouriteBook = favouriteBook;
    }

    /**
     * @return Favourite genre for the user object  (originally equal to database)
     */
    public Integer getFavouriteGenre() {
        return favouriteGenre;
    }

    /**
     * Sets the favourite genre for the user object which is not reflected in the database
     * @param favouriteGenre
     */
    public void setFavouriteGenre(Integer favouriteGenre) {
        this.favouriteGenre = favouriteGenre;
    }

    /**
     * @return Number of pages the user has read
     */
    public Integer getPagesRead() {
        return pagesRead;
    }

    /**
     * @return Number of books the user has read
     */
    public Integer getBooksRead() {
        return booksRead;
    }

    /**
     * @return Password for the user object (originally equal to database)
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the password for the user object which is not reflected in database
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}