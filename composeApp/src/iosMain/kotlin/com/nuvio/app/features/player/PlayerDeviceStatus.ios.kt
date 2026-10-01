package com.nuvio.app.features.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIDevice
import kotlin.math.roundToInt

@Composable
internal actual fun rememberPlayerDeviceStatus(): PlayerDeviceStatus {
    val formatter = remember {
        NSDateFormatter().apply {
            dateStyle = NSDateFormatterNoStyle
            timeStyle = NSDateFormatterShortStyle
        }
    }
    var status by remember {
        mutableStateOf(
            PlayerDeviceStatus(
                timeLabel = formatter.stringFromDate(NSDate()),
                currentTimeMillis = (NSDate().timeIntervalSince1970 * 1000.0).toLong(),
                batteryPercent = (UIDevice.currentDevice.batteryLevel.takeIf { it >= 0f }?.times(100f))?.roundToInt(),
                batteryCharging = false,
                networkType = PlayerDeviceNetworkType.Wifi,
            ),
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = NSDate()
            val batteryLevel = UIDevice.currentDevice.batteryLevel
            status = PlayerDeviceStatus(
                timeLabel = formatter.stringFromDate(now),
                currentTimeMillis = (now.timeIntervalSince1970 * 1000.0).toLong(),
                batteryPercent = if (batteryLevel >= 0f) (batteryLevel * 100f).roundToInt() else null,
                batteryCharging = false,
                networkType = PlayerDeviceNetworkType.Wifi,
            )
            delay(1000L)
        }
    }

    return status
}
