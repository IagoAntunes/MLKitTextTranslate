package com.alura.mail.mlkit

import android.util.Log
import com.alura.mail.R
import com.alura.mail.model.EntityInfo
import com.alura.mail.model.Suggestion
import com.alura.mail.model.SuggestionAction
import com.google.mlkit.nl.entityextraction.EntityExtraction
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions

class EntityExtractor {

    fun extractSuggestions(
        text: String,
        modelIdentifier: String = EntityExtractorOptions.PORTUGUESE,
        onSuccess: (List<EntityInfo>, List<IntRange>) -> Unit,
    ) {
        val entityExtractor =
            EntityExtraction.getClient(
                EntityExtractorOptions.Builder(modelIdentifier)
                    .build()
            )

        entityExtractor
            .downloadModelIfNeeded()
            .addOnSuccessListener { _ ->
                val params = EntityExtractionParams.Builder(text).build()
                entityExtractor
                    .annotate(params)
                    .addOnSuccessListener { entities ->
                        Log.i("EntityExtraction", "Entidades $entities")

                        val listEntityInfo: List<EntityInfo> = entities.map { entity ->
                            val entityText = text.substring(entity.start, entity.end)

                            val action = getSuggestionActionByEntity(entity.entities.first().type)

                            EntityInfo(
                                entityText = entityText,
                                action = action
                            )
                        }

                        val entitiesRanges: List<IntRange> = entities.map {
                            it.start..it.end
                        }

                        Log.i("EntityRanges", "Ranges $entitiesRanges")


                        onSuccess(listEntityInfo, entitiesRanges)
                    }
                    .addOnFailureListener {
                        // Check failure message here.
                    }
            }
            .addOnFailureListener { _ -> /* Model downloading failed. */ }
    }


    fun entityToSuggestionAction(entities: List<EntityInfo>): List<Suggestion> {
        return entities.map { entity ->
            val suggestion = Suggestion(text = entity.entityText, action = entity.action)
            when (entity.action) {
                SuggestionAction.DATE_TIME -> suggestion.copy(icon = R.drawable.ic_date_time)
                SuggestionAction.PHONE_NUMBER -> suggestion.copy(icon = R.drawable.ic_call)
                SuggestionAction.ADDRESS -> suggestion.copy(icon = R.drawable.ic_location)
                SuggestionAction.EMAIL -> suggestion.copy(icon = R.drawable.ic_email)
                SuggestionAction.URL -> suggestion.copy(icon = R.drawable.ic_link)
                else -> suggestion.copy(icon = R.drawable.ic_copy)
            }
        }
    }

    private fun getSuggestionActionByEntity(entityType: Int): SuggestionAction {
        return when (entityType) {
            1 -> SuggestionAction.ADDRESS
            2 -> SuggestionAction.DATE_TIME
            3 -> SuggestionAction.EMAIL
            4 -> SuggestionAction.FLIGHT_NUMBER
            5 -> SuggestionAction.IBAN
            6 -> SuggestionAction.ISBN
            7 -> SuggestionAction.PAYMENT_CARD_NUMBER
            8 -> SuggestionAction.PHONE_NUMBER
            9 -> SuggestionAction.TRACKING_NUMBER
            10 -> SuggestionAction.URL
            11 -> SuggestionAction.MONEY
            else -> SuggestionAction.SMART_REPLY
        }
    }
}