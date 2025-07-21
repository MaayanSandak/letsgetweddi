package com.example.letsgetweddi.ui.categories

import android.app.AlertDialog
import android.widget.EditText
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsgetweddi.databinding.FragmentTipsAndChecklistBinding
import com.example.letsgetweddi.model.ChecklistItem
import com.example.letsgetweddi.model.Tip
import com.example.letsgetweddi.adapters.ChecklistAdapter
import com.example.letsgetweddi.adapters.TipAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TipsAndChecklistFragment : Fragment() {

    private lateinit var binding: FragmentTipsAndChecklistBinding
    private lateinit var database: DatabaseReference

    private val checklist = mutableListOf<ChecklistItem>()
    private val tips = mutableListOf<Tip>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTipsAndChecklistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        binding.recyclerChecklist.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTips.layoutManager = LinearLayoutManager(requireContext())

        val checklistAdapter = ChecklistAdapter(checklist, userId)
        val tipAdapter = TipAdapter(tips)

        binding.recyclerChecklist.adapter = checklistAdapter
        binding.recyclerTips.adapter = tipAdapter

        binding.buttonAddTask.setOnClickListener {
            val editText = EditText(requireContext())
            editText.hint = "Enter your task"

            AlertDialog.Builder(requireContext())
                .setTitle("New Task")
                .setView(editText)
                .setPositiveButton("Add") { _, _ ->
                    val taskText = editText.text.toString()
                    if (taskText.isNotEmpty()) {
                        val taskId = database.child("checklist").child(userId).push().key ?: return@setPositiveButton
                        val newTask = ChecklistItem(id = taskId, task = taskText, isDone = false)
                        database.child("checklist").child(userId).child(taskId).setValue(newTask)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        database = FirebaseDatabase.getInstance().reference

        database.child("checklist").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    checklist.clear()
                    for (child in snapshot.children) {
                        val item = child.getValue(ChecklistItem::class.java)
                        val id = child.key
                        if (item != null && id != null) {
                            val fullItem = item.copy(id = id)
                            checklist.add(fullItem)
                        }
                    }
                    checklistAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        database.child("tips")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tips.clear()
                    for (child in snapshot.children) {
                        val tip = child.getValue(Tip::class.java)
                        if (tip != null) tips.add(tip)
                    }
                    tipAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
