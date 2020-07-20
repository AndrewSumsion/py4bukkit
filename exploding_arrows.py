import minecraft

def arrow_hit(event):
    entity = event.getEntity()
    if entity.getType().toString() == "ARROW":
        entity.getWorld().createExplosion(entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), 4.0)

minecraft.on_event("ProjectileHitEvent", arrow_hit)