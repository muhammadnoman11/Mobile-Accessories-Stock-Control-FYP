package com.fyp.mobileaccessories.stockcontrol.admin.ui.screen.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fyp.mobileaccessories.stockcontrol.data.local.entity.AnnouncementEntity
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTextField
import com.fyp.mobileaccessories.stockcontrol.ui.component.AppTopBar
import com.fyp.mobileaccessories.stockcontrol.ui.component.ConfirmDialog
import com.fyp.mobileaccessories.stockcontrol.ui.component.DropdownField
import com.fyp.mobileaccessories.stockcontrol.ui.component.EmptyState
import com.fyp.mobileaccessories.stockcontrol.ui.component.PrimaryButton
import com.fyp.mobileaccessories.stockcontrol.ui.theme.AppColors
import com.fyp.mobileaccessories.stockcontrol.util.DateUtils

// ============================================================
// ManageAnnouncementsScreen
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAnnouncementsScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    var showSheet by remember { mutableStateOf(false) }
    var editing by remember {
        mutableStateOf<AnnouncementEntity?>(
            null
        )
    }
    var deleteTarget by remember {
        mutableStateOf<AnnouncementEntity?>(
            null
        )
    }
    var snack by remember { mutableStateOf("") }
    val snackState = remember { SnackbarHostState() }

    LaunchedEffect(snack) {
        if (snack.isNotBlank()) {
            snackState.showSnackbar(snack); snack = ""
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Announcements",
                subtitle = "${announcements.size} records", onBack = { navController.navigateUp() })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editing = null; showSheet = true },
                containerColor = AppColors.Primary
            ) { Icon(Icons.Default.Add, null, tint = Color.White) }
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        if (announcements.isEmpty()) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding)) {
                EmptyState(
                    "No announcements", Icons.Default.Campaign,
                    "Add Announcement"
                ) { editing = null; showSheet = true }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(announcements, key = { it.id }) { ann ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                                Text(
                                    ann.title,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    color = AppColors.InfoContainer,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        ann.type,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppColors.Info,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 3.dp
                                        )
                                    )
                                }
                            }
                            Text(
                                ann.content, style = MaterialTheme.typography.bodySmall,
                                color = AppColors.TextSecondary, maxLines = 2
                            )
                            Text(
                                DateUtils.formatDate(ann.dateCreated),
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.TextTertiary
                            )
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(onClick = { editing = ann; showSheet = true }) {
                                    Icon(Icons.Default.Edit, null, tint = AppColors.Primary)
                                }
                                IconButton(onClick = { deleteTarget = ann }) {
                                    Icon(Icons.Default.Delete, null, tint = AppColors.Error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        AnnouncementSheet(
            ann = editing, onDismiss = { showSheet = false },
            onSave = { a ->
                viewModel.saveAnnouncement(a) { ok, msg -> snack = msg; if (ok) showSheet = false }
            })
    }
    deleteTarget?.let { a ->
        ConfirmDialog(
            "Delete announcement?", "\"${a.title}\" will be removed.",
            onConfirm = {
                viewModel.deleteAnnouncement(a) { snack = "Deleted" }; deleteTarget = null
            },
            onDismiss = { deleteTarget = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnnouncementSheet(
    ann: AnnouncementEntity?,
    onDismiss: () -> Unit,
    onSave: (AnnouncementEntity) -> Unit
) {
    var title by remember { mutableStateOf(ann?.title ?: "") }
    var content by remember { mutableStateOf(ann?.content ?: "") }
    var type by remember { mutableStateOf(ann?.type ?: "GENERAL") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (ann == null) "New Announcement" else "Edit Announcement",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
            )
            AppTextField(title, { title = it }, "Title")
            AppTextField(content, { content = it }, "Content", singleLine = false, maxLines = 4)
            DropdownField(
                "Type", type,
                listOf("GENERAL", "SALE", "ALERT", "UPDATE"), { type = it })
            PrimaryButton("Save", onClick = {
                if (title.isBlank()) return@PrimaryButton
                onSave(
                    AnnouncementEntity(
                        id = ann?.id ?: 0, title = title.trim(),
                        content = content.trim(), type = type
                    )
                )
            })
            Spacer(Modifier.height(16.dp))
        }
    }
}
