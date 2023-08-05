
import api.ServiceApi
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.mordant.rendering.TextColors
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import network.TokenHolder
import retrofit2.HttpException
import utils.logging.Level
import utils.logging.TLogger
import kotlin.system.exitProcess



//private val logger = KotlinLogging.logger {}
private val logger = TLogger

fun main(args: Array<String>):Unit =   DiscoveryRun().versionOption("1.0.3").main(args)

class DiscoveryRun: CliktCommand(help = "Execute ou enregistre un Job de scan dans BMC Discovery") {

    enum class scanLevel(val level: String) { FULL("Full Discovery"), SWEEP("Sweep Scan") }
    enum class scanKind(val kind: String) { IP("IP"), CLOUD("Cloud") }

    val validURL = "^(http(s):\\/\\/.)[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)(/)\$"
 //   val validURL_IP = "^http (s?):\\/\\/ ( (2 [0-5] {2}|1 [0-9] {2}| [0-9] {1,2})\\.) {3} (2 [0-5] {2}|1 [0-9] {2}| [0-9] {1,2}) (:\\d+)?(/)\$"

    val server: String by option(
        "-s", "--server",
        help = "URL API du serveur Discovery , (https et termine avec '/') \n " +
                "généralement https://server/api/v1.1/")
        .required()
        .validate {
        if (!it.matches(Regex(validURL)))
            throw UsageError("URL du serveur invalide : $it \n" +
                    "lancer le programme avec l'option -h pour de l'aide")
    }

    val username: String by option(
        "-u", "--username",
        help = "Login - Nom de l'utilisateur"
    ).prompt()

    val password: String by option(
        "-p", "--password",
        help = "Login - Mot de Passe de l'utilisateur"
    ).prompt(hideInput = true)

    val unsafe by option( "-x", "--unsecure",
        help = "do not verify SSL certificate checking process (useful with self signed certificate)"
    ).flag(default = true)

    val company by option(
        "-c", "--company",
        help = "nom de la société cible"
    ).default("")

    val label by option(
        "-l", "--label",
        help = "Label du job"
    ).default("DiscoveryRun App")

    val targets by option(
        "-t", "--targets",
        help = "Liste des cibles (IPv4 ou IPv6) à scanner, séparées par le caractère ','"
    ).required()

    val scan_level by option(
        "-v", "--level",
        help = "Niveau du scan").choice(
        "full" to scanLevel.FULL,
        "sweep" to scanLevel.SWEEP,
    ).default(scanLevel.FULL)

    val scan_kind by option(
        "-k", "--kind",
        help = "type de cibles pour le scan").choice(
        "IP" to scanKind.IP,
        "Cloud" to scanKind.CLOUD,
    ).default(scanKind.IP)

    val params by option(
        "-a", "--params",
        help = "Parametres additionels (string format JSON) \n" +
                " (outpost_id : String) \n" +
                " (scope : String)\n" +
                " (restricted_org_id : String)\n" +
                " [Parametres du scan :]\n" +
                "  \t (provider : String)\n" +
                "  \t (credential : String)\n" +
                "  \t (region : Array of String)\n" +
                "  \t (cluster_url : String)\n" +
                " [Options du scan :]\n" +
                "  \t(NO_PING : Boolean)\n" +
                "  \t(SESSION_LOGGING : Boolean)\n" +
                "  \t(CLOUD_HOST_DETECTION : Boolean)\n" +
                "  \t(SKIP_IMPLICIT_SCANS : Boolean)\n" +
                "  \t(VMWARE_GUEST_IMPLICIT_SCAN : Boolean)\n" +
                "  \t(MAX_START_SSM_SESSION : Int)\n" +
                "  \t(MAX_ACTIVE_SSM_SESSION : Int)\n" +
                "  \t(MAX_ACTIVE_TAP_SESSION : Int)\n"
    ).default("{\"detail1\":\"exemple\"}")

    val schedule by option( "--schedule",
        help = "Enregistre le Job pour une execution périodique (défaut FALSE)"
    ).flag(default = false)

    val plan by option(
        "--plan",
        help = "Parametres planification (string format JSON) \n" +
                " (day_of_month : Array of Int) \n" +
                " (day_of_week : Array of String)\n" +
                " (duration : Int)\n" +
                " (recurrence_type: String)\n" +
                " (start_minute : Int)\n" +
                " (start_times : Array of Int)\n" +
                " (week_def : String)\n"
    ).default("{\"recurrence_type\":\"DAILY\"}")

    val enabled by option( "-e","--enabled",
        help = "Autorise l'exécution du job planifié (défaut FALSE)"
    ).flag(default = false)

