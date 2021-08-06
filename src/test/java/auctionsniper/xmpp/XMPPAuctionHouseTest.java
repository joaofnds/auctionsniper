package auctionsniper.xmpp;

import auctionsniper.*;
import org.jivesoftware.smack.XMPPException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;

public class XMPPAuctionHouseTest {
    private final FakeAuctionServer auctionServer = new FakeAuctionServer("item-54321");
    private XMPPAuctionHouse auctionHouse;

    @Before
    public void openConnection() throws Exception {
        auctionHouse = XMPPAuctionHouse.connect(FakeAuctionServer.XMPP_HOSTNAME, ApplicationRunner.SNIPER_ID, ApplicationRunner.SNIPER_PASSWORD);
    }

    @After
    public void closeConnection() {
        if (auctionHouse != null) {
            auctionHouse.disconnect();
        }
    }

    @Before
    public void startAuction() throws XMPPException {
        auctionServer.startSellingItem();
    }

    @After
    public void stopAuction() {
        auctionServer.stop();
    }


    @Test
    public void
    receivesEventsFromAuctionServerAfterJoining() throws Exception {
        CountDownLatch auctionWasClosed = new CountDownLatch(1);

        var item = new Item(auctionServer.getItemID(), Integer.MAX_VALUE);
        Auction auction = auctionHouse.auctionFor(item);
        auction.addEventListener(auctionClosedListener(auctionWasClosed));
        auction.join();
        auctionServer.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID);
        auctionServer.announcesClosed();

        assertTrue("should have been closed", auctionWasClosed.await(4, SECONDS));
    }

    private AuctionEventListener auctionClosedListener(final CountDownLatch auctionWasClosed) {
        return new AuctionEventListener() {
            public void auctionClosed() {
                auctionWasClosed.countDown();
            }

            @Override
            public void auctionFailed() {

            }

            public void currentPrice(int price, int increment, PriceSource priceSource) {
            }
        };
    }
}
