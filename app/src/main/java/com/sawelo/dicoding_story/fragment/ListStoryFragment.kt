package com.sawelo.dicoding_story.fragment

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.databinding.FragmentListStoryBinding
import com.sawelo.dicoding_story.remote.ApiConfig
import com.sawelo.dicoding_story.remote.StoryResponse
import com.sawelo.dicoding_story.ui.ListStoryAdapter
import com.sawelo.dicoding_story.ui.StoryViewModel
import com.sawelo.dicoding_story.utils.SharedPrefsData
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class ListStoryFragment : Fragment() {
    @Inject
    lateinit var sharedPrefsData: SharedPrefsData
    private lateinit var binding: FragmentListStoryBinding
    private val viewModel: StoryViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.fade)
        exitTransition = inflater.inflateTransition(R.transition.fade)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        postponeEnterTransition()
        binding = FragmentListStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabListAddStory.setOnClickListener {
            parentFragmentManager.commit {
                addToBackStack(null)
                replace<AddStoryFragment>(R.id.fcv_story_fragmentContainer)
            }
        }

        postponeEnterTransition()
        getStories()
    }

    private fun getStories() {
        binding.pbListProgressBar.visibility = View.VISIBLE

        val token = sharedPrefsData.getToken()
        val client = ApiConfig.getApiService().getStories("Bearer $token")
        client.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(
                call: Call<StoryResponse>,
                response: Response<StoryResponse>
            ) {
                binding.pbListProgressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.listStory != null) {
                        viewModel.setListStory(body.listStory)
                        setRecyclerView()
                    }
                } else {
                    if (response.errorBody() != null) {
                        val errorResponse = ApiConfig.convertErrorBody<StoryResponse>(
                            response.errorBody()!!
                        )
                        Snackbar
                            .make(
                                binding.root,
                                "Get stories failed: ${errorResponse.message}",
                                Snackbar.LENGTH_SHORT
                            )
                            .show()
                    }
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                binding.pbListProgressBar.visibility = View.GONE
                Snackbar
                    .make(binding.root, "Get stories failed: ${t.message}", Snackbar.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun setRecyclerView() {
        viewModel.listStory.observe(viewLifecycleOwner) { list ->

            binding.rvListRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.rvListRecyclerView.adapter = ListStoryAdapter(list) { position, imageView ->

                viewModel.setCurrentStory(list[position])

                parentFragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack(null)
                    addSharedElement(imageView, imageView.transitionName)
                    replace<DetailStoryFragment>(R.id.fcv_story_fragmentContainer)
                }
            }

            (view?.parent as ViewGroup).doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }
}