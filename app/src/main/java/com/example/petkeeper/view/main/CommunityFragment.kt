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
import com.example.petkeeper.util.adapter.CommunityAdapter
import com.example.petkeeper.databinding.FragmentCommunityBinding
import com.example.petkeeper.model.CommunityResponse
import com.example.petkeeper.model.Post
import com.example.petkeeper.model.PostList
import com.example.petkeeper.util.App
import com.example.petkeeper.util.api.RetrofitBuilder
import com.example.petkeeper.util.binding.BindingFragment
import com.example.petkeeper.view.dialog.PostDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityFragment : BindingFragment<FragmentCommunityBinding>(R.layout.fragment_community, true) {
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var dialog: PostDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            if(it != null) {
                dialog.closeDialog()
            }else {
                Log.d("test", "사진 선택 안됨")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //위 주석처리된거 다 getAllPosts로 넘김
        getAllPosts()

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



    private fun getAllPosts() {
        RetrofitBuilder.api.getAllPost().enqueue(object: Callback<CommunityResponse>{
            override fun onResponse(call: Call<CommunityResponse>, response: Response<CommunityResponse>) {
                var data = response.body()?.data!!
                Log.d("community test", data.toString())
                data.forEach { post ->
                    post.CommentsCount = post.p_post_comments.size
                    post.LikesCount = post.p_post_likes.size
                    post.IsLikedByCurrentUser = isCurrentUserLikedPost(post)
                }
                val postDetailsList = data.map { post ->
                    PostList(
                        POST_ID = post.POST_ID,
                        POSTED_USER_ID = post.USER_ID,
                        POSTED_USER_PET_IMAGE = post.USER.p_pets[0].PET_IMAGE ?: "",
                        POSTED_USER_EMAIL = post.USER.USER_EMAIL ?: "",
                        POST_IMAGE = post.POST_IMAGE ?: "",
                        POST_CONTENT = post.POST_CONTENT?:"",
                        LIKESCOUNT = post.LikesCount,
                        CommentsCount = post.CommentsCount,
                        POST_DATE = post.POST_DATE ?: "",
                        POST_TIME = post.POST_TIME ?: "",
                        IsLikedByCurrentUser = post.IsLikedByCurrentUser
                    )
                }
                Log.d("community test", postDetailsList.toString())
                Log.d("community test", postDetailsList[0].toString())
                Log.d("community test", postDetailsList[1].toString())

                val recyclerView = binding?.recyclerView
                val item = ArrayList<Data>()

                for(i in postDetailsList){
                    item.add(Data(
                        i.POST_ID,
                        i.POSTED_USER_PET_IMAGE,
                        i.POSTED_USER_EMAIL,
                        i.POST_CONTENT,
                        i.POST_IMAGE,
                        i.LIKESCOUNT,
                        i.CommentsCount,
                        i.IsLikedByCurrentUser
                    ))
                }

                val adapter = CommunityAdapter(requireContext(), item)
                recyclerView?.adapter = adapter
            }

            override fun onFailure(call: Call<CommunityResponse>, t: Throwable) {
                Log.d("community test error", t.message.toString())
            }
        })
    }

    // 포스트에 대해 현재 사용자가 좋아요를 했는지 확인하는 함수
    private fun isCurrentUserLikedPost(post: Post): Boolean {
        val currentUserId = App.preferences.userId
        return post.p_post_likes.any { it.USER_ID == currentUserId }
    }
}