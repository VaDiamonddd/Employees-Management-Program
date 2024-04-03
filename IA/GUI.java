    import java.awt.event.ActionEvent;
    import java.awt.event.ActionListener;
    import javax.swing.*;
    import javax.swing.border.MatteBorder;
    import java.awt.*;
    import java.io.File;
    import java.io.IOException;

    public class GUI extends JFrame {
        String loginUser;
        static JPanel groupsColumn;
        static JPanel accountsColumn;
        public static JPanel subProjectsPanel;
        public static JPanel filesPanel;

        GUI(String loginUser) {
            SwingUtilities.invokeLater(() -> {

                groupsColumn = new JPanel();
                groupsColumn.setLayout(new BorderLayout());
                groupsColumn.setMinimumSize(new Dimension(100, 50));

                //Dropdown for mainColumn sorting options

                JPanel spaceHolder = new JPanel();
                spaceHolder.setLayout(new BorderLayout());

                JSplitPane splitGroupPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, groupsColumn, spaceHolder);
                splitGroupPane.setOneTouchExpandable(true);
                splitGroupPane.setDividerLocation(180);

                accountsColumn = new JPanel();
                accountsColumn.setLayout(new BorderLayout());
                accountsColumn.setMinimumSize(new Dimension(100, 50));


                // panel for containing projects and files

                JPanel projectsPanel = new JPanel();
                projectsPanel.setLayout(new GridLayout(1, 2));

                // create separate subpanels for files and projects
                subProjectsPanel = new JPanel(new BorderLayout());
                subProjectsPanel.setBorder(new MatteBorder(0,0,0,1, Color.gray)); // set border between files and projects panel
                JPanel subProjectsContent = new JPanel(); // for the buttons themselves
                subProjectsPanel.add(subProjectsContent, BorderLayout.CENTER);


                filesPanel = new JPanel(new BorderLayout());
                JPanel filesContent = new JPanel();
                filesPanel.add(filesContent, BorderLayout.CENTER);


                projectsPanel.add(subProjectsPanel);
                projectsPanel.add(filesPanel);

                JSplitPane splitAccountsPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, accountsColumn, projectsPanel);
                splitAccountsPane.setOneTouchExpandable(true);
                splitAccountsPane.setDividerLocation(150);
                spaceHolder.add(splitAccountsPane, BorderLayout.CENTER);

                // create a menu bar
                JMenuBar menuBar = new JMenuBar();
                JMenu createMenu = new JMenu("Create");
                JMenu helpMenu = new JMenu("Help");

                // create items inside each menu
                JMenuItem groupItem = new JMenuItem("Group");
                groupItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {Group.createGroup(loginUser);}
                });

                JMenuItem accountItem = new JMenuItem("Account");
                accountItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Account.createAccount(loginUser);
                    }
                });


                JMenuItem guideItem = new JMenuItem("Open Guide");
                guideItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Desktop.getDesktop().open(new File("C:\\Users\\vadim\\IdeaProjects\\IA3\\help.txt"));
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });

                createMenu.add(groupItem);
                createMenu.add(accountItem);
                helpMenu.add(guideItem);
                menuBar.add(createMenu);
                menuBar.add(helpMenu);


                this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                this.setSize(new Dimension(1000, 500));
                this.setVisible(true);
                this.setTitle("Project");
                this.setLayout(new BorderLayout());
                this.add(splitGroupPane, BorderLayout.CENTER);
                this.setJMenuBar(menuBar);
                this.setLocationRelativeTo(null);

                this.loginUser = loginUser;
            });
        }
    }