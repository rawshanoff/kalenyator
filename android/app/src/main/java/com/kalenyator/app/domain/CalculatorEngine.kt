package com.kalenyator.app.domain

import kotlin.math.abs
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

data class CalculatorState(
    val display: String = "0",
    val history: String = "",
    val error: Boolean = false
)

data class CalculateResult(
    val state: CalculatorState,
    val historyEntry: String? = null
)

class CalculatorEngine {
    private var currentValue = "0"
    private var previousValue = ""
    private var operation: Char? = null
    private var shouldResetDisplay = false

    fun state(): CalculatorState {
        val historyText = if (previousValue.isNotEmpty() && operation != null) {
            val symbol = operatorSymbol(operation!!)
            "$previousValue $symbol"
        } else ""
        return CalculatorState(
            display = currentValue,
            history = historyText,
            error = currentValue == "ERROR"
        )
    }

    fun appendNumber(num: String): CalculatorState {
        if (currentValue == "ERROR") clear()
        if (shouldResetDisplay) {
            currentValue = if (num == ".") "0." else num
            shouldResetDisplay = false
        } else {
            when {
                num == "." && currentValue.contains('.') -> return state()
                currentValue == "0" && num != "." -> currentValue = num
                else -> currentValue += num
            }
        }
        return state()
    }

    fun appendOperator(op: Char): CalculatorState {
        if (currentValue == "ERROR") return state()
        if (operation != null && !shouldResetDisplay) calculate().state
        previousValue = currentValue
        operation = op
        shouldResetDisplay = true
        return state()
    }

    fun calculate(): CalculateResult {
        if (operation == null || shouldResetDisplay || currentValue == "ERROR") {
            return CalculateResult(state())
        }
        val prev = previousValue
        val cur = currentValue
        val prevNum = prev.toDoubleOrNull() ?: return CalculateResult(state())
        val curNum = cur.toDoubleOrNull() ?: return CalculateResult(state())
        val result = when (operation) {
            '+' -> prevNum + curNum
            '-' -> prevNum - curNum
            '*' -> prevNum * curNum
            '/' -> if (curNum == 0.0) null else prevNum / curNum
            '%' -> prevNum % curNum
            '^' -> prevNum.pow(curNum)
            else -> return CalculateResult(state())
        }
        val entry = if (result != null) {
            val symbol = operatorSymbol(operation!!)
            "$prev $symbol $cur = ${formatNumber(result)}"
        } else null
        currentValue = if (result == null) "ERROR" else formatNumber(result)
        operation = null
        previousValue = ""
        shouldResetDisplay = true
        return CalculateResult(state(), entry)
    }

    fun setDisplay(value: String) {
        currentValue = value
        previousValue = ""
        operation = null
        shouldResetDisplay = true
    }

    fun resultFromHistoryEntry(entry: String): String? =
        entry.substringAfterLast('=', "").trim().takeIf { it.isNotEmpty() }

    fun clear(): CalculatorState {
        currentValue = "0"
        previousValue = ""
        operation = null
        shouldResetDisplay = false
        return state()
    }

    fun deleteLast(): CalculatorState {
        if (currentValue == "ERROR") return clear()
        currentValue = if (currentValue.length > 1) currentValue.dropLast(1) else "0"
        return state()
    }

    fun applyFunction(func: String): CalculatorState {
        if (currentValue == "ERROR") return state()
        val num = currentValue.toDoubleOrNull() ?: return state()
        val result: Double? = when (func) {
            "sqrt" -> if (num >= 0) sqrt(num) else null
            "cbrt" -> cbrt(num)
            "square" -> num * num
            "cube" -> num * num * num
            "inverse" -> if (num != 0.0) 1 / num else null
            "factorial" -> factorial(num)
            "exp" -> exp(num)
            "ln" -> if (num > 0) ln(num) else null
            "log" -> if (num > 0) log10(num) else null
            "abs" -> abs(num)
            "sin" -> sin(Math.toRadians(num))
            "cos" -> cos(Math.toRadians(num))
            "tan" -> tan(Math.toRadians(num))
            else -> return state()
        }
        currentValue = if (result == null) "ERROR" else formatNumber(result)
        shouldResetDisplay = true
        return state()
    }

    fun insertConstant(constant: String): CalculatorState {
        currentValue = when (constant) {
            "pi" -> formatNumber(Math.PI)
            "e" -> formatNumber(Math.E)
            else -> currentValue
        }
        shouldResetDisplay = true
        return state()
    }

    private fun operatorSymbol(op: Char): String = when (op) {
        '+' -> "+"
        '-' -> "−"
        '*' -> "×"
        '/' -> "÷"
        '%' -> "%"
        '^' -> "^"
        else -> ""
    }

    private fun factorial(num: Double): Double? {
        if (num < 0 || num != num.toLong().toDouble()) return null
        if (num > 170) return Double.POSITIVE_INFINITY
        var result = 1.0
        for (i in 2..num.toInt()) result *= i
        return result
    }

    private fun formatNumber(value: Double): String {
        if (value.isInfinite()) return "∞"
        if (value.isNaN()) return "ERROR"
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.8f", value).trimEnd('0').trimEnd('.')
        }
    }
}
