package miyucomics.hexpose

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.env.PackagedItemCastEnv
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv
import at.petrak.hexcasting.api.casting.iota.*
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.item.VariantItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import miyucomics.hexpose.actions.blockstates.OpGetBlockProperties
import miyucomics.hexpose.actions.blockstates.OpQueryBlockProperty
import miyucomics.hexpose.actions.display.OpCompareStyles
import miyucomics.hexpose.actions.display.OpDisintegrateDisplay
import miyucomics.hexpose.actions.display.OpParseDisplay
import miyucomics.hexpose.actions.display.OpSplitDisplay
import miyucomics.hexpose.actions.display.chat.OpGetChat
import miyucomics.hexpose.actions.display.chat.OpGetMessage
import miyucomics.hexpose.actions.display.style.OpCreateDisplay
import miyucomics.hexpose.actions.display.style.OpDisplayBoolean
import miyucomics.hexpose.actions.display.style.OpDisplayChildren
import miyucomics.hexpose.actions.display.style.OpDisplayColor
import miyucomics.hexpose.actions.display.style.OpDisplayFont
import miyucomics.hexpose.actions.identifier.OpClassify
import miyucomics.hexpose.actions.identifier.OpIdentify
import miyucomics.hexpose.actions.instance_data.*
import miyucomics.hexpose.actions.item_stack.*
import miyucomics.hexpose.actions.misc.*
import miyucomics.hexpose.actions.raycast.OpFluidRaycast
import miyucomics.hexpose.actions.raycast.OpFluidSurfaceRaycast
import miyucomics.hexpose.actions.raycast.OpPiercingRaycast
import miyucomics.hexpose.actions.raycast.OpPiercingSurfaceRaycast
import miyucomics.hexpose.actions.types.OpGetBlockTypeData
import miyucomics.hexpose.actions.types.OpGetFoodTypeData
import miyucomics.hexpose.actions.types.OpGetItemTypeData
import miyucomics.hexpose.iotas.DisplayIota
import miyucomics.hexpose.iotas.IdentifierIota
import miyucomics.hexpose.iotas.asActionResult
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.entity.decoration.ItemFrame
import net.minecraft.world.entity.decoration.Painting
import net.minecraft.world.entity.monster.Creeper
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.animal.Cat
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.util.FastColor
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.common.Tags

