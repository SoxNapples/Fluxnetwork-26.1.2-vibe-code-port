package sonar.fluxnetworks.common.integration.energy;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import sonar.fluxnetworks.api.energy.IBlockEnergyConnector;
import sonar.fluxnetworks.api.energy.IItemEnergyConnector;
import sonar.fluxnetworks.common.util.FluxUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Connects to NeoForge's standard energy handler capability (the transfer API introduced in 1.21.9).
 */
public class ForgeEnergyConnector implements IBlockEnergyConnector, IItemEnergyConnector {

    public static final BlockCapability<EnergyHandler, Direction> BLOCK_CAP = Capabilities.Energy.BLOCK;
    public static final ForgeEnergyConnector INSTANCE = new ForgeEnergyConnector();

    private ForgeEnergyConnector() {
    }

    @Nullable
    private static EnergyHandler get(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        // the stack lives in a player inventory; mutate it in place like the old capability did
        return stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(stack));
    }

    private static boolean canInsert(EnergyHandler handler) {
        try (Transaction tx = Transaction.openRoot()) {
            return handler.insert(1, tx) > 0;
        }
    }

    private static boolean canExtract(EnergyHandler handler) {
        try (Transaction tx = Transaction.openRoot()) {
            return handler.extract(1, tx) > 0;
        }
    }

    private static long insert(EnergyHandler handler, long amount, boolean simulate) {
        int request = (int) Math.min(amount, Integer.MAX_VALUE);
        if (request <= 0) {
            return 0;
        }
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = handler.insert(request, tx);
            if (!simulate) {
                tx.commit();
            }
            return inserted;
        }
    }

    private static long extract(EnergyHandler handler, long amount, boolean simulate) {
        int request = (int) Math.min(amount, Integer.MAX_VALUE);
        if (request <= 0) {
            return 0;
        }
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = handler.extract(request, tx);
            if (!simulate) {
                tx.commit();
            }
            return extracted;
        }
    }

    @Override
    public boolean hasCapability(@Nonnull BlockEntity target, @Nonnull Direction side) {
        return !target.isRemoved() && FluxUtils.get(target, BLOCK_CAP, side) != null;
    }

    @Override
    public boolean canSendTo(@Nonnull BlockEntity target, @Nonnull Direction side) {
        if (!target.isRemoved()) {
            EnergyHandler handler = FluxUtils.get(target, BLOCK_CAP, side);
            return handler != null && canInsert(handler);
        }
        return false;
    }

    @Override
    public boolean canReceiveFrom(@Nonnull BlockEntity target, @Nonnull Direction side) {
        if (!target.isRemoved()) {
            EnergyHandler handler = FluxUtils.get(target, BLOCK_CAP, side);
            return handler != null && canExtract(handler);
        }
        return false;
    }

    @Override
    public long sendTo(long amount, @Nonnull BlockEntity target, @Nonnull Direction side, boolean simulate) {
        EnergyHandler handler = FluxUtils.get(target, BLOCK_CAP, side);
        return handler == null ? 0 : insert(handler, amount, simulate);
    }

    @Override
    public long receiveFrom(long amount, @Nonnull BlockEntity target, @Nonnull Direction side, boolean simulate) {
        EnergyHandler handler = FluxUtils.get(target, BLOCK_CAP, side);
        return handler == null ? 0 : extract(handler, amount, simulate);
    }

    @Override
    public boolean hasCapability(@Nonnull ItemStack stack) {
        return get(stack) != null;
    }

    @Override
    public boolean canSendTo(@Nonnull ItemStack stack) {
        EnergyHandler handler = get(stack);
        return handler != null && canInsert(handler);
    }

    @Override
    public boolean canReceiveFrom(@Nonnull ItemStack stack) {
        EnergyHandler handler = get(stack);
        return handler != null && canExtract(handler);
    }

    @Override
    public long sendTo(long amount, @Nonnull ItemStack stack, boolean simulate) {
        EnergyHandler handler = get(stack);
        return handler == null ? 0 : insert(handler, amount, simulate);
    }

    @Override
    public long receiveFrom(long amount, @Nonnull ItemStack stack, boolean simulate) {
        EnergyHandler handler = get(stack);
        return handler == null ? 0 : extract(handler, amount, simulate);
    }
}
