Scaffold { paddingValues ->
    // ...same error handling...
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(vertical = 0.dp)
    ) {
        item {
            FeaturedBanner(
                item = trendingItems.firstOrNull(),
                onClick = { trendingItems.firstOrNull()?.aniListId?.let(onAnimeClick) },
                onAiClick = onChatClick,
                modifier = Modifier // no horizontal padding = edge-to-edge
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        // ...rest of the sections unchanged...
    }
}
