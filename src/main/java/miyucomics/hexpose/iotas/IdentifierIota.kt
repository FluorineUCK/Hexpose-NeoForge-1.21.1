package miyucomics.hexpose.iotas

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation

class IdentifierIota(val identifier: ResourceLocation) : Iota({ TYPE }) {
	override fun isTruthy() = true
	override fun toleratesOther(that: Iota) = (typesMatch(this, that) && that is IdentifierIota) && this.identifier == that.identifier
	override fun display(): Component = Component.literal(identifier.toString()).withStyle(ChatFormatting.GOLD)
	override fun hashCode(): Int = identifier.hashCode()

	companion object {
		val TYPE: IotaType<IdentifierIota> = object : IotaType<IdentifierIota>() {
			override fun codec(): MapCodec<IdentifierIota> = CODEC
			override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, IdentifierIota> = STREAM_CODEC
			override fun color() = 0xff_e6c24c.toInt()
		}

		private val CODEC: MapCodec<IdentifierIota> = RecordCodecBuilder.mapCodec { instance ->
			instance.group(
				Codec.STRING.fieldOf("namespace").forGetter { it.identifier.namespace },
				Codec.STRING.fieldOf("path").forGetter { it.identifier.path }
			).apply(instance) { namespace, path ->
				IdentifierIota(ResourceLocation.fromNamespaceAndPath(namespace, path))
			}
		}

		private val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, IdentifierIota> =
			ResourceLocation.STREAM_CODEC
				.map(::IdentifierIota, IdentifierIota::identifier)
				.mapStream { it }
	}
}

inline val ResourceLocation.asActionResult get() = listOf(IdentifierIota(this))

fun List<Iota>.getIdentifier(idx: Int, argc: Int = 0): ResourceLocation {
	val x = this.getOrElse(idx) { throw MishapNotEnoughArgs(idx + 1, this.size) }
	if (x is IdentifierIota)
		return x.identifier
	throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "identifier")
}
