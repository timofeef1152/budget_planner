package ru.sportmaster.mpadapter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
    private JTextField commentField;
    private JXDatePicker balanceDatePicker;
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
                super.setValueAt(aValue, row, column);
                String value = aValue.toString();
                if (column == 0) {
                    updateStoryDateUseCase.execute(currentPlannerId, value, row);
                } else if (column == 1) {
                    updateStoryAmountUseCase.execute(currentPlannerId, value, row);
                } else if (column == 2) {
                    updateStoryCommentUseCase.execute(currentPlannerId, value, row);
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
        commentField = new JTextField();

        // Календарь для истории
        storyDatePicker = new JXDatePicker();
        storyDatePicker.setDate(java.sql.Date.valueOf(LocalDate.now()));
        storyDatePicker.setFormats("yyyy-MM-dd");

        // Календарь для расчета баланса
        balanceDatePicker = new JXDatePicker();
        balanceDatePicker.setDate(java.sql.Date.valueOf(LocalDate.now()));
        balanceDatePicker.setFormats("yyyy-MM-dd");

        // Верхняя панель с балансом
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(balanceLabel);
        topPanel.add(balanceDatePicker);

        JButton calculateButton = new JButton("Calculate Balance");
        calculateButton.addActionListener(e -> calculateBalance());
        topPanel.add(calculateButton);

        // Добавляем кнопку удаления в последнюю колонку
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        // Панель добавления новой записи
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Story"));

        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        formPanel.add(storyDatePicker);

        formPanel.add(new JLabel("Amount:"));
        formPanel.add(amountField);

        formPanel.add(new JLabel("Comment:"));
        formPanel.add(commentField);

        JButton addStory = new JButton("Add story record");
        addStory.addActionListener(e -> {
            addStory();
            refreshData();
        });
        formPanel.add(addStory);

        // Сборка интерфейса
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);

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
    }

    private void addStory() {
        try {
            Date storyDate = storyDatePicker.getDate();
            LocalDate date = storyDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            BigDecimal signedAmount = new BigDecimal(amountField.getText());
            String comment = commentField.getText();

            Story story = new Story(date,
                                    new BudgetChange(signedAmount),
                                    new StoryComment(comment));

            addStoryUseCase.execute(currentPlannerId, story);

            // Очистка полей
            amountField.setText("");
            commentField.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                                          "Error: " + e.getMessage(),
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            log.error("Error while adding story record", e);
        }
    }

    private void calculateBalance() {
        try {
            Date balanceDate = balanceDatePicker.getDate();
            LocalDate date = balanceDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            BigDecimal balanceForDate = calcBudgetForDateUseCase.execute(currentPlannerId, date);
            balanceLabel.setText("Balance on " + date + ": " + balanceForDate);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                                          "Error: " + e.getMessage(),
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
            log.error("Error while calculating balance for date", e);
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
