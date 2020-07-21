import minecraft

def arrow_hit(event):
    entity = event.getEntity()
    if entity.getType().toString() == "ARROW":
        entity.getWorld().createExplosion(entity.getLocation(), 4.0)
        entity.remove()

minecraft.on_event("ProjectileHitEvent", arrow_hit)