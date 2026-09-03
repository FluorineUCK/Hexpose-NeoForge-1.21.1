package miyucomics.hexpose.actions.misc

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import miyucomics.hexpose.iotas.IdentifierIota
import miyucomics.hexpose.iotas.ItemStackIota
import miyucomics.hexpose.iotas.asActionResult
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.projectile.Arrow
import net.minecraft.world.entity.projectile.ShulkerBullet
import net.minecraft.world.entity.projectile.ThrownPotion
import net.minecraft.world.entity.projectile.WitherSkull
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents

object OpGetPrescription : ConstMediaAction {
	override val argc = 1

	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		return when (val arg = args[0]) {
			is EntityIota -> {
				val entity = arg.getEntity(env.world)
					?: throw MishapInvalidIota.of(args[0], 0, "potion_holding")
				env.assertEntityInRange(entity)
				when (entity) {
					is ItemEntity -> handleItemStack(entity.item, args)
					is Arrow -> potionEffects(entity.pickupItemStackOrigin)
					is ThrownPotion -> potionEffects(entity.item)
					is ShulkerBullet -> listOf(identifier(MobEffects.LEVITATION))
					is WitherSkull -> listOf(identifier(MobEffects.WITHER))
					else -> emptyList()
				}.asActionResult
			}
			is ItemStackIota -> handleItemStack(arg.stack, args)
			else -> throw MishapInvalidIota.of(args[0], 0, "potion_holding")
		}
	}

	private fun handleItemStack(stack: ItemStack, args: List<Iota>): List<IdentifierIota> {
		val potionItem = stack.`is`(Items.POTION) || stack.`is`(Items.SPLASH_POTION) ||
			stack.`is`(Items.LINGERING_POTION) || stack.`is`(Items.TIPPED_ARROW)
		val food = stack.get(DataComponents.FOOD)
		if (!potionItem && food == null)
			throw MishapInvalidIota.of(args[0], 0, "potion_holding")

		if (potionItem)
			return potionEffects(stack)

		return food!!.effects.map { possible -> identifier(possible.effect()) }
	}

	private fun potionEffects(stack: ItemStack): List<IdentifierIota> =
		stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
			.allEffects
			.map(::identifier)

	private fun identifier(instance: MobEffectInstance) = identifier(instance.effect)

	private fun identifier(effect: Holder<MobEffect>) =
		IdentifierIota(effect.unwrapKey().orElseThrow().location())
}
