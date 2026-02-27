package TiXYA2357.ParticleAdorn.UI;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryTransactionEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;

import static TiXYA2357.ParticleAdorn.PlayerI.getOrDefaultPlayerI;
import static TiXYA2357.ParticleAdorn.Utils.Delayed;


public class CMListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryTransaction(InventoryTransactionEvent event) {
        event.getTransaction().getActions().forEach(action -> {
            if (action instanceof SlotChangeAction slotChange) {
                if (slotChange.getInventory() instanceof FakeInventoryChest) {
                    var inventory = (AbstractFakeInventory) slotChange.getInventory();
                    if (inventory.isBag()) return;
                    inventory.setEvent(event);
                    var slot = slotChange.getSlot();
                    if (inventory.getSlots().contains(slot)) {
                        Delayed(() -> inventory.getRs().get(slot).run(),2,inventory.isAsync());
                        if (inventory.isAutoClose()) inventory.close(inventory.getPlayer());
                    }event.setCancelled(true);}
            }});}

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        var player = event.getPlayer();
        getOrDefaultPlayerI(player.getUniqueId().toString()).save();
    }
}