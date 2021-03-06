package net.slipcor.pvparena.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.managers.Arenas;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * update manager class
 * 
 * -
 * 
 * provides access to update check and methods
 * 
 * @author slipcor
 * 
 * @version v0.8.10
 * 
 */

public class Update extends Thread {

	public static boolean msg = false;
	public static boolean outdated = false;
	public static byte v = -1;

	private static String vOnline;
	private static String vThis;
	private static Plugin plugin;
	private static Debug db = new Debug(6);

	public Update(Plugin p) {
		plugin = p;
	}

	/**
	 * calculate the message variables based on the versions
	 */
	private static void calculateVersions() {
		db.i("calculating versions");
		String[] aOnline = vOnline.split("\\.");
		String[] aThis = vThis.split("\\.");
		outdated = false;

		for (int i = 0; i < aOnline.length && i < aThis.length; i++) {
			try {
				int o = Integer.parseInt(aOnline[i]);
				int t = Integer.parseInt(aThis[i]);
				if (o == t) {
					msg = false;
					continue;
				}
				msg = true;
				outdated = (o > t);
				v = (byte) i;
				message(null);
				return;
			} catch (Exception e) {
				calculateRadixString(aOnline[i], aThis[i], i);
				return;
			}
		}
	}

	/**
	 * calculate a version part based on letters
	 * 
	 * @param sOnline
	 *            the online letter(s)
	 * @param sThis
	 *            the local letter(s)
	 */
	private static void calculateRadixString(String sOnline, String sThis,
			int pos) {
		db.i("calculating including letters");
		try {
			int o = Integer.parseInt(sOnline, 46);
			int t = Integer.parseInt(sThis, 46);
			if (o == t) {
				msg = false;
				return;
			}
			msg = true;
			outdated = (o > t);
			v = (byte) pos;
			message(null);
		} catch (Exception e) {
		}
	}

	/**
	 * colorize a given string based on a char
	 * 
	 * @param s
	 *            the string to colorize
	 * @return a colorized string
	 */
	private static String colorize(String s) {
		if (v == 0) {
			s = ChatColor.RED + s + ChatColor.WHITE;
		} else if (v == 1) {
			s = ChatColor.GOLD + s + ChatColor.WHITE;
		} else if (v == 2) {
			s = ChatColor.YELLOW + s + ChatColor.WHITE;
		} else if (v == 3) {
			s = ChatColor.BLUE + s + ChatColor.WHITE;
		} else {
			s = ChatColor.GREEN + s + ChatColor.WHITE;
		}
		return s;
	}

	/**
	 * message a player if the version is different
	 * 
	 * @param player
	 *            the player to message
	 */
	public static boolean message(Player player) {
		if (player == null || !(player instanceof Player)) {
			if (!msg) {
				Bukkit.getLogger().info(
						"[PVP Arena] You are on latest version!");
			} else {
				if (outdated) {
					Bukkit.getLogger().warning(
							"[PVP Arena] You are using v" + vThis
									+ ", an outdated version! Latest: "
									+ vOnline);
				} else {
					Bukkit.getLogger()
							.warning(
									"[PVP Arena] You are using v"
											+ vThis
											+ ", an experimental version! Latest stable: "
											+ vOnline);
				}
			}
		}
		if (!msg) {
			db.i("version is up to date!");
			return false;
		}

		if (outdated) {
			Arenas.tellPlayer(player, "You are using " + colorize("v" + vThis)
					+ ", an outdated version! Latest: �a" + "v" + vOnline);
		} else {
			Arenas.tellPlayer(player, "You are using " + colorize("v" + vThis)
					+ ", an experimental version! Latest stable: �a" + "v"
					+ vOnline);
		}
		return true;
	}

