<?php
/*************************************/
/*******       VARIABLE       ********/
/*************************************/
define('TITLE', 'Aresia-Games');
error_reporting(E_ERROR | E_PARSE);
/** URL **/
	define('URL_SITE', 'http://' . $_ENV["URL"]);
	
	/** Réseaux sociaux **/
	define('URL_TWITTER', '#');
	define('URL_FACEBOOK', '#');
		define('NAME_FACEBOOK', '#');
	define('URL_GOOGLE', '#');
	
	/** Autres **/
	define('URL_RPG', 'http://www.rpg-paradize.com/?page=vote&vote=43742');
	define('URL_FORUM', 'http://127.0.0.1/forum/');
	define('URL_BARBOK', 'https://barbok.eratz.fr/');
	define('URL_TEAMSPEAK', 'ts3server://aestia.fr?port=9987');
	define('URL_RSS_NEWS_IPB', 'http://foremu.fr/forum/index.php?app=core&module=global&section=rss&type=forums&id=6');
								

	//Http://www.***.**/... neccésaire dans l'URL ( compatibilité Firefox ).
	define('URL_LAUNCHER_1_29', 'https://www.mediafire.com/file/80a8kvokdptaltq/Dofus_1.29.zip/file');
	define('URL_INSTALLATEUR', '#');
	define('URL_CONFIG', '#');
/** Serveurs **/
	define('REQUEST_TIMEOUT', '1');
		/** Variables **/
	define('DB_IP', '172.28.1.10');
	define('DB_NAME', 'login');
	define('DB_USER', 'root');
	define('DB_PASS', 'password');
	/** Serveur login **/
	define('LOGIN_IP', '172.28.1.12');
	define('LOGIN_PORT', '450');
	define('LOGIN_DB_NAME', 'login');
	define('LOGIN_DB_USER', 'root');
	define('LOGIN_DB_PASS', 'password');
	$login = newPdo(DB_IP, LOGIN_DB_USER, LOGIN_DB_PASS, LOGIN_DB_NAME);
	/** Serveur jiva **/
	define('JIVA_IP', '172.28.1.15');
	define('JIVA_PORT', '5555');
	define('JIVA_DB_NAME', 'game');
	define('JIVA_DB_USER', 'root');
	define('JIVA_DB_PASS', 'password');
	$jiva = newPdo(DB_IP, JIVA_DB_USER, JIVA_DB_PASS, JIVA_DB_NAME);
/** Shop **/
	define('PTS_PER_VOTE', '10');
	define('CLOUDFLARE_ENABLE', false);
	
/** Mysql **/

	$connection = newPdo(DB_IP, DB_USER, DB_PASS, DB_NAME);
	
	/** Fonction **/ 
	function newPdo($ip, $user, $pass, $db) {
		try {
			$options[PDO::ATTR_ERRMODE] = PDO::ERRMODE_EXCEPTION;
			$connection = new PDO('mysql:host=' . $ip . ';dbname=' . $db, $user, $pass, $options);  
			$connection -> exec('SET NAMES utf8');    
			return $connection;
		} catch(Exception $e) {
			die('Error : ' . $e -> getMessage());
		}	
	}

/** Don't tuch **/
define('PAGE_WITHOUT_RIGHT_MENU', 'signin register');
	
/*************************************/
/*******       FUNCTION       ********/
/*************************************/

function checkState($ip, $port) {
	$socket = @fsockopen($ip, $port, $errno, $errstr, REQUEST_TIMEOUT);
	if($socket >= 1)
		return true;
	return false;
}

function convertDateToString($date) {
	$split = explode("-", $date);
	$month = "Unknow";
	
	switch((int) $split[1]) {
		case 1: $month = "Janvier"; break;
		case 2: $month = "Février"; break;
		case 3: $month = "Mars"; break;
		case 4: $month = "Avril"; break;
		case 5: $month = "Mai"; break;
		case 6: $month = "Juin"; break;
		case 7: $month = "Juillet"; break;
		case 8: $month = "Août"; break;
		case 9: $month = "Septembre"; break;
		case 10: $month = "Octobre"; break;
		case 11: $month = "Novembre"; break;
		case 12: $month = "Décembre"; break;
	}
	
	return $split[2] . " " . $month . ", " . $split[0];
}

