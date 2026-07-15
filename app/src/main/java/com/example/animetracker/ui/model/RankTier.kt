package com.example.animetracker.ui.model

/**
 * A single rung on the One Piece-flavored rank ladder. [level] runs 1-7,
 * [completedRequired] is how many titles must be in the Completed list to
 * reach it.
 */
data class RankTier(
    val level: Int,
    val title: String,
    val completedRequired: Int
)

/** Pirate-side ladder, lowest to highest. */
private val pirateRanks = listOf(
    RankTier(1, "Rookie Pirate", 10),
    RankTier(2, "Crewmate", 25),
    RankTier(3, "Vice Captain", 50),
    RankTier(4, "Captain", 100),
    RankTier(5, "Warlord", 150),
    RankTier(6, "Emperor", 200),
    RankTier(7, "King of the Pirates", 275)
)

/** Marine-side ladder, lowest to highest. */
private val marineRanks = listOf(
    RankTier(1, "Recruit", 10),
    RankTier(2, "Seaman", 25),
    RankTier(3, "Lieutenant", 50),
    RankTier(4, "Commander", 100),
    RankTier(5, "Vice Admiral", 150),
    RankTier(6, "Admiral", 200),
    RankTier(7, "Fleet Admiral", 275)
)

fun ranksFor(faction: Faction): List<RankTier> = when (faction) {
    Faction.PIRATE -> pirateRanks
    Faction.MARINE -> marineRanks
}

/** The highest tier whose requirement [completed] meets, or null if under the first threshold. */
fun currentRank(faction: Faction, completed: Int): RankTier? =
    ranksFor(faction).lastOrNull { completed >= it.completedRequired }

/** The next locked tier above the current one, or null if already at the top. */
fun nextRank(faction: Faction, completed: Int): RankTier? =
    ranksFor(faction).firstOrNull { completed < it.completedRequired }
