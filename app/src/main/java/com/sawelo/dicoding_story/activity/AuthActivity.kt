package com.sawelo.dicoding_story.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.fragment.LoginFragment
import com.sawelo.dicoding_story.utils.SharedPrefsData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    @Inject lateinit var sharedPrefsData: SharedPrefsData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        if (sharedPrefsData.getToken() != null) {
            val intent = Intent(this, StoryActivity::class.java)
            startActivity(intent)
            finish()
        }

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<LoginFragment>(R.id.fcv_auth_fragmentContainer)
        }
    }
}