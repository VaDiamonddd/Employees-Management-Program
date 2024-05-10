import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.xml.parsers.SAXParser;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;

public class Projects_Files {
    //private static JTextField nameField;
    private static JTextField descriptionField;
    private static JPanel subGroupsPanel;
    private static JPanel subAccountsPanel;
    private static String getSafeProjSortMethod(String userInput){
        if (userInput == null){
            return "proj.project_id asc";
        }
        switch (userInput){
            case "Sort..": return "proj.account_id ASC";
            case "project_name asc": return "proj.project_name ASC";
            case "project_name desc": return "proj.project_name DESC";
            case "project_id asc": return "proj.project_id ASC";
            case "project_id desc": return "proj.project_id DESC";
            case "changed_at asc": return "proj.changed_at ASC";
            case "changed_at desc": return "proj.changed_at DESC";
            case "deadline closer": return "proj.deadline asc";
            case "deadline farther": return "proj.deadline desc";
            default: return "proj.project_name ASC";
        }
    }
    private static String getSafeFilesSortMethod(String userInput){
        if (userInput == null){
            return "file_id asc";
        }
        switch (userInput){
            case "Sort..": return "file_id asc";
            case "file_id asc": return "file_id ASC";
            case "file_id desc": return "file_id desc";
            case "file_name asc": return "file_name asc";
            case "file_name desc": return "file_name desc";
            case "last_change asc": return "last_change asc";
            case "last_change desc": return "last_change desc";
            default: return "file_id asc";
        }
    }

