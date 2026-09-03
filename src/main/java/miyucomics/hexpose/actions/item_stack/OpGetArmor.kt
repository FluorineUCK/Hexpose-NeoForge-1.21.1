package miyucomics.hexpose.actions.item_stack

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import miyucomics.hexpose.iotas.ItemStackIota
import net.minecraft.world.entity.LivingEntity

object OpGetArmor : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val entity = args.getEntity(env.world, 0, argc)
		env.assertEntityInRange(entity)
		return (if (entity is LivingEntity) entity.armorSlots else emptyList())
			.map { ItemStackIota.createOptimized(it) }
			.asActionResult
	}
}
