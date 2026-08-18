package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import com.example.ui.components.EditDocumentSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.example.data.model.DocumentItem
import com.example.data.model.DocumentType
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.gestures.scrollBy
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.ui.components.AddDocumentSheet
import com.example.ui.components.BottomSearchBar
import com.example.ui.components.DocumentCard
import com.example.ui.components.DocumentViewerDialog
import com.example.ui.components.PinInputDialog
import com.example.ui.components.PinUnlockScreen
import com.example.ui.components.ProfileAndSettingsSheet
import com.example.ui.components.TopHeaderBar
import com.example.ui.components.WelcomeSplashScreen
import com.example.ui.theme.EzWalletTheme
import com.example.ui.viewmodel.PinPromptAction
import com.example.ui.viewmodel.ViewMode
import com.example.ui.viewmodel.WalletViewModel
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.components.LocalAnimatedVisibilityScope
import com.example.ui.components.LocalSharedTransitionScope
import com.example.ui.components.paperBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val walletViewModel: WalletViewModel = viewModel()
            val isDarkMode by walletViewModel.isDarkMode.collectAsStateWithLifecycle()
            val userProfile by walletViewModel.userProfile.collectAsStateWithLifecycle()

            EzWalletTheme(
                darkTheme = isDarkMode,
                customBgColor = userProfile.customBgColor,
                customSelectionColor = userProfile.customSelectionColor,
                customFileColor = userProfile.customFileColor
            ) {
                EzWalletApp(viewModel = walletViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EzWalletApp(viewModel: WalletViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isSplashScreen by viewModel.isSplashScreen.collectAsStateWithLifecycle()
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedDocumentIds.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val draggedDocumentId by viewModel.draggedDocumentId.collectAsStateWithLifecycle()
    val viewingGroupId by viewModel.viewingGroupId.collectAsStateWithLifecycle()
    val viewingGroupName by viewModel.viewingGroupName.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val isAboutSheetOpen by viewModel.isAboutSheetOpen.collectAsStateWithLifecycle()
    val isAddDocumentSheetOpen by viewModel.isAddDocumentSheetOpen.collectAsStateWithLifecycle()
    val viewingDocument by viewModel.viewingDocument.collectAsStateWithLifecycle()
    val pinPromptAction by viewModel.pinPromptAction.collectAsStateWithLifecycle()

    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val aboutSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var showGroupDialog by remember { mutableStateOf(false) }
    var editingDocument by remember { mutableStateOf<DocumentItem?>(null) }

    // First open splash & permission screen
    if (isSplashScreen) {
        WelcomeSplashScreen(
            userName = userProfile.fullName,
            onFinishSplash = { viewModel.dismissSplash() }
        )
        return
    }

    // AppLock Screen if PIN enabled
    if (isAppLocked) {
        PinUnlockScreen(
            userName = userProfile.fullName,
            onUnlockAttempt = { pinEntered ->
                viewModel.unlockApp(pinEntered)
            }
        )
        return
    }

    // Main App Screen
    var lastViewed by remember { androidx.compose.runtime.mutableStateOf<com.example.data.model.DocumentItem?>(null) }
    androidx.compose.runtime.LaunchedEffect(viewingDocument) {
        if (viewingDocument != null) lastViewed = viewingDocument
    }

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = viewingDocument == null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .paperBackground(isDarkMode),
                            containerColor = if (!isDarkMode) Color(0xFFFDFBF7) else MaterialTheme.colorScheme.background,
                            snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopHeaderBar(
                isDarkMode = isDarkMode,
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                viewMode = viewMode,
                onCycleViewMode = { viewModel.cycleViewMode() },
                onOpenMenu = { viewModel.setAboutSheetOpen(true) },
                sortOrder = sortOrder,
                onSortOrderChange = { viewModel.setSortOrder(it) },
                isMultiSelectMode = isMultiSelectMode,
                selectedCount = selectedIds.size,
                onClearSelection = { viewModel.clearSelection() },
                onSelectAll = { viewModel.selectAll() },
                onShareSelected = { viewModel.shareSelectedDocuments() },
                onGroupSelected = { showGroupDialog = true },
                onDeleteSelected = { viewModel.requestDeleteSelected() },
                viewingGroupName = viewingGroupName,
                onBackFromGroup = { viewModel.setViewingGroupId(null) }
            )
        },
        bottomBar = {
            BottomSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                selectedCategory = selectedCategory,
                onSelectCategory = { viewModel.setCategory(it) },
                onAddClick = { viewModel.setAddDocumentSheetOpen(true) }
            )
        }
    ) { innerPadding ->
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
        val coroutineScope = rememberCoroutineScope()
        
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (documents.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matching documents found" else "No Documents Stored Yet",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try searching for a different keyword or check spelling."
                        else "Tap the + button below to securely store Photos, PDFs, ID cards, Contacts, or Copied Notes in EzWallet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (searchQuery.isEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedButton(
                            onClick = { viewModel.setAddDocumentSheetOpen(true) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_empty_state_add")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add First Document")
                        }
                    }
                }
            } else {
                // Document List / Grid
                if (viewMode == ViewMode.BOXES) {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("grid_documents")
                    ) {
                        items(documents, key = { it.id }) { doc ->
                            DocumentCard(
                                doc = doc,
                                viewMode = viewMode,
                                isMultiSelectMode = isMultiSelectMode,
                                isSelected = selectedIds.contains(doc.id),
                                onCardClick = { if (doc.type == DocumentType.GROUP) viewModel.setViewingGroupId(doc.id) else viewModel.viewDocument(doc) },
                                onCardLongClick = {
                                    if (!isMultiSelectMode) {
                                        viewModel.startMultiSelect(doc.id)
                                    } else {
                                        viewModel.toggleSelectDocument(doc.id)
                                    }
                                },
                                onDeleteClick = { viewModel.requestDeleteSingle(doc) },
                                onMoveUp = { viewModel.moveDocumentUp(doc) },
                                onMoveDown = { viewModel.moveDocumentDown(doc) },
                                onDragScroll = { delta ->
                                    coroutineScope.launch {
                                        gridState.scrollBy(-delta)
                                    }
                                },
                                onDragStateChange = { isDragging ->
                                    if (isDragging) viewModel.setDraggedDocumentId(doc.id) else viewModel.setDraggedDocumentId(null)
                                },
                                modifier = if (draggedDocumentId == doc.id) Modifier.zIndex(1f) else Modifier.animateItem()
                            )
                        }
                    }
                } else {
                    // Card View (Top to bottom list)
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("list_documents")
                    ) {
                        items(documents, key = { it.id }) { doc ->
                            DocumentCard(
                                doc = doc,
                                viewMode = viewMode,
                                isMultiSelectMode = isMultiSelectMode,
                                isSelected = selectedIds.contains(doc.id),
                                onCardClick = { if (doc.type == DocumentType.GROUP) viewModel.setViewingGroupId(doc.id) else viewModel.viewDocument(doc) },
                                onCardLongClick = {
                                    if (!isMultiSelectMode) {
                                        viewModel.startMultiSelect(doc.id)
                                    } else {
                                        viewModel.toggleSelectDocument(doc.id)
                                    }
                                },
                                onDeleteClick = { viewModel.requestDeleteSingle(doc) },
                                onMoveUp = { viewModel.moveDocumentUp(doc) },
                                onMoveDown = { viewModel.moveDocumentDown(doc) },
                                onDragScroll = { delta ->
                                    coroutineScope.launch {
                                        listState.scrollBy(-delta)
                                    }
                                },
                                onDragStateChange = { isDragging ->
                                    if (isDragging) viewModel.setDraggedDocumentId(doc.id) else viewModel.setDraggedDocumentId(null)
                                },
                                modifier = if (draggedDocumentId == doc.id) Modifier.zIndex(1f) else Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
                        }
                    }
                } // End of AnimatedVisibility for Scaffold

                // Document Viewer Dialog (Overlay)
                AnimatedVisibility(
                    visible = viewingDocument != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val doc = viewingDocument ?: lastViewed
                    if (doc != null) {
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            DocumentViewerDialog(
                                doc = doc,
                                onDismiss = { viewModel.viewDocument(null) },
                                onDelete = { docToDelete ->
                                    viewModel.requestDeleteSingle(docToDelete)
                                },
                                onEditClick = { docToEdit ->
                                    editingDocument = docToEdit
                                    viewModel.viewDocument(null)
                                },
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                }
            } // End Box
        } // End CompositionLocalProvider
    } // End SharedTransitionLayout

    // Modal Sheets and Dialogs
    
    if (showGroupDialog) {
        var groupTitle by remember { mutableStateOf("") }
        var groupDetails by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showGroupDialog = false },
            title = { Text("Create Group") },
            text = {
                Column {
                    OutlinedTextField(
                        value = groupTitle,
                        onValueChange = { groupTitle = it },
                        label = { Text("Group Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = groupDetails,
                        onValueChange = { groupDetails = it },
                        label = { Text("Group Details (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.groupSelectedDocuments(groupTitle, groupDetails)
                        showGroupDialog = false
                    },
                    enabled = groupTitle.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGroupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    editingDocument?.let { doc ->
        EditDocumentSheet(
            document = doc,
            onDismiss = { editingDocument = null },
            onSave = { id, title, details ->
                viewModel.updateDocumentDetails(id, title, details)
            }
        )
    }

    // Add Document Sheet
    if (isAddDocumentSheetOpen) {
        AddDocumentSheet(
            onDismiss = { viewModel.setAddDocumentSheetOpen(false) },
            onAddFileFromUri = { title, details, date, category, uri, type ->
                viewModel.addFileFromUri(title, details, date, category, uri, type)
                Toast.makeText(context, "$title saved to vault", Toast.LENGTH_SHORT).show()
            },
            onAddBulkFiles = { title, details, category, uris, type ->
                viewModel.addBulkFilesFromUris(title, details, category, uris, type)
                Toast.makeText(context, "Batch saved to vault", Toast.LENGTH_SHORT).show()
            },
            onAddCameraPhoto = { title, details, date, category, bitmap ->
                viewModel.addCameraPhoto(title, details, date, category, bitmap)
                Toast.makeText(context, "$title photo saved to vault", Toast.LENGTH_SHORT).show()
            },
            onAddContact = { title, details, date, category, name, phone ->
                viewModel.addContactDocument(title, details, date, category, name, phone)
                Toast.makeText(context, "$name contact saved to vault", Toast.LENGTH_SHORT).show()
            },
            onAddTextNote = { title, details, date, category, text ->
                viewModel.addTextNoteDocument(title, details, date, category, text)
                Toast.makeText(context, "$title note saved to vault", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 3-Line Menu Drawer / Profile & Settings Sheet
    if (isAboutSheetOpen) {
        ProfileAndSettingsSheet(
            userProfile = userProfile,
            sheetState = aboutSheetState,
            onDismiss = { viewModel.setAboutSheetOpen(false) },
            onUpdateProfile = { name, age, dob, phone, email, address ->
                viewModel.updateUserProfile(name, age, dob, phone, email, address)
            },
            onSetPin = { pin ->
                viewModel.setAppLockPin(pin)
            },
            onDisablePin = {
                viewModel.disableAppLockPin()
            },
            onUpdateTheme = { bg, sel, file ->
                viewModel.updateTheme(bg, sel, file)
            }
        )
    }

    // PIN Confirmation Dialog (for Deleting or Security Actions)
    pinPromptAction?.let { action ->
        val titleText = when (action) {
            is PinPromptAction.DeleteSingle -> "Confirm Delete: ${action.document.title}"
            is PinPromptAction.DeleteMultiple -> "Confirm Delete (${action.documents.size} Files)"
            is PinPromptAction.TurnOffPin -> "Turn Off AppLock PIN"
            is PinPromptAction.ChangePin -> "Verify Current PIN"
        }

        PinInputDialog(
            title = titleText,
            subtitle = "Enter your 4-digit AppLock PIN to confirm",
            onDismiss = { viewModel.dismissPinPrompt() },
            onConfirm = { enteredPin ->
                val success = viewModel.confirmPinPrompt(enteredPin)
                if (success) {
                    Toast.makeText(context, "Action confirmed", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
