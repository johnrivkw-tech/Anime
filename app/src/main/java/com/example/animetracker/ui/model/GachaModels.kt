package com.example.animetracker.ui.model

import com.example.animetracker.data.network.AniListCharacterNode
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * One Piece gacha rarity ladder. [weight] is the probability of a *pull*
 * landing on this tier (must sum to 1.0 across all entries) — this is a
 * separate concern from how many characters happen to live in the tier.
 * No pity system: every pull is an independent draw against these odds.
 */
enum class GachaRarity(val label: String, val weight: Double, val colorHex: Long) {
    MYTHIC("Mythic", 0.0025, 0xFFFF2D6B),
    LEGENDARY("Legendary", 0.02, 0xFFFFC947),
    EPIC("Epic", 0.0775, 0xFFB566FF),
    RARE("Rare", 0.30, 0xFF4FC3F7),
    COMMON("Common", 0.60, 0xFF9E9E9E)
}

/** A single pullable character, resolved from AniList data + a rarity tier. */
data class GachaCharacter(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val tier: GachaRarity,
    val favourites: Int
)

/** Cost in berries to pull. Ten-pull is a 10% discount over ten singles. */
const val GACHA_SINGLE_PULL_COST = 100L
const val GACHA_TEN_PULL_COST = 900L

/** Characters that are always Mythic regardless of AniList favourite count. */
val GACHA_FORCED_MYTHIC = setOf("Imu", "Gol D. Roger", "Joy Boy")

/**
 * Turns a flat list of AniList character nodes (already sorted or not) into
 * a tiered gacha roster. More-favourited characters are rarer pulls — the
 * most beloved/powerful cast members sit at the top of the pyramid instead
 * of being trivially common, which is the opposite of AniList's own
 * favourites ranking but is what makes a gacha pull feel earned.
 *
 * [forcedMythicNames] characters are pinned to Mythic outright (matched by
 * substring, case-insensitive) so lore-critical reveals like Imu land in
 * the top tier even if their favourite count hasn't caught up yet.
 */
fun assignGachaTiers(
    nodes: List<AniListCharacterNode>,
    forcedMythicNames: Set<String> = GACHA_FORCED_MYTHIC
): List<GachaCharacter> {
    val sorted = nodes.distinctBy { it.id }.sortedByDescending { it.favourites }
    val total = sorted.size
    if (total == 0) return emptyList()

    val mythicCount = minOf(total, maxOf(3, (total * 0.02).roundToInt()))
    val legendaryCount = minOf(total - mythicCount, maxOf(6, (total * 0.08).roundToInt()))
    val epicCount = minOf(
        total - mythicCount - legendaryCount,
        maxOf(12, (total * 0.15).roundToInt())
    )
    val rareCount = minOf(
        total - mythicCount - legendaryCount - epicCount,
        maxOf(20, (total * 0.30).roundToInt())
    )

    return sorted.mapIndexed { index, node ->
        val isForced = forcedMythicNames.any { forced ->
            node.displayName.contains(forced, ignoreCase = true)
        }
        val tier = when {
            isForced -> GachaRarity.MYTHIC
            index < mythicCount -> GachaRarity.MYTHIC
            index < mythicCount + legendaryCount -> GachaRarity.LEGENDARY
            index < mythicCount + legendaryCount + epicCount -> GachaRarity.EPIC
            index < mythicCount + legendaryCount + epicCount + rareCount -> GachaRarity.RARE
            else -> GachaRarity.COMMON
        }
        GachaCharacter(
            id = node.id,
            name = node.displayName,
            imageUrl = node.imageUrl,
            tier = tier,
            favourites = node.favourites
        )
    }
}

/**
 * Draws [count] characters from [roster] using [GachaRarity.weight] odds,
 * pure RNG with no pity. If the rolled tier happens to be empty in this
 * roster, falls back to the next-most-common tier so a pull never wastes
 * berries on nothing.
 */
fun rollGacha(roster: List<GachaCharacter>, count: Int, random: Random = Random.Default): List<GachaCharacter> {
    if (roster.isEmpty()) return emptyList()
    val byTier = roster.groupBy { it.tier }
    val tierOrder = GachaRarity.entries.sortedBy { it.weight } // rarest first for fallback search

    return (1..count).map {
        val roll = random.nextDouble()
        var cumulative = 0.0
        var chosenTier = GachaRarity.COMMON
        // Walk tiers in declared order (MYTHIC..COMMON), accumulating weight,
        // and stop at the first bucket the roll lands in.
        for (tier in GachaRarity.entries) {
            cumulative += tier.weight
            chosenTier = tier
            if (roll <= cumulative) break
        }
        val pool = byTier[chosenTier]?.takeIf { it.isNotEmpty() }
            ?: tierOrder.firstNotNullOfOrNull { fallback -> byTier[fallback]?.takeIf { it.isNotEmpty() } }
            ?: roster
        pool.random(random)
    }
}
