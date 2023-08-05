# DiscoveryRun

exemple de ligne de commande: (nécessite un JVM)

Il s'agit d'un programme en ligne de commande qui permet de créer et de lancer un job de scan

Usage: DiscoveryRun [\<options>]

Options:

-s, --server=\<text>   :    URL API du serveur Discovery , (https et termine
avec '/') généralement https://server/api/v1.1/

-u, --username=\<text>  :   Login - Nom de l'utilisateur

-p, --password=\<text>  :   Login - Mot de Passe de l'utilisateur

-x, --unsecure     :       do not verify SSL certificate checking process
(useful with self signed certificate)

-c, --company=\<text>   :   nom de la société cible

-l, --label=\<text>   :     Label du job

-t, --targets=\<text>  :    Liste des cibles (IPv4 ou IPv6) à scanner, séparées
par le caractère ','

-v, --level=(full|sweep) : Niveau du scan

-k, --kind=(IP|Cloud)  :   type de cibles pour le scan

-a, --params=\<text>   :    Parametres additionels (string format JSON)
(outpost_id : String) (scope : String)
(restricted_org_id : String) [Parametres du scan :]
(provider : String) (credential : String) (region :
Array of String) (cluster_url : String) [Options du
scan :] (NO_PING : Boolean) (SESSION_LOGGING :
Boolean) (CLOUD_HOST_DETECTION : Boolean)
(SKIP_IMPLICIT_SCANS : Boolean)
(VMWARE_GUEST_IMPLICIT_SCAN : Boolean)
(MAX_START_SSM_SESSION : Int)
(MAX_ACTIVE_SSM_SESSION : Int)
(MAX_ACTIVE_TAP_SESSION : Int)

--schedule       :         Enregistre le Job pour une execution périodique
(défaut FALSE)

--plan=\<text>     :        Parametres planification (string format JSON)
(day_of_month : Array of Int) (day_of_week : Array
of String) (duration : Int) (recurrence_type:
String) (start_minute : Int) (start_times : Array
of Int) (week_def : String)

-e, --enabled      :       Autorise l'exécution du job planifié (défaut FALSE)

-d, --debug=(off|trace|debug|info|error|fatal|all) :
Niveau du debug

--version         :        Show the version and exit

-h, --help         :       Show this message and exit

