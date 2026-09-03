package miyucomics.hexpose.actions.misc

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import net.minecraft.world.entity.OwnableEntity

object OpPetOwner : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val entity = args.getEntity(env.world, 0, argc)
		env.assertEntityInRange(entity)
		if (entity !is OwnableEntity)
			throw MishapBadEntity.of(entity, "tamable")
		val owner = entity.owner
		if (owner != null && env.isEntityInRange(owner))
			return owner.asActionResult
		return listOf(NullIota())
	}
}