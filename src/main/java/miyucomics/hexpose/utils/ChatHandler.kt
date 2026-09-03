package miyucomics.hexpose.utils

import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import miyucomics.hexpose.iotas.DisplayIota
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.event.ServerChatEvent
import java.time.Duration
import java.time.Instant
import kotlin.math.floor

object ChatHandler {
	fun onChat(event: ServerChatEvent) =
		chatLog.add(Message(event.player.name, event.message, Instant.now()))

	fun getLog(): List<Iota> {
		val now = Instant.now()
		return chatLog.buffer().map { ListIota(it.intoHex(now)) }
	}

	fun getLast(): List<Iota> = chatLog.last()?.intoHex(Instant.now()) ?: listOf(NullIota())

	private val chatLog = RingBuffer<Message>(32)
	private data class Message(val sender: Component, val message: Component, val timestamp: Instant) {
		fun intoHex(now: Instant) = listOf(DisplayIota.createSanitized(sender), DisplayIota.createSanitized(message), DoubleIota(floor(Duration.between(now, timestamp).toMillis() / -50.0)))
	}
}
