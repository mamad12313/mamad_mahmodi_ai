package com.mohammadpatrick.assistant.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.mohammadpatrick.assistant.MohammadPatrickApp
import com.mohammadpatrick.assistant.domain.model.MemoryItem
import com.mohammadpatrick.assistant.ui.components.AppTopBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun MemoryScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as MohammadPatrickApp
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    val allItems by app.memoryRepository.getAllMemoryItems().collectAsState(initial = emptyList())

    val filteredItems = remember(allItems, searchQuery) {
        if (searchQuery.isBlank()) allItems
        else allItems.filter { it.title.contains(searchQuery, ignoreCase = true) || it.content.contains(searchQuery, ignoreCase = true) }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MemoryItem?>(null) }
    var itemTitle by remember { mutableStateOf("") }
    var itemContent by remember { mutableStateOf("") }
    var itemCategory by remember { mutableStateOf("یادداشت") }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "حافظه و یادداشت‌های محلی",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val json = app.memoryRepository.exportMemoryAsJson()
                            app.systemActionsManager.shareText(json)
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "خروجی گرفتن")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingItem = null
                    itemTitle = ""
                    itemContent = ""
                    itemCategory = "یادداشت"
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "افزودن یادداشت", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            // Privacy Assurance Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF042F2E).copy(alpha = 0.5f))
                    .border(1.dp, Color(0xFF0D9488), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF2DD4BF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "اطلاعات حافظه منحصراً روی پایگاه داده داخلی همین گوشی ذخیره می‌شود و هیچ اطلاعاتی به سرورهای خارجی منتقل نخواهد شد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCCFBF1)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجو در یادداشت‌ها و اولویت‌ها...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Items List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 70.dp)
            ) {
                if (filteredItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "موردی با این عبارت یافت نشد." else "هنوز داده‌ای در حافظه ثبت نشده است.\nبا زدن دکمه + یادداشت یا دستورالعملی برای پاتریک ثبت کنید.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredItems, key = { it.id }) { item ->
                        MemoryItemCard(
                            item = item,
                            onEdit = {
                                editingItem = item
                                itemTitle = item.title
                                itemContent = item.content
                                itemCategory = item.category
                                showAddDialog = true
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    app.memoryRepository.deleteMemoryItem(item.id)
                                    Toast.makeText(context, "یادداشت حذف شد", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onTogglePin = {
                                coroutineScope.launch {
                                    app.memoryRepository.updateMemoryItem(item.copy(isPinned = !item.isPinned))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (editingItem != null) "ویرایش حافظه" else "ثبت اطلاعات در حافظه محلی") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = itemTitle,
                        onValueChange = { itemTitle = it },
                        label = { Text("عنوان (مثلاً اولویت‌های من، یادداشت مهم)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = itemContent,
                        onValueChange = { itemContent = it },
                        label = { Text("متن محتوا") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (itemTitle.isNotBlank() && itemContent.isNotBlank()) {
                            coroutineScope.launch {
                                if (editingItem != null) {
                                    app.memoryRepository.updateMemoryItem(
                                        editingItem!!.copy(
                                            title = itemTitle.trim(),
                                            content = itemContent.trim(),
                                            updatedAt = System.currentTimeMillis()
                                        )
                                    )
                                } else {
                                    val newItem = MemoryItem(
                                        id = UUID.randomUUID().toString(),
                                        title = itemTitle.trim(),
                                        content = itemContent.trim(),
                                        category = itemCategory
                                    )
                                    app.memoryRepository.insertMemoryItem(newItem)
                                }
                            }
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("ذخیره در دیتابیس")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun MemoryItemCard(
    item: MemoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(item.updatedAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "سنجاق",
                        tint = if (item.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "ویرایش", tint = MaterialTheme.colorScheme.outline)
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "آخرین بروزرسانی: $dateString",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
