package io.walletconnect.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import io.walletconnect.example.databinding.ScreenMainBinding
import kotlinx.coroutines.launch
import org.walletconnect.Session
import org.walletconnect.nullOnThrow


class MainActivity : ComponentActivity(), Session.Callback {

    private var txRequest: Long? = null

    private lateinit var binding: ScreenMainBinding

    override fun onStatus(status: Session.Status) {
        when (status) {
            Session.Status.Approved -> sessionApproved()
            Session.Status.Closed -> sessionClosed()
            Session.Status.Connected,
            Session.Status.Disconnected,
            is Session.Status.Error -> {
                // Do Stuff
            }

            else -> {}
        }
    }

    override fun onMethodCall(call: Session.MethodCall) {
    }

    private fun sessionApproved() {
        lifecycleScope.launch {
            binding.screenMainStatus.text = "Connected: ${ExampleApplication.session.approvedAccounts()}"
            binding.screenMainConnectButton.visibility = View.GONE
            binding.screenMainDisconnectButton.visibility = View.VISIBLE
            binding.screenMainTxButton.visibility = View.VISIBLE
        }
    }

    private fun sessionClosed() {
        lifecycleScope.launch {
            binding.screenMainStatus.text = "Disconnected"
            binding.screenMainConnectButton.visibility = View.VISIBLE
            binding.screenMainDisconnectButton.visibility = View.GONE
            binding.screenMainTxButton.visibility = View.GONE
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ScreenMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun onStart() {
        super.onStart()
        initialSetup()
        binding.screenMainConnectButton.setOnClickListener {
            ExampleApplication.resetSession()
            ExampleApplication.session.addCallback(this)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(ExampleApplication.config.toWCUri())
            startActivity(i)
        }
        binding.screenMainDisconnectButton.setOnClickListener {
            ExampleApplication.session.kill()
        }
        binding.screenMainTxButton.setOnClickListener {
            val from = ExampleApplication.session.approvedAccounts()?.first()
                ?: return@setOnClickListener
            val txRequest = System.currentTimeMillis()
            ExampleApplication.session.performMethodCall(
                Session.MethodCall.SendTransaction(
                    txRequest,
                    from,
                    "0x24EdA4f7d0c466cc60302b9b5e9275544E5ba552",
                    null,
                    null,
                    null,
                    "0x5AF3107A4000",
                    ""
                ),
                ::handleResponse
            )
            this.txRequest = txRequest
        }
    }

    private fun initialSetup() {
        val session = nullOnThrow { ExampleApplication.session } ?: return
        session.addCallback(this)
        sessionApproved()
    }

    private fun handleResponse(resp: Session.MethodCall.Response) {
        if (resp.id == txRequest) {
            txRequest = null
            lifecycleScope.launch {
                binding.screenMainResponse.visibility = View.VISIBLE
                binding.screenMainResponse.text = "Last response: " + ((resp.result as? String) ?: "Unknown response")
            }
        }
    }

    override fun onDestroy() {
        ExampleApplication.session.removeCallback(this)
        super.onDestroy()
    }
}
