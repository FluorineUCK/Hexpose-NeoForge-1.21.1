package miyucomics.hexpose.actions.identifier

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.common.lib.HexRegistries
import miyucomics.hexpose.iotas.asActionResult

object OpClassify : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment) = env.world.registryAccess()
		.registryOrThrow(HexRegistries.IOTA_TYPE)
		.getKey(args[0].type)!!
		.asActionResult
}
