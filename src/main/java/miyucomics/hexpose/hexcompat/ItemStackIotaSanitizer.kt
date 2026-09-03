package miyucomics.hexpose.hexcompat

import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.FrameForEach
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.ContinuationIota
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.HexDataComponents
import miyucomics.hexpose.iotas.ItemStackIota
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.BundleContents
import net.minecraft.world.item.component.ChargedProjectiles
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.ItemContainerContents

/**
 * Preserves Hexpose's old createOptimized contract after Minecraft moved item
 * state from one NBT tree into typed data components. Any ItemStackIota already
 * contained by the copied stack is replaced with an empty stack Iota, cutting
 * the otherwise unbounded stack -> iota -> stack serialization chain.
 */
object ItemStackIotaSanitizer {
	private const val MAX_DEPTH = 256
	private const val MAX_NODES = 1024
	private const val CURRENT_IOTA_TYPE = "type"
	private const val CURRENT_IOTA_STACK = "stack"
	private const val ITEM_STACK_IOTA_ID = "hexpose:item_stack"

	private class Budget(var remaining: Int = MAX_NODES) {
		fun consume(): Boolean {
			if (remaining <= 0)
				return false
			remaining--
			return true
		}
	}

	@JvmStatic
	fun sanitizeCopy(originalStack: ItemStack): ItemStack {
		if (originalStack.isEmpty)
			return ItemStack.EMPTY
		return sanitizeStack(originalStack.copy(), 0, Budget())
	}

	internal fun sanitizeIotaGraph(iota: Iota): Iota =
		sanitizeIota(iota, 0, Budget())

	private fun sanitizeStack(stack: ItemStack, depth: Int, budget: Budget): ItemStack {
		if (stack.isEmpty)
			return ItemStack.EMPTY
		if (depth >= MAX_DEPTH || !budget.consume())
			return ItemStack.EMPTY

		sanitizeCustomData(stack, DataComponents.CUSTOM_DATA)
		sanitizeCustomData(stack, DataComponents.ENTITY_DATA)
		sanitizeCustomData(stack, DataComponents.BUCKET_ENTITY_DATA)
		sanitizeCustomData(stack, DataComponents.BLOCK_ENTITY_DATA)

		stack.get(HexDataComponents.IOTA_HOLDER_IOTA.get())?.let {
			stack.set(HexDataComponents.IOTA_HOLDER_IOTA.get(), sanitizeIota(it, depth + 1, budget))
		}
		stack.get(HexDataComponents.HEX_HOLDER_PATTERNS.get())?.let { patterns ->
			stack.set(
				HexDataComponents.HEX_HOLDER_PATTERNS.get(),
				patterns.map { sanitizeIota(it, depth + 1, budget) }
			)
		}
		stack.get(HexDataComponents.SPELLBOOK_PAGES.get())?.let { pages ->
			stack.set(
				HexDataComponents.SPELLBOOK_PAGES.get(),
				pages.mapValues { (_, iota) -> sanitizeIota(iota, depth + 1, budget) }
			)
		}

		stack.get(DataComponents.CONTAINER)?.let { contents ->
			val sanitized = (0 until contents.slots).map { slot ->
				sanitizeStack(contents.getStackInSlot(slot).copy(), depth + 1, budget)
			}
			stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(sanitized))
		}
		stack.get(DataComponents.BUNDLE_CONTENTS)?.let { contents ->
			val sanitized = contents.itemsCopy().map {
				sanitizeStack(it, depth + 1, budget)
			}
			stack.set(DataComponents.BUNDLE_CONTENTS, BundleContents(sanitized))
		}
		stack.get(DataComponents.CHARGED_PROJECTILES)?.let { projectiles ->
			val sanitized = projectiles.items.map {
				sanitizeStack(it.copy(), depth + 1, budget)
			}
			stack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(sanitized))
		}

		return stack
	}

	private fun sanitizeIota(iota: Iota, depth: Int, budget: Budget): Iota {
		if (depth >= MAX_DEPTH || !budget.consume())
			return GarbageIota()
		return when (iota) {
			is ItemStackIota -> ItemStackIota(ItemStack.EMPTY)
			is ListIota -> ListIota(iota.list.map { sanitizeIota(it, depth + 1, budget) })
			is ContinuationIota -> ContinuationIota(
				sanitizeContinuation(iota.continuation, depth + 1, budget)
			)
			else -> iota
		}
	}

	private fun sanitizeContinuation(
		continuation: SpellContinuation,
		depth: Int,
		budget: Budget
	): SpellContinuation {
		if (depth >= MAX_DEPTH || !budget.consume())
			return SpellContinuation.Done
		return when (continuation) {
			is SpellContinuation.NotDone -> SpellContinuation.NotDone(
				sanitizeFrame(continuation.frame, depth + 1, budget),
				sanitizeContinuation(continuation.next, depth + 1, budget)
			)
			else -> continuation
		}
	}

	private fun sanitizeFrame(frame: ContinuationFrame, depth: Int, budget: Budget): ContinuationFrame =
		when (frame) {
			is FrameEvaluate -> FrameEvaluate(
				sanitizeTreeList(frame.list, depth + 1, budget),
				frame.isMetacasting
			)
			is FrameForEach -> FrameForEach(
				sanitizeTreeList(frame.data, depth + 1, budget),
				sanitizeTreeList(frame.code, depth + 1, budget),
				sanitizeTreeList(frame.contextStack, depth + 1, budget),
				sanitizeTreeList(frame.stashedStack, depth + 1, budget),
				sanitizeTreeList(frame.acc, depth + 1, budget)
			)
			else -> frame
		}

	private fun sanitizeTreeList(list: TreeList<Iota>, depth: Int, budget: Budget): TreeList<Iota> =
		TreeList.from(list.map { sanitizeIota(it, depth + 1, budget) })

	private fun sanitizeCustomData(stack: ItemStack, type: DataComponentType<CustomData>) {
		val customData = stack.get(type) ?: return
		val tag = customData.copyTag()
		sanitizeNbt(tag)
		stack.set(type, CustomData.of(tag))
	}

	private fun sanitizeNbt(root: CompoundTag) {
		val queue = ArrayDeque<Tag>()
		queue.add(root)
		while (queue.isNotEmpty()) {
			when (val next = queue.removeFirst()) {
				is ListTag -> next.forEach(queue::addLast)
				is CompoundTag -> {
					if (next.contains(ItemStackIota.TAG_STACK_ID)) {
						next.remove(ItemStackIota.TAG_STACK_ID)
						next.remove(ItemStackIota.TAG_STACK_COUNT)
						next.remove(ItemStackIota.TAG_STACK_NBT)
					}
					if (next.getString(CURRENT_IOTA_TYPE) == ITEM_STACK_IOTA_ID)
						next.put(CURRENT_IOTA_STACK, CompoundTag())
					next.allKeys.mapNotNullTo(queue) { next.get(it) }
				}
			}
		}
	}
}
