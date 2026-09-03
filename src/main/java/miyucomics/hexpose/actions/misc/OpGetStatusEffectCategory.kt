package miyucomics.hexpose.actions.misc

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import miyucomics.hexpose.iotas.getIdentifier
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.core.registries.BuiltInRegistries

object OpGetStatusEffectCategory : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val effect = BuiltInRegistries.MOB_EFFECT.get(args.getIdentifier(0, argc))
			?: throw MishapInvalidIota.of(args[0], 0, "status_effect_id")
		return when (effect.category) {
			MobEffectCategory.BENEFICIAL -> (1).asActionResult
			MobEffectCategory.NEUTRAL -> (0).asActionResult
			MobEffectCategory.HARMFUL -> (-1).asActionResult
			else -> throw IllegalStateException()
		}
	}
}
