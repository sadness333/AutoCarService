package com.example.autocarservice.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.autocarservice.model.ServiceStatus

/**
 * A progress bar component for displaying service request progress
 */
@Composable
fun ServiceProgressBar(
    progress: Int = 0,
    status: ServiceStatus,
    modifier: Modifier = Modifier
) {
    // Calculate progress based on status if not explicitly provided
    val calculatedProgress = if (progress > 0) {
        progress
    } else {
        when (status) {
            ServiceStatus.PENDING -> 0
            ServiceStatus.ACCEPTED -> 20
            ServiceStatus.IN_PROGRESS -> 50
            ServiceStatus.PAUSED -> 50
            ServiceStatus.COMPLETED -> 100
            ServiceStatus.CANCELLED -> 0
        }
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = calculatedProgress / 100f,
        label = "progress"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Прогресс: $calculatedProgress%",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = status.toDisplayString(),
                style = MaterialTheme.typography.bodyMedium,
                color = status.getStatusColor()
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = status.getStatusColor(),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * A simpler version of ServiceProgressBar that only shows status without progress percentage
 */
@Composable
fun ServiceStatusIndicator(
    status: ServiceStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = status.getStatusColor().copy(alpha = 0.2f),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = status.toDisplayString(),
                style = MaterialTheme.typography.bodyMedium,
                color = status.getStatusColor(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun ServiceStatus.getStatusColor(): androidx.compose.ui.graphics.Color {
    return when (this) {
        ServiceStatus.PENDING -> MaterialTheme.colorScheme.tertiary
        ServiceStatus.ACCEPTED -> MaterialTheme.colorScheme.primary
        ServiceStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        ServiceStatus.PAUSED -> MaterialTheme.colorScheme.error
        ServiceStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        ServiceStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }
}

fun ServiceStatus.toDisplayString(): String {
    return when (this) {
        ServiceStatus.PENDING -> "Ожидание"
        ServiceStatus.ACCEPTED -> "Принято"
        ServiceStatus.IN_PROGRESS -> "В работе"
        ServiceStatus.PAUSED -> "Приостановлено"
        ServiceStatus.COMPLETED -> "Завершено"
        ServiceStatus.CANCELLED -> "Отменено"
    }
}
