package miyucomics.hexpose.actions.display

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import miyucomics.hexpose.iotas.DisplayIota
import miyucomics.hexpose.iotas.getDisplay
import miyucomics.hexpose.iotas.getRoot
import net.minecraft.network.chat.Component

object OpDisintegrateDisplay : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment) = disintegrate(args.getDisplay(0, argc).text).asActionResult

	private fun disintegrate(text: Component): List<DisplayIota> {
		val result = mutableListOf<DisplayIota>()
		text.getRoot().forEach { char ->
			val charText = Component.literal(char.toString())
			charText.style = text.style
			result.add(DisplayIota(charText))
		}
		text.siblings.forEach { sibling ->
			result.addAll(disintegrate(sibling))
		}
		return result
	}
}
