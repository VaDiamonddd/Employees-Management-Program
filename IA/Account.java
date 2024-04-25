import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.Objects;

public class Account {
    public static String selectedAccSortingMethod;
    private static String getSafeSortMethod(String userInput){
        if (userInput == null){
            return "acc.account_id asc";
        }
        switch (userInput){
            case "Sort..": return "acc.account_id asc";
            case "account_name asc": return "acc.account_name ASC";
            case "account_name desc": return "acc.account_name DESC";
            case "account_id asc": return "acc.account_id ASC";
            case "account_id desc": return "acc.account_id DESC";
            case "age asc": return "acc.age ASC";
            case "age desc": return "acc.age DESC";
            case "changed_at asc": return "acc.changed_at ASC";
            case "changed_at desc": return "acc.changed_at DESC";
            default: return "acc.account_id ASC";
        }
    }
    public static void getAccountsByGroup(String userLogin, String groupName, JPanel accountsPanel, String sortingOption) {
        // assign value to the sorting method
        selectedAccSortingMethod = sortingOption;
        SwingUtilities.invokeLater(() -> {

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                //clear the main panel
                GUI.subProjectsPanel.removeAll();
                GUI.subProjectsPanel.revalidate();
                GUI.subProjectsPanel.repaint();
                GUI.filesPanel.removeAll();
                GUI.filesPanel.revalidate();
                GUI.filesPanel.repaint();
                accountsPanel.removeAll();
                accountsPanel.setLayout(new BorderLayout()); //set layout for our main column
                accountsPanel.revalidate();
                accountsPanel.repaint();

                //dropdown for accountsColumn
                String[] accountsColumnSortingOptions = {"Sort..", "Alphabet A-Z", "Alphabet Z-A", "Age asc", "Age desc", "Creation Date (Older)", "Creation Date (Newer)", "Date of Change (Older)", "Date of Change (Newer)"};
                JComboBox<String> accountsColumnSortingDropdown = new JComboBox<>(accountsColumnSortingOptions);
                accountsColumnSortingDropdown.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // sorting logic
                        String selectedAccountSortingOption = (String) accountsColumnSortingDropdown.getSelectedItem();
                        // assign actions to the items of the dropdown
                        switch (Objects.requireNonNull(selectedAccountSortingOption)){
                            case "Sort..": break;
                            case "Alphabet A-Z":
                                GUI.accountsColumn.removeAll();
                                getAccountsByGroup(userLogin, groupName, accountsPanel, "account_name asc");
                                GUI.accountsColumn.revalidate();
                                GUI.accountsColumn.repaint();
                                break;
                            case "Alphabet Z-A":
                                GUI.accountsColumn.removeAll();
                                getAccountsByGroup(userLogin, groupName, accountsPanel, "account_name desc");
                                GUI.accountsColumn.revalidate();
                                GUI.accountsColumn.repaint();
                                break;
                            case "Age asc":
                                GUI.accountsColumn.removeAll();
                                getAccountsByGroup(userLogin, groupName, accountsPanel, "age asc");
                                GUI.accountsColumn.revalidate();
                                GUI.accountsColumn.repaint();
                                break;
                            case "Age desc":
                                GUI.accountsColumn.removeAll();
                                getAccountsByGroup(userLogin, groupName, accountsPanel, "age desc");
                                GUI.accountsColumn.revalidate();
                                GUI.accountsColumn.repaint();
                                break;
                            case "Creation Date (Older)":
                                GUI.accountsColumn.removeAll();
                                getAccountsByGroup(userLogin, groupName, accountsPanel, "account_id asc");
                                GUI.accountsColumn.revalidate();
                                GUI.accountsColumn.repaint();
                                break;
                            case "Creation Date (Newer)":
                                GUI.accountsColumn.removeAll();
                                getAccountsByGroup(userLogin, groupName, accountsPanel, "account_id desc");
                                GUI.accountsColumn.revalidate();
                                GUI.accountsColumn.repaint();
                                break;
                            case "Date of Change (Older)":
                                GUI.accountsColumn.removeAll();
                                getAccountsByGroup(userLogin, groupName, accountsPanel, "changed_at asc");
                                GUI.accountsColumn.revalidate();
                                GUI.accountsColumn.repaint();
                                break;
                            case "Date of Change (Newer)":
                                GUI.accountsColumn.removeAll();
                                getAccountsByGroup(userLogin, groupName, accountsPanel, "changed_at desc");
                                GUI.accountsColumn.revalidate();
                                GUI.accountsColumn.repaint();
                                break;
                        }
                    }
                });
                // Top panel for the dropdown in accountsColumn
                JPanel accountsTopPanel = new JPanel(new BorderLayout());
                accountsTopPanel.add(accountsColumnSortingDropdown, BorderLayout.CENTER);

