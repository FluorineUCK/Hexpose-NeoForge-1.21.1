package miyucomics.hexpose.actions.raycast

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import miyucomics.hexpose.utils.DDAUtils
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.phys.Vec3

object OpFluidSurfaceRaycast : ConstMediaAction {
	override val argc = 2
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val start = args.getVec3(0, argc)
		env.assertVecInRange(start)
		val direction = args.getVec3(1, argc).normalize()
		if (direction == Vec3.ZERO)
			return listOf(NullIota())
		return DDAUtils.raycastNormal(start, direction, { pos -> env.world.getBlockState(pos).block is LiquidBlock }, { pos -> !env.isVecInRange(pos.center) })
	}
}
