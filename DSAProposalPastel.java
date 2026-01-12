import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// ---------------- DATA CLASSES ----------------
class Customer {
    int consumerNo;
    String name, address, tariff, phone, area;
    ArrayList<Bill> bills = new ArrayList<>();

    Customer(int consumerNo, String name, String address,
             String tariff, String phone, String area) {
        this.consumerNo = consumerNo;
        this.name = name;
        this.address = address;
        this.tariff = tariff;
        this.phone = phone;
        this.area = area;
    }
}

class Bill {
    int billNo, units;
    double total;
    LocalDate date;
    boolean paid;

    Bill(int billNo, int units, double total) {
        this.billNo = billNo;
        this.units = units;
        this.total = total;
        this.date = LocalDate.now();
        this.paid = false;
    }
}

class Meter {
    int meterNo, initialReading, currentReading, consumerNo;

    Meter(int meterNo, int initialReading, int consumerNo) {
        this.meterNo = meterNo;
        this.initialReading = initialReading;
        this.currentReading = initialReading;
        this.consumerNo = consumerNo;
    }
}

// ---------------- MAIN CLASS ----------------
public class DSAProposalPastel {

    HashMap<Integer, Customer> customers = new HashMap<>();
    HashMap<Integer, Meter> meters = new HashMap<>();
    ArrayList<Bill> bills = new ArrayList<>();
    Stack<Bill> paymentStack = new Stack<>();

    JFrame frame;
    JTextField cNo, cName, cAddress, cPhone;
    JComboBox<String> cTariff, areaBox;
    JComboBox<Integer> comboMeter, comboBill;
    JTextField mNo, mInitial, mCurrent;
    JTextField bUnits, bTotal;

    DefaultTableModel tableModel;
    JLabel lblCustomers, lblBills, lblUnpaid;

    // ---------- COLORS ----------
    Color bgColor    = new Color(230, 230, 230);   // Light Gray Background
    Color fieldBg    = new Color(245, 245, 245);  // Slightly lighter gray for input fields
    Color labelColor = new Color(50, 50, 50);     // Dark Gray text
    Color btnText    = Color.WHITE;               // Button Text
    Color btnColor   = new Color(70, 130, 180);   // Steel Blue Buttons

    public DSAProposalPastel() {
        initGUI();
    }

    // ---------------- GUI ----------------
    void initGUI() {
        frame = new JFrame("DSA Electricity Billing System 🌸");
        frame.setSize(1150, 740);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(bgColor);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(bgColor);
        tabs.setForeground(labelColor);

        // -------- CONSUMER PANEL --------
        JPanel p1 = new JPanel(null);
        p1.setBackground(bgColor);
        title(p1, "CONSUMER REGISTRATION");

        cNo = field(p1, "Consumer No:", 50, 60);
        cName = field(p1, "Name:", 50, 100);
        cAddress = field(p1, "Address:", 50, 140);
        cPhone = field(p1, "Phone:", 50, 180);

        JLabel t = new JLabel("Tariff:");
        t.setBounds(50, 220, 130, 25);
        t.setForeground(labelColor);
        p1.add(t);
        cTariff = new JComboBox<>(new String[]{"Domestic", "Commercial", "Industrial"});
        cTariff.setBounds(200, 220, 150, 25);
        cTariff.setBackground(fieldBg);
        cTariff.setForeground(labelColor);
        p1.add(cTariff);

        JLabel a = new JLabel("Area:");
        a.setBounds(50, 260, 130, 25);
        a.setForeground(labelColor);
        p1.add(a);
        areaBox = new JComboBox<>(new String[]{"Latifabad", "Qasimabad", "Saddar"});
        areaBox.setBounds(200, 260, 150, 25);
        areaBox.setBackground(fieldBg);
        areaBox.setForeground(labelColor);
        p1.add(areaBox);

        btn(p1, "Register", 50, 300, e -> addConsumer());
        btn(p1, "Delete Customer", 220, 300, e -> deleteCustomerHistory());

        tabs.add("Consumer", p1);

        // --------- Meter Panel ---------
        JPanel p2 = new JPanel(null);
        p2.setBackground(bgColor);
        title(p2, "METER DETAILS");

        comboMeter = combo(p2, "Consumer No:", 50, 60);
        mNo = field(p2, "Meter No:", 50, 100);
        mInitial = field(p2, "Initial Reading:", 50, 140);
        mCurrent = field(p2, "Current Reading:", 50, 180);

        btn(p2, "Add / Update Meter", 50, 220, e -> addMeter());
        tabs.add("Meter", p2);

        // --------- Billing Panel ---------
        JPanel p3 = new JPanel(null);
        p3.setBackground(bgColor);
        title(p3, "BILLING");

        comboBill = combo(p3, "Consumer No:", 50, 60);
        bUnits = field(p3, "Units:", 50, 100); bUnits.setEditable(false);
        bTotal = field(p3, "Total:", 50, 140); bTotal.setEditable(false);

        btn(p3, "Generate Bill", 50, 180, e -> safeCall(this::generateBill));
        btn(p3, "Pay Bill", 50, 220, e -> safeCall(this::payBill));
        btn(p3, "Undo Payment", 180, 220, e -> safeCall(this::undoPayment));
        btn(p3, "Sort by Amount", 50, 260, e -> safeCall(this::sortBills));

        tableModel = new DefaultTableModel(
                new String[]{"BillNo", "Units", "Total", "Date", "Paid"}, 0);
        JTable table = new JTable(tableModel);
        table.setBackground(fieldBg);
        table.setForeground(labelColor);
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(350, 60, 750, 360);
        p3.add(sp);

        tabs.add("Billing", p3);

        // --------- Dashboard Panel ---------
        JPanel p4 = new JPanel(null);
        p4.setBackground(bgColor);
        title(p4, "DASHBOARD");

        lblCustomers = label(p4, 50, 60);
        lblBills = label(p4, 50, 100);
        lblUnpaid = label(p4, 50, 140);

        btn(p4, "Refresh", 50, 180, e -> updateDashboard());

        tabs.add("Dashboard", p4);

        frame.add(tabs);
        frame.setVisible(true);
    }

