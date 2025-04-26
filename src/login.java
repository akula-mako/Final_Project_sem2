import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.regex.*;

public class login extends JFrame {
    private JTextField textField1;  // Email input field
    private JTextField textField2;  // Password input field
    private JButton loginButton;    // Login button
    private JButton registerButton; // Register button
    private JPanel panel;           // Main panel to hold components
    private JLabel iconLabel;       // Icon label
    private JLabel WelcomeBack;
    private JLabel LogIn;
    private JLabel registerLabel;

    public login() {
        setSize(500, 500);  // Set the window size
        setContentPane(panel);  // Set the content panel from the form
        setVisible(true);  // Make the login window visible
        panel.setBackground(new Color(228, 213, 180));
        // Load the image
        ImageIcon icon = new ImageIcon(getClass().getResource("/logo.png"));

        // Scale the image
        Image scaledImage = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);

        // Set the scaled image as an ImageIcon for the label
        iconLabel.setIcon(new ImageIcon(scaledImage));

        // Initially disable the login button
        loginButton.setEnabled(false);

        // ActionListener for the "Log In" button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get email and password from the text fields
                String email = textField1.getText();
                String password = textField2.getText();

                // Call the login method from the connect class to check credentials
                User user = connect.login(email, password);

                if (user != null) {
                    // If login is successful, show a success message
                    JOptionPane.showMessageDialog(null, "Successfully logged in as " + user.getName(), "Login Success", JOptionPane.INFORMATION_MESSAGE);

                    // Open the Welcome window (or the main app window)
                    new Home(Integer.parseInt(user.getUserId()));
                    setVisible(false);  // Close the login window
                } else {
                    // If login failed, show an error message
                    JOptionPane.showMessageDialog(null, "Login failed. Invalid credentials.", "Login Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ActionListener for the "Register" button (opens register form)
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the register form when the "Register" button is clicked
                register registerForm = new register();  // Assuming register.java is the form class for registration
                registerForm.setVisible(true);  // Make the register form visible
                setVisible(false);  // Close the login window
            }
        });

        // FocusListener to validate email format and block leaving the field until valid email
        textField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String email = textField1.getText();
                if (!isValidEmail(email)) {
                    JOptionPane.showMessageDialog(null, "Invalid email format. Please enter a valid email.", "Email Error", JOptionPane.ERROR_MESSAGE);
                    loginButton.setEnabled(false);  // Disable login button if email is invalid
                } else {
                    loginButton.setEnabled(true);  // Enable login button if email is valid
                }
            }
        });

        // Add a KeyListener to enable/disable the login button as the user types in the email field
        textField1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String email = textField1.getText();
                if (isValidEmail(email)) {
                    loginButton.setEnabled(true);  // Enable login button if email is valid
                } else {
                    loginButton.setEnabled(false);  // Disable login button if email is invalid
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
