package com.example.businesscardexchange

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class ScanActivityResultContract : ActivityResultContract<Unit, String?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(context, ScanActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode == Activity.RESULT_OK && intent != null) {
            return intent.getStringExtra("scan_result")
        }
        return null
    }
}