package bookedout.routes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import bookedout.Collection;
import spark.Request;
import spark.Response;

public class CollectionRoutes {


    static final int TRUE = 1;
    static final int FALSE = 0;

    /**
     * Request to view specified collection.
     * If requester is not owner, specified collection must be public. 
     * @param req userID, collectionID -> /route + ?uid={}&cid={}
     * @param res
     * @return collection information
     */
    public static String viewCollection(Request req, Response res, Connection db) {
        int userID = Integer.parseInt(req.queryParams("uid"));
        int collectionID = Integer.parseInt(req.queryParams("cid"));

        Collection result = Collection.viewCollection(db, userID, collectionID);
        // System.out.println(result);
        if (result == null) {
            res.status(400);
            return "{\"msg\":\"Invalid permissions or collection\"}";
        } else {
            // return json object containing collection information
            GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting();
            Gson gson = builder.create();
            String payload = gson.toJson(result);

            res.status(200);
            return payload;
        }
    }

    /**
     * Create named collection
     * @param req collectionName, userID
     * @param res
     * @return status message
     */
    public static String createNamedCollection(Request req, Response res, Connection db) {
        JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
        String collectionName = json.get("cName").getAsString();
        int userID = json.get("userID").getAsInt();

        int returnCode = Collection.createCollectionObject(db, collectionName, userID, FALSE);

        if (returnCode == -1) {
            res.status(400);
            return "{\"msg\":\"Named Collection already exists\"}";
        } else if (returnCode == -2) {
            res.status(500);
            return "{\"msg\":\"Error creating Named Collection\"}";
        } else {
            res.status(200);
            return "{\"msg\":\"Successfully created named collection\"}";
        }
    }

    /**
     * Add book to collection.
     * @param req userID, collectionID, bookID
     * @param res
     * @return status message
     */
    public static String addBooktoCollection(Request req, Response res, Connection db) {
        JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
        int userID = json.get("userID").getAsInt();
        int collectionID = json.get("collectionID").getAsInt();
        int bookID = json.get("bookID").getAsInt();
        LocalDateTime dateAdded = LocalDateTime.now();

        int returnCode = Collection.addBook(db, userID, collectionID, bookID, dateAdded);

        if (returnCode == -1) {
            res.status(400);
            return "{\"msg\":\"Invalid permissions, collection, or book\"}";
        } else if (returnCode == -2) {
            res.status(500);
            return "{\"msg\":\"Error adding book to collection\"}";
        } else {
            res.status(200);
            return "{\"msg\":\"Successfully added book to collection\"}";
        }
    }

    /**
     * Remove book from collection. 
     * @param req userID, collectionID, bookID
     * @param res
     * @return status message
     */
    public static String removeBookfromCollection(Request req, Response res, Connection db) {
        JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
        int userID = json.get("userID").getAsInt();
        int collectionID = json.get("collectionID").getAsInt();
        int bookID = json.get("bookID").getAsInt();

        int returnCode = Collection.removeBook(db, userID, collectionID, bookID);

        if (returnCode == -1) {
            res.status(400);
            return "{\"msg\":\"Invalid permissions, collection, or book\"}";
        } else if (returnCode == -2) {
            res.status(500);
            return "{\"msg\":\"Error removing book to collection\"}";
        } else {
            res.status(200);
            return "{\"msg\":\"Successfully removed book from collection\"}";
        }
    }

    /**
     * Set collection visibility
     * @param req userID, collectionID, isPublic
     * @param res
     * @return status message
     */
    public static String setCollectionVisibility(Request req, Response res, Connection db) {
        JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
        int userID = json.get("userID").getAsInt();
        int collectionID = json.get("collectionID").getAsInt();
        int isPublic = json.get("isPublic").getAsInt();

        int returnCode = Collection.setVisibility(db, userID, collectionID, isPublic);
        if (returnCode == -1) {
            res.status(400);
            return "{\"msg\":\"Invalid permissions or option\"}";
        } else if (returnCode == -2) {
            res.status(500);
            return "{\"msg\":\"Error updating collection visibility\"}";
        } else {
            res.status(200);
            return "{\"msg\":\"Successfully updated collection visibility\"}";
        }
    }

    /**
     * Get a list of the user's personal collections
     * @param req userID
     * @param res
     * @param db
     * @return status message
     */    
    public static String listOwnCollections(Request req, Response res, Connection db) {
        // TODO: confirm that the user is actually the current session user
        System.out.println("Retrieving collections...");
        int userID = Integer.parseInt(req.queryParams("uid"));
        try {
            Statement st = db.createStatement();
            String query = "SELECT collectionID, name, isPublic FROM collections where ownerID=" + userID + ";";
            ResultSet rs = st.executeQuery(query);

            // Create list to return (collectionID, collectionName) for all collections
            ArrayList<Map<String, Object>> collectionsData = new ArrayList<Map<String, Object>>();
            while(rs.next()) {
                Map<String, Object> col = new HashMap<>();
                col.put("id", rs.getInt("collectionID"));
                col.put("isPublic", Integer.valueOf(rs.getInt("isPublic")));
                col.put("name", rs.getString("name"));
                collectionsData.add(col);
            }
            // return json object containing collection information
            GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting();
            Gson gson = builder.create();
            String payload = gson.toJson(collectionsData);

            res.status(200);
            return payload;
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            res.status(500);
            return "{\"msg\":\"Error connecting to database\"}";
        } catch (Exception ex) {
            System.out.println("SQLException: " + ex.getMessage());
            res.status(500);
            return "{\"msg\":\"Something went wrong here...\"}";
        }
    }
}
