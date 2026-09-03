package miyucomics.hexpose.actions.item_stack

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import miyucomics.hexpose.iotas.ItemStackIota
import net.minecraft.world.entity.player.Player

object OpGetEnderInventory : ConstMediaAction {
	override val argc = 0
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		if (env.castingEntity !is Player)
			throw MishapBadCaster()
		val inventory = (env.castingEntity as Player).enderChestInventory
		return (0 until inventory.containerSize).map { ItemStackIota.createOptimized(inventory.getItem(it)) }.asActionResult
	}
}
