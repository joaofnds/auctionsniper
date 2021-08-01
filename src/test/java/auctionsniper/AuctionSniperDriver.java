package auctionsniper;

import auctionsniper.ui.MainWindow;
import com.objogate.wl.swing.AWTEventQueueProber;
import com.objogate.wl.swing.driver.*;
import com.objogate.wl.swing.gesture.GesturePerformer;

import javax.swing.*;
import javax.swing.table.JTableHeader;

import static com.objogate.wl.swing.matcher.IterableComponentsMatcher.matching;
import static com.objogate.wl.swing.matcher.JLabelTextMatcher.withLabelText;
import static org.hamcrest.Matchers.equalTo;

public class AuctionSniperDriver extends JFrameDriver {
    static {
        System.setProperty("com.objogate.wl.keyboard", "US");
    }

    public AuctionSniperDriver(int timeoutMillis) {
        super(new GesturePerformer(),
                JFrameDriver.topLevelFrame(named(MainWindow.APPLICATION_TITLE), showingOnScreen()),
                new AWTEventQueueProber(timeoutMillis, 100));
    }

    public void showsSniperStatus(String statusText) {
        new JTableDriver(this).hasCell(withLabelText(equalTo(statusText)));
    }


    public void showsSniperStatus(String itemId, int lastPrice, int lastBid, String statusText) {
        JTableDriver table = new JTableDriver(this);
        table.hasRow(matching(
                withLabelText(itemId),
                withLabelText(String.valueOf(lastPrice)),
                withLabelText(String.valueOf(lastBid)),
                withLabelText(statusText)
        ));
    }

    public void hasColumnTitles() {
        JTableHeaderDriver headers = new JTableHeaderDriver(this, JTableHeader.class);
        headers.hasHeaders(matching(
                withLabelText("Item"),
                withLabelText("Last Price"),
                withLabelText("Last Bid"),
                withLabelText("State")
        ));
    }

    public void startBiddingFor(String itemId, int stopPrice) {
        textField(MainWindow.NEW_ITEM_ID_NAME).typeText(itemId);
        textField(MainWindow.NEW_ITEM_STOP_PRICE_NAME).typeText(String.valueOf(stopPrice));
        bidButton().click();
    }

    private JTextFieldDriver textField(String fieldName) {
        JTextFieldDriver newItemId = new JTextFieldDriver(this, JTextField.class, named(fieldName));
        newItemId.focusWithMouse();
        return newItemId;
    }

    private JButtonDriver bidButton() {
        return new JButtonDriver(this, JButton.class, named(MainWindow.JOIN_BUTTON_NAME));
    }
}
