package auctionsniper;

import java.util.HashSet;
import java.util.Set;

public class SniperPortfolio implements SniperCollector {
    private final Set<AuctionSniper> snipers = new HashSet<AuctionSniper>();
    private PortfolioListener portfolioListener;

    @Override
    public void addSniper(AuctionSniper sniper) {
        snipers.add(sniper);
        portfolioListener.sniperAdded(sniper);
    }

    public void addPortfolioListener(PortfolioListener listener) {
        this.portfolioListener = listener;
    }
}
