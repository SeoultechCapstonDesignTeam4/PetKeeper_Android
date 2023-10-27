package com.example.petkeeper.view.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.petkeeper.databinding.DetailDialogBinding
import com.example.petkeeper.util.App
import com.example.petkeeper.util.adapter.DateItem
import com.example.petkeeper.util.adapter.ViewPagerAdapter
import com.example.petkeeper.util.api.RetrofitBuilder
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailDialog(context : AppCompatActivity, private val viewModel: TestViewModel) {
    private val binding = DetailDialogBinding.inflate(context.layoutInflater)
    private val dialog = Dialog(context)
    private val viewPagerAdapter = ViewPagerAdapter(context)

    @SuppressLint("SetTextI18n")
    fun initDialog(year: Int, month: Int, day: DateItem){
        binding.viewPager.adapter = viewPagerAdapter
        binding.indicator.setViewPager(binding.viewPager)
        binding.titleText.text = "${year}년 ${month-1}월 ${day.date}일"

        viewPagerAdapter.apply {
//            addFragment(DetailInfoDialog())
            addFragment(DetailExaminationDialog())
        }

        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(binding.root)
            window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }.show()

        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }

        binding.confirmButton.setOnClickListener {
            Log.d("testtt weight test", viewModel.weight.value.toString())
            RetrofitBuilder.api.postWeight(PET_WEIGHT=viewModel.weight.value.toString(), PET_WEIGHT_DATE = binding.titleText.text.toString()).enqueue(object :
                Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if(response.isSuccessful){
                        Log.d("testtt", response.body().toString())
                        dialog.dismiss()
                    }else{
                        Log.d("testtt", response.body().toString())
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d("testtt", t.message.toString())
                }
            })
        }

        binding.cancelButton.setOnClickListener {
            dialog.dismiss()
        }
    }
}