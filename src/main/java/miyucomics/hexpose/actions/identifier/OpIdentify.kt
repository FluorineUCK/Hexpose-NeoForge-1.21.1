package miyucomics.hexpose.actions.identifier

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import miyucomics.hexpose.iotas.ItemStackIota
import miyucomics.hexpose.iotas.asActionResult
import net.minecraft.core.registries.BuiltInRegistries

object OpIdentify : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		return when (val arg = args[0]) {
			is EntityIota -> {
				val entity = arg.getEntity(env.world) ?: throw MishapInvalidIota.of(arg, 0, "identifiable")
				BuiltInRegistries.ENTITY_TYPE.getKey(entity.type)!!.asActionResult
			}
			is ItemStackIota -> BuiltInRegistries.ITEM.getKey(arg.stack.item)!!.asActionResult
			is Vec3Iota -> {
				val pos = args.getBlockPos(0, argc)
				env.assertPosInRange(pos)
				BuiltInRegistries.BLOCK.getKey(env.world.getBlockState(pos).block)!!.asActionResult
			}
			else -> throw MishapInvalidIota.of(arg, 0, "identifiable")
		}
	}
}
