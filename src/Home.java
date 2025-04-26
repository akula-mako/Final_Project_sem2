import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class Home extends JFrame {
    private JPanel panel; // Main panel
    private JTextField searchField; // Search text field
    private JButton searchButton; // Search button
    private JButton addRecipeButton; // Add recipe button
    private JButton viewRecipeButton; // View recipe button
    private JTable resultsTable; // Results table
    private JComboBox<String> sortCombo; // ComboBox for sorting
    private JScrollPane scrollPane; // Scroll pane for the table
    private DefaultTableModel model; // Table model for dynamic data
    private ArrayList<String[]> recipes; // List to store recipe data
    private JLabel searchLabel;

    public Home(int userId) {
        setSize(800, 600);
        setContentPane(panel);
        setVisible(true);
        panel.setBackground(new Color(228, 213, 180));
        model = new DefaultTableModel();
        model.setColumnIdentifiers(new Object[]{"Recipe ID", "Recipe Name", "Creator", "Created on:"});
        resultsTable.setModel(model);

        // Disable editing in the table
        resultsTable.setDefaultEditor(Object.class, null);

        // Load initial data (display results)
        loadTableData(""); // Initial call to load data without sorting

        // Search button action
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = searchField.getText();
                loadTableData(query); // Load data based on search query
            }
        });

        // Sort combo box action
        sortCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sortOrder = getSortOrderBasedOnSelection(sortCombo.getSelectedIndex());
                loadTableData("", sortOrder); // Load sorted data based on the selected option
            }
        });

        // Add recipe button action
        addRecipeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new addRecipe(userId); // Open addRecipe form with userId
                setVisible(false);
            }
        });

        // View recipe button action
        viewRecipeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = resultsTable.getSelectedRow();
                if (selectedRow == -1) {
                    // No recipe selected
                    JOptionPane.showMessageDialog(null, "No recipe selected!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Get the recipe ID from the first column
                    String recipeIdStr = (String) model.getValueAt(selectedRow, 0); // Get recipe ID
                    try {
                        // Attempt to parse the recipe ID
                        int recipeId = Integer.parseInt(recipeIdStr);
                        openRecipeWindow(recipeId, userId); // Open the addRecipe window with recipe ID
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Invalid recipe ID!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    // Method to load data into the table
    private void loadTableData(String query, String... sortOrder) {
        // Clear existing rows
        model.setRowCount(0);

        // SQL query to fetch recipes and their associated user names and created_at
        String sql = "SELECT r.recipe_id, r.name, u.name, r.created_at " +
                "FROM Recipes r JOIN Users u ON r.user_id = u.user_id";

        // If there's a search query, apply it to the SQL
        boolean hasSearchQuery = !query.isEmpty();
        if (hasSearchQuery) {
            sql += " WHERE r.name LIKE ? OR EXISTS (SELECT 1 FROM Recipe_Tags rt " +
                    "JOIN Tags t ON rt.tag_id = t.tag_id WHERE rt.recipe_id = r.recipe_id AND t.tag_name LIKE ?)";
        }

        // Add sorting logic if needed
        if (sortOrder.length > 0) {
            sql += " " + sortOrder[0];  // Dynamically apply the sort order (A-Z or Z-A)
        }

        // Now, execute the query with parameters (only if the search query is provided)
        ArrayList<String[]> recipesList;
        if (hasSearchQuery) {
            recipesList = executeQuery(sql, "%" + query + "%", "%" + query + "%");
        } else {
            recipesList = executeQuery(sql);  // No parameters needed if there's no search query
        }

        // Populate the table with fetched data
        updateTable(recipesList);
    }

    // Helper method to execute parameterized queries
    private ArrayList<String[]> executeQuery(String query, String... params) {
        ArrayList<String[]> results = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/RecipeManagement", "root", "password");
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            // Set parameters for the query if any are provided
            for (int i = 0; i < params.length; i++) {
                pstmt.setString(i + 1, params[i]);
            }

            // Execute the query
            try (ResultSet rs = pstmt.executeQuery()) {
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
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
        return results;
    }

    // Method to update the table with new data
    private void updateTable(ArrayList<String[]> recipesList) {
        // Clear existing rows first
        model.setRowCount(0);

        // Populate the table with new data
        for (String[] recipe : recipesList) {
            model.addRow(new Object[]{recipe[0], recipe[1], recipe[2], recipe[3]});
        }
    }

    // Method to get the sort order based on the ComboBox selection
    private String getSortOrderBasedOnSelection(int selectedIndex) {
        switch (selectedIndex) {
            case 0: return "ORDER BY r.name ASC";  // Recipe Name A-Z
            case 1: return "ORDER BY r.name DESC"; // Recipe Name Z-A
            case 2: return "ORDER BY r.created_at ASC"; // Date Created A-Z
            case 3: return "ORDER BY r.created_at DESC"; // Date Created Z-A
            default: return "";
        }
    }

    // Method to open the addRecipe window when double-clicking a recipe
    private void openRecipeWindow(int recipeId, int user_id) {
        new openRecipe(recipeId, user_id); // This will pass the recipe ID to the addRecipe window
    }

    public static void main(String[] args) {
        new Home(1); // For now, assuming user_id = 1 for testing
    }
}
