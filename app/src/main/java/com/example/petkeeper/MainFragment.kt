package com.example.petkeeper

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.checkPermission
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petkeeper.databinding.FragmentMainBinding
import com.google.gson.JsonObject
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private lateinit var name: String
    private lateinit var image: Bitmap
    private var isFabOpen = false
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nameText.text = name
        binding.dogImage.setImageBitmap(image)

        binding.fabMain.apply {
            bringToFront()
            setOnClickListener {
                toggleFab()
            }
        }

        binding.dogImage.apply {
            setOnClickListener {
                initAddPhoto()
            }
        }

        binding.fabSub1.setOnClickListener {
            initCamera()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        name = arguments?.getString("name").toString()
        val bitmap = arguments?.getByteArray("image")!!
        image = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.size)

        mainActivity = context as MainActivity
    }

    private fun toggleFab() {
        if (isFabOpen) {
            ObjectAnimator.ofFloat(binding.fabSub1, "translationY", 0f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabSub2, "translationY", 0f).apply { start() }
            binding.fabMain.setImageResource(R.drawable.ic_examination)
        } else {
            ObjectAnimator.ofFloat(binding.fabSub1, "translationY", -200f).apply { start() }
            ObjectAnimator.ofFloat(binding.fabSub2, "translationY", -400f).apply { start() }
            binding.fabMain.setImageResource(R.drawable.ic_down)
        }
        isFabOpen = !isFabOpen
    }

    private fun initCamera() {
        when {
            ContextCompat.checkSelfPermission(
                mainActivity,
                CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                runCamera()
            }
            shouldShowRequestPermissionRationale(CAMERA) -> {
                showPermissionContextPopup(CAMERA)
            }
            else -> {
                requestPermissions(arrayOf(CAMERA), 1000)
            }
        }
    }

    private fun initAddPhoto(){
        when {
            ContextCompat.checkSelfPermission(
                mainActivity,
                READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                navigatePhoto()
            }
            shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE) -> {
                showPermissionContextPopup(READ_EXTERNAL_STORAGE)
            }
            else -> {
                requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), 1000)
            }
        }
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode != Activity.RESULT_OK){
            return
        }
        val selectedImageUri: Uri? = data?.data

        when(requestCode) {
            2000 -> {
                if(selectedImageUri != null){
                    binding.dogImage.setImageURI(selectedImageUri)
                }else{
                    Toast.makeText(mainActivity, "사진을 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
                }
            }

            3000 -> {
                val extras = data?.extras
                val bitmap = extras?.get("data") as Bitmap?
                val rotatedBitmap = rotateBitmap(bitmap!!, 90f)
                val file: File = convertBitmapToFile(rotatedBitmap)

                val survey = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                val multipart = MultipartBody.Part.createFormData("image", "download.jpg", survey)
                postImage(multipart)
            }

            else -> {
                Toast.makeText(mainActivity, "사진을 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPermissionContextPopup(permission: String) {
        AlertDialog.Builder(mainActivity)
            .setTitle("권한이 필요합니다.")
            .setMessage("사진을 등록하기 위해서 권한이 필요합니다.")
            .setPositiveButton("동의하기") { _, _ ->
                requestPermissions(arrayOf(permission), 1000)
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()
    }

    private fun navigatePhoto(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 2000)
    }

    private fun runCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 3000)
    }

    private fun getAbsolutePathFromUri(uri: Uri, context: Context): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        var path: String? = null
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = cursor.getString(columnIndex)
            }
        }
        return path
    }

    private fun convertBitmapToFile(bitmap: Bitmap): File{
        val file = File(mainActivity.filesDir, "picture")
        val output = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        return file
    }

    private fun postImage(body: MultipartBody.Part){
        RetrofitBuilder.api.postEyeImage(body).enqueue(object: Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                Log.d("post eye image", response.toString())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d("post eye image", t.message.toString())
            }
        })
    }
}