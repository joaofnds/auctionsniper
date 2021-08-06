package auctionsniper;

import java.util.EventListener;

public interface AuctionEventListener extends EventListener {
    void auctionClosed();

    void auctionFailed();

    void currentPrice(int currentPrice, int increment, PriceSource priceSource);

    enum PriceSource {
        FromSniper, FromOtherBidder
    }
}
