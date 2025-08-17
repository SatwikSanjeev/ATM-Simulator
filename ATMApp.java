import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Enhanced ATM Simulator
 * - Multi-account types (Savings, Current)
 * - Daily limits, overdraft, loan simulation
 * - Receipt printing to .txt
 * - Interest on savings (simple monthly)
 * - Admin mode (unlock accounts, view all)
 * - Account lock after 3 wrong attempts
 * - Session timeout (auto-logout)
 * - Multi-language (English/Hindi)
 *
 * Test accounts:
 *  - 12345678 -> PIN 1234 (Savings+Current)
 *  - 87654321 -> PIN 4321 (Savings+Current)
 *  - 00000000 -> PIN 0000 (Admin)
 */
public class ATMApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::new);
    }
}

/* ---------- Localization ---------- */
enum Lang { EN, HI }

class Texts {
    private static Lang lang = Lang.EN;
    private static final Map<Lang, Map<String, String>> TEXT = new HashMap<>();

    static {
        Map<String, String> en = new HashMap<>();
        en.put("welcome", "Welcome to ATM");
        en.put("enter_card", "Enter Card Number:");
        en.put("enter_pin", "Enter PIN:");
        en.put("login", "Login");
        en.put("invalid_login", "Invalid Card Number or PIN!");
        en.put("balance", "Balance");
        en.put("deposit", "Deposit");
        en.put("withdraw", "Withdraw");
        en.put("mini_stmt", "Mini Statement");
        en.put("transfer", "Transfer");
        en.put("change_pin", "Change PIN");
        en.put("fast_cash", "Fast Cash");
        en.put("logout", "Logout");
        en.put("exit", "Exit");
        en.put("print_receipt", "Print Receipt");
        en.put("profile", "Profile");
        en.put("savings", "Savings");
        en.put("current", "Current");
        en.put("select_acc", "Select Account Type:");
        en.put("insufficient", "Insufficient Balance!");
        en.put("invalid_amount", "Invalid amount!");
        en.put("enter_amount_keypad", "Enter value using keypad!");
        en.put("transfer_success", "Transferred");
        en.put("pin_changed", "PIN changed successfully!");
        en.put("invalid_pin_format", "Invalid PIN format! Use 4 digits.");
        en.put("incorrect_old_pin", "Incorrect old PIN!");
        en.put("card_blocked", "Card Blocked! Contact Admin.");
        en.put("locked_info", "Account locked after 3 wrong attempts.");
        en.put("admin_unlocked", "Account unlocked by admin.");
        en.put("session_timeout", "Session timed out. Logging out.");
        en.put("loan_approved", "Loan approved (simulated) of ₹");
        en.put("loan_denied", "Loan request denied (limit).");
        en.put("apply_loan", "Apply for Loan");
        en.put("daily_withdraw_limit", "Daily withdrawal limit reached.");
        en.put("daily_transfer_limit", "Daily transfer limit reached.");
        en.put("language_toggle", "Toggle Language");
        en.put("admin_mode", "ADMIN MODE");
        en.put("unlock_account", "Unlock Account");
        en.put("view_accounts", "View All Accounts");
        en.put("save_profile", "Save Profile");
        en.put("print_receipt_file", "Receipt saved to: ");
        en.put("ok", "OK");
        en.put("cancel", "Cancel");
        en.put("insert_card", "Insert Card (Click)");
        en.put("masked_card", "**** **** ");
        en.put("admin_prompt", "Admin: Enter card to unlock:");
        TEXT.put(Lang.EN, en);

        Map<String, String> hi = new HashMap<>();
        hi.put("welcome", "एटीएम में आपका स्वागत है");
        hi.put("enter_card", "कार्ड नंबर दर्ज करें:");
        hi.put("enter_pin", "पिन दर्ज करें:");
        hi.put("login", "लॉगिन");
        hi.put("invalid_login", "अमान्य कार्ड नंबर या पिन!");
        hi.put("balance", "बैलेंस");
        hi.put("deposit", "जमा");
        hi.put("withdraw", "निकासी");
        hi.put("mini_stmt", "मिनी स्टेटमेंट");
        hi.put("transfer", "स्थानांतरण");
        hi.put("change_pin", "पिन बदलें");
        hi.put("fast_cash", "फास्ट कैश");
        hi.put("logout", "लॉगआउट");
        hi.put("exit", "बाहर");
        hi.put("print_receipt", "रसीद प्रिंट करें");
        hi.put("profile", "प्रोफ़ाइल");
        hi.put("savings", "सेविंग्स");
        hi.put("current", "करंट");
        hi.put("select_acc", "खाता प्रकार चुनें:");
        hi.put("insufficient", "पर्याप्त शेष नहीं!");
        hi.put("invalid_amount", "अमान्य राशि!");
        hi.put("enter_amount_keypad", "किपैड का उपयोग करके मान दर्ज करें!");
        hi.put("transfer_success", "स्थानांतरित");
        hi.put("pin_changed", "पिन सफलतापूर्वक बदल गया!");
        hi.put("invalid_pin_format", "पिन 4 अंकों का होना चाहिए।");
        hi.put("incorrect_old_pin", "गलत पुराना पिन!");
        hi.put("card_blocked", "कार्ड ब्लाक! एडमिन से संपर्क करें।");
        hi.put("locked_info", "3 गलत प्रयासों के बाद खाता लॉक कर दिया गया।");
        hi.put("admin_unlocked", "एडमिन द्वारा खाता अनलॉक किया गया।");
        hi.put("session_timeout", "सत्र समय समाप्त। लॉगआउट कर रहे हैं।");
        hi.put("loan_approved", "लोन स्वीकृत (सिम्युलेटेड) ₹");
        hi.put("loan_denied", "लोन अनुरोध अस्वीकृत।");
        hi.put("apply_loan", "लोन के लिए आवेदन करें");
        hi.put("daily_withdraw_limit", "दैनिक निकासी सीमा पूरी हो गई।");
        hi.put("daily_transfer_limit", "दैनिक ट्रांसफ़र सीमा पूरी हो गई।");
        hi.put("language_toggle", "भाषा बदलें");
        hi.put("admin_mode", "एडमिन मोड");
        hi.put("unlock_account", "खाता अनलॉक करें");
        hi.put("view_accounts", "सभी खाते देखें");
        hi.put("save_profile", "प्रोफ़ाइल सहेजें");
        hi.put("print_receipt_file", "रसीद सहेजी गई: ");
        hi.put("ok", "ठीक है");
        hi.put("cancel", "रद्द करें");
        hi.put("insert_card", "कार्ड डालें (क्लिक करें)");
        hi.put("masked_card", "**** **** ");
        hi.put("admin_prompt", "एडमिन: अनलॉक करने के लिए कार्ड दर्ज करें:");
        TEXT.put(Lang.HI, hi);
    }

