package meldexun.entityculling.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import meldexun.entityculling.config.EntityCullingConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;

@Mixin(Chunk.class)
public class MixinChunk {

	@Shadow
	private World world;
	@Shadow
	private Map<BlockPos, TileEntity> tileEntities;

	@Inject(method = "getTileEntity", cancellable = true, at = @At("HEAD"))
	public void getTileEntity(BlockPos pos, EnumCreateEntityType creationMode, CallbackInfoReturnable<TileEntity> info) {
		if (!EntityCullingConfig.enabled) {
			return;
		}

		if (this.world.isRemote && !Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
			TileEntity tileEntity = this.tileEntities.get(pos);
			info.setReturnValue(tileEntity == null || !tileEntity.isInvalid() ? tileEntity : null);
		}
	}

}
