package com.snapnote.export

import android.content.Context
import android.os.Environment
import com.snapnote.data.local.AppDatabase
import com.snapnote.data.model.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MarkdownExporter(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)

    suspend fun exportAllNotes(): List<File> = withContext(Dispatchers.IO) {
        val notes = database.noteDao().getAllNotes().let { flow ->
            var list: List<com.snapnote.data.local.entity.NoteEntity> = emptyList()
            flow.collect { list = it }
            list
        }

        val exportDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "SnapNoteExports"
        ).apply { mkdirs() }

        val exportedFiles = mutableListOf<File>()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        // Export individual notes
        for (note in notes) {
            val kps = database.knowledgePointDao().getKnowledgePointsByNote(note.id).let { flow ->
                var list: List<com.snapnote.data.local.entity.KnowledgePointEntity> = emptyList()
                flow.collect { list = it }
                list
            }

            val markdown = buildMarkdownForNote(note, kps)
            val file = File(exportDir, "${sanitizeFileName(note.title)}_$timestamp.md")
            file.writeText(markdown)
            exportedFiles.add(file)
        }

        // Export combined index
        val indexFile = File(exportDir, "SnapNote_Index_$timestamp.md")
        indexFile.writeText(buildIndexMarkdown(notes))
        exportedFiles.add(indexFile)

        exportedFiles
    }

    private fun buildMarkdownForNote(
        note: com.snapnote.data.local.entity.NoteEntity,
        knowledgePoints: List<com.snapnote.data.local.entity.KnowledgePointEntity>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("# ${note.title}")
        sb.appendLine()
        sb.appendLine("> 分类: ${note.category.ifBlank { "未分类" }} | 创建时间: ${formatDate(note.createdAt)} | 更新时间: ${formatDate(note.updatedAt)}")
        sb.appendLine()

        if (knowledgePoints.isEmpty()) {
            sb.appendLine("*暂无知识点*")
        } else {
            // Group by content type
            val grouped = knowledgePoints.groupBy { it.contentType }

            grouped.forEach { (type, points) ->
                val typeLabel = ContentType.fromString(type).label
                sb.appendLine("## $typeLabel")
                sb.appendLine()

                points.forEach { point ->
                    sb.appendLine("### ${point.number} ${point.title}")
                    sb.appendLine()
                    sb.appendLine(point.content)
                    sb.appendLine()
                    if (point.parentId != null) {
                        sb.appendLine("> 上级知识点编号: ${point.parentId}")
                    }
                    sb.appendLine("---")
                    sb.appendLine()
                }
            }
        }

        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine("*由 SnapNote 自动生成*")

        return sb.toString()
    }

    private fun buildIndexMarkdown(notes: List<com.snapnote.data.local.entity.NoteEntity>): String {
        val sb = StringBuilder()
        sb.appendLine("# SnapNote 笔记索引")
        sb.appendLine()
        sb.appendLine("> 导出时间: ${formatDate(System.currentTimeMillis())}")
        sb.appendLine()
        sb.appendLine("| 序号 | 标题 | 分类 | 创建时间 |")
        sb.appendLine("|------|------|------|----------|")

        notes.forEachIndexed { index, note ->
            sb.appendLine("| ${index + 1} | ${note.title} | ${note.category.ifBlank { "-" }} | ${formatDate(note.createdAt)} |")
        }

        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine("*由 SnapNote 自动生成*")

        return sb.toString()
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9\\u4e00-\\u9fa5]"), "_").take(50)
    }
}