    public static void setLang(Lang l) { lang = l; }
    public static String t(String key) { return TEXT.get(lang).getOrDefault(key, key); }
}

/* ---------- Account model ---------- */
class AccountType {
    public final String typeName;
    private double balance;
    private double overdraftLimit; // e.g., allow negative balance to -overdraftLimit
    private final ArrayList<String> history = new ArrayList<>();
    private double monthlyInterestPercent; // simple monthly interest for savings

    // Daily tracking
    private double dailyWithdrawn = 0;
    private double dailyTransferred = 0;
    private LocalDate lastReset = LocalDate.now();

    public static final double DEFAULT_DAILY_WITHDRAW_LIMIT = 20000;
    public static final double DEFAULT_DAILY_TRANSFER_LIMIT = 50000;

    private double dailyWithdrawLimit = DEFAULT_DAILY_WITHDRAW_LIMIT;
    private double dailyTransferLimit = DEFAULT_DAILY_TRANSFER_LIMIT;

    public AccountType(String typeName, double initialBalance, double overdraftLimit, double monthlyInterestPercent) {
        this.typeName = typeName;
        this.balance = initialBalance;
        this.overdraftLimit = overdraftLimit;
        this.monthlyInterestPercent = monthlyInterestPercent;
        history.add("Account (" + typeName + ") created: ₹" + initialBalance);
    }

    private void resetDailyIfNeeded() {
        LocalDate now = LocalDate.now();
        if (!now.equals(lastReset)) {
            dailyWithdrawn = 0;
            dailyTransferred = 0;
            lastReset = now;
        }
    }

    public synchronized double getBalance() {
        return balance;
    }

    public synchronized boolean deposit(double amt) {
        if (amt <= 0) return false;
        balance += amt;
        history.add(nowStamp() + " Deposited: ₹" + amt + " | Balance: ₹" + balance);
        return true;
    }

    public synchronized boolean withdraw(double amt) {
        resetDailyIfNeeded();
        if (amt <= 0) return false;
        if (dailyWithdrawn + amt > dailyWithdrawLimit) return false;
        if (balance - amt < -overdraftLimit) return false;
        balance -= amt;
        dailyWithdrawn += amt;
        history.add(nowStamp() + " Withdrawn: ₹" + amt + " | Balance: ₹" + balance);
        return true;
    }

