package com.example.ui.screens

import android.provider.OpenableColumns
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DirectChatConversation
import com.example.data.model.DirectChatMessage
import com.example.ui.components.ArtmuseumArtworkImage
import com.example.ui.components.ArtmuseumAvatar
import com.example.ui.components.ArtmuseumIconButton
import com.example.ui.theme.ArtmuseumShapeTokens
import com.example.ui.theme.HeartCrimson
import com.example.ui.theme.LocalArtmuseumColors
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.WarmPaper
import com.example.ui.viewmodel.ArtmuseumViewModel
import kotlinx.coroutines.launch

@Composable
fun DirectMessagesListScreen(
    conversations: List<DirectChatConversation>,
    onOpenConversation: (DirectChatConversation) -> Unit,
    onStartNewChat: (peerUid: String, peerName: String, peerPhoto: String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val colors = LocalArtmuseumColors.current
    var query by remember { mutableStateOf("") }

    val quickPeers = remember {
        listOf(
            Triple("user_arjun", "Arjun", "preset:avatar_daniel"),
            Triple("user_elena", "Elena Vance", "preset:portrait_study"),
            Triple("user_marcus", "Marcus Thorne", "preset:coastal_villa"),
            Triple("user_sophia", "Sophia", "preset:avatar_sophia")
        )
    }

    val filteredConversations = remember(conversations, query) {
        if (query.isBlank()) conversations
        else conversations.filter {
            it.peerName.contains(query, ignoreCase = true) ||
            it.lastMessage.contains(query, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top App Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("dm_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.inkPrimary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Direct Messages",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = colors.inkPrimary
                    )
                    Text(
                        text = "Real conversations with fellow curators",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = colors.mutedText
                    )
                }
            }

            // Search Bar
            Surface(
                shape = ArtmuseumShapeTokens.Pill,
                color = colors.cardSurface,
                border = BorderStroke(1.dp, colors.border.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
                    .height(46.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search messages",
                        tint = colors.mutedText,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search curators or messages...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                color = colors.mutedText
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.inkPrimary,
                                fontSize = 13.5.sp
                            ),
                            cursorBrush = SolidColor(colors.pillSelectedFill),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dm_search_input")
                        )
                    }
                }
            }

            // Quick Connect Members Horizontal Rail
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "COMMUNITY CURATORS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    ),
                    color = colors.mutedText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickPeers) { (uid, name, photo) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onStartNewChat(uid, name, photo) }
                                .padding(4.dp)
                                .testTag("quick_peer_$uid")
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                ArtmuseumAvatar(
                                    photoURL = photo,
                                    contentDescription = name,
                                    size = 48.dp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = name.split(" ").first(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                ),
                                color = colors.inkPrimary
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = colors.border.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
            )

            // Conversation List
            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = colors.mutedText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (query.isNotBlank()) "No conversations match '$query'" else "No messages yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.inkPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap on any community curator above to start a direct reflection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.mutedText
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("conversations_list")
                ) {
                    items(filteredConversations, key = { it.chatId }) { conv ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = colors.cardSurface,
                            border = BorderStroke(0.6.dp, colors.border.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onOpenConversation(conv) }
                                .testTag("conversation_item_${conv.chatId}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    ArtmuseumAvatar(
                                        photoURL = conv.peerPhotoURL,
                                        contentDescription = conv.peerName,
                                        size = 46.dp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(11.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50))
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = conv.peerName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = colors.inkPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = conv.lastMessageTimeFormatted,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = colors.mutedText
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = conv.lastMessage,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                fontWeight = if (conv.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
                                            ),
                                            color = if (conv.unreadCount > 0) colors.inkPrimary else colors.mutedText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (conv.unreadCount > 0) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(HeartCrimson)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatConversationScreen(
    peerUid: String,
    peerName: String,
    peerPhoto: String,
    messages: List<DirectChatMessage>,
    onSendMessage: (text: String, attachmentUrl: String, attachmentType: String, fileName: String, fileSize: String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val colors = LocalArtmuseumColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var pendingAttachmentUri by remember { mutableStateOf<String?>(null) }
    var pendingAttachmentType by remember { mutableStateOf("") }
    var pendingFileName by remember { mutableStateOf("") }
    var pendingFileSize by remember { mutableStateOf("") }
    var isProcessingAttachment by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isProcessingAttachment = true
                val dataUri = ArtmuseumViewModel.encodeUriToCompactDataUri(context, uri)
                isProcessingAttachment = false
                if (dataUri != null) {
                    pendingAttachmentUri = dataUri
                    pendingAttachmentType = "image"
                    pendingFileName = "Photo_${System.currentTimeMillis() % 10000}.jpg"
                    pendingFileSize = "${(dataUri.length * 3 / 4) / 1024} KB"
                }
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isProcessingAttachment = true
                var name = "Document.pdf"
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (cursor.moveToFirst() && nameIdx != -1) {
                            name = cursor.getString(nameIdx)
                        }
                    }
                } catch (_: Throwable) {}
                val (dataUri, sizeStr) = try {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null) {
                        val base64 = Base64.encodeToString(bytes.take(250_000).toByteArray(), Base64.NO_WRAP)
                        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                        Pair("data:$mime;base64,$base64", "${bytes.size / 1024} KB")
                    } else Pair(null, "")
                } catch (_: Throwable) {
                    Pair(null, "")
                }
                isProcessingAttachment = false
                if (dataUri != null) {
                    pendingAttachmentUri = dataUri
                    pendingAttachmentType = "file"
                    pendingFileName = name
                    pendingFileSize = sizeStr
                }
            }
        }
    }

    val quickPrompts = remember {
        listOf(
            "Loved your exhibit! 🎨",
            "What painting techniques do you use?",
            "Inspiring thoughts on peace ✨",
            "Which gallery inspires you most?"
        )
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Surface(
                color = colors.cardSurface,
                border = BorderStroke(0.6.dp, colors.border.copy(alpha = 0.6f)),
                shadowElevation = 2.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.inkPrimary
                        )
                    }

                    Box(contentAlignment = Alignment.BottomEnd) {
                        ArtmuseumAvatar(
                            photoURL = peerPhoto,
                            contentDescription = peerName,
                            size = 38.dp
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = peerName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = colors.inkPrimary
                        )
                        Text(
                            text = "Active in Museum",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Message Stream
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("chat_messages_stream")
            ) {
                items(messages, key = { it.messageId }) { msg ->
                    val isMe = msg.isFromMe
                    Column(
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = if (isMe) {
                                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
                            } else {
                                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
                            },
                            color = if (isMe) colors.pillSelectedFill else colors.cardSurface,
                            border = if (isMe) null else BorderStroke(0.8.dp, colors.border.copy(alpha = 0.7f)),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .testTag("chat_bubble_${msg.messageId}")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                if (msg.attachmentType == "image" && msg.attachmentUrl.isNotBlank()) {
                                    ArtmuseumArtworkImage(
                                        imageSource = msg.attachmentUrl,
                                        contentDescription = msg.fileName.ifBlank { "Shared photo" },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(170.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (msg.text.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                } else if (msg.attachmentType == "file") {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isMe) Color.White.copy(alpha = 0.18f) else colors.subtleSurface,
                                        border = BorderStroke(0.6.dp, if (isMe) Color.White.copy(alpha = 0.3f) else colors.border),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Description,
                                                contentDescription = "File attachment",
                                                tint = if (isMe) WarmPaper else colors.inkPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = msg.fileName.ifBlank { "Attachment" },
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isMe) WarmPaper else colors.inkPrimary
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (msg.fileSize.isNotBlank()) {
                                                    Text(
                                                        text = msg.fileSize,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.sp,
                                                            color = if (isMe) WarmPaper.copy(alpha = 0.8f) else colors.mutedText
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (msg.text.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }

                                if (msg.text.isNotBlank()) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp,
                                            color = if (isMe) WarmPaper else colors.inkPrimary
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = msg.formattedTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                            color = colors.mutedText,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            // Quick Prompt Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickPrompts) { prompt ->
                    Surface(
                        shape = ArtmuseumShapeTokens.Pill,
                        color = colors.subtleSurface,
                        border = BorderStroke(0.6.dp, colors.border),
                        modifier = Modifier
                            .clip(ArtmuseumShapeTokens.Pill)
                            .clickable {
                                onSendMessage(prompt, "", "", "", "")
                            }
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                            color = colors.inkPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Attachment pending preview
            if (isProcessingAttachment) {
                Surface(
                    color = colors.cardSurface,
                    border = BorderStroke(0.6.dp, colors.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = colors.pillSelectedFill
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Attaching media...",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.mutedText
                        )
                    }
                }
            } else if (pendingAttachmentUri != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.cardSurface,
                    border = BorderStroke(0.8.dp, colors.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        if (pendingAttachmentType == "image") {
                            ArtmuseumArtworkImage(
                                imageSource = pendingAttachmentUri ?: "",
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.InsertDriveFile,
                                contentDescription = null,
                                tint = colors.pillSelectedFill,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pendingFileName.ifBlank { "Attached file" },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.inkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${pendingAttachmentType.replaceFirstChar { it.uppercase() }} · $pendingFileSize",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = colors.mutedText
                            )
                        }
                        IconButton(
                            onClick = {
                                pendingAttachmentUri = null
                                pendingAttachmentType = ""
                                pendingFileName = ""
                                pendingFileSize = ""
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Remove attachment",
                                tint = colors.mutedText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                color = colors.cardSurface,
                border = BorderStroke(0.6.dp, colors.border.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    // Attachment Action Icon Button with Dropdown
                    Box {
                        IconButton(
                            onClick = { showAttachmentMenu = true },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("chat_attachment_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AttachFile,
                                contentDescription = "Attach photo or file",
                                tint = if (pendingAttachmentUri != null) colors.pillSelectedFill else colors.inkPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showAttachmentMenu,
                            onDismissRequest = { showAttachmentMenu = false },
                            modifier = Modifier.background(colors.cardSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Photo from Gallery") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Image,
                                        contentDescription = null,
                                        tint = colors.inkPrimary
                                    )
                                },
                                onClick = {
                                    showAttachmentMenu = false
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Document / File") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Description,
                                        contentDescription = null,
                                        tint = colors.inkPrimary
                                    )
                                },
                                onClick = {
                                    showAttachmentMenu = false
                                    filePickerLauncher.launch("*/*")
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = ArtmuseumShapeTokens.Pill,
                        color = colors.subtleSurface,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (inputText.isEmpty() && pendingAttachmentUri == null) {
                                    Text(
                                        text = "Write a thoughtful message...",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                                        color = colors.mutedText
                                    )
                                }
                                BasicTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = colors.inkPrimary,
                                        fontSize = 13.5.sp
                                    ),
                                    cursorBrush = SolidColor(colors.pillSelectedFill),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("chat_input_field")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val canSend = inputText.isNotBlank() || pendingAttachmentUri != null
                    Surface(
                        shape = CircleShape,
                        color = if (canSend) colors.pillSelectedFill else colors.border,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(enabled = canSend) {
                                val toSend = inputText.trim()
                                val uriToSend = pendingAttachmentUri ?: ""
                                val typeToSend = pendingAttachmentType
                                val nameToSend = pendingFileName
                                val sizeToSend = pendingFileSize
                                inputText = ""
                                pendingAttachmentUri = null
                                pendingAttachmentType = ""
                                pendingFileName = ""
                                pendingFileSize = ""
                                onSendMessage(toSend, uriToSend, typeToSend, nameToSend, sizeToSend)
                            }
                            .testTag("chat_send_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send message",
                                tint = WarmPaper,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
