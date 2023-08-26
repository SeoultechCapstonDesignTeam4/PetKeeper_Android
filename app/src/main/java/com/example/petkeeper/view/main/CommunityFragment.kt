package com.example.petkeeper.view.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.petkeeper.util.adapter.Data
import com.example.petkeeper.R
import com.example.petkeeper.util.adapter.RecyclerViewAdapter
import com.example.petkeeper.databinding.FragmentCommunityBinding
import com.example.petkeeper.util.binding.BindingFragment
import com.example.petkeeper.view.dialog.PostDialog

class CommunityFragment : BindingFragment<FragmentCommunityBinding>(R.layout.fragment_community, true) {
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var dialog: PostDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            if(it != null) {
//                dialog.closeDialog()
            }else {
                Log.d("test", "사진 선택 안됨")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = binding?.recyclerView
        val item = ArrayList<Data>()

        for(i in 0..8){
            item.add(Data("test"))
        }

        val adapter = RecyclerViewAdapter(item)
        recyclerView?.adapter = adapter

        val cameraCallback: () -> Unit = {
            initCamera()
        }

        binding?.postButton?.apply {
            setImageResource(R.drawable.post)
            setOnClickListener {
                dialog = PostDialog(context as AppCompatActivity)
                dialog.initDialog(pickMedia, cameraCallback)
            }
        }
    }

    private fun initCamera() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                runCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionContextPopup(Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 1000)
            }
        }
    }

    private fun runCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 3000)
    }

    private fun showPermissionContextPopup(permission: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("권한이 필요합니다.")
            .setMessage("사진을 등록하기 위해서 권한이 필요합니다.")
            .setPositiveButton("동의하기") { _, _ ->
                requestPermissions(arrayOf(permission), 1000)
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()
    }
}