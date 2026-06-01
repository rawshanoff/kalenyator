package com.kalenyator.app.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kalenyator.app.ui.theme.TentakoGold
import com.kalenyator.app.ui.theme.TentakoPink
import com.kalenyator.app.ui.theme.TentakoPinkDark

@Composable
fun RowScope.CalcButton(
    label: String,
    modifier: Modifier = Modifier,
    wide: Boolean = false,
    small: Boolean = false,
    secondary: Boolean = false,
    accent: Boolean = false,
    onClick: () -> Unit
) {
    val weight = if (wide) 2f else 1f
    Button(
        onClick = onClick,
        modifier = modifier
            .weight(weight)
            .then(
                if (small) Modifier.defaultMinSize(minHeight = 44.dp)
                else Modifier.aspectRatio(if (wide) 2.1f else 1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                accent -> TentakoPink
                secondary -> MaterialTheme.colorScheme.surfaceVariant
                else -> TentakoGold.copy(alpha = 0.85f)
            },
            contentColor = when {
                accent -> MaterialTheme.colorScheme.onPrimary
                secondary -> TentakoPinkDark
                else -> TentakoPinkDark
            }
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = if (small) 13.sp else 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
