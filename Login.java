import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;

public class Login extends JFrame implements ActionListener {
    Login(){

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        //login field

        JTextField loginField = new JTextField("Login");
        // hint
        loginField.setForeground(Color.BLACK);
        loginField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (loginField.getText().trim().equals("Login")){
                    loginField.setText("");
                    loginField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (loginField.getText().trim().isEmpty()){
                    loginField.setForeground(Color.gray);
                    loginField.setText("Login");
                }
            }
        });
        gbc.weightx = 1.0;
        loginField.setFont(new Font("Ariel", Font.PLAIN, 20));
        loginField.setPreferredSize(new Dimension(200, 30));
        panel.add(loginField, gbc);

        //password field

        JPasswordField passwordField = new JPasswordField("Password");
        passwordField.setPreferredSize(new Dimension(200, 30));
        passwordField.setFont(new Font("Ariel", Font.PLAIN, 20));
        panel.add(passwordField, gbc);

        //checkbox

        JCheckBox checkBox = new JCheckBox();
        checkBox.setText("Show password");
        checkBox.setFocusable(false);
        checkBox.setFont(new Font("Ariel", Font.PLAIN, 20));
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(checkBox, gbc);

        //sign up button

        JButton signUpButton = new JButton("Sign Up");
        signUpButton.setFocusable(false);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.LINE_START;
        signUpButton.setFont(new Font("Ariel", Font.BOLD, 20));
        panel.add(signUpButton, gbc);

        //assigning action to sign up button

        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { //we add Login.this to pass the existing Login frame as a parameter
                Security.createUser(loginField.getText(), Arrays.toString(passwordField.getPassword()), Login.this);
            }
        });

        //login button

        JButton loginButton = new JButton("Log In");
        loginButton.setFocusable(false);
        gbc.anchor = GridBagConstraints.LINE_START;
        loginButton.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(loginButton, gbc);

        //assigning action to Login button

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { //we add Login.this to pass the existing Login frame as a parameter
                Security.enterUser(loginField.getText(), Arrays.toString(passwordField.getPassword()), Login.this);
            }
        });

        //password method

        final char defaultEchoChar = passwordField.getEchoChar();

        checkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkBox.isSelected()){
                    passwordField.setEchoChar('\0');
                } else {
                    passwordField.setEchoChar(defaultEchoChar);
                }
            }
        });




        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(new Dimension(400, 400));
        this.setVisible(true);
        this.setTitle("Login Form");
        this.setLayout(new FlowLayout());
        this.add(panel);
        this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}