package com.lab.opengarage.data

import com.lab.opengarage.model.Record
import javax.inject.Inject
import javax.inject.Singleton

/** 앱 시작 시 미리 받아둔 첫 피드 페이지. 스플래시 동안 프리페치 → 피드 진입 즉시 표시. */
@Singleton
class FeedCache @Inject constructor() {
    @Volatile
    var firstPage: Page<Record>? = null
}
