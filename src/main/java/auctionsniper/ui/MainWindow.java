package auctionsniper.ui;

import auctionsniper.Item;
import auctionsniper.SniperPortfolio;
import auctionsniper.UserRequestListener;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;

public class MainWindow extends JFrame {
    public static final String STATUS_BIDDING = "Bidding";
    public static final String STATUS_WINNING = "Winning";
    public static final String STATUS_LOSING = "Losing";
    public static final String STATUS_WON = "Won";
    public static final String STATUS_LOST = "Lost";
    public static final String STATUS_FAILED = "Failed";

    public static final String APPLICATION_TITLE = "Auction Sniper";
    public static final String NEW_ITEM_ID_NAME = "item id";
    public static final String NEW_ITEM_STOP_PRICE_NAME = "stop price";
    public static final String JOIN_BUTTON_NAME = "join auction";
    public static final String SNIPERS_TABLE_NAME = "SNIPERS_TABLE_NAME";

    private final Set<UserRequestListener> userRequests = new HashSet<>();

    private final JTextField itemIDField = new JTextField();
    private final JFormattedTextField stopPriceField = new JFormattedTextField(NumberFormat.getIntegerInstance());

    public MainWindow(SniperPortfolio portfolio) {
        super(APPLICATION_TITLE);
        setName(APPLICATION_TITLE);
        fillContentPane(makeSnipersTable(portfolio), makeControls());
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private JPanel makeControls() {
        itemIDField.setColumns(15);
        itemIDField.setName(NEW_ITEM_ID_NAME);
        stopPriceField.setColumns(10);
        stopPriceField.setName(NEW_ITEM_STOP_PRICE_NAME);

        final JButton joinAuctionButton = new JButton("Join Auction");
        joinAuctionButton.setName(JOIN_BUTTON_NAME);
        joinAuctionButton.addActionListener(e -> {
            for (UserRequestListener listener : userRequests) {
                listener.joinAuction(new Item(itemID(), stopPrice()));
            }
            clearTextFields();
        });

        JPanel controls = new JPanel(new FlowLayout());
        controls.add(itemIDField);
        controls.add(stopPriceField);
        controls.add(joinAuctionButton);

        return controls;
    }

    private int stopPrice() {
        return ((Number) stopPriceField.getValue()).intValue();
    }

    private String itemID() {
        return this.itemIDField.getText();
    }

    private void clearTextFields() {
        this.itemIDField.setText("");
        this.stopPriceField.setText("");
    }

    private void fillContentPane(JTable snipersTable, JPanel controlPanel) {
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(controlPanel, BorderLayout.PAGE_START);
        contentPane.add(new JScrollPane(snipersTable), BorderLayout.CENTER);
    }

    private JTable makeSnipersTable(SniperPortfolio portfolio) {
        SnipersTableModel model = new SnipersTableModel();
        portfolio.addPortfolioListener(model);
        final JTable snipersTable = new JTable(model);
        snipersTable.setName(SNIPERS_TABLE_NAME);
        return snipersTable;
    }

    public void addUserRequestListener(UserRequestListener listener) {
        userRequests.add(listener);
    }
}
