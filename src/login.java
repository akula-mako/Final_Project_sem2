import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class login extends JFrame {
    private JTextField textField1;  // Email input field
    private JTextField textField2;  // Password input field
    private JButton loginButton;    // Login button
    private JButton registerButton; // Register button
    private JPanel panel;           // Main panel to hold components
    private JLabel icon;            // Icon label
    private JLabel WelcomeBack;
    private JLabel LogIn;
    private JLabel registerLabel;


    public login() {
        setSize(500, 500);  // Set the window size
        setContentPane(panel);  // Set the content panel from the form
        setVisible(true);  // Make the login window visible

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
                   //TO ADD A METHOD TO LAUNCH THE PROGRAM ITSELF
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
    }
}
