package com.example.moonsyncapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moonsyncapp.data.model.CyclePhase
import com.example.moonsyncapp.ui.theme.MoonSyncTheme
import com.example.moonsyncapp.ui.theme.MoonTheme
import com.example.moonsyncapp.ui.theme.tokens.toColorTokens

/**
 * Full component gallery preview — shows every MoonSync component in all variants.
 *
 * Open in Android Studio → Split view to use as a living design reference.
 * This file ships in debug builds only; remove from release via build flavors
 * when the product ships.
 */
@Preview(
    name = "Component Gallery",
    showBackground = true,
    widthDp = 420,
    heightDp = 2400,
)
@Composable
fun ComponentGallery() {
    MoonSyncTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // ─────────────────────────────────────────────────────────────
            // PhaseRing
            // ─────────────────────────────────────────────────────────────
            item { GallerySection("PhaseRing") }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(CyclePhase.entries) { phase ->
                        PhaseRing(
                            phase = phase,
                            progress = when (phase) {
                                CyclePhase.MENSTRUAL  -> 0.6f
                                CyclePhase.FOLLICULAR -> 0.45f
                                CyclePhase.OVULATION  -> 0.9f
                                CyclePhase.LUTEAL     -> 0.25f
                            },
                            day = when (phase) {
                                CyclePhase.MENSTRUAL  -> 3
                                CyclePhase.FOLLICULAR -> 9
                                CyclePhase.OVULATION  -> 15
                                CyclePhase.LUTEAL     -> 21
                            },
                            size = 100.dp,
                            remainingDays = 4,
                        )
                    }
                }
            }

            item { GallerySeparator() }

            // ─────────────────────────────────────────────────────────────
            // MoonButton
            // ─────────────────────────────────────────────────────────────
            item { GallerySection("MoonButton — Variants × Sizes") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // All variants at Medium size
                    MoonButtonVariant.entries.forEach { variant ->
                        MoonButton(
                            onClick = {},
                            label = variant.name,
                            variant = variant,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    GallerySubsection("Sizes (Primary)")
                    MoonButtonSize.entries.forEach { sz ->
                        MoonButton(
                            onClick = {},
                            label = "Button — ${sz.name}",
                            size = sz,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    GallerySubsection("Phase-tinted Primary")
                    CyclePhase.entries.forEach { phase ->
                        MoonButton(
                            onClick = {},
                            label = phase.displayName,
                            phaseColor = phase,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    GallerySubsection("Loading + Leading icon")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MoonButton(
                            onClick = {},
                            label = "Saving…",
                            loading = true,
                            modifier = Modifier.weight(1f),
                        )
                        MoonButton(
                            onClick = {},
                            label = "Search",
                            icon = Icons.Outlined.Search,
                            variant = MoonButtonVariant.Secondary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    MoonButton(
                        onClick = {},
                        label = "Disabled",
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item { GallerySeparator() }

            // ─────────────────────────────────────────────────────────────
            // MoonChip
            // ─────────────────────────────────────────────────────────────
            item { GallerySection("MoonChip") }
            item {
                val chipLabels = listOf(
                    "Energetic", "Tired", "Cramps", "Happy",
                    "Anxious", "Hydrated", "Exercised", "Calm",
                )
                val selected = remember { mutableStateListOf("Energetic", "Cramps") }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GallerySubsection("Multi-select chip grid (tap to toggle)")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(chipLabels) { label ->
                            MoonChip(
                                selected = label in selected,
                                label = label,
                                onSelect = {
                                    if (label in selected) selected.remove(label)
                                    else selected.add(label)
                                },
                            )
                        }
                    }
                    GallerySubsection("With icon")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MoonChip(
                            selected = true,
                            label = "Favourite",
                            icon = Icons.Outlined.Favorite,
                            onSelect = {},
                        )
                        MoonChip(
                            selected = false,
                            label = "Search",
                            icon = Icons.Outlined.Search,
                            onSelect = {},
                        )
                    }
                }
            }

            item { GallerySeparator() }

            // ─────────────────────────────────────────────────────────────
            // MoonTextField
            // ─────────────────────────────────────────────────────────────
            item { GallerySection("MoonTextField") }
            item {
                var outlinedText by remember { mutableStateOf("") }
                var filledText   by remember { mutableStateOf("Some content") }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MoonTextField(
                        value = outlinedText,
                        onValueChange = { outlinedText = it },
                        label = "Outlined",
                        placeholder = "Placeholder…",
                        variant = MoonTextFieldVariant.Outlined,
                    )
                    MoonTextField(
                        value = filledText,
                        onValueChange = { filledText = it },
                        label = "Filled",
                        variant = MoonTextFieldVariant.Filled,
                    )
                    MoonTextField(
                        value = "Ghost — journal",
                        onValueChange = {},
                        placeholder = "Write anything…",
                        variant = MoonTextFieldVariant.Ghost,
                    )
                    MoonTextField(
                        value = "bad value",
                        onValueChange = {},
                        label = "Error state",
                        error = "This field is required",
                        variant = MoonTextFieldVariant.Outlined,
                    )
                    MoonTextField(
                        value = "With helper",
                        onValueChange = {},
                        label = "Supporting text",
                        supportingText = "Up to 200 characters",
                        variant = MoonTextFieldVariant.Outlined,
                    )
                }
            }

            item { GallerySeparator() }

            // ─────────────────────────────────────────────────────────────
            // MoonCard
            // ─────────────────────────────────────────────────────────────
            item { GallerySection("MoonCard — Elevation × Border") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GallerySubsection("Elevation tiers")
                    MoonCardElevation.entries.forEach { elev ->
                        MoonCard(
                            elevation = elev,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Box(Modifier.padding(16.dp)) {
                                Text("${elev.name} elevation", style = MoonTheme.typography.labelLarge)
                            }
                        }
                    }
                    GallerySubsection("Border variants")
                    MoonCard(
                        elevation = MoonCardElevation.None,
                        border = MoonCardBorder.Subtle,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(Modifier.padding(16.dp)) {
                            Text("Subtle border", style = MoonTheme.typography.labelLarge)
                        }
                    }
                    GallerySubsection("Phase borders")
                    CyclePhase.entries.forEach { phase ->
                        MoonCard(
                            elevation = MoonCardElevation.None,
                            border = MoonCardBorder.Phase,
                            phase = phase,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Box(Modifier.padding(16.dp)) {
                                Text(
                                    "${phase.displayName} card",
                                    style = MoonTheme.typography.labelLarge,
                                    color = phase.toColorTokens().text,
                                )
                            }
                        }
                    }
                    GallerySubsection("Clickable card")
                    MoonCard(
                        elevation = MoonCardElevation.Medium,
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(Modifier.padding(16.dp)) {
                            Text("Tap me →", style = MoonTheme.typography.labelLarge)
                        }
                    }
                }
            }

            item { GallerySeparator() }

            // ─────────────────────────────────────────────────────────────
            // MoonBadge
            // ─────────────────────────────────────────────────────────────
            item { GallerySection("MoonBadge") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GallerySubsection("Phase badges")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(CyclePhase.entries) { phase ->
                            MoonBadge(
                                type  = MoonBadgeType.Phase,
                                value = phase.displayName,
                                phase = phase,
                            )
                        }
                    }
                    GallerySubsection("Streak")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("1 day", "7 days", "30 days", "100 days").forEach { s ->
                            MoonBadge(type = MoonBadgeType.Streak, value = s)
                        }
                    }
                    GallerySubsection("Verified")
                    MoonBadge(type = MoonBadgeType.Verified, value = "Verified")
                    GallerySubsection("Wisdom levels")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("Seedling 🌱", "Sprout 🌿", "Blooming 🌸", "Flourishing 🌺", "Wise Tree 🌳")) { level ->
                            MoonBadge(type = MoonBadgeType.Level, value = level)
                        }
                    }
                }
            }

            item { GallerySeparator() }

            // ─────────────────────────────────────────────────────────────
            // PhaseChip
            // ─────────────────────────────────────────────────────────────
            item { GallerySection("PhaseChip") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Full label
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(CyclePhase.entries) { phase -> PhaseChip(phase = phase) }
                    }
                    // Abbreviated
                    GallerySubsection("Abbreviated labels")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CyclePhase.entries.forEach { phase ->
                            PhaseChip(
                                phase = phase,
                                label = phase.name.take(3),
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// ---------------------------------------------------------------------------
// Gallery helpers (internal only)
// ---------------------------------------------------------------------------

@Composable
private fun GallerySection(title: String) {
    Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
        Text(
            text = title.uppercase(),
            style = MoonTheme.typography.labelSmall.copy(
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.5.sp,
            ),
            color = MoonTheme.colors.primary,
        )
    }
}

@Composable
private fun GallerySubsection(title: String) {
    Text(
        text = title,
        style = MoonTheme.typography.labelSmall,
        color = MoonTheme.colors.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
    )
}

@Composable
private fun GallerySeparator() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        thickness = 0.5.dp,
        color = MoonTheme.colors.outlineVariant,
    )
}

