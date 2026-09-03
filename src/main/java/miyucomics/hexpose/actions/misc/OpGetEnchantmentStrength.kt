package miyucomics.hexpose.actions.misc

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import miyucomics.hexpose.iotas.getIdentifier
import miyucomics.hexpose.iotas.getItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.core.registries.Registries

object OpGetEnchantmentStrength : ConstMediaAction {
	override val argc = 2
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val stack = args.getItemStack(0, argc)
		val registry = env.world.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
		val enchantment = registry.getHolder(args.getIdentifier(1, argc)).orElseThrow {
			MishapInvalidIota.of(args[1], 0, "enchantment_id")
		}
		return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack).asActionResult
	}
}
