package com.sawelo.dicoding_story.fragment

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.databinding.FragmentRegisterBinding
import com.sawelo.dicoding_story.remote.ApiConfig
import com.sawelo.dicoding_story.remote.StoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegisterSignUp.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            when {
                (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) -> {
                    Snackbar
                        .make(
                            binding.btnRegisterSignUp,
                            R.string.error_email,
                            Snackbar.LENGTH_SHORT
                        )
                        .show()
                }
                (password.length < 6) -> {
                    Snackbar
                        .make(
                            binding.btnRegisterSignUp,
                            R.string.error_password,
                            Snackbar.LENGTH_SHORT
                        )
                        .show()
                }
                else -> {
                    register(name, email, password)
                }
            }
        }
    }

    private fun register(name: String, email: String, password: String) {
        binding.pbRegisterProgressBar.visibility = View.VISIBLE

        val client = ApiConfig.getApiService().register(
            name, email, password
        )
        client.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(
                call: Call<StoryResponse>,
                response: Response<StoryResponse>
            ) {
                binding.pbRegisterProgressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    Snackbar
                        .make(binding.btnRegisterSignUp, "Register succeed", Snackbar.LENGTH_SHORT)
                        .show()
                    parentFragmentManager.popBackStack()
                } else {
                    if (response.errorBody() != null) {
                        val errorResponse = ApiConfig.convertErrorBody<StoryResponse>(
                            response.errorBody()!!
                        )
                        Snackbar
                            .make(
                                binding.btnRegisterSignUp,
                                "Register failed: ${errorResponse.message}",
                                Snackbar.LENGTH_SHORT
                            )
                            .show()
                    }
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                binding.pbRegisterProgressBar.visibility = View.GONE
                Snackbar
                    .make(
                        binding.btnRegisterSignUp,
                        "Register failed: ${t.message}",
                        Snackbar.LENGTH_SHORT
                    )
                    .show()
            }
        })
    }
}