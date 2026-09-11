package com.example.mayaassistant

import android.service.voice.VoiceInteractionService

class MayaVoiceInteractionService : VoiceInteractionService() {
    override fun onReady() {
        super.onReady()
        // Maya is now registered as the selected Android VoiceInteractionService.
    }
}
