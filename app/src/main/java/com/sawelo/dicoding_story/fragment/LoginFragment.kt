package com.sawelo.dicoding_story.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.snackbar.Snackbar
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.activity.StoryActivity
import com.sawelo.dicoding_story.databinding.FragmentLoginBinding
import com.sawelo.dicoding_story.remote.ApiConfig
import com.sawelo.dicoding_story.remote.StoryResponse
import com.sawelo.dicoding_story.utils.SharedPrefsDataImpl.Companion.NAME_PREF_KEY
import com.sawelo.dicoding_story.utils.SharedPrefsDataImpl.Companion.TOKEN_PREF_KEY
import com.sawelo.dicoding_story.utils.SharedPrefsDataImpl.Companion.USER_ID_PREF_KEY
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {
    @Inject
    lateinit var sharedPrefs: SharedPreferences
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLoginSignIn.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            when {
                (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) -> {
                    println("Email is $email")
                    Snackbar
                        .make(binding.btnLoginSignIn, R.string.error_email, Snackbar.LENGTH_SHORT)
                        .show()
                }
                (password.length < 6) -> {
                    Snackbar
                        .make(
                            binding.btnLoginSignIn,
                            R.string.error_password,
                            Snackbar.LENGTH_SHORT
                        )
                        .show()
                }
                else -> {
                    login(email, password)
                }
            }
        }

        binding.btnLoginSignUp.setOnClickListener {
            parentFragmentManager.commit {
                addToBackStack(null)
                replace<RegisterFragment>(R.id.fcv_auth_fragmentContainer)
            }
        }
    }

    private fun login(email: String, password: String) {
        binding.pbLoginProgressBar.visibility = View.VISIBLE

        val client = ApiConfig.getApiService().login(email, password)
        client.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(
                call: Call<StoryResponse>,
                response: Response<StoryResponse>
            ) {
                binding.pbLoginProgressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val loginResult = response.body()?.loginResult
                    if (loginResult != null) {
                        sharedPrefs.edit {
                            putString(USER_ID_PREF_KEY, loginResult.userId)
                            putString(NAME_PREF_KEY, loginResult.name)
                            putString(TOKEN_PREF_KEY, loginResult.token)
                        }

                        Snackbar
                            .make(binding.btnLoginSignIn, "Login succeed", Snackbar.LENGTH_SHORT)
                            .show()

                        val intent = Intent(context, StoryActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    }
                } else {
                    if (response.errorBody() != null) {
                        val errorResponse = ApiConfig.convertErrorBody<StoryResponse>(
                            response.errorBody()!!
                        )
                        Snackbar
                            .make(
                                binding.btnLoginSignIn,
                                "Login failed: ${errorResponse.message}",
                                Snackbar.LENGTH_SHORT
                            )
                            .show()
                    }
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                binding.pbLoginProgressBar.visibility = View.GONE
                Snackbar
                    .make(
                        binding.btnLoginSignIn,
                        "Login failed: ${t.message}",
                        Snackbar.LENGTH_SHORT
                    )
                    .show()
            }
        })
    }
}