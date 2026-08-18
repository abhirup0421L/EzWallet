package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EzWalletDatabase
import com.example.data.model.DocumentItem
import com.example.data.model.DocumentType
import com.example.data.model.UserProfile
import com.example.data.repository.EzWalletRepository
import com.example.data.storage.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ViewMode {
    BARS,             // Vertical List Bars
    BOXES,            // 2-Column Grid Boxes
    HORIZONTAL_CARDS  // Wide Horizontal Preview Cards
}

sealed interface PinPromptAction {
    data class DeleteSingle(val document: DocumentItem) : PinPromptAction
    data class DeleteMultiple(val documents: List<DocumentItem>) : PinPromptAction
    data object TurnOffPin : PinPromptAction
    data object ChangePin : PinPromptAction
}

enum class SortOrder(val label: String) {
    CUSTOM("Custom Order"),
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    A_Z("A to Z"),
    Z_A("Z to A")
}

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val _sortOrder = MutableStateFlow(SortOrder.CUSTOM)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }


    private val repository: EzWalletRepository

    init {
        val db = EzWalletDatabase.getInstance(application)
        repository = EzWalletRepository(db.documentDao(), db.userProfileDao())
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _draggedDocumentId = MutableStateFlow<Long?>(null)
    val draggedDocumentId: StateFlow<Long?> = _draggedDocumentId.asStateFlow()

    fun setDraggedDocumentId(id: Long?) {
        _draggedDocumentId.value = id
    }

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.BARS)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    private val _selectedDocumentIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedDocumentIds: StateFlow<Set<Long>> = _selectedDocumentIds.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _isSplashScreen = MutableStateFlow(true)
    val isSplashScreen: StateFlow<Boolean> = _isSplashScreen.asStateFlow()

    private val _isAboutSheetOpen = MutableStateFlow(false)
    val isAboutSheetOpen: StateFlow<Boolean> = _isAboutSheetOpen.asStateFlow()

    private val _isAddDocumentSheetOpen = MutableStateFlow(false)
    val isAddDocumentSheetOpen: StateFlow<Boolean> = _isAddDocumentSheetOpen.asStateFlow()

    private val _viewingDocument = MutableStateFlow<DocumentItem?>(null)
    val viewingDocument: StateFlow<DocumentItem?> = _viewingDocument.asStateFlow()

    private val _pinPromptAction = MutableStateFlow<PinPromptAction?>(null)
    val pinPromptAction: StateFlow<PinPromptAction?> = _pinPromptAction.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .flatMapLatest { profile ->
            if (profile == null) {
                MutableStateFlow(UserProfile())
            } else {
                MutableStateFlow(profile)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserProfile()
        )

    private val _viewingGroupId = MutableStateFlow<Long?>(null)
    val viewingGroupId: StateFlow<Long?> = _viewingGroupId.asStateFlow()

    val viewingGroupName: StateFlow<String?> = _viewingGroupId.flatMapLatest { id ->
        if (id == null) {
            MutableStateFlow(null)
        } else {
            repository.allDocuments.map { docs -> docs.find { it.id == id }?.title }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun setViewingGroupId(groupId: Long?) {
        _viewingGroupId.value = groupId
    }

    val documents: StateFlow<List<DocumentItem>> = combine(
        repository.allDocuments,
        _selectedCategory,
        _sortOrder,
        _viewingGroupId,
        _searchQuery
    ) { allDocs, category, sortOrder, groupId, query ->
        val q = query.trim()

        val searchFiltered = if (q.isBlank()) {
            allDocs
        } else {
            allDocs.filter { doc ->
                doc.title.contains(q, ignoreCase = true) ||
                doc.details.contains(q, ignoreCase = true) ||
                doc.category.contains(q, ignoreCase = true) ||
                (doc.contactName?.contains(q, ignoreCase = true) == true) ||
                (doc.textContent?.contains(q, ignoreCase = true) == true)
            }
        }

        val groupFiltered = if (q.isNotBlank()) {
            if (groupId == null) {
                searchFiltered
            } else {
                searchFiltered.filter { it.groupId == groupId }
            }
        } else {
            if (groupId == null) {
                searchFiltered.filter { it.groupId == null }
            } else {
                searchFiltered.filter { it.groupId == groupId }
            }
        }

        val filtered = when (category) {
            "All" -> groupFiltered
            "Photos" -> groupFiltered.filter { it.type == DocumentType.IMAGE_JPG || it.type == DocumentType.IMAGE_PNG || it.type == DocumentType.PHOTO_CAMERA }
            "PDFs" -> groupFiltered.filter { it.type == DocumentType.PDF }
            "Contacts" -> groupFiltered.filter { it.type == DocumentType.CONTACT }
            "Notes" -> groupFiltered.filter { it.type == DocumentType.TEXT_NOTE }
            else -> groupFiltered.filter { it.category.equals(category, ignoreCase = true) }
        }
        
        when (sortOrder) {
            SortOrder.CUSTOM -> filtered
            SortOrder.DATE_DESC -> filtered.sortedByDescending { it.createdAt }
            SortOrder.DATE_ASC -> filtered.sortedBy { it.createdAt }
            SortOrder.A_Z -> filtered.sortedBy { it.title.lowercase(Locale.getDefault()) }
            SortOrder.Z_A -> filtered.sortedByDescending { it.title.lowercase(Locale.getDefault()) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            val profile = repository.getUserProfileSync()
            if (profile.isDarkMode != null) {
                _isDarkMode.value = profile.isDarkMode
            }
            _viewMode.value = try {
                ViewMode.valueOf(profile.viewMode)
            } catch (e: Exception) {
                if (profile.isGridView) ViewMode.BOXES else ViewMode.BARS
            }
            if (profile.isPinSet && profile.pinCode.isNotEmpty()) {
                _isAppLocked.value = true
            }
        }
        
        // Generate thumbnails for older uploaded PDF documents missing them
        viewModelScope.launch(Dispatchers.IO) {
            val docs = repository.allDocuments.first()
            val context = application
            val docsToUpdate = mutableListOf<DocumentItem>()
            for (doc in docs) {
                if (doc.type == DocumentType.PDF && doc.thumbnailPath.isNullOrBlank() && File(doc.internalPath).exists()) {
                    val thumbPath = FileManager.generatePdfThumbnail(context, doc.internalPath)
                    if (thumbPath != null) {
                        docsToUpdate.add(doc.copy(thumbnailPath = thumbPath))
                    }
                }
            }
            if (docsToUpdate.isNotEmpty()) {
                repository.updateDocuments(docsToUpdate)
            }
        }
    }

    fun dismissSplash() {
        _isSplashScreen.value = false
    }

    fun unlockApp(pinEntered: String): Boolean {
        val currentPin = userProfile.value.pinCode
        if (pinEntered == currentPin) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun toggleDarkMode() {
        _isDarkMode.update { current ->
            val newValue = !current
            viewModelScope.launch {
                val currentProfile = repository.getUserProfileSync()
                repository.saveUserProfile(currentProfile.copy(isDarkMode = newValue))
            }
            newValue
        }
    }

    fun cycleViewMode() {
        _viewMode.update { current ->
            val next = when (current) {
                ViewMode.BARS -> ViewMode.BOXES
                ViewMode.BOXES -> ViewMode.HORIZONTAL_CARDS
                ViewMode.HORIZONTAL_CARDS -> ViewMode.BARS
            }
            viewModelScope.launch {
                val currentProfile = repository.getUserProfileSync()
                repository.saveUserProfile(
                    currentProfile.copy(
                        viewMode = next.name,
                        isGridView = (next == ViewMode.BOXES)
                    )
                )
            }
            next
        }
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
        viewModelScope.launch {
            val currentProfile = repository.getUserProfileSync()
            repository.saveUserProfile(
                currentProfile.copy(
                    viewMode = mode.name,
                    isGridView = (mode == ViewMode.BOXES)
                )
            )
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setAboutSheetOpen(isOpen: Boolean) {
        _isAboutSheetOpen.value = isOpen
    }

    fun setAddDocumentSheetOpen(isOpen: Boolean) {
        _isAddDocumentSheetOpen.value = isOpen
    }

    fun viewDocument(document: DocumentItem?) {
        _viewingDocument.value = document
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Drag / Reorder items
    fun moveDocumentUp(doc: DocumentItem) {
        val currentList = documents.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == doc.id }
        if (index > 0) {
            val temp = currentList[index]
            currentList[index] = currentList[index - 1]
            currentList[index - 1] = temp
            
            viewModelScope.launch {
                currentList.forEachIndexed { i, item ->
                    if (item.orderIndex != i) {
                        repository.updateDocument(item.copy(orderIndex = i))
                    }
                }
            }
        }
    }

    fun moveDocumentDown(doc: DocumentItem) {
        val currentList = documents.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == doc.id }
        if (index >= 0 && index < currentList.size - 1) {
            val temp = currentList[index]
            currentList[index] = currentList[index + 1]
            currentList[index + 1] = temp
            
            viewModelScope.launch {
                currentList.forEachIndexed { i, item ->
                    if (item.orderIndex != i) {
                        repository.updateDocument(item.copy(orderIndex = i))
                    }
                }
            }
        }
    }

    // Selection Mode
    fun startMultiSelect(initialId: Long) {
        _isMultiSelectMode.value = true
        _selectedDocumentIds.value = setOf(initialId)
    }

    fun toggleSelectDocument(id: Long) {
        _selectedDocumentIds.update { set ->
            if (set.contains(id)) {
                val next = set - id
                if (next.isEmpty()) {
                    _isMultiSelectMode.value = false
                }
                next
            } else {
                set + id
            }
        }
    }

    fun selectAll() {
        _selectedDocumentIds.value = documents.value.map { it.id }.toSet()
    }

    fun clearSelection() {
        _isMultiSelectMode.value = false
        _selectedDocumentIds.value = emptySet()
    }

    // Profile & PIN Management
    fun updateTheme(bgColor: String?, selectionColor: String?, fileColor: String?) {
        viewModelScope.launch {
            val current = repository.getUserProfileSync()
            val updated = current.copy(
                customBgColor = bgColor,
                customSelectionColor = selectionColor,
                customFileColor = fileColor
            )
            repository.saveUserProfile(updated)
        }
    }

    fun updateUserProfile(
        name: String,
        age: String,
        dob: String,
        phone: String,
        email: String,
        address: String
    ) {
        viewModelScope.launch {
            val current = repository.getUserProfileSync()
            val updated = current.copy(
                fullName = name.trim(),
                age = age.trim(),
                dob = dob.trim(),
                phoneNumber = phone.trim(),
                email = email.trim(),
                address = address.trim()
            )
            repository.saveUserProfile(updated)
        }
    }

    fun setAppLockPin(pin: String) {
        viewModelScope.launch {
            val current = repository.getUserProfileSync()
            repository.saveUserProfile(
                current.copy(
                    isPinSet = true,
                    pinCode = pin
                )
            )
        }
    }

    fun disableAppLockPin() {
        viewModelScope.launch {
            val current = repository.getUserProfileSync()
            repository.saveUserProfile(
                current.copy(
                    isPinSet = false,
                    pinCode = ""
                )
            )
        }
    }

    // Add Document handlers
    fun addFileFromUri(
        title: String,
        details: String,
        date: String,
        category: String,
        sourceUri: Uri,
        docType: DocumentType
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val extension = docType.extension
            val (savedPath, size) = FileManager.copyUriToInternalStorage(context, sourceUri, title, extension)
            val finalDate = if (date.isBlank()) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            } else date

            // Render PDF thumbnail if PDF
            val thumbPath = if (docType == DocumentType.PDF) {
                FileManager.generatePdfThumbnail(context, savedPath)
            } else {
                null
            }

            val doc = DocumentItem(
                title = title.trim(),
                details = details.trim(),
                type = docType,
                internalPath = savedPath,
                thumbnailPath = thumbPath,
                date = finalDate,
                fileSize = size,
                category = category,
                orderIndex = (documents.value.minOfOrNull { it.orderIndex } ?: 0) - 1, groupId = _viewingGroupId.value
            )
            repository.insertDocument(doc)
            withContext(Dispatchers.Main) {
                _isAddDocumentSheetOpen.value = false
            }
        }
    }

    // Bulk upload multiple URIs
    fun addBulkFilesFromUris(
        baseTitle: String,
        details: String,
        category: String,
        uris: List<Uri>,
        docType: DocumentType
    ) {
        if (uris.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val extension = docType.extension

            var minOrder = documents.value.minOfOrNull { it.orderIndex } ?: 0

            val itemsToInsert = uris.mapIndexed { index, uri ->
                minOrder -= 1
                val fileTitle = if (uris.size == 1) {
                    baseTitle.ifBlank { "Document" }
                } else {
                    "${baseTitle.ifBlank { "Document" }}_${index + 1}"
                }
                val (savedPath, size) = FileManager.copyUriToInternalStorage(context, uri, fileTitle, extension)
                val thumbPath = if (docType == DocumentType.PDF) {
                    FileManager.generatePdfThumbnail(context, savedPath)
                } else {
                    null
                }
                DocumentItem(
                    title = fileTitle,
                    details = details.trim(),
                    type = docType,
                    internalPath = savedPath,
                    thumbnailPath = thumbPath,
                    date = currentDate,
                    fileSize = size,
                    category = category,
                    orderIndex = minOrder, groupId = _viewingGroupId.value
                )
            }

            repository.insertDocuments(itemsToInsert)
            withContext(Dispatchers.Main) {
                _isAddDocumentSheetOpen.value = false
            }
        }
    }

    fun addCameraPhoto(
        title: String,
        details: String,
        date: String,
        category: String,
        bitmap: Bitmap
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val (savedPath, size) = FileManager.saveBitmapToInternalStorage(context, bitmap, title)
            val finalDate = if (date.isBlank()) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            } else date

            val doc = DocumentItem(
                title = title.trim(),
                details = details.trim(),
                type = DocumentType.PHOTO_CAMERA,
                internalPath = savedPath,
                thumbnailPath = null,
                date = finalDate,
                fileSize = size,
                category = category,
                orderIndex = (documents.value.minOfOrNull { it.orderIndex } ?: 0) - 1, groupId = _viewingGroupId.value
            )
            repository.insertDocument(doc)
            withContext(Dispatchers.Main) {
                _isAddDocumentSheetOpen.value = false
            }
        }
    }

    fun addContactDocument(
        title: String,
        details: String,
        date: String,
        category: String,
        contactName: String,
        contactPhone: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val (savedPath, size) = FileManager.saveContactVCardToInternalStorage(context, contactName, contactPhone)
            val finalDate = if (date.isBlank()) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            } else date

            val doc = DocumentItem(
                title = title.ifBlank { contactName }.trim(),
                details = details.trim(),
                type = DocumentType.CONTACT,
                internalPath = savedPath,
                thumbnailPath = null,
                date = finalDate,
                contactName = contactName.trim(),
                contactPhone = contactPhone.trim(),
                fileSize = size,
                category = category.ifBlank { "Contact" },
                orderIndex = (documents.value.minOfOrNull { it.orderIndex } ?: 0) - 1, groupId = _viewingGroupId.value
            )
            repository.insertDocument(doc)
            withContext(Dispatchers.Main) {
                _isAddDocumentSheetOpen.value = false
            }
        }
    }

    fun addTextNoteDocument(
        title: String,
        details: String,
        date: String,
        category: String,
        textContent: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val (savedPath, size) = FileManager.saveTextToInternalStorage(context, title, textContent)
            val finalDate = if (date.isBlank()) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            } else date

            val doc = DocumentItem(
                title = title.trim(),
                details = details.trim(),
                type = DocumentType.TEXT_NOTE,
                internalPath = savedPath,
                thumbnailPath = null,
                date = finalDate,
                textContent = textContent,
                fileSize = size,
                category = category.ifBlank { "Note" },
                orderIndex = (documents.value.minOfOrNull { it.orderIndex } ?: 0) - 1, groupId = _viewingGroupId.value
            )
            repository.insertDocument(doc)
            withContext(Dispatchers.Main) {
                _isAddDocumentSheetOpen.value = false
            }
        }
    }

    // Delete Operations with Pin Check
    fun requestDeleteSingle(doc: DocumentItem) {
        val profile = userProfile.value
        if (profile.isPinSet && profile.pinCode.isNotEmpty()) {
            _pinPromptAction.value = PinPromptAction.DeleteSingle(doc)
        } else {
            executeDeleteSingle(doc)
        }
    }

    fun requestDeleteSelected() {
        val selectedIds = _selectedDocumentIds.value
        val docsToDelete = documents.value.filter { selectedIds.contains(it.id) }
        if (docsToDelete.isEmpty()) return

        val profile = userProfile.value
        if (profile.isPinSet && profile.pinCode.isNotEmpty()) {
            _pinPromptAction.value = PinPromptAction.DeleteMultiple(docsToDelete)
        } else {
            executeDeleteMultiple(docsToDelete)
        }
    }

    fun confirmPinPrompt(pin: String): Boolean {
        val profile = userProfile.value
        if (profile.pinCode != pin) {
            return false
        }

        when (val action = _pinPromptAction.value) {
            is PinPromptAction.DeleteSingle -> {
                executeDeleteSingle(action.document)
            }
            is PinPromptAction.DeleteMultiple -> {
                executeDeleteMultiple(action.documents)
            }
            is PinPromptAction.TurnOffPin -> {
                disableAppLockPin()
            }
            is PinPromptAction.ChangePin -> {
                // Handled in UI
            }
            null -> {}
        }
        _pinPromptAction.value = null
        return true
    }

    fun dismissPinPrompt() {
        _pinPromptAction.value = null
    }

    private fun executeDeleteSingle(doc: DocumentItem) {
        viewModelScope.launch {
            if (doc.type == DocumentType.GROUP) {
                // Orphan the children back to root
                val children = repository.allDocuments.first().filter { it.groupId == doc.id }
                if (children.isNotEmpty()) {
                    repository.updateDocuments(children.map { it.copy(groupId = null) })
                }
            }
            repository.deleteDocument(doc)
            if (_viewingDocument.value?.id == doc.id) {
                _viewingDocument.value = null
            }
        }
    }

    private fun executeDeleteMultiple(docs: List<DocumentItem>) {
        viewModelScope.launch {
            val groupIds = docs.filter { it.type == DocumentType.GROUP }.map { it.id }
            if (groupIds.isNotEmpty()) {
                val allDocs = repository.allDocuments.first()
                val childrenToMove = allDocs.filter { it.groupId in groupIds }
                if (childrenToMove.isNotEmpty()) {
                    repository.updateDocuments(childrenToMove.map { it.copy(groupId = null) })
                }
            }
            repository.deleteDocuments(docs)
            clearSelection()
        }
    }

    fun shareSelectedDocuments() {
        val selectedIds = _selectedDocumentIds.value
        val docsToShare = documents.value.filter { selectedIds.contains(it.id) }
        if (docsToShare.isNotEmpty()) {
            val context = getApplication<Application>()
            FileManager.shareMultipleDocuments(context, docsToShare)
        }
    }

    fun groupSelectedDocuments(groupTitle: String, groupDetails: String = "") {
        val selectedIds = _selectedDocumentIds.value
        if (selectedIds.isEmpty()) return
        
        viewModelScope.launch(Dispatchers.IO) {
            val docs = repository.allDocuments.first()
            val docsToGroup = docs.filter { selectedIds.contains(it.id) }
            
            if (docsToGroup.isNotEmpty()) {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val groupDoc = DocumentItem(
                    title = groupTitle.trim().ifBlank { "New Group" },
                    details = groupDetails.trim(),
                    type = DocumentType.GROUP,
                    date = currentDate
                )
                // insert group
                val groupId = repository.insertDocument(groupDoc)
                
                // update children
                val updatedChildren = docsToGroup.map { it.copy(groupId = groupId) }
                repository.updateDocuments(updatedChildren)
                
                withContext(Dispatchers.Main) {
                    clearSelection()
                }
            }
        }
    }

    fun updateDocumentDetails(documentId: Long, newTitle: String, newDetails: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val doc = repository.getDocumentById(documentId)
            if (doc != null) {
                repository.updateDocument(doc.copy(title = newTitle.trim(), details = newDetails.trim()))
            }
        }
    }
}

