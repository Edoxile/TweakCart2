/*
 * Copyright (c) 2012.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.tweakcraft.tweakcart.listeners;

import net.tweakcraft.tweakcart.api.event.*;
import net.tweakcraft.tweakcart.api.model.CartType;
import net.tweakcraft.tweakcart.api.model.TweakCartEvent;
import net.tweakcraft.tweakcart.api.util.TweakPermissionsManager;
import net.tweakcraft.tweakcart.model.Direction;
import net.tweakcraft.tweakcart.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class TweakCartVehicleListener implements Listener {
    private TweakPluginManager manager = TweakPluginManager.getInstance();
    private TweakPermissionsManager permissionsManager = TweakPermissionsManager.getInstance();


    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            Minecart minecart = (Minecart) event.getVehicle();
            if (!MathUtil.isSameBlock(event.getFrom(), event.getTo())) {
                Block toBlock = event.getTo().getBlock();
                Direction cartDriveDirection = Direction.getDirection(event.getFrom(), event.getTo());
                manager.callEvent(TweakCartEvent.Block.VehicleBlockChangeEvent, new TweakVehicleBlockChangeEvent(minecart, cartDriveDirection, toBlock));
                if (toBlock.getType() == Material.DETECTOR_RAIL) {
                    manager.callEvent(TweakCartEvent.Block.VehicleBlockDetectEvent, new TweakVehicleBlockDetectEvent(minecart, cartDriveDirection, toBlock, CartType.getCartType(minecart)));
                }
                //Signs worden niet door blockCollision gevonden. 'collision checks' hier.
                if (!BlockUtil.isRailBlock(toBlock)) {
                    switch (toBlock.getType()) {
                        case WALL_SIGN:
                        case SIGN_POST:
                            Sign signBlock = (Sign) toBlock.getState();
                            String keyword = signBlock.getLine(0);
                            manager.callEvent(TweakCartEvent.Sign.VehicleCollidesWithSignEvent, new TweakVehicleCollidesWithSignEvent(minecart, cartDriveDirection, signBlock, keyword));
                            break;
                    }
                } else {
                    Set<Sign> signBlockList;
                    if ((signBlockList = BlockUtil.getSignLocationAround(toBlock, cartDriveDirection)).size() != 0) {
                        //Woei, we hebben bordjes gevonden
                        for (Sign sign : signBlockList) {
                            String keyword = sign.getLine(0);
                            //TODO: fix the null
                            manager.callEvent(TweakCartEvent.Sign.VehiclePassesSignEvent, new TweakVehiclePassesSignEvent(minecart, cartDriveDirection, sign, keyword));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onVehicleBlockCollision(VehicleBlockCollisionEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            Minecart cart = (Minecart) event.getVehicle();
            Block block = event.getBlock();
            Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "BlockType: " + block.getType());
            Direction direction = Direction.getDirection(event.getVehicle().getLocation(), event.getBlock().getLocation());
            if (event.getBlock().getTypeId() == Material.DISPENSER.getId()) {
                TweakVehicleCollectEvent collectEvent = new TweakVehicleCollectEvent(cart, block);
                permissionsManager.cartCanCollect(collectEvent);
                if (!collectEvent.isCancelled()) {
                    if (cart instanceof StorageMinecart) {
                        ItemStack[] leftovers = ((StorageMinecart) cart).getInventory().getContents();
                        Location dropLocation = cart.getLocation();
                        for (ItemStack i : leftovers) {
                            if (i != null) {
                                cart.getWorld().dropItem(dropLocation, i);
                            }
                        }
                    }
                    ItemStack itemStack = new ItemStack(VehicleUtil.itemId(cart), 1);
                    Dispenser dispenser = (Dispenser) block.getState();
                    ItemStack[] leftovers = InventoryManager.putContents(dispenser.getInventory(), itemStack);
                    if (leftovers[0] == null) {
                        cart.remove();
                    }
                }
            } else {
                manager.callEvent(TweakCartEvent.Block.VehicleBlockCollisionEvent, new TweakVehicleBlockCollisionEvent(cart, direction, block));
            }
        }
    }

}
