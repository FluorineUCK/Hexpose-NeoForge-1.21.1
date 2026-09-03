package miyucomics.hexpose.actions.instance_data

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import net.minecraft.world.entity.npc.Villager

class OpGetVillagerData(private val process: (Villager) -> List<Iota>) : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val entity = args.getEntity(env.world, 0, argc)
		env.assertEntityInRange(entity)
		if (entity !is Villager)
			throw MishapBadEntity.of(entity, "villager")
		return process(entity)
	}
}