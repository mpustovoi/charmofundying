/*
 * Copyright (C) 2019-2022 Illusive Soulworks
 *
 * Charm of Undying is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Charm of Undying is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with Charm of Undying.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.charmofundying.platform;

import com.illusivesoulworks.charmofundying.CharmOfUndyingConstants;
import com.illusivesoulworks.charmofundying.common.TotemProviders;
import com.illusivesoulworks.charmofundying.common.network.SPacketUseTotem;
import com.illusivesoulworks.charmofundying.platform.services.IPlatform;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PlayerLookup;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public class QuiltPlatform implements IPlatform {

  @Override
  public ItemStack findTotem(LivingEntity livingEntity) {
    return TrinketsApi.getTrinketComponent(livingEntity).map(component -> {
      List<Tuple<SlotReference, ItemStack>> res =
          component.getEquipped(stack -> TotemProviders.IS_TOTEM.test(stack.getItem()));
      return res.size() > 0 ? res.get(0).getB() : ItemStack.EMPTY;
    }).orElse(ItemStack.EMPTY);
  }

  @Override
  public String getRegistryName(Item item) {
    return BuiltInRegistries.ITEM.getKey(item).toString();
  }

  @Override
  public boolean isModLoaded(String name) {
    return QuiltLoader.isModLoaded(name);
  }

  @Override
  public void broadcastTotemEvent(LivingEntity livingEntity) {
    FriendlyByteBuf buf = PacketByteBufs.create();
    SPacketUseTotem.encode(new SPacketUseTotem(livingEntity.getId()), buf);

    for (ServerPlayer serverPlayer : PlayerLookup.tracking(livingEntity)) {
      ServerPlayNetworking.send(serverPlayer, CharmOfUndyingConstants.TOTEM_EVENT, buf);
    }

    if (livingEntity instanceof ServerPlayer serverPlayer) {
      ServerPlayNetworking.send(serverPlayer, CharmOfUndyingConstants.TOTEM_EVENT, buf);
    }
  }
}
