package auctionsniper.ui;

import auctionsniper.AuctionSniperDriver;
import auctionsniper.Item;
import auctionsniper.SniperPortfolio;
import com.objogate.wl.swing.probe.ValueMatcherProbe;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;

public class MainWindowTest {

    private final AuctionSniperDriver driver = new AuctionSniperDriver(100);
    private final SniperPortfolio portfolio = new SniperPortfolio();
    private final MainWindow mainWindow = new MainWindow(portfolio);

    @Test
    public void makesUserRequestWhenJoinButtonClicked() {
        System.setProperty("com.objogate.wl.keyboard", "Mac-GB");
        final ValueMatcherProbe<Item> itemProbe =
                new ValueMatcherProbe<>(equalTo(new Item("itemID", 789)), "join request");

        mainWindow.addUserRequestListener(itemProbe::setReceivedValue);

        driver.startBiddingFor("itemID", 789);
        driver.check(itemProbe);
    }
}
