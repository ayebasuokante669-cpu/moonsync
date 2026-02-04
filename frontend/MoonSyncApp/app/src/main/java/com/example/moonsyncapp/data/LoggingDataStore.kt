package com.example.moonsyncapp.data

import com.example.moonsyncapp.data.model.QuickTapCategory
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.moonsyncapp.data.model.DailyLog
import com.example.moonsyncapp.data.model.LogAttachment
import com.example.moonsyncapp.data.model.QuickTapItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.Instant

private val Context.loggingDataStore by preferencesDataStore(name = "logging_data")

class LoggingDataStore(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    companion object {
        private const val LOGS_PREFIX = "log_"
        private const val CUSTOM_ITEMS = "custom_items"
    }

    // Save a log entry
    suspend fun saveLog(log: DailyLog) {
        val key = stringPreferencesKey("$LOGS_PREFIX${log.id}")
        context.loggingDataStore.edit { prefs ->
            prefs[key] = json.encodeToString(log.toSerializable())
        }
    }

    // Get all logs
    fun getAllLogs(): Flow<List<DailyLog>> = context.loggingDataStore.data
        .map { prefs ->
            prefs.asMap()
                .filterKeys { it.name.startsWith(LOGS_PREFIX) }
                .values
                .mapNotNull { value ->
                    try {
                        val serializable = json.decodeFromString<SerializableDailyLog>(value as String)
                        serializable.toDailyLog()
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedByDescending { it.date }
        }

    // Delete a log
    suspend fun deleteLog(logId: String) {
        val key = stringPreferencesKey("$LOGS_PREFIX$logId")
        context.loggingDataStore.edit { prefs ->
            prefs.remove(key)
        }
    }

    // Update a log
    suspend fun updateLog(log: DailyLog) {
        saveLog(log.copy(isEdited = true))
    }

    // Get custom items
    fun getCustomItems(): Flow<List<QuickTapItem>> = context.loggingDataStore.data
        .map { prefs ->
            val itemsJson = prefs[stringPreferencesKey(CUSTOM_ITEMS)] ?: "[]"
            try {
                json.decodeFromString<List<SerializableQuickTapItem>>(itemsJson)
                    .map { it.toQuickTapItem() }
            } catch (e: Exception) {
                emptyList()
            }
        }

    // Save custom items
    suspend fun saveCustomItems(items: List<QuickTapItem>) {
        context.loggingDataStore.edit { prefs ->
            prefs[stringPreferencesKey(CUSTOM_ITEMS)] = json.encodeToString(
                items.map { it.toSerializable() }
            )
        }
    }
}

// Serializable versions for DataStore
@Serializable
data class SerializableDailyLog(
    val id: String,
    val date: String,
    val selectedItems: List<SerializableQuickTapItem>,
    val freeFormText: String,
    val attachments: List<String>, // Simplified for now
    val isEdited: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDailyLog() = DailyLog(
        id = id,
        date = LocalDate.parse(date),
        selectedItems = selectedItems.map { it.toQuickTapItem() },
        freeFormText = freeFormText,
        attachments = emptyList(),
        isEdited = isEdited,
        createdAt = Instant.ofEpochMilli(createdAt),   // FIX: Long → Instant
        updatedAt = Instant.ofEpochMilli(updatedAt)    // FIX: Long → Instant
    )
}

@Serializable
data class SerializableQuickTapItem(
    val id: String,
    val label: String,
    val emoji: String,
    val category: String,
    val isCustom: Boolean
) {
    fun toQuickTapItem() = QuickTapItem(
        id = id,
        label = label,
        emoji = emoji,
        category = QuickTapCategory.valueOf(category),
        isCustom = isCustom
    )
}

// Extension functions
fun DailyLog.toSerializable() = SerializableDailyLog(
    id = id,
    date = date.toString(),
    selectedItems = selectedItems.map { it.toSerializable() },
    freeFormText = freeFormText,
    attachments = emptyList(),
    isEdited = isEdited,
    createdAt = createdAt.toEpochMilli(),   // FIX: Instant → Long
    updatedAt = updatedAt.toEpochMilli()    // FIX: Instant → Long
)

fun QuickTapItem.toSerializable() = SerializableQuickTapItem(
    id = id,
    label = label,
    emoji = emoji,
    category = category.name,
    isCustom = isCustom
)