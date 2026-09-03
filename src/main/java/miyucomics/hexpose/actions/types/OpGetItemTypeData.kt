package miyucomics.hexpose.actions.types

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import miyucomics.hexpose.iotas.getIdentifier
import net.minecraft.world.item.Item
import net.minecraft.core.registries.BuiltInRegistries

class OpGetItemTypeData(private val process: (Item) -> List<Iota>) : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val id = args.getIdentifier(0, argc)
		if (!BuiltInRegistries.ITEM.containsKey(id))
			throw MishapInvalidIota.of(args[0], 0, "item_id")
		return process(BuiltInRegistries.ITEM.get(id))
	}
}