package bookedout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import bookedout.routes.BookRoutes;
import bookedout.routes.CollectionRoutes;
import bookedout.routes.ReviewRoutes;
import bookedout.routes.SearchRoutes;
import bookedout.routes.UserRoutes;

import static spark.Spark.*;

public class BookedOut {

    private static Connection db;

    public static void main(String[] args) {
        // Attempt to initialise connection to database for the server
        try {
            Class.forName("com.mysql.jdbc.Driver");
            db = DriverManager.getConnection("jdbc:mysql://remotemysql.com:3306/YnPBqfNNvt?user=YnPBqfNNvt&password=Uf3gmlHMsv&autoReconnect=true");
            System.out.println("Connected");
        } catch (SQLException ex) {
            // Connection exception
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            System.exit(-1);
        } catch (Exception ex) {
            // General exception (likely a driver issue)
            System.out.println("Exception: " + ex.getMessage());
            System.exit(-1);
        }

        // Enable CORS for the frontend: https://gist.github.com/saeidzebardast/e375b7d17be3e0f4dddf
        options("/*",
        (request, response) -> {

            String accessControlRequestHeaders = request
                    .headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers",
                        accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request
                    .headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods",
                        accessControlRequestMethod);
            }

            return "OK";
        });
        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        // REST Endpoints
        // Simple test of saying Hi to a user, getting user by username and greeting by displayname
        get("/hello/:username", (req, res) -> UserRoutes.helloUser(req, res, db));
        // User Routes
        get("/user/exists", (req, res) -> UserRoutes.userExists(req, res, db));
        get("/user/authenticate", (req, res) -> UserRoutes.authenticateUser(req, res, db));
        get("/user/:username", (req, res) -> UserRoutes.userProfile(req, res, db));
        get("/user/:username/publicCollections", (req, res) -> UserRoutes.publicCollections(req, res, db));
        post("/user/new", (req, res) -> UserRoutes.createUser(req, res, db));
        post("/user/login", (req, res) -> UserRoutes.loginUser(req, res, db));
        patch("/user/profile", (req, res) -> UserRoutes.updateUserProfile(req, res, db));
        delete("/user/logout/:token", (req, res) -> UserRoutes.logoutUser(req, res, db));
        delete("/user/:username/:token", (req, res) -> UserRoutes.deleteUser(req, res, db));
        // Review Routes
        get("/review/:id", (req, res) -> ReviewRoutes.getReview(req, res, db));
        post("/review/create", (req, res) -> ReviewRoutes.createReview(req, res, db));
        patch("/review/:id", (req, res) -> ReviewRoutes.updateReview(req, res, db));
        delete("/review/:id", (req, res) -> ReviewRoutes.deleteReview(req, res, db)); 
        // Search methods
        get("/search/:subject", (req, res) -> SearchRoutes.search(req, res, db));
        // Book Routes
        get("/book/:id", (req, res) -> BookRoutes.getBookInfo(req, res, db));
        // Collection methods
        get("/collections/:collectionName", (req, res) -> CollectionRoutes.viewCollection(req, res, db));
        post("/collections/:collectionName/create", (req, res) -> CollectionRoutes.createNamedCollection(req, res, db));
        post("/collections/:collectionName/add", (req, res) -> CollectionRoutes.addBooktoCollection(req, res, db));
        post("/collections/:collectionName/remove", (req, res) -> CollectionRoutes.removeBookfromCollection(req, res, db));
        patch("/collections/:collectionName/setVisibility", (req, res) -> CollectionRoutes.setCollectionVisibility(req, res, db));
        get("/collections/:collectionName/list", (req, res) -> CollectionRoutes.listOwnCollections(req, res, db));

        // Recommender
        get("/recommend", (req, res) -> Recommender.getRecommendations(req, res, db));
    }
}
