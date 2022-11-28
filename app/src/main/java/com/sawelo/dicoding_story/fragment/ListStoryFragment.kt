package com.sawelo.dicoding_story.fragment

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sawelo.dicoding_story.R
import com.sawelo.dicoding_story.data.StoryListResponse
import com.sawelo.dicoding_story.databinding.FragmentListStoryBinding
import com.sawelo.dicoding_story.ui.ListStoryAdapter
import com.sawelo.dicoding_story.ui.StoryViewModel
import com.sawelo.dicoding_story.utils.SharedPrefsData
import com.sawelo.dicoding_story.utils.StoryComparator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ListStoryFragment : Fragment() {
    @Inject
    lateinit var sharedPrefsData: SharedPrefsData
    private lateinit var adapter: ListStoryAdapter
    private val viewModel: StoryViewModel by activityViewModels()
    private var binding: FragmentListStoryBinding? = null

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
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        setAdapter()

        binding?.fabListAddStory?.setOnClickListener {
            parentFragmentManager.commit {
                addToBackStack(null)
                replace<AddStoryFragment>(R.id.fcv_story_fragmentContainer)
            }
        }

        binding?.srlListSwipeRefreshLayout?.setOnRefreshListener {
            adapter.refresh()
            binding?.srlListSwipeRefreshLayout?.isRefreshing = false
        }

        parentFragmentManager.setFragmentResultListener(
            AddStoryFragment.ADD_STORY_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            adapter.refresh()
            var isAddStoryFinished = result.getBoolean(AddStoryFragment.ADD_STORY_FINISHED)
            lifecycleScope.launch {
                adapter.loadStateFlow
                    .onStart { delay(500L) }
                    .takeWhile { isAddStoryFinished }
                    .collectLatest {
                        binding?.rvListRecyclerView?.smoothScrollToPosition(0)
                        isAddStoryFinished = false
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setAdapter() {
        val callback = object : ListStoryAdapter.ListStoryAdapterCallback {
            override fun setOnClick(item: StoryListResponse, imageView: ImageView) {
                viewModel.setCurrentStory(item)
                parentFragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack(null)
                    addSharedElement(imageView, imageView.transitionName)
                    replace<DetailStoryFragment>(R.id.fcv_story_fragmentContainer)
                }
            }
        }
        adapter = ListStoryAdapter(StoryComparator, callback)
        binding?.rvListRecyclerView?.layoutManager = LinearLayoutManager(context)
        binding?.rvListRecyclerView?.adapter = adapter

        lifecycleScope.launch {
            viewModel.getStoriesFlow().collect { story ->
                adapter.submitData(story)
            }
        }

        (view as ViewGroup).doOnPreDraw {
            startPostponedEnterTransition()
        }
    }
}