package com.example.petkeeper.view.main

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.petkeeper.R
import com.example.petkeeper.util.api.RetrofitBuilder
import com.example.petkeeper.databinding.FragmentMainBinding
import com.example.petkeeper.model.PetVaccination
import com.example.petkeeper.model.PetWeight
import com.example.petkeeper.model.UserResponse
import com.example.petkeeper.util.App.Companion.preferences
import com.example.petkeeper.util.adapter.DateAdapter
import com.example.petkeeper.util.adapter.DateItem
import com.example.petkeeper.util.adapter.OnItemClickListener
import com.example.petkeeper.util.binding.BindingFragment
import com.example.petkeeper.view.dialog.DetailDialog
import com.example.petkeeper.view.dialog.TestViewModel
import com.example.petkeeper.view.dialog.VaccinationDialog
import com.example.petkeeper.view.dialog.WeightDialog
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainFragment : BindingFragment<FragmentMainBinding>(R.layout.fragment_main, true) {
    private val viewModel: TestViewModel by activityViewModels()

    private lateinit var context: MainActivity
    private var item = ArrayList<DateItem>()
    private val calendar = Calendar.getInstance()
    private lateinit var weightData: List<PetWeight>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context as MainActivity
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initInformation()
        initRecyclerView()
        getUserData()

        binding?.vaccinationDateText?.setOnClickListener {
            val dialog = VaccinationDialog(context)
            dialog.initDialog()
        }

        binding?.weightText?.setOnClickListener {
            val dialog = WeightDialog(context)
            dialog.initDialog()
        }
    }

    private fun initWeightChart() {
        val entries = ArrayList<Entry>()

        for ((index, weightDataItem) in weightData.withIndex()) {
            val weight = weightDataItem.PET_WEIGHT.toFloat()
            val date = weightDataItem.PET_WEIGHT_DATE
            entries.add(Entry(index.toFloat(), weight))
        }

        val dataSet = LineDataSet(entries, null)
        dataSet.color = Color.BLACK
        dataSet.valueTextColor = Color.RED
        dataSet.valueTextSize = 11f

        dataSet.lineWidth = 2.5f

        dataSet.circleRadius = 6f
        dataSet.setDrawCircles(true)

        val lineChart = binding!!.lineChart
        lineChart.data = LineData(dataSet)

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        val xLabels = ArrayList<String>()
        for (weightDataItem in weightData) {
            xLabels.add(weightDataItem.PET_WEIGHT_DATE)
        }

        xAxis.valueFormatter = IndexAxisValueFormatter(xLabels.toTypedArray())
        xAxis.setLabelCount(xLabels.size, true)

        lineChart.axisLeft.isEnabled = false
        lineChart.axisRight.isEnabled = false

        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        lineChart.invalidate()
    }

    private fun getUserData(){
        RetrofitBuilder.api.getUserInfo(userId=preferences.userId.toString()).enqueue(object : Callback<UserResponse>{
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                val responseData = response.body()!!.p_pets[0]
                Log.d("testttt", responseData.p_pet_vaccinations.toString())
                weightData = responseData.p_pet_weights
                Log.d("weight test", weightData.toString())
                initWeightChart()
                binding?.vaccinationDateText?.text = responseData.p_pet_vaccinations[0].PET_VACCINATION_DATE
                Glide.with(requireContext())
                    .load(responseData.PET_IMAGE)
                    .into(binding?.dogImage!!)
                binding?.weightText?.text = responseData.p_pet_weights.last().PET_WEIGHT.toString() + "kg"
                binding?.nameText?.text = responseData.PET_NAME
                binding?.ageText?.text = responseData.PET_BIRTHDATE
                binding?.breedText?.text = responseData.PET_KIND
                if(responseData.PET_GENDER == "male"){
                    binding?.genderImage?.setImageResource(R.drawable.male_icon)
                }else{
                    binding?.genderImage?.setImageResource(R.drawable.female_icon)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {

            }

        })
    }

    fun saveUriToFile(context: Context, uri: Uri): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timeStamp.jpg"

        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val file = File(storageDir, fileName)
                val fos = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    fos.write(buffer, 0, read)
                }
                fos.close()
                inputStream.close()
                return file
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    @SuppressLint("SetTextI18n")
    private fun initInformation(){
        binding?.weightText?.text = "${preferences.Pet().weight}kg"
        binding?.yearMonthText?.text = "${calendar.get(Calendar.YEAR)}년 ${calendar.get(Calendar.MONTH)+1}월"
    }

    private fun initDateItem(){
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        for(i in 1 ..calendar.getActualMaximum(Calendar.DAY_OF_MONTH)){
            item.add(DateItem(i, getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK))))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun initRecyclerView() {
        initDateItem()
        val adapter = DateAdapter(item)
        adapter.setOnItemClickListener(object : OnItemClickListener{
            override fun onItemClick(v: View, data: DateItem, pos: Int) {
                val dialog = DetailDialog(context, viewModel)
                dialog.initDialog(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, data)
            }
        })
        binding?.dateRecyclerView?.adapter = adapter
    }

    private fun getDayOfWeek(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "일"
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            else -> ""
        }
    }
}