package com.snapnote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.snapnote.data.model.ContentType
import com.snapnote.ui.theme.ConceptColor
import com.snapnote.ui.theme.ExampleColor
import com.snapnote.ui.theme.FormulaColor
import com.snapnote.ui.theme.MistakeColor
import com.snapnote.ui.theme.Primary
import com.snapnote.ui.theme.TheoryColor

@Composable
fun KnowledgeAvatar(
    contentType: ContentType,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    val color = when (contentType) {
        ContentType.THEORY -> TheoryColor
        ContentType.EXAMPLE -> ExampleColor
        ContentType.MISTAKE -> MistakeColor
        ContentType.FORMULA -> FormulaColor
        ContentType.CONCEPT -> ConceptColor
        ContentType.UNKNOWN -> Primary
    }

    val icon = when (contentType) {
        ContentType.THEORY -> "知"
        ContentType.EXAMPLE -> "例"
        ContentType.MISTAKE -> "错"
        ContentType.FORMULA -> "公"
        ContentType.CONCEPT -> "概"
        ContentType.UNKNOWN -> "?"
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

@Composable
fun RelationAvatar(
    relationType: com.snapnote.data.model.RelationType,
    modifier: Modifier = Modifier,
    size: Int = 32
) {
    val color = when (relationType) {
        com.snapnote.data.model.RelationType.SUBORDINATE -> com.snapnote.ui.theme.SubordinateColor
        com.snapnote.data.model.RelationType.PREREQUISITE -> com.snapnote.ui.theme.PrerequisiteColor
        com.snapnote.data.model.RelationType.CONFUSION -> com.snapnote.ui.theme.ConfusionColor
        com.snapnote.data.model.RelationType.CAUSAL -> com.snapnote.ui.theme.CausalColor
        else -> Primary
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "●",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
