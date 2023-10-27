package com.example.petkeeper.view.dialog

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petkeeper.R
import com.example.petkeeper.databinding.DetailExaminationDialogBinding
import com.example.petkeeper.model.PetVaccination
import com.example.petkeeper.model.UserResponse
import com.example.petkeeper.util.App
import com.example.petkeeper.util.api.RetrofitBuilder
import com.example.petkeeper.util.binding.BindingFragment
import com.mcdev.quantitizerlibrary.AnimationStyle
import com.mcdev.quantitizerlibrary.QuantitizerListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailExaminationDialog : BindingFragment<DetailExaminationDialogBinding>(R.layout.detail_examination_dialog, false) {
    private val viewModel: TestViewModel by activityViewModels()
    private lateinit var vaccinationData: List<PetVaccination>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getVaccinationData()

        binding?.numberPicker?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                viewModel.updateWeight(s.toString())
            }
        })

    }

    private fun getVaccinationData(){
        RetrofitBuilder.api.getUserInfo(userId= App.preferences.userId.toString()).enqueue(object :
            Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                Log.d("testttt", response.body()!!.toString())
                val responseData = response.body()!!.p_pets[0]
                vaccinationData = responseData.p_pet_vaccinations
                val layout = binding?.layout
                val customTypeface = ResourcesCompat.getFont(requireContext(), R.font.poppins_regular)

                for(i in vaccinationData) {
                    val textView = TextView(requireContext())
                    textView.text = i.PET_VACCINATION_NAME
                    textView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    textView.typeface = customTypeface
                    textView.setTextColor(Color.BLACK)
                    textView.textSize = 15f
                    layout?.addView(textView)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {

            }
        })
    }
}

class TestViewModel(): ViewModel(){
    private val _weight = MutableLiveData<String>()
    val weight: LiveData<String>
        get() = _weight

    fun updateWeight(newWeight: String) {
        _weight.value = newWeight
    }
}