package com.kalenyator.app.ui.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalenyator.app.R
import com.kalenyator.app.ui.LocalViewModelFactory
import com.kalenyator.app.ui.components.CalcButton
import com.kalenyator.app.ui.theme.TentakoBlush
import com.kalenyator.app.ui.theme.TentakoBlushLight
import com.kalenyator.app.ui.theme.TentakoPink
import com.kalenyator.app.ui.theme.TentakoPinkDark

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel(factory = LocalViewModelFactory.current)) {
    val uiState by viewModel.state.collectAsState()
    val engine = uiState.engine
    val context = LocalContext.current
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "displayScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.calc_title),
            style = MaterialTheme.typography.headlineMedium,
            color = TentakoPink
        )
        Text(
            text = stringResource(R.string.calc_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TentakoPinkDark
        )
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(TentakoBlush, TentakoBlushLight)),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .scale(scale),
            verticalArrangement = Arrangement.Bottom
        ) {
            if (uiState.savedHistory.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.calc_history_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = TentakoPinkDark.copy(alpha = 0.8f)
                    )
                    TextButton(onClick = viewModel::clearHistory) {
                        Text(
                            stringResource(R.string.calc_history_clear),
                            color = TentakoPink,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Column(Modifier.fillMaxWidth().heightIn(max = 96.dp)) {
                    uiState.savedHistory.take(4).asReversed().forEach { line ->
                        Text(
                            line,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(onClick = { viewModel.useHistoryEntry(line) })
                                .padding(vertical = 2.dp),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.bodySmall,
                            color = TentakoPinkDark.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                HorizontalDivider(
                    Modifier.padding(vertical = 6.dp),
                    color = TentakoPink.copy(alpha = 0.25f)
                )
            }

            if (engine.history.isNotEmpty()) {
                Text(
                    text = engine.history,
                    style = MaterialTheme.typography.titleMedium,
                    color = TentakoPinkDark.copy(alpha = 0.75f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
            Text(
                text = if (engine.error) stringResource(R.string.calc_error) else engine.display,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp),
                color = TentakoPinkDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("calc", engine.display))
                            Toast.makeText(context, R.string.calc_copied, Toast.LENGTH_SHORT).show()
                        }
                    ),
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(8.dp))

        @Composable
        fun CalcRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
            Spacer(Modifier.height(6.dp))
        }

        CalcRow {
            CalcButton("C", secondary = true) { viewModel.onClear() }
            CalcButton("⌫", secondary = true) { viewModel.onDelete() }
            CalcButton("%") { viewModel.onOperator('%') }
            CalcButton("÷", accent = true) { viewModel.onOperator('/') }
        }
        CalcRow {
            CalcButton("7") { viewModel.onNumber("7") }
            CalcButton("8") { viewModel.onNumber("8") }
            CalcButton("9") { viewModel.onNumber("9") }
            CalcButton("×", accent = true) { viewModel.onOperator('*') }
        }
        CalcRow {
            CalcButton("4") { viewModel.onNumber("4") }
            CalcButton("5") { viewModel.onNumber("5") }
            CalcButton("6") { viewModel.onNumber("6") }
            CalcButton("−", accent = true) { viewModel.onOperator('-') }
        }
        CalcRow {
            CalcButton("1") { viewModel.onNumber("1") }
            CalcButton("2") { viewModel.onNumber("2") }
            CalcButton("3") { viewModel.onNumber("3") }
            CalcButton("+", accent = true) { viewModel.onOperator('+') }
        }
        CalcRow {
            CalcButton("0", wide = true) { viewModel.onNumber("0") }
            CalcButton(".") { viewModel.onNumber(".") }
            CalcButton("=", accent = true) { viewModel.onEquals() }
        }
    }
}
