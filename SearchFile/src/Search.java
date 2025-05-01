import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Search extends JFrame {
    private JTextField searchField;
    private JTextField directoryField;
    private JTextPane resultPane;
    private JButton searchButton;
    private JButton openButton;
    private List<File> foundFiles = new ArrayList<>();
    private int lastSelectedIndex = -1;
    private StyleContext styleContext = new StyleContext();
    private Style defaultStyle;
    private Style highlightStyle;
    private int searchStatusLines = 0; // 记录状态行数（"正在搜索..."和"搜索完成..."）

    public Search() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("文件搜索工具 v1.0");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initTextStyles();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        directoryField = new JTextField(40);
        directoryField.setText("D:\\");
        JButton browseButton = new JButton("浏览...");
        browseButton.addActionListener(e -> browseDirectory());

        searchField = new JTextField(30);
        searchField.addActionListener(e -> performSearch());

        searchButton = new JButton("开始搜索");
        searchButton.addActionListener(e -> performSearch());

        resultPane = new JTextPane();
        resultPane.setEditable(false);
        resultPane.setFont(new Font("宋体", Font.PLAIN, 14));
        resultPane.setBackground(new Color(240, 240, 240));
        JScrollPane scrollPane = new JScrollPane(resultPane);

        openButton = new JButton("打开选中文件");
        openButton.setEnabled(false);
        openButton.addActionListener(e -> openSelectedFile());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);

        topPanel.add(new JLabel("搜索目录:"));
        topPanel.add(directoryField);
        topPanel.add(browseButton);
        topPanel.add(new JLabel("文件名:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);

        setLayout(new BorderLayout(10, 10));
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        resultPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    selectResultLine(e.getPoint());
                } else if (e.getClickCount() == 2) {
                    openSelectedFile();
                }
            }
        });
    }

    private void initTextStyles() {
        defaultStyle = styleContext.addStyle("Default", null);
        StyleConstants.setFontFamily(defaultStyle, "宋体");
        StyleConstants.setFontSize(defaultStyle, 14);
        StyleConstants.setForeground(defaultStyle, Color.BLACK);

        highlightStyle = styleContext.addStyle("Highlight", defaultStyle);
        StyleConstants.setBackground(highlightStyle, Color.YELLOW);
    }

    private void browseDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new File(directoryField.getText()));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            directoryField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void performSearch() {
        String searchDir = directoryField.getText().trim();
        String fileName = searchField.getText().trim();

        if (searchDir.isEmpty() || fileName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "请输入搜索目录和文件名", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        resultPane.setText("正在搜索...\n");
        searchStatusLines = 1; // 只有"正在搜索..."一行
        foundFiles.clear();
        lastSelectedIndex = -1;
        searchButton.setEnabled(false);
        openButton.setEnabled(false);

        new Thread(() -> {
            try {
                searchFile(new File(searchDir), fileName);
                SwingUtilities.invokeLater(() -> {
                    appendText("\n搜索完成！找到 " + foundFiles.size() + " 个文件\n");
                    searchStatusLines = 2; // 加上"搜索完成..."共两行状态行
                    searchButton.setEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendText("搜索出错: " + ex.getMessage() + "\n");
                    searchStatusLines = 2;
                    searchButton.setEnabled(true);
                });
            }
        }).start();
    }

    private void searchFile(File dir, String fileName) throws IOException {
        if (dir == null || !dir.exists() || dir.isFile()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (file.getName().contains(fileName)) {
                        foundFiles.add(file);
                        SwingUtilities.invokeLater(() -> {
                            highlightText(file.getAbsolutePath(), fileName);
                        });
                    }
                } else {
                    searchFile(file, fileName);
                }
            }
        }
    }

    private void highlightText(String text, String keyword) {
        try {
            Document doc = resultPane.getDocument();
            int pos = doc.getLength();
            doc.insertString(pos, "\n", defaultStyle);

            int start = text.indexOf(keyword);
            if (start == -1) {
                doc.insertString(doc.getLength(), text, defaultStyle);
                return;
            }

            doc.insertString(doc.getLength(), text.substring(0, start), defaultStyle);
            doc.insertString(doc.getLength(), keyword, highlightStyle);
            doc.insertString(doc.getLength(), text.substring(start + keyword.length()), defaultStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendText(String text) {
        try {
            Document doc = resultPane.getDocument();
            doc.insertString(doc.getLength(), text, defaultStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void selectResultLine(Point point) {
        try {
            int caretPos = resultPane.viewToModel2D(point);
            Element root = resultPane.getDocument().getDefaultRootElement();
            int lineNum = root.getElementIndex(caretPos);

            // 计算实际文件索引
            int fileIndex = lineNum - searchStatusLines;

            if (fileIndex >= 0 && fileIndex < foundFiles.size()) {
                lastSelectedIndex = fileIndex;
                openButton.setEnabled(true);

                // 高亮显示整行
                Element line = root.getElement(lineNum);
                resultPane.setSelectionStart(line.getStartOffset());
                resultPane.setSelectionEnd(line.getEndOffset() - 1);
            } else {
                // 点击了状态行或无效区域
                lastSelectedIndex = -1;
                openButton.setEnabled(false);
                resultPane.setSelectionStart(0);
                resultPane.setSelectionEnd(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openSelectedFile() {
        if (lastSelectedIndex >= 0 && lastSelectedIndex < foundFiles.size()) {
            File selectedFile = foundFiles.get(lastSelectedIndex);
            if (selectedFile.exists()) {
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(selectedFile);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "当前环境不支持直接打开文件", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this,
                            "无法打开文件: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "请先选择有效的文件路径", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            Search app = new Search();
            app.setVisible(true);
        });
    }
}