    val debugLevel by option(
        "-d", "--debug",
        help = "Niveau du debug").choice(
        "off" to Level.OFF,
        "trace" to Level.TRACE,
            "debug" to Level.DEBUG,
        "info" to Level.INFO,
        "error" to Level.ERROR,
        "fatal" to Level.FATAL,
        "all" to Level.ALL
    ).default(Level.OFF)

    override fun run() {


        echo("===============================================================================")
        echo(" Discovery Run Job Launcher - TSO pour Orange Business - 08/23 - version 1.0.3 ")
        echo("===============================================================================")

        logger.setRunLevel(debugLevel)
        logger.addDateTimeToPrefix()
        logger.addDurationToPrefix()

        val gson = Gson()
        var jsonParams: JsonObject
        try {
            jsonParams = gson.fromJson(params, JsonObject::class.java)
        } catch (e: JsonSyntaxException) {
            logger.error("Erreur JSON pour PARAMS : ${e.message}")
            exitProcess(-4)
        }

        var jsonPlan: JsonObject
        try {
            jsonPlan = gson.fromJson(plan.trim(), JsonObject::class.java)
        } catch (e: JsonSyntaxException) {
            logger.error("Erreur JSON pour PLAN : ${e.message}")
            exitProcess(-4)
        }

        if ((jsonParams.get("scope") != null) && (jsonParams.get("outpost_id") != null))
        {
            logger.info("Erreur d'argument : Vous ne devez pas spécifier 'scope' et 'outpost_id' dans la même commande ! ")
            exitProcess(-4)
        }

        if ((scan_kind == scanKind.CLOUD) && (jsonParams.get("provider") == null))
        {
            logger.info("Erreur d'argument : merci de spécifier 'provider' pour un scan Cloud ")
            exitProcess(-4)
        }

        if (password.isNullOrEmpty())
        {
            logger.info("Erreur d'argument : PASSWORD ne peut pas être vide")
            exitProcess(-4)
        }

        if (username.isNullOrEmpty())
        {
            logger.info("Erreur d'argument : USERNAME ne peut pas être vide")
            exitProcess(-4)
        }

        if (targets.isNullOrEmpty())
        {
            logger.info("Erreur d'argument : si les cibles (--target=) ne sont pas spécifiées cela ne sert à rien de faire un scan!")
            exitProcess(-4)
        }

        apiCallByCoroutines(
            username,
            password,
            server,
            schedule,
            company,
            label,
            targets.trim().split(","),
            scan_kind.kind,
            scan_level.level,
            jsonParams,
            jsonPlan,
            enabled,
            unsafe
        )

    }

}
private fun apiCallByCoroutines(
    username: String,
    password: String,
    server: String,
    schedule: Boolean,
    company: String,
    label: String,
    targets: List<String>,
    scan_kind: String,
    level: String,
    //       scope: String,
    params: JsonObject,
    plan: JsonObject,
    enabled: Boolean,
    unsafe: Boolean
) = runBlocking {
    launch { // launch new coroutine in the scope of runBlocking

        try {
            ServiceApi.apiGetToken(server, username, password, unsafe).let { token: String? ->
                if (token != null) {
                    TokenHolder.saveToken(token)
                    if (schedule) {
                        ServiceApi.apiCreateScheduledRunJob(
                            server,
                            company,
                            label,
                            targets,
                            scan_kind,
                            level,
                            params,
                            plan,
                            enabled,
                            unsafe
                        )
                        logger.info("Enregistrement de la planification du Job")
                    }
                    else {
                        val runId =
                            ServiceApi.apiCreateRunJob(
                                server,
                                company,
                                label,
                                targets,
                                scan_kind,
                                level,
                                params,
                                unsafe
                            )
                        logger.info("Run ID: $runId")
                        val info = ServiceApi.apiWaitJobResults(server, runId!!, unsafe)
                        println(
                            TextColors.yellow(
                                "\nRésultats :  \n" +
                                        " >" +
                                        " | Scanned : ${targets.size}" +
                                        " | Success : ${info?.Success?.count}" +
                                        " | Skipped : ${info?.Skipped?.count}" +
                                        " | No Response : ${info?.NoResponse?.count}" +
                                        " | No Access : ${info?.NoAccess?.count}" +
                                        " | Error : ${info?.Error?.count}" +
                                        " | Dropped : ${info?.Dropped?.count} | <"
                            )
                        )
                    }
                }
            }
        }
        catch (exception: HttpException) {
            logger.error("Erreur : ${exception.message}" )
            exitProcess(-1)
        }
    }


}
