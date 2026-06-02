package com.example.photosorter.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.photosorter.theme.AccentPurple
import com.example.photosorter.theme.DarkCard
import com.example.photosorter.theme.GlassBorder
import com.example.photosorter.theme.TextMuted
import com.example.photosorter.theme.TextPrimary
import com.example.photosorter.theme.TextSecondary
import com.example.photosorter.theme.TrashRed

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var usePermanentDelete by remember { mutableStateOf(false) }
    var showMetadata by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Sorting Preferences Section
        SectionHeader(text = "Sorting")

        SettingsCard {
            SettingsSwitchItem(
                icon = Icons.Default.DeleteSweep,
                title = "Permanent Delete",
                subtitle = if (usePermanentDelete) "Photos will be permanently deleted"
                else "Photos will be moved to trash (recoverable)",
                checked = usePermanentDelete,
                onCheckedChange = { usePermanentDelete = it }
            )

            HorizontalDivider(color = GlassBorder)

            SettingsSwitchItem(
                icon = Icons.Default.PhotoLibrary,
                title = "Show Photo Details",
                subtitle = "Display filename, date, and size on cards",
                checked = showMetadata,
                onCheckedChange = { showMetadata = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About Section
        SectionHeader(text = "About")

        SettingsCard {
            SettingsInfoItem(
                icon = Icons.Default.Info,
                title = "Photo Sorter",
                subtitle = "Version ${com.example.photosorter.util.getAppVersion()}"
            )
            HorizontalDivider(color = GlassBorder)
            SettingsInfoItem(
                icon = Icons.Default.Code,
                title = "Built with",
                subtitle = "Jetpack Compose + Material 3"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = AccentPurple,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCard)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
    ) {
        content()
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (icon == Icons.Default.DeleteSweep && checked) TrashRed else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentPurple,
                checkedTrackColor = AccentPurple.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}
