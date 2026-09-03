package miyucomics.hexpose.actions.raycast

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import miyucomics.hexpose.iotas.getIdentifier
import miyucomics.hexpose.utils.DDAUtils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.phys.Vec3

object OpPiercingSurfaceRaycast : ConstMediaAction {
	override val argc = 3
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val start = args.getVec3(0, argc)
		env.assertVecInRange(start)
		val direction = args.getVec3(1, argc).normalize()
		if (direction == Vec3.ZERO)
			return listOf(NullIota())
		val id = args.getIdentifier(2, argc)
		if (!BuiltInRegistries.BLOCK.containsKey(id))
			throw MishapInvalidIota.of(args[2], 0, "block_id")
		val desired = BuiltInRegistries.BLOCK.get(id)
		return DDAUtils.raycastNormal(start, direction, { pos -> env.world.getBlockState(pos).`is`(desired) }, { pos -> !env.isVecInRange(pos.center) })
	}
}
