package nz.satellite.smsdemo

import android.app.Application
import nz.satellite.smsdemo.data.SmsRepositoryImpl
import nz.satellite.smsdemo.domain.SmsRepository

/**
 * Holds app-wide singletons. Acts as a tiny manual DI container so the UI layer
 * can obtain the [SmsRepository] without depending on its concrete implementation.
 */
class SmsDemoApplication : Application() {
    val smsRepository: SmsRepository by lazy { SmsRepositoryImpl(this) }
}
