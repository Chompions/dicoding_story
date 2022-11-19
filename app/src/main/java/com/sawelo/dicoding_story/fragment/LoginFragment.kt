package com.sawelo.dicoding_story.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.activity.StoryActivity
import com.sawelo.dicoding_story.databinding.FragmentLoginBinding
import com.sawelo.dicoding_story.ui.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.edLoginEmail.doAfterTextChanged { viewModel.setEmail(it.toString()) }
        binding.edLoginPassword.doAfterTextChanged { viewModel.setPassword(it.toString()) }

        binding.btnLoginSignIn.setOnClickListener {
            val email = viewModel.email.value.toString()
            val password = viewModel.password.value.toString()

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
                    lifecycleScope.launch {
                        binding.pbLoginProgressBar.visibility = View.VISIBLE
                        viewModel.login()

                        binding.pbLoginProgressBar.visibility = View.GONE
                        val intent = Intent(context, StoryActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    }
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
}