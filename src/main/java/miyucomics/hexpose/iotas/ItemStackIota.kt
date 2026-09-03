package miyucomics.hexpose.iotas

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import com.mojang.serialization.MapCodec
import miyucomics.hexpose.hexcompat.ItemStackIotaSanitizer
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack

class ItemStackIota(val stack: ItemStack) : Iota({ TYPE }) {
	override fun isTruthy() = !stack.isEmpty
	override fun toleratesOther(that: Iota) = (typesMatch(this, that) && that is ItemStackIota) && ItemStack.matches(this.stack, that.stack)
	override fun display(): Component {
		if (stack.isEmpty)
			return Component.translatable("hexpose.item_stack.null").withStyle(ChatFormatting.GRAY)
		return Component.literal("[item:${BuiltInRegistries.ITEM.getKey(stack.item)}]")
			.append(Component.translatable("hexpose.item_stack.format", Component.empty().append(stack.hoverName).withStyle(stack.rarity.color()), stack.count))
	}
	override fun hashCode(): Int = ItemStack.hashItemAndComponents(stack) * 31 + stack.count

	companion object {
		const val TAG_STACK_ID: String = "hexpose:stack_id"
		const val TAG_STACK_COUNT: String = "hexpose:stack_count"
		const val TAG_STACK_NBT: String = "hexpose:stack_tag"

		val TYPE: IotaType<ItemStackIota> = object : IotaType<ItemStackIota>() {
			override fun codec(): MapCodec<ItemStackIota> = CODEC
			override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, ItemStackIota> = STREAM_CODEC
			override fun color() = 0xff_fc0362.toInt()
		}

		private val CODEC: MapCodec<ItemStackIota> = ItemStack.OPTIONAL_CODEC
			.xmap(::ItemStackIota, ItemStackIota::stack)
			.fieldOf("stack")

		private val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ItemStackIota> =
			ItemStack.OPTIONAL_STREAM_CODEC.map(::ItemStackIota, ItemStackIota::stack)

		fun createOptimized(originalStack: ItemStack): ItemStackIota {
			return ItemStackIota(ItemStackIotaSanitizer.sanitizeCopy(originalStack))
		}
	}
}

inline val ItemStack.asActionResult get() = listOf(ItemStackIota.createOptimized(this))

fun List<Iota>.getItemStack(idx: Int, argc: Int = 0): ItemStack {
	val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
	if (x is ItemStackIota)
		return x.stack.copy()
	throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "item_stack")
}
