package miyucomics.hexpose.iotas

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import com.mojang.serialization.MapCodec
import miyucomics.hexpose.utils.sanitize
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.network.codec.StreamCodec
import net.minecraft.locale.Language

class DisplayIota(val text: Component) : Iota({ TYPE }) {
	override fun isTruthy() = true
	override fun toleratesOther(that: Iota) = (typesMatch(this, that) && that is DisplayIota) && this.text == that.text
	override fun display(): Component = text
	override fun hashCode(): Int = text.hashCode()

	fun getRoot() = this.text.getRoot()

	fun modifyRootBuilder(modifier: (StringBuilder) -> StringBuilder): DisplayIota {
		val builder = StringBuilder(getRoot())
		modifier(builder)
		return getWithNewRoot(builder.toString())
	}

	fun modifyRootString(modifier: (StringBuilder) -> String): DisplayIota {
		val builder = modifier(StringBuilder(getRoot()))
		return getWithNewRoot(builder)
	}

	fun getChildren(): List<Component> = this.text.siblings

	fun getWithNewRoot(root: String): DisplayIota {
		val result = Component.literal(root)
		result.style = this.text.style
		result.siblings.clear()
		result.siblings.addAll(this.text.siblings.map(Component::copy))
		return DisplayIota(result)
	}

	fun getWithNewChildren(children: List<Component>): Component {
		return this.text.copy().also {
			it.siblings.clear()
			it.siblings.addAll(children)
		}
	}

	companion object {
		val TYPE: IotaType<DisplayIota> = object : IotaType<DisplayIota>() {
			override fun codec(): MapCodec<DisplayIota> = CODEC
			override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, DisplayIota> = STREAM_CODEC
			override fun color() = 0xff_db3f30.toInt()
		}

		private val CODEC: MapCodec<DisplayIota> = ComponentSerialization.CODEC
			.xmap(::DisplayIota, DisplayIota::text)
			.fieldOf("text")

		private val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, DisplayIota> =
			ComponentSerialization.STREAM_CODEC.map(::DisplayIota, DisplayIota::text)

		fun createSanitized(text: Component) = DisplayIota(text.sanitize())
	}
}

inline val Component.asActionResult get() = listOf(DisplayIota.createSanitized(this))

fun List<Iota>.getDisplay(idx: Int, argc: Int = 0): DisplayIota {
	val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
	if (x is DisplayIota)
		return x
	throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "display")
}

fun Component.getRoot(): String {
	return when (val content = this.contents) {
		is LiteralContents -> content.text
		is TranslatableContents -> String.format(Language.getInstance().getOrDefault(content.key), *content.args)
		else -> "arimfexendrapuse"
	}
}
