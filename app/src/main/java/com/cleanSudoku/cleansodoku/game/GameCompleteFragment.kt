package com.cleanSudoku.cleansodoku.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cleanSudoku.cleansodoku.databinding.FragmentGameCompleteBinding
import com.cleanSudoku.cleansodoku.util.*
import org.koin.android.ext.android.inject


class GameCompleteFragment : Fragment() {

    private val viewModel: SudokuViewModel by inject()
    private lateinit var binding: FragmentGameCompleteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentGameCompleteBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        setToolbarTitle()
        setDisplayHomeAsUpEnabled(false)

        removeBottomNav()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.btnHome.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnNewGame.setOnClickListener {
            showDifficultyDialogAndStartNewGame(viewModel, FragmentTag.GameComplete.name)

        }


    }

}