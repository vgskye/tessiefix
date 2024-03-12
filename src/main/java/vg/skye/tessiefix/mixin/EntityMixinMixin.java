package vg.skye.tessiefix.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = Entity.class, priority = 1500)
public class EntityMixinMixin {
    // NOTE: your IDE is probably mad right now
    // that's because I'm mixing into a method injected by Porting Lib
    @Inject(method = "finishCapturingDrops", at = @At("TAIL"), cancellable = true, remap = false)
    private void dontNull(CallbackInfoReturnable<List<ItemEntity>> cir) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue(List.of());
        }
    }
}