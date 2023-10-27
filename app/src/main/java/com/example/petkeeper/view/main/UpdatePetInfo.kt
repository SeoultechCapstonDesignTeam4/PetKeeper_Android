package com.example.petkeeper.view.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.example.petkeeper.R
import com.example.petkeeper.databinding.FragmentUpdatePetInfoBinding
import com.example.petkeeper.model.UpdatePetBody
import com.example.petkeeper.model.UserResponse
import com.example.petkeeper.util.App
import com.example.petkeeper.util.api.RetrofitBuilder
import com.example.petkeeper.util.binding.BindingFragment
import com.example.petkeeper.view.dialog.BirthPickerDialog
import com.example.petkeeper.view.dialog.PostDialog
import com.example.petkeeper.view.dialog.PostDialogData
import com.example.petkeeper.view.register.RegisterViewModel
import com.google.gson.JsonObject
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UpdatePetInfo : BindingFragment<FragmentUpdatePetInfoBinding>(R.layout.fragment_update_pet_info, false),
    PostDialogData {
    private lateinit var byteArray: ByteArray
    private lateinit var dialog: PostDialog
    private lateinit var image: MultipartBody.Part
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private val viewModel: RegisterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPetData()
        setImage()
        initPickMedia()
        initAddPhoto()
        binding?.breedSelect?.setOnClickListener {
            initSpinner()
        }
        binding?.birthButton?.setOnClickListener {
            initBirth()
        }
        binding?.saveButton?.setOnClickListener {
            updatePetData()
        }
    }

    fun imageViewToBitmap() {
        val drawable = binding?.image?.drawable as BitmapDrawable
        val bitmap = drawable.bitmap

        val filesDir = requireContext().filesDir
        val file = File(filesDir, "image.jpg")

        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()
        val reqFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        image = MultipartBody.Part.createFormData("image", file.name, reqFile)
    }

    private fun updatePetData() {
        val name = RequestBody.create("text/plain".toMediaTypeOrNull(), binding?.name?.text.toString())
        val kind = RequestBody.create("text/plain".toMediaTypeOrNull(), binding?.breedSelect?.text.toString())
        val birth = RequestBody.create("text/plain".toMediaTypeOrNull(), binding?.birthButton?.text.toString())
        val updatePetBody = HashMap<String, RequestBody>()
        updatePetBody["PET_NAME"] = name
        updatePetBody["PET_KIND"] = kind
        updatePetBody["PET_BIRTHDATE"] = birth

        imageViewToBitmap()
        RetrofitBuilder.api.updatePet(
            pet = updatePetBody
        ).enqueue(object: Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                Log.d("testtt", response.toString())
                Log.d("testt id", App.preferences.Pet().id.toString())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Log.d("testtt", t.message.toString())
                Log.d("testt id", App.preferences.Pet().id.toString())

            }
        })
    }

    private fun getPetData() {
        RetrofitBuilder.api.getUserInfo(userId= App.preferences.userId.toString()).enqueue(object: Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                val responseData = response.body()!!.p_pets[0]
                binding?.name?.setText(responseData.PET_NAME)
                binding?.breedSelect?.text = responseData.PET_KIND
                binding?.birthButton?.text = responseData.PET_BIRTHDATE
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {

            }
        })
    }

    // 사진 선택 dialog
    private fun initAddPhoto() {
        dialog = PostDialog(requireActivity())
        binding?.image?.setOnClickListener {
            dialog.initDialog(pickMedia, cameraCallback)
        }
    }

    // 이미지 설정
    private fun setImage(){
        viewModel.uri.observe(viewLifecycleOwner) {
            binding?.image?.setImageURI(it)
        }
    }

    // 갤러리 접근
    private fun initPickMedia(){
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            if(it != null) {
                dialog.closeDialog()
                viewModel.setUri(it)
            }else {
                Log.d("test", "사진 선택 안됨")
            }
        }
    }

    // 카메라 접근
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode != Activity.RESULT_OK){
            return
        }

        when(requestCode) {
            1000 -> {
                val selectedImageUri: Uri? = data?.data
                if(selectedImageUri != null){
                    binding?.image?.setImageURI(selectedImageUri)
                    val image = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUri)
                    val stream = ByteArrayOutputStream()
                    image.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    byteArray = stream.toByteArray()
                }else{
                    Toast.makeText(requireContext(), "사진을 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
                }
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

    private val cameraCallback: () -> Unit = {
        initCamera()
    }

    // 품종 선택 dialog
    private fun initSpinner() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setItems(R.array.category_array){
                    _, p1 ->
                binding?.breedSelect?.text = resources.getStringArray(R.array.category_array)[p1]
                App.preferences.Pet().breed = resources.getStringArray(R.array.category_array)[p1]
                binding?.breedSelect?.isSelected = true
            }
            create()
            show()
        }
    }

    // 생년월일 선택 dialog
//    fun test(view: View){
//        val dialog = BirthPickerDialog(this)
//        dialog.initDialog()
//    }

    private fun initBirth(){
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val dateString = "${year}년 ${month+1}월 ${day}일"
            binding?.birthButton?.apply {
                text = dateString
                isSelected = true
//                age = calculateAge(year, month+1, day)
            }
        }
        DatePickerDialog(requireContext(),
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)).
        show()
    }

    val calculateAge: (year: Int, month: Int, day: Int) -> Int = { year, month, day ->
        val currentDate = Calendar.getInstance()
        val currentYear = currentDate.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH) + 1
        val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)
        var age = currentYear - year
        if (currentMonth < month || (currentMonth == month && currentDay < day)) age--
        age
    }

    override fun postData(data: String) {
        binding?.birthButton?.text = data
        binding?.birthButton?.isSelected = true
    }
}