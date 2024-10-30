package View;

import javax.swing.*;
import javax.swing.table.*;

import Controller.authentication.Customer;
import Controller.billing.Billing;
import Controller.schemas.Billing_Data;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PayBill extends JPanel {
        public JLabel titleLabel;
        public JScrollPane tableScrollPane;
        public JTable billTable;

        public PayBill() {
                init();
        }

        private void init() {
                titleLabel = new javax.swing.JLabel();
                tableScrollPane = new javax.swing.JScrollPane();
                billTable = new javax.swing.JTable();

                setBackground(new java.awt.Color(255, 255, 255));
                setMaximumSize(new java.awt.Dimension(730, 700));
                setMinimumSize(new java.awt.Dimension(730, 700));
                setLayout(null);

                titleLabel.setFont(new java.awt.Font("Arial", 1, 24));
                titleLabel.setText("Pay Bill");
                titleLabel.setBounds(310, 40, 100, 30);
                add(titleLabel);

                Billing billing = new Billing();
                int size = billing.billing_data.size();
                Object[][] data = new Object[size][14];
                for (int i = 0; i < size; i++) {
                        data[i][0] = billing.billing_data.get(i).cust_id;
                        data[i][1] = billing.billing_data.get(i).billing_month;
                        data[i][2] = billing.billing_data.get(i).billing_year;
                        data[i][3] = billing.billing_data.get(i).curr_meter_reading;
                        data[i][4] = billing.billing_data.get(i).curr_meter_reading_peak;
                        data[i][5] = billing.billing_data.get(i).reading_entry_date;
                        data[i][6] = billing.billing_data.get(i).electricity_cost;
                        data[i][7] = billing.billing_data.get(i).sales_tax;
                        data[i][8] = billing.billing_data.get(i).fixed_charges;
                        data[i][9] = billing.billing_data.get(i).total_billing_amount;
                        data[i][10] = billing.billing_data.get(i).due_date;
                        data[i][11] = billing.billing_data.get(i).bill_status;
                        data[i][12] = billing.billing_data.get(i).bill_payment_date;
                        data[i][13] = "Pay";
                }

                billTable.setModel(new javax.swing.table.DefaultTableModel(
                                data,
                                new String[] {
                                                "Customer ID", "Billing Month", "Billing Year", "Reading (Reg)",
                                                "Reading (Peak)", "Reading Entry Date", "Electricity Cost", "Tax",
                                                "Fixed Charges", "Total Billing Amount", "Due Date", "Payment Status",
                                                "Payment Date", "Pay"
                                }) {
                        boolean[] canEdit = new boolean[] {
                                        false, false, false, false, false, false, false, false, false, false, false,
                                        false, false, true
                        };

                        public boolean isCellEditable(int rowIndex, int columnIndex) {
                                return canEdit[columnIndex];
                        }
                });
                billTable.getTableHeader().setReorderingAllowed(false);
                // set width of each column to allow horizontal scrolling
                billTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                billTable.setFillsViewportHeight(true);

                for (int i = 0; i < billTable.getColumnCount(); i++) {
                        billTable.getColumnModel().getColumn(i).setPreferredWidth(100);
                }

                tableScrollPane.setViewportView(billTable);
                if (billTable.getColumnModel().getColumnCount() > 0) {
                        for (int i = 0; i < billTable.getColumnCount(); i++) {
                                billTable.getColumnModel().getColumn(i).setResizable(false);
                        }
                }

                // Center text in all cells
                DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                centerRenderer.setHorizontalAlignment(JLabel.CENTER);
                for (int i = 0; i < billTable.getColumnCount(); i++) {
                        billTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
                }

                // Set custom renderer and editor for the "Pay" column
                billTable.getColumnModel().getColumn(13).setCellRenderer(new ButtonRenderer());
                billTable.getColumnModel().getColumn(13).setCellEditor(new ButtonEditor(new JCheckBox()));

                tableScrollPane.setBounds(0, 100, 720, 580);
                add(tableScrollPane);
                setVisible(true);

                if (size == 0) {
                        JOptionPane optionPane = new JOptionPane("No bills found", JOptionPane.ERROR_MESSAGE);
                        JDialog dialog = optionPane.createDialog("Error");
                        dialog.setAlwaysOnTop(true);
                        dialog.setVisible(true);
                }
        }

        class ButtonRenderer extends JButton implements TableCellRenderer {
                public ButtonRenderer() {
                        setOpaque(true);
                }

                public Component getTableCellRendererComponent(JTable table, Object value,
                                boolean isSelected, boolean hasFocus, int row, int column) {
                        String status = table.getValueAt(row, 11).toString();
                        setText((value == null) ? "" : value.toString());
                        if ("Paid".equals(status)) {
                                setEnabled(false);
                                setBackground(new Color(200, 200, 200));
                        } else {
                                setEnabled(true);
                                setBackground(UIManager.getColor("Button.background"));
                        }
                        return this;
                }
        }

        class ButtonEditor extends DefaultCellEditor {
                private String label;
                private JButton button;
                private boolean isPushed;

                public ButtonEditor(JCheckBox checkBox) {
                        super(checkBox);
                        button = new JButton();
                        button.setOpaque(true);
                        button.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                        fireEditingStopped();
                                }
                        });
                }

                public Component getTableCellEditorComponent(JTable table, Object value,
                                boolean isSelected, int row, int column) {
                        label = (value == null) ? "" : value.toString();
                        button.setText(label);
                        String status = table.getValueAt(row, 11).toString();
                        if ("Paid".equals(status)) {
                                button.setEnabled(false);
                                button.setBackground(new Color(200, 200, 200));
                        } else {
                                button.setEnabled(true);
                                button.setBackground(UIManager.getColor("Button.background"));
                        }
                        isPushed = true;
                        return button;
                }

                public Object getCellEditorValue() {
                        if (isPushed) {
                                int row = billTable.getSelectedRow();
                                Billing billing = new Billing();
                                Billing_Data billing_data = billing.billing_data.get(row);
                                if (billing_data.bill_status.equals("Unpaid") == false) {
                                        JOptionPane.showMessageDialog(null, "Bill already paid");
                                        isPushed = false;
                                        return label;
                                }
                                billing_data.bill_status = "Paid";
                                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                                                .ofPattern("dd/MM/yyyy");
                                java.time.LocalDate today = java.time.LocalDate.now();
                                today.format(formatter);
                                billing_data.bill_payment_date = today.format(formatter);
                                billing.billing_data.set(row, billing_data);
                                billing.save_data();
                                billing.load_data();
                                Customer customer = new Customer();
                                boolean flag = customer.add_to_units(billing_data.cust_id,
                                                billing_data.curr_meter_reading,
                                                billing_data.curr_meter_reading_peak);
                                customer.save_data();
                                customer.load_data();

                                if (flag == true) {
                                        billTable.setValueAt("Paid", row, 11);
                                        billTable.setValueAt(today.format(formatter), row, 12);
                                        button.setEnabled(false);
                                        button.setBackground(new Color(200, 200, 200));
                                        JOptionPane optionPane = new JOptionPane("Bill paid successfully",
                                                        JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                        JOptionPane optionPane = new JOptionPane("Error in updating units",
                                                        JOptionPane.ERROR_MESSAGE);
                                }
                        }
                        isPushed = false;
                        return label;
                }

                public boolean stopCellEditing() {
                        isPushed = false;
                        return super.stopCellEditing();
                }

                protected void fireEditingStopped() {
                        super.fireEditingStopped();
                }
        }
}
