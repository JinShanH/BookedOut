package bookedout;

import java.util.ArrayList;
import java.util.Date;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Collection Object
 * @author T10A-BookedOut
 */

public class Collection {
    static final int TRUE = 1;
    static final int FALSE = 0;


    public int collectionID;   // starting from 0
    private String name;
    private int ownerID;
    private ArrayList<Integer> books;
    // private ArrayList<Integer> genres;
    private String description;
    private LocalDateTime created;
    private LocalDateTime lastUpdated;
    private int ratings;
    private float avgRatings;
    private int isPublic;
    private int isMain;
    

    public Collection() {
        // this.genres = new ArrayList<Integer>();
        this.books = new ArrayList<Integer>();
    }

    /**
     * Create collection object. Insert new entry into database
     * @param db
     * @param name
     * @param userID
     * @param isMain
     * @return 1 on sucess, -1 on database insert failure, -2 otherwise. 
     */
    public static int createCollectionObject(Connection db, String name, int userID, int isMain) {
        try {
            Collection c = new Collection();
            c.name = name;
            c.ownerID = userID;
            c.description = "";
            c.created = LocalDateTime.now();
            c.lastUpdated = c.created;
            c.isPublic = 0;
            c.isMain = isMain;

            // System.out.println(c.collectionID);
            // System.out.println(c.ownerID);
            // System.out.println();

            Statement st = db.createStatement();

            // If not creating Main collection for new user, check if collection name already exists for specified user
            if (isMain == FALSE) {
                String sql = "SELECT name FROM collections where ownerID = " + userID + ";";
                ResultSet rs = st.executeQuery(sql);
                while (rs.next()) {
                    String entry = rs.getString("name");
                    if (name.equals(entry)) {
                        System.out.println("Named collection: '" + name + "' already exists for " + userID);
                        return -1;
                    }
                }
                rs.close();
            }
            

            // Collection name does not exist for specified user. Insert new entry. 
            st.executeUpdate("INSERT INTO collections (name, ownerID, description, created, lastUpdated, isPublic, isMain) VALUES (" +
                "'" + c.getName() + "', " + 
                "'" + c.getOwner() + "', " +
                "'" + c.getDescription() + "', " +
                "'" + c.getCreated() + "', " +
                "'" + c.getEdited() + "', " +
                "'" + c.isPublic() + "', " +
                "'" + c.isMain() +"');");
            return 1;
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            System.out.println(e.getMessage());
            return -1;
        } catch (Exception e) {
            return -2;
        }
    }

    /**
     * Retrieve collection object from database
     * @param db
     * @param collectionID
     * @return
     */
    public static Collection getCollection(Connection db, int collectionID) {
        try {
            Collection c = new Collection();
            Statement st = db.createStatement();
            String query = "SELECT * FROM collections WHERE collectionID=" + collectionID + ";";
            System.out.println("query: " + query);
            System.out.println("getCollection: Executing Query");
            ResultSet rs = st.executeQuery(query);
            System.out.println("getCollection: Query successful");
            rs.next();
            c.collectionID = rs.getInt(1);
            c.name = rs.getString(2);
            c.ownerID = rs.getInt(3);
            c.description = rs.getString(4);
            System.out.println(c.ownerID);
            System.out.println(c.collectionID);
            
            
            // Convert date object to localdatetime
            Date created = rs.getDate(5);
            Timestamp timestamp_created = new Timestamp(created.getTime());
            c.created = timestamp_created.toLocalDateTime();

            Date lastUpdated = rs.getDate(6);
            Timestamp timestamp_lastUpdated = new Timestamp(lastUpdated.getTime());
            c.lastUpdated = timestamp_lastUpdated.toLocalDateTime();

            c.ratings = rs.getInt(7);
            c.avgRatings = rs.getFloat(8);
            c.isPublic = rs.getInt(9);
            c.isMain = rs.getInt(10);

            c.books = c.getBooksfromDatabase(db, c.collectionID);
            System.out.println("getCollection: successful");

            return c;
        } catch (Exception e) {
            System.out.println("getCollection: Query Failed");
            return null;
        }
    }

    /**
     * View Collection
     * @param userID
     * @param collectionID
     * @return
     */
    public static Collection viewCollection(Connection db, int userID, int collectionID) {
        Collection c = Collection.getCollection(db, collectionID);
        System.out.println("C.viewCollection: successfully retrieved collection");
        if (c == null) {
            System.out.println("C.viewCollection: Invalid collection");
            return null;
        }
        // Check collection ownership and visibility
        if (userID != c.ownerID) {
            System.out.println("C.viewCollection: userID: " + userID);
            System.out.println("C.viewCollection: ownerID: " + c.ownerID);
            if (c.isPublic() == FALSE) {
                System.out.println("C.viewCollection: Invalid permissions");
                return null;
            }
        }
        return c;
    }

    public int getCollectionID() {
        return this.collectionID;
    }

    public boolean canModify(int userID) {
        return userID == this.ownerID;
    }

    public String getName() {
        return this.name;
    }

    public int getOwner() {
        return this.ownerID;
    }

    public ArrayList<Integer> getBooks() {
        return this.books;
    }

