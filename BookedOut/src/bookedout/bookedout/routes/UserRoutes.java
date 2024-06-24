package bookedout.routes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import bookedout.Book;
import bookedout.GenreID;
import bookedout.User;
import bookedout.Collection;
import spark.Request;
import spark.Response;

public class UserRoutes {
// ----- HELPERS -----
    
    /**
     * Creates a new login token in the database for a user and returns it
     * @param userID ID of the user to make a login for
     * @return String representing the created token on success
     * @throws SQLException when the statement fails to execute
     */
    private static String createLogin(int userID, Connection db) throws SQLException {
        String token = Integer.toString(userID) + "," + LocalDateTime.now().toString();
        Statement st = db.createStatement();
        st.execute("INSERT INTO logins (token, userID) VALUES ('" + token + "', " + Integer.toString(userID) + ");");
        return token;
    }

    /**
     * Creates a new login token in the database for a user and returns it
     * @param token token to delete as a login
     * @return String representing the created token on success
     * @throws SQLException when the statement fails to execute
     */
    private static void removeLogin(String token, Connection db) throws SQLException {
        Statement st = db.createStatement();
        st.execute("DELETE FROM logins WHERE token=\"" + token + "\";");
    }

    /**
     * Checks if a token already exists in the database
     * @param token token to check for existence of
     * @return Whether or not the token exists
     */
    public static Boolean tokenExists(String token, Connection db) {
        try {
            Statement st = db.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(token) AS total FROM logins WHERE token=\"" + token + "\";");
            rs.next();
            return (rs.getInt(1) > 0);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Gets the user ID from the user token
     * @param token token to parse
     * @return User ID contained in token
     */
    public static int userIDFromToken(String token) {
        return Integer.parseInt(token.split(",")[0]);
    }

// -----  ROUTES -----

    /**
     * Sample function: Says hi to a specified user
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return String that greets a user by display name
     */
    public static String helloUser(Request req, Response res, Connection db) {
        User user = User.generateUserByName(req.params(":username"), db);
        if (user == null) {
            res.status(404);
            return "";
        } else {
            return "Hello " + user.getDisplayName() +"!";
        }
        
    }

    /**
     * Function to check if a user exists
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return JSON String with message, boolean if it exists, and a string saying which fields are in use 
     */
    public static String userExists(Request req, Response res, Connection db) {
        try {
            Statement st = db.createStatement();
            String email = req.queryParams("email");
            ResultSet counts;
            counts = st.executeQuery("SELECT COUNT(CASE WHEN email='" + email + "' THEN 1 ELSE NULL END) AS total FROM users;");
            counts.next();
            Boolean emailExists = counts.getInt(1) > 0;
            String username = req.queryParams("username");
            counts = st.executeQuery("SELECT COUNT(CASE WHEN username='" + username + "' THEN 1 ELSE NULL END) AS total FROM users;");
            counts.next();
            Boolean usernameExists = counts.getInt(1) > 0;
            res.status(200);
            return "{\"msg\":\"Success\"," +
                    "\"exists\":" + Boolean.toString(emailExists || usernameExists) + "," +
                    "\"emailInUse\":" + Boolean.toString(emailExists) + "," +
                    "\"usernameInUse\":" + Boolean.toString(usernameExists) +
                    "}";
        } catch (SQLException ex) {
            res.status(500);
            System.out.println(ex.toString());
            return "{\"msg\": \"Error connection to database\"}";
        }
    }

    /**
     * Function to create a new user
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return JSON String with message and, on success, a login token
     */
    public static String createUser(Request req, Response res, Connection db) {
        // Convert req params to a JSON object
        JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
        try {
            // Create the user
            User.createUser(json.get("username").getAsString(), 
                            json.get("displayname").getAsString(), 
                            json.get("email").getAsString(),
                            json.get("hashedPassword").getAsString(), db);
        } catch (SQLException e) {
            // Exception caused by UNIQUE key constraint
            res.status(400);
            return "{\"msg\":\"Email or username already in use\"}";
        }
        // Get the user object to fetch the ID of the created user
        User created = User.generateUserByName(json.get("username").getAsString(), db);
        // Use the generated user's ID to initialise their main collection
        int returnCode = Collection.createCollectionObject(db, "Main", created.getUserID(), 1);
        if (returnCode != 1) {
            res.status(500);
            return "{\"msg\":\"Account created, Error creating Main collection\"}";
        }
        try {
            // Use the generated user's ID to create a login
            String token = createLogin(created.getUserID(), db);
            res.status(200);
            res.cookie("token", token);
            return "{\"token\":\"" + token + "\", \"msg\":\"Account created and logged in successfully\"}";
        } catch (SQLException ex) {
            res.status(500);
            return "{\"msg\":\"Account created, error creating login\"}";
        }
    }

    /**
     * Function to login a user
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return JSON String with message and, on success, a login token
     */
    public static String loginUser(Request req, Response res, Connection db) {
        if (tokenExists(req.cookie("token"), db)) {
            // A valid login token exists in the browsers cookies
            res.status(400);
            return "{\"msg\":\"Browser already logged in\"}";
        }
        // Find matching user
        JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
        User user = User.generateUserByName(json.get("username").getAsString(), db);
        if (user == null) {
            // Could not find user with name
            res.status(400);
            return "{\"msg\":\"Username or password does not match.\"}";
        }
        // Check passwords are equivalent
        if (json.get("hashedPassword").getAsString().equals(user.getPassword())) {
            // Passwords match, attempt to create a login for the user
            try {
                String token = createLogin(user.getUserID(), db);
                res.status(200);
                res.cookie("token", token);
                return "{\"msg\":\"Logged in successfully\",\"token\":\"" + token + "\"}";
            } catch (SQLException ex) {
                res.status(500);
                return "{\"msg\":\"Could not login at this time\"}";
            }
        } else {
            // Password is incorrect
            res.status(400);
            return "{\"msg\":\"Username or password does not match.\"}";
        }
    }

    /**
     * Function to logout a user
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return JSON String with message and, on success, clears session login token
     */
    public static String logoutUser(Request req, Response res, Connection db) {
        String token = req.params("token");
        if (!tokenExists(token, db)) {
            res.status(400);
            return "{\"msg\":\"Invalid login\"}";
        }
        try {
            removeLogin(token, db);
            res.removeCookie("token");
            res.status(200);
            return "{\"msg\":\"Successfully logged out\"}";
        } catch (SQLException ex) {
            return "{\"msg\":\"Error logging out\"}";
        }
    }

    /**
     * Deletes a given user from the database, along with their individual data
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return Success/error message
     */
    public static String deleteUser(Request req, Response res, Connection db) {
        // TODO: Verify strings and statements
        String token = req.params(":token");
        if (!tokenExists(token, db)) {
            res.status(400);
            return "{\"msg\":\"Invalid login\"}";
        }
        User user = User.generateUserByID(userIDFromToken(token), db);
        if (!user.getUsername().equals(req.params(":username"))) {
            res.status(400);
            return "{\"msg\":\"Cannot delete another user's account\"}";
        }
        String tarUserID = Integer.toString(User.generateUserByName(req.params(":username"), db).getUserID());
        try {
            Statement st = db.createStatement();

            // Delete collections and related data
            ResultSet rs = st.executeQuery("SELECT collectionID FROM collections WHERE (ownerID=" + tarUserID + ");");
            String collectionOrStatement = "";
            // Build a string to represent any collection the user made
            while (rs.next()) {
                collectionOrStatement = collectionOrStatement + " OR collectionID=" + rs.getString(1);
            }
            // Delete lists of books associated with each collection owned
            if (!collectionOrStatement.equals("")) st.execute("DELETE FROM collectionBooks WHERE (" + collectionOrStatement.substring(4) + ");");
            // Delete followings/followers of collections associated to user
            st.execute("DELETE * FROM collectionFollowings WHERE (userID=" + tarUserID + collectionOrStatement + ");");
            // Delete collections
            if (!collectionOrStatement.equals("")) st.execute("DELETE * FROM collections WHERE (ownerID=" + tarUserID + ");");

            // Delete followers, following, similar users
            st.execute("DELETE FROM similarUsers WHERE (baseUserID=" + tarUserID + " OR similarUserID=" + tarUserID + ");");
            st.execute("DELETE FROM followers WHERE (followerID=" + tarUserID + " OR followedID=" + tarUserID + ");");
        
            // Delete bookstate entries
            st.execute("DELETE FROM bookStates WHERE userID=" + tarUserID);

            // Delete notifications
            st.execute("DELETE FROM notifications WHERE (recipientID=" + tarUserID + " OR notifierID=" + tarUserID + ");");

            // Delete logins and user
            st.execute("DELETE FROM logins WHERE userID=" + tarUserID + ";");
            st.execute("DELETE FROM users WHERE userID=" + tarUserID + ";");
        } catch (SQLException ex) {
            res.status(500);
            return "{\"msg\":\"Unknown SQL Exception\"";
        }
        res.removeCookie("token");
        return "{\"msg\":\"Account successfully deleted\"}";
    }

    /**
     * Used to verify if a user has correctly entered their password to access certain features
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return Success/error message, boolean for result
     */
    public static String authenticateUser(Request req, Response res, Connection db) {
        String token = req.queryParams("token");
        // Check the browser is logged in/token sent
        if (!tokenExists(token, db)) {
            res.status(400);
            return "{\"msg\":\"Invalid login\"}";
        }
        // Get user details from database
        User user = User.generateUserByID(userIDFromToken(token), db);
        // Safety check
        if (user == null) {
            res.status(500);
            return "{\"msg\":\"Could not find user\"}";
        }
        // Authenticate user via password match
        if (req.queryParams("hashedPassword").equals(user.getPassword())) {
            return "{\"msg\":\"Succesfully authenticed\", \"auth\"=true}";
        } else {
            return "{\"msg\":\"Succesfully authenticed\", \"auth\"=false}";
        }
    }

    /**
     * Updates portions of a user's profile
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return Success/error message,
     */
    public static String updateUserProfile(Request req, Response res, Connection db) {
        JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
        String token = json.get("token").getAsString();
        // Check the browser is logged in/token sent
        if (!tokenExists(token, db)) {
            res.status(400);
            return "{\"msg\":\"Invalid login\"}";
        }
        // Get user details from database
        User user = User.generateUserByID(userIDFromToken(token), db);
        // Safety check
        if (user == null) {
            res.status(500);
            return "{\"msg\":\"Could not find user\"}";
        }
        // Change fields
        if (json.get("username") != null) {
            user.setUsername(json.get("username").getAsString());
        }
        if (json.get("password") != null) {
            user.setPassword(json.get("password").getAsString());
        }
        if (json.get("displayName") != null) {
            user.setDisplayName(json.get("displayName").getAsString());
        }
        if (json.get("email") != null) {
            user.setEmail(json.get("email").getAsString());
        }
        if (json.get("author") != null) {
            user.setFavouriteAuthor(json.get("author").getAsString());
        }
        if (json.get("book") != null) {
            user.setFavouriteBook(json.get("book").getAsInt());
        }
        if (json.get("genre") != null) {
            user.setFavouriteGenre(json.get("genre").getAsInt());
        }
        if (json.get("goal") != null) {
            user.setReadingGoal(json.get("goal").getAsInt());
        }
        // Update details in database
        try {
            user.updateInDatabase(db);
            return "{\"msg\":\"Successfully updated user\"}";
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            res.status(500);
            return "{\"msg\":\"Error updating database - email or username already in use\"}";
        }
    }

    /**
     * Gets data about a user's profile
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return JSON Representation of the user
     */
    public static String userProfile(Request req, Response res, Connection db) {
        String username = req.params("username");
        User user = User.generateUserByName(username, db);
        if (user == null) {
            res.status(404);
            return "{\"msg\":\"Could not find user with that name\"}";
        }
        
        int followers, following;
        try {
            Statement st = db.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(followedId) AS total FROM followers WHERE followedId=" + Integer.toString(user.getUserID()) + ";");
            rs.next();
            followers = rs.getInt(1);
            rs = st.executeQuery("SELECT COUNT(followerId) AS total FROM followers WHERE followerId=" + Integer.toString(user.getUserID()) + ";");
            rs.next();
            following = rs.getInt(1);
        } catch (SQLException ex) {
            followers = following = 0;
        }

        try {
            String payload = "{\"msg\":\"Success\"," +
                          "\"userID\":" + Integer.toString(user.getUserID()) + "," +
                          "\"username\":\"" + user.getUsername() + "\"," +
                          "\"displayName\":\"" + user.getDisplayName() + "\"," +
                          "\"followers\":" + Integer.toString(followers) + "," +
                          "\"following\":" + Integer.toString(following) + "," +
                          "\"readingGoal\":" + Integer.toString(user.getReadingGoal()) + "," +
                          "\"booksReadMonth\":" + Integer.toString(user.getBooksReadMonth()) + "," +
                          "\"favouriteAuthor\":\"" + user.getFavouriteAuthor() + "\",";
            Book b = Book.generateBookByID(user.getFavouriteBook(), db);
            if (b != null) {
                    payload = payload + "\"favouriteBook\":" + b.toJSONObject() + ",";
            } else {
                payload = payload + "\"favouriteBook\":{},";
            }
            payload = payload + "\"favouriteGenre\":\"" + GenreID.getType(user.getFavouriteGenre()) + "\"," +
                          "\"pagesRead\":" + Integer.toString(user.getPagesRead()) + "," +
                          "\"booksRead\":" + Integer.toString(user.getBooksRead());
            if (user.getUsername().equals(req.params(":username"))) {
                payload = payload + ",\"email\":\"" + user.getEmail() + "\""; 
            }
            payload = payload + "}";
            return payload;
        } catch (Exception ex) {
            res.status(500);
            return "{\"msg\":\"Fatal SQL Exception Occured.\"}";
        }
    }

    /**
     * Gets list of public collections owned by user
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return JSON Array of collections
     */
    public static String publicCollections(Request req, Response res, Connection db) {
        String username = req.params("username");
        User user = User.generateUserByName(username, db);
        if (user == null) {
            res.status(404);
            return "{\"msg\":\"No such user\"}";
        }
        try {
            ResultSet rs = db.createStatement().executeQuery("SELECT collectionID FROM collections WHERE ownerID=" + user.getUserID() + " AND isPublic=1;");
            String array = "[";
            while(rs.next()) {
                array = array + Integer.toString(rs.getInt(1)) + ",";
            }
            array = array.substring(0, array.length() - 1);
            array = (array.equals(""))? "[]": array + "]";
            return "{\"msg\":\"Success\",\"collections\":" + array + "}";
        } catch (SQLException ex) {
            res.status(500);
            return "{\"msg\":\"Unknown SQL Error\"}";
        }
    }
    
}
