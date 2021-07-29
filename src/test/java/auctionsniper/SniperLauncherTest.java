package auctionsniper;

import auctionsniper.xmpp.AuctionHouse;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.junit.Test;

public class SniperLauncherTest {
    private final Mockery context = new Mockery();
    private final States auctionState = context.states("auction state").startsAs("not joined");
    private final Auction auction = context.mock(Auction.class);
    private final SniperCollector sniperCollector = context.mock(SniperCollector.class);
    private final AuctionHouse auctionHouse = context.mock(AuctionHouse.class);
    private final SniperLauncher launcher = new SniperLauncher(auctionHouse, sniperCollector);


    @Test
    public void addsNewSniperToCollectionAndThenJoinsAuction() {
        final String itemID = "item 123";
        context.checking(new Expectations() {{
            allowing(auctionHouse).auctionFor(itemID);
            will(returnValue(auction));

            oneOf(auction).addEventListener(with(any(AuctionSniper.class))); when(auctionState.is("not joined"));
            oneOf(sniperCollector).addSniper(with(any(AuctionSniper.class))); when(auctionState.is("not joined"));

            one(auction).join(); then(auctionState.is("joined"));
        }});

        launcher.joinAuction(itemID);
    }
}
