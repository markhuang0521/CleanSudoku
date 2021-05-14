package com.example.cleansodoku.game

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.cleansodoku.R
import com.example.cleansodoku.databinding.FragmentGameBinding
import com.example.cleansodoku.utils.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.android.synthetic.main.fragment_game.*
import org.koin.android.ext.android.inject
import timber.log.Timber


class GameBoardFragment : Fragment(), SudokuBoardView.OnTouchListener {
    private val viewModel: SudokuViewModel by inject()
    private lateinit var binding: FragmentGameBinding
    private lateinit var gameTimer: Chronometer
    var hintAd: RewardedAd? = null
    private lateinit var adView: AdView


    private var acutalMistake = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game, container, false)
        binding.viewmodel = viewModel

        showToolbar()
        setToolbarTitle()
        removeBottomNav()
        setDisplayHomeAsUpEnabled(true)
        // ads


        RewardedAd.load(
            requireActivity(),
            "ca-app-pub-3940256099942544/5224354917",
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Timber.d("Ad was onAdFailedToLoad.")

                    hintAd = null
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Timber.d("Ad was onAdLoaded.")

                    hintAd = rewardedAd
                }
            })

        hintAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Timber.d("Ad was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Timber.d("Ad failed to show.")

            }

            override fun onAdShowedFullScreenContent() {
                Timber.d("Ad  to show full screen.")
                // Called when ad is dismissed.
                // Don't set the ad reference to null to avoid showing the ad a second time.
                hintAd = null
            }
        }
//        loadBannerAd()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setGameTimer()
        sudokuBoardView.setBoardTouchListener(this)
        binding.lifecycleOwner = this
        setUpObservers()
    }

//    private fun loadBannerAd() {
//
//        val display = requireActivity().windowManager.defaultDisplay
//        val outMetrics = DisplayMetrics()
//        display.getMetrics(outMetrics)
//
//        val density = outMetrics.density
//
//        var adWidthPixels = binding.bannerAd.width.toFloat()
//        if (adWidthPixels == 0f) {
//            adWidthPixels = outMetrics.widthPixels.toFloat()
//        }
//
//        val adWidth = (adWidthPixels / density).toInt()
//        val adSize =
//            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(requireContext(), adWidth);
//
//
//        adView = AdView(requireContext())
//        adView.adSize = adSize
//        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
//        binding.bannerAd.addView(adView)
//
//
//        val adRequest = AdRequest.Builder().build()
//        adView.loadAd(adRequest)
//
//
//    }

    @SuppressLint("SetTextI18n")
    private fun setUpObservers() {
        viewModel.selectedCell.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.sudokuBoardView.updateSelectedCellUI(it)
            }
        })

        viewModel.timer.observe(viewLifecycleOwner, Observer {

        })

        viewModel.mistakes.observe(viewLifecycleOwner, Observer {
            it?.let {
                acutalMistake++
                if (viewModel.gameOver()) {
                    val builder = AlertDialog.Builder(requireContext())
                        .setTitle("Game Over")
                        .setMessage("Opps!, you got 3 strikes!")
                        .setPositiveButton("Second Chance") { dialog, i ->
                            //show add
                            viewModel.mistakes.value = it - 1

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
                if (viewModel.moreHints()) {
                    binding.btnHint.text = getString(R.string.btn_hint_ads)
                    if (hintAd != null) {
                        hintAd?.show(requireActivity()) { rewardItem ->
                            Timber.d("User  getting  the reward.")

                            viewModel.hints.value = hint + rewardItem.amount
                        }
                    } else {
                        Timber.d("User not getting  the reward.")
                    }
                } else {
                    binding.btnHint.text = "Hints: $hint"

                }
            }
        })

        viewModel.gameBoard.observe(viewLifecycleOwner, Observer {
            it?.let {
                // check if board is completed
                // update board view with user input and check is current cell correct

                val isCorrect = viewModel.isSelectedCellCorrect()
                binding.sudokuBoardView.updateBoard(it, isCorrect)
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

    }


    override fun onResume() {
        super.onResume()
        gameTimer.base = SystemClock.elapsedRealtime() - viewModel.timer.value!!;

        gameTimer.start()


    }


//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            android.R.id.home -> {
//                (nav_host_fragment as NavHostFragment).navController.popBackStack()
//            }
//            R.id.menu_theme -> {
//
//            }
//
//            R.id.menu_setting -> {
//            }
//
//
//        }
//        return super.onOptionsItemSelected(item)
//    }


}