package com.cleanSudoku.cleansodoku.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cleanSudoku.cleansodoku.databinding.ItemStaticsBinding
import com.cleanSudoku.cleansodoku.game.SudokuViewModel
import com.cleanSudoku.cleansodoku.util.Difficulty
import org.koin.android.ext.android.inject

private const val ARG_DIFFICULTY: String = "difficulty"


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
    }


}

