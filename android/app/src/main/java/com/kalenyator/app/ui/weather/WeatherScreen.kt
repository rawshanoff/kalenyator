package com.kalenyator.app.ui.weather

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kalenyator.app.R
import com.kalenyator.app.data.weather.WeatherInfo
import com.kalenyator.app.ui.LocalViewModelFactory
import com.kalenyator.app.ui.theme.TentakoPink
import com.kalenyator.app.util.DateFormatUtil
import com.kalenyator.app.util.weatherEmoji

@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel(factory = LocalViewModelFactory.current)) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                stringResource(R.string.weather_title),
                style = MaterialTheme.typography.headlineSmall,
                color = TentakoPink
            )
            if (state.offline) {
                Text(
                    stringResource(R.string.weather_offline),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.weather_add_city)) },
                singleLine = true
            )
        }
        if (state.searching) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = TentakoPink, modifier = Modifier.height(32.dp))
                }
            }
        }
        items(state.searchResults, key = { "${it.latitude}_${it.longitude}" }) { result ->
            TextButton(onClick = { viewModel.addCity(result) }, modifier = Modifier.fillMaxWidth()) {
                Text(result.displayName(), modifier = Modifier.fillMaxWidth())
            }
        }
        if (state.loading && state.cities.isEmpty()) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = TentakoPink)
                }
            }
        }
        if (state.error != null && state.cities.isEmpty()) {
            item {
                Text(stringResource(R.string.weather_error), color = MaterialTheme.colorScheme.error)
            }
        }
        items(state.cities, key = { "${it.latitude}_${it.longitude}" }) { w ->
            WeatherCityCard(
                weather = w,
                onSetHome = { viewModel.setHomeCity(w) },
                onRemove = { viewModel.removeCity(w) }
            )
        }
        if (!state.loading && state.cities.isEmpty() && state.searchQuery.length < 2) {
            item {
                Text(
                    stringResource(R.string.weather_add_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun WeatherCityCard(
    weather: WeatherInfo,
    onSetHome: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (weather.isHome) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(weatherEmoji(weather.weatherCode), fontSize = 48.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            weather.cityName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (weather.isHome) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Star,
                                contentDescription = stringResource(R.string.weather_home_city),
                                tint = TentakoPink,
                                modifier = Modifier.height(18.dp)
                            )
                        }
                    }
                    Text(
                        "${weather.temperatureC.toInt()}°C",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TentakoPink
                    )
                    Text("${stringResource(R.string.weather_humidity)}: ${weather.humidity}%")
                    Text("${stringResource(R.string.weather_wind)}: ${weather.windKmh.toInt()} km/h")
                    if (weather.updatedAt.isNotEmpty()) {
                        Text(
                            "${stringResource(R.string.weather_updated)}: ${DateFormatUtil.formatDateTime(weather.updatedAt)}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    if (weather.isOffline) {
                        Text(
                            stringResource(R.string.weather_cached),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Column {
                    IconButton(onClick = onSetHome) {
                        Icon(
                            if (weather.isHome) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = stringResource(R.string.weather_set_home),
                            tint = TentakoPink
                        )
                    }
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.weather_remove_city))
                    }
                }
            }
            if (weather.dailyForecast.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.weather_forecast_7d),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    weather.dailyForecast.forEach { day ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(
                                Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(day.dateLabel, style = MaterialTheme.typography.labelSmall)
                                Text(weatherEmoji(day.weatherCode), fontSize = 22.sp)
                                Text("${day.maxC}°/${day.minC}°", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
