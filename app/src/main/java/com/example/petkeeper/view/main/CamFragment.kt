package com.example.petkeeper.view.main

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.petkeeper.R
import com.example.petkeeper.databinding.FragmentCamBinding
import com.example.petkeeper.util.api.RetrofitBuilder
import com.example.petkeeper.util.binding.BindingFragment
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CamFragment : BindingFragment<FragmentCamBinding>(R.layout.fragment_cam, true) {
    private lateinit var image: MultipartBody.Part
    private lateinit var uri: Uri
    private lateinit var result: String

    private val photoPickLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { result ->
        result?.let {
            binding?.imageView?.setImageURI(it)
            binding?.imageView?.isSelected = true
            uri = it
            image = uriToMultipart(it)
            postImage(image)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initGallery()
    }

    private fun initResult() {
        binding?.startButton?.setOnClickListener {
            if(binding?.imageView?.isSelected!!) {
                val transaction = activity?.supportFragmentManager?.beginTransaction()
                transaction?.apply {
                    replace(R.id.fragment, ResultFragment(uri, result))
                    addToBackStack(null)
                    commit()
                }
            }else {
                Toast.makeText(requireContext(), "사진을 선택해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("Recycle")
    fun uriToMultipart(uri: Uri): MultipartBody.Part {
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        val byteArray = inputStream?.readBytes() ?: byteArrayOf()

        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), byteArray)
        return MultipartBody.Part.createFormData("image", "image.jpg", requestFile)
    }

    private fun postImage(body: MultipartBody.Part){
        RetrofitBuilder.api.postEyeImage(image = body).enqueue(object: Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val data = response.body().toString()
                val jsonObject = JSONObject(data)
                val diseaseData = jsonObject.getJSONObject("data")

                var maxLabel = ""
                var maxValue = Double.MIN_VALUE

                for (key in diseaseData.keys()) {
                    val value = diseaseData.getDouble(key)
                    if (value > maxValue) {
                        maxValue = value
                        maxLabel = key
                    }
                }
                result = maxLabel
                initResult()
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d("post eye image error", t.message.toString())
            }
        })
    }

    private fun initGallery() {
        binding?.photoButton?.setOnClickListener {
            photoPickLauncher.launch(PickVisualMediaRequest())
        }
    }
}