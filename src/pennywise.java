// These imports give the program tools for windows, tables, dates, files, and money.
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

/**
 * PennyWise - a beginner-friendly Java Swing personal finance manager.
 */

// This class creates the PennyPilot desktop window.
public class PennyWise extends JFrame {
    // This is the file where transactions are saved.
    private static final String DATA_FILE = "pennypilot-transactions.csv";
    // This list keeps all income and expense records while the app is running.
    private final List<Transaction> transactions = new ArrayList<Transaction>();
    // CardLayout lets the app switch between pages.
    private final CardLayout cardLayout = new CardLayout();
    // This panel holds the dashboard, transactions, and budget pages.
    private final JPanel content = new JPanel(cardLayout);

    // These labels show the totals and summaries on the screen.
    private JLabel incomeValue;
    private JLabel expenseValue;
    private JLabel balanceValue;
    private JLabel budgetValue;
    private JLabel transactionCountValue;
    private JLabel categorySummary;
    private DefaultTableModel recentModel;
    private DefaultTableModel transactionModel;
    private JTable transactionTable;
    private JTextField searchField;
    private JComboBox<String> filterType;

    // These colours keep the app design consistent.
    private final Color NAVY = new Color(54, 35, 98);
    private final Color BLUE = new Color(88, 105, 230);
    private final Color BACKGROUND = new Color(245, 247, 251);
    private final Color TEXT = new Color(35, 45, 65);
    private final Color MUTED = new Color(112, 123, 143);
    private final Color GREEN = new Color(25, 145, 100);
    private final Color RED = new Color(210, 75, 85);

