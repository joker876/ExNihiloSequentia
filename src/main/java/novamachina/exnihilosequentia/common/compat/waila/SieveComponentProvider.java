package novamachina.exnihilosequentia.common.compat.waila;

import java.util.List;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import novamachina.exnihilosequentia.common.item.mesh.EnumMesh;
import novamachina.exnihilosequentia.common.tileentity.SieveTile;
import novamachina.exnihilosequentia.common.utility.ExNihiloConstants;
import novamachina.exnihilosequentia.common.utility.StringUtils;

import javax.annotation.Nonnull;

public class SieveComponentProvider implements IComponentProvider {
    @Override
    public void appendBody(@Nonnull final List<ITextComponent> tooltip, @Nonnull final IDataAccessor accessor,
                           @Nonnull final IPluginConfig config) {
        @Nonnull final SieveTile sieveTile = (SieveTile) accessor.getTileEntity();

        if (!sieveTile.getBlockStack().isEmpty()) {
            tooltip.add(new TranslationTextComponent("waila.progress", StringUtils
                    .formatPercent(sieveTile.getProgress())));
            tooltip.add(new TranslationTextComponent("waila.sieve.block", new TranslationTextComponent(sieveTile.getBlockStack().getDescriptionId())));
        }
        if (sieveTile.getMesh() != EnumMesh.NONE) {
            tooltip.add(new TranslationTextComponent("waila.sieve.mesh", new TranslationTextComponent("item." + ExNihiloConstants.ModIds.EX_NIHILO_SEQUENTIA + "." + sieveTile.getMesh().getMeshName())));
        }
    }
}