    // ---------------- GUI HELPERS ----------------
    void title(JPanel p, String t) {
        JLabel l = new JLabel(t);
        l.setBounds(380, 10, 500, 30);
        l.setFont(new Font("Arial", Font.BOLD, 18));
        l.setForeground(labelColor);
        p.add(l);
    }

    JTextField field(JPanel p, String t, int x, int y) {
        JLabel l = new JLabel(t);
        l.setBounds(x, y, 130, 25);
        l.setForeground(labelColor);
        p.add(l);
        JTextField f = new JTextField();
        f.setBounds(200, y, 150, 25);
        f.setBackground(fieldBg);
        f.setForeground(labelColor);
        p.add(f);
        return f;
    }

    JComboBox<Integer> combo(JPanel p, String t, int x, int y) {
        JLabel l = new JLabel(t);
        l.setBounds(x, y, 130, 25);
        l.setForeground(labelColor);
        p.add(l);
        JComboBox<Integer> c = new JComboBox<>();
        c.setBounds(200, y, 150, 25);
        c.setBackground(fieldBg);
        c.setForeground(labelColor);
        p.add(c);
        return c;
    }

    JButton btn(JPanel p, String t, int x, int y, java.awt.event.ActionListener a) {
        JButton b = new JButton(t);
        b.setBounds(x, y, 150, 30);
        b.setBackground(btnColor);
        b.setForeground(btnText);
        b.addActionListener(a);
        p.add(b);
        return b;
    }

    JLabel label(JPanel p, int x, int y) {
        JLabel l = new JLabel();
        l.setBounds(x, y, 350, 25);
        l.setForeground(labelColor);
        p.add(l);
        return l;
    }

    void safeCall(Runnable r) {
        try { r.run(); }
        catch(Exception e) { JOptionPane.showMessageDialog(frame,"Error: "+e.getMessage()); }
    }

