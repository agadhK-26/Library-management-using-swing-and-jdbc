import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.Vector;


public class LibraryManagementApp extends JFrame {

    // ------------ DB CONFIG (edit if needed) ------------
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1Aguk@26";
    // ----------------------------------------------------

    // UI components (books)
    private JTextField txtIsbn, txtTitle, txtAuthor, txtPublisher, txtYear, txtCopies, txtSearch;
    private JComboBox<String> comboGenre, comboFilterGenre;
    private JTable tblBooks;
    private DefaultTableModel booksTableModel;

    // UI components (members)
    private JTextField memName, memContact, memEmail, memSearch;
    private JTable tblMembers;
    private DefaultTableModel membersTableModel;

    // UI components (transactions)
    private JComboBox<String> comboIssueBook, comboIssueMember;
    private JButton btnIssue, btnReturn;

    // Styling
    private Font uiFont = new Font("Segoe UI", Font.PLAIN, 14);

    public LibraryManagementApp() {
        setTitle("Library Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 700));
        initUI();
        loadBooksToTable("");
        loadMembersToTable("");
        populateIssueCombos();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ---------- Menu Bar (dropdown menus) ----------
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        file.add(exit);

        JMenu manage = new JMenu("Manage");
        JMenuItem manageBooks = new JMenuItem("Books");
        JMenuItem manageMembers = new JMenuItem("Members");
        JMenuItem manageTrans = new JMenuItem("Transactions");

        manageBooks.addActionListener(e -> showTab(0));
        manageMembers.addActionListener(e -> showTab(1));
        manageTrans.addActionListener(e -> showTab(2));

        manage.add(manageBooks);
        manage.add(manageMembers);
        manage.add(manageTrans);

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Library Management System\nBuilt with Java Swing & MySQL\n", "About", JOptionPane.INFORMATION_MESSAGE));
        help.add(about);

        menuBar.add(file);
        menuBar.add(manage);
        menuBar.add(help);
        setJMenuBar(menuBar);

        // ---------- Top Panel: Title ----------
        JPanel top = new GradientPanel(new Color(18, 77, 121), new Color(54, 137, 214));
        top.setLayout(new BorderLayout());
        JLabel lbTitle = new JLabel("Library Management System");
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lbTitle.setForeground(Color.WHITE);
        lbTitle.setBorder(new EmptyBorder(20, 20, 20, 20));
        top.add(lbTitle, BorderLayout.WEST);

        add(top, BorderLayout.NORTH);

        // ---------- Center: CardPanel with tabs ----------
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(uiFont);

        tabs.add("Books", createBooksPanel());
        tabs.add("Members", createMembersPanel());
        tabs.add("Transactions", createTransactionPanel());

        add(tabs, BorderLayout.CENTER);
    }

    // ---------- Helper to switch tabs via menu ----------
    private void showTab(int index) {
        JTabbedPane tp = findTabbedPane(this);
        if (tp != null && index >= 0 && index < tp.getTabCount()) tp.setSelectedIndex(index);
    }

    private JTabbedPane findTabbedPane(Container c) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JTabbedPane) return (JTabbedPane) comp;
            if (comp instanceof Container) {
                JTabbedPane t = findTabbedPane((Container) comp);
                if (t != null) return t;
            }
        }
        return null;
    }

    // ---------- Books Panel ----------
    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12,12,12,12));

        // Left: form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new CompoundBorder(new TitledBorder("Book Details"), new EmptyBorder(8,8,8,8)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("ISBN:"), gbc);
        txtIsbn = new JTextField(); txtIsbn.setFont(uiFont);
        gbc.gridx = 1; gbc.gridy = y++; form.add(txtIsbn, gbc);

        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Title:"), gbc);
        txtTitle = new JTextField(); txtTitle.setFont(uiFont);
        gbc.gridx = 1; gbc.gridy = y++; form.add(txtTitle, gbc);

        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Author:"), gbc);
        txtAuthor = new JTextField(); txtAuthor.setFont(uiFont);
        gbc.gridx = 1; gbc.gridy = y++; form.add(txtAuthor, gbc);

        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Genre:"), gbc);
        comboGenre = new JComboBox<>(new String[]{"General","Science","Technology","Fiction","History","Art","Others"});
        gbc.gridx = 1; gbc.gridy = y++; form.add(comboGenre, gbc);

        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Publisher:"), gbc);
        txtPublisher = new JTextField(); txtPublisher.setFont(uiFont);
        gbc.gridx = 1; gbc.gridy = y++; form.add(txtPublisher, gbc);

        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Year:"), gbc);
        txtYear = new JTextField(); txtYear.setFont(uiFont);
        gbc.gridx = 1; gbc.gridy = y++; form.add(txtYear, gbc);

        gbc.gridx = 0; gbc.gridy = y; form.add(new JLabel("Copies:"), gbc);
        txtCopies = new JTextField("1"); txtCopies.setFont(uiFont);
        gbc.gridx = 1; gbc.gridy = y++; form.add(txtCopies, gbc);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton btnAdd = new JButton("Add Book");
        JButton btnUpdate = new JButton("Update Book");
        JButton btnDelete = new JButton("Delete Book");
        btnRow.add(btnAdd); btnRow.add(btnUpdate); btnRow.add(btnDelete);
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; form.add(btnRow, gbc);
        gbc.gridwidth = 1; y++;

        // Search & filter
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(new TitledBorder("Search / Filter"));
        GridBagConstraints s = new GridBagConstraints();
        s.insets = new Insets(6,6,6,6);
        s.fill = GridBagConstraints.HORIZONTAL;
        s.gridx = 0; s.gridy = 0; searchPanel.add(new JLabel("Search (Title/Author/ISBN):"), s);
        txtSearch = new JTextField(); txtSearch.setFont(uiFont);
        s.gridx = 1; s.gridy = 0; s.weightx = 1.0; searchPanel.add(txtSearch, s);
        s.weightx = 0; s.gridx = 0; s.gridy = 1; searchPanel.add(new JLabel("Genre:"), s);
        comboFilterGenre = new JComboBox<>(new String[]{"All","General","Science","Technology","Fiction","History","Art","Others"});
        s.gridx = 1; s.gridy = 1; searchPanel.add(comboFilterGenre, s);

        JButton btnSearch = new JButton("Search");
        s.gridx = 0; s.gridy = 2; s.gridwidth = 2; searchPanel.add(btnSearch, s);

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; form.add(searchPanel, gbc);
        gbc.gridwidth = 1; y++;

        // Left wrapper
        JPanel leftWrap = new JPanel(new BorderLayout(8,8));
        leftWrap.add(form, BorderLayout.NORTH);

        // Right: table
        booksTableModel = new DefaultTableModel(new String[]{"ID","ISBN","Title","Author","Genre","Publisher","Year","Copies","Available"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblBooks = new JTable(booksTableModel);
        tblBooks.setFont(uiFont);
        tblBooks.setRowHeight(24);
        JScrollPane scrBooks = new JScrollPane(tblBooks);

        // Layout split
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftWrap, scrBooks);
        split.setResizeWeight(0.35);
        panel.add(split, BorderLayout.CENTER);

        // ---------- Button actions ----------
        btnAdd.addActionListener(e -> addBook());
        btnUpdate.addActionListener(e -> updateSelectedBook());
        btnDelete.addActionListener(e -> deleteSelectedBook());
        btnSearch.addActionListener(e -> loadBooksToTable(txtSearch.getText().trim()));
        comboFilterGenre.addActionListener(e -> loadBooksToTable(txtSearch.getText().trim()));

        tblBooks.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateBookFormFromSelection();
        });

        return panel;
    }

    // ---------- Members Panel ----------
    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout(12,12));
        panel.setBorder(new EmptyBorder(12,12,12,12));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new CompoundBorder(new TitledBorder("Member Details"), new EmptyBorder(8,8,8,8)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y=0;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Name:"), gbc);
        memName = new JTextField(); memName.setFont(uiFont);
        gbc.gridx=1; gbc.gridy=y++; form.add(memName, gbc);

        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Contact:"), gbc);
        memContact = new JTextField(); memContact.setFont(uiFont);
        gbc.gridx=1; gbc.gridy=y++; form.add(memContact, gbc);

        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Email:"), gbc);
        memEmail = new JTextField(); memEmail.setFont(uiFont);
        gbc.gridx=1; gbc.gridy=y++; form.add(memEmail, gbc);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,10,0));
        JButton addMem = new JButton("Add Member");
        JButton updMem = new JButton("Update");
        JButton delMem = new JButton("Delete");
        btns.add(addMem); btns.add(updMem); btns.add(delMem);
        gbc.gridx=0; gbc.gridy=y; gbc.gridwidth=2; form.add(btns, gbc);
        gbc.gridwidth=1; y++;

        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(new TitledBorder("Search Members"));
        GridBagConstraints s = new GridBagConstraints();
        s.insets = new Insets(6,6,6,6);
        s.fill = GridBagConstraints.HORIZONTAL;
        s.gridx=0; s.gridy=0; searchPanel.add(new JLabel("Name/Contact/Email:"), s);
        memSearch = new JTextField(); memSearch.setFont(uiFont);
        s.gridx=1; s.gridy=0; searchPanel.add(memSearch, s);
        JButton btnMemberSearch = new JButton("Search");
        s.gridx=0; s.gridy=1; s.gridwidth=2; searchPanel.add(btnMemberSearch, s);

        JPanel leftWrap = new JPanel(new BorderLayout(8,8));
        leftWrap.add(form, BorderLayout.NORTH);
        leftWrap.add(searchPanel, BorderLayout.CENTER);

        // members table
        membersTableModel = new DefaultTableModel(new String[]{"ID","Name","Contact","Email"}, 0) {
            public boolean isCellEditable(int r,int c){return false;}
        };
        tblMembers = new JTable(membersTableModel);
        tblMembers.setRowHeight(24);
        JScrollPane scrMembers = new JScrollPane(tblMembers);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftWrap, scrMembers);
        split.setResizeWeight(0.30);

        panel.add(split, BorderLayout.CENTER);

        // actions
        addMem.addActionListener(e -> addMember());
        updMem.addActionListener(e -> updateSelectedMember());
        delMem.addActionListener(e -> deleteSelectedMember());
        btnMemberSearch.addActionListener(e -> loadMembersToTable(memSearch.getText().trim()));

        tblMembers.getSelectionModel().addListSelectionListener(e -> populateMemberFormFromSelection());

        return panel;
    }

    // ---------- Transactions Panel ----------
    private JPanel createTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout(12,12));
        panel.setBorder(new EmptyBorder(12,12,12,12));

        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(new TitledBorder("Issue / Return Book"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0; top.add(new JLabel("Select Book (ISBN - Title):"), gbc);
        comboIssueBook = new JComboBox<>();
        comboIssueBook.setPreferredSize(new Dimension(400, 28));
        gbc.gridx=1; gbc.gridy=0; top.add(comboIssueBook, gbc);

        gbc.gridx=0; gbc.gridy=1; top.add(new JLabel("Select Member:"), gbc);
        comboIssueMember = new JComboBox<>();
        comboIssueMember.setPreferredSize(new Dimension(400,28));
        gbc.gridx=1; gbc.gridy=1; top.add(comboIssueMember, gbc);

        btnIssue = new JButton("Issue Book");
        btnReturn = new JButton("Return Selected Transaction");

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.add(btnIssue); btnRow.add(btnReturn);

        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2; top.add(btnRow, gbc);

        // Transactions table
        DefaultTableModel transModel = new DefaultTableModel(new String[]{"Txn ID","Book","Member","Issue Date","Return Date","Returned"}, 0) {
            public boolean isCellEditable(int r,int c){return false;}
        };
        JTable tblTrans = new JTable(transModel);
        tblTrans.setRowHeight(24);
        JScrollPane scrTrans = new JScrollPane(tblTrans);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrTrans, BorderLayout.CENTER);

        // load transactions
        loadTransactionsToTable(transModel);

        btnIssue.addActionListener(e -> {
            issueBook();
            loadTransactionsToTable(transModel);
            loadBooksToTable("");
            populateIssueCombos();
        });

        btnReturn.addActionListener(e -> {
            int sel = tblTrans.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(this, "Select a transaction row to mark as returned.");
                return;
            }
            int txnId = Integer.parseInt(transModel.getValueAt(sel, 0).toString());
            boolean alreadyReturned = Boolean.parseBoolean(transModel.getValueAt(sel, 5).toString());
            if (alreadyReturned) {
                JOptionPane.showMessageDialog(this, "Transaction already marked returned.");
                return;
            }
            markReturned(txnId);
            loadTransactionsToTable(transModel);
            loadBooksToTable("");
            populateIssueCombos();
        });

        return panel;
    }

    // ------------------ DB CRUD operations ------------------

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // Books
    private void addBook() {
        String isbn = txtIsbn.getText().trim();
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String genre = comboGenre.getSelectedItem().toString();
        String publisher = txtPublisher.getText().trim();
        int year = parseIntOrZero(txtYear.getText().trim());
        int copies = Math.max(1, parseIntOrZero(txtCopies.getText().trim()));

        if (title.isEmpty()) { showErr("Title is required"); return; }

        String sql = "INSERT INTO books (isbn, title, author, genre, publisher, year, copies, available) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, isbn.isEmpty() ? null : isbn);
            p.setString(2, title);
            p.setString(3, author);
            p.setString(4, genre);
            p.setString(5, publisher);
            if (year == 0) p.setNull(6, Types.INTEGER); else p.setInt(6, year);
            p.setInt(7, copies);
            p.setInt(8, copies);
            p.executeUpdate();
            showMsg("Book added");
            loadBooksToTable("");
            populateIssueCombos();
            clearBookForm();
        } catch (SQLException ex) {
            showErr("Failed to add book: " + ex.getMessage());
        }
    }

    private void updateSelectedBook() {
        int sel = tblBooks.getSelectedRow();
        if (sel == -1) { showErr("Select a book from table to update"); return; }
        int id = Integer.parseInt(booksTableModel.getValueAt(sel, 0).toString());

        String isbn = txtIsbn.getText().trim();
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String genre = comboGenre.getSelectedItem().toString();
        String publisher = txtPublisher.getText().trim();
        int year = parseIntOrZero(txtYear.getText().trim());
        int copies = Math.max(1, parseIntOrZero(txtCopies.getText().trim()));

        String sql = "UPDATE books SET isbn=?, title=?, author=?, genre=?, publisher=?, year=?, copies=?, available=available + (? - copies) WHERE id=?";
        // Note: update available to shift by difference between new copies and old copies in DB is complex.
        // For simplicity, we'll set available = greatest(0, available + diff). But here we try a reasonable approach:
        try (Connection c = getConnection()) {
            // fetch old copies
            int oldCopies = 0;
            try (PreparedStatement ps = c.prepareStatement("SELECT copies, available FROM books WHERE id=?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        oldCopies = rs.getInt("copies");
                        int oldAvailable = rs.getInt("available");
                        int diff = copies - oldCopies;
                        int newAvailable = oldAvailable + diff;
                        if (newAvailable < 0) newAvailable = 0;

                        String upd = "UPDATE books SET isbn=?, title=?, author=?, genre=?, publisher=?, year=?, copies=?, available=? WHERE id=?";
                        try (PreparedStatement p2 = c.prepareStatement(upd)) {
                            p2.setString(1, isbn.isEmpty() ? null : isbn);
                            p2.setString(2, title);
                            p2.setString(3, author);
                            p2.setString(4, genre);
                            p2.setString(5, publisher);
                            if (year == 0) p2.setNull(6, Types.INTEGER); else p2.setInt(6, year);
                            p2.setInt(7, copies);
                            p2.setInt(8, newAvailable);
                            p2.setInt(9, id);
                            p2.executeUpdate();
                        }
                    } else {
                        showErr("Book not found.");
                        return;
                    }
                }
            }
            showMsg("Book updated");
            loadBooksToTable("");
            populateIssueCombos();
            clearBookForm();
        } catch (SQLException ex) {
            showErr("Failed to update book: " + ex.getMessage());
        }
    }

    private void deleteSelectedBook() {
        int sel = tblBooks.getSelectedRow();
        if (sel == -1) { showErr("Select a book to delete"); return; }
        int id = Integer.parseInt(booksTableModel.getValueAt(sel, 0).toString());
        int resp = JOptionPane.showConfirmDialog(this, "Delete selected book? This will remove all records referencing it.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (resp != JOptionPane.YES_OPTION) return;

        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM books WHERE id=?")) {
            p.setInt(1, id);
            p.executeUpdate();
            showMsg("Book deleted");
            loadBooksToTable("");
            populateIssueCombos();
        } catch (SQLException ex) {
            showErr("Failed to delete: " + ex.getMessage());
        }
    }

    private void loadBooksToTable(String searchFilter) {
        booksTableModel.setRowCount(0);
        String genreFilter = comboFilterGenre != null ? (String) comboFilterGenre.getSelectedItem() : "All";
        String sql = "SELECT id,isbn,title,author,genre,publisher,year,copies,available FROM books WHERE 1=1";
        if (searchFilter != null && !searchFilter.isEmpty()) {
            sql += " AND (title LIKE ? OR author LIKE ? OR isbn LIKE ?)";
        }
        if (!"All".equals(genreFilter)) sql += " AND genre = ?";
        sql += " ORDER BY title ASC";
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            int idx = 1;
            if (searchFilter != null && !searchFilter.isEmpty()) {
                String s = "%" + searchFilter + "%";
                p.setString(idx++, s);
                p.setString(idx++, s);
                p.setString(idx++, s);
            }
            if (!"All".equals(genreFilter)) p.setString(idx++, genreFilter);

            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("id"));
                    row.add(rs.getString("isbn"));
                    row.add(rs.getString("title"));
                    row.add(rs.getString("author"));
                    row.add(rs.getString("genre"));
                    row.add(rs.getString("publisher"));
                    row.add(rs.getObject("year") == null ? "" : rs.getInt("year"));
                    row.add(rs.getInt("copies"));
                    row.add(rs.getInt("available"));
                    booksTableModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            showErr("Failed to load books: " + ex.getMessage());
        }
    }

    private void populateBookFormFromSelection() {
        int sel = tblBooks.getSelectedRow();
        if (sel == -1) return;
        txtIsbn.setText(String.valueOf(booksTableModel.getValueAt(sel, 1)));
        txtTitle.setText(String.valueOf(booksTableModel.getValueAt(sel, 2)));
        txtAuthor.setText(String.valueOf(booksTableModel.getValueAt(sel, 3)));
        comboGenre.setSelectedItem(String.valueOf(booksTableModel.getValueAt(sel, 4)));
        txtPublisher.setText(String.valueOf(booksTableModel.getValueAt(sel, 5)));
        txtYear.setText(String.valueOf(booksTableModel.getValueAt(sel, 6)));
        txtCopies.setText(String.valueOf(booksTableModel.getValueAt(sel, 7)));
    }

    private void clearBookForm() {
        txtIsbn.setText("");
        txtTitle.setText("");
        txtAuthor.setText("");
        comboGenre.setSelectedIndex(0);
        txtPublisher.setText("");
        txtYear.setText("");
        txtCopies.setText("1");
    }

    // Members
    private void addMember() {
        String name = memName.getText().trim();
        String contact = memContact.getText().trim();
        String email = memEmail.getText().trim();
        if (name.isEmpty()) { showErr("Member name required"); return; }

        String sql = "INSERT INTO members (name, contact, email) VALUES (?,?,?)";
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, name); p.setString(2, contact); p.setString(3, email);
            p.executeUpdate();
            showMsg("Member added");
            loadMembersToTable("");
            clearMemberForm();
            populateIssueCombos();
        } catch (SQLException ex) {
            showErr("Failed to add member: " + ex.getMessage());
        }
    }

    private void updateSelectedMember() {
        int sel = tblMembers.getSelectedRow();
        if (sel == -1) { showErr("Select a member to update"); return; }
        int id = Integer.parseInt(membersTableModel.getValueAt(sel, 0).toString());
        String name = memName.getText().trim();
        String contact = memContact.getText().trim();
        String email = memEmail.getText().trim();
        if (name.isEmpty()) { showErr("Name required"); return; }
        String sql = "UPDATE members SET name=?, contact=?, email=? WHERE id=?";
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, name); p.setString(2, contact); p.setString(3, email); p.setInt(4, id);
            p.executeUpdate();
            showMsg("Member updated");
            loadMembersToTable("");
            populateIssueCombos();
        } catch (SQLException ex) {
            showErr("Failed to update: " + ex.getMessage());
        }
    }

    private void deleteSelectedMember() {
        int sel = tblMembers.getSelectedRow();
        if (sel == -1) { showErr("Select a member to delete"); return; }
        int id = Integer.parseInt(membersTableModel.getValueAt(sel, 0).toString());
        int r = JOptionPane.showConfirmDialog(this, "Delete member? Related transactions remain but member reference will be set to NULL.", "Confirm", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM members WHERE id=?")) {
            p.setInt(1, id); p.executeUpdate();
            showMsg("Member deleted");
            loadMembersToTable("");
            populateIssueCombos();
        } catch (SQLException ex) {
            showErr("Failed to delete member: " + ex.getMessage());
        }
    }

    private void loadMembersToTable(String filter) {
        membersTableModel.setRowCount(0);
        String sql = "SELECT id, name, contact, email FROM members WHERE 1=1";
        if (filter != null && !filter.isEmpty()) sql += " AND (name LIKE ? OR contact LIKE ? OR email LIKE ?)";
        sql += " ORDER BY name";
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            if (filter != null && !filter.isEmpty()) {
                String s = "%" + filter + "%";
                p.setString(1, s); p.setString(2, s); p.setString(3, s);
            }
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> r = new Vector<>();
                    r.add(rs.getInt("id"));
                    r.add(rs.getString("name"));
                    r.add(rs.getString("contact"));
                    r.add(rs.getString("email"));
                    membersTableModel.addRow(r);
                }
            }
        } catch (SQLException ex) {
            showErr("Failed to load members: " + ex.getMessage());
        }
    }

    private void populateMemberFormFromSelection() {
        int sel = tblMembers.getSelectedRow();
        if (sel == -1) return;
        memName.setText(String.valueOf(membersTableModel.getValueAt(sel,1)));
        memContact.setText(String.valueOf(membersTableModel.getValueAt(sel,2)));
        memEmail.setText(String.valueOf(membersTableModel.getValueAt(sel,3)));
    }

    private void clearMemberForm() {
        memName.setText(""); memContact.setText(""); memEmail.setText("");
    }

    // Transactions
    private void populateIssueCombos() {
        comboIssueBook.removeAllItems();
        comboIssueMember.removeAllItems();
        // Load available books
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement("SELECT id,isbn,title,available FROM books ORDER BY title")) {
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int avail = rs.getInt("available");
                    String isbn = rs.getString("isbn");
                    String title = rs.getString("title");
                    String label = id + " - " + (isbn==null?"":isbn) + " - " + title + " (avail:" + avail + ")";
                    comboIssueBook.addItem(label);
                }
            }
        } catch (SQLException ex) {
            showErr("Failed to populate books: " + ex.getMessage());
        }
        // Load members
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement("SELECT id, name FROM members ORDER BY name")) {
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    comboIssueMember.addItem(id + " - " + name);
                }
            }
        } catch (SQLException ex) {
            showErr("Failed to populate members: " + ex.getMessage());
        }
    }

    private void issueBook() {
        String bookSel = (String) comboIssueBook.getSelectedItem();
        String memSel = (String) comboIssueMember.getSelectedItem();
        if (bookSel == null || memSel == null) { showErr("Select both book and member"); return; }

        int bookId = Integer.parseInt(bookSel.split(" - ")[0].trim());
        int memberId = Integer.parseInt(memSel.split(" - ")[0].trim());

        try (Connection c = getConnection()) {
            // check availability
            try (PreparedStatement p = c.prepareStatement("SELECT available FROM books WHERE id=?")) {
                p.setInt(1, bookId);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) {
                        int avail = rs.getInt("available");
                        if (avail <= 0) { showErr("Selected book is not available."); return; }
                    } else { showErr("Book not found."); return; }
                }
            }

            // create transaction
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO transactions (book_id, member_id, issue_date, returned) VALUES (?,?,NOW(),FALSE)")) {
                ps.setInt(1, bookId); ps.setInt(2, memberId); ps.executeUpdate();
            }

            // decrement available
            try (PreparedStatement pu = c.prepareStatement("UPDATE books SET available = available - 1 WHERE id=?")) {
                pu.setInt(1, bookId); pu.executeUpdate();
            }

            showMsg("Book issued successfully.");
            populateIssueCombos();
        } catch (SQLException ex) {
            showErr("Issue failed: " + ex.getMessage());
        }
    }

    private void markReturned(int txnId) {
        try (Connection c = getConnection()) {
            int bookId = -1;
            try (PreparedStatement p = c.prepareStatement("SELECT book_id, returned FROM transactions WHERE id=?")) {
                p.setInt(1, txnId);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) {
                        bookId = rs.getInt("book_id");
                        if (rs.getBoolean("returned")) { showErr("Already returned"); return; }
                    } else { showErr("Transaction not found"); return; }
                }
            }

            try (PreparedStatement p2 = c.prepareStatement("UPDATE transactions SET returned=TRUE, return_date=NOW() WHERE id=?")) {
                p2.setInt(1, txnId); p2.executeUpdate();
            }

            if (bookId != -1) {
                try (PreparedStatement p3 = c.prepareStatement("UPDATE books SET available = available + 1 WHERE id=?")) {
                    p3.setInt(1, bookId); p3.executeUpdate();
                }
            }
            showMsg("Marked returned.");
        } catch (SQLException ex) {
            showErr("Return failed: " + ex.getMessage());
        }
    }

    private void loadTransactionsToTable(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT t.id, b.title, m.name, t.issue_date, t.return_date, t.returned " +
                "FROM transactions t LEFT JOIN books b ON t.book_id=b.id LEFT JOIN members m ON t.member_id=m.id ORDER BY t.issue_date DESC";
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet rs = p.executeQuery()) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            while (rs.next()) {
                Vector<Object> r = new Vector<>();
                r.add(rs.getInt(1));
                r.add(rs.getString(2));
                r.add(rs.getString(3));
                Timestamp issueTs = rs.getTimestamp(4);
                Timestamp retTs = rs.getTimestamp(5);
                r.add(issueTs == null ? "" : issueTs.toLocalDateTime().format(fmt));
                r.add(retTs == null ? "" : retTs.toLocalDateTime().format(fmt));
                r.add(rs.getBoolean(6));
                model.addRow(r);
            }
        } catch (SQLException ex) {
            showErr("Failed to load transactions: " + ex.getMessage());
        }
    }

    // ------------------ Utilities ------------------
    private int parseIntOrZero(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private void showErr(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
    private void showMsg(String msg) { JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE); }

    // ------------------ Main ------------------
    public static void main(String[] args) {
        // load JDBC driver (optional with modern drivers but safe)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new LibraryManagementApp().setVisible(true);
        });
    }

    // ---------- Simple gradient panel for nicer UI ----------
    private static class GradientPanel extends JPanel {
        private final Color c1, c2;
        GradientPanel(Color c1, Color c2) { this.c1 = c1; this.c2 = c2; setOpaque(true); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, c1, w, h, c2));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }
}
