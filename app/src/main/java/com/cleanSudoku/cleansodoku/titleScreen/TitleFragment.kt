package com.cleanSudoku.cleansodoku.titleScreen

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.cleanSudoku.cleansodoku.R
import com.cleanSudoku.cleansodoku.databinding.FragmentTitleBinding
import com.cleanSudoku.cleansodoku.game.SudokuViewModel
import com.cleanSudoku.cleansodoku.util.*
import org.koin.android.ext.android.inject


class TitleFragment : Fragment() {
    private lateinit var binding: FragmentTitleBinding
    private val viewModel: SudokuViewModel by inject()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTitleBinding.inflate(inflater, container, false)
        showBottomNav()
//        setToolbar(binding.toolbar)
        setToolbarTitle("Clean Sudoku")

        setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.btnContinueGame.setOnClickListener {
            findNavController().navigate(TitleFragmentDirections.actionTitleFragmentToGameFragment())
        }
        if (viewModel.gameId.value == null) {
            viewModel.loadGame()
        }

        viewModel.gameId.observe(viewLifecycleOwner, Observer {
            viewModel.gameId.value?.let {
                binding.btnContinueGame.visibility = View.VISIBLE
            }
        })

        binding.btnNewGame.setOnClickListener {
            showDifficultyDialogAndStartNewGame(viewModel, FragmentTag.Title.name)

        }
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