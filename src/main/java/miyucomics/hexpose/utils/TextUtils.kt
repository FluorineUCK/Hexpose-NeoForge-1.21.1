package miyucomics.hexpose.utils

import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents
import net.minecraft.network.chat.contents.TranslatableContents

object TextUtils {
	fun split(text: Component): MutableList<Component> {
		val chars = mutableListOf<Component>()
		collectStyledCharacters(text, text.style, chars)
		return chars
	}

	private fun collectStyledCharacters(text: Component, parentStyle: Style, out: MutableList<Component>) {
		val effectiveStyle = text.style.applyTo(parentStyle)
		val content = text.contents
		if (content is LiteralContents)
			content.text.forEach { out += Component.literal(it.toString()).setStyle(effectiveStyle) }
		for (child in text.siblings)
			collectStyledCharacters(child, effectiveStyle, out)
	}
}

// nice little function that recursively explores and flattens Text into consistent literals
fun Component.sanitize(): Component {
	val sanitizedRoot: MutableComponent = when (val content = this.contents) {
		is LiteralContents -> Component.literal(content.text)
		is TranslatableContents -> {
			val pattern = Language.getInstance().getOrDefault(content.key)
			val args = content.args.map { arg ->
				when (arg) {
					is Component -> arg.sanitize().string
					else -> arg.toString()
				}
			}.toTypedArray()
			Component.literal(String.format(pattern, *args))
		}
		else -> Component.literal("arimfexendrapuse")
	}

	sanitizedRoot.style = this.style
		.withClickEvent(null)
		.withHoverEvent(null)
		.withInsertion(null)

	for (child in this.siblings)
		sanitizedRoot.append(child.sanitize())

	return sanitizedRoot
}