object HexposeActions {
	@JvmStatic
	fun registerAll(registrar: (ResourceLocation, ActionRegistryEntry) -> Unit) {
		this.registrar = registrar
		register("am_enlightened", "awqaqqq", HexDir.SOUTH_EAST, OpGetPlayerData {
			val advancement = it.server?.advancements?.get(HexAPI.modLoc("enlightenment"))
				?: return@OpGetPlayerData false.asActionResult
			it.advancements.getOrStartProgress(advancement).isDone.asActionResult
		})
		register("is_brainswept", "qqqaqqq", HexDir.SOUTH_EAST, OpGetLivingEntityData {
			if (it is Mob)
				return@OpGetLivingEntityData IXplatAbstractions.INSTANCE.isBrainswept(it).asActionResult
			return@OpGetLivingEntityData false.asActionResult
		})

		register("create_display", "awaqeeeee", HexDir.SOUTH_WEST, OpCreateDisplay)
		register("display_children", "dwdeqqqqq", HexDir.SOUTH_EAST, OpDisplayChildren)
		register("display_color", "awaqeeeeewded", HexDir.SOUTH_WEST, OpDisplayColor)
		register("display_bold", "awaqeeeeedd", HexDir.SOUTH_WEST, OpDisplayBoolean(Style::isBold, Style::withBold))
		register("display_italics", "awaqeeeeede", HexDir.SOUTH_WEST, OpDisplayBoolean(Style::isItalic, Style::withItalic))
		register("display_underline", "awaqeeeeedw", HexDir.SOUTH_WEST, OpDisplayBoolean(Style::isUnderlined, Style::withUnderlined))
		register("display_strikethrough", "awaqeeeeedq", HexDir.SOUTH_WEST, OpDisplayBoolean(Style::isStrikethrough, Style::withStrikethrough))
		register("display_obfuscated", "awaqeeeeeda", HexDir.SOUTH_WEST, OpDisplayBoolean(Style::isObfuscated, Style::withObfuscated))
		register("display_font", "awaqeeeeedaqa", HexDir.SOUTH_WEST, OpDisplayFont)

		register("compare_style", "dwdeqqqqqdda", HexDir.SOUTH_EAST, OpCompareStyles)
		register("parse_display", "dwdewqqqwqqaeq", HexDir.SOUTH_EAST, OpParseDisplay)
		register("split_display", "dwdeqqqwqqqqae", HexDir.SOUTH_EAST, OpSplitDisplay)
		register("disintegrate_display", "dwdeqqqqqdeee", HexDir.SOUTH_EAST, OpDisintegrateDisplay)

		register("fluid_raycast", "wqqaqwede", HexDir.EAST, OpFluidRaycast)
		register("fluid_surface_raycast", "weedewqaq", HexDir.EAST, OpFluidSurfaceRaycast)
		register("piercing_raycast", "wqqddqeqddq", HexDir.EAST, OpPiercingRaycast)
		register("piercing_surface_raycast", "weeaaeqeaae", HexDir.EAST, OpPiercingSurfaceRaycast)

		register("block_hardness", "qaqqqqqeeeeedq", HexDir.EAST, OpGetBlockTypeData { block -> block.defaultDestroyTime().asActionResult })
		register("block_blast_resistance", "qaqqqqqewaawaawa", HexDir.EAST, OpGetBlockTypeData { block -> block.explosionResistance.asActionResult })
		register("blockstate_rotation", "qaqqqqqwadeeed", HexDir.EAST, OpGetBlockStateData { state ->
			if (state.hasProperty(BlockStateProperties.FACING))
				return@OpGetBlockStateData state.getValue(BlockStateProperties.FACING).step().asActionResult
			if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
				return@OpGetBlockStateData state.getValue(BlockStateProperties.HORIZONTAL_FACING).step().asActionResult
			if (state.hasProperty(BlockStateProperties.VERTICAL_DIRECTION))
				return@OpGetBlockStateData state.getValue(BlockStateProperties.VERTICAL_DIRECTION).step().asActionResult
			if (state.hasProperty(BlockStateProperties.AXIS))
				return@OpGetBlockStateData Direction.fromAxisAndDirection(
					state.getValue(BlockStateProperties.AXIS), Direction.AxisDirection.POSITIVE
				).step().asActionResult
			if (state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
				return@OpGetBlockStateData Direction.fromAxisAndDirection(
					state.getValue(BlockStateProperties.HORIZONTAL_AXIS), Direction.AxisDirection.POSITIVE
				).step().asActionResult
			if (state.hasProperty(BlockStateProperties.FACING_HOPPER))
				return@OpGetBlockStateData state.getValue(BlockStateProperties.FACING_HOPPER).step().asActionResult

			return@OpGetBlockStateData listOf(NullIota())
		})
		register("blockstate_crop", "qaqqqqqwaea", HexDir.EAST, OpGetBlockStateData { state ->
			val candidates = listOf(
				BlockStateProperties.AGE_1 to 1.0,
				BlockStateProperties.AGE_2 to 2.0,
				BlockStateProperties.AGE_3 to 3.0,
				BlockStateProperties.AGE_4 to 4.0,
				BlockStateProperties.AGE_5 to 5.0,
				BlockStateProperties.AGE_7 to 7.0,
				BlockStateProperties.AGE_15 to 15.0,
				BlockStateProperties.LEVEL_CAULDRON to 3.0,
				BlockStateProperties.LEVEL_COMPOSTER to 8.0,
				BlockStateProperties.LEVEL_HONEY to 15.0,
				BlockStateProperties.BITES to 6.0
			)

			for ((prop, divisor) in candidates)
				if (state.hasProperty(prop))
					return@OpGetBlockStateData (state.getValue(prop).toDouble() / divisor).asActionResult

			return@OpGetBlockStateData listOf(NullIota())
		})
		register("get_blockstates", "qaqqqeqqqwqaww", HexDir.EAST, OpGetBlockProperties)
		register("query_blockstate", "qaqqqqqeawa", HexDir.EAST, OpQueryBlockProperty)
		register("block_map_color", "qwedewqqqqq", HexDir.EAST, OpGetBlockTypeData { block ->
			val color = block.defaultMapColor().col
			Vec3(
				FastColor.ARGB32.red(color) / 255.0,
				FastColor.ARGB32.green(color) / 255.0,
				FastColor.ARGB32.blue(color) / 255.0
			).asActionResult
		})

		register("get_chat", "dqqqaw", HexDir.SOUTH_EAST, OpGetChat)
		register("get_message", "aeeedw", HexDir.SOUTH_WEST, OpGetMessage)

		register("get_enchantments", "waqeaeqawqwawaw", HexDir.WEST, OpGetItemStackData { stack ->
			val enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet().mapNotNull { enchantment ->
				enchantment.unwrapKey().orElse(null)?.location()?.let(::IdentifierIota)
			}
			enchantments.asActionResult
		})
		register("get_enchantment_strength", "waqwwqaweede", HexDir.WEST, OpGetEnchantmentStrength)

		register("entity_width", "dwe", HexDir.NORTH_WEST, OpGetEntityData { entity -> entity.bbWidth.asActionResult })
		register("theodolite", "wqaa", HexDir.EAST, OpGetEntityData { entity ->
			val upPitch = (-entity.xRot + 90) * (Math.PI.toFloat() / 180)
			val yaw = -entity.yHeadRot * (Math.PI.toFloat() / 180)
			val h = Mth.cos(yaw).toDouble()
			val j = Mth.cos(upPitch).toDouble()
			Vec3(
				Mth.sin(yaw).toDouble() * j,
				Mth.sin(upPitch).toDouble(),
				h * j
			).asActionResult
		})
		register("get_health", "wddwaqqwawq", HexDir.SOUTH_EAST, OpGetLivingEntityData { entity -> entity.health.asActionResult })
		register("get_max_health", "wddwwawaeqwawq", HexDir.SOUTH_EAST, OpGetLivingEntityData { entity -> entity.maxHealth.asActionResult })
		register("burning", "eewdead", HexDir.WEST, OpGetEntityData { entity -> (entity.remainingFireTicks.toDouble() / 20).asActionResult })
		register("is_wet", "qqqqwaadq", HexDir.SOUTH_WEST, OpGetEntityData { entity -> entity.isInWaterRainOrBubble.asActionResult })
		register("get_air", "wwaade", HexDir.EAST, OpGetLivingEntityData { entity -> (entity.airSupply.toDouble() / 20).asActionResult })
		register("get_max_air", "wwaadee", HexDir.EAST, OpGetLivingEntityData { entity -> (entity.maxAirSupply.toDouble() / 20).asActionResult })
		register("is_sleeping", "aqaew", HexDir.NORTH_WEST, OpGetLivingEntityData { entity -> entity.isSleeping.asActionResult })
		register("is_sprinting", "eaq", HexDir.WEST, OpGetLivingEntityData { entity -> entity.isSprinting.asActionResult })
		register("is_baby", "awaqdwaaw", HexDir.SOUTH_WEST, OpGetLivingEntityData { entity -> entity.isBaby.asActionResult })
		register("breedable", "awaaqdqaawa", HexDir.EAST, OpGetLivingEntityData { entity ->
			if (entity !is Animal)
				return@OpGetLivingEntityData listOf(NullIota())
			return@OpGetLivingEntityData entity.isInLove.asActionResult
		})
		register("get_player_hunger", "qqqadaddw", HexDir.WEST, OpGetPlayerData { player -> player.foodData.foodLevel.asActionResult })
		register("get_player_saturation", "qqqadaddq", HexDir.WEST, OpGetPlayerData { player -> player.foodData.saturationLevel.asActionResult })
		register("entity_vehicle", "eqqedwewew", HexDir.EAST, OpGetEntityData { entity -> entity.vehicle.asActionResult })
		register("entity_passengers", "qeeqawqwqw", HexDir.EAST, OpGetEntityData { entity -> entity.passengers.map { EntityIota(it) }.asActionResult })
		register("shooter", "aadedade", HexDir.EAST, OpShooter)
		register("pet_owner", "qdaqwawqeewde", HexDir.WEST, OpPetOwner)
		register("entity_name", "edeweedw", HexDir.SOUTH_WEST, OpGetEntityData { it.name.asActionResult })
		register("absorption_hearts", "waawedwdwd", HexDir.NORTH_EAST, OpGetLivingEntityData { entity -> entity.absorptionAmount.asActionResult })

		register("env_ambit", "wawaw", HexDir.EAST, OpGetAmbit)
		register("env_staff", "waaq", HexDir.NORTH_EAST, OpGetEnvData { env -> (env is StaffCastEnv).asActionResult })
		register("env_offhand", "qaqqqwaaq", HexDir.NORTH_EAST, OpGetEnvData { env -> (env.castingHand == InteractionHand.MAIN_HAND).asActionResult })
		register("env_packaged_hex", "waaqwwaqqqqq", HexDir.NORTH_EAST, OpGetEnvData { env -> (env is PackagedItemCastEnv).asActionResult })
		register("env_circle", "waaqdeaqwqae", HexDir.NORTH_EAST, OpGetEnvData { env -> (env is CircleCastEnv).asActionResult })

		register("edible", "adaqqqdd", HexDir.WEST, OpGetItemTypeData { item -> item.components().has(DataComponents.FOOD).asActionResult })
		register("get_hunger", "adaqqqddqe", HexDir.WEST, OpGetFoodTypeData { _, food -> food.nutrition.asActionResult })
		register("get_saturation", "adaqqqddqw", HexDir.WEST, OpGetFoodTypeData { _, food -> food.saturation.asActionResult })
		register("is_meat", "adaqqqddaed", HexDir.WEST, OpGetFoodTypeData { item, _ ->
			val holder = item.builtInRegistryHolder()
			(holder.`is`(Tags.Items.FOODS_RAW_MEAT) || holder.`is`(Tags.Items.FOODS_COOKED_MEAT) ||
				holder.`is`(Tags.Items.FOODS_RAW_FISH) || holder.`is`(Tags.Items.FOODS_COOKED_FISH)).asActionResult
		})
		register("is_snack", "adaqqqddaq", HexDir.WEST, OpGetFoodTypeData { _, food -> (food.eatSeconds < 1.6f).asActionResult })

		register("identify", "qqqqqe", HexDir.NORTH_EAST, OpIdentify)
		register("classify", "edqdeq", HexDir.WEST, OpClassify)

		register("get_stack", "edeedq", HexDir.WEST, OpItemIota)
		register("create_stack", "qaqqae", HexDir.EAST, OpCreateStack)
		register("get_mainhand", "qaqqqq", HexDir.NORTH_EAST, OpGetHeldStack(InteractionHand.MAIN_HAND))
		register("get_offhand", "edeeee", HexDir.NORTH_WEST, OpGetHeldStack(InteractionHand.OFF_HAND))
		register("get_armor", "qaqddqeeeeqd", HexDir.NORTH_EAST, OpGetArmor)
		register("get_ender_chest", "qaqdqaqdeeewedw", HexDir.NORTH_EAST, OpGetEnderInventory)
		register("get_inventory", "edeeeeeqdee", HexDir.WEST, OpGetInventory)
		register("get_block_inventory", "qaqqqqqeaqq", HexDir.EAST, OpGetContainer)
		register("count_stack", "qaqqwqqqw", HexDir.EAST, OpGetItemStackData { stack -> stack.count.asActionResult })
		register("count_max_stack", "edeeweeew", HexDir.WEST, OpGetItemTypeData { item -> item.defaultMaxStackSize.asActionResult })
		register("damage_stack", "eeweeewdeq", HexDir.NORTH_EAST, OpGetItemStackData { stack -> stack.damageValue.asActionResult })
		register("damage_max_stack", "qqwqqqwaqe", HexDir.NORTH_WEST, OpGetItemTypeData { item -> (item.components().get(DataComponents.MAX_DAMAGE) ?: 0).asActionResult })
		register("item_variant", "dwaawaqwa", HexDir.WEST, OpGetItemStackData { stack ->
			if (stack.item is VariantItem)
				return@OpGetItemStackData (stack.item as VariantItem).getVariant(stack).asActionResult
			return@OpGetItemStackData listOf(NullIota())
		})
		register("item_variant_max", "dwaawaqwawq", HexDir.WEST, OpGetItemStackData { stack ->
			if (stack.item is VariantItem)
				return@OpGetItemStackData (stack.item as VariantItem).numVariants().asActionResult
			return@OpGetItemStackData listOf(NullIota())
		})
		register("item_name", "qwawqwaqea", HexDir.SOUTH_EAST, OpGetItemStackData { stack -> stack.hoverName.asActionResult })
		register("item_lore", "dwewdwedea", HexDir.NORTH_WEST, OpGetItemStackData { stack ->
			val lore = stack.get(DataComponents.LORE) ?: return@OpGetItemStackData emptyList<Iota>().asActionResult
			lore.lines.map(DisplayIota::createSanitized).asActionResult
		})
		register("read_book", "awqqwaqd", HexDir.WEST, OpReadBook)
		register("book_sources", "eaedweew", HexDir.EAST, OpBookSources)
		register("item_rarity", "wqqed", HexDir.NORTH_EAST, OpGetItemStackData { stack -> stack.rarity.ordinal.asActionResult })

		register("get_effects_entity", "wqqq", HexDir.SOUTH_WEST, OpGetLivingEntityData { entity ->
			val list = mutableListOf<Iota>()
			for (effect in entity.activeEffects)
				effect.effect.unwrapKey().orElse(null)?.location()?.let { list.add(IdentifierIota(it)) }
			list.asActionResult
		})
		register("get_effects_item", "wqqqadee", HexDir.SOUTH_WEST, OpGetPrescription)
		register("get_effect_category", "wqqqaawd", HexDir.SOUTH_WEST, OpGetStatusEffectCategory)
		register("get_effect_amplifier", "wqqqaqwa", HexDir.SOUTH_WEST, OpGetStatusEffectInstanceData { it.amplifier.asActionResult })
		register("get_effect_duration", "wqqqaqwdd", HexDir.SOUTH_WEST, OpGetStatusEffectInstanceData { it.duration.asActionResult })

		register("villager_level", "qeqwqwqwqwqeqawdaeaeaeaeaea", HexDir.EAST, OpGetVillagerData { villager -> villager.villagerData.level.asActionResult })
		register("villager_profession", "qeqwqwqwqwqeqawewawqwawadeeeee", HexDir.EAST, OpGetVillagerData { villager -> BuiltInRegistries.VILLAGER_PROFESSION.getKey(villager.villagerData.profession)!!.asActionResult })
		register("villager_type", "qeqwqwqwqwqeqaweqqqqqwded", HexDir.EAST, OpGetVillagerData { villager -> BuiltInRegistries.VILLAGER_TYPE.getKey(villager.villagerData.type)!!.asActionResult })
		register("biome_to_villager", "qeqwqwqwqwqeqawewwqqwwqwwqqww", HexDir.EAST, OpVillagerTypeFromBiome)

		register("get_media", "ddew", HexDir.WEST, OpGetMedia)
		register("env_media", "dde", HexDir.WEST,
			OpGetEnvData { env ->
				((Long.MAX_VALUE - env.extractMedia(
					Long.MAX_VALUE,
					true
				)).toDouble() / MediaConstants.DUST_UNIT.toDouble()).asActionResult
			})
		register("media_max_stack", "ddeaq", HexDir.EAST, OpGetItemStackData {
			val holder = IXplatAbstractions.INSTANCE.findMediaHolder(it) ?: return@OpGetItemStackData listOf(NullIota())
			return@OpGetItemStackData (holder.maxMedia.toDouble() / MediaConstants.DUST_UNIT.toDouble()).asActionResult
		})

		register("get_weather", "eweweweweweeeaedqdqde", HexDir.WEST, OpGetWorldData { world -> (if (world.isThundering) 2.0 else if (world.isRaining) 1.0 else 0.0).asActionResult })
		register("get_light", "wqwqwqwqwqwaeqqqqaeqaeaeaeaw", HexDir.SOUTH_WEST, OpGetPositionData { world, position -> world.getMaxLocalRawBrightness(position).asActionResult })
		register("get_power", "qwqwqwqwqwqqwwaadwdaaww", HexDir.EAST, OpGetPositionData { world, position -> world.getBestNeighborSignal(position).asActionResult })
		register("get_comparator", "eweweweweweewwddawaddww", HexDir.WEST, OpGetPositionData { world, position ->
			val state = world.getBlockState(position)
			if (state.hasAnalogOutputSignal())
				return@OpGetPositionData state.getAnalogOutputSignal(world, position).asActionResult
			return@OpGetPositionData listOf(NullIota())
		})
		register("get_day", "wwawwawwqqawwdwwdwwaqwqwqwqwq", HexDir.SOUTH_EAST, OpGetWorldData { world -> (world.dayTime.toDouble() / 24000.0).asActionResult })
		register("get_time", "wddwaqqwqaddaqqwddwaqqwqaddaq", HexDir.SOUTH_EAST, OpGetWorldData { world -> world.gameTime.asActionResult })
		register("get_moon", "eweweweweweeweeedadw", HexDir.WEST, OpGetWorldData { world -> world.moonBrightness.asActionResult })
		register("get_biome", "qwqwqawdqqaqqdwaqwqwq", HexDir.WEST, OpGetPositionData { world, position -> world.getBiome(position).unwrapKey().orElseThrow().location().asActionResult })
		register("get_dimension", "qwqwqwqwqwqqaedwaqd", HexDir.WEST, OpGetWorldData { world -> world.dimension().location().asActionResult })
		register("get_einstein", "aqwawqwqqwqwqwqwqwq", HexDir.SOUTH_WEST, OpGetWorldData { world -> world.dimensionType().natural().asActionResult })

		register("cat_variant", "wqwqqwqwawaaw", HexDir.SOUTH_WEST, object : ConstMediaAction {
			override val argc = 1
			override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
				val entity = args.getEntity(env.world, 0, argc)
				env.assertEntityInRange(entity)
				if (entity !is Cat)
					throw MishapBadEntity.of(entity, "cat")
				return entity.variant.value().texture().asActionResult
			}
		})
		register("creeper_fuse", "dedwaqwede", HexDir.WEST, object : ConstMediaAction {
			override val argc = 1
			override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
				val entity = args.getEntity(env.world, 0, argc)
				env.assertEntityInRange(entity)
				if (entity !is Creeper)
					throw MishapBadEntity.of(entity, "creeper")
				return entity.getSwelling(0f).asActionResult
			}
		})
		register("item_frame_rotation", "ewdwewdea", HexDir.NORTH_EAST, object : ConstMediaAction {
			override val argc = 1
			override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
				val entity = args.getEntity(env.world, 0, argc)
				env.assertEntityInRange(entity)
				if (entity !is ItemFrame)
					throw MishapBadEntity.of(entity, "item_frame")
				return entity.rotation.asActionResult
			}
		})
		register("painting_variant", "wawwwqwwawwwqadaqeda", HexDir.SOUTH_WEST, object : ConstMediaAction {
			override val argc = 1
			override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
				val entity = args.getEntity(env.world, 0, argc)
				env.assertEntityInRange(entity)
				if (entity !is Painting)
					throw MishapBadEntity.of(entity, "painting")
				return entity.variant.unwrapKey().orElseThrow().location().asActionResult
			}
		})
	}

	private lateinit var registrar: (ResourceLocation, ActionRegistryEntry) -> Unit

	private fun register(name: String, signature: String, startDir: HexDir, action: Action) =
		registrar(
			HexposeMain.id(name),
			ActionRegistryEntry(HexPattern.Companion.fromAngles(signature, startDir), action)
		)
}
