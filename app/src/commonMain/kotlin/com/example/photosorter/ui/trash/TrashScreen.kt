package com.example.photosorter.ui.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.photosorter.theme.DarkBackground
import com.example.photosorter.theme.DarkSurface

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Restore
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.material.icons.filled.CheckCircle

private val TrashRed = Color(0xFFFF6B6B)
private val KeepGreen = Color(0xFF4ECDC4)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel,
    modifier: Modifier = Modifier
) {
    val trashedPhotos by viewModel.trashedPhotos.collectAsStateWithLifecycle()
    val isDeleting by viewModel.isDeleting.collectAsStateWithLifecycle()
    val selectedPhotoIds by viewModel.selectedPhotoIds.collectAsStateWithLifecycle()

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (selectedPhotoIds.isEmpty()) "Review Trash" else "${selectedPhotoIds.size} Selected", 
                        color = Color.White, 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            if (trashedPhotos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🗑️", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Trash is empty",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    contentPadding = PaddingValues(bottom = 100.dp) // Leave space for bottom bar and button
                ) {
                    items(trashedPhotos) { photo ->
                        val isSelected = selectedPhotoIds.contains(photo.id)
                        val inSelectionMode = selectedPhotoIds.isNotEmpty()

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .combinedClickable(
                                    onClick = {
                                        if (inSelectionMode) {
                                            viewModel.toggleSelection(photo.id)
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.toggleSelection(photo.id)
                                    }
                                )
                        ) {
                            AsyncImage(
                                model = photo.uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .border(2.dp, KeepGreen, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = KeepGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (trashedPhotos.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedPhotoIds.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.restoreSelected() },
                        enabled = !isDeleting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KeepGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { viewModel.emptyTrash() },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrashRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedPhotoIds.isNotEmpty()) "Delete" else "Empty Trash (${trashedPhotos.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
