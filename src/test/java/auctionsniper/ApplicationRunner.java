package auctionsniper;

import auctionsniper.ui.MainWindow;

import static auctionsniper.FakeAuctionServer.XMPP_HOSTNAME;
import static auctionsniper.ui.SnipersTableModel.textFor;

public class ApplicationRunner {
    public static final String SNIPER_ID = "sniper";
    public static final String SNIPER_PASSWORD = "sniper";
    public static final String SNIPER_XMPP_ID = "sniper@2390d55e6478/Auction";
    private AuctionSniperDriver driver;

    public void startBiddingIn(final FakeAuctionServer... auctions) {
        startBiddingWithStopPrice(Integer.MAX_VALUE, auctions);
    }

    public void startBiddingWithStopPrice(int stopPrice, FakeAuctionServer... auctions) {
        startSniper();

        for (FakeAuctionServer auction : auctions) {
            final String itemID = auction.getItemID();
            driver.startBiddingFor(itemID, stopPrice);
            driver.showsSniperStatus(itemID, 0, 0, textFor(SniperState.JOINING));
        }
    }

    private void startSniper() {
        Thread thread = new Thread("Test Application") {
            @Override
            public void run() {
                try {
                    Main.main(XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        thread.setDaemon(true);
        thread.start();
        driver = new AuctionSniperDriver(1000);
        driver.hasTitle(MainWindow.APPLICATION_TITLE);
        driver.hasColumnTitles();
    }

    public void hasShownSniperIsBidding(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.getItemID(), lastPrice, lastBid, MainWindow.STATUS_BIDDING);
    }

    public void hasShownSniperIsWinning(FakeAuctionServer auction, int winningBid) {
        driver.showsSniperStatus(auction.getItemID(), winningBid, winningBid, MainWindow.STATUS_WINNING);
    }

    public void showsSniperHasWonAuction(FakeAuctionServer auction, int lastPrice) {
        driver.showsSniperStatus(auction.getItemID(), lastPrice, lastPrice, MainWindow.STATUS_WON);
    }

    public void stop() {
        if (driver != null) {
            driver.dispose();
        }
    }

    public void hasShownSniperIsLosing(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.getItemID(), lastPrice, lastBid, MainWindow.STATUS_LOSING);
    }

    public void showsSniperHasLostAuction(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.getItemID(), lastPrice, lastBid, MainWindow.STATUS_LOST);
    }
}