                accountsPanel.add(accountsTopPanel, BorderLayout.NORTH);


                // create panel for future buttons

                JPanel buttonsPanel = new JPanel();
                buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

                //get groupId from the group table
                PreparedStatement prst1 = conn.prepareStatement("select group_id from gr_" + userLogin + " where name = '" + groupName + "';");
                ResultSet rs = prst1.executeQuery();
                int groupId = 0;
                while (rs.next()) {
                    groupId = rs.getInt("group_id");
                }

                // select account names assigned to the group with respect to sorting method
                String baseQuery = "select account_name from acc_" + userLogin + " as acc inner join acc_gr_junction_" + userLogin + " as acc_gr_j on acc.account_id = acc_gr_j.account_id where acc_gr_j.group_id = ? ";
                String safeSortingMethod = getSafeSortMethod(sortingOption);
                String fullQuery = baseQuery + "order by " + safeSortingMethod;
                PreparedStatement fetchSortedAccounts = conn.prepareStatement(fullQuery);
                fetchSortedAccounts.setInt(1, groupId);
                ResultSet resultSet = fetchSortedAccounts.executeQuery();
                while (resultSet.next()) {
                    String accountName = resultSet.getString("account_name");
                    // also find specific id of the account
                    PreparedStatement fetchAccountID = conn.prepareStatement("select account_id from acc_" + userLogin + " where account_name = ?");
                    fetchAccountID.setString(1, accountName);
                    int accountID = 0;
                    ResultSet rs1 = fetchAccountID.executeQuery();
                    while (rs1.next()){
                        accountID = rs1.getInt(1);
                    }

                    //add button to the panel
                    JPanel buttonWrapper = new JPanel(new BorderLayout());
                    JButton accountButton = new JButton(accountName);
                    accountButton.setFocusable(false);
                    accountButton.setPreferredSize(new Dimension(1, 40));

                    // assign action to the button (loads files and projects)
                    String finalAccountName = accountName;
                    accountButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Projects_Files.getProjectsFilesByAccount(userLogin, finalAccountName, GUI.subProjectsPanel, GUI.filesPanel, "project_name asc");
                        }
                    });
                    // add right-click popup list (change and delete)
                    int finalAccountID = accountID;
                    int finalAccountID1 = accountID;
                    accountButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (SwingUtilities.isRightMouseButton(e)){
                                // create popup menu
                                JPopupMenu popupMenu = new JPopupMenu();
                                JMenuItem accountChangeItem = new JMenuItem("Change");
                                JMenuItem accountDeleteItem = new JMenuItem("Delete");
                                // add menu item
                                popupMenu.add(accountChangeItem);
                                popupMenu.add(accountDeleteItem);

                                popupMenu.show(e.getComponent(), e.getX(), e.getY());

                                // assign actions
                                accountChangeItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        JPanel changePanel = new JPanel();
                                        changePanel.setLayout(new BoxLayout(changePanel, BoxLayout.Y_AXIS));

                                        // create text fields with values from the database
                                        JTextField nameField = new JTextField(finalAccountName);
                                        nameField.setFont(new Font("Arial", Font.PLAIN, 20));
                                        nameField.setPreferredSize(new Dimension(200, 30));
                                        changePanel.add(nameField);

                                        // create age field with data from the table
                                        int accountAge = 0;
                                        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                                            PreparedStatement fetchAge = conn.prepareStatement("select age from acc_" + userLogin + " where account_name = ?");
                                            fetchAge.setString(1, finalAccountName);
                                            ResultSet rs = fetchAge.executeQuery();
                                            while (rs.next()){
                                                accountAge = rs.getInt("age");
                                            }
                                        } catch (SQLException e1){
                                            e1.printStackTrace();
                                        }
                                        JTextField ageField = new JTextField(String.valueOf(accountAge));
                                        ageField.setFont(new Font("Arial", Font.PLAIN, 20));
                                        ageField.setPreferredSize(new Dimension(200, 30));
                                        changePanel.add(ageField);
                                        // hint
                                        if (accountAge == 0){
                                            ageField.setText("Age");
                                            ageField.setForeground(Color.gray);
                                            ageField.addFocusListener(new FocusListener() {
                                                @Override
                                                public void focusGained(FocusEvent e) {
                                                    if(ageField.getText().equals("Age")){
                                                        ageField.setText("");
                                                        ageField.setForeground(Color.black);
                                                    }
                                                }

                                                @Override
                                                public void focusLost(FocusEvent e) {
                                                    if (ageField.getText().isEmpty()){
                                                        ageField.setForeground(Color.gray);
                                                        ageField.setText("Age");
                                                    }
                                                }
                                            });
                                        }

                                        // email field
                                        String accountEmail = null;
                                        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                                            PreparedStatement fetchEmail = conn.prepareStatement("select email from acc_" + userLogin + " where account_name = ?");
                                            fetchEmail.setString(1, finalAccountName);
                                            ResultSet rs = fetchEmail.executeQuery();
                                            if (rs.next()){
                                                accountEmail = rs.getString("email");
                                            }
                                        } catch (SQLException e1){
                                            e1.printStackTrace();
                                        }
                                        JTextField emailField = new JTextField(accountEmail);
                                        emailField.setFont(new Font("Arial", Font.PLAIN, 20));
                                        emailField.setPreferredSize(new Dimension(200, 30));
                                        changePanel.add(emailField);
                                        // hint
                                        if (accountEmail == null){
                                            emailField.setText("Email");
                                            emailField.setForeground(Color.gray);
                                            emailField.addFocusListener(new FocusListener() {
                                                @Override
                                                public void focusGained(FocusEvent e) {
                                                    if(emailField.getText().equals("Email")){
                                                        emailField.setText("");
                                                        emailField.setForeground(Color.black);
                                                    }
                                                }

                                                @Override
                                                public void focusLost(FocusEvent e) {
                                                    if (emailField.getText().isEmpty()){
                                                        emailField.setForeground(Color.gray);
                                                        emailField.setText("Email");
                                                    }
                                                }
                                            });
                                        }

                                        // phone number field
                                        String accountPhoneNumber = null;
                                        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                                            PreparedStatement fetchPhoneNumber = conn.prepareStatement("select phone_number from acc_" + userLogin + " where account_name = ?");
                                            fetchPhoneNumber.setString(1, finalAccountName);
                                            ResultSet rs = fetchPhoneNumber.executeQuery();
                                            if (rs.next()){
                                                accountPhoneNumber = rs.getString("phone_number");
                                            }
                                        } catch (SQLException e1){
                                            e1.printStackTrace();
                                        }
                                        JTextField phoneField = new JTextField(accountPhoneNumber);
                                        phoneField.setFont(new Font("Arial", Font.PLAIN, 20));
                                        phoneField.setPreferredSize(new Dimension(200, 30));
                                        changePanel.add(phoneField);
                                        // hint
                                        if (accountPhoneNumber == null){
                                            phoneField.setText("Phone Number");
                                            phoneField.setForeground(Color.gray);
                                            phoneField.addFocusListener(new FocusListener() {
                                                @Override
                                                public void focusGained(FocusEvent e) {
                                                    if(phoneField.getText().equals("Phone Number")){
                                                        phoneField.setText("");
                                                        phoneField.setForeground(Color.black);
                                                    }
                                                }

                                                @Override
                                                public void focusLost(FocusEvent e) {
                                                    if (phoneField.getText().isEmpty()){
                                                        phoneField.setForeground(Color.gray);
                                                        phoneField.setText("Phone Number");
                                                    }
                                                }
                                            });
                                        }

                                        // work id field
                                        String accountWorkID = null;
                                        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                                            PreparedStatement fetchWorkID = conn.prepareStatement("select workID from acc_" + userLogin + " where account_name = ?");
                                            fetchWorkID.setString(1, finalAccountName);
                                            ResultSet rs = fetchWorkID.executeQuery();
                                            if (rs.next()){
                                                accountWorkID = rs.getString("workID");
                                            }
                                        } catch (SQLException e1){
                                            e1.printStackTrace();
                                        }
                                        JTextField workIDField = new JTextField(accountWorkID);
                                        workIDField.setFont(new Font("Arial", Font.PLAIN, 20));
                                        workIDField.setPreferredSize(new Dimension(200, 30));
                                        changePanel.add(workIDField);
                                        // hint
                                        if (accountWorkID == null){
                                            workIDField.setText("Age");
                                            workIDField.setForeground(Color.gray);
                                            workIDField.addFocusListener(new FocusListener() {
                                                @Override
                                                public void focusGained(FocusEvent e) {
                                                    if(workIDField.getText().equals("Age")){
                                                        workIDField.setText("");
                                                        workIDField.setForeground(Color.black);
                                                    }
                                                }

                                                @Override
                                                public void focusLost(FocusEvent e) {
                                                    if (workIDField.getText().isEmpty()){
                                                        workIDField.setForeground(Color.gray);
                                                        workIDField.setText("Age");
                                                    }
                                                }
                                            });
                                        }

                                        // Panel for selected groups
                                        JPanel groupCheckBoxes = new JPanel();
                                        groupCheckBoxes.setLayout(new BoxLayout(groupCheckBoxes, BoxLayout.Y_AXIS));


                                        // fetch all groups from the table and groups selected by the user previously separately
                                        List<String> allGroups = new ArrayList<>();
                                        List<String> selectedGroups = new ArrayList<>();
                                        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                                            PreparedStatement fetchAllGroups = conn.prepareStatement("select name from gr_" + userLogin + " where group_id <> 1");
                                            ResultSet rs = fetchAllGroups.executeQuery();
                                            while (rs.next()){
                                                allGroups.add(rs.getString("name"));
                                            }

                                            // fetch group IDs to which the group is assigned
                                            PreparedStatement fetchGroupID = conn.prepareStatement("select group_id from acc_gr_junction_" + userLogin + " where account_id = ?");
                                            fetchGroupID.setInt(1, finalAccountID);
                                            ResultSet rs1 = fetchGroupID.executeQuery();
                                            while (rs1.next()){
                                                int selectedGroupID = rs1.getInt(1);

                                                // Now fetch the name of each selected group
                                                PreparedStatement fetchSelectedGroupName = conn.prepareStatement("select name from gr_" + userLogin + " where group_id = ?");
                                                fetchSelectedGroupName.setInt(1, selectedGroupID);
                                                ResultSet getName = fetchSelectedGroupName.executeQuery();
                                                // add it to selectedGroups array
                                                while(getName.next()){
                                                    selectedGroups.add(getName.getString("name"));
                                                }
                                            }
                                        } catch (SQLException e1){
                                            e1.printStackTrace();
                                        }
                                        // Now add check boxes with name of each group and tick ones that were selected
                                        for (String groupName : allGroups){
                                            JCheckBox groupCheckBox = new JCheckBox(groupName);
                                            if (selectedGroups.contains(groupName)){
                                                groupCheckBox.setSelected(true);
                                            }
                                            groupCheckBoxes.add(groupCheckBox);
                                        }
                                        groupCheckBoxes.revalidate();
                                        groupCheckBoxes.repaint();

                                        // frame
                                        JFrame changeAccountFrame = new JFrame("Change Account");
                                        changeAccountFrame.setLayout(new BorderLayout());
                                        changeAccountFrame.add(changePanel, BorderLayout.NORTH);

                                        // scroll pane for the check boxes
                                        JScrollPane scrollPane = new JScrollPane(groupCheckBoxes); // set the final check boxes panel to scroll pane
                                        changeAccountFrame.add(scrollPane, BorderLayout.CENTER);

                                        // Submit changes button
                                        JButton submitButton = new JButton("Submit Changes");
                                        submitButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
                                        submitButton.setFocusable(false);
                                        changeAccountFrame.add(submitButton, BorderLayout.SOUTH);

                                        // add action to the button
                                        submitButton.addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                // add ticked groups to Array List
                                                List<String> tickedGroupNames = new ArrayList<>();
                                                for (Component comp : groupCheckBoxes.getComponents()){
                                                    if (comp instanceof JCheckBox){
                                                        JCheckBox tickedCheckBox = (JCheckBox) comp;
                                                        if (tickedCheckBox.isSelected()){
                                                            tickedGroupNames.add(tickedCheckBox.getText());
                                                        }
                                                    }
                                                }

                                                String updatedNameAccount = nameField.getText().trim();
                                                String updatedAgeAccount = ageField.getText().trim();
                                                String updatedEmailAccount = emailField.getText().trim();
                                                String updatedPhoneNumberAccount = phoneField.getText().trim();
                                                String updatedWorkIDAccount = workIDField.getText().trim();

                                                // check if the name is empty
                                                if (!updatedNameAccount.isEmpty() && !updatedNameAccount.equals("Name")){
                                                    Integer age = null;
                                                    try {
                                                        // if age field is not empty parse age
                                                        if (!updatedAgeAccount.isEmpty() && !updatedAgeAccount.equals("Age")){
                                                            age = Integer.parseInt(updatedAgeAccount);
                                                        }
                                                        // update existing info of the account
                                                        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                                                            // check if account with updated name already exist in the table
                                                            try (PreparedStatement checkStmt = conn.prepareStatement("select count(*) from acc_" + userLogin + " where account_name = ? and account_id <> ?")){
                                                                checkStmt.setString(1, updatedNameAccount);
                                                                checkStmt.setInt(2, finalAccountID);
                                                                ResultSet rs = checkStmt.executeQuery();
                                                                if (rs.next() && rs.getInt(1) > 0){
                                                                    // account name already exists
                                                                    JOptionPane.showMessageDialog(null, "Such account name already exists");
                                                                } else {
                                                                    // update the project row with new data
                                                                    try (PreparedStatement updateAccount = conn.prepareStatement("update acc_" + userLogin + " set account_name = ?, age = ?, email = ?, phone_number = ?, workID = ?, changed_at = current_timestamp where account_id = ?")){
                                                                        updateAccount.setString(1, updatedNameAccount);
                                                                        if (age != null){
                                                                            updateAccount.setString(2, String.valueOf(age));
                                                                        } else {
                                                                            updateAccount.setNull(2, Types.INTEGER);
                                                                        }
                                                                        if (!updatedEmailAccount.isEmpty()){
                                                                            updateAccount.setString(3, updatedEmailAccount);
                                                                        } else {
                                                                            updateAccount.setNull(3, Types.VARCHAR);
                                                                        }
                                                                        if (!updatedPhoneNumberAccount.isEmpty()){
                                                                            updateAccount.setString(4, updatedPhoneNumberAccount);
                                                                        } else {
                                                                            updateAccount.setNull(4, Types.VARCHAR);
                                                                        }
                                                                        if (!updatedWorkIDAccount.isEmpty()){
                                                                            updateAccount.setString(5, updatedWorkIDAccount);
                                                                        } else {
                                                                            updateAccount.setNull(5, Types.VARCHAR);
                                                                        }
                                                                        updateAccount.setInt(6, finalAccountID);
                                                                        updateAccount.executeUpdate();

                                                                        // delete existing connections from the junction table
                                                                        try (PreparedStatement deleteJunctionConnections = conn.prepareStatement("delete from acc_gr_junction_" + userLogin + " where account_id = ?")){
                                                                            deleteJunctionConnections.setInt(1, finalAccountID);
                                                                            deleteJunctionConnections.executeUpdate();
                                                                        }
                                                                        // insert new ones (if no groups are selected, insert into Unassigned Accounts)
                                                                        if (!tickedGroupNames.isEmpty()) {
                                                                            for (String tickedGroupName : tickedGroupNames) {
                                                                                try (PreparedStatement updateJunctionConnections = conn.prepareStatement("insert into acc_gr_junction_" + userLogin + " values ((select group_id from gr_" + userLogin + " where name = ?), ?)")) {
                                                                                    updateJunctionConnections.setString(1, tickedGroupName);
                                                                                    updateJunctionConnections.setInt(2, finalAccountID);
                                                                                    updateJunctionConnections.executeUpdate();
                                                                                }
                                                                            }
                                                                        } else {
                                                                            try (PreparedStatement updateUnassignedAccounts = conn.prepareStatement("insert into acc_gr_junction_" + userLogin + " values (1, ?)")){
                                                                                updateUnassignedAccounts.setInt(1, finalAccountID);
                                                                                updateUnassignedAccounts.executeUpdate();
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } catch (SQLException e1){
                                                            e1.printStackTrace();
                                                        }
                                                    } catch (NumberFormatException ex) {
                                                        ex.printStackTrace();
                                                    }
                                                } else {
                                                    JOptionPane.showMessageDialog(null, "Error. Please re-enter the name.");
                                                }
                                                SwingUtilities.invokeLater(() ->{
                                                    accountButton.setText(nameField.getText().trim());
                                                    GUI.accountsColumn.revalidate();
                                                    GUI.accountsColumn.repaint();
                                                });
                                            }
                                        });
                                        changeAccountFrame.setPreferredSize(new Dimension(300, 400));
                                        changeAccountFrame.setMaximumSize(new Dimension(300, 1000));
                                        changeAccountFrame.setMinimumSize(new Dimension(250, 330));
                                        changeAccountFrame.pack();
                                        changeAccountFrame.setVisible(true);
                                        changeAccountFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                        changeAccountFrame.setLocationRelativeTo(null);
                                    }
                                });

                                // delete account button action
                                accountDeleteItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        // confirmation window
                                        int response = JOptionPane.showConfirmDialog(null, "Do you want to delete the account? Be aware that all the assigned projects and files will be automatically deleted!", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                                        if (response == JOptionPane.YES_OPTION){
                                            try(Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                                                // delete projects assigned to account
                                                PreparedStatement selectProjectsToDelete = conn.prepareStatement("select project_id from projects_junction_" + userLogin + " where account_id = ?");
                                                selectProjectsToDelete.setInt(1, finalAccountID1);
                                                ResultSet rs = selectProjectsToDelete.executeQuery();
                                                while (rs.next()){
                                                    // delete project with id assigned to account
                                                    int deletedProjectID = rs.getInt(1);
                                                    PreparedStatement deleteProjects = conn.prepareStatement("delete from projects_" + userLogin + " where project_id = ?");
                                                    deleteProjects.setInt(1, deletedProjectID);
                                                    deleteProjects.executeUpdate();
                                                }

                                                // delete files assigned to account
                                                PreparedStatement deleteFiles = conn.prepareStatement("delete from files_" + userLogin + " where account_id = ?");
                                                deleteFiles.setInt(1, finalAccountID1);
                                                deleteFiles.executeUpdate();

                                                //delete account itself
                                                PreparedStatement deleteAccount = conn.prepareStatement("delete from acc_" + userLogin + " where account_id = ?");
                                                deleteAccount.setInt(1, finalAccountID1);
                                                deleteAccount.executeUpdate();
                                                JOptionPane.showMessageDialog(null, "The account deleted successfully!");
                                            } catch (SQLException e1){
                                                e1.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });

                    buttonWrapper.add(accountButton, BorderLayout.CENTER);
                    buttonsPanel.add(buttonWrapper);

                }
                JPanel contentArea = new JPanel(new BorderLayout());
                contentArea.add(buttonsPanel, BorderLayout.NORTH);
                accountsPanel.add(contentArea, BorderLayout.CENTER);

                accountsPanel.repaint();
                accountsPanel.revalidate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static void createAccount(String userLogin) {
        SwingUtilities.invokeLater(() -> {
            JPanel textFieldsPanel = new JPanel();
            textFieldsPanel.setLayout(new BoxLayout(textFieldsPanel, BoxLayout.Y_AXIS));

            // create name field
            JTextField nameField = new JTextField("Name");
            //hint
            nameField.setForeground(Color.gray);
            nameField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (nameField.getText().equals("Name")) {
                        nameField.setText("");
                        nameField.setForeground(Color.BLACK);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (nameField.getText().isEmpty()) {
                        nameField.setForeground(Color.gray);
                        nameField.setText("Name");
                    }
                }
            });
            nameField.setFont(new Font("Arial", Font.PLAIN, 20));
            nameField.setPreferredSize(new Dimension(200, 30));
            textFieldsPanel.add(nameField);

            // create age field
            JTextField ageField = new JTextField("Age");
            // hint
            ageField.setForeground(Color.gray);
            ageField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (ageField.getText().equals("Age")) {
                        ageField.setText("");
                        ageField.setForeground(Color.BLACK);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (ageField.getText().isEmpty()) {
                        ageField.setForeground(Color.gray);
                        ageField.setText("Age");
                    }
                }
            });

            ageField.setFont(new Font("Arial", Font.PLAIN, 20));
            ageField.setPreferredSize(new Dimension(200, 30));
            textFieldsPanel.add(ageField);

            // create e-mail field
            JTextField emailField = new JTextField("E-mail");
            //hint
            emailField.setForeground(Color.gray);
            emailField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (emailField.getText().equals("E-mail")) {
                        emailField.setText("");
                        emailField.setForeground(Color.BLACK);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (emailField.getText().isEmpty()) {
                        emailField.setForeground(Color.gray);
                        emailField.setText("E-mail");
                    }
                }
            });
            emailField.setFont(new Font("Arial", Font.PLAIN, 20));
            emailField.setPreferredSize(new Dimension(200, 30));
            textFieldsPanel.add(emailField);

            // create phone_number field
            JTextField phoneField = new JTextField("Phone Number");
            //hint
            phoneField.setForeground(Color.gray);
            phoneField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (phoneField.getText().equals("Phone Number")) {
                        phoneField.setText("");
                        phoneField.setForeground(Color.BLACK);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (phoneField.getText().isEmpty()) {
                        phoneField.setForeground(Color.gray);
                        phoneField.setText("Phone Number");
                    }
                }
            });
            phoneField.setFont(new Font("Arial", Font.PLAIN, 20));
            phoneField.setPreferredSize(new Dimension(200, 30));
            textFieldsPanel.add(phoneField);

            //create work_id field
            JTextField workIDField = new JTextField("Work ID");
            //hint
            workIDField.setForeground(Color.gray);
            workIDField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (workIDField.getText().equals("Work ID")) {
                        workIDField.setText("");
                        workIDField.setForeground(Color.BLACK);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (workIDField.getText().isEmpty()) {
                        workIDField.setForeground(Color.gray);
                        workIDField.setText("Work ID");
                    }
                }
            });
            workIDField.setFont(new Font("Arial", Font.PLAIN, 20));
            workIDField.setPreferredSize(new Dimension(200, 30));
            textFieldsPanel.add(workIDField);

            //fetch group names from Groups table (only if they are not empty)
            List<String> groupNames = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                PreparedStatement prst = conn.prepareStatement("select name from gr_" + userLogin + " where group_id <> 1 order by group_id asc;"); // we exclude the table Unassigned Accounts for obvious reasons
                ResultSet rs = prst.executeQuery();

                while (rs.next()) {
                    groupNames.add(rs.getString("name"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // create UI elements for each group
            JPanel groupsPanel = new JPanel();
            groupsPanel.setLayout(new BoxLayout(groupsPanel, BoxLayout.Y_AXIS));

            for (String groupName : groupNames) {
                JPanel groupPanel = new JPanel(new BorderLayout());

                JCheckBox checkBox = new JCheckBox();
                groupPanel.add(checkBox, BorderLayout.WEST);

                JLabel groupLabel = new JLabel(groupName);
                groupPanel.add(groupLabel, BorderLayout.CENTER);

                groupsPanel.add(groupPanel);
            }

            groupsPanel.revalidate();
            groupsPanel.repaint();

            // frame

            JFrame createAccountFrame = new JFrame("Create Account");
            createAccountFrame.setLayout(new BorderLayout());
            createAccountFrame.add(textFieldsPanel, BorderLayout.NORTH);

            // create scroll pane for the list of the groups.

            JScrollPane scrollPane = new JScrollPane(groupsPanel);
            createAccountFrame.add(scrollPane, BorderLayout.CENTER);

            // create button

            JButton createButton = new JButton("Create Account");
            createButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
            createButton.setFocusable(false);
            createAccountFrame.add(createButton, BorderLayout.SOUTH);

            // add action to the button
            createButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<String> selectedGroups = new ArrayList<>(); //array for ticked groups names

                    // iterate through all the elements in groupsPanel
                    for (Component comp : groupsPanel.getComponents()) {
                        if (comp instanceof JPanel) {
                            JPanel groupPanel = (JPanel) comp;

                            // iterate through elements of the groupPanel
                            for (Component innerComp : groupPanel.getComponents()) {
                                if (innerComp instanceof JCheckBox) {
                                    JCheckBox checkBox = (JCheckBox) innerComp;

                                    if (checkBox.isSelected()) {
                                        int index = groupPanel.getComponentZOrder(checkBox) + 1;
                                        if (index < groupPanel.getComponentCount() && groupPanel.getComponent(index) instanceof JLabel) {
                                            JLabel label = (JLabel) groupPanel.getComponent(index);
                                            selectedGroups.add(label.getText());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // add the account into SQL table
                    //save the data from user input
                    String accountName = nameField.getText().trim();
                    String accountAge = ageField.getText().trim();
                    String accountEmail = emailField.getText().trim();
                    String accountPhone = phoneField.getText().trim();
                    String accountWorkID = workIDField.getText().trim();

                    // check if the name is filled
                    if (!accountName.equals("Name") && !accountName.isEmpty()) {
                        Integer age = null; // initialize age
                        try {
                            // only attempt to parse age if it's not the default hint or not empty
                            if (!accountAge.equals("Age") && !accountAge.isEmpty()) {
                                age = Integer.parseInt(accountAge); // it might throw an exception
                            }
                            // put the values into the account database
                            try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                                // check if such account already exists
                                try (PreparedStatement checkStmt = conn.prepareStatement("select count(*) from acc_" + userLogin + " where account_name = ?")) {
                                    checkStmt.setString(1, accountName);
                                    ResultSet rs = checkStmt.executeQuery();
                                    if (rs.next() && rs.getInt(1) > 0) {
                                        // account name already exists
                                        JOptionPane.showMessageDialog(null, "Such account name already exists. Please choose a different one.");
                                    } else {
                                        try (PreparedStatement prst = conn.prepareStatement("insert into acc_" + userLogin + " (account_name, age, email, phone_number, workID) values (?, ?, ?, ?, ?)")) {
                                            prst.setString(1, accountName);
                                            if (age != null) {                                            // insert age if exists
                                                prst.setString(2, String.valueOf(age));
                                            } else {
                                                prst.setNull(2, Types.INTEGER);
                                            }
                                            if (!accountEmail.equals("E-mail") && !accountEmail.isEmpty()) {      // insert email if exists
                                                prst.setString(3, accountEmail);
                                            } else {
                                                prst.setNull(3, Types.VARCHAR);
                                            }
                                            if (!accountPhone.equals("Phone Number") && !accountPhone.isEmpty()) { // insert phone number if exists
                                                prst.setString(4, accountPhone);
                                            } else {
                                                prst.setNull(4, Types.VARCHAR);
                                            }
                                            if (!accountWorkID.equals("Work ID") && !accountWorkID.isEmpty()) { // insert work id if exists
                                                prst.setString(5, accountWorkID);
                                            } else {
                                                prst.setNull(5, Types.VARCHAR);
                                            }

                                            prst.executeUpdate();

                                        } catch (SQLException exception) {
                                            exception.printStackTrace();
                                            JOptionPane.showMessageDialog(null, "Error inserting data.");
                                        }
                                    }
                                } catch (SQLException e9) {
                                    e9.printStackTrace();
                                }
                            } catch (SQLException exception) {
                                exception.printStackTrace();
                            }
                            // add the relation of the account and the group into the junction table
                            // If no groups were selected, put into Unassigned Accounts
                            if (!selectedGroups.isEmpty()) {
                                for (String selectedGroup : selectedGroups) {
                                    Account.connectAccountGroup(selectedGroup, accountName, userLogin);
                                }
                            } else {
                                Account.connectAccountGroup("Unassigned Accounts", accountName, userLogin);
                            }

                            JOptionPane.showMessageDialog(null, "Account created successfully.");

                        } catch (NumberFormatException exception) {
                            JOptionPane.showMessageDialog(null, "Invalid age format.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Please fill in the account name.");
                    }

                    // reload the panel with groupsGUI.groupsColumn.revalidate();
                    Group.loadGroupsFromDatabase(userLogin, "group_id asc");

                }
            });

            createAccountFrame.setPreferredSize(new Dimension(300, 400));
            createAccountFrame.setMaximumSize(new Dimension(300, 1000));
            createAccountFrame.setMinimumSize(new Dimension(250, 330));
            createAccountFrame.pack();
            createAccountFrame.setVisible(true);
            createAccountFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            createAccountFrame.setLocationRelativeTo(null);

            Group.loadGroupsFromDatabase(userLogin, "group_id asc");
        });
    }


    public static void connectAccountGroup(String groupName, String accName, String userLogin) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
            PreparedStatement preparedStatement = conn.prepareStatement("select group_id from gr_" + userLogin + " where name = ?;");
            preparedStatement.setString(1, groupName);

            ResultSet rs = preparedStatement.executeQuery();
            int groupID = 0;
            while (rs.next()) {
                groupID = rs.getInt("group_id"); //find the index of the group
            }

            PreparedStatement preparedStatement1 = conn.prepareStatement("select account_id from acc_" + userLogin + " where account_name = ?;");
            preparedStatement1.setString(1, accName);

            ResultSet rs1 = preparedStatement1.executeQuery();
            int accountID = 0;
            while (rs1.next()) {
                accountID = rs1.getInt("account_id");
            }
            // check if the connection already exists
            PreparedStatement checkStmt = conn.prepareStatement("select count(*) from acc_gr_junction_" + userLogin + " where group_id = ? and account_id = ?;");
            checkStmt.setInt(1, groupID);
            checkStmt.setInt(2, accountID);
            ResultSet rsCheck = checkStmt.executeQuery();
            if (rsCheck.next() && rsCheck.getInt(1) == 0) {
                // connection doesn't exist, proceed with insertion
                PreparedStatement insertStmt = conn.prepareStatement("insert into acc_gr_junction_" + userLogin + " (group_id, account_id) values (?, ?)");
                insertStmt.setInt(1, groupID);
                insertStmt.setInt(2, accountID);
                insertStmt.executeUpdate();
            } else {
                JOptionPane.showMessageDialog(null, "Some groups and accounts are already linked to each other. Please recheck.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}