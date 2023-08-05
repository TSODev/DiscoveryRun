package api


import com.google.gson.JsonObject
import kotlinx.coroutines.delay
import models.*
import network.RetrofitClient
import retrofit2.HttpException
import utils.Transformation.Companion.getStringValue
import utils.Transformation.Companion.getValue
import utils.Transformation.Companion.stringToArrayInt
import utils.Transformation.Companion.stringToInt
import utils.Transformation.Companion.stringToListString
import utils.logging.TLogger

//private val logger = KotlinLogging.logger {}
private val logger = TLogger

interface ServiceApi {

    companion object {

        fun apiGetToken(serverUrl: String, username: String, password: String, unsafe: Boolean): String? {
            val retrofit =
                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
            val apiService = retrofit.create(DiscoveryApi::class.java)

            var token: String? = null
            val response = apiService.authenticateUser("password", username, password).execute()
            if (response.isSuccessful) {
                token = response.body()?.token!!
            } else {
                throw HttpException(response)
            }
            return token

        }

        fun apiSendEvent(serverUrl: String, source: String, type: String, params: JsonObject, unsafe: Boolean): String? {
            val retrofit =
                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
            val apiService = retrofit.create(DiscoveryApi::class.java)
            var id = ""
            val data = JsonObject()
            data.addProperty("source", source)
            data.addProperty("type", type)
            data.add("params", params)
            val response = apiService.apiSendEvent(data).execute()
            if (response.isSuccessful) {
                id = response.body().toString()
            } else {
                throw HttpException(response)
            }
            return id
        }

        fun apiCreateRunJob(serverUrl: String, company: String, label: String, targets: List<String>,scan_kind: String,level: String, params: JsonObject, unsafe: Boolean): String? {
            val retrofit =
                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
            val apiService = retrofit.create(DiscoveryApi::class.java)
            var id = ""

            val data = JobRun(
                company = company,
                label = label,
                outpost_id = getValue(params,"outpost_id"),
                ranges = targets,
                restricted_org_id = getValue(params,"restricted_org_id"),
                scan_kind = scan_kind,
                scan_level = level,
                scan_options = ScanOptions(
                    CLOUD_HOST_DETECTION = true,
                    MAX_ACTIVE_IAP_SESSIONS = 50,
                    MAX_ACTIVE_SSM_SESSIONS = 50,
                    MAX_START_SSM_SESSIONS = 3,
                    NO_PING = false,
                    SESSION_LOGGING = false,
                    SKIP_IMPLICIT_SCANS = false,
                    VMWARE_GUEST_IMPLICIT_SCANS = true
                ),
                scan_params = ScanParams(
                    cluster_url = getValue(params,"cluster_url"),
                    credential = getValue(params,"credential"),
                    provider = getValue(params,"provider"),
                    region = listOf()
                ),
                scope = getValue(params,"scope")
                )

            val response = apiService.apiCreateRunJob(data).execute()
            if (response.isSuccessful) {
                id = response.body()?.uuid.toString()
            } else {
                throw HttpException(response)
            }
            return id
        }

        fun apiCreateScheduledRunJob(serverUrl: String, company: String, label: String, targets: List<String>,scan_kind: String,level: String, params: JsonObject, plan: JsonObject, enabled: Boolean, unsafe: Boolean) {
            val retrofit =
                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
            val apiService = retrofit.create(DiscoveryApi::class.java)

            logger.debug("plan : $plan")
            val schedule = Schedule(
                days_of_month = stringToArrayInt(getStringValue(plan, "days_of_month")),
                days_of_week =  stringToListString( getStringValue(plan, "days_of_week")),
                duration = stringToInt(getStringValue( plan, "duration")),
                recurrence_type = getStringValue(plan, "recurrence_type"),
                start_minute = stringToInt(getStringValue(plan, "start_minute")),
                start_times = stringToArrayInt(getStringValue(plan, "start_times")),
                week_def = getStringValue(plan, "week_def")
            )
            logger.debug("shedule : $schedule")

            val data = JobRunScheduled(
                company = company,
                label = label,
                outpost_id = getValue(params,"outpost_id"),
                ranges = targets,
                restricted_org_id = getValue(params,"restricted_org_id"),
                scan_kind = scan_kind,
                scan_level = level,
                scan_options = ScanOptions(
                    CLOUD_HOST_DETECTION = true,
                    MAX_ACTIVE_IAP_SESSIONS = 50,
                    MAX_ACTIVE_SSM_SESSIONS = 50,
                    MAX_START_SSM_SESSIONS = 3,
                    NO_PING = false,
                    SESSION_LOGGING = false,
                    SKIP_IMPLICIT_SCANS = false,
                    VMWARE_GUEST_IMPLICIT_SCANS = true
                ),
                scan_params = ScanParams(
                    cluster_url = getValue(params,"cluster_url"),
                    credential = getValue(params,"credential"),
                    provider = getValue(params,"provider"),
                    region = listOf()
                ),
                enabled = enabled,
                scan_type = "",
                schedule = schedule,
                scope = getValue(params,"scope")
            )

            val response = apiService.apiCreateScheduledRunJob(data).execute()
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
        }


        suspend fun apiWaitJobResults(serverUrl: String, run_id: String, unsafe: Boolean): JobFinalResults? {
            val retrofit =
                if (!unsafe) RetrofitClient.getClient(serverUrl) else RetrofitClient.getUnsafeClient(serverUrl)
            val apiDiscovery = retrofit.create(DiscoveryApi::class.java)
            var id = ""
            var finished = false
            var results: JobResultsItem? = null
            logger.info("En attente des résultats du scan...")
            while (!finished) {
                val response = apiDiscovery.apiGetJobResults(run_id).execute()
                if (response.isSuccessful) {
                    if (!response.body().isNullOrEmpty()) {
                        results = response.body()!![0]
//                        logger.debug("$results")
                        finished = results.finished
                        delay(3000)
                        logger.info(".",false)
                    } else {
                        logger.debug("Pas de resultats succes au Job lancé !")
                        finished = true
                    }
                } else {
                    throw HttpException(response)
                }
            }
//            val inferred = apiDiscovery.apiGetInferred(results?.inferred!!).execute()
//            logger.debug("$inferred")
            val finalResults = apiDiscovery.apiGetFinalResults(results?.results!!).execute()
            logger.debug("$finalResults")
            return finalResults.body()
        }

    }
}




