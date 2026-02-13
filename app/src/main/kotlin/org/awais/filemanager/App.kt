package org.awais.filemanager

import com.github.ajalt.reprint.core.Reprint
import com.google.android.gms.ads.MobileAds
import org.fossify.commons.FossifyApp

class App : FossifyApp() {
    override val isAppLockFeatureAvailable = true

    override fun onCreate() {
        super.onCreate()
        Reprint.initialize(this)
        MobileAds.initialize(this) {}
    }
}