    // ---------------- LOGIC ----------------
    void addConsumer() {
        try {
            int no = Integer.parseInt(cNo.getText());
            if(customers.containsKey(no)) { 
                JOptionPane.showMessageDialog(frame,"Consumer already exists!"); 
                return; 
            }

            Customer cu = new Customer(
                    no,
                    cName.getText(),
                    cAddress.getText(),
                    cTariff.getSelectedItem().toString(),
                    cPhone.getText(),
                    areaBox.getSelectedItem().toString()
            );

            customers.put(no, cu);
            refreshCombos();
            updateDashboard();

            JOptionPane.showMessageDialog(frame, 
                    "Consumer Registered Successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);

            cNo.setText(""); cName.setText(""); cAddress.setText(""); cPhone.setText("");

        } catch(NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, 
                    "Please enter valid Consumer Number!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        } catch(Exception e) {
            JOptionPane.showMessageDialog(frame, 
                    "Error: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    void deleteCustomerHistory() {
        try {
            int no = Integer.parseInt(JOptionPane.showInputDialog(frame,"Enter Consumer No"));
            if(!customers.containsKey(no)) { JOptionPane.showMessageDialog(frame,"Consumer not found!"); return; }

            bills.removeAll(customers.get(no).bills);
            meters.values().removeIf(m -> m.consumerNo == no);
            customers.remove(no);

            refreshCombos();
            refreshTable();
            updateDashboard();

            JOptionPane.showMessageDialog(frame, 
                    "Customer Deleted Successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);

        } catch(Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: "+e.getMessage());
        }
    }

    void addMeter() {
        try {
            if(comboMeter.getSelectedItem()==null) { JOptionPane.showMessageDialog(frame,"Select Consumer!"); return; }
            int c = Integer.parseInt(comboMeter.getSelectedItem().toString());
            Meter m = new Meter(Integer.parseInt(mNo.getText()),
                    Integer.parseInt(mInitial.getText()), c);
            m.currentReading = Integer.parseInt(mCurrent.getText());
            meters.put(m.meterNo, m);
            JOptionPane.showMessageDialog(frame,"Meter Added/Updated Successfully!");
            mNo.setText(""); mInitial.setText(""); mCurrent.setText("");
        } catch(Exception e) {
            JOptionPane.showMessageDialog(frame,"Error: "+e.getMessage());
        }
    }

    void generateBill() {
        if(comboBill.getSelectedItem()==null) { JOptionPane.showMessageDialog(frame,"Select Consumer!"); return; }
        int c = Integer.parseInt(comboBill.getSelectedItem().toString());
        Meter m = null;
        for(Meter x: meters.values()) if(x.consumerNo==c) m = x;
        if(m==null) { JOptionPane.showMessageDialog(frame,"Meter not found!"); return; }

        int units = Math.max(0, m.currentReading - m.initialReading);
        bUnits.setText(String.valueOf(units));

        Customer cu = customers.get(c);
        double baseRate = getRate(cu.area, cu.tariff);
        double total = slabBill(units, baseRate);
        bTotal.setText(String.format("%.2f", total));

        Bill b = new Bill(bills.size()+1, units, total);
        cu.bills.add(b);
        bills.add(b);
        m.initialReading = m.currentReading;

        refreshTable();
        updateDashboard();

        JOptionPane.showMessageDialog(frame,"Bill Generated Successfully!");
    }

    double getRate(String area, String tariff) {
        if(area.equals("Latifabad")) return tariff.equals("Domestic")?28:(tariff.equals("Commercial")?38:48);
        if(area.equals("Qasimabad")) return tariff.equals("Domestic")?30:(tariff.equals("Commercial")?40:50);
        return tariff.equals("Domestic")?32:(tariff.equals("Commercial")?42:52);
    }

    double slabBill(int units, double rate) {
        if(units<=100) return units*rate;
        if(units<=300) return 100*rate + (units-100)*(rate+5);
        return 100*rate + 200*(rate+5) + (units-300)*(rate+10);
    }

    void payBill() {
        try {
            int n = Integer.parseInt(JOptionPane.showInputDialog(frame,"Enter Bill No to Pay"));
            boolean found=false;
            for(Bill b: bills)
                if(b.billNo==n && !b.paid) { b.paid=true; paymentStack.push(b); found=true; }
            refreshTable();
            updateDashboard();
            if(found) JOptionPane.showMessageDialog(frame,"Bill Paid Successfully!");
            else JOptionPane.showMessageDialog(frame,"Bill not found or already paid!");
        } catch(Exception e) {
            JOptionPane.showMessageDialog(frame,"Error: "+e.getMessage());
        }
    }

    void undoPayment() {
        if(!paymentStack.isEmpty()) {
            Bill b = paymentStack.pop();
            b.paid=false;
            refreshTable();
            updateDashboard();
            JOptionPane.showMessageDialog(frame,"Last Payment Undone Successfully!");
        } else {
            JOptionPane.showMessageDialog(frame,"No payments to undo!");
        }
    }

    void sortBills() {
        bills.sort((a,b) -> Double.compare(b.total, a.total));
        refreshTable();
        JOptionPane.showMessageDialog(frame,"Bills Sorted by Amount!");
    }

    void refreshCombos() {
        comboBill.removeAllItems();
        comboMeter.removeAllItems();
        for(int c: customers.keySet()) {
            comboBill.addItem(c);
            comboMeter.addItem(c);
        }
    }

    void refreshTable() {
        tableModel.setRowCount(0);
        for(Bill b: bills)
            tableModel.addRow(new Object[]{b.billNo,b.units,b.total,b.date,b.paid});
    }

    void updateDashboard() {
        lblCustomers.setText("Total Customers: "+customers.size());
        lblBills.setText("Total Bills: "+bills.size());
        lblUnpaid.setText("Unpaid Bills: "+bills.stream().filter(x->!x.paid).count());
    }

    public static void main(String[] args) {
        new DSAProposalPastel();
    }
}
