package com.example.animetracker.ui.model

/**
 * A single rung on the One Piece-flavored rank ladder. [level] runs 1-12,
 * [completedRequired] is how many titles must be in the Completed list to
 * reach it (top tier requires 500).
 */
data class RankTier(
    val level: Int,
    val title: String,
    val completedRequired: Int
)

/** Pirate-side ladder, lowest to highest. */
private val pirateRanks = listOf(
    RankTier(1, "Deckhand", 5),
    RankTier(2, "Rookie Pirate", 15),
    RankTier(3, "Crewmate", 30),
    RankTier(4, "First Mate", 50),
    RankTier(5, "Quartermaster", 80),
    RankTier(6, "Vice Captain", 115),
    RankTier(7, "Captain", 160),
    RankTier(8, "Fleet Captain", 210),
    RankTier(9, "Warlord", 265),
    RankTier(10, "Emperor", 330),
    RankTier(11, "Grand Emperor", 410),
    RankTier(12, "King of the Pirates", 500)
)

/** Marine-side ladder, lowest to highest. */
private val marineRanks = listOf(
    RankTier(1, "Cadet", 5),
    RankTier(2, "Recruit", 15),
    RankTier(3, "Seaman", 30),
    RankTier(4, "Petty Officer", 50),
    RankTier(5, "Ensign", 80),
    RankTier(6, "Lieutenant", 115),
    RankTier(7, "Commander", 160),
    RankTier(8, "Captain", 210),
    RankTier(9, "Rear Admiral", 265),
    RankTier(10, "Vice Admiral", 330),
    RankTier(11, "Admiral", 410),
    RankTier(12, "Fleet Admiral", 500)
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
