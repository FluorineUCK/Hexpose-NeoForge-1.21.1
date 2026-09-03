package miyucomics.hexpose.actions.item_stack

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import miyucomics.hexpose.iotas.ItemStackIota
import miyucomics.hexpose.iotas.asActionResult
import net.minecraft.world.entity.decoration.ItemFrame
import net.minecraft.world.entity.animal.horse.AbstractHorse
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.vehicle.ContainerEntity

object OpGetInventory : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		return when (val entity = args.getEntity(env.world, 0, argc)) {
			is AbstractHorse -> entity.inventory.snapshot().asActionResult
			is ItemFrame -> entity.item.asActionResult
			is Player -> {
				if (env.castingEntity != entity)
					throw MishapOthersName(entity)
				entity.inventory.items.map { ItemStackIota.createOptimized(it) }.asActionResult
			}
			is ContainerEntity -> entity.itemStacks.map { ItemStackIota.createOptimized(it) }.asActionResult
			else -> listOf(NullIota())
		}
	}

	private fun net.minecraft.world.Container.snapshot() =
		(0 until containerSize).map { ItemStackIota.createOptimized(getItem(it)) }
}
