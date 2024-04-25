import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

public class Security {
    //hashing password method
    private static String getHash(String password, byte[] salt){
        if (!password.equals("Password")) {
            String generatedPassword = null;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(salt);
                byte[] bytes = md.digest(password.getBytes());
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < bytes.length; i++) {
                    sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                }
                generatedPassword = sb.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return generatedPassword;
        } else {
            return null;
        }
    }

    //salt generator method
    private static byte[] getSalt() throws NoSuchAlgorithmException{
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }
    //method of checking if such login already exists in our database
    private static boolean loginExists(String userLogin){
        try(Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
            PreparedStatement preparedStatement = conn.prepareStatement("select count(*) from userauth where Login = ?");

            preparedStatement.setString(1, userLogin);

            try(ResultSet rs = preparedStatement.executeQuery()){
                if(rs.next()){
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    //creation of new user

    public static void createUser(String loginUser, String passwordUser, Login loginFrame){
        if(!Objects.equals(loginUser, "") && !Objects.equals(loginUser, "Login")) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                // check if user's database contains userauth table. We place it here, because userauth is used in loginExists method. We need to check userauth before its execution.
                try (PreparedStatement checkUserauth = conn.prepareStatement("select * from information_schema.TABLES where TABLE_SCHEMA = 'mydb' and TABLE_NAME = 'userauth'")) {
                    ResultSet checker = checkUserauth.executeQuery();
                    // if no, create it
                    if (!checker.next()) {
                        PreparedStatement createUserauth = conn.prepareStatement("create table userauth (login varchar(100) primary key, password varchar(64), salt binary(16))");
                        createUserauth.executeUpdate();
                    }
                }
                if (!loginExists(loginUser)) {
                    byte[] salt = getSalt();
                    String hashedPassword = getHash(passwordUser, salt);

                    if (hashedPassword != null) {
                        PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO userauth (Login, password, salt) VALUES (?, ?, ?)");

                        preparedStatement.setString(1, loginUser);
                        preparedStatement.setString(2, hashedPassword);
                        preparedStatement.setBytes(3, salt);
                        int affectedRows = preparedStatement.executeUpdate();


                        if (affectedRows > 0) {

                            String tableNameAcc = "acc_" + loginUser;
                            String sql = "create table " + tableNameAcc + " (account_id int auto_increment primary key, account_name varchar(255) not null, age int, email varchar(50), phone_number varchar(50), workID varchar(50), changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
                            try (Statement st = conn.createStatement()) {
                                st.executeUpdate(sql);
                            }

                            String tableNameGr = "gr_" + loginUser;
                            String sqlGr = "create table " + tableNameGr + " (group_id int auto_increment primary key, name varchar(255) not null unique, changed_at timestamp default current_timestamp)";
                            try (Statement st = conn.createStatement()) {
                                st.executeUpdate(sqlGr);
                            }
                            // add there a row for unassigned accounts
                            PreparedStatement preparedStatement1 = conn.prepareStatement("insert into " + tableNameGr + " (name) values ('Unassigned Accounts');");
                            preparedStatement1.executeUpdate();

                            String tableNameJun = "acc_gr_junction_" + loginUser;
                            String sqlJun = "create table " + tableNameJun + " (group_id int, account_id int, primary key (group_id, account_id), foreign key (group_id) references " + tableNameGr + "(group_id), foreign key (account_id) references " + tableNameAcc + "(account_id) on delete cascade);";
                            try (Statement st = conn.createStatement()) {
                                st.executeUpdate(sqlJun);
                            }
                            // create tables for files and projects and junction tables for them
                            try (Statement st = conn.createStatement()) {
                                st.executeUpdate("create table files_" + loginUser + " (file_id int auto_increment primary key, account_id int, file_name varchar(255) not null, file_path varchar(255) not null, file_type varchar(50), last_change timestamp default current_timestamp, file_creation timestamp, foreign key (account_id) references " + tableNameAcc + "(account_id) on delete cascade)");
                            }
                            try (Statement st = conn.createStatement()) {
                                st.executeUpdate("create table projects_" + loginUser + " (project_id int auto_increment primary key, project_name varchar(100) not null, description text, deadline date, changed_at timestamp default current_timestamp);");
                            }
                            try (Statement st = conn.createStatement()) {
                                st.executeUpdate("create table projects_junction_" + loginUser + " (account_id int, project_id int, primary key (account_id, project_id),foreign key (account_id) references " + tableNameAcc + "(account_id), foreign key (project_id) references projects_" + loginUser + "(project_id) on delete cascade)");
                            }

                            //starts the gui window if successful
                            loginFrame.dispose();
                            new GUI(loginUser);
                            Group.loadGroupsFromDatabase(loginUser, "group_id asc");

                        } else {
                            JOptionPane.showMessageDialog(null, "User creation failed. Try again!");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Enter valid password");
                    }
                }  else {
                    JOptionPane.showMessageDialog(null, "User creation failed. Such user already exists.");
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Login must not be null.");
        }
    }
    private static byte[] getSaltByLogin(String login){
        String sql = "select salt from userauth where Login = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
            PreparedStatement st = conn.prepareStatement(sql);

            st.setString(1, login);

            try(ResultSet rs = st.executeQuery()){
                if (rs.next()){
                    return rs.getBytes("salt");
                }
            }

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }
// enter user (we add Login loginFrame to ensure closing of the Login frame in case everything is successful)
    public static void enterUser(String loginUser, String passwordUser, Login loginFrame) {
        if (!Objects.equals(loginUser, "")){
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                // check if the userauth table exists
                try (PreparedStatement checkUserauth = conn.prepareStatement("select * from information_schema.TABLES where TABLE_SCHEMA = 'mydb' and TABLE_NAME = 'userauth'")) {
                    ResultSet checker = checkUserauth.executeQuery();
                    // if no, create it
                    if (!checker.next()) {
                        PreparedStatement createUserauth = conn.prepareStatement("create table userauth (login varchar(100) primary key, password varchar(64), salt binary(16))");
                        createUserauth.executeUpdate();
                    }
                }
                if (loginExists(loginUser)){
                    byte[] salt = getSaltByLogin(loginUser);
                    String hashedPasswordUser = getHash(passwordUser, salt);
                    String hashedPasswordDB = null;

                    if (hashedPasswordUser != null) {
                        // get the password from the db
                        PreparedStatement st = conn.prepareStatement("select password from userauth where Login = ?");
                        st.setString(1, loginUser);
                        ResultSet rs = st.executeQuery();
                        if (rs.next()) {
                            hashedPasswordDB = rs.getString("password");
                        }
                        // proceed if the password is correct, start the main frame
                        if (Objects.equals(hashedPasswordUser, hashedPasswordDB)) {
                            loginFrame.dispose();
                            new GUI(loginUser);
                            Group.loadGroupsFromDatabase(loginUser, "group_id asc");

                            // check all the deadlines. If any of them expires tomorrow, show the warning
                            SwingUtilities.invokeLater(() -> {
                                // put here another try-catch clause, since the connection is closed, while the Runnable from invokeLater is still being executed
                                try (Connection conn1 = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                                    try (PreparedStatement checkDeadlines = conn1.prepareStatement("select count(*) from projects_" + loginUser + " where deadline = date_add(curdate(), interval 1 day)")) {
                                        ResultSet countExpiringDeadlines = checkDeadlines.executeQuery();
                                        if (countExpiringDeadlines.next() && countExpiringDeadlines.getInt(1) > 0) {
                                            // if there are any expiring dates, add them to the array
                                            List<String> expiringProjects = new ArrayList<>();
                                            PreparedStatement fetchExpiringProjects = conn1.prepareStatement("select project_name from projects_" + loginUser + " where deadline = date_add(curdate(), interval 1 day)");
                                            ResultSet rs1 = fetchExpiringProjects.executeQuery();
                                            while (rs1.next()) {
                                                expiringProjects.add(rs1.getString("project_name"));
                                            }
                                            // display the waring with names of the projects
                                            JFrame warningMessage = new JFrame("Expiring deadline");
                                            warningMessage.setVisible(true);
                                            JPanel warningPanel = new JPanel(new BorderLayout());
                                            warningMessage.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                                            JLabel warningText = new JLabel("Warning! Some projects will expire tomorrow:");
                                            warningText.setFont(new Font("Arial", Font.BOLD, 20));
                                            warningPanel.add(warningText, BorderLayout.NORTH);

                                            JPanel namesPanel = new JPanel();
                                            namesPanel.setLayout(new BoxLayout(namesPanel, BoxLayout.Y_AXIS));
                                            for (String expiringProject : expiringProjects) {
                                                JLabel expiringLabel = new JLabel(expiringProject);
                                                expiringLabel.setFont(new Font("Arial", Font.PLAIN, 15));
                                                expiringLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                                                namesPanel.add(expiringLabel);
                                            }
                                            warningPanel.add(namesPanel, BorderLayout.CENTER);
                                            warningMessage.add(warningPanel);
                                            warningMessage.pack();
                                            warningMessage.setLocationRelativeTo(null);
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    // now count expired deadlines
                                    try (PreparedStatement countExpiredDeadlines = conn1.prepareStatement("select count(*) from projects_" + loginUser + " where deadline < curdate()")) {
                                        ResultSet rs1 = countExpiredDeadlines.executeQuery();
                                        if (rs1.next() && rs1.getInt(1) > 0) {
                                            // add expired projects names to the list
                                            List<String> expiredProjects = new ArrayList<>();
                                            PreparedStatement fetchExpiredProjects = conn1.prepareStatement("select project_name from projects_" + loginUser + " where deadline < curdate()");
                                            ResultSet rs2 = fetchExpiredProjects.executeQuery();
                                            while (rs2.next()) {
                                                expiredProjects.add(rs2.getString("project_name"));
                                            }
                                            // display the warning
                                            JFrame warningMessage = new JFrame("Expired Deadline");
                                            warningMessage.setVisible(true);
                                            JPanel warningPanel = new JPanel();
                                            warningPanel.setLayout(new BorderLayout());
                                            warningMessage.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                                            JLabel warningText = new JLabel("Warning! Some projects have been expired and automatically deleted");
                                            warningText.setFont(new Font("Arial", Font.BOLD, 20));
                                            warningPanel.add(warningText, BorderLayout.NORTH);

                                            JPanel namesPanel = new JPanel();
                                            namesPanel.setLayout(new BoxLayout(namesPanel, BoxLayout.Y_AXIS));
                                            for (String expiredProject : expiredProjects) {
                                                JLabel expiredLabel = new JLabel(expiredProject);
                                                expiredLabel.setFont(new Font("Arial", Font.PLAIN, 15));
                                                expiredLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                                                namesPanel.add(expiredLabel);
                                            }
                                            warningPanel.add(namesPanel, BorderLayout.CENTER);
                                            warningMessage.add(warningPanel);
                                            warningMessage.pack();
                                            warningMessage.setLocationRelativeTo(null);
                                            // delete all the expired projects
                                            PreparedStatement deleteExpiredProjects = conn1.prepareStatement("delete from projects_" + loginUser + " where deadline < curdate()");
                                            deleteExpiredProjects.executeUpdate();
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                } catch (SQLException e1) {
                                    e1.printStackTrace();
                                }
                            });

                        } else {
                            JOptionPane.showMessageDialog(null, "Error. Wrong password");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Error. Enter valid password");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Error. Such user doesn't exist");
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Error. Please enter user login");
        }
    }

}