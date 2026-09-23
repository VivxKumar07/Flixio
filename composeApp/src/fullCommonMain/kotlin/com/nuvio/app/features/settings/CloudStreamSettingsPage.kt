package com.nuvio.app.features.settings

import androidx.compose.foundation.lazy.LazyListScope
import com.nuvio.app.features.cloudstream.CloudStreamSettingsPageContent

internal actual fun LazyListScope.cloudStreamSettingsContent() {
    item {
        CloudStreamSettingsPageContent()
    }
}
