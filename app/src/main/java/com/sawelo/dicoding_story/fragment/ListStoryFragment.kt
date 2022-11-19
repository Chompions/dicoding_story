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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.databinding.FragmentListStoryBinding
import com.sawelo.dicoding_story.ui.ListStoryAdapter
import com.sawelo.dicoding_story.ui.StoryViewModel
import com.sawelo.dicoding_story.utils.SharedPrefsData
import com.sawelo.dicoding_story.utils.StoryComparator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ListStoryFragment : Fragment() {
    @Inject
    lateinit var sharedPrefsData: SharedPrefsData
    private lateinit var binding: FragmentListStoryBinding
    private lateinit var adapter: ListStoryAdapter
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
        binding = FragmentListStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        setRecyclerView()
        setData()

        binding.fabListAddStory.setOnClickListener {
            parentFragmentManager.commit {
                addToBackStack(null)
                replace<AddStoryFragment>(R.id.fcv_story_fragmentContainer)
            }
        }
    }

    private fun setData() {
        lifecycleScope.launch {
            viewModel.getStories().collectLatest { story ->
                adapter.submitData(story)
            }
        }
        (view?.parent as ViewGroup).doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    private fun setRecyclerView() {
        lifecycleScope.launch {
            adapter = ListStoryAdapter(StoryComparator) {
                    data, imageView ->

                viewModel.setCurrentStory(data)

                parentFragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack(null)
                    addSharedElement(imageView, imageView.transitionName)
                    replace<DetailStoryFragment>(R.id.fcv_story_fragmentContainer)
                }
            }

            binding.rvListRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.rvListRecyclerView.adapter = adapter
        }
    }
}