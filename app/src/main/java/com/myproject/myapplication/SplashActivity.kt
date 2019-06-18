package com.myproject.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myproject.myapplication.main.MainActivity
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {

    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposable = Observable.timer(1300, TimeUnit.MILLISECONDS)
            .subscribe(
                {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                },
                { it.printStackTrace() },
                { finish() }
            )
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }
}
