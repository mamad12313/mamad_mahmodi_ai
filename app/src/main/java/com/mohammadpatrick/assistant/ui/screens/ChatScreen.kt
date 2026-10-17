package com.mohammadpatrick.assistant.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohammadpatrick.assistant.MohammadPatrickApp
import com.mohammadpatrick.assistant.domain.model.ChatMessage
import com.mohammadpatrick.assistant.domain.model.MessageSender
import com.mohammadpatrick.assistant.domain.model.MessageStatus
import com.mohammadpatrick.assistant.domain.repository.AssistantResult
import com.mohammadpatrick.assistant.ui.components.AppTopBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun ChatScreen(
    conversationIdArg: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as MohammadPatrickApp
    val coroutineScope = rememberCoroutineScope()

    var activeConversationId by remember { mutableStateOf(conversationIdArg ?: "") }
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    // Attached files/images
    var attachedImageUri by remember { mutableStateOf<Uri?>(null) }
    var attachedFileUri by remember { mutableStateOf<Uri?>(null) }
    var attachedFileName by remember { mutableStateOf<String?>(null) }

    val conversations by app.chatRepository.getAllConversations().collectAsState(initial = emptyList())
    val messages by app.chatRepository.getMessagesForConversation(activeConversationId).collectAsState(initial = emptyList())
    val settings by app.settingsRepository.settingsFlow.collectAsState(initial = null)

    val listState = rememberLazyListState()

    // File / Image Picker Launchers
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        attachedImageUri = uri
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        attachedFileUri = uri
        attachedFileName = uri?.lastPathSegment ?: "document"
    }

    // Auto-select or create first conversation
    LaunchedEffect(conversations) {
        if (activeConversationId.isBlank() && conversations.isNotEmpty()) {
            activeConversationId = conversations.first().id
        } else if (activeConversationId.isBlank() && conversations.isEmpty()) {
            val newId = app.chatRepository.createConversation("گفتگوی پیش‌فرض")
            activeConversationId = newId
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }

    val isApiConfigured = settings?.apiConfig?.isConfigured == true && settings?.apiConfig?.isEnabled == true

    Scaffold(
        topBar = {
            AppTopBar(
                title = conversations.find { it.id == activeConversationId }?.title ?: "گفتگوی متنی",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val newId = app.chatRepository.createConversation("گفتگوی ${conversations.size + 1}")
                            activeConversationId = newId
                            Toast.makeText(context, "گفتگوی جدید آغاز شد", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "گفتگوی جدید")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "منوی گفتگو")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("تغییر نام گفتگو") },
                            onClick = {
                                showMenu = false
                                renameText = conversations.find { it.id == activeConversationId }?.title ?: ""
                                showRenameDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("پاک‌سازی پیام‌های این گفتگو") },
                            onClick = {
                                showMenu = false
                                coroutineScope.launch {
                                    app.chatRepository.clearAllMessages(activeConversationId)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("حذف این گفتگو") },
                            onClick = {
                                showMenu = false
                                coroutineScope.launch {
                                    app.chatRepository.deleteConversation(activeConversationId)
                                    activeConversationId = ""
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // API Unconfigured Warning Banner (Requirement 4 & 5)
            if (!isApiConfigured) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF451A03))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .clickable { onNavigateToSettings() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "برای دریافت پاسخ هوشمند، ابتدا API خود را متصل کنید. (لمس جهت تنظیم)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFEF3C7),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هیچ پیامی در این گفتگو وجود ندارد.\nپیام خود را بنویسید یا دکمه صوتی را لمس کنید.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(messages, key = { it.id }) { message ->
                        ChatMessageItem(
                            message = message,
                            onCopy = {
                                app.systemActionsManager.copyToClipboard(message.text)
                                Toast.makeText(context, "متن پیام کپی شد", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    app.chatRepository.deleteMessage(message.id)
                                }
                            }
                        )
                    }
                }
            }

            // Attached preview preview row
            if (attachedImageUri != null || attachedFileUri != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (attachedImageUri != null) Icons.Default.Image else Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = attachedFileName ?: "تصویر ضمیمه شده",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        attachedImageUri = null
                        attachedFileUri = null
                        attachedFileName = null
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "حذف ضمیمه")
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = "انتخاب عکس", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(onClick = { documentPickerLauncher.launch("*/*") }) {
                    Icon(imageVector = Icons.Default.AttachFile, contentDescription = "انتخاب فایل", tint = MaterialTheme.colorScheme.primary)
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("پیام خود را بنویسید...") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    maxLines = 4
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isSending) {
                            val userText = inputText.trim()
                            inputText = ""
                            isSending = true

                            coroutineScope.launch {
                                val userMsg = ChatMessage(
                                    id = UUID.randomUUID().toString(),
                                    conversationId = activeConversationId,
                                    text = userText,
                                    sender = MessageSender.USER,
                                    timestamp = System.currentTimeMillis(),
                                    attachedImagePath = attachedImageUri?.toString(),
                                    attachedFilePath = attachedFileUri?.toString(),
                                    attachedFileName = attachedFileName
                                )
                                app.chatRepository.saveMessage(userMsg)

                                // Clear attachment state
                                attachedImageUri = null
                                attachedFileUri = null
                                attachedFileName = null

                                // Send to AssistantRepository (Clean Architecture)
                                val result = app.assistantRepository.sendMessage(
                                    conversationId = activeConversationId,
                                    userPrompt = userText
                                )

                                val replyMsg = when (result) {
                                    is AssistantResult.Success -> ChatMessage(
                                        id = UUID.randomUUID().toString(),
                                        conversationId = activeConversationId,
                                        text = result.replyText,
                                        sender = MessageSender.ASSISTANT,
                                        timestamp = System.currentTimeMillis(),
                                        status = MessageStatus.SENT
                                    )
                                    is AssistantResult.ApiNotConfigured -> ChatMessage(
                                        id = UUID.randomUUID().toString(),
                                        conversationId = activeConversationId,
                                        text = result.message,
                                        sender = MessageSender.SYSTEM,
                                        timestamp = System.currentTimeMillis(),
                                        status = MessageStatus.PENDING_API
                                    )
                                    is AssistantResult.Error -> ChatMessage(
                                        id = UUID.randomUUID().toString(),
                                        conversationId = activeConversationId,
                                        text = result.errorMessage,
                                        sender = MessageSender.SYSTEM,
                                        timestamp = System.currentTimeMillis(),
                                        status = MessageStatus.FAILED
                                    )
                                    else -> null
                                }

                                if (replyMsg != null) {
                                    app.chatRepository.saveMessage(replyMsg)
                                }

                                isSending = false
                            }
                        }
                    },
                    enabled = inputText.isNotBlank() && !isSending
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "ارسال پیام",
                            tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("تغییر عنوان گفتگو") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("نام جدید گفتگو") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotBlank()) {
                        coroutineScope.launch {
                            app.chatRepository.renameConversation(activeConversationId, renameText.trim())
                        }
                    }
                    showRenameDialog = false
                }) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val isSystem = message.sender == MessageSender.SYSTEM

    val bubbleColor = when {
        isUser -> MaterialTheme.colorScheme.primary
        isSystem -> Color(0xFF7F1D1D).copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isUser -> MaterialTheme.colorScheme.onPrimary
        isSystem -> Color(0xFFFCA5A5)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                if (message.attachedFileName != null) {
                    Text(
                        text = "📎 ${message.attachedFileName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "کپی",
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onCopy() }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onDelete() }
                    )
                }
            }
        }
    }
}
