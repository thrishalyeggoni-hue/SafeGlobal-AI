package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Compatibility alias — forwards all calls to [TopCenterBrandedLoadingIndicator].
 * The original water-wave animation has been superseded by the orbital radar
 * branded indicator. This shim keeps all existing callers working unchanged.
 */
@Composable
fun WaterWaveLoadingIndicator(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    label: String = "All Protections Armed",
    showBadge: Boolean = true
) {
    TopCenterBrandedLoadingIndicator(
        modifier = modifier,
        isLoading = isLoading,
        label = label,
        showBadge = showBadge
    )
}
