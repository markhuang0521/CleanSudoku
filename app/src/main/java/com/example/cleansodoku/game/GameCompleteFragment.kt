package com.example.cleansodoku.game

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cleansodoku.databinding.FragmentGameCompleteBinding
import com.example.cleansodoku.utils.*
import org.koin.android.ext.android.inject


class GameCompleteFragment : Fragment() {

    private val viewModel: SudokuViewModel by inject()
    private lateinit var binding: FragmentGameCompleteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGameCompleteBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel

        removeToolbar()
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