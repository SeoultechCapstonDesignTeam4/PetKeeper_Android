package com.example.petkeeper.util.binding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.example.petkeeper.view.dialog.FinishDialog

abstract class BindingFragment<T : ViewDataBinding>(
    @LayoutRes val layoutRes: Int,
    private val isConnectedBnv: Boolean,
) : Fragment() {
    private var _binding: T? = null
    protected val binding
        get() = _binding
    var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.lifecycleOwner = viewLifecycleOwner
        if(isConnectedBnv){
            initBackpressedListener()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if(isConnectedBnv){
            onBackPressedCallback?.remove()
        }
        _binding = null
    }

    private fun initBackpressedListener(){
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val dialog = FinishDialog(context as AppCompatActivity)
                dialog.initDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            onBackPressedCallback as OnBackPressedCallback
        )
    }
}