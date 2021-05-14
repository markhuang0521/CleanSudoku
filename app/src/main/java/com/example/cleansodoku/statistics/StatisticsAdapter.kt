package com.example.cleansodoku.statistics

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.cleansodoku.databinding.ItemStaticsBinding
import com.example.cleansodoku.game.SudokuViewModel
import com.example.cleansodoku.utils.Difficulty
import org.koin.android.ext.android.inject


private const val ARG_DIFFICULTY = "difficulty"

data class GameStatistics(
    val totalGame: String?,
    val totalWin: String?,
    val winRate: String?,
    val bestTime: String?,
    val avgTime: String?
)

class StatisticsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {


    private val difficultyArray =
        arrayOf(Difficulty.Easy, Difficulty.Medium, Difficulty.Hard, Difficulty.Insane)

    override fun getItemCount(): Int {
        return difficultyArray.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = StatisticsFragment()

        fragment.arguments = Bundle().apply {
            // Our object is just an integer :-P
            putString(ARG_DIFFICULTY, difficultyArray[position].name)
        }
        return fragment
    }


}


class StatisticsFragment : Fragment() {

    private lateinit var binding: ItemStaticsBinding
    private val viewModel: SudokuViewModel by inject()
    private lateinit var currentStatistics: GameStatistics


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ItemStaticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel


        arguments?.takeIf { it.containsKey(ARG_DIFFICULTY) }?.apply {
            getString(ARG_DIFFICULTY)?.let { difficulty ->


                viewModel.getGameStatistic(difficulty)
//                Log.d("TAG", "ARG_DIFFICULTY: ${it.name} ")

                viewModel.gameStatistic.observe(viewLifecycleOwner, Observer { statistic ->
                    binding.statistics = statistic
                    Log.d(
                        "TAG",
                        "${difficulty} game stat: ${viewModel.gameStatistic.value.toString()} "
                    )

                })


            }


        }
    }


}