    // The constructor prepares the window and loads saved information.
    public PennyWise() {
        super("PennyWise | Personal Finance");
        // Load previous transactions before showing the pages.
        loadTransactions();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 650));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        // Add the navigation menu on the left.
        add(buildSidebar(), BorderLayout.WEST);
        // Add the main page area beside the menu.
        add(buildMainArea(), BorderLayout.CENTER);
        // Update the totals and tables after setup.
        refreshAll();
    }

    // Build the left menu with buttons for each page.
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(215, 0));
        sidebar.setBorder(new EmptyBorder(22, 14, 18, 14));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("PennyWise");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 24));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Your money, in focus");
        tagline.setForeground(new Color(178, 190, 213));
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tagline.setBorder(new EmptyBorder(5, 0, 28, 0));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(brand);
        sidebar.add(tagline);
        // Each menu button opens the page with the matching name.
        sidebar.add(navButton("Dashboard", "dashboard"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("Transactions", "transactions"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("Budgets & Insights", "budgets"));
        sidebar.add(Box.createVerticalGlue());

        JLabel local = new JLabel("<html><div style='width:155px'>LOCAL MODE<br><br>Your transactions are saved on this computer.</div></html>");
        local.setForeground(new Color(178, 190, 213));
        local.setFont(new Font("SansSerif", Font.PLAIN, 11));
        local.setBorder(new EmptyBorder(10, 5, 8, 5));
        local.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(local);
        return sidebar;
    }

    // Create a navigation button and tell it which page to open.
    private JButton navButton(String label, final String page) {
        JButton button = new JButton(label);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setPreferredSize(new Dimension(185, 42));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(NAVY);
        button.setBorder(new EmptyBorder(0, 14, 0, 8));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Run this code when the user clicks the button.
        button.addActionListener(e -> {
            cardLayout.show(content, page);
            if ("transactions".equals(page)) refreshTransactionTable();
            refreshAll();
        });
        // Change the button colour when the mouse moves over it.
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(new Color(42, 57, 88)); }
            public void mouseExited(MouseEvent e) { button.setBackground(NAVY); }
        });
        return button;
    }

    // Build the top heading and the area where pages appear.
    private JPanel buildMainArea() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BACKGROUND);
        wrapper.setBorder(new EmptyBorder(22, 24, 24, 24));

        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setOpaque(false);
        JLabel title = new JLabel("Personal Finance Overview");
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        title.setForeground(TEXT);
        JLabel date = new JLabel(LocalDate.now().toString());
        date.setFont(new Font("SansSerif", Font.PLAIN, 12));
        date.setForeground(MUTED);
        topbar.add(title, BorderLayout.WEST);
        topbar.add(date, BorderLayout.EAST);
        topbar.setBorder(new EmptyBorder(0, 0, 18, 0));

        content.setOpaque(false);
        // Register each page with a name for navigation.
        content.add(buildDashboardPage(), "dashboard");
        content.add(buildTransactionsPage(), "transactions");
        content.add(buildBudgetsPage(), "budgets");

        wrapper.add(topbar, BorderLayout.NORTH);
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    // Build the dashboard with totals, quick actions, and recent records.
    private JPanel buildDashboardPage() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));

        // Arrange the three money summary cards in one row.
        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0));
        cards.setOpaque(false);
        incomeValue = new JLabel("R0.00");
        expenseValue = new JLabel("R0.00");
        balanceValue = new JLabel("R0.00");
        cards.add(statCard("TOTAL INCOME", incomeValue, GREEN, "Money received"));
        cards.add(statCard("TOTAL EXPENSES", expenseValue, RED, "Money spent"));
        cards.add(statCard("AVAILABLE BALANCE", balanceValue, BLUE, "Income minus expenses"));
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        cards.setPreferredSize(new Dimension(700, 125));

        // Place category spending and quick actions side by side.
        JPanel middle = new JPanel(new GridLayout(1, 2, 14, 0));
        middle.setOpaque(false);
        JPanel spending = panelCard("Spending by category");
        categorySummary = new JLabel("<html><div style='padding:8px'>No expenses yet. Add a transaction to see your spending.</div></html>");
        categorySummary.setForeground(TEXT);
        categorySummary.setFont(new Font("SansSerif", Font.PLAIN, 13));
        spending.add(categorySummary, BorderLayout.CENTER);

        JPanel quick = panelCard("Quick actions");
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        JButton addExpense = primaryButton("+ Add transaction");
        addExpense.setAlignmentX(Component.LEFT_ALIGNMENT);
        addExpense.addActionListener(e -> showAddTransactionDialog());
        JButton viewAll = secondaryButton("View all transactions");
        viewAll.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewAll.addActionListener(e -> {
            cardLayout.show(content, "transactions");
            refreshTransactionTable();
        });
        actions.add(addExpense);
        actions.add(Box.createVerticalStrut(10));
        actions.add(viewAll);
        quick.add(actions, BorderLayout.CENTER);
        middle.add(spending);
        middle.add(quick);
        middle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 235));

        // Show a table of the latest transactions.
        JPanel recent = panelCard("Recent transactions");
        recentModel = new DefaultTableModel(new Object[]{"Date", "Description", "Category", "Type", "Amount"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable recentTable = styledTable(recentModel);
        recent.add(new JScrollPane(recentTable), BorderLayout.CENTER);
        recent.setPreferredSize(new Dimension(800, 230));
        recent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 270));

        page.add(cards);
        page.add(Box.createVerticalStrut(16));
        page.add(middle);
        page.add(Box.createVerticalStrut(16));
        page.add(recent);
        return page;
    }

    // Build the page for searching, filtering, and deleting transactions.
    private JPanel buildTransactionsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 14));
        page.setOpaque(false);

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel label = new JLabel("Transactions");
        label.setFont(new Font("SansSerif", Font.BOLD, 20));
        label.setForeground(TEXT);
        JButton add = primaryButton("+ Add transaction");
        add.addActionListener(e -> showAddTransactionDialog());
        heading.add(label, BorderLayout.WEST);
        heading.add(add, BorderLayout.EAST);

        JPanel tools = new JPanel(new BorderLayout(10, 0));
        tools.setOpaque(false);
        searchField = new JTextField();
        searchField.setToolTipText("Search description or category");
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(250, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 225, 234)),
            new EmptyBorder(7, 10, 7, 10)));
        // Refresh the table whenever the user types in the search box.
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { refreshTransactionTable(); }
        });
        // Let the user show all records, income only, or expenses only.
        filterType = new JComboBox<String>(new String[]{"All types", "Income", "Expense"});
        filterType.setPreferredSize(new Dimension(140, 36));
        filterType.addActionListener(e -> refreshTransactionTable());
        tools.add(searchField, BorderLayout.CENTER);
        tools.add(filterType, BorderLayout.EAST);

        transactionModel = new DefaultTableModel(new Object[]{"Date", "Description", "Category", "Type", "Amount"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        transactionTable = styledTable(transactionModel);
        JScrollPane tableScroll = new JScrollPane(transactionTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 229, 237)));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);
        JButton delete = secondaryButton("Delete selected");
        delete.addActionListener(e -> deleteSelectedTransaction());
        bottom.add(delete);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(tools, BorderLayout.CENTER);
        page.add(top, BorderLayout.NORTH);
        page.add(tableScroll, BorderLayout.CENTER);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    // Build the page where the user sets a monthly spending limit.
    private JPanel buildBudgetsPage() {
        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));

        JPanel intro = panelCard("Budgets & insights");
        JLabel description = new JLabel("<html><div style='width:560px'>Set a monthly spending limit and PennyPilot will compare it with your expenses for the current month. This starter version uses one overall monthly budget.</div></html>");
        description.setFont(new Font("SansSerif", Font.PLAIN, 13));
        description.setForeground(MUTED);
        intro.add(description, BorderLayout.CENTER);

        JPanel budget = panelCard("Monthly budget");
        budgetValue = new JLabel("Not set");
        budgetValue.setFont(new Font("SansSerif", Font.BOLD, 22));
        budgetValue.setForeground(BLUE);
        budget.add(budgetValue, BorderLayout.NORTH);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        form.setOpaque(false);
        JTextField amount = new JTextField(12);
        amount.setText("5000.00");
        JButton set = primaryButton("Set budget");
        JLabel result = new JLabel(" ");
        result.setForeground(MUTED);
        // Check and save the budget when the button is clicked.
        set.addActionListener(e -> {
            try {
                BigDecimal limit = new BigDecimal(amount.getText().trim());
                if (limit.signum() <= 0) throw new NumberFormatException();
                saveBudget(limit);
                refreshBudgetSummary();
                result.setText("Budget saved.");
                result.setForeground(GREEN);
            // Show a helpful message if the amount is not a number.
        } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid budget greater than zero.", "Invalid budget", JOptionPane.WARNING_MESSAGE);
            }
        });
        form.add(new JLabel("Monthly limit (R):"));
        form.add(amount);
        form.add(set);
        form.add(result);
        budget.add(form, BorderLayout.CENTER);

        JPanel tips = panelCard("Helpful insights");
        JLabel tipText = new JLabel("<html><div style='width:560px;line-height:1.5'>• Record expenses as soon as possible.<br>• Review categories where spending is highest.<br>• Keep an emergency buffer in your budget.<br>• This app is a local portfolio demo, not a bank or financial-advice service.</div></html>");
        tipText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tipText.setForeground(TEXT);
        tips.add(tipText, BorderLayout.CENTER);

        intro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        budget.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        tips.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        page.add(intro);
        page.add(Box.createVerticalStrut(14));
        page.add(budget);
        page.add(Box.createVerticalStrut(14));
        page.add(tips);
        return page;
    }

    // Create one reusable summary card for a money total.
    private JPanel statCard(String title, JLabel value, Color accent, String caption) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 233, 241)),
            new EmptyBorder(16, 16, 14, 16)));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 10));
        heading.setForeground(MUTED);
        value.setFont(new Font("SansSerif", Font.BOLD, 23));
        value.setForeground(accent);
        JLabel sub = new JLabel(caption);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(MUTED);
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(value);
        body.add(Box.createVerticalStrut(4));
        body.add(sub);
        card.add(heading, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    // Create a white panel with a heading.
    private JPanel panelCard(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 233, 241)),
            new EmptyBorder(16, 16, 16, 16)));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 15));
        heading.setForeground(TEXT);
        panel.add(heading, BorderLayout.NORTH);
        return panel;
    }

    // Create a main button with the app's blue style.
    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(BLUE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 15, 10, 15));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Create a secondary button with a light background.
    private JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(TEXT);
        b.setBackground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(215, 221, 232)),
            new EmptyBorder(9, 13, 9, 13)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Apply the same appearance to a table wherever it is used.
    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setGridColor(new Color(235, 238, 244));
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(226, 234, 255));
        table.setSelectionForeground(TEXT);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(new Color(249, 250, 252));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
        return table;
    }

    // Show a form where the user enters a new income or expense.
    private void showAddTransactionDialog() {
        JTextField description = new JTextField(18);
        JTextField amount = new JTextField(12);
        JComboBox<String> type = new JComboBox<String>(new String[]{"Expense", "Income"});
        JComboBox<String> category = new JComboBox<String>(new String[]{
            "Food", "Transport", "Housing", "Utilities", "Healthcare",
            "Education", "Entertainment", "Shopping", "Salary", "Freelance", "Other"
        });
        JTextField date = new JTextField(LocalDate.now().toString(), 12);

        // Arrange the form labels and fields in two columns.
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 5, 7, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Description"), c);
        c.gridx = 1; form.add(description, c);
        c.gridx = 0; c.gridy++; form.add(new JLabel("Amount (R)"), c);
        c.gridx = 1; form.add(amount, c);
        c.gridx = 0; c.gridy++; form.add(new JLabel("Type"), c);
        c.gridx = 1; form.add(type, c);
        c.gridx = 0; c.gridy++; form.add(new JLabel("Category"), c);
        c.gridx = 1; form.add(category, c);
        c.gridx = 0; c.gridy++; form.add(new JLabel("Date (YYYY-MM-DD)"), c);
        c.gridx = 1; form.add(date, c);

        // Ask the user to save the form or cancel it.
        int result = JOptionPane.showConfirmDialog(this, form, "Add transaction",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        // Validate the form before creating a transaction object.
        try {
            String desc = description.getText().trim();
            if (desc.isEmpty()) throw new IllegalArgumentException("Please enter a description.");
            BigDecimal value = new BigDecimal(amount.getText().trim());
            if (value.signum() <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
            LocalDate transactionDate = LocalDate.parse(date.getText().trim());
            String transactionType = (String) type.getSelectedItem();
            String transactionCategory = (String) category.getSelectedItem();

            // Use the selected type to create the correct transaction object.
            Transaction transaction;
            if ("Income".equals(transactionType)) {
                transaction = new IncomeTransaction(desc, value, transactionCategory, transactionDate);
            } else {
                transaction = new ExpenseTransaction(desc, value, transactionCategory, transactionDate);
            }
            // Add the new record, save it, and refresh the screen.
            transactions.add(transaction);
            saveTransactions();
            refreshAll();
            JOptionPane.showMessageDialog(this, "Transaction saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid amount, for example 125.50.", "Invalid amount", JOptionPane.WARNING_MESSAGE);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Enter the date in YYYY-MM-DD format.", "Invalid date", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check your input", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Delete the row selected by the user after confirmation.
    private void deleteSelectedTransaction() {
        int row = transactionTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a transaction to delete.", "Nothing selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String desc = String.valueOf(transactionModel.getValueAt(row, 1));
        String date = String.valueOf(transactionModel.getValueAt(row, 0));
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete \"" + desc + "\" dated " + date + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Find the matching record in the full list using its details.
        String amount = String.valueOf(transactionModel.getValueAt(row, 4));
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction t = transactions.get(i);
            if (t.getDate().toString().equals(date) &&
                t.getDescription().equals(desc) &&
                formatMoney(t.getAmount()).equals(amount) &&
                (t instanceof IncomeTransaction ? "Income" : "Expense").equals(String.valueOf(transactionModel.getValueAt(row, 3)))) {
                transactions.remove(i);
                break;
            }
        }
        saveTransactions();
        refreshAll();
    }

    // Recalculate totals and refresh every summary and table.
    private void refreshAll() {
        // Start both totals at zero before adding the records.
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;
        // Go through every transaction and add it to the right total.
        for (Transaction t : transactions) {
            if (t instanceof IncomeTransaction) income = income.add(t.getAmount());
            else expenses = expenses.add(t.getAmount());
        }
        if (incomeValue != null) incomeValue.setText(formatMoney(income));
        if (expenseValue != null) expenseValue.setText(formatMoney(expenses));
        if (balanceValue != null) {
            balanceValue.setText(formatMoney(income.subtract(expenses)));
            balanceValue.setForeground(income.subtract(expenses).signum() < 0 ? RED : BLUE);
        }
        refreshRecentTable();
        refreshTransactionTable();
        refreshCategorySummary();
        refreshBudgetSummary();
    }

    // Show up to five transactions, newest first.
    private void refreshRecentTable() {
        if (recentModel == null) return;
        recentModel.setRowCount(0);
        // Copy the list so it can be sorted for display.
        List<Transaction> sorted = new ArrayList<Transaction>(transactions);
        Collections.sort(sorted, (a, b) -> b.getDate().compareTo(a.getDate()));
        for (int i = 0; i < Math.min(5, sorted.size()); i++) {
            Transaction t = sorted.get(i);
            recentModel.addRow(new Object[]{t.getDate(), t.getDescription(), t.getCategory(),
                t instanceof IncomeTransaction ? "Income" : "Expense",
                (t instanceof IncomeTransaction ? "+" : "-") + formatMoney(t.getAmount())});
        }
    }

    // Rebuild the transaction table using the search and type filter.
    private void refreshTransactionTable() {
        if (transactionModel == null) return;
        transactionModel.setRowCount(0);
        // Read the search text safely, even during page setup.
        String search = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedType = filterType == null ? "All types" : String.valueOf(filterType.getSelectedItem());
        List<Transaction> sorted = new ArrayList<Transaction>(transactions);
        Collections.sort(sorted, (a, b) -> b.getDate().compareTo(a.getDate()));
        for (Transaction t : sorted) {
            String type = t instanceof IncomeTransaction ? "Income" : "Expense";
            // Skip records that do not match the selected type.
            if (!"All types".equals(selectedType) && !selectedType.equals(type)) continue;
            // Skip records that do not match the search text.
            if (!search.isEmpty() &&
                !t.getDescription().toLowerCase(Locale.ROOT).contains(search) &&
                !t.getCategory().toLowerCase(Locale.ROOT).contains(search)) continue;
            transactionModel.addRow(new Object[]{t.getDate(), t.getDescription(), t.getCategory(), type,
                (t instanceof IncomeTransaction ? "+" : "-") + formatMoney(t.getAmount())});
        }
    }

    // Add up this month's expenses for each category.
    private void refreshCategorySummary() {
        if (categorySummary == null) return;
        // Store each category name with its total spending.
        Map<String, BigDecimal> totals = new TreeMap<String, BigDecimal>();
        YearMonth month = YearMonth.now();
        for (Transaction t : transactions) {
            if (t instanceof ExpenseTransaction && YearMonth.from(t.getDate()).equals(month)) {
                BigDecimal previous = totals.containsKey(t.getCategory()) ? totals.get(t.getCategory()) : BigDecimal.ZERO;
                totals.put(t.getCategory(), previous.add(t.getAmount()));
            }
        }
        if (totals.isEmpty()) {
            categorySummary.setText("<html><div style='padding:8px'>No expenses recorded this month.<br>Add an expense to see category totals.</div></html>");
            return;
        }
        StringBuilder html = new StringBuilder("<html><div style='padding:6px'>");
        for (Map.Entry<String, BigDecimal> entry : totals.entrySet()) {
            html.append("<b>").append(escapeHtml(entry.getKey())).append("</b>")
                .append("<span style='color:#718096'> &nbsp; ")
                .append(formatMoney(entry.getValue())).append("</span><br><br>");
        }
        html.append("</div></html>");
        categorySummary.setText(html.toString());
    }

    // Compare this month's expenses with the saved budget.
    private void refreshBudgetSummary() {
        if (budgetValue == null) return;
        BigDecimal budget = loadBudget();
        BigDecimal expenses = BigDecimal.ZERO;
        YearMonth month = YearMonth.now();
        for (Transaction t : transactions) {
            if (t instanceof ExpenseTransaction && YearMonth.from(t.getDate()).equals(month)) {
                expenses = expenses.add(t.getAmount());
            }
        }
        if (budget == null) {
            budgetValue.setText("No monthly budget set");
            return;
        }
        BigDecimal remaining = budget.subtract(expenses);
        budgetValue.setText(formatMoney(expenses) + " spent of " + formatMoney(budget));
        budgetValue.setForeground(remaining.signum() < 0 ? RED : BLUE);
    }

    // Save the monthly budget to a small text file.
    private void saveBudget(BigDecimal budget) {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream("pennypilot-budget.txt"), StandardCharsets.UTF_8))) {
            out.println(budget.toPlainString());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save budget: " + ex.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Read the saved budget, or return null if none is available.
    private BigDecimal loadBudget() {
        File file = new File("pennypilot-budget.txt");
        if (!file.exists()) return null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = in.readLine();
            return line == null ? null : new BigDecimal(line.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    // Write every transaction to the CSV file.
    private void saveTransactions() {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(DATA_FILE), StandardCharsets.UTF_8))) {
            // Write column names at the top of the file.
            out.println("type,date,description,category,amount");
            for (Transaction t : transactions) {
                String type = t instanceof IncomeTransaction ? "INCOME" : "EXPENSE";
                out.println(type + "," + t.getDate() + "," + csv(t.getDescription()) + "," +
                    csv(t.getCategory()) + "," + t.getAmount().toPlainString());
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save transactions: " + ex.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Load saved transactions when the app starts.
    private void loadTransactions() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            // Read the file one line at a time.
            while ((line = in.readLine()) != null) {
                if (header) { header = false; continue; }
                String[] parts = parseCsvLine(line);
                if (parts.length != 5) continue;
                try {
                    String type = parts[0];
                    LocalDate date = LocalDate.parse(parts[1]);
                    String description = parts[2];
                    String category = parts[3];
                    BigDecimal amount = new BigDecimal(parts[4]);
                    if (amount.signum() <= 0) continue;
                    if ("INCOME".equals(type)) transactions.add(new IncomeTransaction(description, amount, category, date));
                    else if ("EXPENSE".equals(type)) transactions.add(new ExpenseTransaction(description, amount, category, date));
                } catch (Exception ignored) { /* Ignore this damaged row and continue loading. */ }
            }
        } catch (IOException ex) {
            System.err.println("Could not load transactions: " + ex.getMessage());
        }
    }

    // Split one CSV line while keeping commas inside quoted text.
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        // Track whether the parser is inside quotation marks.
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            // Quotes tell the parser that a comma may belong to the text.
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"'); i++;
                } else quoted = !quoted;
            // A comma outside quotes marks the end of a field.
            } else if (ch == ',' && !quoted) {
                fields.add(current.toString()); current.setLength(0);
            } else current.append(ch);
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    // Wrap text in quotes and double any quotes inside it.
    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // Display a money value with an R symbol and two decimal places.
    private String formatMoney(BigDecimal value) {
        return "R" + value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    // Protect special characters before showing category names as HTML.
    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // Java starts running the application from this method.
    public static void main(String[] args) {
        // Create the window on Swing's UI thread.
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { /* Keep the default look if this setting is unavailable. */ }
            new PennyPilot().setVisible(true);
        });
    }

    // Encapsulation: transaction fields are private and are initialized by the constructor.
    // This parent class stores information shared by all transactions.
    public static abstract class Transaction {
        // These fields store the transaction details.
        private final String description;
        private final BigDecimal amount;
        private final String category;
        private final LocalDate date;

        // Store the details passed in when a transaction is created.
        public Transaction(String description, BigDecimal amount, String category, LocalDate date) {
            this.description = Objects.requireNonNull(description);
            this.amount = Objects.requireNonNull(amount);
            this.category = Objects.requireNonNull(category);
            this.date = Objects.requireNonNull(date);
        }

        // Getters let other parts of the app read the saved details.
        public String getDescription() { return description; }
        public BigDecimal getAmount() { return amount; }
        public String getCategory() { return category; }
        public LocalDate getDate() { return date; }
    }

    // Inheritance: IncomeTransaction and ExpenseTransaction extend Transaction.
    // This class represents money coming in.
    public static class IncomeTransaction extends Transaction {
        public IncomeTransaction(String description, BigDecimal amount, String category, LocalDate date) {
            super(description, amount, category, date);
        }
    }

    // This class represents money going out.
    public static class ExpenseTransaction extends Transaction {
        public ExpenseTransaction(String description, BigDecimal amount, String category, LocalDate date) {
            super(description, amount, category, date);
        }
    }
}
