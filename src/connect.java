import org.mindrot.jbcrypt.BCrypt;
import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;

public class connect {
    private static final String URL = "jdbc:mysql://localhost:3306/RecipeManagement";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    // Method to execute a simple query and return the results
    public static ArrayList<String[]> executeQuery(String query) {
        ArrayList<String[]> results = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            // Get the metadata of the result set
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Add rows to the results list
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getString(i + 1);
                }
                results.add(row);
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
        return results;
    }

    // Method for user login
    public static User login(String email, String password) {
        User user = null;
        String query = "SELECT user_id, name, password FROM Users WHERE email = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        user = new User(rs.getString("user_id"), rs.getString("name"));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
        return user;
    }

    // Method for user registration
    public static boolean register(String name, String email, String password) {
        // Hash the password before storing it
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String query = "INSERT INTO Users (name, email, password) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, hashedPassword);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            return false;
        }
    }

    // Method to add the recipe to the Recipes table along with its tag associations
    public static void addRecipeToDatabase(String recipeName, String ingredients, String instructions, ArrayList<String> validTags, int user_id) {
        String query = "INSERT INTO Recipes (name, ingredients, instructions, user_id) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, recipeName);
            pstmt.setString(2, ingredients);
            pstmt.setString(3, instructions);
            pstmt.setInt(4, user_id);  // For now, using a static user_id (modify for actual logged-in user)
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int recipeId = generatedKeys.getInt(1);
                        // For each valid tag, add an entry in the Recipe_Tags junction table
                        for (String tag : validTags) {
                            addRecipeTag(recipeId, tag);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error (addRecipeToDatabase): " + e.getMessage());
        }
    }

    // Method to add an association between a recipe and a tag into the Recipe_Tags table
    public static void addRecipeTag(int recipeId, String tag) {
        String query = "INSERT INTO Recipe_Tags (recipe_id, tag_id) SELECT ?, tag_id FROM Tags WHERE tag_name = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, recipeId);
            pstmt.setString(2, tag);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Error (addRecipeTag): " + e.getMessage());
        }
    }

}

// User class to represent logged-in user
class User {
    private String user_id;
    private String name;

    public User(String user_id, String name) {
        this.user_id = user_id;
        this.name = name;
    }

    public String getUserId() {
        return user_id;
    }

    public String getName() {
        return name;
    }
}
