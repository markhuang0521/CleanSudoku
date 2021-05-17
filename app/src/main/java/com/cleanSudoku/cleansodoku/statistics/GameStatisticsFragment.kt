package com.cleanSudoku.cleansodoku.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cleanSudoku.cleansodoku.databinding.FragmentGameStatBinding
import com.cleanSudoku.cleansodoku.game.SudokuViewModel
import com.cleanSudoku.cleansodoku.utils.Difficulty
import com.cleanSudoku.cleansodoku.utils.setToolbarTitle
import com.cleanSudoku.cleansodoku.utils.showBottomNav
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.android.ext.android.inject


class GameStatisticsFragment : Fragment() {
    private lateinit var binding: FragmentGameStatBinding
    private lateinit var statisticsAdapter: StatisticsAdapter
    private val viewModel: SudokuViewModel by inject()

    private val difficultyArray =
        arrayOf(Difficulty.Easy, Difficulty.Medium, Difficulty.Hard, Difficulty.Insane)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGameStatBinding.inflate(inflater, container, false)
        setToolbarTitle("Statistics")
        showBottomNav()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        statisticsAdapter = StatisticsAdapter(this)
        binding.viewPagerStatics.adapter = statisticsAdapter

        TabLayoutMediator(binding.tabLayoutStatics, binding.viewPagerStatics) { tab, position ->
            tab.text = difficultyArray[position].name
        }.attach()

        binding.tabLayoutStatics.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
                viewModel.getGameStatistic(tab.text.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                viewModel.gameStatistic.value = null
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.getGameStatistic(tab.text.toString())
            }

        })
    }


}