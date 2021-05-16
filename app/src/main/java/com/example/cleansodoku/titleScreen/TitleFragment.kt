package com.example.cleansodoku.titleScreen

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.cleansodoku.R
import com.example.cleansodoku.databinding.FragmentTitleBinding
import com.example.cleansodoku.game.SudokuViewModel
import com.example.cleansodoku.utils.*
import org.koin.android.ext.android.inject


class TitleFragment : Fragment() {
    private lateinit var binding: FragmentTitleBinding
    private val viewModel: SudokuViewModel by inject()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentTitleBinding.inflate(inflater, container, false)
        showBottomNav()
//        setToolbar(binding.toolbar)
        showToolbar()
        setToolbarTitle("Clean Sudoku")

        setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.loadGame()
        viewModel.gameBoard.observe(viewLifecycleOwner, Observer {
            viewModel.gameBoard.value?.let {
                binding.btnContinueGame.visibility = View.VISIBLE
                binding.btnContinueGame.setOnClickListener {
                    findNavController().navigate(TitleFragmentDirections.actionTitleFragmentToGameFragment())
                }
            }
        })
        binding.btnNewGame.setOnClickListener {
            showDifficultyDialogAndStartNewGame(viewModel, FragmentTag.Title.name)

        }
    }


    private fun showContinueGame(binding: FragmentTitleBinding) {

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.game_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.settingsFragment -> {
                findNavController().navigate(TitleFragmentDirections.actionTitleFragmentToSettingsFragment())

            }
        }
        return super.onOptionsItemSelected(item)
    }


}