    public synchronized boolean transferOut(double amt) {
        resetDailyIfNeeded();
        if (amt <= 0) return false;
        if (dailyTransferred + amt > dailyTransferLimit) return false;
        if (balance - amt < -overdraftLimit) return false;
        balance -= amt;
        dailyTransferred += amt;
        history.add(nowStamp() + " Transferred out: ₹" + amt + " | Balance: ₹" + balance);
        return true;
    }

    public synchronized void receiveTransfer(double amt, String fromCard) {
        balance += amt;
        history.add(nowStamp() + " Received ₹" + amt + " from " + fromCard + " | Balance: ₹" + balance);
    }

    public List<String> getHistory() { return history; }

    public void addHistory(String s) { history.add(nowStamp() + " " + s); }

    public void applyMonthlyInterest() {
        if (monthlyInterestPercent > 0) {
            double interest = balance * (monthlyInterestPercent / 100.0);
            balance += interest;
            history.add(nowStamp() + " Interest applied: ₹" + String.format("%.2f", interest));
        }
    }

    public void setDailyLimits(double withdrawLimit, double transferLimit) {
        this.dailyWithdrawLimit = withdrawLimit;
        this.dailyTransferLimit = transferLimit;
    }

    private String nowStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public boolean canWithdrawDaily(double amt) {
        resetDailyIfNeeded();
        return (dailyWithdrawn + amt) <= dailyWithdrawLimit && (balance - amt) >= -overdraftLimit;
    }

    public boolean canTransferDaily(double amt) {
        resetDailyIfNeeded();
        return (dailyTransferred + amt) <= dailyTransferLimit && (balance - amt) >= -overdraftLimit;
    }

    public double getDailyWithdrawn() { resetDailyIfNeeded(); return dailyWithdrawn; }
    public double getDailyTransferred() { resetDailyIfNeeded(); return dailyTransferred; }
}

/* A full ATM implementation for a card (can have multiple account types) */
class ATMImplementation {
    private final String cardNumber;
    private final Map<String, AccountType> accounts = new HashMap<>();
    private final ArrayList<String> profile = new ArrayList<>(); // simple profile storage: phone/email
    private String pin;
    private boolean locked = false;
    private int failedAttempts = 0;

    public ATMImplementation(String cardNumber, String pin) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        // by default create two account types: Savings and Current
        accounts.put("Savings", new AccountType("Savings", 5000.0, 0.0, 0.5)); // small monthly interest
        accounts.put("Current", new AccountType("Current", 5000.0, 5000.0, 0.0)); // overdraft allowed
        profile.add("Phone: -");
        profile.add("Email: -");
        getAny().addHistory("Account created with Savings & Current");
    }

    public String getCardNumber() { return cardNumber; }

    public boolean checkPin(String attempt) {
        if (locked) return false;
        if (pin.equals(attempt)) {
            failedAttempts = 0;
            return true;
        } else {
            failedAttempts++;
            if (failedAttempts >= 3) locked = true;
            return false;
        }
    }

    public void changePin(String newPin) { this.pin = newPin; }

    public boolean isLocked() { return locked; }
    public void unlock() { locked = false; failedAttempts = 0; getAny().addHistory("Account unlocked by admin"); }

    public Collection<String> accountTypes() { return accounts.keySet(); }

    public AccountType getAccount(String type) { return accounts.get(type); }

    // For convenience return any account (Savings preference)
    public AccountType getAny() {
        if (accounts.containsKey("Savings")) return accounts.get("Savings");
        return accounts.values().iterator().next();
    }

    public List<String> getProfile() { return profile; }
    public void setProfile(String phone, String email) {
        profile.clear();
        profile.add("Phone: " + phone);
        profile.add("Email: " + email);
        getAny().addHistory("Profile updated");
    }

    public boolean applyLoan(double amount, double maxLoanLimit) {
        // simple simulated loan: if requested <= maxLoanLimit, approve and deposit into Current
        if (amount <= maxLoanLimit) {
            AccountType cur = accounts.get("Current");
            if (cur != null) {
                cur.addHistory("Loan credited: ₹" + amount);
                cur.receiveTransfer(amount, "BankLoan");
                return true;
            }
        }
        return false;
    }
}

