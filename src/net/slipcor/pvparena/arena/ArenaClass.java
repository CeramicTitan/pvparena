package net.slipcor.pvparena.arena;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * arena class class
 * 
 * -
 * 
 * contains arena class methods and variables for quicker access
 * 
 * @author slipcor
 * 
 * @version v0.7.11
 * 
 */

public final class ArenaClass {

	private final String name;
	private final ItemStack[] items;

	// private statics: item definitions
	private static final List<Material> ARMORS_TYPE = new LinkedList<Material>();
	private static final List<Material> HELMETS_TYPE = new LinkedList<Material>();
	private static final List<Material> CHESTPLATES_TYPE = new LinkedList<Material>();
	private static final List<Material> LEGGINGS_TYPE = new LinkedList<Material>();
	private static final List<Material> BOOTS_TYPE = new LinkedList<Material>();

	// static filling of the items array
	static {
		HELMETS_TYPE.add(Material.LEATHER_HELMET);
		HELMETS_TYPE.add(Material.GOLD_HELMET);
		HELMETS_TYPE.add(Material.CHAINMAIL_HELMET);
		HELMETS_TYPE.add(Material.IRON_HELMET);
		HELMETS_TYPE.add(Material.DIAMOND_HELMET);
		HELMETS_TYPE.add(Material.WOOL);

		CHESTPLATES_TYPE.add(Material.LEATHER_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.GOLD_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.CHAINMAIL_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.IRON_CHESTPLATE);
		CHESTPLATES_TYPE.add(Material.DIAMOND_CHESTPLATE);

		LEGGINGS_TYPE.add(Material.LEATHER_LEGGINGS);
		LEGGINGS_TYPE.add(Material.GOLD_LEGGINGS);
		LEGGINGS_TYPE.add(Material.CHAINMAIL_LEGGINGS);
		LEGGINGS_TYPE.add(Material.IRON_LEGGINGS);
		LEGGINGS_TYPE.add(Material.DIAMOND_LEGGINGS);

		BOOTS_TYPE.add(Material.LEATHER_BOOTS);
		BOOTS_TYPE.add(Material.GOLD_BOOTS);
		BOOTS_TYPE.add(Material.CHAINMAIL_BOOTS);
		BOOTS_TYPE.add(Material.IRON_BOOTS);
		BOOTS_TYPE.add(Material.DIAMOND_BOOTS);

		ARMORS_TYPE.addAll(HELMETS_TYPE);
		ARMORS_TYPE.addAll(CHESTPLATES_TYPE);
		ARMORS_TYPE.addAll(LEGGINGS_TYPE);
		ARMORS_TYPE.addAll(BOOTS_TYPE);
	}
	
	public static void equip(Player player, ItemStack[] items) {
		for (ItemStack item : items) {
			if (ARMORS_TYPE.contains(item.getType())) {
				equipArmor(item, player.getInventory());
			} else {
				player.getInventory().addItem(new ItemStack[] { item });
			}
		}
	}

	/**
	 * equip an armor item to the respective slot
	 * 
	 * @param stack
	 *            the item to equip
	 * @param inv
	 *            the player's inventory
	 */
	private static void equipArmor(ItemStack stack, PlayerInventory inv) {
		Material type = stack.getType();
		if (HELMETS_TYPE.contains(type)) {
			inv.setHelmet(stack);
		} else if (CHESTPLATES_TYPE.contains(type)) {
			inv.setChestplate(stack);
		} else if (LEGGINGS_TYPE.contains(type)) {
			inv.setLeggings(stack);
		} else if (BOOTS_TYPE.contains(type)) {
			inv.setBoots(stack);
		}
	}

	public ArenaClass(String className, ItemStack[] classItems) {
		this.name = className;
		this.items = classItems.clone();
	}

	public String getName() {
		return name;
	}

	public void load(Player player) {
		for (int i = 0; i < items.length; ++i) {
			ItemStack stack = items[i];
			if (ARMORS_TYPE.contains(stack.getType())) {
				equipArmor(stack, player.getInventory());
			} else {
				player.getInventory().addItem(new ItemStack[] { stack });
			}
		}
	}
	
	
}