package com.lab.opengarage

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OpenGarageApp : Application(), ImageLoaderFactory {
    // 정비 도해(SVG)와 사진(래스터)을 모두 렌더링하도록 SVG 디코더 추가
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .build()
}