function convertDateEnToFr($date) {
	$split = explode(" ", $date);
	$month = "Unknow";

	switch(strtoupper($split[1])) {
		case "DEC": return str_replace("Dec", "Décembre", $date);
		case "JAN":
		case "FEV":
		case "MAR":
		case "AVR":
		case "MAI":
		case "JUI":
		case "JUI":
		case "":
		case "":
		case "":
		return "";
		
	}
	return "";
}

function convertTimestampToUptime($time) {
	$jour = '0';
	$heure = '0';
	$min = '0';
	$time1 = '';
	$time2 = '';
	$time3 = '';
	if($time > 86399) {
		$jour = intval($time/86400);
		$time = $time - ($jour * 86400);
	}
	if($time > 3599) {
		$heure = intval($time/3600);
		$time = $time - ($heure * 3600);
	}
	if($time > 59) {
		$min = intval($time/60);
		$time = $time - ($min * 60);
	}
	if($jour > 100)
	return "0j 0h 0m";
	
	return $jour."j ".$heure."h ".$min."m";
}

function convertClassIdToString($id, $sexe) {
	switch($id) {
	case 1: return ($sexe == 0 ? "Féca" : "Fécatte");
	case 2: return ($sexe == 0 ? "Osanamodas" : "Osamodas");
	case 3: return ($sexe == 0 ? "Enutrof" : "Enutrof");
	case 4: return ($sexe == 0 ? "Sram" : "Sramette");
	case 5: return ($sexe == 0 ? "Xélor" : "Xélor");
	case 6: return ($sexe == 0 ? "Ecaflip" : "Ecaflip");
	case 7: return ($sexe == 0 ? "Eniripsa" : "Eniripsa");
	case 8: return ($sexe == 0 ? "Iop" : "Iopette");
	case 9: return ($sexe == 0 ? "Crâ" : "Crâtte");
	case 10: return ($sexe == 0 ? "Sadida" : "Sadida");
	case 11: return ($sexe == 0 ? "Sacrieur" : "Sacrieuse");
	case 12: return ($sexe == 0 ? "Pandawa" : "Pandawa");
	}
	return "Unknow";
}

function checkString($data) {
	if(!preg_match("#[a-zA-Z0-9ÀÁÂÃÄÅàáâãäåÒÓÔÕÖØòóôõöøÈÉÊËèéêëÇçÌÍÎÏìíîïÙÚÛÜùúûüÿÑñ_-]$#", $data)) 
		return true;
	return false;
}

function parseDate($date) {
	$array = explode("~", $date);
	return "le " . $array[2] . "/" . $array[1] . "/" . $array[0] . " à " . $array[3] . "h" . $array[4];
}

function array_sort($array, $on, $order = SORT_ASC) {
    $new_array = array();
    $sortable_array = array();

    if(count($array) > 0) {
        foreach ($array as $k => $v) {
            if (is_array($v)) {
                foreach ($v as $k2 => $v2) {
                    if ($k2 == $on) {
                        $sortable_array[$k] = $v2;
                    }
                }
            } else {
                $sortable_array[$k] = $v;
            }
        }

        switch ($order) {
            case SORT_ASC:
                asort($sortable_array);
            break;
            case SORT_DESC:
                arsort($sortable_array);
            break;
        }

        foreach ($sortable_array as $k => $v) {
            $new_array[$k] = $array[$k];
        }
    }

    return $new_array;
}

function convertStatsToString($data) {
	$stats = explode(",", $data);
	$value = "";
	for($i = 0; $i < count($stats); $i++) {
		$array = explode("#", $stats[$i]);
    	$id = $array[0];
		$de = hexdec($array[1]);
		$a = hexdec($array[2]);
		
		$x = convertStatsId($id);
		
		$suffix = explode(";", $x);
		$signe = explode(";", $x);
		if ($x == ";")
			continue;
			
		if($a == 0)
			echo $signe[1]." $de ".$suffix[0]." <br>" ;
		else
			echo $signe[1]." $de à $a ".$suffix[0]." <br>" ;
	}
	return "";
}

