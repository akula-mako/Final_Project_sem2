import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.awt.*;

public class openRecipe extends JFrame {
    private JLabel nameLabel;
    private JLabel iconLabel;
    private JLabel ingredientsLabel;
    private JLabel instructionsLabel;
    private JLabel tagsLabel;
    private JTextArea ingredientsArea;
    private JTextArea instructionsArea;
    private JTextArea tagsArea;
    private JButton editButton;
    private JButton deleteButton;

    private void loadRecipeData(int recipeID) {
        // Query to get the recipe details (image URL, name, ingredients, instructions)
        String query = "SELECT r.name, r.ingredients, r.instructions, i.image_url " +
                "FROM Recipes r " +
                "LEFT JOIN Images i ON r.recipe_id = i.recipe_id " + // Join with Images table to get the image_url
                "WHERE r.recipe_id = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/RecipeManagement", "root", "password");
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, recipeID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nameLabel.setText(rs.getString("name"));
                    ingredientsArea.setText(rs.getString("ingredients"));
                    instructionsArea.setText(rs.getString("instructions"));
                    setRecipeIcon(rs.getString("image_url"));  // Image URL is retrieved from Images table
                }
            }

            // Query to get the tags associated with this recipe
            String tagQuery = "SELECT GROUP_CONCAT(t.tag_name) AS tags " +
                    "FROM Tags t " +
                    "JOIN Recipe_Tags rt ON t.tag_id = rt.tag_id " +
                    "WHERE rt.recipe_id = ?";
            try (PreparedStatement tagPstmt = connection.prepareStatement(tagQuery)) {
                tagPstmt.setInt(1, recipeID);
                try (ResultSet tagRs = tagPstmt.executeQuery()) {
                    if (tagRs.next()) {
                        tagsArea.setText(tagRs.getString("tags"));  // Set the tags in the tags area
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error (loadRecipeData): " + e.getMessage());
        }
    }

    private JPanel panel;

    public openRecipe(int recipeID, int currentUserID) {
        setSize(600, 600);
        setContentPane(panel);
        setVisible(true);
        panel.setBackground(new Color(228, 213, 180));
        // Initially set text areas as non-editable
        ingredientsArea.setEditable(false);
        instructionsArea.setEditable(false);
        tagsArea.setEditable(false);

        // Load recipe data
        loadRecipeData(recipeID);

        // Check if the current user is the creator of the recipe
        boolean isCreator = isRecipeCreator(recipeID, currentUserID);

        // Enable or disable the Edit/Delete buttons based on whether the user is the creator
        if (isCreator) {
            editButton.setEnabled(true);
            deleteButton.setEnabled(true);
        } else {
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }

        // ActionListener for the "Delete" button
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Delete the recipe from the database using connect method
                connect.deleteRecipe(recipeID);
                JOptionPane.showMessageDialog(null, "Recipe deleted successfully");
                setVisible(false);
                new Home(currentUserID); // Redirect back to home page
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Toggle between editing and saving
                if (editButton.getText().equals("Edit")) {
                    // Only allow editing if the user is the creator
                    if (isCreator) {
                        ingredientsArea.setEditable(true);
                        instructionsArea.setEditable(true);
                        tagsArea.setEditable(true);
                        editButton.setText("Save");
                    } else {
                        JOptionPane.showMessageDialog(null, "You are not the creator of this recipe.");
                    }
                } else {
                    // Save the updated recipe to the database using the connect methods

                    String updatedIngredients = ingredientsArea.getText();
                    String updatedInstructions = instructionsArea.getText();
                    String updatedTags = tagsArea.getText();

                    // Delete old tags and add new ones via the connect methods
                    deleteTags(recipeID);  // Delete old tags first
                    addTags(recipeID, updatedTags);  // Add the new tags

                    // Update the recipe using the connect method (ingredients and instructions)
                    connect.updateRecipe(recipeID, updatedIngredients, updatedInstructions);

                    JOptionPane.showMessageDialog(null, "Recipe updated successfully");
                    setVisible(false);
                    new Home(currentUserID); // Redirect back to home page
                }
            }
        });
    }

    // Method to set the recipe icon
    private void setRecipeIcon(String iconPath) {
        ImageIcon icon = connect.getImageIcon(iconPath);
        Image scaledImage = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        iconLabel.setIcon(new ImageIcon(scaledImage));
    }

    // Method to check if the current user is the creator of the recipe
    private boolean isRecipeCreator(int recipeID, int currentUserID) {
        String query = "SELECT user_id FROM Recipes WHERE recipe_id = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/RecipeManagement", "root", "password");
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, recipeID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id") == currentUserID;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error (isRecipeCreator): " + e.getMessage());
        }
        return false;
    }

    // Method to delete old tags associated with the recipe
    private void deleteTags(int recipeID) {
        String deleteTagsQuery = "DELETE FROM Recipe_Tags WHERE recipe_id = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/RecipeManagement", "root", "password");
             PreparedStatement pstmt = connection.prepareStatement(deleteTagsQuery)) {
            pstmt.setInt(1, recipeID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Error (deleteTags): " + e.getMessage());
        }
    }

    // Method to add new tags associated with the recipe
    private void addTags(int recipeID, String updatedTags) {
        // Split the tags entered by the user
        String[] tagArray = updatedTags.split("[ ,]+");

        for (String tag : tagArray) {
            tag = tag.trim();
            if (!tag.isEmpty()) {
                // Add the tag to the Recipe_Tags table using the connect method
                connect.addRecipeTag(recipeID, tag);
            }
        }
    }
}
