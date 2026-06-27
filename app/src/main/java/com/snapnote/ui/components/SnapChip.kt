package com.snapnote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.snapnote.data.model.ContentType
import com.snapnote.data.model.RelationType
import com.snapnote.ui.theme.ConceptColor
import com.snapnote.ui.theme.ExampleColor
import com.snapnote.ui.theme.FormulaColor
import com.snapnote.ui.theme.MistakeColor
import com.snapnote.ui.theme.Primary
import com.snapnote.ui.theme.TheoryColor

@Composable
fun ContentTypeChip(contentType: ContentType) {
    val (color, bgColor) = when (contentType) {
        ContentType.THEORY -> TheoryColor to TheoryColor.copy(alpha = 0.1f)
        ContentType.EXAMPLE -> ExampleColor to ExampleColor.copy(alpha = 0.1f)
        ContentType.MISTAKE -> MistakeColor to MistakeColor.copy(alpha = 0.1f)
        ContentType.FORMULA -> FormulaColor to FormulaColor.copy(alpha = 0.1f)
        ContentType.CONCEPT -> ConceptColor to ConceptColor.copy(alpha = 0.1f)
        ContentType.UNKNOWN -> Primary to Primary.copy(alpha = 0.1f)
    }

    Chip(text = contentType.label, color = color, backgroundColor = bgColor)
}

@Composable
fun RelationTypeChip(relationType: RelationType) {
    val (color, bgColor) = when (relationType) {
        RelationType.SUBORDINATE -> com.snapnote.ui.theme.SubordinateColor to com.snapnote.ui.theme.SubordinateColor.copy(alpha = 0.1f)
        RelationType.PREREQUISITE -> com.snapnote.ui.theme.PrerequisiteColor to com.snapnote.ui.theme.PrerequisiteColor.copy(alpha = 0.1f)
        RelationType.CONFUSION -> com.snapnote.ui.theme.ConfusionColor to com.snapnote.ui.theme.ConfusionColor.copy(alpha = 0.1f)
        RelationType.CAUSAL -> com.snapnote.ui.theme.CausalColor to com.snapnote.ui.theme.CausalColor.copy(alpha = 0.1f)
        else -> Primary to Primary.copy(alpha = 0.1f)
    }

    Chip(text = relationType.label, color = color, backgroundColor = bgColor)
}

@Composable
fun Chip(
    text: String,
    color: Color = Primary,
    backgroundColor: Color = Primary.copy(alpha = 0.1f)
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}
