package com.kalenyator.app.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalenyator.app.data.settings.SettingsRepository
import com.kalenyator.app.domain.CalculatorEngine
import com.kalenyator.app.domain.CalculatorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val engine: CalculatorState = CalculatorState(),
    val savedHistory: List<String> = emptyList()
)

class CalculatorViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val engine = CalculatorEngine()
    private val engineState = MutableStateFlow(engine.state())

    val state: StateFlow<CalculatorUiState> = combine(
        engineState,
        settingsRepository.calculatorHistory
    ) { engine, saved ->
        CalculatorUiState(engine = engine, savedHistory = saved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalculatorUiState())

    private fun refresh() {
        engineState.value = engine.state()
    }

    fun onNumber(num: String) {
        engine.appendNumber(num)
        refresh()
    }

    fun onOperator(op: Char) {
        engine.appendOperator(op)
        refresh()
    }

    fun onEquals() {
        val result = engine.calculate()
        result.historyEntry?.let { entry ->
            viewModelScope.launch { settingsRepository.addCalculatorHistoryEntry(entry) }
        }
        refresh()
    }

    fun onClear() {
        engine.clear()
        refresh()
    }

    fun onDelete() {
        engine.deleteLast()
        refresh()
    }

    fun onFunction(func: String) {
        engine.applyFunction(func)
        refresh()
    }

    fun onConstant(c: String) {
        engine.insertConstant(c)
        refresh()
    }

    fun clearHistory() {
        viewModelScope.launch { settingsRepository.clearCalculatorHistory() }
    }

    fun useHistoryEntry(entry: String) {
        engine.resultFromHistoryEntry(entry)?.let { engine.setDisplay(it) }
        refresh()
    }
}
