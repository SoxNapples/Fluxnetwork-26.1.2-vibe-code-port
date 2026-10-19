package sonar.fluxnetworks.api.energy;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;

/**
 * Exposes an {@link IFNEnergyStorage} through NeoForge's transactional {@link EnergyHandler} API (1.21.9+).
 * <p>
 * Inserts and extracts are only simulated while the transaction is open. The accepted amounts are
 * accumulated in a journal and applied to the underlying storage when the root transaction commits,
 * so aborted transactions never touch the Flux buffer.
 */
public final class FNEnergyHandler extends SnapshotJournal<long[]> implements EnergyHandler {

    @Nonnull
    private final IFNEnergyStorage mStorage;

    private long mPendingInsert;
    private long mPendingExtract;

    public FNEnergyHandler(@Nonnull IFNEnergyStorage storage) {
        mStorage = storage;
    }

    @Override
    public long getAmountAsLong() {
        return Math.max(0, mStorage.getEnergyStoredL() + mPendingInsert - mPendingExtract);
    }

    @Override
    public long getCapacityAsLong() {
        return Math.max(0, mStorage.getMaxEnergyStoredL());
    }

    @Override
    public int insert(int amount, @Nonnull TransactionContext transaction) {
        if (amount <= 0 || !mStorage.canReceive()) {
            return 0;
        }
        long possible = mStorage.receiveEnergyL(mPendingInsert + amount, true) - mPendingInsert;
        int accepted = (int) Math.max(0, Math.min(amount, possible));
        if (accepted > 0) {
            updateSnapshots(transaction);
            mPendingInsert += accepted;
        }
        return accepted;
    }

    @Override
    public int extract(int amount, @Nonnull TransactionContext transaction) {
        if (amount <= 0 || !mStorage.canExtract()) {
            return 0;
        }
        long possible = mStorage.extractEnergyL(mPendingExtract + amount, true) - mPendingExtract;
        int extracted = (int) Math.max(0, Math.min(amount, possible));
        if (extracted > 0) {
            updateSnapshots(transaction);
            mPendingExtract += extracted;
        }
        return extracted;
    }

    @Override
    protected long[] createSnapshot() {
        return new long[]{mPendingInsert, mPendingExtract};
    }

    @Override
    protected void revertToSnapshot(long[] snapshot) {
        mPendingInsert = snapshot[0];
        mPendingExtract = snapshot[1];
    }

    @Override
    protected void onRootCommit(long[] originalState) {
        long insert = mPendingInsert;
        long extract = mPendingExtract;
        mPendingInsert = 0;
        mPendingExtract = 0;
        if (insert > 0) {
            mStorage.receiveEnergyL(insert, false);
        }
        if (extract > 0) {
            mStorage.extractEnergyL(extract, false);
        }
    }
}
