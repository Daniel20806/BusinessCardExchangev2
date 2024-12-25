package com.example.businesscardexchange

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import android.util.Base64
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream


class CardEditFragment : Fragment() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // 權限被授予
                //Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // 權限被拒絕
                //Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    /*資料格式定義*/
    private lateinit var editName: EditText
    private lateinit var editCompany: EditText
    private lateinit var editPhone: EditText
    private lateinit var editEmail: EditText
    private lateinit var qrCodeImageView: ImageView
    private lateinit var cardImageView: ImageView  // 顯示選擇的照片
    private var selectedImageUri: Uri? = null // 儲存選擇的照片URI
    private lateinit var selectPhotoButton: Button
    private var editPhoto: String = ""

    private val PICK_IMAGE_REQUEST = 1  // 請求代碼

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_card_edit, container, false)

        /*findViewId 獲取元件 */
        editName = view.findViewById(R.id.edit_name)
        editCompany = view.findViewById(R.id.edit_company)
        editPhone = view.findViewById(R.id.edit_phone)
        editEmail = view.findViewById(R.id.edit_email)
        qrCodeImageView = view.findViewById(R.id.qr_code_image)
        cardImageView = view.findViewById(R.id.photo_image_view) // 顯示選擇的名片照片
        selectPhotoButton = view.findViewById(R.id.select_photo_button)




        // 压缩图片并转换为 Base64
        fun compressImage(uri: Uri): String {
            val imageBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)

            // 计算缩放比例
            val maxWidth = 100 // 设置最大宽度
            val maxHeight = 100 // 设置最大高度
            val width = imageBitmap.width
            val height = imageBitmap.height
            val scaleFactor = Math.min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)

            // 缩放图片
            val scaledBitmap = Bitmap.createScaledBitmap(
                imageBitmap,
                (width * scaleFactor).toInt(),
                (height * scaleFactor).toInt(),
                false
            )

            val byteArrayOutputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 1, byteArrayOutputStream) // 使用 JPEG 格式压缩，质量为 1
            val byteArray = byteArrayOutputStream.toByteArray()
            val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)

            return base64Image
        }

        // 定义 ActivityResultLauncher
        val pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                // 处理选择的图片 Uri
                // 压缩图片并转换为 Base64
                val base64Image = compressImage(uri)

                // 显示 Toast 消息
                Toast.makeText(requireContext(), "選擇成功", Toast.LENGTH_SHORT).show()

                // 将 Base64 数据保存到 editPhoto 中
                editPhoto = base64Image
                // 将图片显示在 CardView 中的 ImageView 中
                val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                cardImageView.setImageBitmap(bitmap)
            } else {
                // 用户未选择图片
                Toast.makeText(requireContext(), "選擇失敗", Toast.LENGTH_SHORT).show()
            }
        }

// selectPhotoButton 点击事件处理程序
        selectPhotoButton.setOnClickListener {
            //checkPhoto()
            // 直接选择图片
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        view.findViewById<Button>(R.id.request_photos_permission_button).setOnClickListener {
            checkPhoto()
        }



        /*保存按鈕*/
        val saveButton = view.findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener {
            val name = editName.text.toString()
            val company = editCompany.text.toString()
            val phone = editPhone.text.toString()
            val email = editEmail.text.toString()

            val sharedPreferences = requireActivity().getSharedPreferences("card_data", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("name", name)
            editor.putString("company", company)
            editor.putString("phone", phone)
            editor.putString("email", email)
            editor.putString("photo", editPhoto ?: "")


            editor.apply()

            Toast.makeText(requireContext(), "名片已保存", Toast.LENGTH_SHORT).show()
        }

        /*刪除按鈕*/
        val deleteButton = view.findViewById<Button>(R.id.delete_button)
        deleteButton.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("card_data", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.clear()
            editor.apply()

            Toast.makeText(requireContext(), "名片已刪除", Toast.LENGTH_SHORT).show()

            // 清空視圖
            editName.setText("")
            editCompany.setText("")
            editPhone.setText("")
            editEmail.setText("")
            editPhoto = ""
            qrCodeImageView.setImageBitmap(null)
            cardImageView.setImageBitmap(null)  // 清空名片照片
            cardImageView.setImageResource(R.drawable.ic_my_profile)
        }

        /*查看自己名片按鈕*/
        val statusButton = view.findViewById<Button>(R.id.status_button)
        statusButton.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("card_data", Context.MODE_PRIVATE)
            val name = sharedPreferences.getString("name", "")
            val company = sharedPreferences.getString("company", "")
            val phone = sharedPreferences.getString("phone", "")
            val email = sharedPreferences.getString("email", "")

            val cardContent = """
                姓名：$name
                公司：$company
                電話：$phone
                信箱：$email
            """.trimIndent()

            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setTitle("名片內容")
            builder.setMessage(cardContent)
            builder.setPositiveButton("確定", null)

            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.alert_dialog_background)
            dialog.show()
        }

        /*獲取 QR Code 按鈕*/
        val generateQrCodeButton = view.findViewById<Button>(R.id.generate_qr_code_button)
        generateQrCodeButton.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("card_data", Context.MODE_PRIVATE)
            val name = sharedPreferences.getString("name", "")
            val company = sharedPreferences.getString("company", "")
            val phone = sharedPreferences.getString("phone", "")
            val email = sharedPreferences.getString("email", "")

            val cardData = JSONObject()
            cardData.put("name", name)
            cardData.put("company", company)
            cardData.put("phone", phone)
            cardData.put("email", email)
            cardData.put("photo", editPhoto)

            Toast.makeText(requireContext(), editPhoto, Toast.LENGTH_SHORT).show()

            val jsonCardData = cardData.toString()

            try {
                val bitMatrix = MultiFormatWriter().encode(jsonCardData, BarcodeFormat.QR_CODE, 500, 500)
                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                qrCodeImageView.setImageBitmap(bitmap)

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val byteArray = baos.toByteArray()
                val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)

                val editor = sharedPreferences.edit()
                editor.putString("qr_code_image", encodedImage)
                editor.apply()
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }

        return view
    }

    /*開啓app以後默認顯示自己名片内容以及 QR Code*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("card_data", Context.MODE_PRIVATE)
        editName.setText(sharedPreferences.getString("name", ""))
        editCompany.setText(sharedPreferences.getString("company", ""))
        editPhone.setText(sharedPreferences.getString("phone", ""))
        editEmail.setText(sharedPreferences.getString("email", ""))
        editPhoto = sharedPreferences.getString("photo", "") ?: ""

    }

    /*避免因切換底欄造成資料丟失*/
    override fun onResume() {
        super.onResume()

        val sharedPreferences = requireActivity().getSharedPreferences("card_data", Context.MODE_PRIVATE)
        val encodedImage = sharedPreferences.getString("qr_code_image", null)
        if (encodedImage != null) {
            val decodedString = Base64.decode(encodedImage, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            qrCodeImageView.setImageBitmap(decodedByte)
        }
    }
    private fun checkPhoto() {
        // 檢查相片權限
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            // 如果相片權限已授予，執行相關操作
            //Toast.makeText(this, "Photos permission already granted", Toast.LENGTH_SHORT).show()
        } else {
            // 否則，請求相片權限
            requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

}