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
        setSize(500, 500);                  // Set the window size
        setContentPane(panel);              // Set the content panel from the form
        setVisible(true);                   // Make the login window visible
        panel.setBackground(new Color(228, 213, 180));

        // Load and set the icon image
        ImageIcon icon = new ImageIcon(getClass().getResource("/logo.png"));
        Image scaledImage = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        iconLabel.setIcon(new ImageIcon(scaledImage));

        // Initially disable the login button
        loginButton.setEnabled(false);

        // ActionListener for the "Log In" button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = textField1.getText();
                String password = textField2.getText();

                User user = connect.login(email, password);

                if (user != null) {
                    JOptionPane.showMessageDialog(
                            login.this,
                            "Successfully logged in as " + user.getName(),
                            "Login Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    new Home(Integer.parseInt(user.getUserId()));
                    setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(
                            login.this,
                            "Login failed. Invalid credentials.",
                            "Login Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        // ActionListener for the "Register" button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register registerForm = new register();
                registerForm.setVisible(true);
                setVisible(false);
            }
        });

        // FocusListener for email validation, skipping when moving focus to Register button
        textField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                Component newFocusOwner = e.getOppositeComponent();
                if (newFocusOwner == registerButton) {
                    return;
                }

                String email = textField1.getText();
                if (!isValidEmail(email)) {
                    JOptionPane.showMessageDialog(
                            login.this,
                            "Invalid email format. Please enter a valid email.",
                            "Email Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    loginButton.setEnabled(false);
                } else {
                    loginButton.setEnabled(true);
                }
            }
        });

        // KeyListener for enabling/disabling login button on the fly
        textField1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loginButton.setEnabled(isValidEmail(textField1.getText()));
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
