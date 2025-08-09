package com.example.letsgetweddi.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.letsgetweddi.R
import com.example.letsgetweddi.databinding.FragmentGalleryBinding
import com.example.letsgetweddi.ui.gallery.ImageAdapter
import com.google.firebase.storage.FirebaseStorage

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ImageAdapter
    private val imageUrls = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ImageAdapter(imageUrls)
        binding.textGallery.visibility = View.GONE
        binding.root.post {
            setupRecycler()
            loadImages()
        }
    }

    private fun setupRecycler() {
        val spanCount = 3
        binding.root.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)?.let {
            // no-op if someone already added a RecyclerView
        } ?: run {
            val rv = androidx.recyclerview.widget.RecyclerView(requireContext())
            rv.id = View.generateViewId()
            val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            (binding.root as ViewGroup).addView(rv, params)
            rv.layoutManager = GridLayoutManager(requireContext(), 3)
            rv.adapter = adapter
        }
    }

    private fun loadImages() {
        val supplierId = arguments?.getString("supplierId").orEmpty()
        if (supplierId.isBlank()) {
            imageUrls.clear()
            imageUrls.add("android.resource://${requireContext().packageName}/${R.drawable.ic_launcher_foreground}")
            adapter.notifyDataSetChanged()
            return
        }

        val path = "suppliers/$supplierId/gallery"
        val ref = FirebaseStorage.getInstance().reference.child(path)

        ref.listAll()
            .addOnSuccessListener { listResult ->
                val tasks = listResult.items.map { it.downloadUrl }
                if (tasks.isEmpty()) {
                    imageUrls.clear()
                    imageUrls.add("android.resource://${requireContext().packageName}/${R.drawable.ic_launcher_foreground}")
                    adapter.notifyDataSetChanged()
                } else {
                    com.google.android.gms.tasks.Tasks.whenAllSuccess<android.net.Uri>(tasks)
                        .addOnSuccessListener { uris ->
                            imageUrls.clear()
                            imageUrls.addAll(uris.map { it.toString() })
                            adapter.notifyDataSetChanged()
                        }
                }
            }
            .addOnFailureListener {
                imageUrls.clear()
                imageUrls.add("android.resource://${requireContext().packageName}/${R.drawable.ic_launcher_foreground}")
                adapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(supplierId: String): GalleryFragment {
            val f = GalleryFragment()
            f.arguments = Bundle().apply { putString("supplierId", supplierId) }
            return f
        }
    }
}