    public static void getProjectsFilesByAccount(String userLogin, String accountName, JPanel projectsPanel, JPanel filesPanel, String projectsSortingOption){
        SwingUtilities.invokeLater(() -> {

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                // clear the Project panel
                projectsPanel.removeAll();
                projectsPanel.setLayout(new BorderLayout());
                projectsPanel.revalidate();
                projectsPanel.repaint();

                // create dropdown list
                String[] projectsSortingOptions = {"Sort..", "Alphabet A-Z", "Alphabet Z-A", "Deadline Closer", "Deadline Farther", "Creation Date (Older)", "Creation Date (Newer)", "Date of Change (Older)", "Date of Change (Newer)"};
                JComboBox<String> projectsSortingDropdown = new JComboBox<>(projectsSortingOptions);
                projectsSortingDropdown.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Sorting logic
                        String selectedProjectSortingOption = (String) projectsSortingDropdown.getSelectedItem();
                        // assign actions to the itens of the dropdown
                        switch (Objects.requireNonNull(selectedProjectSortingOption)){
                            case "Sort..": break;
                            case "Alphabet A-Z":
                                GUI.subProjectsPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "project_name asc");
                                GUI.subProjectsPanel.revalidate();
                                GUI.subProjectsPanel.repaint();
                                break;
                            case "Alphabet Z-A":
                                GUI.subProjectsPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "project_name desc");
                                GUI.subProjectsPanel.revalidate();
                                GUI.subProjectsPanel.repaint();
                                break;
                            case "Deadline Closer":
                                GUI.subProjectsPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "deadline closer");
                                GUI.subProjectsPanel.revalidate();
                                GUI.subProjectsPanel.repaint();
                                break;
                            case "Deadline Farther":
                                GUI.subProjectsPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "deadline farther");
                                GUI.subProjectsPanel.revalidate();
                                GUI.subProjectsPanel.repaint();
                                break;
                            case "Creation Date (Older)":
                                GUI.subProjectsPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "project_id asc");
                                GUI.subProjectsPanel.revalidate();
                                GUI.subProjectsPanel.repaint();
                                break;
                            case "Creation Date (Newer)":
                                GUI.subProjectsPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "project_id desc");
                                GUI.subProjectsPanel.revalidate();
                                GUI.subProjectsPanel.repaint();
                                break;
                            case "Date of Change (Older)":
                                GUI.subProjectsPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "changed_at asc");
                                GUI.subProjectsPanel.revalidate();
                                GUI.subProjectsPanel.repaint();
                                break;
                            case "Date of Change (Newer)":
                                GUI.subProjectsPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "changed_at desc");
                                GUI.subProjectsPanel.revalidate();
                                GUI.subProjectsPanel.repaint();
                                break;
                        }
                    }
                });
                // Top panel for dropdown in Projects
                JPanel projectsTopPanel = new JPanel(new BorderLayout());
                projectsTopPanel.add(projectsSortingDropdown, BorderLayout.CENTER);

                // add to the main Projects Panel
                projectsPanel.add(projectsTopPanel, BorderLayout.NORTH);

                // panel for future buttons
                JPanel projectsButtonsPanel = new JPanel();
                projectsButtonsPanel.setLayout(new BoxLayout(projectsButtonsPanel, BoxLayout.Y_AXIS));

                // get account id from accounts table
                PreparedStatement preparedStatement = conn.prepareStatement("select account_id from acc_" + userLogin + " where account_name = ?");
                preparedStatement.setString(1, accountName);
                ResultSet rs = preparedStatement.executeQuery();
                int accountID = 0;
                while(rs.next()){
                    accountID = rs.getInt("account_id"); // get account id
                }
                // select the assigned project names with respect to the sorting method. Then, display the buttons
                String baseQuery = "select project_name from projects_" + userLogin + " as proj inner join projects_junction_" + userLogin + " as proj_j on proj.project_id = proj_j.project_id where proj_j.account_id = ? ";
                String safeSortingMethod = getSafeProjSortMethod(projectsSortingOption);
                String fullQuery = baseQuery + "order by " + safeSortingMethod;
                PreparedStatement fetchSortedProjects = conn.prepareStatement(fullQuery);
                fetchSortedProjects.setInt(1, accountID);
                ResultSet resultSet = fetchSortedProjects.executeQuery();
                while (resultSet.next()) {
                    String projectName = resultSet.getString("project_name");

                    // add buttons to wrapperPanel
                    JPanel projectButtonWrapper = new JPanel(new BorderLayout());
                    JButton projectButton = new JButton(projectName);
                    projectButton.setFocusable(false);
                    projectButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));

                    projectButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                // create a popup menu for the options
                                JPopupMenu projectPopupMenu = new JPopupMenu();
                                JMenuItem projectChangeItem = new JMenuItem("Change");
                                JMenuItem projectDeleteItem = new JMenuItem("Delete");

                                // add menu item
                                projectPopupMenu.add(projectChangeItem);
                                projectPopupMenu.add(projectDeleteItem);

                                projectPopupMenu.show(e.getComponent(), e.getX(), e.getY());

                                // assign actions to the buttons
                                projectChangeItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        changeProject(userLogin, projectName, projectButton);
                                    }
                                });

                                // delete project button
                                projectDeleteItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        // confirmation window
                                        int response = JOptionPane.showConfirmDialog(null, "Do you want to delete the project? It will be deleted from all the accounts", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                                        if (response == JOptionPane.YES_OPTION){
                                            try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                                                PreparedStatement deleteProject = conn.prepareStatement("delete from projects_" + userLogin + " where project_name = ?");
                                                deleteProject.setString(1, projectName);
                                                deleteProject.executeUpdate();

                                            } catch (SQLException e1){
                                                e1.printStackTrace();
                                            }
                                        }
                                        // revalidate the windows
                                        GUI.subProjectsPanel.removeAll();
                                        GUI.subProjectsPanel.revalidate();
                                        GUI.subProjectsPanel.repaint();
                                    }
                                });
                            }
                        }
                    });
                    projectButtonWrapper.add(projectButton, BorderLayout.CENTER);
                    projectsButtonsPanel.add(projectButtonWrapper);

                }
                JPanel projectContentArea = new JPanel(new BorderLayout());
                projectContentArea.add(projectsButtonsPanel, BorderLayout.NORTH);
                projectsPanel.add(projectContentArea, BorderLayout.CENTER);

                JButton createButton = new JButton("Create Project"); // create button
                createButton.setFocusable(false);
                createButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Projects_Files.createProject(userLogin, accountName);
                    }
                });
                projectsPanel.add(createButton, BorderLayout.SOUTH);
                projectsPanel.revalidate();
                projectsPanel.repaint();


                // now we do almost the same as above to the files Panel
                filesPanel.removeAll();
                filesPanel.setLayout(new BorderLayout());
                filesPanel.revalidate();
                filesPanel.repaint();
                // update the last_change column in all the rows. Check if any file exists
                PreparedStatement countFiles = conn.prepareStatement("select count(*) from files_" + userLogin);
                ResultSet resultSet1 = countFiles.executeQuery();
                if (resultSet1.next() && resultSet1.getInt(1) > 0){
                    PreparedStatement fetchFilePaths = conn.prepareStatement("select file_path from files_" + userLogin);
                    ResultSet resultSet2 = fetchFilePaths.executeQuery();
                    while (resultSet2.next()){
                        // for each path update the last change
                        Path filePathForUpdate = Paths.get(resultSet2.getString("file_path"));
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(filePathForUpdate, BasicFileAttributes.class);
                            Date updatedLastChangeDate = new Date(attrs.lastModifiedTime().toMillis());

                            // format to sql format
                            SimpleDateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                            String finalUpdatedLastChangeDate = dateSQLFormat.format(updatedLastChangeDate);

                            PreparedStatement updateLastChange = conn.prepareStatement("update files_" + userLogin + " set last_change = ? where file_path = ?");
                            updateLastChange.setString(1, finalUpdatedLastChangeDate);
                            updateLastChange.setString(2, String.valueOf(filePathForUpdate));
                            updateLastChange.executeUpdate();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }

                // create Files Sorting dropdown list
                JComboBox<String> filesSortingDropdown = new JComboBox<>(projectsSortingOptions);
                filesSortingDropdown.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // sorting logic
                        String selectedFilesSortingOption = (String) filesSortingDropdown.getSelectedItem();
                        // assign actions
                        switch (Objects.requireNonNull(selectedFilesSortingOption)){
                            case "Sort..": break;
                            case "Alphabet A-Z":
                                GUI.filesPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "file_name asc");
                                GUI.filesPanel.revalidate();
                                GUI.filesPanel.repaint();
                                break;
                            case "Alphabet Z-A":
                                GUI.filesPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "file_name desc");
                                GUI.filesPanel.revalidate();
                                GUI.filesPanel.repaint();
                                break;
                            case "Creation Date (Older)":
                                GUI.filesPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "file_id asc");
                                GUI.filesPanel.revalidate();
                                GUI.filesPanel.repaint();
                                break;
                            case "Creation Date (Newer)":
                                GUI.filesPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "file_id desc");
                                GUI.filesPanel.revalidate();
                                GUI.filesPanel.repaint();
                                break;
                            case "Date of Change (Older)":
                                GUI.filesPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "last_change asc");
                                GUI.filesPanel.revalidate();
                                GUI.filesPanel.repaint();
                                break;
                            case "Date of Change (Newer)":
                                GUI.filesPanel.removeAll();
                                getProjectsFilesByAccount(userLogin, accountName, projectsPanel, filesPanel, "last_change desc");
                                GUI.filesPanel.revalidate();
                                GUI.filesPanel.repaint();
                                break;
                        }
                    }

                });
                // top panel for dropdown of files
                JPanel filesTopPanel = new JPanel(new BorderLayout());
                filesTopPanel.add(filesSortingDropdown, BorderLayout.CENTER);
                // add to the main panel
                filesPanel.add(filesTopPanel, BorderLayout.NORTH);
                // panel for future buttons
                JPanel filesButtonsPanel = new JPanel();
                filesButtonsPanel.setLayout(new BoxLayout(filesButtonsPanel, BoxLayout.Y_AXIS));

                // select files assigned to the account with respect to sorting
                // display the buttons of the files assigned to the account
                String baseFileQuery = "select file_name from files_" + userLogin + " where account_id = ? ";
                String safeFileSort = getSafeFilesSortMethod(projectsSortingOption);
                String fullFileQuery = baseFileQuery + "order by " + safeFileSort;
                try (PreparedStatement preparedStatement2 = conn.prepareStatement(fullFileQuery)){
                    preparedStatement2.setInt(1, accountID);

                    ResultSet resultSet4 = preparedStatement2.executeQuery();
                    while (resultSet4.next()){
                        String fileName = resultSet4.getString("file_name");

                        // add buttons to the panel
                        JPanel fileButtonWrapper = new JPanel(new BorderLayout());
                        JButton fileButton = new JButton(fileName);
                        fileButton.setFocusable(false);
                        fileButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));

                        // open the file after clicking the button
                        fileButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                // fetch file path from its name
                                try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
                                    PreparedStatement fetchFilePath = conn.prepareStatement("select file_path from files_" + userLogin + " where file_name = ?");
                                    fetchFilePath.setString(1, fileName);
                                    ResultSet rs2 = fetchFilePath.executeQuery();
                                    String filePath = null;
                                    while (rs2.next()){
                                        filePath = rs2.getString("file_path");
                                    }
                                    // open the file
                                    try{
                                        Desktop.getDesktop().open(new File(filePath));
                                    } catch (IOException ex){
                                        ex.printStackTrace();
                                    }
                                } catch (SQLException e1){
                                    e1.printStackTrace();
                                }
                            }
                        });

                        fileButton.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                if(SwingUtilities.isRightMouseButton(e)){
                                    // create a popup menu for the options
                                    JPopupMenu filePopupMenu = new JPopupMenu();
                                    JMenuItem fileDeleteItem = new JMenuItem("Delete");

                                    // add menu item
                                    filePopupMenu.add(fileDeleteItem);

                                    filePopupMenu.show(e.getComponent(), e.getX(), e.getY());

                                    // assign actions to the items
                                    fileDeleteItem.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            int response = JOptionPane.showConfirmDialog(null, "Do you want to delete the file? It will be deleted from all the accounts", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                                            if (response == JOptionPane.YES_OPTION) {
                                                try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                                                    PreparedStatement preparedStatement1 = conn.prepareStatement("delete from files_" + userLogin + " where file_name = ?");
                                                    preparedStatement1.setString(1, fileName);
                                                    preparedStatement1.executeUpdate();
                                                    GUI.filesPanel.removeAll();
                                                    GUI.filesPanel.revalidate();
                                                    GUI.filesPanel.repaint();
                                                } catch (SQLException exception) {
                                                    exception.printStackTrace();
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        });

                        fileButtonWrapper.add(fileButton, BorderLayout.CENTER);
                        filesButtonsPanel.add(fileButtonWrapper);
                    }

                } catch (SQLException e){
                    e.printStackTrace();
                }
                JPanel fileContentArea = new JPanel(new BorderLayout());
                fileContentArea.add(filesButtonsPanel, BorderLayout.NORTH);
                filesPanel.add(fileContentArea, BorderLayout.CENTER);

                // add drag & drop function to the filesPanel
                int finalAccountID1 = accountID;
                filesPanel.setTransferHandler(new TransferHandler() {
                    // check if the file is ready for import
                    public boolean canImport(TransferHandler.TransferSupport info){
                        return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
                    }

                    public boolean importData(TransferSupport info){
                        if (!canImport(info)){
                            return false;
                        }

                        // Extract the files that are dropped
                        Transferable t = info.getTransferable();
                        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                            try {
                                List<File> droppedFiles = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                                for (File file : droppedFiles) {
                                    // for each file insert its data into the database
                                    String fileName = file.getName();
                                    String filePath = file.getAbsolutePath();
                                    String fileCreationDate = null;
                                    String fileLastChangeDate = null;
                                    String fileExtension = null;
                                    int dotIndex = fileName.lastIndexOf(".");
                                    if (dotIndex > 0){
                                        fileExtension = fileName.substring(dotIndex + 1);
                                    }
                                    // count if the file's already added
                                    PreparedStatement countExisting = conn.prepareStatement("select count(*) from files_" + userLogin + " where file_path = ?");
                                    countExisting.setString(1, filePath);
                                    ResultSet rs1 = countExisting.executeQuery();
                                    if (rs1.next() && rs1.getInt(1) == 0) {
                                        // fetch the dates of the file and convert them to sql format
                                        try {
                                            Path filePathForDate = Paths.get(filePath);
                                            BasicFileAttributes attrs = Files.readAttributes(filePathForDate, BasicFileAttributes.class);

                                            // get time and convert to date
                                            Date creationDate = new Date(attrs.creationTime().toMillis());
                                            Date lastChangeDate = new Date(attrs.lastModifiedTime().toMillis());

                                            // format dates to SQL format
                                            SimpleDateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                            fileCreationDate = dateSQLFormat.format(creationDate);
                                            fileLastChangeDate = dateSQLFormat.format(lastChangeDate);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                        PreparedStatement insertFile = conn.prepareStatement("insert into files_" + userLogin + " (account_id, file_name, file_path, file_type, last_change, file_creation) values (?, ?, ?, ?, ?, ?)");
                                        insertFile.setInt(1, finalAccountID1);
                                        insertFile.setString(2, fileName);
                                        insertFile.setString(3, filePath);
                                        if (fileExtension != null) {
                                            insertFile.setString(4, fileExtension);
                                        } else {
                                            insertFile.setNull(4, Types.VARCHAR);
                                        }
                                        insertFile.setString(5, fileLastChangeDate);
                                        insertFile.setString(6, fileCreationDate);
                                        insertFile.executeUpdate();
                                        JOptionPane.showMessageDialog(null, "File(s) added successfully!");
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Error occurred while insertion of the file " + fileName);
                                    }
                                }
                                return true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (SQLException e){
                            e.printStackTrace();
                        }
                        return false;
                    }
                });

                JButton browseButton = new JButton("Browse..");
                browseButton.setFocusable(false);
                int finalAccountID = accountID; // declare final account id
                browseButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // file chooser
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setDialogTitle("Choose a File");
                        int result = fileChooser.showOpenDialog(null); // user reaction in the fileChooser

                        if (result == JFileChooser.APPROVE_OPTION){
                            // get selected file
                            File selectedFile = fileChooser.getSelectedFile();
                            // file name
                            String fileName = selectedFile.getName();

                            // file path
                            String filePath = selectedFile.getAbsolutePath();

                            // proceed only if such file isn't already added
                            try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                                PreparedStatement countDuplicates = conn.prepareStatement("select count(*) from files_" + userLogin + " where file_path = ?");
                                countDuplicates.setString(1, filePath);
                                ResultSet rs1 = countDuplicates.executeQuery();
                                if (rs1.next() && rs1.getInt(1) == 0) {
                                    // get file extension
                                    String fileExtension = null;
                                    int dotIndex = fileName.lastIndexOf(".");
                                    if (dotIndex > 0) {
                                        fileExtension = fileName.substring(dotIndex + 1);
                                    }

                                    // file creation date
                                    String fileCreationDate = null;

                                    // file last change date
                                    String fileLastChangeDate = null;

                                    // Now we fetch creation and last change date and transform it into SQL format
                                    try {
                                        Path filePathForDate = Paths.get(filePath);
                                        BasicFileAttributes attrs = Files.readAttributes(filePathForDate, BasicFileAttributes.class);

                                        // get time and convert them to date
                                        Date creationDate = new Date(attrs.creationTime().toMillis());
                                        Date lastChangeDate = new Date(attrs.lastModifiedTime().toMillis());

                                        // format dates to SQL format
                                        SimpleDateFormat dateSQLFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // the format of convertion

                                        fileCreationDate = dateSQLFormat.format(creationDate);
                                        fileLastChangeDate = dateSQLFormat.format(lastChangeDate);
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                    // Once we get all the data, proceed with insertion into files table
                                    try (Connection conn1 = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                                        PreparedStatement insertFile = conn1.prepareStatement("insert into files_" + userLogin + " (account_id, file_name, file_path, file_type, last_change, file_creation) values (?, ?, ?, ?, ?, ?)");
                                        insertFile.setInt(1, finalAccountID);
                                        insertFile.setString(2, fileName);
                                        insertFile.setString(3, filePath);
                                        if (fileExtension != null) {                                 // check if extension is null
                                            insertFile.setString(4, fileExtension);
                                        } else {
                                            insertFile.setNull(4, Types.VARCHAR);
                                        }
                                        insertFile.setString(5, fileLastChangeDate);
                                        insertFile.setString(6, fileCreationDate);
                                        insertFile.executeUpdate();
                                    } catch (SQLException e1) {
                                        e1.printStackTrace();
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(null, "The file is already added");
                                }
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }

                    }
                });
                filesPanel.add(browseButton, BorderLayout.SOUTH);
                filesPanel.revalidate();
                filesPanel.repaint();
            } catch (SQLException e){
                e.printStackTrace();
            }
        });
    }
    // create project method
    private static void createProject(String userLogin, String assignedAccountName){
        // the frame
        JFrame projectCreationFrame = new JFrame("Create Project");
        projectCreationFrame.setLayout(new BorderLayout());

        // text fields panel
        JPanel textFieldsPanel = new JPanel();
        textFieldsPanel.setLayout(new BoxLayout(textFieldsPanel, BoxLayout.Y_AXIS));

        // name field
        JTextField nameField = new JTextField();
        // hint
        nameField.setForeground(Color.black);
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

        // deadline field
        JTextField deadlineField = new JTextField("Deadline");
        // hint
        deadlineField.setForeground(Color.gray);
        deadlineField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (deadlineField.getText().equals("Deadline")){
                    deadlineField.setText("");
                    deadlineField.setForeground(Color.black);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (deadlineField.getText().isEmpty()){
                    deadlineField.setForeground(Color.gray);
                    deadlineField.setText("Deadline");
                }
            }
        });
        deadlineField.setFont(new Font("Arial", Font.PLAIN, 20));
        deadlineField.setPreferredSize(new Dimension(200, 30));
        textFieldsPanel.add(deadlineField);

        // description field
        JTextField descriptionField = new JTextField("Description");
        // hint
        descriptionField.setForeground(Color.gray);
        descriptionField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (descriptionField.getText().equals("Description")) {
                    descriptionField.setText("");
                    descriptionField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (descriptionField.getText().isEmpty()) {
                    descriptionField.setForeground(Color.gray);
                    descriptionField.setText("Description");
                }
            }
        });
        descriptionField.setFont(new Font("Arial", Font.PLAIN, 20));
        descriptionField.setPreferredSize(new Dimension(200, 70));
        textFieldsPanel.add(descriptionField);

        // add the panel to the frame
        projectCreationFrame.add(textFieldsPanel, BorderLayout.NORTH);

        // fetch account and group names from the table
        List<String> accountNames = new ArrayList<>();
        List<String> groupNames = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
            PreparedStatement fetchAccountNames = conn.prepareStatement("select account_name from acc_" + userLogin + " order by account_id asc");
            ResultSet rs = fetchAccountNames.executeQuery();
            while (rs.next()){
                accountNames.add(rs.getString("account_name"));
            }
            PreparedStatement fetchGroupNames = conn.prepareStatement("select distinct name from gr_" + userLogin + " gr inner join acc_gr_junction_" + userLogin + " gr_j on gr.group_id = gr_j.group_id"); // select only ones that are not empty
            ResultSet rs1 = fetchGroupNames.executeQuery();
            while (rs1.next()){
                groupNames.add(rs1.getString("name"));
            }

        } catch (SQLException exception){
            exception.printStackTrace();
        }

        // create 2 panels for Accounts and Groups
        JPanel checkBoxesPanel = new JPanel();
        checkBoxesPanel.setLayout(new GridLayout(1, 2));

        subGroupsPanel = new JPanel();
        subGroupsPanel.setLayout(new BoxLayout(subGroupsPanel, BoxLayout.Y_AXIS));
        subGroupsPanel.setBorder(new MatteBorder(0, 0, 0, 1, Color.gray));
        checkBoxesPanel.add(subGroupsPanel);

        subAccountsPanel = new JPanel();
        subAccountsPanel.setLayout(new BoxLayout(subAccountsPanel, BoxLayout.Y_AXIS));
        checkBoxesPanel.add(subAccountsPanel);

        // add check boxes to group panel and account panel respectively
        for (String group : groupNames){
            JCheckBox groupCheckBox = new JCheckBox(group);
            subGroupsPanel.add(groupCheckBox);
        }
        for (String account : accountNames){
            JCheckBox accountCheckBox = new JCheckBox(account);
            if (Objects.equals(account, assignedAccountName)){
                accountCheckBox.setSelected(true);
            }
            subAccountsPanel.add(accountCheckBox);
        }
        // make the account and group panels scrollable
        JScrollPane groupScrollPane = new JScrollPane(subGroupsPanel);
        JScrollPane accountScrollPane = new JScrollPane(subAccountsPanel);

        // add them to the frame
        checkBoxesPanel.add(groupScrollPane);
        checkBoxesPanel.add(accountScrollPane);
        projectCreationFrame.add(checkBoxesPanel, BorderLayout.CENTER);

        // create 'Create Project' button
        JButton createProject = new JButton("Create Project");
        createProject.setFocusable(false);
        createProject.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
        projectCreationFrame.add(createProject, BorderLayout.SOUTH);

        // assign action to the button
        createProject.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // select data from the text fields
                String projectName = nameField.getText().trim();
                String description = null;
                if (!descriptionField.getText().trim().isEmpty() && !descriptionField.getText().trim().equals("Description")) {
                    description = descriptionField.getText().trim();
                }
                String deadline = deadlineField.getText().trim();

                // convert the date to sql format if it's not hint and null
                java.sql.Date sqlDate = null;
                boolean isDate = false;
                if (!deadline.equals("Deadline") && !deadline.isEmpty()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    java.util.Date parsedDate = null;
                    try {
                        // fetch the entered deadline
                        parsedDate = dateFormat.parse(deadline);

                        // check if this date is not in the past
                        java.util.Date currentDate = new java.util.Date();

                        if (!parsedDate.before(currentDate)) { // if no, convert it to sql format and switch flag
                            // convert java.util.Date to java.sql.Date
                            sqlDate = new java.sql.Date(parsedDate.getTime());
                            isDate = true;
                        }
                    } catch (ParseException ex) {
                        JOptionPane.showMessageDialog(null, "Wrong date format! It must be dd.mm.yyyy HH:mm");
                    }
                } else {
                    isDate = true;
                }

                // check if user entered name
                if(!projectName.isEmpty() && !projectName.equals("Name")) {

                    // check if date is date, if false, stop
                    if (isDate) {
                        // get the list of chosen groups and accounts
                        List<String> chosenFinalAccounts = new ArrayList<>();
                        List<String> chosenGroups = new ArrayList<>();
                        List<String> chosenAccounts = new ArrayList<>();
                        for (Component comp : subGroupsPanel.getComponents()) {
                            if (comp instanceof JCheckBox) {
                                JCheckBox groupCheckBox = (JCheckBox) comp;
                                if (groupCheckBox.isSelected()) {
                                    chosenGroups.add(groupCheckBox.getText()); // add selected groups to the list
                                }
                            }
                        }
                        for (Component comp : subAccountsPanel.getComponents()) {
                            if (comp instanceof JCheckBox) {
                                JCheckBox accountCheckBox = (JCheckBox) comp;
                                if (accountCheckBox.isSelected()) {
                                    chosenAccounts.add(accountCheckBox.getText());
                                }
                            }
                        }
                        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                            for (String chosenGroup : chosenGroups) {
                                PreparedStatement getGroupID = conn.prepareStatement("select group_id from gr_" + userLogin + " where name = ?");
                                getGroupID.setString(1, chosenGroup);
                                ResultSet rs = getGroupID.executeQuery();
                                int groupID = 0;
                                while (rs.next()) {
                                    groupID = rs.getInt(1);
                                }
                                PreparedStatement getAccountsID = conn.prepareStatement("select account_id from acc_gr_junction_" + userLogin + " where group_id = ?");
                                getAccountsID.setInt(1, groupID);
                                ResultSet rs1 = getAccountsID.executeQuery();
                                int accountIDFromGroup = 0;
                                while (rs1.next()) {
                                    accountIDFromGroup = rs1.getInt(1);
                                    PreparedStatement fetchAccountNames = conn.prepareStatement("select account_name from acc_" + userLogin + " where account_id = ?");
                                    fetchAccountNames.setInt(1, accountIDFromGroup);
                                    ResultSet rs2 = fetchAccountNames.executeQuery();
                                    while (rs2.next()) {
                                        chosenFinalAccounts.add(rs2.getString("account_name"));
                                    }
                                }
                            }
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }

                        // Now add the accounts to the final accounts list (only these that aren't added yet)
                        for (String selectedAccount : chosenAccounts) {
                            if (!chosenFinalAccounts.contains(selectedAccount)) {
                                chosenFinalAccounts.add(selectedAccount); // now we got the ArrayList with all the accounts where the project must be inserted
                            }
                        }
                        // proceed if any account is chosen
                        if (!chosenFinalAccounts.isEmpty()) {
                            try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                                PreparedStatement countIfExist = conn.prepareStatement("select count(*) from projects_" + userLogin + " where project_name = ?");
                                countIfExist.setString(1, projectName);
                                ResultSet rs = countIfExist.executeQuery();
                                if (rs.next() && !(rs.getInt(1) > 0)) {

                                    // insert data into projects table
                                    PreparedStatement insertProject = conn.prepareStatement("insert into projects_" + userLogin + "(project_name, description, deadline) values (?, ?, ?)");
                                    insertProject.setString(1, projectName);
                                    if (description != null) {
                                        insertProject.setString(2, description);
                                    } else {
                                        insertProject.setNull(2, Types.VARCHAR);        // if description is null, insert null
                                    }
                                    if (isDate) {
                                        if (sqlDate != null) {
                                            insertProject.setDate(3, sqlDate);
                                        } else {
                                            insertProject.setNull(3, Types.TIMESTAMP);
                                        }
                                    } else {
                                        insertProject.setNull(3, Types.TIMESTAMP);
                                    }
                                    insertProject.executeUpdate();

                                    // now insert into junction
                                    for (String chosenFinalAccount : chosenFinalAccounts) {
                                        PreparedStatement insertJunctionProject = conn.prepareStatement("insert into projects_junction_" + userLogin + "(account_id, project_id) values ((select account_id from acc_" + userLogin + " where account_name = ?), (select project_id from projects_" + userLogin + " where project_name = ?))");
                                        insertJunctionProject.setString(1, chosenFinalAccount);
                                        insertJunctionProject.setString(2, projectName);
                                        insertJunctionProject.executeUpdate();
                                    }
                                }
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "No account is chosen. Please try again.");
                        }
                        JOptionPane.showMessageDialog(null, "Project created successfully!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Wrong date, try again");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Error. Please re-enter project name.");
                }
            }

        });
        projectCreationFrame.setPreferredSize(new Dimension(300, 400));
        projectCreationFrame.setMinimumSize(new Dimension(250, 330));
        projectCreationFrame.setMaximumSize(new Dimension(300, 1000));
        projectCreationFrame.pack();
        projectCreationFrame.setVisible(true);
        projectCreationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        projectCreationFrame.setLocationRelativeTo(null);
    }

    // change project method
    private static void changeProject(String userLogin, String certainProjectName, JButton currentProjectButton) {
        JPanel changePanel = new JPanel();
        changePanel.setLayout(new BoxLayout(changePanel, BoxLayout.Y_AXIS));

        // create name field that will get its value from the database
        JTextField nameField = new JTextField(certainProjectName);
        nameField.setFont(new Font("Arial", Font.PLAIN, 20));
        nameField.setPreferredSize(new Dimension(200, 30));
        changePanel.add(nameField);

        // fetch deadline from the database
        String deadline = null;
        try(Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
            PreparedStatement fetchDeadline = conn.prepareStatement("select deadline from projects_" + userLogin + " where project_name = ?");
            fetchDeadline.setString(1, certainProjectName);
            ResultSet rs = fetchDeadline.executeQuery();
            if(rs.next()){
                Date fetchedDeadline = rs.getDate(1);
                if (fetchedDeadline != null) {
                    SimpleDateFormat sqlDateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    deadline = sqlDateFormat.format(fetchedDeadline);
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        // create deadline
        JTextField deadlineField = new JTextField();
        if (deadline == null){
            deadlineField.setText("Deadline");
            deadlineField.setForeground(Color.gray);
            deadlineField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (deadlineField.getText().trim().equals("Deadline")){
                        deadlineField.setText("");
                        deadlineField.setForeground(Color.black);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (deadlineField.getText().trim().isEmpty()){
                        deadlineField.setForeground(Color.gray);
                        deadlineField.setText("Deadline");
                    }
                }
            });
        } else {
            deadlineField.setText(deadline);
            deadlineField.setForeground(Color.black);
        }
        deadlineField.setFont(new Font("Arial", Font.PLAIN, 20));
        deadlineField.setPreferredSize(new Dimension(200, 70));
        changePanel.add(deadlineField);

        // create description field
        descriptionField = new JTextField();

        // parse the database for the description
        String description = null;
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
            PreparedStatement preparedStatement2 = conn.prepareStatement("select description from projects_" + userLogin + " where project_name = ?");
            preparedStatement2.setString(1, certainProjectName);
            ResultSet rs = preparedStatement2.executeQuery();
            while (rs.next()) {
                description = rs.getString("description");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        if (description == null){
            descriptionField.setText("Description");
            descriptionField.setForeground(Color.gray);
            descriptionField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (descriptionField.getText().trim().equals("Description")){
                        descriptionField.setText("");
                        descriptionField.setForeground(Color.black);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (descriptionField.getText().trim().isEmpty()){
                        descriptionField.setText("Description");
                        descriptionField.setForeground(Color.gray);
                    }
                }
            });
        } else {
            descriptionField.setText(description);
            descriptionField.setForeground(Color.black);
        }
        descriptionField.setFont(new Font("Arial", Font.PLAIN, 20));
        descriptionField.setPreferredSize(new Dimension(200, 70));
        changePanel.add(descriptionField);

        // panel for groups and accounts
        JPanel combinedPanel = new JPanel(new GridLayout(1, 2));
        // panel for groups checkboxes
        JPanel subGroupsPanel = new JPanel();
        subGroupsPanel.setLayout(new BoxLayout(subGroupsPanel, BoxLayout.Y_AXIS));

        // FETCH GROUPS FROM THE PANEL
        List<String> groupNames = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")){
            PreparedStatement fetchGroups = conn.prepareStatement("select distinct name from gr_" + userLogin + " gr inner join acc_gr_junction_" + userLogin + " gr_j on gr.group_id = gr_j.group_id"); // display only not empty groups
            ResultSet rs = fetchGroups.executeQuery();
            while (rs.next()){
                groupNames.add(rs.getString("name"));
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
        // create checkboxes for each group
        for (String groupName : groupNames){
            JCheckBox groupCheckBox = new JCheckBox(groupName);
            subGroupsPanel.add(groupCheckBox);
        }
        // panel for accounts check boxes
        JPanel subAccountsPanel = new JPanel();
        subAccountsPanel.setLayout(new BoxLayout(subAccountsPanel, BoxLayout.Y_AXIS));

        // fetch accounts from the table
        List<String> accountNames = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
            PreparedStatement fetchNames = conn.prepareStatement("select account_name from acc_" + userLogin + " order by account_id asc");
            ResultSet rs = fetchNames.executeQuery();
            while (rs.next()) {
                accountNames.add(rs.getString("account_name"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        // fetch accounts connected to the project
        List<String> linkedAccounts = new ArrayList<>();
        // fetch project ID at first
        int projectID = 0;
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
            PreparedStatement fetchProjectID = conn.prepareStatement("select project_id from projects_" + userLogin + " where project_name = ?");
            fetchProjectID.setString(1, certainProjectName);
            ResultSet rs = fetchProjectID.executeQuery();
            while (rs.next()) {
                projectID = rs.getInt(1);
            }
            // and now fetch the accounts themselves (start from the indexes)
            PreparedStatement fetchLinkedAccountsID = conn.prepareStatement("select account_id from projects_junction_" + userLogin + " where project_id = ?");
            fetchLinkedAccountsID.setInt(1, projectID);
            ResultSet resultSet1 = fetchLinkedAccountsID.executeQuery();
            while (resultSet1.next()) {
                int linkedAccountID = resultSet1.getInt("account_id");

                PreparedStatement fetchLinkedAccountsNames = conn.prepareStatement("select account_name from acc_" + userLogin + " where account_id = ?");
                fetchLinkedAccountsNames.setInt(1, linkedAccountID);
                ResultSet resultSet2 = fetchLinkedAccountsNames.executeQuery();
                while (resultSet2.next()) {
                    linkedAccounts.add(resultSet2.getString("account_name"));
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        // Now, iterate through the list of all the accounts and tick that ones that are selected by the user
        for (String accountName : accountNames) {
            JCheckBox checkBox = new JCheckBox(accountName);
            if (linkedAccounts.contains(accountName)) {
                checkBox.setSelected(true);
            }
            subAccountsPanel.add(checkBox);
        }
        subAccountsPanel.revalidate();
        subAccountsPanel.repaint();

        // frame
        JFrame changeProjectFrame = new JFrame("Change Project");
        changeProjectFrame.setLayout(new BorderLayout());
        changeProjectFrame.add(changePanel, BorderLayout.NORTH);

        // scroll pane for accounts and groups panel
        JScrollPane accScrollPane = new JScrollPane(subAccountsPanel);
        JScrollPane grScrollPane = new JScrollPane(subGroupsPanel);
        combinedPanel.add(grScrollPane);
        combinedPanel.add(accScrollPane);
        changeProjectFrame.add(combinedPanel, BorderLayout.CENTER);

        // Submit Changes Button
        JButton submitButton = new JButton("Submit Changes");
        submitButton.setPreferredSize(new Dimension(20, 50));
        submitButton.setFocusable(false);
        changeProjectFrame.add(submitButton, BorderLayout.SOUTH);

        // Add action to the button
        int finalProjectID = projectID;
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get the values from fields
                String updatedProjectName = nameField.getText().trim();
                String updatedDescription = descriptionField.getText().trim();

                // get deadline and convert it to date format. If it's not a date, turn flag to false
                String updatedDeadline = deadlineField.getText().trim();
                boolean isDate = true;
                java.sql.Date sqlDate = null;
                if (!updatedDeadline.equals("Deadline") && !updatedDeadline.isEmpty())
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                        java.util.Date parsedDate = dateFormat.parse(updatedDeadline);
                        sqlDate = new java.sql.Date(parsedDate.getTime());

                    } catch (ParseException e1){
                        isDate = false;
                    }

                // check if user entered project name
                if (!updatedProjectName.isEmpty() && !updatedProjectName.equals("Name")) {
                    // check if the deadline is either date or empty
                    if (isDate) {
                        // check if account with new name already exists in the table
                        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/mydb", "root", "root")) {
                            PreparedStatement checkExist = conn.prepareStatement("select count(*) from projects_" + userLogin + " where project_name = ? and project_id <> ?");
                            checkExist.setString(1, updatedProjectName);
                            checkExist.setInt(2, finalProjectID);

                            ResultSet resultSet1 = checkExist.executeQuery();
                            if (resultSet1.next()) {
                                int count = resultSet1.getInt(1);
                                if (count > 0) {
                                    // A project with new name already exists, excluding the initial project name
                                    JOptionPane.showMessageDialog(null, "A project with this name already exists. Please choose a different name.");
                                } else { // The name is unique, proceed with adjusting tables

                                    // get ticked check boxes
                                    List<String> selectedAccounts = new ArrayList<>();
                                    List<String> selectedGroups = new ArrayList<>();
                                    for (Component comp : subAccountsPanel.getComponents()) {
                                        if (comp instanceof JCheckBox) {
                                            JCheckBox checkBox = (JCheckBox) comp;
                                            if (checkBox.isSelected()) {
                                                selectedAccounts.add(checkBox.getText());
                                            }
                                        }
                                    }
                                    for (Component comp : subGroupsPanel.getComponents()) {
                                        if (comp instanceof JCheckBox) {
                                            JCheckBox checkBox = (JCheckBox) comp;
                                            if (checkBox.isSelected()) {
                                                selectedGroups.add(checkBox.getText());
                                            }
                                        }
                                    }
                                    // fetch account ID linked to the group
                                    try (PreparedStatement fetchAccountIDFromGroupID = conn.prepareStatement("select account_id from acc_gr_junction_" + userLogin + " where group_id = (select group_id from gr_" + userLogin + " where name = ?)")) {
                                        for (String selectedGroup : selectedGroups) {
                                            fetchAccountIDFromGroupID.setString(1, selectedGroup);
                                            ResultSet rs = fetchAccountIDFromGroupID.executeQuery();
                                            while (rs.next()) {
                                                int accountID = rs.getInt(1);

                                                // get account name by its id
                                                PreparedStatement fetchAccountName = conn.prepareStatement("select account_name from acc_" + userLogin + " where account_id = ?");
                                                fetchAccountName.setInt(1, accountID);
                                                ResultSet rs1 = fetchAccountName.executeQuery();
                                                while (rs1.next()) {
                                                    String accountNameFromGroup = rs1.getString("account_name");
                                                    if (!selectedAccounts.contains(accountNameFromGroup)) {
                                                        selectedAccounts.add(accountNameFromGroup);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // proceed if user selected at least one account. If not, show the warning
                                    if (!selectedAccounts.isEmpty()) {
                                        // update the tables
                                        PreparedStatement changeProjectRow = conn.prepareStatement("update projects_" + userLogin + " set project_name = ?, description = ?, deadline = ?, changed_at = current_timestamp where project_id = ?");
                                        changeProjectRow.setString(1, updatedProjectName);
                                        if (!updatedDescription.isEmpty()) {
                                            changeProjectRow.setString(2, updatedDescription);
                                        } else {
                                            changeProjectRow.setNull(2, Types.VARCHAR);
                                        }
                                        if (isDate && sqlDate != null) {
                                            changeProjectRow.setDate(3, sqlDate);
                                        } else {
                                            changeProjectRow.setNull(3, Types.DATE);
                                        }
                                        changeProjectRow.setInt(4, finalProjectID);
                                        changeProjectRow.executeUpdate();

                                        // delete the existing connections in juntion table
                                        PreparedStatement deleteJunctionConnections = conn.prepareStatement("delete from projects_junction_" + userLogin + " where project_id = ?");
                                        deleteJunctionConnections.setInt(1, finalProjectID);
                                        deleteJunctionConnections.executeUpdate();
                                        // upload the new links from ticked checkboxes list
                                        for (String selectedAccount : selectedAccounts) {
                                            PreparedStatement updateJunctionConnections = conn.prepareStatement("insert into projects_junction_" + userLogin + " values ((select account_id from acc_" + userLogin + " where account_name = ?), ?)");
                                            updateJunctionConnections.setString(1, selectedAccount);
                                            updateJunctionConnections.setInt(2, finalProjectID);
                                            updateJunctionConnections.executeUpdate();
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(null, "No account is chosen. Try again");
                                    }
                                }
                            }

                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }

                    } else {
                        JOptionPane.showMessageDialog(null, "Wrong deadline format. Try again");
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "Error. Please re-enter the name.");
                }
                SwingUtilities.invokeLater(() -> {
                    currentProjectButton.setText(updatedProjectName);
                    GUI.subProjectsPanel.revalidate();
                    GUI.subProjectsPanel.repaint();
                });
                JOptionPane.showMessageDialog(null, "The project's changed successfully");
            }
        });
        changeProjectFrame.setPreferredSize(new Dimension(300, 400));
        changeProjectFrame.setMaximumSize(new Dimension(300, 1000));
        changeProjectFrame.setMinimumSize(new Dimension(250, 330));
        changeProjectFrame.pack();
        changeProjectFrame.setVisible(true);
        changeProjectFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        changeProjectFrame.setLocationRelativeTo(null);
    }
}