package com.example.petkeeper.view.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.petkeeper.R
import com.example.petkeeper.databinding.FragmentUpdateUserInfoBinding
import com.example.petkeeper.model.UserResponse
import com.example.petkeeper.util.App
import com.example.petkeeper.util.api.RetrofitBuilder
import com.example.petkeeper.util.binding.BindingFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateUserInfo : BindingFragment<FragmentUpdateUserInfoBinding>(R.layout.fragment_update_user_info, false) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUserData()
    }

    private fun updateUserData() {
//        RetrofitBuilder.api.getUserInfo()
    }

    private fun getUserData(){
        RetrofitBuilder.api.getUserInfo(userId= App.preferences.userId.toString()).enqueue(object :
            Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                val responseData = response.body()!!
                binding?.name?.setText(responseData.USER_NAME)
                binding?.email?.setText(responseData.USER_EMAIL)
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {

            }

        })
    }
}