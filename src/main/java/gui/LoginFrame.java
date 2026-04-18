package gui;

import bus.AuthService;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIntelliJLaf;
import dto.UserDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private final AuthService authService;
    private JTextField tfUser;
    private JPasswordField tfPass;
    private JButton btnLogin;

    private final Color PRIMARY_COLOR = new Color(99, 102, 241);
    private final Color TEXT_MAIN = new Color(30, 41, 59);
    private final Color TEXT_SUB = new Color(100, 116, 139);

    public LoginFrame() {
        this.authService = new AuthService();

        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
            UIManager.put("Button.arc", 16);
            UIManager.put("Component.arc", 16);
            UIManager.put("TextComponent.arc", 16);
        } catch (Exception ignored) {}

        setupFrame();
        initUI();
        attachListeners();
    }

    private void setupFrame() {
        setTitle("Hệ thống Quản lý Tuyển sinh - Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(Color.WHITE);

        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(99, 102, 241), 0, getHeight(), new Color(168, 85, 247));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.fillOval(-100, -100, 400, 400);
                g2d.fillOval(getWidth() - 200, getHeight() - 200, 300, 300);
            }
        };
        leftPanel.setLayout(new GridBagLayout());
        JLabel welcomeTxt = new JLabel("<html><div style='text-align: center;'>MÔ HÌNH PHÂN LỚP <br>NHÓM 5</div></html>");
        welcomeTxt.setFont(new Font("Segoe UI", Font.BOLD, 42));
        welcomeTxt.setForeground(Color.WHITE);
        leftPanel.add(welcomeTxt);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);

        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setOpaque(false);
        formContainer.setPreferredSize(new Dimension(350, 500));

        JLabel lbHeader = new JLabel("Welcome Back");
        lbHeader.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lbHeader.setForeground(TEXT_MAIN);

        JLabel lbSub = new JLabel("Vui lòng đăng nhập tài khoản của bạn");
        lbSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbSub.setForeground(TEXT_SUB);

        JLabel lbUserLabel = new JLabel("Username / Email");
        lbUserLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbUserLabel.setBorder(new EmptyBorder(30, 0, 8, 0));

        tfUser = new JTextField();
        tfUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập tài khoản...");
        tfUser.setPreferredSize(new Dimension(15,30));
        tfUser.putClientProperty(FlatClientProperties.STYLE, "arc: 16; focusWidth: 2; outlineColor: #6366f1");
        setLeadingIcon(tfUser, "/icons/icons8-user-16.png");

        JLabel lbPassLabel = new JLabel("Mật khẩu");
        lbPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbPassLabel.setBorder(new EmptyBorder(15, 0, 8, 0));

        tfPass = new JPasswordField();
        tfPass.setPreferredSize(new Dimension(15, 30));
        tfPass.putClientProperty(FlatClientProperties.STYLE, "arc: 16; focusWidth: 2; outlineColor: #6366f1; showRevealButton: true");
        setLeadingIcon(tfPass, "/icons/icons8-lock-16.png");

   
        JPanel extraPanel = new JPanel(new BorderLayout());
        extraPanel.setOpaque(false);
        extraPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        JCheckBox chkRemember = new JCheckBox("Remember me");
        chkRemember.setOpaque(false);
        extraPanel.add(chkRemember, BorderLayout.WEST);

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setBackground(PRIMARY_COLOR);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JButton btnGoogle = new JButton("Tiếp tục với Google");
        setLeadingIcon(btnGoogle, "/icons/icons8-google-16.png");
        btnGoogle.setBackground(Color.WHITE);
        btnGoogle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        formContainer.add(lbHeader);
        formContainer.add(lbSub);
        formContainer.add(lbUserLabel);
        formContainer.add(tfUser);
        formContainer.add(lbPassLabel);
        formContainer.add(tfPass);
        formContainer.add(extraPanel);
        formContainer.add(Box.createVerticalStrut(25));
        formContainer.add(btnLogin);
        formContainer.add(new JSeparator() {{ setMaximumSize(new Dimension(Integer.MAX_VALUE, 10)); }});
        formContainer.add(btnGoogle);

        rightPanel.add(formContainer);
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        add(mainPanel);
    }

    private void setLeadingIcon(AbstractButton c, String path) {
        URL url = getClass().getResource(path);
        if (url != null) c.setIcon(new ImageIcon(url));
    }

    private void setLeadingIcon(JTextField c, String path) {
        URL url = getClass().getResource(path);
        if (url != null) c.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new ImageIcon(url));
    }

    private void attachListeners() {
        btnLogin.addActionListener(e -> handleLogin());
        tfPass.addActionListener(e -> handleLogin()); 
    }

    private void handleLogin() {
        String username = tfUser.getText().trim();
        String password = new String(tfPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            UserDTO user = authService.login(username, password);
            if (user != null) {
                SessionManager.setCurrentUser(user);
                this.dispose();
                new MainFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Tài khoản hoặc mật khẩu không đúng", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }
}