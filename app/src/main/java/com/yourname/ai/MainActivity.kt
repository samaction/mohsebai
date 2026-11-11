package com.yourname.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.ai.ui.theme.MohasibTheme // تم تحديث اسم الثيم
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MohasibTheme { // تم تحديث اسم الثيم هنا أيضاً
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope() // بديل آمن لـ MainScope

    var dataList by remember { mutableStateOf(listOf<DataEntity>()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // دالة لجلب البيانات
    fun loadData() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val items = withContext(Dispatchers.IO) {
                    db.dataDao().getAllData()
                }
                dataList = items
            } catch (e: Exception) {
                errorMessage = "خطأ في جلب البيانات: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // تشغيل الجلب عند فتح الشاشة
    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تطبيق محاسب AI") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        db.dataDao().insertData(DataEntity(title = "عنصر جديد", description = "تمت إضافته الآن"))
                    }
                    loadData() // تحديث القائمة بعد الإضافة
                }
            }) {
                Text("+", fontSize = 24.sp)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()

                errorMessage != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadData() }) { Text("إعادة المحاولة") }
                    }
                }

                dataList.isEmpty() -> {
                    Text(
                        text = "لا توجد بيانات. اضغط على زر + لإضافة عنصر.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(dataList) { item ->
                            DataCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DataCard(item: DataEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = item.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}