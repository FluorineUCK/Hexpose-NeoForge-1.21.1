package miyucomics.hexpose

import at.petrak.hexcasting.common.lib.hex.HexArithmetics
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import at.petrak.hexcasting.common.lib.HexRegistries
import miyucomics.hexpose.actions.display.arithmetic.DisplayArithmetic
import miyucomics.hexpose.iotas.DisplayIota
import miyucomics.hexpose.iotas.IdentifierIota
import miyucomics.hexpose.iotas.ItemStackIota
import miyucomics.hexpose.utils.ChatHandler
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.registries.RegisterEvent

@Mod(HexposeMain.MOD_ID)
class HexposeMain(modBus: IEventBus) {
	init {
		modBus.addListener(::registerHexContent)
		NeoForge.EVENT_BUS.addListener(ChatHandler::onChat)
	}

	private fun registerHexContent(event: RegisterEvent) {
		when (event.registryKey) {
			HexRegistries.IOTA_TYPE -> {
				event.register(HexRegistries.IOTA_TYPE, id("identifier")) { IdentifierIota.TYPE }
				event.register(HexRegistries.IOTA_TYPE, id("item_stack")) { ItemStackIota.TYPE }
				event.register(HexRegistries.IOTA_TYPE, id("text")) { DisplayIota.TYPE }
			}
			HexRegistries.ARITHMETIC ->
				event.register(HexRegistries.ARITHMETIC, id("display")) { DisplayArithmetic }
			HexRegistries.ACTION ->
				HexposeActions.registerAll { identifier, entry ->
					event.register(HexRegistries.ACTION, identifier) { entry }
				}
		}
	}

	companion object {
		const val MOD_ID = "hexpose"
		fun id(string: String) = ResourceLocation.fromNamespaceAndPath(MOD_ID, string)
	}
}
