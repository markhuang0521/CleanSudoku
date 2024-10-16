package com.cleanSudoku.cleansodoku.game

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.widget.Chronometer
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.cleanSudoku.cleansodoku.BuildConfig
import com.cleanSudoku.cleansodoku.R
import com.cleanSudoku.cleansodoku.databinding.FragmentGameBinding
import com.cleanSudoku.cleansodoku.settings.Setting
import com.cleanSudoku.cleansodoku.util.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.android.synthetic.main.fragment_game.*
import org.koin.android.ext.android.inject
import timber.log.Timber


class GameBoardFragment : Fragment(), SudokuBoardView.OnTouchListener {
    private val viewModel: SudokuViewModel by inject()
    private val setting: Setting by inject()
    private lateinit var binding: FragmentGameBinding
    private lateinit var gameTimer: Chronometer
    var rewardAd: RewardedAd? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentGameBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        setToolbarTitle("")
        showToolbar()
        removeBottomNav()
        setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        // ads


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MobileAds.initialize(requireActivity()) {
            if (activity != null) {
                loadRewardAd()

            }
        }
        setGameTimer()
        sudokuBoardView.setBoardTouchListener(this)
        binding.lifecycleOwner = this
        setUpObservers()
    }

    private fun loadRewardAd() {

        val adRequest = AdRequest.Builder().build()
        if (rewardAd == null) {
            RewardedAd.load(
                requireActivity(),
                BuildConfig.ad_hint_rewarded_id,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Timber.d("Ad was onAdFailedToLoad. $adError")

                        rewardAd = null
                    }

                    override fun onAdLoaded(rewardedAd: RewardedAd) {
                        Timber.d("Ad was onAdLoaded.")

                        rewardAd = rewardedAd
                        rewardAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Timber.d("Ad was dismissed.")
                                rewardAd = null
                                loadRewardAd()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                                Timber.d("Ad failed to show.")
                                rewardAd = null
                            }

                            override fun onAdShowedFullScreenContent() {
                                Timber.d("Ad  to show full screen.")
                                rewardAd = null

                            }
                        }
                    }
                })
        }

    }

    private fun showRewardAd(rewardType: ReWardType) {
        if (rewardAd != null) {

            rewardAd?.show(requireActivity()) {
                Timber.d("amount: ${it.amount}  type: ${it.type}")
                when (rewardType) {
                    ReWardType.Hint -> {
                        viewModel.hints.value = viewModel.hints.value?.plus(2)
                    }
                    ReWardType.Mistake -> {
                        viewModel.mistakes.value = viewModel.mistakes.value?.minus(1)
                    }
                }


            }
        } else {
            Timber.d("ad never begin")

        }

    }


    @Suppress("UNUSED_VARIABLE")
    @SuppressLint("SetTextI18n")
    private fun setUpObservers() {
        viewModel.selectedCell.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.sudokuBoardView.updateSelectedCellUI(it)
            }
        })

        viewModel.mistakes.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it >= viewModel.mistakeLimit) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Game Over")
                        .setMessage("Oops, you Got 10 Strikes!")
                        .setPositiveButton("Second Chance (Ad)") { dialog, i ->
                            //show add
                            showRewardAd(ReWardType.Mistake)

                        }
                        .setNeutralButton("New Game") { dialogInterface, i ->
                            // save game

                            showDifficultyDialogAndStartNewGame(
                                viewModel,
                                FragmentTag.GameBoard.name
                            )
                        }

                        .setCancelable(false)
                        .show()
                }
            }
        })

        viewModel.hints.observe(viewLifecycleOwner, Observer {
            it?.let { hint ->
                when {
                    it == 0 -> {
                        binding.btnHint.text = getString(R.string.btn_hint_ads)

                    }
                    viewModel.moreHints() -> {
                        binding.btnHint.text = getString(R.string.btn_hint_ads)
                        showRewardAd(ReWardType.Hint)
                    }
                    else -> {
                        binding.btnHint.text = "Hints: $hint"

                    }
                }

            }
        })

        viewModel.timer.observe(viewLifecycleOwner, Observer {
            viewModel.timer.value?.let {
//                gameTimer.text = it.formatToTimeString()
            }
        })

        viewModel.gameBoard.observe(viewLifecycleOwner, Observer {
            it?.let {
                // check if board is completed
                // update board view with user input and check is current cell correct
                val isCorrect = viewModel.isSelectedCellCorrect()

                binding.sudokuBoardView.updateBoard(it, viewModel.solutionBoard.value)
                if (viewModel.isBoardCompleted()) {
                    viewModel.setTimer(SystemClock.elapsedRealtime() - gameTimer.base)

                    viewModel.updateCurrentGame(isCompleted = true, isSucceed = true)

                    findNavController().navigate(GameBoardFragmentDirections.actionGameFragmentToGameCompleteFragment())
                } else {


                    viewModel.updateCurrentGame(isCompleted = false, isSucceed = false)

                }


            }
        })

    }


    override fun onCellTouched(row: Int, col: Int) {
        viewModel.updateSelectedCell(row, col)
    }

    private fun setGameTimer() {
        gameTimer = binding.gameTimer

    }


    override fun onStop() {
        super.onStop()
        gameTimer.stop()
        viewModel.setTimer(SystemClock.elapsedRealtime() - gameTimer.base)
//        viewModel.updateCurrentGame(false, false)

    }


    override fun onResume() {
        super.onResume()
        gameTimer.base = SystemClock.elapsedRealtime() - viewModel.timer.value!!
        gameTimer.start()

        if (setting.timer) {
            gameTimer.visibility = View.VISIBLE
        } else {
            gameTimer.visibility = View.GONE

        }


    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.mediaPlayer?.release()
        viewModel.mediaPlayer = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.game_menu, menu)
    }

    //
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {


            R.id.settingsFragment -> {
                findNavController().navigate(GameBoardFragmentDirections.actionGameFragmentToSettingsFragment())
            }

        }
        return super.onOptionsItemSelected(item)
    }


}