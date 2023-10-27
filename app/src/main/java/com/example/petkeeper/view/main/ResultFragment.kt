package com.example.petkeeper.view.main

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.petkeeper.R
import com.example.petkeeper.databinding.FragmentResultBinding
import com.example.petkeeper.util.binding.BindingFragment

class ResultFragment(uri: Uri, result: String) : BindingFragment<FragmentResultBinding>(R.layout.fragment_result, false) {
    private val uri = uri
    private val result = result

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animationView = binding?.load!!

        animationView.playAnimation()

        Handler().postDelayed({
            animationView.cancelAnimation()
            animationView.visibility = View.GONE
            binding?.loadingText?.visibility = View.GONE
            initResult()
        }, 3000)
    }

    private fun initResult() {
        binding?.resultDetail?.visibility = View.VISIBLE
        binding?.titleText?.visibility = View.VISIBLE
        binding?.imageView?.setImageURI(uri)
        binding?.resultSummary?.visibility = View.VISIBLE
        binding?.resultSummary?.text = "$result 입니다"
        binding?.resultDetail?.text = when(result) {
            "정상" -> "눈 검사 결과 이상이 없습니다.\n앞으로도 정기적인 검사를 통해 건강을 지켜주세요!"
            "결막염" -> "결막염이 의심됩니다. 빨리 나아질 수 있도록 빠른 시일 내에 수의사의 진료를 받아주세요. \n 아래 지도 버튼을 클릭하시면 근처의 동물 병원들을 확인하실 수 있습니다."
            "유루증" -> "유루증이 의심됩니다. 유루증은 미관상의 문제를 일으킬 뿐만 아니라 눈물이 많이 흐른 피부와 털 주변에 세균이 자라나기 좋은 환경을 만들어 눈 주변 조직에 피부염을 일으킬 수 있기 때문에 항상 눈 주위를 청결하게 해주고 전용 사료를 급여하는 것이 중요합니다."
            "색소침착성 각막염" -> "각막염이 의심됩니다."
            "백내장" -> "백내장이 의심됩니다. 백내장은 시력 장애, 심지어 시력 상실까지 일으킬 수 있는 강아지 백내장은 강아지의 삶의 질에 상당한 영향을 미칠 수 있는 질환입니다. 아래 지도 버튼을 눌러 근처의 동물병원에서 정확한 진단을 받아보세요"
            else -> ""
        }
    }
}