/* ---------- Login screen ---------- */
class LoginScreen extends JFrame {
    static final Map<String, String> ACCOUNTS = new HashMap<>(); // card -> pin (initial)
    static final Map<String, ATMImplementation> ACCOUNT_MODELS = new HashMap<>();
    static final Set<String> ADMIN_CARDS = new HashSet<>();

    // track blocked accounts persistently (for this session)
    static {
        // sample data
        ATMImplementation a1 = new ATMImplementation("12345678", "1234");
        a1.getAccount("Savings").deposit(5000); // boost
        a1.getAccount("Current").deposit(2000);

        ATMImplementation a2 = new ATMImplementation("87654321", "4321");
        a2.getAccount("Savings").deposit(8000);
        a2.getAccount("Current").deposit(3000);

        ATMImplementation admin = new ATMImplementation("00000000", "0000");

        ACCOUNTS.put("12345678", "1234");
        ACCOUNTS.put("87654321", "4321");
        ACCOUNTS.put("00000000", "0000");

        ACCOUNT_MODELS.put("12345678", a1);
        ACCOUNT_MODELS.put("87654321", a2);
        ACCOUNT_MODELS.put("00000000", admin);

        ADMIN_CARDS.add("00000000");
    }

    public LoginScreen() {
        super(Texts.t("welcome"));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(Color.BLACK);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        JLabel title = new JLabel(Texts.t("welcome"));
        title.setForeground(Color.GREEN);
        title.setFont(new Font("Monospaced", Font.BOLD, 36));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;
        JLabel cardLabel = new JLabel(Texts.t("enter_card"));
        cardLabel.setForeground(Color.GREEN);
        cardLabel.setFont(new Font("Monospaced", Font.PLAIN, 20));
        gbc.gridx = 0; gbc.gridy = 1;
        add(cardLabel, gbc);

        JTextField cardField = new JTextField(14);
        cardField.setFont(new Font("Monospaced", Font.PLAIN, 20));
        gbc.gridx = 1; add(cardField, gbc);

        JLabel pinLabel = new JLabel(Texts.t("enter_pin"));
        pinLabel.setForeground(Color.GREEN);
        pinLabel.setFont(new Font("Monospaced", Font.PLAIN, 20));
        gbc.gridx = 0; gbc.gridy = 2;
        add(pinLabel, gbc);

        JPasswordField pinField = new JPasswordField(14);
        pinField.setFont(new Font("Monospaced", Font.PLAIN, 20));
        gbc.gridx = 1; add(pinField, gbc);

        JButton loginBtn = new JButton(Texts.t("login"));
        loginBtn.setFont(new Font("Monospaced", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(loginBtn, gbc);

        JButton insertCardBtn = new JButton(Texts.t("insert_card"));
        insertCardBtn.setFont(new Font("Monospaced", Font.PLAIN, 16));
        gbc.gridy = 4;
        add(insertCardBtn, gbc);

        // language toggle
        JButton langBtn = new JButton(Texts.t("language_toggle"));
        langBtn.setFont(new Font("Monospaced", Font.PLAIN, 12));
        gbc.gridy = 5;
        add(langBtn, gbc);

        langBtn.addActionListener(e -> {
            // simpler toggle logic
            if (Texts.t("language_toggle").equals("भाषा बदलें")) {
                Texts.setLang(Lang.HI);
            } else if (Texts.t("language_toggle").equals("Toggle Language")) {
                Texts.setLang(Lang.EN);
            } else {
                // fallback: toggle
                Texts.setLang(Texts.t("language_toggle").equals("Toggle Language") ? Lang.HI : Lang.EN);
            }
            // refresh UI by recreating
            dispose();
            SwingUtilities.invokeLater(LoginScreen::new);
        });

        // Card insert simulation: prefills a card number choices (for convenience)
        insertCardBtn.addActionListener(e -> {
            Object[] opts = ACCOUNT_MODELS.keySet().toArray();
            String choice = (String) JOptionPane.showInputDialog(this, "Select card:", "Insert Card",
                    JOptionPane.PLAIN_MESSAGE, null, opts, opts.length > 0 ? opts[0] : null);
            if (choice != null) cardField.setText(choice);
        });

        loginBtn.addActionListener(e -> {
            String card = cardField.getText().trim();
            String pin = new String(pinField.getPassword()).trim();
            if (!ACCOUNTS.containsKey(card)) {
                JOptionPane.showMessageDialog(this, Texts.t("invalid_login"));
                return;
            }
            ATMImplementation impl = ACCOUNT_MODELS.get(card);
            if (impl.isLocked()) {
                JOptionPane.showMessageDialog(this, Texts.t("card_blocked"));
                return;
            }
            if (impl.checkPin(pin) || (ADMIN_CARDS.contains(card) && ACCOUNTS.get(card).equals(pin))) {
                // login success
                dispose();
                if (ADMIN_CARDS.contains(card)) {
                    new ATMAdminGUI(impl, card);
                } else {
                    new ATMGUI(impl, card);
                }
            } else {
                if (impl.isLocked()) {
                    JOptionPane.showMessageDialog(this, Texts.t("locked_info"));
                } else {
                    JOptionPane.showMessageDialog(this, Texts.t("invalid_login"));
                }
            }
        });

        setVisible(true);
    }
}

/* ---------- ATM GUI (user) ---------- */
class ATMGUI extends JFrame {
    private final ATMImplementation atmModel;
    private final String currentCard;
    private final JLabel screenLabel;
    private final JTextArea historyArea;
    private final JTextField inputField;
    private final JComboBox<String> accountSelector;
    private javax.swing.Timer sessionTimer; // fully qualified to avoid ambiguity
    private static final int SESSION_TIMEOUT_MS = 2 * 60 * 1000; // 2 minutes

    // Receipt folder
    private static final Path RECEIPT_DIR = Paths.get(System.getProperty("user.home"), "ATMReceipts");

    public ATMGUI(ATMImplementation atmModel, String card) {
        super("ATM - " + Texts.t("welcome"));
        this.atmModel = atmModel;
        this.currentCard = card;

        // ensure receipt dir exists
        try { Files.createDirectories(RECEIPT_DIR); } catch (IOException ignored) {}

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(Color.BLACK);

        // Top screen label
        screenLabel = new JLabel(Texts.t("welcome") + " - " + maskCard(card), SwingConstants.CENTER);
        screenLabel.setForeground(Color.GREEN);
        screenLabel.setFont(new Font("Monospaced", Font.BOLD, 28));
        screenLabel.setOpaque(true);
        screenLabel.setBackground(Color.BLACK);
        add(screenLabel, BorderLayout.NORTH);

        // Left panel: action buttons grid
        JPanel btnPanel = new JPanel(new GridLayout(4, 3, 18, 18));
        btnPanel.setBackground(Color.BLACK);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JButton balanceBtn = makeButton(Texts.t("balance"));
        JButton depositBtn = makeButton(Texts.t("deposit"));
        JButton withdrawBtn = makeButton(Texts.t("withdraw"));
        JButton miniStmtBtn = makeButton(Texts.t("mini_stmt"));
        JButton transferBtn = makeButton(Texts.t("transfer"));
        JButton changePinBtn = makeButton(Texts.t("change_pin"));
        JButton fastCashBtn = makeButton(Texts.t("fast_cash"));
        JButton profileBtn = makeButton(Texts.t("profile"));
        JButton receiptBtn = makeButton(Texts.t("print_receipt"));
        JButton loanBtn = makeButton(Texts.t("apply_loan"));
        JButton logoutBtn = makeButton(Texts.t("logout"));
        JButton exitBtn = makeButton(Texts.t("exit"));

        JButton[] buttons = {balanceBtn, depositBtn, withdrawBtn, miniStmtBtn, transferBtn,
                changePinBtn, fastCashBtn, profileBtn, receiptBtn, loanBtn, logoutBtn, exitBtn};
        for (JButton b : buttons) btnPanel.add(b);

        add(btnPanel, BorderLayout.CENTER);

        // Right panel: input + keypad + account selector
        JPanel right = new JPanel(new BorderLayout(8,8));
        right.setBackground(Color.BLACK);

        // account selector
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topRight.setBackground(Color.BLACK);
        JLabel selLabel = new JLabel(Texts.t("select_acc"));
        selLabel.setForeground(Color.GREEN);
        topRight.add(selLabel);
        accountSelector = new JComboBox<>(atmModel.accountTypes().toArray(new String[0]));
        accountSelector.setFont(new Font("Monospaced", Font.BOLD, 18));
        topRight.add(accountSelector);
        right.add(topRight, BorderLayout.NORTH);

        // input field
        inputField = new JTextField();
        inputField.setEditable(false);
        inputField.setBackground(Color.BLACK);
        inputField.setForeground(Color.GREEN);
        inputField.setFont(new Font("Monospaced", Font.BOLD, 28));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        right.add(inputField, BorderLayout.CENTER);

        // keypad
        JPanel keys = new JPanel(new GridLayout(4, 3, 10, 10));
        keys.setBackground(Color.BLACK);
        String[] keysArr = {"1","2","3","4","5","6","7","8","9","Clear","0","Enter"};
        for (String k : keysArr) {
            JButton b = makeButton(k);
            b.addActionListener(e -> handleKeypad(k));
            keys.add(b);
        }
        right.add(keys, BorderLayout.SOUTH);
        add(right, BorderLayout.EAST);

        // Bottom history/mini-statement area
        historyArea = new JTextArea(8, 60);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        historyArea.setBackground(Color.BLACK);
        historyArea.setForeground(Color.GREEN);
        JScrollPane scroll = new JScrollPane(historyArea);
        add(scroll, BorderLayout.SOUTH);

        // Session timeout: resets when user interacts
        sessionTimer = new javax.swing.Timer(SESSION_TIMEOUT_MS, e -> {
            JOptionPane.showMessageDialog(this, Texts.t("session_timeout"));
            dispose();
            new LoginScreen();
        });
        sessionTimer.setRepeats(false);
        sessionTimer.start();

        // Add listeners to reset timer on any button press or keypad
        ActionListener resetTimer = e -> sessionTimer.restart();
        for (JButton b : buttons) b.addActionListener(resetTimer);
        for (Component c : keys.getComponents()) if (c instanceof JButton) ((JButton)c).addActionListener(resetTimer);

        // Button actions
        balanceBtn.addActionListener(e -> showBalance());
        depositBtn.addActionListener(e -> processInput("Deposit"));
        withdrawBtn.addActionListener(e -> processInput("Withdraw"));
        miniStmtBtn.addActionListener(e -> showMiniStatement());
        transferBtn.addActionListener(e -> processInput("Transfer"));
        changePinBtn.addActionListener(e -> processInput("PIN"));
        fastCashBtn.addActionListener(e -> showFastCashOptions());
        profileBtn.addActionListener(e -> showProfileEditor());
        receiptBtn.addActionListener(e -> saveReceiptDialog());
        loanBtn.addActionListener(e -> applyLoan());
        logoutBtn.addActionListener(e -> logout());
        exitBtn.addActionListener(e -> System.exit(0));

        setVisible(true);
        refreshHistory("Login successful.");
    }

    private void refreshHistory(String head) {
        historyArea.setText(head + "\n\n");
        for (String t : atmModel.getAny().getHistory()) {
            historyArea.append(t + "\n");
        }
    }

    private void showBalance() {
        AccountType acc = atmModel.getAccount((String) accountSelector.getSelectedItem());
        screenLabel.setText(Texts.t("balance") + ": ₹" + String.format("%.2f", acc.getBalance()));
    }

    private void processInput(String type) {
        String txt = inputField.getText().trim();
        if (txt.isEmpty()) {
            screenLabel.setText(Texts.t("enter_amount_keypad"));
            return;
        }
        try {
            double amt = Double.parseDouble(txt);
            AccountType acc = atmModel.getAccount((String) accountSelector.getSelectedItem());
            switch (type) {
                case "Deposit":
                    if (amt <= 0) { screenLabel.setText(Texts.t("invalid_amount")); break; }
                    acc.deposit(amt);
                    screenLabel.setText("Deposited ₹" + amt + " | " + Texts.t("balance") + ": ₹" + acc.getBalance());
                    break;
                case "Withdraw":
                    if (!acc.canWithdrawDaily(amt)) {
                        screenLabel.setText(Texts.t("daily_withdraw_limit"));
                        break;
                    }
                    if (acc.withdraw(amt)) {
                        screenLabel.setText("Withdrawn ₹" + amt + " | " + Texts.t("balance") + ": ₹" + acc.getBalance());
                    } else {
                        screenLabel.setText(Texts.t("insufficient"));
                    }
                    break;
                case "Transfer":
                    String target = JOptionPane.showInputDialog(this, Texts.t("enter_card"));
                    if (target == null || !LoginScreen.ACCOUNT_MODELS.containsKey(target) || target.equals(currentCard)) {
                        screenLabel.setText(Texts.t("invalid_login"));
                        break;
                    }
                    AccountType targetAcc = LoginScreen.ACCOUNT_MODELS.get(target).getAccount("Current"); // deposit to recipient current
                    if (!acc.canTransferDaily(amt)) {
                        screenLabel.setText(Texts.t("daily_transfer_limit"));
                        break;
                    }
                    if (acc.transferOut(amt)) {
                        targetAcc.receiveTransfer(amt, currentCard);
                        acc.addHistory("Transferred ₹" + amt + " to " + target);
                        targetAcc.addHistory("Received ₹" + amt + " from " + currentCard);
                        screenLabel.setText(Texts.t("transfer_success") + " ₹" + amt + " to " + target);
                    } else {
                        screenLabel.setText(Texts.t("insufficient"));
                    }
                    break;
                case "PIN":
                    String oldPin = JOptionPane.showInputDialog(this, "Enter current PIN:");
                    if (oldPin != null && atmModel.checkPin(oldPin)) {
                        String newPin = txt;
                        if (newPin.matches("\\d{4}")) {
                            atmModel.changePin(newPin);
                            screenLabel.setText(Texts.t("pin_changed"));
                        } else {
                            screenLabel.setText(Texts.t("invalid_pin_format"));
                        }
                    } else {
                        screenLabel.setText(Texts.t("incorrect_old_pin"));
                    }
                    break;
            }
            inputField.setText("");
            refreshHistory("Updated:");
        } catch (NumberFormatException ex) {
            screenLabel.setText(Texts.t("invalid_amount"));
            inputField.setText("");
        }
    }

    private void showMiniStatement() {
        AccountType acc = atmModel.getAccount((String) accountSelector.getSelectedItem());
        List<String> h = acc.getHistory();
        StringBuilder sb = new StringBuilder(Texts.t("mini_stmt") + ":\n");
        int start = Math.max(0, h.size() - 5);
        for (int i = start; i < h.size(); i++) sb.append(h.get(i)).append("\n");
        historyArea.setText(sb.toString());
    }

    private void showFastCashOptions() {
        String[] opts = {"500", "1000", "2000"};
        String choice = (String) JOptionPane.showInputDialog(this, "Select Fast Cash:", Texts.t("fast_cash"),
                JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        if (choice != null) {
            double amt = Double.parseDouble(choice);
            AccountType acc = atmModel.getAccount((String) accountSelector.getSelectedItem());
            if (acc.withdraw(amt)) {
                screenLabel.setText("Fast Cash: ₹" + amt + " | Balance: ₹" + acc.getBalance());
                refreshHistory("Fast cash used");
            } else {
                screenLabel.setText(Texts.t("insufficient"));
            }
        }
    }

    private void showProfileEditor() {
        List<String> prof = atmModel.getProfile();
        String phone = prof.size() > 0 ? prof.get(0).replace("Phone: ", "") : "";
        String email = prof.size() > 1 ? prof.get(1).replace("Email: ", "") : "";
        JTextField phoneF = new JTextField(phone);
        JTextField emailF = new JTextField(email);
        Object[] fields = { "Phone:", phoneF, "Email:", emailF };
        int ok = JOptionPane.showConfirmDialog(this, fields, Texts.t("profile"), JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            atmModel.setProfile(phoneF.getText(), emailF.getText());
            screenLabel.setText("Profile updated.");
        }
    }

    private void saveReceiptDialog() {
        AccountType acc = atmModel.getAccount((String) accountSelector.getSelectedItem());
        String txn = "Receipt for " + currentCard + " (" + acc.typeName + ")\n";
        txn += "Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n";
        txn += "Balance: ₹" + String.format("%.2f", acc.getBalance()) + "\n";
        txn += "Recent txns:\n";
        List<String> h = acc.getHistory();
        int start = Math.max(0, h.size() - 5);
        for (int i = start; i < h.size(); i++) txn += h.get(i) + "\n";
        // save file
        String filename = "receipt_" + currentCard + "_" + System.currentTimeMillis() + ".txt";
        Path out = RECEIPT_DIR.resolve(filename);
        try {
            Files.write(out, txn.getBytes());
            JOptionPane.showMessageDialog(this, Texts.t("print_receipt_file") + out.toString());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save receipt: " + ex.getMessage());
        }
    }

    private void applyLoan() {
        String amtStr = JOptionPane.showInputDialog(this, "Enter loan amount:");
        if (amtStr == null) return;
        try {
            double amt = Double.parseDouble(amtStr);
            double maxLoan = 20000; // simple rule
            if (atmModel.applyLoan(amt, maxLoan)) {
                screenLabel.setText(Texts.t("loan_approved") + amt);
                refreshHistory("Loan credited");
            } else {
                screenLabel.setText(Texts.t("loan_denied"));
            }
        } catch (NumberFormatException ex) {
            screenLabel.setText(Texts.t("invalid_amount"));
        }
    }

    private void logout() {
        if (sessionTimer != null) sessionTimer.stop();
        dispose();
        new LoginScreen();
    }

    private void handleKeypad(String key) {
        if ("Clear".equals(key)) inputField.setText("");
        else if ("Enter".equals(key)) {
            // no-op — user presses operation buttons to perform
        } else {
            inputField.setText(inputField.getText() + key);
        }
    }

    private JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 16));
        btn.setBackground(Color.DARK_GRAY);
        btn.setForeground(Color.GREEN);
        btn.setFocusPainted(false);
        return btn;
    }

