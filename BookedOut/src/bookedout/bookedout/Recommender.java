package bookedout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.Gson;


// import jnr.ffi.annotations.In;

import spark.Request;
import spark.Response;

public class Recommender {
    /**
     * Retrieve a list of book recommendations for the user
     * @param req uid
     * @param res
     * @param db
     * @return
     */
    public static String getRecommendations(Request req, Response res, Connection db) {
        int userID = Integer.parseInt(req.queryParams("uid"));
        System.out.println("Rec: userID = "+userID);
        
        try {
            Statement st = db.createStatement();
            String query = "SELECT collectionID FROM collections WHERE isMain = 1 AND ownerID=" + userID + ";";
            ResultSet rs = st.executeQuery(query);

            Collection c = new Collection();
            if (!rs.next()) {
                System.out.println("REC: User does not exist!");
                res.status(400);
                return "{\"msg\":\"Invalid user\"}";
            }
            c.collectionID = rs.getInt("collectionID");
            System.out.println("Rec: main collectionID = "+c.collectionID);

            // Retrieve list of books in user's main collection
            ArrayList<String> readbooks = new ArrayList<String>();
            readbooks = c.getBooksTitlesfromDatabase(db, c.collectionID);
            // Retrieve absolute script path to call process
            String script = "Recommender.py";
            String absolutePath = FileSystems.getDefault().getPath(script).normalize().toAbsolutePath().toString();
            ProcessBuilder procBuilder = null;
            if (readbooks.size() > 0) {
                // If collection contains books, pass list to python script as a string
                String readList = readbooks.toString();
                String args = readList.replace("[", "").replace("]", "");
                System.out.println("payload: " + args);
                procBuilder = new ProcessBuilder("python", absolutePath, args);
            } else {
                // otherwise run python script without arguments
                procBuilder = new ProcessBuilder("python", absolutePath);
            }
            Process pyProcess = procBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(pyProcess.getInputStream()));
            BufferedReader errors = new BufferedReader(new InputStreamReader(pyProcess.getErrorStream()));
            
            String lines = null;
            String result = null;
            while((lines = reader.readLine()) != null) {
                System.out.println("rec_lines: " + lines);
                result = lines;
            }
            while((lines = errors.readLine()) != null) {
                System.out.println("error: " + lines);
            }

            // Convert string to arraylist of ISBNs
            result = result.replace("[", "").replace("]", "").replace("'", "").replace(" ", "");
            // result = result.replace("[", "").replace("]", "").replace("'", "");
            ArrayList<String> recISBN = new ArrayList<>(Arrays.asList(result.split(",")));
            System.out.println(recISBN);

            // Convert isbns to list of book objects to send to frontend.
            ArrayList<Book> recommendations = new ArrayList<Book>();
            for (String isbn : recISBN) {
                System.out.println("making book for: " + isbn);
                Book rec = Book.generateBookbyISBN(isbn, db);
                recommendations.add(rec);
                System.out.println("made book for: " + isbn);
            } // Comment out this block if testing only isbn
            System.out.println("number of recs: " + recommendations.size());

            // Convert payload to json format
            // GsonBuilder builder = new GsonBuilder();
            // builder.setPrettyPrinting();
            // Gson gson = builder.create();
            // String payload = gson.toJson(recommendations); // Comment out if testing only isbn
            // String payload = gson.toJson(recISBN);

            Gson gson = new Gson();
            String payload = gson.toJson(recommendations);
            System.out.println(payload);

            res.status(200);
            System.out.println("Rec: success?");
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



    
    public static void main(String[] args) throws IOException {

        ArrayList<String> readBooks = new ArrayList<String>();
        readBooks.add("Airframe");
        String readList = readBooks.toString();
        String payload = readList.replace("[", "").replace("]", "");
        System.out.println("payload: " + payload);
        
        String script = "Recommender.py";
        String absolutePath = FileSystems.getDefault().getPath(script).normalize().toAbsolutePath().toString();        
        // String path = "\\BookedOut\\src\\bookedout\\bookedout\\Recommender.py";

        ProcessBuilder builder = new ProcessBuilder("python", absolutePath, payload);
        Process pyProcess = builder.start();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(pyProcess.getInputStream()));
        BufferedReader errors = new BufferedReader(new InputStreamReader(pyProcess.getErrorStream()));
        String lines = null;
        String result = null;
        while((lines = reader.readLine()) != null) {
            System.out.println("rec_lines: " + lines);
            result = lines;
        }
        while((lines = errors.readLine()) != null) {
            System.out.println("error: " + lines);
        }

        ArrayList<Book> recommendations = new ArrayList<Book>();

        // result = result.replace("[", "").replace("]", "").replace("'", "").replace(" ", "");
        // result = result.replace("[", "").replace("]", "").replace("'", "").replace(" ", "");
        result = result.replace("[", "").replace("]", "").replace("'", "");


        ArrayList<String> recISBN = new ArrayList<>(Arrays.asList(result.split(",")));
        // System.out.println(recISBN);
        
        for (String isbn : recISBN) {
            System.out.println("isbn: " + isbn);
        }
    }
}
