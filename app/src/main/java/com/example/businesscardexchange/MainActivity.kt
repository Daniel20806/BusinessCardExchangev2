package com.example.businesscardexchange

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.delay
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    // 用於請求權限的 launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // 權限被授予
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // 權限被拒絕
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    /*處理掃描結果*/
    private val scanLauncher = registerForActivityResult(ScanActivityResultContract()) { scanResult ->
        if (scanResult != null) {
            try {
                val jsonObject = JSONObject(scanResult)

                // 从 jsonObject 中提取名片信息
                jsonObject.optString("name", "")
                jsonObject.optString("company", "")
                jsonObject.optString("phone", "")
                jsonObject.optString("email", "")
                jsonObject.optString("photo", "")

                //Toast.makeText(this, jsonObject.optString("photo", "").toString(), Toast.LENGTH_LONG).show()
                // 使用 Toast 顯示名片訊息
                /*val toastMessage = "姓名: $name\n公司: $company\n电话: $phone\n邮箱: $email"
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()*/
                Toast.makeText(this, "Add Success!", Toast.LENGTH_SHORT).show()
                // 調用 addCardData() 方法
                cardListFragment.addCardData(scanResult)

            } catch (e: JSONException) {
                Toast.makeText(this, "JSON 解析錯誤！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private lateinit var cardListFragment: CardListFragment
    private lateinit var cardEditFragment: CardEditFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 檢查相機權限
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            // 如果權限已授予，執行相關操作
            //Toast.makeText(this, "Camera permission already granted", Toast.LENGTH_SHORT).show()
        } else {
            // 否則，請求權限
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)

        }


        cardListFragment = CardListFragment()
        cardEditFragment = CardEditFragment()

        /*當點擊底欄時，切換Fragment*/
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                /*左Fragment*/
                R.id.navigation_card_list -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, cardListFragment)
                        .commit()
                    true
                }

                /*右Fragment*/
                R.id.navigation_my_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, cardEditFragment)
                        .commit()
                    true
                }

                else -> false
            }
        }

        // 默認顯示 CardListFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, cardListFragment)
            .commit()

        val scanButton = findViewById<FloatingActionButton>(R.id.fab_add) // 使用 FloatingActionButton 类型
        scanButton.setOnClickListener {
            scanLauncher.launch(Unit)
        }
    }

    private fun checkPhoto() {
        // 檢查相片權限
        if (ContextCompat.checkSelfPermission(
                this,
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
