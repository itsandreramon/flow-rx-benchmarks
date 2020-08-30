package com.andreramon.demo.ui

import com.robinhood.spark.SparkAdapter


class SparkAdapter(private val yData: FloatArray) : SparkAdapter() {
    override fun getCount(): Int {
        return yData.size
    }

    override fun getItem(index: Int): Any {
        return yData[index]
    }

    override fun getY(index: Int): Float {
        return yData[index]
    }
}