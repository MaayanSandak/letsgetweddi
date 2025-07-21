package com.example.letsgetweddi.ui.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsgetweddi.databinding.ActivityChatBinding
import com.example.letsgetweddi.model.Message
import com.example.letsgetweddi.adapters.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var database: DatabaseReference
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val supplierId = intent.getStringExtra("supplierId") ?: return
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userId = user.uid

        val adapter = MessageAdapter(messages, userId)
        binding.recyclerChat.layoutManager = LinearLayoutManager(this)
        binding.recyclerChat.adapter = adapter

        database = FirebaseDatabase.getInstance()
            .getReference("chats")
            .child(userId)
            .child(supplierId)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)
                    if (message != null) messages.add(message)
                }
                adapter.notifyDataSetChanged()
                binding.recyclerChat.scrollToPosition(messages.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.buttonSend.setOnClickListener {
            val text = binding.editMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val message = Message(
                    senderId = userId,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )

                val chatRef = FirebaseDatabase.getInstance().getReference("chats")

                chatRef.child(userId).child(supplierId).push().setValue(message)
                chatRef.child(supplierId).child(userId).push().setValue(message)

                binding.editMessage.setText("")
            }
        }
    }
}
