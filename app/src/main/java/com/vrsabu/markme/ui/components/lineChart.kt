package com.vrsabu.markme.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
private fun JetpackComposeBasicLineChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
) {
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(),
            ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}

@Composable
fun JetpackComposeBasicLineChart(modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            // Learn more: https://patrykandpatrick.com/vmml6t.
            lineSeries { series(13, 8, 7, 12, 0, 1, 15, 14, 0, 11, 6, 12, 0, 11, 12, 11) }
        }
    }
    JetpackComposeBasicLineChart(modelProducer, modifier)
}

@Composable
@Preview
private fun Preview() {
    val modelProducer = remember { CartesianChartModelProducer() }
    // Use `runBlocking` only for previews, which don’t support asynchronous execution.
    runBlocking {
        modelProducer.runTransaction {
            // Learn more: https://patrykandpatrick.com/vmml6t.
            lineSeries { series(13, 8, 7, 12, 0, 1, 15, 14, 0, 11, 6, 12, 0, 11, 12, 11) }
        }
    }
    PreviewBox { JetpackComposeBasicLineChart(modelProducer) }
}

@Composable
fun PercentageDateLineChart(
    dates: List<String>,
    percentages: List<Float>,
    modifier: Modifier = Modifier,
    percentagesAreFractions: Boolean = false,
    datePattern: String = "MMM d",
) {
    // Validate input
    if (dates.size != percentages.size) {
        throw IllegalArgumentException("dates and percentages must have the same size")
    }

    // Prepare formatters and data
    val sdf = SimpleDateFormat(datePattern, Locale.getDefault())

    // Convert incoming percentages to fractional values expected by the chart (0.0..1.0)
    val yValues: List<Double> = if (percentagesAreFractions) {
        percentages.map { it.toDouble() }
    } else {
        percentages.map { it / 100.0 }
    }

    // Dates are supplied as strings (usually epoch millis as string). Convert to numeric x-values.
    // If callers pass human-readable strings, parsing will fail -> we catch and fallback to indices.
    val xValuesNumeric: List<Long> = try {
        dates.map { it.toLong() }
    } catch (_: Exception) {
        // fallback: use sequential indices as millis (not ideal but keeps the chart working)
        val base = System.currentTimeMillis()
        dates.indices.map { base + it.toLong() }
    }

    // Formatter expected signature: (CartesianMeasuringContext, Double, Axis.Position?) -> CharSequence
    val percentFormatter = CartesianValueFormatter { _, value, _ ->
        // value is Double in range 0.0..1.0; convert to percentage
        val pct = value * 100.0
        if (pct % 1.0 == 0.0) {
            String.format(Locale.getDefault(), "%d%%", pct.toInt())
        } else {
            String.format(Locale.getDefault(), "%.1f%%", pct)
        }
    }

    val dateFormatter = CartesianValueFormatter { _, value, _ ->
        val millis = value.toLong()
        sdf.format(Date(millis))
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(dates, percentages) {
        modelProducer.runTransaction {
            lineSeries { series(xValuesNumeric, yValues) }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(valueFormatter = percentFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = dateFormatter,
                itemPlacer = remember { HorizontalAxis.ItemPlacer.segmented() }
            )
        ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}

/*@Composable
@Preview
private fun PreviewPercentageDateLineChart() {
    // Sample usage: 7 days starting today with percentages in 0..100
    val today = System.currentTimeMillis()
    val dayMs = 24L * 60L * 60L * 1000L
    val sampleDates = (0 until 7).map { today + it * dayMs }
    val samplePercentages = listOf(12f, 25f, 45f, 30f, 55f, 60f, 50f)

    PreviewBox { PercentageDateLineChart(dates = sampleDates, percentages = samplePercentages) }
}*/
