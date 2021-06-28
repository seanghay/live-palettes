package com.seanghay.livepalettes

import android.graphics.*
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.palette.graphics.Palette
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.*


class PalettesAnalyzer(
 private val onPaletteGenerated: (Palette) -> Unit
): ImageAnalysis.Analyzer {

  private val queue: Queue<Runnable> = LinkedList()

  private var delta = 0.0
  private var updates = 0
  private var lastTime = System.nanoTime()
  private var timer = System.currentTimeMillis()
  private var frames = 0

  var frameInfo: (String) -> Unit = {}
  var ns = 1000000000.0 / 1

  override fun analyze(image: ImageProxy) {
    while (queue.isNotEmpty()) {
      queue.poll()?.run()
    }

    val now = System.nanoTime()
    delta += (now - lastTime) / ns
    lastTime = now;
    if (delta >= 1.0) {
      doFrame(image)
      updates++
      delta--
    }
    frames++;

    if (System.currentTimeMillis() - timer > 1000) {
      timer += 1000
      frameInfo("Frame Info: $updates update(s)/s, $frames frame(s)/s")
      updates = 0
      frames = 0
    }

    image.close()
  }

  private fun doFrame(image: ImageProxy) {
    val bitmap = image.toBitmap()
    val palette = Palette.from(bitmap).generate()
    onPaletteGenerated(palette)
    bitmap.recycle()
  }

  fun setComputeRate(value: Float) {
    queue.add(Runnable {
      ns = 1000000000.0 / (value * 30).coerceAtLeast(1f)
    })
  }

  companion object {


    private fun ImageProxy.toBitmap(): Bitmap {

      val yBuffer = planes[0].buffer // Y
      val uBuffer = planes[1].buffer // U
      val vBuffer = planes[2].buffer // V

      val ySize = yBuffer.remaining()
      val uSize = uBuffer.remaining()
      val vSize = vBuffer.remaining()

      val nv21 = ByteArray(ySize + uSize + vSize)

      yBuffer.get(nv21, 0, ySize)
      vBuffer.get(nv21, ySize, vSize)
      uBuffer.get(nv21, ySize + vSize, uSize)

      val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
      val out = ByteArrayOutputStream()
      yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
      val imageBytes = out.toByteArray()
      return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
      rewind()    // Rewind the buffer to zero
      val data = ByteArray(remaining())
      get(data)   // Copy the buffer into a byte array
      return data // Return the byte array
    }
  }
}