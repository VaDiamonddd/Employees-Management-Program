import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Group {
    public static String selectedGrSortingMethod;
    private static String getSafeSortMethod(String userInput){
        switch (userInput) {
            case "name asc": return "name ASC";
            case "name desc": return "name DESC";
            case "group_id asc": return "group_id ASC";
            case "group_id desc": return "group_id DESC";
            case "changed_at asc": return "changed_at ASC";
            case "changed_at desc": return "changed_at DESC";
            default: return "name ASC"; // Default sorting method
        }
    }

    public static void loadGroupsFromDatabase(String loginUser, String sortingMethod) {

        // assign value to the selectedSortingMethod for further implementation
        selectedGrSortingMethod = sortingMethod;
        //use it to invoke long-lasting operations linked with both GUI and other processes safely on EDT (thread for GUI operations)
        SwingUtilities.invokeLater(() -> {
            String[] groupsColumnSortingOptions = {"Sort..", "Alphabet A-Z", "Alphabet Z-A", "Creation Date (Older)", "Creation Date (Newer)", "Date of Change (Older)", "Date of Change (Newer)"};
            JComboBox<String> groupsColumnSortingDropdown = new JComboBox<>(groupsColumnSortingOptions);
            groupsColumnSortingDropdown.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // sorting logic
                    String selectedGroupSortingOption = (String) groupsColumnSortingDropdown.getSelectedItem();

                    // assign actions to different items
                    switch (Objects.requireNonNull(selectedGroupSortingOption)){
                        case "Sort..":
                            break;
                        case "Alphabet A-Z":
                            GUI.groupsColumn.removeAll();
                            loadGroupsFromDatabase(loginUser, "name asc");
                            GUI.groupsColumn.revalidate();
                            GUI.groupsColumn.repaint();
                            GUI.accountsColumn.removeAll();
                            GUI.accountsColumn.revalidate();
                            GUI.accountsColumn.repaint();
                            break;
                        case "Alphabet Z-A":
                            GUI.groupsColumn.removeAll();
                            loadGroupsFromDatabase(loginUser, "name desc");
                            GUI.groupsColumn.revalidate();
                            GUI.groupsColumn.repaint();
                            GUI.accountsColumn.removeAll();
                            GUI.accountsColumn.revalidate();
                            GUI.accountsColumn.repaint();
                            break;
                        case "Creation Date (Older)":
                            GUI.groupsColumn.removeAll();
                            loadGroupsFromDatabase(loginUser, "group_id asc");
                            GUI.groupsColumn.revalidate();
                            GUI.groupsColumn.repaint();
                            GUI.accountsColumn.removeAll();
                            GUI.accountsColumn.revalidate();
                            GUI.accountsColumn.repaint();
                            break;
                        case "Creation Date (Newer)":
                            GUI.groupsColumn.removeAll();
                            loadGroupsFromDatabase(loginUser, "group_id desc");
                            GUI.groupsColumn.revalidate();
                            GUI.groupsColumn.repaint();
                            GUI.accountsColumn.removeAll();
                            GUI.accountsColumn.revalidate();
                            GUI.accountsColumn.repaint();
                            break;
                        case "Date of Change (Older)":
                            GUI.groupsColumn.removeAll();
                            loadGroupsFromDatabase(loginUser, "changed_at asc");
                            GUI.groupsColumn.revalidate();
                            GUI.groupsColumn.repaint();
                            GUI.accountsColumn.removeAll();
                            GUI.accountsColumn.revalidate();
                            GUI.accountsColumn.repaint();
                            break;
                        case "Date of Change (Newer)":
                            GUI.groupsColumn.removeAll();
                            loadGroupsFromDatabase(loginUser, "changed_at desc");
                            GUI.groupsColumn.revalidate();
                            GUI.groupsColumn.repaint();
                            GUI.accountsColumn.removeAll();
                            GUI.accountsColumn.revalidate();
                            GUI.accountsColumn.repaint();
                            break;
                    }
                }
            });

            // add dropdown at the top of mainColumn
            GUI.groupsColumn.add(groupsColumnSortingDropdown, BorderLayout.NORTH);

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                // display buttons according to the selected sorting method
                String baseQuery = "select name from gr_" + loginUser + " where group_id <> 1 ";
                String safeSortMethod = getSafeSortMethod(sortingMethod);
                String fullQuery = baseQuery + "order by " + safeSortMethod;
                PreparedStatement pst = conn.prepareStatement(fullQuery);
                ResultSet rs = pst.executeQuery();

                // button for unassigned accounts
                JButton unassignedAccountsButton = new JButton("Unassigned Accounts");
                unassignedAccountsButton.setFocusable(false);
                unassignedAccountsButton.setPreferredSize(new Dimension(1, 40));
                // assign action to the unassigned accounts
                unassignedAccountsButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Account.getAccountsByGroup(loginUser, "Unassigned Accounts", GUI.accountsColumn, Account.selectedAccSortingMethod);
                    }
                });


                JPanel restButtonsPanel = new JPanel();
                restButtonsPanel.setLayout(new BoxLayout(restButtonsPanel, BoxLayout.Y_AXIS));
                while (rs.next()) {
                    String groupName = rs.getString("name");

                    JPanel buttonWrapper = new JPanel(new BorderLayout());
                    JButton groupButton = new JButton(groupName);
                    groupButton.setFocusable(false);
                    groupButton.setPreferredSize(new Dimension(1, 40));

                    // Assign action to the button
                    groupButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Account.getAccountsByGroup(loginUser, groupName, GUI.accountsColumn, Account.selectedAccSortingMethod);
                        }
                    });
                    groupButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                // create dropdown list
                                JPopupMenu groupsDropDown = new JPopupMenu();
                                // create items and add them to the list
                                JMenuItem groupChangeItem = new JMenuItem("Change");
                                JMenuItem groupDeleteItem = new JMenuItem("Delete");

                                groupsDropDown.add(groupChangeItem);
                                groupsDropDown.add(groupDeleteItem);

                                groupsDropDown.show(e.getComponent(), e.getX(), e.getY());
                                // assign actions to the items
                                groupChangeItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        changeGroup(loginUser, groupName, groupButton);
                                    }
                                });
                                groupDeleteItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        // confirmation window
                                        int response = JOptionPane.showConfirmDialog(null, "Are you sure that you want to delete the group?" +
                                                " Certain accounts will be forcefully moved to 'Unassigned Accounts'.", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                                        if (response == JOptionPane.YES_OPTION){
                                            try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                                                // get ids of all accounts assigned to deleted group
                                                PreparedStatement fetchMovedAccountsIDs = conn.prepareStatement("select account_id from acc_gr_junction_" + loginUser
                                                        + " where group_id = (select group_id from gr_" + loginUser + " where name = ?)");
                                                fetchMovedAccountsIDs.setString(1, groupName);
                                                ResultSet rs = fetchMovedAccountsIDs.executeQuery();
                                                while (rs.next()){
                                                    int movedAccountID = rs.getInt(1);

                                                    // if the account is assigned to only the deleted group, move to the unassigned
                                                    PreparedStatement countAssignedGroups = conn.prepareStatement("select count(*) from acc_gr_junction_" + loginUser + " where account_id = ?");
                                                    countAssignedGroups.setInt(1, movedAccountID);
                                                    ResultSet resultSet = countAssignedGroups.executeQuery();
                                                    if (resultSet.next() && resultSet.getInt(1) == 1){
                                                        // assign the moved account to unassigned accounts
                                                        PreparedStatement moveToUnassignedAccounts = conn.prepareStatement("insert into acc_gr_junction_" + loginUser + " values (1, ?)");
                                                        moveToUnassignedAccounts.setInt(1, movedAccountID);
                                                        moveToUnassignedAccounts.executeUpdate();
                                                    }
                                                    // delete existing connections
                                                    PreparedStatement deleteExistingConnections = conn.prepareStatement("delete from acc_gr_junction_" + loginUser
                                                            + " where group_id = (select group_id from gr_" + loginUser + " where name = ?) and account_id = ?");
                                                    deleteExistingConnections.setString(1, groupName);
                                                    deleteExistingConnections.setInt(2, movedAccountID);
                                                    deleteExistingConnections.executeUpdate();
                                                }
                                                // delete the group itself
                                                PreparedStatement deleteGroup = conn.prepareStatement("delete from gr_" + loginUser + " where name = ?");
                                                deleteGroup.setString(1, groupName);
                                                deleteGroup.executeUpdate();

                                                // revalidate the groups column
                                                SwingUtilities.invokeLater(() -> {
                                                    GUI.groupsColumn.removeAll();
                                                    loadGroupsFromDatabase(loginUser, sortingMethod);
                                                    GUI.groupsColumn.revalidate();
                                                    GUI.groupsColumn.repaint();
                                                    GUI.accountsColumn.removeAll();
                                                    GUI.accountsColumn.revalidate();
                                                    GUI.accountsColumn.repaint();
                                                });
                                            } catch (SQLException e1){
                                                e1.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });

                    buttonWrapper.add(groupButton, BorderLayout.CENTER);

                    restButtonsPanel.add(buttonWrapper);

                }
                JPanel dummyPanel = new JPanel(new BorderLayout()); // create it to avoid conflict with dropdown list
                dummyPanel.add(restButtonsPanel, BorderLayout.NORTH);

                // panel for all the buttons including Unassigned Accounts
                JPanel buttonsPanelWithUnassigned = new JPanel(new BorderLayout());
                buttonsPanelWithUnassigned.add(dummyPanel, BorderLayout.CENTER);
                buttonsPanelWithUnassigned.add(unassignedAccountsButton, BorderLayout.NORTH);

                GUI.groupsColumn.add(buttonsPanelWithUnassigned, BorderLayout.CENTER);

                GUI.groupsColumn.revalidate();
                GUI.groupsColumn.repaint();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static void createGroup(String loginUser) {
        SwingUtilities.invokeLater(() -> {
            JTextField nameField = new JTextField("Name");
            // hint
            nameField.setForeground(Color.gray);
            nameField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (nameField.getText().equals("Name")) {
                        nameField.setText("");
                        nameField.setForeground(Color.black);
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
            nameField.setPreferredSize(new Dimension(200, 40));

            // parse sql table for accounts' names
            List<String> accountNames = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                PreparedStatement preparedStatement = conn.prepareStatement("SELECT account_name FROM acc_" + loginUser + " ORDER BY account_id ASC;");

                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    String accountName = rs.getString("account_name");
                    accountNames.add(accountName);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // add GUI elements using accountNames
            JPanel accountsPanel = new JPanel();
            accountsPanel.setLayout(new BoxLayout(accountsPanel, BoxLayout.Y_AXIS));

            for (String accountName : accountNames) {
                JPanel accountPanel = new JPanel(new BorderLayout());

                JCheckBox checkBox = new JCheckBox();
                accountPanel.add(checkBox, BorderLayout.WEST);

                JLabel label = new JLabel(accountName);
                accountPanel.add(label, BorderLayout.CENTER);

                accountsPanel.add(accountPanel);
            }
            accountsPanel.revalidate();
            accountsPanel.repaint();

            // scroll pane for accounts
            JScrollPane scrollPane = new JScrollPane(accountsPanel);

            // createButton
            JButton createButton = new JButton("Create Group");
            createButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
            createButton.setFocusable(false);

            // assign action to the button
            createButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    GUI.groupsColumn.removeAll();
                    String groupName = nameField.getText().trim(); // value for the text of nameField
                    // proceed only if group's name is not empty or default
                    if (!groupName.equals("Name") && !groupName.isEmpty()) {

                        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {

                            // Check if the group already exists
                            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM gr_" + loginUser + " WHERE name = ?;");
                            checkStmt.setString(1, groupName);
                            ResultSet rsCheck = checkStmt.executeQuery();

                            if (rsCheck.next() && rsCheck.getInt(1) == 0) {
                                // Group does not exist, proceed with insertion
                                PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO gr_" + loginUser + " (name) VALUES (?);");
                                preparedStatement.setString(1, groupName);
                                preparedStatement.executeUpdate();

                                // Collect selected accounts
                                List<String> selectedAccounts = new ArrayList<>();

                                //iterate through all the elements
                                for (Component comp : accountsPanel.getComponents()) {

                                    if (comp instanceof JPanel) {
                                        JPanel accountPanel = (JPanel) comp;
                                        for (Component innerComp : accountPanel.getComponents()) {

                                            if (innerComp instanceof JCheckBox) {
                                                JCheckBox checkBox = (JCheckBox) innerComp;
                                                if (checkBox.isSelected()) {

                                                    int index = accountPanel.getComponentZOrder(checkBox) + 1;
                                                    if (index < accountPanel.getComponentCount() && accountPanel.getComponent(index) instanceof JLabel) {
                                                        JLabel label = (JLabel) accountPanel.getComponent(index);
                                                        selectedAccounts.add(label.getText());          // add the selected names into the list
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                // Insert connections into junction table
                                for (String selectedAccount : selectedAccounts) {
                                    Account.connectAccountGroup(groupName, selectedAccount, loginUser);

                                    // if the account was previously in Unassigned Groups, it's automatically removed from there
                                    try(PreparedStatement checkUnassigned = conn.prepareStatement("select group_id from gr_" + loginUser +
                                            " where name = 'Unassigned Accounts'")){
                                        ResultSet rs = checkUnassigned.executeQuery();
                                        if (rs.next()){
                                            int unassignedGroupId = rs.getInt("group_id");

                                            // check if the account is in unassigned accounts
                                            try(PreparedStatement checkAccountInUnassigned = conn.prepareStatement("select count(*) from acc_gr_junction_"
                                                    + loginUser + " where group_id = ? and account_id = (select account_id from acc_" + loginUser +
                                                    " where account_name = ?);")){
                                                checkAccountInUnassigned.setInt(1, unassignedGroupId);
                                                checkAccountInUnassigned.setString(2, selectedAccount);
                                                ResultSet resultSet = checkAccountInUnassigned.executeQuery();

                                                //if yes, delete from that group
                                                if(resultSet.next() && resultSet.getInt(1) > 0){
                                                    try (PreparedStatement removeFromUnassigned = conn.prepareStatement("delete from acc_gr_junction_" + loginUser +
                                                            " where group_id = ? and account_id = (select account_id from acc_" + loginUser + " where account_name = ?)")){
                                                        removeFromUnassigned.setInt(1, unassignedGroupId);
                                                        removeFromUnassigned.setString(2, selectedAccount);
                                                        removeFromUnassigned.executeUpdate();
                                                    }
                                                }
                                            }
                                        }
                                    } catch (SQLException exception){
                                        exception.printStackTrace();
                                        JOptionPane.showMessageDialog(null, "Error updating group membership.");
                                    }
                                }
                            } else {
                                // Group already exists
                                JOptionPane.showMessageDialog(null, "Group with this name already exists.");
                            }
                        } catch (SQLException e1) {
                            JOptionPane.showMessageDialog(null, "Error inserting data");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Please enter the name of the group.");
                    }
                    loadGroupsFromDatabase(loginUser, selectedGrSortingMethod);
                    JOptionPane.showMessageDialog(null, "Group created successfully.");
                }
            });

            JFrame frame = new JFrame("Create Group");
            frame.setLayout(new BorderLayout());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
            frame.add(nameField, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(createButton, BorderLayout.SOUTH);
            frame.setPreferredSize(new Dimension(300, 350));
            frame.pack();
            frame.setLocationRelativeTo(null);
        });
    }
    public static void changeGroup(String loginUser, String certainGroupName, JButton currentGroupButton){
        SwingUtilities.invokeLater(() -> {
            // declare the frame
            JFrame changeGroupFrame = new JFrame("Change Group");
            changeGroupFrame.setLayout(new BorderLayout());

            // create text field for group's name
            JTextField nameField = new JTextField(certainGroupName);
            nameField.setFont(new Font("Arial", Font.PLAIN, 20));
            nameField.setPreferredSize(new Dimension(200, 30));
            changeGroupFrame.add(nameField, BorderLayout.NORTH);

            // Panel for assigned accounts
            JPanel accountsPanel = new JPanel();
            accountsPanel.setLayout(new BoxLayout(accountsPanel, BoxLayout.Y_AXIS));
            changeGroupFrame.add(accountsPanel, BorderLayout.CENTER);

            // fetch all the account names from db and the ones that the user previously selected
            List<String> allAccountNames = new ArrayList<>();
            List<String> previouslySelectedAccountNames = new ArrayList<>();
            int groupID = 0;
            try(Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                PreparedStatement fetchAllAccountNames = conn.prepareStatement("select account_name from acc_" + loginUser + " order by account_id asc");
                ResultSet rs = fetchAllAccountNames.executeQuery();
                while (rs.next()){
                    allAccountNames.add(rs.getString("account_name"));
                }

                // get group id
                PreparedStatement fetchGroupId = conn.prepareStatement("select group_id from gr_" + loginUser + " where name = ?");
                fetchGroupId.setString(1, certainGroupName);
                ResultSet rs1 = fetchGroupId.executeQuery();
                while(rs1.next()){
                    groupID = rs1.getInt(1);
                    // get IDs of accounts assigned to the group
                    PreparedStatement fetchAccountIDs = conn.prepareStatement("select account_id from acc_gr_junction_" + loginUser + " where group_id = ?");
                    fetchAccountIDs.setInt(1, groupID);
                    ResultSet rs2 = fetchAccountIDs.executeQuery();
                    while(rs2.next()){
                        int accountID = rs2.getInt(1);
                        // now get the names of selected accounts and add them to the array
                        PreparedStatement fetchAccountNames = conn.prepareStatement("select account_name from acc_" + loginUser + " where account_id = ?");
                        fetchAccountNames.setInt(1, accountID);
                        ResultSet finalRS = fetchAccountNames.executeQuery();
                        while(finalRS.next()){
                            previouslySelectedAccountNames.add(finalRS.getString("account_name"));
                        }
                    }
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
            // Now create check boxes for account names and tick selected ones
            for (String accountName : allAccountNames){
                JCheckBox accountCheckBox = new JCheckBox(accountName);
                if (previouslySelectedAccountNames.contains(accountName)){
                    accountCheckBox.setSelected(true);
                }
                accountsPanel.add(accountCheckBox);
            }
            // create Submit Changes button
            JButton submitButton = new JButton("Submit Changes");
            submitButton.setPreferredSize(new Dimension(20, 50));
            submitButton.setFocusable(false);
            changeGroupFrame.add(submitButton, BorderLayout.SOUTH);

            // add action to the button
            int finalGroupID = groupID; // final group id
            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // proceed if user entered group name
                    if (!nameField.getText().isEmpty()) {
                        // save the new name of group for further substitution
                        String userNewGroupName = nameField.getText();

                        // initialize array list for selected accounts and insert there their names
                        List<String> selectedAccountNames = new ArrayList<>();
                        for (Component comp : accountsPanel.getComponents()) {
                            if (comp instanceof JCheckBox) {
                                JCheckBox checkBox = (JCheckBox) comp;
                                if (checkBox.isSelected()) {
                                    selectedAccountNames.add(checkBox.getText());
                                }
                            }
                        }

                        // change group name
                        try(Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                            try(PreparedStatement changeGroupName = conn.prepareStatement("update gr_" + loginUser + " set name = ? where group_id = ?")){
                                changeGroupName.setString(1, nameField.getText().trim());
                                changeGroupName.setInt(2, finalGroupID);
                                changeGroupName.executeUpdate();
                            }
                        } catch (SQLException e1){
                            e1.printStackTrace();
                        }

                        // clear existing connections for the group and insert new ones. Also add change change_at
                        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                            try (PreparedStatement deleteExistingConnections = conn.prepareStatement("delete from acc_gr_junction_" + loginUser + " where group_id = ?")) {
                                deleteExistingConnections.setInt(1, finalGroupID);
                                deleteExistingConnections.executeUpdate();
                            }
                            // get IDs for selected accounts
                            for (String selectedAccountName : selectedAccountNames) {
                                PreparedStatement fetchAccountID = conn.prepareStatement("select account_id from acc_" + loginUser + " where account_name = ?");
                                fetchAccountID.setString(1, selectedAccountName);
                                ResultSet rs = fetchAccountID.executeQuery();
                                while (rs.next()) {
                                    int selectedAccountID = rs.getInt(1);

                                    // check if selected account was in unassigned groups.
                                    PreparedStatement countUnassigned = conn.prepareStatement("select count(*) from acc_gr_junction_" + loginUser
                                            + " where account_id = ? and group_id = ?");
                                    countUnassigned.setInt(1, selectedAccountID);
                                    countUnassigned.setInt(2, 1); // 1 - id of unassigned accounts group
                                    ResultSet rs1 = countUnassigned.executeQuery();
                                    if (rs1.next() && rs1.getInt(1) > 0){ //
                                        PreparedStatement deleteFromUnassigned = conn.prepareStatement("delete from acc_gr_junction_" + loginUser
                                                + " where account_id = ? and group_id = ?");
                                        deleteFromUnassigned.setInt(1, selectedAccountID);
                                        deleteFromUnassigned.setInt(2, 1);
                                        deleteFromUnassigned.executeUpdate();
                                    }

                                    // insert values into junction table
                                    PreparedStatement insertAccGrJunction = conn.prepareStatement("insert into acc_gr_junction_" + loginUser + " values (?, ?)");
                                    insertAccGrJunction.setInt(1, finalGroupID);
                                    insertAccGrJunction.setInt(2, selectedAccountID);
                                    insertAccGrJunction.executeUpdate();
                                }
                            }
                            // change changed_at
                            try (PreparedStatement changeTime = conn.prepareStatement("update gr_" + loginUser + " set changed_at = current_timestamp where group_id = ?")){
                                changeTime.setInt(1, finalGroupID);
                                changeTime.executeUpdate();
                            }
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                        // change the button name according to user input
                        SwingUtilities.invokeLater(() -> {
                            currentGroupButton.setText(userNewGroupName);
                            GUI.groupsColumn.revalidate();
                            GUI.groupsColumn.repaint();
                        });
                    } else {
                        JOptionPane.showMessageDialog(null, "Type group name!");
                    }

                }
            });
            changeGroupFrame.setPreferredSize(new Dimension(300, 400));
            changeGroupFrame.setMaximumSize(new Dimension(new Dimension(300, 1000)));
            changeGroupFrame.setMinimumSize(new Dimension(250, 330));
            changeGroupFrame.pack();
            changeGroupFrame.setVisible(true);
            changeGroupFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            changeGroupFrame.setLocationRelativeTo(null);
        });
    }
}