package com.yourname.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F766E))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // أيقونة التطبيق العصرية
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Text(
                    text = if (isSignUp) "إنشاء حساب مؤسسي جديد" else "مرحباً بك في محاسب AI",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (isSignUp) "قم بإنشاء حسابك لمزامنة بياناتك المالية سحابياً" else "سجل دخولك للوصول إلى ذاكرتك المالية المعزولة والدخول اللحظي",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // زر تسجيل الدخول الرسمي عبر حساب Google
                Button(
                    onClick = {
                        onLoginSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                ) {
                    Text("🔍 تسجيل الدخول باستخدام حساب Google", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f))
                    Text(" أو بالبريد ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Divider(modifier = Modifier.weight(1f))
                }

                // حقل البريد الإلكتروني
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                // حقل كلمة المرور
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                // زر تسجيل الدخول
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "يرجى كتابة البريد وكلمة المرور"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                if (isSignUp) {
                                    auth.createUserWithEmailAndPassword(email, password)
                                } else {
                                    auth.signInWithEmailAndPassword(email, password)
                                }
                                isLoading = false
                                onLoginSuccess()
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "خطأ: ${e.localizedMessage ?: "فشل الدخول"}"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(
                            text = if (isSignUp) "إنشاء الحساب" else "تسجيل الدخول",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // زر الدخول كزائر / تخطي الدخول
                OutlinedButton(
                    onClick = { onLoginSuccess() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("المتابعة كزائر بدون حساب")
                }

                // التبديل بين التسجيل والدخول
                TextButton(onClick = { isSignUp = !isSignUp }) {
                    Text(
                        text = if (isSignUp) "لديك حساب بالفعل؟ سجل دخولك" else "ليس لديك حساب؟ قم بإنشاء حساب جديد",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}