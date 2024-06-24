package bookedout.routes;

import java.sql.Connection;
import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import bookedout.Review;
import spark.Request;
import spark.Response;

public class ReviewRoutes {

// ----- HELPERS -----

// -----  ROUTES -----
    /**
     * Gets an individual review from the database given by the id in the query route
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return Success/Error message, JSON Object representing review
     */
    public static String getReview(Request req, Response res, Connection db) {
        Integer id = Integer.parseInt(req.params("id"));
        try {
            Review r = Review.generateReviewByID(id, db);
            if (r == null) {
                res.status(404);
                return "{\"msg\":\"Could not find review with given ID\"}";
            }
            return "{\"msg\":\"Success\", \"review\":" + r.toJSONObject() + "}";
        } catch (SQLException ex) {
            res.status(500);
            return "{\"msg\":\"Unknown SQL Exception occurred.\"}";
        }
    }

    /**
     * Creates a review for a book by a user. User may only have one review per book.
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return Success/error message
     */
    public static String createReview(Request req, Response res, Connection db) {
        JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
        String token = json.get("token").getAsString();
        if (!UserRoutes.tokenExists(token, db)) {
            res.status(400);
            return "{\"msg\":\"Invalid login.\"}";
        }
        int userID = UserRoutes.userIDFromToken(token);
        int bookID = json.get("bookID").getAsInt();
        if (Review.reviewExists(userID, bookID, db)) {
            res.status(400);
            return "{\"msg\":\"Review already exists for this book by this user\"}";
        }
        int rating = json.get("rating").getAsInt();
        boolean textReview = !(json.get("title") == null || json.get("body") == null);
        String title = "", body = "";
        if (textReview) {
            title = json.get("title").getAsString();
            body = json.get("body").getAsString();
        }
        try {
            Review.createReview(bookID, userID, rating, textReview, title, body, db);
            res.status(200);
            return "{\"msg\":\"Success\"}";
        } catch (SQLException ex) {
            res.status(500);
            return "{\"msg\":\"Fatal SQL Exception occured\"}";
        }
    }

    /**
     * Deletes a review by ID if the one requesting to delete is the same user that created the review
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return Success/error message
     */
    public static String deleteReview(Request req, Response res, Connection db) {
        String token = req.queryParams("token");
        int reviewID = Integer.parseInt(req.params(":id"));
        if (!UserRoutes.tokenExists(token, db)) {
            res.status(400);
            return "{\"msg\":\"Invalid login.\"}";
        }
        
        try {
            Review r = Review.generateReviewByID(reviewID, db);
            if (r.getReviewerID() != UserRoutes.userIDFromToken(token)) {
                res.status(400);
                return "{\"msg\":\"You do not have permission to delete this review.\"}";
            }
            Review.deleteReview(reviewID, db);
            return "{\"msg\":\"Success.\"}";
        } catch (SQLException ex) {
            res.status(500);
            return "{\"msg\":\"Fatal SQL Exception.\"}";
        } catch (NullPointerException ex) {
            res.status(404);
            return "{\"msg\":\"Could not find specified review.\"}";
        }
    }

    /**
     * Updates a review by ID if the one requesting to update is the same user that created the review
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return Success/error message
     */
    public static String updateReview(Request req, Response res, Connection db) {
        JsonObject json = JsonParser.parseString(req.body()).getAsJsonObject();
        String token = json.get("token").getAsString();
        int reviewID = Integer.parseInt(req.params(":id"));
        if (!UserRoutes.tokenExists(token, db)) {
            res.status(400);
            return "{\"msg\":\"Invalid login.\"}";
        }
        
        try {
            Review r = Review.generateReviewByID(reviewID, db);
            if (r.getReviewerID() != UserRoutes.userIDFromToken(token)) {
                res.status(400);
                return "{\"msg\":\"You do not have permission to modify this review.\"}";
            }
            int rating = json.get("rating").getAsInt();
            boolean textReview = !(json.get("title") == null || json.get("body") == null);
            String title = "", body = "";
            if (textReview) {
                title = json.get("title").getAsString();
                body = json.get("body").getAsString();
            }
            
            Review.updateReview(r.getBookID(), reviewID, rating, r.getRating(), textReview, title, body, db);
            return "{\"msg\":\"Success.\"}";
        } catch (SQLException ex) {
            res.status(500);
            return "{\"msg\":\"Fatal SQL Exception.\"}";
        } catch (NullPointerException ex) {
            res.status(404);
            return "{\"msg\":\"Could not find specified review.\"}";
        }
    }
    
}
