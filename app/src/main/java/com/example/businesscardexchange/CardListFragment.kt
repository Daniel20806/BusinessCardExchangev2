package com.example.businesscardexchange

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class CardListFragment : Fragment() {

    private var cardDataList = mutableListOf<CardData>()
    private lateinit var adapter: CardListAdapter
    private lateinit var sharedPreferences: SharedPreferences

    /*讀取已經儲存名片*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("card_data", Context.MODE_PRIVATE)
        loadCardData()
    }



    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_card_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)

        /*顯示畫面*/
        adapter = CardListAdapter(cardDataList, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        return view
    }

    /*新增卡片方法，可解析 OR Code 中的 Json*/
    @OptIn(ExperimentalEncodingApi::class)
    fun addCardData(jsonData: String) {
        try {
            val jsonObject = JSONObject(jsonData)
            val name = jsonObject.optString("name", "")
            val company = jsonObject.optString("company", "")
            val phone = jsonObject.optString("phone", "")
            val email = jsonObject.optString("email", "")
            val photo = jsonObject.optString("photo", "")

            val cardData = CardData(name, company, phone, email, photo)
            cardDataList.add(cardData)
            adapter.notifyItemInserted(cardDataList.size - 1)

            saveCardData() // 保存資料到 SharedPreferences
        } catch (e: JSONException) {
            // 處理 JSON 解析錯誤
        }
    }

    /*移除卡片方法*/
    fun removeCardData(position: Int) {
        cardDataList.removeAt(position)
        adapter.notifyItemRemoved(position)
        saveCardData()
    }

    /*編輯卡片方法*/
    fun editCardData(position: Int) {
        val cardData = cardDataList[position]

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("編輯名片")

        // 创建 TextInputLayout 和 EditText
        val nameInputLayout = TextInputLayout(requireContext())
        val nameEditText = EditText(requireContext())
        nameInputLayout.addView(nameEditText)
        nameInputLayout.hint = "姓名"
        nameEditText.setText(cardData.name)

        val companyInputLayout = TextInputLayout(requireContext())
        val companyEditText = EditText(requireContext())
        companyInputLayout.addView(companyEditText)
        companyInputLayout.hint = "公司"
        companyEditText.setText(cardData.company)

        val phoneInputLayout = TextInputLayout(requireContext())
        val phoneEditText = EditText(requireContext())
        phoneInputLayout.addView(phoneEditText)
        phoneInputLayout.hint = "電話"
        phoneEditText.setText(cardData.phone)

        val emailInputLayout = TextInputLayout(requireContext())
        val emailEditText = EditText(requireContext())
        emailInputLayout.addView(emailEditText)
        emailInputLayout.hint = "信箱"
        emailEditText.setText(cardData.email)

        // 将 TextInputLayout 添加到 AlertDialog 中
        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.addView(nameInputLayout)
        linearLayout.addView(companyInputLayout)
        linearLayout.addView(phoneInputLayout)
        linearLayout.addView(emailInputLayout)
        builder.setView(linearLayout)

        builder.setPositiveButton("確定") { dialog, which ->
            val updatedCardData = CardData(
                nameEditText.text.toString(),
                companyEditText.text.toString(),
                phoneEditText.text.toString(),
                emailEditText.text.toString(),
                cardData.photo
            )
            cardDataList[position] = updatedCardData
            adapter.notifyItemChanged(position)
            saveCardData()
        }

        builder.setNegativeButton("取消", null)

        builder.show()
    }

    /*保存卡片方法*/
    private fun saveCardData() {
        val editor = sharedPreferences.edit()
        editor.putString("card_data_list", cardDataList.toJsonString()) // 自定义方法，将 cardDataList 转换为 JSON 字符串
        editor.apply()
    }


    /*加載卡片方法*/
    private fun loadCardData() {
        val jsonString = sharedPreferences.getString("card_data_list", null)
        if (jsonString != null) {
            cardDataList = fromJsonString(jsonString) // 自定义方法，将 JSON 字符串转换为 cardDataList
        } else {
            // 如果没有保存的数据，则使用默认数据
            cardDataList = mutableListOf(
                CardData("測試人員", "嘉義股份有限公司", "123-456-7890", "Chiayi.@ncyu.com", ""),
            )
        }
    }

    // 自定義方法，將 cardDataList 轉換爲 JSON 字符串
    private fun MutableList<CardData>.toJsonString(): String {
        val jsonArray = org.json.JSONArray()
        for (cardData in this) {
            val jsonObject = JSONObject()
            jsonObject.put("name", cardData.name)
            jsonObject.put("company", cardData.company)
            jsonObject.put("phone", cardData.phone)
            jsonObject.put("email", cardData.email)
            jsonObject.put("photo", cardData.photo)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    // 自定義方法，將 JSON 字符串轉換爲 cardDataList
    private fun fromJsonString(jsonString: String): MutableList<CardData> {
        val cardDataList = mutableListOf<CardData>()
        val jsonArray = org.json.JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val name = jsonObject.optString("name", "")
            val company = jsonObject.optString("company", "")
            val phone = jsonObject.optString("phone", "")
            val email = jsonObject.optString("email", "")
            val photo = jsonObject.optString("photo", "")
            cardDataList.add(CardData(name, company, phone, email, photo))
        }
        return cardDataList
    }

    companion object {
    }
}