# DiscoveryRun

exemple de ligne de commande: (nécessite un JVM)

*--server=https://server/api/v1.9/ --username=Allen --password=Password_1234 --label=TSO_Run --targets=192.168.100.202,192.168.100.201 --level=full --kind=IP  --debug=info*

*--server=https://server/api/v1.9/ --username=Allen --password=Password_1234 --label=TSO_Run_Plan --targets=192.168.100.202,192.168.100.201 --level=full --kind=IP  --schedule --plan={'days_of_month':'[1,8,15,22,29]','days_of_week':'MONDAY,FRIDAY','duration':'0','recurrence_type':'WEEKLY','start_minute':'0','start_times':'6,8,12,20','week_def':'FIRST'} --debug=OFF*

> Il s'agit d'un programme en ligne de commande qui permet de créer, de lancer, ou de programmer un job de scan

###Usage: DiscoveryRun [\<options>]

#########Options:



**-s, --server**=\<text>   :    URL API du serveur Discovery , *(https et termine
avec '/') généralement https://server/api/v1.1/*

**-u, --username**=\<text>  :   Nom de l'utilisateur

**-p, --password**=\<text>  :   Mot de Passe de l'utilisateur

**-x, --unsecure **    :       pas de vérification du certificat SSL
*(permet l'utilisation de certificat auto signé)*

**-c, --company**=\<text>   :   nom de la société cible

**-l, --label**=\<text>   :     Label du job

**-t, --targets**=\<text>  :    Liste des cibles (IPv4 ou IPv6) à scanner, *séparées
par le caractère ','*

**-v, --level**=(full | sweep) : Niveau du scan

**-k, --kind**=(IP | Cloud)  :   type de cibles pour le scan

**-a, --params**=\<text>   :    Parametres additionnels (string format JSON)

* (outpost_id : String) 
* (scope : String)
* (restricted\_org\_id : String) 

[Parametres du scan :]

* (provider : String)         
* (credential : String)         
* (region : Array of String)         
* (cluster\_url : String) 

[Options du scan :] 

* (NO\_PING : Boolean) 
* (SESSION\_LOGGING :Boolean) 
* (CLOUD\_HOST_DETECTION : Boolean)
* (SKIP\_IMPLICIT_SCANS : Boolean)
* (VMWARE\_GUEST\_IMPLICIT_SCAN : Boolean)
* (MAX\_START\_SSM_SESSION : Int)
* (MAX\_ACTIVE\_SSM\_SESSION : Int)
* (MAX\_ACTIVE\_TAP\_SESSION : Int)

**--schedule**       :         Enregistre le Job pour une execution périodique
(défaut FALSE)

**--plan**=\<text>     :        Parametres planification (string format JSON)

* (day\_of_month : Array of Int) 
* (day\_of_week : Arrayof String) 
* (duration : Int) 
* (recurrence\_type:String) 
* (start\_minute : Int) 
* (start\_times : Arrayof Int) 
* (week\_def : String)

**-e, --enabled**      :       Autorise l'exécution du job planifié (défaut FALSE)

**-d, --debug**=(off | trace | debug | info | error | fatal | all) :
Niveau du debug

**--version**        :        Show the version and exit

**-h, --help**         :       Show this message and exit

