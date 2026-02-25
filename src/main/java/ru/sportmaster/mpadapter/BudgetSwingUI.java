package ru.sportmaster.mpadapter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.JXDatePicker;

@Slf4j
public class BudgetSwingUI extends JFrame {

    private final PlannerId currentPlannerId;
    private final GetPlannerUseCase getPlannerUseCase;
    private final AddStoryUseCase addStoryUseCase;
    private final DeleteStoryUseCase deleteStoryUseCase;
    private final CalculateBalanceForDateUseCase calcBudgetForDateUseCase;
    private final UpdateStoryDateUseCase updateStoryDateUseCase;
    private final UpdateStoryAmountUseCase updateStoryAmountUseCase;
    private final UpdateStoryCommentUseCase updateStoryCommentUseCase;

    private List<Story> currentStories;
    private DefaultTableModel tableModel;
    private JLabel balanceLabel;
    private JTextField amountField;
    private JTextArea commentField;
    private JXDatePicker storyDatePicker;
    private JTable table;

    public BudgetSwingUI(Planner planner,
                         GetPlannerUseCase getPlannerUseCase,
                         AddStoryUseCase addStoryUseCase,
                         DeleteStoryUseCase deleteStoryUseCase,
                         CalculateBalanceForDateUseCase calcBudgetForDateUseCase,
                         UpdateStoryDateUseCase updateStoryDateUseCase,
                         UpdateStoryAmountUseCase updateStoryAmountUseCase,
                         UpdateStoryCommentUseCase updateStoryCommentUseCase) {
        super(planner.name().value());
        this.currentPlannerId = planner.id();
        this.getPlannerUseCase = getPlannerUseCase;
        this.addStoryUseCase = addStoryUseCase;
        this.deleteStoryUseCase = deleteStoryUseCase;
        this.calcBudgetForDateUseCase = calcBudgetForDateUseCase;
        this.updateStoryDateUseCase = updateStoryDateUseCase;
        this.updateStoryAmountUseCase = updateStoryAmountUseCase;
        this.updateStoryCommentUseCase = updateStoryCommentUseCase;
        initializeUI();
        setupEventListeners();
        refreshData();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Инициализация компонентов
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 1 || column == 2 || column == 3;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                String value = aValue.toString();
                
                if (column == 0) {
                    try {
                        updateStoryDateUseCase.execute(currentPlannerId, value, row);
                        super.setValueAt(aValue, row, column);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(BudgetSwingUI.this,
                                "Invalid date format. Please use YYYY-MM-DD format.",
                                "Validation Error",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } else if (column == 1) {
                    // Проверяем, что значение - это число
                    try {
                        new BigDecimal(value);
                        updateStoryAmountUseCase.execute(currentPlannerId, value, row);
                        super.setValueAt(aValue, row, column);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(BudgetSwingUI.this,
                                "Invalid amount format. Please enter a valid number.",
                                "Validation Error",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(BudgetSwingUI.this,
                                "Error updating amount: " + e.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else if (column == 2) {
                    try {
                        updateStoryCommentUseCase.execute(currentPlannerId, value, row);
                        super.setValueAt(aValue, row, column);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(BudgetSwingUI.this,
                                "Error updating comment: " + e.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                // После изменения данных обновляем цвета строк
                table.repaint();
            }
        };

        // Создаем таблицу с кастомным рендерером
        tableModel.setColumnIdentifiers(new String[]{"Date", "Amount", "Comment", "Action"});
        table = new JTable(tableModel);

        // Устанавливаем кастомный рендерер для всех столбцов кроме Action
        table.setDefaultRenderer(Object.class, new ColorRowRenderer());

        balanceLabel = new JLabel("Balance: 0.00");
        amountField = new JTextField();
        commentField = new JTextArea(5, 20); // Увеличили до 5 строк
        commentField.setLineWrap(true);
        commentField.setWrapStyleWord(true);
        
        // Ограничиваем поле amount только для чисел
        amountField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // Разрешаем только цифры и минус
                if (!(Character.isDigit(c) || c == '-')) {
                    e.consume(); // Блокируем ввод других символов
                }
                // Разрешаем минус только в начале и только один раз
                if (c == '-' && (amountField.getCaretPosition() != 0 || amountField.getText().contains("-"))) {
                    e.consume();
                }
            }
            
            public void keyPressed(KeyEvent e) {
                // Разрешаем служебные клавиши
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || 
                    e.getKeyCode() == KeyEvent.VK_DELETE || 
                    e.getKeyCode() == KeyEvent.VK_LEFT || 
                    e.getKeyCode() == KeyEvent.VK_RIGHT || 
                    e.getKeyCode() == KeyEvent.VK_HOME || 
                    e.getKeyCode() == KeyEvent.VK_END ||
                    e.isControlDown()) {
                    return; // Разрешаем эти клавиши
                }
            }
        });

        // Календарь для истории
        storyDatePicker = new JXDatePicker();
        storyDatePicker.setDate(java.sql.Date.valueOf(LocalDate.now()));
        storyDatePicker.setFormats("yyyy-MM-dd");

        // Верхняя панель - пустая или можно добавить заголовок
        JPanel topPanel = new JPanel(new FlowLayout());

        // Добавляем кнопку удаления в последнюю колонку
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        // Панель добавления новой записи
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Story"));
        formPanel.setPreferredSize(new Dimension(600, 180)); // Увеличили высоту для большего комментария
        formPanel.setMinimumSize(new Dimension(600, 180));
        formPanel.setMaximumSize(new Dimension(600, 180));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Дата
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        formPanel.add(storyDatePicker, gbc);
        
        // Сумма
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Amount:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        formPanel.add(amountField, gbc);
        
        // Комментарий
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Comment:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH; // Разрешаем растягивание по вертикали
        gbc.weightx = 0.5;
        gbc.weighty = 0.3; // Небольшой вес для вертикального растягивания
        formPanel.add(commentField, gbc); // Добавляем без JScrollPane
        
        // Кнопка добавления
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0;
        
        JButton addStory = new JButton("Add story record");
        addStory.addActionListener(e -> {
            addStory();
            refreshData();
        });
        formPanel.add(addStory, gbc);

        // Сборка интерфейса
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Создаем панель для баланса и формы
        JPanel southPanel = new JPanel(new BorderLayout());
        
        // Панель баланса над формой
        JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        balancePanel.add(balanceLabel);
        southPanel.add(balancePanel, BorderLayout.NORTH);
        
        // Форма добавления записи
        JPanel formWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        formWrapper.setPreferredSize(new Dimension(800, 210));
        formWrapper.setMaximumSize(new Dimension(800, 210));
        formWrapper.setMinimumSize(new Dimension(800, 210));
        formWrapper.add(formPanel);
        southPanel.add(formWrapper, BorderLayout.CENTER);
        
        add(southPanel, BorderLayout.SOUTH);

        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private void setupEventListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Можно добавить сохранение состояния
                super.windowClosing(e);
            }
        });
        
        // Добавляем обработчик выбора строки для автоматического расчета баланса
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                calculateBalanceForSelectedRow();
            }
        });
        
        // Добавляем поддержку клавиши Enter для быстрого добавления записи
        amountField.addActionListener(e -> {
            addStory();
            refreshData();
        });
        
        // Для JTextArea используем KeyListener для Enter
        commentField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()) {
                    // Ctrl+Enter для добавления записи
                    addStory();
                    refreshData();
                }
            }
        });
    }

    private void addStory() {
        try {
            // Валидация полей
            if (amountField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter an amount",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                amountField.requestFocus();
                return;
            }
            
            Date storyDate = storyDatePicker.getDate();
            if (storyDate == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select a date",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                storyDatePicker.requestFocus();
                return;
            }
            
            LocalDate date = storyDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            BigDecimal signedAmount;
            
            try {
                signedAmount = new BigDecimal(amountField.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid amount format. Please enter a valid number.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                amountField.requestFocus();
                amountField.selectAll();
                return;
            }
            
            String comment = commentField.getText().trim();
            // Оставляем комментарий пустым, если пользователь ничего не ввел

            Story story = new Story(date,
                                    new BudgetChange(signedAmount),
                                    new StoryComment(comment));

            addStoryUseCase.execute(currentPlannerId, story);

            // Очистка полей и фокус на поле суммы
            amountField.setText("");
            commentField.setText("");
            amountField.requestFocus();
                    
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                                          "Error: " + e.getMessage(),
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            log.error("Error while adding story record", e);
        }
    }

    private void calculateBalanceForSelectedRow() {
        try {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                Object dateValue = tableModel.getValueAt(selectedRow, 0);
                if (dateValue != null) {
                    LocalDate date = LocalDate.parse(dateValue.toString());
                    BigDecimal balanceForDate = calcBudgetForDateUseCase.execute(currentPlannerId, date);
                    balanceLabel.setText("Balance on " + date + ": " + balanceForDate);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                                          "Error: " + e.getMessage(),
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            log.error("Error while calculating balance for selected row", e);
        }
    }

    private void refreshData() {
        tableModel.setRowCount(0); // Очистка таблицы

        Planner planner = getPlannerUseCase.execute(currentPlannerId);

        currentStories = planner.stories();
        for (Story story : currentStories) {
            tableModel.addRow(new Object[]{story.date().toString(),
                    story.change().value().toString(), story.comment().value(), "Delete"});
        }

        // Перерисовываем таблицу для обновления цветов
        table.repaint();
        
        // Рассчитываем баланс на сегодняшнюю дату при запуске
        calculateBalanceForToday();
    }
    
    private void calculateBalanceForToday() {
        try {
            LocalDate today = LocalDate.now();
            BigDecimal balanceForToday = calcBudgetForDateUseCase.execute(currentPlannerId, today);
            balanceLabel.setText("Balance on " + today + ": " + balanceForToday);
        } catch (Exception e) {
            balanceLabel.setText("Balance: Error calculating");
            log.error("Error while calculating balance for today", e);
        }
    }

    // Кастомный рендерер для окрашивания строк
    class ColorRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value,
                                                              isSelected, hasFocus, row, column);

            // Получаем значение из второго столбца (Amount)
            Object amountValue = table.getModel().getValueAt(row, 1);
            // Получаем значение из первого столбца (Date)
            Object dateValue = table.getModel().getValueAt(row, 0);

            try {
                if (amountValue != null) {
                    BigDecimal amount = new BigDecimal(amountValue.toString());

                    // Устанавливаем цвет фона в зависимости от значения
                    if (amount.compareTo(BigDecimal.ZERO) >= 0) {
                        // Положительное значение или ноль - зеленый
                        c.setBackground(new Color(220, 255, 220)); // Светло-зеленный
                    } else {
                        // Отрицательное значение - красный
                        c.setBackground(new Color(255, 220, 220)); // Светло-красный
                    }

                    // Если строка выделена, делаем цвет более насыщенным
                    if (isSelected) {
                        if (amount.compareTo(BigDecimal.ZERO) >= 0) {
                            c.setBackground(new Color(180, 230, 180));
                        } else {
                            c.setBackground(new Color(230, 180, 180));
                        }
                    }
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                }

                // Проверяем, является ли дата сегодняшней
                if (dateValue != null) {
                    try {
                        LocalDate storyDate = LocalDate.parse(dateValue.toString());
                        LocalDate today = LocalDate.now();
                        
                        if (storyDate.equals(today)) {
                            // Выделяем сегодняшнюю дату тонкой рамкой
                            if (c instanceof JComponent) {
                                ((JComponent) c).setBorder(BorderFactory.createLineBorder(new Color(0, 100, 200), 1));
                            }
                        } else {
                            // Убираем рамку для других дат
                            if (c instanceof JComponent) {
                                ((JComponent) c).setBorder(null);
                            }
                        }
                    } catch (Exception e) {
                        // Если не удалось распарсить дату, убираем рамку
                        if (c instanceof JComponent) {
                            ((JComponent) c).setBorder(null);
                        }
                    }
                } else {
                    // Если дата отсутствует, убираем рамку
                    if (c instanceof JComponent) {
                        ((JComponent) c).setBorder(null);
                    }
                }
            } catch (NumberFormatException e) {
                // Если не удалось преобразовать в число, оставляем стандартный цвет
                c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(null);
                }
            }

            return c;
        }
    }

    // Класс для отображения кнопки в таблице
    class ButtonRenderer extends javax.swing.table.DefaultTableCellRenderer {

        private final JButton button = new JButton("Delete");

        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            // Получаем значение из второго столбца для определения цвета
            Object amountValue = table.getModel().getValueAt(row, 1);

            try {
                if (amountValue != null) {
                    BigDecimal amount = new BigDecimal(amountValue.toString());

                    // Устанавливаем цвет кнопки в зависимости от значения
                    if (amount.compareTo(BigDecimal.ZERO) >= 0) {
                        button.setBackground(new Color(220, 255, 220));
                    } else {
                        button.setBackground(new Color(255, 220, 220));
                    }

                    // Если строка выделена, делаем цвет более насыщенным
                    if (isSelected) {
                        if (amount.compareTo(BigDecimal.ZERO) >= 0) {
                            button.setBackground(new Color(180, 230, 180));
                        } else {
                            button.setBackground(new Color(230, 180, 180));
                        }
                    }
                } else {
                    button.setBackground(isSelected ? new Color(180, 180, 255) : UIManager.getColor("Button.background"));
                }
            } catch (NumberFormatException e) {
                button.setBackground(isSelected ? new Color(180, 180, 255) : UIManager.getColor("Button.background"));
            }

            return button;
        }
    }

    // Класс для обработки нажатия кнопки в таблице
    class ButtonEditor extends DefaultCellEditor {

        private final JButton button;
        private int currentRow;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Delete");
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                refreshData();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column) {
            currentRow = row;
            isPushed = true;

            // Устанавливаем цвет кнопки в редакторе
            Object amountValue = table.getModel().getValueAt(row, 1);

            try {
                if (amountValue != null) {
                    BigDecimal amount = new BigDecimal(amountValue.toString());

                    if (amount.compareTo(BigDecimal.ZERO) >= 0) {
                        button.setBackground(new Color(180, 230, 180));
                    } else {
                        button.setBackground(new Color(230, 180, 180));
                    }
                } else {
                    button.setBackground(new Color(180, 180, 255));
                }
            } catch (NumberFormatException e) {
                button.setBackground(new Color(180, 180, 255));
            }

            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Вызываем удаление при нажатии кнопки
                deleteStory(currentRow);
            }
            isPushed = false;
            return "Delete";
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        private void deleteStory(int rowIndex) {
            if (rowIndex >= 0 && rowIndex < currentStories.size()) {
                Story storyToDelete = currentStories.get(rowIndex);

                try {
                    deleteStoryUseCase.execute(currentPlannerId, storyToDelete.id());
                    // refreshData() вызывается в getCellEditorValue()
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BudgetSwingUI.this,
                                                  "Error deleting: " + e.getMessage(),
                                                  "Error",
                                                  JOptionPane.ERROR_MESSAGE);
                    log.error("Error while deleting story record", e);
                }
            }
        }
    }
}
