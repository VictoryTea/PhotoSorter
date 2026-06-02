package com.example.photosorter.ui.leaderboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.photosorter.data.repository.LeaderboardEntry

private val DarkBackground = Color(0xFF1A1A2E)
private val DarkSurface = Color(0xFF16213E)
private val GoldColor = Color(0xFFFFD700)
private val SilverColor = Color(0xFFC0C0C0)
private val BronzeColor = Color(0xFFCD7F32)
private val AccentColor = Color(0xFFE94560)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: LeaderboardViewModel,
    modifier: Modifier = Modifier
) {
    val currentUsername by viewModel.currentUsername.collectAsStateWithLifecycle()
    val topPlayers by viewModel.topPlayers.collectAsStateWithLifecycle()
    val isJoining by viewModel.isJoining.collectAsStateWithLifecycle()
    val joinError by viewModel.joinError.collectAsStateWithLifecycle()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (currentUsername == null) {
            JoinLeaderboardContent(
                isJoining = isJoining,
                joinError = joinError,
                onJoin = { viewModel.joinLeaderboard(it) },
                onClearError = { viewModel.clearError() }
            )
        } else {
            LeaderboardListContent(
                topPlayers = topPlayers,
                currentUsername = currentUsername!!
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinLeaderboardContent(
    isJoining: Boolean,
    joinError: String?,
    onJoin: (String) -> Unit,
    onClearError: () -> Unit
) {
    var usernameInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Trophy",
            tint = GoldColor,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Join the Leaderboard",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Compete with others and show off your photo sorting skills!",
            fontSize = 16.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = usernameInput,
            onValueChange = { 
                usernameInput = it
                if (joinError != null) onClearError()
            },
            label = { Text("Choose a Username", color = Color.Gray) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentColor,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = joinError != null) {
            Text(
                text = joinError ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onJoin(usernameInput) },
            enabled = !isJoining && usernameInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isJoining) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Join Now", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaderboardListContent(
    topPlayers: List<LeaderboardEntry>,
    currentUsername: String
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "Top Sorters",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(topPlayers) { index, player ->
                val isCurrentUser = player.username == currentUsername
                LeaderboardRow(
                    rank = index + 1,
                    player = player,
                    isCurrentUser = isCurrentUser
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    player: LeaderboardEntry,
    isCurrentUser: Boolean
) {
    val backgroundColor = if (isCurrentUser) AccentColor.copy(alpha = 0.2f) else DarkSurface
    val borderColor = if (isCurrentUser) AccentColor else Color.Transparent

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getRankColor(rank)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    color = if (rank <= 3) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Player Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.username,
                    color = Color.White,
                    fontWeight = if (isCurrentUser) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (isCurrentUser) {
                    Text(
                        text = "You",
                        color = AccentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Score
            Text(
                text = "${player.score} pts",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }
    }
}

private fun getRankColor(rank: Int): Color {
    return when (rank) {
        1 -> GoldColor
        2 -> SilverColor
        3 -> BronzeColor
        else -> Color.DarkGray
    }
}
