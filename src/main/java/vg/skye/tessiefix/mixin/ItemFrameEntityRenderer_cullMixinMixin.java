package vg.skye.tessiefix.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.sugar.Local;
import io.sc3.library.ScLibrary;
import io.sc3.library.ext.ItemFrameEvents;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalInt;

import static ca.fxco.moreculling.utils.CullingUtils.shouldShowMapFace;

@Restriction(
        require = {
                @Condition("moreculling"),
                @Condition("sc-library"),
        }
)
@Mixin(value = ItemFrameEntityRenderer.class, priority = 1500)
public abstract class ItemFrameEntityRenderer_cullMixinMixin<T extends ItemFrameEntity> extends EntityRenderer<T> {
    @Unique
    private boolean callbackHandled;

    protected ItemFrameEntityRenderer_cullMixinMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
        throw new AssertionError();
    }

    @Unique
    private boolean renderWithCallback(
            ItemFrameEntity entity,
            ItemStack stack,
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            int light
    ) {
        double offsetZFighting = entity.isInvisible() ? 0.5 : 0.4375;
        matrices.translate(0.0, 0.0, offsetZFighting);
        boolean ret = ItemFrameEvents.ITEM_RENDER.invoker().invoke(entity, stack, matrices, consumers, light);
        matrices.translate(0.0, 0.0, -offsetZFighting);
        return ret;
    }

    @TargetHandler(
            mixin = "ca.fxco.moreculling.mixin.entities.ItemFrameEntityRenderer_cullMixin",
            name = "moreculling$optimizedRender"
    )
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;getMapId()Ljava/util/OptionalInt;"))
    private void renderItemFrameEvents(
            T entity,
            float yaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            int light,
            CallbackInfo originalCi,
            CallbackInfo ci,
            @Local ItemStack stack,
            @Local Direction direction) {
        try {
            if (shouldShowMapFace(direction, entity.getPos(), this.dispatcher.camera.getPos()) && stack != null && renderWithCallback(entity, stack, matrices, consumers, light)) {
                callbackHandled = true;
            }
        } catch (Exception e) {
            ScLibrary.INSTANCE.getLog().error("Error in ItemFrameEvents.ITEM_RENDER", e);
        }
    }

    @TargetHandler(
            mixin = "ca.fxco.moreculling.mixin.entities.ItemFrameEntityRenderer_cullMixin",
            name = "moreculling$optimizedRender"
    )
    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;getMapId()Ljava/util/OptionalInt;"))
    private OptionalInt fakeMapId(ItemFrameEntity entity) {
        if (callbackHandled) {
            return OptionalInt.of(0);
        } else {
            return entity.getMapId();
        }
    }

    @TargetHandler(
            mixin = "ca.fxco.moreculling.mixin.entities.ItemFrameEntityRenderer_cullMixin",
            name = "moreculling$optimizedRender"
    )
    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/FilledMapItem;getMapState(Ljava/lang/Integer;Lnet/minecraft/world/World;)Lnet/minecraft/item/map/MapState;"))
    private MapState fakeMapState(Integer id, World world) {
        if (callbackHandled) {
            callbackHandled = false;
            return null;
        } else {
            return FilledMapItem.getMapState(id, world);
        }
    }
}
