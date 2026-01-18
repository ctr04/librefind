package com.jksalcedo.librefind.data.remote.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jksalcedo.librefind.data.remote.firebase.dto.FeedbackDto
import com.jksalcedo.librefind.data.remote.firebase.dto.FossSolutionDto
import com.jksalcedo.librefind.data.remote.firebase.dto.ProprietaryTargetDto
import com.jksalcedo.librefind.data.remote.firebase.dto.RatingDto
import com.jksalcedo.librefind.data.remote.firebase.dto.SubmissionDto
import com.jksalcedo.librefind.data.remote.firebase.dto.UserProfileDto
import com.jksalcedo.librefind.domain.model.Alternative
import com.jksalcedo.librefind.domain.model.Feedback
import com.jksalcedo.librefind.domain.model.Submission
import com.jksalcedo.librefind.domain.model.UserProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class FirestoreService(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_PROPRIETARY = "proprietary_targets"
        private const val COLLECTION_FOSS = "foss_solutions"
        private const val COLLECTION_PROPOSALS = "alternative_proposals"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_SUBMISSIONS = "user_submissions"
    }

    /**
     * Checks if a package is in the proprietary database
     *
     * @param packageName Package to check
     * @return True if found in proprietary collection
     */
    suspend fun isProprietaryPackage(packageName: String): Boolean {
        return try {
            val sanitized = sanitizePackageName(packageName)
            android.util.Log.d("FIRESTORE", "Checking proprietary: $packageName -> $sanitized")
            val doc = firestore.collection(COLLECTION_PROPRIETARY)
                .document(sanitized)
                .get()
                .await()

            val exists = doc.exists()
            android.util.Log.d("FIRESTORE", "Result for $sanitized: exists=$exists")
            exists
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE", "Error checking $packageName: ${e.message}", e)
            false
        }
    }

    /**
     * Gets alternatives for a proprietary package
     *
     * @param packageName Proprietary package
     * @return List of Alternative objects
     */
    suspend fun getAlternatives(packageName: String): List<Alternative> {
        return try {
            val sanitized = sanitizePackageName(packageName)
            val doc = firestore.collection(COLLECTION_PROPRIETARY)
                .document(sanitized)
                .get()
                .await()

            if (!doc.exists()) return emptyList()

            val target = doc.toObject(ProprietaryTargetDto::class.java) ?: return emptyList()

            // Fetch each alternative in parallel
            coroutineScope {
                target.alternatives.map { altId ->
                    async { fetchFossSolution(altId) }
                }.awaitAll().filterNotNull()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchFossSolution(id: String): Alternative? {
        return try {
            val doc = firestore.collection(COLLECTION_FOSS)
                .document(id)
                .get()
                .await()

            if (!doc.exists()) return null

            val dto = doc.toObject(FossSolutionDto::class.java) ?: return null
            dto.toDomain(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAlternativeById(altId: String): Alternative? {
        return fetchFossSolution(altId)
    }

    /**
     * Submits a new alternative proposal
     *
     * @return True if successful
     */
    suspend fun submitProposal(
        proprietaryPackage: String,
        alternativeId: String,
        userId: String
    ): Boolean {
        return try {
            val proposal = hashMapOf(
                "proprietary_package" to proprietaryPackage,
                "alternative_id" to alternativeId,
                "user_id" to userId,
                "timestamp" to System.currentTimeMillis(),
                "status" to "pending"
            )

            firestore.collection(COLLECTION_PROPOSALS)
                .add(proposal)
                .await()

            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Votes for an alternative
     *
     * @param alternativeId Alternative to vote for
     * @param category "privacy" or "usability"
     * @param userId User casting vote
     * @return True if successful
     */
    suspend fun voteForAlternative(
        alternativeId: String,
        category: String,
        userId: String
    ): Boolean {
        return try {
            val docRef = firestore.collection(COLLECTION_FOSS)
                .document(alternativeId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val votes = snapshot.get("votes") as? Map<String, Int> ?: emptyMap()
                val currentVotes = votes[category] ?: 0

                val updatedVotes = votes.toMutableMap()
                updatedVotes[category] = currentVotes + 1

                transaction.update(docRef, "votes", updatedVotes)
            }.await()

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sanitizes package name for use as Firestore document ID
     * Replaces dots with underscores
     */
    private fun sanitizePackageName(packageName: String): String {
        return packageName.replace(".", "_")
    }

    // ========== User Profile Methods ==========

    suspend fun createOrUpdateProfile(profile: UserProfile): Boolean {
        return try {
            val dto = UserProfileDto.fromDomain(profile)
            firestore.collection(COLLECTION_USERS)
                .document(profile.uid)
                .set(dto)
                .await()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getProfile(uid: String): UserProfile? {
        return try {
            val doc = firestore.collection(COLLECTION_USERS)
                .document(uid)
                .get()
                .await()
            if (!doc.exists()) return null
            doc.toObject(UserProfileDto::class.java)?.toDomain()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun isUsernameTaken(username: String): Boolean {
        return try {
            val query = firestore.collection(COLLECTION_USERS)
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .await()
            !query.isEmpty
        } catch (_: Exception) {
            true
        }
    }

    // ========== Submission Methods ==========

    suspend fun submitEntry(submission: Submission): Result<String> {
        return try {
            val dto = SubmissionDto.fromDomain(submission)
            val docRef = firestore.collection(COLLECTION_SUBMISSIONS)
                .add(dto)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMySubmissions(uid: String): List<Submission> {
        return try {
            val query = firestore.collection(COLLECTION_SUBMISSIONS)
                .whereEqualTo("submitter_uid", uid)
                .get()
                .await()
            query.documents.mapNotNull { doc ->
                doc.toObject(SubmissionDto::class.java)?.toDomain(doc.id)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getProprietaryTargets(): List<String> {
        return try {
            val query = firestore.collection(COLLECTION_PROPRIETARY)
                .get()
                .await()
            query.documents.mapNotNull { it.getString("package_name") }
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ========== Rating Methods ==========

    suspend fun rateAlternative(altId: String, uid: String, stars: Int): Boolean {
        return try {
            val ratingRef = firestore.collection(COLLECTION_FOSS)
                .document(altId)
                .collection("ratings")
                .document(uid)

            val existing = ratingRef.get().await()
            val now = System.currentTimeMillis()
            val isNew = !existing.exists()
            val oldStars = if (isNew) 0 else existing.getLong("stars")?.toInt() ?: 0

            val ratingData = RatingDto(
                stars = stars,
                createdAt = if (isNew) now else existing.getLong("created_at") ?: now,
                updatedAt = now
            )
            ratingRef.set(ratingData).await()

            // Update aggregates on alternative doc
            val altRef = firestore.collection(COLLECTION_FOSS).document(altId)
            firestore.runTransaction { transaction ->
                val altDoc = transaction.get(altRef)
                val currentAvg = altDoc.getDouble("rating_avg")?.toFloat() ?: 0f
                val currentCount = altDoc.getLong("rating_count")?.toInt() ?: 0

                val (newAvg, newCount) = if (isNew) {
                    val total = currentAvg * currentCount + stars
                    val count = currentCount + 1
                    (total / count) to count
                } else {
                    val total = currentAvg * currentCount - oldStars + stars
                    (total / currentCount) to currentCount
                }

                transaction.update(altRef, mapOf(
                    "rating_avg" to newAvg,
                    "rating_count" to newCount
                ))
            }.await()

            true
        } catch (e: Exception) {
            android.util.Log.e("FIRESTORE", "Error rating: ${e.message}", e)
            false
        }
    }

    suspend fun getUserRating(altId: String, uid: String): Int? {
        return try {
            val doc = firestore.collection(COLLECTION_FOSS)
                .document(altId)
                .collection("ratings")
                .document(uid)
                .get()
                .await()
            if (doc.exists()) doc.getLong("stars")?.toInt() else null
        } catch (_: Exception) {
            null
        }
    }

    // ========== Feedback Methods ==========

    suspend fun submitFeedback(altId: String, feedback: Feedback): Result<String> {
        return try {
            val dto = FeedbackDto.fromDomain(feedback)
            val docRef = firestore.collection(COLLECTION_FOSS)
                .document(altId)
                .collection("feedback")
                .add(dto)
                .await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getApprovedFeedback(altId: String): List<Feedback> {
        return try {
            val query = firestore.collection(COLLECTION_FOSS)
                .document(altId)
                .collection("feedback")
                .whereEqualTo("status", "APPROVED")
                .get()
                .await()
            query.documents.mapNotNull { doc ->
                doc.toObject(FeedbackDto::class.java)?.toDomain(doc.id)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun voteHelpful(altId: String, feedbackId: String): Boolean {
        return try {
            firestore.collection(COLLECTION_FOSS)
                .document(altId)
                .collection("feedback")
                .document(feedbackId)
                .update("votes_helpful", FieldValue.increment(1))
                .await()
            true
        } catch (_: Exception) {
            false
        }
    }
}


