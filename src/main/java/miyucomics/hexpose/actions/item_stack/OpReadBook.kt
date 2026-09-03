package miyucomics.hexpose.actions.item_stack

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import miyucomics.hexpose.iotas.DisplayIota
import miyucomics.hexpose.iotas.getItemStack
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.WritableBookItem
import net.minecraft.world.item.WrittenBookItem
import net.minecraft.network.chat.Component

object OpReadBook : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		val book = args.getItemStack(0, argc)
		val pages = when (book.item) {
			is WritableBookItem -> book.get(DataComponents.WRITABLE_BOOK_CONTENT)
				?.getPages(false)
				?.map { DisplayIota.createSanitized(Component.literal(it)) }
				?.toList()
				?: return listOf(NullIota())
			is WrittenBookItem -> book.get(DataComponents.WRITTEN_BOOK_CONTENT)
				?.getPages(false)
				?.map(DisplayIota::createSanitized)
				?: return listOf(NullIota())
			else -> return listOf(NullIota())
		}
		return listOf(ListIota(pages))
	}
}
