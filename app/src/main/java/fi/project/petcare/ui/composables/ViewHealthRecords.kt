package fi.project.petcare.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fi.project.petcare.model.data.HealthRecord
import fi.project.petcare.model.data.HealthRecordType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Dummy function to fetch health records (replace with actual implementation)
fun getDummyHealthRecords(): List<HealthRecord> {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return listOf(
        HealthRecord(type = HealthRecordType.OPERATION, date = "2023-05-05", details = "Details of operation"),
        HealthRecord(type = HealthRecordType.VETERINARIAN_VISIT, date = "2021-03-04", details = "Details of vet visit"),
        HealthRecord(type = HealthRecordType.MEDICATION, date = "2020-03-03", details = "Details of medication"),
        HealthRecord(type = HealthRecordType.SYMPTOM, date = "1992-04-06", details = "Details of symptom"),
        HealthRecord(type = HealthRecordType.ALLERGY, date = "1999-09-09", details = "Details of allergy"),
        HealthRecord(type = HealthRecordType.EXERCISE, date = "2008-03-05", details = "Details of exercise"),
        HealthRecord(type = HealthRecordType.WEIGHT_MEASUREMENT, date = "2005-02-05", details = "Details of weight measurement")
    )
}

@Composable
fun ViewHealthRecords() {
    val healthRecords = getDummyHealthRecords()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "View Health Records")

        Spacer(modifier = Modifier.height(25.dp))


        // Display each health record
        healthRecords.forEach { record ->
            Text("${record.type}: ${record.date} - ${record.details}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Create scatter plot
        CreateScatterPlot(healthRecords)
    }
}


@Composable
fun CreateScatterPlot(data: List<HealthRecord>) {
    val plotWidth = 900f
    val plotHeight = 900f
    val margin = 20f
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val backgroundColor = Color.White // Choose your desired background color

    Canvas(modifier = Modifier.size(plotWidth.dp)) {
        // Draw background
        drawRect(color = backgroundColor, size = Size(plotWidth, plotHeight))

        // Calculate xValues and yValues
        val xValues = data.map { 
            dateFormat.parse(it.date)?.time?.toFloat() ?: 0f
        }
        val yValues = data.map { it.type.ordinal.toFloat() }

        // Calculate scaling factors
        val xMin = xValues.minOrNull() ?: 0f
        val xMax = xValues.maxOrNull() ?: 1f
        val yMin = yValues.minOrNull() ?: 0f
        val yMax = yValues.maxOrNull() ?: 1f
        
        val xRange = if (xMax - xMin == 0f) 1f else xMax - xMin
        val yRange = if (yMax - yMin == 0f) 1f else yMax - yMin
        
        val xScale = (plotWidth - 2 * margin) / xRange
        val yScale = (plotHeight - 2 * margin) / yRange

        // Draw gridlines
        val gridLineColor = Color.LightGray.copy(alpha = 0.5f)
        val gridStepX = (xRange / 5f)
        val gridStepY = (yRange / 5f)
        for (i in 0..5) {
            val xLine = margin + i * gridStepX * xScale
            drawLine(
                color = gridLineColor,
                start = Offset(xLine, margin),
                end = Offset(xLine, plotHeight - margin)
            )
        }
        for (i in 0..5) {
            val yLine = plotHeight - margin - i * gridStepY * yScale
            drawLine(
                color = gridLineColor,
                start = Offset(margin, yLine),
                end = Offset(plotWidth - margin, yLine)
            )
        }

        // Draw the data points with different colors based on type
        val colorMap = mapOf(
            HealthRecordType.OPERATION to Color.Red,
            HealthRecordType.VETERINARIAN_VISIT to Color.Blue,
            HealthRecordType.MEDICATION to Color(0xFFFFA500), // Orange
            HealthRecordType.SYMPTOM to Color.Green,
            HealthRecordType.ALLERGY to Color(0xFF388E3C), // Green shade
            HealthRecordType.EXERCISE to Color(0xFFA020F0), // Purple
            HealthRecordType.WEIGHT_MEASUREMENT to Color.Gray
        )

        data.forEach { record ->
            val time = dateFormat.parse(record.date)?.time?.toFloat() ?: 0f
            val x = margin + (time - xMin) * xScale
            val y = plotHeight - margin - (record.type.ordinal.toFloat() - yMin) * yScale
            val color = colorMap[record.type] ?: Color.Black
            drawCircle(color = color, radius = 10f, center = Offset(x, y))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewViewHealthRecords() {
    ViewHealthRecords()
}
