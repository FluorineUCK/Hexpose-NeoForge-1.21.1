package miyucomics.hexpose.actions.item_stack

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import miyucomics.hexpose.iotas.DisplayIota
import miyucomics.hexpose.iotas.getItemStack
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.WrittenBookItem
import net.minecraft.world.item.component.WrittenBookContent
import net.minecraft.network.chat.Component

object OpBookSources : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val book = args.getItemStack(0, argc)
		if (book.item !is WrittenBookItem)
			return listOf(NullIota())
		val content = book.get(DataComponents.WRITTEN_BOOK_CONTENT) ?: WrittenBookContent.EMPTY
		return listOf(
			DisplayIota.createSanitized(Component.literal(content.author)),
			DoubleIota(content.generation.toDouble())
		)
	}
}
