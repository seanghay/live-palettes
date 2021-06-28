package com.seanghay.livepalettes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.seanghay.livepalettes.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                val fragment = PaletteFragment.newInstance()
                replace(R.id.container_view, fragment, PaletteFragment.TAG)
                setPrimaryNavigationFragment(fragment)
            }
        }
    }
}