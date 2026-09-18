package com.example.randomgospel.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.randomgospel.data.SettingsStore

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var translationCode by rememberSaveable {
        mutableStateOf(SettingsStore.getTranslationCode(context))
    }
    var versionId by rememberSaveable {
        mutableStateOf(SettingsStore.getVersionId(context))
    }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "\u2699  Settings",
            fontSize = 28.sp,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Set the default translation used on the home screen.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = translationCode,
            onValueChange = { input ->
                translationCode = input.uppercase().filter { it.isLetter() }.take(6)
                message = ""
            },
            label = { Text("Translation code (NIV, AVD, KJV…)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = versionId,
            onValueChange = { input ->
                versionId = input.filter { it.isDigit() }.take(6)
                message = ""
            },
            label = { Text("Translation ID / Version ID (111 = NIV, 13 = AVD…)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    SettingsStore.reset(context)
                    translationCode = SettingsStore.DEFAULT_TRANSLATION_CODE
                    versionId = SettingsStore.DEFAULT_VERSION_ID
                    message = "Reset to defaults."
                },
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text("Reset")
            }
            Button(
                onClick = {
                    SettingsStore.save(
                        context,
                        translationCode.ifBlank { SettingsStore.DEFAULT_TRANSLATION_CODE },
                        versionId.ifBlank { SettingsStore.DEFAULT_VERSION_ID }
                    )
                    message = "Saved."
                },
                modifier = Modifier.weight(2f).height(56.dp)
            ) {
                Text("Save", fontSize = 16.sp)
            }
        }

        if (message.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(32.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Back to Home", fontSize = 16.sp)
        }
    }
}
