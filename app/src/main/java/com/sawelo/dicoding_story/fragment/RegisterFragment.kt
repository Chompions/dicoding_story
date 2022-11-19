package com.sawelo.dicoding_story.fragment

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.databinding.FragmentRegisterBinding
import com.sawelo.dicoding_story.ui.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.edRegisterName.doAfterTextChanged { viewModel.setName(it.toString()) }
        binding.edRegisterEmail.doAfterTextChanged { viewModel.setEmail(it.toString()) }
        binding.edRegisterPassword.doAfterTextChanged { viewModel.setPassword(it.toString()) }

        binding.btnRegisterSignUp.setOnClickListener {
            val email = viewModel.email.value.toString()
            val password = viewModel.password.value.toString()

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
                    lifecycleScope.launch {
                        binding.pbRegisterProgressBar.visibility = View.VISIBLE
                        viewModel.register()

                        binding.pbRegisterProgressBar.visibility = View.GONE
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }
}