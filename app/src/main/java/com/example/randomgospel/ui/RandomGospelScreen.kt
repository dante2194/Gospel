package com.example.randomgospel.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.randomgospel.data.SettingsStore
import com.example.randomgospel.util.BibleUrlBuilder

@Composable
fun RandomGospelScreen(onOpenSettings: () -> Unit) {
    val context = LocalContext.current

    // Re-read from prefs whenever this composable enters the composition
    // (i.e. every time we come back from the Settings screen).
    val translationCode = remember { SettingsStore.getTranslationCode(context) }
    val versionId       = remember { SettingsStore.getVersionId(context) }

    var finalUrl by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onOpenSettings) {
                Text("\u2699  Settings")
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "\uD83D\uDCD6 Random Gospel",
            fontSize = 32.sp,
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Translation: $translationCode  •  Translation ID: $versionId",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                val url = BibleUrlBuilder.buildRandomGospelUrl(
                    translationCode = translationCode.ifBlank {
                        SettingsStore.DEFAULT_TRANSLATION_CODE
                    },
                    versionId = versionId.ifBlank {
                        SettingsStore.DEFAULT_VERSION_ID
                    }
                )
                finalUrl = url
                openUrl(context, url)
            },
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) {
            Text("Open a Random Gospel Chapter", fontSize = 18.sp)
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Final URL — edit the translation code, chapter, verse…",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = finalUrl,
            onValueChange = { finalUrl = it.trim() },
            label = { Text("URL") },
            placeholder = { Text("https://bible.com/bible/111/mat.4.1.NIV") },
            singleLine = false,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                capitalization = KeyboardCapitalization.None
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { finalUrl = "" },
                enabled = finalUrl.isNotEmpty(),
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("Clear")
            }
            Button(
                onClick = { openUrl(context, finalUrl) },
                enabled = finalUrl.isNotBlank(),
                modifier = Modifier.weight(2f).height(56.dp)
            ) {
                Text("Open in Browser", fontSize = 16.sp)
            }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    if (url.isBlank()) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) { /* no browser installed */ }
}
