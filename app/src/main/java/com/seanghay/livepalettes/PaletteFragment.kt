package com.seanghay.livepalettes

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.seanghay.livepalettes.databinding.FragmentPaletteBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class PaletteFragment : Fragment() {

  @field:ViewScopeOnly
  private var _binding: FragmentPaletteBinding? = null

  private val binding: FragmentPaletteBinding get() = _binding!!

  @field:ViewScopeOnly
  private var _analysisExecutor: ExecutorService? = null

  private val analysisExecutor get() = _analysisExecutor!!

  private val cameraLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isCameraPermissionGranted ->
      if (isCameraPermissionGranted) {
        prepareCamera()
      } else {
        Toast.makeText(requireContext(), "Camera permission is denied!", Toast.LENGTH_SHORT).show()
      }
    }

  @SuppressLint("SetTextI18n")
  private fun prepareCamera() {
    _analysisExecutor = Executors.newSingleThreadExecutor()
    val processCameraProvider = ProcessCameraProvider.getInstance(requireContext())
    val runnable = Runnable {
      val provider = processCameraProvider.get()
      val preview = Preview.Builder()
        .build()

      preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
      val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
      val imageAnalysis = ImageAnalysis.Builder().build()
      val analyzer = PalettesAnalyzer { palette ->
        val vibrant = palette.vibrantSwatch?.rgb
        val vibrantText = palette.vibrantSwatch?.titleTextColor

        val muted = palette.mutedSwatch?.rgb
        val mutedText = palette.mutedSwatch?.titleTextColor

        val dominant = palette.dominantSwatch?.rgb
        val dominantText = palette.dominantSwatch?.titleTextColor

        requireActivity().runOnUiThread {

          if (vibrant != null) {
            binding.viewColor.setBackgroundColor(vibrant)
            binding.textViewColor.setTextColor(vibrantText ?: Color.WHITE)
            binding.textViewColor.text = "#${Integer.toHexString(0xFFFFFF and vibrant)}"
          }

          if (muted != null) {
            binding.viewColor2.setBackgroundColor(muted)
            binding.textViewColor.setTextColor(mutedText ?: Color.WHITE)
            binding.textViewColor2.text = "#${Integer.toHexString(0xFFFFFF and muted )}"
          }

          if (dominant != null) {
            binding.viewColor3.setBackgroundColor(dominant)
            binding.textViewColor.setTextColor(dominantText ?: Color.WHITE)
            binding.textViewColor3.text = "#${Integer.toHexString(0xFFFFFF and dominant)}"          }

        }

      }

      analyzer.frameInfo = { info ->
        Log.d(TAG, "info=$info")
        requireActivity().runOnUiThread {
          binding.textViewFrameInfo.text = info
        }
      }

      binding.slider.setLabelFormatter { value ->
         (value * 30).coerceAtLeast(1f).roundToInt().toString()
      }
      binding.slider.addOnChangeListener { slider, value, fromUser ->
        analyzer.setComputeRate(value)
      }

      imageAnalysis.setAnalyzer(analysisExecutor, analyzer)

      val result = runCatching {
        provider.unbindAll()
        provider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalysis)
      }

      if (result.isFailure) {
        Toast.makeText(
          requireContext(),
          result.exceptionOrNull()?.localizedMessage,
          Toast.LENGTH_SHORT
        ).show()
      }

    }
    processCameraProvider.addListener(
      runnable, ContextCompat.getMainExecutor(requireContext())
    )
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentPaletteBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    cameraLauncher.launch(
      android.Manifest.permission.CAMERA
    )
  }


  companion object {

    internal const val TAG = "PaletteFragment"

    fun newInstance(): PaletteFragment {
      return PaletteFragment()
    }
  }
}