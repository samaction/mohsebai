package com.yourname.ai

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * مدير الذاكرة الدائمة والسحابية للذكاء الاصطناعي
 * يقوم بحفظ سياق المحادثات وتفضيلات المستخدم الماليّة سحابياً في Firestore
 */
class FirebaseMemoryManager {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String
        get() = auth.currentUser?.uid ?: "guest_user"

    /**
     * حفظ سياق المحادثة أو ملخص الذاكرة المالية في Firestore
     */
    suspend fun saveMemorySnippet(key: String, content: String) {
        val memoryData = hashMapOf(
            "key" to key,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .collection("memory")
            .document(key)
            .set(memoryData)
            .await()
    }

    /**
     * جلب كافة محتويات الذاكرة الدائمة للذكاء الاصطناعي
     */
    suspend fun loadAllMemory(): Map<String, String> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("memory")
                .get()
                .await()

            val memoryMap = mutableMapOf<String, String>()
            for (doc in snapshot.documents) {
                val key = doc.getString("key") ?: doc.id
                val content = doc.getString("content") ?: ""
                memoryMap[key] = content
            }
            memoryMap
        } catch (e: Exception) {
            emptyMap()
        }
    }
}