	@Override
	public void run() {
		db.i("checking for updates");
		if (!plugin.getConfig().getBoolean("updatecheck")) {
			Language.log_info("notupdating");
			return;
		}
		Language.log_info("updating");
		try {
			final URLConnection connection = new URL("http://bukget.org/api/plugin/pvp-arena").openConnection();
	        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        final StringBuffer stringBuffer = new StringBuffer();
	        String line;
	        while ((line = bufferedReader.readLine()) != null)
	            stringBuffer.append(line);
	            bufferedReader.close();
	        JSONParser parser=new JSONParser();
	        Object obj=parser.parse(stringBuffer.toString());
	       
	        JSONArray array=(JSONArray)((JSONObject) obj).get("versions");
	        
	        for (int i = 0 ; i < array.size(); i++) {
	        	JSONObject value = (JSONObject)array.get(i);
	        	String type = (String) value.get("type");
	        	//String link = (String) value.get("dl_link");
	        	if (!type.equalsIgnoreCase("Release")) {
	        		continue;
	        	}
	        	String sOnlineVersion = (String) value.get("name");
				String sThisVersion = plugin.getDescription().getVersion();

				if (sOnlineVersion.toUpperCase().contains("BETA")
						|| sOnlineVersion.toUpperCase().contains("ALPHA")) {
					continue;
				}

				if (sOnlineVersion.contains(" ")) {
					String[] s = sOnlineVersion.split(" ");
					for (int j=0; j< s.length; j++) {
						if (s[j].contains(".")) {
							sOnlineVersion = s[j];
							break;
						}
					}
				}

				vOnline = sOnlineVersion.replace("v", "");
				vThis = sThisVersion.replace("v", "");
				db.i("online version: " + vOnline);
				db.i("local version: " + vThis);

				calculateVersions();
				return;
	        }
	        
		
		} catch (Exception e) {
			
		}
		
		return;
		
		/*
		
		
		

		String pluginUrlString = "http://dev.bukkit.org/server-mods/pvp-arena/files.rss";

		NodeList nodes = null;

		try {
			URL url = new URL(pluginUrlString);
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(url.openConnection().getInputStream());
			doc.getDocumentElement().normalize();
			nodes = doc.getElementsByTagName("item");

		} catch (Exception localException) {
			return;
		}

		for (int i = 0; i < nodes.getLength(); i++) {
			Node selectedFile = nodes.item(i);

			if (selectedFile.getNodeType() == 1) {
				Element firstElement = (Element) selectedFile;
				NodeList firstElementTagName = firstElement
						.getElementsByTagName("title");

				Element firstNameElement = (Element) firstElementTagName
						.item(0);
				NodeList firstNodes = firstNameElement.getChildNodes();

				String sOnlineVersion = firstNodes.item(0).getNodeValue();
				String sThisVersion = plugin.getDescription().getVersion();

				System.out.print(((Element) firstElement.getElementsByTagName("link").item(0)).getChildNodes().item(0).getNodeValue());
				
				if (sOnlineVersion.toUpperCase().contains("BETA")
						|| sOnlineVersion.toUpperCase().contains("ALPHA")) {
					continue;
				}

				while (sOnlineVersion.contains(" ")) {
					String[] s = sOnlineVersion.split(" ");
					sOnlineVersion = s[1];
				}

				vOnline = sOnlineVersion.replace("v", "");
				vThis = sThisVersion.replace("v", "");
				db.i("online version: " + vOnline);
				db.i("local version: " + vThis);

				calculateVersions();
				return;
			}
		}*/
	}

	public void init() {
		if (PVPArena.instance.getConfig().getBoolean("modulecheck", true)) {
			try {
				File destination = PVPArena.instance.getDataFolder();

				File lib = new File(destination, "install.yml");

				System.out.println("Downloading module update file...");
				URL url = new URL(
						"http://www.slipcor.net/public/mc/pafiles/install.yml");
				ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				FileOutputStream output = new FileOutputStream(lib);
				output.getChannel().transferFrom(rbc, 0, 1 << 24);
				System.out.println("Downloaded module update file");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void message(Player p, boolean b) {
		// b = announce update!
		if (!message(p)) {
			if (p != null) {
				Arenas.tellPlayer(p, "[PVP Arena] You are on latest version!");
			}
			return;
		}

		if (p == null) {
			System.out
					.print("http://dev.bukkit.org/server-mods/pvp-arena/files/");
		} else {
			Arenas.tellPlayer(p,
					"http://dev.bukkit.org/server-mods/pvp-arena/files/");
		}
	}
}
