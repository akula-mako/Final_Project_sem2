import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.regex.*;

public class register extends JFrame {
    private JTextField textField1;    // Email input field
    private JTextField textField2;    // Password input field
    private JTextField textField3;    // Name input field
    private JButton registerButton;   // Register button
    private JButton logInButton;      // Back to login button
    private JPanel panel;             // Main panel to hold components
    private JLabel iconLabel;
    private JLabel emailLabel;
    private JLabel passLabel;
    private JLabel goBack;
    private JLabel nameLabel;

    public register() {
        setSize(500, 500);  // Set the window size
        setContentPane(panel);  // Set the content panel from the form
        setVisible(true);  // Make the register window visible
        panel.setBackground(new Color(228, 213, 180));
        // Load the image
        ImageIcon icon = new ImageIcon(getClass().getResource("/logo.png"));

        // Scale the image
        Image scaledImage = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);

        // Set the scaled image as an ImageIcon for the label
        iconLabel.setIcon(new ImageIcon(scaledImage));

        // ActionListener for the "Register" button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Retrieve input data from the fields
                String name = textField3.getText();    // Name field
                String email = textField1.getText();   // Email field
                String password = textField2.getText(); // Password field

                // Check if all fields are filled
                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Call the register method from the connect class to register the new user
                boolean success = connect.register(name, email, password);

                if (success) {
                    // If registration is successful, show success message
                    JOptionPane.showMessageDialog(null, "Registration Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    new login();  // Open the login window after successful registration
                    setVisible(false);  // Close the register window
                } else {
                    // If registration fails, show an error message
                    JOptionPane.showMessageDialog(null, "Registration failed. Try again.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ActionListener for the "Back to Log In" button
        logInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the login form when the "Back to Log In" button is clicked
                new login();  // Open the login window
                setVisible(false);  // Close the register window
            }
        });

        // FocusListener to validate email format and block leaving the field until valid email
        textField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String email = textField1.getText();
                if (!isValidEmail(email)) {
                    JOptionPane.showMessageDialog(null, "Invalid email format. Please enter a valid email.", "Email Error", JOptionPane.ERROR_MESSAGE);
                    textField1.requestFocusInWindow();  // Prevent focus from leaving the email field
                }
            }
        });
    }

    // Method to validate email using regex
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
