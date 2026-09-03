package miyucomics.hexpose.actions.instance_data

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getLivingEntityButNotArmorStand
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import miyucomics.hexpose.iotas.getIdentifier
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.core.registries.BuiltInRegistries

class OpGetStatusEffectInstanceData(private val process: (MobEffectInstance) -> List<Iota>) : ConstMediaAction {
	override val argc = 2
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val entity = args.getLivingEntityButNotArmorStand(env.world, 0, argc)
		env.assertEntityInRange(entity)
		val effect = args.getIdentifier(1, argc)
		if (!BuiltInRegistries.MOB_EFFECT.containsKey(effect))
			throw MishapInvalidIota.of(args[1], 0, "status_effect")
		val holder = BuiltInRegistries.MOB_EFFECT.getHolder(effect).orElseThrow()
		return process(entity.getEffect(holder) ?: return listOf(NullIota()))
	}
}