    /**
     * Retrieve collection's books
     * @param db
     * @param collectionID
     * @return list of bookID
     */
    public ArrayList<Integer> getBooksfromDatabase(Connection db, int collectionID) {
        ArrayList<Integer> books = new ArrayList<Integer>();

        try {
            Statement st = db.createStatement();
            String query = "SELECT bookID, dateAdded FROM collectionBooks WHERE collectionID = " + collectionID + " ORDER BY primaryID DESC;" ;
            ResultSet rs = st.executeQuery(query);
            while(rs.next()) {
                books.add(rs.getInt("bookID"));
            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            System.out.println(e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        System.out.println(books);
        return books;
    }

    public ArrayList<String> getBooksTitlesfromDatabase(Connection db, int collectionID) {
        ArrayList<String> titles = new ArrayList<String>();

        try {
            Statement st = db.createStatement();
            // String query = "SELECT title, dateAdded FROM collectionBooks WHERE collectionID = " + collectionID + " ORDER BY primaryID DESC;" ;
            String query = "SELECT b.title FROM collectionBooks cb JOIN books b ON cb.bookID=b.bookID WHERE cb.collectionID = "+ collectionID + ";";
            ResultSet rs = st.executeQuery(query);
            while(rs.next()) {
                titles.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            System.out.println("Get book titles failed: SQL ex");
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            System.out.println(e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Get book titles failed: random ex");
            System.out.println(e.getMessage());
            return null;
        }
        System.out.println(titles);
        return titles;
    }

    // public boolean hasBook(int bookID) {
    //     for (Book book : books) {
    //         if (book.getID() == bookID) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }

    // public ArrayList<Integer> getGenres() {
    //     return this.genres;
    // }

    // public boolean hasGenre(Integer id) {
    //     return this.genres.contains(id);
    // }

    public String getDescription() {
        return this.description;
    }
    
    public LocalDateTime getCreated() {
        return this.created;
    }

    public LocalDateTime getEdited() {
        return this.lastUpdated;
    }

    public int getRatings() {
        return this.ratings;
    }

    public float getAvgRatings() {
        return this.avgRatings;
    }

    public int isPublic() {
        return this.isPublic;
    }

    public int isMain() {
        return this.isMain;
    }


    public static int addBook(Connection db, int userID, int collectionID, int bookID, LocalDateTime dateAdded) {
        Collection c = Collection.getCollection(db, collectionID);
        if (c.equals(null)) {
            System.out.println("Invalid collection");
            return -1;
        }
        // Check collection ownership
        if (userID != c.ownerID) {
            System.out.println("Not owner, cannot add.");
            return -1;
        }

        try {
            Statement st = db.createStatement();

            // Check if book already in collection
            String query1 = "SELECT bookID FROM collectionBooks WHERE collectionID = " + collectionID + ";";
            ResultSet rs = st.executeQuery(query1);
            while (rs.next()) {
                if (rs.getInt(1) == bookID) {
                    System.out.println("Book already in collection");
                    return -1;
                }
            }
            rs.close();

            // Add book to collection
            String query2 = "INSERT INTO collectionBooks (collectionID, bookID, dateAdded) VALUES (" + 
                        "'" + c.collectionID + "', " +
                        "'" + bookID + "', " +
                        "'" + dateAdded +"');";
            st.executeUpdate(query2);
            return 1;
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            System.out.println(e.getMessage());
            return -1;
        } catch (Exception e) {
            return -2;
        }
    }

    public static int removeBook(Connection db, int userID, int collectionID, int bookID) {
        Collection c = Collection.getCollection(db, collectionID);
        if (c.equals(null)) {
            System.out.println("Invalid collection");
            return -1;
        }

        // Check collection ownership
        if (userID != c.ownerID) {
            return -1;
        }

        try {
            Statement st = db.createStatement();

            // Check if book not in collection
            String query1 = "SELECT * FROM collectionBooks WHERE collectionID = " + collectionID + " and bookID = " + bookID + ";";
            ResultSet rs = st.executeQuery(query1);
            if (!rs.next()) {
                System.out.println("Book not in collection");
                return -1;
            }
            rs.close();

            // Remove book from collection
            String query = "DELETE FROM collectionBooks WHERE collectionID = " + collectionID + " and bookID = " + bookID + ";";
            st.executeUpdate(query);
            return 1;
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            System.out.println(e.getMessage());
            return -1;
        } catch (Exception e) {
            return -2;
        }
    }
    

    // Change collection visibility
    public static int setVisibility(Connection db, int userID, int collectionID, int isPublic) {
        Collection c = Collection.getCollection(db, collectionID);
        if (c.equals(null)) {
            System.out.println("Invalid collection");
            return -1;
        }

        // Check collection ownership
        if (userID != c.ownerID) {
            System.out.println("Not owner, cannot add.");
            return -1;
        }
        // Check if status is already set
        if (c.isPublic == isPublic) {
            System.out.println("SetVisibility: no change");
            return -1;
        }

        try {
            Statement st = db.createStatement();
            String query = "UPDATE collections SET isPublic = '" + isPublic + "' WHERE collectionID = " + collectionID;
            st.executeUpdate(query);
            return 1;
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
            System.out.println(e.getMessage());
            return -1;
        } catch (Exception e) {
            System.out.println("?");
            return -2;
        }
    }
}