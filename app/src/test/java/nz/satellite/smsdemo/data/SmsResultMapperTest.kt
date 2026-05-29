package nz.satellite.smsdemo.data

import android.app.Activity
import android.telephony.SmsManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsResultMapperTest {

    @Test
    fun resultOk_returnsSuccessDescription() {
        val desc = smsResultDescription(Activity.RESULT_OK)
        assertTrue("Expected success message, got: $desc", desc.contains("success", ignoreCase = true))
    }

    @Test
    fun noService_mentionsNoService() {
        val desc = smsResultDescription(SmsManager.RESULT_ERROR_NO_SERVICE)
        assertTrue("Expected 'no service' in: $desc", desc.contains("service", ignoreCase = true))
    }

    @Test
    fun radioOff_mentionsRadio() {
        val desc = smsResultDescription(SmsManager.RESULT_ERROR_RADIO_OFF)
        assertTrue("Expected 'radio' in: $desc", desc.contains("radio", ignoreCase = true))
    }

    @Test
    fun nullPdu_returnsMeaningfulText() {
        val desc = smsResultDescription(SmsManager.RESULT_ERROR_NULL_PDU)
        assertFalse("Should not be empty", desc.isBlank())
    }

    @Test
    fun genericFailure_returnsMeaningfulText() {
        val desc = smsResultDescription(SmsManager.RESULT_ERROR_GENERIC_FAILURE)
        assertTrue(desc.contains("generic", ignoreCase = true))
    }

    @Test
    fun unknownCode_includesCodeValue() {
        val code = 99999
        val desc = smsResultDescription(code)
        assertTrue("Should include the unknown code value", desc.contains(code.toString()))
    }
}
