package com.andreramon.demo.util

import android.util.Log

class Logger : ILogger {
    override fun log(tag: String, msg: String) {
        Log.d(tag, "${Thread.currentThread().name} >> $msg")
    }
}