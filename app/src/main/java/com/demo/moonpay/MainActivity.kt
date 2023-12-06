package com.demo.moonpay

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.demo.moonpay.databinding.ActMainBinding
import com.moonpay.sdk.BuildConfig
import com.moonpay.sdk.MoonPayAndroidSdk
import com.moonpay.sdk.MoonPayBuyQueryParams
import com.moonpay.sdk.MoonPayHandlers
import com.moonpay.sdk.MoonPayRenderingOptionAndroid
import com.moonpay.sdk.MoonPaySdkBuyConfig
import com.moonpay.sdk.MoonPaySdkSellConfig
import com.moonpay.sdk.MoonPaySellQueryParams
import com.moonpay.sdk.MoonPayWidgetEnvironment
import com.moonpay.sdk.OnAuthTokenRequestPayload
import com.moonpay.sdk.OnInitiateDepositRequestPayload
import com.moonpay.sdk.OnInitiateDepositResponsePayload
import com.moonpay.sdk.OnLoginRequestPayload
import com.moonpay.sdk.OnTransactionCompletedRequestPayload
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActMainBinding
    private lateinit var moonPaySdk: MoonPayAndroidSdk
    private val pk = ""
    private val sk = ""
    private val address = "0x49F006c1AC9d42b26a5a1F1CFa66393D4FE40338"
    private val TAG = "testMoonPay"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //1.on-ramp
        binding.btOnRamp.setOnClickListener {
            onRamp()
        }
        //2.off-ramp
        binding.btOffRamp.setOnClickListener {
            offRamp()
        }
        //3.off-ramp with walletAddress and signature
        binding.btOffRamp2.setOnClickListener {
            offRamp(address)
        }
        moonPaySdk = MoonPayAndroidSdk(this)
    }

    private fun onRamp() {
        val handlers = MoonPayHandlers(onLogin = {
            Log.i(TAG, "onLogin called with payload $it")
        },
            onInitiateDeposit = {
                Log.i(TAG, "onInitiateDeposit called with payload $it")
                OnInitiateDepositResponsePayload(depositId = "someDepositId")
            })

        val params = MoonPayBuyQueryParams(pk)
        params.setWalletAddress("0x49F006c1AC9d42b26a5a1F1CFa66393D4FE40338")

        val config = MoonPaySdkBuyConfig(
            BuildConfig.DEBUG, MoonPayWidgetEnvironment.Sandbox,
            params,
            handlers
        )
        moonPaySdk.updateConfig(config)
        moonPaySdk.show(MoonPayRenderingOptionAndroid.WebViewOverlay)
    }

    private fun offRamp(address: String? = null) {
        val handlers = MoonPayHandlers(onLogin = {
            Log.i(TAG, "onLogin called with payload $it")
        },
            onInitiateDeposit = {
                Log.i(TAG, "onInitiateDeposit called with payload $it")
                OnInitiateDepositResponsePayload(depositId = "someDepositId")
            })

        val params = MoonPaySellQueryParams(pk)
        params.setWalletAddress("0x49F006c1AC9d42b26a5a1F1CFa66393D4FE40338")
        val config = MoonPaySdkSellConfig(
            BuildConfig.DEBUG, MoonPayWidgetEnvironment.Sandbox,
            params,
            handlers
        )
        moonPaySdk.updateConfig(config)
        if (!TextUtils.isEmpty(address)) {
            moonPaySdk.updateSignature(signUrl(moonPaySdk.generateUrlForSigning()))
        }
        moonPaySdk.show(MoonPayRenderingOptionAndroid.WebViewOverlay)
    }

    private fun signUrl(url: String): String {
        try {
            val index = url.indexOf("?")
            var query = url.substring(index)
            Log.i(TAG, "query=$query")
            val sha_HMAC = Mac.getInstance("HmacSHA256")
            val secret_key = SecretKeySpec(sk.toByteArray(), "HmacSHA256")
            sha_HMAC.init(secret_key)
            val bytes = sha_HMAC.doFinal(query.toByteArray())
            val thash = Base64.getEncoder().encodeToString(bytes)
            Log.i(TAG, "signature=$thash")
            return thash
        } catch (e: Exception) {
        }
        return ""
    }
}

