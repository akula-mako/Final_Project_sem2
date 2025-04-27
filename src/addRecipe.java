import javax.swing.*;
import java.awt.*;
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
    private JButton logInButton;               // Placeholder navigation button (open recipe window)
    private JButton registerButton;                // Placeholder navigation button (open about window)
    private JButton homeButton;                 // Placeholder navigation button (open home window)
    private JLabel label;
    private JLabel recipeNameLabel;
    private JLabel iconLabel;
    private JLabel ingredientsLabel;
    private JLabel instructionsLabel;
    private JLabel tagsLabel;
    private JPanel panel;                     // Main panel
    private JLabel recipeLabel;
    private ArrayList<String> imagePaths;      // List to hold image file paths

    public addRecipe(int user_id) {
        setSize(600, 400);
        setContentPane(panel);
        setVisible(true);
        panel.setBackground(new Color(228, 213, 180));
        imagePaths = new ArrayList<>(); // Initialize imagePaths list

        // ActionListener for the "Add Recipe" button
        addRecipeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 1) Gather inputs
                String recipeName = recipeNameField.getText();
                String ingredients = ingredientsTextArea.getText();
                String instructions = instructionsTextArea.getText();
                String tags = tagsTextArea.getText();

                // 2) Process tags: split by comma/space, insert new tags as needed
                String[] tagArray = tags.split("[ ,]+");
                ArrayList<String> validTags = new ArrayList<>();
                for (String tag : tagArray) {
                    tag = tag.trim();
                    if (!tag.isEmpty()) {
                        if (!checkTagInDatabase(tag)) {
                            addTagToDatabase(tag);
                        }
                        validTags.add(tag);
                    }
                }

                // 3) Insert recipe and retrieve the generated recipeId
                int recipeId = connect.addRecipeToDatabase(
                        recipeName,
                        ingredients,
                        instructions,
                        validTags,
                        user_id
                );

                // 4) Upload each selected photo under the new recipeId
                for (String imagePath : imagePaths) {
                    addImageToDatabase(imagePath, recipeId);
                }

                // 5) Notify the user and navigate back home
                JOptionPane.showMessageDialog(
                        null,
                        "Recipe added successfully!",
                        "Recipe information",
                        JOptionPane.INFORMATION_MESSAGE
                );
                setVisible(false);
                new Home(user_id);
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
        logInButton.addActionListener(e -> { setVisible(false);
            new login(); });
        registerButton.addActionListener(e -> { setVisible(false);
            new register(); });
        homeButton.addActionListener(e -> {
            setVisible(false);
            new Home(user_id);
        });

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
