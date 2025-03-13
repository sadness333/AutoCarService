package com.example.autocarservice.repository

import com.example.autocarservice.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    private val chatMessagesCollection = firestore.collection("chat_messages")

    override suspend fun sendMessage(message: ChatMessage): Result<ChatMessage> {
        return try {
            val docRef = chatMessagesCollection.document()
            val chatMessage = message.copy(id = docRef.id)
            docRef.set(chatMessage).await()
            Result.success(chatMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMessagesForServiceRequest(serviceRequestId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = chatMessagesCollection
            .whereEqualTo("serviceRequestId", serviceRequestId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull {
                    it.toObject(ChatMessage::class.java)
                } ?: emptyList()
                
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun markMessagesAsRead(serviceRequestId: String, userId: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            
            // Get all unread messages sent to this user
            val querySnapshot = chatMessagesCollection
                .whereEqualTo("serviceRequestId", serviceRequestId)
                .whereNotEqualTo("senderId", userId) // Messages not sent by this user
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            querySnapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUnreadMessageCount(serviceRequestId: String, userId: String): Flow<Int> = callbackFlow {
        val listener = chatMessagesCollection
            .whereEqualTo("serviceRequestId", serviceRequestId)
            .whereNotEqualTo("senderId", userId) // Messages not sent by this user
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val count = snapshot?.size() ?: 0
                trySend(count)
            }
        
        awaitClose { listener.remove() }
    }
}