    private String maskCard(String c) {
        if (c.length() >= 4) {
            return Texts.t("masked_card") + c.substring(c.length()-4);
        }
        return c;
    }
}

/* ---------- Admin GUI ---------- */
class ATMAdminGUI extends JFrame {
    private final ATMImplementation adminModel;
    private final String adminCard;
    private final JTextArea display;

    public ATMAdminGUI(ATMImplementation adminModel, String card) {
        super(Texts.t("admin_mode"));
        this.adminModel = adminModel;
        this.adminCard = card;
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));
        getContentPane().setBackground(Color.BLACK);

        JPanel left = new JPanel(new GridLayout(5,1,12,12));
        left.setBackground(Color.BLACK);
        JButton viewAll = new JButton(Texts.t("view_accounts"));
        JButton unlock = new JButton(Texts.t("unlock_account"));
        JButton applyInterest = new JButton("Apply Monthly Interest");
        JButton logout = new JButton(Texts.t("logout"));
        JButton exit = new JButton(Texts.t("exit"));
        JButton langToggle = new JButton(Texts.t("language_toggle"));

        JButton[] bs = {viewAll, unlock, applyInterest, logout, exit};
        for (JButton b : bs) {
            b.setFont(new Font("Monospaced", Font.BOLD, 16));
            b.setBackground(Color.DARK_GRAY);
            b.setForeground(Color.GREEN);
            b.setFocusPainted(false);
            left.add(b);
        }
        left.add(langToggle);
        add(left, BorderLayout.WEST);

        display = new JTextArea();
        display.setEditable(false);
        display.setBackground(Color.BLACK);
        display.setForeground(Color.GREEN);
        display.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(display), BorderLayout.CENTER);

        viewAll.addActionListener(e -> showAllAccounts());
        unlock.addActionListener(e -> unlockAccount());
        applyInterest.addActionListener(e -> {
            for (ATMImplementation impl : LoginScreen.ACCOUNT_MODELS.values()) {
                for (String accType : impl.accountTypes()) {
                    impl.getAccount(accType).applyMonthlyInterest();
                }
            }
            display.append("\nMonthly interest applied to eligible accounts.\n");
        });
        logout.addActionListener(e -> { dispose(); new LoginScreen(); });
        exit.addActionListener(e -> System.exit(0));
        langToggle.addActionListener(e -> {
            Texts.setLang(Texts.t("language_toggle").equals("Toggle Language") ? Lang.HI : Lang.EN);
            dispose(); SwingUtilities.invokeLater(() -> new ATMAdminGUI(adminModel, adminCard));
        });

        setVisible(true);
        // Use adminModel to display admin info to avoid "unused" warning
        display.append("Admin: " + (adminModel != null ? adminModel.getCardNumber() : adminCard) + "\n\n");
        showAllAccounts();
    }

    private void showAllAccounts() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ATMImplementation> e : LoginScreen.ACCOUNT_MODELS.entrySet()) {
            String card = e.getKey();
            ATMImplementation impl = e.getValue();
            sb.append("Card: ").append(card).append(impl.isLocked() ? " [LOCKED]\n" : "\n");
            for (String t : impl.accountTypes()) {
                AccountType at = impl.getAccount(t);
                sb.append("  - ").append(t).append(": ₹").append(String.format("%.2f", at.getBalance()))
                  .append(" (dailyW: ₹").append(String.format("%.2f", at.getDailyWithdrawn())).append(")\n");
            }
            sb.append("  Profile: ").append(impl.getProfile()).append("\n");
        }
        display.setText(sb.toString());
    }

    private void unlockAccount() {
        String card = JOptionPane.showInputDialog(this, Texts.t("admin_prompt"));
        if (card == null) return;
        ATMImplementation impl = LoginScreen.ACCOUNT_MODELS.get(card);
        if (impl == null) {
            JOptionPane.showMessageDialog(this, "No such account.");
            return;
        }
        impl.unlock();
        display.append("\n" + card + " unlocked.\n");
    }
}
