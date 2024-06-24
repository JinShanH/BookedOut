package bookedout.routes;

import java.sql.Connection;
import java.sql.SQLException;

import bookedout.Book;
import spark.Request;
import spark.Response;


public class BookRoutes {
// ----- HELPERS -----
    
// -----  ROUTES -----
    /**
     * Function to get all info about a book
     * @param req Request sent by frontend
     * @param res Status of the browser as updated by backend
     * @return JSON String with message, and a book object
     */
    public static String getBookInfo(Request req, Response res, Connection db) {
        try {
            Book target = Book.generateBookByID(Integer.parseInt(req.params("id")), db);
            if (target == null) {
                res.status(404);
                return "{\"msg\":\"Could not find book with given ID.\"}";
            }
            return "{\"msg\":\"Success.\",\"book\":" + target.toJSONObject() + "}";
        } catch (SQLException ex) {
            res.status(500);
            return "{\"msg\":\"Fatal SQL Exception occured.\"}";
        }
    }
}
