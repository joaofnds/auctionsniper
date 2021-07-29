package auctionsniper;

public class AuctionSniper implements AuctionEventListener {
    private final Auction auction;
    private SniperListener sniperListener;
    private SniperSnapshot snapshot;

    public AuctionSniper(String itemID, Auction auction) {
        this.auction = auction;
        this.snapshot = SniperSnapshot.joining(itemID);
    }

    public void addSniperListener(SniperListener listener) {
        this.sniperListener = listener;
    }

    @Override
    public void auctionClosed() {
        snapshot = snapshot.closed();
        notifyChange();
    }

    @Override
    public void currentPrice(int price, int increment, PriceSource priceSource) {
        switch (priceSource) {
            case FromSniper -> snapshot = snapshot.winning(price);
            case FromOtherBidder -> {
                int bid = price + increment;
                auction.bid(bid);
                snapshot = snapshot.bidding(price, bid);
            }
        }

        notifyChange();
    }

    private void notifyChange() {
        sniperListener.sniperStateChanged(snapshot);
    }

    public SniperSnapshot getSnapshot() {
        return snapshot;
    }
}
