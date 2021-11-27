<?php 
header('Content-Type: text/html;charset=UTF-8');
echo '<?xml version="1.0" encoding="UTF-8"?>';

define("DB_HOST",  "front-ha-mysql-01.shpv.fr");
define("DB_LOGIN", "agsswoqe");
define("DB_PASS",  "3EFo6awk14");
define("DB_BDD",   "agsswoqe_00---login");
 
mysql_connect(DB_HOST,DB_LOGIN,DB_PASS); 
mysql_select_db(DB_BDD);
mysql_query("SET NAMES UTF8");

$query = mysql_query("SELECT * FROM `client_rss_news` ORDER BY `id` DESC LIMIT 0, 6") or die(mysql_error());

function suppr_accents($chaine) {
	$accents = array('&agrave;','&eacute;','&egrave;','&icirc;','&iuml;','&ocirc;','&ugrave;','&ecirc;');
	$sans = array('à','é','è','î','ï','ô','ù','ê');
	return str_replace($accents, $sans, $chaine);
} ?>

<rss version="2.0">
	<channel>
		<title>Aresia-Games</title>
		<link>http://aresia-games.com/</link>
		<description>Serveur Semi'like</description>
		<?php 
		while($data=mysql_fetch_assoc($query)){
			echo "<item>\n";
			echo "<guid>" . $data["id"] . "</guid>";
			echo "<title>" . suppr_accents($data["title"]) . "</title>\n";
			echo "<link>" . $data["link"] . "</link>\n";
			echo "<icon>News_" . $data["icon"] . "</icon>\n";
			echo "<pubDate>" . date("D, d M Y H:i:s", strtotime($data["date"])) . " +0200</pubDate>\n";
			echo "</item>\n";
		} ?>
	</channel>
</rss>
