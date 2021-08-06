package auctionsniper;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static auctionsniper.AuctionEventListener.PriceSource.FromOtherBidder;
import static auctionsniper.AuctionEventListener.PriceSource.FromSniper;
import static auctionsniper.SniperState.*;
import static org.hamcrest.Matchers.equalTo;

@RunWith(JMock.class)
public class AuctionSniperTest {
    private static final Item item = new Item("auction-1234", 1234);
    private final Mockery context = new Mockery();
    private final Auction auction = context.mock(Auction.class);
    private final SniperListener sniperListener = context.mock(SniperListener.class);
    private final States sniperState = context.states("sniper");
    private AuctionSniper sniper;

    @Before
    public void initializeSniper() {
        sniper = new AuctionSniper(item, auction);
        sniper.addSniperListener(sniperListener);
    }

    @Test
    public void reportsLostWhenAuctionClosesImmediately() {
        context.checking(new Expectations() {{
            one(sniperListener).sniperStateChanged(
                    new SniperSnapshot(item.identifier, 0, 0, LOST)
            );
        }});

        sniper.auctionClosed();
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;

        context.checking(new Expectations() {{
            one(auction).bid(price + increment);
            atLeast(1).of(sniperListener).sniperStateChanged(
                    new SniperSnapshot(item.identifier, price, bid, BIDDING)
            );
        }});

        sniper.currentPrice(price, increment, FromOtherBidder);
    }

    @Test
    public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        context.checking(new Expectations() {{
            ignoring(auction);
            allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(BIDDING)));
            then(sniperState.is("bidding"));
            atLeast(1).of(sniperListener).sniperStateChanged(
                    new SniperSnapshot(item.identifier, 135, 135, WINNING));
            when(sniperState.is("bidding"));
        }});


        sniper.currentPrice(123, 12, FromOtherBidder);
        sniper.currentPrice(135, 45, FromSniper);
    }

    @Test
    public void reportsLostIfAuctionClosesWhenBidding() {
        context.checking(new Expectations() {{
            ignoring(auction);
            allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(BIDDING)));
            then(sniperState.is("bidding"));
            atLeast(1).of(sniperListener).sniperStateChanged(with(aSniperThat(LOST)));
            then(sniperState.is("lost"));
        }});

        sniper.currentPrice(123, 45, FromOtherBidder);
        sniper.auctionClosed();
    }

    @Test
    public void reportsWonIfAuctionClosesWhenWinning() {
        context.checking(new Expectations() {{
            ignoring(auction);

            allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(WINNING)));
            then(sniperState.is("winning"));
            atLeast(1).of(sniperListener).sniperStateChanged(with(aSniperThat(WON)));
            when(sniperState.is("winning"));
        }});

        sniper.currentPrice(123, 45, FromSniper);
        sniper.auctionClosed();
    }

    @Test
    public void doesNotBidAndReportsLosingIfSubsequentPriceIsAboveStopPrice() {
        allowingSniperBidding();

        context.checking(new Expectations() {{
            int bid = 123 + 45;
            allowing(auction).bid(bid);
            atLeast(1).of(sniperListener).sniperStateChanged(
                    new SniperSnapshot(item.identifier, 2345, bid, LOSING));
            when(sniperState.is("bidding"));
        }});

        sniper.currentPrice(123, 45, FromOtherBidder);
        sniper.currentPrice(2345, 25, FromOtherBidder);
    }

    @Test
    public void doesNotBitAndReportsLosingIfPriceAfterWinningIsAboveStopPrice() {
        final int price = 1233;
        final int increment = 25;

        allowingSniperBidding();
        allowingSniperWinning();

        context.checking(new Expectations() {{
            int bid = 123 + 45;
            allowing(auction).bid(bid);

            atLeast(1).of(sniperListener)
                    .sniperStateChanged(new SniperSnapshot(item.identifier, price, bid, LOSING));
        }});

        sniper.currentPrice(123, 45, FromOtherBidder);
        sniper.currentPrice(168, 45, FromSniper);
        sniper.currentPrice(price, increment, FromOtherBidder);
    }

    @Test
    public void continuesToBeLosingOnceStopPriceHasBeenReached() {
        final Sequence states = context.sequence("sniper states");
        final int price1 = 1233;
        final int price2 = 1258;

        context.checking(new Expectations() {{
            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(item.identifier, price1, 0, LOSING));
            inSequence(states);
            atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(item.identifier, price2, 0, LOSING));
            inSequence(states);
        }});

        sniper.currentPrice(price1, 25, FromOtherBidder);
        sniper.currentPrice(price2, 25, FromOtherBidder);
    }

    @Test
    public void doesNotBidAndReportsLosingIfPriceAfterWinningIsAboveStopPrice() {
        final int price = 1233;
        final int increment = 25;

        allowingSniperBidding();
        allowingSniperWinning();

        context.checking(new Expectations() {{
            int bid = 123 + 45;
            allowing(auction).bid(bid);

            atLeast(1).of(sniperListener)
                    .sniperStateChanged(new SniperSnapshot(item.identifier, price, bid, LOSING));
        }});

        sniper.currentPrice(123, 45, FromOtherBidder);
        sniper.currentPrice(168, 45, FromSniper);
        sniper.currentPrice(price, increment, FromOtherBidder);
    }

    @Test
    public void reportsFailedIfAuctionFailsWhenBidding() {
        ignoringAuction();
        allowingSniperBidding();

        expectSniperToFailWhenItIs("bidding");

        sniper.currentPrice(123, 45, FromOtherBidder);
        sniper.auctionFailed();
    }

    private void expectSniperToFailWhenItIs(final String state) {
        context.checking(new Expectations() {{
            atLeast(1).of(sniperListener).sniperStateChanged(
                    new SniperSnapshot(item.identifier, 0, 0, SniperState.FAILED));
            when(sniperState.is(state));
        }});
    }

    private void allowingSniperBidding() {
        allowSniperStateChange(BIDDING, "bidding");
    }

    private void allowingSniperWinning() {
        allowSniperStateChange(WINNING, "winning");
    }

    private void allowingSniperLosing() {
        allowSniperStateChange(LOSING, "losing");
    }

    private void allowSniperStateChange(final SniperState newState, final String oldState) {
        context.checking(new Expectations() {{
            allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(newState)));
            then(sniperState.is(oldState));
        }});
    }

    private void ignoringAuction() {
        context.checking(new Expectations() {{
            ignoring(auction);
        }});
    }

    private Matcher<SniperSnapshot> aSniperThat(final SniperState state) {
        return new FeatureMatcher<SniperSnapshot, SniperState>(
                equalTo(state), "sniper that ", "was"
        ) {
            @Override
            protected SniperState featureValueOf(SniperSnapshot actual) {
                return actual.state;
            }
        };
    }

    private Matcher<SniperSnapshot> aSniperThatIs(final SniperState state) {
        return new FeatureMatcher<SniperSnapshot, SniperState>(
                equalTo(state), "sniper that is ", "was"
        ) {
            @Override
            protected SniperState featureValueOf(SniperSnapshot actual) {
                return actual.state;
            }
        };
    }
}
