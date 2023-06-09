package com.example.petkeeper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.petkeeper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var backKeyPressedTime : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initNavigationBar()
    }

    override fun onBackPressed() {
        if(System.currentTimeMillis() > backKeyPressedTime + 2500){
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "종료하려면 뒤로가기를 한 번 더 누르세요", Toast.LENGTH_SHORT).show()
            return;
        }
        if(System.currentTimeMillis() <= backKeyPressedTime + 2500){
            finishAffinity()
        }
    }


    private fun initNavigationBar(){
        fun changeFragment(fragment: Fragment) {
            val name = intent.getStringExtra("name")
            val image = intent.getByteArrayExtra("image")

            val bundle = Bundle()
            bundle.putString("name", name)
            bundle.putByteArray("image", image)
            val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            fragment.arguments = bundle
            fragmentTransaction.replace(R.id.fragment, fragment)
            fragmentTransaction.commit()
        }

        changeFragment(MainFragment())
        binding.bottomNav.run {
            setItemSelected(R.id.home)
            setOnItemSelectedListener { item ->
                when (item){
                    R.id.home -> changeFragment(MainFragment())
                    R.id.cam -> changeFragment(CamFragment())
                    R.id.map -> changeFragment(MapFragment())
                    R.id.community -> changeFragment(CommunityFragment())
                    R.id.profile -> changeFragment(ProfileFragment())
                }
            }
        }
    }
}