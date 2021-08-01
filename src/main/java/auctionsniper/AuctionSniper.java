package auctionsniper;

public class AuctionSniper implements AuctionEventListener {
    private final Auction auction;
    private final Item item;
    private SniperListener sniperListener;
    private SniperSnapshot snapshot;

    public AuctionSniper(Item item, Auction auction) {
        this.item = item;
        this.auction = auction;
        this.snapshot = SniperSnapshot.joining(item.identifier);
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
                if (item.allowsBid(bid)) {
                    auction.bid(bid);
                    snapshot = snapshot.bidding(price, bid);
                } else {
                    snapshot = snapshot.losing(price);
                }
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
