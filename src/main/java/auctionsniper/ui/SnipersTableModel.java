package auctionsniper.ui;

import auctionsniper.*;
import auctionsniper.utils.Defect;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class SnipersTableModel extends AbstractTableModel implements SniperListener, PortfolioListener {
    private final static String[] STATUS_TEXT = {"Joining", "Bidding", "Winning", "Losing", "Lost", "Won", "Failed"};
    private final List<SniperSnapshot> snapshots = new ArrayList<SniperSnapshot>();

    public static String textFor(SniperState state) {
        return STATUS_TEXT[state.ordinal()];
    }

    @Override
    public int getRowCount() {
        return snapshots.size();
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return Column.at(columnIndex).valueIn(snapshots.get(rowIndex));
    }

    @Override
    public void sniperStateChanged(SniperSnapshot newSnapshot) {
        int row = rowMatching(newSnapshot);
        snapshots.set(row, newSnapshot);
        fireTableRowsUpdated(row, row);
    }

    private int rowMatching(SniperSnapshot snapshot) {
        for (int i = 0; i < snapshots.size(); i++) {
            if (snapshot.isForSameItemAs(snapshots.get(i))) {
                return i;
            }
        }

        throw new Defect("Cannot find match for " + snapshot);
    }

    @Override
    public String getColumnName(int column) {
        return Column.at(column).name;
    }

    public void addSniper(SniperSnapshot snapshot) {
        snapshots.add(snapshot);
        fireTableRowsInserted(snapshots.size(), snapshots.size());
    }

    @Override
    public void sniperAdded(AuctionSniper sniper) {
        addSniper(sniper.getSnapshot());
        sniper.addSniperListener(new SwingThreadSniperListener(this));
    }

    public class SwingThreadSniperListener implements SniperListener {
        private final SnipersTableModel snipers;

        public SwingThreadSniperListener(SnipersTableModel snipers) {
            this.snipers = snipers;
        }

        @Override
        public void sniperStateChanged(final SniperSnapshot sniperSnapshot) {
            snipers.sniperStateChanged(sniperSnapshot);
        }
    }
}
