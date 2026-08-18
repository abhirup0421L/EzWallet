package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DocumentItem
import com.example.data.model.DocumentType
import com.example.data.storage.FileManager
import com.example.ui.theme.DocContactColor
import com.example.ui.theme.DocImageColor
import com.example.ui.theme.DocPdfColor
import com.example.ui.theme.DocTextColor
import com.example.ui.viewmodel.ViewMode
import java.io.File

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun DocumentCard(
    doc: DocumentItem,
    viewMode: ViewMode,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onCardLongClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onDragScroll: ((Float) -> Unit)? = null,
    onDragStateChange: ((Boolean) -> Unit)? = null,
    isDarkMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    val typeColor = when (doc.type) {
        DocumentType.IMAGE_JPG, DocumentType.IMAGE_PNG, DocumentType.PHOTO_CAMERA -> DocImageColor
        DocumentType.PDF -> DocPdfColor
        DocumentType.CONTACT -> DocContactColor
        DocumentType.TEXT_NOTE -> DocTextColor
        DocumentType.GROUP -> MaterialTheme.colorScheme.primary
    }

    val typeIcon: ImageVector = when (doc.type) {
        DocumentType.IMAGE_JPG, DocumentType.IMAGE_PNG, DocumentType.PHOTO_CAMERA -> Icons.Default.Image
        DocumentType.PDF -> Icons.Default.PictureAsPdf
        DocumentType.CONTACT -> Icons.Default.Person
        DocumentType.TEXT_NOTE -> Icons.AutoMirrored.Filled.Note
        DocumentType.GROUP -> Icons.Default.Folder
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        if (!isDarkMode) Color(0xFFE2C9CC) else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    }

    var dragAccumulation by remember { mutableStateOf(0f) }
    var visualOffsetY by remember { mutableStateOf(0f) }
    var isPressed by remember { mutableStateOf(false) }

    val animatedVisualOffset by animateFloatAsState(
        targetValue = if (isPressed) visualOffsetY else 0f,
        animationSpec = if (isPressed) snap() else spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "visualOffset"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, 
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale_anim"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 16.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "elevation_anim"
    )

    val sharedBoundsModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = "doc_bounds_${doc.id}"),
                animatedVisibilityScope = animatedVisibilityScope,
                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
            )
        }
    } else Modifier

    val dragModifier = if (!isMultiSelectMode) {
        Modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { 
                    isPressed = true
                    dragAccumulation = 0f
                    visualOffsetY = 0f
                    onDragStateChange?.invoke(true)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragAccumulation += dragAmount.y
                    visualOffsetY += dragAmount.y
                    onDragScroll?.invoke(dragAmount.y)
                    val threshold = 90f
                    if (dragAccumulation > threshold && onMoveDown != null) {
                        onMoveDown()
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        dragAccumulation -= threshold
                        visualOffsetY -= threshold
                    } else if (dragAccumulation < -threshold && onMoveUp != null) {
                        onMoveUp()
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        dragAccumulation += threshold
                        visualOffsetY += threshold
                    }
                },
                onDragEnd = { 
                    isPressed = false
                    dragAccumulation = 0f 
                    visualOffsetY = 0f
                    onDragStateChange?.invoke(false)
                },
                onDragCancel = { 
                    isPressed = false
                    dragAccumulation = 0f 
                    visualOffsetY = 0f
                    onDragStateChange?.invoke(false)
                }
            )
        }
    } else Modifier

    Card(
        modifier = modifier
            .offset { androidx.compose.ui.unit.IntOffset(0, animatedVisualOffset.toInt()) }
            .zIndex(if (isPressed) 1f else 0f)
            .then(sharedBoundsModifier)
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 3.dp else 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (isMultiSelectMode) {
                        onCardLongClick() // For multi-select, a single click selects/deselects
                    } else {
                        if (doc.type == DocumentType.CONTACT && !doc.contactPhone.isNullOrBlank()) {
                            FileManager.dialContact(context, doc.contactPhone)
                        } else {
                            onCardClick()
                        }
                    }
                },
                onLongClick = {
                    if (!isMultiSelectMode) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCardLongClick() // Enter multi-select mode
                    }
                }
            )
            .testTag("document_card_${doc.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        when (viewMode) {
            ViewMode.BOXES -> {
                // 2-COLUMN GRID BOX VIEW
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .paperBackground(isDarkMode)
                        .padding(10.dp)
                ) {
                    // Header with Type Badge & Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(typeColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = doc.type.label,
                                tint = typeColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (doc.category.isNotEmpty() && !isMultiSelectMode) {
                                Text(
                                    text = doc.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            if (isMultiSelectMode) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = if (isSelected) "Selected" else "Not selected",
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Drag to reorder",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .then(dragModifier)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Thumbnail / Preview Box (shows image or PDF 1st page preview)
                    CardThumbnail(
                        doc = doc,
                        typeColor = typeColor,
                        typeIcon = typeIcon,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(105.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Subtitle / Phone / Detail
                    if (doc.type == DocumentType.CONTACT && !doc.contactPhone.isNullOrBlank()) {
                        Text(
                            text = doc.contactPhone,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
                            maxLines = 1
                        )
                    } else if (doc.details.isNotEmpty()) {
                        Text(
                            text = doc.details,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Date & Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (doc.date.isNotEmpty()) {
                            Text(
                                text = doc.date,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Action Icons: Logos only
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (doc.type == DocumentType.CONTACT && !doc.contactPhone.isNullOrBlank()) {
                                IconButton(
                                    onClick = { FileManager.dialContact(context, doc.contactPhone) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Dial", tint = DocContactColor, modifier = Modifier.size(15.dp))
                                }
                            }
                            IconButton(
                                onClick = { FileManager.shareDocument(context, doc) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                            }
                            IconButton(
                                onClick = { FileManager.exportDocumentToDownloads(context, doc) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = "Download", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(15.dp))
                            }
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }
            }

            ViewMode.HORIZONTAL_CARDS -> {
                // HORIZONTAL CARDS VIEW (Wide banner-style layout with large preview & rich controls)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .paperBackground(isDarkMode)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Large Thumbnail Preview (Image, PDF 1st page, or Note preview)
                    CardThumbnail(
                        doc = doc,
                        typeColor = typeColor,
                        typeIcon = typeIcon,
                        modifier = Modifier
                            .size(width = 110.dp, height = 95.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Content Details & Reorder / Actions
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = doc.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (doc.category.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = doc.category,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        if (doc.type == DocumentType.CONTACT && !doc.contactPhone.isNullOrBlank()) {
                            Text(
                                text = "📞 ${doc.contactPhone}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DocContactColor,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                        } else if (doc.details.isNotEmpty()) {
                            Text(
                                text = doc.details,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (!doc.textContent.isNullOrBlank()) {
                            Text(
                                text = doc.textContent,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Date, Type, and Quick Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = doc.type.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = typeColor
                                )
                                if (doc.date.isNotEmpty()) {
                                    Text(
                                        text = "• ${doc.date}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Reorder Controls (Move Up / Move Down) and Action Logos
                            if (!isMultiSelectMode) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    IconButton(
                                        onClick = { FileManager.shareDocument(context, doc) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { FileManager.exportDocumentToDownloads(context, doc) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.FileDownload, contentDescription = "Download", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = onDeleteClick,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                    }
                                    Icon(
                                        imageVector = Icons.Default.DragHandle,
                                        contentDescription = "Drag to reorder",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(start = 4.dp)
                                            .then(dragModifier)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = if (isSelected) "Selected" else "Not selected",
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            ViewMode.BARS -> {
                // VERTICAL LIST BARS VIEW
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .paperBackground(isDarkMode)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail / Icon box (shows image or PDF 1st page preview)
                    CardThumbnail(
                        doc = doc,
                        typeColor = typeColor,
                        typeIcon = typeIcon,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Content Details
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = doc.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (doc.category.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = doc.category,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (doc.type == DocumentType.CONTACT && !doc.contactPhone.isNullOrBlank()) {
                            Text(
                                text = "📞 ${doc.contactPhone}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DocContactColor,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                        } else if (doc.details.isNotEmpty()) {
                            Text(
                                text = doc.details,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (!doc.textContent.isNullOrBlank()) {
                            Text(
                                text = doc.textContent,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (doc.date.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = doc.date,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = doc.type.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = typeColor
                            )
                        }
                    }

                    // Action Icons (Logos only)
                    if (isMultiSelectMode) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (isSelected) "Selected" else "Not selected",
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(24.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (doc.type == DocumentType.CONTACT && !doc.contactPhone.isNullOrBlank()) {
                                IconButton(
                                    onClick = { FileManager.dialContact(context, doc.contactPhone) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("btn_dial_${doc.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Dial",
                                        tint = DocContactColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Share Logo button
                            IconButton(
                                onClick = { FileManager.shareDocument(context, doc) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_share_${doc.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Download Logo button
                            IconButton(
                                onClick = { FileManager.exportDocumentToDownloads(context, doc) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_download_${doc.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Download",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Delete button
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_delete_${doc.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            // Drag Handle
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Drag to reorder",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(start = 4.dp)
                                    .then(dragModifier)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardThumbnail(
    doc: DocumentItem,
    typeColor: Color,
    typeIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    val isImage = (doc.type == DocumentType.IMAGE_JPG || doc.type == DocumentType.IMAGE_PNG || doc.type == DocumentType.PHOTO_CAMERA) &&
            doc.internalPath.isNotEmpty() && File(doc.internalPath).exists()

    val hasPdfThumbnail = doc.type == DocumentType.PDF &&
            !doc.thumbnailPath.isNullOrEmpty() && File(doc.thumbnailPath).exists()

    Box(
        modifier = modifier
            .background(typeColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        if (isImage) {
            AsyncImage(
                model = File(doc.internalPath),
                contentDescription = doc.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (hasPdfThumbnail) {
            // Render 1st page of PDF as preview image
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = File(doc.thumbnailPath!!),
                    contentDescription = "PDF Preview ${doc.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Small PDF badge overlay
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 6.dp),
                    color = DocPdfColor,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "PDF",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(28.dp)
                )
                if (doc.type == DocumentType.PDF) {
                    Text(
                        text = "PDF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    )
                }
            }
        }
    }
}

