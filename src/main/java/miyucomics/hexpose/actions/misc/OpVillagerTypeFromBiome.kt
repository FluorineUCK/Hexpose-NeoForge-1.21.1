package miyucomics.hexpose.actions.misc

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import miyucomics.hexpose.iotas.asActionResult
import miyucomics.hexpose.iotas.getIdentifier
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.npc.VillagerType

object OpVillagerTypeFromBiome : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val identifier = args.getIdentifier(0, argc)
		val biome = env.world.registryAccess().registryOrThrow(Registries.BIOME)
			.getHolder(identifier).orElse(null) ?: return listOf(NullIota())
		val type = VillagerType.byBiome(biome)
		return BuiltInRegistries.VILLAGER_TYPE.getKey(type)!!.asActionResult
	}
}
