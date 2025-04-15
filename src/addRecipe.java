import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class addRecipe extends JFrame {
    private JTextField recipeNameField;       // Recipe name input field
    private JTextArea ingredientsTextArea;      // Ingredients (as long text)
    private JTextArea instructionsTextArea;     // Instructions (as long text)
    private JTextArea tagsTextArea;             // Tags input field (multiple tags entered together)
    private JButton addPhotosButton;            // Button to add photos (opens a file chooser)
    private JButton addRecipeButton;            // Button to add the recipe to the database
    private JButton recipeButton;               // Placeholder navigation button (open recipe window)
    private JButton aboutButton;                // Placeholder navigation button (open about window)
    private JButton homeButton;                 // Placeholder navigation button (open home window)
    private JLabel recipeLabel;
    private JLabel recipeNameLabel;
    private JLabel iconLabel;
    private JLabel ingredientsLabel;
    private JLabel instructionsLabel;
    private JLabel tagsLabel;
    private JPanel panel;                     // Main panel
    private ArrayList<String> imagePaths;      // List to hold image file paths

    public addRecipe(int user_id) {
        setSize(600, 400);
        setContentPane(panel);
        setVisible(true);

        imagePaths = new ArrayList<>(); // Initialize imagePaths list

        // ActionListener for the "Add Recipe" button
        addRecipeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Retrieve input data from the text areas and field
                String recipeName = recipeNameField.getText();
                String ingredients = ingredientsTextArea.getText();
                String instructions = instructionsTextArea.getText();
                String tags = tagsTextArea.getText();

                // Process the tags: split by comma and/or space characters
                String[] tagArray = tags.split("[ ,]+");
                ArrayList<String> validTags = new ArrayList<>();

                // For each tag, check if it exists; if not, add it into the Tags table
                for (String tag : tagArray) {
                    tag = tag.trim();
                    if (!tag.isEmpty()) {
                        boolean tagExists = checkTagInDatabase(tag);
                        if (!tagExists) {
                            addTagToDatabase(tag);
                        }
                        validTags.add(tag);
                    }
                }

                // Call the method in connect.java to add the recipe to the database
                connect.addRecipeToDatabase(recipeName, ingredients, instructions, validTags, user_id);

                // After recipe is added, upload images to the database
                for (String imagePath : imagePaths) {
                    addImageToDatabase(imagePath, user_id);
                }
            }
        });

        // ActionListener for the "Add Photos" button (opens file chooser)
        addPhotosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open file chooser for selecting photos
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(true);  // Allow multiple selections
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = fileChooser.getSelectedFiles();
                    for (File selectedFile : selectedFiles) {
                        // Save the file path for each image
                        String imagePath = selectedFile.getAbsolutePath();
                        imagePaths.add(imagePath);

                        // Optionally, you could also move the image to a folder on the server for better organization
                        JOptionPane.showMessageDialog(null, "Photo selected: " + selectedFile.getName());
                    }
                }
            }
        });

        // Navigation button placeholders (to be implemented later)
        recipeButton.addActionListener(e -> { /* open another window here */ });
        aboutButton.addActionListener(e -> { /* open another window here */ });
        homeButton.addActionListener(e -> { /* open another window here */ });
    }

    // Method to check if a tag exists in the Tags table
    private boolean checkTagInDatabase(String tag) {
        String query = "SELECT * FROM Tags WHERE tag_name = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/RecipeManagement", "root", "password");
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, tag);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("SQL Error (checkTagInDatabase): " + e.getMessage());
        }
        return false;
    }

    // Method to add a new tag to the Tags table
    private void addTagToDatabase(String tag) {
        String query = "INSERT INTO Tags (tag_name) VALUES (?)";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/RecipeManagement", "root", "password");
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, tag);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Error (addTagToDatabase): " + e.getMessage());
        }
    }

    // Method to add image URL to the Images table
    private void addImageToDatabase(String imagePath, int recipeId) {
        String query = "INSERT INTO Images (recipe_id, image_url) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/RecipeManagement", "root", "password");
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, recipeId);
            pstmt.setString(2, imagePath);  // Save the image path (can also be a URL if using cloud storage)
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Error (addImageToDatabase): " + e.getMessage());
        }
    }
}
