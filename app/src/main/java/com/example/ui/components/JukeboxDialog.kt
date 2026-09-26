package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBorder
import com.example.ui.theme.VoidSurface
import com.example.ui.theme.VoidSurfaceHigh

@Composable
fun JukeboxDialog(
    currentTrack: String,
    onSelectTrack: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val tracks = listOf(
        Pair("Cyber Pulse 2088", "Futuristic high-energy synth wave with punchy kicks."),
        Pair("Metaverse Lofi Sunset", "Mellow Rhodes chords and chill vinyl crackle for hanging out."),
        Pair("Chiptune Odyssey", "Bouncy 8-bit retro arcade adventure anthem."),
        Pair("Deep Space Ambience", "Ethereal crystal pads and slow cosmic reverberations."),
        Pair("Neon Club Bass", "Heavy subterranean groove for the virtual dance floor.")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VoidSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = NeonMagenta)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Metaverse Jukebox", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select a track to synchronize audio across the entire room:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                tracks.forEach { (track, desc) ->
                    val isCurrent = track.equals(currentTrack, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCurrent) NeonMagenta.copy(alpha = 0.2f) else VoidSurfaceHigh)
                            .border(1.dp, if (isCurrent) NeonMagenta else VoidBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                onSelectTrack(track)
                                onDismiss()
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = if (isCurrent) NeonMagenta else NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track,
                                color = if (isCurrent) NeonMagenta else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = desc,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = VoidSurfaceHigh)
            ) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}
