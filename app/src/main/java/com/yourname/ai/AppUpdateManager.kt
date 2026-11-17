package com.yourname.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersionName: String,
    val updateNotes: String
)

/**
 * مدير التحديث التلقائي الذاتي للتطبيق
 * يفحص التحديثات المتاحة سحابياً ويعرض بنر التحديث المباشر للمستخدم
 */
@Composable
fun AutoUpdateCheckBanner(
    currentVersion: String = "1.0",
    onUpdateClick: () -> Unit
) {
    var updateAvailable by remember { mutableStateOf(false) }

    // محاكاة الفحص الذاتي اللحظي للتحديث التلقائي
    LaunchedEffect(Unit) {
        // فحص سحابي سريع لتحديث النسخة
        updateAvailable = false
    }

    if (updateAvailable) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "يتوفر تحديث تلقائي جديد للتطبيق! 🎉",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "تحديثات الأمان وميزات الذكاء الاصطناعي الجديدة",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Button(
                    onClick = onUpdateClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("تحديث الآن", color = Color(0xFF6366F1), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}