function convertStatsId($id) {
	$suffix = "";
	$signe = "";
	switch($id)	{
		case '99':
		$suffix = ' Vitalité.';
		$signe = '- ';
		break;
		case '9d':
		$suffix = ' Terre.';
		$signe = '- ';
		break;
		case '9b':
		$suffix = ' Feu.';
		$signe = '- ';
		break;
		case '9a':
		$suffix = ' Air.';
		$signe = '- ';
		break;
		case '98':
		$suffix = ' Eau.';
		$signe = '- ';
		break;
		case '7d':
		$suffix = ' Vitalité.';
		$signe = '+ ';
		break;
		case '7c':
		$suffix = ' Sagesse.';
		$signe = '+ ';
		break;
		case '76':
		$suffix = ' Terre.';
		$signe = '+ ';
		break;
		case '7e':
		$suffix = ' Feu.';
		$signe = '+ ';
		break;
		case '77':
		$suffix = ' Air.';
		$signe = '+ ';
		break;
		case '7b':
		$suffix = ' Eau.';
		$signe = '+ ';
		break;
		case '6f':
		$suffix = ' Pa.';
		$signe = '+ ';
		break;
		case '65':
		$suffix = ' Pa.';
		$signe = '- ';
		break;
		case '80':
		$suffix = ' Pm.';
		$signe = '+ ';
		break;
		case '7f':
		$suffix = ' Pm.';
		$signe = '- ';
		break;
		case '75':
		$suffix = ' Po.';
		$signe = '+ ';
		break;
		case '74':
		$suffix = ' Po.';
		$signe = '- ';
		break;
		case '70':
		$suffix = ' Dommage.';
		$signe = '+ ';
		break;
		case '91':
		$suffix = ' Dommage.';
		$signe = '- ';
		break;
		case '8a':
		$suffix = ' Dommage (%).';
		$signe = '+ ';
		break;
		case 'ba':
		$suffix = ' Dommage (%).';
		$signe = '- ';
		break;
		case 'dc':
		$suffix = ' Dommage renvoyé.';
		$signe = '+ ';
		break;
		case 'b2':
		$suffix = ' Soins.';
		$signe = '+ ';
		break;
		case 'b3':
		$suffix = ' Soins.';
		$signe = '- ';
		break;
		case '73':
		$suffix = ' Coup Critique.';
		$signe = '+ ';
		break;
		case '7a':
		$suffix = ' Echec Critique.';
		$signe = '+ ';
		break;
		case 'b6':
		$suffix = ' Invocation.';
		$signe = '+ ';
		break;
		case '9e':
		$suffix = ' Pod.';
		$signe = '+ ';
		break;
		case '9f':
		$suffix = ' Pod.';
		$signe = '- ';
		break;
		case 'ae':
		$suffix = ' Initiative.';
		$signe = '+ ';
		break;
		case 'af':
		$suffix = ' Initiative.';
		$signe = '- ';
		break;
		case 'b0':
		$suffix = ' Prospection.';
		$signe = '+ ';
		break;
		case 'b1':
		$suffix = ' Prospection.';
		$signe = '- ';
		break;
		case 'e1':
		$suffix = ' Piège.';
		$signe = '+ ';
		break;
		case 'e2':
		$suffix = ' Piège (%).';
		$signe = '+ ';
		break;
		case 'a0':
		$suffix = ' Esquive perte Pa (%).';
		$signe = '+ ';
		break;
		case 'aa2':
		$suffix = ' Esquive perte Pa (%).';
		$signe = '- ';
		break;
		case 'a1':
		$suffix = ' Esquive perte Pm (%).';
		$signe = '+ ';
		break;
		case 'a3':
		$suffix = ' Esquive perte Pm (%).';
		$signe = '- ';
		break;
		case 'f4':
		$suffix = ' Résistance Neutre.';
		$signe = '+ ';
		break;
		case 'f9':
		$suffix = ' Résistance Neutre.';
		$signe = '- ';
		break;
		case 'f0':
		$suffix = ' Résistance Terre.';
		$signe = '+ ';
		break;
		case 'f5':
		$suffix = ' Résistance Terre.';
		$signe = '- ';
		break;
		case 'f3':
		$suffix = ' Résistance Feu.';
		$signe = '+ ';
		break;
		case 'f8':
		$suffix = ' Résistance Feu.';
		$signe = '- ';
		break;
		case 'f2':
		$suffix = ' Résistance Air.';
		$signe = '+ ';
		break;
		case 'f7':
		$suffix = ' Résistance Air.';
		$signe = '- ';
		break;
		case 'f1':
		$suffix = ' Résistance Eau.';
		$signe = '+ ';
		break;
		case 'f6':
		$suffix = ' Résistance Eau.';
		$signe = '- ';
		break;
		case 'd6':
		$suffix = ' Résistance Neutre (%).';
		$signe = '+ ';
		break;
		case 'db':
		$suffix = ' Résistance Neutre (%).';
		$signe = '- ';
		break;
		case 'd2':
		$suffix = ' Résistance Terre (%).';
		$signe = '+ ';
		break;
		case 'd7':
		$suffix = ' Résistance Terre (%).';
		$signe = '- ';
		break;
		case 'd5':
		$suffix = ' Résistance Feu (%).';
		$signe = '+ ';
		break;
		case 'da':
		$suffix = ' Résistance Feu (%).';
		$signe = '- ';
		break;
		case 'd4':
		$suffix = ' Résistance Air (%).';
		$signe = '+ ';
		break;
		case 'd9':
		$suffix = ' Résistance Air (%).';
		$signe = '- ';
		break;
		case 'd3':
		$suffix = ' Résistance Eau (%).';
		$signe = '+ ';
		break;
		case 'd8':
		$suffix = ' Résistance Eau (%).';
		$signe = '- ';
		break;
		case 'fe':
		$suffix = ' Résistance Neutre face aux combattants (%).';
		$signe = '+ ';
		break;
		case '103':
		$suffix = ' Résistance Neutre face aux combattants (%).';
		$signe = '- ';
		break;
		case 'fa':
		$suffix = ' Résistance Terre face aux combattants (%).';
		$signe = '+ ';
		break;
		case 'ff':
		$suffix = ' Résistance Terre face aux combattants (%).';
		$signe = '- ';
		break;
		case 'fd':
		$suffix = ' Résistance Feu face aux combattants (%).';
		$signe = '+ ';
		break;
		case '102':
		$suffix = ' Résistance Feu face aux combattants (%).';
		$signe = '- ';
		break;
		case 'fb':
		$suffix = ' Résistance Eau face aux combattants (%).';
		$signe = '+ ';
		break;
		case '100':
		$suffix = ' Résistance Eau face aux combattants (%).';
		$signe = '- ';
		break;
		case 'fc':
		$suffix = ' Résistance Air face aux combattants (%).';
		$signe = '+ ';
		break;
		case '101':
		$suffix = ' Résistance Air face aux combattants (%).';
		$signe = '- ';
		break;
		case '108':
		$suffix = ' Résistance Neutre face aux combattants.';
		$signe = '+ ';
		break;
		case '104':
		$suffix = ' Résistance Terre face aux combattants.';
		$signe = '+ ';
		break;
		case '107':
		$suffix = ' Résistance Feu face aux combattants.';
		$signe = '+ ';
		break;
		case '105':
		$suffix = ' Résistance Eau face aux combattants.';
		$signe = '+ ';
		break;
		case '106':
		$suffix = ' Résistance Air face aux combattants.';
		$signe = '+ ';
		break;
		case '8b':
        $suffix = ' Energie.';
        $signe = '+';
        break;
		default:
		$suffix = '';
		$signe = '';
		break;
	}
	return $suffix.";".$signe;
}
?>
